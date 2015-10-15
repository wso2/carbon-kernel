/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.carbon.startupcoordinator;


import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO
 */
@Component(
        name = "org.wso2.carbon.startupcoordinator.MyRequireCapabilityListener",
        immediate = true,
        service = RequireCapabilityListener.class,
        property = "required-service-interface=org.wso2.carbon.startupcoordinator.DemoListener"

)
public class MyRequireCapabilityListener implements RequireCapabilityListener {
    private static final Logger logger = LoggerFactory.getLogger(MyRequireCapabilityListener.class);


    @Override
    public void onAllRequiredCapabilitiesAvailable() {
        logger.info("HURRAYYYYY {} is satisfied with all required " +
                "services org.wso2.carbon.startupcoordinator.DemoListener", this.getClass().getName());
    }

    @Activate
    public void registerCommandProvider(BundleContext bundleContext) {
    }

    @Deactivate
    public void unregisterCommandProvider(BundleContext bundleContext) {
    }
}
