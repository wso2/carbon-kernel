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

package org.apache.axiom.attachments;

import javax.activation.FileDataSource;
import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class CachedFileDataSource extends FileDataSource {

    String contentType = null;
    
    protected static Log log = LogFactory.getLog(CachedFileDataSource.class);

    // The AttachmentCacheMonitor is used to delete expired copies of attachment files.
    private static AttachmentCacheMonitor acm = 
        AttachmentCacheMonitor.getAttachmentCacheMonitor();
    
    // Represents the absolute pathname of cached attachment file
    private String cachedFileName = null;

    public CachedFileDataSource(File arg0) {
        super(arg0);
        if (log.isDebugEnabled()) {
        	log.debug("Enter CachedFileDataSource ctor");
        }
        if (arg0 != null) {
        	try {
        		cachedFileName = arg0.getCanonicalPath();
        	} catch (java.io.IOException e) {
        		log.error("IOException caught: " + e);
        	}
        }
        if (cachedFileName != null) {
        	if (log.isDebugEnabled()) {
        		log.debug("Cached file: " + cachedFileName);
        		log.debug("Registering the file with AttachmentCacheMonitor and also marked it as being accessed");
        	}
            // Tell the monitor that the file is being accessed.
        	acm.access(cachedFileName);
            // Register the file with the AttachmentCacheMonitor
            acm.register(cachedFileName);
        }
    }

    public String getContentType() {
        if (this.contentType != null) {
            return contentType;
        } else {
            return super.getContentType();
        }
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
