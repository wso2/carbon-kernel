/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.securevault;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.securevault.secret.AbstractSecretCallbackHandler;
import org.wso2.securevault.secret.SingleSecretCallback;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Callback handler used to provide the symmetric encryption key to Secure Vault.
 *
 * <p>The handler looks for key data under {@code carbon.home} using the following order:</p>
 * <ol>
 * <li>{@code encryption-key(.txt)}</li>
 * <li>{@code encryption-key-tmp(.txt)}</li>
 * <li>{@code encryption-key-persist(.txt)}</li>
 * </ol>
 *
 * <p>If no file is found, it prompts on the console. Unless {@code persist.password=true}, the first file is
 * renamed to a temporary file and the temporary file is overwritten and deleted after use.</p>
 */
public class EncryptionKeyCallbackHandler extends AbstractSecretCallbackHandler {

    private static final Log LOG = LogFactory.getLog(EncryptionKeyCallbackHandler.class);

    private static volatile String encryptionKey;

    @Override
    public void handleSingleSecretCallback(SingleSecretCallback singleSecretCallback) {

        if (singleSecretCallback == null) {
            return;
        }
        if (encryptionKey == null) {
            synchronized (EncryptionKeyCallbackHandler.class) {
                if (encryptionKey == null) {
                    LOG.debug("Resolving encryption key for secure vault");
                    encryptionKey = resolveEncryptionKey();
                }
            }
        }
        if (StringUtils.isBlank(encryptionKey)) {
            handleException("Encryption key is not available");
        }
        singleSecretCallback.setSecret(encryptionKey);
    }

    private String resolveEncryptionKey() {

        String encryptionKeyFileName;
        String encryptionKeyFileNameTmp;
        String encryptionKeyFilePersist;
        String keyValue;
        String carbonHome = System.getProperty(SecureVaultConstants.CARBON_HOME);
        if (StringUtils.isBlank(carbonHome)) {
            handleException("System property '" + SecureVaultConstants.CARBON_HOME + "' is not set");
        }
        String osName = System.getProperty(SecureVaultConstants.OS_NAME, StringUtils.EMPTY).toLowerCase(Locale.ENGLISH);
        if (osName.contains(SecureVaultConstants.WINDOWS_OS_TOKEN)) {
            encryptionKeyFileName = SecureVaultConstants.ENCRYPTION_KEY_FILE + SecureVaultConstants.FILE_EXTENSION_TXT;
            encryptionKeyFileNameTmp = SecureVaultConstants.ENCRYPTION_KEY_FILE_TMP +
                    SecureVaultConstants.FILE_EXTENSION_TXT;
            encryptionKeyFilePersist = SecureVaultConstants.ENCRYPTION_KEY_FILE_PERSIST +
                    SecureVaultConstants.FILE_EXTENSION_TXT;
        } else {
            encryptionKeyFileName = SecureVaultConstants.ENCRYPTION_KEY_FILE;
            encryptionKeyFileNameTmp = SecureVaultConstants.ENCRYPTION_KEY_FILE_TMP;
            encryptionKeyFilePersist = SecureVaultConstants.ENCRYPTION_KEY_FILE_PERSIST;
        }
        boolean persistPassword = Boolean.parseBoolean(
                System.getProperty(SecureVaultConstants.PERSIST_PASSWORD, StringUtils.EMPTY).trim());
        File file = new File(carbonHome, encryptionKeyFileName);
        if (file.exists()) {
            keyValue = readPassword(file);
            if (!persistPassword && !renameConfigFile(file, encryptionKeyFileNameTmp)) {
                handleException("Error renaming encryption key config file");
            }
            return keyValue;
        }
        file = new File(carbonHome, encryptionKeyFileNameTmp);
        if (file.exists()) {
            keyValue = readPassword(file);
            if (!persistPassword && !deleteConfigFile(file)) {
                handleException("Error deleting encryption key config file");
            }
            return keyValue;
        }
        file = new File(carbonHome, encryptionKeyFilePersist);
        if (file.exists()) {
            return readPassword(file);
        }
        Console console = System.console();
        if (console != null) {
            char[] password = console.readPassword("[%s]", SecureVaultConstants.CONSOLE_PROMPT);
            if (ArrayUtils.isNotEmpty(password)) {
                return String.valueOf(password);
            }
        }
        handleException("Unable to read the encryption key from file or console");
        return null;
    }

    private String readPassword(File file) {

        try (FileInputStream inputStream = new FileInputStream(file);
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line = bufferedReader.readLine();
            if (StringUtils.isBlank(line)) {
                handleException("Encryption key file is empty: " + file.getAbsolutePath());
            }
            return line;
        } catch (IOException e) {
            handleException("Error reading encryption key from text file " + file.getAbsolutePath(), e);
            return null;
        }
    }

    private boolean deleteConfigFile(File file) {

        try (FileOutputStream outputStream = new FileOutputStream(file);
                BufferedWriter bufferedWriter = new BufferedWriter(
                        new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
            bufferedWriter.write(SecureVaultConstants.OVERWRITE_TOKEN);
            bufferedWriter.flush();
            outputStream.getFD().sync();
        } catch (IOException e) {
            handleException("Error writing values to text file " + file.getAbsolutePath(), e);
        }
        return file.exists() && file.delete();
    }

    private boolean renameConfigFile(File file, String fileName) {

        if (file.exists()) {
            File newConfigFile = new File(file.getParentFile(), fileName);
            if (newConfigFile.exists() && !newConfigFile.delete()) {
                LOG.warn("Unable to remove existing temporary encryption key file: " +
                        newConfigFile.getAbsolutePath());
                return false;
            }
            return file.renameTo(newConfigFile);
        }
        return false;
    }

    private static void handleException(String msg, Exception e) {

        LOG.error(msg, e);
        throw new SecretCallbackHandlerException(msg, e);
    }

    private static void handleException(String msg) {

        LOG.error(msg);
        throw new SecretCallbackHandlerException(msg);
    }
}
