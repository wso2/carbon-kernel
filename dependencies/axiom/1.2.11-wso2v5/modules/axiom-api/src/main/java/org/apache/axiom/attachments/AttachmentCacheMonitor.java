/*
 * Copyright 2004, 2009 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.axiom.attachments;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import java.io.File;

import java.security.AccessController;
import java.security.PrivilegedAction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The CacheMonitor is responsible for deleting temporary attachment files
 * after a timeout period has expired.
 * 
 * The register method is invoked when the attachment file is created.
 * The access method is invoked whenever the attachment file is accessed.
 * The checkForAgedFiles method is invoked whenever the monitor should look for 
 * files to cleanup (delete).
 * 
 */
public final class AttachmentCacheMonitor {

    static Log log =
         LogFactory.getLog(AttachmentCacheMonitor.class.getName());

    // Setting this property puts a limit on the lifetime of a cache file
    // The default is "0", which is interpreted as forever
    // The suggested value is 300 seconds
    private int attachmentTimeoutSeconds = 0;  // Default is 0 (forever)
    private int refreshSeconds = 0;
    public static final String ATTACHMENT_TIMEOUT_PROPERTY = "org.apache.axiom.attachments.tempfile.expiration";

    // HashMap
    // Key String = Absolute file name
    // Value Long = Last Access Time
    private HashMap files = new HashMap();

    // Delete detection is batched
    private Long priorDeleteMillis = getTime();

    private Timer timer = null;

    private static AttachmentCacheMonitor _singleton = null;


    /**
     * Get or Create an AttachmentCacheMonitor singleton
     * @return TODO
     */
    public static synchronized AttachmentCacheMonitor getAttachmentCacheMonitor() {
        if (_singleton == null) {
            _singleton = new AttachmentCacheMonitor();
        }
        return _singleton;
    }

    /**
     * Constructor
     * Intentionally private.  Callers should use getAttachmentCacheMonitor
     * @see getAttachmentCacheMonitor
     */
    private AttachmentCacheMonitor() {
        String value = "";
        try {
            value = System.getProperty(ATTACHMENT_TIMEOUT_PROPERTY, "0");
            attachmentTimeoutSeconds = Integer.valueOf(value).intValue();
        } catch (Throwable t) {
            // Swallow exception and use default, but log a warning message
        	if (log.isDebugEnabled()) {
        		log.debug("The value of " + value + " was not valid. The default " + 
        		        attachmentTimeoutSeconds + " will be used instead.");
        	}
        }
        refreshSeconds = attachmentTimeoutSeconds / 2;

        if (log.isDebugEnabled()) {
            log.debug("Custom Property Key =  " + ATTACHMENT_TIMEOUT_PROPERTY);
            log.debug("              Value = " + attachmentTimeoutSeconds);
        }

        if (refreshSeconds > 0) {
            timer = new Timer( true );
            timer.schedule( new CleanupFilesTask(), 
                    refreshSeconds * 1000, 
                    refreshSeconds * 1000 );
        }
    }
    
    /**
     * @return timeout value in seconds
     */
    public synchronized int getTimeout() {
    	return attachmentTimeoutSeconds;
    }
    
    /**
     * This method should
     * Set a new timeout value 
     * @param timeout new timeout value in seconds
     */
    public synchronized void setTimeout(int timeout) {
        // If the setting to the same value, simply return
        if (timeout == attachmentTimeoutSeconds) {
            return;
        }
        
    	attachmentTimeoutSeconds = timeout;
    	
    	// Reset the refresh
    	refreshSeconds = attachmentTimeoutSeconds / 2;
    	
    	// Make sure to cancel the prior timer
    	if (timer != null) {
            timer.cancel(); // Remove scheduled tasks from the prior timer
            timer = null;
        }
    	
    	// Make a new timer if necessary
        if (refreshSeconds > 0) {
        	timer = new Timer( true );
            timer.schedule( new CleanupFilesTask(), 
                    refreshSeconds * 1000, 
                    refreshSeconds * 1000 );
        }
        
        if (log.isDebugEnabled()) { 
        	log.debug("New timeout = " + attachmentTimeoutSeconds);
        	log.debug("New refresh = " + refreshSeconds);
        }
    }

    /**
     * Register a file name with the monitor.  
     * This will allow the Monitor to remove the file after
     * the timeout period.
     * @param fileName
     */
    public void  register(String fileName) {
        if (attachmentTimeoutSeconds    > 0) {
            _register(fileName);
            _checkForAgedFiles();
        }
    }
    
    /**
     * Indicates that the file was accessed.
     * @param fileName
     */
    public void access(String fileName) {
        if (attachmentTimeoutSeconds    > 0) {
            _access(fileName);
            _checkForAgedFiles();
        }
    }
    
    /**
     * Check for aged files and remove the aged ones.
     */
    public void checkForAgedFiles() {
        if (attachmentTimeoutSeconds > 0) {
            _checkForAgedFiles();
        }
    }

    private synchronized void _register(String fileName) {
        Long currentTime = getTime();
        if (log.isDebugEnabled()) {
            log.debug("Register file " + fileName);
            log.debug("Time = " + currentTime); 
        }
        files.put(fileName, currentTime);
    }

    private synchronized void _access(String fileName) {
        Long currentTime = getTime();
        Long priorTime = (Long) files.get(fileName);
        if (priorTime != null) {
            files.put(fileName, currentTime);
            if (log.isDebugEnabled()) {
                log.debug("Access file " + fileName);
                log.debug("Old Time = " + priorTime); 
                log.debug("New Time = " + currentTime); 
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("The following file was already deleted and is no longer available: " + 
                          fileName);
                log.debug("The value of " + ATTACHMENT_TIMEOUT_PROPERTY + 
                          " is " + attachmentTimeoutSeconds);
            }
        }
    }

    private synchronized void _checkForAgedFiles() {
        Long currentTime = getTime();
        // Don't keep checking the map, only trigger
        // the checking if it is plausible that 
        // files will need to be deleted.
        // I chose a value of ATTACHMENTT_TIMEOUT_SECONDS/4
        if (isExpired(priorDeleteMillis,
                      currentTime,
                      refreshSeconds)) {
            Iterator it = files.keySet().iterator();
            while (it.hasNext()) {
                String fileName = (String) it.next();
                Long lastAccess = (Long) files.get(fileName);
                if (isExpired(lastAccess,
                              currentTime,
                              attachmentTimeoutSeconds)) {

                    if (log.isDebugEnabled()) {
                        log.debug("Expired file " + fileName);
                        log.debug("Old Time = " + lastAccess); 
                        log.debug("New Time = " + currentTime); 
                        log.debug("Elapsed Time (ms) = " + 
                                  (currentTime.longValue() - lastAccess.longValue())); 
                    }

                    deleteFile(fileName);
                    // Use the iterator to remove this
                    // file from the map (this avoids
                    // the dreaded ConcurrentModificationException
                    it.remove(); 
                }     
            }
               
            // Reset the prior delete time
            priorDeleteMillis = currentTime;
        }
    }

    private boolean deleteFile(final String fileName ) {
        Boolean privRet = (Boolean) AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    return _deleteFile(fileName);
                }
            });
        return privRet.booleanValue();
    }

    private Boolean _deleteFile(String fileName) {
        boolean ret = false;
        File file = new File(fileName);
        if (file.exists()) {
            ret = file.delete();
            if (log.isDebugEnabled()) {
                log.debug("Deletion Successful ? " + ret);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("This file no longer exists = " + fileName);
            }
        }
        return ret;
    }


    private Long getTime() {
        return System.currentTimeMillis();
    }

    private boolean isExpired (Long oldTimeMillis, 
                                      Long newTimeMillis, 
                                      int thresholdSecs) {
        long elapse = newTimeMillis - oldTimeMillis;
        return (elapse > (thresholdSecs*1000));
    }


    private class CleanupFilesTask extends TimerTask {

        /**
         * Trigger a checkForAgedFiles event
         */
        public void run() {
            checkForAgedFiles();
        }
    }
}
