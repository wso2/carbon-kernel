
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateCrtKey;
import java.util.Enumeration;

public class BCMain {
    public static void main(String[] args) {
        //==============================
        // Setup stuff
        //==============================

        //Input FileName, Alias and Password
        String jksFileName = ".keystore";
        String jksAlias = "dims";
        char[] jksPassword = "security".toCharArray();

        //Output FileName, Alias and Password
        String pkcs12FileName = "keystore.p12";
        String pkcs12Alias = "dims";
        char[] pkcs12Password = "security".toCharArray();

        //Plug the Provider into the JCA/JCE
        Security.addProvider(new BouncyCastleProvider());



        //================================
        // JKS Stuff
        //================================

        InputStream jksInputStream = null;
        try {
            jksInputStream = new FileInputStream(jksFileName);
            System.out.println("Establish JKS InputStream to " +
                    jksFileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
        KeyStore jksKeyStore = null;
        try {
            jksKeyStore = KeyStore.getInstance("JKS", "SUN");
            System.out.println("Create JKS KeyStore Object.");
        } catch (KeyStoreException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
            System.exit(1);
        }

        //Load the keystore
        try {
            jksKeyStore.load(jksInputStream, jksPassword);
            System.out.println("Load JKS KeyStore.");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (CertificateException e) {
            e.printStackTrace();
            System.exit(1);
        }

        //Fetch all aliases from the keystore.
        Enumeration aliases = null;
        try {
            aliases = jksKeyStore.aliases();
            System.out.println("Got KeyStore aliases.");
        } catch (KeyStoreException e) {
            e.printStackTrace();
            System.exit(1);
        }

        //Shows all aliases from the keystore, only for info
        while (aliases.hasMoreElements()) {
            System.out.println("Has alias: " + aliases.nextElement());
        }



        //Get PrivateKey
        RSAPrivateCrtKey jksPrivateCrtKey = null;
        try {
            jksPrivateCrtKey =
                    (RSAPrivateCrtKey) jksKeyStore.getKey(jksAlias, jksPassword);
            System.out.println("Get PKCS#12 RSAPrivateCrtKey(" + jksPrivateCrtKey +
                    "): [Bit-Length: " + jksPrivateCrtKey.getModulus().bitLength() +
                    ", Modulus: " + jksPrivateCrtKey.getModulus() +
                    ", PublicExponent: " + jksPrivateCrtKey.getPublicExponent() +
                    ", PrivateExponent: " + jksPrivateCrtKey.getPrivateExponent() +
                    ", Prime-P: " + jksPrivateCrtKey.getPrimeP() +
                    ", Prime-Q: " + jksPrivateCrtKey.getPrimeQ() +
                    ", Prime-Exponent-P: " + jksPrivateCrtKey.getPrimeExponentP() +
                    ", Prime-Exponent-Q: " + jksPrivateCrtKey.getPrimeExponentQ() +
                    ", CRT-Coefficient: " + jksPrivateCrtKey.getCrtCoefficient()
            );
        } catch (KeyStoreException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
            System.exit(1);
        }

        //Get Certificate
        Certificate jksCert = null;
        try {
            jksCert = jksKeyStore.getCertificate(jksAlias);
            System.out.println("Get Certificate from PKCS#12: " + jksCert);
        } catch (KeyStoreException e) {
            e.printStackTrace();
            System.exit(1);
        }

        //Get Certificate Chain
        Certificate[] jksCerts = null;
        try {
            jksCerts = jksKeyStore.getCertificateChain(jksAlias);
            System.out.println("Get Certificate Chain from JKS, with " +
                    jksCerts.length + " certs.");
            for (int i = 0; i < jksCerts.length; i++) {
                System.out.println("Certificate " + (i + 1) +
                        " from JKS in the chain: " + jksCerts[i]);
            }
        } catch (KeyStoreException e) {
            e.printStackTrace();
            System.exit(1);
        }

        //=====================================
        // PKCS#12 stuff
        //=====================================

        KeyStore pkcs12KeyStore = null;
        try {
            pkcs12KeyStore = KeyStore.getInstance("PKCS12", "BC");
            System.out.println("Create PKCS#12 KeyStore Object.");
        } catch (KeyStoreException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
            System.exit(1);
        }
        try {
            pkcs12KeyStore.load(null, pkcs12Password);
            System.out.println(
                    "Load a new fresh PKCS#12 KeyStore from scratch.");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (CertificateException e) {
            e.printStackTrace();
            System.exit(1);
        }
        try {
            pkcs12KeyStore.setKeyEntry(pkcs12Alias, jksPrivateCrtKey,
                    pkcs12Password, jksCerts);
            System.out.println("Add the RSA Private Crt Key and the " +
                    "Certificate Chain to the PKCS#12 KeyStore.");
        } catch (KeyStoreException e) {
            e.printStackTrace();
            System.exit(1);
        }
        OutputStream pkcs12OutputStream = null;
        try {
            pkcs12OutputStream = new FileOutputStream(pkcs12FileName);
            System.out.println(
                    "Establish PKCS#12 OutputStream to " + pkcs12FileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
        try {
            pkcs12KeyStore.store(pkcs12OutputStream, pkcs12Password);
            pkcs12OutputStream.close();
            System.out.println("Store PKCS#12 KeyStore: " + pkcs12FileName);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (KeyStoreException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (CertificateException e) {
            e.printStackTrace();
            System.exit(1);
        }

        //=====================================
        // Reread the pkcs12KeyStore
        //=====================================

        InputStream pkcs12InputStream = null;
        try {
            pkcs12InputStream = new FileInputStream(pkcs12FileName);
            System.out.println(
                    "Establish PKCS#12 InputStream to " + pkcs12FileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
        try {
            pkcs12KeyStore.load(pkcs12InputStream, pkcs12Password);
            System.out.println("Re-read the PKCS#12 KeyStore.");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (CertificateException e) {
            e.printStackTrace();
            System.exit(1);
        }

        //Get PrivateKey
        RSAPrivateCrtKey pkcs12PrivateCrtKey = null;
        try {
            pkcs12PrivateCrtKey =
                    (RSAPrivateCrtKey) pkcs12KeyStore.getKey(pkcs12Alias, pkcs12Password);
            System.out.println(
                    "Get PKCS#12 RSAPrivateCrtKey(" + pkcs12PrivateCrtKey +
                    "): [Bit-Length: " + pkcs12PrivateCrtKey.getModulus().bitLength() +
                    ", Modulus: " + pkcs12PrivateCrtKey.getModulus() +
                    ", PublicExponent: " + pkcs12PrivateCrtKey.getPublicExponent() +
                    ", PrivateExponent: " + pkcs12PrivateCrtKey.getPrivateExponent() +
                    ", Prime-P: " + pkcs12PrivateCrtKey.getPrimeP() +
                    ", Prime-Q: " + pkcs12PrivateCrtKey.getPrimeQ() +
                    ", Prime-Exponent-P: " + pkcs12PrivateCrtKey.getPrimeExponentP() +
                    ", Prime-Exponent-Q: " + pkcs12PrivateCrtKey.getPrimeExponentQ() +
                    ", CRT-Coefficient: " + pkcs12PrivateCrtKey.getCrtCoefficient()
            );
        } catch (KeyStoreException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
            System.exit(1);
        }

        //Get Certificate
        Certificate pkcs12Cert = null;
        try {
            pkcs12Cert = pkcs12KeyStore.getCertificate(pkcs12Alias);
            System.out.println("Get Certificate from PKCS#12: " + pkcs12Cert);
        } catch (KeyStoreException e) {
            e.printStackTrace();
            System.exit(1);
        }

        //Get Certificate Chain
        Certificate[] pkcs12Certs = null;
        try {
            pkcs12Certs = pkcs12KeyStore.getCertificateChain(pkcs12Alias);
            System.out.println("Get Certificate Chain from PKCS#12, with " +
                    pkcs12Certs.length + " certs.");
            for (int i = 0; i < pkcs12Certs.length; i++) {
                System.out.println("Certificate " + (i + 1) +
                        " from PKCS#12 in the chain: " + pkcs12Certs[i]);
            }
        } catch (KeyStoreException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
