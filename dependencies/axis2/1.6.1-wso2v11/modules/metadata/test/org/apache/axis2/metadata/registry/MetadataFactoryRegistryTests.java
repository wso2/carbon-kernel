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

package org.apache.axis2.metadata.registry;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.wsdl.WSDLReaderConfigurator;

import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import java.io.File;


public class MetadataFactoryRegistryTests extends TestCase {
    
    public void testConfigurationFile() {
        String configLoc = null;
        try {
            String sep = "/";
            configLoc = sep + "test-resources" + sep + "META-INF" + sep + "services" +
            sep + "org.apache.axis2.metadata.registry.MetadataFactoryRegistry";
            String baseDir = new File(System.getProperty("basedir",".")).getCanonicalPath();
            configLoc = new File(baseDir + configLoc).getAbsolutePath();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        if(configLoc != null) {
            MetadataFactoryRegistry.setConfigurationFileLocation(configLoc);
            Object obj = MetadataFactoryRegistry.getFactory(TestInterface.class);
            assertNotNull(obj);
            assertEquals(obj.getClass().getName(), TestImplementation.class.getName()); 
        }
    }
    
    public void testRegisterWSDLReaderConfigurator() {
    	Exception e = null;
    	WSDLReader reader = null;
    	try {
    		WSDLFactory factory = WSDLFactory.newInstance();
        	reader = factory.newWSDLReader();
    	}
    	catch(Exception e2) {
    		e.printStackTrace();
    		e = e2;
    	}
    	assertNull(e);
    	assertNotNull(reader);
    	WSDLReaderConfigurator configurator = (WSDLReaderConfigurator) MetadataFactoryRegistry.
    		getFactory(WSDLReaderConfigurator.class);
    	assertNotNull(configurator);
    	try {
    		configurator.configureReaderInstance(reader);
    	}
    	catch(Exception e2) {
    		e = e2;
    	}
    	assertNull(e);
    	assertEquals(reader.getFeature(com.ibm.wsdl.Constants.FEATURE_VERBOSE), false);
    }

    // This interface class will be used to test the file based registration
    // of custom implementations with the MetadataFactoryRegistry
    public interface TestInterface {
        public void doSomething();
    }
    
}
