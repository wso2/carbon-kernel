/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.security.keystore;

import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

/**
 * This service contains the methods to manage certificates of the keystore and client truststore.
 */
public interface KeyStoreManagementService {

    /**
     * Retrieves the list of certificate aliases from the keystore.
     *
     * @param tenantDomain tenant domain of the keystore.
     * @param filter used to filter the result. Supports sw, ew, eq & co. eg:filter=alias+sw+wso2.
     * @return the {@link List} of alias.
     * @throws KeyStoreManagementException when retrieving the certificate aliases failed.
     */
    List<String> getKeyStoreCertificateAliases(String tenantDomain, String filter) throws KeyStoreManagementException;

    /**
     * Retrieves the public certificate from the keystore.
     *
     * @param tenantDomain tenant domain of the keystore.
     * @return a {@link Map} with public key alias and {@link X509Certificate}.
     * @throws KeyStoreManagementException when retrieving the public certificate.
     */
    Map<String, X509Certificate> getPublicCertificate(String tenantDomain) throws KeyStoreManagementException;

    /**
     * Retrieves the certificate of the given alias from the keystore.
     *
     * @param tenantDomain tenant domain of the keystore.
     * @param alias of the certificate.
     * @return the {@link X509Certificate}
     * @throws KeyStoreManagementException when retrieving the certificate failed.
     */
    X509Certificate getKeyStoreCertificate(String tenantDomain, String alias) throws KeyStoreManagementException;

    /**
     * Retrieves the list of certificate aliases from the client truststore.
     *
     * @param tenantDomain tenant domain of the keystore.
     * @param filter used to filter the result. Supports sw, ew, eq & co. eg:filter=alias+sw+wso2.
     * @return the {@link List} of alias
     * @throws KeyStoreManagementException when retrieving the certificate aliases failed.
     */
    List<String> getClientCertificateAliases(String tenantDomain, String filter) throws KeyStoreManagementException;

    /**
     * Retrieves the certificate of the given alias from the client truststore.
     *
     * @param tenantDomain tenant domain of the keystore.
     * @param alias of the certificate.
     * @return the {@link X509Certificate}
     * @throws KeyStoreManagementException when retrieving the certificate failed.
     */
    X509Certificate getClientCertificate(String tenantDomain, String alias) throws KeyStoreManagementException;

    /**
     * Imports the certificate to the keystore.
     *
     * @param tenantDomain tenant domain of the keystore.
     * @param alias of the certificate.
     * @param certificate the certificate to be imported.
     * @throws KeyStoreManagementException when importing the certificate failed.
     */
    void addCertificate(String tenantDomain, String alias, String certificate) throws KeyStoreManagementException;

    /**
     * Deletes the certificate from the keystore.
     *
     * @param tenantDomain tenant domain of the keystore.
     * @param alias of the certificate.
     * @throws KeyStoreManagementException when importing the certificate failed.
     */
    void deleteCertificate(String tenantDomain, String alias) throws KeyStoreManagementException;
}
