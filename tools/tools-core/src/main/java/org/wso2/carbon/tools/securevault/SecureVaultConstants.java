package org.wso2.carbon.tools.securevault;

/**
 * Created by nipuni on 5/6/16. todo
 */
public class SecureVaultConstants {

    public static final String CONFIGURE = "configure";
    public static final String CHANGE = "change";
    public static final String TRUE = "true";
    public static final String CONSOLE_PASSWORD_PARAM = "password";
    public static final String KEYSTORE_PASSWORD = "keystore.passowrd";
    public static final String CIPHER_TEXT_PROPERTY_FILE_PROPERTY = "cipher.text.properties.file";
    public static final String UTF8 = "UTF-8";
    public static final String CARBON_HOME= "carbon.home";
    public static final String CONF_DIR = "conf";
    public static final String CARBON_CONFIG_FILE = "security.yml";   //todo change to carbon.yml
    public static final String SECURITY_DIR = "security";
    public static final String CIPHER_TEXT_PROPERTY_FILE = "secrets.properties";
    public static final String SECRET_PROPERTY_FILE = "secret.conf.properties.file";
    public static final String SECRET_YAML_FILE = "secret-vault.yml";

    public static final class PrimaryKeyStore {

        public static final String PRIMARY_KEY_LOCATION_PROPERTY = "primary.key.location";
        public static final String PRIMARY_KEY_TYPE_PROPERTY = "primary.key.type";
        public static final String PRIMARY_KEY_ALIAS_PROPERTY = "primary.key.alias";
        public static final String SECRET_FILE_LOCATION = "secretRepositories.file.location";
    }

    public static final class SecureVault {
        public static final String NS_PREFIX = "xmlns:svns";
        public static final String NS = "http://org.wso2.securevault/configuration";
        public static final String ATTRIBUTE = "provider";
        public static final String SECRET_PROVIDER_CLASS =
                "org.wso2.securevault.secret.handler.SecretManagerSecretCallbackHandler";
        public static final String CARBON_DEFAULT_SECRET_PROVIDER =
                "org.wso2.carbon.securevault.DefaultSecretCallbackHandler";
        public static final String ALIAS = "svns:secretAlias";
        public static final String PASSWORD = "password";
        public static final String SECRET_REPOSITORIES = "secretRepositories";
        public static final String CARBON_SECRET_PROVIDER = "carbon.secretProvider";
        public static final String SECRET_FILE_PROVIDER = "secretRepositories.file.provider";
        public static final String SECRET_FILE_BASE_PROVIDER_CLASS =
                "org.wso2.carbon.kernel.security.repository.FileBaseSecretRepositoryProvider";
        public static final String SECRET_FILE_LOCATION = "secretRepositories.file.location";
        public static final String KEYSTORE_LOCATION = "keystore.identity.location";
        public static final String KEYSTORE_TYPE = "keystore.identity.type";
        public static final String KEYSTORE_ALIAS = "keystore.identity.alias";
        public static final String KEYSTORE_STORE_PASSWORD = "keystore.identity.store.password";
        public static final String IDENTITY_STORE_PASSWORD = "identity.store.password";
        public static final String KEYSTORE_STORE_SECRET_PROVIDER = "keystore.identity.store.secretProvider";
        public static final String KEYSTORE_KEY_PASSWORD = "keystore.identity.key.password";
        public static final String IDENTITY_KEY_PASSWORD = "identity.key.password";
        public static final String KEYSTORE_KEY_SECRET_PROVIDER = "keystore.identity.key.secretProvider";
    }
}

