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

package org.wso2.carbon.registry.synchronization.operation;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.util.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.synchronization.SynchronizationConstants;
import org.wso2.carbon.registry.synchronization.SynchronizationException;
import org.wso2.carbon.registry.synchronization.UserInputCallback;
import org.wso2.carbon.registry.synchronization.Utils;
import org.wso2.carbon.registry.synchronization.message.Message;
import org.wso2.carbon.registry.synchronization.message.MessageCode;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * This command is used to perform a update operation which will download any changes to the
 * resources and collections from the provided registry instance into a checked out set of files and
 * directories local filesystem.
 */
@SuppressWarnings({"ResultOfMethodCallIgnored", "unused"})
public class UpdateCommand {
    private static final Log log = LogFactory.getLog(Utils.class);

    private static boolean ignoreConflicts = Boolean.parseBoolean(
            System.getProperty(SynchronizationConstants.REGISTRY_IGNORE_CONFLICTS, "false"));

    private String registryUrl = null;
    private String outputFile = null;
    private String workingLocation = null;
    private String checkOutPath = null;
    private String username = null;
    private boolean cleanRegistry = false;
    private boolean isSilentUpdate = false;

    ////////////////////////////////////////////////////////
    // Fields maintaining status of command execution
    ////////////////////////////////////////////////////////

    private int addedCount = 0;
    private int updatedCount = 0;
    private int conflictedCount = 0;
    private int deletedCount = 0;
    private int notDeletedCount = 0;
    private boolean updated = false;

    /**
     * Creates an instance of a update command which can be executed against a provided registry
     * instance.
     *
     * @param outputFile     if the content is to be downloaded into a single meta file, this
     *                       parameter can be used to specify the path to the meta file.
     * @param workingLocation     if the content is to be downloaded into a directory on the filesystem,
     *                       this parameter can be used to specify the path to the corresponding
     *                       location.
     * @param userUrl        aggregate URL containing a concatenation of the registry URL and the
     *                       resource path that is capable of referencing a remote resource. This
     *                       url will contain only the resource path if the resource was local to
     *                       the given registry instance.
     * @param isSilentUpdate whether this update requires user's intervention or not.
     * @param username       the name of the user (which should be a valid username on the target
     *                       server on which the provided registry instance is running) that
     *                       performs this operation.
     * @param cleanRegistry  whether the embedded registry instance must be cleaned after the
     *                       execution of the operation.
     *
     * @throws SynchronizationException if the operation failed.
     */
    public UpdateCommand(String outputFile,
                         String workingLocation,
                         String userUrl,
                         boolean isSilentUpdate,
                         String username,
                         boolean cleanRegistry) throws SynchronizationException {
        this.outputFile = outputFile;
        this.workingLocation = workingLocation;
        this.isSilentUpdate = isSilentUpdate;
        this.username = username;
        this.cleanRegistry = cleanRegistry;

        // now if the user url is different to the registry url we are going to consider that as well.
        if (userUrl != null) {
            registryUrl = Utils.getRegistryUrl(userUrl);
            checkOutPath = Utils.getPath(userUrl);
            if (checkOutPath == null || checkOutPath.equals("")) {
                checkOutPath = "/";
                // we are converting the root path to the current directory of the file system
            }
        } else {
            // get the update details form the meta element of the current checkout
            OMElement metaOMElement = Utils.getMetaOMElement(workingLocation);
            if (metaOMElement == null) {
                throw new SynchronizationException(MessageCode.CHECKOUT_BEFORE_UPDATE);
            }
            registryUrl = metaOMElement.getAttributeValue(new QName("registryUrl"));
            checkOutPath = metaOMElement.getAttributeValue(new QName("path"));
        }
    }

    /**
     * Method to obtain the count of files added.
     *
     * @return the count of files added.
     */
    public int getAddedCount() {
        return addedCount;
    }

    /**
     * Method to obtain the count of files updated.
     *
     * @return the count of files updated.
     */
    public int getUpdatedCount() {
        return updatedCount;
    }

    /**
     * Method to obtain the count of files conflicted.
     *
     * @return the count of files conflicted.
     */
    public int getConflictedCount() {
        return conflictedCount;
    }

    /**
     * Method to obtain the count of files deleted.
     *
     * @return the count of files deleted.
     */
    public int getDeletedCount() {
        return deletedCount;
    }

    /**
     * Method to obtain the count of files that were not deleted.
     *
     * @return the count of files that were not deleted.
     */
    public int getNotDeletedCount() {
        return notDeletedCount;
    }

    /**
     * Method to specify that an operation is a silent update (requires user intervention or not).
     *
     * @param silentUpdate whether this operation is a silent update.
     */
    public void setSilentUpdate(boolean silentUpdate) {
        isSilentUpdate = silentUpdate;
    }

    /**
     * This method will execute the update command utilizing the various parameters passed when
     * creating the instance of the command. This method accepts the users preference to whether a
     * file or directory should be deleted on the filesystem.
     *
     * @param registry the registry instance to be used.
     * @param callback the instance of a callback that can be used to determine the user's
     *                 preference before deleting an existing file or directory during operation. If
     *                 this parameter is null, the default behaviour of deleting the existing file
     *                 will be used.
     * @return whether file system updated
     *
     * @throws SynchronizationException if the operation failed.
     */
    public boolean execute(Registry registry, UserInputCallback callback) throws
            SynchronizationException {
        if (outputFile != null) {
            throw new SynchronizationException(MessageCode.OUTPUT_FILE_NOT_SUPPORTED);
        }

        // we are doing the update through a temp file. (so assumed enough spaces are there)
        File tempFile = null;
        boolean deleteTempFileFailed = false;
        FileWriter writer = null;
        try {
            try {
                tempFile = File.createTempFile(SynchronizationConstants.DUMP_META_FILE_NAME,
                        SynchronizationConstants.META_FILE_EXTENSION);
                try {
                    writer = new FileWriter(tempFile);
                    // doing the dump
                    registry.dump(checkOutPath, writer);
                } finally {
                    if (writer != null) {
                        writer.close();
                    }
                }
            } catch (RegistryException e) {
                throw new SynchronizationException(
                        MessageCode.ERROR_IN_DUMPING_NO_RESOURCE_OR_NO_PERMISSION, e,
                        new String[]{"path: " + checkOutPath, "username: " + username,
                                "registry url: " + registryUrl});
            } catch (IOException e) {
                throw new SynchronizationException(
                        MessageCode.ERROR_IN_CREATING_TEMP_FILE_FOR_DUMP);
            }
            // now read the xml stream from the file
            XMLStreamReader xmlReader = null;
            Reader reader = null;
            try {
                try {
                    reader = new FileReader(tempFile);
                    xmlReader = XMLInputFactory.newInstance().createXMLStreamReader(reader);
                    updateRecursively(xmlReader, workingLocation, checkOutPath, callback);
                } finally {
                    try {
                        if (xmlReader != null) {
                            xmlReader.close();
                        }
                    } finally {
                        if (reader != null) {
                            reader.close();
                        }
                    }
                }
            } catch (IOException e) {
                throw new SynchronizationException(
                        MessageCode.ERROR_IN_READING_TEMP_FILE_OF_DUMP);
            } catch (XMLStreamException e) {
                throw new SynchronizationException(
                        MessageCode.ERROR_IN_READING_STREAM_OF_TEMP_FILE_OF_DUMP);
            }
        } finally {
            if (tempFile != null) {
                // Our intention here is to delete the temporary file. We are not bothered whether
                // this operation fails.
                deleteTempFileFailed = !FileUtils.deleteQuietly(tempFile);
            }
        }
        if (deleteTempFileFailed) {
            throw new SynchronizationException(MessageCode.ERROR_IN_CLEANING_UP,
                    new String[]{"file path: " + tempFile.getAbsolutePath()});
        }

        if (cleanRegistry && registryUrl == null) {
            Utils.cleanEmbeddedRegistry();
        }

        return updated;
    }

    /**
     * This method will execute the update command utilizing the various parameters passed when
     * creating the instance of the command.
     *
     * @param registry the registry instance to be used.
     *
     * @return whether file system updated
     *
     * @throws SynchronizationException if the operation failed.
     */
    public boolean execute(Registry registry) throws SynchronizationException {
        return execute(registry, null);
    }

    // Performs a recursive update operation.
    private void updateRecursively(XMLStreamReader xmlReader,
                                   String filePath,
                                   String path,
                                   UserInputCallback callback)
            throws SynchronizationException, XMLStreamException {

        // we will first generate the axiom node from the reader,
        OMElement root = Utils.readMetaElement(xmlReader);
        // adding path and the registryUrl
        root.addAttribute("path", path, null);
        if (registryUrl != null) {
            root.addAttribute("registryUrl", registryUrl, null);
        }

        String isCollectionString = root.getAttributeValue(new QName("isCollection"));
        boolean isCollection = isCollectionString.equals("true");
        String name = root.getAttributeValue(new QName("name"));

        byte[] contentBytes = new byte[0];

        File file = new File(filePath);

        boolean isUpdating = false;
        boolean isConflicting = false;
        boolean collectionIsNotUpdated = false; // valid only for the collection.
        String updatingMD5 = null;
        Iterator children = root.getChildren();
        while (children.hasNext()) {
            OMElement child = (OMElement) children.next();
            String localName = child.getLocalName();

            // get content
            if (localName.equals("content")) {
                OMText text = (OMText) child.getFirstOMChild();
                // we keep content as base64 encoded
                if (text != null) {
                    contentBytes = Base64.decode(text.getText());
                }
                updatingMD5 = Utils.getMD5(contentBytes);
                root.addAttribute("md5", updatingMD5, null);
                child.detach();
            }
        }

        // access the meta info of the current
        String metaFilePath;

        if (isCollection) {
            metaFilePath = filePath + File.separator + SynchronizationConstants.META_DIRECTORY +
                    File.separator +
                    SynchronizationConstants.META_FILE_PREFIX +
                    SynchronizationConstants.META_FILE_EXTENSION;
        } else {
            String parentDirName = file.getAbsoluteFile().getParent();
            metaFilePath =
                    parentDirName + File.separator + SynchronizationConstants.META_DIRECTORY +
                            File.separator +
                            SynchronizationConstants.META_FILE_PREFIX +
                            Utils.encodeResourceName(name) +
                            SynchronizationConstants.META_FILE_EXTENSION;
        }

        File metaFile = new File(metaFilePath);

        if (file.exists()) {
            isUpdating = true;
            if (isCollection != file.isDirectory()) {
                throw new SynchronizationException(
                        MessageCode.COLLECTION_AND_RESOURCE_SAME_NAME,
                        new String[]{"file name: " + filePath});
            }

            if (metaFile.exists()) {
                // we need to check the last updated times of the current resource
                // and the updating resource

                OMElement updatingVersionElement = root.getFirstChildWithName(new QName("version"));
                if (updatingVersionElement == null) {
                    throw new SynchronizationException(MessageCode.CHECKOUT_OLD_VERSION,
                            new String[]{"missing element: version", "path: " + path});
                }
                String updatingVersionStr = updatingVersionElement.getText();

                // get the meta file OMElement
                OMElement metaFileElement = Utils.getOMElementFromMetaFile(metaFilePath);

                OMElement metaFileVersionElement =
                        metaFileElement.getFirstChildWithName(new QName("version"));

                String metaFileVersionStr;
                if (metaFileVersionElement == null) {
                    //Version not defined for the newly added files. They are added when updating.
                    metaFileVersionStr = updatingVersionStr;
                    root.addAttribute("version", metaFileVersionStr, null);
                    Utils.createMetaFile(metaFilePath, root);
                } else {
                    metaFileVersionStr = metaFileVersionElement.getText();
                }

                if (isCollection) {
                    if (metaFileVersionStr.equals(updatingVersionStr)) {
                        // so there is no server updates for the collection
                        Utils.createMetaFile(metaFilePath, root);
                        collectionIsNotUpdated = true;
                    }
                } else {
                    // here we not just check server side updates, but also check local changes using md5s
                    byte[] currentFileContent = Utils.getBytesFromFile(file);

                    String metaFileMD5 = metaFileElement.getAttributeValue(new QName("md5"));
                    String currentMD5 = Utils.getMD5(currentFileContent);

                    if (metaFileMD5 != null && metaFileMD5.equals(currentMD5)) {
                        // there is no modifications happens to the current file locally,
                        if (metaFileVersionStr.equals(updatingVersionStr)) {
                            // the file in the server is not updated, so just keep the current file locally.
                            // so we are only storing the meta information in the meta file.
                            Utils.createMetaFile(metaFilePath, root);
                            return;
                        }
                        // else:
                        // there is a server update to the file, so lets allow it to update the local one
                        // local one is not updated, so it will be overwritten by the server one
                    } else if (metaFileVersionStr.equals(updatingVersionStr)) {
                        // there is no server side changes, but there are client side changes,
                        // just don't update the content, but let the meta file get updated.
                        Utils.createMetaFile(metaFilePath, root);
                        return;
                    } else {
                        // this is the following scenario
                        // (!metaFileMD5.equals(currentMD5) &&
                        //    !metaFileVersionStr.equals(updatingVersionStr))
                        if (updatingMD5 != null && !updatingMD5.equals(currentMD5)) {
                            isConflicting = true;
                            root.addAttribute("md5", "", null);
                        }
                    }
                }
            } else if (!isCollection) {
                // if there is no meta file exists, that mean there is a conflict
                // a new resource is created both locally and in server
                isConflicting = true;
            }

            if (isConflicting && !isSilentUpdate && !ignoreConflicts) {
                // should rename the current file as file.mine
                String mineFileName = filePath + SynchronizationConstants.MINE_FILE_POSTFIX;
                File mineFile = new File(mineFileName);
                Utils.copy(file, mineFile);

                // updating the current file as versionedFileName
                String versionedFileName = filePath + SynchronizationConstants.SERVER_FILE_POSTFIX;
                file = new File(versionedFileName);

                // set the conflicting flag
                root.addAttribute("conflicting", "true", null);
            }
        } else if (isSilentUpdate) {
            // if no files exists locally, the silent update will return
            return;
        } else if (metaFile.exists()) {
            // now the meta file is there but the direct file is deleted, so we will
            // ask whether he want to get the up. Default behaviour is not to take the update from
            // the server. This is because the working copy is up-to-date.
            if (callback == null || callback.getConfirmation(new Message(
                    MessageCode.KEEP_DELETED_FILE,
                        new String[]{filePath}),
                        SynchronizationConstants.DELETE_CONFIRMATION_CONTEXT)) {
                return;
            }
        }
        if (!isUpdating) {
            try {
                // Create file if it does not exist
                if (isCollection) {
                    boolean ignore = file.mkdir(); // ignores the return value purposely
                } else {
                    boolean ignore = file.createNewFile(); // ignores the return value purposely
                }
            } catch (IOException e) {
                throw new SynchronizationException(MessageCode.FILE_CREATION_FAILED, e,
                        new String[]{"file name: " + filePath});
            }
        }

        if (!isCollection) {
            FileOutputStream fileOutputStream = null;
            try {
                boolean writeToFile = true;

                if (file.exists()) {
                    byte[] currentContentBytes = Utils.getBytesFromFile(file);
                    if (currentContentBytes != null && contentBytes != null) {
                        String currentContentMd5 = Utils.getMD5(currentContentBytes);
                        String writingContentMd5 = Utils.getMD5(contentBytes);
                        if (writingContentMd5 != null &&
                                writingContentMd5.equals(currentContentMd5)) {
                            writeToFile = false;
                        }
                    }
                }
                if (writeToFile) {
                    fileOutputStream = new FileOutputStream(file);
                    fileOutputStream.write(contentBytes);
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }

            } catch (IOException e) {
                throw new SynchronizationException(MessageCode.PROBLEM_IN_CREATING_CONTENT,
                        e,
                        new String[]{"file name: " + filePath});
            }finally {
                try {
                    if(fileOutputStream!=null){
                        fileOutputStream.close();
                    }
                } catch (IOException e) {
                    log.error("Failed to close the stream" ,e);
                }
            }
        } else {
            // creating the meta directory
            String metaDirectoryName =
                    filePath + File.separator + SynchronizationConstants.META_DIRECTORY;
            File metaDirectory = new File(metaDirectoryName);
            if (!metaDirectory.exists() && !metaDirectory.mkdir()) {
                throw new SynchronizationException(MessageCode.ERROR_CREATING_META_FILE,
                        new String[]{"file: " + metaDirectoryName});
            }
        }

        boolean iterateChildren = true;

        if (!xmlReader.hasNext() || !(xmlReader.isStartElement() &&
                xmlReader.getLocalName().equals("children"))) {
            // finished the recursion
            // consuming the stream until the resource end element found
            while (xmlReader.hasNext() && !(xmlReader.isEndElement() &&
                    xmlReader.getLocalName().equals("resource"))) {
                xmlReader.next();
            }
            iterateChildren = false;
        }
        if (iterateChildren) {
            do {
                xmlReader.next();
                if (xmlReader.isEndElement() && xmlReader.getLocalName().equals("children")) {
                    // this means empty children, just quit from here
                    // before that we have to set the cursor to the end of the current resource
                    if (xmlReader.hasNext()) {
                        do {
                            xmlReader.next();
                        } while (xmlReader.hasNext() && !(xmlReader.isEndElement() &&
                                xmlReader.getLocalName().equals("resource")));
                    }
                    iterateChildren = false;
                    break;
                }
            } while (!xmlReader.isStartElement() && xmlReader.hasNext());
        }

        Map<String, Boolean> childNames = new HashMap<String, Boolean>();

        if (iterateChildren) {
            while (xmlReader.hasNext() && xmlReader.isStartElement() &&
                    xmlReader.getLocalName().equals("resource")) {
                // prepare the children absolute path

                String childName = xmlReader.getAttributeValue(null, "name");
                String fileResourceName = childName;
                String childFilePath = filePath + File.separator + fileResourceName;
                String childPath = (path.equals("/") ? "" : path) + "/" + childName;

                updateRecursively(xmlReader, childFilePath, childPath, callback);
                childNames.put(fileResourceName, true);


                while ((!xmlReader.isStartElement() && xmlReader.hasNext()) &&
                        !(xmlReader.isEndElement() &&
                                xmlReader.getLocalName().equals("children"))) {
                    xmlReader.next();
                }
                if (xmlReader.isEndElement() && xmlReader.getLocalName().equals("children")) {
                    // we are in the end of the children tag.
                    break;
                }
            }
            // consuming the stream until the resource end element found
            while (xmlReader.hasNext() && !(xmlReader.isEndElement() &&
                    xmlReader.getLocalName().equals("resource"))) {
                xmlReader.next();
            }

            // now we are checking which files have been deleted at the server end.
            String[] childFileNames = file.list();
            if (childFileNames != null) {
                for (String childFileName : childFileNames) {
                    if (childFileName.equals(SynchronizationConstants.META_DIRECTORY)) {
                        continue;
                    }
                    if (childNames.get(childFileName) != null && childNames.get(childFileName)) {
                        // this files stays on the server as well, so nothing to worry
                        continue;
                    }
                    // hm, we have a situation that stuff exist local, but not at the server
                    // first need to check whether they are newly added.
                    // we can do that by checking the existence of meta directory
                    String childFilePath = file + File.separator + childFileName;
                    File childFile = new File(file, childFileName);
                    boolean shouldDelete = false;
                    File childMetaFile;
                    if (childFile.isDirectory()) {
                        // the meta directory should exist in .meta
                        String metaDirName =
                                filePath + File.separator + childFileName + File.separator +
                                        SynchronizationConstants.META_DIRECTORY + File.separator +
                                        File.separator + SynchronizationConstants.META_FILE_PREFIX +
                                        SynchronizationConstants.META_FILE_EXTENSION;
                        childMetaFile = new File(metaDirName);

                        if (childMetaFile.exists()) {
                            // looks like it's bean earlier checkout from registry, mean it is now deleted
                            shouldDelete = true;
                        }
                    } else {
                        String metaFileName =
                                filePath + File.separator +
                                        SynchronizationConstants.META_DIRECTORY + File.separator +
                                        SynchronizationConstants.META_FILE_PREFIX +
                                        Utils.encodeResourceName(childFileName) +
                                        SynchronizationConstants.META_FILE_EXTENSION;
                        childMetaFile = new File(metaFileName);
                        if (childMetaFile.exists()) {
                            // looks like it's bean earlier checkout from registry, mean it is now deleted
                            shouldDelete = true;
                        }
                    }
                    if (shouldDelete && !isSilentUpdate) {
                        deleteFile(callback, childFilePath, childFile, childMetaFile);
                    }
                }
            }
        }else {
//            Seems like the user has deleted all the resources under a particular collection

            String[] allFilesNames = file.list();
            if (allFilesNames != null) {
                for (String filesName : allFilesNames) {
                    String childFilePath;
                    File childFile;
                    File childMetaFile;

                    childFilePath = filePath + File.separator + filesName;
                    childFile = new File(childFilePath);
                    if (childFile.isDirectory()) {
    //                    Here we are deleting all the collections that were deleted from the server
                        String childMetaCollectionPath = childFilePath + File.separator
                                + SynchronizationConstants.META_DIRECTORY;
                        childMetaFile = new File(childMetaCollectionPath);
                        if (childMetaFile.exists() && !isSilentUpdate) {
                            deleteFile(callback, childFilePath, childFile, childMetaFile);
                        }
                    }else{
    //                    Here we remove all the resource that have been deleted from the server
                        String metaFileFullName =
                                filePath + File.separator +
                                        SynchronizationConstants.META_DIRECTORY + File.separator +
                                        SynchronizationConstants.META_FILE_PREFIX +
                                        Utils.encodeResourceName(filesName) +
                                        SynchronizationConstants.META_FILE_EXTENSION;

                        childMetaFile = new File(metaFileFullName);
                        if (childMetaFile.exists() && !isSilentUpdate) {
                            deleteFile(callback, childFilePath, childFile, childMetaFile);
                        }
                    }
                }
            }
        }

        if (file.isDirectory() && collectionIsNotUpdated) {
            return;
        }

        // creating the meta file
        String metaFileName;
        if (isCollection) {
            metaFileName = filePath + File.separator + SynchronizationConstants.META_DIRECTORY +
                    File.separator + SynchronizationConstants.META_FILE_PREFIX +
                    SynchronizationConstants.META_FILE_EXTENSION;
        } else {
            String parentDirName = file.getAbsoluteFile().getParent();
            metaFileName =
                    parentDirName + File.separator + SynchronizationConstants.META_DIRECTORY +
                            File.separator + SynchronizationConstants.META_FILE_PREFIX +
                            Utils.encodeResourceName(name) +
                            SynchronizationConstants.META_FILE_EXTENSION;
        }
        Utils.createMetaFile(metaFileName, root);

        // printing out the information of the file
        if (isConflicting) {
            if (callback != null && !isSilentUpdate) {
                callback.displayMessage(new Message(MessageCode.CONFLICTED,
                        new String[]{refinedPathToPrint(filePath)}));
            }
            conflictedCount++;
        } else if (isUpdating) {
            if (callback != null && !isSilentUpdate) {
                callback.displayMessage(new Message(MessageCode.UPDATED,
                        new String[]{refinedPathToPrint(filePath)}));
            }
            updatedCount++;
            updated = true;
        } else {
            if (callback != null && !isSilentUpdate) {
                callback.displayMessage(new Message(MessageCode.ADDED,
                        new String[]{refinedPathToPrint(filePath)}));
            }
            addedCount++;
            updated = true;
        }
    }

    private void deleteFile(UserInputCallback callback, String childFilePath, File childFile, File childMetaFile) throws SynchronizationException {
        boolean isDeleted = Utils.confirmDelete(childFile, childMetaFile, callback);
        if (isDeleted) {
            if (callback != null && !isSilentUpdate) {
                callback.displayMessage(new Message(MessageCode.DELETED,
                        new String[]{refinedPathToPrint(childFilePath)}));
            }
            deletedCount++;
            updated = true;
        } else {
            if (callback != null && !isSilentUpdate) {
                callback.displayMessage(new Message(MessageCode.NOT_DELETED,
                        new String[]{refinedPathToPrint(childFilePath)}));
            }
            notDeletedCount++;
        }
    }

    // Method to obtain a refined path from the given path. In here, ".." will be replaced with the
    // correct name of the directory.
    private static String refinedPathToPrint(String path) {
        StringTokenizer tokenizer = new StringTokenizer(path, "/");

        StringBuilder refinedPath = new StringBuilder("");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            // if .. found ignore the next element
            if (!token.equals("..")) {
                refinedPath.append(token);
                if (tokenizer.hasMoreElements()) {
                    refinedPath.append("/");
                }
            }
        }
        return refinedPath.toString();
    }
}
