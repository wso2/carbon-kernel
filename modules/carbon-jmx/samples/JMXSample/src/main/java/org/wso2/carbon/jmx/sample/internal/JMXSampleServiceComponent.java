/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.jmx.sample.internal;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.jmx.sample.Test;

import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * JMXSampleServiceComponent
 *
 * @since 1.0.0
 */
@Component(
        name = "org.wso2.carbon.jmx.sample.internal.JMXSampleServiceComponent",
        immediate = true
)
public class JMXSampleServiceComponent {
    private static final Logger logger = LoggerFactory.getLogger(JMXSampleServiceComponent.class);

    /**
     * This is the activation method of JMXSampleServiceComponent. This will be called when all the references are
     * satisfied.
     *
     * @param bundleContext the bundle context instance of this bundle.
     * @throws Exception this will be thrown if an issue occurs while executing the activate method
     */
    @Activate
    protected void start(BundleContext bundleContext) throws Exception {
        Test test = new Test();

        ObjectName mbeanName = new ObjectName("org.wso2.carbon.jmx.sample:type=Test");
        DataHolder.getInstance().getmBeanServer().registerMBean(test, mbeanName);

        logger.info("JMXSampleServiceComponent is activated");
    }

    /**
     * This is the deactivation method of JMXSampleServiceComponent. This will be called when this component
     * is being stopped or references are un-satisfied during runtime.
     *
     * @throws Exception this will be thrown if an issue occurs while executing the de-activate method
     */
    @Deactivate
    protected void stop() throws Exception {
        logger.info("JMXSampleServiceComponent is deactivated");
    }

    @Reference(
            name = "mbean-server-service",
            service = MBeanServer.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetMBeanServer"
    )
    protected void setMBeanServer(MBeanServer mBeanServer) {
        DataHolder.getInstance().setmBeanServer(mBeanServer);
    }

    protected void unsetMBeanServer(MBeanServer mBeanServer) {
        DataHolder.getInstance().setmBeanServer(null);
    }
}
