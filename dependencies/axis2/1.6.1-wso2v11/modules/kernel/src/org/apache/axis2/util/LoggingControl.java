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

package org.apache.axis2.util;

import java.security.PrivilegedAction;

import org.apache.axis2.java.security.AccessController;

/**
 * This class provides a more efficient means of control over logging than
 * do most providers of the Common's logging API at the cost of runtime
 * flexibility.
 */
public class LoggingControl {
    /**
     * If this flag is set to false then debug messages will not be logged,
     * irrespective of the level set for the logger itself.  This can only
     * be changed as the result of a JVM restart or a purge and reloading
     * of this class.
     * <p/>
     * Usage: if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled())...
     * or
     * if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled())...
     */
    public static final boolean debugLoggingAllowed;

    static {
        String prop = null;
        try {
            // need doPriv to get system prop with J2S enabled
            prop = (String) AccessController.doPrivileged(new PrivilegedAction<String>() {
                public String run() {
                    return System.getProperty("Axis2.prohibitDebugLogging");
                }
            });
        } catch (SecurityException SE) {
            //do nothing
        }
        debugLoggingAllowed = prop == null;
    }
}
