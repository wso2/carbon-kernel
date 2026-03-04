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

    private static final String CARBON_HOME = "carbon.home";
    private static final String PERSIST_PASSWORD = "persist.password";
    private static final String OS_NAME = "os.name";
    private static final String WINDOWS_OS_TOKEN = "win";

    private static final String ENCRYPTION_KEY_FILE = "encryption-key";
    private static final String ENCRYPTION_KEY_FILE_TMP = "encryption-key-tmp";
    private static final String ENCRYPTION_KEY_FILE_PERSIST = "encryption-key-persist";
    private static final String FILE_EXTENSION_TXT = ".txt";

    private static final String CONSOLE_PROMPT = "Enter Symmetric Encryption Key :";
    private static final String OVERWRITE_TOKEN = "!@#$%^&*()SDFGHJZXCVBNM!@#$%^&*";

    public static final String ENCRYPTION_KEY_PASSWORD = "encryption.key.password";
    private static volatile String encryptionKey;

    @Override
    public void handleSingleSecretCallback(SingleSecretCallback singleSecretCallback) {

        if (encryptionKey == null) {
            LOG.debug("Resolving encryption key for secure vault");
            synchronized (EncryptionKeyCallbackHandler.class) {
                if (encryptionKey == null) {
                    encryptionKey = resolveEncryptionKey();
                }
            }
        }

        if (singleSecretCallback != null && ENCRYPTION_KEY_PASSWORD.equals(singleSecretCallback.getId())) {
            singleSecretCallback.setSecret(encryptionKey);
        }
    }

    private String resolveEncryptionKey() {

        String encryptionKeyFileName;
        String encryptionKeyFileNameTmp;
        String encryptionKeyFilePersist;
        String keyValue;

        String carbonHome = System.getProperty(CARBON_HOME);
        if (carbonHome == null || carbonHome.trim().isEmpty()) {
            handleException("System property '" + CARBON_HOME + "' is not set");
        }

        String osName = System.getProperty(OS_NAME, "").toLowerCase(Locale.ENGLISH);
        if (osName.contains(WINDOWS_OS_TOKEN)) {
            encryptionKeyFileName = ENCRYPTION_KEY_FILE + FILE_EXTENSION_TXT;
            encryptionKeyFileNameTmp = ENCRYPTION_KEY_FILE_TMP + FILE_EXTENSION_TXT;
            encryptionKeyFilePersist = ENCRYPTION_KEY_FILE_PERSIST + FILE_EXTENSION_TXT;
        } else {
            encryptionKeyFileName = ENCRYPTION_KEY_FILE;
            encryptionKeyFileNameTmp = ENCRYPTION_KEY_FILE_TMP;
            encryptionKeyFilePersist = ENCRYPTION_KEY_FILE_PERSIST;
        }

        boolean persistPassword = Boolean.parseBoolean(System.getProperty(PERSIST_PASSWORD, "").trim());
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
            char[] password = console.readPassword("[%s]", CONSOLE_PROMPT);
            if (password != null) {
                return String.valueOf(password);
            }
        }

        handleException("Unable to read the encryption key from file or console");
        return null;
    }

    private String readPassword(File file) {

        try (FileInputStream inputStream = new FileInputStream(file);
             BufferedReader bufferedReader =
                     new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line = bufferedReader.readLine();
            if (line == null || line.isEmpty()) {
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
             BufferedWriter bufferedWriter =
                     new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
            bufferedWriter.write(OVERWRITE_TOKEN);
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
