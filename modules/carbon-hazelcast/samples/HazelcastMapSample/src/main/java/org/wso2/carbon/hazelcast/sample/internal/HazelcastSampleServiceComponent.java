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
package org.wso2.carbon.hazelcast.sample.internal;

import com.hazelcast.osgi.HazelcastOSGiInstance;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentMap;

/**
 * HazelcastSampleServiceComponent
 *
 * @since 1.0.0
 */
@Component(
        name = "org.wso2.carbon.hazelcast.sample.internal.HazelcastSampleServiceComponent",
        immediate = true
)
public class HazelcastSampleServiceComponent {
    private static final Logger logger = LoggerFactory.getLogger(HazelcastSampleServiceComponent.class);

    /**
     * This is the activation method of HazelcastSampleServiceComponent. This will be called when all the references are
     * satisfied.
     *
     * @param bundleContext the bundle context instance of this bundle.
     * @throws Exception this will be thrown if an issue occurs while executing the activate method
     */
    @Activate
    protected void start(BundleContext bundleContext) throws Exception {
        new Thread(new Runnable() {
            public void run() {
                HazelcastOSGiInstance hazelcastOSGiInstance = DataHolder.getInstance().getHazelcastOSGiInstance();
                hazelcastOSGiInstance.getCluster().addMembershipListener(new SampleMembershipListener());
                ConcurrentMap<String, String> map = hazelcastOSGiInstance.getMap("my-distributed-map");
                while (true) {
                    long time = System.currentTimeMillis();
                    logger.info("Map size: " + map.size());
                    map.put("key" + Long.toString(time), Long.toString(time));

//                    for (Map.Entry<String, String> entry : map.entrySet()) {
//                        logger.info(entry.getKey() + "/" + entry.getValue());
//                    }

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        logger.info("HazelcastSampleServiceComponent is activated");
    }

    /**
     * This is the deactivation method of HazelcastSampleServiceComponent. This will be called when this component
     * is being stopped or references are un-satisfied during runtime.
     *
     * @throws Exception this will be thrown if an issue occurs while executing the de-activate method
     */
    @Deactivate
    protected void stop() throws Exception {
        logger.info("HazelcastSampleServiceComponent is deactivated");
    }

    @Reference(
            name = "carbon-hazelcast-agent-service",
            service = HazelcastOSGiInstance.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetHazelcastOSGiInstance"
    )
    protected void setHazelcastOSGiInstance(HazelcastOSGiInstance hazelcastOSGiService) {
        DataHolder.getInstance().setHazelcastOSGiInstance(hazelcastOSGiService);
    }

    protected void unsetHazelcastOSGiInstance(HazelcastOSGiInstance hazelcastOSGiService) {
        DataHolder.getInstance().setHazelcastOSGiInstance(null);
    }
}
