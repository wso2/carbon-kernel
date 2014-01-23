/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.core.transports;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.deployment.DeploymentConstants;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.core.RegistryResources;
import org.wso2.carbon.core.persistence.AbstractPersistenceManager;
import org.wso2.carbon.core.persistence.PersistenceFactory;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;

import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TransportPersistenceManager extends AbstractPersistenceManager {

    private static final String TRANSPORT_LISTENER = "listener";
    private static final String TRANSPORT_SENDER = "sender";

    private static final Log log = LogFactory.getLog(TransportPersistenceManager.class);

    /**
     * Constructor gets the axis config and create reference to the config registry instances.
     *
     * @param axisConfig - AxisConfiguration
     * @throws org.apache.axis2.AxisFault - if the config registry is not found
     */
    public TransportPersistenceManager(AxisConfiguration axisConfig) throws AxisFault {
        super(axisConfig);
        try {
            if (this.pf == null) {
                this.pf = PersistenceFactory.getInstance(axisConfig);
            }
        } catch (Exception e) {
            log.error("Error getting PersistenceFactory instance", e);
        }
    }

    public void saveTransportListener(TransportInDescription transportIn,
                                      boolean enabled) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("Saving the " + transportIn.getName() + " listener configuration");
        }
        OMElement element = TransportBuilderUtils.serializeTransportListener(transportIn);
        saveTransport(element, enabled);
    }

    public void saveTransportSender(TransportOutDescription transportOut,
                                    boolean enabled) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("Saving the " + transportOut.getName() + " sender configuration");
        }
        OMElement element = TransportBuilderUtils.serializeTransportSender(transportOut);
        saveTransport(element, enabled);
    }

    public void saveTransport(OMElement element, boolean enabled) throws Exception {

        boolean listener = false;
        if (DeploymentConstants.TAG_TRANSPORT_RECEIVER.equals(element.getLocalName())) {
            listener = true;
        } else if (DeploymentConstants.TAG_TRANSPORT_SENDER.equals(element.getLocalName())) {
            listener = false;
        } else {
            handleFault("Invalid transport configuration element");
        }

        String name = element.getAttributeValue(new QName(DeploymentConstants.ATTRIBUTE_NAME));
        if (name == null || "".equals(name)) {
            handleFault("Transport configuration does not specify the name attribute");
        }

        String path = RegistryResources.TRANSPORTS + name + "/";
        if (listener) {
            path += TRANSPORT_LISTENER;
        } else {
            path += TRANSPORT_SENDER;
        }

        Resource resource = configRegistry.newResource();
        resource.setContent(element.toString());
        resource.setProperty(RegistryResources.Transports.PROTOCOL_NAME, name);
        resource.setProperty(RegistryResources.Transports.IS_ENABLED, String.valueOf(enabled));
        configRegistry.put(path, resource);
        resource.discard();
    }

    public OMElement getTransportElement(String name, boolean listener) throws Exception {
        Resource resource = getTransportResource(configRegistry, name, listener);

        if (resource != null) {
            ByteArrayInputStream in = new ByteArrayInputStream((byte[]) resource.getContent());
            resource.discard();
            StAXOMBuilder builder = new StAXOMBuilder(in);
            OMElement el = builder.getDocumentElement();
            in.close();
            return el;
        }
        return null;
    }

    public TransportInDescription getTransportListener(String name,
                                                       boolean init) throws Exception {

        OMElement element = getTransportElement(name, true);
        if (element != null) {
            return TransportBuilderUtils.processTransportReceiver(element, init);
        }
        return null;
    }

    public TransportOutDescription getTransportSender(String name,
                                                      boolean init) throws Exception {

        OMElement element = getTransportElement(name, false);
        if (element != null) {
            return TransportBuilderUtils.processTransportSender(element, init);
        }
        return null;
    }

    public void addParameter(String name, boolean listener, boolean enabled,
                             Parameter p) throws Exception {

        OMElement element = getTransportElement(name, listener);
        if (element != null) {
            element.addChild(TransportBuilderUtils.serializeParameter(p,
                                                                      OMAbstractFactory.getOMFactory()));
            saveTransport(element, enabled);
        }
    }

    public void removeParameter(String name, boolean listener, boolean enabled,
                                String paramName) throws Exception {

        OMElement element = getTransportElement(name, listener);
        if (element != null) {
            Iterator params = element.getChildrenWithLocalName(DeploymentConstants.TAG_PARAMETER);
            OMElement p = null;
            while (params.hasNext()) {
                p = (OMElement) params.next();
                if (paramName.equals(p.getAttributeValue(new QName(
                        DeploymentConstants.ATTRIBUTE_NAME)))) {
                    break;
                }
            }
            if (p != null) {
                p.detach();
            }
            saveTransport(element, enabled);
        }
    }

    public String[] getEnabledTransports(boolean listener) throws Exception {
        if (!configRegistry.resourceExists(RegistryResources.TRANSPORTS)) {
            return null;
        }

        Collection transports = (Collection) configRegistry.get(RegistryResources.TRANSPORTS);
        String[] childResources = transports.getChildren();
        if (childResources == null || childResources.length == 0) {
            return null;
        }

        List<String> enabledTransports = new ArrayList<String>();
        for (String childPath : childResources) {
            if (!childPath.endsWith("/")) {
                childPath += "/";
            }

            if (listener) {
                childPath += TRANSPORT_LISTENER;
            } else {
                childPath += TRANSPORT_SENDER;
            }

            if (configRegistry.resourceExists(childPath)) {
                Resource resource = configRegistry.get(childPath);
                if (Boolean.valueOf(resource.getProperty(RegistryResources.Transports.IS_ENABLED))) {
                    enabledTransports.add(resource.getProperty(RegistryResources.
                            Transports.PROTOCOL_NAME));
                }
            }
        }

        if (enabledTransports.size() > 0) {
            return enabledTransports.toArray(new String[enabledTransports.size()]);
        }

        return null;
    }

    public Resource getTransportResource(String name) throws Exception {
        return getTransportResource(configRegistry, name, true);
    }

    public void setTransportEnabled(String name, boolean listener,
                                    boolean enabled) throws Exception {

        Resource resource = getTransportResource(configRegistry, name, listener);
        if (resource != null) {
            resource.setProperty(RegistryResources.Transports.IS_ENABLED, String.valueOf(enabled));
            configRegistry.put(resource.getPath(), resource);
            resource.discard();
        }
    }

    public void updateEnabledTransports(
            java.util.Collection<TransportInDescription> listeners,
            java.util.Collection<TransportOutDescription> senders) throws Exception {

        for (TransportInDescription listener : listeners) {
            if (getTransportResource(configRegistry, listener.getName(), true) == null) {
                // We are explicitly setting the 'enabled' attribute to 'false' here.
                // This condition can only occur during the first startup and at that time
                // all transports are loaded from the axis2.xml.
                saveTransportListener(listener, false);
            }
        }

        for (TransportOutDescription sender : senders) {
            if (getTransportResource(configRegistry, sender.getName(), false) == null) {
                // We are explicitly setting the 'enabled' attribute to 'false' here.
                // This condition can only occur during the first startup and at that time
                // all transports are loaded from the axis2.xml.
                saveTransportSender(sender, false);
            }
        }
    }

    /**
     * This method checks whether the configuration of a particular transport is available in
     * the registry. If it does then the method will simply return. Otherwise it will attempt
     * to load the transport configurations from the specified transport configuration file.
     * Once loaded the transport configurations will be permanently stored in the registry so
     * the subsequent calls to this method will always find the transport configurations in
     * the registry.
     *
     * @param transport     Name of the transport
     * @param configFileURL URL to the transport configuration file available in the transport bundle
     * @throws Exception on error
     */
    public void saveTransportConfiguration(String transport,
                                           URL configFileURL) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("Persiting the initial transport configuration for " + transport);
        }

        if (getTransportElement(transport, true) == null) {
            OMElement conf = TransportBuilderUtils.parseTransportConfiguration(
                    transport, configFileURL, true);
            if (conf != null) {
                saveTransport(conf, false);
            } else {
                log.warn("No transport listener configuration found for : " + transport);
            }
        }

        if (getTransportElement(transport, false) == null) {
            OMElement conf = TransportBuilderUtils.parseTransportConfiguration(
                    transport, configFileURL, false);
            if (conf != null) {
                saveTransport(conf, false);
            } else {
                log.warn("No transport sender configuration found for : " + transport);
            }
        }
    }

    private Resource getTransportResource(Registry registry, String name,
                                          boolean listener) throws Exception {

        String path = RegistryResources.TRANSPORTS + name + "/";
        if (listener) {
            path += TRANSPORT_LISTENER;
        } else {
            path += TRANSPORT_SENDER;
        }

        if (registry.resourceExists(path)) {
            return registry.get(path);
        }
        return null;
    }

    private void handleFault(String msg) throws CarbonException {
        log.error(msg);
        throw new CarbonException(msg);
    }

}
