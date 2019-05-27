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
import org.junit.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.wso2.carbon.automation.engine.FrameworkConstants;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.integration.tests.common.utils.CarbonIntegrationBaseTest;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.FileManipulator;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


public class SymmetricEncryptionTestCase extends CarbonIntegrationBaseTest {

    private static final Log log = LogFactory.getLog(SymmetricEncryptionTestCase.class);

    private static SecretKey symmetricKey = null;
    private static String symmetricKeyEncryptAlgoDefault = "AES";
    private String symmetricKeyEncryptAlgo = "AES";
    private String passwordString = "administrator";
    private String encryptedString = "l58EohFmzXxXe8I924WQoQ==";
    private String secret = "229E09ED15D5DBB6605FAEF188274946";
    private ServerConfigurationManager serverConfigurationManager;
    private static int portOffset = 0;
    private String carbonHome;

    public SymmetricEncryptionTestCase() {

    }

    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception {

        super.init();
        carbonHome = CarbonUtils.getCarbonHome();
        String pathToDeploymentTOML = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator +
                "CARBON" + File.separator + "encryption" + File.separator + "deployment.toml";

        String targetDeploymentTOML = carbonHome + File.separator + "repository" + File.separator +
                "conf" + File.separator + "deployment.toml";
        serverConfigurationManager = new ServerConfigurationManager(automationContext);
        serverConfigurationManager.applyConfigurationWithoutRestart(new File(pathToDeploymentTOML), new File
                (targetDeploymentTOML), false);
        serverConfigurationManager.restartGracefully();
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

            int statusCode = response.getResponseCode();
            String encryptedStringXml = response.getData();
            InputStream encryptedStringXmlStream = new ByteArrayInputStream(encryptedStringXml.getBytes());
            DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = fac.newDocumentBuilder();
            Document encryptedStringDoc = builder.parse(encryptedStringXmlStream);
            String encryptedString = encryptedStringDoc.getElementsByTagName("soapenv:Body").item(0).getChildNodes()
                    .item(0).getChildNodes().item(0).getTextContent();

            if (statusCode != 500) {
                String encryptedStringTest = Base64.encode(encryptWithSymmetricKey(passwordString.getBytes()));
                Assert.assertEquals("Error in encrypting with symmetric key", encryptedString, encryptedStringTest);
            }
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

            int statusCode = response.getResponseCode();
            String decryptedStringXml = response.getData();
            InputStream decryptedStringXmlStream = new ByteArrayInputStream(decryptedStringXml.getBytes());
            DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = fac.newDocumentBuilder();
            Document decryptedStringDoc = builder.parse(decryptedStringXmlStream);
            String decryptedString = decryptedStringDoc.getElementsByTagName("soapenv:Body").item(0).getChildNodes()
                    .item(0).getChildNodes().item(0).getTextContent();

            if (statusCode != 500) {
                String decryptedStringTest = new String(decryptWithSymmetricKey(Base64.decode(encryptedString)));
                Assert.assertEquals("Error in decrypting with symmetric key", decryptedString, decryptedStringTest);
            }
        } catch (Exception e) {
            throw new CryptoException("Error in decrypting with symmetric key");
        }
    }

    private void readSymmetricKey() {

        symmetricKey = new SecretKeySpec(secret.getBytes(), 0, secret.getBytes().length, symmetricKeyEncryptAlgo);

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
