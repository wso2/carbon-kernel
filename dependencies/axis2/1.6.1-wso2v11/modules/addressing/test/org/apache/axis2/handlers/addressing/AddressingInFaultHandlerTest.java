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
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.handlers.util.TestUtil;

import javax.xml.namespace.QName;
import java.util.List;

public class AddressingInFaultHandlerTest extends TestCase {

    /** @param testName  */
    public AddressingInFaultHandlerTest(String testName) {
        super(testName);
    }

    public void testInvalidAddressingHeaderWsaToSOAP11() throws Exception {
        AxisFault af = getFaultForTest("InvalidAddressingHeader", true);
        assertNotNull(af);
        assertEquals("Wrong fault code!",
                     AddressingConstants.Final.QNAME_INVALID_HEADER,
                     af.getFaultCode());
        OMElement probHeaderDetail = af.getFaultDetailElement().getFirstChildWithName(
                AddressingConstants.Final.QNAME_PROBLEM_HEADER);
        assertNotNull(probHeaderDetail);
        assertEquals(AddressingConstants.Final.QNAME_WSA_TO,
                     probHeaderDetail.getTextAsQName());
    }

    public void testMissingActionSOAP11() throws Exception {
        AxisFault af = getFaultForTest("MessageAddressingHeaderRequired", true);
        assertNotNull(af);
    }

    public void testInvalidAddressingHeaderWsaToSOAP12() throws Exception {
        AxisFault af = getFaultForTest("InvalidAddressingHeader", false);
        assertNotNull(af);
        assertEquals("Wrong fault code", SOAP12Constants.QNAME_SENDER_FAULTCODE, af.getFaultCode());
        List subCodes = af.getFaultSubCodes();
        assertNotNull(subCodes);
        assertEquals(1, subCodes.size());
        assertEquals("Wrong fault subcode",
                     new QName(AddressingConstants.Final.WSA_NAMESPACE,
                               AddressingConstants.Final.FAULT_INVALID_HEADER),
                     subCodes.get(0));
    }

    public void testMissingActionSOAP12() throws Exception {
        AxisFault af = getFaultForTest("MessageAddressingHeaderRequired", false);
        assertNotNull(af);
    }

    private AxisFault getFaultForTest(String testName, boolean isSOAP11) throws Exception {
        String testfile =
                "fault-messages/" + (isSOAP11 ? "soap11" : "soap12") + "/" + testName + ".xml";
        SOAPEnvelope envelope = TestUtil.getSOAPEnvelope(testfile);
        MessageContext msgContext = new MessageContext();
        msgContext.setConfigurationContext(
                ConfigurationContextFactory.createEmptyConfigurationContext());
        msgContext.setEnvelope(envelope);
        AddressingInHandler afih = new AddressingInHandler();
        afih.invoke(msgContext);
        AddressingInFaultHandler aifh = new AddressingInFaultHandler();
        aifh.invoke(msgContext);

        return (AxisFault)msgContext.getProperty(Constants.INBOUND_FAULT_OVERRIDE);
    }

}
