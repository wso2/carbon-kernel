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

package org.apache.axis2.scripting;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;

public class TestUtils {

    public static OMElement createOMElement(String xml) {
        try {

            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(xml));
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            return builder.getDocumentElement();

        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    public static MessageContext createMockMessageContext(String payload) throws AxisFault {
        MessageContext inMC = new MessageContext();
        SOAPEnvelope envelope = OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope();
        OMDocument omDoc = OMAbstractFactory.getSOAP11Factory().createOMDocument();
        omDoc.addChild(envelope);
        envelope.getBody().addChild(TestUtils.createOMElement(payload));
        inMC.setEnvelope(envelope);
        AxisConfiguration axisConfig = new AxisConfiguration();
        AxisService as = new AxisService();
        as.setName("ScriptService");
        AxisServiceGroup asg = new AxisServiceGroup(axisConfig);
        asg.addService(as);
        as.addParameter(new Parameter("script.js",
                                      "function invoke(inMC, outMC) { outMC.setPayloadXML(" +
                                              payload + ")}"));
        ConfigurationContext cfgCtx = new ConfigurationContext(axisConfig);
        ServiceGroupContext sgc = cfgCtx.createServiceGroupContext(asg);
        inMC.setAxisService(as);
        inMC.setServiceContext(sgc.getServiceContext(as));
        return inMC;
    }
}
