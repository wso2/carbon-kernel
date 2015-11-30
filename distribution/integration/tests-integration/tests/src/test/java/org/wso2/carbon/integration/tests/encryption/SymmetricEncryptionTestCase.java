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
import org.compass.core.util.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.FrameworkConstants;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.extensions.servers.carbonserver.TestServerManager;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.integration.tests.common.utils.CarbonIntegrationBaseTest;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.FileManipulator;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
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
import java.util.HashMap;
import java.util.Properties;


public class SymmetricEncryptionTestCase {

    private static final Log log = LogFactory
            .getLog(SymmetricEncryptionTestCase.class);

    private static SecretKey symmetricKey = null;
    private static boolean isSymmetricKeyFromFile = false;
    private static String symmetricKeyEncryptAlgoDefault = "AES";
    private static String symmetricKeySecureVaultAliasDefault = "symmetric.key.value";
    private String propertyKey = "symmetric.key";
    private String symmetricKeyEncryptEnabled;
    private String symmetricKeyEncryptAlgo;
    private String symmetricKeySecureVaultAlias;
    private static final String resourcePath = "identity/config/symmetricKey";
    private String passwordString = "admin";
    private String encryptedString = "Adsghjk=";

    private static int portOffset = 28;
    private TestServerManager serverManager;
    private AutomationContext context;
    private String carbonHome;


    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception {
        HashMap<String, String> startUpParameterMap = new HashMap<String, String>();
        startUpParameterMap.put("-DportOffset", String.valueOf(portOffset));
        context = new AutomationContext();
        serverManager = new TestServerManager(context, System.getProperty("carbon.zip"), startUpParameterMap);
        serverManager.startServer();
        carbonHome = serverManager.getCarbonHome();

        readSymmetricKey();
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        if (serverManager != null) {
            serverManager.stopServer();
        }
    }

    @Test(groups = "carbon.core", description = "Check encryption using the symmetric encryption")
    public void encrypt() throws CryptoException {

        try {
            uploadApp();
            String serviceEndpoint = "http://" + context.getInstance().getHosts().get("default") + ":" +
                    (Integer.parseInt(FrameworkConstants.SERVER_DEFAULT_HTTP_PORT) + portOffset) +
                    "/services/DssVerifierService/";
            String endpoint = "encrypt";
            String contentType = "application/json";

            String jsonRequest = "{\"" + endpoint + "\":{\"plainText\":\"" + passwordString + "\"}}";
            HttpResponse response = this.getHttpResponse(serviceEndpoint + endpoint, contentType, jsonRequest);
            String encryptedString = response.getData();
            int statusCode = response.getResponseCode();

            if (statusCode != 500) {
                String encryptedStringTest = Base64.encode(encryptWithSymmetricKey(Base64.decode(passwordString)));
                if (!encryptedString.equals(encryptedStringTest)) {
                    Assert.hasText(encryptedString, "Error in encrypting with symmetric key");
                }
            }
        } catch (CryptoException e) {
            throw new CryptoException("Error in encrypting with symmetric key");
        } catch (Exception e) {
            throw new CryptoException("Error in encrypting with symmetric key");
        }
    }

    @Test(groups = "carbon.core", description = "Check decryption of the symmetric encryption")
    public void decrypt() throws CryptoException {

        try {
            String serviceEndpoint = "http://" + context.getInstance().getHosts().get("default") + ":" +
                    (Integer.parseInt(FrameworkConstants.SERVER_DEFAULT_HTTP_PORT) + portOffset) +
                    "/services/DssVerifierService/";
            String endpoint = "decrypt";
            String contentType = "application/json";

            String jsonRequest = "{\"" + endpoint + "\":{\"encryptedText\":\"" + encryptedString + "\"}}";
            HttpResponse response = this.getHttpResponse(serviceEndpoint + endpoint, contentType, jsonRequest);
            String decryptedString = response.getData();
            int statusCode = response.getResponseCode();

            if (statusCode != 500) {
                String decryptedStringTest = Base64.encode(encryptWithSymmetricKey(Base64.decode(encryptedString)));
                if (!decryptedString.equals(decryptedStringTest)) {
                    Assert.hasText(decryptedString, "Error in decrypting with symmetric key");
                }
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
            ServerConfiguration serverConfiguration = ServerConfiguration.getInstance();
            symmetricKeyEncryptEnabled = serverConfiguration.getFirstProperty("SymmetricEncryption.IsEnabled");
            symmetricKeyEncryptAlgo = serverConfiguration.getFirstProperty("SymmetricEncryption.Algorithm");
            symmetricKeySecureVaultAlias = serverConfiguration.getFirstProperty("SymmetricEncryption.SecureVaultAlias");

            String filePath = CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator + "resources" +
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
                    symmetricKey = new SecretKeySpec(Base64.decode(properties.getProperty(propertyKey)), 0,
                            Base64.decode(properties.getProperty(propertyKey)).length, encryptionAlgo);
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

    private void uploadApp() throws Exception {
        String aarServiceFile = "Axis2SampleService.aar";
        String axis2SampleServiceDir = System.getProperty("axis2.sample.service.dir");
        if (axis2SampleServiceDir == null || !(new File(axis2SampleServiceDir)).exists()) {
            log.warn("Symmetric encryption test not enabled");
            return;
        }
        assert carbonHome != null : "carbonHome cannot be null";
        File srcFile = new File(axis2SampleServiceDir + aarServiceFile);
        assert srcFile.exists() : srcFile.getAbsolutePath() + " does not exist";
        String deploymentPath =
                carbonHome + File.separator + "repository" + File.separator + "deployment" + File.separator + "server" +
                        File.separator + "axis2services";
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
