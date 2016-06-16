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

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.kernel.utils.configfiletypes.AbstractConfigFileType;
import org.wso2.carbon.kernel.utils.configfiletypes.Properties;
import org.wso2.carbon.kernel.utils.configfiletypes.YAML;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
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

    private static Logger logger = LoggerFactory.getLogger(ConfigUtil.class.getName());
    private static final String DEPLOYMENT_PROPERTIES_FILE_NAME = "deployment.properties";
    private static final String DEPLOYMENT_PROPERTIES_FILE_PATH = getPath();
    private static final String ROOT_ELEMENT = "configurations";
    private static final String FILE_REGEX = "\\[.+\\.(yml|properties)\\]";
    private static final String PLACEHOLDER_REGEX = "\\$\\s*(sys|env|sec)\\s*:(.+)";
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile(PLACEHOLDER_REGEX);
    private static final String PLACEHOLDER_WITH_DEFAULT_REGEX = "\\$\\s*(sys|env|sec)\\s*:(.+),(.+)";
    private static final Pattern PLACEHOLDER_WITH_DEFAULT_PATTERN = Pattern.compile(PLACEHOLDER_WITH_DEFAULT_REGEX);
    private static final Map<String, Map<String, String>> deploymentPropertiesMap = readDeploymentFile();

    private enum ConfigFileFormat {
        YAML, XML, PROPERTIES
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
     * @param klass Configuration file type which is a subclass of {@link AbstractConfigFileType}.
     * @return The new configurations in the given format as a String.
     * @see AbstractConfigFileType
     */
    public static <T extends AbstractConfigFileType> T getConfig(File file, Class<T> klass) {
        T newConfigs = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            newConfigs = getConfig(fileInputStream, file.getName(), klass);
        } catch (FileNotFoundException e) {
            logger.warn("File not found at " + file.getAbsolutePath() + " ; " + e);
        }
        return newConfigs;
    }

    /**
     * This method reads the provided {@link FileInputStream} and returns an object of type
     * {@link AbstractConfigFileType} which was given as a input. That object has the new configurations as a String
     * which you can get by {@link AbstractConfigFileType#getValue()} method.
     *
     * @param <T>         The class representing the configuration file type
     * @param inputStream FileInputStream of the config file
     * @param fileName    Name of the config file
     * @param klass       Configuration file type which is a subclass of {@link AbstractConfigFileType}.
     * @return The new configurations in the given format as a String.
     * @see AbstractConfigFileType
     */
    public static <T extends AbstractConfigFileType> T getConfig(FileInputStream inputStream, String fileName,
            Class<T> klass) {

        String xmlString = "";
        //This variable stores the original file format
        ConfigFileFormat configFileFormat = ConfigFileFormat.XML;
        String convertedString = null;

        //If an exception occur when processing, don't apply the new configurations and don't convert back to
        //original format.
        try {
            //properties file can be directly load from the input stream
            if (klass.isAssignableFrom(Properties.class)) {
                //Convert the properties file to XML format
                xmlString = convertPropertiesToXml(inputStream);
                configFileFormat = ConfigFileFormat.PROPERTIES;
            } else {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();

                //Convert the file to XML format
                if (klass.isAssignableFrom(org.wso2.carbon.kernel.utils.configfiletypes.XML.class)) {
                    xmlString = stringBuilder.toString();
                    configFileFormat = ConfigFileFormat.XML;
                } else if (klass.isAssignableFrom(YAML.class)) {
                    xmlString = convertToXml(stringBuilder.toString(), ConfigFileFormat.YAML);
                    configFileFormat = ConfigFileFormat.YAML;
                }
            }
            if (xmlString.length() != 0) {
                //Apply the new configs
                xmlString = applyNewConfigs(xmlString, fileName);
                //Convert xml back to original format
                convertedString = convertToOriginalFormat(xmlString, configFileFormat);
            }
        } catch (UnsupportedEncodingException e) {
            logger.error("Cannot read FileInputStream:  " + e);
        } catch (IOException e) {
            logger.error("IOException:  " + e);
        }

        AbstractConfigFileType baseObject;
        if (klass.isAssignableFrom(YAML.class)) {
            baseObject = new YAML();
        } else if (klass.isAssignableFrom(org.wso2.carbon.kernel.utils.configfiletypes.XML.class)) {
            baseObject = new org.wso2.carbon.kernel.utils.configfiletypes.XML();
        } else if (klass.isAssignableFrom(Properties.class)) {
            baseObject = new Properties();
        } else {
            throw new RuntimeException("Unsupported type " + klass.getTypeName());
        }
        baseObject.setValue(convertedString);

        return klass.cast(baseObject);
    }

    private static String convertToOriginalFormat(String xmlString, ConfigFileFormat fileFormat) {
        switch (fileFormat) {
            case XML:
                return xmlString;
            case YAML:
                return convertXMLToYAML(xmlString);
            case PROPERTIES:
                return convertXMLToProperties(xmlString);
            default:
                throw new RuntimeException("Unsupported file format: " + fileFormat);
        }
    }

    /**
     * This method returns the configuration value associated with the given key in the <b>deployment.properties</b>
     * file.
     *
     * @param key Key of the configuration
     * @return The new configuration value if the key. If it does not have a new value, {@code null} will be returned.
     */
    public static String getConfig(String key) {
        String returnValue = null;
        int index = key.indexOf('/');
        if (index != -1) {
            String fileName = key.substring(0, index);
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
                logger.debug(xpath + " was not found");
            }
        }
        return returnValue;
    }

    /**
     * This method converts a String in given format to XML format
     *
     * @param data             String to be converted
     * @param configFileFormat Current format of the String
     * @return String in XML format
     */
    private static String convertToXml(String data, ConfigFileFormat configFileFormat) {
        //No need to convert xml to xml
        String convertedConfig = data;
        if (configFileFormat == ConfigFileFormat.YAML) {
            convertedConfig = convertYAMLToXML(data);
        } else {
            logger.error("Unsupported file format: " + configFileFormat);
        }
        return convertedConfig;
    }

    /**
     * This method converts a given YAML String to XML format
     *
     * @param yamlString YAML String that needs to be converted to XML format
     * @return String in XML format
     */
    private static String convertYAMLToXML(String yamlString) {
        String jsonString = convertYAMLToJSON(yamlString);
        return convertJSONToXML(jsonString);
    }

    /**
     * This method converts a given XML String to YAML format
     *
     * @param xmlString XML String that needs to be converted to YAML format
     * @return String in YAML format
     */
    private static String convertXMLToYAML(String xmlString) {
        String jsonString = convertXMLToJSON(xmlString);
        return convertJSONToYAML(jsonString);
    }

    /**
     * This method converts a given YAML String to JSON format
     *
     * @param yamlString YAML String that needs to be converted to JSON format
     * @return String in JSON format
     */
    private static String convertYAMLToJSON(String yamlString) {
        String jsonString;
        Yaml yaml = new Yaml();
        Map map = (Map) yaml.load(yamlString);
        JSONObject jsonObject = new JSONObject(map);
        jsonString = jsonObject.toString();
        return jsonString;
    }

    /**
     * This method converts a given JSON String to YAML format
     *
     * @param jsonString JSON String that needs to be converted to YAML format
     * @return String in YAML format
     */
    private static String convertJSONToYAML(String jsonString) {
        Yaml yaml = new Yaml();
        Map map = yaml.loadAs(jsonString, Map.class);
        //Remove root element
        if (map.containsKey(ROOT_ELEMENT)) {
            map = (Map) map.get(ROOT_ELEMENT);
        }
        return yaml.dumpAsMap(map);
    }

    /**
     * This method converts a given JSON String to XML format
     *
     * @param jsonString JSON String that needs to be converted to XML format
     * @return String in XML format
     */
    private static String convertJSONToXML(String jsonString) {
        String xmlString = "";
        try {
            JSONObject json = new JSONObject(jsonString);
            xmlString = XML.toString(json);
        } catch (JSONException e) {
            logger.error("Exception occurred while converting JSON to XML: " + e);
        }
        //Need to add a root element
        xmlString = createXmlElement(ROOT_ELEMENT, xmlString);
        return xmlString;
    }

    /**
     * This method converts a given XML String to JSON format
     *
     * @param xmlString XML String that needs to be converted to JSON format
     * @return String in JSON format
     */
    private static String convertXMLToJSON(String xmlString) {
        String jsonString = "";
        try {
            JSONObject xmlJSONObj = XML.toJSONObject(xmlString);
            jsonString = xmlJSONObj.toString();
        } catch (JSONException e) {
            logger.error("Exception occurred while converting XML to JSON: " + e);
        }
        return jsonString;
    }

    /**
     * This method converts a Properties file to XML formatted String
     *
     * @param inputStream InputStream of the Properties file
     * @return String in XML format
     */
    private static String convertPropertiesToXml(InputStream inputStream) {
        String xmlString = "";
        java.util.Properties deploymentProperties = new java.util.Properties();
        try {
            deploymentProperties.load(inputStream);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(xmlString);
            for (Map.Entry<Object, Object> entry : deploymentProperties.entrySet()) {
                stringBuilder.append(createXmlElement(entry.getKey().toString(), entry.getValue().toString()));
            }
            xmlString = stringBuilder.toString();
        } catch (IOException e) {
            logger.error("Exception occurred while converting Properties to XML: " + e);
        }
        //Need to add a root element
        xmlString = createXmlElement(ROOT_ELEMENT, xmlString);
        return prettyFormatXMLString(xmlString);
    }

    /**
     * This method converts a given XML String to Properties format
     *
     * @param xmlString XML String that needs to be converted to Properties format
     * @return String in Properties format
     */
    private static String convertXMLToProperties(String xmlString) {
        String jsonString = convertXMLToJSON(xmlString);
        Yaml yaml = new Yaml();
        Map map = yaml.loadAs(jsonString, Map.class);
        //Remove root element
        if (map.containsKey(ROOT_ELEMENT)) {
            map = (Map) map.get(ROOT_ELEMENT);
        }
        StringBuilder stringBuilder = new StringBuilder();
        String tempString;
        for (Object entryObject : map.entrySet()) {
            Map.Entry entry = (Map.Entry) entryObject;
            tempString = entry.getKey() + " = " + entry.getValue();
            stringBuilder.append(tempString).append("\n");
        }
        return stringBuilder.toString();
    }

    /**
     * This method creates and returns a String formatted XML element
     *
     * @param tagName Tag name of the element
     * @param text    Text of the element
     * @return XML element in String format
     */
    private static String createXmlElement(String tagName, String text) {
        return "<" + tagName + ">" + text + "</" + tagName + ">";
    }

    /**
     * This method applies new configurations to given XML String
     *
     * @param xmlString Current configs in XML format
     * @param fileName  Filename of the current configs
     * @return New configs in XML formatted String
     */
    private static String applyNewConfigs(String xmlString, String fileName) {

        String formattedFileName = "[" + fileName + "]";
        String updatedString = xmlString;

        if (deploymentPropertiesMap.containsKey(formattedFileName)) {
            Map<String, String> newConfigs = deploymentPropertiesMap.get(formattedFileName);
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();

            try {
                DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
                Document document = docBuilder.parse(new InputSource(new StringReader(xmlString)));
                XPath xPath = XPathFactory.newInstance().newXPath();

                newConfigs.keySet().forEach(xPathKey -> {
                    try {
                        NodeList nodeList = (NodeList) xPath.compile(xPathKey)
                                                            .evaluate(document, XPathConstants.NODESET);
                        if (nodeList.item(0) != null) {
                            Node firstNode = nodeList.item(0);
                            firstNode.getFirstChild().setNodeValue(newConfigs.get(xPathKey));
                        } else {
                            //If key in deployment.properties not found in the config file
                            logger.error(xPathKey + " was not found in " + fileName);
                            throw new RuntimeException(xPathKey + " was not found in " + fileName);
                        }
                    } catch (XPathExpressionException e) {
                        logger.error("Exception occurred when applying xpath: " + e);
                        throw new RuntimeException("Exception occurred when applying xpath: " + e);
                    }
                });
                //Process the placeholders
                processPlaceholders(document.getDocumentElement().getChildNodes());
                updatedString = convertXMLtoString(document);
            } catch (ParserConfigurationException | IOException | SAXException e) {
                logger.error("Exception occurred when building document: " + e);
                throw new RuntimeException("Exception occurred when building document: " + e);
            }
        } else {
            logger.debug("New configurations for " + formattedFileName + " was not found in "
                    + DEPLOYMENT_PROPERTIES_FILE_NAME);
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
     * This method properly formats a given XML String
     *
     * @param xmlString XML String that needs to be formatted
     * @return Properly formatted XML formatted String
     */
    private static String prettyFormatXMLString(String xmlString) {
        Source xmlInput = new StreamSource(new StringReader(xmlString));
        return convertXMLSourceToString(xmlInput);
    }

    /**
     * This method converts the given Document to String format
     *
     * @param doc Document that needs to be converted to XML formatted String
     * @return XML formatted String
     */
    private static String convertXMLtoString(Document doc) {
        DOMSource domSource = new DOMSource(doc);
        return convertXMLSourceToString(domSource);
    }

    /**
     * This method converts the given Source to XML formatted String
     *
     * @param source Source that needs to be converted to XML formatted String
     * @return XML formatted String
     */
    private static String convertXMLSourceToString(Source source) {
        String xmlString = "";
        try {
            StringWriter stringWriter = new StringWriter();
            StreamResult xmlOutput = new StreamResult(stringWriter);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", 4);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.transform(source, xmlOutput);
            xmlString = xmlOutput.getWriter().toString();
        } catch (TransformerException e) {
            logger.error("Exception occurred while converting doc to string: " + e);
        }
        return xmlString;
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
                logger.debug(DEPLOYMENT_PROPERTIES_FILE_PATH + " found. Reading new config data.");
                input = new FileInputStream(file);
                deploymentProperties.load(input);

                deploymentProperties.keySet().forEach(key -> {
                    String keyString = key.toString();
                    int index = keyString.indexOf("/");
                    String fileName = keyString.substring(0, index);
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
            logger.error("Error occurred during reading the " + DEPLOYMENT_PROPERTIES_FILE_NAME +
                    " file. Error: " + ioException.toString());
            throw new RuntimeException("Error occurred during reading the " + DEPLOYMENT_PROPERTIES_FILE_NAME +
                    " file. Error: " + ioException.toString());
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ioException2) {
                    logger.warn("Error occurred while closing the InputStream: " + ioException2);
                }
            }
        }
        return tempPropertiesMap;
    }

    /**
     * This method returns the Environment, System, Secure value which correspond to the given placeholder
     *
     * @param placeholder Placeholder that needs to be replaced
     * @return New value which corresponds to placeholder
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
     * This method returns the Environment, System, Secure value which correspond to the given placeholder. If the
     * Environment, System, Secure value is not available, it will return the default value.
     *
     * @param placeholder Placeholder that needs to be replaced
     * @return New value which corresponds to placeholder or the default value
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
        logger.debug("deployment.properties file path: " + path.toString());
        return path.toString();
    }
}
