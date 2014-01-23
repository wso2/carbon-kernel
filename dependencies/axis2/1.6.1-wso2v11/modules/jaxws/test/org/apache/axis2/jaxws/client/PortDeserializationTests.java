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
package org.apache.axis2.jaxws.client;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.WSDL11ToAllAxisServicesBuilder;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.spi.ClientMetadataTest;

import javax.xml.namespace.QName;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.TestCase;

/**
 * Test the serialization of a MessageContext and subsequent deserialization to ensure the
 * JAXWS and Axis2 objects for the relevant ports are hooked back up correctly.  In particluar,
 * make sure the AxisServices can be hooked back up with the MEssageContexts correctly.
 * 
 * This is done as a JAX-WS test rather than an Axis2 test to verify the behavior of ports 
 * added by JAX-WS, particluarly dynamic ports.
 */
public class PortDeserializationTests extends TestCase {
    static final String namespaceURI = "http://client.jaxws.axis2.apache.org";
    static final String svcLocalPart = "svcLocalPart";
    
    static final String dynamicPort = "dynamicPort";
    static final String bindingID = null;
    static final String epr = "http://dummy/endpoint/address";

    /**
     * Validate that a message context can be serialized and deserialized with the Axis2 
     * and JAX-WS description objects not being recreated.  This is similar to a message
     * being serialized and deserialized without the server being stopped.
     */
    public void testDynamicPortSingleServiceNoRecreate() {
        try {
            ClientMetadataTest.installCachingFactory();
            QName svc1QN = new QName(namespaceURI + "?1", svcLocalPart);

            Service svc1 = Service.create(svc1QN);
            QName portQN = new QName(namespaceURI, dynamicPort + "_1");
            svc1.addPort(portQN, bindingID, epr);
            Dispatch<String> port1 = svc1.createDispatch(portQN, String.class, Service.Mode.PAYLOAD);
            
            // We need to get the AxisService so we can set in on a MessageContext and test 
            // serialization / deserialization.  We do this using NON-Public, INTERNAL SPIs
            // since we need to get at the internals of the engine
            org.apache.axis2.jaxws.spi.BindingProvider bindingProvider = 
                (org.apache.axis2.jaxws.spi.BindingProvider) port1;
            EndpointDescription endpointDesc = bindingProvider.getEndpointDescription();
            AxisService axisService = endpointDesc.getAxisService();
            assertNotNull(axisService);
            assertEquals(svc1QN.getLocalPart(), axisService.getName());

            // Now that the AxisService is setup, create an Axis2 message context, set the
            // AxisService on it.  Serialize it out then read it back in.
            MessageContext msgCtx = new MessageContext();
            msgCtx.setAxisService(axisService);
            msgCtx.setAxisServiceGroup(axisService.getAxisServiceGroup());
            ByteArrayOutputStream baos = serializeMessageContext(msgCtx);
            
            // Read in the message context and activate it, which is required by message
            // context deserialization to connect the message context to existing runtime 
            // objects such as AxisService
            MessageContext mcRead = deserializeMessageContext(baos);
            ConfigurationContext configContext = endpointDesc.getServiceDescription().getAxisConfigContext();
            assertNotNull(configContext);
            mcRead.activate(configContext);
            
            AxisService asRead = mcRead.getAxisService();
            assertNotNull(asRead);
            assertEquals(axisService.getName(), asRead.getName());
            assertSame(axisService, asRead);
            AxisServiceGroup agRead = mcRead.getAxisServiceGroup();
            assertNotNull(agRead);
            
            // This keeps the port from being GC'd and causing the AxisService to be freed
            // before the test method completes.
            assertNotNull(port1);

        } catch (Exception t) {
            t.printStackTrace();
            fail("Caught throwable " + t);
        } finally {
            ClientMetadataTest.restoreOriginalFactory();
        }
    }
    
    /**
     * Validate that a message context can be serialized and deserialized with the Axis2 
     * and JAX-WS description objects not being recreated.  This is similar to a message
     * being serialized and deserialized without the server being stopped.
     * 
     * This test uses two services with different namespaces, each with a dynamic port of the
     * same name.
     */
    public void testDynamicPortMultipleServiceNoRecreate() {
        try {
            ClientMetadataTest.installCachingFactory();
            QName svc1QN = new QName(namespaceURI + "?1", svcLocalPart);
            QName svc2QN = new QName(namespaceURI + "?2", svcLocalPart);

            Service svc1 = Service.create(svc1QN);
            Service svc2 = Service.create(svc2QN);

            // Create the same port under the two different services.  Each port gets a unique
            // EPR so it will cause a new port to be created (rather than shared) which in 
            // turn causes a new AxisService to be crated.
            QName portQN = new QName(namespaceURI, dynamicPort + "_1");
            svc1.addPort(portQN, bindingID, epr + "1");
            Dispatch<String> port1 = svc1.createDispatch(portQN, String.class, Service.Mode.PAYLOAD);
            svc2.addPort(portQN, bindingID, epr + "2");
            Dispatch<String> port2 = svc2.createDispatch(portQN, String.class, Service.Mode.PAYLOAD);
            
            // We need to get the AxisService so we can set in on a MessageContext and test 
            // serialization / deserialization.  We do this using NON-Public, INTERNAL SPIs
            // since we need to get at the internals of the engine
            
            // Check the AxisService created for both ports.
            org.apache.axis2.jaxws.spi.BindingProvider bindingProvider1 = 
                (org.apache.axis2.jaxws.spi.BindingProvider) port1;
            EndpointDescription endpointDesc1 = bindingProvider1.getEndpointDescription();
            AxisService axisService1 = endpointDesc1.getAxisService();
            assertNotNull(axisService1);
            assertEquals(svc1QN.getLocalPart(), axisService1.getName());
            
            org.apache.axis2.jaxws.spi.BindingProvider bindingProvider2 = 
                (org.apache.axis2.jaxws.spi.BindingProvider) port2;
            EndpointDescription endpointDesc2 = bindingProvider2.getEndpointDescription();
            AxisService axisService2 = endpointDesc2.getAxisService();
            assertNotNull(axisService2);
            assertNotSame(axisService1, axisService2);
            // The 2nd AxisService created gets a unique ID appended to the name
            String baseName = svc2QN.getLocalPart();
            assertFalse(baseName.equals(axisService2.getName()));
            assertTrue(axisService2.getName().startsWith(baseName));

            // Now that the AxisService is setup, create two Axis2 message contexts, set the
            // AxisServices on them.  Serialize them out
            MessageContext msgCtx1 = new MessageContext();
            msgCtx1.setAxisService(axisService1);
            msgCtx1.setAxisServiceGroup(axisService1.getAxisServiceGroup());
            ByteArrayOutputStream baos1 = serializeMessageContext(msgCtx1);
            
            MessageContext msgCtx2 = new MessageContext();
            msgCtx2.setAxisService(axisService2);
            msgCtx2.setAxisServiceGroup(axisService2.getAxisServiceGroup());
            ByteArrayOutputStream baos2 = serializeMessageContext(msgCtx2);
            
            // Read in the message contexts and activate them, which is required by message
            // context deserialization to connect the message context to existing runtime 
            // objects such as AxisService
            // Note that we do them in reverse order to make sure the logic is order-independent
            
            // Do the same for the second Message Context
            MessageContext mcRead2 = deserializeMessageContext(baos2);
            ConfigurationContext configContext2 = endpointDesc2.getServiceDescription().getAxisConfigContext();
            assertNotNull(configContext2);
            mcRead2.activate(configContext2);
            AxisService asRead2 = mcRead2.getAxisService();
            assertNotNull(asRead2);
            assertEquals(axisService2.getName(), asRead2.getName());
            assertSame(axisService2, asRead2);
            AxisServiceGroup agRead2 = mcRead2.getAxisServiceGroup();
            assertNotNull(agRead2);

            MessageContext mcRead1 = deserializeMessageContext(baos1);
            ConfigurationContext configContext1 = endpointDesc1.getServiceDescription().getAxisConfigContext();
            assertSame(configContext1, configContext2);
            assertNotNull(configContext1);
            mcRead1.activate(configContext1);
            AxisService asRead1 = mcRead1.getAxisService();
            assertNotNull(asRead1);
            assertEquals(axisService1.getName(), asRead1.getName());
            assertSame(axisService1, asRead1);
            AxisServiceGroup agRead1 = mcRead1.getAxisServiceGroup();
            assertNotNull(agRead1);

            
            // These keep the ports from being GC'd before the test method completes and
            // freeing up the AxisServices
            assertNotNull(port1);
            assertNotNull(port2);
            
        } catch (Exception t) {
            t.printStackTrace();
            fail("Caught throwable " + t);
        } finally {
            ClientMetadataTest.restoreOriginalFactory();
        }
    }
    /**
     * Validate that a message context can be serialized and deserialized with the Axis2 
     * and JAX-WS description objects not being recreated.  This is similar to a message
     * being serialized and deserialized without the server being stopped.
     * 
     * This test uses two services with the same namespaces, each with a dynamic port of the
     * same name.
     */
    public void testDynamicPortMultipleServiceNoRecreateSameNS() {
        try {
            ClientMetadataTest.installCachingFactory();
            QName svc1QN = new QName(namespaceURI + "?1", svcLocalPart);
            QName svc2QN = new QName(namespaceURI + "?1", svcLocalPart);

            Service svc1 = Service.create(svc1QN);
            Service svc2 = Service.create(svc2QN);

            // Create the same port under the two different services.  Each port gets a unique
            // EPR so it will cause a new port to be created (rather than shared) which in 
            // turn causes a new AxisService to be crated.
            QName portQN = new QName(namespaceURI, dynamicPort + "_1");
            svc1.addPort(portQN, bindingID, epr + "1");
            Dispatch<String> port1 = svc1.createDispatch(portQN, String.class, Service.Mode.PAYLOAD);
            svc2.addPort(portQN, bindingID, epr + "2");
            Dispatch<String> port2 = svc2.createDispatch(portQN, String.class, Service.Mode.PAYLOAD);
            
            // We need to get the AxisService so we can set in on a MessageContext and test 
            // serialization / deserialization.  We do this using NON-Public, INTERNAL SPIs
            // since we need to get at the internals of the engine
            
            // Check the AxisService created for both ports.
            org.apache.axis2.jaxws.spi.BindingProvider bindingProvider1 = 
                (org.apache.axis2.jaxws.spi.BindingProvider) port1;
            EndpointDescription endpointDesc1 = bindingProvider1.getEndpointDescription();
            AxisService axisService1 = endpointDesc1.getAxisService();
            assertNotNull(axisService1);
            assertEquals(svc1QN.getLocalPart(), axisService1.getName());
            
            org.apache.axis2.jaxws.spi.BindingProvider bindingProvider2 = 
                (org.apache.axis2.jaxws.spi.BindingProvider) port2;
            EndpointDescription endpointDesc2 = bindingProvider2.getEndpointDescription();
            AxisService axisService2 = endpointDesc2.getAxisService();
            assertNotNull(axisService2);
            assertNotSame(axisService1, axisService2);
            // The 2nd AxisService created gets a unique ID appended to the name
            String baseName = svc2QN.getLocalPart();
            assertFalse(baseName.equals(axisService2.getName()));
            assertTrue(axisService2.getName().startsWith(baseName));
            
            // Now that the AxisService is setup, create two Axis2 message contexts, set the
            // AxisServices on them.  Serialize them out
            MessageContext msgCtx1 = new MessageContext();
            msgCtx1.setAxisService(axisService1);
            msgCtx1.setAxisServiceGroup(axisService1.getAxisServiceGroup());
            ByteArrayOutputStream baos1 = serializeMessageContext(msgCtx1);
            
            MessageContext msgCtx2 = new MessageContext();
            msgCtx2.setAxisService(axisService2);
            msgCtx2.setAxisServiceGroup(axisService2.getAxisServiceGroup());
            ByteArrayOutputStream baos2 = serializeMessageContext(msgCtx2);
            
            // Read in the message contexts and activate them, which is required by message
            // context deserialization to connect the message context to existing runtime 
            // objects such as AxisService
            MessageContext mcRead1 = deserializeMessageContext(baos1);
            ConfigurationContext configContext1 = endpointDesc1.getServiceDescription().getAxisConfigContext();
            assertNotNull(configContext1);
            mcRead1.activate(configContext1);
            AxisService asRead1 = mcRead1.getAxisService();
            assertNotNull(asRead1);
            assertEquals(axisService1.getName(), asRead1.getName());
            assertSame(axisService1, asRead1);
            AxisServiceGroup agRead1 = mcRead1.getAxisServiceGroup();
            assertNotNull(agRead1);
           
            // Do the same for the second Message Context
            MessageContext mcRead2 = deserializeMessageContext(baos2);
            ConfigurationContext configContext2 = endpointDesc2.getServiceDescription().getAxisConfigContext();
            assertSame(configContext1, configContext2);
            assertNotNull(configContext2);
            mcRead2.activate(configContext2);
            AxisService asRead2 = mcRead2.getAxisService();
            assertNotNull(asRead2);
            assertEquals(axisService2.getName(), asRead2.getName());
            assertSame(axisService2, asRead2);
            AxisServiceGroup agRead2 = mcRead2.getAxisServiceGroup();
            assertNotNull(agRead2);

            // These keep the ports from being GC'd before the test method completes and
            // freeing up the AxisServices
            assertNotNull(port1);
            assertNotNull(port2);
            
        } catch (Exception t) {
            t.printStackTrace();
            fail("Caught throwable " + t);
        } finally {
            ClientMetadataTest.restoreOriginalFactory();
        }
    }

    /**
     * Validate that a message context can be serialized and deserialized with the Axis2 
     * and JAX-WS description objects being recreated before the deserialization.  This is 
     * similar to a message being serialized, the server being restarted and then the 
     * messgage being deserialized.  In this case it must be hooked up with new instances 
     * of what should be identical objects such as AxisServices.
     */
    public void testDynamicPortMultipleServiceWithRecreate() {
        try {
            ClientMetadataTest.installCachingFactory();
            QName svc1QN = new QName(namespaceURI + "?1", svcLocalPart);
            QName svc2QN = new QName(namespaceURI + "?2", svcLocalPart);
            QName portQN = new QName(namespaceURI, dynamicPort + "_1");
            
            Service svc1 = Service.create(svc1QN);
            Service svc2 = Service.create(svc2QN);

            // Create the same port under the two different services.  Each port gets a unique
            // EPR so it will cause a new port to be created (rather than shared) which in 
            // turn causes a new AxisService to be crated.
            svc1.addPort(portQN, bindingID, epr + "1");
            Dispatch<String> port1 = svc1.createDispatch(portQN, String.class, Service.Mode.PAYLOAD);
            svc2.addPort(portQN, bindingID, epr + "2");
            Dispatch<String> port2 = svc2.createDispatch(portQN, String.class, Service.Mode.PAYLOAD);
            
            // We need to get the AxisService so we can set in on a MessageContext and test 
            // serialization / deserialization.  We do this using NON-Public, INTERNAL SPIs
            // since we need to get at the internals of the engine
            
            // Check the AxisService created for both ports.
            org.apache.axis2.jaxws.spi.BindingProvider bindingProvider1 = 
                (org.apache.axis2.jaxws.spi.BindingProvider) port1;
            EndpointDescription endpointDesc1 = bindingProvider1.getEndpointDescription();
            AxisService axisService1 = endpointDesc1.getAxisService();
            assertNotNull(axisService1);
            assertEquals(svc1QN.getLocalPart(), axisService1.getName());
            
            org.apache.axis2.jaxws.spi.BindingProvider bindingProvider2 = 
                (org.apache.axis2.jaxws.spi.BindingProvider) port2;
            EndpointDescription endpointDesc2 = bindingProvider2.getEndpointDescription();
            AxisService axisService2 = endpointDesc2.getAxisService();
            assertNotNull(axisService2);
            assertNotSame(axisService1, axisService2);
            // The 2nd AxisService created gets a unique ID appended to the name
            String baseName = svc2QN.getLocalPart();
            assertFalse(baseName.equals(axisService2.getName()));
            assertTrue(axisService2.getName().startsWith(baseName));
            
            // Now that the AxisService is setup, create two Axis2 message contexts, set the
            // AxisServices on them.  Serialize them out
            MessageContext msgCtx1 = new MessageContext();
            msgCtx1.setAxisService(axisService1);
            msgCtx1.setAxisServiceGroup(axisService1.getAxisServiceGroup());
            ByteArrayOutputStream baos1 = serializeMessageContext(msgCtx1);
            
            MessageContext msgCtx2 = new MessageContext();
            msgCtx2.setAxisService(axisService2);
            msgCtx2.setAxisServiceGroup(axisService2.getAxisServiceGroup());
            ByteArrayOutputStream baos2 = serializeMessageContext(msgCtx2);
            
            // Now cause the runtime objects to be recreated.  Do this by forcing a release
            // of both Service instances, which will also release the runtime objects
            // such as the AxisService underneath them.  Then recreate the ports
            org.apache.axis2.jaxws.spi.ServiceDelegate.releaseService(svc1);
            org.apache.axis2.jaxws.spi.ServiceDelegate.releaseService(svc2);
        
            Service svc1_redo = Service.create(svc1QN);
            Service svc2_redo = Service.create(svc2QN);
            
            svc1_redo.addPort(portQN, bindingID, epr + "1");
            Dispatch<String> port1_redo = svc1_redo.createDispatch(portQN, String.class, Service.Mode.PAYLOAD);
            svc2_redo.addPort(portQN, bindingID, epr + "2");
            Dispatch<String> port2_redo = svc2_redo.createDispatch(portQN, String.class, Service.Mode.PAYLOAD);

            org.apache.axis2.jaxws.spi.BindingProvider bindingProvider1_redo = 
                (org.apache.axis2.jaxws.spi.BindingProvider) port1_redo;
            EndpointDescription endpointDesc1_redo = bindingProvider1_redo.getEndpointDescription();
            AxisService axisService1_redo = endpointDesc1_redo.getAxisService();
            assertNotNull(axisService1_redo);
            assertEquals(baseName, axisService1_redo.getName());
            
            org.apache.axis2.jaxws.spi.BindingProvider bindingProvider2_redo = 
                (org.apache.axis2.jaxws.spi.BindingProvider) port2_redo;
            EndpointDescription endpointDesc2_redo = bindingProvider2_redo.getEndpointDescription();
            AxisService axisService2_redo = endpointDesc2_redo.getAxisService();
            assertNotNull(axisService2_redo);
            assertNotSame(axisService1_redo, axisService2_redo);
            Parameter svcQNParam1_redo = axisService1_redo.getParameter(WSDL11ToAllAxisServicesBuilder.WSDL_SERVICE_QNAME);
            assertEquals(svc1QN, svcQNParam1_redo.getValue());
            // The 2nd AxisService created gets a unique ID appended to the name
            assertFalse(baseName.equals(axisService2_redo.getName()));
            assertTrue(axisService2_redo.getName().startsWith(baseName));
            Parameter svcQNParam2_redo = axisService2_redo.getParameter(WSDL11ToAllAxisServicesBuilder.WSDL_SERVICE_QNAME);
            assertEquals(svc2QN, svcQNParam2_redo.getValue());

            // Read in the message contexts and activate them, which is required by message
            // context deserialization to connect the message context to existing runtime 
            // objects such as AxisService
            MessageContext mcRead1 = deserializeMessageContext(baos1);
            ConfigurationContext configContext1 = endpointDesc1_redo.getServiceDescription().getAxisConfigContext();
            assertNotNull(configContext1);
            mcRead1.activate(configContext1);
            AxisService asRead1 = mcRead1.getAxisService();
            assertNotNull(asRead1);
            assertEquals(axisService1_redo.getName(), asRead1.getName());
            assertSame(axisService1_redo, asRead1);
            AxisServiceGroup agRead1 = mcRead1.getAxisServiceGroup();
            assertNotNull(agRead1);

            
            // Do the same for the second Message Context
            MessageContext mcRead2 = deserializeMessageContext(baos2);
            ConfigurationContext configContext2 = endpointDesc2_redo.getServiceDescription().getAxisConfigContext();
            assertNotNull(configContext2);
            assertSame(configContext1, configContext2);
            mcRead2.activate(configContext2);
            AxisService asRead2 = mcRead2.getAxisService();
            assertNotNull("AxisService was not activated", asRead2);
            assertEquals(axisService2_redo.getName(), asRead2.getName());
            assertSame(axisService2_redo, asRead2);
            AxisServiceGroup agRead2 = mcRead2.getAxisServiceGroup();
            assertNotNull("AxisServiceGroup was not activated", agRead2);
            
            // These keep the ports from being GC'd before the test method completes and
            // freeing up the AxisServices
            assertNotNull(port1);
            assertNotNull(port2);
            assertNotNull(port1_redo);
            assertNotNull(port2_redo);
            
        } catch (Exception t) {
            t.printStackTrace();
            fail("Caught throwable " + t);
        } finally {
            ClientMetadataTest.restoreOriginalFactory();
        }
    }


    /**
     * Deserialize a message context from the stream.  IMPORTANT NOTE!  After the 
     * message context is deserialized, the activate(...) method must be called on it.
     * @param baos
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private MessageContext deserializeMessageContext(ByteArrayOutputStream baos)
            throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        
        MessageContext mcRead = new MessageContext();
        mcRead.readExternal(ois);
        return mcRead;
    }

    private ByteArrayOutputStream serializeMessageContext(MessageContext msgCtx)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        msgCtx.writeExternal(oos);
        oos.flush();
        oos.close();
        return baos;
    }

}
