/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.carbon.core.keystore.dao;

import org.wso2.carbon.core.keystore.KeyStoreManagementException;
import org.wso2.carbon.core.keystore.model.PubCertModel;

import java.util.Optional;

/**
 * Data Access Object for PubCert.
 */
public interface PubCertDAO {

    /**
     * Add a new Public Certificate.
     *
     * @param pubCertModel PubCert model where the PubCert data is maintained.
     * @throws KeyStoreManagementException If an error occurs while adding the PubCert.
     */
    String addPubCert(PubCertModel pubCertModel) throws KeyStoreManagementException;

    /**
     * Get a Public Certificate.
     *
     * @param uuid UUID of the PubCert.
     * @return PubCertModel.
     * @throws KeyStoreManagementException If an error occurs while retrieving the PubCert.
     */
    Optional<PubCertModel> getPubCert(String uuid) throws KeyStoreManagementException;
}
