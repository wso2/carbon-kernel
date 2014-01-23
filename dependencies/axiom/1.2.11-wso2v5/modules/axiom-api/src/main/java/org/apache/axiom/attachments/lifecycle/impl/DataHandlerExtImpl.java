/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axiom.attachments.lifecycle.impl;

import java.io.File;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import org.apache.axiom.attachments.CachedFileDataSource;
import org.apache.axiom.attachments.lifecycle.DataHandlerExt;
import org.apache.axiom.attachments.lifecycle.LifecycleManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DataHandlerExtImpl extends DataHandler implements
		DataHandlerExt, Observer {

	private static final Log log = LogFactory.getLog(DataHandlerExtImpl.class);
	private DataHandler dataHandler = null;
	private LifecycleManager manager = null;
	private static int READ_COUNT = 1;
	private boolean deleteOnreadOnce = false;
	public DataHandlerExtImpl(DataHandler dataHandler, LifecycleManager manager){		
		super(dataHandler.getDataSource());
		this.dataHandler = dataHandler;
		this.manager = manager;
	}

	public void deleteWhenReadOnce() throws IOException {
		deleteOnreadOnce = true;
		FileAccessor fa =manager.getFileAccessor(getName());
		if(fa==null){
			log.warn("Could not find FileAccessor, delete on readOnce Failed");				
			return;
		}
		if(fa.getAccessCount() >= READ_COUNT){
			purgeDataSource();
		}else{
			fa.addObserver(this);
		}			
	}

	public void purgeDataSource() throws IOException {
		if(log.isDebugEnabled()){
			log.debug("Start purgeDataSource");
		}
		File file = getFile();		
		if(file!=null){
			//Invoke delete from LifecycleManager
			manager.delete(file);
			//If file was registered with VMShutdown hook
			//lets remove it from the list to be deleted on VMExit.
			VMShutdownHook hook =VMShutdownHook.hook();
			if(hook.isRegistered()){
				hook.remove(file);
			}
			if(log.isDebugEnabled()){
				log.debug("File Purged and removed from Shutdown Hook Collection");
			}
		}else{
			if(log.isDebugEnabled()){
				log.debug("DataSource is not a CachedFileDataSource, Unable to Purge.");
			}
		}
		
		if(log.isDebugEnabled()){
			log.debug("End purgeDataSource");
		}
	}
	
	public void update(Observable o, Object arg) {
		try{
			if(log.isDebugEnabled()){
				log.debug("Start update in Observer");
			}
			if(o instanceof FileAccessor){
				FileAccessor fa = (FileAccessor)o;
				if(deleteOnreadOnce && fa.getAccessCount()>=READ_COUNT){
					purgeDataSource();
				}
			}						
		}catch(IOException e){
			if(log.isDebugEnabled()){
				log.debug("delete on readOnce Failed");
			}
			log.warn("delete on readOnce Failed with IOException in Observer"+e.getMessage());
		}
		if(log.isDebugEnabled()){
			log.debug("End update in Observer");
		}
	}

	private File getFile(){
		//get DataSource from DataHandler
		DataSource dataSource = dataHandler.getDataSource();
		if(dataSource instanceof CachedFileDataSource){
			CachedFileDataSource cds = (CachedFileDataSource)dataSource;
			//get the file object from data source.
			return cds.getFile();
		}
		return null;
	}	
}
