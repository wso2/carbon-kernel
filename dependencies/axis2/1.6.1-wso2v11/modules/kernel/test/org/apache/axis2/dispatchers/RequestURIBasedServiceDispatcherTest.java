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

package org.apache.axis2.dispatchers;

import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisEndpoint;
import org.apache.axis2.engine.AxisConfiguration;

public class RequestURIBasedServiceDispatcherTest extends TestCase {

    public void testFindService() throws AxisFault {
        MessageContext messageContext;

        //Global services
        AxisService as1 = new AxisService("Service1");
        AxisService as2 = new AxisService("Service2");
        as1.addEndpoint("Service1Endpoint", new AxisEndpoint());

        //Hierarchical Services
        AxisService as3 = new AxisService("foo/bar/Version");
        AxisService as4 = new AxisService("foo/bar/1.0.1/Echo");
        as3.addEndpoint("VersionEndpoint", new AxisEndpoint());
        as4.addEndpoint("EchoEndpoint", new AxisEndpoint());

        ConfigurationContext cc = ConfigurationContextFactory.createEmptyConfigurationContext();
        AxisConfiguration ac = cc.getAxisConfiguration();
        ac.addService(as1);
        ac.addService(as2);
        ac.addService(as3);
        ac.addService(as4);

        RequestURIBasedServiceDispatcher ruisd = new RequestURIBasedServiceDispatcher();

        /**
         * Tests for global services
         */
        messageContext = cc.createMessageContext();
        messageContext.setTo(new EndpointReference("http://127.0.0.1:8080" +
                "/axis2/services/Service2"));
        ruisd.invoke(messageContext);
        assertEquals(as2, messageContext.getAxisService());

        //service/operation scenario
        messageContext = cc.createMessageContext();
        messageContext.setTo(new EndpointReference("http://127.0.0.1:8080" +
                "/axis2/services/Service2/getName"));
        ruisd.invoke(messageContext);
        assertEquals(as2, messageContext.getAxisService());

        //REST scenario
        messageContext = cc.createMessageContext();
        messageContext.setTo(new EndpointReference("http://127.0.0.1:8080" +
                "/axis2/services/Service2/student/name/peter/age/25"));
        ruisd.invoke(messageContext);
        assertEquals(as2, messageContext.getAxisService());

        //service.endpoint scenario
        messageContext = cc.createMessageContext();
        messageContext.setTo(new EndpointReference("http://127.0.0.1:8080" +
                "/axis2/services/Service1.Service1Endpoint"));
        ruisd.invoke(messageContext);
        assertEquals(as1, messageContext.getAxisService());

        //service.endpoint/operation scenario
        messageContext = cc.createMessageContext();
        messageContext.setTo(new EndpointReference("http://127.0.0.1:8080" +
                "/axis2/services/Service1.Service1Endpoint/getName"));
        ruisd.invoke(messageContext);
        assertEquals(as1, messageContext.getAxisService());

        /**
         * Tests for hierarchical services
         */
        messageContext = cc.createMessageContext();
        messageContext.setTo(new EndpointReference("http://127.0.0.1:8080" +
                "/axis2/services/foo/bar/Version"));
        ruisd.invoke(messageContext);
        assertEquals(as3, messageContext.getAxisService());

        messageContext = cc.createMessageContext();
        messageContext.setTo(new EndpointReference("http://127.0.0.1:8080" +
                "/axis2/services/foo/bar/Version/getVersion"));
        ruisd.invoke(messageContext);
        assertEquals(as3, messageContext.getAxisService());

        messageContext = cc.createMessageContext();
        messageContext.setTo(new EndpointReference("http://127.0.0.1:8080" +
                "/axis2/services/foo/bar/Version/student/name/peter/age/25"));
        ruisd.invoke(messageContext);
        assertEquals(as3, messageContext.getAxisService());
        
        messageContext = cc.createMessageContext();
        messageContext.setTo(new EndpointReference("http://127.0.0.1:8080" +
                "/axis2/services/foo/bar/Version.VersionEndpoint"));
        ruisd.invoke(messageContext);
        assertEquals(as3, messageContext.getAxisService());

        messageContext = cc.createMessageContext();
        messageContext.setTo(new EndpointReference("http://127.0.0.1:8080" +
                "/axis2/services/foo/bar/Version.VersionEndpoint/getVersion"));
        ruisd.invoke(messageContext);
        assertEquals(as3, messageContext.getAxisService());

        /**
         * Tests for hierarchical services with '.' in the service name
         */
        messageContext = cc.createMessageContext();
        messageContext.setTo(new EndpointReference("http://127.0.0.1:8080" +
                "/axis2/services/foo/bar/1.0.1/Echo"));
        ruisd.invoke(messageContext);
        assertEquals(as4, messageContext.getAxisService());

        messageContext = cc.createMessageContext();
        messageContext.setTo(new EndpointReference("http://127.0.0.1:8080" +
                "/axis2/services/foo/bar/1.0.1/Echo/echo"));
        ruisd.invoke(messageContext);
        assertEquals(as4, messageContext.getAxisService());

        messageContext = cc.createMessageContext();
        messageContext.setTo(new EndpointReference("http://127.0.0.1:8080" +
                "/axis2/services/foo/bar/1.0.1/Echo/student/name/peter/age/25"));
        ruisd.invoke(messageContext);
        assertEquals(as4, messageContext.getAxisService());

        messageContext = cc.createMessageContext();
        messageContext.setTo(new EndpointReference("http://127.0.0.1:8080" +
                "/axis2/services/foo/bar/1.0.1/Echo.EchoEndpoint"));
        ruisd.invoke(messageContext);
        assertEquals(as4, messageContext.getAxisService());

        messageContext = cc.createMessageContext();
        messageContext.setTo(new EndpointReference("http://127.0.0.1:8080" +
                "/axis2/services/foo/bar/1.0.1/Echo.EchoEndpoint/echo"));
        ruisd.invoke(messageContext);
        assertEquals(as4, messageContext.getAxisService());
    }

}
