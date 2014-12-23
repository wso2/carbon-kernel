/*
 * Copyright 2014 WSO2 Inc. (http://wso2.org)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.metrics.manager.internal;

import java.lang.management.ManagementFactory;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.metrics.manager.MetricService;
import org.wso2.carbon.metrics.manager.jmx.MetricManagerMXBean;
import org.wso2.carbon.metrics.manager.jmx.MetricManagerMXBeanImpl;

/**
 * @scr.component name="org.wso2.carbon.metrics.manager.internal.MetricManagerComponent" immediate="true"
 * @scr.reference name="metric.service" interface="org.wso2.carbon.metrics.manager.MetricService" cardinality="1..1"
 *                policy="dynamic" bind="setMetricService" unbind="unsetMetricService"
 */
public class MetricManagerComponent {

    private static final Log log = LogFactory.getLog(MetricManagerComponent.class);

    private static final String MBEAN_NAME = "org.wso2.carbon:type=MetricManager";

    private ServiceReferenceHolder serviceReferenceHolder = ServiceReferenceHolder.getInstance();

    protected void activate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Metrics manager component activated");
        }

        registerMXBean();
    }

    private void registerMXBean() {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            ObjectName name = new ObjectName(MBEAN_NAME);
            if (mBeanServer.isRegistered(name)) {
                mBeanServer.unregisterMBean(name);
            }
            MetricManagerMXBean mxBean = new MetricManagerMXBeanImpl(serviceReferenceHolder.getMetricService());
            mBeanServer.registerMBean(mxBean, name);
            if (log.isDebugEnabled()) {
                log.debug(String.format("MetricManagerMXBean registered under name: %s", name));
            }
        } catch (JMException e) {
            if (log.isErrorEnabled()) {
                log.error(String.format("MetricManagerMXBean registration failed. Name: %s", MBEAN_NAME), e);
            }
        }
    }

    protected void deactivate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Deactivating Metrics manager component");
        }

        unregisterMXBean();
    }

    private void unregisterMXBean() {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            ObjectName name = new ObjectName(MBEAN_NAME);
            if (mBeanServer.isRegistered(name)) {
                mBeanServer.unregisterMBean(name);
            }
            if (log.isDebugEnabled()) {
                log.debug(String.format("MetricManagerMXBean with name '%s' was unregistered.", name));
            }
        } catch (JMException e) {
            if (log.isErrorEnabled()) {
                log.error(String.format("MetricManagerMXBean with name '%s' was failed to unregister", MBEAN_NAME), e);
            }
        }
    }

    protected void setMetricService(MetricService metricService) {
        serviceReferenceHolder.setMetricService(metricService);
    }

    protected void unsetMetricService(MetricService metricService) {
        serviceReferenceHolder.setMetricService(null);
    }

}
