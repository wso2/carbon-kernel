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
package org.wso2.carbon.hazelcast.internal;

import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.osgi.HazelcastOSGiService;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;

/**
 * This service component is responsible for creating a CarbonHazelcastAgent once the HazelcastOSGiService
 * got available and registering the CarbonHazelcastAgent service.
 *
 * @since 1.0.0
 */
@Component(
        name = "org.wso2.carbon.hazelcast.internal.CarbonHazelcastComponent",
        immediate = true
)
public class CarbonHazelcastComponent {
    private static final Logger logger = LoggerFactory.getLogger(CarbonHazelcastComponent.class);
    private HazelcastInstance hazelcastInstance;

    /**
     * This is the activation method of CarbonHazelcastComponent. This will be called when all the references are
     * satisfied.
     *
     * @param bundleContext the bundle context instance of this bundle.
     */
    @Activate
    protected void start(BundleContext bundleContext) {
        try {
            String hazelcastFilePath = Paths.get(System.getProperty("carbon.home"),
                    "conf", "hazelcast", "hazelcast.xml").toString();

            Config config = new XmlConfigBuilder(hazelcastFilePath).build();

            hazelcastInstance = DataHolder.getInstance().getHazelcastOSGiService()
                    .newHazelcastInstance(config);
            bundleContext.registerService(HazelcastInstance.class, hazelcastInstance, null);

            logger.info("CarbonHazelcastComponent is activated");
        } catch (Throwable throwable) {
            logger.error("Failed to start CarbonHazelcastComponent. ", throwable);
        }
    }

    /**
     * This is the deactivation method of CarbonHazelcastComponent. This will be called when this component
     * is being stopped or references are un-satisfied during runtime.
     *
     * @throws Exception this will be thrown if an issue occurs while executing the de-activate method
     */
    @Deactivate
    protected void stop() throws Exception {
        try {
            hazelcastInstance.shutdown();
        } catch (Throwable throwable) {
            logger.error("Failed to stop CarbonHazelcastComponent. ", throwable);
        }
    }

    @Reference(
            name = "hazelcast-osgi-service",
            service = HazelcastOSGiService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetHazelcastOSGiService"
    )
    protected void setHazelcastOSGiService(HazelcastOSGiService hazelcastOSGiService) {
        DataHolder.getInstance().setHazelcastOSGiService(hazelcastOSGiService);
    }

    protected void unsetHazelcastOSGiService(HazelcastOSGiService hazelcastOSGiService) {
        DataHolder.getInstance().setHazelcastOSGiService(null);
    }
}
