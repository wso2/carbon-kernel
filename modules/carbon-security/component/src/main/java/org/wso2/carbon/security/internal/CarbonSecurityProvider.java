/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.security.internal;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.permissionadmin.PermissionAdmin;
import org.osgi.service.permissionadmin.PermissionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.security.internal.config.DefaultPermissionInfo;
import org.wso2.carbon.security.internal.config.DefaultPermissionInfoCollection;
import org.wso2.carbon.security.internal.config.YAMLSecurityConfigBuilder;
import org.wso2.carbon.security.jaas.CarbonPolicy;

import java.security.Policy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * OSGi service component which handle authentication and authorization
 */
@Component(
        name = "org.wso2.carbon.security.internal.CarbonSecurityProvider",
        immediate = true
)
public class CarbonSecurityProvider {

    private static final Logger log = LoggerFactory.getLogger(CarbonSecurityProvider.class);

    @Activate
    public void registerCarbonSecurityProvider(BundleContext bundleContext) {

        // Set default permissions for all bundles
        setDefaultPermissions(bundleContext);

        //Registering CarbonPolicy
        CarbonPolicy policy = new CarbonPolicy();
        Policy.setPolicy(policy);
        System.setSecurityManager(new SecurityManager());

        log.info("Carbon-Security bundle activated successfully.");
    }

    @Deactivate
    public void unregisterCarbonSecurityProvider(BundleContext bundleContext) {
        log.info("Carbon-Security bundle deactivated successfully.");
    }

    /**
     * Set default permissions for all bundles using PermissionAdmin
     *
     * @param context
     */
    private void setDefaultPermissions(BundleContext context) {

        PermissionAdmin permissionAdmin = getPermissionAdmin(context);
        if (permissionAdmin != null) {

            DefaultPermissionInfoCollection permissionInfoCollection = YAMLSecurityConfigBuilder
                    .buildDefaultPermissionInfoCollection();
            List<PermissionInfo> permissionInfoList = new ArrayList<>();
            if (!Collections.EMPTY_SET.equals(permissionInfoCollection.getPermissions())) {

                for (DefaultPermissionInfo permissionInfo : permissionInfoCollection.getPermissions()) {

                    if (permissionInfo.getType() == null || permissionInfo.getType().trim().isEmpty()) {
                        throw new IllegalArgumentException("type can't be null or empty");

                    }

                    if (permissionInfo.getName() == null || permissionInfo.getName().trim().isEmpty()) {
                        throw new IllegalArgumentException("name can't be null or empty");
                    }

                    permissionInfoList.add(new PermissionInfo(permissionInfo.getType(), permissionInfo.getName(),
                                                              (permissionInfo.getActions() != null && !permissionInfo
                                                                      .getActions().trim().isEmpty()) ?
                                                              permissionInfo.getActions().trim() : null));
                }
            } else {
                throw new RuntimeException("Default permission info collection can't be empty");
            }

            permissionAdmin.setDefaultPermissions(permissionInfoList
                                                          .toArray(new PermissionInfo[permissionInfoList.size()]));
        }
    }

    private PermissionAdmin getPermissionAdmin(BundleContext context) {
        return (PermissionAdmin) context.getService(context.getServiceReference(PermissionAdmin.class.getName()));
    }

}

