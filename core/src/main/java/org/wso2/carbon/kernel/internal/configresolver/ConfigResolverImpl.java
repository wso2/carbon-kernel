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
package org.wso2.carbon.kernel.internal.configresolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.kernel.configresolver.ConfigResolver;
import org.wso2.carbon.kernel.configresolver.ConfigResolverUtils;
import org.wso2.carbon.kernel.configresolver.configfiles.AbstractConfigFile;
import org.wso2.carbon.kernel.securevault.exception.SecureVaultException;
import org.wso2.carbon.kernel.utils.StringUtils;
import org.wso2.carbon.kernel.utils.Utils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
 * the name {@link ConfigResolverImpl#CONFIG_FILE_NAME}.
 *
 * @since 5.2.0
 */
public class ConfigResolverImpl implements ConfigResolver {

    private static final Logger logger = LoggerFactory.getLogger(ConfigResolverImpl.class.getName());
    //This is used to read the {@link ConfigUtil#CONFIG_FILE_NAME} file location using System Properties or
    // Environmental variables.
    private static final String FILE_PATH_KEY = "deployment.conf";
    //This variable contains the name of the config overriding file
    private static final String CONFIG_FILE_NAME = "deployment.properties";
    //This variable contains the value which will be used as the root element when converting yaml, properties files
    // to xml format
    private static final String ROOT_ELEMENT = "configurations";
    //This regex is used to identify yaml, properties files
    private static final String FILE_REGEX;
    //This regex is used to identify placeholders
    private static final String PLACEHOLDER_REGEX;
    //This is used to match placeholders
    private static final Pattern PLACEHOLDER_PATTERN;
    /*
     This map contains config data read from the deployment.properties file. The keys will be file names and values
     will be another map. In the 2nd map, the key will be the xpath and the value will be tha value in the
     deployment.properties file.
     */
    private static Map<String, Map<String, String>> deploymentPropertiesMap;

    static {
        FILE_REGEX = ".+\\.(" + getFileTypesString() + ")";
        PLACEHOLDER_REGEX = "(.*?)(\\$\\{(" + getPlaceholderString() + "):([^,]+?)((,)(.+?))?\\})(.*?)";
        PLACEHOLDER_PATTERN = Pattern.compile(PLACEHOLDER_REGEX);
        loadConfigs();
    }

    /**
     * This method will load configurations from the {@link ConfigResolverImpl#CONFIG_FILE_NAME} file. The reason for
     * this to be a separate method is to improve code coverage. We can change the visibility of this method to public
     * when testing and then use this to test loading file path from environment variables/system properties.
     */
    private static void loadConfigs() {
        String path = getPath();
        deploymentPropertiesMap = readDeploymentFile(path);
    }

    /**
     * Enum to hold the file types which we need to add root elements when converting to xml.
     */
    private enum FileType {

        YAML("yaml|yml"), PROPERTIES("properties");     //todo: Remove yml after renaming all yml files to yaml

        private String value;

        FileType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * Enum to hold the supported placeholder types.
     */
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

    public <T extends AbstractConfigFile> T getConfig(T configFile) {
        String newXmlString = applyNewConfig(configFile.getCanonicalContent(), configFile.getFilename());
        configFile.updateContent(newXmlString);
        return configFile;
    }

    /**
     * This method returns the configuration associated with the given key in the
     * {@link ConfigResolverImpl#CONFIG_FILE_NAME}.
     *
     * @param key Key of the configuration.
     * @return The new configuration if the key is found. If the key is not found, null is returned. This is because
     * the developer can check whether the given key is overridden or not.
     */
    public String getConfig(String key) {
        int index = key.indexOf('/');
        if (index == -1) {
            String msg = "Invalid key. Key should be in [filename]/xpath format.";
            logger.error(msg);
            throw new RuntimeException(msg);
        }
        //Get the filename and remove [,] which enclose the filename
        String fileName = key.substring(0, index).replaceAll("[\\[\\]]", "");
        String xpath = key.substring(index);
        //Add root element for yaml, properties files
        if (fileName.matches(FILE_REGEX)) {
            xpath = "/" + ROOT_ELEMENT + xpath;
        }
        if (!deploymentPropertiesMap.containsKey(fileName)) {
            //return null if the filename was not found
            logger.debug("Entry for filename {} was not found in {}", fileName, CONFIG_FILE_NAME);
            return null;
        }
        //Get the configurations map of the given file
        Map<String, String> configMap = deploymentPropertiesMap.get(fileName);
        //If the map has a new value for the given xpath
        if (configMap.containsKey(xpath)) {
            String returnValue = configMap.get(xpath);
            //If the value contain a placeholder, process the placeholder before returning the value
            if (returnValue.matches(PLACEHOLDER_REGEX)) {
                returnValue = processPlaceholder(returnValue);
            }
            return returnValue;
        }
        logger.debug("XPath {} under filename {} was not found in {}", xpath, fileName, CONFIG_FILE_NAME);
        return null;
    }

    /**
     * This method applies new configurations to given XML String.
     *
     * @param xmlString   Current config in XML format.
     * @param fileNameKey Filename of the current config.
     * @return New config in XML formatted String.
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
                newConfig.keySet()
                        .forEach(xPathKey -> {
                            try {
                                NodeList nodeList = (NodeList) xPath.compile(xPathKey).evaluate(document,
                                        XPathConstants.NODESET);
                                 /* If deployment.properties has configs which is not in the original config
                                 file, the nodeList.item(0) will be null. In this case, we have to throw an exception
                                  and indicate that there are additional configs in the deployment.properties file
                                  which are not in the original config file */
                                if (nodeList.item(0) == null) {
                                    //If xpath in deployment.properties not found in the original config file, throw an
                                    // exception
                                    String msg = String.format(
                                            "%s was not found in %s. Remove this entry from the %s or add this config"
                                                    + " to %s file.", xPathKey, fileNameKey, CONFIG_FILE_NAME,
                                            fileNameKey);
                                    logger.error(msg);
                                    throw new RuntimeException(msg);
                                }
                                Node firstNode = nodeList.item(0);
                                firstNode.getFirstChild().setNodeValue(newConfig.get(xPathKey));
                            } catch (XPathExpressionException e) {
                                String msg = String.format(
                                        "Exception occurred when applying xpath. Check the syntax and make sure it is"
                                                + " a valid xpath: %s", xPathKey);
                                logger.error(msg, e);
                                throw new RuntimeException(msg, e);
                            }
                        });
            }
            //process placeholders
            processPlaceholdersInXML(document.getDocumentElement().getChildNodes());
            return ConfigResolverUtils.convertXMLtoString(document);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            String msg = "Exception occurred when applying new config.";
            logger.error(msg, e);
            throw new RuntimeException(msg, e);
        } finally {
            if (stringReader != null) {
                stringReader.close();
            }
        }
    }

    /**
     * This method iterates through the given node list and replaces the placeholders with corresponding values.
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
            logger.debug("Traversing Node: {}", tempNode.getNodeName());

            /* First child contains the values. Check whether child node exists. If child exists, We only need to
            process the node value only if node value is not null. If a node does not contain any textual value (only
             contain sub elements), node value will be null */
            Node firstChild = tempNode.getFirstChild();
            if (firstChild != null && firstChild.getNodeValue() != null) {
                //Some nodes contain new line and space as node values. So we don't need to check those nodes
                String value = firstChild.getNodeValue().trim();
                if (value.length() != 0) {
                    setNewValue(firstChild, value);
                }
            }
            //Check attributes for placeholders
            if (tempNode.hasAttributes()) {
                // Get attributes' names and values
                NamedNodeMap nodeMap = tempNode.getAttributes();
                //Iterate through all attributes and process placeholders
                for (int i = 0; i < nodeMap.getLength(); i++) {
                    Node node = nodeMap.item(i);
                    logger.debug("Traversing Attribute: {}", node.getNodeName());
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
     * This helper method sets the given value to a placeholder.
     *
     * @param node  Node which contains the placeholder.
     * @param value Value of the node.
     */
    private static void setNewValue(Node node, String value) {
        if (value.matches(PLACEHOLDER_REGEX)) {
            logger.debug("Placeholder match found: {}", value);
            //Process the placeholder and set the new value
            String newValue = processPlaceholder(value);
            node.setNodeValue(newValue);
            logger.debug("Setting new value: {}", newValue);
        }
    }

    /**
     * This method read the {@link ConfigResolverImpl#CONFIG_FILE_NAME} on when this class loads.
     *
     * @return Configurations in the {@link ConfigResolverImpl#CONFIG_FILE_NAME} in Map format
     */
    private static Map<String, Map<String, String>> readDeploymentFile(String filePath) {
        Map<String, Map<String, String>> tempPropertiesMap = new HashMap<>();

        File file = new File(filePath);
        if (!file.exists()) {
            //If the file is not found, return an empty map
            logger.debug("{} not found.", filePath);
            return tempPropertiesMap;
        }

        try (InputStream input = new FileInputStream(file)) {
            logger.debug("{} found. Reading new config data.", filePath);

            java.util.Properties deploymentProperties = new java.util.Properties();
            deploymentProperties.load(input);
            //Process each entry in the deployment.properties file and add them to tempPropertiesMap
            deploymentProperties.entrySet()
                    .forEach(entry -> {
                        String keyString = entry.getKey().toString();
                        String value = entry.getValue().toString();

                        int index = keyString.indexOf("/");
                        //Splitting the string
                        String fileName = keyString.substring(0, index).replaceAll("[\\[\\]]", "");
                        String xpath = keyString.substring(index);
                        //Add root element for yml, properties files
                        if (fileName.matches(FILE_REGEX)) {
                            xpath = "/" + ROOT_ELEMENT + xpath;
                        }
                        //If the fileName is already in the tempPropertiesMap, update the 2nd map
                        // (which is the value of the tempPropertiesMap). Otherwise create a new map
                        // and add it to the tempPropertiesMap
                        if (tempPropertiesMap.containsKey(fileName)) {
                            Map<String, String> tempMap = tempPropertiesMap.get(fileName);
                            tempMap.put(xpath, value);
                        } else {
                            Map<String, String> tempMap = new HashMap<>();
                            tempMap.put(xpath, value);
                            tempPropertiesMap.put(fileName, tempMap);
                        }
                    });
        } catch (IOException ioException) {
            String msg = String.format("Error occurred during reading the %s file.", CONFIG_FILE_NAME);
            logger.error(msg, ioException);
            throw new RuntimeException(msg, ioException);
        }
        return tempPropertiesMap;
    }

    /**
     * This method returns the new value after processing the placeholders. This method can process multiple
     * placeholders within the same String as well.
     *
     * @param inputString Placeholder that needs to be replaced
     * @return New getContent which corresponds to inputString
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
                    try {
                        inputString = new String(ConfigResolverDataHolder.getInstance().getOptSecureVault()
                                .orElseThrow(() -> new RuntimeException("Secure Vault service is not available"))
                                .resolve(value));
                    } catch (SecureVaultException e) {
                        throw new RuntimeException("Unable to resolve the given alias", e);
                    }
                    break;
                default:
                    String msg = String.format("Unsupported placeholder: %s", key);
                    logger.error(msg);
                    throw new RuntimeException(msg);
            }
        }
        return inputString;
    }

    /**
     * This method process a given placeholder string and returns the string with replaced new value.
     *
     * @param func         Function to apply.
     * @param key          Environment Variable/System Property key.
     * @param inputString  String which needs to process.
     * @param defaultValue Default value of the placeholder. If default value is not available, this is null.
     * @param type         Type of the placeholder (env/sys/sec) This is used to print the error message.
     * @return String which has the new value instead of the placeholder.
     */
    private static String processValue(Function<String, String> func, String key, String inputString, String
            defaultValue, Placeholder type) {
        String newValue = func.apply(key);
        //If the new value is not null, replace the placeholder with the new value and return the string.
        if (newValue != null) {
            return inputString.replaceFirst(PLACEHOLDER_REGEX, "$1" + newValue + "$8");
        }
        //If the new value is empty and the default value is not empty, replace the placeholder with the default
        // value and return the string
        if (defaultValue != null) {
            return inputString.replaceFirst(PLACEHOLDER_REGEX, "$1" + defaultValue + "$8");
        }
        //Otherwise print an error message and throw na exception
        String msg;
        if (Placeholder.ENV.getValue().equals(type.getValue())) {
            msg = String.format("Environment variable %s not found. Processing %s failed. Placeholder: %s", key,
                    CONFIG_FILE_NAME, inputString);
        } else if (Placeholder.SYS.getValue().equals(type.getValue())) {
            msg = String.format("System property %s not found. Processing %s failed. Placeholder: %s", key,
                    CONFIG_FILE_NAME, inputString);
        } else {
            msg = String.format("Unsupported placeholder type: %s", type.getValue());
        }
        logger.error(msg);
        throw new RuntimeException(msg);
    }

    /**
     * This method will concatenate and return the file types that need a root element. File types will be separated
     * by | token. This method will be used to create the {@link ConfigResolverImpl#FILE_REGEX}.
     *
     * @return String that contains file types which are separated by | token.
     */
    private static String getFileTypesString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (FileType fileType : FileType.values()) {
            stringBuilder.append(fileType.getValue()).append("|");
        }
        String value = stringBuilder.substring(0, stringBuilder.length() - 1);
        logger.debug("FileTypes String: {}", value);
        return value;
    }

    /**
     * This method will concatenate and return the placeholder types.. Placeholder types will be separated
     * by | token. This method will be used to create the {@link ConfigResolverImpl#PLACEHOLDER_REGEX}.
     *
     * @return String that contains placeholder types which are separated by | token.
     */
    private static String getPlaceholderString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Placeholder placeholder : Placeholder.values()) {
            stringBuilder.append(placeholder.getValue()).append("|");
        }
        String value = stringBuilder.substring(0, stringBuilder.length() - 1);
        logger.debug("PlaceHolders String: {}", value);
        return value;
    }

    /**
     * This method will return the {@link ConfigResolverImpl#CONFIG_FILE_NAME} file path. Path will be read using
     * {@link ConfigResolverImpl#FILE_PATH_KEY} environment variable or {@link ConfigResolverImpl#FILE_PATH_KEY}
     * system variable respectively. If both are null, CARBON_HOME/conf will be set as the default location.
     *
     * @return Location of the {@link ConfigResolverImpl#CONFIG_FILE_NAME} file.
     */
    private static String getPath() {
        String fileLocation = System.getenv(FILE_PATH_KEY);
        if (!StringUtils.isNullOrEmpty(fileLocation)) {
            logger.debug("{} location found in system variables. Location: {}", FILE_PATH_KEY, fileLocation);
            return fileLocation;
        }
        if (fileLocation != null && fileLocation.isEmpty()) {
            logger.warn("{} found in environment variables. But value is empty.", FILE_PATH_KEY);
        }
        fileLocation = System.getProperty(FILE_PATH_KEY);
        if (!StringUtils.isNullOrEmpty(fileLocation)) {
            logger.debug("{} location found in environment variables. Location: {}", FILE_PATH_KEY, fileLocation);
            return fileLocation;
        }
        if (fileLocation != null && fileLocation.isEmpty()) {
            logger.warn("{} found in system property. But value is empty.", FILE_PATH_KEY);
        }
        logger.debug("{} not found in both System properties, Environment variables.", FILE_PATH_KEY);
        Path confHome = Utils.getCarbonConfigHome();
        Path path = Paths.get(confHome.toString(), CONFIG_FILE_NAME);
        logger.debug("{} file path is set to: {}", CONFIG_FILE_NAME, path.toString());
        return path.toString();
    }
}
