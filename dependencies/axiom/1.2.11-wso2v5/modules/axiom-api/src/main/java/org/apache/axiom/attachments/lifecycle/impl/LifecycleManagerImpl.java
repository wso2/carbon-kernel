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
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Hashtable;

import org.apache.axiom.attachments.lifecycle.LifecycleManager;
import org.apache.axiom.util.UIDGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LifecycleManagerImpl implements LifecycleManager {
    private static final Log log = LogFactory.getLog(LifecycleManagerImpl.class);

    //Hashtable to store file accessors.
    private static Hashtable table = new Hashtable();
    private VMShutdownHook hook = null;
    public LifecycleManagerImpl() {
        super(); 
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.lifecycle.LifecycleManager#create(java.lang.String)
     */
    public FileAccessor create(String attachmentDir) throws IOException {
        if(log.isDebugEnabled()){
            log.debug("Start Create()");
        }
        File file = null;
        File dir = null;
        if (attachmentDir != null) {
            dir = new File(attachmentDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("Given Axis2 Attachment File Cache Location "
                + dir + "  should be a directory.");
        }
        // Generate unique id.  The UID generator is used so that we can limit
        // synchronization with the java random number generator.
        String id = UIDGenerator.generateUID();

        String fileString = "Axis2" + id + ".att";
        file = new File(dir, fileString);
        FileAccessor fa = new FileAccessor(this, file);
        //add the fileAccesor to table
        table.put(fileString, fa);
        //Default behaviour
        deleteOnExit(file);
        if(log.isDebugEnabled()){
            log.debug("End Create()");
        }
        return fa;
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.lifecycle.LifecycleManager#delete(java.io.File)
     */
    public void delete(File file) throws IOException {
        if(log.isDebugEnabled()){
            log.debug("Start delete()");
        }

        if(file!=null && file.exists()){
            table.remove(file);
            if(log.isDebugEnabled()){
                log.debug("invoking file.delete()");
            }

            if(file.delete()){
                if(log.isDebugEnabled()){
                    log.debug("delete() successful");
                }
            }else{
                if(log.isDebugEnabled()){
                    log.debug("Cannot delete file, set to delete on VM shutdown");
                }
                deleteOnExit(file);
            }
        }
        if(log.isDebugEnabled()){
            log.debug("End delete()");
        }
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.lifecycle.LifecycleManager#deleteOnExit(java.io.File)
     */
    public void deleteOnExit(File file) throws IOException {
        if(log.isDebugEnabled()){
            log.debug("Start deleteOnExit()");
        }
        if(hook == null){
            hook = RegisterVMShutdownHook();
        }

        if(file!=null){
            if(log.isDebugEnabled()){
                log.debug("Invoking deleteOnExit() for file = "+file.getAbsolutePath());
            }
            hook.add(file);
            table.remove(file);
        }
        if(log.isDebugEnabled()){
            log.debug("End deleteOnExit()");
        }
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.lifecycle.LifecycleManager#deleteOnTimeInterval(int)
     */
    public void deleteOnTimeInterval(int interval, File file) throws IOException {
        if(log.isDebugEnabled()){
            log.debug("Start deleteOnTimeInterval()");
        }

        Thread t = new Thread(new LifecycleManagerImpl.FileDeletor(interval, file));
        t.setDaemon(true);
        t.start();
        if(log.isDebugEnabled()){
            log.debug("End deleteOnTimeInterval()");
        }
    }

    private VMShutdownHook RegisterVMShutdownHook() throws RuntimeException{
        if(log.isDebugEnabled()){
            log.debug("Start RegisterVMShutdownHook()");
        }
        try{
            hook = (VMShutdownHook)AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws SecurityException, IllegalStateException, IllegalArgumentException {
                    VMShutdownHook hook = VMShutdownHook.hook();
                    if(!hook.isRegistered()){
                        Runtime.getRuntime().addShutdownHook(hook);
                        hook.setRegistered(true);
                    }
                    return hook;
                }
            });
        }catch (PrivilegedActionException e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception thrown from AccessController: " + e);
                log.debug("VM Shutdown Hook not registered.");
            }
            throw new RuntimeException(e);
        }
        if(log.isDebugEnabled()){
            log.debug("Exit RegisterVMShutdownHook()");
        }
        return hook;
    }

    public static class FileDeletor implements Runnable{
        int interval;
        File _file;

        public FileDeletor(int interval, File file) {
            super();
            this.interval = interval;
            this._file = file;           
        }

        public void run() {
            try{
                Thread.sleep(interval*1000);
                if(_file.exists()){
                    table.remove(_file);
                    _file.delete();
                }
            }catch(InterruptedException e){
                //Log Exception
                if(log.isDebugEnabled()){
                    log.warn("InterruptedException occured "+e.getMessage());
                }
            }
        }        
    }

	public FileAccessor getFileAccessor(String fileName) throws IOException {		
		return (FileAccessor)table.get(fileName);
	}

}


