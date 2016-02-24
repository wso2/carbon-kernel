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

package org.wso2.carbon.security.keystore.service;

import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.security.SecurityConfigException;
import org.wso2.carbon.security.keystore.KeyStoreAdmin;

public class KeyStoreAdminServiceImpl extends AbstractAdmin implements KeyStoreAdminInterface {

    @Override
    public KeyStoreData[] getKeyStores() throws SecurityConfigException {
        KeyStoreAdmin admin = new KeyStoreAdmin(CarbonContext.getThreadLocalCarbonContext().getTenantId(),
                getGovernanceSystemRegistry());
        boolean isSuperTenant = CarbonContext.getThreadLocalCarbonContext().getTenantId() ==
                MultitenantConstants.SUPER_TENANT_ID;
        return admin.getKeyStores(isSuperTenant);
    }

    @Override
    public void addKeyStore(String fileData, String filename, String password, String provider,
                            String type, String pvtkeyPass) throws SecurityConfigException {
        KeyStoreAdmin admin = new KeyStoreAdmin(CarbonContext.getThreadLocalCarbonContext().getTenantId(),
                getGovernanceSystemRegistry());
        admin.addKeyStore(fileData, filename, password, provider, type, pvtkeyPass);
    }

    @Override
    public void addTrustStore(String fileData, String filename, String password, String provider,
                              String type) throws SecurityConfigException {
        KeyStoreAdmin admin = new KeyStoreAdmin(CarbonContext.getThreadLocalCarbonContext().getTenantId(),
                getGovernanceSystemRegistry());
        admin.addTrustStore(fileData, filename, password, provider, type);
    }

    @Override
    public void deleteStore(String keyStoreName) throws SecurityConfigException {
        KeyStoreAdmin admin = new KeyStoreAdmin(CarbonContext.getThreadLocalCarbonContext().getTenantId(),
                getGovernanceSystemRegistry());
        admin.deleteStore(keyStoreName);

    }

    @Override
    public void importCertToStore(String fileName, String fileData, String keyStoreName)
            throws SecurityConfigException {
        KeyStoreAdmin admin = new KeyStoreAdmin(CarbonContext.getThreadLocalCarbonContext().getTenantId(),
                getGovernanceSystemRegistry());
        admin.importCertToStore(fileName, fileData, keyStoreName);

    }

    @Override
    public String[] getStoreEntries(String keyStoreName) throws SecurityConfigException {
        KeyStoreAdmin admin = new KeyStoreAdmin(CarbonContext.getThreadLocalCarbonContext().getTenantId(),
                getGovernanceSystemRegistry());
        return admin.getStoreEntries(keyStoreName);

    }

    @Override
    public KeyStoreData getKeystoreInfo(String keyStoreName) throws SecurityConfigException {
        KeyStoreAdmin admin = new KeyStoreAdmin(CarbonContext.getThreadLocalCarbonContext().getTenantId(),
                getGovernanceSystemRegistry());
        return admin.getKeystoreInfo(keyStoreName);

    }

    @Override
    public void removeCertFromStore(String alias, String keyStoreName) throws SecurityConfigException {
        KeyStoreAdmin admin = new KeyStoreAdmin(CarbonContext.getThreadLocalCarbonContext().getTenantId(),
                getGovernanceSystemRegistry());
        admin.removeCertFromStore(alias, keyStoreName);
    }

    public PaginatedKeyStoreData getPaginatedKeystoreInfo(String keyStoreName, int pageNumber) throws SecurityConfigException {
        KeyStoreAdmin admin = new KeyStoreAdmin(CarbonContext.getThreadLocalCarbonContext().getTenantId(),
                getGovernanceSystemRegistry());
        return admin.getPaginatedKeystoreInfo(keyStoreName, pageNumber);

    }

}
