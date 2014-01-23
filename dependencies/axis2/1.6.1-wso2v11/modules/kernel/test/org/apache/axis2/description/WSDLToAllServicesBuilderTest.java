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

package org.apache.axis2.description;

import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.engine.ListenerManager;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.List;


/**
 * Tests the WSDL11ToAllServicesBuilder class.
 */
public class WSDLToAllServicesBuilderTest extends TestCase {
    private static final String[] expectedService11 = {
            "EchoServicePortOne",
            "EchoServicePortTwo",
            "EchoServicePortThree"};
    private static final String[] expectedService20 = {
            "echoService1$echoServiceSOAPBinding_http",
            "echoService1$echoServiceEndpoint2SOAPBinding_http",
            "echoService2$echoServiceSOAPBinding_http"};
    private ConfigurationContext configContext;
    ListenerManager lm;

    protected void setUp() throws Exception {
        configContext =
                ConfigurationContextFactory.createEmptyConfigurationContext();
        lm = new ListenerManager();
        lm.init(configContext);
        lm.start();
    }

    protected void tearDown() throws AxisFault {
        configContext.terminate();
    }

    private void checkResults(List axisServices, String expectedService[]) {

        Iterator asi = axisServices.iterator();
        int i = 0;
        while (asi.hasNext() && i < expectedService.length) {
            AxisService as = (AxisService) asi.next();
            System.out.println("AxisService : " + as.getName());
            assertEquals("Unexpected service name in AxisService List: expected "
                    + expectedService[i] + " but found " + as.getName() + ".",
                         as.getName(),
                         expectedService[i]);
            i++;
        }
    }

    public void testWSDL11toAllAxisServices() throws Exception {
        File testResourceFile = new File("target/test-classes/wsdl/EchoServiceWsdl11.wsdl");
        File outLocation = new File("target/test-resources");
        outLocation.mkdirs();
        if (testResourceFile.exists()) {
            List axisServices = null;
            try {
                WSDL11ToAllAxisServicesBuilder builder = new WSDL11ToAllAxisServicesBuilder(
                        new FileInputStream(testResourceFile));
                axisServices = builder.populateAllServices();
                System.out.println("WSDL file: " + testResourceFile.getName());
            } catch (Exception e) {
                System.out.println("Error in WSDL : " + testResourceFile.getName());
                System.out.println("Exception: " + e.toString());
                throw e;
            }
            checkResults(axisServices, expectedService11);

        }
    }

    public void testWSDL20toAllAxisServices() throws Exception {
        File testResourceFile = new File("target/test-classes/wsdl/EchoServiceWsdl20.wsdl");
        File outLocation = new File("target/test-resources");
        outLocation.mkdirs();
        if (testResourceFile.exists()) {
            List axisServices = null;
            try {
                WSDL20ToAllAxisServicesBuilder builder = new WSDL20ToAllAxisServicesBuilder(
                        new FileInputStream(testResourceFile));
                axisServices = builder.populateAllServices();
                System.out.println("WSDL file: " + testResourceFile.getName());
            } catch (Exception e) {
                System.out.println("Error in WSDL : " + testResourceFile.getName());
                System.out.println("Exception: " + e.toString());
                throw e;
            }
            checkResults(axisServices, expectedService20);

        }
    }


}
