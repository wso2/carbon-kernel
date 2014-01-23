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

package org.apache.axis2.deployment;

import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.AddressingHelper;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.WSDL11ToAllAxisServicesBuilder;
import org.apache.axis2.engine.AxisConfiguration;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.StringReader;

/**
 *
 */
public class WSDL11ToAxisServiceBuilderTest extends XMLTestCase {

    private String wsdlLocation = System.getProperty("basedir", ".") + "/" + "test-resources/wsdl/Version.wsdl";

    public void testVersion() {
        XMLUnit.setIgnoreWhitespace(true);
        File testResourceFile = new File(wsdlLocation);
        try {
            WSDL11ToAllAxisServicesBuilder builder = new WSDL11ToAllAxisServicesBuilder(
                    new FileInputStream(testResourceFile));
            AxisService axisService = builder.populateService();
            ConfigurationContext configContext = ConfigurationContextFactory.createDefaultConfigurationContext();
            AxisConfiguration axisConfig = configContext.getAxisConfiguration();
            axisConfig.addService(axisService);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            axisService.printWSDL(baos);
            assertXMLEqual(new FileReader(testResourceFile), new StringReader(new String(baos.toByteArray())));
        } catch (Exception e) {
            System.out.println("Error in WSDL : " + testResourceFile.getName());
            System.out.println("Exception: " + e.toString());
            fail("Caught exception " + e.toString());
        } finally {
            XMLUnit.setIgnoreWhitespace(false);
        }
    }

    // Check the addressing requirement parameter is set to
    // ADDRESSING_UNSPECIFIED when <wsaw:UsingAddressing /> is NOT used in the
    // WSDL
    public void testWithoutUsingAddressing() {
        File testResourceFile = new File(wsdlLocation);
        try {
            WSDL11ToAllAxisServicesBuilder builder = new WSDL11ToAllAxisServicesBuilder(
                    new FileInputStream(testResourceFile));
            AxisService axisService = builder.populateService();
            String addressingRequired = AddressingHelper
                    .getAddressingRequirementParemeterValue(axisService);
            assertEquals("Unexpected addressingRequirementParameter value: "
                    + addressingRequired,
                    AddressingConstants.ADDRESSING_UNSPECIFIED,
                    addressingRequired);
        } catch (Exception e) {
            System.out.println("Error in WSDL : " + testResourceFile.getName());
            System.out.println("Exception: " + e.toString());
            fail("Caught exception " + e.toString());
        }
    }

    // Check the addressing requirement parameter is set to
    // ADDRESSING_OPTIONAL when <wsaw:UsingAddressing /> is used in the WSDL
    public void testUsingAddressing() {
        wsdlLocation = System.getProperty("basedir", ".") + "/"
                + "test-resources/wsdl/UsingAddressing.wsdl";
        File testResourceFile = new File(wsdlLocation);
        try {
            WSDL11ToAllAxisServicesBuilder builder = new WSDL11ToAllAxisServicesBuilder(
                    new FileInputStream(testResourceFile));
            AxisService axisService = builder.populateService();
            String addressingRequired = AddressingHelper
                    .getAddressingRequirementParemeterValue(axisService);
            assertEquals("Unexpected addressingRequirementParameter value: "
                    + addressingRequired,
                    AddressingConstants.ADDRESSING_OPTIONAL, addressingRequired);
        } catch (Exception e) {
            System.out.println("Error in WSDL : " + testResourceFile.getName());
            System.out.println("Exception: " + e.toString());
            fail("Caught exception " + e.toString());
        }
    }

    // Check the addressing requirement parameter is set to
    // ADDRESSING_OPTIONAL when <wsaw:UsingAddressing wsdl:required="false" />
    // is used in the WSDL
    public void testUsingAddressingOptional() {
        wsdlLocation = System.getProperty("basedir", ".") + "/"
                + "test-resources/wsdl/UsingAddressingOptional.wsdl";
        File testResourceFile = new File(wsdlLocation);
        try {
            WSDL11ToAllAxisServicesBuilder builder = new WSDL11ToAllAxisServicesBuilder(
                    new FileInputStream(testResourceFile));
            AxisService axisService = builder.populateService();
            String addressingRequired = AddressingHelper
                    .getAddressingRequirementParemeterValue(axisService);
            assertEquals("Unexpected addressingRequirementParameter value: "
                    + addressingRequired,
                    AddressingConstants.ADDRESSING_OPTIONAL, addressingRequired);
        } catch (Exception e) {
            System.out.println("Error in WSDL : " + testResourceFile.getName());
            System.out.println("Exception: " + e.toString());
            fail("Caught exception " + e.toString());
        }
    }

    // Check the addressing requirement parameter is set to
    // ADDRESSING_REQUIRED when <wsaw:UsingAddressing wsdl:required="true" /> is
    // used in the WSDL
    public void testUsingAddressingRequired() {
        wsdlLocation = System.getProperty("basedir", ".") + "/"
                + "test-resources/wsdl/UsingAddressingRequired.wsdl";
        File testResourceFile = new File(wsdlLocation);
        try {
            WSDL11ToAllAxisServicesBuilder builder = new WSDL11ToAllAxisServicesBuilder(
                    new FileInputStream(testResourceFile));
            AxisService axisService = builder.populateService();
            String addressingRequired = AddressingHelper
                    .getAddressingRequirementParemeterValue(axisService);
            assertEquals("Unexpected addressingRequirementParameter value: "
                    + addressingRequired,
                    AddressingConstants.ADDRESSING_REQUIRED, addressingRequired);
        } catch (Exception e) {
            System.out.println("Error in WSDL : " + testResourceFile.getName());
            System.out.println("Exception: " + e.toString());
            fail("Caught exception " + e.toString());
        }
    }

}