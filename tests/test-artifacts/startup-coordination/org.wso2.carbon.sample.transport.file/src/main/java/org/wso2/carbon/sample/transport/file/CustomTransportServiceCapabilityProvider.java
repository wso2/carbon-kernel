/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.sample.transport.file;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.kernel.startupresolver.CapabilityProvider;
import org.wso2.carbon.sample.transport.mgt.Transport;

/**
 * Sample TransportServiceCapabilityProvider class that registers CustomTransport as a service multiple times to test
 * the startup order resolver implementation.
 *
 * @since 5.0.0
 */
@Component(
        name = "org.wso2.carbon.sample.transport.http.CustomTransportServiceCapabilityProvider",
        immediate = true,
        property = "capabilityName=org.wso2.carbon.sample.transport.mgt.Transport"
)
public class CustomTransportServiceCapabilityProvider implements CapabilityProvider {
    private static final int customTransportServiceCount = 3;

    @Activate
    protected void start(BundleContext bundleContext) {
        Thread thread = new Thread(() -> {
            for (int i = 0; i < customTransportServiceCount; i++) {
                bundleContext.registerService(Transport.class, new FileTransport(), null);

                try {
                    Thread.sleep(1000 * 2);
                } catch (InterruptedException ignore) {
                }
            }
        });
        thread.start();
    }

    @Override
    public int getCount() {
        return customTransportServiceCount;
    }
}
