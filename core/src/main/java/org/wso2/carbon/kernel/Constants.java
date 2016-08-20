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
package org.wso2.carbon.kernel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Carbon Constants.
 *
 * @since 5.0.0
 */
public final class Constants {

    public static final String CARBON_HOME = "carbon.home";
    public static final String CARBON_HOME_ENV = "CARBON_HOME";
    public static final String CARBON_CONFIG_YAML = "carbon.yml";

    public static final String START_TIME = "carbon.start.time";

    public static final String LOGIN_MODULE_ENTRY = "CarbonSecurityConfig";

    public static final String DEFAULT_TENANT = "default";

    public static final String TENANT_NAME = "tenant.name";

    public static final String SERVER_PACKAGE = "org.wso2.carbon";

    /**
     * The logger that needs to be used for auditing purposes.
     *
     */
    public static final Logger AUDIT_LOG = LoggerFactory.getLogger("AUDIT_LOG");

    /**
     * Remove default constructor and make it not available to initialize.
     */
    private Constants() {
        throw new AssertionError("Trying to a instantiate a constant class");
    }

    /**
     * Default value if it is not set in sys prop/env.
     */
    public static class PlaceHolders {
        public static final String SERVER_KEY = "carbon-kernel";
        public static final String SERVER_NAME = "WSO2 Carbon Kernel";
        public static final String SERVER_VERSION = "5";
    }
}
