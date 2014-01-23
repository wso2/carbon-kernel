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

package org.apache.axis2.jaxws.dispatch.server;

import java.util.Iterator;

import javax.xml.ws.BindingType;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.soap.SOAPBinding;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.impl.llom.soap12.SOAP12Factory;

/**
 * A Provider&lt;OMElement&gt; implementation used to test sending and 
 * receiving SOAP 1.2 messages.
 */
@WebServiceProvider(
        serviceName="OMElementProviderService", 
        wsdlLocation="META-INF/OMElementProviderService.wsdl", 
        targetNamespace="http://org/apache/axis2/jaxws/test/OMELEMENT")
@BindingType(SOAPBinding.SOAP12HTTP_BINDING)
@ServiceMode(value=Service.Mode.MESSAGE)
public class OMElementProvider implements Provider<OMElement> {

    public OMElement invoke(OMElement obj) {
        // since this is test code, let's check the inbound obj:
        SOAPEnvelope inboundEnv = (SOAPEnvelope)obj;
        SOAPBody inboundBody = inboundEnv.getBody();
        Iterator it = inboundBody.getChildren();
        OMElement el3 = null;
        for (;it.hasNext();) {
            OMElement el2 = (OMElement)it.next();
            Iterator it2 = el2.getChildElements();
            for (;it2.hasNext();) {
                el3 = (OMElement)it2.next();
                assert(el3.getText().equals("SAMPLE REQUEST MESSAGE"));
            }
        }
        assert(el3 != null);

        OMElement payload = createPayload();
        
        SOAPFactory factory = new SOAP12Factory();
        SOAPEnvelope env = factory.createSOAPEnvelope();
        SOAPBody body = factory.createSOAPBody(env);
        
        body.addChild(payload);
        
        return env;
    }
    
    private OMElement createPayload() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://org/apache/axis2/jaxws/test/SOAPENVELOPE", "test");
        
        OMElement response = fac.createOMElement("echoOMElement", omNs);
        
        OMElement output = fac.createOMElement("output", omNs);
        response.addChild(output);
        
        OMElement data = fac.createOMElement("data", omNs);
        output.addChild(data);
        
        OMText binaryData = fac.createOMText("SAMPLE RESPONSE MESSAGE");
        data.addChild(binaryData);
        
        return response;
    }

}
