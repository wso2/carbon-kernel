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

import org.apache.axiom.om.*;
import org.apache.axiom.om.util.Base64;
import org.apache.commons.io.FileUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.DumpConstants;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.synchronization.SynchronizationConstants;
import org.wso2.carbon.registry.synchronization.SynchronizationException;
import org.wso2.carbon.registry.synchronization.UserInputCallback;
import org.wso2.carbon.registry.synchronization.Utils;
import org.wso2.carbon.registry.synchronization.message.Message;
import org.wso2.carbon.registry.synchronization.message.MessageCode;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipInputStream;

/**
 * This command is used to perform a check-in operation which will upload the files and directories
 * from the local filesystem into the provided registry instance.
 */
@SuppressWarnings({"ResultOfMethodCallIgnored", "unused"})
public class CheckInCommand {

    private static boolean ignoreConflicts = Boolean.parseBoolean(
            System.getProperty(SynchronizationConstants.REGISTRY_IGNORE_CONFLICTS, "false"));

    private String inputFile = null;
    private String workingLocation = null;
    private String registryUrl = null;
    private String checkInPath = null;
    private String username = null;
    private boolean silentUpdate = true;
    private boolean cleanRegistry = false;
    private boolean updated = false;

    ////////////////////////////////////////////////////////
    // Fields maintaining status of command execution
    ////////////////////////////////////////////////////////

    private int sentCount = -1;

    /**
     * Creates an instance of a check-in command which can be executed against a provided registry
     * instance.
     *
     * @param inputFile          if the content is to be uploaded from a single meta file, this
     *                           parameter can be used to specify the path to the meta file.
     * @param workingLocation         if the content is to be uploaded from a directory on the
     *                           filesystem, this parameter can be used to specify the path to the
     *                           corresponding location.
     * @param userUrl            aggregate URL containing a concatenation of the registry URL and
     *                           the resource path that is capable of referencing a remote resource.
     *                           This url will contain only the resource path if the resource was
     *                           local to the given registry instance.
     * @param username           the name of the user (which should be a valid username on the
     *                           target server on which the provided registry instance is running)
     *                           that performs this operation.
     * @param silentUpdate       to ignore the conflicts and perform silent update on the server
     *                           side.
     * @param cleanRegistry      whether the embedded registry instance must be cleaned after the
     *                           execution of the operation.
     * @param testContentChanged when this parameter is set to true, check-in will only happen if
     *                           the content has changed.
     *
     * @throws SynchronizationException if the operation failed.
     */
    public CheckInCommand(String inputFile,
                          String workingLocation,
                          String userUrl,
                          String username,
                          boolean silentUpdate,
                          boolean cleanRegistry,
                          boolean testContentChanged) throws SynchronizationException {
        // now if the user url is different to the registry url we are going to consider that as
        // well.

        this.inputFile = inputFile;
        if(workingLocation.endsWith(File.separator)){
            workingLocation = workingLocation.substring(0, workingLocation.length() -1);
        }
        this.workingLocation = workingLocation;
        String userUrl1 = userUrl;
        this.username = username;
        this.silentUpdate = silentUpdate;
        this.cleanRegistry = cleanRegistry;
        boolean testContentChanged1 = testContentChanged;

        // get the update details form the meta element of the current checkout
        OMElement metaOMElement = Utils.getMetaOMElement(workingLocation);
        if (metaOMElement != null) {
            checkInPath = metaOMElement.getAttributeValue(new QName(DumpConstants.RESOURCE_PATH));
        }

        if (userUrl != null) {
            registryUrl = Utils.getRegistryUrl(userUrl);
            String suggestedCheckInPath = Utils.getPath(userUrl);
            if (suggestedCheckInPath == null || suggestedCheckInPath.equals("")) {
                suggestedCheckInPath = "/";
                // we are converting the root path to the current directory of the file system
            }
            if (!suggestedCheckInPath.equals(checkInPath)) {
                testContentChanged1 = false;
                checkInPath = suggestedCheckInPath;
            }
        } else {
            if (metaOMElement == null) {
                throw new SynchronizationException(MessageCode.CHECKOUT_BEFORE_CHECK_IN);
            }
            registryUrl = metaOMElement.getAttributeValue(new QName("registryUrl"));
        }
    }

    /**
     * Method to obtain the count of files sent.
     *
     * @return the count of files sent.
     */
    public int getSentCount() {
        return sentCount;
    }

    /**
     * This method will execute the check-in command utilizing the various parameters passed when
     * creating the instance of the command. This method accepts the users preference if a deletion
     * of a file or directory is required in the process.
     *
     * @param registry the registry instance to be used.
     * @param callback the instance of a callback that can be used to determine the user's
     *                 preference before deleting an existing file or directory during the update
     *                 after the check-in has been done. If this parameter is null, the default
     *                 behaviour of deleting the existing file will be used.
     * @return whether the resources updated in the registry
     *
     * @throws SynchronizationException if the operation failed.
     */
    public boolean execute(Registry registry, UserInputCallback callback)
            throws SynchronizationException {
        if (inputFile != null) {
            // restore a single file.
            restoreFromFile(registry);
        } else {
            restoreFromFileSystem(registry, callback);
        }
        return updated;
    }

    /**
     * This method will execute the check-in command utilizing the various parameters passed when
     * creating the instance of the command.
     *
     * @param registry the registry instance to be used.
     *
     * @return whether the resources updated in the registry
     *
     * @throws SynchronizationException if the operation failed.
     */
    public boolean execute(Registry registry) throws SynchronizationException {
        return execute(registry, null);
    }

    // Restores the given resources and collections from a dump file.
    private void restoreFromFile(Registry registry) throws SynchronizationException {
        String workingLocation = this.workingLocation;

        if (workingLocation != null) {
            inputFile = workingLocation + File.separator + inputFile;
        }

        // do the restoring
        try {
            ZipInputStream zis = new ZipInputStream(new FileInputStream(inputFile));
            zis.getNextEntry();
            Reader reader = new InputStreamReader(zis, StandardCharsets.UTF_8);
            registry.restore(checkInPath, reader);
        } catch (FileNotFoundException e) {
            throw new SynchronizationException(MessageCode.FILE_DOES_NOT_EXIST, e,
                    new String[]{"Output file" + inputFile});
        } catch (Exception e) {
            if(e.getCause() instanceof UnknownHostException) {
                  throw new SynchronizationException(MessageCode.ERROR_IN_CONNECTING_REGISTRY, e,
                        new String[] {" registry url:" + registryUrl});
            }
            throw new SynchronizationException(MessageCode.ERROR_IN_RESTORING, e,
                    new String[]{"path: " + checkInPath,
                            "registry url: " + registryUrl,
                            "username: " + username});
        }

        if (cleanRegistry && registryUrl == null) {
            Utils.cleanEmbeddedRegistry();
        }
    }

    // Restores the given resources and collections from files and folders on the filesystem.
    private void restoreFromFileSystem(Registry registry, UserInputCallback callback)
            throws SynchronizationException {
        sentCount = 0;

        // we are doing the check-in through a temp file. (so assumed enough spaces are there)
        File tempFile = null;
        boolean deleteTempFileFailed = false;
        XMLStreamWriter xmlWriter = null;
        Writer writer = null;
        try {
            try {
                tempFile = File.createTempFile(SynchronizationConstants.DUMP_META_FILE_NAME,
                        SynchronizationConstants.META_FILE_EXTENSION);

                try {
                    writer = new FileWriter(tempFile);
                    // wrap the writer with an xml stream writer
                    xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(writer);
                    // prepare the dump xml
                    xmlWriter.writeStartDocument();
                    createMetaElement(xmlWriter, workingLocation, checkInPath, callback);
                    xmlWriter.writeEndDocument();
                } finally {
                    try {
                        if (xmlWriter != null) {
                            xmlWriter.close();
                        }
                    } finally {
                        if (writer != null) {
                            writer.close();
                        }
                    }
                }
            } catch (IOException e) {
                throw new SynchronizationException(
                        MessageCode.ERROR_IN_CREATING_TEMP_FILE_FOR_DUMP,
                        e);
            } catch (XMLStreamException e) {
                throw new SynchronizationException(
                        MessageCode.ERROR_IN_CREATING_XML_STREAM_WRITER, e);
            }

            // do the restoring if the file system is actually updated
            if(updated) {
                try {
                    Reader reader = null;
                    try {
                        reader = new FileReader(tempFile);
                        registry.restore(checkInPath, reader);
                    } finally {
                        if (reader != null) {
                            reader.close();
                        }
                    }
                } catch (IOException e) {
                    throw new SynchronizationException(
                            MessageCode.ERROR_IN_READING_TEMP_FILE_OF_DUMP, e);
                } catch (RegistryException e) {
                    throw new SynchronizationException(MessageCode.ERROR_IN_RESTORING, e,
                            new String[]{"path: " + checkInPath,
                                    "registry url: " + registryUrl,
                                    "username: " + username});
                }
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
    }

    // Creates the dump element from the given file or directory.
    private void createMetaElement(XMLStreamWriter xmlWriter, String filePath, String path,
                                   UserInputCallback callback)
            throws SynchronizationException, XMLStreamException {
        File file = new File(filePath);
        if (file.isDirectory()) {
            createDirectoryMetaElement(xmlWriter, filePath, path, callback);
        } else {
            String metaFilePath = Utils.getMetaFilePath(file.getPath());
            createResourceMetaElement(xmlWriter, file, metaFilePath, path, callback);
        }
    }

    // Creates the dump element from the given directory. If the path is not given it is retrieved
    // from the meta file.
    private void createDirectoryMetaElement(XMLStreamWriter xmlWriter, String filePath, String path,
                                            UserInputCallback callback)
            throws SynchronizationException, XMLStreamException {
        // first get the meta file of the directory.
        String metaDirectoryPath = filePath + File.separator +
                SynchronizationConstants.META_DIRECTORY;
        String metaFilePath = metaDirectoryPath + File.separator +
                SynchronizationConstants.META_FILE_PREFIX +
                SynchronizationConstants.META_FILE_EXTENSION;

        // confirm the existence of the meta file.
        OMElement metaElement = Utils.getOMElementFromMetaFile(metaFilePath);
        if (metaElement == null) {
            return;
        }

        // alerting non-backward compatibility...
        String checkoutPathAttribute = metaElement.getAttributeValue(new QName("checkoutPath"));
        if (checkoutPathAttribute != null) {
            throw new SynchronizationException(MessageCode.CHECKOUT_OLD_VERSION);
        }

        // we are re-adjusting the name of the resource to make sure the file name and the resource
        // name is equal
        String resourceName = RegistryUtils.getResourceName(path);
        metaElement.addAttribute(DumpConstants.RESOURCE_NAME, resourceName, null);

        OMFactory factory = OMAbstractFactory.getOMFactory();

        OMAttribute status;
        if((status = metaElement.getAttribute(new QName(DumpConstants.RESOURCE_STATUS))) != null){
            if(DumpConstants.RESOURCE_ADDED.equals(status.getAttributeValue())){
                metaElement = Utils.updateDefaultAddMetaFile(metaElement, path, username, true);
            }
        }

        if(status != null && !DumpConstants.RESOURCE_DELETED.equals(status.getAttributeValue())){
            metaElement.removeAttribute(factory.createOMAttribute(DumpConstants.RESOURCE_STATUS, null, status.getAttributeValue()));
            Utils.updateMetaFile(metaFilePath, metaElement);
            metaElement.addAttribute(DumpConstants.RESOURCE_STATUS, status.getAttributeValue(), null);
        }

        // now write the meta data of the meta element to the writer (except children)
        Utils.writeMetaElement(xmlWriter, metaElement);
        if(status != null){
            if (callback != null) {
                callback.displayMessage(new Message(MessageCode.SENT, new String[]{filePath}));
            }
            sentCount++;
            updated = true;
        }

        // now add the child element to the meta element
        xmlWriter.writeStartElement(DumpConstants.CHILDREN);

        File metaDirFile = new File(metaDirectoryPath);
        String[] metaFiles = metaDirFile.list(new FilenameFilter(){
            public boolean accept(File file, String s) {
                if(!s.equals("~.xml")){
                    return true;
                }
                return false;
            }
        });
        if (metaFiles != null) {
            for(String childMetaFileName : metaFiles){
                String childFileName = Utils.decodeFilename(childMetaFileName.
                        substring(1, childMetaFileName.length() - 4));
                String childFilePath = metaDirFile.getParent() + File.separator + childFileName;
                createResourceMetaElement(xmlWriter, new File(childFilePath),
                        metaDirectoryPath + File.separator + childMetaFileName, path, callback);
            }
        }


        File directory = new File(filePath);
        String[] childrenNames = directory.list(new FilenameFilter() {
            public boolean accept(File file, String s) {
                if(file.isDirectory()){
                    if(s.equals(SynchronizationConstants.META_DIRECTORY)) {
                        return false;
                    }
                    return true;
                }
                return false;
            }
        });
        if (childrenNames != null) {
            for (String childFileName : childrenNames) {
                // Get childFileName of file or directory
                String childResourceName = Utils.decodeFilename(childFileName);

                if (childResourceName.endsWith(SynchronizationConstants.MINE_FILE_POSTFIX) ||
                        childResourceName.endsWith(SynchronizationConstants.SERVER_FILE_POSTFIX)) {
                    // there is an conflicts
                    throw new SynchronizationException(MessageCode.RESOLVE_CONFLICTS);
                }

                String childPath = path + "/" + childResourceName;
                String childFilePath = filePath + File.separator + childFileName;
                createDirectoryMetaElement(xmlWriter, childFilePath, childPath, callback);
            }
        }

        if(status != null && DumpConstants.RESOURCE_DELETED.equals(status.getAttributeValue())){
            FileUtils.deleteQuietly(directory);
        }

        xmlWriter.writeEndElement(); // to end children tag.
        xmlWriter.writeEndElement(); // to end resource tag.
        xmlWriter.flush();
    }

    private void createResourceMetaElement(XMLStreamWriter xmlWriter,
                                           File resourceFile,
                                           String metaFilePath,
                                           String path,
                                           UserInputCallback callback)
            throws XMLStreamException, SynchronizationException {
        String fileName = resourceFile.getName();
        String filePath = resourceFile.getPath();
        // confirm the existence of the meta file.
        OMElement metaElement = Utils.getOMElementFromMetaFile(metaFilePath);
        if (metaElement == null) {
            return;
        }
        // we are re-adjusting the name of the resource to make sure the file name and the
        // resource name is equal
        metaElement.addAttribute(DumpConstants.RESOURCE_NAME, fileName, null);
        if (!silentUpdate && !ignoreConflicts) {
            // we only set the ignoreConflicts attribute if it is failing. This will enforce a
            // check for conflicts at the server side.
            metaElement.addAttribute(DumpConstants.IGNORE_CONFLICTS, "false", null);
        }

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMAttribute status;
        if((status = metaElement.getAttribute(new QName(DumpConstants.RESOURCE_STATUS))) != null){
            if(DumpConstants.RESOURCE_ADDED.equals(status.getAttributeValue())){
                metaElement = Utils.updateDefaultAddMetaFile(metaElement,
                        path + File.separator + fileName, username, false);
            } else if(DumpConstants.RESOURCE_DELETED.equals(status.getAttributeValue())){
                FileUtils.deleteQuietly(new File(metaFilePath));
                FileUtils.deleteQuietly(resourceFile);
            }
        }  else if(Utils.fileContentChanged(resourceFile)) {
            status = metaElement.addAttribute(
                    DumpConstants.RESOURCE_STATUS, DumpConstants.RESOURCE_UPDATED, null);
        }

        if(status != null && !DumpConstants.RESOURCE_DELETED.equals(status.getAttributeValue())){
            metaElement.removeAttribute(factory.createOMAttribute(
                    DumpConstants.RESOURCE_STATUS, null, status.getAttributeValue()));
            metaElement.addAttribute("md5", Utils.getMD5(resourceFile), null);
            Utils.updateMetaFile(metaFilePath, metaElement);
            metaElement.addAttribute(DumpConstants.RESOURCE_STATUS, status.getAttributeValue(), null);
        }

        // now write the meta data of the meta element to the writer (except children)
        Utils.writeMetaElement(xmlWriter, metaElement);
        if(status != null){
            if (callback != null) {
                callback.displayMessage(new Message(MessageCode.SENT, new String[]{filePath}));
            }
            sentCount++;
            updated = true;
        }

        // adding the content if resource is not deleted
        if(status == null ||
                (status != null &&
                        !DumpConstants.RESOURCE_DELETED.equals(status.getAttributeValue()))){
            byte[] content = Utils.getBytesFromFile(resourceFile);
            String encodedContent = Base64.encode(content);

            OMElement contentEle = factory.createOMElement(new QName(DumpConstants.CONTENT));
            OMText contentText = factory.createOMText(encodedContent);
            contentEle.addChild(contentText);
            contentEle.serialize(xmlWriter);
        }
        xmlWriter.writeEndElement(); // to end resource tag.
        xmlWriter.flush();
    }
}
