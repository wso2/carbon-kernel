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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.Base64;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreConfigConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.internal.UserStoreMgtDSComponent;
import org.wso2.carbon.user.core.tracker.UserStoreManagerRegistry;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import javax.crypto.Cipher;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.security.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

public class UserStoreConfigXMLProcessor {

    private static final Log log = LogFactory.getLog(UserStoreConfigXMLProcessor.class);
    private SecretResolver secretResolver;
    private String filePath = null;
    private static BundleContext bundleContext;

    private static Cipher keyStoreCipher = null;
    public UserStoreConfigXMLProcessor(String path) {
        this.filePath = path;
        initializeKeyStore();
    }

    public static void setBundleContext(BundleContext bundleContext) {
        UserStoreConfigXMLProcessor.bundleContext = bundleContext;
    }


    public RealmConfiguration buildUserStoreConfigurationFromFile() throws UserStoreException {
        OMElement realmElement;
        try {
            realmElement = getRealmElement();
            return buildUserStoreConfiguration(realmElement);
        } catch (Exception e) {
            String message = "Error while building user store manager from file";
            log.error(message, e);
            throw new UserStoreException(message, e);
        }

    }

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

        if(!xmlProcessorUtils.isMandatoryFieldsProvided(userStoreProperties, UserStoreManagerRegistry.getUserStoreProperties(userStoreClass).getMandatoryProperties())){
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

    private Map<String, String> getChildPropertyElements(OMElement omElement,
                                                         SecretResolver secretResolver) {
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
                if (secretResolver.isTokenProtected("UserManager.Configuration.Property."
                        + propName + domainName)) {
                    propValue = secretResolver.resolve("UserManager.Configuration.Property."
                            + propName + domainName);
                }
                if (secretResolver.isTokenProtected("UserStoreManager.Property." + propName + domainName)) {
                    propValue = secretResolver.resolve("UserStoreManager.Property." + propName + domainName);
                    tokenProtected = true;
                }
            }
            if(!tokenProtected && propValue != null){
                propValue = resolveEncryption(propElem);
            }
            tokenProtected = false;
            if(propName != null &&  propValue!= null ){
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

    /**
     * Read in realm element from config file
     *
     * @return
     * @throws javax.xml.stream.XMLStreamException
     *
     * @throws java.io.IOException
     * @throws org.wso2.carbon.user.core.UserStoreException
     *
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
            throw new UserStoreException(e.getMessage(), e);
        } finally {
            if(inStream != null){
                try{
                    inStream.close();
                }catch (IOException ex){
                    String errMsg = "File close operation failed " + filePath;
                    log.error(errMsg, ex);
                }
            }
        }
    }

    public void setSecretResolver(OMElement rootElement) {
        secretResolver = SecretResolverFactory.create(rootElement, true);
    }

    /**
     * decrypts encrypted text value if the property element has the attribute encrypt="true"
     *
     * @param propElem Property OMElement
     * @return         decrypted text value
     */
    private String resolveEncryption(OMElement propElem) {
        String propValue = propElem.getText();
        if (propValue != null) {
            String secretPropName = propElem.getAttributeValue(new QName("encrypted"));
            if (secretPropName != null && secretPropName.equalsIgnoreCase("true")) {
                if (log.isDebugEnabled()) {
                    log.debug("Eligible to be decrypted=" + propElem.getAttributeValue(new QName(
                            UserCoreConstants.RealmConfig.ATTR_NAME_PROP_NAME)));
                }
                try {
                    propValue = new String(keyStoreCipher.doFinal(Base64.
                            decode(propValue.trim())));
                } catch (GeneralSecurityException e) {
                    String errMsg = "encryption of Property=" + propElem.getAttributeValue(
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
    private void initializeKeyStore() {
        ServerConfigurationService serverConfigurationService =
                UserStoreMgtDSComponent.getServerConfigurationService();

        if (serverConfigurationService == null) {
            String message = "Key store initialization for decrypting secondary store failed due to" +
                    " serverConfigurationService is null while attempting to decrypt secondary store";
            log.error(message);
            return;
        }

        if (keyStoreCipher == null) {

            String password = serverConfigurationService.getFirstProperty(
                    "Security.KeyStore.Password");
            String keyPass = serverConfigurationService.getFirstProperty(
                    "Security.KeyStore.KeyPassword");
            String keyAlias = serverConfigurationService.getFirstProperty(
                    "Security.KeyStore.KeyAlias");
            InputStream in = null;
            try {
                KeyStore store = KeyStore.getInstance(
                        serverConfigurationService.getFirstProperty(
                                "Security.KeyStore.Type"));
                String file = new File(serverConfigurationService.getFirstProperty(
                        "Security.KeyStore.Location")).getAbsolutePath();
                in = new FileInputStream(file);
                store.load(in, password.toCharArray());
                PrivateKey privateKey = (PrivateKey) store.getKey(keyAlias, keyPass.toCharArray());
                keyStoreCipher = Cipher.getInstance("RSA", "BC");
                keyStoreCipher.init(Cipher.DECRYPT_MODE, privateKey);
            } catch (FileNotFoundException e) {
                String errorMsg = "Keystore File Not Found in configured location";
                log.error(errorMsg, e);
            } catch (IOException e) {
                String errorMsg = "Keystore File IO operation failed";
                log.error(errorMsg, e);
            } catch (InvalidKeyException e) {
                String errorMsg = "Invalid key is used to access keystore";
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
        }
    }
}
