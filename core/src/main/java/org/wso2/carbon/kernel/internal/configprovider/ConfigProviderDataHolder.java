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
package org.wso2.carbon.kernel.internal.configprovider;

import org.wso2.carbon.kernel.securevault.SecureVault;


/**
 * Config Provider Data Holder.
 *
 * @since 5.2.0
 */
public class ConfigProviderDataHolder {
    private static ConfigProviderDataHolder instance = new ConfigProviderDataHolder();
    SecureVault secureVault;

    public static ConfigProviderDataHolder getInstance() {
        return instance;
    }

    private ConfigProviderDataHolder() {
    }

    public SecureVault getSecureVault() {
        return secureVault;
    }

    public void setSecureVault(SecureVault secureVault) {
        this.secureVault = secureVault;
    }
}
