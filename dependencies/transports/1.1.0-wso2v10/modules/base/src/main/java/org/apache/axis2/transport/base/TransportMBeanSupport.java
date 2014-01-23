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

package org.apache.axis2.transport.base;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.transport.TransportSender;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Support class to register MBeans for transport listeners and senders.
 * This class can be used by {@link TransportListener} and {@link TransportSender} classes
 * to register the {@link TransportView} management bean. It takes care of registering
 * the bean under a consistent name and makes sure that a JMX related error doesn't stop
 * the transport from working: a failure to register the MBean will cause JMX support
 * to be disabled.
 */
public class TransportMBeanSupport {
    private static final Log log = LogFactory.getLog(TransportMBeanSupport.class);
    
    private boolean enabled = true;
    private boolean registered;
    private MBeanServer mbs;
    private ObjectName mbeanName;
    private TransportView mbeanInstance;
    
    private TransportMBeanSupport(String connectorName, TransportView mbeanInstance) {
        try {
            mbs = ManagementFactory.getPlatformMBeanServer();
        } catch (SecurityException ex) {
            log.warn("Unable to get the platform MBean server; JMX support disabled", ex);
            enabled = false;
            return;
        }
        String jmxAgentName = System.getProperty("jmx.agent.name");
        if (jmxAgentName == null || "".equals(jmxAgentName)) {
            jmxAgentName = "org.apache.axis2";
        }
        String mbeanNameString = jmxAgentName + ":Type=Transport,ConnectorName=" + connectorName;
        try {
            mbeanName = ObjectName.getInstance(mbeanNameString);
        } catch (MalformedObjectNameException ex) {
            log.warn("Unable to create object name '" + mbeanNameString
                        + "'; JMX support disabled", ex);
            enabled = false;
        }
        this.mbeanInstance = mbeanInstance;
    }
    
    public TransportMBeanSupport(TransportListener listener, String name) {
        this(name + "-listener-" + listener.hashCode(), new TransportView(listener, null));
    }
    
    public TransportMBeanSupport(TransportSender sender, String name) {
        this(name + "-sender-" + sender.hashCode(), new TransportView(null, sender));
    }
    
    public ObjectName getMBeanName() {
        return mbeanName;
    }
    
    /**
     * Register the {@link TransportView} MBean.
     */
    public void register() {
        if (enabled && !registered) {
            try {
                mbs.registerMBean(mbeanInstance, mbeanName);
                registered = true;
            } catch (Exception e) {
                log.warn("Error registering a MBean with objectname ' " + mbeanName +
                    " ' for JMX management", e);
                enabled = false;
            }
        }
    }
    
    /**
     * Unregister the {@link TransportView} MBean.
     */
    public void unregister() {
        if (enabled && registered) {
            try {
                mbs.unregisterMBean(mbeanName);
                registered = false;
            } catch (Exception e) {
                log.warn("Error un-registering a MBean with objectname ' " + mbeanName +
                    " ' for JMX management", e);
            }
        }
    }
}
