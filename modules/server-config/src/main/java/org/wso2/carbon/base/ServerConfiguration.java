/* 
 * Copyright 2005,2013 WSO2, Inc. http://www.wso2.org
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
package org.wso2.carbon.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * This class stores the configuration of the Carbon Server.
 */
@SuppressWarnings("unused")
public class ServerConfiguration implements ServerConfigurationService {

    private static final Logger logger = LoggerFactory.getLogger(ServerConfiguration.class);

    private Map<String, List<String>> configuration = new HashMap<String, List<String>>();
    private boolean isInitialized;
    private boolean isLoadedConfigurationPreserved = false;
    private String documentXML;

    /**
     * Stores the singleton server configuration instance.
     */
    private static ServerConfiguration instance = new ServerConfiguration();

    /**
     * Method to retrieve an instance of the server configuration.
     *
     * @return instance of the server configuration
     */
    public static ServerConfiguration getInstance() {
        // Need permissions in order to instantiate ServerConfiguration
        return instance;
    }

    // Private constructor preventing creation of duplicate instances.

    private ServerConfiguration() {
    }

    /**
     * This initializes the server configuration. This method should only be
     * called once, for successive calls, it will be checked.
     *
     * @param xmlInputStream the server configuration file stream.
     * @throws org.wso2.carbon.base.ServerConfigurationException
     *          if the operation failed.
     */
    public synchronized void init(InputStream xmlInputStream)
            throws ServerConfigurationException {
        if (isInitialized) {
            return;
        }

        if (!isLoadedConfigurationPreserved) {
            configuration.clear();
        }

        try {
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = dBuilder.parse(xmlInputStream);
            Stack<String> nameStack = new Stack<String>();
            if (doc.hasChildNodes()) {
                NodeList nodeList = doc.getChildNodes();
                for (int count = 0; count < nodeList.getLength(); count++) {
                    Node tempNode = nodeList.item(count);
                    // make sure it's element node.
                    if (tempNode.getNodeType() == Node.ELEMENT_NODE) {
                        NodeList chiledNodeList = tempNode.getChildNodes();
                        readChildElements(chiledNodeList, nameStack);
                    }
                }
            }
            isInitialized = true;
            isLoadedConfigurationPreserved = false;
        } catch (ParserConfigurationException e) {
            logger.error("Problem in parsing the configuration file ", e);
            throw new ServerConfigurationException(e);
        } catch (SAXException e) {
            logger.error("Problem in parsing the configuration file ", e);
            throw new ServerConfigurationException(e);
        } catch (IOException e) {
            logger.error("Problem in parsing the configuration file ", e);
            throw new ServerConfigurationException(e);
        }
    }

    /**
     * This initializes the server configuration. This method should only be
     * called once, for successive calls, it will be checked.
     *
     * @param configurationXMLLocation the location of the server configuration file (carbon.xml).
     * @throws ServerConfigurationException if the operation failed.
     */
    public synchronized void init(String configurationXMLLocation)
            throws ServerConfigurationException {
        if (isInitialized) {
            return;
        }
        if (configurationXMLLocation == null) {
            configurationXMLLocation = "conf/carbon.xml";
        }

        InputStream xmlInputStream = null;
        try {
            try {
                // URL will parse the location according to respective RFC's and
                // open a connection.
                URL urlXMLLocation = new URL(configurationXMLLocation);
                xmlInputStream = urlXMLLocation.openStream();
            } catch (MalformedURLException e) {
                File f = new File(configurationXMLLocation);
                try {
                    xmlInputStream = new FileInputStream(f);
                } catch (FileNotFoundException e1) {
                    // As a last resort test in the classpath
                    ClassLoader cl = ServerConfigurationService.class
                            .getClassLoader();
                    xmlInputStream = cl
                            .getResourceAsStream(configurationXMLLocation);
                    if (xmlInputStream == null) {
                        String msg = "Configuration File cannot be loaded from "
                                     + configurationXMLLocation;
                        logger.error(msg, e1);
                        throw new ServerConfigurationException(msg, e1);

                    }
                }
            } catch (IOException e) {
                logger.error("Configuration File cannot be loaded from "
                          + configurationXMLLocation, e);
                throw new ServerConfigurationException(e);
            }
            init(xmlInputStream);
        } finally {
            if (xmlInputStream != null) {
                try {
                    xmlInputStream.close();
                } catch (IOException e) {
                    logger.warn("Cannot close input stream", e);
                }
            }
        }
    }

    /**
     * Method to forcibly initialize the server configuration. If there is any
     * configuration loaded, it will not be preserved.
     *
     * @param xmlInputStream the server configuration file stream.
     * @throws ServerConfigurationException if the operation failed.
     */
    public synchronized void forceInit(InputStream xmlInputStream)
            throws ServerConfigurationException {
        isInitialized = false;
        init(xmlInputStream);
    }

    /**
     * Method to forcibly initialize the server configuration. If there is any
     * configuration loaded, it will not be preserved.
     *
     * @param configurationXMLLocation the location of the server configuration file (carbon.xml).
     * @throws ServerConfigurationException if the operation failed.
     */
    public synchronized void forceInit(String configurationXMLLocation)
            throws ServerConfigurationException {
        isInitialized = false;
        init(configurationXMLLocation);
    }

    /**
     * Method to forcibly initialize the server configuration.
     *
     * @param configurationXMLLocation       the location of the server configuration file (carbon.xml).
     * @param isLoadedConfigurationPreserved whether the currently loaded configuration is preserved.
     * @throws ServerConfigurationException if the operation failed.
     */
    public synchronized void forceInit(String configurationXMLLocation,
                                       boolean isLoadedConfigurationPreserved)
            throws ServerConfigurationException {
        isInitialized = false;
        this.isLoadedConfigurationPreserved = isLoadedConfigurationPreserved;
        init(configurationXMLLocation);
    }

    private void readChildElements(NodeList nodeList,
                                   Stack<String> nameStack) {
        for (int count = 0; count < nodeList.getLength(); count++) {
            Node tempNode = nodeList.item(count);
            if (tempNode.getNodeType() == Node.ELEMENT_NODE) {
                nameStack.push(tempNode.getNodeName());
                if (elementHasText(tempNode)) {
                    String key = getKey(nameStack);
                    String value = replaceSystemProperty(tempNode.getFirstChild().getNodeValue());
                    addToConfiguration(key, value);
                }
                readChildElements(tempNode.getChildNodes(), nameStack);
                nameStack.pop();
            }
        }
    }

    private void addToConfiguration(String key, String value) {
        List<String> list = configuration.get(key);
        if (list == null) {
            list = new ArrayList<String>();
            list.add(value);
            configuration.put(key, list);
        } else {
            if (!list.contains(value)) {
                list.add(value);
            }
        }
    }

    private void overrideConfiguration(String key, String value) {
        List<String> list = new ArrayList<String>();
        list.add(value);
        configuration.put(key, list);
    }

    private String replaceSystemProperty(String text) {
        int indexOfStartingChars = -1;
        int indexOfClosingBrace;

        // The following condition deals with properties.
        // Properties are specified as ${system.property},
        // and are assumed to be System properties
        while (indexOfStartingChars < text.indexOf("${")
               && (indexOfStartingChars = text.indexOf("${")) != -1
               && (indexOfClosingBrace = text.indexOf('}')) != -1) { // Is a
            // property
            // used?
            String sysProp = text.substring(indexOfStartingChars + 2,
                                            indexOfClosingBrace);
            String propValue = System.getProperty(sysProp);
            if (propValue != null) {
                text = text.substring(0, indexOfStartingChars) + propValue
                       + text.substring(indexOfClosingBrace + 1);
            }
            if (sysProp.equals("carbon.home") && propValue != null
                && propValue.equals(".")) {

                text = new File(".").getAbsolutePath() + File.separator + text;

            }
        }
        return text;
    }

    private String getKey(Stack<String> nameStack) {
        StringBuffer key = new StringBuffer();
        for (int i = 0; i < nameStack.size(); i++) {
            String name = nameStack.elementAt(i);
            key.append(name).append(".");
        }
        key.deleteCharAt(key.lastIndexOf("."));

        return key.toString();
    }

    private boolean elementHasText(Node element) {
        String text = element.getFirstChild().getNodeValue();
        return text != null && text.trim().length() != 0;
    }

    @Override
    public void setConfigurationProperty(String key, String value) {
        //TODO
    }

    @Override
    public void overrideConfigurationProperty(String key, String value) {
        //TODO
    }

    /**
     * There can be multiple objects with the same key. This will return the
     * first String from them
     *
     * @param key the search key
     * @return value corresponding to the given key
     */
    @Override
    public String getFirstProperty(String key) {
        List<String> value = configuration.get(key);
        if (value == null) {
            return null;
        }
        return value.get(0);
    }

    /**
     * There can be multiple object corresponding to the same object.
     *
     * @param key the search key
     * @return the properties corresponding to the <code>key</code>
     */
    @Override
    public String[] getProperties(String key) {
        List<String> values = configuration.get(key);
        if (values == null) {
            return new String[0];
        }
        return values.toArray(new String[values.size()]);
    }

    @Override
    public Element getDocumentElement() {
        return null;
    }


}
