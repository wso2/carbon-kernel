/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.registry.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@Deprecated
public class ResourceImporter {

    private static final Log log = LogFactory.getLog(ResourceImporter.class);

    private Registry registry;

    public ResourceImporter(Registry registry) {
        this.registry = registry;
    }

    public void importDirectory(String fromURL, String toPath) throws RegistryException {

        toPath = preparePath(toPath);

        List toBeProcessed = new ArrayList();

        URI uri;
        try {
            uri = new URI(fromURL);

        } catch (URISyntaxException e) {
            String msg = "Directory to be imported should be given as a file URL.\n" +
                    "Caused by: " + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg);
        }

        String rootPath;
        File topFile = new File(uri);
        if (!topFile.exists()) {
            String msg = "Could not import the non-existence directory " + uri;
            log.error(msg);
            throw new RegistryException(msg);
        }

        rootPath = topFile.getAbsolutePath();
        toBeProcessed.add(rootPath);

        while (toBeProcessed.size() > 0) {

            String path = (String) toBeProcessed.remove(0);
            File file = new File(path);

            if (file.isDirectory()) {

                putDirectory(rootPath, file, toPath);

                File[] children = file.listFiles();
                for (File aChildren : children) {
                    toBeProcessed.add(aChildren.getAbsolutePath());
                }

            } else {
                putFile(rootPath, file, toPath);
            }
        }
    }

    private void putDirectory(String rootPath, File directory, String toPath)
            throws RegistryException {

        Collection collection = registry.newCollection();
        String targetPath =
                toPath + directory.getAbsolutePath().substring(rootPath.length());
        registry.put(targetPath, collection);
    }

    private void putFile(String rootPath, File file, String toPath) throws RegistryException {
        FileInputStream fileStream = null;
        try {
            fileStream = new FileInputStream(file);
            //ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            //int b;
            //while ((b = fileStream.read()) != -1) {
            //    outputStream.write(b);
            //}

            ResourceImpl resource = new ResourceImpl();
            resource.setContentStream(fileStream);

            String targetPath =
                    toPath + file.getAbsolutePath().substring(rootPath.length());
            registry.put(targetPath, resource);

        } catch (Exception e) {
            String msg = "Could not read from the file " + file.getPath();
            log.error(msg, e);
            throw new RegistryException(msg);
        } finally {
            if (fileStream != null) {
                try {
                    fileStream.close();
                } catch (IOException e) {
                    log.error("Error while closing the file: " + file.getName());
                }

            }
        }
    }

    private String preparePath(String rawPath) {

        String preparedPath = rawPath;

        if (rawPath.endsWith(RegistryConstants.PATH_SEPARATOR)) {
            preparedPath = rawPath.substring(
                    0, rawPath.length() - RegistryConstants.PATH_SEPARATOR.length());
        }

        return preparedPath;
    }
}
