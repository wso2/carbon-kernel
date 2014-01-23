/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.axis2.osgi.deployment.tracker;

import org.osgi.framework.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.AxisFault;
import org.apache.axis2.osgi.deployment.BundleClassLoader;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.rpc.receivers.RPCInOnlyMessageReceiver;
import org.apache.axis2.rpc.receivers.RPCMessageReceiver;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * WSTracker will listen to registered services and expose them as Web Services (AxisService)
 * if and only if the attached Directory object contains the name/value pair "org.apache.axis2.ws".
 * In addition to this WSTracker uses different name/value pairs to manipulate AxisService as required.
 */
public class WSTracker {

    private boolean open;

    private final Lock lock = new ReentrantLock();

    private BundleContext context;

    private ServiceListener serviceListener;

    private static Log log = LogFactory.getLog(WSTracker.class);

    private List<Bundle> bundleList = new ArrayList<Bundle>();

    public static String AXIS2_WS = "org.apache.axis2.osgi.ws";

    private ConfigurationContext configCtx;

    public WSTracker(ConfigurationContext configCtx, BundleContext context) {
        this.context = context;
        this.configCtx = configCtx;
        this.serviceListener = new ServiceListener() {

            public void serviceChanged(ServiceEvent event) {
                int serviceType = event.getType();
                try {
                    switch (serviceType) {
                        case ServiceEvent.REGISTERED:
                            ServiceReference reference = event.getServiceReference();
                            createWS(reference.getBundle(), event.getServiceReference());
                            break;
                        case ServiceEvent.UNREGISTERING:
                            //TODO remove web service
                            break;
                    }
                } catch (AxisFault e) {
                    String msg = "Error while creating AxisService";
                    log.error(msg, e);
                }
            }
        };

    }

    public void open() {
        if (open) {
            return;
        }
        open = true;
        for (Bundle bundle : context.getBundles()) {
            if (bundle != context.getBundle()) {
                ServiceReference[] references = bundle.getRegisteredServices();
                try {
                    createWS(bundle, references);
                } catch (AxisFault e) {
                    String msg = "Error while creating AxisService from bundle : " +
                                 bundle.getBundleId();
                    log.error(msg, e);
                }
                bundleList.add(bundle);
            }
        }
        context.addServiceListener(serviceListener);
    }

    /**
     * ServiceReferece will be used to create the web service based on Directory objects.
     *
     * @param bundle;     associated bundle to obtain meta information
     * @param references; ServiceReferences array
     * @throws org.apache.axis2.AxisFault will be thrown
     */
    private void createWS(Bundle bundle, ServiceReference[] references) throws AxisFault {
        if (bundle != null && references != null) {
            for (ServiceReference reference : references) {
                createWS(bundle, reference);
            }
        }
    }

    /**
     * TODO: This method need more modifications
     *
     * @param bundle    bundle
     * @param reference reference
     * @throws AxisFault will be thrown
     */
    private void createWS(Bundle bundle, ServiceReference reference) throws AxisFault {
        if (bundle != null && reference != null) {
            Object axis2Ws = reference.getProperty(AXIS2_WS);
            if (axis2Ws == null) {
                return;
            }
            String wsName = axis2Ws.toString();
            lock.lock();
            try {
                Object service = context.getService(reference);
                AxisService axisService = AxisService.createService(
                        service.getClass().getName(),
                        configCtx.getAxisConfiguration(),
                        createDefaultMessageReceivers(),
                        null,
                        null,
                        new BundleClassLoader(bundle, WSTracker.class.getClassLoader()));
                axisService.setName(wsName);
                configCtx.getAxisConfiguration().addService(axisService);
                log.info("Added new WS from ServiceReference : " + service.getClass().getName());
            } finally {
                lock.unlock();
            }
        }
    }

    private Map createDefaultMessageReceivers() throws AxisFault {
        Map<String, MessageReceiver> messageReciverMap = new HashMap<String, MessageReceiver>();
        try {
            MessageReceiver messageReceiver = RPCInOnlyMessageReceiver.class.newInstance();
            messageReciverMap.put(WSDL2Constants.MEP_URI_IN_ONLY,
                                  messageReceiver);
            MessageReceiver inOutmessageReceiver = RPCMessageReceiver.class.newInstance();
            messageReciverMap.put(WSDL2Constants.MEP_URI_IN_OUT,
                                  inOutmessageReceiver);
            messageReciverMap.put(WSDL2Constants.MEP_URI_ROBUST_IN_ONLY,
                                  inOutmessageReceiver);
        } catch (InstantiationException e) {
            String msg = "Message receivers cannot be instantiated";
            log.error(msg, e);
            throw new AxisFault(msg, e);
        } catch (IllegalAccessException e) {
            String msg = "Illegal access";
            log.error(msg, e);
            throw new AxisFault(msg, e);
        }
        return messageReciverMap;
    }


}
