/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.security.keystore;

import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.core.util.KeyStoreUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.testutil.powermock.PowerMockIdentityBaseTest;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.security.keystore.service.CertData;
import org.wso2.carbon.security.keystore.service.PaginatedKeyStoreData;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.Assert.assertEquals;

@PrepareForTest({CarbonUtils.class, IdentityTenantUtil.class, IdentityUtil.class, KeyStoreManager.class,
        ServerConfiguration.class, KeyStoreUtil.class})
public class KeyStoreAdminTest extends PowerMockIdentityBaseTest {

    public static final String SERVER_TRUSTSTORE_FILE = "Security.TrustStore.Location";
    public static final String SERVER_TRUSTSTORE_PASSWORD = "Security.TrustStore.Password";
    @Mock
    ServerConfiguration serverConfiguration;
    @Mock
    KeyStoreManager keyStoreManager;
    @Mock
    Registry registry;
    private KeyStoreAdmin keyStoreAdmin;
    private int tenantID = -1234;

    @BeforeClass
    public void setup() {

        System.setProperty(
                CarbonBaseConstants.CARBON_HOME,
                Paths.get(System.getProperty("user.dir"), "src", "test", "resources").toString()
        );
    }

    @Test
    public void testGetPaginatedKeystoreInfo() throws Exception {

        mockStatic(ServerConfiguration.class);
        when(ServerConfiguration.getInstance()).thenReturn(serverConfiguration);

        mockStatic(KeyStoreManager.class);
        when(KeyStoreManager.getInstance(anyInt())).thenReturn(keyStoreManager);
        when(keyStoreManager.getKeyStore("wso2carbon.jks")).thenReturn(getKeyStoreFromFile("wso2carbon.jks",
                "wso2carbon"));
        when(serverConfiguration.getFirstProperty(SERVER_TRUSTSTORE_FILE)).thenReturn(createPath("wso2carbon.jks").toString());
        when(serverConfiguration.getFirstProperty(SERVER_TRUSTSTORE_PASSWORD)).thenReturn("wso2carbon");

        mockStatic(KeyStoreUtil.class);
        when(KeyStoreUtil.isPrimaryStore(any())).thenReturn(true);

        mockStatic(KeyStoreManager.class);
        when(KeyStoreManager.getInstance(tenantID)).thenReturn(keyStoreManager);
        when(keyStoreManager.getPrimaryKeyStore()).thenReturn(getKeyStoreFromFile("wso2carbon.jks", "wso2carbon"));

        keyStoreAdmin = new KeyStoreAdmin(tenantID, registry);
        PaginatedKeyStoreData result = keyStoreAdmin.getPaginatedKeystoreInfo("wso2carbon.jks", 10);
        int actualKeysNo = findCertDataSetSize(result.getPaginatedKeyData().getCertDataSet());
        assertEquals(actualKeysNo, 3, "Incorrect key numbers");

    }

    private KeyStore getKeyStoreFromFile(String keystoreName, String password) throws Exception {

        Path tenantKeystorePath = createPath(keystoreName);
        FileInputStream file = new FileInputStream(tenantKeystorePath.toString());
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(file, password.toCharArray());
        return keystore;
    }

    private Path createPath(String keystoreName) {

        Path keystorePath = Paths.get(System.getProperty(CarbonBaseConstants.CARBON_HOME), "repository",
                "resources", "security", keystoreName);
        return keystorePath;
    }

    private int findCertDataSetSize(CertData[] certDataSet) {

        int ans = 0;
        for (CertData cert : certDataSet) {
            if (cert != null) {
                ans += 1;
            }
        }
        return ans;
    }

}
