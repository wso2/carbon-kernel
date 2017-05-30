/*
 * Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.feature.mgt.services.internal;

import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class FeatureManagerBundleActivator implements BundleActivator {
    public void start(BundleContext bundleContext) throws Exception {
        //Registering FeatureManagerCommandProvider as an OSGi service.
        bundleContext.registerService(CommandProvider.class.getName(), new FeatureManagerCommandProvider(), null);
    }

    public void stop(BundleContext bundleContext) throws Exception {
    }
}
