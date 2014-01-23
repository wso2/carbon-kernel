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

import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.ListenerManager;
import org.apache.axis2.receivers.RawXMLINOnlyMessageReceiver;
import org.apache.axis2.receivers.RawXMLINOutMessageReceiver;
import org.apache.axis2.wsdl.WSDLConstants;

/**
 * Base class for transport util servers used in unit testing
 */
public class UtilsTransportServer {

    private final ListenerManager listnMgr;
    private final ConfigurationContext cfgCtx;

    public UtilsTransportServer() throws AxisFault {
        cfgCtx = ConfigurationContextFactory.
                    createConfigurationContext(new CustomAxisConfigurator());

        // create listener manager
        listnMgr = new ListenerManager();
        cfgCtx.setTransportManager(listnMgr);
    }
    
    public void addTransport(TransportInDescription trpInDesc,
                             TransportOutDescription trpDescOut) throws AxisFault {
        cfgCtx.getAxisConfiguration().addTransportIn(trpInDesc);
        if (trpDescOut != null) {
            trpDescOut.getSender().init(cfgCtx, trpDescOut);
            cfgCtx.getAxisConfiguration().addTransportOut(trpDescOut);
        }
    }

    public void addTransport(TransportInDescription trpInDesc) throws AxisFault {
        addTransport(trpInDesc, null);
    }

    public void start() throws Exception {
        listnMgr.init(cfgCtx);
        listnMgr.start();
    }
    
    public void stop() throws Exception {
        listnMgr.stop();
        listnMgr.destroy();
    }

    public void enableAddressing() throws AxisFault {
        cfgCtx.getAxisConfiguration().engageModule("addressing");
    }

    public ConfigurationContext getConfigurationContext() {
        return cfgCtx;
    }

    public AxisConfiguration getAxisConfiguration() {
        return cfgCtx.getAxisConfiguration();
    }
    
    /**
     * Deploy the standard Echo service with the custom parameters passed in
     * @param name the service name to assign
     * @param parameters the parameters for the service
     * @throws Exception 
     */
    public void deployEchoService(String name, List<Parameter> parameters) throws Exception {

        AxisService service = new AxisService(name);
        service.setClassLoader(Thread.currentThread().getContextClassLoader());
        service.addParameter(new Parameter(Constants.SERVICE_CLASS, Echo.class.getName()));

        // add operation echoOMElement
        AxisOperation axisOp = new InOutAxisOperation(new QName("echoOMElement"));
        axisOp.setMessageReceiver(new RawXMLINOutMessageReceiver());
        axisOp.setStyle(WSDLConstants.STYLE_RPC);
        service.addOperation(axisOp);
        service.mapActionToOperation(Constants.AXIS2_NAMESPACE_URI + "/echoOMElement", axisOp);

        // add operation echoOMElementNoResponse
        axisOp = new InOutAxisOperation(new QName("echoOMElementNoResponse"));
        axisOp.setMessageReceiver(new RawXMLINOnlyMessageReceiver());
        axisOp.setStyle(WSDLConstants.STYLE_RPC);
        service.addOperation(axisOp);
        service.mapActionToOperation(Constants.AXIS2_NAMESPACE_URI + "/echoOMElementNoResponse", axisOp);

        for (Parameter parameter : parameters) {
            service.addParameter(parameter);
        }

        cfgCtx.getAxisConfiguration().addService(service);
    }
}
