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

package org.apache.axis2.handlers.addressing;

import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.InOnlyAxisOperation;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.axis2.description.OutInAxisOperation;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.handlers.util.TestUtil;

import javax.xml.namespace.QName;

public class AddressingValidationHandlerTest extends TestCase implements AddressingConstants {
    AddressingInHandler inHandler = new AddressingInHandler();
    AddressingValidationHandler validationHandler = new AddressingValidationHandler();
    String addressingNamespace = AddressingConstants.Final.WSA_NAMESPACE;
    String versionDirectory = "final";

    protected MessageContext testMessageWithOmittedHeaders(String testName) throws Exception {
        return testAddressingMessage("omitted-header-messages", testName + "Message.xml");
    }

    protected MessageContext testAddressingMessage(String directory, String testName)
            throws Exception {
        String testfile = directory + "/" + versionDirectory + "/" + testName;

        MessageContext mc = new MessageContext();
        mc.setConfigurationContext(ConfigurationContextFactory.createEmptyConfigurationContext());
        mc.setEnvelope(TestUtil.getSOAPEnvelope(testfile));

        inHandler.invoke(mc);

        return mc;
    }

    public void testMessageWithOmittedMessageIDInOutMEP() throws Exception {
        MessageContext messageContext = testMessageWithOmittedHeaders("noMessageID");
        String messageID = messageContext.getOptions().getMessageId();

        assertNull("The message id is not null.", messageID);

        AxisOperation axisOperation = new InOutAxisOperation();
        messageContext.setAxisOperation(axisOperation);
        AxisService axisService = new AxisService();
        messageContext.setAxisService(axisService);

        try {
            validationHandler.invoke(messageContext);
            fail("An AxisFault should have been thrown due to the absence of a message id.");
        }
        catch (AxisFault af) {
            //Test passed.
        }
    }

    public void testMessageWithOmittedMessageIDInOnlyMEP() throws Exception {
        MessageContext messageContext = testMessageWithOmittedHeaders("noMessageID");
        String messageID = messageContext.getOptions().getMessageId();

        assertNull("The message id is not null.", messageID);

        AxisOperation axisOperation = new InOnlyAxisOperation();
        messageContext.setAxisOperation(axisOperation);
        AxisService axisService = new AxisService();
        messageContext.setAxisService(axisService);
        validationHandler.invoke(messageContext);
    }

    public void testMessageWithMessageIDInOutMEP() throws Exception {
        MessageContext messageContext = testMessageWithOmittedHeaders("noFrom");
        String messageID = messageContext.getOptions().getMessageId();

        assertNotNull("The message id is null.", messageID);

        AxisOperation axisOperation = new InOutAxisOperation();
        messageContext.setAxisOperation(axisOperation);
        AxisService axisService = new AxisService();
        messageContext.setAxisService(axisService);
        validationHandler.invoke(messageContext);
    }

    public void testInOutMessageWithOmittedMessageID() throws Exception {
        MessageContext messageContext = testMessageWithOmittedHeaders("noMessageID");
        String messageID = messageContext.getOptions().getMessageId();

        assertNull("The message id is not null.", messageID);

        AxisOperation axisOperation = new InOutAxisOperation();
        messageContext.setAxisOperation(axisOperation);
        AxisService axisService = new AxisService();
        messageContext.setAxisService(axisService);

        try {
            validationHandler.invoke(messageContext);
        } catch (AxisFault axisFault) {
            // Confirm this is the correct fault
            assertEquals("Wrong fault code",
                         new QName(Final.WSA_NAMESPACE,
                                   Final.FAULT_ADDRESSING_HEADER_REQUIRED),
                         axisFault.getFaultCode());
            return;
        }
        fail("Validated message with missing message ID!");
    }
    
    public void testCheckUsingAdressingOnClient() throws Exception {
        // Need to create full description hierarchy to prevent NullPointerExceptions
        AxisOperation axisOperation = new OutInAxisOperation(new QName("Temp"));
        AxisService axisService = new AxisService("Temp");
        AxisConfiguration axisConfiguration = new AxisConfiguration();
        axisService.addOperation(axisOperation);
        axisConfiguration.addService(axisService);
        ConfigurationContext configurationContext = new ConfigurationContext(axisConfiguration);

        // Make addressing required using the same property as the AddressingConfigurator on the request
        MessageContext request = configurationContext.createMessageContext();
        request.setProperty(AddressingConstants.ADDRESSING_REQUIREMENT_PARAMETER, AddressingConstants.ADDRESSING_REQUIRED);
        
        // Create a response to invoke the in handler on        
        MessageContext response = configurationContext.createMessageContext();

        // Link the response to the request message context using the context hierarchy
        ServiceGroupContext serviceGroupContext = configurationContext.createServiceGroupContext(axisService.getAxisServiceGroup());
        ServiceContext serviceContext = serviceGroupContext.getServiceContext(axisService);
        OperationContext opContext = axisOperation.findOperationContext(request, serviceContext);
        axisOperation.registerOperationContext(request, opContext);
        request.setServiceContext(serviceContext);
        response.setServiceContext(serviceContext);
        request.setOperationContext(opContext);
        response.setOperationContext(opContext);
        
        // Invoke the in handler for a response message without addressing headers
        response.setEnvelope(TestUtil.getSOAPEnvelope("addressingDisabledTest.xml"));
        inHandler.invoke(response);
        
        // Check an exception is thrown by the validation handler because the client
        // requires addressing but the response message does not have addressing headers
        try {
            validationHandler.invoke(response);
            fail("An AxisFault should have been thrown due to the absence of addressing headers.");
        } catch (AxisFault axisFault) {
            // Confirm this is the correct fault
            assertEquals("Wrong fault code",
                         new QName(Final.FAULT_ADDRESSING_HEADER_REQUIRED),
                         axisFault.getFaultCode());
        }
    }
}
