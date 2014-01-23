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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Observable;

import javax.activation.DataHandler;
import javax.mail.MessagingException;

import org.apache.axiom.attachments.CachedFileDataSource;
import org.apache.axiom.attachments.lifecycle.LifecycleManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * FileAccessor wraps the attachment temp file. It is created from PartOnFile.
 * The idea behind wrapping the file is to give rumtime an ability to track
 * when the file is accessed with streams or data handler  and accordingly trigger
 * events to handle the the files lifecycle.
 *
 */
public class FileAccessor extends Observable{
    private static final Log log = LogFactory.getLog(FileAccessor.class);
    File file = null;
    LifecycleManager manager;
    private int accessCount = 0;
    public FileAccessor(LifecycleManager manager, File file) {
        super();
        this.manager = manager;
        this.file = file;   
    }

    public DataHandler getDataHandler(String contentType) throws MessagingException {
        if(log.isDebugEnabled()){
            log.debug("getDataHandler()");
            log.debug("accessCount =" +accessCount);
        }
        CachedFileDataSource dataSource = new CachedFileDataSource(file);
        dataSource.setContentType(contentType);
       	accessCount++;
       	setChanged();
       	notifyObservers();
       	DataHandler dataHandler = new DataHandler(dataSource);
       	return new DataHandlerExtImpl(dataHandler, manager);        
    }

    public String getFileName() throws MessagingException {
        if(log.isDebugEnabled()){
            log.debug("getFileName()");
        }
        return file.getAbsolutePath();
    }

    public InputStream getInputStream() throws IOException, MessagingException {
        if(log.isDebugEnabled()){
            log.debug("getInputStream()");
        }
        return new FileInputStream(file);
    }

    public OutputStream getOutputStream() throws FileNotFoundException{
        if(log.isDebugEnabled()){
            log.debug("getOutputStream()");
        }
        return new FileOutputStream(file);
    }

    public long getSize() {
        return file.length();
    }
    
    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

	public int getAccessCount() {
		return accessCount;
	}

}
