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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.axiom.soap.SOAPFaultDetail;
import org.apache.axiom.soap.SOAPFaultReason;
import org.apache.axiom.soap.SOAPFaultSubCode;
import org.apache.axiom.soap.SOAPFaultText;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.AddressingFaultsHelper;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.handlers.AbstractHandler;

import java.util.Iterator;

/**
 * This class is used to extract WS-Addressing Spec defined Faults and FaultDetail and convert them
 * into understandable AxisFault objects.
 */
public class AddressingInFaultHandler extends AbstractHandler implements AddressingConstants {

    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
        String action = msgContext.getWSAAction();

        if (Final.WSA_FAULT_ACTION.equals(action)
                || Final.WSA_SOAP_FAULT_ACTION.equals(action)
                || Submission.WSA_FAULT_ACTION.equals(action)) {
            SOAPEnvelope envelope = msgContext.getEnvelope();
            SOAPFault fault = envelope.getBody().getFault();

            if (fault == null) {
                throw new AxisFault("A Soap envelope with fault action -" + action + " has been received without " +
                        "a fault element in the soap body");
            }

            SOAPFactory sf = ((SOAPFactory)envelope.getOMFactory());
            SOAPFaultDetail detail = null;

            if (msgContext.isSOAP11()) {
                SOAPHeader header = envelope.getHeader();
                OMElement element = header.getFirstChildWithName(Final.QNAME_WSA_HEADER_DETAIL);
                if (element != null) {
                    detail = sf.createSOAPFaultDetail(fault);
                    Iterator i = element.getChildElements();
                    while (i.hasNext()) {
                        OMElement detailElement = (OMElement)i.next();
                        detail.addDetailEntry(detailElement);
                    }
                }
            } else {
                detail = fault.getDetail();
            }

            String faultDetailString = null;
            if (detail != null) {
                OMElement element = detail.getFirstElement();
                if (element != null) {
                    faultDetailString = element.getText();
                }
            }

            String faultLocalName;
            SOAPFaultCode code = fault.getCode();
            SOAPFaultSubCode subCode = code.getSubCode();
            if (subCode == null) {
                faultLocalName = code.getTextAsQName().getLocalPart();
            } else {
                while (subCode.getSubCode() != null) {
                    subCode = subCode.getSubCode();
                }
                faultLocalName = subCode.getValue().getTextAsQName().getLocalPart();
            }

            String newReason = AddressingFaultsHelper
                    .getMessageForAxisFault(faultLocalName, faultDetailString);

            if (newReason != null) {
                SOAPFaultReason sfr = sf.createSOAPFaultReason();
                if (envelope.getNamespace().getNamespaceURI()
                        .equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
                    sfr.setText(newReason);
                } else {
                    SOAPFaultText sft = sf.createSOAPFaultText();
                    sft.setText(newReason);
                    sfr.addSOAPText(sft);
                }
                // else call the on error method with the fault
                AxisFault axisFault = new AxisFault(fault.getCode(), sfr,
                                                    fault.getNode(), fault.getRole(),
                                                    detail);
                msgContext.setProperty(Constants.INBOUND_FAULT_OVERRIDE, axisFault);
            }
        }

        return InvocationResponse.CONTINUE;
    }
}
