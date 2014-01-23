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

package org.apache.axis2.jaxws;

import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.utility.JavaUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Controls error logging of exceptions thrown by the WebService application (provider, impl, etc.)
 * 
 * This class logs errors for all non-checked exceptions.
 * This class logs extensive debug information for all exceptions.
 */
public class WebServiceExceptionLogger {
    private static final Log log = LogFactory.getLog(WebServiceExceptionLogger.class);
    
    /**
     * Logs an error if the exception thrown by @WebMethod m is not a checked exception.
     * If debug logging is enabled, all exceptions are logged.
     * @param method
     * @param throwable
     * @param logFully (if true then the exception is logged, otherwise only the class and stack is logged)
     * @param serviceImplClass class of service implementation
     * @param serviceInstance 
     * @param args Object[] arguments pass to method
     */
    public static void log(Method method, 
                           Throwable throwable, 
                           boolean logFully,
                           Class serviceImplClass,
                           Object serviceInstance,
                           Object[] args) {
        
        // Must have debug or error logging enabled
        if (!log.isDebugEnabled() && !log.isErrorEnabled()) {
            return;
        }
        
        // Get the root of the exception
        Throwable rootT = null;
        if (throwable instanceof InvocationTargetException) {
            rootT = ((InvocationTargetException) throwable).getTargetException();
        }
        
        
        String name = rootT.getClass().getName();
        String stack = stackToString(rootT);
        
        // Determine if this is a checked exception or non-checked exception
        Class checkedException = JavaUtils.getCheckedException(rootT, method);
        
        if (checkedException == null) {
            // Only log errors for non-checked exceptions
            if (log.isErrorEnabled()) {
                String text = "";
                if (logFully) {
                    text = Messages.getMessage("failureLogger", name, rootT.toString());
                   
                } else {
                    text = Messages.getMessage("failureLogger", name, stack);
                }
                log.error(text);
            }
            
        } 
        
        // Full logging if debug is enabled.
        if (log.isDebugEnabled()) {
            log.debug("Exception invoking a method of " + serviceImplClass.toString()
                    + " of instance " + serviceInstance.toString());
            log.debug("Exception type thrown: " + throwable.getClass().getName());
            if (rootT != null) {
                log.debug("Root Exception type thrown: " + rootT.getClass().getName());
            }
            if (checkedException != null) {
                log.debug("The exception is an instance of checked exception: " + 
                          checkedException.getName());
            }
            
            // Extra trace if ElementNSImpl incompatibility problem.
            // The incompatibility exception occurs if the JAXB Unmarshaller 
            // unmarshals to a dom element instead of a generated object.  This can 
            // result in class cast exceptions.  The solution is usually a missing
            // @XmlSeeAlso annotation in the jaxws or jaxb classes. 
            if (rootT.toString().contains("org.apache.xerces.dom.ElementNSImpl incompatible")) {
            	log.debug("This exception may be due to a missing @XmlSeeAlso in the client's jaxws or" +
            			" jaxb classes.");
            }
            log.debug("Method = " + method.toGenericString());
            for (int i = 0; i < args.length; i++) {
                String value =
                        (args[i] == null) ? "null"
                                : args[i].getClass().toString();
                log.debug(" Argument[" + i + "] is " + value);
            }
        }
        return;
        
    }
    
    /**
     * Get a string containing the stack of the specified exception
     *
     * @param e
     * @return
     */
    private static String stackToString(Throwable e) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.BufferedWriter bw = new java.io.BufferedWriter(sw);
        java.io.PrintWriter pw = new java.io.PrintWriter(bw);
        e.printStackTrace(pw);
        pw.close();
        String text = sw.getBuffer().toString();
        // Jump past the throwable
        text = text.substring(text.indexOf("at"));
        return text;
    }
    
    
}
