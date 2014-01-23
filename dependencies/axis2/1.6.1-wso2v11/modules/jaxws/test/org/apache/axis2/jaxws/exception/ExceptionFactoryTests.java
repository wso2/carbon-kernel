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

package org.apache.axis2.jaxws.exception;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.ExceptionFactory;

import javax.xml.ws.ProtocolException;
import javax.xml.ws.WebServiceException;

/**
 * Tests the ExceptionFactory
 */
public class ExceptionFactoryTests extends TestCase {
	private static final String sampleText = "Sample";

    /**
     * @param name
     */
    public ExceptionFactoryTests(String name) {
        super(name);
    }
    
    /**
     * @teststrategy Tests creation of a WebServiceException
     */
    public void testExceptionFactory00() throws Exception {
    	try{
    		throw ExceptionFactory.makeWebServiceException(sampleText);
    	} catch(WebServiceException e){
    		assertTrue(sampleText.equals(e.getMessage()));
    		assertTrue(e.getCause() == null);
    	}
    }
    
    /**
     * @teststrategy Tests creation of a WebServiceException from another WebServiceException
     */
    public void testExceptionFactory01() throws Exception {
    	try{
    		WebServiceException wse = (WebServiceException) ExceptionFactory.makeWebServiceException(sampleText);
    		throw ExceptionFactory.makeWebServiceException(wse);
    	} catch(WebServiceException e){
    		// Should only be a single WebServiceException
    		assertTrue(sampleText.equals(e.getMessage()));
    		assertTrue(e.getCause() == null);
    	}
    }
    
    /**
     * @teststrategy Tests creation of a WebServiceException->WebServiceException->ProtocolException
     */
    public void testExceptionFactory02() throws Exception {
    	ProtocolException pe = new ProtocolException(sampleText);
    	try{
    		WebServiceException wse = (WebServiceException) ExceptionFactory.makeWebServiceException(pe);
    		throw ExceptionFactory.makeWebServiceException(wse);
    	} catch(WebServiceException e){
    		// Should only be a single WebServiceException with a Protocol Exception
    		assertTrue(sampleText.equals(e.getMessage()));
    		assertTrue(e.getCause() == null);
    	}
    }
    
    /**
     * @teststrategy Tests creation of a WebServiceException->WebServiceException->NullPointerException
     */
    public void testExceptionFactory03() throws Exception {
    	NullPointerException npe = new NullPointerException();
    	try{
    		WebServiceException wse = (WebServiceException) ExceptionFactory.makeWebServiceException(npe);
    		throw ExceptionFactory.makeWebServiceException(wse);
    	} catch(WebServiceException e){
    		// Should only be a single WebServiceException with a Protocol Exception
    		assertTrue(e.getCause() == npe);
    		assertTrue(e.getCause().getMessage() == null);
    	}
    }
    
}
