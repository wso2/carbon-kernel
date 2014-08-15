/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.registry.core.utils;

import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.pagination.PaginationContext;
import org.wso2.carbon.registry.core.pagination.PaginationUtils;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.*;
import java.util.*;

/**
 * Utility class for managing and fetching media types.
 */
@SuppressWarnings("unused")
// Some utility methods are used outside the kernel, in the registry components.
public class MediaTypesUtils {

    private static final Log log = LogFactory.getLog(MediaTypesUtils.class);

    private static final String RESOURCE_MEDIA_TYPE_MAPPINGS_FILE = "mime.types";
    private static final String MIME_TYPE_COLLECTION = RegistryConstants.GOVERNANCE_COMPONENT_PATH +
            "/media-types";

    private static final String RESOURCE_MIME_TYPE_INDEX = "index";

    private static final String COLLECTION_MIME_TYPE_INDEX = "collection";

    private static final String CUSTOM_UI_MIME_TYPE_INDEX = "custom.ui";

    // The following map contains the human-readable-media-type, mime-type mapping
    private static Map<String,String> humanReadableMediaTypeMap;

    private static final String HUMAN_READABLE_MEDIA_TYPE_MAPPINGS_FILE = "mime.mappings";
    private static final String
            FAILED_TO_READ_THE_THE_HUMAN_READABLE_MEDIA_TYPE_MIME_TYPE_MAPPINGS_FILE_MSG =
            "Failed to read the the human readable media type mime type mappings file.";

    /**
     * Method to obtain the collection media types.
     *
     * @param registryContext the registry context.
     *
     * @return a String of collection media types, in the format name:type,name:type,...
     * @throws RegistryException if the operation failed.
     */
    public static String getCollectionMediaTypeMappings(RegistryContext registryContext)
            throws RegistryException {
        if (registryContext == null) {
            return RegistryContext.getBaseInstance().getCollectionMediaTypes();
        } else {
            return registryContext.getCollectionMediaTypes();
        }
    }

    /**
     * Method to obtain a list of paths having resources of the given media type.
     *
     * @param registry  the registry instance to run query on.
     * @param mediaType the media type.
     * @return an array of resource paths.
     * @throws RegistryException if the operation failed.
     */
    public static String[] getResultPaths(Registry registry, String mediaType) throws RegistryException {
        String[] result;
        String[] paginatedResult;
        try {
            Map<String, String> parameter = new HashMap<String, String>();
            parameter.put("1", mediaType);
            parameter.put("query", "SELECT DISTINCT REG_PATH_ID, REG_NAME FROM REG_RESOURCE WHERE REG_MEDIA_TYPE=?");
            result = (String[]) registry.executeQuery(null, parameter).getContent();
            if (result == null || result.length == 0) {
                return new String[0];
            }
            result = removeMountPaths(result, registry);
            MessageContext messageContext = MessageContext.getCurrentMessageContext();
            if (PaginationUtils.isPaginationAnnotationFound("getPaginatedGovernanceArtifacts") &&
                    ((messageContext != null && PaginationUtils.isPaginationHeadersExist(messageContext)) || PaginationContext.getInstance() != null)) {

                int rowCount = result.length;
                PaginationContext paginationContext;
                try {
                    if (messageContext != null) {
                        PaginationUtils.setRowCount(messageContext, Integer.toString(rowCount));
                        paginationContext = PaginationUtils.initPaginationContext(messageContext);
                    } else {
                        paginationContext = PaginationContext.getInstance();
                    }

                    int start = paginationContext.getStart();
                    int count = paginationContext.getCount();

                    int startIndex;
                    if (start == 1) {
                        startIndex = 0;
                    } else {
                        startIndex = start;
                    }
                    if (rowCount < start + count) {
                        paginatedResult = new String[rowCount - startIndex];
                        System.arraycopy(result, startIndex, paginatedResult, 0, (rowCount - startIndex));
                    } else {
                        paginatedResult = new String[count];
                        System.arraycopy(result, startIndex, paginatedResult, 0, count);
                    }
                    return paginatedResult;

                } finally {
                    PaginationContext.destroy();
                }
            }
        } catch (RegistryException e) {
            String msg = "Error in getting the result for media type: " + mediaType + ".";
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
        return result;
    }

    private static String[] removeMountPaths(String[] paths, Registry registry) {
        if (paths == null) {
            return new String[0];
        }
        List<String> fixedPaths = new LinkedList<String>();
        for (String path : paths) {
            if (!path.contains(RegistryConstants.SYSTEM_MOUNT_PATH)) {
                fixedPaths.add(path);
            }
        }
        return fixedPaths.toArray(new String[fixedPaths.size()]);
    }

    /**
     * Method to obtain the collection media types.
     *
     * @param configSystemRegistry a configuration system registry instance.
     *
     * @return a String of collection media types, in the format name:type,name:type,...
     * @throws RegistryException if the operation failed.
     */
    public static String getCollectionMediaTypeMappings(Registry configSystemRegistry)
            throws RegistryException {

        RegistryContext registryContext = configSystemRegistry.getRegistryContext();

        if (getCollectionMediaTypeMappings(registryContext) != null) {
            return getCollectionMediaTypeMappings(registryContext);
        }

        Resource resource;
        String mediaTypeString = null;

        String resourcePath = MIME_TYPE_COLLECTION + RegistryConstants.PATH_SEPARATOR +
                RESOURCE_MIME_TYPE_INDEX;

        // TODO: Adding the media types should ideally be done by the handler associated with the
        // media type
        if (!configSystemRegistry.resourceExists(resourcePath)) {
            getResourceMediaTypeMappings(configSystemRegistry);
        }
        if (!configSystemRegistry.resourceExists(resourcePath + RegistryConstants.PATH_SEPARATOR +
                COLLECTION_MIME_TYPE_INDEX)) {
            resource = configSystemRegistry.newResource();
            resource.setDescription("This resource contains the media Types associated with " +
                    "collections on the Registry. Add, Edit or Delete properties to Manage Media " +
                    "Types.");
            configSystemRegistry.put(resourcePath + RegistryConstants.PATH_SEPARATOR +
                    COLLECTION_MIME_TYPE_INDEX, resource);
        } else {
            resource = configSystemRegistry.get(resourcePath + RegistryConstants.PATH_SEPARATOR +
                    COLLECTION_MIME_TYPE_INDEX);
        }
        Properties properties = resource.getProperties();
        if (properties.size() > 0) {
            Set<Object> keySet = properties.keySet();
            for (Object key : keySet) {
                if (key instanceof String) {
                    String ext = (String) key;
                    if (RegistryUtils.isHiddenProperty(ext)) {
                        continue;
                    }
                    String value = resource.getProperty(ext);
                    String mediaTypeMapping = ext + ":" + value;
                    if (mediaTypeString == null) {
                        mediaTypeString = mediaTypeMapping;
                    } else {
                        mediaTypeString = mediaTypeString + "," + mediaTypeMapping;
                    }
                }
            }
        }
        registryContext.setCollectionMediaTypes(mediaTypeString);
        return mediaTypeString;
    }

    /**
     * Method to obtain the custom UI media types.
     *
     * @param registryContext the registry context.
     *
     * @return a String of custom UI media types, in the format name:type,name:type,...
     * @throws RegistryException if the operation failed.
     */
    public static String getCustomUIMediaTypeMappings(RegistryContext registryContext)
            throws RegistryException {
        if (registryContext == null) {
            return RegistryContext.getBaseInstance().getCustomUIMediaTypes();
        } else {
            return registryContext.getCustomUIMediaTypes();
        }
    }

    /**
     * Method to obtain the custom UI media types.
     *
     * @param configSystemRegistry a configuration system registry instance.
     *
     * @return a String of custom UI media types, in the format name:type,name:type,...
     * @throws RegistryException if the operation failed.
     */
    public static String getCustomUIMediaTypeMappings(Registry configSystemRegistry)
            throws RegistryException {

        RegistryContext registryContext = configSystemRegistry.getRegistryContext();

        if (getCustomUIMediaTypeMappings(registryContext) != null) {
            return getCustomUIMediaTypeMappings(registryContext);
        }

        Resource resource;
        String mediaTypeString = null;

        String resourcePath = MIME_TYPE_COLLECTION + RegistryConstants.PATH_SEPARATOR +
                RESOURCE_MIME_TYPE_INDEX;

        // TODO: Adding the media types should ideally be done by the handler associated with the
        // media type
        if (!configSystemRegistry.resourceExists(resourcePath)) {
            getResourceMediaTypeMappings(configSystemRegistry);
        }
        if (!configSystemRegistry.resourceExists(resourcePath + RegistryConstants.PATH_SEPARATOR +
                CUSTOM_UI_MIME_TYPE_INDEX)) {
            resource = configSystemRegistry.newResource();
            resource.setProperty("profiles", "application/vnd.wso2-profiles+xml");
            //resource.setProperty("service", "application/vnd.wso2-service+xml");
            resource.setDescription("This resource contains the media Types associated with " +
                    "custom user interfaces on the Registry. Add, Edit or Delete properties to " +
                    "Manage Media Types.");
            configSystemRegistry.put(resourcePath + RegistryConstants.PATH_SEPARATOR +
                    CUSTOM_UI_MIME_TYPE_INDEX, resource);
        } else {
            resource = configSystemRegistry.get(resourcePath + RegistryConstants.PATH_SEPARATOR +
                    CUSTOM_UI_MIME_TYPE_INDEX);
        }
        Properties properties = resource.getProperties();
        if (properties.size() > 0) {
            Set<Object> keySet = properties.keySet();
            for (Object key : keySet) {
                if (key instanceof String) {
                    String ext = (String) key;
                    if (RegistryUtils.isHiddenProperty(ext)) {
                        continue;
                    }
                    String value = resource.getProperty(ext);
                    String mediaTypeMapping = ext + ":" + value;
                    if (mediaTypeString == null) {
                        mediaTypeString = mediaTypeMapping;
                    } else {
                        mediaTypeString = mediaTypeString + "," + mediaTypeMapping;
                    }
                }
            }
        }
        registryContext.setCustomUIMediaTypes(mediaTypeString);
        return mediaTypeString;
    }

    /**
     * Method to obtain the resource media types.
     *
     * @param registryContext the registry context.
     *
     * @return a String of resource media types, in the format extension:type,extension:type,...
     * @throws RegistryException if the operation failed.
     */
    public static String getResourceMediaTypeMappings(RegistryContext registryContext)
            throws RegistryException {
        if (registryContext == null) {
            return RegistryContext.getBaseInstance().getResourceMediaTypes();
        } else {
            return registryContext.getResourceMediaTypes();
        }
    }

    /**
     * Method to obtain the resource media types.
     *
     * @param configSystemRegistry a configuration system registry instance.
     *
     * @return a String of resource media types, in the format extension:type,extension:type,...
     * @throws RegistryException if the operation failed.
     */
    public static String getResourceMediaTypeMappings(Registry configSystemRegistry)
            throws RegistryException {

        RegistryContext registryContext = configSystemRegistry.getRegistryContext();

        if (getResourceMediaTypeMappings(registryContext) != null) {
            return getResourceMediaTypeMappings(registryContext);
        }

        Resource resource;
        String mediaTypeString = null;

        String resourcePath = MIME_TYPE_COLLECTION + RegistryConstants.PATH_SEPARATOR +
                RESOURCE_MIME_TYPE_INDEX;

        if (!configSystemRegistry.resourceExists(resourcePath)) {
            resource = configSystemRegistry.newCollection();
        } else {
            resource = configSystemRegistry.get(resourcePath);
            Properties properties = resource.getProperties();
            if (properties.size() > 0) {
                Set<Object> keySet = properties.keySet();
                for (Object key : keySet) {
                    if (key instanceof String) {
                        String ext = (String) key;
                        if (RegistryUtils.isHiddenProperty(ext)) {
                            continue;
                        }
                        String value = resource.getProperty(ext);
                        String mediaTypeMapping = ext + ":" + value;
                        if (mediaTypeString == null) {
                            mediaTypeString = mediaTypeMapping;
                        } else {
                            mediaTypeString = mediaTypeString + "," + mediaTypeMapping;
                        }
                    }
                }
            }
            registryContext.setResourceMediaTypes(mediaTypeString);
            return mediaTypeString;
        }

        BufferedReader reader;
        try {
            File mimeFile = getMediaTypesFile();
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(mimeFile)));
        } catch (Exception e) {
            String msg = "Failed to read the the media type definitions file. Only a limited " +
                    "set of media type definitions will be populated. ";
            log.error(msg, e);
            mediaTypeString = "txt:text/plain,jpg:image/jpeg,gif:image/gif";
            registryContext.setResourceMediaTypes(mediaTypeString);
            return mediaTypeString;
        }


        try {
            while (reader.ready()) {
                String mediaTypeData = reader.readLine().trim();
                if (mediaTypeData.startsWith("#")) {
                    // ignore the comments
                    continue;
                }

                if (mediaTypeData.length() == 0) {
                    // ignore the blank lines
                    continue;
                }

                // mime.type file delimits media types:extensions by tabs. if there is no
                // extension associated with a media type, there are no tabs in the line. so we
                // don't need such lines.
                if (mediaTypeData.indexOf('\t') > 0) {

                    String[] parts = mediaTypeData.split("\t+");
                    if (parts.length == 2 && parts[0].length() > 0 && parts[1].length() > 0) {

                        // there can multiple extensions associated with a single media type. in
                        // that case, extensions are delimited by a space.
                        String[] extensions = parts[1].trim().split(" ");
                        for (String extension : extensions) {
                            if (extension.length() > 0) {
                                String mediaTypeMapping = extension + ":" + parts[0];
                                resource.setProperty(extension, parts[0]);
                                if (mediaTypeString == null) {
                                    mediaTypeString = mediaTypeMapping;
                                } else {
                                    mediaTypeString = mediaTypeString + "," + mediaTypeMapping;
                                }
                            }
                        }
                    }
                }
            }
            resource.setDescription("This collection contains the media Types available for " +
                    "resources on the Registry. Add, Edit or Delete properties to Manage Media " +
                    "Types.");
            Resource collection = configSystemRegistry.newCollection();
            collection.setDescription("This collection lists the media types available on the " +
                    "Registry Server. Before changing an existing media type, please make sure " +
                    "to alter existing resources/collections and related configuration details.");
            configSystemRegistry.put(MIME_TYPE_COLLECTION, collection);
            configSystemRegistry.put(resourcePath, resource);

        } catch (IOException e) {
            String msg = "Could not read the media type mappings file from the location: ";
            throw new RegistryException(msg, e);

        } finally {
            try {
                reader.close();
            } catch (IOException ignore) {
            }
        }
        registryContext.setResourceMediaTypes(mediaTypeString);
        return mediaTypeString;
    }

    /**
     * Method to obtain the media type of a given resource.
     *
     * @param resourceName the name of the resource.
     *
     * @return the media type.
     * @throws RegistryException if the operation failed.
     */
    public static String getMediaType(String resourceName)
            throws RegistryException {
        if (resourceName == null || resourceName.indexOf('.') == -1 ||
                resourceName.indexOf('.') + 1 >= resourceName.length()) {
            return null;
        }
        String extension = resourceName.substring(resourceName.lastIndexOf('.') + 1).toLowerCase();
        String mediaTypes = getResourceMediaTypeMappings(RegistryContext.getBaseInstance());
        if (mediaTypes == null) {
            // We don't treat this as an error, since some collections and resources would be
            // created even before the media types have been stored into the registry, and have
            // been initialized.
            return null;
        }
        extension += ":";
        if (mediaTypes.contains(extension)) {
            String temp = mediaTypes.substring(mediaTypes.indexOf(extension) + extension.length());
            if (temp.indexOf(',') == -1) {
                return temp;
            } else {
                return temp.substring(0, temp.indexOf(','));
            }
        }
        return null;
    }

    // Method to obtain the media types file.

    private static File getMediaTypesFile() throws RegistryException {

        String carbonHome = System.getProperty("carbon.home");

        if (carbonHome == null) {
            carbonHome = System.getenv("CARBON_HOME");
        }

        if (carbonHome != null) {

            File mediaTypesFile = new File(CarbonUtils.getEtcCarbonConfigDirPath(),
                    RESOURCE_MEDIA_TYPE_MAPPINGS_FILE);
            if (!mediaTypesFile.exists()) {
                String msg = "Resource media type definitions file (mime.types) file does " +
                        "not exist in the path " + CarbonUtils.getEtcCarbonConfigDirPath();
                log.error(msg);
                throw new RegistryException(msg);
            }

            return mediaTypesFile;

        } else {
            String msg = "carbon.home system property is not set. It is required to to derive " +
                    "the path of the media type definitions file (mime.types).";
            log.error(msg);
            throw new RegistryException(msg);
        }
    }
    private static File getHumanMediaTypeMappingsFile() throws RegistryException {

        String carbonHome = System.getProperty("carbon.home");

        if (carbonHome == null) {
            carbonHome = System.getenv("CARBON_HOME");
        }

        if (carbonHome != null) {

            File mediaTypesFile = new File(CarbonUtils.getEtcCarbonConfigDirPath(),
                    HUMAN_READABLE_MEDIA_TYPE_MAPPINGS_FILE);
            if (!mediaTypesFile.exists()) {
                String msg = "Resource human readable media type mappings file (mime.mappings) file does " +
                        "not exist in the path " + CarbonUtils.getEtcCarbonConfigDirPath();
                log.error(msg);
                throw new RegistryException(msg);
            }

            return mediaTypesFile;

        } else {
            String msg = "carbon.home system property is not set. It is required to to derive " +
                    "the path of the media type definitions file (mime.types).";
            log.error(msg);
            throw new RegistryException(msg);
        }
    }

    private static void populateMediaTypeMappings() throws RegistryException {
        BufferedReader reader;
        humanReadableMediaTypeMap = new HashMap<String, String>();
        try {
            File mimeFile = getHumanMediaTypeMappingsFile();
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(mimeFile)));
        } catch (Exception e) {
            String msg =
                    FAILED_TO_READ_THE_THE_HUMAN_READABLE_MEDIA_TYPE_MIME_TYPE_MAPPINGS_FILE_MSG;
            log.warn(msg, e);
            return;
        }

        try {
            while (reader.ready()) {
                String mediaTypeData = reader.readLine().trim();
                if (mediaTypeData.startsWith("#")) {
                    // ignore the comments
                    continue;
                }

                if (mediaTypeData.length() == 0) {
                    // ignore the blank lines
                    continue;
                }

                // mime.mappings file delimits media types:extensions by tabs. if there is no
                // extension associated with a media type, there are no tabs in the line. so we
                // don't need such lines.
                if (mediaTypeData.indexOf('\t') > 0) {

                    String[] parts = mediaTypeData.split("\t+");
                    if (parts.length == 2 && parts[0].length() > 0 && parts[1].length() > 0) {

                        // there can multiple extensions associated with a single media type. in
                        // that case, extensions are delimited by a space.
                        humanReadableMediaTypeMap.put(parts[1], parts[0]);
                    }
                }
            }
        } catch (IOException e) {
            String msg =
                    FAILED_TO_READ_THE_THE_HUMAN_READABLE_MEDIA_TYPE_MIME_TYPE_MAPPINGS_FILE_MSG;
            log.error(msg,e);
            throw new RegistryException(msg);
        } finally {
            try {
                reader.close();
                if(humanReadableMediaTypeMap == null){
                    humanReadableMediaTypeMap = Collections.emptyMap();
                }
            } catch (IOException ignore) {
            }
        }
    }

        /**
     * Method to obtain the mime type when the human readable media type is given
     *
     * @param mediaType - the human readable media type
     * @return the mime type
     *
     * */

    public static String getMimeTypeFromHumanReadableMediaType(String mediaType){

        try {
            if (humanReadableMediaTypeMap == null) {
                populateMediaTypeMappings();
            }

            if(humanReadableMediaTypeMap.containsKey(mediaType)){
                return humanReadableMediaTypeMap.get(mediaType);
            }
            return mediaType;

        } catch (RegistryException e) {
            String msg =
                    FAILED_TO_READ_THE_THE_HUMAN_READABLE_MEDIA_TYPE_MIME_TYPE_MAPPINGS_FILE_MSG;
            log.error(msg);
        }

        return null;
    }
    public static String getHumanReadableMediaTypeFromMimeType(String mediaType){

        try {
            if (humanReadableMediaTypeMap == null) {
                populateMediaTypeMappings();
            }

            if(humanReadableMediaTypeMap.containsValue(mediaType)){
                for (Map.Entry<String, String> entry : humanReadableMediaTypeMap.entrySet()) {
                    if(entry.getValue().equals(mediaType)){
                        return entry.getKey();
                    }
                }
            }
            return mediaType;

        } catch (RegistryException e) {
            String msg =
                    FAILED_TO_READ_THE_THE_HUMAN_READABLE_MEDIA_TYPE_MIME_TYPE_MAPPINGS_FILE_MSG;
            log.error(msg);
        }

        return null;
    }

    public static String getAllHumanTypes() {
        StringBuilder returnStringBuilder = new StringBuilder();
        String returnString = "";
        try {
            if (humanReadableMediaTypeMap == null) {
                populateMediaTypeMappings();
            }

            for (Map.Entry<String, String> entry : humanReadableMediaTypeMap.entrySet()) {
                returnStringBuilder.append(
                        entry.getValue()).append(":").append(entry.getKey()).append(",");
            }
            returnString = returnStringBuilder.toString();
            if(returnString.endsWith(",")){
                returnString = returnString.substring(0,returnString.lastIndexOf(','));
            }

        } catch (RegistryException e) {
            String msg =
                    FAILED_TO_READ_THE_THE_HUMAN_READABLE_MEDIA_TYPE_MIME_TYPE_MAPPINGS_FILE_MSG;
            log.error(msg);
        }
        return returnString;
    }


}
