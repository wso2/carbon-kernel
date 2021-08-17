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

package org.wso2.carbon.logging.correlation.bean;

import java.util.Arrays;

/**
 * Immutable configuration bean class for correlation log.
 * An object of this class is used to dispatch configuration changes to <code>CorrelationLogConfigurable</code>
 * service implementations.
 */
public class ImmutableCorrelationLogConfig {
    private boolean enable;
    private String[] deniedThreads;
    private boolean logAllMethods;

    public ImmutableCorrelationLogConfig(boolean enable, String[] deniedThreads, boolean logAllMethods) {
        this.enable = enable;
        this.deniedThreads = deniedThreads;
        this.logAllMethods = logAllMethods;
    }

    public boolean isEnable() {
        return enable;
    }

    public String[] getDeniedThreads() {
        return deniedThreads;
    }

    public boolean isLogAllMethods() {
        return logAllMethods;
    }
}
