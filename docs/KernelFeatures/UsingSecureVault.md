# Using Secure Vault
> For the full list of capabilities available in this kernel version, see the **features** section in the [root README.md file](../../README.md#key-features-and-tools). 

WSO2 Carbon is shipped with a Secure Vault implementation. This allows you to store encrypted passwords that are mapped to aliases, i.e., you can use the aliases instead of the actual passwords in your configuration files for better security. For example, some configurations require the admin username and password. If the admin user password is 'admin', you could use the `UserManager.AdminUser.Password` alias in your configuration file. You would then map that alias to the actual password 'admin'. At runtime, the product will look up this alias and SecureVault will return the decrypted password.

There are three files that are needed by the SecureVault:

1. secure-vault.yaml: 
    Configurations that are required for configuring the SecureVault are given in this file.
2. master-keys.yaml: 
    The default SecureVault implementation is based on the Java Key Store (JKS). Passwords that are needed to access the JKS and Keys are specified in this file. 
3. secrets.properties: 
    This file contains the alias with the password that is in plain text or is encrypted.
    Example:
    
        UserManager.AdminUser.Password=plainText ABC@123
        UserManager.AdminUser.Password=cipherText SnBSWKjtZZOo0UsmOpPRhP6ZMNYTb80+BZHRDC/kxNT9ExcTswAbFjb/aip2KgQNaVuIT27UtrBaIv77Mb5sNPGiwyPrfajLNhSOlke2p8YmMkegx/mG2ytJhJa5j9iMGtCsbMt+SAf85v6kGIiH0gZA20qDZ9jnveT7/Ifz7v0\=

The SecureVault reads the aliases and passwords given in the secrets.properties file and returns the resolved (decrypted) password.

The SecureVault implementation has two major sub-components, namely the Master Key Reader and Secret Repository. The SecureVault implementation allows you to plugin custom implementations for both these sub-components:

1. Secret Repository
   The default implementation of Secret Repository is based on the passwords and aliases given in the secrets.properties file and the default JKS that is packed with the Carbon product.
2. Master Key Reader
   The default implementation of MasterKeyReader gets a list of required passwords from the Secret Repository and provides the values for those passwords by reading system properties, environment variables and the master-keys.yaml file.

## How To Use Secure Vault
SecureVault is by default enabled. It reads the aliases and passwords given in the secrets.properties file. The secrets.properties file may contain both plain text and encrypted passwords. We have a separate tool called 'ciphertool' (shipped with Carbon /bin) to encrypt the secrets.properties file. Once the tool is run, it will encrypt all the plain text passwords in the secrets.properties file.

CipherTool also depends on the configurations given in the file. Therefore, it is mandatory to make changed in the `secure-vault.yaml` file before running the Cipher tool. Once configured, running the 'ciphertool' is as simple as running the ciphertool script (ciphertool.sh on Linux/Mac and cihpertool.bat on Windows).

## How to Implement the Custom Master Key Reader
All the MasterKeyReader implementations should derive from the MasterKeyReader (org.wso2.carbon.kernel.securevault.MasterKeyReader) interface. This interface should be registered as an OSGi service. SecureVault gets all the MasterKeyReader implementations and binds itself only with the matching MasterKeyReader, which is specified in the `secure-vault.yaml` file.

    public class CustomHardCodedMasterKeyReader implements MasterKeyReader {
        private static Logger logger = LoggerFactory.getLogger(DefaultHardCodedMasterKeyReader.class);

        @Override
        public void init(MasterKeyReaderConfiguration masterKeyReaderConfiguration) throws SecureVaultException {
            // No initializations needed for the DefaultMasterKeyReader
        }

        @Override
        public void readMasterKeys(List<MasterKey> masterKeys) throws SecureVaultException {
            logger.debug("Providing hard coded secrets for 'keyStorePassword' and 'privateKeyPassword'");

            MasterKey keyStorePassword = SecureVaultUtils.getSecret(masterKeys, JKSBasedCipherProvider.KEY_STORE_PASSWORD);
            keyStorePassword.setMasterKeyValue("wso2carbon".toCharArray());

            MasterKey privateKeyPassword = SecureVaultUtils.getSecret(masterKeys, JKSBasedCipherProvider.PRIVATE_KEY_PASSWORD);
            privateKeyPassword.setMasterKeyValue("wso2carbon".toCharArray());
        }
    }

## How to Implement the Secret Repository
All the Secret Repository implementations should derive from the Secret Repository interface and should be registered as an OSGi service of that interface. From all the registered implementations for Secret Repository, SecureVault chooses the correct Secret Repository based on the configurations given in the secure-vault.yaml file.

    @Component(
            name = "org.wso2.carbon.kernel.securevault.repository.CustomSecretRepository",
            immediate = true,
            property = {
                    "capabilityName=org.wso2.carbon.kernel.securevault.SecretRepository"
            },
            service = SecretRepository.class
    )
    public class CustomSecretRepository implements SecretRepository {
        private final Map<String, char[]> secrets = new HashMap<>();
    
        @Override
        public void init(SecretRepositoryConfiguration secretRepositoryConfiguration, MasterKeyReader masterKeyReader)
                throws SecureVaultException {
    
        }
    
        @Override
        public void loadSecrets(SecretRepositoryConfiguration secretRepositoryConfiguration)
                throws SecureVaultException {
            secrets.put("password1", "my_p455wOrd_1".toCharArray());
            secrets.put("password2", "my_p455wOrd_2".toCharArray());
        }
        
        @Override
        public void persistSecrets(SecretRepositoryConfiguration secretRepositoryConfiguration) throws SecureVaultException {
            // Write the encrypted passwords into the file.
        }
        
        @Override
        public char[] resolve(String alias) {
            char[] secret = secrets.get(alias);
            if (secret != null && secret.length != 0) {
                return secret;
            }
            return new char[0];
        }
        
        @Override
        public byte[] encrypt(byte[] plainText) throws SecureVaultException {
            // Return the encrypted password
            return new byte[0];
        }
        
        @Override
        public byte[] decrypt(byte[] cipherText) throws SecureVaultException {
            // Return the decrypted password
            return new byte[0];
        }
    }
