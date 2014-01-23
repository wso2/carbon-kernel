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

import javax.xml.namespace.QName;

public class FaultThrowingService {

    public static final String THROW_FAULT_AS_AXIS_FAULT = "ThrowFaultAsAxisFault";
    public static final String THROW_FAULT_WITH_MSG_CTXT = "ThrowFaultWithMsgCtxt";

    private SOAPFaultCode soapFaultCode;
    private SOAPFaultReason soapFaultReason;
    private SOAPFaultDetail soapFaultDetail;

    public OMElement echoWithFault(OMElement echoOMElement) throws AxisFault {
        String text = echoOMElement.getText();
        if (THROW_FAULT_AS_AXIS_FAULT.equalsIgnoreCase(text)) {
            throw new AxisFault(new QName("http://test.org", "TestFault", "test"), "FaultReason",
                                new Exception("This is a test Exception"));
        } else if (THROW_FAULT_WITH_MSG_CTXT.equalsIgnoreCase(text)) {
            MessageContext inMessageContext = MessageContext.getCurrentMessageContext();
            initFaultInformation(inMessageContext);

            inMessageContext.setProperty(SOAP12Constants.SOAP_FAULT_CODE_LOCAL_NAME, soapFaultCode);
            inMessageContext
                    .setProperty(SOAP12Constants.SOAP_FAULT_REASON_LOCAL_NAME, soapFaultReason);
            inMessageContext
                    .setProperty(SOAP12Constants.SOAP_FAULT_DETAIL_LOCAL_NAME, soapFaultDetail);
            throw new AxisFault("Fake exception occurred !!");
        } else {
            return echoOMElement;
        }
    }

    private void initFaultInformation(MessageContext inMessageContext) {
        SOAPFactory soapFactory;
        if (inMessageContext.isSOAP11()) {
            soapFactory = OMAbstractFactory.getSOAP11Factory();
        } else {
            soapFactory = OMAbstractFactory.getSOAP12Factory();
        }

        soapFaultCode = soapFactory.createSOAPFaultCode();
        SOAPFaultValue soapFaultValue = soapFactory.createSOAPFaultValue(soapFaultCode);
        soapFaultValue.setText(new QName("http://test.org", "TestFault", "test"));

        soapFaultReason = soapFactory.createSOAPFaultReason();
        SOAPFaultText soapFaultText = soapFactory.createSOAPFaultText(soapFaultReason);
        soapFaultText.setText("This is some FaultReason");

        soapFaultDetail = soapFactory.createSOAPFaultDetail();
        QName qName = new QName("http://someuri.org", "FaultException");
        OMElement detail = soapFactory.createOMElement(qName, soapFaultDetail);
        qName = new QName("http://someuri.org", "ExceptionMessage");
        Throwable e = new Exception("This is a test Exception");
        while (e != null) {
            OMElement exception = soapFactory.createOMElement(qName, null);
            exception.setText(e.getMessage());
            detail.addChild(exception);
            e = e.getCause();
        }
    }


}
