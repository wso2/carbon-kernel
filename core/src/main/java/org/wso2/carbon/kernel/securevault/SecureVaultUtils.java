/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.kernel.securevault;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.internal.utils.Utils;
import org.wso2.carbon.kernel.securevault.config.model.SecretRepositoryConfiguration;
import org.wso2.carbon.kernel.securevault.exception.SecureVaultException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Properties;

/**
 * Secure Vault utility methods.
 *
 * @since 5.2.0
 */
public class SecureVaultUtils {
    private static final Logger logger = LoggerFactory.getLogger(SecureVaultUtils.class);
    private static final String defaultCharset = StandardCharsets.UTF_8.name();

    public static MasterKey getSecret(List<MasterKey> masterKeys, String secretName) throws SecureVaultException {
        return masterKeys.stream()
                .filter(masterKey -> masterKey.getMasterKeyName().equals(secretName))
                .findFirst()
                .orElseThrow(() -> new SecureVaultException(
                        "No secret found with given secret name '" + secretName + "'"));
    }

    public static byte[] base64Decode(byte[] base64Encoded) {
        return Base64.getDecoder().decode(base64Encoded);
    }

    public static byte[] base64Encode(byte[] original) {
        return Base64.getEncoder().encode(original);
    }

    public static char[] toChars(byte[] bytes) {
        Charset charset = Charset.forName(defaultCharset);
        return charset.decode(ByteBuffer.wrap(bytes)).array();
    }

    public static byte[] toBytes(String value) {
        return value.getBytes(Charset.forName(defaultCharset));
    }

    public static Properties loadSecretFile(Path secretsFilePath) throws SecureVaultException {
        Properties properties = new Properties();
        try (InputStream inputStream = new FileInputStream(secretsFilePath.toFile());
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, defaultCharset))) {

            // TODO : Use ConfigUtil to update with environment variables

            properties.load(bufferedReader);
        } catch (FileNotFoundException e) {
            throw new SecureVaultException("Cannot find secrets file in given location. (location: "
                    + secretsFilePath + ")", e);
        } catch (IOException e) {
            throw new SecureVaultException("Cannot access secrets file in given location. (location: "
                    + secretsFilePath + ")", e);
        }
        return properties;
    }

    public static void updateSecretFile(Path secretsFilePath, Properties properties) throws SecureVaultException {
        try (OutputStream outputStream = new FileOutputStream(secretsFilePath.toFile());
             OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, defaultCharset)) {

            properties.store(outputStreamWriter, null);
        } catch (FileNotFoundException e) {
            throw new SecureVaultException("Cannot find secrets file in given location. (location: "
                    + secretsFilePath + ")", e);
        } catch (IOException e) {
            throw new SecureVaultException("Cannot access secrets file in given location. (location: "
                    + secretsFilePath + ")", e);
        }
    }

    public static String getSecretPropertiesFileLocation(SecretRepositoryConfiguration secretRepositoryConfiguration) {
        return secretRepositoryConfiguration.getParameter(SecureVaultConstants.LOCATION)
                .orElseGet(() -> Utils.getSecretsPropertiesLocation());
    }

    /**
     * Remove default constructor and make it not available to initialize.
     */
    private SecureVaultUtils() {
        throw new AssertionError("Trying to a instantiate a constant class");
    }
}
