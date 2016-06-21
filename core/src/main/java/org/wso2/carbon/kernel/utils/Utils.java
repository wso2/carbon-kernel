/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.kernel.Constants;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.management.ManagementPermission;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * Carbon utility methods.
 *
 * @since 5.0.0
 */
public class Utils {
    private static final Pattern varPattern = Pattern.compile("\\$\\{([^}]*)}");
    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    /**
     * Remove default constructor and make it not available to initialize.
     */

    private Utils() {
        throw new AssertionError("Instantiating utility class...");

    }

    /**
     * This method will return the carbon configuration directory path.
     * i.e ${carbon.home}/conf
     *
     * @return returns the Carbon Configuration directory path
     */
    public static Path getCarbonConfigHome() {
        return Paths.get(getCarbonHome().toString(), "conf");
    }

    /**
     * Returns the Carbon Home directory path. If {@code carbon.home} system property is not found, gets the
     * {@code CARBON_HOME_ENV} system property value and sets to the carbon home.
     *
     * @return returns the Carbon Home directory path
     */
    public static Path getCarbonHome() {
        String carbonHome = System.getProperty(Constants.CARBON_HOME);
        if (carbonHome == null) {
            carbonHome = System.getenv(Constants.CARBON_HOME_ENV);
            System.setProperty(Constants.CARBON_HOME, carbonHome);
        }
        return Paths.get(carbonHome);
    }

    /**
     * Replace system property holders in the property values.
     * e.g. Replace ${carbon.home} with value of the carbon.home system property.
     *
     * @param value string value to substitute
     * @return String substituted string
     */
    public static String substituteVariables(String value) {
        Matcher matcher = varPattern.matcher(value);
        boolean found = matcher.find();
        if (!found) {
            return value;
        }
        StringBuffer sb = new StringBuffer();
        do {
            String sysPropKey = matcher.group(1);
            String sysPropValue = getSystemVariableValue(sysPropKey, null);
            if (sysPropValue == null || sysPropValue.length() == 0) {
                String msg = "System property " + sysPropKey + " is not specified";
                logger.error(msg);
                throw new RuntimeException(msg);
            }
            // Due to reported bug under CARBON-14746
            sysPropValue = sysPropValue.replace("\\", "\\\\");
            matcher.appendReplacement(sb, sysPropValue);
        } while (matcher.find());
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * A utility which allows reading variables from the environment or System properties.
     * If the variable in available in the environment as well as a System property, the System property takes
     * precedence.
     *
     * @param variableName System/environment variable name
     * @param defaultValue default value to be returned if the specified system variable is not specified.
     * @return value of the system/environment variable
     */
    public static String getSystemVariableValue(String variableName, String defaultValue) {
        return getSystemVariableValue(variableName, defaultValue, Constants.PlaceHolders.class);
    }

    /**
     * A utility which allows reading variables from the environment or System properties.
     * If the variable in available in the environment as well as a System property, the System property takes
     * precedence.
     *
     * @param variableName  System/environment variable name
     * @param defaultValue  default value to be returned if the specified system variable is not specified.
     * @param constantClass Class from which the Predefined value should be retrieved if system variable and default
     *                      value is not specified.
     * @return value of the system/environment variable
     */
    public static String getSystemVariableValue(String variableName, String defaultValue, Class constantClass) {
        String value = null;
        if (System.getProperty(variableName) != null) {
            value = System.getProperty(variableName);
        } else if (System.getenv(variableName) != null) {
            value = System.getenv(variableName);
        } else {
            try {
                String constant = variableName.replaceAll("\\.", "_").toUpperCase(Locale.getDefault());
                Field field = constantClass.getField(constant);
                value = (String) field.get(constant);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                //Nothing to do
            }
            if (value == null) {
                value = defaultValue;
            }
        }
        return value;
    }

    /**
     * When the java security manager is enabled, the {@code checkSecurity} method can be used to protect/prevent
     * methods being executed by unsigned code.
     */
    public static void checkSecurity() {
        SecurityManager secMan = System.getSecurityManager();
        if (secMan != null) {
            secMan.checkPermission(new ManagementPermission("control"));
        }
    }

    /**
     * This method converts a given YAML String to XML format
     *
     * @param yamlString YAML String that needs to be converted to XML format
     * @return String in XML format
     */
    public static String convertYAMLToXML(String yamlString, String rootElement) {
        String jsonString = convertYAMLToJSON(yamlString);
        return convertJSONToXML(jsonString, rootElement);
    }

    /**
     * This method converts a given XML String to YAML format
     *
     * @param xmlString XML String that needs to be converted to YAML format
     * @return String in YAML format
     */
    public static String convertXMLToYAML(String xmlString, String rootElement) {
        String jsonString = convertXMLToJSON(xmlString);
        return convertJSONToYAML(jsonString, rootElement);
    }

    /**
     * This method converts a given YAML String to JSON format
     *
     * @param yamlString YAML String that needs to be converted to JSON format
     * @return String in JSON format
     */
    public static String convertYAMLToJSON(String yamlString) {
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
    public static String convertJSONToYAML(String jsonString, String rootElement) {
        Yaml yaml = new Yaml();
        Map map = yaml.loadAs(jsonString, Map.class);
        //Remove root element
        if (map.containsKey(rootElement)) {
            map = (Map) map.get(rootElement);
        } else {
            throw new RuntimeException("Root element not found when converting JSON to YAML");
        }
        return yaml.dumpAsMap(map);
    }

    /**
     * This method converts a given JSON String to XML format
     *
     * @param jsonString JSON String that needs to be converted to XML format
     * @return String in XML format
     */
    public static String convertJSONToXML(String jsonString, String rootElement) {
        String xmlString;
        try {
            JSONObject json = new JSONObject(jsonString);
            xmlString = XML.toString(json);
        } catch (JSONException e) {
            logger.error("Exception occurred while converting JSON to XML: ", e);
            throw new RuntimeException("Exception occurred while converting JSON to XML: ", e);
        }
        //Need to add a root element
        xmlString = createXmlElement(rootElement, xmlString);
        return xmlString;
    }

    /**
     * This method creates and returns a String formatted XML element
     *
     * @param tagName Tag name of the element
     * @param text    Text of the element
     * @return XML element in String format
     */
    public static String createXmlElement(String tagName, String text) {
        return "<" + tagName + ">" + text + "</" + tagName + ">";
    }

    /**
     * This method converts a given XML String to JSON format
     *
     * @param xmlString XML String that needs to be converted to JSON format
     * @return String in JSON format
     */
    public static String convertXMLToJSON(String xmlString) {
        String jsonString;
        try {
            JSONObject xmlJSONObj = XML.toJSONObject(xmlString);
            jsonString = xmlJSONObj.toString();
        } catch (JSONException e) {
            logger.error("Exception occurred while converting XML to JSON: ", e);
            throw new RuntimeException("Exception occurred while converting XML to JSON: ", e);
        }
        return jsonString;
    }

    /**
     * This method converts a Properties file to XML formatted String
     *
     * @param inputStream InputStream of the Properties file
     * @return String in XML format
     */
    public static String convertPropertiesToXml(InputStream inputStream, String rootElement) {
        String xmlString;
        java.util.Properties properties = new java.util.Properties();
        try {
            properties.load(inputStream);
            StringBuilder stringBuilder = new StringBuilder();
            properties.entrySet().forEach(entry -> stringBuilder
                    .append(createXmlElement(entry.getKey().toString(), entry.getValue().toString())));
            xmlString = stringBuilder.toString();
        } catch (IOException e) {
            logger.error("Exception occurred while converting Properties to XML: ", e);
            throw new RuntimeException("Exception occurred while converting Properties to XML: ", e);
        }
        //Need to add a root element
        xmlString = createXmlElement(rootElement, xmlString);
        return prettyFormatXMLString(xmlString);
    }

    /**
     * This method converts a given XML String to Properties format
     *
     * @param xmlString XML String that needs to be converted to Properties format
     * @return String in Properties format
     */
    public static String convertXMLToProperties(String xmlString, String rootElement) {
        String jsonString = convertXMLToJSON(xmlString);
        Yaml yaml = new Yaml();
        Map map = yaml.loadAs(jsonString, Map.class);
        //Remove root element
        if (map.containsKey(rootElement)) {
            map = (Map) map.get(rootElement);
        } else {
            throw new RuntimeException("Root element not found when converting XML to Properties");
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
     * This method properly formats a given XML String
     *
     * @param xmlString XML String that needs to be formatted
     * @return Properly formatted XML formatted String
     */
    public static String prettyFormatXMLString(String xmlString) {
        Source xmlInput = new StreamSource(new StringReader(xmlString));
        return convertXMLSourceToString(xmlInput);
    }

    /**
     * This method converts the given Document to String format
     *
     * @param doc Document that needs to be converted to XML formatted String
     * @return XML formatted String
     */
    public static String convertXMLtoString(Document doc) {
        DOMSource domSource = new DOMSource(doc);
        return convertXMLSourceToString(domSource);
    }

    /**
     * This method converts the given Source to XML formatted String
     *
     * @param source Source that needs to be converted to XML formatted String
     * @return XML formatted String
     */
    public static String convertXMLSourceToString(Source source) {
        String xmlString;
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
            logger.error("Exception occurred while converting doc to string: ", e);
            throw new RuntimeException("Exception occurred while converting doc to string: ", e);
        }
        return xmlString;
    }
}
