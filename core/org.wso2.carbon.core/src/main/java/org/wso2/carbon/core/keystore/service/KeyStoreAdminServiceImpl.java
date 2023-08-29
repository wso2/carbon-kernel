/*
 * Copyright (c) 2010, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.core.keystore.service;

import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.core.keystore.KeyStoreAdmin;
import org.wso2.carbon.core.keystore.KeyStoreManagementException;

/**
 * Key Store Admin Service implementation class.
 */
public class KeyStoreAdminServiceImpl extends AbstractAdmin implements KeyStoreAdminInterface {

    @Override
    public KeyStoreData[] getKeyStores() throws KeyStoreManagementException {
        KeyStoreAdmin admin = new KeyStoreAdmin(CarbonContext.getThreadLocalCarbonContext().getTenantId());
        boolean isSuperTenant = CarbonContext.getThreadLocalCarbonContext().getTenantId() ==
                MultitenantConstants.SUPER_TENANT_ID;
        return admin.getKeyStores(isSuperTenant);
    }

    @Override
    public void addKeyStore(String fileData, String filename, String password, String provider,
                            String type, String pvtkeyPass) throws KeyStoreManagementException {
        KeyStoreAdmin admin = new KeyStoreAdmin(CarbonContext.getThreadLocalCarbonContext().getTenantId());
        admin.addKeyStore(fileData, filename, password, provider, type, pvtkeyPass);
    }

    @Override
    public void addTrustStore(String fileData, String filename, String password, String provider,
                              String type) throws KeyStoreManagementException {
        KeyStoreAdmin admin = new KeyStoreAdmin(CarbonContext.getThreadLocalCarbonContext().getTenantId());
        admin.addTrustStore(fileData, filename, password, provider, type);
    }

    @Override
    public void deleteStore(String keyStoreName) throws KeyStoreManagementException {

        KeyStoreAdmin admin = new KeyStoreAdmin(CarbonContext.getThreadLocalCarbonContext().getTenantId());
        admin.deleteStore(keyStoreName);
    }

    @Override
    public void importCertToStore(String fileName, String fileData, String keyStoreName)
            throws KeyStoreManagementException {
        KeyStoreAdmin admin = new KeyStoreAdmin(CarbonContext.getThreadLocalCarbonContext().getTenantId());
        admin.importCertToStore(fileName, fileData, keyStoreName);

    }

    @Override
    public String[] getStoreEntries(String keyStoreName) throws KeyStoreManagementException {
        KeyStoreAdmin admin = new KeyStoreAdmin(CarbonContext.getThreadLocalCarbonContext().getTenantId());
        return admin.getStoreEntries(keyStoreName);

    }

    @Override
    public KeyStoreData getKeystoreInfo(String keyStoreName) throws KeyStoreManagementException {
        KeyStoreAdmin admin = new KeyStoreAdmin(CarbonContext.getThreadLocalCarbonContext().getTenantId());
        return admin.getKeystoreInfo(keyStoreName);

    }

    @Override
    public void removeCertFromStore(String alias, String keyStoreName) throws KeyStoreManagementException {
        KeyStoreAdmin admin = new KeyStoreAdmin(CarbonContext.getThreadLocalCarbonContext().getTenantId());
        admin.removeCertFromStore(alias, keyStoreName);
    }

    public PaginatedKeyStoreData getPaginatedKeystoreInfo(String keyStoreName, int pageNumber) throws KeyStoreManagementException {
        KeyStoreAdmin admin = new KeyStoreAdmin(CarbonContext.getThreadLocalCarbonContext().getTenantId());
        return admin.getPaginatedKeystoreInfo(keyStoreName, pageNumber);

    }

    /**
     * Calls method to get the keystore info using keystore name and its certificates filtered by the given filter.
     *
     * @param keyStoreName Keystore name.
     * @param pageNumber   Page number.
     * @param filter       Filter for certificate alias.
     * @return Paginated keystore data with certificates.
     * @throws KeyStoreManagementException
     */
    public PaginatedKeyStoreData getFilteredPaginatedKeyStoreInfo(String keyStoreName, int pageNumber,
                                                                  String filter) throws KeyStoreManagementException {

        KeyStoreAdmin admin = new KeyStoreAdmin(CarbonContext.getThreadLocalCarbonContext().getTenantId());
        return admin.getFilteredPaginatedKeyStoreInfo(keyStoreName, pageNumber, filter);
    }
}
