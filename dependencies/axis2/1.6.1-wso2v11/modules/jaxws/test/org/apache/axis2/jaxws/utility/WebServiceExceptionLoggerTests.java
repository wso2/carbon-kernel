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

package org.apache.axis2.jaxws.utility;

import org.apache.axis2.jaxws.WebServiceExceptionLogger;

import java.awt.Image;
import java.io.File;
import java.lang.reflect.Method;

import javax.imageio.ImageIO;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import junit.framework.TestCase;

/**
 * Validate the FailureLogger utility
 */
public class WebServiceExceptionLoggerTests extends TestCase {
    /**
     * Validate FailureLogger.getCheckedException method
     * @throws Exception
     */
    public void testCheckedException() throws Exception {
        
        Method m = Sample.class.getMethod("m", new Class[] {});
        
        Throwable t = new ExceptionA();
        Class checkedException = JavaUtils.getCheckedException(t, m);      
        assertTrue(ExceptionA.class.equals(checkedException));
        
        t = new ExceptionB();
        checkedException = JavaUtils.getCheckedException(t, m); 
        assertTrue(ExceptionB.class.equals(checkedException));
        
        t = new ExceptionC();
        checkedException = JavaUtils.getCheckedException(t, m); 
        assertTrue(ExceptionB.class.equals(checkedException));
        
        t = new ExceptionD();
        checkedException = JavaUtils.getCheckedException(t, m); 
        assertTrue(checkedException == null);
        
        t = new NullPointerException();
        checkedException = JavaUtils.getCheckedException(t, m); 
        assertTrue(checkedException == null);
        
    }
    
    class Sample {
        public void m() throws ExceptionA, ExceptionB {}
    }
    
    class ExceptionA extends Exception {
        
    }
    
    class ExceptionB extends Exception {
        
    }
    
    class ExceptionC extends ExceptionB {
        
    }
    
    class ExceptionD extends Exception {
        
    }
}
