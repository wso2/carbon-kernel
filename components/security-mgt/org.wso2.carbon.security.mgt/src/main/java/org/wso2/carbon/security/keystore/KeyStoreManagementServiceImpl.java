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

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.core.util.KeyStoreUtil;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.security.SecurityConfigException;
import org.wso2.carbon.security.SecurityConstants;
import org.wso2.carbon.security.keystore.service.CertData;
import org.wso2.carbon.security.keystore.service.CertDataDetail;
import org.wso2.carbon.security.keystore.service.KeyStoreData;

import java.io.File;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.wso2.carbon.security.SecurityConstants.KeyStoreMgtConstants.ErrorMessage.ERROR_CODE_ADD_CERTIFICATE;
import static org.wso2.carbon.security.SecurityConstants.KeyStoreMgtConstants.ErrorMessage.ERROR_CODE_BAD_VALUE_FOR_FILTER;
import static org.wso2.carbon.security.SecurityConstants.KeyStoreMgtConstants.ErrorMessage.ERROR_CODE_CERTIFICATE_EXISTS;
import static org.wso2.carbon.security.SecurityConstants.KeyStoreMgtConstants.ErrorMessage.ERROR_CODE_DELETE_CERTIFICATE;
import static org.wso2.carbon.security.SecurityConstants.KeyStoreMgtConstants.ErrorMessage.ERROR_CODE_EMPTY_ALIAS;
import static org.wso2.carbon.security.SecurityConstants.KeyStoreMgtConstants.ErrorMessage.ERROR_CODE_RETRIEVE_CLIENT_TRUSTSTORE;
import static org.wso2.carbon.security.SecurityConstants.KeyStoreMgtConstants.ErrorMessage.ERROR_CODE_RETRIEVE_CLIENT_TRUSTSTORE_CERTIFICATE;
import static org.wso2.carbon.security.SecurityConstants.KeyStoreMgtConstants.ErrorMessage.ERROR_CODE_RETRIEVE_KEYSTORE;
import static org.wso2.carbon.security.SecurityConstants.KeyStoreMgtConstants.ErrorMessage.ERROR_CODE_RETRIEVE_KEYSTORE_INFORMATION;
import static org.wso2.carbon.security.SecurityConstants.KeyStoreMgtConstants.ErrorMessage.ERROR_CODE_UNSUPPORTED_FILTER_OPERATION;
import static org.wso2.carbon.security.SecurityConstants.KeyStoreMgtConstants.FILTER_FIELD_ALIAS;
import static org.wso2.carbon.security.SecurityConstants.KeyStoreMgtConstants.FILTER_OPERATION_CONTAINS;
import static org.wso2.carbon.security.SecurityConstants.KeyStoreMgtConstants.FILTER_OPERATION_ENDS_WITH;
import static org.wso2.carbon.security.SecurityConstants.KeyStoreMgtConstants.FILTER_OPERATION_EQUALS;
import static org.wso2.carbon.security.SecurityConstants.KeyStoreMgtConstants.FILTER_OPERATION_STARTS_WITH;
import static org.wso2.carbon.security.SecurityConstants.KeyStoreMgtConstants.SERVER_TRUSTSTORE_FILE;

/**
 * This class is used to manage the keystore certificates.
 */
public class KeyStoreManagementServiceImpl implements KeyStoreManagementService {

    private KeyStoreAdmin keyStoreAdmin = new KeyStoreAdmin(CarbonContext.getThreadLocalCarbonContext().getTenantId(),
            (Registry) CarbonContext.getThreadLocalCarbonContext().getRegistry(RegistryType.SYSTEM_GOVERNANCE));

    @Override
    public List<String> getKeyStoreCertificateAliases(String filter) throws KeyStoreManagementException {

        KeyStoreData keyStoreInfo = getKeystoreData(getKeyStoreName());
        return filterAlias(getAliasList(keyStoreInfo), filter);
    }

    @Override
    public Map<String, X509Certificate> getPublicCertificate() throws KeyStoreManagementException {

        Map<String, X509Certificate> certData = new HashMap<>();
        KeyStoreData keyStoreInfo = getKeystoreData(getKeyStoreName());
        CertData key = keyStoreInfo.getKey();
        certData.put(key.getAlias(), ((CertDataDetail) key).getCertificate());
        return certData;
    }

    @Override
    public X509Certificate getKeyStoreCertificate(String alias) throws KeyStoreManagementException {

        if (StringUtils.isEmpty(alias)) {
            handleClientException(ERROR_CODE_EMPTY_ALIAS, null);
        }

        KeyStoreData keyStoreInfo = getKeystoreData(getKeyStoreName());
        CertData key = keyStoreInfo.getKey();
        if (key != null && StringUtils.equals(key.getAlias(), alias)) {
            return ((CertDataDetail) key).getCertificate();
        }

        CertData[] certDataArray = keyStoreInfo.getCerts();
        for (CertData certData : certDataArray) {
            String aliasFromKeyStore = certData.getAlias();
            if (StringUtils.equals(aliasFromKeyStore, alias)) {
                return ((CertDataDetail) certData).getCertificate();
            }
        }
        return null;
    }

    @Override
    public List<String> getClientCertificateAliases(String filter) throws KeyStoreManagementException {

        KeyStoreData truststoreInfo = getKeystoreData(getTrustStoreName());
        return filterAlias(getAliasList(truststoreInfo), filter);
    }

    @Override
    public X509Certificate getClientCertificate(String alias) throws KeyStoreManagementException {

        if (StringUtils.isEmpty(alias)) {
            handleClientException(ERROR_CODE_EMPTY_ALIAS, null);
        }

        KeyStore trustStore = null;
        try {
            trustStore = keyStoreAdmin.getTrustStore();
        } catch (SecurityConfigException e) {
            handleServerException(ERROR_CODE_RETRIEVE_CLIENT_TRUSTSTORE, getTenantDomain(), e);
        }

        if (trustStore != null) {
            try {
                if (trustStore.containsAlias(alias)) {
                    return (X509Certificate) trustStore.getCertificate(alias);
                }
            } catch (KeyStoreException e) {
                handleServerException(ERROR_CODE_RETRIEVE_CLIENT_TRUSTSTORE_CERTIFICATE, alias, e);
            }
        }
        return null;
    }

    @Override
    public void addCertificate(String alias, String certificate) throws KeyStoreManagementException {

        if (getKeyStoreCertificate(alias) != null) {
            handleClientException(ERROR_CODE_CERTIFICATE_EXISTS, alias);
        }

        try {
            keyStoreAdmin.importCertToStore(alias, certificate, getKeyStoreName());
        } catch (SecurityConfigException e) {
            handleServerException(ERROR_CODE_ADD_CERTIFICATE, alias, e);
        }
    }

    @Override
    public void deleteCertificate(String alias) throws KeyStoreManagementException {

        try {
            keyStoreAdmin.removeCertFromStore(alias, getKeyStoreName());
        } catch (SecurityConfigException e) {
            handleServerException(ERROR_CODE_DELETE_CERTIFICATE, alias, e);
        }
    }

    private String getKeyStoreName() throws KeyStoreManagementException {

        KeyStoreData[] keyStoreDataArray = new KeyStoreData[0];
        try {
            keyStoreDataArray = keyStoreAdmin.getKeyStores(isSuperTenant());
        } catch (SecurityConfigException e) {
            handleServerException(ERROR_CODE_RETRIEVE_KEYSTORE, getTenantDomain(), e);
        }

        for (KeyStoreData keyStoreData : keyStoreDataArray) {
            if (keyStoreData == null) {
                break;
            }
            String keyStoreName = keyStoreData.getKeyStoreName();
            if (isSuperTenant()) {
                if (KeyStoreUtil.isPrimaryStore(keyStoreName)) {
                    return keyStoreName;
                }
            } else {
                String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
                String tenantKeyStoreName = tenantDomain.trim().replace(".", "-") + ".jks";
                if (StringUtils.equals(keyStoreName, tenantKeyStoreName)) {
                    return keyStoreName;
                }
            }
        }
        handleServerException(ERROR_CODE_RETRIEVE_KEYSTORE, getTenantDomain());
        return null;
    }

    private KeyStoreData getKeystoreData(String keyStoreName) throws KeyStoreManagementException {

        KeyStoreData keyStoreData = null;
        keyStoreAdmin.setIncludeCert(true);
        try {
            keyStoreData = keyStoreAdmin.getKeystoreInfo(keyStoreName);
        } catch (SecurityConfigException e) {
            handleServerException(ERROR_CODE_RETRIEVE_KEYSTORE_INFORMATION, keyStoreName, e);
        }
        return keyStoreData;
    }

    private List<String> getAliasList(KeyStoreData keyStoreData) {

        List<String> aliasList = new ArrayList<>();
        CertData key = keyStoreData.getKey();
        if (key != null && key.getAlias() != null) {
            aliasList.add(key.getAlias());
        }

        CertData[] certDataArray = keyStoreData.getCerts();
        for (CertData certData : certDataArray) {
            String alias = certData.getAlias();
            if (alias != null) {
                aliasList.add(alias);
            }
        }
        return aliasList;
    }

    private List<String> filterAlias(List<String> aliases, String filter) throws KeyStoreManagementException {

        if (filter != null) {
            filter = filter.replace(" ", "+");
            String[] extractedFilter = filter.split("[+]");
            if (extractedFilter.length == 3) {
                if (StringUtils.equals(extractedFilter[0], FILTER_FIELD_ALIAS)) {
                    String operation = extractedFilter[1];
                    String value = extractedFilter[2];
                    if (StringUtils.equals(operation, FILTER_OPERATION_EQUALS)) {
                        aliases = aliases.stream().filter(alias -> alias.matches(value))
                                .collect(Collectors.toList());
                    } else if (StringUtils.equals(operation, FILTER_OPERATION_STARTS_WITH)) {
                        aliases = aliases.stream().filter(alias -> alias.startsWith(value))
                                .collect(Collectors.toList());
                    } else if (StringUtils.equals(operation, FILTER_OPERATION_ENDS_WITH)) {
                        aliases = aliases.stream().filter(alias -> alias.endsWith(value))
                                .collect(Collectors.toList());
                    } else if (StringUtils.equals(operation, FILTER_OPERATION_CONTAINS)) {
                        aliases = aliases.stream().filter(alias -> alias.contains(value))
                                .collect(Collectors.toList());
                    } else {
                        handleClientException(ERROR_CODE_UNSUPPORTED_FILTER_OPERATION, operation);
                    }
                }
            } else {
                handleClientException(ERROR_CODE_BAD_VALUE_FOR_FILTER, filter);
            }
        }
        return aliases;
    }

    private String getTenantDomain() {

        return CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
    }

    private boolean isSuperTenant() {

        return CarbonContext.getThreadLocalCarbonContext().getTenantId() == MultitenantConstants.SUPER_TENANT_ID;
    }

    private String getTrustStoreName() {

        ServerConfiguration serverConfiguration = ServerConfiguration.getInstance();
        String fileName = serverConfiguration.getFirstProperty(SERVER_TRUSTSTORE_FILE);
        int index = fileName.lastIndexOf('/');
        String name;
        if (index != -1) {
            name = fileName.substring(index + 1);
        } else {
            index = fileName.lastIndexOf(File.separatorChar);
            if (index != -1) {
                name = fileName.substring(fileName.lastIndexOf(File.separatorChar));
            } else {
                name = fileName;
            }
        }
        return name;
    }

    private void handleServerException(SecurityConstants.KeyStoreMgtConstants.ErrorMessage error, String data)
            throws KeyStoreManagementException {

        String message = includeData(error, data);
        throw new KeyStoreManagementServerException(error.getCode(), message);
    }

    private void handleServerException(SecurityConstants.KeyStoreMgtConstants.ErrorMessage error, String data,
                                       Throwable e) throws KeyStoreManagementException {

        String message = includeData(error, data);
        throw new KeyStoreManagementServerException(error.getCode(), message, e);
    }

    private void handleClientException(SecurityConstants.KeyStoreMgtConstants.ErrorMessage error, String data)
            throws KeyStoreManagementException {

        String message = includeData(error, data);
        throw new KeyStoreManagementClientException(error.getCode(), message);
    }

    private static String includeData(SecurityConstants.KeyStoreMgtConstants.ErrorMessage error, String data) {

        String message;
        if (StringUtils.isNotBlank(data)) {
            message = String.format(error.getMessage(), data);
        } else {
            message = error.getMessage();
        }
        return message;
    }
}
