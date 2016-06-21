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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * This util class provide the ability to override configurations in various components using a single
 * <b>deployment.properties</b> file.
 *
 * @since 5.2.0
 */
public final class ConfigUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigUtil.class.getName());
    private static final String DEPLOYMENT_PROPERTIES_FILE_NAME = "deployment.properties";
    private static final String DEPLOYMENT_PROPERTIES_FILE_PATH = getPath();
    private static final String ROOT_ELEMENT = "configurations";
    private static final String FILE_REGEX = ".+\\.(" + getFileTypesString() + ")";
    private static final String PLACEHOLDER_REGEX = "\\$\\s*(" + getPlaceholderString() + ")\\s*:(.+)";
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile(PLACEHOLDER_REGEX);
    private static final String PLACEHOLDER_WITH_DEFAULT_REGEX = "\\$\\s*(sys|env|sec)\\s*:(.+),(.+)"; // TODO: 6/21/16
    private static final Pattern PLACEHOLDER_WITH_DEFAULT_PATTERN = Pattern
            .compile(PLACEHOLDER_WITH_DEFAULT_REGEX); // TODO: 6/21/16
    //    (\${(sys|env|sec):([^,]+?)(,.+)?})
    //add comments
    private static final Map<String, Map<String, String>> deploymentPropertiesMap = readDeploymentFile();

    private enum ConfigFileFormat {
        YAML, XML, PROPERTIES
    }

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

    private static String getFileTypesString() {
        StringBuffer stringBuffer = new StringBuffer();
        for (FileType fileType : FileType.values()) {
            stringBuffer.append(fileType.getValue()).append("|");
        }
        String value = stringBuffer.toString();
        return value.substring(0, value.length() - 1);
    }

    private static String getPlaceholderString() {
        StringBuffer stringBuffer = new StringBuffer();
        for (Placeholder placeholder : Placeholder.values()) {
            stringBuffer.append(placeholder.getValue()).append("|");
        }
        String value = stringBuffer.toString();
        return value.substring(0, value.length() - 1);
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
        T newConfigs;
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            newConfigs = getConfig(fileInputStream, file.getName(), clazz);
        } catch (FileNotFoundException e) {
            LOGGER.error("File not found at " + file.getAbsolutePath() + " : ", e);
            throw new RuntimeException("File not found at " + file.getAbsolutePath() + " : ", e);
        }
        return newConfigs;
    }

    /**
     * This method reads the provided {@link FileInputStream} and returns an object of type
     * {@link ConfigFileType} which was given as a input. That object has the new configurations as a String
     * which you can get by {@link ConfigFileType#getValue()} method.
     *
     * @param <T>           The class representing the configuration file type
     * @param inputStream   FileInputStream of the config file
     * @param configFileKey Name of the config file
     * @param clazz         Configuration file type which is a subclass of {@link ConfigFileType}.
     * @return The new configurations in the given format as a String.
     * @see ConfigFileType
     */
    public static <T extends ConfigFileType> T getConfig(FileInputStream inputStream, String configFileKey,
            Class<T> clazz) {

        //This variable stores the original file format
        ConfigFileFormat originalFileFormat = null;
        String xmlString = null;
        String convertedString;

        //properties file can be directly load from the input stream
        if (clazz.isAssignableFrom(Properties.class)) {
            //Convert the properties file to XML format
            xmlString = Utils.convertPropertiesToXml(inputStream, ROOT_ELEMENT);
            originalFileFormat = ConfigFileFormat.PROPERTIES;
        } else {
            try (BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String inputString = bufferedReader.lines().collect(Collectors.joining("\n"));
                if (clazz.isAssignableFrom(org.wso2.carbon.kernel.utils.configfiletypes.XML.class)) {
                    xmlString = inputString;
                    originalFileFormat = ConfigFileFormat.XML;
                } else if (clazz.isAssignableFrom(YAML.class)) {
                    //Convert the file to XML format
                    xmlString = Utils.convertYAMLToXML(inputString, ROOT_ELEMENT);
                    originalFileFormat = ConfigFileFormat.YAML;
                } else {
                    throw new RuntimeException("Unsupported class format:  " + clazz);
                }
            } catch (IOException e) {
                LOGGER.error("IOException: ", e);
                throw new RuntimeException("IOException:  ", e);
            }
        }

        //Apply the new configs
        xmlString = applyNewConfig(xmlString, configFileKey);
        //Convert xml back to original format
        convertedString = convertToOriginalFormat(xmlString, originalFileFormat);

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

    private static String convertToOriginalFormat(String xmlString, ConfigFileFormat fileFormat) {
        switch (fileFormat) {
            case XML:
                return xmlString;
            case YAML:
                return Utils.convertXMLToYAML(xmlString, ROOT_ELEMENT);
            case PROPERTIES:
                return Utils.convertXMLToProperties(xmlString, ROOT_ELEMENT);
            default:
                throw new RuntimeException("Unsupported file format: " + fileFormat);
        }
    }

    /**
     * This method returns the configuration getValue associated with the given key in the <b>deployment.properties</b>
     * file.
     *
     * @param key Key of the configuration
     * @return The new configuration getValue if the key. If it does not have a new getValue, {@code null} will be
     * returned.
     */
    public static String getConfig(String key) {
        String returnValue = null;
        int index = key.indexOf('/');
        if (index != -1) {
            String fileName = key.substring(0, index).replaceAll("[\\[\\]]", "");
            String xpath = key.substring(index);

            //Add root element for yml, properties files
            if (fileName.matches(FILE_REGEX)) {
                xpath = "/" + ROOT_ELEMENT + xpath;
            }
            if (deploymentPropertiesMap.containsKey(fileName)) {
                Map<String, String> configMap = deploymentPropertiesMap.get(fileName);
                if (configMap.containsKey(xpath)) {
                    returnValue = configMap.get(xpath);
                    if (returnValue.matches(PLACEHOLDER_WITH_DEFAULT_REGEX)) {
                        returnValue = processPlaceholderWithDefaultValue(returnValue);
                    } else if (returnValue.matches(PLACEHOLDER_REGEX)) {
                        returnValue = processPlaceholder(returnValue);
                    }
                }
            } else {
                LOGGER.error(xpath + " was not found in " + DEPLOYMENT_PROPERTIES_FILE_NAME);
                throw new RuntimeException(xpath + " was not found in " + DEPLOYMENT_PROPERTIES_FILE_NAME);
            }
        }
        return returnValue;
    }

    /**
     * This method applies new configurations to given XML String
     *
     * @param xmlString Current configs in XML format
     * @param fileName  Filename of the current configs
     * @return New configs in XML formatted String
     */
    private static String applyNewConfig(String xmlString, String fileName) { // TODO: 6/20/16
        String formattedFileName = fileName;
        String updatedString;
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        //        docBuilderFactory .setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        if (deploymentPropertiesMap.containsKey(formattedFileName)) {
            Map<String, String> newConfigs = deploymentPropertiesMap.get(formattedFileName);
            try {
                DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
                Document document = docBuilder.parse(new InputSource(new StringReader(xmlString))); // TODO: 6/20/16
                XPath xPath = XPathFactory.newInstance().newXPath();
                newConfigs.keySet().forEach(xPathKey -> {
                    try {
                        NodeList nodeList = (NodeList) xPath.compile(xPathKey)
                                                            .evaluate(document, XPathConstants.NODESET);
                        if (nodeList.item(0) != null) { // TODO: 6/20/16 comments
                            Node firstNode = nodeList.item(0);
                            firstNode.getFirstChild().setNodeValue(newConfigs.get(xPathKey));
                        } else {
                            //If key in deployment.properties not found in the config file
                            LOGGER.error(xPathKey + " was not found in " + fileName);
                            throw new RuntimeException(xPathKey + " was not found in " + fileName);
                        }
                    } catch (XPathExpressionException e) {
                        LOGGER.error("Exception occurred when applying xpath: ", e);
                        throw new RuntimeException("Exception occurred when applying xpath: ", e);
                    }
                });
                //Process the placeholders
                processPlaceholders(document.getDocumentElement().getChildNodes());
                updatedString = Utils.convertXMLtoString(document);
            } catch (ParserConfigurationException | IOException | SAXException e) {
                LOGGER.error("Exception occurred when building document: ", e);
                throw new RuntimeException("Exception occurred when building document: ", e);
            }
        } else {
            LOGGER.debug("New configurations for " + formattedFileName + " was not found in "
                    + DEPLOYMENT_PROPERTIES_FILE_NAME);
            try {
                DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
                Document document = docBuilder.parse(new InputSource(new StringReader(xmlString)));
                processPlaceholders(document.getDocumentElement().getChildNodes());
                updatedString = Utils.convertXMLtoString(document);
            } catch (ParserConfigurationException | IOException | SAXException e) {
                LOGGER.error("Exception occurred when building document: ", e);
                throw new RuntimeException("Exception occurred when building document: ", e);
            }
        }
        return updatedString;
    }

    /**
     * This method iterates throught the given node list and replaces the placeholders with values
     *
     * @param nodeList Node list that needs to be checked for placeholders
     */
    private static void processPlaceholders(NodeList nodeList) {
        for (int count = 0; count < nodeList.getLength(); count++) {
            Node tempNode = nodeList.item(count);
            // Make sure that the node is a Element node
            if (tempNode.getNodeType() == Node.ELEMENT_NODE) {
                String value;
                if (tempNode.getFirstChild() != null) {
                    if (tempNode.getFirstChild().getNodeValue() != null) {
                        value = tempNode.getFirstChild().getNodeValue().trim();
                        if (value.length() != 0) {
                            String newValue = value;
                            if (value.matches(PLACEHOLDER_WITH_DEFAULT_REGEX)) {
                                newValue = processPlaceholderWithDefaultValue(value);
                            } else if (value.matches(PLACEHOLDER_REGEX)) {
                                newValue = processPlaceholder(value);
                            }
                            tempNode.getFirstChild().setNodeValue(newValue);
                        }
                    }
                }
                if (tempNode.hasAttributes()) {
                    // Get attributes' names and values
                    NamedNodeMap nodeMap = tempNode.getAttributes();
                    for (int i = 0; i < nodeMap.getLength(); i++) {
                        Node node = nodeMap.item(i);
                        value = node.getNodeValue();
                        String newValue = value;
                        if (value.matches(PLACEHOLDER_WITH_DEFAULT_REGEX)) {
                            newValue = processPlaceholderWithDefaultValue(value);
                        } else if (value.matches(PLACEHOLDER_REGEX)) {
                            newValue = processPlaceholder(value);
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
     * This method read the <b>deployment.properties</b> file on when this class loads
     *
     * @return Configurations in the <b>deployment.properties</b> file in Map format
     */
    private static Map<String, Map<String, String>> readDeploymentFile() {

        Map<String, Map<String, String>> tempPropertiesMap = new HashMap<>();
        java.util.Properties deploymentProperties = new java.util.Properties();
        InputStream input = null;

        try {
            File file = new File(DEPLOYMENT_PROPERTIES_FILE_PATH);

            if (file.exists()) {
                LOGGER.debug(DEPLOYMENT_PROPERTIES_FILE_PATH + " found. Reading new config data.");
                input = new FileInputStream(file);
                deploymentProperties.load(input);

                deploymentProperties.keySet().forEach(key -> {
                    String keyString = key.toString();
                    int index = keyString.indexOf("/");
                    String fileName = keyString.substring(0, index).replaceAll("[\\[\\]]", "");
                    String xpath = keyString.substring(index);
                    String value = deploymentProperties.getProperty(keyString);

                    //Add root element for yml, properties files
                    if (fileName.matches(FILE_REGEX)) {
                        xpath = "/" + ROOT_ELEMENT + xpath;
                    }
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
            LOGGER.error("Error occurred during reading the " + DEPLOYMENT_PROPERTIES_FILE_NAME +
                    " file. Error: ", ioException);
            throw new RuntimeException("Error occurred during reading the " + DEPLOYMENT_PROPERTIES_FILE_NAME +
                    " file. Error: ", ioException);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ioException2) {
                    LOGGER.warn("Error occurred while closing the InputStream: ", ioException2);
                }
            }
        }
        return tempPropertiesMap;
    }

    /**
     * This method returns the Environment, System, Secure getValue which correspond to the given placeholder
     *
     * @param placeholder Placeholder that needs to be replaced
     * @return New getValue which corresponds to placeholder
     */
    private static String processPlaceholder(String placeholder) {
        String newValue = placeholder;
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(placeholder);
        if (matcher.find()) {
            String key = matcher.group(1).trim();
            String value = matcher.group(2).trim();
            switch (key) {
                case "env":
                    newValue = System.getenv(value);
                    if (newValue == null) {
                        String failMessage = "Processing " + DEPLOYMENT_PROPERTIES_FILE_NAME + " failed.";
                        throw new RuntimeException("Environment Variable " + value + " not found." + failMessage);
                    }
                    break;
                case "sys":
                    newValue = System.getProperty(value);
                    if (newValue == null) {
                        String failMessage = "Processing " + DEPLOYMENT_PROPERTIES_FILE_NAME + " failed.";
                        throw new RuntimeException("System property " + value + " not found." + failMessage);
                    }
                    break;
                case "sec":
                    //todo
                    break;
                default:
                    throw new RuntimeException("Unidentified placeholder key: " + key);
            }
        }
        return newValue;
    }

    /**
     * This method returns the Environment, System, Secure getValue which correspond to the given placeholder. If the
     * Environment, System, Secure getValue is not available, it will return the default getValue.
     *
     * @param placeholder Placeholder that needs to be replaced
     * @return New getValue which corresponds to placeholder or the default getValue
     */
    private static String processPlaceholderWithDefaultValue(String placeholder) {
        String newValue = placeholder;
        Matcher matcher = PLACEHOLDER_WITH_DEFAULT_PATTERN.matcher(placeholder);
        if (matcher.find()) {
            String key = matcher.group(1).trim();
            String value = matcher.group(2).trim();
            String defaultValue = matcher.group(3).trim();
            switch (key) {
                case "env":
                    newValue = System.getenv(value);
                    if (newValue == null) {
                        newValue = defaultValue;
                    }
                    break;
                case "sys":
                    newValue = System.getProperty(value);
                    if (newValue == null) {
                        newValue = defaultValue;
                    }
                    break;
                case "sec":
                    //todo
                    break;
                default:
                    throw new RuntimeException("Unidentified placeholder key: " + key);
            }
        }
        return newValue;
    }

    private static String getPath() {
        Path confHome = Utils.getCarbonConfigHome();
        Path path = Paths.get(confHome.toString(), "deployment.properties");
        LOGGER.debug("deployment.properties file path: " + path.toString());
        return path.toString();
    }
}
