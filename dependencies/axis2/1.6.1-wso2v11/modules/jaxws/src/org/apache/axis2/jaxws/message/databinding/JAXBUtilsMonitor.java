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

package org.apache.axis2.jaxws.message.databinding;

import org.apache.axis2.util.JavaUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * The monitor is used by the tests to ensure
 * that the JAXBUtils code is used and is functioning correctly.
 * In normal usage, monitoring is off.
 * 
 * Currently the JAXBUtilsMonitor keeps track of the packageKeys
 * passed to createJAXBContext(*).  Future improvements may monitor
 * the size of the pools, the pool hits versus misses, etc.
 */
public class JAXBUtilsMonitor {
    
    private static final Log log = LogFactory.getLog(JAXBUtilsMonitor.class);
    private static boolean monitoring = false;
    private static ArrayList<String> packageKeys = new ArrayList<String>(); 
    private static String semifore = "JAXBUtils";
    
    /**
     * Intentionally private.  All methods are static.
     */
    private JAXBUtilsMonitor() {
        
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
        JAXBUtilsMonitor.monitoring = monitoring;
    }
    
    /**
     * Clear the number of creates and failed creates
     */
    public static void clear() {
        synchronized(semifore) {
            packageKeys.clear();
        }
    }

    /**
     * @return package keys
     */
    public static List<String> getPackageKeys() {
        synchronized(semifore) {
            return new ArrayList<String>(packageKeys);
        }
        
    }

    /**
     * Add PackageKey
     */
    public static void addPackageKey(String packageKey) {
        if (isMonitoring()) {
            synchronized(semifore) {
                if (log.isTraceEnabled()) {
                    log.trace(JavaUtils.callStackToString());
                }
                if (!packageKeys.contains(packageKey)) {
                    packageKeys.add(packageKey);
                }
            }
        }
    }
  
}
