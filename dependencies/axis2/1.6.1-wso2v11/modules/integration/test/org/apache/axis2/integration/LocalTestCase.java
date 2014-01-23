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

package org.apache.axis2.integration;

import junit.framework.TestCase;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.DispatchPhase;
import org.apache.axis2.engine.Phase;
import org.apache.axis2.receivers.RawXMLINOnlyMessageReceiver;
import org.apache.axis2.receivers.RawXMLINOutMessageReceiver;
import org.apache.axis2.rpc.client.RPCServiceClient;
import org.apache.axis2.transport.local.LocalTransportReceiver;
import org.apache.axis2.transport.local.LocalTransportSender;

import java.util.ArrayList;

/**
 * LocalTestCase is an extendable base class which provides common functionality
 * for building JUnit tests which exercise Axis2 using the (fast, in-process)
 * "local" transport.
 */
public class LocalTestCase extends TestCase {
    /** Our server AxisConfiguration */
    protected AxisConfiguration serverConfig;

    /** Our client ConfigurationContext */
    protected ConfigurationContext clientCtx;

    protected ConfigurationContext serverCtx;

    LocalTransportSender sender = new LocalTransportSender();

    protected void setUp() throws Exception {
        // Configuration - server side
        serverCtx = ConfigurationContextFactory.
                createConfigurationContextFromFileSystem(null);
        serverConfig = serverCtx.getAxisConfiguration();
        LocalTransportReceiver.CONFIG_CONTEXT = new ConfigurationContext(serverConfig);
        LocalTransportReceiver.CONFIG_CONTEXT.setServicePath("services");
        LocalTransportReceiver.CONFIG_CONTEXT.setContextRoot("local:/");
        TransportOutDescription tOut = new TransportOutDescription(Constants.TRANSPORT_LOCAL);
        tOut.setSender(new LocalTransportSender());
        serverConfig.addTransportOut(tOut);

//        addInPhases(serverConfig.getInFlowPhases());
//        DispatchPhase dp = (DispatchPhase)serverConfig.getInFlowPhases().get(1);
//        dp.addHandler(new AddressingBasedDispatcher());
//
//        addInPhases(serverConfig.getInFaultFlowPhases());
//
//        addOutPhases(serverConfig.getOutFlowPhases());
//        addOutPhases(serverConfig.getOutFaultFlowPhases());

        ///////////////////////////////////////////////////////////////////////
        // Set up raw message receivers for OMElement based tests

        serverConfig.addMessageReceiver(WSDL2Constants.MEP_URI_IN_ONLY,
                                        new RawXMLINOnlyMessageReceiver());
        serverConfig.addMessageReceiver(WSDL2Constants.MEP_URI_IN_OUT,
                                        new RawXMLINOutMessageReceiver());
        serverConfig.addMessageReceiver(WSDL2Constants.MEP_URI_ROBUST_IN_ONLY,
                                        new RawXMLINOutMessageReceiver());

        ///////////////////////////////////////////////////////////////////////
        // And client side
        clientCtx = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null);
    }

    /**
     * Add well-known Phases on the in side
     *
     * @param flow the Flow in which to add these Phases
     */
    private void addInPhases(ArrayList flow) {
        flow.add(new Phase("PreDispatch"));
        Phase dispatchPhase = new DispatchPhase("Dispatch");
        flow.add(dispatchPhase);
    }

    /**
     * Add well-known Phases on the out side
     *
     * @param flow the Flow in which to add these Phases
     */
    private void addOutPhases(ArrayList flow) {
        flow.add(new Phase("MessageOut"));
    }

    /**
     * Deploy a class as a service.
     *
     * @param name the service name
     * @param myClass the Java class to deploy (all methods exposed by default)
     * @return a fully configured AxisService, already deployed into the server
     * @throws Exception in case of problems
     */
    protected AxisService deployClassAsService(String name, Class myClass) throws Exception {
        AxisService service = new AxisService(name);
        service.addParameter(Constants.SERVICE_CLASS,
                              myClass.getName());

        Utils.fillAxisService(service, serverConfig, null, null);

        serverConfig.addService(service);
        return service;
    }

    /**
     * Deploy a class as a service.
     *
     * @param name the service name
     * @param myClass the Java class to deploy (all methods exposed by default)
     * @return a fully configured AxisService, already deployed into the server
     * @throws Exception in case of problems
     */
    /**
     * Deploy a class as a service.
     *
     * @param name the service name
     * @param myClass the Java class to deploy (all methods exposed by default)
     * @param scope the service scope
     * @return a fully configured AxisService, already deployed into the server
     * @throws Exception in case of problems
     */
    protected AxisService deployClassAsService(String name, Class myClass, String scope)
            throws Exception {
         AxisService service = AxisService.createService(myClass.getName(),serverConfig);
         if (scope != null) service.setScope(scope);
        service.addParameter(Constants.SERVICE_CLASS,
                              myClass.getName());
        service.setName(name);
        serverCtx.deployService(service);
        return service;
    }



    /**
     * Get a pre-initialized ServiceClient set up to talk to our local
     * server.  If you want to set options, call this and then use getOptions()
     * on the return.
     *
     * @return a ServiceClient, pre-initialized to talk using our local sender
     * @throws AxisFault if there's a problem
     */
    protected ServiceClient getClient() throws AxisFault {
        Options opts = getOptions();
        ServiceClient client = new ServiceClient(clientCtx, null);
        client.setOptions(opts);
        return client;
    }

    /**
     * Get a pre-initialized ServiceClient set up to talk to our local
     * server.  If you want to set options, call this and then use getOptions()
     * on the return.
     *
     * @return a ServiceClient, pre-initialized to talk using our local sender
     * @throws AxisFault if there's a problem
     */
    protected RPCServiceClient getRPCClient() throws AxisFault {
        Options opts = getOptions();
        RPCServiceClient client = new RPCServiceClient(clientCtx, null);
        client.setOptions(opts);
        return client;
    }

    /**
     * Get a pre-initialized ServiceClient set up to talk to our local
     * server.  If you want to set options, call this and then use getOptions()
     * on the return. Clients created using this method have their To EPR
     * preset to include the address for the service+operation.
     *
     * @return a ServiceClient, pre-initialized to talk using our local sender
     * @throws AxisFault if there's a problem
     */
    protected ServiceClient getClient(String serviceName, String operationName) throws AxisFault {
        String url = LocalTransportReceiver.CONFIG_CONTEXT.getServiceContextPath()+"/"+serviceName;

        Options opts = getOptions();
        opts.setTo(new EndpointReference(url));
        opts.setAction(operationName);
        ServiceClient client = new ServiceClient(clientCtx, null);
        client.setOptions(opts);
        return client;
    }

    /**
     * Get an Options object initialized with the right transport info, defaulting to SOAP 1.2
     *
     * @return pre-initialized Options object
     */
    protected Options getOptions() {
        TransportOutDescription td = new TransportOutDescription("local");
        td.setSender(sender);

        Options opts = new Options();
        opts.setTransportOut(td);
        opts.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        return opts;
    }
}
