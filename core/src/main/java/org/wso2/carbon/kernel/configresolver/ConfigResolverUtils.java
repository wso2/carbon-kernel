package org.wso2.carbon.kernel.configresolver;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * Config Resolver utility methods.
 *
 * @since 5.2.0
 */
public class ConfigResolverUtils {
    private static final Logger logger = LoggerFactory.getLogger(ConfigResolverUtils.class);


    /**
     * This method converts a given YAML String to XML format.
     *
     * @param yamlString YAML String that needs to be converted to XML format
     * @return String in XML format
     */
    public static String convertYAMLToXML(String yamlString, String rootElement) {
        String jsonString = convertYAMLToJSON(yamlString);
        return convertJSONToXML(jsonString, rootElement);
    }

    /**
     * This method converts a given XML String to YAML format.
     *
     * @param xmlString XML String that needs to be converted to YAML format
     * @return String in YAML format
     */
    public static String convertXMLToYAML(String xmlString, String rootElement) {
        String jsonString = convertXMLToJSON(xmlString);
        return convertJSONToYAML(jsonString, rootElement);
    }

    /**
     * This method converts a given YAML String to JSON format.
     *
     * @param yamlString YAML String that needs to be converted to JSON format
     * @return String in JSON format
     */
    public static String convertYAMLToJSON(String yamlString) {
        Yaml yaml = new Yaml();
        Map map = (Map) yaml.load(yamlString);
        JSONObject jsonObject = new JSONObject(map);
        return jsonObject.toString();
    }

    /**
     * This method converts a given JSON String to YAML format.
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
     * This method converts a given JSON String to XML format.
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
     * This method creates and returns a String formatted XML element.
     *
     * @param tagName Tag name of the element
     * @param text    Text of the element
     * @return XML element in String format
     */
    public static String createXmlElement(String tagName, String text) {
        return "<" + tagName + ">" + text + "</" + tagName + ">";
    }

    /**
     * This method converts a given XML String to JSON format.
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
     * This method converts a Properties file to XML formatted String.
     *
     * @param inputStream InputStream of the Properties file
     * @return String in XML format
     */
    public static String convertPropertiesToXml(InputStream inputStream, String rootElement) {
        StringBuilder stringBuilder;
        java.util.Properties properties = new java.util.Properties();
        try {
            properties.load(inputStream);
            stringBuilder = new StringBuilder();
            properties.entrySet().forEach(entry -> stringBuilder
                    .append(createXmlElement(entry.getKey().toString(), entry.getValue().toString())));

        } catch (IOException e) {
            logger.error("Exception occurred while converting Properties to XML: ", e);
            throw new RuntimeException("Exception occurred while converting Properties to XML: ", e);
        }
        String xmlString = createXmlElement(rootElement, stringBuilder.toString());
        //Need to add a root element
        return prettyFormatXMLString(xmlString);
    }

    /**
     * This method converts a given XML String to Properties format.
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
        for (Object entryObject : map.entrySet()) {
            Map.Entry entry = (Map.Entry) entryObject;
            stringBuilder.append(entry.getKey()).append(" = ").append(entry.getValue()).append("\n");
        }
        return stringBuilder.toString();
    }

    /**
     * This method properly formats a given XML String.
     *
     * @param xmlString XML String that needs to be formatted
     * @return Properly formatted XML formatted String
     */
    public static String prettyFormatXMLString(String xmlString) {
        Source xmlInput = new StreamSource(new StringReader(xmlString));
        return convertXMLSourceToString(xmlInput);
    }

    /**
     * This method converts the given Document to String format.
     *
     * @param doc Document that needs to be converted to XML formatted String
     * @return XML formatted String
     */
    public static String convertXMLtoString(Document doc) {
        DOMSource domSource = new DOMSource(doc);
        return convertXMLSourceToString(domSource);
    }

    /**
     * This method converts the given Source to XML formatted String.
     *
     * @param source Source that needs to be converted to XML formatted String
     * @return XML formatted String
     */
    public static String convertXMLSourceToString(Source source) {
        try (StringWriter stringWriter = new StringWriter()) {
            StreamResult xmlOutput = new StreamResult(stringWriter);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", 4);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.transform(source, xmlOutput);
            return xmlOutput.getWriter().toString();
        } catch (TransformerException e) {
            logger.error("Exception occurred while converting doc to string: ", e);
            throw new RuntimeException("Exception occurred while converting doc to string: ", e);
        } catch (IOException e) {
            logger.error("Exception occurred while closing the StringWriter: ", e);
            throw new RuntimeException("Exception occurred while closing the StringWriter: ", e);
        }
    }
}
