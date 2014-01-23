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

/**
 * The monitor is used by the tests to ensure
 * that the JAXBCustomBuilder is used and is functioning correctly.
 * In normal usage, monitoring is off.
 */
public class JAXBCustomBuilderMonitor {
    
    private static boolean monitoring = false;
    private static int totalBuilders = 0;
    private static int totalCreates = 0;
    private static int totalFailedCreates = 0;
    private static String semifore = "JAXBCustomBuilderMonitor";
    
    /**
     * Intentionally private.  All methods are static.
     */
    private JAXBCustomBuilderMonitor() {
        
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
        JAXBCustomBuilderMonitor.monitoring = monitoring;
    }
    
    /**
     * Clear the number of creates and failed creates
     */
    public static void clear() {
        synchronized(semifore) {
            totalCreates = 0;
            totalFailedCreates = 0;
        }
    }

    /**
     * @return number of JAXBCustomBuilders created
     */
    public static int getTotalBuilders() {
        synchronized(semifore) {
            return totalBuilders;
        }
        
    }

    /**
     * Increase number of total builders 
     */
    static void updateTotalBuilders() {
        synchronized(semifore) {
            JAXBCustomBuilderMonitor.totalBuilders++;
        }
    }

    /**
     * @return number of successful creates
     */
    public static int getTotalCreates() {
        if (isMonitoring()) {
            synchronized(semifore) {
                return totalCreates;
            }
        }
        return 0;
    }

    /**
     * Increment number of creates
     */
    static void updateTotalCreates() {
        if (isMonitoring()) {
            synchronized(semifore) {
                JAXBCustomBuilderMonitor.totalCreates++;
            }
        }
    }

    /**
     * @return number of failed attempts
     */
    public static int getTotalFailedCreates() {
        if (isMonitoring()) {
            synchronized(semifore) {
                return totalFailedCreates;
            }
        }
        return 0;
    }

    /**
     * Increment number of failed creates
     */
    static void updateTotalFailedCreates() {
        if (isMonitoring()) {
            synchronized(semifore) {
                JAXBCustomBuilderMonitor.totalFailedCreates++;
            }
        }
    }
  
}
