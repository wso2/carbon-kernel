/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.logging.correlation.utils;

/**
 * Correlation log constants class.
 */
public class CorrelationLogConstants {

    // Configuration field names
    public static final String ENABLE = "ENABLE";
    public static final String COMPONENTS = "COMPONENTS";
    public static final String BLACKLISTED_THREADS = "BLACKLISTED_THREADS";
    public static final String LOG_ALL_METHODS = "LOG_ALL_METHODS";

    // Config paths in Carbon.xml file
    public static final String CONFIG_ENABLE = "CorrelationLogs.enable";
    public static final String CONFIG_COMPONENTS = "CorrelationLogs.components";
    public static final String CONFIG_BLACKLISTED_THREADS = "CorrelationLogs.blacklistedThreads";
    public static final String CONFIG_LOG_ALL_METHODS = "CorrelationLogs.logAllMethods";

}
