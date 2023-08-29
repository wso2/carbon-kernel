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

package org.wso2.carbon.core.keystore;

import org.apache.axiom.om.util.Base64;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.core.RegistryResources;
import org.wso2.carbon.core.keystore.util.KeyStoreMgtUtil;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.core.util.KeyStoreUtil;
import org.wso2.carbon.core.keystore.dao.KeyStoreDAO;
import org.wso2.carbon.core.keystore.dao.PubCertDAO;
import org.wso2.carbon.core.keystore.dao.impl.KeyStoreDAOImpl;
import org.wso2.carbon.core.keystore.dao.impl.PubCertDAOImpl;
import org.wso2.carbon.core.keystore.model.KeyStoreModel;
import org.wso2.carbon.core.keystore.service.CertData;
import org.wso2.carbon.core.keystore.service.CertDataDetail;
import org.wso2.carbon.core.keystore.service.KeyStoreData;
import org.wso2.carbon.core.keystore.service.PaginatedCertData;
import org.wso2.carbon.core.keystore.service.PaginatedKeyStoreData;
import org.wso2.carbon.user.api.UserStoreException;
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
import java.security.UnrecoverableKeyException;
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
import java.util.Optional;

import static org.wso2.carbon.core.keystore.constants.KeyStoreConstants.CACHING_PAGE_SIZE;
import static org.wso2.carbon.core.keystore.constants.KeyStoreConstants.ErrorMessage.ERROR_MESSAGE_RETRIEVE_KEYSTORE;
import static org.wso2.carbon.core.keystore.constants.KeyStoreConstants.ErrorMessage.ERROR_MESSAGE_RETRIEVE_PUBLIC_CERT;
import static org.wso2.carbon.core.keystore.constants.KeyStoreConstants.ITEMS_PER_PAGE;
import static org.wso2.carbon.core.keystore.constants.KeyStoreConstants.KEY_STORES;

/**
 * Key Store Admin class.
 */
public class KeyStoreAdmin {

    // todo: add public method comments

    //trust store
    public static final String SERVER_TRUSTSTORE_FILE = "Security.TrustStore.Location";
    public static final String SERVER_TRUSTSTORE_PASSWORD = "Security.TrustStore.Password";
    public static final String SERVER_TRUSTSTORE_TYPE = "Security.TrustStore.Type";

    private static final String PATH_SEPARATOR = "/";

    // This is used as the alternative for RegistryResources.SecurityManagement.PRIMARY_KEYSTORE_PHANTOM_RESOURCE.
    private static final String ALTERNATE_PRIMARY_KEYSTORE_PHANTOM_RESOURCE = "carbon-primary-ks";

    private static final Log log = LogFactory.getLog(KeyStoreAdmin.class);
    private static final String PROP_TRUST_STORE_UPDATE_REQUIRED =
            "org.wso2.carbon.identity.core.util.TRUST_STORE_UPDATE_REQUIRED";
    private int tenantId;
    private String tenantUUID;
    private boolean includeCert = false;
    private KeyStoreDAO keyStoreDAO;
    private PubCertDAO pubCertDAO;
    private final String trustStoreLocation;
    private final String trustStorePassword;

    public KeyStoreAdmin(int tenantId) {

        ServerConfiguration config = ServerConfiguration.getInstance();
        trustStoreLocation = config.getFirstProperty(SERVER_TRUSTSTORE_FILE);
        trustStorePassword = config.getFirstProperty(SERVER_TRUSTSTORE_PASSWORD);
        this.tenantId = tenantId;
        try {
            tenantUUID = KeyStoreMgtUtil.getTenantUUID(tenantId);
            keyStoreDAO = new KeyStoreDAOImpl();
            pubCertDAO = new PubCertDAOImpl();
        } catch (KeyStoreManagementException e) {
            log.error("Error while retrieving the tenant ID.", e);
        } catch (UserStoreException e) {
            log.error("Error while retrieving the tenant UUID.", e);
        }
    }

    public boolean isIncludeCert() {

        return includeCert;
    }

    public void setIncludeCert(boolean includeCert) {

        this.includeCert = includeCert;
    }

    /**
     * Method to retrieve keystore data.
     *
     * @param isSuperTenant Indication whether the querying super tenant data.
     * @return A key store data array.
     * @throws KeyStoreManagementException Throws if an error occurred while getting the key stores.
     */
    public KeyStoreData[] getKeyStores(boolean isSuperTenant) throws KeyStoreManagementException {

        CarbonUtils.checkSecurity();
        KeyStoreData[] names = new KeyStoreData[0];

        try {
            List<KeyStoreModel> keyStores = keyStoreDAO.getKeyStores(tenantUUID);
            List<KeyStoreData> keyStoreDataList = new ArrayList<>();
            if (!CollectionUtils.isEmpty(keyStoreDataList)) {
                for (KeyStoreModel keyStoreModel : keyStores) {
                    if (ALTERNATE_PRIMARY_KEYSTORE_PHANTOM_RESOURCE.equals(keyStoreModel.getFileName())) {
                        continue;
                    }

                    KeyStoreData data = new KeyStoreData();
                    data.setKeyStoreName(keyStoreModel.getFileName());
                    data.setKeyStoreType(keyStoreModel.getType());
                    data.setProvider(keyStoreModel.getProvider());

                    String alias = keyStoreModel.getPrivateKeyAlias();
                    if (alias != null) {
                        data.setPrivateStore(true);
                    } else {
                        data.setPrivateStore(false);
                    }

                    if (!isSuperTenant) {
                        Optional<String> pubCertId =
                                keyStoreDAO.getPubCertIdFromKeyStore(tenantUUID, keyStoreModel.getFileName());

                        pubCertId.flatMap(id -> {
                            try {
                                return pubCertDAO.getPubCert(id);
                            } catch (KeyStoreManagementException e) {
                                log.error(ERROR_MESSAGE_RETRIEVE_PUBLIC_CERT.getMessage(), e);
                                return Optional.empty();
                            }
                        })
                        .ifPresent(pubCert -> {
                            String fileName = generatePubCertFileName(KEY_STORES + PATH_SEPARATOR +
                                    keyStoreModel.getFileName(), pubCert.getFileNameAppender());
                            if (MessageContext.getCurrentMessageContext() != null) {
                                String pubKeyFilePath = KeyStoreMgtUtil.dumpCert(
                                        MessageContext.getCurrentMessageContext().getConfigurationContext(),
                                        pubCert.getContent(), fileName);
                                data.setPubKeyFilePath(pubKeyFilePath);
                            }
                        });
                    }
                    keyStoreDataList.add(data);
                }
            }

            // Prepare the next position for the super tenant keystore data.
            names = new KeyStoreData[keyStoreDataList.size() + 1];
            Iterator<KeyStoreData> keyStoreDataIterator = keyStoreDataList.iterator();
            int count = 0;
            while (keyStoreDataIterator.hasNext()) {
                names[count] = keyStoreDataIterator.next();
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
            return names;
        } catch (KeyStoreManagementException e) {
            log.error(ERROR_MESSAGE_RETRIEVE_KEYSTORE.getMessage(), e);
            throw new KeyStoreManagementException(ERROR_MESSAGE_RETRIEVE_KEYSTORE.getMessage(), e);
        }
    }

    /**
     * Method to add keystore when a file path is given instead of file data.
     *
     * @param filePath   File path of the keystore data.
     * @param filename   Name of the keystore.
     * @param password   Password of the keystore.
     * @param provider   Provider of the keystore.
     * @param type       Type of the keystore.
     * @param pvtKeyPass Password of the private key.
     * @throws KeyStoreManagementException Throws if an error occurred while adding the keystore.
     */
    public void addKeyStoreWithFilePath(String filePath, String filename, String password,
                                        String provider, String type, String pvtKeyPass)
            throws KeyStoreManagementException {

        try {
            addKeyStore(readBytesFromFile(filePath), filename, password, provider, type, pvtKeyPass);
        } catch (IOException e) {
            throw new KeyStoreManagementException("Error while loading keystore from file " + filePath, e);
        }

    }

    /**
     * Method to add keystore when a file data is given.
     *
     * @param fileData   File data of the keystore.
     * @param filename   Name of the keystore.
     * @param password   Password of the keystore.
     * @param provider   Provider of the keystore.
     * @param type       Type of the keystore.
     * @param pvtKeyPass Password of the private key.
     * @throws KeyStoreManagementException Throws if an error occurred while adding the keystore.
     */
    public void addKeyStore(String fileData, String filename, String password, String provider,
                            String type, String pvtKeyPass) throws KeyStoreManagementException {

        byte[] content = Base64.decode(fileData);
        addKeyStore(content, filename, password, provider, type, pvtKeyPass);
    }

    /**
     * Method to add keystore when a byte array is given for the file data.
     *
     * @param content    Byte array of the keystore data.
     * @param filename   Name of the keystore.
     * @param password   Password of the keystore.
     * @param provider   Provider of the keystore.
     * @param type       Type of the keystore.
     * @param pvtKeyPass Password of the private key.
     * @throws KeyStoreManagementException Throws if an error occurred while adding the keystore.
     */
    public void addKeyStore(byte[] content, String filename, String password, String provider,
                            String type, String pvtKeyPass) throws KeyStoreManagementException {

        if (filename == null) {
            throw new KeyStoreManagementException("Key Store name can't be null");
        }
        try {
            if (KeyStoreUtil.isPrimaryStore(filename)) {
                throw new KeyStoreManagementException("Key store " + filename + " already available");
            }
            if (isTrustStore(filename)) {
                throw new KeyStoreManagementException("Key store " + filename + " already available");
            }
            if (isExistKeyStore(filename)) {
                throw new KeyStoreManagementException("Key store " + filename + " already available");
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
            keyStore.getKey(pvtKeyAlias, pvtKeyPass.toCharArray());

            CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();

            KeyStoreModel data;

            if (pvtKeyAlias == null) {
                data = new KeyStoreModel.KeyStoreModelBuilder()
                        .fileName(filename)
                        .type(type)
                        .provider(provider)
                        .password(cryptoUtil.encryptAndBase64Encode(password.getBytes()).toCharArray())
                        .content(content)
                        .build();
            } else {
                data = new KeyStoreModel.KeyStoreModelBuilder()
                        .fileName(filename)
                        .type(type)
                        .provider(provider)
                        .password(cryptoUtil.encryptAndBase64Encode(password.getBytes()).toCharArray())
                        .privateKeyAlias(pvtKeyAlias)
                        .privateKeyPass(cryptoUtil.encryptAndBase64Encode(pvtKeyPass.getBytes()).toCharArray())
                        .content(content)
                        .build();
            }

            keyStoreDAO.addKeyStore(tenantUUID, data);
        } catch (KeyStoreManagementException | CryptoException | IOException | NoSuchAlgorithmException |
                 CertificateException | UnrecoverableKeyException | KeyStoreException e) {
            String msg = "Error when adding a keyStore";
            log.error(msg, e);
            throw new KeyStoreManagementException(msg, e);
        }
    }

    /**
     * Method to add trust store when trust store file data is given.
     *
     * @param fileData File data of the trust store.
     * @param filename Name of the trust store.
     * @param password Password of the trust store.
     * @param provider Provider of the trust store.
     * @param type     Type of the trust store.
     * @throws KeyStoreManagementException Throws if an error occurred while adding the trust store.
     */
    public void addTrustStore(String fileData, String filename, String password, String provider,
                              String type) throws KeyStoreManagementException {

        byte[] content = Base64.decode(fileData);
        addTrustStore(content, filename, password, provider, type);
    }

    /**
     * Method to add trust store when trust store file path is given for the file data.
     *
     * @param content  Byte array of the trust store data.
     * @param filename Name of the trust store.
     * @param password Password of the trust store.
     * @param provider Provider of the trust store.
     * @param type     Type of the trust store.
     * @throws KeyStoreManagementException Throws if an error occurred while adding the trust store.
     */
    public void addTrustStore(byte[] content, String filename, String password, String provider, String type)
            throws KeyStoreManagementException {

        if (filename == null) {
            throw new KeyStoreManagementException("Key Store name can't be null");
        }
        try {
            if (KeyStoreUtil.isPrimaryStore(filename)) {
                throw new KeyStoreManagementException("Key store " + filename + " already available");
            }

            if (isExistKeyStore(filename)) {
                throw new KeyStoreManagementException("Key store " + filename + " already available");
            }

            KeyStore keyStore = KeyStore.getInstance(type);
            keyStore.load(new ByteArrayInputStream(content), password.toCharArray());
            CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();

            KeyStoreModel data = new KeyStoreModel.KeyStoreModelBuilder()
                    .fileName(filename)
                    .type(type)
                    .provider(provider)
                    .password(cryptoUtil.encryptAndBase64Encode(password.getBytes()).toCharArray())
                    .content(content)
                    .build();
            keyStoreDAO.addKeyStore(tenantUUID, data);
        } catch (KeyStoreManagementException e) {
            throw e;
        } catch (Exception e) {
            String msg = "Error when adding a trustStore";
            log.error(msg, e);
            throw new KeyStoreManagementException(msg, e);
        }
    }

    /**
     * Method to delete a key store when a store data file name is given.
     *
     * @param keyStoreName Name of the key store.
     * @throws KeyStoreManagementException Throws if an error occurred while adding the trust store.
     */
    public void deleteStore(String keyStoreName) throws KeyStoreManagementException {

        try {

            if (StringUtils.isBlank(keyStoreName)) {
                throw new KeyStoreManagementException("Key Store name can't be null");
            }

            if (KeyStoreUtil.isPrimaryStore(keyStoreName)) {
                throw new KeyStoreManagementException("Not allowed to delete the primary key store : "
                        + keyStoreName);
            }
            if (isTrustStore(keyStoreName)) {
                throw new KeyStoreManagementException("Not allowed to delete the trust store : "
                        + keyStoreName);
            }

            // TODO: verify that this behaves as expected
            if (keyStoreDAO.getPubCertIdFromKeyStore(tenantUUID, keyStoreName).isPresent()) {
                throw new KeyStoreManagementException("Key store : " + keyStoreName +
                        " is already in use and can't be deleted");
            }

            keyStoreDAO.deleteKeyStore(tenantUUID, keyStoreName);
        } catch (KeyStoreManagementException e) {
            // Catch KeyStoreManagementException and throw the expected KeyStoreManagementException.
            String msg = "Error when deleting a keyStore";
            log.error(msg, e);
            throw new KeyStoreManagementException(msg, e);
        }
    }

    /**
     * Method to import a public certificate to the keystore.
     *
     * @param fileName     Name of the certificate.
     * @param certData     Certificate data.
     * @param keyStoreName Name of the keystore.
     * @throws KeyStoreManagementException Throws if an error occurred while importing the public certificate to the
     *                                 trust store.
     */
    public void importCertToStore(String fileName, String certData, String keyStoreName)
            throws KeyStoreManagementException {

        try {
            if (keyStoreName == null) {
                throw new KeyStoreManagementException("Key Store name can't be null");
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
                System.setProperty(PROP_TRUST_STORE_UPDATE_REQUIRED, "true");
            }

        } catch (KeyStoreManagementException e) {
            throw e;
        } catch (Exception e) {
            String msg = "Error when importing cert to the keyStore";
            log.error(msg, e);
            throw new KeyStoreManagementException(msg, e);
        }

    }

    /**
     * Method to import a public certificate to the keystore when a public certificate filename is not given.
     *
     * @param certData     Certificate data.
     * @param keyStoreName Name of the keystore.
     * @return Returns the alias or the filename of the certificate.
     * @throws KeyStoreManagementException Throws if an error occurred while importing the public certificate to the
     *                                 trust store.
     */
    public String importCertToStore(String certData, String keyStoreName)
            throws KeyStoreManagementException {

        String alias = null;

        try {
            if (keyStoreName == null) {
                throw new KeyStoreManagementException("Key Store name can't be null");
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
                System.setProperty(PROP_TRUST_STORE_UPDATE_REQUIRED, "true");
            }

            return alias;

        } catch (KeyStoreManagementException e) {
            throw e;
        } catch (Exception e) {
            String msg = "Error when importing cert to keyStore";
            log.error(msg, e);
            throw new KeyStoreManagementException(msg);
        }
    }

    /**
     * Remove public certificate from store.
     *
     * @param alias        Alias of the certificate.
     * @param keyStoreName Name of the keystore.
     * @throws KeyStoreManagementException Throws if an error occurred while removing the public certificate from the
     *                                 trust store.
     */
    public void removeCertFromStore(String alias, String keyStoreName)
            throws KeyStoreManagementException {

        try {
            if (keyStoreName == null) {
                throw new KeyStoreManagementException("Key Store name can't be null");
            }

            KeyStore ks = getKeyStore(keyStoreName);

            if (ks.getCertificate(alias) == null) {
                return;
            }

            ks.deleteEntry(alias);
            updateKeyStore(keyStoreName, ks);

            if (isTrustStore(keyStoreName)) {
                System.setProperty(PROP_TRUST_STORE_UPDATE_REQUIRED, Boolean.TRUE.toString());
            }
        } catch (KeyStoreManagementException e) {
            throw e;
        } catch (Exception e) {
            String msg = "Error when removing cert from store";
            log.error(msg, e);
            throw new KeyStoreManagementException(msg);
        }
    }

    /**
     * Retrieve list of alias entries of a given keystore.
     *
     * @param keyStoreName Name of the keystore.
     * @return Returns the list of alias entries of a given keystore.
     * @throws KeyStoreManagementException Throws if an error occurred while getting the store entries.
     */
    public String[] getStoreEntries(String keyStoreName) throws KeyStoreManagementException {

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
        } catch (KeyStoreManagementException e) {
            throw e;
        } catch (Exception e) {
            String msg = "Error when getting store entries";
            log.error(msg, e);
            throw new KeyStoreManagementException(msg);
        }

        return names;
    }

    /**
     * This method will list 1. Certificate aliases 2. Private key alise 3. Private key value to a
     * given keystore.
     *
     * @param keyStoreName The name of the keystore
     * @return Instance of KeyStoreData.
     * @throws KeyStoreManagementException will be thrown if an error occurs while getting the keystore.
     */
    public KeyStoreData getKeystoreInfo(String keyStoreName) throws KeyStoreManagementException {

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
                if (!isExistKeyStore(keyStoreName)) {
                    throw new KeyStoreManagementException("Key Store not found");
                }
                KeyStoreModel resource = keyStoreDAO.getKeyStore(tenantUUID, keyStoreName).get();
                keyStore = getKeyStore(keyStoreName);
                keyStoreType = resource.getType();

                String encryptionPassphrase = String.valueOf(resource.getPrivateKeyPass());
                // todo: check the actual value is empty or null
                if (encryptionPassphrase != null || encryptionPassphrase.isEmpty()) {
                    CryptoUtil util = CryptoUtil.getDefaultCryptoUtil();
                    privateKeyPassword = new String(util.base64DecodeAndDecrypt(encryptionPassphrase));
                }
            }
            // Fill the information about the certificates
            Enumeration<String> aliases = keyStore.aliases();
            List<CertData> certDataList = new ArrayList<>();
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
            throw new KeyStoreManagementException(msg);
        }

    }

    /**
     * Retrieve private key of a given alias.
     *
     * @param alias         Alias of the key.
     * @param isSuperTenant Indication whether the querying super tenant data.
     * @return Returns the private key of a given alias.
     * @throws KeyStoreManagementException Throws if an error occurred while getting the private key.
     */
    public Key getPrivateKey(String alias, boolean isSuperTenant) throws KeyStoreManagementException {

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
            throw new KeyStoreManagementException(msg);
        }
        return null;
    }

    /**
     * Fill certificate data.
     *
     * @param cert      Certificate.
     * @param alise     Certificate alias.
     * @param formatter Formatter.
     * @return Filled certificate data.
     * @throws CertificateEncodingException Certificate encoding exception.
     */
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

    /**
     * Read store data using the given store name.
     *
     * @param filePath File path of the keystore.
     * @return Returns the keystore data as bytes stream.
     * @throws IOException Throws if an error occurred while reading the keystore.
     */
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
     * This method is used to generate the file name of the pub. cert of a tenant.
     *
     * @param ksLocation keystore location in the registry.
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
     * @param pageNumber  Page Number.
     * @param certDataSet Set of keyStoreData.
     * @return PaginatedPolicySetDTO object containing the number of pages and the set of policies
     * that reside in the given page.
     */
    private PaginatedCertData doPaging(int pageNumber, CertData[] certDataSet) {

        PaginatedCertData paginatedCertData = new PaginatedCertData();
        if (certDataSet.length == 0) {
            paginatedCertData.setCertDataSet(new CertData[0]);
            return paginatedCertData;
        }
        int itemsPerPageInt = ITEMS_PER_PAGE;
        int numberOfPages = (int) Math.ceil((double) certDataSet.length / itemsPerPageInt);
        if (pageNumber > numberOfPages - 1) {
            pageNumber = numberOfPages - 1;
        }
        int startIndex = pageNumber * itemsPerPageInt;
        int endIndex = certDataSet.length;
        if (numberOfPages > CACHING_PAGE_SIZE) {
            endIndex = (pageNumber + CACHING_PAGE_SIZE) * itemsPerPageInt;
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
     * @param keyStoreName The name of the keystore.
     * @param pageNumber   Page number.
     * @return Instance of KeyStoreData.
     * @throws KeyStoreManagementException If an error occurs while getting the keystore this exception will be thrown.
     */
    public PaginatedKeyStoreData getPaginatedKeystoreInfo(String keyStoreName, int pageNumber)
            throws KeyStoreManagementException {

        if (StringUtils.isEmpty(keyStoreName)) {
            throw new KeyStoreManagementException("Keystore name cannot be empty or null.");
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
            throw new KeyStoreManagementException(e.getMessage());
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
     * @throws KeyStoreManagementException will be thrown.
     */
    public PaginatedKeyStoreData getFilteredPaginatedKeyStoreInfo(String keyStoreName, int pageNumber,
                                                                  String filter) throws KeyStoreManagementException {

        if (StringUtils.isEmpty(keyStoreName)) {
            throw new KeyStoreManagementException("Keystore name cannot be empty or null.");
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
            throw new KeyStoreManagementException(e.getMessage());
        }
    }

    /**
     * Retrieves key store for a given keystore name of a tenant.
     *
     * @param tenantId     Tenant Id.
     * @param keyStoreName Keystore Name.
     * @return KeyStore.
     * @throws Exception This will be thrown if an error occurs while retrieving the keystore.
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
     * @return Keystore type.
     * @throws KeyStoreManagementException If an error occurs while retrieving the keystore.
     */
    private String getKeyStoreType(String keyStoreName) throws KeyStoreManagementException {

        String keyStoreType;
        if (KeyStoreUtil.isPrimaryStore(keyStoreName)) {
            ServerConfiguration serverConfig = ServerConfiguration.getInstance();
            keyStoreType = serverConfig
                    .getFirstProperty(RegistryResources.SecurityManagement.SERVER_PRIMARY_KEYSTORE_TYPE);
        } else if (isTrustStore(keyStoreName)) {
            ServerConfiguration serverConfig = ServerConfiguration.getInstance();
            keyStoreType = serverConfig.getFirstProperty(SERVER_TRUSTSTORE_TYPE);
        } else {
            if (!isExistKeyStore(keyStoreName)) {
                throw new KeyStoreManagementException("Keystore " + keyStoreName + " not found.");
            }
            KeyStoreModel keyStoreModel = keyStoreDAO.getKeyStore(tenantUUID, keyStoreName).get();
            keyStoreType = keyStoreModel.getType();
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
     * @throws KeyStoreException            If an error occurs while retrieving the keystore.
     * @throws CertificateEncodingException If an error occurs while encoding the certificate.
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
     * @throws KeyStoreException            If an error occurs while retrieving the keystore.
     * @throws CertificateEncodingException If an error occurs while encoding the certificate.
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
     * @throws KeyStoreManagementException if retrieving the truststore fails.
     */
    public KeyStore getTrustStore() throws KeyStoreManagementException {

        //Allow only the super tenant to access the default trust store.
        if (tenantId != MultitenantConstants.SUPER_TENANT_ID) {
            throw new KeyStoreManagementException("Permission denied for accessing trust store");
        }

        KeyStore trustStore;
        ServerConfiguration serverConfiguration = ServerConfiguration.getInstance();
        String file = new File(serverConfiguration.getFirstProperty(SERVER_TRUSTSTORE_FILE)).getAbsolutePath();

        KeyStore store;
        try {
            store = KeyStore.getInstance(serverConfiguration.getFirstProperty(SERVER_TRUSTSTORE_TYPE));
        } catch (KeyStoreException e) {
            throw new KeyStoreManagementException("Error occurred while loading keystore.", e);
        }

        String password = serverConfiguration.getFirstProperty(SERVER_TRUSTSTORE_PASSWORD);

        try (FileInputStream in = new FileInputStream(file)) {
            store.load(in, password.toCharArray());
            trustStore = store;
        } catch (CertificateException | NoSuchAlgorithmException | IOException e) {
            throw new KeyStoreManagementException("Error occurred while loading trust store", e);
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

    /**
     * Updates the key store.
     *
     * @param name     Name of the key store.
     * @param keyStore KeyStore object.
     * @throws Exception If an error occurred while updating the key store.
     */
    private void updateKeyStore(String name, KeyStore keyStore) throws Exception {

        FileOutputStream resource1;
        String outputStream1;
        String path;
        if (isTrustStore(name)) {
            path = (new File(trustStoreLocation)).getAbsolutePath();
            resource1 = null;

            try {
                resource1 = new FileOutputStream(path);
                outputStream1 = trustStorePassword;
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
     * @throws KeyStoreManagementException if extracting the certificate fails.
     */
    public X509Certificate extractCertificate(String certData) throws KeyStoreManagementException {

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
            throw new KeyStoreManagementException("Invalid format of the provided certificate file");
        }
        return cert;
    }

    /**
     * Returns whether the key store exists or not.
     *
     * @param fileName Name of the key store.
     * @return True if the key store exists.
     * @throws KeyStoreManagementException If an error occurred while checking the existence of the key store.
     */
    private boolean isExistKeyStore(String fileName) throws KeyStoreManagementException {

        return keyStoreDAO.getKeyStore(tenantUUID, fileName).isPresent();
    }
}
