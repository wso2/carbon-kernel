/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.axis2.transport.sms;

import org.apache.axis2.receivers.AbstractInOutMessageReceiver;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.AxisFault;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMElement;

/**
 * A Simple Message reveiver used for the SMS Transport Test.
 */
public class SimpleInOutMessageReceiver extends AbstractInOutMessageReceiver {


    public void invokeBusinessLogic(MessageContext inMessageContext,
                                    MessageContext outMessageContext) throws AxisFault {
        log.debug("Got The message to the MessageReceiver");


        String soapNamespace = inMessageContext.getEnvelope().getNamespace().getNamespaceURI();
        // creating a soap factory according the the soap namespce uri
        SOAPFactory soapFactory = null;
        if (soapNamespace.equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI)){
            soapFactory = OMAbstractFactory.getSOAP11Factory();
        } else if (soapNamespace.equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI)){
            soapFactory = OMAbstractFactory.getSOAP12Factory();
        } else {
            System.out.println("Unknow soap message");
        }

        SOAPEnvelope responseEnvelope = soapFactory.getDefaultEnvelope();

        // creating a body element
        OMFactory omFactory = OMAbstractFactory.getOMFactory();
        OMNamespace omNamespace = omFactory.createOMNamespace("http://sms.test","ns1");
        OMElement omElement = omFactory.createOMElement("Response", omNamespace);
        omElement.setText("Sucess");
        responseEnvelope.getBody().addChild(omElement);

        outMessageContext.setEnvelope(responseEnvelope);
    }
}
