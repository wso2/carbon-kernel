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

package org.wso2.carbon.registry.core.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.CollectionImpl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This class is used to provide client side utilities when someone uses Remote registry If user
 * want to import Registry to a local file system or export a local file system into a remote
 * registry (s)he can use this utility class.
 */
@SuppressWarnings("unused")
// This class is used outside the registry kernel, by client code.
public class RegistryClientUtils {

    private static final Log log = LogFactory.getLog(RegistryClientUtils.class);

    /**
     * This method can be used to import a local file system into a running instance of a registry.
     * Need to create a file object representing the local file and the need to tell where to add
     * the resource in the registry.
     *
     * @param file     : File representing local file system
     * @param path     : Where to put the file
     * @param registry : Registry instance
     *
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *          : if something went wrong
     */
    public static void importToRegistry(File file, String path, Registry registry)
            throws RegistryException {
        try {
            if (file == null || path == null) {
                throw new RegistryException("The values of the mandatory parameters, file and " +
                        "path cannot be null.");
            }
            processImport(file, path, registry);
        } catch (Exception e) {
            log.error("Failed to import to registry", e);
            throw new RegistryException("Failed to import to registry", e);
        }
    }

    /**
     * This method can be used to export registry instance or node in a registry to a local file
     * system. When we call this method a file structure will be created to match the structure to
     * map the structure in the registry
     *
     * @param toFile   : File in the local file system
     * @param path     : To which node export
     * @param registry : Registry instance
     *
     * @throws RegistryException : If something went wrong
     */
    public static void exportFromRegistry(File toFile, String path, Registry registry)
            throws RegistryException {
        try {
            processExport(path, toFile, registry, true);
        } catch (Exception e) {
            log.error("Failed to export from registry", e);
            throw new RegistryException("Failed to export from registry", e);
        }
    }

    // Processes imports

    private static void processImport(File file, String path, Registry registry) throws Exception {
        String filePart = file.getName();
        String resourcePath = path + "/" + filePart;
        if (file.isDirectory()) {
            File files[] = file.listFiles();
            if (files.length > 0) {
                for (File childFile : files) {
                    processImport(childFile, resourcePath, registry);
                }
            } else {
                CollectionImpl resource = new CollectionImpl();
                resource.setPath(resourcePath);
                registry.put(resourcePath, resource);
            }
        } else {
            ResourceImpl resource = new ResourceImpl();
            resource.setContent(new FileInputStream(file));
            resource.setPath(resourcePath);
            registry.put(resourcePath, resource);
        }
    }

    // Processes exports

    private static void processExport(String fromPath,
                                      File toFile,
                                      Registry registry,
                                      boolean useOriginal) throws RegistryException {
        Resource resource = registry.get(fromPath);
        if (resource != null) {

            String resourcePath = resource.getPath();
            int versionIndex = resourcePath.lastIndexOf(RegistryConstants.URL_SEPARATOR +
                    RegistryConstants.VERSION_PARAMETER_NAME +
                    RegistryConstants.URL_PARAMETER_SEPARATOR);
            if (versionIndex > 0) {
                resourcePath = resourcePath.substring(0, versionIndex);
            }
            int slashIndex = resourcePath.lastIndexOf('/');
            //getting only the last part of the resource path
            resourcePath = resourcePath.substring(slashIndex, resourcePath.length());
            File tempFile;
            if (!useOriginal) {
                tempFile = new File(toFile, resourcePath);
                if (!tempFile.exists() && resource instanceof Collection) {
                    boolean ignore = tempFile.mkdirs();
                }
            } else {
                tempFile = toFile;
            }
            if (resource instanceof Collection) {
                String childNodes[] = (String[]) resource.getContent();
                ArrayList<String> tobeDeleted = new ArrayList<String>();
                String[] files = tempFile.list();
                if (files != null) {
                    for (String file : files) {
                        tobeDeleted.add("/" + file);
                    }
                }
                for (String childNode : childNodes) {
                    versionIndex = childNode.lastIndexOf(RegistryConstants.URL_SEPARATOR +
                            RegistryConstants.VERSION_PARAMETER_NAME +
                            RegistryConstants.URL_PARAMETER_SEPARATOR);
                    if (versionIndex > 0) {
                        childNode = childNode.substring(0, versionIndex);
                    }
                    slashIndex = childNode.lastIndexOf('/');
                    //getting only the last part of the resource path
                    childNode = childNode.substring(slashIndex, childNode.length());
                    //tobeDeleted.remove(childNode);
                    if (tobeDeleted.contains(childNode)) {
                        slashIndex = childNode.lastIndexOf('/');
                        childNode = childNode.substring(slashIndex, childNode.length());
                        File deleteFile = new File(tempFile, childNode);
                        if (deleteFile.exists() && deleteFile.isDirectory()) {
                            deleteDir(deleteFile);
                            if (log.isTraceEnabled()) {
                                log.trace("Deleting a directory : " + deleteFile.getPath());
                            }
                        } else {
                            boolean ignore = deleteFile.delete();
                            if (log.isTraceEnabled()) {
                                log.trace("Deleting a file : " + deleteFile.getPath());
                            }
                        }
                    }
                }

                for (String childNode : childNodes) {
                    processExport(childNode, tempFile, registry, false);
                }
            } else {
                try {
                    FileOutputStream out = new FileOutputStream(tempFile);
                    out.write((byte[]) resource.getContent());
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    throw new RegistryException("An error occurred while creating the file: " +
                            tempFile.getAbsolutePath(), e);
                }
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("No resource found for : " + fromPath);
            }
        }
    }

    // deletes a directory

    private static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String aChildren : children) {
                boolean success = deleteDir(new File(dir, aChildren));
                if (!success) {
                    return false;
                }
            }
        }
        // The directory is now empty so delete it
        return dir.delete();
    }
}
