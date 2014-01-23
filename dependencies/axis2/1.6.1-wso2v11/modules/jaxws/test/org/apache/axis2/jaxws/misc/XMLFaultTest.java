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

/**
 * 
 */
package org.apache.axis2.jaxws.misc;

import junit.framework.TestCase;

import org.apache.axis2.jaxws.message.XMLFault;
import org.apache.axis2.jaxws.message.XMLFaultCode;
import org.apache.axis2.jaxws.message.XMLFaultReason;
import org.apache.axis2.jaxws.message.util.XMLFaultUtils;
import org.apache.axis2.jaxws.utility.JavaUtils;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;

/**
 * Tests XMLFault logic
 */
public class XMLFaultTest extends TestCase {

    private static final QName CUSTOM = new QName("http://mySample", "CustomCode", "pre");
    
    /**
     * Test Custom FaultQName for SOAP 1.1
     * @throws Exception
     */
    public void testCustomFault11() throws Exception {
        MessageFactory mf = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        SOAPMessage sm = mf.createMessage();
        SOAPBody body = sm.getSOAPBody();
        SOAPFault fault = body.addFault(CUSTOM, "Custom Fault");
        
        XMLFault xmlFault = XMLFaultUtils.createXMLFault(fault);
        
        assertTrue(xmlFault != null);
        
        XMLFaultReason reason = xmlFault.getReason();
        assertTrue(reason != null);
        assertTrue(reason.getText().equals("Custom Fault"));
        
        XMLFaultCode code = xmlFault.getCode();
        assertTrue(code != null);
        
        QName codeQName = code.toQName(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE);
        assertTrue("Expected QName = " + CUSTOM + " but received = " + codeQName, codeQName.equals(CUSTOM));
                        
    }
    
    /**
     * Test Custom FaultQName for SOAP 1.2
     * @throws Exception
     */
    public void testCustomFault12() throws Exception {
        MessageFactory mf = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage sm = mf.createMessage();
        SOAPBody body = sm.getSOAPBody();
        
        try {
            SOAPFault fault = body.addFault(CUSTOM, "Custom Fault");
            fail("Expected Failure, custom fault codes are not supported with SOAP 1.2");
        } catch (SOAPException e) {
            // Expected...
        } catch (Throwable t) {
            fail("Expected different failure, received: " + t);
        }

    }
    
    /**
     * Tests that Role and Node
     * are set properly on SOAP 1.2 Fault.
     * @throws Exception
     */
    public void testCustomRoleNodeFault12() throws Exception {
        MessageFactory mf = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage sm = mf.createMessage();
        SOAPBody body = sm.getSOAPBody();
        
        SOAPFault fault = body.addFault();
        fault.setFaultRole("TestRole");
        fault.setFaultNode("http://XMLFaultTest/testCustomRoleNodeFault/");
        
        XMLFault xmlFault = XMLFaultUtils.createXMLFault(fault);
        
        SOAPFault retFault = XMLFaultUtils.createSAAJFault(xmlFault, body);
        
        assertTrue(retFault != null);
        
        String role = retFault.getFaultRole();
        assertTrue(role != null);
        assertTrue(role.equals("TestRole"));
        
        // Actor and role should be the same
        String actor = retFault.getFaultActor();
        assertTrue(actor != null);
        assertTrue(actor.equals("TestRole"));

        String node = retFault.getFaultNode();
        assertTrue(node != null);
        assertTrue(node.equals("http://XMLFaultTest/testCustomRoleNodeFault/"));
    }
}
