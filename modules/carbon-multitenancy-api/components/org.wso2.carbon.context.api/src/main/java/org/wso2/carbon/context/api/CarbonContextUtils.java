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
package org.wso2.carbon.context.api;

import java.lang.management.ManagementPermission;
import java.util.Optional;

/**
 * Utility class for carbon context api bundle.
 *
 * @since 5.0.0
 */
public class CarbonContextUtils {

    //TODO move this constant to kernel
    public static final String TENANT_DOMAIN = "TENANT_DOMAIN";
    public static final String DEFAULT_TENANT = "default.tenant";

    public static void checkSecurity() {
        Optional<SecurityManager> securityManager = Optional.ofNullable(System.getSecurityManager());
        securityManager.ifPresent(secMan -> secMan.checkPermission(new ManagementPermission("control")));
    }

    public static Optional<String> getSystemTenantDomain() {
        return Optional
                .ofNullable(Optional
                        .ofNullable(System.getProperty(TENANT_DOMAIN))
                        .orElseGet(() -> System.getenv(TENANT_DOMAIN)));
    }
}
