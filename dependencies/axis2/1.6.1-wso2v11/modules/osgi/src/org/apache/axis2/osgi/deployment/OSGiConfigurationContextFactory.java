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
package org.apache.axis2.osgi.deployment;

import org.apache.axis2.AxisFault;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.deployment.Deployer;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.engine.*;
import static org.apache.axis2.osgi.deployment.OSGiAxis2Constants.*;
import org.apache.axis2.osgi.deployment.tracker.BundleTracker;
import org.apache.axis2.osgi.deployment.tracker.WSTracker;
import org.apache.axis2.osgi.tx.HttpListener;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.transport.TransportSender;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.*;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

import java.util.Dictionary;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * OSGiConfigurationContextFactory creates ConfigurationContext, which is the ultimate Axis2 environment.
 * This creation is handled as a ManagedService service, thus, Configuraiton Admin has control over it.
 */
public class OSGiConfigurationContextFactory implements ManagedService {

    private static Log log = LogFactory.getLog(OSGiConfigurationContextFactory.class);

    private BundleContext context;

    private ServiceRegistration mngServiceRegistration;

    private ConfigurationContext configCtx;

    private ServiceRegistration configCtxServiceRegistration;

    private BundleTracker bundleTracker;

    public synchronized void start(BundleContext context) {
        this.context = context;
        bundleTracker = new BundleTracker(context);
        Dictionary props = new Properties();
        props.put(Constants.SERVICE_PID, "org.apache.axis2.osgi");
        mngServiceRegistration =
                context.registerService(ManagedService.class.getName(), this, props);
    }

    public synchronized void stop() {
        if (mngServiceRegistration != null) {
            mngServiceRegistration.unregister();
        }
        bundleTracker.close();
        if (configCtx != null) {
            try {
                configCtx.terminate();
                configCtx = null;
            } catch (AxisFault e) {
                String msg = "Error while ConfigurationContext is terminated";
                log.error(msg, e);
            }
        }
        log.info("Axis2 environment has stopped");
    }

    public synchronized void startConfigurationContext(Dictionary dictionary) throws AxisFault {
        AxisConfigurator configurator = new OSGiServerConfigurator(context);
        configCtx = ConfigurationContextFactory.createConfigurationContext(configurator);
        ListenerManager listenerManager = new ListenerManager();
        listenerManager.init(configCtx);
        listenerManager.start();
    }

    public void updated(Dictionary dictionary) throws ConfigurationException {
        try {
            startConfigurationContext(dictionary);
            if (configCtxServiceRegistration != null) {
                configCtxServiceRegistration.unregister();
            }
            //register ConfigurationContext as a OSGi serivce
            configCtxServiceRegistration =
                    context.registerService(ConfigurationContext.class.getName(), configCtx, null);

            Registry servicesRegistry = new ServiceRegistry(context, configCtx);
            Registry moduleRegistry = new ModuleRegistry(context, configCtx, servicesRegistry);
            bundleTracker.addRegistry(servicesRegistry);
            bundleTracker.addRegistry(moduleRegistry);
            bundleTracker.open();

            new WSTracker(configCtx, context).open();

            context.addServiceListener(new AxisConfigServiceListener(configCtx, context));

            Dictionary prop = new Properties();
            prop.put(PROTOCOL, "http");
            //adding the default listener
            context.registerService(TransportListener.class.getName(), new HttpListener(context),
                                    prop);
            log.info("Axis2 environment has started.");
        } catch (AxisFault e) {
            String msg = "Error while creating ConfigurationContext";
            log.error(msg, e);
            throw new ConfigurationException(msg, msg, e);
        }

    }

    /**
     * @see org.osgi.framework.ServiceListener
     *      <p/>
     *      AxisConfigServiceListener is a ServiceListener. This class listen to OSGi services and
     *      build the appropriate AxisConfiguration plugins. These plugins include, message receivers,
     *      transport listeners, transport senders, message formatters & builders, etc.
     */
    private static class AxisConfigServiceListener implements ServiceListener {

        private ConfigurationContext configCtx;

        private AxisConfiguration axisConfig;

        private BundleContext context;

        private Lock lock = new ReentrantLock();

        public AxisConfigServiceListener(ConfigurationContext configCtx, BundleContext context) {
            this.configCtx = configCtx;
            this.context = context;
            this.axisConfig = configCtx.getAxisConfiguration();
        }

        public void serviceChanged(ServiceEvent event) {
            ServiceReference reference = event.getServiceReference();
            Object service = context.getService(reference);
            if (service instanceof TransportListener) {
                String protocol = (String) reference.getProperty(PROTOCOL);
                if (protocol == null || protocol.length() == 0) {
                    String msg = "Protocol is not found for the trnasport object";
                    log.error(msg);
                    throw new RuntimeException(msg);
                }
                if (event.getType() == ServiceEvent.REGISTERED) {
                    TransportListener txListener =
                            (TransportListener) service;

                    TransportInDescription txInDes = new TransportInDescription(protocol);
                    txInDes.setReceiver(txListener);
                    String[] keys = reference.getPropertyKeys();
                    if (keys != null) {
                        for (String key : keys) {
                            if (key.equals(PROTOCOL)) {
                                continue;
                            }
                            //TODO: assume String properties at this moment.
                            try {
                                Object propObj = reference.getProperty(key);
                                if (propObj instanceof String) {
                                    String value = (String) propObj;
                                    Parameter param = new Parameter(key, value);
                                    txInDes.addParameter(param);
                                }
                            } catch (AxisFault e) {
                                String msg = "Error while reading transport properties from :" +
                                             txListener.toString();
                                log.error(msg, e);
                                throw new RuntimeException(msg, e);
                            }
                        }
                    }
                    try {
                        configCtx.getListenerManager().addListener(txInDes, false);
                        //Now update the AxisService endpoint map
                        lock.lock();
                        try {
                            for (Iterator iterator = axisConfig.getServices().keySet().iterator();
                                 iterator.hasNext();) {
                                String serviceName = (String) iterator.next();
                                AxisService axisService = axisConfig.getService(serviceName);
                                Utils.addEndpointsToService(axisService, axisConfig);
                            }
                        } finally {
                            lock.unlock();
                        }
                    } catch (AxisFault e) {
                        String msg = "Error while intiating and starting the listener";
                        log.error(msg, e);
                        throw new RuntimeException(msg, e);
                    }
                }

            } else if (service instanceof Builder) {
                String contextType = (String) reference.getProperty(CONTENT_TYPE);
                if (contextType == null || contextType.length() == 0) {
                    String msg = CONTENT_TYPE + " is missing from builder object";
                    log.error(msg);
                    throw new RuntimeException(msg);
                }
                if (event.getType() == ServiceEvent.REGISTERED || event.getType() ==
                                                                  ServiceEvent.MODIFIED) {
                    Builder builder = (Builder) service;
                    lock.lock();
                    try {
                        axisConfig.addMessageBuilder(contextType, builder);
                    } finally {
                        lock.unlock();
                    }
                }
            } else if (service instanceof MessageFormatter) {
                String contextType = (String) reference.getProperty(CONTENT_TYPE);
                if (contextType == null || contextType.length() == 0) {
                    String msg = CONTENT_TYPE + " is missing from formatter object";
                    log.error(msg);
                    throw new RuntimeException(msg);
                }
                if (event.getType() == ServiceEvent.REGISTERED || event.getType() ==
                                                                  ServiceEvent.MODIFIED) {
                    MessageFormatter formatter = (MessageFormatter) service;
                    lock.lock();
                    try {
                        axisConfig.addMessageFormatter(contextType, formatter);
                    } finally {
                        lock.unlock();
                    }
                }
            } else if (service instanceof MessageReceiver) {
                String mep = (String) reference.getProperty(MEP);
                if (mep == null || mep.length() == 0) {
                    String msg = MEP + " is missing from message receiver object";
                    log.error(msg);
                    throw new RuntimeException(msg);
                }
                if (event.getType() == ServiceEvent.REGISTERED || event.getType() ==
                                                                  ServiceEvent.MODIFIED) {
                    MessageReceiver mr = (MessageReceiver) service;
                    lock.lock();
                    try {
                        axisConfig.addMessageReceiver(mep, mr);
                    } finally {
                        lock.unlock();
                    }
                }
            } else if (service instanceof AxisObserver) {
                if (event.getType() == ServiceEvent.REGISTERED || event.getType() ==
                                                                  ServiceEvent.MODIFIED) {
                    AxisObserver axisObserver = (AxisObserver) service;
                    lock.lock();
                    try {
                        axisObserver.init(axisConfig);
                        axisConfig.addObservers(axisObserver);
                    } finally {
                        lock.unlock();
                    }
                }
            } else if (service instanceof TransportSender) {
                //TODO: TBD
            } else if (service instanceof Deployer) {
                // TODO: TBD, there is no Axis2 API yet available to add deployers.
            }
        }
    }

}
