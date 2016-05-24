/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.sample.runtime.custom;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.kernel.startupresolver.CapabilityProvider;

import java.util.stream.IntStream;

/**
 * Sample Custom Runtime Service Component class that registers CustomRuntime as a service multiple times to test
 * the startup order resolver implementation.
 *
 * @since 5.0.0
 */
@Component(
        name = "org.wso2.carbon.sample.runtime.custom.CustomRuntimeServiceComponent",
        immediate = true,
        property = "capabilityName=org.wso2.carbon.sample.runtime.mgt.Runtime"
)
public class CustomRuntimeServiceComponent implements CapabilityProvider {
    private int runtimeServiceCount = 3;

    @Activate
    public void activate(BundleContext bundleContext) {
        IntStream.range(0, runtimeServiceCount).forEach(
                count -> bundleContext.registerService(org.wso2.carbon.sample.runtime.mgt.Runtime.class,
                        new CustomRuntime(), null)
        );
    }

    @Override
    public int getCount() {
        return runtimeServiceCount;
    }
}
