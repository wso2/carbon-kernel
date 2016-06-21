/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.kernel.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.kernel.utils.configfiletypes.ConfigFileType;
import org.wso2.carbon.kernel.utils.configfiletypes.Properties;
import org.wso2.carbon.kernel.utils.configfiletypes.YAML;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * This util class provide the ability to override configurations in various components using a single file which has
 * the name {@link ConfigUtil#DEPLOYMENT_PROPERTIES_FILE}.
 *
 * @since 5.2.0
 */
public final class ConfigUtil {

    private static final Logger logger = LoggerFactory.getLogger(ConfigUtil.class.getName());
    //This is used to read the deployment.properties file location using System/Environment variables.
    private static final String DEPLOYMENT_PROPERTIES_VAR_NAME = "deployment.properties";
    //This variable contains the name of the config overriding file
    private static final String DEPLOYMENT_PROPERTIES_FILE = "deployment.properties";
    //This variable contains the path to the deployment.properties file.
    private static final String DEPLOYMENT_PROPERTIES_FILE_PATH = getPath();
    //This variable contains the value which will be used as the root element when converting yaml,properties files
    private static final String ROOT_ELEMENT = "configurations";
    //This regex is used to identify yaml, properties files
    private static final String FILE_REGEX = ".+\\.(" + getFileTypesString() + ")";
    //This regex is used to identify placeholder values
    private static final String PLACEHOLDER_REGEX =
            "(.*?)(\\$\\{(" + getPlaceholderString() + "):([^,]+?)((,)(.+?))?\\})(.*?)";
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile(PLACEHOLDER_REGEX);
    /*
     This map contains config data read from the deployment.properties file. The keys will be file names and values
     will be another map. In the 2nd map, the key will be the xpath and the value will be tha value in the
     deployment.properties file.
     */
    private static final Map<String, Map<String, String>> deploymentPropertiesMap = readDeploymentFile();

    //Enum to hold the file types which we need to add root elements when converting
    private enum FileType {
        YAML("yml"), PROPERTIES("properties"); // TODO: 6/21/16 Change yml to yaml
        private String value;

        FileType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    //Enum to hold the supported placeholder types
    private enum Placeholder {
        SYS("sys"), ENV("env"), SEC("sec");
        private String value;

        Placeholder(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private ConfigUtil() {

    }

    /**
     * This method reads the provided {@link File} and returns an object of type {@link ConfigFileType}
     * which was given as a input. That object has the new configurations as a String which you can get by
     * {@link ConfigFileType#getValue()} method.
     *
     * @param <T>   The class representing the configuration file type
     * @param file  Input config file
     * @param clazz Configuration file type which is a subclass of {@link ConfigFileType}.
     * @return The new configurations in the given format as a String.
     * @see ConfigFileType
     */
    public static <T extends ConfigFileType> T getConfig(File file, Class<T> clazz) {
        T newConfig;
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            newConfig = getConfig(fileInputStream, file.getName(), clazz);
        } catch (FileNotFoundException e) {
            logger.error("File not found at " + file.getAbsolutePath() + " : ", e);
            throw new RuntimeException("File not found at " + file.getAbsolutePath() + " : ", e);
        }
        return newConfig;
    }

    /**
     * This method reads the provided {@link FileInputStream} and returns an object of type
     * {@link ConfigFileType} which was given as a input. That object has the new configurations as a String
     * which you can get by {@link ConfigFileType#getValue()} method.
     *
     * @param <T>         The class representing the configuration file type
     * @param inputStream FileInputStream of the config file
     * @param fileNameKey Name of the config file
     * @param clazz       Configuration file type which is a subclass of {@link ConfigFileType}.
     * @return The new configurations in the given format as a String.
     * @see ConfigFileType
     */
    public static <T extends ConfigFileType> T getConfig(FileInputStream inputStream, String fileNameKey,
            Class<T> clazz) {

        String xmlString;
        //properties file can be directly load from the input stream
        if (clazz.isAssignableFrom(Properties.class)) {
            //Convert the properties file to XML format
            xmlString = Utils.convertPropertiesToXml(inputStream, ROOT_ELEMENT);
        } else {
            try (BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String inputString = bufferedReader.lines().collect(Collectors.joining("\n"));
                if (clazz.isAssignableFrom(org.wso2.carbon.kernel.utils.configfiletypes.XML.class)) {
                    xmlString = inputString;
                } else if (clazz.isAssignableFrom(YAML.class)) {
                    //Convert the file to XML format
                    xmlString = Utils.convertYAMLToXML(inputString, ROOT_ELEMENT);
                } else {
                    throw new RuntimeException("Unsupported class format:  " + clazz);
                }
            } catch (IOException e) {
                logger.error("IOException: ", e);
                throw new RuntimeException("IOException:  ", e);
            }
        }

        //Apply the new config
        xmlString = applyNewConfig(xmlString, fileNameKey);
        //Convert xml back to original format
        String convertedString = convertToOriginalFormat(xmlString, clazz);

        ConfigFileType baseObject;

        if (clazz.isAssignableFrom(YAML.class)) {
            baseObject = new YAML(convertedString);
        } else if (clazz.isAssignableFrom(org.wso2.carbon.kernel.utils.configfiletypes.XML.class)) {
            baseObject = new org.wso2.carbon.kernel.utils.configfiletypes.XML(convertedString);
        } else if (clazz.isAssignableFrom(Properties.class)) {
            baseObject = new Properties(convertedString);
        } else {
            throw new RuntimeException("Unsupported type " + clazz.getTypeName());
        }
        return clazz.cast(baseObject);
    }

    private static String convertToOriginalFormat(String xmlString, Class clazz) {
        switch (clazz.getName()) {
            case "org.wso2.carbon.kernel.utils.configfiletypes.XML":
                return xmlString;
            case "org.wso2.carbon.kernel.utils.configfiletypes.YAML":
                return Utils.convertXMLToYAML(xmlString, ROOT_ELEMENT);
            case "org.wso2.carbon.kernel.utils.configfiletypes.Properties":
                return Utils.convertXMLToProperties(xmlString, ROOT_ELEMENT);
            default:
                throw new RuntimeException("Unsupported class: " + clazz);
        }
    }

    /**
     * This method returns the configuration associated with the given key in the
     * {@link ConfigUtil#DEPLOYMENT_PROPERTIES_FILE}.
     *
     * @param key Key of the configuration
     * @return The new configuration getValue if the key. If it does not have a new getValue,
     * {@link RuntimeException} will be thrown.
     */
    public static String getConfig(String key) {
        String returnValue = null;
        int index = key.indexOf('/');
        if (index != -1) {
            //Get the filename and remove [,]
            String fileName = key.substring(0, index).replaceAll("[\\[\\]]", "");
            String xpath = key.substring(index);
            //Add root element for yaml, properties files
            if (fileName.matches(FILE_REGEX)) {
                xpath = "/" + ROOT_ELEMENT + xpath;
            }
            if (deploymentPropertiesMap.containsKey(fileName)) {
                Map<String, String> configMap = deploymentPropertiesMap.get(fileName);
                if (configMap.containsKey(xpath)) {
                    returnValue = configMap.get(xpath);
                    //If the value contain a placeholder, process the placeholder before returning the value
                    if (returnValue.matches(PLACEHOLDER_REGEX)) {
                        returnValue = processPlaceholder(returnValue);
                    }
                }
            } else {
                // TODO: 6/21/16 Do we need to throw an exception? or warn the user and return a null?
                logger.error(xpath + " was not found in " + DEPLOYMENT_PROPERTIES_FILE);
                throw new RuntimeException(xpath + " was not found in " + DEPLOYMENT_PROPERTIES_FILE);
            }
        } else {
            logger.error("Invalid key. Key should be in [filename]/xpath format");
            throw new RuntimeException("Invalid key. Key should be in [filename]/xpath format");
        }
        return returnValue;
    }

    /**
     * This method applies new configurations to given XML String
     *
     * @param xmlString   Current config in XML format
     * @param fileNameKey Filename of the current config
     * @return New config in XML formatted String
     */
    private static String applyNewConfig(String xmlString, String fileNameKey) {
        String updatedString;
        if (deploymentPropertiesMap.containsKey(fileNameKey)) {
            Map<String, String> newConfig = deploymentPropertiesMap.get(fileNameKey);
            StringReader stringReader = null;
            try {
                DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
                //Preventing Entity Expansion Attacks
                docBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
                stringReader = new StringReader(xmlString);
                InputSource inputSource = new InputSource(stringReader);
                Document document = docBuilder.parse(inputSource);
                XPath xPath = XPathFactory.newInstance().newXPath();

                newConfig.keySet().forEach(xPathKey -> {
                    try {
                        NodeList nodeList = (NodeList) xPath.compile(xPathKey)
                                                            .evaluate(document, XPathConstants.NODESET);
                        /* If deployment.properties has configs which is not in the original config
                         file, the nodeList.item(0) will be null. In this case, we have to throw an exception and
                         indicate that there are additional configs in the deployment.properties file which are not
                         in the original config file */
                        if (nodeList.item(0) != null) {
                            Node firstNode = nodeList.item(0);
                            firstNode.getFirstChild().setNodeValue(newConfig.get(xPathKey));
                        } else {
                            //If xpath in deployment.properties not found in the original config file, throw an
                            // exception
                            logger.error(
                                    xPathKey + " was not found in " + fileNameKey + " . remove this entry from the "
                                            + DEPLOYMENT_PROPERTIES_FILE + " or add this config to the original config "
                                            + "file");
                            throw new RuntimeException(
                                    xPathKey + " was not found in " + fileNameKey + " . remove this entry from the "
                                            + DEPLOYMENT_PROPERTIES_FILE + " or add this config to the original"
                                            + " config file");
                        }
                    } catch (XPathExpressionException e) {
                        logger.error("Exception occurred when applying xpath. Check the syntax and "
                                + "make sure it is a valid xpath: " + xPathKey, e);
                        throw new RuntimeException("Exception occurred when applying xpath. Check the syntax and "
                                + "make sure it is a valid xpath: " + xPathKey, e);
                    }
                });
                //Process the placeholders
                processPlaceholders(document.getDocumentElement().getChildNodes());
                updatedString = Utils.convertXMLtoString(document);
            } catch (ParserConfigurationException | IOException | SAXException e) {
                logger.error("Exception occurred when building document: ", e);
                throw new RuntimeException("Exception occurred when building document: ", e);
            } finally {
                if (stringReader != null) {
                    stringReader.close();
                }
            }
        } else {
            logger.debug("New configurations for " + fileNameKey + " was not found in " + DEPLOYMENT_PROPERTIES_FILE);
            //If no new configs found, just process the placeholders
            StringReader stringReader = null;
            try {
                DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
                //Preventing Entity Expansion Attacks
                docBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
                stringReader = new StringReader(xmlString);
                InputSource inputSource = new InputSource(stringReader);
                Document document = docBuilder.parse(inputSource);
                processPlaceholders(document.getDocumentElement().getChildNodes());
                updatedString = Utils.convertXMLtoString(document);
            } catch (ParserConfigurationException | IOException | SAXException e) {
                logger.error("Exception occurred when processing: ", e);
                throw new RuntimeException("Exception occurred when processing: ", e);
            } finally {
                if (stringReader != null) {
                    stringReader.close();
                }
            }
        }
        return updatedString;
    }

    /**
     * This method iterates through the given node list and replaces the placeholders with corresponding values
     *
     * @param nodeList Node list that needs to be checked for placeholders
     */
    private static void processPlaceholders(NodeList nodeList) {
        for (int count = 0; count < nodeList.getLength(); count++) {
            Node tempNode = nodeList.item(count);
            // Make sure that the node is a Element node
            if (tempNode.getNodeType() == Node.ELEMENT_NODE) {
                String value;
                //First child contains the values. Check whether child node exists
                if (tempNode.getFirstChild() != null) {
                    //We only need to process the node value only if values is not null. If a node does not contain
                    // any textual value (only contain sub elements), node value will be null
                    if (tempNode.getFirstChild().getNodeValue() != null) {
                        //Some nodes contain new line and space as node values. So we don't need to check those nodes
                        value = tempNode.getFirstChild().getNodeValue().trim();
                        if (value.length() != 0) {
                            String newValue = value;
                            if (value.matches(PLACEHOLDER_REGEX)) {
                                newValue = processPlaceholder(value);
                            }
                            //Set the new value
                            tempNode.getFirstChild().setNodeValue(newValue);
                        }
                    }
                }
                if (tempNode.hasAttributes()) {
                    // Get attributes' names and values
                    NamedNodeMap nodeMap = tempNode.getAttributes();
                    //Iterate through all attributes and process palceholders
                    for (int i = 0; i < nodeMap.getLength(); i++) {
                        Node node = nodeMap.item(i);
                        value = node.getNodeValue();
                        String newValue;
                        if (value.matches(PLACEHOLDER_REGEX)) {
                            newValue = processPlaceholder(value);
                        } else {
                            //If the value does not contain a placeholder, return the original value
                            newValue = value;
                        }
                        node.setNodeValue(newValue);
                    }
                }
                if (tempNode.hasChildNodes()) {
                    // Loop again if the current node has child nodes
                    processPlaceholders(tempNode.getChildNodes());
                }
            }
        }
    }

    /**
     * This method read the {@link ConfigUtil#DEPLOYMENT_PROPERTIES_FILE} on when this class loads
     *
     * @return Configurations in the {@link ConfigUtil#DEPLOYMENT_PROPERTIES_FILE} in Map format
     */
    private static Map<String, Map<String, String>> readDeploymentFile() {

        Map<String, Map<String, String>> tempPropertiesMap = new HashMap<>();
        java.util.Properties deploymentProperties = new java.util.Properties();
        InputStream input = null;
        try {
            File file = new File(DEPLOYMENT_PROPERTIES_FILE_PATH);
            if (file.exists()) {
                logger.debug(DEPLOYMENT_PROPERTIES_FILE_PATH + " found. Reading new config data.");
                input = new FileInputStream(file);
                deploymentProperties.load(input);
                //Process each entry in the deployment.properties file and add them to tempPropertiesMap
                deploymentProperties.keySet().forEach(key -> {
                    String keyString = key.toString();
                    int index = keyString.indexOf("/");
                    //Splitting the string
                    String fileName = keyString.substring(0, index).replaceAll("[\\[\\]]", "");
                    String xpath = keyString.substring(index);
                    String value = deploymentProperties.getProperty(keyString);
                    //Add root element for yml, properties files
                    if (fileName.matches(FILE_REGEX)) {
                        xpath = "/" + ROOT_ELEMENT + xpath;
                    }
                    //If the fileName is already in the tempPropertiesMap, update the 2nd map (which is the value of
                    // the tempPropertiesMap)
                    //Otherwise create a new map and add it to the tempPropertiesMap
                    if (tempPropertiesMap.containsKey(fileName)) {
                        Map<String, String> tempMap = tempPropertiesMap.get(fileName);
                        tempMap.put(xpath, value);
                    } else {
                        Map<String, String> tempMap = new HashMap<>();
                        tempMap.put(xpath, value);
                        tempPropertiesMap.put(fileName, tempMap);
                    }
                });
            }
        } catch (IOException ioException) {
            logger.error("Error occurred during reading the " + DEPLOYMENT_PROPERTIES_FILE +
                    " file. Error: ", ioException);
            throw new RuntimeException("Error occurred during reading the " + DEPLOYMENT_PROPERTIES_FILE +
                    " file. Error: ", ioException);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ioException2) {
                    logger.warn("Error occurred while closing the InputStream: ", ioException2);
                }
            }
        }
        return tempPropertiesMap;
    }

    /**
     * This method returns the new value after processing the placeholders. This method can process multiple
     * placeholders within the same String as well.
     *
     * @param inputString Placeholder that needs to be replaced
     * @return New getValue which corresponds to inputString
     */
    private static String processPlaceholder(String inputString) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(inputString);
        //Match all placeholders in the inputString
        while (matcher.find()) {
            //Group 3 corresponds to the key in the inputString
            String key = matcher.group(3);
            //Group 4 corresponds to the value of the inputString
            String value = matcher.group(4);
            //Group 7 corresponds to the default value in the inputString. If default value is not available, this
            // will be null
            String defaultValue = matcher.group(7);
            String newValue;
            switch (key) {
                case "env":
                    newValue = System.getenv(value);
                    if (newValue == null) {
                        if (defaultValue == null) {
                            throw new RuntimeException("Environment Variable " + value + " not found. Processing " +
                                    DEPLOYMENT_PROPERTIES_FILE + " failed. Placeholder: " + inputString);
                        } else {
                            inputString = inputString.replaceFirst(PLACEHOLDER_REGEX, "$1" + defaultValue + "$8");
                        }
                    } else {
                        inputString = inputString.replaceFirst(PLACEHOLDER_REGEX, "$1" + newValue + "$8");
                    }
                    break;
                case "sys":
                    newValue = System.getProperty(value);
                    if (newValue == null) {
                        if (defaultValue == null) {
                            throw new RuntimeException("System property" + value + " not found. Processing " +
                                    DEPLOYMENT_PROPERTIES_FILE + " failed. Placeholder: " + inputString);
                        } else {
                            inputString = inputString.replaceFirst(PLACEHOLDER_REGEX, "$1" + defaultValue + "$8");
                        }
                    } else {
                        inputString = inputString.replaceFirst(PLACEHOLDER_REGEX, "$1" + newValue + "$8");
                    }
                    break;
                case "sec":
                    //todo
                    inputString = "";
                    break;
                default:
                    throw new RuntimeException("Unsupported inputString key: " + key);
            }
        }
        return inputString;
    }

    /**
     * This method will concatenate and return the file types that need a root element. File types will be separated
     * by | token. This method will be used to create the {@link ConfigUtil#FILE_REGEX}
     *
     * @return String that contains file types which are separated by | token
     */
    private static String getFileTypesString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (FileType fileType : FileType.values()) {
            stringBuilder.append(fileType.getValue()).append("|");
        }
        String value = stringBuilder.toString();
        return value.substring(0, value.length() - 1);
    }

    /**
     * This method will concatenate and return the placeholder types.. Placeholder types will be separated
     * by | token. This method will be used to create the {@link ConfigUtil#PLACEHOLDER_REGEX}
     *
     * @return String that contains placeholder types which are separated by | token
     */
    private static String getPlaceholderString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Placeholder placeholder : Placeholder.values()) {
            stringBuilder.append(placeholder.getValue()).append("|");
        }
        String value = stringBuilder.toString();
        return value.substring(0, value.length() - 1);
    }

    /**
     * This method will return the {@link ConfigUtil#DEPLOYMENT_PROPERTIES_FILE} file path. Path will be read using
     * {@link ConfigUtil#DEPLOYMENT_PROPERTIES_VAR_NAME} environment variable or
     * {@link ConfigUtil#DEPLOYMENT_PROPERTIES_VAR_NAME}
     * system variable respectively. If both are null, CARBON_HOME/conf will be set as the default location.
     *
     * @return Location of the {@link ConfigUtil#DEPLOYMENT_PROPERTIES_FILE} file
     */
    private static String getPath() {

        String fileLocation = System.getenv(DEPLOYMENT_PROPERTIES_VAR_NAME);
        if (fileLocation == null) {
            fileLocation = System.getProperty(DEPLOYMENT_PROPERTIES_VAR_NAME);
            if (fileLocation == null) {
                logger.debug(DEPLOYMENT_PROPERTIES_VAR_NAME + " not found in both System,Environment variables");
                Path confHome = Utils.getCarbonConfigHome();
                Path path = Paths.get(confHome.toString(), "deployment.properties");
                logger.debug("deployment.properties file path is set to: " + path.toString());
                fileLocation = path.toString();
            } else {
                logger.debug(DEPLOYMENT_PROPERTIES_VAR_NAME + " location found in System variables. Location: "
                        + fileLocation);
            }
        } else {
            logger.debug(DEPLOYMENT_PROPERTIES_VAR_NAME + " location found in Environment variables. Location: " +
                    fileLocation);
        }
        return fileLocation;
    }
}
