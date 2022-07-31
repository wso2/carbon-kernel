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

package org.wso2.carbon.security.keystore;

import org.apache.axiom.om.util.Base64;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.core.RegistryResources;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.core.util.KeyStoreUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.security.SecurityConfigException;
import org.wso2.carbon.security.SecurityConstants;
import org.wso2.carbon.security.keystore.service.CertData;
import org.wso2.carbon.security.keystore.service.CertDataDetail;
import org.wso2.carbon.security.keystore.service.KeyStoreData;
import org.wso2.carbon.security.keystore.service.PaginatedCertData;
import org.wso2.carbon.security.keystore.service.PaginatedKeyStoreData;
import org.wso2.carbon.security.util.KeyStoreMgtUtil;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

public class KeyStoreAdmin {

    //trust store
    public static final String SERVER_TRUSTSTORE_FILE = "Security.TrustStore.Location";
    public static final String SERVER_TRUSTSTORE_PASSWORD = "Security.TrustStore.Password";
    public static final String SERVER_TRUSTSTORE_TYPE = "Security.TrustStore.Type";

    private static final Log log = LogFactory.getLog(KeyStoreAdmin.class);
    private Registry registry = null;
    private int tenantId;
    private boolean includeCert = false;

    private static String TRUST_STORE_LOCATION;
    private static String TRUST_STORE_PASSWORD;

    public KeyStoreAdmin(int tenantId, Registry registry) {

        ServerConfiguration config = ServerConfiguration.getInstance();
        TRUST_STORE_LOCATION = config.getFirstProperty("Security.TrustStore.Location");
        TRUST_STORE_PASSWORD = config.getFirstProperty("Security.TrustStore.Password");
        this.registry = registry;
        this.tenantId = tenantId;
    }

    public boolean isIncludeCert() {
        return includeCert;
    }

    public void setIncludeCert(boolean includeCert) {
        this.includeCert = includeCert;
    }

    /**
     * Method to retrive keystore data.
     *
     * @param isSuperTenant - Indication whether the querying super tennat data
     * @return
     * @throws SecurityConfigException
     */
    public KeyStoreData[] getKeyStores(boolean isSuperTenant) throws SecurityConfigException {
        CarbonUtils.checkSecurity();
        KeyStoreData[] names = new KeyStoreData[0];
        try {
            if (registry.resourceExists(SecurityConstants.KEY_STORES)) {
                Collection collection = (Collection) registry.get(SecurityConstants.KEY_STORES);
                String[] ks = collection.getChildren();
                List<KeyStoreData> lst = new ArrayList<>();
                for (int i = 0; i < ks.length; i++) {
                    String fullname = ks[i];

                    if (RegistryResources.SecurityManagement.PRIMARY_KEYSTORE_PHANTOM_RESOURCE
                            .equals(fullname)) {
                        continue;
                    }

                    Resource store = registry.get(ks[i]);
                    int lastIndex = fullname.lastIndexOf("/");
                    String name = fullname.substring(lastIndex + 1);
                    String type = store.getProperty(SecurityConstants.PROP_TYPE);
                    String provider = store.getProperty(SecurityConstants.PROP_PROVIDER);

                    KeyStoreData data = new KeyStoreData();
                    data.setKeyStoreName(name);
                    data.setKeyStoreType(type);
                    data.setProvider(provider);

                    String alias = store.getProperty(SecurityConstants.PROP_PRIVATE_KEY_ALIAS);
                    if (alias != null) {
                        data.setPrivateStore(true);
                    } else {
                        data.setPrivateStore(false);
                    }

                    // Dump the generated public key to the file system for sub tenants 
                    if (!isSuperTenant) {
                        Association[] associations = registry.getAssociations(
                                ks[i], SecurityConstants.ASSOCIATION_TENANT_KS_PUB_KEY);
                        if (associations != null && associations.length > 0) {
                            Resource pubKeyResource = registry.get(associations[0].getDestinationPath());
                            String fileName = generatePubCertFileName(ks[i],
                                    pubKeyResource.getProperty(
                                            SecurityConstants.PROP_TENANT_PUB_KEY_FILE_NAME_APPENDER));
                            if (MessageContext.getCurrentMessageContext() != null) {
                                String pubKeyFilePath = KeyStoreMgtUtil.dumpCert(
                                        MessageContext.getCurrentMessageContext().getConfigurationContext(),
                                        (byte[]) pubKeyResource.getContent(), fileName);
                                data.setPubKeyFilePath(pubKeyFilePath);
                            }
                        }
                    }
                    lst.add(data);

                }
                names = new KeyStoreData[lst.size() + 1];
                Iterator<KeyStoreData> ite = lst.iterator();
                int count = 0;
                while (ite.hasNext()) {
                    names[count] = ite.next();
                    count++;
                }

                if (isSuperTenant) {
                    KeyStoreData data = new KeyStoreData();
                    ServerConfiguration config = ServerConfiguration.getInstance();
                    String fileName = config
                            .getFirstProperty(RegistryResources.SecurityManagement.SERVER_PRIMARY_KEYSTORE_FILE);
                    String type = config
                            .getFirstProperty(RegistryResources.SecurityManagement.SERVER_PRIMARY_KEYSTORE_TYPE);
                    String name = KeyStoreUtil.getKeyStoreFileName(fileName);
                    data.setKeyStoreName(name);
                    data.setKeyStoreType(type);
                    data.setProvider(" ");
                    data.setPrivateStore(true);

                    names[count] = data;
                }

            }
            return names;
        } catch (RegistryException e) {
            String msg = "Error when getting keyStore data";
            log.error(msg, e);
            throw new SecurityConfigException(msg, e);
        }
    }

    public void addKeyStoreWithFilePath(String filePath, String filename, String password,
                                        String provider, String type, String pvtkeyPass) throws SecurityConfigException {
        try {
            addKeyStore(readBytesFromFile(filePath), filename, password, provider, type, pvtkeyPass);
        } catch (IOException e) {
            throw new SecurityConfigException("Error while loading keystore from file " + filePath, e);
        }

    }

    public void addKeyStore(String fileData, String filename, String password, String provider,
                            String type, String pvtkeyPass) throws SecurityConfigException {
        byte[] content = Base64.decode(fileData);
        addKeyStore(content, filename, password, provider, type, pvtkeyPass);
    }

    public void addKeyStore(byte[] content, String filename, String password, String provider,
                            String type, String pvtkeyPass) throws SecurityConfigException {
        if (filename == null) {
            throw new SecurityConfigException("Key Store name can't be null");
        }
        try {
            if (KeyStoreUtil.isPrimaryStore(filename)) {
                throw new SecurityConfigException("Key store " + filename + " already available");
            }
            if (isTrustStore(filename)) {
                throw new SecurityConfigException("Key store " + filename + " already available");
            }
            String path = SecurityConstants.KEY_STORES + "/" + filename;
            if (registry.resourceExists(path)) {
                throw new SecurityConfigException("Key store " + filename + " already available");
            }

            KeyStore keyStore = KeyStore.getInstance(type);
            keyStore.load(new ByteArrayInputStream(content), password.toCharArray());

            // check for more private keys
            Enumeration enumeration = keyStore.aliases();
            String pvtKeyAlias = null;
            while (enumeration.hasMoreElements()) {
                String alias = (String) enumeration.nextElement();
                if (keyStore.isKeyEntry(alias)) {
                    pvtKeyAlias = alias;
                }
            }

            // just to test weather pvt key password is correct.
            keyStore.getKey(pvtKeyAlias, pvtkeyPass.toCharArray());

            CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();

            Resource resource = registry.newResource();
            resource.addProperty(SecurityConstants.PROP_PASSWORD, cryptoUtil
                    .encryptAndBase64Encode(password.getBytes()));
            resource.addProperty(SecurityConstants.PROP_PROVIDER, provider);
            resource.addProperty(SecurityConstants.PROP_TYPE, type);

            if (pvtKeyAlias != null) {
                resource.addProperty(SecurityConstants.PROP_PRIVATE_KEY_ALIAS, pvtKeyAlias);
                resource.addProperty(SecurityConstants.PROP_PRIVATE_KEY_PASS, cryptoUtil
                        .encryptAndBase64Encode(pvtkeyPass.getBytes()));
            }

            resource.setContent(content);
            registry.put(path, resource);
        } catch (SecurityConfigException e) {
            throw e;
        } catch (Exception e) {
            String msg = "Error when adding a keyStore";
            log.error(msg, e);
            throw new SecurityConfigException(msg, e);
        }
    }

    public void addTrustStore(String fileData, String filename, String password, String provider,
                              String type) throws SecurityConfigException {
        byte[] content = Base64.decode(fileData);
        addTrustStore(content, filename, password, provider, type);
    }

    public void addTrustStore(byte[] content, String filename, String password, String provider, String type) throws SecurityConfigException {
        if (filename == null) {
            throw new SecurityConfigException("Key Store name can't be null");
        }
        try {
            if (KeyStoreUtil.isPrimaryStore(filename)) {
                throw new SecurityConfigException("Key store " + filename + " already available");
            }

            String path = SecurityConstants.KEY_STORES + "/" + filename;
            if (registry.resourceExists(path)) {
                throw new SecurityConfigException("Key store " + filename + " already available");
            }

            KeyStore keyStore = KeyStore.getInstance(type);
            keyStore.load(new ByteArrayInputStream(content), password.toCharArray());
            CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
            Resource resource = registry.newResource();
            resource.addProperty(SecurityConstants.PROP_PASSWORD, cryptoUtil
                    .encryptAndBase64Encode(password.getBytes()));
            resource.addProperty(SecurityConstants.PROP_PROVIDER, provider);
            resource.addProperty(SecurityConstants.PROP_TYPE, type);
            resource.setContent(content);
            registry.put(path, resource);
        } catch (SecurityConfigException e) {
            throw e;
        } catch (Exception e) {
            String msg = "Error when adding a trustStore";
            log.error(msg, e);
            throw new SecurityConfigException(msg, e);
        }
    }

    public void deleteStore(String keyStoreName) throws SecurityConfigException {
        try {

            if (StringUtils.isBlank(keyStoreName)) {
                throw new SecurityConfigException("Key Store name can't be null");
            }

            if (KeyStoreUtil.isPrimaryStore(keyStoreName)) {
                throw new SecurityConfigException("Not allowed to delete the primary key store : "
                        + keyStoreName);
            }
            if (isTrustStore(keyStoreName)) {
                throw new SecurityConfigException("Not allowed to delete the trust store : "
                        + keyStoreName);
            }
            String path = SecurityConstants.KEY_STORES + "/" + keyStoreName;
            boolean isFound = false;
            Association[] assocs = registry.getAllAssociations(path);
            if (assocs.length > 0) {
                isFound = true;
            }

            if (isFound) {
                throw new SecurityConfigException("Key store : " + keyStoreName +
                        " is already in use and can't be deleted");
            }
            registry.delete(path);
        } catch (RegistryException e) {
            String msg = "Error when deleting a keyStore";
            log.error(msg, e);
            throw new SecurityConfigException(msg, e);
        }
    }

    public void importCertToStore(String fileName, String certData, String keyStoreName)
            throws SecurityConfigException {
        try {
            if (keyStoreName == null) {
                throw new SecurityConfigException("Key Store name can't be null");
            }

            KeyStore ks = getKeyStore(keyStoreName);
            X509Certificate cert = extractCertificate(certData);

            if (ks.getCertificateAlias(cert) != null) {
                // We already have this certificate in the key store - ignore
                // adding it twice
                return;
            }

            ks.setCertificateEntry(fileName, cert);

            updateKeyStore(keyStoreName, ks);

            if (isTrustStore(keyStoreName)) {
                System.setProperty(IdentityUtil.PROP_TRUST_STORE_UPDATE_REQUIRED, "true");
            }

        } catch (SecurityConfigException e) {
            throw e;
        } catch (Exception e) {
            String msg = "Error when importing cert to the keyStore";
            log.error(msg, e);
            throw new SecurityConfigException(msg, e);
        }

    }

    public String importCertToStore(String certData, String keyStoreName)
            throws SecurityConfigException {
        String alias = null;

        try {
            if (keyStoreName == null) {
                throw new SecurityConfigException("Key Store name can't be null");
            }

            KeyStore ks = getKeyStore(keyStoreName);
            X509Certificate cert = extractCertificate(certData);

            if (ks.getCertificateAlias(cert) != null) {
                // We already have this certificate in the key store - ignore
                // adding it twice
                return null;
            }
            alias = cert.getSubjectDN().getName();
            ks.setCertificateEntry(alias, cert);

            updateKeyStore(keyStoreName, ks);

            if (isTrustStore(keyStoreName)) {
                System.setProperty(IdentityUtil.PROP_TRUST_STORE_UPDATE_REQUIRED, "true");
            }

            return alias;

        } catch (SecurityConfigException e) {
            throw e;
        } catch (Exception e) {
            String msg = "Error when importing cert to keyStore";
            log.error(msg, e);
            throw new SecurityConfigException(msg);
        }
    }

    public void removeCertFromStore(String alias, String keyStoreName)
            throws SecurityConfigException {
        try {
            if (keyStoreName == null) {
                throw new SecurityConfigException("Key Store name can't be null");
            }

            KeyStore ks = getKeyStore(keyStoreName);

            if (ks.getCertificate(alias) == null) {
                return;
            }

            ks.deleteEntry(alias);
            updateKeyStore(keyStoreName, ks);

            if (isTrustStore(keyStoreName)) {
                System.setProperty(IdentityUtil.PROP_TRUST_STORE_UPDATE_REQUIRED, Boolean.TRUE.toString());
            }
        } catch (SecurityConfigException e) {
            throw e;
        } catch (Exception e) {
            String msg = "Error when removing cert from store";
            log.error(msg, e);
            throw new SecurityConfigException(msg);
        }
    }

    public String[] getStoreEntries(String keyStoreName) throws SecurityConfigException {
        String[] names;
        try {
            if (keyStoreName == null) {
                throw new Exception("keystore name cannot be null");
            }

            //KeyStoreManager keyMan = KeyStoreManager.getInstance(tenantId);
            KeyStore ks = getKeyStore(keyStoreName);

            Enumeration<String> enm = ks.aliases();
            List<String> lst = new ArrayList<>();
            while (enm.hasMoreElements()) {
                lst.add(enm.nextElement());
            }

            names = lst.toArray(new String[lst.size()]);
        } catch (SecurityConfigException e) {
            throw e;
        } catch (Exception e) {
            String msg = "Error when getting store entries";
            log.error(msg, e);
            throw new SecurityConfigException(msg);
        }

        return names;
    }

    /**
     * This method will list 1. Certificate aliases 2. Private key alise 3. Private key value to a
     * given keystore.
     *
     * @param keyStoreName The name of the keystore
     * @return Instance of KeyStoreData
     * @throws SecurityConfigException will be thrown
     */
    public KeyStoreData getKeystoreInfo(String keyStoreName) throws SecurityConfigException {
        try {

            if (keyStoreName == null) {
                throw new Exception("keystore name cannot be null");
            }

            KeyStore keyStore;
            String keyStoreType;
            String privateKeyPassword = null;
            if (KeyStoreUtil.isPrimaryStore(keyStoreName)) {
                KeyStoreManager keyMan = KeyStoreManager.getInstance(tenantId);
                keyStore = keyMan.getPrimaryKeyStore();
                ServerConfiguration serverConfig = ServerConfiguration.getInstance();
                keyStoreType = serverConfig
                        .getFirstProperty(RegistryResources.SecurityManagement.SERVER_PRIMARY_KEYSTORE_TYPE);
                privateKeyPassword = serverConfig
                        .getFirstProperty(RegistryResources.SecurityManagement.SERVER_PRIVATE_KEY_PASSWORD);
            } else if (isTrustStore(keyStoreName)) {
                keyStore = getTrustStore();
                ServerConfiguration serverConfig = ServerConfiguration.getInstance();
                keyStoreType = serverConfig.getFirstProperty(SERVER_TRUSTSTORE_TYPE);
                privateKeyPassword = serverConfig.getFirstProperty(SERVER_TRUSTSTORE_PASSWORD);
            } else {
                String path = SecurityConstants.KEY_STORES + "/" + keyStoreName;
                if (!registry.resourceExists(path)) {
                    throw new SecurityConfigException("Key Store not found");
                }
                Resource resource = registry.get(path);
                keyStore = getKeyStore(keyStoreName);
                keyStoreType = resource.getProperty(SecurityConstants.PROP_TYPE);

                String encpass = resource.getProperty(SecurityConstants.PROP_PRIVATE_KEY_PASS);
                if (encpass != null) {
                    CryptoUtil util = CryptoUtil.getDefaultCryptoUtil();
                    privateKeyPassword = new String(util.base64DecodeAndDecrypt(encpass));
                }
            }
            // Fill the information about the certificates
            Enumeration<String> aliases = keyStore.aliases();
            List<org.wso2.carbon.security.keystore.service.CertData> certDataList = new ArrayList<>();
            Format formatter = new SimpleDateFormat("dd/MM/yyyy");

            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                if (keyStore.isCertificateEntry(alias)) {
                    X509Certificate cert = (X509Certificate) keyStore.getCertificate(alias);
                    certDataList.add(fillCertData(cert, alias, formatter));
                }
            }

            // Create a cert array
            CertData[] certs = certDataList.toArray(new CertData[certDataList.size()]);

            // Create a KeyStoreData bean, set the name and fill in the cert information
            KeyStoreData keyStoreData = new KeyStoreData();
            keyStoreData.setKeyStoreName(keyStoreName);
            keyStoreData.setCerts(certs);
            keyStoreData.setKeyStoreType(keyStoreType);

            aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                // There be only one entry in WSAS related keystores
                if (keyStore.isKeyEntry(alias)) {
                    X509Certificate cert = (X509Certificate) keyStore.getCertificate(alias);
                    keyStoreData.setKey(fillCertData(cert, alias, formatter));
                    PrivateKey key = (PrivateKey) keyStore.getKey(alias, privateKeyPassword
                            .toCharArray());
                    String pemKey;
                    pemKey = "-----BEGIN PRIVATE KEY-----\n";
                    pemKey += Base64.encode(key.getEncoded());
                    pemKey += "\n-----END PRIVATE KEY-----";
                    keyStoreData.setKeyValue(pemKey);
                    break;

                }
            }
            return keyStoreData;
        } catch (Exception e) {
            String msg = "Error has encounted while loading the keystore to the given keystore name "
                    + keyStoreName;
            log.error(msg, e);
            throw new SecurityConfigException(msg);
        }

    }

    public Key getPrivateKey(String alias, boolean isSuperTenant) throws SecurityConfigException {
        KeyStoreData[] keystores = getKeyStores(isSuperTenant);
        KeyStore keyStore = null;
        String privateKeyPassowrd = null;

        try {

            for (int i = 0; i < keystores.length; i++) {
                if (KeyStoreUtil.isPrimaryStore(keystores[i].getKeyStoreName())) {
                    KeyStoreManager keyMan = KeyStoreManager.getInstance(tenantId);
                    keyStore = keyMan.getPrimaryKeyStore();
                    ServerConfiguration serverConfig = ServerConfiguration.getInstance();
                    privateKeyPassowrd = serverConfig
                            .getFirstProperty(RegistryResources.SecurityManagement.SERVER_PRIVATE_KEY_PASSWORD);
                    return keyStore.getKey(alias, privateKeyPassowrd.toCharArray());
                }
            }
        } catch (Exception e) {
            String msg = "Error has encounted while loading the key for the given alias " + alias;
            log.error(msg, e);
            throw new SecurityConfigException(msg);
        }
        return null;
    }

    private CertData fillCertData(X509Certificate cert, String alise, Format formatter)
            throws CertificateEncodingException {
        CertData certData = null;

        if (includeCert) {
            certData = new CertDataDetail();
        } else {
            certData = new CertData();
        }
        certData.setAlias(alise);
        certData.setSubjectDN(cert.getSubjectDN().getName());
        certData.setIssuerDN(cert.getIssuerDN().getName());
        certData.setSerialNumber(cert.getSerialNumber());
        certData.setVersion(cert.getVersion());
        certData.setNotAfter(formatter.format(cert.getNotAfter()));
        certData.setNotBefore(formatter.format(cert.getNotBefore()));
        certData.setPublicKey(Base64.encode(cert.getPublicKey().getEncoded()));

        if (includeCert) {
            ((CertDataDetail) certData).setCertificate(cert);
        }

        return certData;
    }

    private byte[] readBytesFromFile(String filePath) throws IOException {
        InputStream inputStream = null;
        File file = new File(filePath);
        long length;
        byte[] bytes;
        int offset = 0;
        int numRead = 0;

        try {
            inputStream = new FileInputStream(file);
            length = file.length();
            bytes = new byte[(int) length];

            while (offset < bytes.length
                    && (numRead = inputStream.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }

        return bytes;
    }

    /**
     * This method is used to generate the file name of the pub. cert of a tenant
     *
     * @param ksLocation keystore location in the registry
     * @param uuid       UUID appender
     * @return file name of the pub. cert
     */
    private String generatePubCertFileName(String ksLocation, String uuid) {
        String tenantName = ksLocation.substring(ksLocation.lastIndexOf("/"));
        if (tenantName.endsWith(".jks")) {
            tenantName = tenantName.replace(".jks", "");
        }
        return tenantName + "-" + uuid + ".cert";
    }

    /**
     * This method is used internally to do the pagination purposes.
     *
     * @param pageNumber  page Number
     * @param certDataSet set of keyStoreData
     * @return PaginatedPolicySetDTO object containing the number of pages and the set of policies
     * that reside in the given page.
     */
    private PaginatedCertData doPaging(int pageNumber, CertData[] certDataSet) {

        PaginatedCertData paginatedCertData = new PaginatedCertData();
        if (certDataSet.length == 0) {
            paginatedCertData.setCertDataSet(new CertData[0]);
            return paginatedCertData;
        }
        int itemsPerPageInt = SecurityConstants.ITEMS_PER_PAGE;
        int numberOfPages = (int) Math.ceil((double) certDataSet.length / itemsPerPageInt);
        if (pageNumber > numberOfPages - 1) {
            pageNumber = numberOfPages - 1;
        }
        int startIndex = pageNumber * itemsPerPageInt;
        int endIndex = certDataSet.length;
        if (numberOfPages > SecurityConstants.CACHING_PAGE_SIZE) {
            endIndex = (pageNumber + SecurityConstants.CACHING_PAGE_SIZE) * itemsPerPageInt;
        }
        CertData[] returnedCertDataSet = new CertData[endIndex];

        for (int i = startIndex, j = 0; i < endIndex && i < certDataSet.length; i++, j++) {
            returnedCertDataSet[j] = certDataSet[i];
        }

        paginatedCertData.setCertDataSet(returnedCertDataSet);
        paginatedCertData.setNumberOfPages(numberOfPages);

        return paginatedCertData;
    }

    /**
     * This method is used internally for the filtering purposes.
     *
     * @param filter      Filter string.
     * @param certDataSet Certificate or key array.
     * @return Cert Data array after filtering.
     */
    private static CertData[] doFilter(String filter, CertData[] certDataSet) {

        if (certDataSet != null && certDataSet.length != 0) {
            String regPattern = filter.replace("*", ".*");
            List<CertData> certDataList = new ArrayList<CertData>();

            for (CertData cert : certDataSet) {
                if (cert != null && cert.getAlias().toLowerCase().matches(regPattern.toLowerCase())) {
                    certDataList.add(cert);
                }
            }

            return (CertData[]) certDataList.toArray(new CertData[0]);
        } else {
            return new CertData[0];
        }
    }

    /**
     * Gets the keystore info by keystore name with its certificates and key certificates.
     *
     * @param keyStoreName The name of the keystore
     * @param pageNumber   page number
     * @return Instance of KeyStoreData
     * @throws SecurityConfigException will be thrown
     */
    public PaginatedKeyStoreData getPaginatedKeystoreInfo(String keyStoreName, int pageNumber)
            throws SecurityConfigException {

        if (StringUtils.isEmpty(keyStoreName)) {
            throw new SecurityConfigException("Keystore name cannot be empty or null.");
        }

        try {
            // Get keystore.
            KeyStore keyStore = getKeyStore(tenantId, keyStoreName);
            // Get keystore type.
            String keyStoreType = getKeyStoreType(keyStoreName);

            // Extract certificates from aliases as list.
            List<CertData> certDataList = getCertificates(keyStore);
            List<CertData> keyCertDataList = getKeyCertificates(keyStore);

            // Create a certificate array.
            CertData[] certs = certDataList.toArray(new CertData[certDataList.size()]);
            // Get paginated certificates.
            PaginatedCertData paginatedCerts = doPaging(pageNumber, certs);

            // Create a key certificate array.
            CertData[] keyCerts = keyCertDataList.toArray(new CertData[keyCertDataList.size()]);
            // Get paginated key certificates.
            PaginatedCertData paginatedKeyCerts = doPaging(pageNumber, keyCerts);

            // Fill information about the keystore to PaginatedKeyStoreData.
            PaginatedKeyStoreData keyStoreData = fillPaginatedKeyStoreData(keyStoreName, keyStoreType,
                    paginatedCerts, paginatedKeyCerts);

            return keyStoreData;
        } catch (Exception e) {
            throw new SecurityConfigException(e.getMessage());
        }

    }

    /**
     * Gets the keystore info by keystore name and filters its certificates and key certificates
     * by applying the filter for certificate aliases.
     *
     * @param keyStoreName The name of the keystore.
     * @param pageNumber   Page number.
     * @param filter       Filter for certificate alias.
     * @return Instance of KeyStoreData.
     * @throws SecurityConfigException will be thrown.
     */
    public PaginatedKeyStoreData getFilteredPaginatedKeyStoreInfo(String keyStoreName, int pageNumber,
                                                                  String filter) throws SecurityConfigException {

        if (StringUtils.isEmpty(keyStoreName)) {
            throw new SecurityConfigException("Keystore name cannot be empty or null.");
        }

        try {
            // Get keystore.
            KeyStore keyStore = getKeyStore(tenantId, keyStoreName);
            // Get keystore type.
            String keyStoreType = getKeyStoreType(keyStoreName);

            // Extract certificates from aliases as list.
            List<CertData> certDataList = getCertificates(keyStore);
            List<CertData> keyCertDataList = getKeyCertificates(keyStore);
            // Filter and paginate certs and keyCerts.
            PaginatedCertData paginatedCerts = filterAndPaginateCerts(certDataList, filter, pageNumber);
            PaginatedCertData paginatedKeyCerts = filterAndPaginateCerts(keyCertDataList, filter, pageNumber);
            // Fill information about the keystore to PaginatedKeyStoreData.
            PaginatedKeyStoreData keyStoreData = fillPaginatedKeyStoreData(keyStoreName, keyStoreType,
                    paginatedCerts, paginatedKeyCerts);

            return keyStoreData;
        } catch (Exception e) {
            throw new SecurityConfigException(e.getMessage());
        }
    }

    /**
     * @param tenantId     Tenant Id.
     * @param keyStoreName Keystore Name.
     * @return
     * @throws Exception
     */
    private KeyStore getKeyStore(int tenantId, String keyStoreName) throws Exception {
    
        KeyStore keyStore;
        if (KeyStoreUtil.isPrimaryStore(keyStoreName)) {
            KeyStoreManager keyStoreManager = KeyStoreManager.getInstance(tenantId);
            keyStore = keyStoreManager.getPrimaryKeyStore();
        } else if (isTrustStore(keyStoreName)) {
            keyStore = getTrustStore();
        } else {
            keyStore = getKeyStore(keyStoreName);
        }
        return keyStore;
    }

    /**
     * Get keystore type.
     *
     * @param keyStoreName Keystore name.
     * @return
     * @throws SecurityConfigException
     * @throws RegistryException
     */
    private String getKeyStoreType(String keyStoreName) throws SecurityConfigException, RegistryException {
    
        String keyStoreType;
        if (KeyStoreUtil.isPrimaryStore(keyStoreName)) {
            ServerConfiguration serverConfig = ServerConfiguration.getInstance();
            keyStoreType = serverConfig
                    .getFirstProperty(RegistryResources.SecurityManagement.SERVER_PRIMARY_KEYSTORE_TYPE);
        } else if (isTrustStore(keyStoreName)) {
            ServerConfiguration serverConfig = ServerConfiguration.getInstance();
            keyStoreType = serverConfig.getFirstProperty(SERVER_TRUSTSTORE_TYPE);
        } else {
            String path = SecurityConstants.KEY_STORES + "/" + keyStoreName;
            if (!registry.resourceExists(path)) {
                throw new SecurityConfigException("Keystore " + keyStoreName + " not found at " + path);
            }
            Resource resource = registry.get(path);
            keyStoreType = resource.getProperty(SecurityConstants.PROP_TYPE);
        }
        return keyStoreType;
    }

    /**
     * Fill PaginatedKeyStoreData with keystore details.
     *
     * @param keyStoreName Name of the keystore.
     * @param keyStoreType Type of the keystore.
     * @param certs        Paginated certificates.
     * @param keyCerts     Paginated key certificates.
     * @return Paginated KeyStore Data.
     */
    private PaginatedKeyStoreData fillPaginatedKeyStoreData(String keyStoreName, String keyStoreType,
                                                            PaginatedCertData certs, PaginatedCertData keyCerts) {

        // Create a KeyStoreData bean, set the name, type and fill in the cert information.
        PaginatedKeyStoreData keyStoreData = new PaginatedKeyStoreData();
        keyStoreData.setKeyStoreName(keyStoreName);
        keyStoreData.setKeyStoreType(keyStoreType);
        keyStoreData.setPaginatedCertData(certs);
        keyStoreData.setPaginatedKeyData(keyCerts);
        return keyStoreData;
    }

    /**
     * Get certificates related to alias from the keystore.
     *
     * @param keyStore Keystore
     * @return List of certificate data.
     * @throws KeyStoreException
     * @throws CertificateEncodingException
     */
    private List<CertData> getCertificates(KeyStore keyStore)
            throws KeyStoreException, CertificateEncodingException {

        Enumeration<String> aliases = keyStore.aliases();
        // Create lists for cert and key lists.
        List<CertData> certDataList = new ArrayList<>();
        Format formatter = new SimpleDateFormat("dd/MM/yyyy");

        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            X509Certificate cert = (X509Certificate) keyStore.getCertificate(alias);
            if (keyStore.isCertificateEntry(alias)) {
                certDataList.add(fillCertData(cert, alias, formatter));
            }
        }
        return certDataList;
    }

    /**
     * Get key certificates related to alias from the keystore.
     *
     * @param keyStore Keystore
     * @return List of certificate data.
     * @throws KeyStoreException
     * @throws CertificateEncodingException
     */
    private List<CertData> getKeyCertificates(KeyStore keyStore)
            throws KeyStoreException, CertificateEncodingException {

        Enumeration<String> aliases = keyStore.aliases();
        // Create lists for cert and key lists.
        List<CertData> certDataList = new ArrayList<>();
        Format formatter = new SimpleDateFormat("dd/MM/yyyy");

        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            X509Certificate cert = (X509Certificate) keyStore.getCertificate(alias);
            if (keyStore.isKeyEntry(alias)) {
                certDataList.add(fillCertData(cert, alias, formatter));
            }
        }
        return certDataList;
    }

    /**
     * Filter and paginate certificate list.
     *
     * @param certDataList Certificate list.
     * @param filterString Filter text.
     * @param pageNumber   Page number.
     * @return Paginated and Filtered Certificate Data.
     */
    private PaginatedCertData filterAndPaginateCerts(List<CertData> certDataList, String filterString, int pageNumber) {

        PaginatedCertData paginatedCerts;
        CertData[] certs = certDataList.toArray(new CertData[0]);
        certs = (doFilter(filterString, certs));
        paginatedCerts = doPaging(pageNumber, certs);
        return paginatedCerts;
    }

    /**
     * Load the default trust store (allowed only for super tenant).
     *
     * @return trust store object
     * @throws SecurityConfigException if retrieving the truststore fails.
     */
    public KeyStore getTrustStore() throws SecurityConfigException {
    
        //Allow only the super tenant to access the default trust store.
        if (tenantId != MultitenantConstants.SUPER_TENANT_ID) {
            throw new SecurityConfigException("Permission denied for accessing trust store");
        }

        KeyStore trustStore;
        ServerConfiguration serverConfiguration = ServerConfiguration.getInstance();
        String file = new File(serverConfiguration.getFirstProperty(SERVER_TRUSTSTORE_FILE)).getAbsolutePath();

        KeyStore store;
        try {
            store = KeyStore.getInstance(serverConfiguration.getFirstProperty(SERVER_TRUSTSTORE_TYPE));
        } catch (KeyStoreException e) {
            throw new SecurityConfigException("Error occurred while loading keystore.", e);
        }

        String password = serverConfiguration.getFirstProperty(SERVER_TRUSTSTORE_PASSWORD);

        try (FileInputStream in = new FileInputStream(file)) {
            store.load(in, password.toCharArray());
            trustStore = store;
        } catch (CertificateException | NoSuchAlgorithmException | IOException e) {
            throw new SecurityConfigException("Error occurred while loading trust store", e);
        }
        return trustStore;
    }

    /**
     * Check if the supplied id is the system configured trust store
     *
     * @param id id (file name) of the keystore
     * @return boolean true if supplied id is the configured trust store
     */
    private boolean isTrustStore(String id) {

        ServerConfiguration serverConfiguration = ServerConfiguration.getInstance();
        String fileName = serverConfiguration.getFirstProperty(SERVER_TRUSTSTORE_FILE);
        int index = fileName.lastIndexOf('/');
        if (index != -1) {
            String name = fileName.substring(index + 1);
            if (name.equals(id)) {
                return true;
            }
        } else {
            index = fileName.lastIndexOf(File.separatorChar);
            String name;
            if (index != -1) {
                name = fileName.substring(fileName.lastIndexOf(File.separatorChar));
            } else {
                name = fileName;
            }

            if (name.equals(id)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves the {@link KeyStore} object of the given keystore name.
     *
     * @param keyStoreName name of the keystore.
     * @return {@link KeyStore} object.
     * @throws Exception if retrieving the keystore fails.
     */
    public KeyStore getKeyStore(String keyStoreName) throws Exception {

        if (isTrustStore(keyStoreName)) {
            return getTrustStore();
        } else {
            KeyStoreManager keyMan = KeyStoreManager.getInstance(tenantId);
            return keyMan.getKeyStore(keyStoreName);
        }
    }

    private void updateKeyStore(String name, KeyStore keyStore) throws Exception {

        FileOutputStream resource1;
        String outputStream1;
        String path;
        if (isTrustStore(name)) {
            path = (new File(TRUST_STORE_LOCATION)).getAbsolutePath();
            resource1 = null;

            try {
                resource1 = new FileOutputStream(path);
                outputStream1 = TRUST_STORE_PASSWORD;
                keyStore.store(resource1, outputStream1.toCharArray());
            } finally {
                if (resource1 != null) {
                    resource1.close();
                }
            }
        } else {
            KeyStoreManager keyStoreManager = KeyStoreManager.getInstance(tenantId);
            keyStoreManager.updateKeyStore(name, keyStore);
        }
    }

    /**
     * Extract the encoded certificate into {@link X509Certificate}.
     *
     * @param certData encoded certificate.
     * @return {@link X509Certificate} object.
     * @throws SecurityConfigException if extracting the certificate fails.
     */
    public X509Certificate extractCertificate(String certData) throws SecurityConfigException {

        byte[] bytes = Base64.decode(certData);
        X509Certificate cert;
        try {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            cert = (X509Certificate) factory
                    .generateCertificate(new ByteArrayInputStream(bytes));
        } catch (CertificateException e) {
            if (log.isDebugEnabled()) {
                log.debug(e.getMessage(), e);
            }
            throw new SecurityConfigException("Invalid format of the provided certificate file");
        }
        return cert;
    }
}
