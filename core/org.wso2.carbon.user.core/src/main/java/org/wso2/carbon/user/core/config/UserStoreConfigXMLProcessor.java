/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.user.core.config;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.Base64;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.crypto.api.CipherMetaDataHolder;
import org.wso2.carbon.crypto.api.CryptoException;
import org.wso2.carbon.crypto.api.CryptoService;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreConfigConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.internal.UserStoreMgtDSComponent;
import org.wso2.carbon.user.core.internal.UserStoreMgtDataHolder;
import org.wso2.carbon.user.core.tracker.UserStoreManagerRegistry;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;
import org.wso2.securevault.commons.MiscellaneousUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

public class UserStoreConfigXMLProcessor {

    private static final Log log = LogFactory.getLog(UserStoreConfigXMLProcessor.class);
    private static BundleContext bundleContext;
    private static final String CIPHER_TRANSFORMATION_SYSTEM_PROPERTY = "org.wso2.CipherTransformation";
    private static PrivateKey privateKey = getPrivateKey();
    private SecretResolver secretResolver;
    private String filePath = null;
    private Gson gson = new Gson();
    private static final String CRYPTO_API_PROVIDER_BC = "BC";
    private static final String ENCRYPTION_KEYSTORE = "Security.UserStorePasswordEncryption";
    private static final String INTERNAL_KEYSTORE = "InternalKeystore";
    private static final String CRYPTO_PROVIDER = "CryptoService.InternalCryptoProviderClassName";
    private static final String SYMMETRIC_KEY_CRYPTO_PROVIDER = "org.wso2.carbon.crypto.provider" +
            ".SymmetricKeyInternalCryptoProvider";

    public UserStoreConfigXMLProcessor(String path) {
        this.filePath = path;
    }

    public static void setBundleContext(BundleContext bundleContext) {
        UserStoreConfigXMLProcessor.bundleContext = bundleContext;
    }

    public static OMElement serialize(RealmConfiguration realmConfig) {
        OMFactory factory = OMAbstractFactory.getOMFactory();

        // add the user store manager properties
        OMElement userStoreManagerElement = factory.createOMElement(new QName(
                UserCoreConstants.RealmConfig.LOCAL_NAME_USER_STORE_MANAGER));
        addPropertyElements(factory, userStoreManagerElement, realmConfig.getUserStoreClass(), realmConfig.getUserStoreProperties());

        return userStoreManagerElement;
    }

    /**
     * Add all the user store property elements
     *
     * @param factory
     * @param parent
     * @param className
     * @param properties
     */
    private static void addPropertyElements(OMFactory factory, OMElement parent, String className,
                                            Map<String, String> properties) {
        if (className != null) {
            parent.addAttribute(UserCoreConstants.RealmConfig.ATTR_NAME_CLASS, className, null);
        }
        Iterator<Map.Entry<String, String>> ite = properties.entrySet().iterator();
        while (ite.hasNext()) {
            Map.Entry<String, String> entry = ite.next();
            String name = entry.getKey();
            String value = entry.getValue();
            OMElement propElem = factory.createOMElement(new QName(
                    UserCoreConstants.RealmConfig.LOCAL_NAME_PROPERTY));
            OMAttribute propAttr = factory.createOMAttribute(
                    UserCoreConstants.RealmConfig.ATTR_NAME_PROP_NAME, null, name);
            propElem.addAttribute(propAttr);
            propElem.setText(value);
            parent.addChild(propElem);
        }
    }

    public RealmConfiguration buildUserStoreConfigurationFromFile() throws UserStoreException {
        OMElement realmElement;
        try {
            realmElement = getRealmElement();
            return buildUserStoreConfiguration(realmElement);
        } catch (Exception e) {
            String message = "Error while building user store manager from file";
            if (log.isDebugEnabled()) {
                log.debug(message, e);
            }
            throw new UserStoreException(message, e);
        }

    }

    /**
     * To build an Userstore Realm Configuration from a given Userstore Element.
     *
     * @param userStoreElement  Userstore element.
     * @return RealmConfiguration for successful build or null if any required userstore property is missing in
     * the UserStoreManagerRegistry.
     */
    public RealmConfiguration buildUserStoreConfiguration(OMElement userStoreElement) throws org.wso2.carbon.user.api.UserStoreException {

        RealmConfiguration realmConfig = null;
        String userStoreClass = null;
        Map<String, String> userStoreProperties = null;
        boolean passwordsExternallyManaged = false;
        XMLProcessorUtils xmlProcessorUtils = new XMLProcessorUtils();

        realmConfig = new RealmConfiguration();
//        String[] fileNames = filePath.split(File.separator);
        String pattern = Pattern.quote(System.getProperty("file.separator"));
        String[] fileNames = filePath.split(pattern);
        String fileName = fileNames[fileNames.length - 1].replace(".xml", "").replace("_", ".");
        RealmConfiguration primaryRealm = UserStoreMgtDSComponent.getRealmService().getBootstrapRealmConfiguration();
        userStoreClass = userStoreElement.getAttributeValue(new QName(UserCoreConstants.RealmConfig.ATTR_NAME_CLASS));
        userStoreProperties = getChildPropertyElements(userStoreElement, secretResolver);

        if (!userStoreProperties.get(UserStoreConfigConstants.DOMAIN_NAME).equalsIgnoreCase(fileName)) {
            throw new UserStoreException("File name is required to be the user store domain name(eg.: wso2.com-->wso2_com.xml).");
        }

//        if(!xmlProcessorUtils.isValidDomain(fileName,true)){
//            throw new UserStoreException("Invalid domain name provided");
//        }

        if (UserStoreManagerRegistry.getUserStoreProperties(userStoreClass) == null) {
            return null;
        }
        if (!xmlProcessorUtils.isMandatoryFieldsProvided(userStoreProperties, UserStoreManagerRegistry.getUserStoreProperties(userStoreClass).getMandatoryProperties())) {
            throw new UserStoreException("A required mandatory field is missing.");
        }

        String sIsPasswordExternallyManaged = userStoreProperties
                .get(UserCoreConstants.RealmConfig.LOCAL_PASSWORDS_EXTERNALLY_MANAGED);

        if (null != sIsPasswordExternallyManaged
                && !sIsPasswordExternallyManaged.trim().equals("")) {
            passwordsExternallyManaged = Boolean.parseBoolean(sIsPasswordExternallyManaged);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("External password management is disabled.");
            }
        }

        Map<String, String> multipleCredentialsProperties = getMultipleCredentialsProperties(userStoreElement);

        realmConfig.setUserStoreClass(userStoreClass);
        realmConfig.setAuthorizationManagerClass(primaryRealm.getAuthorizationManagerClass());
        realmConfig.setEveryOneRoleName(UserCoreUtil.addDomainToName(primaryRealm.getEveryOneRoleName(),
                UserCoreConstants.INTERNAL_DOMAIN));
        realmConfig.setUserStoreProperties(userStoreProperties);
        realmConfig.setPasswordsExternallyManaged(passwordsExternallyManaged);
        realmConfig.setAuthzProperties(primaryRealm.getAuthzProperties());
        realmConfig.setRealmProperties(primaryRealm.getRealmProperties());
        realmConfig.setPasswordsExternallyManaged(primaryRealm.isPasswordsExternallyManaged());
        realmConfig.addMultipleCredentialProperties(userStoreClass, multipleCredentialsProperties);

        if (realmConfig
                .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_MAX_USER_LIST) == null) {
            realmConfig.getUserStoreProperties().put(
                    UserCoreConstants.RealmConfig.PROPERTY_MAX_USER_LIST,
                    UserCoreConstants.RealmConfig.PROPERTY_VALUE_DEFAULT_MAX_COUNT);
        }

        if (realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_READ_ONLY) == null) {
            realmConfig.getUserStoreProperties().put(
                    UserCoreConstants.RealmConfig.PROPERTY_READ_ONLY,
                    UserCoreConstants.RealmConfig.PROPERTY_VALUE_DEFAULT_READ_ONLY);
        }

        return realmConfig;
    }

    private Map<String, String> getChildPropertyElements(OMElement omElement, SecretResolver secretResolver)
            throws org.wso2.carbon.user.api.UserStoreException {
        String domainName = "";
        try {
            AXIOMXPath xPath = new AXIOMXPath(UserCoreConstants.RealmConfig.DOMAIN_NAME_XPATH);
            OMElement val = (OMElement) xPath.selectSingleNode(omElement);
            if (val != null) {
                domainName = "." + val.getText();
            }
        } catch (Exception e) {
            log.debug("Error While getting DomainName from Configurations ");
        }

        Map<String, String> map = new HashMap<String, String>();
        Iterator<?> ite = omElement.getChildrenWithName(new QName(
                UserCoreConstants.RealmConfig.LOCAL_NAME_PROPERTY));
        boolean tokenProtected = false;
        while (ite.hasNext()) {
            OMElement propElem = (OMElement) ite.next();
            String propName = propElem.getAttributeValue(new QName(
                    UserCoreConstants.RealmConfig.ATTR_NAME_PROP_NAME));
            String propValue = propElem.getText();
            if (secretResolver != null && secretResolver.isInitialized()) {
                String alias = MiscellaneousUtil.getProtectedToken(propValue);
                if (StringUtils.isNotEmpty(alias) && secretResolver.isTokenProtected(alias)) {
                    propValue = secretResolver.resolve(alias);
                    tokenProtected = true;
                } else {
                    if (secretResolver.isTokenProtected("UserManager.Configuration.Property."
                            + propName + domainName)) {
                        propValue = secretResolver.resolve("UserManager.Configuration.Property."
                                + propName + domainName);
                        tokenProtected = true;
                    }
                    if (secretResolver.isTokenProtected("UserStoreManager.Property." + propName + domainName)) {
                        propValue = secretResolver.resolve("UserStoreManager.Property." + propName + domainName);
                        tokenProtected = true;
                    }
                }

            }
            if (!tokenProtected && propValue != null) {
                propValue = resolveEncryption(propElem);
            }
            tokenProtected = false;
            if (propName != null && propValue != null) {
                map.put(propName.trim(), propValue.trim());
            }

        }
        return map;
    }

    private Map<String, String> getMultipleCredentialsProperties(OMElement omElement) {
        Map<String, String> map = new HashMap<String, String>();
        OMElement multipleCredentialsEl = omElement.getFirstChildWithName(new QName(
                UserCoreConstants.RealmConfig.LOCAL_NAME_MULTIPLE_CREDENTIALS));
        if (multipleCredentialsEl != null) {
            Iterator<?> ite = multipleCredentialsEl
                    .getChildrenWithLocalName(UserCoreConstants.RealmConfig.LOCAL_NAME_CREDENTIAL);
            while (ite.hasNext()) {

                Object OMObj = ite.next();
                if (!(OMObj instanceof OMElement)) {
                    continue;
                }
                OMElement credsElem = (OMElement) OMObj;
                String credsType = credsElem.getAttributeValue(new QName(
                        UserCoreConstants.RealmConfig.ATTR_NAME_TYPE));
                String credsClassName = credsElem.getText();
                map.put(credsType.trim(), credsClassName.trim());
            }
        }
        return map;
    }

    /**
     * Read in realm element from config file
     *
     * @return
     * @throws javax.xml.stream.XMLStreamException
     * @throws java.io.IOException
     * @throws org.wso2.carbon.user.core.UserStoreException
     */
    private OMElement getRealmElement() throws XMLStreamException, IOException, UserStoreException {
        StAXOMBuilder builder = null;
        InputStream inStream = null;
        inStream = new FileInputStream(filePath);

        try {
            inStream = CarbonUtils.replaceSystemVariablesInXml(inStream);
            builder = new StAXOMBuilder(inStream);
            OMElement documentElement = builder.getDocumentElement();
            setSecretResolver(documentElement);

            return documentElement;
        } catch (CarbonException e) {
            if (log.isDebugEnabled()) {
                log.debug(e.getMessage(), e);
            }
            throw new UserStoreException(e.getMessage(), e);
        } finally {
            inStream.close();
        }
    }

    public void setSecretResolver(OMElement rootElement) {
        secretResolver = SecretResolverFactory.create(rootElement, true);
    }

    /**
     * decrypts encrypted text value if the property element has the attribute encrypt="true"
     *
     * @param propElem Property OMElement
     * @return decrypted text value
     */
    private String resolveEncryption(OMElement propElem) throws org.wso2.carbon.user.api.UserStoreException {
        String propValue = propElem.getText();
        if (propValue != null) {
            String secretPropName = propElem.getAttributeValue(new QName("encrypted"));
            if (secretPropName != null && secretPropName.equalsIgnoreCase("true")) {
                if (log.isDebugEnabled()) {
                    log.debug("Eligible to be decrypted=" + propElem.getAttributeValue(new QName(
                            UserCoreConstants.RealmConfig.ATTR_NAME_PROP_NAME)));
                }
                try {
                    propValue = decryptProperty(propValue);
                } catch (CryptoException e) {
                    String errMsg = "decryption of Property=" + propElem.getAttributeValue(
                            new QName(UserCoreConstants.RealmConfig.ATTR_NAME_PROP_NAME))
                            + " failed";
                    log.error(errMsg, e);
                }
            }
        }
        return propValue;
    }

    /**
     * Initializes and assign the keyStoreCipher only for the first time.
     */
    private static PrivateKey getPrivateKey() {
        ServerConfigurationService serverConfigurationService =
                UserStoreMgtDSComponent.getServerConfigurationService();

        if (serverConfigurationService == null) {
            String message = "Key store initialization for decrypting secondary store failed due to" +
                    " serverConfigurationService is null while attempting to decrypt secondary store";
            log.error(message);
            return null;
        }

        InputStream in = null;

        String passwordXPath = "Security.KeyStore.Password";
        String keypassXPath = "Security.KeyStore.KeyPassword";
        String keyAliasXPath = "Security.KeyStore.KeyAlias";
        String locationXPath = "Security.KeyStore.Location";
        String typeXPath = "Security.KeyStore.Type";

        String password = serverConfigurationService.getFirstProperty(passwordXPath);
        String keyPass = serverConfigurationService.getFirstProperty(keypassXPath);
        String keyAlias = serverConfigurationService.getFirstProperty(keyAliasXPath);

        try {
            KeyStore store = KeyStore.getInstance(serverConfigurationService.getFirstProperty(typeXPath));
            String file = new File(serverConfigurationService.getFirstProperty(locationXPath)).getAbsolutePath();
            in = new FileInputStream(file);
            store.load(in, password.toCharArray());
            return (PrivateKey) store.getKey(keyAlias, keyPass.toCharArray());
        } catch (FileNotFoundException e) {
            String errorMsg = "Keystore File Not Found in configured location";
            log.error(errorMsg, e);
        } catch (IOException e) {
            String errorMsg = "Keystore File IO operation failed";
            log.error(errorMsg, e);
        } catch (KeyStoreException e) {
            String errorMsg = "Faulty keystore";
            log.error(errorMsg, e);
        } catch (GeneralSecurityException e) {
            String errorMsg = "Some parameters assigned to access the " +
                    "keystore is invalid";
            log.error(errorMsg, e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.error("Error occurred while closing Registry key store file", e);
                }
            }
        }
        return null;
    }

    /**
     * Function to decrypt given cipher text
     *
     * @param propValue base64encoded ciphertext
     * @return plaintext
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws org.wso2.carbon.user.api.UserStoreException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    private String decryptProperty(String propValue) throws CryptoException {

        CryptoService cryptoService = UserStoreMgtDataHolder.getInstance().getCryptoService();
        String cipherTransformation = System.getProperty(CIPHER_TRANSFORMATION_SYSTEM_PROPERTY);
        byte[] cipherTextBytes = Base64.decode(propValue.trim());
        String algorithm = null;
        byte[] decryptedValue;
        boolean isInternalKeyStoreEncryptionEnabled = false;
        boolean isSymmetricKeyEncryptionEnabled = false;
        ServerConfigurationService config =
                UserStoreMgtDSComponent.getServerConfigurationService();
        if (config != null) {
            String encryptionKeyStore = config.getFirstProperty(ENCRYPTION_KEYSTORE);

            if (INTERNAL_KEYSTORE.equalsIgnoreCase(encryptionKeyStore)) {
                isInternalKeyStoreEncryptionEnabled = true;
            }
            String cryptoProvider = config.getFirstProperty(CRYPTO_PROVIDER);
            if (SYMMETRIC_KEY_CRYPTO_PROVIDER.equalsIgnoreCase(cryptoProvider)) {
                isSymmetricKeyEncryptionEnabled = true;
            }
        }

        if (isInternalKeyStoreEncryptionEnabled && isSymmetricKeyEncryptionEnabled) {

            throw new CryptoException(String.format("Userstore encryption can not be supported due to " +
                    "conflicting configurations: '%s' and '%s'. When using internal keystore, assymetric crypto " +
                    "provider should be used.", INTERNAL_KEYSTORE, SYMMETRIC_KEY_CRYPTO_PROVIDER));
        } else if (isInternalKeyStoreEncryptionEnabled || isSymmetricKeyEncryptionEnabled) {

            if (cipherTransformation != null) {
                // extract the original cipher if custom transformation is used configured in carbon.properties.
                CipherMetaDataHolder cipherHolder = cipherTextToCipherHolder(cipherTextBytes);
                if (cipherHolder != null) {
                    // cipher with meta data.
                    if (log.isDebugEnabled()) {
                        log.debug("Cipher transformation for decryption : " + cipherHolder.getTransformation());
                    }
                    algorithm = cipherHolder.getTransformation();
                    cipherTextBytes = cipherHolder.getCipherBase64Decoded();
                } else {
                    // If the ciphertext is not a self-contained, directly decrypt using transformation configured in
                    // carbon.properties file
                    algorithm = cipherTransformation;
                }
            }
            if (cipherTextBytes.length == 0) {
                decryptedValue = StringUtils.EMPTY.getBytes();
                if (log.isDebugEnabled()) {
                    log.debug("Ciphertext is empty. An empty array will be used as the plaintext bytes.");
                }
            } else {
                decryptedValue = cryptoService.decrypt(cipherTextBytes, algorithm, CRYPTO_API_PROVIDER_BC);
            }
            return new String(decryptedValue);

        } else {
            return decryptWithPrimaryKeyStore(propValue);

        }

    }

    private String decryptWithPrimaryKeyStore(String propValue) throws CryptoException {

        Cipher keyStoreCipher = null;
        String cipherTransformation = System.getProperty(CIPHER_TRANSFORMATION_SYSTEM_PROPERTY);
        byte[] cipherTextBytes = Base64.decode(propValue.trim());
        byte[] plainTextBytes = new byte[0];

        privateKey = (privateKey == null) ? getPrivateKey() : privateKey;
        if (privateKey == null) {
            throw new CryptoException(
                    "Private key initialization failed. Cannot decrypt the userstore password.");
        }

        try {
            if (cipherTransformation != null) {
                // extract the original cipher if custom transformation is used configured in carbon.properties.
                CipherMetaDataHolder cipherHolder = cipherTextToCipherHolder(cipherTextBytes);
                if (cipherHolder != null) {
                    // cipher with meta data.
                    if (log.isDebugEnabled()) {
                        log.debug("Cipher transformation for decryption : " + cipherHolder.getTransformation());
                    }

                    keyStoreCipher = Cipher.getInstance(cipherHolder.getTransformation(), "BC");

                    cipherTextBytes = cipherHolder.getCipherBase64Decoded();
                } else {
                    // If the ciphertext is not a self-contained, directly decrypt using transformation configured in
                    // carbon.properties file

                    keyStoreCipher = Cipher.getInstance(cipherTransformation, "BC");

                }
            } else {
                // If reach here, user have removed org.wso2.CipherTransformation property or carbon.properties file
                // hence RSA is considered as default transformation
                if (log.isDebugEnabled()) {
                    log.debug("Cipher transformation property is not available.Hence RSA is considered as default " +
                            "cipher transformation.");
                }
                keyStoreCipher = Cipher.getInstance("RSA", "BC");

            }

            keyStoreCipher.init(Cipher.DECRYPT_MODE, privateKey);
            plainTextBytes = keyStoreCipher.doFinal(cipherTextBytes);

        } catch (GeneralSecurityException e) {
            String errMsg = "decryption of secondary userstore property failed.";
            throw new CryptoException(errMsg);
        }
        return new String(plainTextBytes, Charset.defaultCharset());

    }


    /**
     * Function to convert cipher byte array to {@link CipherMetaDataHolder}.
     *
     * @param cipherText cipher text as a byte array
     * @return if cipher text is not a cipher with meta data
     */
    private CipherMetaDataHolder cipherTextToCipherHolder(byte[] cipherText) {

        String cipherStr = new String(cipherText, Charset.defaultCharset());
        try {
            return gson.fromJson(cipherStr, CipherMetaDataHolder.class);
        } catch (JsonSyntaxException e) {
            if (log.isDebugEnabled()) {
                log.debug("Deserialization failed since cipher string is not representing cipher with metadata");
            }
            return null;
        }
    }

}
