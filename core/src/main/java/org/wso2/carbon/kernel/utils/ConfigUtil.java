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
import org.wso2.carbon.kernel.utils.configfiletypes.AbstractConfigFileType;
import org.wso2.carbon.kernel.utils.configfiletypes.Properties;
import org.wso2.carbon.kernel.utils.configfiletypes.XML;
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
import java.util.ResourceBundle;
import java.util.function.Function;
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
 * the name {@link ConfigUtil#CONFIG_FILE_NAME}.
 *
 * @since 5.2.0
 */
public final class ConfigUtil {

    private static final Logger logger = LoggerFactory.getLogger(ConfigUtil.class.getName());
    //This is used to read the deployment.properties file location using System/Environment variables.
    private static final String FILE_PATH_KEY = "deployment.conf";
    //This variable contains the name of the config overriding file
    private static final String CONFIG_FILE_NAME = "deployment.properties";
    //This variable contains the value which will be used as the root element when converting yaml,properties files
    private static final String ROOT_ELEMENT = "configurations";
    //This regex is used to identify yaml, properties files
    private static final String FILE_REGEX;
    //This regex is used to identify placeholder values
    private static final String PLACEHOLDER_REGEX;
    private static final Pattern PLACEHOLDER_PATTERN;
    /*
     This map contains config data read from the deployment.properties file. The keys will be file names and values
     will be another map. In the 2nd map, the key will be the xpath and the value will be tha value in the
     deployment.properties file.
     */
    private static final Map<String, Map<String, String>> deploymentPropertiesMap;

    private static final ResourceBundle bundle;

    static {
        bundle = ResourceBundle.getBundle("Resources");
        FILE_REGEX = ".+\\.(" + getFileTypesString() + ")";
        PLACEHOLDER_REGEX = "(.*?)(\\$\\{(" + getPlaceholderString() + "):([^,]+?)((,)(.+?))?\\})(.*?)";
        String path = getPath();
        deploymentPropertiesMap = readDeploymentFile(path);
        PLACEHOLDER_PATTERN = Pattern.compile(PLACEHOLDER_REGEX);
    }

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
     * This method reads the provided {@link File} and returns an object of type {@link AbstractConfigFileType}
     * which was given as a input. That object has the new configurations as a String which you can get by
     * {@link AbstractConfigFileType#getValue()} method.
     *
     * @param <T>   The class representing the configuration file type
     * @param file  Input config file
     * @param clazz Configuration file type which is a subclass of {@link AbstractConfigFileType}.
     * @return The new configurations in the given format as a String.
     * @see AbstractConfigFileType
     */
    public static <T extends AbstractConfigFileType> T getConfig(File file, Class<T> clazz) {
        T newConfig;
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            newConfig = getConfig(fileInputStream, file.getName(), clazz);
        } catch (FileNotFoundException e) {
            String msg = String.format(bundle.getString("file.not.found"), file.getAbsolutePath());
            logger.error(msg, e);
            throw new RuntimeException(msg, e);
        }
        return newConfig;
    }

    /**
     * This method reads the provided {@link FileInputStream} and returns an object of type
     * {@link AbstractConfigFileType} which was given as a input. That object has the new configurations as a String
     * which you can get by {@link AbstractConfigFileType#getValue()} method.
     *
     * @param <T>         The class representing the configuration file type
     * @param inputStream FileInputStream of the config file
     * @param fileNameKey Name of the config file
     * @param clazz       Configuration file type which is a subclass of {@link AbstractConfigFileType}.
     * @return The new configurations in the given format as a String.
     * @see AbstractConfigFileType
     */
    public static <T extends AbstractConfigFileType> T getConfig(FileInputStream inputStream, String fileNameKey,
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
                if (clazz.isAssignableFrom(XML.class)) {
                    xmlString = inputString;
                } else if (clazz.isAssignableFrom(YAML.class)) {
                    //Convert the file to XML format
                    xmlString = Utils.convertYAMLToXML(inputString, ROOT_ELEMENT);
                } else {
                    String msg = String.format(bundle.getString("unsupported.class"), clazz.getName());
                    logger.error(msg);
                    throw new RuntimeException(msg);
                }
            } catch (IOException e) {
                String msg = bundle.getString("error.reading.inputstream");
                logger.error(msg, e);
                throw new RuntimeException(msg, e);
            }
        }

        //Apply the new config
        xmlString = applyNewConfig(xmlString, fileNameKey);
        //Convert xml back to original format
        String convertedString = convertToOriginalFormat(xmlString, clazz);

        AbstractConfigFileType baseObject;
        if (clazz == YAML.class) {
            baseObject = new YAML(convertedString);
        } else if (clazz == XML.class) {
            baseObject = new XML(convertedString);
        } else if (clazz == Properties.class) {
            baseObject = new Properties(convertedString);
        } else {
            String msg = String.format(bundle.getString("unsupported.class"), clazz.getTypeName());
            logger.error(msg);
            throw new RuntimeException(msg);
        }
        return clazz.cast(baseObject);
    }

    private static String convertToOriginalFormat(String xmlString, Class clazz) {
        if (clazz == YAML.class) {
            return Utils.convertXMLToYAML(xmlString, ROOT_ELEMENT);
        } else if (clazz == XML.class) {
            return xmlString;
        } else if (clazz == Properties.class) {
            return Utils.convertXMLToProperties(xmlString, ROOT_ELEMENT);
        } else {
            String msg = String.format(bundle.getString("unsupported.class"), clazz.getName());
            logger.error(msg);
            throw new RuntimeException(msg);
        }
    }

    /**
     * This method returns the configuration associated with the given key in the
     * {@link ConfigUtil#CONFIG_FILE_NAME}.
     *
     * @param key Key of the configuration
     * @return The new configuration getValue if the key. If it does not have a new getValue,
     * {@link RuntimeException} will be thrown.
     */
    public static String getConfig(String key) {
        int index = key.indexOf('/');
        if (index == -1) {
            String msg = bundle.getString("invalid.key");
            logger.error(msg);
            throw new RuntimeException(msg);
        } else {
            //Get the filename and remove [,]
            String fileName = key.substring(0, index).replaceAll("[\\[\\]]", "");
            String xpath = key.substring(index);
            //Add root element for yaml, properties files
            if (fileName.matches(FILE_REGEX)) {
                xpath = "/" + ROOT_ELEMENT + xpath;
            }
            if (!deploymentPropertiesMap.containsKey(fileName)) {
                String msg = String.format(bundle.getString("filename.not.found"), fileName, CONFIG_FILE_NAME);
                logger.error(msg);
                throw new RuntimeException(msg);
            } else {
                Map<String, String> configMap = deploymentPropertiesMap.get(fileName);
                if (configMap.containsKey(xpath)) {
                    String returnValue = configMap.get(xpath);
                    //If the value contain a placeholder, process the placeholder before returning the value
                    if (returnValue.matches(PLACEHOLDER_REGEX)) {
                        returnValue = processPlaceholder(returnValue);
                    }
                    return returnValue;
                } else {
                    String msg = String.format(bundle.getString("xpath.not.found"), xpath, fileName, CONFIG_FILE_NAME);
                    logger.error(msg);
                    throw new RuntimeException(msg);
                }
            }
        }
    }

    /**
     * This method applies new configurations to given XML String
     *
     * @param xmlString   Current config in XML format
     * @param fileNameKey Filename of the current config
     * @return New config in XML formatted String
     */
    private static String applyNewConfig(String xmlString, String fileNameKey) {
        StringReader stringReader = null;
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            //Preventing Entity Expansion Attacks
            docBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            stringReader = new StringReader(xmlString);
            InputSource inputSource = new InputSource(stringReader);
            Document document = docBuilder.parse(inputSource);
            //Apply new config if new config available
            if (deploymentPropertiesMap.containsKey(fileNameKey)) {
                XPath xPath = XPathFactory.newInstance().newXPath();
                Map<String, String> newConfig = deploymentPropertiesMap.get(fileNameKey);
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
                            String msg = String.format(bundle.getString("config.not.found"), xPathKey, fileNameKey,
                                    CONFIG_FILE_NAME, fileNameKey);
                            logger.error(msg);
                            throw new RuntimeException(msg);
                        }
                    } catch (XPathExpressionException e) {
                        String msg = String.format(bundle.getString("xpath.error"), xPathKey);
                        logger.error(msg, e);
                        throw new RuntimeException(msg, e);
                    }
                });
            }
            //process placeholders
            processPlaceholdersInXML(document.getDocumentElement().getChildNodes());
            return Utils.convertXMLtoString(document);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            String msg = bundle.getString("apply.new.config.error");
            logger.error(msg, e);
            throw new RuntimeException(msg, e);
        } finally {
            if (stringReader != null) {
                stringReader.close();
            }
        }
    }

    /**
     * This method iterates through the given node list and replaces the placeholders with corresponding values
     *
     * @param nodeList Node list that needs to be checked for placeholders
     */
    private static void processPlaceholdersInXML(NodeList nodeList) {
        //forEach not applicable to NodeList
        for (int count = 0; count < nodeList.getLength(); count++) {
            Node tempNode = nodeList.item(count);
            // Make sure that the node is a Element node. Otherwise continue to the next element
            if (tempNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            if (logger.isDebugEnabled()) {
                logger.debug(String.format(bundle.getString("traversing.node"), tempNode.getNodeName()));
            }
            /* First child contains the values. Check whether child node exists. If child exists, We only need to
            process the node value only if node value is not null. If a node does not contain any textual value (only
             contain sub elements), node value will be null */
            if (tempNode.getFirstChild() != null && tempNode.getFirstChild().getNodeValue() != null) {
                //Some nodes contain new line and space as node values. So we don't need to check those nodes
                String value = tempNode.getFirstChild().getNodeValue().trim();
                if (value.length() != 0) {
                    setNewValue(tempNode.getFirstChild(), value);
                }
            }
            if (tempNode.hasAttributes()) {
                // Get attributes' names and values
                NamedNodeMap nodeMap = tempNode.getAttributes();
                //Iterate through all attributes and process placeholders
                for (int i = 0; i < nodeMap.getLength(); i++) {
                    Node node = nodeMap.item(i);
                    if (logger.isDebugEnabled()) {
                        logger.debug(String.format(bundle.getString("traversing.attribute"), node.getNodeName()));
                    }
                    String value = node.getNodeValue();
                    setNewValue(node, value);
                }
            }
            if (tempNode.hasChildNodes()) {
                // Loop again if the current node has child nodes
                processPlaceholdersInXML(tempNode.getChildNodes());
            }
        }
    }

    /**
     * This helper method sets the value to a placeholder
     *
     * @param node  Node which contains the placeholder
     * @param value Value of the node
     */
    private static void setNewValue(Node node, String value) {
        if (value.matches(PLACEHOLDER_REGEX)) {
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("Placeholder match found: %s", value));
            }
            //Process the placeholder and set the new value
            String newValue = processPlaceholder(value);
            node.setNodeValue(newValue);
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("Setting new value: %s", newValue));
            }
        }
    }

    /**
     * This method read the {@link ConfigUtil#CONFIG_FILE_NAME} on when this class loads
     *
     * @return Configurations in the {@link ConfigUtil#CONFIG_FILE_NAME} in Map format
     */
    private static Map<String, Map<String, String>> readDeploymentFile(String filePath) {
        Map<String, Map<String, String>> tempPropertiesMap = new HashMap<>();
        java.util.Properties deploymentProperties = new java.util.Properties();
        InputStream input = null;
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return tempPropertiesMap;
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug(String.format(bundle.getString("conf.file.found"), filePath));
                }
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

                    if (logger.isDebugEnabled()) {
                        logger.debug(String.format("Processing - filename: %s ; xpath: %s ; value: %s", fileName, xpath,
                                value));
                    }

                    //Add root element for yml, properties files
                    if (fileName.matches(FILE_REGEX)) {
                        xpath = "/" + ROOT_ELEMENT + xpath;
                        if(logger.isDebugEnabled()){
                            logger.debug("Root element added");
                        }
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
            String msg = String.format(bundle.getString("error.reading.file"), CONFIG_FILE_NAME);
            logger.error(msg, ioException);
            throw new RuntimeException(msg, ioException);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ioException2) {
                    logger.warn(bundle.getString("error.closing.inputstream"), ioException2);
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
            switch (key) {
                case "env":
                    inputString = processValue(System::getenv, value, inputString, defaultValue, Placeholder.ENV);
                    break;
                case "sys":
                    inputString = processValue(System::getProperty, value, inputString, defaultValue, Placeholder.SYS);
                    break;
                case "sec":
                    //todo
                    inputString = "";
                    break;
                default:
                    String msg = String.format(bundle.getString("unsupported.placeholder"), key);
                    logger.error(msg);
                    throw new RuntimeException(msg);
            }
        }
        return inputString;
    }

    /**
     * This method process a given placeholder string and returns the string with replaced new value
     *
     * @param func         Function to apply
     * @param key          Environment Variable/System Property key
     * @param inputString  String which needs to process
     * @param defaultValue Default value of the placeholder. If default value is not available, this is null
     * @param type         Type of the placeholder (env/sys/sec) This is used to print the error message
     * @return String which has the new value instead of the placeholder
     */
    private static String processValue(Function<String, String> func, String key, String inputString,
            String defaultValue, Placeholder type) {
        String newValue = func.apply(key);
        if (newValue != null) {
            return inputString.replaceFirst(PLACEHOLDER_REGEX, "$1" + newValue + "$8");
        } else {
            if (defaultValue != null) {
                return inputString.replaceFirst(PLACEHOLDER_REGEX, "$1" + defaultValue + "$8");
            } else {
                String msg = String.format(bundle.getString("processing.placeholder.failed." + type.getValue()), key,
                        CONFIG_FILE_NAME, inputString);
                logger.error(msg);
                throw new RuntimeException(msg);
            }
        }
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
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("FileType String: %s", value));
        }
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
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("PlaceHolder String: %s", value));
        }
        return value.substring(0, value.length() - 1);
    }

    /**
     * This method will return the {@link ConfigUtil#CONFIG_FILE_NAME} file path. Path will be read using
     * {@link ConfigUtil#FILE_PATH_KEY} environment variable or
     * {@link ConfigUtil#FILE_PATH_KEY}
     * system variable respectively. If both are null, CARBON_HOME/conf will be set as the default location.
     *
     * @return Location of the {@link ConfigUtil#CONFIG_FILE_NAME} file
     */
    private static String getPath() {
        String fileLocation = System.getenv(FILE_PATH_KEY);
        if (!StringUtils.isNullOrEmpty(fileLocation)) {
            if (logger.isDebugEnabled()) {
                logger.debug(
                        String.format(bundle.getString("env.sys.var.found"), FILE_PATH_KEY, bundle.getString("system"),
                                fileLocation));
            }
            return fileLocation;
        } else {
            if (fileLocation != null && fileLocation.isEmpty()) {
                logger.warn(String.format(bundle.getString("empty.env.sys.variable"), bundle.getString("environment"),
                        FILE_PATH_KEY));
            }
            fileLocation = System.getProperty(FILE_PATH_KEY);
            if (!StringUtils.isNullOrEmpty(fileLocation)) {
                if (logger.isDebugEnabled()) {
                    logger.debug(String.format(bundle.getString("env.sys.var.found"), FILE_PATH_KEY,
                            bundle.getString("environment"), fileLocation));
                }
                return fileLocation;
            } else {
                if (fileLocation != null && fileLocation.isEmpty()) {
                    logger.warn(String.format(bundle.getString("empty.env.sys.variable"), bundle.getString("system"),
                            FILE_PATH_KEY));
                }
                if (logger.isDebugEnabled()) {
                    logger.debug(String.format(bundle.getString("env.sys.var.not.found"), FILE_PATH_KEY));
                }
                Path confHome = Utils.getCarbonConfigHome();
                Path path = Paths.get(confHome.toString(), CONFIG_FILE_NAME);
                if (logger.isDebugEnabled()) {
                    logger.debug(String.format(bundle.getString("path.set.to"), CONFIG_FILE_NAME, path.toString()));
                }
                return path.toString();
            }
        }
    }
}
