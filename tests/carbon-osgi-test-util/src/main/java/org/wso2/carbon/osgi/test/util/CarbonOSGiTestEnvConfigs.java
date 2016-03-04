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
package org.wso2.carbon.osgi.test.util;

/**
 * CarbonOSGiTestEnvConfigs.
 */
public class CarbonOSGiTestEnvConfigs {
    private String serverKey = "carbon-kernel";
    private String serverName = "WSO2 Carbon Kernel";
    private String serverVersion = "5.0.0";
    private String carbonHome;


    public String getServerKey() {
        return serverKey;
    }

    public void setServerKey(String serverKey) {
        this.serverKey = serverKey;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getServerVersion() {
        return serverVersion;
    }

    public void setServerVersion(String serverVersion) {
        this.serverVersion = serverVersion;
    }

    public String getCarbonHome() {
        return carbonHome;
    }

    public void setCarbonHome(String carbonHome) {
        this.carbonHome = carbonHome;
    }
}
