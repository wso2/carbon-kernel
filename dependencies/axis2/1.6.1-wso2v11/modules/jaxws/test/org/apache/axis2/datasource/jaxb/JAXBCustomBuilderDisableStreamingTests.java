/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.datasource.jaxb;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.jaxws.Constants;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;

/**
 * Test that the JAXB Payload streaming can be disabled. 
 */
public class JAXBCustomBuilderDisableStreamingTests extends TestCase {
    
    public void testDisableJAXBPayloadStreaming() {
        
        JAXBDSContext jaxbDSC = new JAXBDSContext(null, null);
        JAXBCustomBuilder jaxbCB = new JAXBCustomBuilder(jaxbDSC);
        MessageContext msgCtx = new MessageContext();
        // Disable the JAXB Payload streaming
        msgCtx.setProperty(Constants.JAXWS_ENABLE_JAXB_PAYLOAD_STREAMING, new Boolean(false));
        jaxbDSC.setMessageContext(msgCtx);
        try {
            assertNull(jaxbCB.create("ns", "lp", null, new MockXMLStreamReader(), null));
        } catch (Exception e) {
            // Since we didn't set up the JAXBDSContext fully, if the disabling of it didn't
            // work, then we'll get some sort of exception.
            fail("JAXB Payload streaming was not disabled");
        }
    }
    
    public void testDisableJAXBPayloadStreamingWithHighFidelity() {
        
        JAXBDSContext jaxbDSC = new JAXBDSContext(null, null);
        JAXBCustomBuilder jaxbCB = new JAXBCustomBuilder(jaxbDSC);
        MessageContext msgCtx = new MessageContext();
        // Disable the JAXB Payload streaming
        msgCtx.setProperty(Constants.JAXWS_PAYLOAD_HIGH_FIDELITY, new Boolean(true));
        jaxbDSC.setMessageContext(msgCtx);
        try {
            assertNull(jaxbCB.create("ns", "lp", null, new MockXMLStreamReader(), null));
        } catch (Exception e) {
            // Since we didn't set up the JAXBDSContext fully, if the disabling of it didn't
            // work, then we'll get some sort of exception.
            fail("JAXB Payload streaming was not disabled");
        }
    }
    
    public void testDisableJAXBPayloadStreamingWithHighFidelityParameter() throws Exception {
        
        JAXBDSContext jaxbDSC = new JAXBDSContext(null, null);
        JAXBCustomBuilder jaxbCB = new JAXBCustomBuilder(jaxbDSC);
        MessageContext msgCtx = new MessageContext();
        AxisService service = new AxisService();
        msgCtx.setAxisService(service);
        service.addParameter(Constants.JAXWS_PAYLOAD_HIGH_FIDELITY, "true");
        
        jaxbDSC.setMessageContext(msgCtx);
        try {
            assertNull(jaxbCB.create("ns", "lp", null, new MockXMLStreamReader(), null));
        } catch (Exception e) {
            // Since we didn't set up the JAXBDSContext fully, if the disabling of it didn't
            // work, then we'll get some sort of exception.
            fail("JAXB Payload streaming was not disabled");
        }
    }
    
    public void testDefaultJAXBPayloadStreaming() {
        
        JAXBDSContext jaxbDSC = new JAXBDSContext(null, null);
        JAXBCustomBuilder jaxbCB = new JAXBCustomBuilder(jaxbDSC);
        MessageContext msgCtx = new MessageContext();
        // Do NOT Disable the JAXB Payload streaming; the default should be ON
        // msgCtx.setProperty(Constants.JAXWS_ENABLE_JAXB_PAYLOAD_STREAMING, new Boolean(false));
        jaxbDSC.setMessageContext(msgCtx);
        try {
            jaxbCB.create("ns", "lp", null, new MockXMLStreamReader(), null);
            fail("JAXB Payload streaming default was not enabled");
        } catch (Exception e) {
            // Expected code path
            // Since we didn't set up the JAXBDSContext fully, if the disabling of it didn't
            // work, then we'll get some sort of exception.
        }
    }

    
    class MockXMLStreamReader implements javax.xml.stream.XMLStreamReader {

        public void close() throws XMLStreamException {
        }

        public int getAttributeCount() {
            return 0;
        }

        public String getAttributeLocalName(int i) {
            return null;
        }

        public QName getAttributeName(int i) {
            return null;
        }

        public String getAttributeNamespace(int i) {
            return null;
        }

        public String getAttributePrefix(int i) {
            return null;
        }

        public String getAttributeType(int i) {
            return null;
        }

        public String getAttributeValue(int i) {
            return null;
        }

        public String getAttributeValue(String s, String s1) {
            return null;
        }

        public String getCharacterEncodingScheme() {
            return null;
        }

        public String getElementText() throws XMLStreamException {
            return null;
        }

        public String getEncoding() {
            return null;
        }

        public int getEventType() {
            return 0;
        }

        public String getLocalName() {
            return null;
        }

        public Location getLocation() {
            return null;
        }

        public QName getName() {
            return null;
        }

        public NamespaceContext getNamespaceContext() {
            return null;
        }

        public int getNamespaceCount() {
            return 0;
        }

        public String getNamespacePrefix(int i) {
            return null;
        }

        public String getNamespaceURI() {
            return null;
        }

        public String getNamespaceURI(int i) {
            return null;
        }

        public String getNamespaceURI(String s) {
            return null;
        }

        public String getPIData() {
            return null;
        }

        public String getPITarget() {
            return null;
        }

        public String getPrefix() {
            return null;
        }

        public Object getProperty(String s) throws IllegalArgumentException {
            return null;
        }

        public String getText() {
            return null;
        }

        public char[] getTextCharacters() {
            return null;
        }

        public int getTextCharacters(int i, char[] ac, int j, int k) throws XMLStreamException {
            return 0;
        }

        public int getTextLength() {
            return 0;
        }

        public int getTextStart() {
            return 0;
        }

        public String getVersion() {
            return null;
        }

        public boolean hasName() {
            return false;
        }

        public boolean hasNext() throws XMLStreamException {
            return false;
        }

        public boolean hasText() {
            return false;
        }

        public boolean isAttributeSpecified(int i) {
            return false;
        }

        public boolean isCharacters() {
            return false;
        }

        public boolean isEndElement() {
            return false;
        }

        public boolean isStandalone() {
            return false;
        }

        public boolean isStartElement() {
            return false;
        }

        public boolean isWhiteSpace() {
            return false;
        }

        public int next() throws XMLStreamException {
            return 0;
        }

        public int nextTag() throws XMLStreamException {
            return 0;
        }

        public void require(int i, String s, String s1) throws XMLStreamException {
            
        }

        public boolean standaloneSet() {
            return false;
        }
        
    }
}
