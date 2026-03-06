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

/**
 * Constants used in secure vault operations.
 */
public final class SecureVaultConstants {

    private SecureVaultConstants() {
        // Prevent instantiation
    }

    // System property names
    public static final String CARBON_HOME = "carbon.home";
    public static final String PERSIST_PASSWORD = "persist.password";
    public static final String OS_NAME = "os.name";

    // OS detection
    public static final String WINDOWS_OS_TOKEN = "win";

    // File names
    public static final String ENCRYPTION_KEY_FILE = "encryption-key";
    public static final String ENCRYPTION_KEY_FILE_TMP = "encryption-key-tmp";
    public static final String ENCRYPTION_KEY_FILE_PERSIST = "encryption-key-persist";
    
    // File extensions
    public static final String FILE_EXTENSION_TXT = ".txt";

    // User prompts
    public static final String CONSOLE_PROMPT = "Enter Symmetric Encryption Key :";

    // Security tokens
    public static final String OVERWRITE_TOKEN = "!@#$%^&*()SDFGHJZXCVBNM!@#$%^&*";
}