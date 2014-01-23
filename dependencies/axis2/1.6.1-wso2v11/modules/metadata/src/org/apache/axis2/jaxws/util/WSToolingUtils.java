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

package org.apache.axis2.jaxws.util;

import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import java.io.IOException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Properties;
import java.util.StringTokenizer;

public class WSToolingUtils {

    private static final Log log = LogFactory.getLog(WSToolingUtils.class);

    /**
     * A handy function to check for empty or null string
     * 
     * @param str
     * @return boolean
     * 
     */
    public static boolean hasValue(String str) {
        return ((str != null) && (str.length() > 0));
    }

    /**
     * Retrieves the major version number of the WsGen class that we're using
     * 
     * @return String
     * 
     */
    public static String getWsGenVersion() throws ClassNotFoundException, IOException {

        Class clazz = null;
        try {

            clazz = forName("com.sun.tools.ws.WsGen", false,
                getContextClassLoader(null));

        } catch (ClassNotFoundException e1) {

            try {

                clazz = forName("com.sun.tools.internal.ws.WsGen", false,
                    getContextClassLoader(null));

            } catch (ClassNotFoundException e2) {
                if (log.isDebugEnabled()) {
                    log.debug("Exception thrown from getWsGenVersion: " + e2.getMessage(), e2);
                }
                throw (ClassNotFoundException) e2;
            }
        }

        Properties p = new Properties();

        try {

            p.load(clazz.getResourceAsStream("version.properties"));

        } catch (IOException ioex) {
            if (log.isDebugEnabled()) {
                log.debug("Exception thrown from getWsGenVersion: " + ioex.getMessage(), ioex);

            }
            throw (IOException) ioex.getCause();
        }

        return (p.getProperty("major-version"));
    }

    /**
     * @return ClassLoader
     */
    private static ClassLoader getContextClassLoader(final ClassLoader classLoader) {
        ClassLoader cl;
        try {
            cl = (ClassLoader) AccessController.doPrivileged(
                new PrivilegedExceptionAction() {
                    public Object run() throws ClassNotFoundException {
                        return classLoader != null ? classLoader : Thread.currentThread().getContextClassLoader();
                    }
                }
            );
        } catch (PrivilegedActionException e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception thrown from AccessController: " + e.getMessage(), e);
            }
            throw ExceptionFactory.makeWebServiceException(e.getException());
        }

        return cl;
    }

    /**
     * Return the class for this name
     *
     * @return Class
     */
    private static Class forName(final String className, final boolean initialize,
        final ClassLoader classloader) throws ClassNotFoundException {
        Class cl = null;
        try {
            cl = (Class) AccessController.doPrivileged(
                new PrivilegedExceptionAction() {
                    public Object run() throws ClassNotFoundException {
                        return Class.forName(className, initialize, classloader);
                    }
                }
            );
        } catch (PrivilegedActionException e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception thrown from AccessController: " + e.getMessage(), e);
            }
            throw (ClassNotFoundException) e.getException();
        }

        return cl;
    }

    /**
     * Answer if the input version number is 2.1.6 or later.  Version 2.1.6 is the Sun RI version that changed
     * the WebMethod annotation semantics.
     * 
     * @param wsGenVersion A version number separated by "."  Up to the first 3 values will be checked.
     * @return true if the version number is 2.1.6 or later, false otherwise.
     */
    public static boolean isValidVersion(String wsGenVersion) {
        if(log.isDebugEnabled()){
            log.debug("Start isValidVersion(String)");
        }

        if (log.isDebugEnabled()) {
            log.debug("isValidVersion: Determining if WsGen version: " +wsGenVersion
                +" is appropriate version for using new SUN RI behavior");
        }
        if(wsGenVersion == null){
            return false;
        }
        /*
         * This algorithm is improvement over the old algorithm we had to validate the
         * version. In this algorithm we don't assume that the format will be x.x.x.
         * This algorithm looks for versionNumbers in a String token delimited by a
         * ".", the idea is to look for the first digit in each token and compare that
         * with the version validation requirements.
         * we return false if version is less that 2.1.6.
         * possible input version strings could be "JAX-WS RI 2.2-b05-", "2.1.6" "2.1.0" etc.
         */
        
        // Minimum version required is 2.1.6
        final int minimumVersionRequired[] = {2, 1, 6};

        String version = wsGenVersion.trim();
        
        StringTokenizer st = new StringTokenizer(version, ".");
        if(st.countTokens() <= 0){
            if(log.isDebugEnabled()){
                log.debug("No Tokens to validate the tooling version, Input version String is invalid.");
            }
            return false;
        }
        
        // Check up to as many version values as we have values in the minimum required version
        boolean lastCheckEqual = false;
        int tokenCnt = 0;

        for( ; tokenCnt < minimumVersionRequired.length && st.hasMoreTokens(); tokenCnt++) {
            String token = st.nextToken();
            if(token == null){
                return false;
            }
            int versionNumber = getIntegerValue(token);
            int minimumVersionNumber = minimumVersionRequired[tokenCnt];
            
            if (versionNumber < minimumVersionNumber) {
                // The version number is too low, so it is invalid
                if(log.isDebugEnabled()){
                    log.debug("Validation failed on tokenCnt = " + tokenCnt);
                    log.debug("Input VersionNumber =" + versionNumber);
                    log.debug("Minimum Version Number required = " + minimumVersionNumber);
                }
                return false;
            } else if (versionNumber > minimumVersionNumber) {
                // The version number is higher than required, so it passes validation.
                if(log.isDebugEnabled()){
                    log.debug("Validation passed on tokenCnt = " + tokenCnt);
                    log.debug("Input VersionNumber = " + versionNumber);
                    log.debug("Minimum Version Number required = " + minimumVersionNumber);
                }
                return true;
            } else {
                // The version number sub-value matches exactly, so we need to check the next sub-value.s
                if(log.isDebugEnabled()){
                    log.debug("Validation unresolved on tokenCnt = " + tokenCnt);
                    log.debug("Input VersionNumber = " + versionNumber);
                    log.debug("Minimum Version Number required = " + minimumVersionNumber);
                }
                lastCheckEqual = true;
                continue;
            }
        }
        if(log.isDebugEnabled()){
            log.debug("Exit isValidVersion(String)");
        }
        // If the version numbers we checked so far were equal to the minimum version BUT it was shorter
        // in length, then return false.  For example if the input is "2.1", that is actually "2.1.0"
        // which would be less than "2.1.6".
        if (lastCheckEqual && tokenCnt < minimumVersionRequired.length) {
            return false;
        } 
        return true;
    }
    
    /**
     * Parse the input string and return an integer value based on it.  It will look for the first numeric value
     * in the string then use all digits up to the next non-numeric value or end of the string as the basis for
     * the value.  For example "JAX-WS RI 27" will return the value 27.
     * @param s - String containing the integer to be returned.
     * @return a value or -1 if not integer value was found in the token.
     */
    private static int getIntegerValue(String s){
        int returnValue = -1;

        // Build up a buffer containing any digits up to the first non-numeric character.
        StringBuffer valueString = new StringBuffer();
        for(int i = 0; i < s.length(); i++){
            char ch = s.charAt(i);
            if(Character.isDigit(ch)){
                valueString.append(Character.getNumericValue(ch));
            } else if (valueString.length() > 0){
                // We've found some numbers then encountered the first non-numeric value, so
                // exit the loop and use those numbers as the value
                break;
            }
        }
        
        // If there were any numeric values found in the string, convert them to the integer return value
        if (valueString.length() > 0) {
            returnValue = Integer.valueOf(valueString.toString());
        }
        return returnValue;
    }
}
