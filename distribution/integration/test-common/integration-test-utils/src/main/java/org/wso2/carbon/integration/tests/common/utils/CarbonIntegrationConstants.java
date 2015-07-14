/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.integration.tests.common.utils;

public class CarbonIntegrationConstants {

    public static final String PRODUCT_GROUP = "CARBON";
    public static final String INSTANCE = "carbon002";
    public static final String CONTEXT_XPATH_DATA_SOURCE = "//datasources/datasource[@name='%s']";
    public static final String SERVER_STARTUP_MESSAGE = "Mgt Console URL";
    public static final long DEFAULT_WAIT_MS = 1000 * 60 * 5;
}
