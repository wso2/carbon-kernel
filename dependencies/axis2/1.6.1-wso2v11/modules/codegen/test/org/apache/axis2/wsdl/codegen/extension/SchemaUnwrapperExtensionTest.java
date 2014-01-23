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

package org.apache.axis2.wsdl.codegen.extension;

import junit.framework.TestCase;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.wsdl.WSDLUtil;
import org.apache.axis2.wsdl.codegen.CodeGenerationException;
import org.apache.axis2.wsdl.util.Constants;
import org.apache.axis2.wsdl.util.MessagePartInformationHolder;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;

import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

public class SchemaUnwrapperExtensionTest extends TestCase {

    private AxisMessage axisMessage;
    private AxisService axisService;

    private static final String PARAMETER_ONE = "ParameterOne";
    private static final String PARAMETER_TWO = "ParameterTwo";
    private static final String PARAMETER_THREE = "ParameterThree";
    private static final String PARAMETER_FOUR = "ParameterFour";
    private static final String ADD_OPERATION = "Add";

    protected void setUp() throws Exception {
        AxisOperation axisOperation = new InOutAxisOperation(new QName(ADD_OPERATION));
        axisMessage = new AxisMessage();
        axisMessage.setName("AddRequest");
        axisMessage.setElementQName(new QName("http://ws.apache.org/schemas/axis2", "AddRequest"));
        axisOperation.addMessage(axisMessage, WSDLConstants.MESSAGE_LABEL_IN_VALUE);
        axisMessage.setParent(axisOperation);

        axisService = new AxisService("DummyService");
        axisService.addOperation(axisOperation);
        axisOperation.setParent(axisService);
    }

    /** This refers to the schema-1.xsd which has an AddRequest element which is of complex type */
    public void testScenarioOne() {
        String schemaLocation = "test-resources/schemas/schema-1.xsd";

        createAndWalkSchema(schemaLocation);

        assertTrue(axisMessage.getParameter(Constants.UNWRAPPED_KEY).getValue() == Boolean.TRUE);

        Parameter parameter = axisMessage.getParameter(Constants.UNWRAPPED_DETAILS);
        MessagePartInformationHolder messagePartInformationHolder =
                (MessagePartInformationHolder)parameter.getValue();
        List partsList = messagePartInformationHolder.getPartsList();

        assertTrue(partsList.contains(WSDLUtil.getPartQName(ADD_OPERATION,
                                                            WSDLConstants.INPUT_PART_QNAME_SUFFIX,
                                                            PARAMETER_ONE)));
        assertTrue(partsList.contains(WSDLUtil.getPartQName(ADD_OPERATION,
                                                            WSDLConstants.INPUT_PART_QNAME_SUFFIX,
                                                            PARAMETER_TWO)));
        assertTrue(partsList.size() == 2);


    }

    /**
     * This refers to the schema-2.xsd which has an AddRequest element which is of AddRequestType.
     * AddRequestType is a complex type
     */
    public void testScenarioTwo() {
        String schemaLocation = "test-resources/schemas/schema-2.xsd";

        createAndWalkSchema(schemaLocation);

        assertTrue(axisMessage.getParameter(Constants.UNWRAPPED_KEY).getValue() == Boolean.TRUE);

        Parameter parameter = axisMessage.getParameter(Constants.UNWRAPPED_DETAILS);
        MessagePartInformationHolder messagePartInformationHolder =
                (MessagePartInformationHolder)parameter.getValue();
        List partsList = messagePartInformationHolder.getPartsList();

        assertTrue(partsList.contains(WSDLUtil.getPartQName(ADD_OPERATION,
                                                            WSDLConstants.INPUT_PART_QNAME_SUFFIX,
                                                            PARAMETER_ONE)));
        assertTrue(partsList.contains(WSDLUtil.getPartQName(ADD_OPERATION,
                                                            WSDLConstants.INPUT_PART_QNAME_SUFFIX,
                                                            PARAMETER_TWO)));
        assertTrue(partsList.size() == 2);
    }

    /**
     * 1. AddRequest is of AddRequestType 2. AddRequestType extends from AbstractParameterType 3.
     * AbstractParameterType has primitive types only
     */
    public void testScenarioThree() {
        String schemaLocation = "test-resources/schemas/schema-3.xsd";

        createAndWalkSchema(schemaLocation);

        assertTrue(axisMessage.getParameter(Constants.UNWRAPPED_KEY).getValue() == Boolean.TRUE);

        Parameter parameter = axisMessage.getParameter(Constants.UNWRAPPED_DETAILS);
        MessagePartInformationHolder messagePartInformationHolder =
                (MessagePartInformationHolder)parameter.getValue();
        List partsList = messagePartInformationHolder.getPartsList();

        assertTrue(partsList.contains(WSDLUtil.getPartQName(ADD_OPERATION,
                                                            WSDLConstants.INPUT_PART_QNAME_SUFFIX,
                                                            PARAMETER_ONE)));
        assertTrue(partsList.contains(WSDLUtil.getPartQName(ADD_OPERATION,
                                                            WSDLConstants.INPUT_PART_QNAME_SUFFIX,
                                                            PARAMETER_TWO)));
        assertTrue(partsList.size() == 2);
    }

    /**
     * 1. AddRequest is of AddRequestType 2. AddRequestType extends from AbstractParameterType and
     * it AddRequestType has more stuff defined in a sequence, in addition to the extension. 3.
     * AbstractParameterType has primitive types only
     */
    public void testScenarioFour() {
        String schemaLocation = "test-resources/schemas/schema-4.xsd";

        createAndWalkSchema(schemaLocation);

        assertTrue(axisMessage.getParameter(Constants.UNWRAPPED_KEY).getValue() == Boolean.TRUE);

        Parameter parameter = axisMessage.getParameter(Constants.UNWRAPPED_DETAILS);
        MessagePartInformationHolder messagePartInformationHolder =
                (MessagePartInformationHolder)parameter.getValue();
        List partsList = messagePartInformationHolder.getPartsList();

        assertTrue(partsList.contains(WSDLUtil.getPartQName(ADD_OPERATION,
                                                            WSDLConstants.INPUT_PART_QNAME_SUFFIX,
                                                            PARAMETER_ONE)));
        assertTrue(partsList.contains(WSDLUtil.getPartQName(ADD_OPERATION,
                                                            WSDLConstants.INPUT_PART_QNAME_SUFFIX,
                                                            PARAMETER_TWO)));
        assertTrue(partsList.contains(WSDLUtil.getPartQName(ADD_OPERATION,
                                                            WSDLConstants.INPUT_PART_QNAME_SUFFIX,
                                                            PARAMETER_THREE)));
        assertTrue(partsList.contains(WSDLUtil.getPartQName(ADD_OPERATION,
                                                            WSDLConstants.INPUT_PART_QNAME_SUFFIX,
                                                            PARAMETER_FOUR)));
        assertTrue(partsList.size() == 4);
    }

    private void createAndWalkSchema(String schemaLocation) {
        try {
            XmlSchema xmlSchema = loadSchema(schemaLocation);
            axisService.addSchema(xmlSchema);
            SchemaUnwrapperExtension extension = new SchemaUnwrapperExtension();
            extension.walkSchema(axisMessage, WSDLConstants.INPUT_PART_QNAME_SUFFIX);
        } catch (FileNotFoundException e) {
            fail(schemaLocation + " file can not be found");
        } catch (CodeGenerationException e) {
            fail(e.getMessage());
        }
    }

    private XmlSchema loadSchema(String schemaLocation) throws FileNotFoundException {
        InputStream is = new FileInputStream(schemaLocation);
        XmlSchemaCollection schemaCol = new XmlSchemaCollection();
        return schemaCol.read(new StreamSource(is), null);
    }
}
