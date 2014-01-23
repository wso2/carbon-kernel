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

package org.apache.axis2.jaxws.type_substitution.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.jaxws.framework.AbstractTestCase;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import java.io.ByteArrayInputStream;
import java.util.Iterator;

public class TypeSubstitutionTests extends AbstractTestCase {

    private String NS = "http://apple.org";
    private QName XSI_TYPE = new QName("http://www.w3.org/2001/XMLSchema-instance", "type");

    private String endpointUrl = "http://localhost:6060/axis2/services/AppleFinderService.AppleFinderPort";
    private QName serviceName = new QName(NS, "AppleFinderService");
    private QName portName = new QName(NS, "AppleFinderPort");
    
    private String reqMsgStart = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body>";
    
    private String reqMsgEnd = "</soap:Body></soap:Envelope>";
   
    private String GET_APPLES = "<ns2:getApples xmlns:ns2=\"" + NS + "\"/>";
                
    public static Test suite() {
        return getTestSetup(new TestSuite(TypeSubstitutionTests.class));
    }
   
    public void testTypeSubstitution() throws Exception {
        Dispatch<SOAPMessage> dispatch = createDispatch();
             
        String msg = reqMsgStart + GET_APPLES + reqMsgEnd;
        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage request = factory.createMessage(null, 
                                                    new ByteArrayInputStream(msg.getBytes()));
            
        SOAPMessage response = dispatch.invoke(request);

        SOAPBody body = response.getSOAPBody();

        TestLogger.logger.debug(">> Response [" + body + "]");

        QName expectedXsiType1 = new QName(NS, "fuji");
        QName expectedXsiType2 = new QName(NS, "freyburg");

        Iterator iter;
        SOAPElement element;
        QName xsiType;

        iter = body.getChildElements(new QName(NS, "getApplesResponse"));
        assertTrue(iter.hasNext());
        
        element = (SOAPElement)iter.next();

        iter = element.getChildElements(new QName("return"));

        // check value1
        assertTrue(iter.hasNext());
        element = (SOAPElement)iter.next();
        xsiType = getXsiTypeAttribute(element);
        assertEquals("xsi:type 1", expectedXsiType1, xsiType);
        
        // check value2
        assertTrue(iter.hasNext());
        element = (SOAPElement)iter.next();
        xsiType = getXsiTypeAttribute(element);
        assertEquals("xsi:type 2", expectedXsiType2, xsiType);
    }
    
    private QName getXsiTypeAttribute(SOAPElement element) throws Exception {
        String value = element.getAttributeValue(XSI_TYPE);
        QName xsiType = null;
        if (value != null) {
            int pos = value.indexOf(":");
            if (pos != -1) {
                String prefix = value.substring(0, pos);
                String localName = value.substring(pos+1);
                String namespace = element.getNamespaceURI(prefix);
                xsiType = new QName(namespace, localName, prefix);
            } else {
                xsiType = new QName(value);
            }
        }
        return xsiType;
    }

    private Dispatch<SOAPMessage> createDispatch() throws Exception {
        Service svc = Service.create(serviceName);
        svc.addPort(portName, null, endpointUrl);
        Dispatch<SOAPMessage> dispatch = 
            svc.createDispatch(portName, SOAPMessage.class, Service.Mode.MESSAGE);
        return dispatch;
    }

}
