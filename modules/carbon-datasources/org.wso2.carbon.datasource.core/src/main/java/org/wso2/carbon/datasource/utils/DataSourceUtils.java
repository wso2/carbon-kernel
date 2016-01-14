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
import org.wso2.carbon.datasource.common.DataSourceConstants;
import org.wso2.carbon.datasource.common.DataSourceException;
import org.wso2.carbon.kernel.utils.Utils;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import javax.xml.XMLConstants;
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

    private static ThreadLocal<String> dataSourceId = new ThreadLocal<String>() {
        protected synchronized String initialValue() {
            return null;
        }
    };

    public static void setCurrentDataSourceId(String dsId) {
        dataSourceId.set(dsId);
    }

    public static String getCurrentDataSourceId() {
        return dataSourceId.get();
    }

    public static boolean nullAllowEquals(Object lhs, Object rhs) {
        if (lhs == null && rhs == null) {
            return true;
        }
        if ((lhs == null && rhs != null) || (lhs != null && rhs == null)) {
            return false;
        }
        return lhs.equals(rhs);
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
            log.error("Error while convering element to string: " + e.getMessage(), e);
            return null;
        }
    }

    public static Document convertToDocument(File file) throws DataSourceException {
        try {
            return getSecuredDocumentBuilder(false).parse(file);
        } catch (Exception e) {
            throw new DataSourceException("Error in creating an XML document from file: " +
                    e.getMessage(), e);
        }
    }

    public static InputStream elementToInputStream(Element element) {
        try {
            if (element == null) {
                return null;
            }
            String xmlString = elementToString(element);
            InputStream stream = new ByteArrayInputStream(xmlString.getBytes());
            return stream;
        } catch (Exception e) {
            log.error("Error while convering element to InputStream: " + e.getMessage(), e);
            return null;
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
        documentBuilder.setEntityResolver(new EntityResolver() {
            public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                throw new SAXException("Possible XML External Entity (XXE) attack. Skip resolving entity");
            }
        });
        return documentBuilder;
    }

    public static Path getDataSourceConfigPath() {
        return Utils.getCarbonConfigHome().resolve(DataSourceConstants.DATASOURCES_DIRECTORY_NAME);
    }

    public static Path getMasterDataSource() {
        return getDataSourceConfigPath().resolve(DataSourceConstants.MASTER_DS_FILE_NAME);
    }

    /**
     * @param xmlConfiguration InputStream that carries xml configuration
     * @return returns a InputStream that has evaluated system variables in input
     * @throws Exception
     */
    public static InputStream replaceSystemVariablesInXml(InputStream xmlConfiguration) throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder;
        Document doc;
        try {
            documentBuilderFactory.setNamespaceAware(true);
            documentBuilderFactory.setExpandEntityReferences(false);
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            SecurityManager securityManager = new SecurityManager();
//            securityManager.setEntityExpansionLimit(ENTITY_EXPANSION_LIMIT);
//            documentBuilderFactory.setAttribute(SECURITY_MANAGER_PROPERTY, securityManager);
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            documentBuilder.setEntityResolver(new EntityResolver() {
                @Override
                public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                    throw new SAXException("Possible XML External Entity (XXE) attack. Skip resolving entity");
                }
            });
            doc = documentBuilder.parse(xmlConfiguration);
        } catch (Exception e) {
            throw new Exception("Error in building Document", e);
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
     * @throws Exception
     */
    public static InputStream toInputStream(Document doc) throws Exception {
        InputStream in;
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Source xmlSource = new DOMSource(doc);
            Result result = new StreamResult(outputStream);
            TransformerFactory.newInstance().newTransformer().transform(xmlSource, result);
            in = new ByteArrayInputStream(outputStream.toByteArray());
        } catch (TransformerException e) {
            throw new Exception("Error in transforming DOM to InputStream", e);
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

    public static String replaceSystemVariablesInXml(String xmlConfiguration) throws Exception {
        InputStream in = replaceSystemVariablesInXml(new ByteArrayInputStream(xmlConfiguration.getBytes()));
        try {
            xmlConfiguration = IOUtils.toString(in);
        } catch (IOException e) {
            throw new Exception("Error in converting InputStream to String");
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
}
