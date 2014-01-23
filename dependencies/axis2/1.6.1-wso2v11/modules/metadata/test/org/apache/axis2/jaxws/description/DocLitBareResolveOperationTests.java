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

import junit.framework.TestCase;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.namespace.QName;
import javax.xml.ws.Holder;

/**
 * Validate that the information needed to resolve an incoming Doc/Lit/Bare message is setup
 * correctly via annotations.
 */
public class DocLitBareResolveOperationTests extends TestCase {
    private ServiceDescription svcDesc;
    private EndpointDescription endpointDesc;
    private AxisService axisService;

    protected void setUp() {
        if (svcDesc == null) {
            svcDesc = DescriptionFactory.createServiceDescription(DLBResolveOperation.class);
            assertNotNull(svcDesc);
            EndpointDescription[] epDescs = svcDesc.getEndpointDescriptions();
            assertNotNull(epDescs);
            assertEquals(1, epDescs.length);
            endpointDesc = epDescs[0];
            axisService = endpointDesc.getAxisService();
            assertNotNull(axisService);
        }
    }


    public void testDocLitBareSingleInParam() {
        QName operationQName = new QName("", "op1");
        AxisOperation axisOperationFromOpName = axisService.getOperation(operationQName);
        assertNotNull(axisOperationFromOpName);

        QName messageElementQName =
                new QName("org.apache.axis2.jaxws.description.DLBResolveOperation",
                          "op1param1PartName");
        AxisOperation axisOperationFromElement =
                axisService.getOperationByMessageElementQName(messageElementQName);
        assertNotNull(axisOperationFromElement);
        assertEquals(axisOperationFromOpName, axisOperationFromElement);

        QName wrongMessageElementQName =
                new QName("org.apache.axis2.jaxws.description.DLBResolveOperation",
                          "wrongElementName");
        AxisOperation axisOperationFromWrongElement =
                axisService.getOperationByMessageElementQName(wrongMessageElementQName);
        assertNull(axisOperationFromWrongElement);

        QName wrongMessageElementNSQName = new QName("wrong.namespace",
                                                     "op1param1PartName");
        AxisOperation axisOperationFromWrongElementNS =
                axisService.getOperationByMessageElementQName(wrongMessageElementNSQName);
        assertNull(axisOperationFromWrongElementNS);

    }

    public void testDocLitBareSingleInOutParam() {
        QName operationQName = new QName("", "op2");
        AxisOperation axisOperationFromOpName = axisService.getOperation(operationQName);
        assertNotNull(axisOperationFromOpName);

        QName messageElementQName =
                new QName("org.apache.axis2.jaxws.description.DLBResolveOperation",
                          "op2param1PartName");
        AxisOperation axisOperationFromElement =
                axisService.getOperationByMessageElementQName(messageElementQName);
        assertNotNull(axisOperationFromElement);
        assertEquals(axisOperationFromOpName, axisOperationFromElement);
    }

    public void testDocLitBareTwoInParams() {
        QName operationQName = new QName("", "op3");
        AxisOperation axisOperationFromOpName = axisService.getOperation(operationQName);
        assertNotNull(axisOperationFromOpName);

        QName messageElementQName =
                new QName("org.apache.axis2.jaxws.description.DLBResolveOperation",
                          "op3param1PartName");
        AxisOperation axisOperationFromElement =
                axisService.getOperationByMessageElementQName(messageElementQName);
        assertNotNull(axisOperationFromElement);
        assertEquals(axisOperationFromOpName, axisOperationFromElement);

        QName wrongMessageElementQName =
                new QName("org.apache.axis2.jaxws.description.DLBResolveOperation",
                          "op3param2PartName");
        AxisOperation axisOperationFromWrongElement =
                axisService.getOperationByMessageElementQName(wrongMessageElementQName);
        assertNull(axisOperationFromWrongElement);

    }

    public void testDocLitBareOutParamOnly() {
        QName operationQName = new QName("", "op4");
        AxisOperation axisOperationFromOpName = axisService.getOperation(operationQName);
        assertNotNull(axisOperationFromOpName);

        QName messageElementQName =
                new QName("org.apache.axis2.jaxws.description.DLBResolveOperation",
                          "op4param1PartName");
        AxisOperation axisOperationFromElement =
                axisService.getOperationByMessageElementQName(messageElementQName);
        assertNull(axisOperationFromElement);

    }

    public void testDocLitBareOutParamFirst() {
        QName operationQName = new QName("", "op5");
        AxisOperation axisOperationFromOpName = axisService.getOperation(operationQName);
        assertNotNull(axisOperationFromOpName);

        QName messageElementQName =
                new QName("org.apache.axis2.jaxws.description.DLBResolveOperation",
                          "op5param2PartName");
        AxisOperation axisOperationFromElement =
                axisService.getOperationByMessageElementQName(messageElementQName);
        assertNotNull(axisOperationFromElement);
        assertEquals(axisOperationFromOpName, axisOperationFromElement);

        QName wrongMessageElementQName =
                new QName("org.apache.axis2.jaxws.description.DLBResolveOperation",
                          "op5param1PartName");
        AxisOperation axisOperationFromWrongElement =
                axisService.getOperationByMessageElementQName(wrongMessageElementQName);
        assertNull(axisOperationFromWrongElement);

    }

    public void testDocLitBareDefaultPartAnno() {
        QName operationQName = new QName("", "op6");
        AxisOperation axisOperationFromOpName = axisService.getOperation(operationQName);
        assertNotNull(axisOperationFromOpName);

        QName messageElementQName = new QName("http://description.jaxws.axis2.apache.org/",
                                              "op6");
        AxisOperation axisOperationFromElement =
                axisService.getOperationByMessageElementQName(messageElementQName);
        assertNotNull(axisOperationFromElement);
        assertEquals(axisOperationFromOpName, axisOperationFromElement);

    }

    public void testDocLitBareNoParams() {
        QName operationQName = new QName("", "op7");
        AxisOperation axisOperationFromOpName = axisService.getOperation(operationQName);
        assertNotNull(axisOperationFromOpName);

        QName messageElementQName = new QName("http://description.jaxws.axis2.apache.org/",
                                              "op7");
        // We should *not* be able to find the operation based on a QName containing the name, but
        // we should be able to find it based on a null.  That is because a Doc/Lit/Bare no-argument 
        // message will have an empty soap:Body
        AxisOperation axisOperationFromElement =
                axisService.getOperationByMessageElementQName(messageElementQName);
        assertNull(axisOperationFromElement);
        axisOperationFromElement =
            axisService.getOperationByMessageElementQName(null);
        assertNotNull(axisOperationFromElement);
        assertEquals(axisOperationFromOpName, axisOperationFromElement);
    }

    public void testDocLitBareHeaderParamFirst() {
        QName operationQName = new QName("", "op8");
        AxisOperation axisOperationFromOpName = axisService.getOperation(operationQName);
        assertNotNull(axisOperationFromOpName);

        QName messageElementQName =
                new QName("org.apache.axis2.jaxws.description.DLBResolveOperation",
                          "op8param2PartName");
        AxisOperation axisOperationFromElement =
                axisService.getOperationByMessageElementQName(messageElementQName);
        assertNotNull(axisOperationFromElement);
        assertEquals(axisOperationFromOpName, axisOperationFromElement);

        QName wrongMessageElementQName =
                new QName("org.apache.axis2.jaxws.description.DLBResolveOperation",
                          "op8param1PartName");
        AxisOperation axisOperationFromWrongElement =
                axisService.getOperationByMessageElementQName(wrongMessageElementQName);
        assertNull(axisOperationFromWrongElement);
    }


    public void testDocLitWrapped() {
        QName operationQName = new QName("", "op10");
        AxisOperation axisOperationFromOpName = axisService.getOperation(operationQName);
        assertNotNull(axisOperationFromOpName);

        QName messageElementQName =
                new QName("org.apache.axis2.jaxws.description.DLBResolveOperation",
                          "op10param1PartName");
        AxisOperation axisOperationFromElement =
                axisService.getOperationByMessageElementQName(messageElementQName);
        assertNull(axisOperationFromElement);
    }
}

@WebService
class DLBResolveOperation {

    @SOAPBinding(style = SOAPBinding.Style.DOCUMENT, use = SOAPBinding.Use.LITERAL,
                 parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public void op1(
            @WebParam(targetNamespace = "org.apache.axis2.jaxws.description.DLBResolveOperation",
                      name = "op1param1PartName") int param1) {
    }

    @SOAPBinding(style = SOAPBinding.Style.DOCUMENT, use = SOAPBinding.Use.LITERAL,
                 parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public void op2(
            @WebParam(targetNamespace = "org.apache.axis2.jaxws.description.DLBResolveOperation",
                      name = "op2param1PartName",
                      mode = WebParam.Mode.INOUT) Holder<Integer> param1) {
    }

    @SOAPBinding(style = SOAPBinding.Style.DOCUMENT, use = SOAPBinding.Use.LITERAL,
                 parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public void op3(
            @WebParam(targetNamespace = "org.apache.axis2.jaxws.description.DLBResolveOperation",
                      name = "op3param1PartName",
                      mode = WebParam.Mode.INOUT) Holder<Integer> param1,
            @WebParam(targetNamespace = "org.apache.axis2.jaxws.description.DLBResolveOperation",
                      name = "op3param2PartName",
                      mode = WebParam.Mode.IN) int param2) {
    }

    @SOAPBinding(style = SOAPBinding.Style.DOCUMENT, use = SOAPBinding.Use.LITERAL,
                 parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public void op4(
            @WebParam(targetNamespace = "org.apache.axis2.jaxws.description.DLBResolveOperation",
                      name = "op4param1PartName",
                      mode = WebParam.Mode.OUT) Holder<Integer> param1) {
    }

    @SOAPBinding(style = SOAPBinding.Style.DOCUMENT, use = SOAPBinding.Use.LITERAL,
                 parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public void op5(
            @WebParam(targetNamespace = "org.apache.axis2.jaxws.description.DLBResolveOperation",
                      name = "op5param1PartName",
                      mode = WebParam.Mode.OUT) Holder<Integer> param1,
            @WebParam(targetNamespace = "org.apache.axis2.jaxws.description.DLBResolveOperation",
                      name = "op5param2PartName",
                      mode = WebParam.Mode.IN) int param2) {
    }

    @SOAPBinding(style = SOAPBinding.Style.DOCUMENT, use = SOAPBinding.Use.LITERAL,
                 parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public void op6(
            @WebParam int param1) {
    }

    @SOAPBinding(style = SOAPBinding.Style.DOCUMENT, use = SOAPBinding.Use.LITERAL,
                 parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public void op7() {
    }

    @SOAPBinding(style = SOAPBinding.Style.DOCUMENT, use = SOAPBinding.Use.LITERAL,
                 parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public void op8(
            @WebParam(targetNamespace = "org.apache.axis2.jaxws.description.DLBResolveOperation",
                      name = "op8param1PartName",
                      mode = WebParam.Mode.IN,
                      header = true) Integer param1,
            @WebParam(targetNamespace = "org.apache.axis2.jaxws.description.DLBResolveOperation",
                      name = "op8param2PartName",
                      mode = WebParam.Mode.IN) int param2) {
    }

    @SOAPBinding(style = SOAPBinding.Style.DOCUMENT, use = SOAPBinding.Use.LITERAL,
                 parameterStyle = SOAPBinding.ParameterStyle.WRAPPED)
    public void op10(
            @WebParam(targetNamespace = "org.apache.axis2.jaxws.description.DLBResolveOperation",
                      name = "op10param1PartName") int param1) {
    }

}
