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
package org.wso2.carbon.kernel.startupresolver;

import org.wso2.carbon.kernel.internal.startupresolver.StartupServiceCache;

/**
 * A Utility which provides a way to update the startup service cache.
 *
 * @since 5.2.0
 */
public class StartupServiceUtils {

    private StartupServiceUtils() {
        throw new AssertionError("Instantiating utility class...");
    }

    /**
     * All the components that are using startup order resolver functionality should call this method once those
     * components receive a reference to a dependent OSGi service. This method will update the internal service cache
     * of the StartupOrderResolver.
     *
     * Existing {@link StartupServiceCache} implementation uses the {@code componentName} to track the startup
     * components which has received the OSGi service of type {@code interfaceName}.
     *
     * @param componentName name of the reporting component
     * @param interfaceName name of the OSGi service interface
     */
    public static void updateServiceCache(String componentName, Class interfaceName)  {
        StartupServiceCache.getInstance().update(componentName, interfaceName);
    }
}
