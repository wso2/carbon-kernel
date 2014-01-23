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

package org.apache.axis2.jaxws.client.proxy;

import org.apache.axis2.jaxws.client.InterceptableClientTestCase;
import org.apache.axis2.jaxws.client.TestClientInvocationController;
import org.apache.axis2.jaxws.core.InvocationContext;
import org.apache.axis2.jaxws.core.MessageContext;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.soap.MTOMFeature;

/**
 * This suite of tests is for the MTOMFeature configuration that can
 * be used on Proxy clients.
 */
public class ProxyMTOMFeatureTest extends InterceptableClientTestCase {

    /*
     * Make sure MTOM is not enabled by default.
     */
    public void testNoMTOMFeature() {
        Service svc = Service.create(new QName("http://test", "ProxyMTOMService"));
        ProxyMTOMService proxy = svc.getPort(ProxyMTOMService.class);
        assertTrue("Proxy instance was null", proxy != null);
        
        proxy.sendAttachment("12345");
        
        TestClientInvocationController testController = getInvocationController();
        InvocationContext ic = testController.getInvocationContext();
        MessageContext request = ic.getRequestMessageContext();
        
        assertTrue("Request should not be null.", request != null);
        assertTrue("MTOM should not be abled on the Message by default.", !request.getMessage().isMTOMEnabled());
    }
    
    /*
     * Test the default configuration of the MTOMFeature.
     */
    public void testDefaultMTOMFeature() {
        // Use the default feature config
        MTOMFeature feature = new MTOMFeature();
        
        Service svc = Service.create(new QName("http://test", "ProxyMTOMService"));
        ProxyMTOMService proxy = svc.getPort(ProxyMTOMService.class, feature);
        assertTrue("Proxy instance was null", proxy != null);
        
        proxy.sendAttachment("12345");
        
        TestClientInvocationController testController = getInvocationController();
        InvocationContext ic = testController.getInvocationContext();
        MessageContext request = ic.getRequestMessageContext();
        
        assertTrue("Request should not be null.", request != null);
        assertTrue("MTOM should be abled on the Message by default.", request.getMessage().isMTOMEnabled());
    }
    
    /*
     * Test disabling the MTOM feature.
     */
    public void testDisabledMTOMFeature() {
        // Use the default feature config
        MTOMFeature feature = new MTOMFeature(false);
        
        Service svc = Service.create(new QName("http://test", "ProxyMTOMService"));
        ProxyMTOMService proxy = svc.getPort(ProxyMTOMService.class, feature);
        assertTrue("Proxy instance was null", proxy != null);
        
        proxy.sendAttachment("12345");
        
        TestClientInvocationController testController = getInvocationController();
        InvocationContext ic = testController.getInvocationContext();
        MessageContext request = ic.getRequestMessageContext();
        
        assertTrue("Request should not be null.", request != null);
        assertTrue("MTOM should NOT be abled on the Message by default.", !request.getMessage().isMTOMEnabled());
    }
    
    /*
     * Test the configuration of the threshold for MTOM when no attachment is 
     * specified.  In this case, although enabled per the MTOMFeature, MTOM should
     * not be enabled on the Message, since the attachment size is too small.
     */
    public void testMTOMFeatureThreshold() {
        // Set a threshold that we will not meet.
        int threshold = 20000;
        MTOMFeature feature = new MTOMFeature(threshold);
        
        Service svc = Service.create(new QName("http://test", "ProxyMTOMService"));
        ProxyMTOMService proxy = svc.getPort(ProxyMTOMService.class, feature);
        assertTrue("Proxy instance was null", proxy != null);
        
        proxy.sendAttachment("12345");
        
        TestClientInvocationController testController = getInvocationController();
        InvocationContext ic = testController.getInvocationContext();
        MessageContext request = ic.getRequestMessageContext();
        
        assertTrue("Request should not be null.", request != null);
        // A Threshold indicates that MTOM should be enabled.
        // The decision about whether the attachment is inlined is made on a per attachment
        // basis in Axiom...and cannot be tested in unit test that does not send the message
        // to the server.
        assertTrue("MTOM should be enabled.", request.getMessage().isMTOMEnabled());
    }
    
    @WebService()
    public interface ProxyMTOMService {
    
        public String sendAttachment(String id);
        
    }
}
