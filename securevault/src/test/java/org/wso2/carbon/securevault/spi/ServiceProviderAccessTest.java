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

package org.wso2.carbon.securevault.spi;


import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.securevault.SecureVaultInitializer;
import org.wso2.carbon.securevault.exception.SecureVaultException;
import org.wso2.carbon.securevault.internal.SecureVaultDataHolder;

/**
 * Unit tests class for org.wso2.carbon.securevault.SecureVaultInitializer
 *
 * @since 5.2.0
 */
public class ServiceProviderAccessTest {

    @Test
    public void testNonOSGIAccessToSecureVaultResolve() throws SecureVaultException {
        System.clearProperty("CARBON_HOME");
        System.clearProperty("carbon.home");

        String masterKeysFilePath = "src/test/resources/securevault/spi/master-keys.yaml";
        String secretPropertiesFilePath = "src/test/resources/securevault/spi/secrets.properties";
        String secureVaultYAMLPath = "src/test/resources/securevault/spi/secure-vault.yaml";

        SecureVaultInitializer.getInstance().initializeSecureVault(masterKeysFilePath,
                secretPropertiesFilePath, secureVaultYAMLPath);
        String alias = "wso2.sample.password2";
        Assert.assertEquals(String.valueOf(SecureVaultDataHolder.getInstance().getSecretRepository()
                .orElseThrow(() -> new SecureVaultException("No secret repository found."))
                .resolve(alias)), "ABC@123");
    }
}
