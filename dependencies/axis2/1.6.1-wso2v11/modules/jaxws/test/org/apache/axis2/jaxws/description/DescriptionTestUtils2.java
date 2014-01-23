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


package org.apache.axis2.jaxws.description;

import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.spi.ServiceDelegate;
import org.apache.axis2.jaxws.unitTest.TestLogger;

import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.ws.Service;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.io.File;

/**
 * 
 */
public class DescriptionTestUtils2 {
    
    /*
     * ========================================================================
     * Test utility methods
     * ========================================================================
     */

    static public URL getWSDLURL() {
        return getWSDLURL("WSDLTests.wsdl");
        
    }
    
    static public String getWSDLLocation(String wsdlFileName) {
        // Get the URL to the WSDL file.  Note that 'basedir' is setup by Maven
        String basedir = System.getProperty("basedir",".");
        return basedir + "/test-resources/wsdl/" + wsdlFileName;
    }
    
    static public URL getWSDLURL(String wsdlFileName) {
        URL wsdlURL = null;
        String urlString = getWSDLLocation(wsdlFileName);
        try {
            wsdlURL = new File(urlString).getAbsoluteFile().toURL();
        } catch (Exception e) {
            TestLogger.logger.debug(
                    "Caught exception creating WSDL URL :" + urlString + "; exception: " +
                            e.toString());
        }
        return wsdlURL;
    }
    
    static Definition createWSDLDefinition(URL wsdlURL) {
        Definition wsdlDefinition = null;
        try {
            WSDLFactory factory = WSDLFactory.newInstance();
            WSDLReader reader = factory.newWSDLReader();
            wsdlDefinition = reader.readWSDL(wsdlURL.toString());
        }
        catch (Exception e) {
            TestLogger.logger
                    .debug("*** ERROR ***: Caught exception trying to create WSDL Definition: " +
                            e);
            e.printStackTrace();
        }

        return wsdlDefinition;
    }

    static public ServiceDelegate getServiceDelegate(Service service) {
        // Need to get to the private Service._delegate field in order to get to the ServiceDescription to test
        ServiceDelegate returnServiceDelegate = null;
        try {
            try {
//                Field serviceDelgateField = service.getClass().getDeclaredFields()[0];
                Field serviceDelgateField = service.getClass().getDeclaredField("delegate");
                serviceDelgateField.setAccessible(true);
                returnServiceDelegate = (ServiceDelegate) serviceDelgateField.get(service);
            } catch (NoSuchFieldException e) {
                // This may be a generated service subclass, so get the delegate from the superclass
                Field serviceDelegateField = service.getClass().getSuperclass().getDeclaredField("delegate");
                serviceDelegateField.setAccessible(true);
                returnServiceDelegate = (ServiceDelegate) serviceDelegateField.get(service);
            } 
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchFieldException e) { 
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return returnServiceDelegate;
    }
    
    static public DescriptionBuilderComposite getServiceDescriptionComposite(ServiceDescription svcDesc) {
        DescriptionBuilderComposite returnComposite = null;
        // Need to get the composite off the implementation using the getter method, but it is all
        // packaged protected and not part of the interface.
        try {
            Method getComposite = svcDesc.getClass().getDeclaredMethod("getDescriptionBuilderComposite");
            getComposite.setAccessible(true);
            returnComposite = (DescriptionBuilderComposite) getComposite.invoke(svcDesc, null);
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return returnComposite;
    }

}
