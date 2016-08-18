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

package org.wso2.carbon.tools.securevault;

import org.wso2.carbon.tools.CarbonTool;
import org.wso2.carbon.tools.exception.CarbonToolException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * The Java class which defines the CipherTool as a CarbonTool.
 *
 * @since 5.2.0
 */
public class CipherTool implements CarbonTool {
    private static final Logger logger = Logger.getLogger(CipherTool.class.getName());
    private static final String ENCRYPT_TEXT = "encryptText";
    private static final String DECRYPT_TEXT = "decryptText";
    private static final String CUSTOM_LIB_PATH = "customLibPath";
    private static final String CIPHER_TOOL_CLASS = "org.wso2.carbon.kernel.securevault.tool.CipherTool";


    @Override
    public void execute(String... toolArgs) {
        Optional<String> optCommandlineParam = validateAndGetCurrentCommand(toolArgs);
        if (toolArgs.length > 0 && !optCommandlineParam.isPresent()) {
            printHelpMessage();
            return;
        }

        URLClassLoader urlClassLoader = getCustomClassLoader(optCommandlineParam);
        try {
            Object objCipherTool = createCipherTool(urlClassLoader);
            processCommand(optCommandlineParam, objCipherTool);
        } catch (CarbonToolException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private Optional<String> validateAndGetCurrentCommand(String... toolArgs) {
        if (toolArgs.length != 0) {
            return Stream.of(toolArgs)
                    .filter(arg -> arg.startsWith(ENCRYPT_TEXT + "=")
                            || arg.startsWith(DECRYPT_TEXT + "=")
                            || arg.startsWith(CUSTOM_LIB_PATH + "="))
                    .findFirst();
        }
        return Optional.empty();
    }

    private URLClassLoader getCustomClassLoader(Optional<String> optCommandlineParam) {
        List<URL> urls = new ArrayList<>();
        optCommandlineParam
                .filter(param -> param.startsWith(CUSTOM_LIB_PATH))
                .map(param -> param.substring(CUSTOM_LIB_PATH.length() + 1))
                .map(path -> Paths.get(path))
                .filter(path -> path.toFile().exists() && path.toFile().isDirectory())
                .ifPresent(path -> urls.addAll(getJarURLs(path.toString())));

        Optional.ofNullable(System.getProperty("carbon.home"))
                .ifPresent(carbonHome -> {
                    urls.addAll(getJarURLs(Paths.get(carbonHome, "osgi", "dropins").toString()));
                    urls.addAll(getJarURLs(Paths.get(carbonHome, "osgi", "plugins").toString()));
                });

        return (URLClassLoader) AccessController.doPrivileged(
                (PrivilegedAction<Object>) () -> new URLClassLoader(urls.toArray(new URL[urls.size()])));
    }

    private Object createCipherTool(URLClassLoader urlClassLoader) throws CarbonToolException {
        Object objCipherTool;
        try {
            objCipherTool = urlClassLoader.loadClass(CIPHER_TOOL_CLASS).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new CarbonToolException("Unable to instantiate Cipher Tool", e);
        }

        try {
            Method initMethod = objCipherTool.getClass().getMethod("init", URLClassLoader.class);
            initMethod.invoke(objCipherTool, urlClassLoader);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new CarbonToolException("Failed to initialize Cipher Tool", e);
        }
        return objCipherTool;
    }

    private void processCommand(Optional<String> optCommandlineParam, Object objCipherTool) throws CarbonToolException {
        String command = optCommandlineParam
                .map(s -> s.substring(0, s.indexOf('=')))
                .orElse("");
        String parameter = optCommandlineParam
                .map(s -> s.substring(s.indexOf('=') + 1))
                .orElse("");
        Method method;
        try {
            switch (command) {
                case ENCRYPT_TEXT:
                    method = objCipherTool.getClass().getMethod("encryptText", String.class);
                    method.invoke(objCipherTool, parameter);
                    break;
                case DECRYPT_TEXT:
                    method = objCipherTool.getClass().getMethod("decryptText", String.class);
                    method.invoke(objCipherTool, parameter);
                    break;
                default:
                    method = objCipherTool.getClass().getMethod("encryptSecrets");
                    method.invoke(objCipherTool);
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new CarbonToolException("Failed to execute Cipher Tool command", e);
        }
    }

    private List<URL> getJarURLs(String location) {
        File fileLocation = new File(location);
        List<URL> urls = new ArrayList<>();
        File[] fileList = fileLocation.listFiles((File file) -> file.getPath().toLowerCase().endsWith(".jar"));
        if (fileList != null) {
            for (File file : fileList) {
                urls.addAll(getInternalJarURLs(file));
            }
        }
        return urls;
    }

    private List<URL> getInternalJarURLs(File file) {
        List<URL> urls = new ArrayList<>();

        try (JarFile jarFile = new JarFile(file)) {
            urls.add(file.getAbsoluteFile().toURI().toURL());
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".jar")) {
                    JarEntry internalJar = jarFile.getJarEntry(entry.getName());
                    try (InputStream inputStream = jarFile.getInputStream(internalJar)) {
                        File tempFile = File.createTempFile(internalJar.getName(), ".tmp");
                        tempFile.deleteOnExit();
                        try (FileOutputStream fileOutputStream = new FileOutputStream(tempFile)) {
                            byte[] buffer = new byte[1024];
                            int length;
                            while ((length = inputStream.read(buffer)) != -1) {
                                fileOutputStream.write(buffer, 0, length);
                            }
                        }
                        urls.add(tempFile.getAbsoluteFile().toURI().toURL());
                    }
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "CipherTool exits with error", e);
        }

        return urls;
    }

    /**
     * Returns a help message for the secure vault tool usage.
     */
    private void printHelpMessage() {
        logger.info("Incorrect usage of the cipher tool.\n\n"
                + "Instructions: sh ciphertool.sh [option]\n"
                + "option - a key=value pair based on the operation\n\n"
                + "Options:\n"
                + "1. With no option specified, cipher tool will encrypt the secrets given in the" +
                " [CARBON_HOME]conf/security/secrets.properties file.\n"
                + "2. encryptText : this option will first encrypt a given text and then prints the base64 encoded " +
                "string of the encoded cipher text in the console.\n"
                + "  Eg: ciphertool.sh encryptText=Abc@123\n"
                + "3. decryptText : this option accepts base64 encoded cipher text and prints the decoded plain text " +
                "in the console.\n"
                + "  Eg: ciphertool.sh decryptText=XxXxXx\n"
        );
    }
}
