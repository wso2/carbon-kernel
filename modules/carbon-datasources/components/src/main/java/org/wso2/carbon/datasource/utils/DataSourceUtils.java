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
package org.wso2.carbon.datasource.utils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.wso2.carbon.datasource.core.exception.DataSourceException;
import org.wso2.carbon.datasource.rdbms.RDBMSDataSourceConstants;
import org.wso2.carbon.kernel.utils.Utils;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Data Sources utility class.
 */
public class DataSourceUtils {

    private static Log log = LogFactory.getLog(DataSourceUtils.class);

    private static final String XML_DECLARATION = "xml-declaration";

    private static final String DATASOURCES_DIRECTORY_NAME = "datasources";

    private static ThreadLocal<String> dataSourceId = new ThreadLocal<String>() {
        protected synchronized String initialValue() {
            return null;
        }
    };

    public static String getCurrentDataSourceId() {
        return dataSourceId.get();
    }

    public static boolean nullAllowEquals(Object lhs, Object rhs) {
        return lhs == null && rhs == null || !((lhs == null && rhs != null) || (lhs != null && rhs == null))
                && lhs.equals(rhs);
    }

    public static String elementToString(Element element) {
        try {
            if (element == null) {
                                /* return an empty string because, the other way around works the same,
                                where if we give a empty string as the XML, we get a null element
                                from "stringToElement" */
                return "";
            }
            Document document = element.getOwnerDocument();
            DOMImplementationLS domImplLS = (DOMImplementationLS) document.getImplementation();
            LSSerializer serializer = domImplLS.createLSSerializer();
            //by default its true, so set it to false to get String without xml-declaration
            serializer.getDomConfig().setParameter(XML_DECLARATION, false);
            return serializer.writeToString(element);
        } catch (Exception e) {
            log.error("Error while converting element to string: " + e.getMessage(), e);
            return null;
        }
    }

    public static Document convertToDocument(File file) throws DataSourceException {
        try {
            return getSecuredDocumentBuilder(false).parse(file);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new DataSourceException("Error in creating an XML document from file: " + e.getMessage(), e);
        }
    }

    /**
     * This method provides a secured document builder which will secure XXE attacks.
     *
     * @param setIgnoreComments whether to set setIgnoringComments in DocumentBuilderFactory.
     * @return DocumentBuilder
     * @throws ParserConfigurationException
     */
    private static DocumentBuilder getSecuredDocumentBuilder(boolean setIgnoreComments) throws
            ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setIgnoringComments(setIgnoreComments);
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilderFactory.setExpandEntityReferences(false);
        documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        documentBuilder.setEntityResolver((publicId, systemId) -> {
            throw new SAXException("Possible XML External Entity (XXE) attack. Skip resolving entity");
        });
        return documentBuilder;
    }

    /**
     * Returns the conf directory path located in carbon.home.
     *
     * @return {@link Path}
     */
    public static Path getDataSourceConfigPath() {
        return Utils.getCarbonConfigHome().resolve(DATASOURCES_DIRECTORY_NAME);
    }


    /**
     * Replaces system variables in the input xml configuration.
     *
     * @param xmlConfiguration InputStream that carries xml configuration
     * @return returns a InputStream that has evaluated system variables in input
     * @throws DataSourceException
     */
    public static InputStream replaceSystemVariablesInXml(InputStream xmlConfiguration) throws DataSourceException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder;
        Document doc;
        try {
            documentBuilderFactory.setNamespaceAware(true);
            documentBuilderFactory.setExpandEntityReferences(false);
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
//            SecurityManager securityManager = new SecurityManager();
//            securityManager.setEntityExpansionLimit(ENTITY_EXPANSION_LIMIT);
//            documentBuilderFactory.setAttribute(SECURITY_MANAGER_PROPERTY, securityManager);
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            documentBuilder.setEntityResolver((publicId, systemId) -> {
                throw new SAXException("Possible XML External Entity (XXE) attack. Skip resolving entity");
            });
            doc = documentBuilder.parse(xmlConfiguration);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new DataSourceException("Error in building Document", e);
        }
        NodeList nodeList = null;
        if (doc != null) {
            nodeList = doc.getElementsByTagName("*");
        }
        if (nodeList != null) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                resolveLeafNodeValue(nodeList.item(i));
            }
        }
        return toInputStream(doc);
    }

    /**
     * @param doc the DOM.Document to be converted to InputStream.
     * @return Returns InputStream.
     * @throws DataSourceException
     */
    public static InputStream toInputStream(Document doc) throws DataSourceException {
        InputStream in;
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Source xmlSource = new DOMSource(doc);
            Result result = new StreamResult(outputStream);
            TransformerFactory.newInstance().newTransformer().transform(xmlSource, result);
            in = new ByteArrayInputStream(outputStream.toByteArray());
        } catch (TransformerException e) {
            throw new DataSourceException("Error in transforming DOM to InputStream", e);
        }
        return in;
    }

    public static void resolveLeafNodeValue(Node node) {
        if (node != null) {
            Element element = (Element) node;
            NodeList childNodeList = element.getChildNodes();
            for (int j = 0; j < childNodeList.getLength(); j++) {
                Node chileNode = childNodeList.item(j);
                if (!chileNode.hasChildNodes()) {
                    String nodeValue = resolveSystemProperty(chileNode.getTextContent());
                    childNodeList.item(j).setTextContent(nodeValue);
                } else {
                    resolveLeafNodeValue(chileNode);
                }
            }
        }
    }

    /**
     * Replaces system variables in the input xml configuration.
     *
     * @param xmlConfiguration String
     * @return String
     * @throws DataSourceException
     */
    public static String replaceSystemVariablesInXml(String xmlConfiguration) throws DataSourceException {
        InputStream in = replaceSystemVariablesInXml(new ByteArrayInputStream(xmlConfiguration.getBytes()));
        try {
            xmlConfiguration = IOUtils.toString(in);
        } catch (IOException e) {
            throw new DataSourceException("Error in converting InputStream to String", e);
        }
        return xmlConfiguration;
    }


    public static String resolveSystemProperty(String text) {
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

    public static Map<String, String> extractPrimitiveFieldNameValuePairs(Object object) throws DataSourceException {
        Map<String, String> nameValueMap = new HashMap<>();
        Method methods[] = object.getClass().getMethods();
        for (Method method : methods) {
            if (isMethodMatched(method)) {
                String FieldName = getFieldNameFromMethodName(method.getName());
                try {
                    if (method.invoke(object) != null) {
                        String result = method.invoke(object).toString();
                        nameValueMap.put(FieldName, result);
                    }
                } catch (Exception e) {
                    throw new DataSourceException("Error in retrieving " + FieldName + " value from the object :" +
                            object.getClass() + e.getMessage(), e);
                }
            }
        }
        return nameValueMap;
    }

    private static String getFieldNameFromMethodName(String name) throws DataSourceException {
        String prefixGet = "get";
        String prefixIs = "is";
        String firstLetter;

        if (name.startsWith(prefixGet)) {
            firstLetter = name.substring(3, 4);
            name = name.substring(4);
        } else if (name.startsWith(prefixIs)) {
            firstLetter = name.substring(2, 3);
            name = name.substring(3);
        } else {
            throw new DataSourceException("Error in retrieving attribute name from method : " + name);
        }
        firstLetter = firstLetter.toLowerCase();
        return firstLetter.concat(name);
    }

    private static boolean isMethodMatched(Method method) {
        String returnType = method.getReturnType().getSimpleName();
        String methodName = method.getName();

        if (!Modifier.isPublic(method.getModifiers())) {
            return false;
        }
        if (returnType.equals("void")) {
            return false;
        }
        if (!(methodName.startsWith("get") ||
                (methodName.startsWith("is") && (returnType.equals("Boolean") || returnType.equals("boolean"))))) {
            return false;
        }
        if (!(method.getReturnType().isPrimitive() ||
                Arrays.asList(RDBMSDataSourceConstants.CLASS_RETURN_TYPES).contains(returnType))) {
            return false;
        }
        return true;
    }

    /**
     * Generate the configuration bean by reading the xml file or string xml content.
     *
     * @param configuration This should be either a {@link File} or a {@code String}
     * @param clazz         class type of the generated bean
     * @param <T>           class type of the generated bean
     * @param <U>           {@link File} or a {@code String}
     * @return
     * @throws DataSourceException
     */
    public static <T, U> T loadJAXBConfiguration(U configuration, Class<T> clazz) throws DataSourceException {
        try {
            JAXBContext ctx = JAXBContext.newInstance(clazz);
            if (configuration instanceof File) {
                if (log.isDebugEnabled()) {
                    log.debug("Parsing configuration file: " + ((File) configuration).getName());
                }
                Document doc = DataSourceUtils.convertToDocument((File) configuration);
                return (T) ctx.createUnmarshaller().unmarshal(doc);
            } else if (configuration instanceof String) {
                String xmlConfiguration = DataSourceUtils.replaceSystemVariablesInXml((String) configuration);
                return (T) ctx.createUnmarshaller().unmarshal(new ByteArrayInputStream(xmlConfiguration.getBytes()));
            } else {
                throw new DataSourceException("Only a file or string content allowed as the first parameter.");
            }
        } catch (JAXBException e) {
            throw new DataSourceException("Error occurred while converting configuration into jaxb beans", e);
        }
    }
}
