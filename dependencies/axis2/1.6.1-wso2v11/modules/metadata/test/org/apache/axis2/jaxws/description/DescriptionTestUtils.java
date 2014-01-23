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

import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.io.File;

/**
 * 
 */
public class DescriptionTestUtils {

    /*
    * ========================================================================
    * Test utility methods
    * ========================================================================
    */

    static public URL getWSDLURL() {
        return getWSDLURL("WSDLTests.wsdl");

    }
    
    static public String getWSDLLocation(String wsdlFileName) {
        String basedir = System.getProperty("basedir", ".");
        String urlString = basedir + "/test-resources/wsdl/" + wsdlFileName;
        return urlString;
    }

    static public URL getWSDLURL(String wsdlFileName) {
        URL wsdlURL = null;
        String urlString = getWSDLLocation(wsdlFileName);
        // Get the URL to the WSDL file.  Note that 'basedir' is setup by Maven
        try {
            wsdlURL = new File(urlString).getAbsoluteFile().toURL();
        } catch (Exception e) {
            System.out.println("Caught exception creating WSDL URL :" + urlString +
                    "; exception: " + e.toString());
        }
        return wsdlURL;
    }

    static public Definition createWSDLDefinition(URL wsdlURL) {
        Definition wsdlDefinition = null;
        try {
            WSDLFactory factory = WSDLFactory.newInstance();
            WSDLReader reader = factory.newWSDLReader();
            wsdlDefinition = reader.readWSDL(wsdlURL.toString());
            wsdlDefinition.setDocumentBaseURI(wsdlURL.toString());
        }
        catch (Exception e) {
            System.out.println(
                    "*** ERROR ***: Caught exception trying to create WSDL Definition: " + e);
            e.printStackTrace();
        }

        return wsdlDefinition;
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
