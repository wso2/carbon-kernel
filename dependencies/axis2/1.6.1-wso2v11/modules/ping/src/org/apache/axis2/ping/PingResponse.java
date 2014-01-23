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

package org.apache.axis2.ping;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;

import javax.xml.namespace.QName;

public class PingResponse implements PingConstants {

    private OMElement pingResponse;
    private SOAPFactory soapFactory;
    private OMNamespace pingNamespace;

    public PingResponse() {

    }

    public void initPingResponse(MessageContext msgContext) {
        if (msgContext.isSOAP11()) {
            soapFactory = OMAbstractFactory.getSOAP11Factory();
        } else {
            soapFactory = OMAbstractFactory.getSOAP12Factory();
        }

        OMElement pingRequestElement = msgContext.getEnvelope().
                getBody().getFirstChildWithName(new QName(TYPE_PING_REQUEST));

        if (pingRequestElement != null) {
            pingNamespace = pingRequestElement.getNamespace();
        } else {
            pingNamespace = soapFactory.createOMNamespace(PING_NAMESPACE,"");
        }

        pingResponse = soapFactory.createOMElement(TYPE_PING_RESPONSE, pingNamespace);
    }

    public void addOperationStatus(String operationName, int opStatus) {
        OMElement status = soapFactory.createOMElement(TAG_STATUS, pingNamespace);
        //TODO Send the integer value or the string description
        status.addChild(soapFactory.createOMText(Integer.toString(opStatus)));

        OMElement operation = soapFactory.createOMElement(TAG_OPERATION_STATUS, pingNamespace);
        operation.addAttribute(ATTRIBUTE_NAME, operationName, pingNamespace);
        operation.addChild(status);

        pingResponse.addChild(operation);
    }

    public void addOperationStatus(AxisOperation axisOperation, int opStatus) {
        addOperationStatus(axisOperation.getName().getLocalPart(), opStatus);
    }

    public SOAPEnvelope getResposeEnvelope() {
        SOAPEnvelope envelope = soapFactory.getDefaultEnvelope();
        envelope.getBody().addChild(pingResponse);
        return envelope;
    }
}