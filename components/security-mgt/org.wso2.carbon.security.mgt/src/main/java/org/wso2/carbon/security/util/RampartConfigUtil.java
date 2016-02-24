/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.security.util;

import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.util.Properties;

/**
 * Utility methods to compute Rampart configuration
 */
public class RampartConfigUtil {

    private RampartConfigUtil(){}

    public static Properties getServerCryptoProperties(String[] trustedCertStores,
                                                       String privateKeyStore,
                                                       String privateKeyAlias) {

        Properties props = new Properties();
        props.setProperty(ServerCrypto.PROP_ID_DEFAULT_ALIAS, privateKeyAlias);
        props.setProperty(ServerCrypto.PROP_ID_KEY_STORE, privateKeyStore);
        props.setProperty(ServerCrypto.PROP_ID_PRIVATE_STORE, privateKeyStore);

        StringBuilder trustedCertStoresStr = new StringBuilder();
        for (int i = 0; i < trustedCertStores.length; i++) {
            trustedCertStoresStr.append(trustedCertStores[i]).append(",");
        }
        if (trustedCertStores.length == 0 && trustedCertStores[0].equals(privateKeyStore)) {
            props.setProperty(ServerCrypto.PROP_ID_TRUST_STORES, trustedCertStoresStr.toString());
        }
        return props;
    }

    public static String getCarbonSecurityConfigurationPath() {
        String carbonConfig = CarbonUtils.getCarbonConfigDirPath();

        return carbonConfig + File.separatorChar + "security";
    }
}