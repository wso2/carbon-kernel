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
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.InOnlyAxisOperation;
import org.apache.axis2.description.AxisEndpoint;
import org.apache.axis2.engine.AxisConfiguration;

import javax.xml.namespace.QName;

public class RequestURIBasedOperationDispatcherTest extends TestCase {

    public void testFindService() throws AxisFault {
        MessageContext messageContext;

        //Global services
        AxisService as1 = new AxisService("Service1");
        as1.addEndpoint("Service1Endpoint", new AxisEndpoint());

        //Hierarchical Services
        AxisService as2 = new AxisService("foo/bar/Version");
        AxisService as3 = new AxisService("foo/bar/1.0.1/Echo");
        as2.addEndpoint("VersionEndpoint", new AxisEndpoint());
        as3.addEndpoint("EchoEndpoint", new AxisEndpoint());


        AxisOperation operation1 = new InOnlyAxisOperation(new QName("operation1"));
        AxisOperation operation2 = new InOnlyAxisOperation(new QName("operation2"));
        as1.addOperation(operation1);
        as1.addOperation(operation2);

        AxisOperation operation3 = new InOnlyAxisOperation(new QName("getVersion"));
        AxisOperation operation4 = new InOnlyAxisOperation(new QName("echo"));
        as2.addOperation(operation3);
        as3.addOperation(operation4);

        ConfigurationContext cc = ConfigurationContextFactory.createEmptyConfigurationContext();
        AxisConfiguration ac = cc.getAxisConfiguration();
        ac.addService(as1);
        ac.addService(as2);
        ac.addService(as3);

        RequestURIBasedOperationDispatcher ruisd = new RequestURIBasedOperationDispatcher();

        /**
         * Tests for global services
         */
        messageContext = cc.createMessageContext();
        messageContext.setTo(new EndpointReference("http://127.0.0.1:8080" +
                "/axis2/services/Service1/operation2"));
        messageContext.setAxisService(as1);
        ruisd.invoke(messageContext);
        assertEquals(operation2, messageContext.getAxisOperation());

        messageContext = cc.createMessageContext();
        messageContext.setTo(new EndpointReference("http://127.0.0.1:8080" +
                "/axis2/services/Service1.Service1Endpoint/operation2"));
        messageContext.setAxisService(as1);
        ruisd.invoke(messageContext);
        assertEquals(operation2, messageContext.getAxisOperation());

        /**
         * Tests for hierarchical services
         */
        messageContext = cc.createMessageContext();
        messageContext.setTo(new EndpointReference("http://127.0.0.1:8080" +
                "/axis2/services/foo/bar/Version/getVersion"));
        messageContext.setAxisService(as2);
        ruisd.invoke(messageContext);
        assertEquals(operation3, messageContext.getAxisOperation());

        messageContext = cc.createMessageContext();
        messageContext.setTo(new EndpointReference("http://127.0.0.1:8080" +
                "/axis2/services/foo/bar/Version.VersionEndpoint/getVersion"));
        messageContext.setAxisService(as2);
        ruisd.invoke(messageContext);
        assertEquals(operation3, messageContext.getAxisOperation());


        messageContext = cc.createMessageContext();
        messageContext.setTo(new EndpointReference("http://127.0.0.1:8080" +
                "/axis2/services/foo/bar/1.0.1/Echo/echo"));
        messageContext.setAxisService(as3);
        ruisd.invoke(messageContext);
        assertEquals(operation4, messageContext.getAxisOperation());

        messageContext = cc.createMessageContext();
        messageContext.setTo(new EndpointReference("http://127.0.0.1:8080" +
                "/axis2/services/foo/bar/1.0.1/Echo.EchoEndpoint/echo"));
        messageContext.setAxisService(as3);
        ruisd.invoke(messageContext);
        assertEquals(operation4, messageContext.getAxisOperation());
    }

}
