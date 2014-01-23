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

package org.apache.axis2.engine.util;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.axiom.soap.SOAPFaultDetail;
import org.apache.axiom.soap.SOAPFaultReason;
import org.apache.axiom.soap.SOAPFaultText;
import org.apache.axiom.soap.SOAPFaultValue;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.handlers.AbstractHandler;

import javax.xml.namespace.QName;

public class FaultHandler extends AbstractHandler {
    public static final String FAULT_REASON =
            "This is a test fault message which happened suddenly";
    public static final String DETAIL_MORE_INFO =
            "This error is a result due to a fake problem in Axis2 engine. Do not worry ;)";
    public static final String M_FAULT_EXCEPTION = "m:FaultException";

    public static final String ERR_HANDLING_WITH_MSG_CTXT = "ErrorHandlingWithParamsSetToMsgCtxt";
    public static final String ERR_HANDLING_WITH_AXIS_FAULT =
            "ErrorHandlingWithParamsSetToAxisFault";

    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
        // this handler will be used to check the fault handling of Axis2.
        // this will create some dummy faults and send

        SOAPFactory soapFac = msgContext.isSOAP11() ? OMAbstractFactory.getSOAP11Factory() :
                OMAbstractFactory.getSOAP12Factory();

        // I have a sudden fake error ;)
        OMElement firstElement = msgContext.getEnvelope().getBody().getFirstElement();

        OMElement detailEntry = soapFac.createOMElement("MoreInfo", null);
        detailEntry.setText(DETAIL_MORE_INFO);

        if (ERR_HANDLING_WITH_MSG_CTXT.equals(firstElement.getLocalName())) {
            SOAPFaultCode soapFaultCode = soapFac.createSOAPFaultCode();
            soapFaultCode.declareNamespace("http://someuri.org", "m");
            if (msgContext.isSOAP11()) {
                soapFaultCode.setText(M_FAULT_EXCEPTION);
            } else {
                SOAPFaultValue soapFaultValue = soapFac.createSOAPFaultValue(soapFaultCode);
                soapFaultValue.setText(M_FAULT_EXCEPTION);
            }

            SOAPFaultReason soapFaultReason = soapFac.createSOAPFaultReason();

            if (msgContext.isSOAP11()) {
                soapFaultReason.setText(FAULT_REASON);
            } else {
                SOAPFaultText soapFaultText = soapFac.createSOAPFaultText();
                soapFaultText.setLang("en");
                soapFaultText.setText(FAULT_REASON);
                soapFaultReason.addSOAPText(soapFaultText);
            }

            SOAPFaultDetail faultDetail = soapFac.createSOAPFaultDetail();
            faultDetail.addDetailEntry(detailEntry);

            msgContext.setProperty(SOAP12Constants.SOAP_FAULT_CODE_LOCAL_NAME, soapFaultCode);
            msgContext.setProperty(SOAP12Constants.SOAP_FAULT_REASON_LOCAL_NAME, soapFaultReason);
            msgContext.setProperty(SOAP12Constants.SOAP_FAULT_DETAIL_LOCAL_NAME, faultDetail);

            throw new AxisFault("A dummy exception has occurred");
        } else if (ERR_HANDLING_WITH_AXIS_FAULT.equals(firstElement.getLocalName())) {
            throw new AxisFault(new QName(M_FAULT_EXCEPTION), FAULT_REASON, null, null,
                                detailEntry);
        }
        return InvocationResponse.CONTINUE;
    }
}
