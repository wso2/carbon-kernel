/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.caching.invalidator.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.caching.impl.CacheInvalidator;
import org.wso2.carbon.caching.invalidator.amqp.CacheInvalidationPublisher;
import org.wso2.carbon.caching.invalidator.amqp.CacheInvalidationSubscriber;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="org.wso2.carbon.caching.invalidator.internal.CacheInvalidationServiceComponent" immediate="true"
 * @scr.reference name="configuration.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 */

public class CacheInvalidationServiceComponent {
    private static Log log = LogFactory.getLog(CacheInvalidationServiceComponent.class);
    ServiceRegistration serviceRegistration;
    CacheInvalidationSubscriber subscriber;
    CacheInvalidationPublisher publisher;

    protected void activate(ComponentContext ctxt) {
        log.info("Initializing the CacheInvalidationServiceComponent...");
        try {
            subscriber = new CacheInvalidationSubscriber();
            subscriber.init();
            publisher = new CacheInvalidationPublisher();
            serviceRegistration = ctxt.getBundleContext().registerService(CacheInvalidator.class, publisher, null);
        } catch (Exception e) {
            String msg = "Failed to initialize the CacheInvalidationServiceComponent.";
            log.error(msg, e);
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        log.info("Stopping the CacheInvalidationServiceComponent");
        try{
            serviceRegistration.unregister();
        }catch (Exception e){
            String msg = "Failed to Stop the CacheInvalidationServiceComponent.";
            log.error(msg, e);
        }
    }

    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        CacheInvalidationDataHolder.setConfigContext(contextService.getServerConfigContext());
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        CacheInvalidationDataHolder.setConfigContext(null);
    }
}
