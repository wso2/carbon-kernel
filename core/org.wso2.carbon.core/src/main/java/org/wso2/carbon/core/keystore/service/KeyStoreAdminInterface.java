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

import org.wso2.carbon.core.keystore.KeyStoreManagementException;

public interface KeyStoreAdminInterface {

    KeyStoreData[] getKeyStores() throws KeyStoreManagementException;

    void addKeyStore(String file, String filename,
                     String password, String provider, String type, String pvtkeyPass) throws KeyStoreManagementException;

    void addTrustStore(String file, String filename,
                       String password, String provider, String type) throws KeyStoreManagementException;

    void deleteStore(String keyStoreName) throws KeyStoreManagementException;

    void importCertToStore(String fileName, String fileData, String keyStoreName) throws KeyStoreManagementException;

    String[] getStoreEntries(String keyStoreName) throws KeyStoreManagementException;

    KeyStoreData getKeystoreInfo(String keyStoreName) throws KeyStoreManagementException;

    void removeCertFromStore(String alias, String keyStoreName) throws KeyStoreManagementException;

}
