/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.registry.server.internal;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * The SCR Component encapsulating the server-side functionality of the registry.
 *
 * @scr.component name="org.wso2.carbon.registry.server" immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService" cardinality="1..1"
 * policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="0..1" policy="dynamic"  bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 */
@SuppressWarnings({"JavaDoc", "unused"})
public class RegistryServerServiceComponent {

    private static Log log = LogFactory.getLog(RegistryServerServiceComponent.class);

    /**
     * Activates the Registry Kernel bundle.
     *
     * @param context the OSGi component context.
     */
    protected void activate(ComponentContext context) {
        log.debug("Registry Service bundle is activated ");
    }

    /**
     * Deactivates the Registry Kernel bundle.
     *
     * @param context the OSGi component context.
     */
    protected void deactivate(ComponentContext context) {
        log.debug("Registry Service bundle is deactivated ");
    }

    /**
     * Method to set the registry service used. This will be used when accessing the registry. This
     * method is called when the OSGi registry service is available.
     *
     * @param registryService the registry service.
     */
    protected void setRegistryService(RegistryService registryService) {
        Utils.setRegistryService(registryService);
    }

    /**
     * This method is called when the current registry service becomes un-available.
     *
     * @param registryService the current registry service instance, to be used for any cleaning-up.
     */
    protected void unsetRegistryService(RegistryService registryService) {
        Utils.setRegistryService(null);
    }

    /**
     * Method to set the configuration context service used. This method is called when the OSGi
     * ConfigurationContext Service is available.
     *
     * @param contextService the configuration context service.
     */
    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        try {
            if (contextService.getServerConfigContext() != null &&
                    contextService.getServerConfigContext().getAxisConfiguration() != null) {
                contextService.getServerConfigContext().getAxisConfiguration().engageModule(
                        "pagination");
            } else {
                log.error("Failed to pagination Activation Module.");
            }
        } catch (AxisFault e) {
            log.error("Failed to pagination Module", e);
        }
    }

    /**
     * This method is called when the current configuration context service becomes un-available.
     *
     * @param contextService the current configuration context service instance, to be used for any
     *                       cleaning-up.
     */
    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
    }
}
