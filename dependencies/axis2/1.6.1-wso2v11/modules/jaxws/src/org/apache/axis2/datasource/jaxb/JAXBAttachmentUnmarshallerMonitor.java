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

package org.apache.axis2.datasource.jaxb;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * The monitor is used by the tests to ensure
 * that the JAXBAttachmentUnmarshaller is used and is functioning correctly.
 * In normal usage, monitoring is off.
 */
public class JAXBAttachmentUnmarshallerMonitor {
    
    private static final Log log = LogFactory.getLog(JAXBAttachmentUnmarshallerMonitor.class);
    private static boolean monitoring = false;
    private static ArrayList blobCIDs = new ArrayList(); 
    private static String semifore = "JAXBAttachmentUnmarshallerMonitor";
    
    /**
     * Intentionally private.  All methods are static.
     */
    private JAXBAttachmentUnmarshallerMonitor() {
        
    }

    /**
     * @return true if monitoring
     */
    public static boolean isMonitoring() {
        return monitoring;
    }

    /**
     * Set monitoring
     * @param monitoring boolean
     */
    public static void setMonitoring(boolean monitoring) {
        JAXBAttachmentUnmarshallerMonitor.monitoring = monitoring;
    }
    
    /**
     * Clear the number of creates and failed creates
     */
    public static void clear() {
        synchronized(semifore) {
            blobCIDs.clear();
        }
    }

    /**
     * @return number of JAXBCustomBuilders created
     */
    public static List getBlobCIDs() {
        synchronized(semifore) {
            if (log.isDebugEnabled()) {
                log.debug("NUM BLOB CIDS =" + blobCIDs.size());
            }
            return new ArrayList(blobCIDs);
        }
        
    }

    /**
     * Increase number of total builders 
     */
    public static void addBlobCID(String blobCID) {
        if (log.isDebugEnabled()) {
            log.debug("call ADD BLOB CID =" + blobCID);
        }
        if (isMonitoring()) {
            synchronized(semifore) {
                if (log.isDebugEnabled()) {
                    log.debug("ADD BLOB CID =" + blobCID);
                }
                blobCIDs.add(blobCID);
            }
        }
    }
  
}
