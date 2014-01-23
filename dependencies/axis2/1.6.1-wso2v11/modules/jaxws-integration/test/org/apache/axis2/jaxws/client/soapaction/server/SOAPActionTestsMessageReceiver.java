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

package org.apache.axis2.jaxws.client.soapaction.server;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMText;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.util.MessageContextBuilder;

import javax.xml.namespace.QName;
import java.util.Iterator;

/**
 * This MessageReceiver is used for a series of tests relating to the
 * SOAP action.  This is mostly for testing that the correct operation
 * was resolved based on the SOAP action that was sent by the client.  
 */
public class SOAPActionTestsMessageReceiver implements MessageReceiver {
    
    public void receive(MessageContext request) throws AxisFault {
        TestLogger.logger.debug("[server] SOAPActionTestsMessageReceiver: new request received");
        
        SOAPEnvelope env = request.getEnvelope();
        TestLogger.logger.debug("[server] request message [" + env + "]");
        
        // Get the first child element
        Iterator itr = env.getBody().getChildElements();
        OMElement child = (OMElement) itr.next();

        // Create the envelope for the response
        SOAPFactory sf = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope responseEnv = sf.createSOAPEnvelope();
        sf.createSOAPBody(responseEnv);
        OMElement responseBodyContent = null;
        
        // Check to see which operation was invoked and then validate the contents 
        // of the request (resolved AxisOperation and the soap action) to see if they are correct.
        String name = child.getLocalName();
        if (name.equals("getPrice")) {
            float status = 0;
            if (checkOperation("getPrice", request) &&
                checkSOAPAction("", request)) {
                TestLogger.logger.debug("[server] all checks passed");
                status = 1;
            }
            else {
                TestLogger.logger.debug("[server] some checks failed");
            }
            
            responseBodyContent = sf.createOMElement(new QName("http://jaxws.axis2.apache.org/client/soapaction", "getPriceWithActionResponse"), responseEnv.getBody());
            OMElement elem = sf.createOMElement(new QName("", "price"), responseBodyContent);
            OMText text = sf.createOMText(Float.toString(status));
            elem.addChild(text);
        }
        else if (name.equals("getPriceWithAction")) {
            float status = 0;
            if (checkOperation("getPriceWithAction", request) &&
                checkSOAPAction("http://jaxws.axis2.apache.org/client/soapaction/getPrice", request)) {
                TestLogger.logger.debug("[server] all checks passed");
                status = 1;
            }
            else {
                TestLogger.logger.debug("[server] some checks failed");
            }
            
            responseBodyContent = sf.createOMElement(new QName("http://jaxws.axis2.apache.org/client/soapaction", "getPriceWithActionResponse"), responseEnv.getBody());
            OMElement elem = sf.createOMElement(new QName("", "price"), responseBodyContent);
            OMText text = sf.createOMText(Float.toString(status));
            elem.addChild(text);
        }
        
        /*
        else if (name.equals("item")) {
            if (checkOperation("getInventory", request) &&
                checkSOAPAction("", request)) {
                status = STATUS_PASS;
            }
        }
        else if (name.equals("itemWithAction")) {
            if (checkOperation("getInventoryWithAction", request) &&
                checkSOAPAction("http://jaxws.axis2.apache.org/client/soapaction/getInventory", request)) {
                status = STATUS_PASS;
            }
        }
        */
        
        // Fill in the contents of the response and send it back
        MessageContext response = MessageContextBuilder.createOutMessageContext(request);
        responseEnv.getBody().addChild(responseBodyContent);
        response.setEnvelope(responseEnv);

        TestLogger.logger.debug("[server] response message [" + responseEnv.toString() + "]");
        
        response.getOperationContext().addMessageContext(response);
        AxisEngine.send(response);    
    }
    
    /*
     * Verify that the AxisOperation on the MessageContext is the 
     * one that we were expecting based on the request.
     */
    private boolean checkOperation(String expectedOperationName, MessageContext mc) {
        AxisOperation op = mc.getAxisOperation();
        TestLogger.logger.debug("[server] checking expected operation [" + expectedOperationName +
                "] against resolved operation [" + op.getName() + "]");
        if (op.getName().getLocalPart().equals(expectedOperationName)) {
            TestLogger.logger.debug("[server] operation name is correct");
            return true;
        }
        else {
            TestLogger.logger.debug("[server] operation name is incorrect");
            return false;
        }
    }
    
    /*
     * Verify that the SOAPAction present on the MessageContext is
     * the one that we were expecting based on the request.
     */
    private boolean checkSOAPAction(String expectedAction, MessageContext mc) {
       String action = mc.getSoapAction();
        TestLogger.logger.debug("[server] checking expected action [" + expectedAction +
                "] against received action [" + action + "]");
       if (action != null && action.equals(expectedAction)) {
           TestLogger.logger.debug("[server] soap action is correct");
           return true;
       }           
       else {
           TestLogger.logger.debug("[server] soap action is incorrect");
           return false;
       }   
    }
}
