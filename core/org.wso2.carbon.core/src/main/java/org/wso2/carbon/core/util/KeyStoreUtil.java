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
package org.wso2.carbon.core.util;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.util.XMLUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.core.RegistryResources;
import org.wso2.carbon.core.internal.CarbonCoreDataHolder;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;
import org.wso2.securevault.commons.MiscellaneousUtil;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;

public class KeyStoreUtil {

    private static final Log log = LogFactory.getLog(KeyStoreUtil.class);

    /**
     * KeyStore name will be here.
     * 
     * @param store - keyStore
     * @return
     */
    public static String getPrivateKeyAlias(KeyStore store) throws Exception {
        String alias = null;
        Enumeration<String> enums = store.aliases();
        while(enums.hasMoreElements()){
            String name = enums.nextElement();
            if(store.isKeyEntry(name)){
                alias = name;
                break;
            }
        }
        return alias;
    }

    public static String getKeyStoreFileName(String fullName) {
        ServerConfigurationService config =
                CarbonCoreDataHolder.getInstance().getServerConfigurationService();
        String fileName = config
                .getFirstProperty(RegistryResources.SecurityManagement.SERVER_PRIMARY_KEYSTORE_FILE);
        String name = null;
        int index = fileName.lastIndexOf('/');
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

    /**
     * Check whether the given key store is the primary store.
     * @param fileName  File name of the key store.
     *
     * @return True if the given key store is the primary store.
     */
    public static boolean isPrimaryStore(String fileName) {

        String keystorePath = CarbonCoreDataHolder.getInstance().getServerConfigurationService()
                .getFirstProperty(RegistryResources.SecurityManagement.SERVER_PRIMARY_KEYSTORE_FILE);
        int index = keystorePath.lastIndexOf('/');
        String keystoreName = index != -1 ? keystorePath.substring(index + 1) : new File(keystorePath).getName();
        return StringUtils.equals(keystoreName, fileName);
    }

    /**
     * Check whether the given key store is a trust store.
     * @param fileName  File name of the key store.
     *
     * @return True if the given key store is a trust store.
     */
    public static boolean isTrustStore(String fileName) {

        String trustStorePath = CarbonCoreDataHolder.getInstance().getServerConfigurationService()
                .getFirstProperty(RegistryResources.SecurityManagement.SERVER_TRUSTSTORE_FILE);
        int index = trustStorePath.lastIndexOf('/');
        String trustStoreName = index != -1 ? trustStorePath.substring(index + 1)
                : new File(trustStorePath).getName();
        return StringUtils.equals(trustStoreName, fileName);
    }

    public static Certificate getCertificate(String alias, KeyStore store) throws AxisFault {
        try {
            Enumeration enumeration = store.aliases();
            while (enumeration.hasMoreElements()) {
                String itemAlias = (String) enumeration.nextElement();
                if (itemAlias.equals(alias)) {
                    return store.getCertificate(alias);
                }
            }
            return null;
        } catch (Exception e) {
            String msg = "Could not read certificates from keystore file. ";
            throw new AxisFault(msg + e.getMessage());
        }
    }

    /**
     * This method checks if the given key store name is for a custom key store by checking the prefix.
     *
     * @param keyStoreName      Custom key store name.
     * @return Boolean value indicating if the given name is for a custom key store.
     */
    public static boolean isCustomKeyStore(String keyStoreName) {

        return keyStoreName.startsWith(RegistryResources.SecurityManagement.CustomKeyStore.CUSTOM_KEYSTORE_PREFIX);
    }

    /**
     * This method builds the QName object for a configuration within the carbon.xml namespace.
     *
     * @param localPart     Local part of the configuration.
     * @return QName object.
     */
    public static QName getQNameWithCarbonNS(String localPart) {

        return new QName(ServerConstants.CARBON_SERVER_XML_NAMESPACE, localPart);
    }

    /**
     * This method returns the OMElement object corresponding to the custom key store configuration in Carbon.xml file.
     *
     * @param keyStoreName          Name of the custom key store.
     * @param serverConfiguration   ServerConfigurationService instance.
     * @return OMElement object for the requested configuration.
     * @throws CarbonException      If failed to retrieve the requested configuration.
     */
    public static OMElement getCustomKeyStoreConfigElement(
            String keyStoreName, ServerConfigurationService serverConfiguration) throws CarbonException {

        // Remove prefix if exists.
        if (KeyStoreUtil.isCustomKeyStore(keyStoreName)) {
            keyStoreName = keyStoreName.substring(
                    RegistryResources.SecurityManagement.CustomKeyStore.CUSTOM_KEYSTORE_PREFIX.length());
        }

        OMElement config = null;
        try {
            OMElement customKeyStoresConfigElem = XMLUtils.toOM(serverConfiguration.getDocumentElement())
                    .getFirstChildWithName(KeyStoreUtil.getQNameWithCarbonNS(
                            RegistryResources.SecurityManagement.CustomKeyStore.ELEM_SECURITY))
                    .getFirstChildWithName(KeyStoreUtil.getQNameWithCarbonNS(
                            RegistryResources.SecurityManagement.CustomKeyStore.ELEM_CUSTOM_KEYSTORES));

            Iterator<OMElement> iterator = customKeyStoresConfigElem.getChildElements();
            while (iterator.hasNext()) {
                OMElement customKeyStoreConfig = iterator.next();
                String location = customKeyStoreConfig.getFirstChildWithName(KeyStoreUtil.getQNameWithCarbonNS(
                        RegistryResources.SecurityManagement.CustomKeyStore.PROP_LOCATION)).getText();

                if (location.endsWith(keyStoreName)) {
                    config = customKeyStoreConfig;
                    break;
                }
            }
        } catch (Exception e) {
            throw new CarbonException("Error occurred while reading custom keystore configuration.", e);
        }

        if (config == null) {
            throw new CarbonException("No configuration found for custom key store : " + keyStoreName);
        }

        return config;
    }

    /**
     * This method returns the requested key store property value from the configuration.
     *
     * @param config                OMElement object of configuration.
     * @param propertyName                  Property name.
     * @return Configuration value as String.
     * @throws CarbonException      If failed to retrieve the requested configuration.
     */
    public static String getCustomKeyStoreConfig(OMElement config, String propertyName) throws CarbonException {

        validateKeyStoreConfigName(propertyName);

        String configValue = config.getFirstChildWithName(getQNameWithCarbonNS(propertyName)).getText();

        if (RegistryResources.SecurityManagement.CustomKeyStore.PROP_LOCATION.equals(propertyName)) {
            // Replace "{$carbon.home}" placeholder with proper location path.
            if (configValue.startsWith(RegistryResources.SecurityManagement.CARBON_HOME_PLACEHOLDER)) {
                configValue = configValue.replace(RegistryResources.SecurityManagement.CARBON_HOME_PLACEHOLDER, "");
                configValue = new File(".").getAbsolutePath() + configValue;
            } else {
                throw new CarbonException("Invalid key store location: " + configValue);
            }
        }
        if (RegistryResources.SecurityManagement.CustomKeyStore.PROP_PASSWORD.equals(propertyName) ||
                RegistryResources.SecurityManagement.CustomKeyStore.PROP_KEY_PASSWORD.equals(propertyName)) {
            // Enable support for cipher tool.
            SecretResolver secretResolver = SecretResolverFactory.create(config, true);
            String resolvedValue = MiscellaneousUtil.resolve(configValue, secretResolver);
            if (resolvedValue != null && !resolvedValue.isEmpty()) {
                configValue = resolvedValue;
            }
        }

        return configValue;
    }

    /**
     * This method validates the requested key store configuration name.
     * Only 'Location', 'Type', 'Password', 'KeyAlias' and 'KeyPassword' are valid key store configuration names.
     *
     * @param propertyName          Requested key store configuration name.
     * @throws CarbonException      If the requested key store configuration is invalid.
     */
    public static void validateKeyStoreConfigName(String propertyName) throws CarbonException {

        String[] keyStoreProperties = {
                RegistryResources.SecurityManagement.CustomKeyStore.PROP_LOCATION,
                RegistryResources.SecurityManagement.CustomKeyStore.PROP_TYPE,
                RegistryResources.SecurityManagement.CustomKeyStore.PROP_PASSWORD,
                RegistryResources.SecurityManagement.CustomKeyStore.PROP_KEY_ALIAS,
                RegistryResources.SecurityManagement.CustomKeyStore.PROP_KEY_PASSWORD
        };

        if (!Arrays.asList(keyStoreProperties).contains(propertyName)) {
            throw new CarbonException("Requested key store configuration is invalid.");
        }
    }

    /**
     * Dumping the generated public cert to a file.
     *
     * @param configurationContext Configuration context of the current message.
     * @param cert                 Content of the certificate.
     * @param fileName             File name.
     * @return file system location of the public cert.
     */
    public static String dumpCert(ConfigurationContext configurationContext, byte[] cert, String fileName) {

        if (!verifyCertExistence(fileName, configurationContext)) {
            String workingDirectory = (String) configurationContext.getProperty(ServerConstants.WORK_DIR);
            File publicCert = new File(workingDirectory + File.separator + ServerConstants.PUBLIC_CERTS_DIRECTORY_NAME);

            if (fileName == null) {
                fileName = System.currentTimeMillis() + new SecureRandom().nextDouble() + ".cert";
            }
            if (!publicCert.exists()) {
                publicCert.mkdirs();
            }

            String filePath = workingDirectory + File.separator + ServerConstants.PUBLIC_CERTS_DIRECTORY_NAME +
                    File.separator + fileName;
            try (OutputStream outStream = Files.newOutputStream(Paths.get(filePath))) {
                outStream.write(cert);
            } catch (Exception e) {
                String msg = "Error when writing the public certificate to a file";
                log.error(msg, e);
                throw new SecurityException(msg, e);
            }

            Map fileResourcesMap = (Map) configurationContext.getProperty(ServerConstants.FILE_RESOURCE_MAP);
            if (fileResourcesMap == null) {
                fileResourcesMap = new Hashtable();
                configurationContext.setProperty(ServerConstants.FILE_RESOURCE_MAP, fileResourcesMap);
            }
            fileResourcesMap.put(fileName, filePath);
        }
        return ServerConstants.ContextPaths.DOWNLOAD_PATH + "?id=" + fileName;
    }

    /**
     * Check whether the certificate is available in the file system.
     *
     * @param fileName             File name.
     * @param configurationContext Configuration context of the current message.
     * @return True if the certificate is available in the file system, false otherwise.
     */
    private static boolean verifyCertExistence(String fileName, ConfigurationContext configurationContext) {

        String workingDirectory = (String) configurationContext.getProperty(ServerConstants.WORK_DIR);
        String filePath =
                workingDirectory + File.separator + ServerConstants.PUBLIC_CERTS_DIRECTORY_NAME + File.separator +
                        fileName;
        File pubCert = new File(
                workingDirectory + File.separator + ServerConstants.PUBLIC_CERTS_DIRECTORY_NAME + File.separator +
                        fileName);

        // If cert is still available then exit.
        if (pubCert.exists()) {
            Map fileResourcesMap = (Map) configurationContext.getProperty(ServerConstants.FILE_RESOURCE_MAP);
            if (fileResourcesMap == null) {
                fileResourcesMap = new Hashtable();
                configurationContext.setProperty(ServerConstants.FILE_RESOURCE_MAP, fileResourcesMap);
            }
            fileResourcesMap.putIfAbsent(fileName, filePath);
            return true;
        }
        return false;
    }
}
