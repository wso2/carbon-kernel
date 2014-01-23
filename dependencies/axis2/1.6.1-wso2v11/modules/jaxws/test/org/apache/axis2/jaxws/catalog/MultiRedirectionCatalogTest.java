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

package org.apache.axis2.jaxws.catalog;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.catalog.impl.OASISCatalogManager;
import org.apache.axis2.jaxws.util.WSDL4JWrapper;

import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.wsdl.PortType;
import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Tests the use of the Apache Commons Resolver API to resolve URIs.
 */
public class MultiRedirectionCatalogTest extends TestCase {
	private static final String ROOT_WSDL = "/test-resources/catalog/root.wsdl";
	private static final String TEST_RESOURCES = "/test-resources/catalog/";
	
	public void testOneCatalogSuccess() {
		verifySuccess(ROOT_WSDL, TEST_RESOURCES + "basic-catalog.xml");
	}

	public void testNextCatalogSuccess() {
		verifySuccess(ROOT_WSDL, TEST_RESOURCES + "root-catalog.xml");
	}
	
	public void testNextCatalogFailure() {
		verifyFailure(ROOT_WSDL, TEST_RESOURCES + "fail/root-catalog.xml");		
	}
	
	public void testNoCatEntryForFirstImport() {
		verifyFailure(ROOT_WSDL, TEST_RESOURCES + "fail/firstImportFail.xml");
	}
	
	public void testNoCatEntryForSecondImport() {
		verifyFailure(ROOT_WSDL, TEST_RESOURCES + "fail/secondImportFail.xml");
	}
	
	public void testNoCatEntryForThirdImport() {
		verifyFailure(ROOT_WSDL, TEST_RESOURCES + "fail/thirdImportFail.xml");
	}
	
	/**
	 * Ensure that the catalog is used to locate imported resources.
	 */
	private void verifySuccess(String wsdlLocation, String catalogFile) {
	    URL url = getURLFromLocation(wsdlLocation);
	    
	    try{
			OASISCatalogManager catalogManager = new OASISCatalogManager();
			catalogManager.setCatalogFiles(getURLFromLocation(catalogFile).toString());
            WSDL4JWrapper w4j = new WSDL4JWrapper(url, catalogManager, false, 0);
	    	Definition wsdlDef = w4j.getDefinition();
	    	assertNotNull(wsdlDef);   
	    	QName portTypeName = new QName("http://www.example.com/test/calculator",
	    			                       "CalculatorService",
	    			                       "");
	    	PortType portType = wsdlDef.getPortType(portTypeName);
	    	assertNotNull(portType);
	    	Operation clearOp = portType.getOperation("clear", null, null);
	    	assertNotNull(clearOp);
	    	Input clearOpInput = clearOp.getInput();
	    	assertNotNull(clearOpInput);
	    	Message msg = clearOpInput.getMessage();
	    	assertNotNull(msg);
	    	Part expectedPart = msg.getPart("part1");
            assertNotNull(expectedPart);
	    }catch(Exception e){
	    	e.printStackTrace();
	    	fail();
	    }
	}	
	
	/**
	 * Ensure that the test case is valid by failing in the absence of a needed
	 * catalog entry.
	 */
	private void verifyFailure(String wsdlLocation, String catalogFile) {
	    URL url = getURLFromLocation(wsdlLocation);
	    
	    try{
			OASISCatalogManager catalogManager = new OASISCatalogManager();
            catalogManager.setCatalogFiles(getURLFromLocation(catalogFile).toString());
	    	WSDL4JWrapper w4j = new WSDL4JWrapper(url, catalogManager, false, 0);
	    	w4j.getDefinition();
	    	fail("Should have received a WSDLException due to the invalid WSDL location " 
	        		+ "not redirected by the catalog.");
	    } catch(WSDLException e) {
	    	// do nothing - successful test case
	    } catch(Exception e){
	    	e.printStackTrace();
	    	fail();
	    }
	}
	
	/**
	 * Given a String representing a file location, return a URL.
	 * @param wsdlLocation
	 * @return
	 */
	private URL getURLFromLocation(String wsdlLocation) {
		URL url = null;
	    try {
	    	try{
	        	String baseDir = new File(System.getProperty("basedir",".")).getCanonicalPath();
	        	wsdlLocation = new File(baseDir + wsdlLocation).getAbsolutePath();
        	}catch(Exception e){
        		e.printStackTrace();
        		fail();
        	}
	       	File file = new File(wsdlLocation);
	       	url = file.toURL();
	    } catch (MalformedURLException e) {
	        e.printStackTrace();
	        fail();
	    }
	    
	    return url;
	}
}
