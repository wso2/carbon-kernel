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

package org.wso2.carbon.registry.synchronization;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.utils.LogWriter;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.synchronization.message.Message;
import org.wso2.carbon.registry.synchronization.message.MessageCode;

import javax.xml.namespace.QName;
import javax.xml.stream.*;
import java.io.*;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Some utility methods used by the synchronization operations.
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class Utils {

    private static final Log log = LogFactory.getLog(Utils.class);
    public static final int RADIX = 16;

    private Utils() {
    }

    private static final String ATTR_NAME = "name";
    private static final String ATTR_IGNORE_CONFLICTS = "ignoreConflicts";
    private static final String ATTR_IS_COLLECTION = "isCollection";
    private static final String ATTR_STATUS = "status";
    private static final String ATTR_PATH = "path";
    private static final String ATTR_KEY = "key";
    private static final String ELEM_RESOURCE = "resource";
    private static final String ELEM_MEDIA_TYPE = "mediaType";
    private static final String ELEM_CREATOR = "creator";
    private static final String ELEM_CREATED_TIME = "createdTime";
    private static final String ELEM_LAST_UPDATER = "lastUpdater";
    private static final String ELEM_LAST_MODIFIED = "lastModified";
    private static final String ELEM_DESCRIPTION = "description";
    private static final String ELEM_UUID = "uuid";
    private static final String ELEM_PROPERTIES = "properties";
    private static final String ELEM_COMMENTS = "comments";
    private static final String ELEM_TAGGINGS = "taggings";
    private static final String ELEM_RATINGS = "ratings";
    private static final String ELEM_VERSION = "version";
    private static final String ELEM_ASSOCIATIONS = "associations";
    private static final String ELEM_CONTENT = "content";
    private static final String ELEM_PROPERTY = "property";
    private static final String ELEM_COMMENT = "comment";
    private static final String ELEM_USER = "user";
    private static final String ELEM_TEXT = "text";
    private static final String ELEM_TAGGING = "tagging";
    private static final String ELEM_DATE = "date";
    private static final String ELEM_TAG_NAME = "tagName";
    private static final String ELEM_RATING = "rating";
    private static final String ELEM_RATE = "rate";
    private static final String ELEM_ASSOCIATION = "association";
    private static final String ELEM_SOURCE = "source";
    private static final String ELEM_DESTINATION = "destination";
    private static final String ELEM_TYPE = "type";
    private static final String ELEM_CHILDREN = "children";

    private static final String REGISTRY_CONTEXT = "/registry";
    private static final String FILE_PATH = "file path: ";
    private static final String META_FILE_NAME = "meta file name: ";
    private static final String FILE_NAME = "file name: ";
    private static final List<String> SERIALIZABLE_ELEMENTS =
            Arrays.asList(ELEM_MEDIA_TYPE, ELEM_CREATOR, ELEM_CREATED_TIME, ELEM_LAST_UPDATER,
                    ELEM_LAST_MODIFIED, ELEM_DESCRIPTION, ELEM_PROPERTIES, ELEM_COMMENTS,
                    ELEM_TAGGINGS, ELEM_RATINGS, ELEM_VERSION, ELEM_ASSOCIATIONS);

    /**
     * This method writes the meta element to the xml stream up to the children.
     *
     * @param xmlWriter   xml writer
     * @param metaElement meta element to write
     *
     * @throws XMLStreamException if the operation failed
     */
    public static void writeMetaElement(XMLStreamWriter xmlWriter, OMElement metaElement)
            throws XMLStreamException {

        xmlWriter.writeStartElement(ELEM_RESOURCE);

        // adding path as an attribute, updated dump has name instead of path
        String name = metaElement.getAttributeValue(new QName(ATTR_NAME));
        xmlWriter.writeAttribute(ATTR_NAME, name);

        // adding status as an attribute
        String status = metaElement.getAttributeValue(new QName(ATTR_STATUS));
        if(status != null){
            xmlWriter.writeAttribute(ATTR_STATUS, status);
        }

        // adding ignoreConflicts as an attribute, if it is available.:
        String ignoreConflicts = metaElement.getAttributeValue(new QName(ATTR_IGNORE_CONFLICTS));
        if (ignoreConflicts != null) {
            xmlWriter.writeAttribute(ATTR_IGNORE_CONFLICTS, ignoreConflicts);
        }

        // adding isCollection as an attribute
        String isCollectionStr = metaElement.getAttributeValue(new QName(ATTR_IS_COLLECTION));
        xmlWriter.writeAttribute(ATTR_IS_COLLECTION, isCollectionStr);

        Iterator childrenIt = metaElement.getChildren();
        while (childrenIt.hasNext()) {
            Object childObj = childrenIt.next();
            if (!(childObj instanceof OMElement)) {
                continue;
            }
            OMElement childElement = (OMElement) childObj;
            String childName = childElement.getLocalName();

            // the following elements will be serialized to the writer directly
            if (SERIALIZABLE_ELEMENTS.contains(childName)) {
                childElement.serialize(xmlWriter);
            }
        }
    }

    /**
     * This method reads the xml stream up to the children and return the meta element.
     *
     * @param xmlReader the xml reader.
     *
     * @return the meta element.
     * @throws SynchronizationException if the provided XML is invalid.
     * @throws XMLStreamException       if XML parsing failed.
     */
    public static OMElement readMetaElement(XMLStreamReader xmlReader)
            throws SynchronizationException, XMLStreamException {
        try {
            while (!xmlReader.isStartElement() && xmlReader.hasNext()) {
                xmlReader.next();
            }

            if (!xmlReader.hasNext()) {
                // nothing to parse
                return null;
            }

            if (!xmlReader.getLocalName().equals(Utils.ELEM_RESOURCE)) {
                throw new SynchronizationException(
                        MessageCode.INVALID_DUMP_CREATE_META_FILE);

            }

            // alerting non-backward compatibility...
//            String pathAttribute = xmlReader.getAttributeValue(null, ATTR_PATH);
//            if (pathAttribute != null) {
//                throw new SynchronizationException(MessageCode.CHECKOUT_OLD_VERSION);
//            }

            OMFactory factory = OMAbstractFactory.getOMFactory();
            OMElement root = factory.createOMElement(new QName(Utils.ELEM_RESOURCE));

            String resourceName = xmlReader.getAttributeValue(null, ATTR_NAME);
            String isCollectionString = xmlReader.getAttributeValue(null, ATTR_IS_COLLECTION);

            root.addAttribute(ATTR_NAME, resourceName, null);
            root.addAttribute(ATTR_IS_COLLECTION, isCollectionString, null);

            // traversing to the next element
            do {
                xmlReader.next();
            } while (!xmlReader.isStartElement() && xmlReader.hasNext());

            while (xmlReader.hasNext()) {
                String localName = xmlReader.getLocalName();

                // version
                if (localName.equals(ELEM_VERSION)) {
                    String text = xmlReader.getElementText();
                    OMElement versionElement = factory.createOMElement(new QName(ELEM_VERSION));
                    if (text != null) {
                        versionElement.setText(text);
                    }
                    root.addChild(versionElement);
                    // now go to the next element
                    do {
                        xmlReader.next();
                    } while (!xmlReader.isStartElement() && xmlReader.hasNext() &&
                            !(xmlReader.isEndElement() &&
                                    xmlReader.getLocalName().equals(Utils.ELEM_RESOURCE)));
                }
                // setMediaType
                else if (localName.equals(ELEM_MEDIA_TYPE)) {
                    String text = xmlReader.getElementText();
                    OMElement mediaTypeElement = factory.createOMElement(new QName(ELEM_MEDIA_TYPE));
                    if (text != null) {
                        mediaTypeElement.setText(text);
                    }
                    root.addChild(mediaTypeElement);
                    // now go to the next element
                    do {
                        xmlReader.next();
                    } while (!xmlReader.isStartElement() && xmlReader.hasNext() &&
                            !(xmlReader.isEndElement() &&
                                    xmlReader.getLocalName().equals(Utils.ELEM_RESOURCE)));
                }
                // creator
                else if (localName.equals(ELEM_CREATOR)) {
                    String text = xmlReader.getElementText();
                    OMElement creatorElement = factory.createOMElement(new QName(ELEM_CREATOR));
                    if (text != null) {
                        creatorElement.setText(text);
                    }
                    root.addChild(creatorElement);
                    // now go to the next element
                    do {
                        xmlReader.next();
                    } while (!xmlReader.isStartElement() && xmlReader.hasNext() &&
                            !(xmlReader.isEndElement() &&
                                    xmlReader.getLocalName().equals(Utils.ELEM_RESOURCE)));
                }
                // createdTime
                else if (localName.equals(ELEM_CREATED_TIME)) {
                    String text = xmlReader.getElementText();
                    OMElement createdTimeElement =
                            factory.createOMElement(new QName(ELEM_CREATED_TIME));
                    if (text != null) {
                        createdTimeElement.setText(text);
                    }
                    root.addChild(createdTimeElement);
                    // now go to the next element
                    do {
                        xmlReader.next();
                    } while (!xmlReader.isStartElement() && xmlReader.hasNext() &&
                            !(xmlReader.isEndElement() &&
                                    xmlReader.getLocalName().equals(Utils.ELEM_RESOURCE)));
                }
                // createdTime
                else if (localName.equals(ELEM_CONTENT)) {
                    // currently we are keeping the content within the root element, and remove it
                    // later. Before Carbon 3.0.0 the content was in the middle of the other
                    // resource attributes
                    String text = xmlReader.getElementText();
                    OMElement contentElement = factory.createOMElement(new QName(ELEM_CONTENT));
                    if (text != null) {
                        contentElement.setText(text);
                    }
                    root.addChild(contentElement);
                    // now go to the next element
                    do {
                        xmlReader.next();
                    } while (!xmlReader.isStartElement() && xmlReader.hasNext() &&
                            !(xmlReader.isEndElement() &&
                                    xmlReader.getLocalName().equals(Utils.ELEM_RESOURCE)));
                }
                // setLastUpdater
                else if (localName.equals(ELEM_LAST_UPDATER)) {
                    String text = xmlReader.getElementText();
                    OMElement lastUpdaterElement =
                            factory.createOMElement(new QName(ELEM_LAST_UPDATER));
                    if (text != null) {
                        lastUpdaterElement.setText(text);
                    }
                    root.addChild(lastUpdaterElement);
                    // now go to the next element
                    do {
                        xmlReader.next();
                    } while (!xmlReader.isStartElement() && xmlReader.hasNext() &&
                            !(xmlReader.isEndElement() &&
                                    xmlReader.getLocalName().equals(Utils.ELEM_RESOURCE)));
                }
                // LastModified
                else if (localName.equals(ELEM_LAST_MODIFIED)) {
                    String text = xmlReader.getElementText();
                    OMElement lastModifiedElement =
                            factory.createOMElement(new QName(ELEM_LAST_MODIFIED));
                    if (text != null) {
                        lastModifiedElement.setText(text);
                    }
                    root.addChild(lastModifiedElement);
                    // now go to the next element
                    do {
                        xmlReader.next();
                    } while (!xmlReader.isStartElement() && xmlReader.hasNext() &&
                            !(xmlReader.isEndElement() &&
                                    xmlReader.getLocalName().equals(Utils.ELEM_RESOURCE)));
                }
                // get description
                else if (localName.equals(ELEM_DESCRIPTION)) {
                    String text = xmlReader.getElementText();
                    OMElement description = factory.createOMElement(new QName(ELEM_DESCRIPTION));
                    if (text != null) {
                        description.setText(text);
                    }
                    root.addChild(description);
                    // now go to the next element
                    do {
                        xmlReader.next();
                    } while (!xmlReader.isStartElement() && xmlReader.hasNext() &&
                            !(xmlReader.isEndElement() &&
                                    xmlReader.getLocalName().equals(Utils.ELEM_RESOURCE)));
                    // now go to the next element while
                    // (!xmlReader.isStartElement() && xmlReader.hasNext());
                }
                // get uuid
                else if (localName.equals(ELEM_UUID)) {
                    String text = xmlReader.getElementText();
                    OMElement description = factory.createOMElement(new QName(ELEM_UUID));
                    if (text != null) {
                        description.setText(text);
                    }
                    root.addChild(description);
                    // now go to the next element
                    do {
                        xmlReader.next();
                    } while (!xmlReader.isStartElement() && xmlReader.hasNext() &&
                            !(xmlReader.isEndElement() &&
                                    xmlReader.getLocalName().equals(Utils.ELEM_RESOURCE)));
                    // now go to the next element while
                    // (!xmlReader.isStartElement() && xmlReader.hasNext());
                }
                // get properties
                else if (localName.equals(ELEM_PROPERTIES)) {
                    // iterating trying to find the children..
                    OMElement properties = factory.createOMElement(new QName(ELEM_PROPERTIES));
                    root.addChild(properties);
                    do {
                        xmlReader.next();
                    } while (!xmlReader.isStartElement() && xmlReader.hasNext() &&
                            !(xmlReader.isEndElement() &&
                                    xmlReader.getLocalName().equals(Utils.ELEM_RESOURCE)));
                    while (xmlReader.hasNext() &&
                            xmlReader.getLocalName().equals(ELEM_PROPERTY)) {
                        String key = xmlReader.getAttributeValue(null, ATTR_KEY);
                        String text = xmlReader.getElementText();
                        OMElement property = factory.createOMElement(new QName(ELEM_PROPERTY));
                        property.addAttribute(ATTR_KEY, key, null);
                        properties.addChild(property);

                        if (text.equals("")) {
                            text = null;
                        }
                        if (text != null) {
                            property.setText(text);
                        }
                        do {
                            xmlReader.next();
                        } while (!xmlReader.isStartElement() && xmlReader.hasNext() &&
                                !(xmlReader.isEndElement() &&
                                        xmlReader.getLocalName().equals(Utils.ELEM_RESOURCE)));
                    }
                    // getting comment information
                } else if (localName.equals(ELEM_COMMENTS)) {
                    // iterating trying to find the children..
                    OMElement commentsElement = factory.createOMElement(new QName(ELEM_COMMENTS));
                    root.addChild(commentsElement);
                    do {
                        xmlReader.next();
                    } while (!xmlReader.isStartElement() && xmlReader.hasNext());
                    while (xmlReader.hasNext() &&
                            xmlReader.getLocalName().equals(ELEM_COMMENT)) {
                        do {
                            xmlReader.next();
                        } while (!xmlReader.isStartElement() && xmlReader.hasNext() &&
                                !(xmlReader.isEndElement() &&
                                        xmlReader.getLocalName().equals(Utils.ELEM_RESOURCE)));

                        localName = xmlReader.getLocalName();
                        OMElement commentElement = factory.createOMElement(new QName(ELEM_COMMENT));
                        commentsElement.addChild(commentElement);
                        while (xmlReader.hasNext() &&
                                (localName.equals(ELEM_USER) || localName.equals(ELEM_TEXT))) {
                            if (localName.equals(ELEM_USER)) {
                                String text = xmlReader.getElementText();
                                if (text != null) {
                                    OMElement userElement =
                                            factory.createOMElement(new QName(ELEM_USER));
                                    userElement.setText(text);
                                    commentElement.addChild(userElement);
                                }
                            } else if (localName.equals(ELEM_TEXT)) {
                                String text = xmlReader.getElementText();
                                if (text != null) {
                                    OMElement textElement =
                                            factory.createOMElement(new QName(ELEM_TEXT));
                                    textElement.setText(text);
                                    commentElement.addChild(textElement);
                                }
                            }

                            do {
                                xmlReader.next();
                            } while (!xmlReader.isStartElement() && xmlReader.hasNext() &&
                                    !(xmlReader.isEndElement() &&
                                            xmlReader.getLocalName().equals(Utils.ELEM_RESOURCE)));
                            if (xmlReader.hasNext()) {
                                localName = xmlReader.getLocalName();
                            }
                        }
                    }
                }
                // getting tagging information
                else if (localName.equals(ELEM_TAGGINGS)) {
                    // iterating trying to find the children..
                    OMElement taggingsElement = factory.createOMElement(new QName(ELEM_TAGGINGS));
                    root.addChild(taggingsElement);
                    do {
                        xmlReader.next();
                    } while (!xmlReader.isStartElement() && xmlReader.hasNext() &&
                            !(xmlReader.isEndElement() &&
                                    xmlReader.getLocalName().equals(Utils.ELEM_RESOURCE)));
                    while (xmlReader.hasNext() &&
                            xmlReader.getLocalName().equals(ELEM_TAGGING)) {
                        do {
                            xmlReader.next();
                        } while (!xmlReader.isStartElement() && xmlReader.hasNext() &&
                                !(xmlReader.isEndElement() &&
                                        xmlReader.getLocalName().equals(Utils.ELEM_RESOURCE)));

                        localName = xmlReader.getLocalName();

                        OMElement taggingElement = factory.createOMElement(new QName(ELEM_TAGGING));
                        taggingsElement.addChild(taggingElement);

                        while (xmlReader.hasNext() &&
                                (localName.equals(ELEM_USER) || localName.equals(ELEM_DATE) ||
                                        localName.equals(ELEM_TAG_NAME))) {
                            if (localName.equals(ELEM_USER)) {
                                String text = xmlReader.getElementText();
                                if (text != null) {
                                    OMElement userElement =
                                            factory.createOMElement(new QName(ELEM_USER));
                                    userElement.setText(text);
                                    taggingElement.addChild(userElement);
                                }
                            } else if (localName.equals(ELEM_DATE)) {
                                String text = xmlReader.getElementText();
                                if (text != null) {
                                    OMElement dateElement =
                                            factory.createOMElement(new QName(ELEM_DATE));
                                    dateElement.setText(text);
                                    taggingElement.addChild(dateElement);
                                }
                            } else if (localName.equals(ELEM_TAG_NAME)) {
                                String text = xmlReader.getElementText();
                                if (text != null) {
                                    OMElement tagNameElement =
                                            factory.createOMElement(new QName(ELEM_TAG_NAME));
                                    tagNameElement.setText(text);
                                    taggingElement.addChild(tagNameElement);
                                }
                            }
                            do {
                                xmlReader.next();
                            } while (!xmlReader.isStartElement() && xmlReader.hasNext() &&
                                    !(xmlReader.isEndElement() &&
                                            xmlReader.getLocalName().equals(Utils.ELEM_RESOURCE)));
                            if (xmlReader.hasNext()) {
                                localName = xmlReader.getLocalName();
                            }
                        }
                    }
                }
                // getting rating information
                else if (localName.equals(ELEM_RATINGS)) {
                    // iterating trying to find the children..
                    OMElement ratingsElement = factory.createOMElement(new QName(ELEM_RATINGS));
                    root.addChild(ratingsElement);
                    do {
                        xmlReader.next();
                    } while (!xmlReader.isStartElement() && xmlReader.hasNext() &&
                            !(xmlReader.isEndElement() &&
                                    xmlReader.getLocalName().equals(Utils.ELEM_RESOURCE)));
                    while (xmlReader.hasNext() &&
                            xmlReader.getLocalName().equals(ELEM_RATING)) {
                        do {
                            xmlReader.next();
                        } while (!xmlReader.isStartElement() && xmlReader.hasNext());

                        localName = xmlReader.getLocalName();

                        OMElement ratingElement = factory.createOMElement(new QName(ELEM_RATING));
                        ratingsElement.addChild(ratingElement);

                        while (xmlReader.hasNext() &&
                                (localName.equals(ELEM_USER) || localName.equals(ELEM_DATE) ||
                                        localName.equals(ELEM_RATE))) {
                            if (localName.equals(ELEM_USER)) {
                                String text = xmlReader.getElementText();
                                if (text != null) {
                                    OMElement userElement =
                                            factory.createOMElement(new QName(ELEM_USER));
                                    userElement.setText(text);
                                    ratingElement.addChild(userElement);
                                }
                            } else if (localName.equals(ELEM_DATE)) {
                                String text = xmlReader.getElementText();
                                if (text != null) {
                                    OMElement dateElement =
                                            factory.createOMElement(new QName(ELEM_DATE));
                                    dateElement.setText(text);
                                    ratingElement.addChild(dateElement);
                                }
                            } else if (localName.equals(ELEM_RATE)) {
                                String text = xmlReader.getElementText();
                                if (text != null) {
                                    OMElement rateElement =
                                            factory.createOMElement(new QName(ELEM_RATE));
                                    rateElement.setText(text);
                                    ratingElement.addChild(rateElement);
                                }
                            }
                            do {
                                xmlReader.next();
                            } while (!xmlReader.isStartElement() && xmlReader.hasNext() &&
                                    !(xmlReader.isEndElement() &&
                                            xmlReader.getLocalName().equals(Utils.ELEM_RESOURCE)));
                            if (xmlReader.hasNext()) {
                                localName = xmlReader.getLocalName();
                            }
                        }
                    }
                }
                // getting rating information
                else if (localName.equals(ELEM_ASSOCIATIONS)) {
                    // iterating trying to find the children..
                    OMElement associationsElement =
                            factory.createOMElement(new QName(ELEM_ASSOCIATIONS));
                    root.addChild(associationsElement);
                    do {
                        xmlReader.next();
                    } while (!xmlReader.isStartElement() && xmlReader.hasNext() &&
                            !(xmlReader.isEndElement() &&
                                    xmlReader.getLocalName().equals(Utils.ELEM_RESOURCE)));
                    while (xmlReader.hasNext() &&
                            xmlReader.getLocalName().equals(ELEM_ASSOCIATION)) {

                        do {
                            xmlReader.next();
                        } while (!xmlReader.isStartElement() && xmlReader.hasNext() &&
                                !(xmlReader.isEndElement() &&
                                        xmlReader.getLocalName().equals(Utils.ELEM_RESOURCE)));

                        localName = xmlReader.getLocalName();

                        OMElement associationElement =
                                factory.createOMElement(new QName(ELEM_ASSOCIATION));
                        associationsElement.addChild(associationElement);

                        while (xmlReader.hasNext() &&
                                (localName.equals(ELEM_SOURCE) || 
                                        localName.equals(ELEM_DESTINATION) ||
                                        localName.equals(ELEM_TYPE))) {
                            if (localName.equals(ELEM_SOURCE)) {
                                String text = xmlReader.getElementText();
                                if (text != null) {
                                    OMElement sourceElement =
                                            factory.createOMElement(new QName(ELEM_SOURCE));
                                    sourceElement.setText(text);
                                    associationElement.addChild(sourceElement);
                                }
                            } else if (localName.equals(ELEM_DESTINATION)) {
                                String text = xmlReader.getElementText();
                                if (text != null) {
                                    OMElement destinationElement =
                                            factory.createOMElement(new QName(ELEM_DESTINATION));
                                    destinationElement.setText(text);
                                    associationElement.addChild(destinationElement);
                                }
                            } else if (localName.equals(ELEM_TYPE)) {
                                String text = xmlReader.getElementText();
                                if (text != null) {
                                    OMElement typeElement =
                                            factory.createOMElement(new QName(ELEM_TYPE));
                                    typeElement.setText(text);
                                    associationElement.addChild(typeElement);
                                }
                            }
                            do {
                                xmlReader.next();
                            } while (!xmlReader.isStartElement() && xmlReader.hasNext() &&
                                    !(xmlReader.isEndElement() &&
                                            xmlReader.getLocalName().equals(Utils.ELEM_RESOURCE)));
                            if (xmlReader.hasNext()) {
                                localName = xmlReader.getLocalName();
                            }
                        }
                    }
                } else if (localName.equals(ELEM_CHILDREN) ||
                        localName.equals(Utils.ELEM_RESOURCE)) {
                    // checking the children or content element to terminate the check.
                    break;
                } else {
                    // we do mind having unwanted elements, now go to the next element
                    break;
                }

                if ((xmlReader.isEndElement() && xmlReader.getLocalName().equals(
                        Utils.ELEM_RESOURCE))) {
                    // here we come the end of the resource tag
                    break;
                }
            }
            return root;
        } catch (XMLStreamException e) {
            throw new SynchronizationException(
                    MessageCode.ERROR_IN_READING_STREAM_TO_CREATE_META_FILE, e);
        }
    }

    /**
     * This method creates the file that store the meta data of the current directory or file.
     *
     * @param fileName the name of the file.
     * @param metaData the meta data element.
     *
     * @throws SynchronizationException if the operation failed.
     */
    public static void createMetaFile(String fileName,
                                      OMElement metaData) throws SynchronizationException {

        try {
            File file = new File(fileName);

            String metaDirName = file.getAbsoluteFile().getParent();
            if (metaDirName != null) {
                File metaDir = new File(metaDirName);
                if (!metaDir.exists() && !metaDir.mkdir()) {
                    throw new SynchronizationException(MessageCode.ERROR_CREATING_META_FILE,
                            new String[]{FILE_NAME + fileName});
                }
            }
            if (!file.exists() && !file.createNewFile()) {
                throw new SynchronizationException(MessageCode.FILE_ALREADY_EXISTS,
                        new String[]{FILE_NAME + fileName});
            }

            FileOutputStream fileOut = null;
            try {
                fileOut = new FileOutputStream(file, false);
                metaData.serialize(fileOut);
            } finally {
                if (fileOut != null) {
                    fileOut.close();
                }
            }

        } catch (IOException e) {
            throw new SynchronizationException(MessageCode.ERROR_CREATING_META_FILE, e,
                    new String[]{FILE_NAME + fileName});
        } catch (XMLStreamException e) {
            throw new SynchronizationException(MessageCode.ERROR_WRITING_TO_META_FILE, e,
                    new String[]{FILE_NAME + fileName});
        }

    }

    /**
     * This method update the file that store the meta data of the current directory or file.
     *
     * @param fileName the name of the file.
     * @param metaData the meta data element.
     *
     * @throws SynchronizationException if the operation failed.
     */
    public static void updateMetaFile(String fileName,
                                      OMElement metaData) throws SynchronizationException {

        try {
            File file = new File(fileName);

            FileOutputStream fileOut = null;
            try {
                fileOut = new FileOutputStream(file, false);
                metaData.serialize(fileOut);
            } finally {
                if (fileOut != null) {
                    fileOut.close();
                }
            }

        } catch (IOException e) {
            throw new SynchronizationException(MessageCode.ERROR_CREATING_META_FILE, e,
                    new String[]{FILE_NAME + fileName});
        } catch (XMLStreamException e) {
            throw new SynchronizationException(MessageCode.ERROR_WRITING_TO_META_FILE, e,
                    new String[]{FILE_NAME + fileName});
        }

    }

    /**
     * This method checks whether the resource updated or not in the Registry from the last checkout/update
     *
     * @param metaFilePath Resource metadata file location in the file system
     * @param metaElement     Metadata of Registry resource
     * @return             Whether the resource updated or not in the Registry from the last checkout/update
     */
    public static boolean resourceUpdated(String metaFilePath, OMElement metaElement){
        File file = new File(metaFilePath);
        if(file.exists()){
            Reader reader = null;
            try {
                reader = new FileReader(file);
                XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader(reader);
                OMElement fileMetaElement = readMetaElement(xmlReader);
                if(getLastModified(metaElement) == getLastModified(fileMetaElement)){
                    return false;
                }
            } catch (Exception e) {
                //if something went wrong, then consider as resource need to be updated
                return true;
            }finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        log.error("Failed to close the stream" ,e);
                    }
                }
            }
        }
        return true;
    }

    private static long getLastModified(OMElement metadata){
        return Long.parseLong(((OMElement)metadata.
                    getChildrenWithLocalName("lastModified").next()).getText());
    }

    /**
     * Returns the contents of the file in a byte array.
     *
     * @param file the file the to read
     *
     * @return the content of the file
     * @throws SynchronizationException if the operation failed.
     */
    public static byte[] getBytesFromFile(File file) throws SynchronizationException {

        InputStream is = null;
        try {
            try {
                is = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                throw new SynchronizationException(MessageCode.FILE_TO_READ_IS_NOT_FOUND, e,
                        new String[]{FILE_NAME + file.getName()});
            }

            long length = file.length();

            // to ensure that file is not larger than Integer.MAX_VALUE.
            if (length > Integer.MAX_VALUE) {
                // File is too large
                throw new SynchronizationException(MessageCode.FILE_LENGTH_IS_TOO_LARGE,
                        new String[]{FILE_NAME + file.getName(),
                                "file length limit: " + Integer.MAX_VALUE});
            }

            // byte array to keep the data
            byte[] bytes = new byte[(int) length];

            int offset = 0;
            int numRead;
            try {
                while (offset < bytes.length) {
                    numRead = is.read(bytes, offset, bytes.length - offset);
                    if (numRead < 0) {
                        break;
                    }
                    offset += numRead;
                }
            } catch (IOException e) {
                throw new SynchronizationException(MessageCode.ERROR_IN_READING, e,
                        new String[]{FILE_NAME + file.getName()});
            }

            if (offset < bytes.length) {
                throw new SynchronizationException(MessageCode.ERROR_IN_COMPLETELY_READING,
                        new String[]{FILE_NAME + file.getName()});
            }
            return bytes;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ignore) {
                // We only want to make sure that the stream is closed. We don't quite need to worry
                // whether it failed or not, as this might happen due to another upstream exception.
            }
        }

    }

    /**
     * Method to obtain the XML representation of the meta information corresponding to the given
     * file. This method will first determine the path of the meta file for the given file-path, and
     * then obtain the corresponding XML representation using the {@link
     * #getOMElementFromMetaFile(String)} method.
     *
     * @param filePath the path to the file of which the meta information is required.
     *
     * @return An OM element containing the meta information.
     * @throws SynchronizationException if the operation failed.
     */
    public static OMElement getMetaOMElement(String filePath) throws SynchronizationException {
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }
        String fileName = file.getName();
        String metaFilePath = Utils.getMetaFilePath(file);
        // check the existence of the meta directory
        File metaFile = new File(metaFilePath);
        if (!metaFile.exists()) {
            return null;
        }
        return Utils.getOMElementFromMetaFile(metaFilePath);
    }

    /**
     * Method to obtain the XML representation of the data contained in a meta file.
     *
     * @param metaFilePath the path to the meta file.
     *
     * @return An OM element containing the meta information.
     * @throws SynchronizationException if the operation failed.
     */
    public static OMElement getOMElementFromMetaFile(String metaFilePath) throws
            SynchronizationException {
        File metaFile = new File(metaFilePath);
        OMElement element = null;
        // if the file exists, just get it.
        FileInputStream directoryMetaFileStream = null;
        if (metaFile.exists()) {
            try {
                directoryMetaFileStream = new FileInputStream(metaFilePath);

                //create the parser
                XMLStreamReader parser = XMLInputFactory.newInstance()
                        .createXMLStreamReader(directoryMetaFileStream);
                //create the builder
                StAXOMBuilder builder = new StAXOMBuilder(parser);
                // get the element to restore
                element = builder.getDocumentElement().cloneOMElement();
                try {
                    directoryMetaFileStream.close();
                } catch (IOException e) {
                    throw new SynchronizationException(
                            MessageCode.ERROR_IN_CLOSING_META_FILE_STREAM, e,
                            new String[]{META_FILE_NAME + metaFilePath});
                }
            } catch (IOException e) {
                throw new SynchronizationException(MessageCode.ERROR_IN_READING_META_FILE,
                        e,
                        new String[]{META_FILE_NAME + metaFilePath});
            } catch (XMLStreamException e) {
                throw new SynchronizationException(
                        MessageCode.ERROR_IN_READING_META_FILE_STREAM,
                        e,
                        new String[]{META_FILE_NAME + metaFilePath});
            } finally {
                if (directoryMetaFileStream != null) {
                    try {
                        directoryMetaFileStream.close();
                    } catch (IOException ignore) {
                        // We cannot throw an exception in here because it would swallow the
                        // incoming exception if there are any. A potential user would in fact be
                        // interested in the incoming exception instead of this exception.
                    }
                }
            }
        }
        return element;
    }

    /**
     * Method to generate the XML content of a meta file. This method will require information about
     * the resource and details of who created it.
     *
     * @param isCollection whether the resource at the given path is a collection.
     * @param path         the path of the resource for which the meta information is generated.
     * @param username     the username of the creator.
     *
     * @return the meta information as an OM element.
     * @throws SynchronizationException if the operation failed.
     */
    public static OMElement createDefaultMetaFile(boolean isCollection,
                                                  String path,
                                                  String username)
            throws SynchronizationException {
        // if not provide the default details
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement root = factory.createOMElement(new QName(Utils.ELEM_RESOURCE));

        // adding path as an attribute
        root.addAttribute(ATTR_PATH, path, null);

        String resourceName = RegistryUtils.getResourceName(path);
        root.addAttribute(ATTR_NAME, resourceName, null);

        // adding isCollection as an attribute
        root.addAttribute(ATTR_IS_COLLECTION, isCollection ? "true" : "false", null);
        OMElement child;

        // guessing the media type by extension
        String mediaType = "unknown";
        int lastIndex = path.lastIndexOf('.');
        if (lastIndex > 0) {
            mediaType = path.substring(lastIndex + 1);
        }
        child = factory.createOMElement(new QName(ELEM_MEDIA_TYPE));
        child.setText(mediaType);
        root.addChild(child);

        // set creator
        child = factory.createOMElement(new QName(ELEM_CREATOR));
        child.setText(username);
        root.addChild(child);

        // set updator
        child = factory.createOMElement(new QName(ELEM_LAST_UPDATER));
        child.setText(username);
        root.addChild(child);

        // set Description
        child = factory.createOMElement(new QName(ELEM_DESCRIPTION));
        root.addChild(child);


         // set UUID
        child = factory.createOMElement(new QName(ELEM_UUID));
        root.addChild(child);

        // create a 0 version tag
        child = factory.createOMElement((new QName(ELEM_VERSION)));
        child.setText("0");
        root.addChild(child);

        return root;
    }


    /**
     * Method to update the XML content of a meta file. This method will require information about
     * the resource and details of who created it.
     *
     * @param root         metadata OMElement
     * @param path         the path of the resource for which the meta information is generated.
     * @param username     the username of the creator.
     *
     * @return the meta information as an OM element.
     * @throws SynchronizationException if the operation failed.
     */
    public static OMElement updateDefaultAddMetaFile(OMElement root,
                                                  String path,
                                                  String username, boolean isDirectory)
            throws SynchronizationException {

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement child;
        // guessing the media type by extension
        if(!isDirectory && root.getChildrenWithName(new QName("mediaType")) == null){
            String mediaType = "unknown";
            int lastIndex = path.lastIndexOf('.');
            if (lastIndex > 0) {
                mediaType = path.substring(lastIndex + 1);
            }
            child = factory.createOMElement(new QName(ELEM_MEDIA_TYPE));
            child.setText(mediaType);
            root.addChild(child);
        }


        // set creator
        child = factory.createOMElement(new QName(ELEM_CREATOR));
        child.setText(username);
        root.addChild(child);

        // set updator
        child = factory.createOMElement(new QName(ELEM_LAST_UPDATER));
        child.setText(username);
        root.addChild(child);

        // set Description
        child = factory.createOMElement(new QName(ELEM_DESCRIPTION));
        root.addChild(child);


         // set UUID
        child = factory.createOMElement(new QName(ELEM_UUID));
        root.addChild(child);

        return root;
    }

    /**
     * copying the contents of one file to another.
     *
     * @param source      source
     * @param destination destination
     *
     * @throws SynchronizationException throws if the operation failed.
     */
    public static void copy(File source, File destination) throws SynchronizationException {
        try {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = new FileInputStream(source);
                out = new FileOutputStream(destination);

                // Transfer bytes from in to out
                byte[] buf = new byte[RegistryConstants.DEFAULT_BUFFER_SIZE];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } finally {
                    if (out != null) {
                        out.close();
                    }
                }
            }
        } catch (IOException e) {
            throw new SynchronizationException(MessageCode.ERROR_IN_COPYING, e,
                    new String[]{"source: " + source, "target: " + destination});
        }
    }

    /**
     * Method to extract the URL of the remote registry instance from the given URL.
     *
     * @param url aggregate URL containing a concatenation of the registry URL and the resource path
     *            that is capable of referencing a remote resource. This url will contain only the
     *            resource path if the resource was local to the given registry instance.
     *
     * @return the URL of the remote instance, or null if the instance was local.
     */
    public static String getRegistryUrl(String url) {
        if (url.startsWith("/")) {
            // mean this is a local path..
            return null;
        }
        if (url.indexOf(REGISTRY_CONTEXT) > 0) {
            return url.substring(0, url.lastIndexOf(REGISTRY_CONTEXT) + REGISTRY_CONTEXT.length());
        }
        return null;
    }

    /**
     * Method to extract the resource path from the given URL.
     *
     * @param url aggregate URL containing a concatenation of the registry URL and the resource path
     *            that is capable of referencing a remote resource. This url will contain only the
     *            resource path if the resource was local to the given registry instance.
     *
     * @return the path of the resource on the registry.
     */
    public static String getPath(String url) {
        if (url.startsWith("/")) {
            // mean this is a local path..
            return url;
        }
        if (url.indexOf(REGISTRY_CONTEXT) > 0) {
            return url.substring(url.lastIndexOf(REGISTRY_CONTEXT) + REGISTRY_CONTEXT.length());
        }
        return null;
    }

    /**
     * This method will obtain the encoded representation of the given resource name.
     *
     * @param resourceName the name of the resource.
     *
     * @return the encoded name.
     * @throws SynchronizationException if the operation failed.
     * @see URLEncoder
     */
    public static String encodeResourceName(String resourceName) throws SynchronizationException {
        String encodedName;
        try {
            encodedName = URLEncoder.encode(resourceName, RegistryConstants.DEFAULT_CHARSET_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new SynchronizationException(MessageCode.ERROR_ENCODING_RESOURCE_NAME, e,
                    new String[]{"resource name: " + resourceName});
        }
        return encodedName;
    }

    /**
     * This method will obtain the decoded representation of the given encoded resource path.
     *
     * @param path the encoded path of the resource.
     *
     * @return the decoded path.
     * @throws SynchronizationException if the operation failed.
     * @see URLDecoder
     */
    public static String decodeFilename(String path) throws SynchronizationException {
        String decodedName;
        try {
            decodedName = URLDecoder.decode(path, RegistryConstants.DEFAULT_CHARSET_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new SynchronizationException(MessageCode.ERROR_DECODING_PATH, e,
                    new String[]{"path: " + path});
        }
        return decodedName;

    }

    /**
     * This method will obtain the consent from the user to delete the specified file and meta file
     * (corresponding to the file), if the user agrees to.
     *
     * @param file     the file or directory to delete.
     * @param metaFile the meta file corresponding to the file or directory to delete.
     * @param callback the callback which is used to obtain the user's consent. If this parameter is
     *                 null, the file and the meta file will be deleted irrespective of the user's
     *                 choice.
     *
     * @return whether the operation succeeded or not.
     * @throws SynchronizationException if an error occurred during the operation.
     */
    public static boolean confirmDelete(File file, File metaFile, UserInputCallback callback)
            throws SynchronizationException {
        String filePath = file.getAbsolutePath();
        boolean isDirectory = file.isDirectory();
        boolean sameContent = false;
        if(!isDirectory && metaFile.exists()){
            OMElement metaElement;
            try {
                metaElement = new StAXOMBuilder(new FileInputStream(metaFile)).getDocumentElement();
            } catch (Exception e) {
                throw new SynchronizationException(MessageCode.RESOURCE_METADATA_CORRUPTED, e);
            }
            OMAttribute md5Attribute =  metaElement.getAttribute(new QName("md5"));

            String metaFileMD5 = null;
            if(md5Attribute != null){
                metaFileMD5 = md5Attribute.getAttributeValue();
            }
            String fileMD5 = Utils.getMD5(file);
            sameContent = fileMD5.equals(metaFileMD5);
        }

        boolean inputCode;
        if(!sameContent){
            if (callback == null) {
                // The default behaviour is to delete.
                inputCode = true;
            } else if (file.isDirectory()) {
                inputCode = callback.getConfirmation(new Message(
                        MessageCode.DIRECTORY_DELETE_CONFIRMATION,
                        new String[]{FILE_PATH + filePath}),
                        SynchronizationConstants.DELETE_CONFIRMATION_CONTEXT);
            } else {
                inputCode = callback.getConfirmation(new Message(
                        MessageCode.FILE_DELETE_CONFIRMATION,
                        new String[]{FILE_PATH + filePath}),
                        SynchronizationConstants.DELETE_CONFIRMATION_CONTEXT);
            }

            if (!inputCode) {
                // just continue;
                return false;
            }
        }

        // if you come here it is for the permission to delete
        boolean deleted = deleteFile(file);
        if (!deleted) {
            throw new SynchronizationException(MessageCode.ERROR_IN_DELETING,
                    new String[]{FILE_PATH + filePath});
        }
        if (!isDirectory) {
            deleted = deleteFile(metaFile);
            if (!deleted) {
                throw new SynchronizationException(MessageCode.ERROR_IN_DELETING,
                        new String[]{FILE_PATH + metaFile.getAbsolutePath()});
            }
        }
        return true;
    }

    /**
     * This method deletes the specified file from the filesystem. If the given file-path
     * corresponds to a directory, the directory and everything under it (child files and
     * directories) will be recursively deleted in the process.
     *
     * @param file the file or directory to delete.
     *
     * @return true if the operation succeeded or false if it failed.
     */
    public static boolean deleteFile(File file) {
        if (file.isDirectory()) {
            String[] children = file.list();
            for (String child : children) {
                boolean success = deleteFile(new File(file, child));
                if (!success) {
                    return false;
                }
            }
        }
        return FileUtils.deleteQuietly(file);
    }

    /**
     * Method to determine the files that are required to be cleaned up from meta information
     * directory and preserve only the given list of files. This method will determine all meta
     * files within the given directory that have no corresponding physical files on the filesystem.
     * This is used after a check-in operation has been propagated to ensure that the filesystem
     * will always remain consistent.
     *
     * @param directory       the meta information directory that requires cleaning up.
     * @param filesToPreserve the list of files to preserve.
     *
     * @return the list of files to be cleaned.
     */
    public static List<String> cleanUpDirectory(File directory, List<String> filesToPreserve) {
        List<String> filesToClean = new LinkedList<String>();
        if (directory.isDirectory()) {
            String[] children = directory.list();
            for (String child : children) {
                File file = new File(directory, child);
                String absoluteFilePath = file.getAbsolutePath();
                if (!filesToPreserve.contains(absoluteFilePath)) {
                    filesToClean.add(absoluteFilePath);
                }
            }
        }
        return filesToClean;
    }

    /**
     * Method to obtain the MD5 hash value for the given content.
     *
     * @param content the content as an array of bytes.
     *
     * @return the MD5 hash of the content.
     */
    public static String getMD5(byte[] content) {
        MessageDigest m;
        try {
            m = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            //String msg = "Error in generating the md5. ";
            //throw new Exception(msg, e);
            return null;
        }
        m.update(content, 0, content.length);

        return new BigInteger(1, m.digest()).toString(RADIX);
    }

    /**
     * Method to obtain the MD5 hash value for the given file.
     *
     * @param file the content file.
     *
     * @return the MD5 hash of the content.
     */
    public static String getMD5(File file) throws SynchronizationException {
        return getMD5(getBytesFromFile(file));
    }

    /**
     * Determines whether the content of the given file has changed. If the given file is a
     * directory, this method will recursively test each file under this directory to determine
     * whether the content of any child, or grand child has changed.
     * <p/>
     * The change in content is determined using MD5 hash values written to the meta files during a
     * check-out or update. If the MD5 hash value of the given file was not found in its meta file,
     * this operation will assume that a change has been made, irrespective of whether the content
     * of the file changed or not.
     *
     * @param file the file to test for changes.
     *
     * @return true if the content has changed, or false if not.
     * @throws SynchronizationException if an error occurred during the operation.
     */
    public static boolean contentChanged(File file)
            throws SynchronizationException {
        return contentChanged(file, true);
    }

    // Method that actually checks for content changes. This needs to know whether the call is the
    // first-call to this method, or was it a recursive call.
    private static boolean contentChanged(File file, boolean isParent)
            throws SynchronizationException {
        if (!file.exists()) {
            // If we don't find a file, this is not a valid checkout.
            throw new SynchronizationException(MessageCode.CHECKOUT_BEFORE_CHECK_IN);
        } else if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null || files.length == 0) {
                // The meta information for this directory has not been found. Therefore, this is
                // a new directory.
                if (isParent) {
                    throw new SynchronizationException(MessageCode.CHECKOUT_BEFORE_CHECK_IN);
                }
                return true;
            }
            File metaFile = new File(file.getPath() + File.separator +
                    SynchronizationConstants.META_DIRECTORY);
            File[] metaFiles = metaFile.listFiles();
            if (metaFiles == null || metaFiles.length == 0) {
                // Though the meta directory exists, the meta information for the given directory
                // has not been found. Therefore, this is a new directory waiting to be checked in.
                return true;
            }
            // Child directories don't have meta files. Therefore, we need the number of children
            // that are not directories.
            int childFiles = 0;
            for (File child : files) {
                if (!child.isDirectory()) {
                    childFiles++;
                }
            }
            // We don't count the meta file corresponding to this directory.
            if ((metaFiles.length - 1) != childFiles) {
                // A file has been added or removed from this directory.
                return true;
            }
            for (File child : files) {
                // If the given child is not the meta directory, and if the content has changed,
                // the content of this directory has changed.
                if (!child.getPath().endsWith(SynchronizationConstants.META_DIRECTORY) &&
                        contentChanged(child, false)) {
                    return true;
                }
            }
            return false;
        }
        return fileContentChanged(file);
    }

    public static boolean fileContentChanged(File file) throws SynchronizationException {
        String parentDirName = file.getAbsoluteFile().getParent();
        String name = file.getName();
        String metaFilePath = parentDirName + File.separator +
                SynchronizationConstants.META_DIRECTORY + File.separator +
                SynchronizationConstants.META_FILE_PREFIX + Utils.encodeResourceName(name) +
                SynchronizationConstants.META_FILE_EXTENSION;
        String currentMD5 = getMD5(getBytesFromFile(file));
        OMElement metaFileElement = getOMElementFromMetaFile(metaFilePath);
        String metaFileMD5 = null;
        if (metaFileElement != null) {
            metaFileMD5 = metaFileElement.getAttributeValue(new QName("md5"));
        }
        // We obtain the MD5 value of the file and compare it against the one saved in the meta file
        // to see whether any change has been done.
        return metaFileMD5 == null || !metaFileMD5.equals(currentMD5);
    }

    /**
     * Method to clean the embedded registry instance, after the synchronization operation. This
     * method should only be invoked if the synchronization happens at a client that terminates soon
     * after the execution of the synchronization operation, to prevent loss of activity logs.
     */
    public static void cleanEmbeddedRegistry() {
        RegistryContext registryContext = RegistryContext.getBaseInstance();
        if (registryContext != null) {
            LogWriter logWriter = registryContext.getLogWriter();
            if (logWriter != null) {
                logWriter.interrupt();
            }
        }
    }

    /**
     * Get metadata file path for a given file path
     *
     * @param path file path
     * @return
     * @throws SynchronizationException if operation failed
     */
    public static String getMetaFilePath(String path) throws SynchronizationException {
        File f = new File(path);
        return getMetaFilePath(f);
    }

    /**
     * Get metadata file path for a given file
     *
     * @param f  file
     * @return
     * @throws SynchronizationException if operation failed
     */
    public static String getMetaFilePath(File f) throws SynchronizationException {
        String absPath = f.getAbsolutePath();
        if (f.isDirectory()) {
            return absPath + File.separator + SynchronizationConstants.META_DIRECTORY +
                    File.separator + SynchronizationConstants.META_FILE_PREFIX +
                    SynchronizationConstants.META_FILE_EXTENSION;
        } else {
            return absPath.substring(0, absPath.lastIndexOf(File.separator)) +
                    File.separator + SynchronizationConstants.META_DIRECTORY +
                    File.separator + SynchronizationConstants.META_FILE_PREFIX +
                    Utils.encodeResourceName(f.getName()) +
                    SynchronizationConstants.META_FILE_EXTENSION;
        }
    }

    /**
     * This method add the resources which are not added to commit
     *
     * @param path
     * @throws SynchronizationException
     */
    public static void addResource(String path) throws SynchronizationException {
        String registryUrl = null;
        String metaFile = Utils.getMetaFilePath(path);
        File file = new File(metaFile);
        String pathAttribute;
        try {
            OMElement resourceElement = new StAXOMBuilder(new FileInputStream(file)).getDocumentElement();
            pathAttribute = resourceElement.getAttribute(new QName("path")).getAttributeValue();
            OMAttribute registryUrlAttr;
            if((registryUrlAttr = resourceElement.getAttribute(new QName("registryUrl")) ) != null){
                registryUrl = registryUrlAttr.getAttributeValue();
            }
        } catch (FileNotFoundException e) {
            throw new SynchronizationException(MessageCode.CURRENT_COLLECTION_NOT_UNDER_REGISTRY_CONTROL);
        } catch (Exception e) {
            throw new SynchronizationException(MessageCode.RESOURCE_METADATA_CORRUPTED);
        }
        addResourceMetadataRecursively(path, pathAttribute, registryUrl, true);
    }

    private static void addResourceMetadataRecursively(String path, String parentRegistryPath,
                                                String registryUrl, boolean root) throws SynchronizationException {
        File file = new File(path);
        String registryPath = parentRegistryPath + RegistryConstants.PATH_SEPARATOR + file.getName();
        String metaFilePath;
        if(file.isDirectory()){
            metaFilePath = path + File.separator + SynchronizationConstants.META_DIRECTORY +
                    File.separator + SynchronizationConstants.META_FILE_PREFIX +
                    SynchronizationConstants.META_FILE_EXTENSION;
            addMetadata(metaFilePath, file.getName(), true, registryPath, registryUrl, root);
            for(String fileName : file.list(new FilenameFilter() {
                public boolean accept(File file, String s) {
                    if(SynchronizationConstants.META_DIRECTORY.equals(s)){
                        return false;
                    }
                    return true;
                }
            })){
                addResourceMetadataRecursively(path + File.separator + fileName, registryPath, registryUrl, false);
            }
        } else {
            String parentDirName = file.getParent();
            metaFilePath =
                    parentDirName + File.separator + SynchronizationConstants.META_DIRECTORY +
                            File.separator + SynchronizationConstants.META_FILE_PREFIX +
                            Utils.encodeResourceName(file.getName()) +
                            SynchronizationConstants.META_FILE_EXTENSION;
            addMetadata(metaFilePath, file.getName(), false, registryPath, registryUrl, root);
        }
    }

    private static void addMetadata(String metaFilePath, String fileName, boolean isCollection,
                             String registryPath, String registryUrl, boolean root)
            throws SynchronizationException {
        FileWriter writer = null;
        File metaDir;
        File file = new File(metaFilePath);
        if(file.exists()){
            return;
        }
        try {
            metaDir = new File(file.getParent());
            metaDir.mkdirs();
            file.createNewFile();
            writer = new FileWriter(file);
            XMLOutputFactory xof = XMLOutputFactory.newInstance();
            XMLStreamWriter xmlWriter = xof.createXMLStreamWriter(writer);
            xmlWriter.writeStartElement("resource");
            xmlWriter.writeAttribute("name", fileName);
            xmlWriter.writeAttribute("isCollection", String.valueOf(isCollection));
            xmlWriter.writeAttribute("path", registryPath);
            if(registryUrl != null){
                xmlWriter.writeAttribute("registryUrl", registryUrl);
            }
            xmlWriter.writeAttribute("status", "added");
            xmlWriter.writeEndElement();
            xmlWriter.flush();
        } catch (Exception e) {
            throw new SynchronizationException(MessageCode.ERROR_IN_ADDING_METADATA);
        }finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                  log.error("Failed to close the stream" ,e);
                }
            }
        }
    }

    /**
     * This method mark the resources as delete if it no longer exists in the file system
     *
     * @param dirPath Parent directory of the deleted file/s
     * @throws SynchronizationException If the operation failed
     */
    public static void setResourcesDelete(String dirPath) throws SynchronizationException {
        if(dirPath.endsWith(File.separator)){
            dirPath = dirPath.substring(0, dirPath.length() -1);
        }
        findAndSetResourcesDeleteRecursively(dirPath);
    }

    private static void findAndSetResourcesDeleteRecursively(String dirPath) throws SynchronizationException {
        String metaDirPath = dirPath + File.separator + SynchronizationConstants.META_DIRECTORY;
        File file = new File(metaDirPath);

        String[] metaFiles = file.list(new FilenameFilter(){
            public boolean accept(File file, String s) {
                if(!s.equals("~.xml")){
                    return true;
                }
                return false;
            }
        });
        for(String metaFilePath : metaFiles){
            String fileName = metaFilePath.substring(1, metaFilePath.length() - 4);
            String filePath = dirPath + File.separator + fileName;
            File resourceFile = new File(decodeFilename(filePath));
            if(!resourceFile.exists()){
                setDelete(metaDirPath + File.separator + metaFilePath);
            }
        }

        File dir = new File(dirPath);
        String[] childFiles = dir.list(new FilenameFilter(){
            public boolean accept(File file, String s) {
                if(!s.equals(".meta")){
                    return true;
                }
                return false;
            }
        });

        for(String child : childFiles) {
            String childPath = dirPath + File.separator + child;
            if (new File(childPath).isDirectory()){
                findAndSetResourcesDeleteRecursively(childPath);
            }
        }
    }

    private static void setDelete(String metaFilePath) throws SynchronizationException {
        File metaFile = new File(metaFilePath);
        OMElement resourceElement;
        FileWriter writer = null;
        try {
            resourceElement = new StAXOMBuilder(new FileInputStream(metaFile)).getDocumentElement();
            resourceElement.addAttribute("status", "deleted", null);
            writer = new FileWriter(metaFile);
            XMLOutputFactory xof = XMLOutputFactory.newInstance();
            XMLStreamWriter xmlWriter = xof.createXMLStreamWriter(writer);
            resourceElement.serialize(xmlWriter);
            xmlWriter.flush();

        } catch (FileNotFoundException e) {
            throw new SynchronizationException(MessageCode.RESOURCE_NOT_UNDER_REGISTRY_CONTROL);
        } catch (Exception e) {
            throw new SynchronizationException(MessageCode.RESOURCE_METADATA_CORRUPTED);
        }finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    log.error("Failed to close the stream" ,e);
                }
            }
        }
    }
}

