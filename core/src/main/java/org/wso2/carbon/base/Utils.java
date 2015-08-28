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
package org.wso2.carbon.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.base.exception.ConfigurationInitializationException;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Generic Base Utility methods
 */
public class Utils {
    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    /**
     * Remove default constructor and make it not available to initialize.
     */

    private Utils() {
        throw new AssertionError("Instantiating utility class...");

    }

    public static String getServerXml() {
        String carbonXML = System
                .getProperty(Constants.CARBON_REPOSITORY);
        /*
         * if user set the system property telling where is the configuration
         * directory
         */
        if (carbonXML == null) {
            return getCarbonConfigDirPath() + File.separator + "carbon.xml";
        }
        return carbonXML + File.separator + "carbon.xml";
    }

    public static String getCarbonConfigDirPath() {
        String configDirPath = null;
        String carbonRepoDirPath = System
                .getProperty(Constants.CARBON_REPOSITORY);
        if (carbonRepoDirPath == null) {
            carbonRepoDirPath = System
                    .getenv(Constants.CARBON_REPOSITORY_PATH_ENV);
        }
        if (carbonRepoDirPath == null) {
            configDirPath = getCarbonHome() + File.separator + "repository"
                    + File.separator + "conf";
        } else {
            configDirPath = carbonRepoDirPath + File.separator + "conf";
        }
        return configDirPath;
    }

    public static String getCarbonHome() {
        String carbonHome = System.getProperty(Constants.CARBON_HOME);
        if (carbonHome == null) {
            carbonHome = System.getenv(Constants.CARBON_HOME_ENV);
            System.setProperty(Constants.CARBON_HOME, carbonHome);
        }
        return carbonHome;
    }

    public static InputStream parseXmlConfiguration(String configurationXMLLocation)
            throws ConfigurationInitializationException {
        InputStream xmlInputStream = null;
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
                ClassLoader cl = Thread.currentThread().getClass().getClassLoader();
                if (cl != null) {
                    xmlInputStream = cl
                            .getResourceAsStream(configurationXMLLocation);
                    if (xmlInputStream == null) {
                        String msg = "Configuration File cannot be loaded from "
                                + configurationXMLLocation;
                        logger.error(msg, e1);
                        throw new ConfigurationInitializationException(msg, e1);
                    }
                } else {
                    throw new ConfigurationInitializationException(e1);
                }
            }
        } catch (IOException e) {
            logger.error("Configuration File cannot be loaded from "
                    + configurationXMLLocation, e);
            throw new ConfigurationInitializationException(e);
        }
        return xmlInputStream;
    }

    public static void buildXmlConfiguration(InputStream xmlInputStream,
                                             Map<String, List<Object>> configuration)
            throws ConfigurationInitializationException {
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
                        NodeList childNodes = tempNode.getChildNodes();
                        readChildElements(childNodes, nameStack, configuration);
                    }
                }
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            logger.error("Problem in parsing the configuration file ", e);
            throw new ConfigurationInitializationException(e);
        }
    }

    private static void readChildElements(NodeList nodeList, Stack<String> nameStack,
                                          Map<String, List<Object>> configuration) {
        for (int count = 0; count < nodeList.getLength(); count++) {
            Node tempNode = nodeList.item(count);
            if (tempNode.getNodeType() == Node.ELEMENT_NODE) {
                nameStack.push(tempNode.getNodeName());
                if (elementHasText(tempNode)) {
                    String key = getKey(nameStack);
                    String value = replaceSystemProperty(tempNode.getFirstChild().getNodeValue());
                    addToConfiguration(key, value, configuration);
                } else {
                    String key = getKey(nameStack);
                    addToConfiguration(key, tempNode, configuration);
                }
                readChildElements(tempNode.getChildNodes(), nameStack, configuration);
                nameStack.pop();
            }
        }
    }

    private static void addToConfiguration(String key, Object value,
                                           Map<String, List<Object>> configuration) {
        List<Object> list = configuration.get(key);
        if (list == null) {
            list = new ArrayList<Object>();
            list.add(value);
            configuration.put(key, list);
        } else {
            if (!list.contains(value)) {
                list.add(value);
            }
        }
    }


    private static String getKey(Stack<String> nameStack) {
        StringBuffer key = new StringBuffer();
        for (int i = 0; i < nameStack.size(); i++) {
            String name = nameStack.elementAt(i);
            key.append(name).append(".");
        }
        key.deleteCharAt(key.lastIndexOf("."));

        return key.toString();
    }

    private static boolean elementHasText(Node element) {
        String text = element.getFirstChild().getNodeValue();
        return text != null && text.trim().length() != 0;
    }

    private static String replaceSystemProperty(String text) {
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
}
