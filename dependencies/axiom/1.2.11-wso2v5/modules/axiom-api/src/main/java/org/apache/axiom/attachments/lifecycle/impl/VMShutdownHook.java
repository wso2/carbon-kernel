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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/*
 * VMShutdown Hook will be registered with Runtime object to be invoked 
 * when Virutal Machine is shutdown.
 * This class will be used to delete any cached attachments file that where
 * added by runtime to be deleted on VM shutdown.
 */
public class VMShutdownHook extends Thread {
    private static final Log log = LogFactory.getLog(VMShutdownHook.class);
    private static VMShutdownHook instance = null;
    private static Set files = Collections.synchronizedSet(new HashSet());
    private boolean isRegistered = false;

    static VMShutdownHook hook() {
        if (instance == null){
            if(log.isDebugEnabled()){
                log.debug("creating VMShutdownHook");
            }
            instance = new VMShutdownHook();            
        }
        if(log.isDebugEnabled()){
            log.debug("returning VMShutdownHook instance");
        }
        return instance;
    }

    private VMShutdownHook(){}
    void remove(File file){
        if(file == null){
            return;
        }
        if(log.isDebugEnabled()){
            log.debug("Removing File to Shutdown Hook Collection");
        }
        files.remove(file);
    }
    void add(File file) {
        if(file == null){
            return;
        }
        if(log.isDebugEnabled()){
            log.debug("Adding File to Shutdown Hook Collection");
        }
        files.add(file);   
    }

    public void run() {
        if(log.isDebugEnabled()){
            log.debug("JVM running VM Shutdown Hook");
        }       
        Iterator iter = files.iterator();
        while(iter.hasNext()){
            File file = (File)iter.next();
            if(log.isDebugEnabled()){
                log.debug("Deleting File from Shutdown Hook Collection"+file.getAbsolutePath());
            }    		
            file.delete();
        }    
        if(log.isDebugEnabled()){
            log.debug("JVM Done running VM Shutdown Hook");
        }
    }

    public boolean isRegistered() {
        if(log.isDebugEnabled()){
            if(!isRegistered){
                log.debug("hook isRegistered= false");
            }else{
                log.debug("hook isRegistered= true");
            }
        }
        return isRegistered;
    }

    public void setRegistered(boolean isRegistered) {
        this.isRegistered = isRegistered;
    }
}
