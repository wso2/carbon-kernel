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

package org.apache.axis2.jaxws.wsdl.schemareader;

import junit.framework.TestCase;

import org.apache.axis2.jaxws.unitTest.TestLogger;
import org.apache.axis2.jaxws.util.WSDL4JWrapper;
import org.apache.axis2.jaxws.wsdl.impl.SchemaReaderImpl;

import javax.wsdl.Definition;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

public class SchemaReaderTests extends TestCase {
	public void testSchemaReader(){
		SchemaReaderImpl sri = new SchemaReaderImpl();
		
	    String wsdlLocation="/test-resources/wsdl/shapes.wsdl";
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
	    try{
	    	WSDL4JWrapper w4j = new WSDL4JWrapper(url);
	    	Definition wsdlDef = w4j.getDefinition();
	    	assertNotNull(wsdlDef);
	    	Set<String> pkg = sri.readPackagesFromSchema(wsdlDef);
            TestLogger.logger.debug("Packages:");
	    	for(String pkgName:pkg){
                TestLogger.logger.debug(pkgName);
	    	}
	    }catch(Exception e){
	    	e.printStackTrace();
	    	fail();
	    }
	}
	
}
