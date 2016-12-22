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

package org.wso2.carbon.tools.securevault.utils;

import org.wso2.carbon.tools.exception.CarbonToolException;
import org.wso2.carbon.tools.securevault.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
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

/**
 * Cipher Tool utility methods.
 *
 * @since 5.2.0
 */
public class Utils {
    private static final Logger logger = Logger.getLogger(Utils.class.getName());

    /**
     * Remove default constructor and make it not available to initialize.
     */
    private Utils() {
        throw new AssertionError("Instantiating utility class...");
    }

    public static CommandLineParser createCommandLineParser(String... toolArgs) throws CarbonToolException {
        return new CommandLineParser(toolArgs);
    }

    public static URLClassLoader getCustomClassLoader(Optional<String> optCustomLibPath) {
        List<URL> urls = new ArrayList<>();

        optCustomLibPath.map(path -> Paths.get(path))
                .filter(path -> path.toFile().exists() && path.toFile().isDirectory())
                .ifPresent(path -> urls.addAll(getJarURLs(path.toString())));

        Optional.ofNullable(System.getProperty("carbon.home"))
                .ifPresent(carbonHome -> {
                    urls.addAll(getJarURLs(Paths.get(carbonHome, "lib").toString()));
                    urls.addAll(getJarURLs(Paths.get(carbonHome, "wso2/lib", "plugins").toString()));
                });

        return (URLClassLoader) AccessController.doPrivileged(
                (PrivilegedAction<Object>) () -> new URLClassLoader(urls.toArray(new URL[urls.size()])));
    }

    public static Object createCipherTool(URLClassLoader urlClassLoader) throws CarbonToolException {
        Object objCipherTool;

        try {
            objCipherTool = urlClassLoader.loadClass(Constants.CIPHER_TOOL_CLASS).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new CarbonToolException("Unable to instantiate Cipher Tool", e);
        }

        try {
            Method initMethod = objCipherTool.getClass().getMethod(Constants.INIT_METHOD, URLClassLoader.class);
            initMethod.invoke(objCipherTool, urlClassLoader);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new CarbonToolException("Failed to initialize Cipher Tool", e);
        }
        return objCipherTool;
    }

    private static List<URL> getJarURLs(String location) {
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

    private static List<URL> getInternalJarURLs(File file) {
        List<URL> urls = new ArrayList<>();

        try {
            urls.add(file.getAbsoluteFile().toURI().toURL());
        } catch (MalformedURLException e) {
            logger.log(Level.SEVERE, "Unable to add file url in to URL list", e);
        }

        try (JarFile jarFile = new JarFile(file)) {
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
}
