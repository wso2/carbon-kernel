package org.wso2.carbon.tools.securevault;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by nipuni on 5/10/16.  //todo
 */
public class Utils {

    /**
     * Set the system properties
     */
    public static void setSystemProperties() {
        String keyStoreFile = null, keyType = null, keyAlias = null, secretConfPropFile, secretConfFile = null, secretPropFile = null;

        String homeFolder = System.getProperty(SecureVaultConstants.CARBON_HOME);

        Properties properties = System.getProperties();

        //Verify if this is WSO2 environment  todo
        Path path = Paths.get(homeFolder, SecureVaultConstants.CONF_DIR, SecureVaultConstants.CARBON_CONFIG_FILE);

        if (Files.exists(path)) {
            //WSO2 Environment

            Yaml yaml = new Yaml();
            try {
                Map<String, Object> config = (Map<String, Object>) yaml.load(new FileInputStream(path.toString()));
                Map<String, Object> keyStoreConfig = (Map<String, Object>) config.get("KeyStore");
                keyStoreFile = (String) keyStoreConfig.get("Location");
                keyStoreFile = homeFolder + keyStoreFile.substring((keyStoreFile.indexOf('}')) + 1);
                System.setProperty(SecureVaultConstants.PrimaryKeyStore.PRIMARY_KEY_LOCATION_PROPERTY, keyStoreFile);
                keyType = (String) keyStoreConfig.get("Type");
                keyAlias = (String) keyStoreConfig.get("KeyAlias");
                secretPropFile = homeFolder + File.separator + SecureVaultConstants.CONF_DIR + File.separator +
                        SecureVaultConstants.SECURITY_DIR + File.separator + SecureVaultConstants.CIPHER_TEXT_PROPERTY_FILE;
                //todo initialize other properties
                secretConfFile = homeFolder + File.separator + SecureVaultConstants.CONF_DIR + File.separator + SecureVaultConstants.SECURITY_DIR +
                        File.separator + SecureVaultConstants.SECRET_YAML_FILE;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }


//                keyStoreFile = Utils.getValueFromXPath(document.getDocumentElement(),
//                        Constants.PrimaryKeyStore.PRIMARY_KEY_LOCATION_XPATH);
//                keyStoreFile = homeFolder + keyStoreFile.substring((keyStoreFile.indexOf('}')) + 1);
//                System.setProperty(Constants.PrimaryKeyStore.PRIMARY_KEY_LOCATION_PROPERTY, keyStoreFile);
//                keyType = Utils.getValueFromXPath(document.getDocumentElement(),
//                        Constants.PrimaryKeyStore.PRIMARY_KEY_TYPE_XPATH);
//                keyAlias = Utils.getValueFromXPath(document.getDocumentElement(),
//                        Constants.PrimaryKeyStore.PRIMARY_KEY_ALIAS_XPATH);
//
//                secretConfFile = homeFolder + File.separator + Constants.REPOSITORY_DIR +
//                        File.separator + Constants.CONF_DIR + File.separator + Constants.SECURITY_DIR +
//                        File.separator + Constants.SECRET_PROPERTY_FILE;
//                cipherTextPropFile = Constants.REPOSITORY_DIR + File.separator + Constants.CONF_DIR + File.separator +
//                        Constants.SECURITY_DIR + File.separator + Constants.CIPHER_TEXT_PROPERTY_FILE;
//                cipherToolPropFile =
//                        homeFolder + File.separator + Constants.REPOSITORY_DIR + File.separator + Constants.CONF_DIR +
//                                File.separator + Constants.SECURITY_DIR + File.separator + Constants.CIPHER_TOOL_PROPERTY_FILE;

//            } catch (ParserConfigurationException e) {
////                throw new CipherToolException(
////                        "Error reading primary key Store details from " + Constants.CARBON_CONFIG_FILE + " file ", e);
//            } catch (SAXException e) {
////                throw new CipherToolException(
////                        "Error reading primary key Store details from " + Constants.CARBON_CONFIG_FILE + " file ", e);
//            } catch (IOException e) {
////                throw new CipherToolException(
////                        "Error reading primary key Store details from " + Constants.CARBON_CONFIG_FILE + " file ", e);
//            }
        } else {
            //todo handling non-wso2 environments
            Path currentPath = Paths.get("");
            homeFolder = currentPath.toAbsolutePath().toString();
            Path standaloneConfigPath = Paths.get("");
            //Paths.get(homeFolder, Constants.CONF_DIR, Constants.CIPHER_STANDALONE_CONFIG_PROPERTY_FILE);
            if (!Files.exists(standaloneConfigPath)) {
//                throw new CipherToolException(
//                        "File, " + Constants.CIPHER_STANDALONE_CONFIG_PROPERTY_FILE + " does not exist.");
            }
//            Properties standaloneConfigProp = Utils.loadProperties(standaloneConfigPath.toAbsolutePath().toString());
//            if (standaloneConfigProp.size() <= 0) {
////                throw new CipherToolException(
////                        "File, " + Constants.CIPHER_STANDALONE_CONFIG_PROPERTY_FILE + " cannot be empty");
//            }
//
//            keyStoreFile = standaloneConfigProp.getProperty(Constants.PrimaryKeyStore.PRIMARY_KEY_LOCATION_PROPERTY);
//            keyType = standaloneConfigProp.getProperty(Constants.PrimaryKeyStore.PRIMARY_KEY_TYPE_PROPERTY);
//            keyAlias = standaloneConfigProp.getProperty(Constants.PrimaryKeyStore.PRIMARY_KEY_ALIAS_PROPERTY);
//            secretConfPropFile = standaloneConfigProp.getProperty(Constants.SECRET_PROPERTY_FILE_PROPERTY);
//            secretConfFile = homeFolder + File.separator + secretConfPropFile;
//            cipherTextPropFile = standaloneConfigProp.getProperty(Constants.CIPHER_TEXT_PROPERTY_FILE_PROPERTY);
//            cipherToolPropFile = standaloneConfigProp.getProperty(Constants.CIPHER_TOOL_PROPERTY_FILE_PROPERTY);
        }

        if (keyStoreFile.trim().isEmpty()) {
//            throw new CipherToolException("KeyStore file path cannot be empty");
        }
        if (keyAlias == null || keyAlias.trim().isEmpty()) {
//            throw new CipherToolException("Key alias cannot be empty");
        }

//        System.setProperty(SecureVaultConstants.HOME_FOLDER, homeFolder);
        System.setProperty(SecureVaultConstants.PrimaryKeyStore.PRIMARY_KEY_LOCATION_PROPERTY, keyStoreFile);
        System.setProperty(SecureVaultConstants.PrimaryKeyStore.PRIMARY_KEY_TYPE_PROPERTY, keyType);
        System.setProperty(SecureVaultConstants.PrimaryKeyStore.PRIMARY_KEY_ALIAS_PROPERTY, keyAlias);
        System.setProperty(SecureVaultConstants.SECRET_PROPERTY_FILE, secretConfFile);
        System.setProperty(SecureVaultConstants.SecureVault.SECRET_FILE_LOCATION, secretPropFile);
        System.setProperty(SecureVaultConstants.CIPHER_TEXT_PROPERTY_FILE_PROPERTY,
                secretPropFile);
//        System.setProperty(SecureVaultConstants.CIPHER_TOOL_PROPERTY_FILE_PROPERTY, cipherToolPropFile);
    }

    /**
     * read values from property file
     *
     * @param filePath file path
     * @return Properties properties
     */
    public static Properties loadProperties(String filePath) {
        Properties properties = new Properties();
        File file = new File(filePath);
        if (!file.exists()) {
            return properties;
        }

        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            properties.load(inputStream);
        } catch (IOException e) {
            String msg = "Error loading properties from a file at :" + filePath;
            throw new RuntimeException(msg + " Error : " + e.getMessage());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    System.err.println("Error while closing input stream");
                }
            }
        }
        return properties;
    }

    /**
     * Write to the Secret-conf.properties
     */
    public static void writeSecretYamlConfiguration() throws IOException {

        Map<String, Object> data = new HashMap<>();

        String keyStoreFile = System.getProperty(SecureVaultConstants.PrimaryKeyStore.PRIMARY_KEY_LOCATION_PROPERTY);
        String keyType = System.getProperty(SecureVaultConstants.PrimaryKeyStore.PRIMARY_KEY_TYPE_PROPERTY);
        String aliasName = System.getProperty(SecureVaultConstants.PrimaryKeyStore.PRIMARY_KEY_ALIAS_PROPERTY);

//        properties.setProperty(Constants.SecureVault.SECRET_REPOSITORIES, "file");
//        properties.setProperty(Constants.SecureVault.SECRET_FILE_PROVIDER,
//                Constants.SecureVault.SECRET_FILE_BASE_PROVIDER_CLASS);
//        properties.setProperty(Constants.SecureVault.SECRET_FILE_LOCATION, System.getProperty(
//                Constants.SecureVault.SECRET_FILE_LOCATION));

        data.put(SecureVaultConstants.SecureVault.SECRET_REPOSITORIES, "file");
        data.put(SecureVaultConstants.SecureVault.SECRET_FILE_PROVIDER, SecureVaultConstants.SecureVault.SECRET_FILE_BASE_PROVIDER_CLASS);
        data.put(SecureVaultConstants.SecureVault.SECRET_FILE_LOCATION, System.getProperty(SecureVaultConstants.SecureVault.SECRET_FILE_LOCATION));
        data.put(SecureVaultConstants.SecureVault.KEYSTORE_LOCATION, keyStoreFile);
        data.put(SecureVaultConstants.SecureVault.KEYSTORE_TYPE, keyType);
        data.put(SecureVaultConstants.SecureVault.KEYSTORE_ALIAS, aliasName);
        data.put(SecureVaultConstants.SecureVault.KEYSTORE_STORE_PASSWORD, SecureVaultConstants.SecureVault.IDENTITY_STORE_PASSWORD);

        Yaml yaml = new Yaml();
        FileWriter writer = null;
        try {
            writer = new FileWriter(System.getProperty(SecureVaultConstants.SECRET_PROPERTY_FILE));
            yaml.dump(data, writer);
        } catch (IOException e) {
            e.printStackTrace(); //todo
        }

        if (writer != null) {
            writer.close();
        }
        System.out.println("\nSecret Configurations are written to the property file successfully\n");
    }

}
