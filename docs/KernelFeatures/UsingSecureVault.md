# Secure Vault
> For the full list of capabilities available in this kernel version, see the **features** section in the [root README.md file](../../README.md#key-features-and-tools). 

WSO2 Carbon is shipped with a Secure Vault implementation. This allows you to store encrypted passwords that are mapped to aliases, i.e., you can use the aliases instead of the actual passwords in your configuration files for better security. For example, some configurations require the admin username and password. If the admin user password is "admin", you could use the alias `UserManager.AdminUser.Password` in your configuration file. You would then map that alias to the actual password "admin". At runtime, the product will look up this alias in the secure vault and SecureVault will return the decrypted password.

There are three files that are needed by the SecureVault:

1. Secure-vault.yaml: 
    Configurations that are needed to configure the SecureVault is given in this file
2. Master-keys.yaml: 
    The default SecureVault implementation is based on the Java Key Store (JKS). Passwords that are needed to access the JKS and Keys are specified in this file. 
3. Secrets.properties: 
    This file contains the alias with plain text or encrypted password.
    Example:
    
        UserManager.AdminUser.Password=plainText ABC@123
        UserManager.AdminUser.Password=cipherText SnBSWKjtZZOo0UsmOpPRhP6ZMNYTb80+BZHRDC/kxNT9ExcTswAbFjb/aip2KgQNaVuIT27UtrBaIv77Mb5sNPGiwyPrfajLNhSOlke2p8YmMkegx/mG2ytJhJa5j9iMGtCsbMt+SAf85v6kGIiH0gZA20qDZ9jnveT7/Ifz7v0\=

The SecureVatult reads the aliases and passwords given in the secrets.properties file and return the resolved (decrypted) password when it is queried wish a valid alias of a password.

The SecureVault implementation has two major sub-components in it, namely Master Key Reader and Secret Repository. The SecureVault implementation allows to plugin custom implementations for both of these:

1. Secret Repository
   The default implementation of SectetRepository is based on the passwords and aliases given in the Secrets.properties file and the default JKS packed with the carbon product.
2. Master Key Reader
   The default implementation of MasterKeyReader gets a list of required passwords from the SecretRepository and provides the values for those passwords by reading system properties, environment variables and master-keys.yaml file.

## How To Use SecureVault
SecureVault is by default enabled. It reads the aliases and passwords given in the secrets.properties file. The secrets.properties file may contain both plain text and encrypted passwords. We have a separate tool called "ciphertool" (shipped with Carbon /bin) to encrypt the secrets.properties file. Once the tool is run, it will encrypt all the plaintext password in the secrets.properties file.

CipherTool also depends on the configurations given in the secure-vault.yaml. Hence making necessary changes in the "secure-vault.yaml" file before running the CipherTool is mandatory. Once configured, running the "ciphertool" is simple as running the "ciphertool.sh" on Linux and Mac and running the "ciphertool.bat" on Windows machines.

## How To Implement Custom Master Key Reader
MasterKeyReader (org.wso2.carbon.kernel.securevault.MasterKeyReader) is the interface which all the MasterKeyReader implementations should derive from and should be registered as an OSGi service. SecureVault gets all the MasterKeyReader implementations and binds itself only with the matching MasterKeyReader which is specified in the "secure-vault.yaml" file.

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

## How To Implement Secret Repository
All the SecretRepository implementations should derive from the interface SecretRepository and register itself as an OSGi service of that interface. From all the registered implementations for SecretRepository, SecureVault chooses the correct SecretRepository based on the configurations given in the secure-vault.yaml file.

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
