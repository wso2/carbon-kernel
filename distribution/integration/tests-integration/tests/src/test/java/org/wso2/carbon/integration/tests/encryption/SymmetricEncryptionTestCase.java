/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.integration.tests.encryption;

import org.apache.axiom.om.util.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.FrameworkConstants;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.integration.tests.common.utils.CarbonIntegrationBaseTest;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.FileManipulator;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;


public class SymmetricEncryptionTestCase extends CarbonIntegrationBaseTest {

    private static final Log log = LogFactory.getLog(SymmetricEncryptionTestCase.class);

    private static SecretKey symmetricKey = null;
    private static boolean isSymmetricKeyFromFile = false;
    private static String symmetricKeyEncryptAlgoDefault = "AES";
    private static String symmetricKeySecureVaultAliasDefault = "symmetric.key.value";
    private String propertyKey = "symmetric.key";
    private String symmetricKeyEncryptEnabled;
    private String symmetricKeyEncryptAlgo;
    private String symmetricKeySecureVaultAlias;
    private String passwordString = "administrator";
    private String encryptedString = "l58EohFmzXxXe8I924WQoQ==";
    private ServerConfigurationManager serverConfigurationManager;
    private static int portOffset = 0;
    private String carbonHome;

    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception {

        super.init();
        carbonHome = CarbonUtils.getCarbonHome();
        String pathToCarbonXML = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator +
                "CARBON" + File.separator + "encryption" + File.separator + "carbon.xml";
        String targetCarbonXML = carbonHome + File.separator + "repository" + File.separator +
                "conf" + File.separator + "carbon.xml";
        String pathToSymmetricProperties = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File
                .separator + "CARBON" + File.separator + "encryption" + File.separator + "symmetric-key.properties";
        String securityFolder = carbonHome + File.separator + "repository" + File.separator +
                "resources" + File.separator + "security" + File.separator + "symmetric-key.properties";
        serverConfigurationManager = new ServerConfigurationManager(automationContext);
        serverConfigurationManager.applyConfigurationWithoutRestart(new File(pathToSymmetricProperties), new File
                (securityFolder), false);
        serverConfigurationManager.applyConfigurationWithoutRestart(new File(pathToCarbonXML), new File
                (targetCarbonXML), false);
        serverConfigurationManager.restartGracefully();
        super.init();
        uploadApp();
        readSymmetricKey();
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        serverConfigurationManager.restoreToLastConfiguration();
    }

    @Test(groups = "carbon.core", description = "Check the symmetric encryption", priority = 1)
    public void encrypt() throws CryptoException {

        try {
            String serviceEndpoint = "http://" + automationContext.getInstance().getHosts().get("default") + ":" +
                    (Integer.parseInt(FrameworkConstants.SERVER_DEFAULT_HTTP_PORT) + portOffset) +
                    "/services/SymmetricEncryptionService/";
            String endpoint = "encrypt";
            String contentType = "application/soap+xml";

            String xmlRequest = "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" " +
                    "xmlns:ser=\"http://service.sample.axis2.tests.integration.carbon.wso2.org\">\n" +
                    "   <soap:Header/>\n" +
                    "   <soap:Body>\n" +
                    "      <ser:encrypt>\n" +
                    "         <ser:plainText>" + passwordString + "</ser:plainText>\n" +
                    "      </ser:encrypt>\n" +
                    "   </soap:Body>\n" +
                    "</soap:Envelope>";
            HttpResponse response = this.getHttpResponse(serviceEndpoint + endpoint, contentType, xmlRequest);
            String encryptedString = response.getData();
            int statusCode = response.getResponseCode();

            if (statusCode != 500) {
                String encryptedStringTest = Base64.encode(encryptWithSymmetricKey(passwordString.getBytes()));
                assert !encryptedString.equals(encryptedStringTest) : "Error in encrypting with symmetric key";
            }
        } catch (CryptoException e) {
            throw new CryptoException("Error in encrypting with symmetric key");
        } catch (Exception e) {
            throw new CryptoException("Error in encrypting with symmetric key");
        }
    }

    @Test(groups = "carbon.core", description = "Check decryption of the symmetric encryption", priority = 2)
    public void decrypt() throws CryptoException {

        try {
            String serviceEndpoint = "http://" + automationContext.getInstance().getHosts().get("default") + ":" +
                    (Integer.parseInt(FrameworkConstants.SERVER_DEFAULT_HTTP_PORT) + portOffset) +
                    "/services/SymmetricEncryptionService/";
            String endpoint = "decrypt";
            String contentType = "application/soap+xml";
            String xmlRequest = "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" " +
                    "xmlns:ser=\"http://service.sample.axis2.tests.integration.carbon.wso2.org\">\n" +
                    "   <soap:Header/>\n" +
                    "   <soap:Body>\n" +
                    "      <ser:decrypt>\n" +
                    "         <ser:encryptedText>" + encryptedString + "</ser:encryptedText>\n" +
                    "      </ser:decrypt>\n" +
                    "   </soap:Body>\n" +
                    "</soap:Envelope>";

            HttpResponse response = this.getHttpResponse(serviceEndpoint + endpoint, contentType, xmlRequest);
            String decryptedString = response.getData();
            int statusCode = response.getResponseCode();

            if (statusCode != 500) {
                String decryptedStringTest = new String(decryptWithSymmetricKey(encryptedString.getBytes()));
                assert !decryptedString.equals(decryptedStringTest) : "Error in decrypting with symmetric key";
            }
        } catch (CryptoException e) {
            throw new CryptoException("Error in decrypting with symmetric key");
        } catch (Exception e) {
            throw new CryptoException("Error in decrypting with symmetric key");
        }
    }


    private void readSymmetricKey() throws CryptoException {
        FileInputStream fileInputStream = null;
        String secretAlias;
        String encryptionAlgo;
        Properties properties;

        try {
            symmetricKeyEncryptEnabled = "true";
            symmetricKeyEncryptAlgo = "AES";
            symmetricKeySecureVaultAlias = "symmetric.key.value";

            String filePath = carbonHome + File.separator + "repository" + File.separator + "resources" +
                    File.separator + "security" + File.separator + "symmetric-key.properties";

            File file = new File(filePath);
            if (file.exists()) {
                fileInputStream = new FileInputStream(file);
                properties = new Properties();
                properties.load(fileInputStream);

                SecretResolver secretResolver = SecretResolverFactory.create(properties);
                if (symmetricKeySecureVaultAlias == null) {
                    secretAlias = symmetricKeySecureVaultAliasDefault;
                } else {
                    secretAlias = symmetricKeySecureVaultAlias;
                }

                if (symmetricKeyEncryptAlgo == null) {
                    encryptionAlgo = symmetricKeyEncryptAlgoDefault;
                } else {
                    encryptionAlgo = symmetricKeyEncryptAlgo;
                }

                if (secretResolver != null && secretResolver.isInitialized()) {
                    if (secretResolver.isTokenProtected(secretAlias)) {
                        symmetricKey = new SecretKeySpec(Base64.decode(secretResolver.resolve(secretAlias)), 0,
                                Base64.decode(secretResolver.resolve(secretAlias)).length, encryptionAlgo);
                    } else {
                        symmetricKey = new SecretKeySpec(Base64.decode((String) properties.get(secretAlias)), 0,
                                Base64.decode((String) properties.get(secretAlias)).length, encryptionAlgo);
                    }
                } else if (properties.containsKey(propertyKey)) {
                    symmetricKey = new SecretKeySpec(properties.getProperty(propertyKey).getBytes(), 0,
                            properties.getProperty(propertyKey).getBytes().length, encryptionAlgo);
                }

                if (symmetricKey != null) {
                    isSymmetricKeyFromFile = true;
                }
            }
        } catch (Exception e) {
            throw new CryptoException("Error in generating symmetric key", e);
        }
    }

    private byte[] encryptWithSymmetricKey(byte[] plainText) throws CryptoException {
        Cipher c = null;
        byte[] encryptedData = null;
        String encryptionAlgo;
        try {
            if (symmetricKeyEncryptAlgo == null) {
                encryptionAlgo = symmetricKeyEncryptAlgoDefault;
            } else {
                encryptionAlgo = symmetricKeyEncryptAlgo;
            }
            c = Cipher.getInstance(encryptionAlgo);
            c.init(Cipher.ENCRYPT_MODE, symmetricKey);
            encryptedData = c.doFinal(plainText);
        } catch (NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException |
                NoSuchPaddingException | InvalidKeyException e) {
            throw new CryptoException("Error when encrypting data.", e);
        }
        return encryptedData;

    }

    public byte[] decryptWithSymmetricKey(byte[] encryptionBytes) throws CryptoException {
        Cipher c = null;
        byte[] decryptedData = null;
        String encryptionAlgo;
        try {
            if (symmetricKeyEncryptAlgo == null) {
                encryptionAlgo = symmetricKeyEncryptAlgoDefault;
            } else {
                encryptionAlgo = symmetricKeyEncryptAlgo;
            }
            c = Cipher.getInstance(encryptionAlgo);
            c.init(Cipher.DECRYPT_MODE, symmetricKey);
            decryptedData = c.doFinal(encryptionBytes);
        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException |
                NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new CryptoException("Error when decrypting data.", e);
        }
        return decryptedData;
    }

    private void uploadApp() throws Exception {
        String aarServiceFile = "Axis2SampleService.aar";
        String axis2SampleServiceDir = System.getProperty("axis2.sample.service.dir");
        if (axis2SampleServiceDir == null || !(new File(axis2SampleServiceDir)).exists()) {
            log.warn("Symmetric Encryption test not enabled");
            return;
        }
        assert carbonHome != null : "carbonHome cannot be null";
        File srcFile = new File(axis2SampleServiceDir + aarServiceFile);
        assert srcFile.exists() : srcFile.getAbsolutePath() + " does not exist";
        String deploymentPath = carbonHome + File.separator + "repository" + File.separator + "deployment" +
                File.separator + "server" + File.separator + "axis2services";
        File depFile = new File(deploymentPath);
        if (!depFile.exists() && !depFile.mkdir()) {
            throw new IOException("Error while creating the deployment folder : " + deploymentPath);
        }
        File dstFile = new File(depFile.getAbsoluteFile() + File.separator + aarServiceFile);
        log.info("Copying " + srcFile.getAbsolutePath() + " => " + dstFile.getAbsolutePath());
        FileManipulator.copyFile(srcFile, dstFile);
        Thread.sleep(20000);
    }

    HttpResponse getHttpResponse(String endpoint, String contentType, String data) throws Exception {
        if (endpoint.startsWith("http://")) {
            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", contentType);
            conn.setRequestProperty("Accept", contentType);
            conn.setRequestProperty("charset", "UTF-8");
            conn.setReadTimeout(10000);
            conn.setRequestProperty("Content-Length", String.valueOf(data.length()));
            OutputStream os = conn.getOutputStream();
            os.write(data.getBytes());
            os.close();
            conn.connect();
            // Get the response
            StringBuilder sb = new StringBuilder();
            BufferedReader rd = null;
            try {
                rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = rd.readLine()) != null) {
                    sb.append(line);
                }
            } catch (IOException ignored) {
                rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                String line;
                while ((line = rd.readLine()) != null) {
                    sb.append(line);
                }
            } finally {
                if (rd != null) {
                    rd.close();
                }
            }
            log.info("Response: " + sb.toString() + ". Response Code" + conn.getResponseCode());
            return new HttpResponse(sb.toString(), conn.getResponseCode());
        }
        return null;
    }
}
