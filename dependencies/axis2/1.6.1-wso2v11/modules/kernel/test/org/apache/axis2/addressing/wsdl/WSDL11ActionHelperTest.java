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

package org.apache.axis2.addressing.wsdl;

import junit.framework.TestCase;
import org.apache.axis2.AbstractTestCase;

import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Input;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.PortType;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import java.io.File;
import java.net.URL;
import java.util.List;

public class WSDL11ActionHelperTest extends TestCase {

    String testWSDLFile = "/target/test-resources/wsdl/actionTests.wsdl";

    Definition definition;

    protected void setUp() throws Exception {
        super.setUp();
        WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
        reader.setFeature("javax.wsdl.importDocuments", false);
        reader.setFeature("javax.wsdl.verbose", false);

        URL wsdlFile = new File(AbstractTestCase.basedir + testWSDLFile)
                .toURL();//getClass().getClassLoader().getResource(testWSDLFile);
        definition = reader.readWSDL(wsdlFile.toString());
    }


    // Test DefaultActionPattern (no names in WSDL)
    // Test not required for Fault as Fault elements MUST have naes per the WSDL 1.1 spec
    //    portType=withoutWSAWActionNoName
    // operation=echo
    public void testGenerateInputActionNoNames() {
        String expectedAction =
                "http://ws.apache.org/axis2/actiontest/withoutWSAWActionNoName/echoRequest";
        PortType pt = definition.getPortType(
                new QName("http://ws.apache.org/axis2/actiontest/", "withoutWSAWActionNoName"));
        List operations = pt.getOperations();
        Operation op = (Operation) operations.get(0);
        Input in = op.getInput();
        String actualAction = WSDL11ActionHelper.getActionFromInputElement(definition, pt, op, in);
        assertEquals(expectedAction, actualAction);
    }

    public void testGenerateOutputActionNoNames() {
        String expectedAction =
                "http://ws.apache.org/axis2/actiontest/withoutWSAWActionNoName/echoResponse";
        PortType pt = definition.getPortType(
                new QName("http://ws.apache.org/axis2/actiontest/", "withoutWSAWActionNoName"));
        List operations = pt.getOperations();
        Operation op = (Operation) operations.get(0);
        Output out = op.getOutput();
        String actualAction =
                WSDL11ActionHelper.getActionFromOutputElement(definition, pt, op, out);
        assertEquals(expectedAction, actualAction);
    }

    // Test DefaultActionPattern (names explicitly set in WSDL)
    //    portType=withoutWSAWAction
    // operation=echo
    public void testGenerateInputAction() {
        String expectedAction =
                "http://ws.apache.org/axis2/actiontest/withoutWSAWAction/NamedInput";
        PortType pt = definition.getPortType(
                new QName("http://ws.apache.org/axis2/actiontest/", "withoutWSAWAction"));
        List operations = pt.getOperations();
        Operation op = (Operation) operations.get(0);
        Input in = op.getInput();
        String actualAction = WSDL11ActionHelper.getActionFromInputElement(definition, pt, op, in);
        assertEquals(expectedAction, actualAction);
    }

    public void testGenerateOutputAction() {
        String expectedAction =
                "http://ws.apache.org/axis2/actiontest/withoutWSAWAction/NamedOutput";
        PortType pt = definition.getPortType(
                new QName("http://ws.apache.org/axis2/actiontest/", "withoutWSAWAction"));
        List operations = pt.getOperations();
        Operation op = (Operation) operations.get(0);
        Output out = op.getOutput();
        String actualAction =
                WSDL11ActionHelper.getActionFromOutputElement(definition, pt, op, out);
        assertEquals(expectedAction, actualAction);
    }

    public void testGenerateFaultAction() {
        String expectedAction =
                "http://ws.apache.org/axis2/actiontest/withoutWSAWAction/echo/Fault/echoFault";
        PortType pt = definition.getPortType(
                new QName("http://ws.apache.org/axis2/actiontest/", "withoutWSAWAction"));
        List operations = pt.getOperations();
        Operation op = (Operation) operations.get(0);
        Fault fault = op.getFault("echoFault");
        String actualAction =
                WSDL11ActionHelper.getActionFromFaultElement(definition, pt, op, fault);
        assertEquals(expectedAction, actualAction);
    }

    // Test reading wsaw:Action values
    // portType=withWSAWAction
    // operation=echo
    public void testGetWSAWInputAction() {
        String expectedAction = "http://example.org/action/echoIn";
        PortType pt = definition
                .getPortType(new QName("http://ws.apache.org/axis2/actiontest/", "withWSAWAction"));
        List operations = pt.getOperations();
        Operation op = (Operation) operations.get(0);
        Input in = op.getInput();
        String actualAction = WSDL11ActionHelper.getActionFromInputElement(definition, pt, op, in);
        assertEquals(expectedAction, actualAction);
    }

    public void testGetWSAWOutputAction() {
        String expectedAction = "http://example.org/action/echoOut";
        PortType pt = definition
                .getPortType(new QName("http://ws.apache.org/axis2/actiontest/", "withWSAWAction"));
        List operations = pt.getOperations();
        Operation op = (Operation) operations.get(0);
        Output out = op.getOutput();
        String actualAction =
                WSDL11ActionHelper.getActionFromOutputElement(definition, pt, op, out);
        assertEquals(expectedAction, actualAction);
    }

    public void testGetWSAWFaultAction() {
        String expectedAction = "http://example.org/action/echoFault";
        PortType pt = definition
                .getPortType(new QName("http://ws.apache.org/axis2/actiontest/", "withWSAWAction"));
        List operations = pt.getOperations();
        Operation op = (Operation) operations.get(0);
        Fault fault = op.getFault("echoFault");
        String actualAction =
                WSDL11ActionHelper.getActionFromFaultElement(definition, pt, op, fault);
        assertEquals(expectedAction, actualAction);
    }
}
