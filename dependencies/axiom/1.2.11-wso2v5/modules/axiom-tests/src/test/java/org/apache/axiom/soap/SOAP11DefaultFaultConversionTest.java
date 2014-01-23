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

package org.apache.axiom.soap;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.dom.DOOMAbstractFactory;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;

import junit.framework.TestCase;

/**
 * Test case to check conversion of the default fault envelope generated
 * http://issues.apache.org/jira/browse/WSCOMMONS-343
 */
public class SOAP11DefaultFaultConversionTest extends TestCase {
    
    public void testConversion() {
        
        String faultCode = "soapenv" + ":" + "Server";
        String faultReason = "/ by zero";
        String faultDetail = "org.apache.axis2.AxisFault: / by zero \n" +
                             "... 24 more)";
        
        SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
        
        SOAPEnvelope envelope = factory.getDefaultFaultEnvelope();
        SOAPFault fault = envelope.getBody().getFault();
        
        fault.getCode().setText(faultCode);
        
        fault.getReason().setText(faultReason);
        
        OMElement exception = factory.createOMElement("Exception", null);
        exception.setText(faultDetail);
        
        fault.getDetail().addDetailEntry(exception);
        
        envelope.build();
        
        factory = DOOMAbstractFactory.getSOAP11Factory();
        
        StAXSOAPModelBuilder stAXSOAPModelBuilder = new StAXSOAPModelBuilder(
                envelope.getXMLStreamReader(), factory,SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        SOAPEnvelope env = stAXSOAPModelBuilder.getSOAPEnvelope();
        ((OMNode) env.getParent()).build();
        
        fault = env.getBody().getFault();
        
        assertEquals(faultCode, fault.getCode().getText());
        assertEquals(faultReason,fault.getReason().getText());
        
        exception = (OMElement)fault.getDetail().getFirstOMChild();
        assertEquals(faultDetail, exception.getText());
        
    }

}
