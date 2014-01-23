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

package org.apache.axis2.faults;

import junit.framework.TestCase;
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
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.util.MessageContextBuilder;
import org.apache.axis2.util.Utils;

import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;

public class FaultSerializationTest extends TestCase {
    public void testFaultSerialization() throws Exception {
        final String REASON = "ReasonValue";

        SOAPFactory soapFactory = OMAbstractFactory.getSOAP12Factory();
        SOAPFaultCode soapFaultCode = soapFactory.createSOAPFaultCode();
        SOAPFaultValue soapFaultValue = soapFactory
                .createSOAPFaultValue(soapFaultCode);
        soapFaultValue.setText(new QName(
                SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI, "Sender"));

        SOAPFaultReason soapFaultReason = soapFactory.createSOAPFaultReason();
        SOAPFaultText soapFaultText = soapFactory
                .createSOAPFaultText(soapFaultReason);
        soapFaultText.setText(REASON);

        SOAPFaultDetail soapFaultDetail = soapFactory.createSOAPFaultDetail();
        QName qName = new QName("http://mycompany.com", "FaultException", "ex");
        OMElement exception = soapFactory.createOMElement(qName,
                soapFaultDetail);
        exception.setText("Detail text");
        AxisFault fault = new AxisFault(soapFaultCode, soapFaultReason, null,
                null, soapFaultDetail);

        ConfigurationContext cc = ConfigurationContextFactory
                .createDefaultConfigurationContext();
        MessageContext ctx = cc.createMessageContext();
        SOAPFactory fac = OMAbstractFactory.getSOAP12Factory();
        ctx.setEnvelope(fac.getDefaultEnvelope());
        MessageContext faultCtx = MessageContextBuilder
                .createFaultMessageContext(ctx, fault);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TransportUtils.writeMessage(faultCtx, bos);

        String result = new String(bos.toByteArray());

        // For right now, just making sure we have a test for AXIS2-2752
        // Confirm reason was correctly processed
        assertTrue("Incorrect or missing reason!", result.indexOf(REASON) > -1);
    }

    // test for https://issues.apache.org/jira/browse/AXIS2-1703
    public void testFaultReason() throws Exception {
        SOAPFactory soapFactory = OMAbstractFactory.getSOAP12Factory();
        OMElement response = soapFactory.createOMElement(new QName("testNs",
                "test"));
        String faultReason = "myFaultReason";
        AxisFault fault = new AxisFault(new QName("myQname"), faultReason,
                "myFaultNode", "myFaultRole", response);

        ConfigurationContext cc = ConfigurationContextFactory
                .createDefaultConfigurationContext();
        MessageContext ctx = cc.createMessageContext();
        SOAPFactory fac = OMAbstractFactory.getSOAP12Factory();
        ctx.setEnvelope(fac.getDefaultEnvelope());
        MessageContext faultCtx = MessageContextBuilder
                .createFaultMessageContext(ctx, fault);

        assertEquals(faultReason, Utils.getInboundFaultFromMessageContext(
                faultCtx).getReason());
    }
}
