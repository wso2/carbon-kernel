/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/* CVS Header
   $Id$
   $Log$
*/

package org.apache.axis2.xmlbeans;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class XSDConfig {
    private static final String XMLBEANS_NS = "http://xml.apache.org/xmlbeans/2004/02/xbean/config";
    private static final String XMLBEANS_QNAME_NODE = "qname";
    private static final String XMLBEANS_NS_NODE = "namespace";

    /** The parsed xsdconfig file */
    private Document xsdConfigDoc = null;
    /** The list of prefixes on the document root */
    private HashMap prefixesToURIMappings = null;
    /** The list of schema tyes to Java class names */
    private HashMap qnamesToJavaNamesMappings = null;
    /** The list of namespaces to Java package names */
    private HashMap nsToJavaPackagesMap = null;
    /** Indicates whether we have any QName to Java class name mappings */
    public boolean hasQNameToJavaNameMappings = false;
    /** Indicates whether we have any namespace to Java package mappings */
    public boolean hasNamespaceToJavaPackageMappings = false;

    public XSDConfig(String xsdConfigFile) {
        try {
            DocumentBuilder builder = null;
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            factory.setNamespaceAware(true);
            factory.setValidating(false);

            builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new ParseErrorHandler());

            xsdConfigDoc = builder.parse(new File(xsdConfigFile));

            // Create a mapping for all the namespaces in the document
            prefixesToURIMappings = new HashMap();
            NamedNodeMap attributes = xsdConfigDoc.getDocumentElement().getAttributes();
            for (int c = 0; c < attributes.getLength(); c++) {
                /* Do we have a namespace declaration?
                * xmlns:mv="urn:weegietech:minerva"
                */
                if (attributes.item(c).getNodeName().indexOf("xmlns:") != -1) {
                    String[] parts = attributes.item(c).getNodeName().split(":");

                    // Add the prefix to uri mapping to our list
                    prefixesToURIMappings.put(parts[1], attributes.item(c).getNodeValue());
                }
            }

            // Load up the list of QName to Java class name mappings
            qnamesToJavaNamesMappings = getQNamesToJavaNames();
            if (qnamesToJavaNamesMappings.size() > 0)
                hasQNameToJavaNameMappings = true;

            // Load up the list of namespaces to Java packages mappings
            nsToJavaPackagesMap = getNamespacesToPackages();
            if (nsToJavaPackagesMap.size() > 0)
                hasNamespaceToJavaPackageMappings = true;
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        } catch (SAXException se) {
            throw new RuntimeException(se);
        } catch (IllegalArgumentException iae) {
            throw new RuntimeException(iae);
        } catch (ParserConfigurationException pce) {
            throw new RuntimeException(pce);
        }
    }

    /**
     * Returns the pre loaded schema types to Java class names mappings.
     *
     * @return HashMap of schema types to Java class names mappings as as specified in the xsdconfig
     *         file.
     */
    public HashMap getSchemaTypesToJavaNames() {
        return qnamesToJavaNamesMappings;
    }

    /**
     * Returns the pre loaded namespace to Java package mappings.
     *
     * @return HashMap of namespace to Java package mappings as as specified in the xsdconfig file.
     */
    public HashMap getNamespacesToJavaPackages() {
        return nsToJavaPackagesMap;
    }

    /**
     * Loads the schema types to Java class name mappings
     *
     * @return HashMap containing the schema types to Java class name mappings as specified in the
     *         xsdconfig file. If there are no mappings, the returned HashMap will be empty.
     */
    private HashMap getQNamesToJavaNames() {
        HashMap qnamesToJavaNamesMap = new HashMap();

        /* Look for all the <xb:qname ... /> nodes as these specify
        * xml schema types to Java class mappings.
        * <xb:qname name="mv:moduleType" javaname="Module"/>
        */
        NodeList qnameNodes = xsdConfigDoc.getElementsByTagNameNS(XMLBEANS_NS, XMLBEANS_QNAME_NODE);

        for (int c = 0; c < qnameNodes.getLength(); c++) {
            Node qnameNode = qnameNodes.item(c);

            /* In the xsdconfig file we'll get schema types with a prefix and not a uri.
            * <xb:qname name="mv:moduleType" javaname="Module"/>
            * but XMLBeans will call BindingConfig::lookupJavanameForQName with a QName
            * which has a namespace uri and no prefix.
            * So we'll store the fully qualifed schema type name in the mapping list.
            * i.e. we pick it up from the xsdconfig file as:
            * mv:moduleType
            * but we'll store it as urn:weegietech:minerva:moduleType
            */
            String schemaType = qnameNode.getAttributes().getNamedItem("name").getNodeValue();
            if (schemaType.indexOf(":") != -1) {
                // mv:moduleType
                String prefix = schemaType.split(":")[0];
                String localName = schemaType.split(":")[1];

                if (prefixesToURIMappings.containsKey(prefix)) {
                    // Store as urn:weegietech:minerva:moduleType
                    String key = (String)prefixesToURIMappings.get(prefix) + ":" + localName;

                    // Direct mapping now from schema types to Java class names
                    qnamesToJavaNamesMap.put(key, qnameNode.getAttributes()
                            .getNamedItem("javaname").getNodeValue());
                }
            }
        }

        return qnamesToJavaNamesMap;
    }

    /**
     * Loads the namespace to Java package mappings
     *
     * @return HashMap containing the namespace to Java package mappings as specified in the
     *         xsdconfig file. If there are no mappings, the returned HashMap will be empty.
     */
    private HashMap getNamespacesToPackages() {
        HashMap nsToJavaPackagesMap = new HashMap();

        /* Look for all the <xb:namespace ... /> nodes as these specify
        * xml namespace to Java package mappings.
        * <xb:qname name="mv:moduleType" javaname="Module"/>
        */
        NodeList nsNodes = xsdConfigDoc.getElementsByTagNameNS(XMLBEANS_NS, XMLBEANS_NS_NODE);

        for (int nsNodesCount = 0; nsNodesCount < nsNodes.getLength(); nsNodesCount++) {
            Node nsNode = nsNodes.item(nsNodesCount);

            // What's the current namespace?
            String uri = nsNode.getAttributes().getNamedItem("uri").getNodeValue();

            // Get the package name for the current namespace uri
            String packageName = null;
            NodeList childNodes = nsNode.getChildNodes();
            for (int childNodesCount = 0; childNodesCount < childNodes.getLength();
                 childNodesCount++) {
                Node childNode = childNodes.item(childNodesCount);
                if (childNode.getLocalName() != null) {
                    if (childNode.getLocalName().equals("package")) {
                        packageName = childNode.getFirstChild().getNodeValue();
                    }
                }
            }

            // Store the namespace uri to Java package mapping
            if (packageName != null) {
                nsToJavaPackagesMap.put(uri, packageName);
            }
        }

        return nsToJavaPackagesMap;
    }

    class ParseErrorHandler implements ErrorHandler {
        public void error(SAXParseException exception) throws SAXException {
            throw new SAXException(exception);
        }

        public void fatalError(SAXParseException exception) throws SAXException {
            throw new SAXException(exception);
        }

        public void warning(SAXParseException exception) throws SAXException {
            throw new SAXException(exception);
        }
    }
}
