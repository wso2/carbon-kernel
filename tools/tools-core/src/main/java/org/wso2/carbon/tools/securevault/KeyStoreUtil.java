package org.wso2.carbon.tools.securevault;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

/**
 * Created by nipuni on 5/6/16. //todo
 */
public class KeyStoreUtil {
    /**
     * Initializes the Cipher
     * @return cipher cipher
     */
    public static Cipher initializeCipher() {
        Cipher cipher = null;
        String keyStoreFile = System.getProperty(SecureVaultConstants.PrimaryKeyStore.PRIMARY_KEY_LOCATION_PROPERTY);
        String keyType = System.getProperty(SecureVaultConstants.PrimaryKeyStore.PRIMARY_KEY_TYPE_PROPERTY);
        String keyAlias = System.getProperty(SecureVaultConstants.PrimaryKeyStore.PRIMARY_KEY_ALIAS_PROPERTY);
        String password;
        if (System.getProperty(SecureVaultConstants.KEYSTORE_PASSWORD) != null &&
                System.getProperty(SecureVaultConstants.KEYSTORE_PASSWORD).length() > 0) {
            password = System.getProperty(SecureVaultConstants.KEYSTORE_PASSWORD);
        } else {
//            password = Utils.getValueFromConsole("Please Enter Primary KeyStore Password of Carbon Server : ", true);
            password = "wso2carbon";//todo read from console
        }
        if (password == null) {
//            throw new CipherToolException("KeyStore password can not be null");
        }
        System.out.println("keyStoreFile : " +keyStoreFile + ", password : " +password + ", keyType : " + keyType);
        KeyStore primaryKeyStore = getKeyStore(keyStoreFile, password, keyType);
        try {
            Certificate certs = primaryKeyStore.getCertificate(keyAlias);
//            System.out.println("certs : " + certs);
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, certs);
        } catch (KeyStoreException e) {
            throw new RuntimeException("Error initializing Cipher ", e);   //todo
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error initializing Cipher ", e);    //todo
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException("Error initializing Cipher ", e);     //todo
        } catch (InvalidKeyException e) {
            throw new RuntimeException("Error initializing Cipher ", e);     //todo
        }

        System.out.println("\nPrimary KeyStore of Carbon Server is initialized Successfully\n");
        return cipher;
    }

    private static KeyStore getKeyStore(String location, String storePassword, String storeType) {
        BufferedInputStream bufferedInputStream = null;
        try {
            System.out.println("............. store type : "+storeType);
            bufferedInputStream = new BufferedInputStream(new FileInputStream(location));
            KeyStore keyStore = KeyStore.getInstance(storeType);
            keyStore.load(bufferedInputStream, storePassword.toCharArray());
            return keyStore;
        } catch (KeyStoreException e) {
            throw new RuntimeException("Error loading keyStore from ' " + location + " ' ", e);   //todo ciphertoolexception
        } catch (IOException e) {
            throw new RuntimeException("Error loading keyStore from ' " + location + " ' ", e);   //todo ciphertoolexception
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error loading keyStore from ' " + location + " ' ", e);   //todo ciphertoolexception
        } catch (CertificateException e) {
            throw new RuntimeException("Error loading keyStore from ' " + location + " ' ", e);    //todo ciphertoolexception
        } finally {
            if (bufferedInputStream != null) {
                try {
                    bufferedInputStream.close();
                } catch (IOException e) {
                    System.err.println("Error while closing input stream");
                }
            }
        }
    }
}
