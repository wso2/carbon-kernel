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
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.DumpConstants;
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
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * This command is used to perform a check-out operation which will download the resources and
 * collections from the provided registry instance into the local filesystem.
 */
@SuppressWarnings({"ResultOfMethodCallIgnored", "unused"})
public class CheckOutCommand {

    private String outputFile = null;
    private String checkOutPath = null;
    private String registryUrl = null;
    private String workingDir = null;
    private String username = null;
    private boolean cleanRegistry = false;

    ////////////////////////////////////////////////////////
    // Fields maintaining status of command execution
    ////////////////////////////////////////////////////////

    private int addedCount = -1;
    private int overwrittenCount = 0;
    private int nonOverwrittenCount = 0;

    /**
     * Creates an instance of a check-out command which can be executed against a provided registry
     * instance.
     *
     * @param outputFile    if the content is to be downloaded into a single meta file, this
     *                      parameter can be used to specify the path to the meta file.
     * @param workingDir    if the content is to be downloaded into a directory on the filesystem,
     *                      this parameter can be used to specify the path to the corresponding
     *                      location.
     * @param userUrl       aggregate URL containing a concatenation of the registry URL and the
     *                      resource path that is capable of referencing a remote resource. This url
     *                      will contain only the resource path if the resource was local to the
     *                      given registry instance.
     * @param username      the name of the user (which should be a valid username on the target
     *                      server on which the provided registry instance is running) that performs
     *                      this operation.
     * @param cleanRegistry whether the embedded registry instance must be cleaned after the
     *                      execution of the operation.
     *
     * @throws SynchronizationException if the operation failed.
     */
    public CheckOutCommand(String outputFile,
                           String workingDir,
                           String userUrl,
                           String username,
                           boolean cleanRegistry) throws SynchronizationException {

        this.outputFile = outputFile;
        this.workingDir = workingDir;
        this.username = username;
        this.cleanRegistry = cleanRegistry;

        if (userUrl == null) {
            throw new SynchronizationException(MessageCode.CO_PATH_MISSING);
        }

        // derive the registry url and the path
        registryUrl = Utils.getRegistryUrl(userUrl);
        checkOutPath = Utils.getPath(userUrl);

        if (checkOutPath == null || checkOutPath.equals("")) {
            checkOutPath = "/";
            // we are converting the root path to the current directory of the file system
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
     * Method to obtain the count of files overwritten.
     *
     * @return the count of files overwritten.
     */
    public int getOverwrittenCount() {
        return overwrittenCount;
    }

    /**
     * Method to obtain the count of files that were not overwritten.
     *
     * @return the count of files that were not overwritten.
     */
    public int getNonOverwrittenCount() {
        return nonOverwrittenCount;
    }

    /**
     * This method will execute the check-out command utilizing the various parameters passed when
     * creating the instance of the command. This method accepts the users preference to whether a
     * file or directory should be overwritten on the filesystem.
     *
     * @param registry the registry instance to be used.
     * @param callback the instance of a callback that can be used to determine the user's
     *                 preference before overwriting an existing file or directory during operation.
     *                 If this parameter is null, the default behaviour of overwriting the existing
     *                 file will be used.
     * @return whether checkout succeeded
     *
     * @throws SynchronizationException if the operation failed.
     */
    public boolean execute(Registry registry, UserInputCallback callback)
            throws SynchronizationException {
        if (outputFile != null) {
            dumpToFile(registry);
        } else {
            dumpToFileSystem(registry, callback);
        }
        return true;
    }

    /**
     * This method will execute the check-out command utilizing the various parameters passed when
     * creating the instance of the command.
     *
     * @param registry the registry instance to be used.
     *
     * @return whether checkout succeeded
     *
     * @throws SynchronizationException if the operation failed.
     */
    public boolean execute(Registry registry) throws SynchronizationException {
        return execute(registry, null);
    }

    // Downloads the given resources and collections into a dump file.
    private void dumpToFile(Registry registry) throws SynchronizationException {

        String outputXml = outputFile + SynchronizationConstants.META_FILE_EXTENSION;
        if (workingDir != null) {
            outputFile = workingDir + File.separator + outputFile;
        }

        try {
            if (!registry.resourceExists(checkOutPath)) {
                throw new SynchronizationException(
                        MessageCode.ERROR_IN_DUMPING_NO_RESOURCE_OR_NO_PERMISSION,
                        new String[]{"path: " + checkOutPath, "username: " + username});
            }
        } catch (Exception e) {
            if(e.getCause() instanceof UnknownHostException) {
                  throw new SynchronizationException(MessageCode.ERROR_IN_CONNECTING_REGISTRY, e,
                        new String[] {" registry url:" + registryUrl});
            }
            throw new SynchronizationException(MessageCode.ERROR_IN_DUMPING_AUTHORIZATION_FAILED, e,
                    new String[]{"path: " + checkOutPath, "username: " + username});
        }

        try {
            // we don't care what is dumping..
            // doing the dump
            ZipOutputStream zos = new
                    ZipOutputStream(new FileOutputStream(outputFile));
            ZipEntry ze = new ZipEntry(outputXml);
            ze.setMethod(ZipEntry.DEFLATED);
            zos.putNextEntry(ze);


            Writer zipWriter = new OutputStreamWriter(zos);

            registry.dump(checkOutPath, zipWriter);

            zos.close();
        } catch (Exception e) {
            throw new SynchronizationException(MessageCode.ERROR_IN_DUMPING, e,
                    new String[]{"path: " + checkOutPath, "username: " + username});
        }

        if (cleanRegistry && registryUrl == null) {
            Utils.cleanEmbeddedRegistry();
        }
    }

    // Downloads the given resources and collections into files and folders on the filesystem.
    private void dumpToFileSystem(Registry registry, UserInputCallback callback)
            throws SynchronizationException {
        addedCount = 0;

        // first try getting the path and confirmed it is a collection
        Resource r;
        try {
            r = registry.get(checkOutPath);
        } catch (Exception e) {
            throw new SynchronizationException(
                    MessageCode.ERROR_IN_DUMPING_NO_RESOURCE_OR_NO_PERMISSION,
                    new String[]{"path: " + checkOutPath, "username: " + username});
        }
        if (!(r instanceof Collection)) {
            throw new SynchronizationException(MessageCode.DUMPING_NON_COLLECTION,
                    new String[]{"path: " + checkOutPath, "username: " + username});
        }

        // we are doing the checkout through a temp file. (so assumed enough spaces are there)
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
                        MessageCode.ERROR_IN_DUMPING_NO_RESOURCE_OR_NO_PERMISSION,
                        new String[]{"path: " + checkOutPath, "username: " + username});
            } catch (IOException e) {
                throw new SynchronizationException(
                        MessageCode.ERROR_IN_CREATING_TEMP_FILE_FOR_DUMP,
                        e);
            }
            // now read the xml stream from the file
            XMLStreamReader xmlReader = null;
            Reader reader = null;
            try {
                try {
                    reader = new FileReader(tempFile);
                    xmlReader = XMLInputFactory.newInstance().createXMLStreamReader(reader);
                    checkOutRecursively(xmlReader, workingDir, checkOutPath, callback);
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
                        MessageCode.ERROR_IN_READING_TEMP_FILE_OF_DUMP, e);
            } catch (XMLStreamException e) {
                throw new SynchronizationException(
                        MessageCode.ERROR_IN_READING_STREAM_OF_TEMP_FILE_OF_DUMP, e);
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

    // Performs a recursive check-out operation.
    private void checkOutRecursively(XMLStreamReader xmlReader,
                                     String filePath,
                                     String path,
                                     UserInputCallback callback)
            throws SynchronizationException, XMLStreamException {
        // we will first generate the axiom node from the reader,
        OMElement root = Utils.readMetaElement(xmlReader);
        // adding path and the registryUrl
        root.addAttribute(DumpConstants.RESOURCE_PATH, path, null);
        if (registryUrl != null) {
            root.addAttribute("registryUrl", registryUrl, null);
        }

        String isCollectionString = root.getAttributeValue(new QName(DumpConstants.RESOURCE_IS_COLLECTION));
        boolean isCollection = isCollectionString.equals("true");

        String name = root.getAttributeValue(new QName(DumpConstants.RESOURCE_NAME));

        byte[] contentBytes = new byte[0];
        File file = new File(filePath);
        boolean overwrite = true;
        boolean fileAlreadyExist = false;

        String parentDirName = file.getAbsoluteFile().getParent();

        String metaDirectoryName;
        String metaFilePath;
        if (isCollection) {
            metaDirectoryName = filePath + File.separator + SynchronizationConstants.META_DIRECTORY;
            metaFilePath = filePath + File.separator + SynchronizationConstants.META_DIRECTORY +
                    File.separator +
                    SynchronizationConstants.META_FILE_PREFIX +
                    SynchronizationConstants.META_FILE_EXTENSION;
        } else {
            metaDirectoryName =
                    parentDirName + File.separator + SynchronizationConstants.META_DIRECTORY;
            metaFilePath =
                    parentDirName + File.separator + SynchronizationConstants.META_DIRECTORY +
                            File.separator + SynchronizationConstants.META_FILE_PREFIX +
                            Utils.encodeResourceName(name) +
                            SynchronizationConstants.META_FILE_EXTENSION;
        }

        if(file.exists()){
            fileAlreadyExist = true;
        }

        if (!isCollection && fileAlreadyExist) {
            if (!Utils.resourceUpdated(metaFilePath, root) || (callback != null &&
                    !callback.getConfirmation(new Message(
                            MessageCode.FILE_OVERWRITE_CONFIRMATION,
                            new String[]{filePath}),
                            SynchronizationConstants.OVERWRITE_CONFIRMATION_CONTEXT))) {
                overwrite = false;
            }
        }
        try {
            // Create file if it does not exist
            if (isCollection) {
                if(!fileAlreadyExist && Utils.resourceUpdated(metaFilePath, root)){
                    boolean ignore = file.mkdir(); // ignores the return value purposely
                } else {
                    overwrite = false;
                }
            } else if (overwrite) {
                boolean ignore = file.createNewFile(); // ignores the return value purposely
            }
        } catch (IOException e) {
            throw new SynchronizationException(MessageCode.FILE_CREATION_FAILED, e,
                    new String[]{
                            "file: " + filePath});
        }

        // we are extracting the content from the meta element.
        Iterator children = root.getChildren();
        while (children.hasNext()) {
            OMElement child = (OMElement) children.next();
            String localName = child.getLocalName();

            // LastModified
            if (localName.equals(DumpConstants.LAST_MODIFIED)) {
                OMText text = (OMText) child.getFirstOMChild();
                if (text != null) {
                    long date = Long.parseLong(text.getText());
                    // We are not bothered whether this failed to set the last-modified time. If we
                    // cannot modify the file, we would fail when attempting to write to it anyway.
                    boolean ignore = file.setLastModified(date);
                }
            }
            // get content
            else if (localName.equals(DumpConstants.CONTENT)) {
                OMText text = (OMText) child.getFirstOMChild();
                // we keep content as base64 encoded
                if (text != null) {
                    contentBytes = Base64.decode(text.getText());
                }
                String md5 = Utils.getMD5(contentBytes);
                root.addAttribute("md5", md5, null);
                child.detach();
            }
        }

        if (!isCollection && overwrite) {
            try {
                FileOutputStream fileStream = null;
                try {
                    fileStream = new FileOutputStream(file);
                    fileStream.write(contentBytes);
                    fileStream.flush();
                } finally {
                    if (fileStream != null) {
                        fileStream.close();
                    }
                }
            } catch (IOException e) {
                throw new SynchronizationException(MessageCode.PROBLEM_IN_CREATING_CONTENT,
                        e,
                        new String[]{"file: " + filePath});
            }
        }

        // creating the meta directory
        File metaDirectory = new File(metaDirectoryName);
        if (!metaDirectory.exists() && !metaDirectory.mkdir()) {
            throw new SynchronizationException(MessageCode.ERROR_CREATING_META_FILE,
                    new String[]{"file: " + metaDirectoryName});
        }

        // creating the meta file
        Utils.createMetaFile(metaFilePath, root);

        // printing out the information of the file
        if (!fileAlreadyExist) {
            if (callback != null) {
                callback.displayMessage(new Message(MessageCode.ADDED, new String[]{filePath}));
            }
            addedCount++;
        } else {
            if (overwrite) {
                if (callback != null) {
                    callback.displayMessage(
                            new Message(MessageCode.OVERWRITTEN, new String[]{filePath}));
                }
                overwrittenCount++;
            } else {
                if (callback != null) {
                    callback.displayMessage(
                            new Message(MessageCode.NON_OVERWRITTEN, new String[]{filePath}));
                }
                nonOverwrittenCount++;
            }
        }


        if (!xmlReader.hasNext() || !(xmlReader.isStartElement() &&
                xmlReader.getLocalName().equals(DumpConstants.CHILDREN))) {
            // finished the recursion
            // consuming the stream until the resource end element found
            while (xmlReader.hasNext() && !(xmlReader.isEndElement() &&
                    xmlReader.getLocalName().equals(DumpConstants.RESOURCE))) {
                xmlReader.next();
            }
            return;
        }

        do {
            xmlReader.next();
            if (xmlReader.isEndElement() && xmlReader.getLocalName().equals(DumpConstants.CHILDREN)) {
                // this means empty children, just quit from here
                // before that we have to set the cursor to the end of the current resource
                if (xmlReader.hasNext()) {
                    do {
                        xmlReader.next();
                    } while (xmlReader.hasNext() && !(xmlReader.isEndElement() &&
                            xmlReader.getLocalName().equals(DumpConstants.RESOURCE)));
                }
                return;
            }
        } while (!xmlReader.isStartElement() && xmlReader.hasNext());

        while (xmlReader.hasNext() && xmlReader.isStartElement() &&
                xmlReader.getLocalName().equals(DumpConstants.RESOURCE)) {
            // prepare the children absolute path
            String childName = xmlReader.getAttributeValue(null, DumpConstants.RESOURCE_NAME);
            String fileResourceName = childName;
            String childFilePath = filePath + File.separator + fileResourceName;
            String childPath = (path.equals("/") ? "" : path) + "/" + childName;

            checkOutRecursively(xmlReader, childFilePath, childPath, callback);

            while ((!xmlReader.isStartElement() && xmlReader.hasNext()) &&
                    !(xmlReader.isEndElement() && xmlReader.getLocalName().equals(DumpConstants.CHILDREN))) {
                xmlReader.next();
            }
            if (xmlReader.isEndElement() && xmlReader.getLocalName().equals(DumpConstants.CHILDREN)) {
                // we are in the end of the children tag.
                break;
            }
        }
        // consuming the stream until the resource end element found
        while (xmlReader.hasNext() && !(xmlReader.isEndElement() &&
                xmlReader.getLocalName().equals(DumpConstants.RESOURCE))) {
            xmlReader.next();
        }
    }
}
