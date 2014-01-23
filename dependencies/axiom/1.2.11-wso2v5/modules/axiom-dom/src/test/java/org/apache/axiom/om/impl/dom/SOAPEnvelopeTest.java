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

package org.apache.axiom.om.impl.dom;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPProcessingException;
import org.apache.axiom.soap.impl.dom.soap11.SOAP11Factory;
import org.apache.axiom.soap.impl.dom.soap12.SOAP12Factory;

public class SOAPEnvelopeTest extends TestCase {
        
    public void testAppendSOAP11() throws Exception {
        SOAP11Factory factory;
        SOAPEnvelope env;
        
        // SOAP 1.1 allows for arbitrary elements after SOAPBody element
        
        // these addChild() should fail since appending before SOAPBody
        // but they do not at this point (need a better check).
        factory = new SOAP11Factory();
        env = factory.createSOAPEnvelope();
        checkAddChild(env, false);
        
        factory = new SOAP11Factory();
        env = factory.createSOAPEnvelope();
        factory.createSOAPHeader(env);
        checkAddChild(env, false);
        
        // these addChild() should work since appending after SOAPBody   
        factory = new SOAP11Factory();
        env = factory.createSOAPEnvelope();
        factory.createSOAPBody(env);  
        checkAddChild(env, false);
        
        factory = new SOAP11Factory();
        env = factory.createSOAPEnvelope();
        factory.createSOAPHeader(env);
        factory.createSOAPBody(env);        
        checkAddChild(env, false);
    }
    
    public void testAppendSOAP12() throws Exception {
        SOAP12Factory factory;
        SOAPEnvelope env;
        
        // SOAP 1.2 only allows SOAPHeader and SOAPBody elements
        
        // All these addChild() should fail
        factory = new SOAP12Factory();  
        env = factory.createSOAPEnvelope();
        checkAddChild(env, true);
        
        factory = new SOAP12Factory();  
        env = factory.createSOAPEnvelope();
        factory.createSOAPHeader(env);
        checkAddChild(env, true);
        
        factory = new SOAP12Factory();  
        env = factory.createSOAPEnvelope();
        factory.createSOAPBody(env);        
        checkAddChild(env, true);
        
        factory = new SOAP12Factory();  
        env = factory.createSOAPEnvelope();
        factory.createSOAPHeader(env);
        factory.createSOAPBody(env); 
        checkAddChild(env, true);        
    }

    private void checkAddChild(SOAPEnvelope env, boolean fail) {
        OMElement elem = env.getOMFactory().createOMElement(new QName("foo"));
        if (fail) {
            try {
                env.addChild(elem);
                fail("did not throw exception");
            } catch (SOAPProcessingException e) {
                // expected
            }
        } else {
            env.addChild(elem);
        }
    }
}
