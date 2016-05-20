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
package org.wso2.carbon.utils.transport;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.SessionContext;
import org.apache.axis2.deployment.DeploymentConstants;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.transport.base.ManagementSupport;
import org.apache.axis2.transport.base.MetricsCollector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.NetworkUtils;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.utils.SessionContextUtil;

import javax.xml.namespace.QName;
import java.lang.reflect.Method;
import java.net.SocketException;
import java.util.Map;

/**
 * This class will provide basic functionality to write http and https listeners for WSO2 projects.
 */
public abstract class AbstractTransportListener implements TransportListener, ManagementSupport {
    private static final Log log = LogFactory.getLog(AbstractTransportListener.class);

    private static final String TRANSPORT_MANAGER =
            "org.wso2.carbon.tomcat.ext.transport.ServletTransportManager";
    private static final int DEFAULT_HTTP_PROXY_PORT = 80;
    private static final int DEFAULT_HTTPS_PROXY_PORT = 443;
    private Class transportManagerClass;
    private Object transportManager;
    private String transport;

    protected ConfigurationContext configurationContext;
    protected int port = -1;
    protected int proxyPort = -1;

    private boolean isStopped;

    /** Metrics collector for this transport */
    private MetricsCollector metrics = new MetricsCollector();

    public void init(ConfigurationContext configContext,
                     TransportInDescription transportIn) throws AxisFault {
        this.configurationContext = configContext;
        transport = transportIn.getName();
        if (CarbonUtils.isRunningInStandaloneMode()) {
            try {
                transportManagerClass = Class.forName(TRANSPORT_MANAGER);
                transportManager = transportManagerClass.newInstance();
                Method method = transportManagerClass.getMethod("getPort", String.class);
                port = (Integer) method.invoke(transportManager, transport);
                transportIn.addParameter(getParameter("port", String.valueOf(port)));
            } catch (Exception e) {
                String msg = "Cannot get transport port";
                log.error(msg, e);
                throw new AxisFault(msg, e);
            }
        }
        String portSysProp = System.getProperty(transport + "Port");
        if (portSysProp != null) {
            try {
                port = Integer.parseInt(portSysProp);
                if (log.isDebugEnabled()) {
                    log.debug("Using " + transport + " port " + port +" defined in System property " +
                             (transport + "Port"));
                }
            } catch (NumberFormatException ignored) {
                if (log.isDebugEnabled()) {
                    log.debug(ignored);
                }
            }
        }

        // Set the proxy port
        if (CarbonUtils.isRunningInStandaloneMode()) {
            transport = transportIn.getName();
            try {
                Method method = transportManagerClass.getMethod("getProxyPort", String.class);
                proxyPort = (Integer) method.invoke(transportManager, transport);
                if (proxyPort != -1) {
                    transportIn.addParameter(getParameter("proxyPort", String.valueOf(proxyPort)));
                }
            } catch (Exception e) {
                String msg = "Cannot get transport proxy port";
                log.error(msg, e);
                throw new AxisFault(msg, e);
            }
        } else {
            Parameter proxyPortParam = transportIn.getParameter("proxyPort");
            if (proxyPortParam != null) {
                proxyPort = Integer.parseInt(proxyPortParam.getValue().toString().trim());
            }
        }
    }

    public synchronized void start() throws AxisFault {
        // On first time startup, org.wso2.carbon.core.internal.StartupFinalizerServiceComponent
        // will start the transports when intialization is completed. However, when the transport is
        // stopped & started using the TransportManagement component, this code will start the
        // TransportListener

         if (CarbonUtils.isRunningInStandaloneMode() && isStopped) {
            try {
                Class<?> transportManagerClass = Class.forName(TRANSPORT_MANAGER);
                Object transportManager = transportManagerClass.newInstance();

                Class[] parametertype = new Class[] {String.class};
                Method method = transportManagerClass.getMethod("startTransport", parametertype);
                method.invoke(transportManager, new String[]{transport});
            } catch (Exception e) {
                String msg = "Cannot start " + transport + " transport";
                log.fatal(msg, e);
                return;
            }
        }
        isStopped = false;
    }

    public synchronized void stop() throws AxisFault {
       if (CarbonUtils.isRunningInStandaloneMode() && transportManagerClass != null) {
           try {
                Method method = transportManagerClass.getMethod("stopTransport", String.class);
                method.invoke(transportManager, transport);
            } catch (Exception e) {
                String msg = "Cannot get stop transport";
                log.error(msg, e);
                throw new AxisFault(msg, e);
            }
        }
        isStopped = true;
    }

    public EndpointReference getEPR(String protocol,
                                    String serviceName,
                                    String ip) throws AxisFault {
        if (configurationContext == null) {
            return null;
        }
        String serviceContextPath = configurationContext.getServiceContextPath();
        if (serviceContextPath == null) {
            throw new AxisFault("couldn't find service path");
        }
        return genEpr(protocol, ip, serviceContextPath, serviceName);
    }

    public SessionContext getSessionContext(MessageContext messageContext) {
        return SessionContextUtil.createSessionContext(messageContext);
    }

    public void destroy() {
        this.configurationContext = null;
    }

    /**
     * Returns the endpoint reference for a service.
     *
     * @param protocol The protocol used eg: http
     * @param ip The IP for the service
     * @param serviceContextPath The context path
     * @param serviceName  The name of the service
     * @return The end point reference
     * @throws AxisFault Thrown in case of a socket exception
     */
    protected EndpointReference genEpr(String protocol, String ip, String serviceContextPath,
            String serviceName) throws AxisFault {
        try {
            if (ip == null) {
                ip = NetworkUtils.getLocalHostname();
            }
            //Retrieving proxy context path of the worker node.
            String proxyContextPath = CarbonUtils.getProxyContextPath(true);

            String tmp = protocol + "://" + ip;

            String workerProxyPort = null;
            if (protocol == ServerConstants.HTTP_TRANSPORT) {
                workerProxyPort = ServerConfiguration.getInstance().getFirstProperty("Ports.WorkerHttpProxyPort");
            } else if (protocol == ServerConstants.HTTPS_TRANSPORT) {
                workerProxyPort = ServerConfiguration.getInstance().getFirstProperty("Ports.WorkerHttpsProxyPort");
            }

            if (workerProxyPort != null) {
                int workerProxyPortParsed = Integer.parseInt(workerProxyPort.trim());
                if (workerProxyPortParsed == DEFAULT_HTTP_PROXY_PORT ||
                        workerProxyPortParsed == DEFAULT_HTTPS_PROXY_PORT) {
                    tmp += proxyContextPath + serviceContextPath + "/" + serviceName;
                } else {
                    tmp += ":" + workerProxyPortParsed + proxyContextPath + serviceContextPath + "/" + serviceName;
                }
            } else if (proxyPort == DEFAULT_HTTP_PROXY_PORT || proxyPort == DEFAULT_HTTPS_PROXY_PORT) {
                tmp += proxyContextPath + serviceContextPath + "/" + serviceName;
            } else if (proxyPort != -1) {
                tmp += ":" + proxyPort + proxyContextPath + serviceContextPath + "/" + serviceName;
            } else {
                tmp += ":" + port + proxyContextPath + serviceContextPath + "/" + serviceName;
            }
            // This trailing / is needed for REST to work. The http Location is resolved against
            // this base URI and produce completely dirrefent results if this is not present.
            return new EndpointReference(tmp + "/");
        } catch (SocketException e) {
            throw AxisFault.makeFault(e);
        }
    }

    /**
     * Helper method to construct complete Axis2 text parameters. This method
     * should not be used to build OM parameters.
     *
     * @param name Name of the parameter
     * @param value Text value of the parameter
     * @return A complete Axis2 parameter instance
     */
    private Parameter getParameter(String name, String value) {
        Parameter p = new Parameter(name, value);
        p.setParameterType(Parameter.TEXT_PARAMETER);

        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMElement paramElement = fac.createOMElement(new QName(DeploymentConstants.TAG_PARAMETER));
        paramElement.addAttribute(DeploymentConstants.ATTRIBUTE_NAME, name, null);
        paramElement.setText(value);
        p.setParameterElement(paramElement);
        return p;
    }

    // -- jmx/management methods--
    public void pause() throws AxisFault {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void resume() throws AxisFault {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void maintenenceShutdown(long l) throws AxisFault {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getActiveThreadCount() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getQueueSize() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public long getMessagesReceived() {
        if (metrics != null) {
            return metrics.getMessagesReceived();
        }
        return -1;
    }

    public long getFaultsReceiving() {
        if (metrics != null) {
            return metrics.getFaultsReceiving();
        }
        return -1;
    }

    public long getBytesReceived() {
        if (metrics != null) {
            return metrics.getBytesReceived();
        }
        return -1;
    }

    public long getMessagesSent() {
        if (metrics != null) {
            return metrics.getMessagesSent();
        }
        return -1;
    }

    public long getFaultsSending() {
        if (metrics != null) {
            return metrics.getFaultsSending();
        }
        return -1;
    }

    public long getBytesSent() {
        if (metrics != null) {
            return metrics.getBytesSent();
        }
        return -1;
    }

    public long getTimeoutsReceiving() {
        if (metrics != null) {
            return metrics.getTimeoutsReceiving();
        }
        return -1;
    }

    public long getTimeoutsSending() {
        if (metrics != null) {
            return metrics.getTimeoutsSending();
        }
        return -1;
    }

    public long getMinSizeReceived() {
        if (metrics != null) {
            return metrics.getMinSizeReceived();
        }
        return -1;
    }

    public long getMaxSizeReceived() {
        if (metrics != null) {
            return metrics.getMaxSizeReceived();
        }
        return -1;
    }

    public double getAvgSizeReceived() {
        if (metrics != null) {
            return metrics.getAvgSizeReceived();
        }
        return -1;
    }

    public long getMinSizeSent() {
        if (metrics != null) {
            return metrics.getMinSizeSent();
        }
        return -1;
    }
      //
    public long getMaxSizeSent() {
        if (metrics != null) {
            return metrics.getMaxSizeSent();
        }
        return -1;
    }

    public double getAvgSizeSent() {
        if (metrics != null) {
            return metrics.getAvgSizeSent();
        }
        return -1;
    }

    public Map getResponseCodeTable() {
        if (metrics != null) {
            return metrics.getResponseCodeTable();
        }
        return null;
    }

    public void resetStatistics() {
        if (metrics != null) {
            metrics.reset();
        }
    }

    public long getLastResetTime() {
        if (metrics != null) {
            return metrics.getLastResetTime();
        }
        return -1;
    }

    public long getMetricsWindow() {
        if (metrics != null) {
            return System.currentTimeMillis() - metrics.getLastResetTime();
        }
        return -1;
    }
}
