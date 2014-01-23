/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.axis2.transport;

import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.transport.UtilsTransportServer;

public abstract class AbstractTransportTest extends TestCase {

    private UtilsTransportServer server;

    protected void setUp() throws Exception {
        // Temporarily change jmx.agent.name system property to avoid collisions
        // between MBeans registered by the server context and those registered
        // by the client context
        String agentName = System.getProperty("jmx.agent.name", "org.apache.synapse");
        System.setProperty("jmx.agent.name", agentName + "-server");
        server = createServer();
        server.start();
        System.setProperty("jmx.agent.name", agentName);
    }

    protected void tearDown() throws Exception {
        server.stop();
        server = null;
    }
    
    protected abstract UtilsTransportServer createServer() throws Exception;

    /**
     * Create the payload for an echoOMElement request
     * @return
     */
    protected OMElement createPayload(String textValue) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://localhost/axis2/services/EchoXMLService", "my");
        OMElement method = fac.createOMElement("echoOMElement", omNs);
        OMElement value = fac.createOMElement("myValue", omNs);
        value.addChild(fac.createOMText(value, textValue));
        method.addChild(value);
        return method;
    }
    
    protected OMElement createPayload() {
        return createPayload("omTextValue");
    }

    protected void assertEchoResponse(String textValue, OMElement element) {
        assertEquals("echoOMElementResponse", element.getLocalName());
        assertEquals("http://localhost/axis2/services/EchoXMLService",
                     element.getNamespace().getNamespaceURI());
        OMElement valueElement = element.getFirstElement();
        assertEquals("myValue", valueElement.getLocalName());
        assertEquals("http://localhost/axis2/services/EchoXMLService",
                     valueElement.getNamespace().getNamespaceURI());
        assertEquals(textValue, valueElement.getText());
    }
    
    protected void assertEchoResponse(OMElement element) {
        assertEchoResponse("omTextValue", element);
    }
    
    protected void assertSOAPEchoResponse(String textValue, XMLStreamReader reader) {
        SOAPEnvelope env = new StAXSOAPModelBuilder(reader).getSOAPEnvelope();
        assertEchoResponse(textValue, env.getBody().getFirstElement());
    }
    
    protected void assertSOAPEchoResponse(XMLStreamReader reader) {
        assertSOAPEchoResponse("omTextValue", reader);
    }
    
    /**
     * Get the default axis2 configuration context for a client
     * @return
     * @throws Exception
     */
    protected ConfigurationContext getClientCfgCtx() throws Exception {
        AxisConfiguration axisCfg = new AxisConfiguration();
        ConfigurationContext cfgCtx = new ConfigurationContext(axisCfg);
        return cfgCtx;
    }

}
