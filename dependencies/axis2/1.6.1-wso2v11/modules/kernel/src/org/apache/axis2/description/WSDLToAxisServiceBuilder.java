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

package org.apache.axis2.description;

import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.namespace.Constants;
import org.apache.neethi.PolicyRegistry;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.resolver.URIResolver;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public abstract class WSDLToAxisServiceBuilder {

    protected static final String XMLSCHEMA_NAMESPACE_URI = Constants.URI_2001_SCHEMA_XSD;

    protected static final String XMLSCHEMA_NAMESPACE_PREFIX = "xs";

    protected static final String XML_SCHEMA_LOCAL_NAME = "schema";

    protected static final String XML_SCHEMA_SEQUENCE_LOCAL_NAME = "sequence";

    protected static final String XML_SCHEMA_COMPLEX_TYPE_LOCAL_NAME = "complexType";

    protected static final String XML_SCHEMA_ELEMENT_LOCAL_NAME = "element";

    protected static final String XML_SCHEMA_IMPORT_LOCAL_NAME = "import";

    protected static final String XSD_NAME = "name";

    protected static final String XSD_TARGETNAMESPACE = "targetNamespace";

    protected static final String XMLNS_AXIS2WRAPPED = "xmlns:axis2wrapped";

    protected static final String AXIS2WRAPPED = "axis2wrapped";

    protected static final String XSD_TYPE = "type";

    protected static final String XSD_REF = "ref";

    protected static int nsCount = 0;

    protected Map resolvedRpcWrappedElementMap = new HashMap();

    protected static final String XSD_ELEMENT_FORM_DEFAULT = "elementFormDefault";

    protected static final String XSD_UNQUALIFIED = "unqualified";

    protected InputStream in;

    protected AxisService axisService;

    protected PolicyRegistry registry;
    
    protected AxisConfiguration axisConfig;

    protected QName serviceName;
    protected boolean isServerSide = true;
    protected String style = null;
    private URIResolver customResolver;
    private String baseUri = null;
    protected static final String TYPES = "Types";

    // keeping whether builder is used in codegen or not
    protected boolean isCodegen;

    protected WSDLToAxisServiceBuilder() {

    }

    public WSDLToAxisServiceBuilder(InputStream in, QName serviceName) {
        this.in = in;
        this.serviceName = serviceName;
        this.axisService = new AxisService();
        setPolicyRegistryFromService(axisService);
    }

    public WSDLToAxisServiceBuilder(InputStream in, AxisService axisService) {
        this.in = in;
        this.axisService = axisService;
        setPolicyRegistryFromService(axisService);
    }

    /**
     * Sets a custom xmlschema URI resolver
     *
     * @param customResolver a URIResolver to use when working with schemas
     */
    public void setCustomResolver(URIResolver customResolver) {
        this.customResolver = customResolver;
    }

    public boolean isServerSide() {
        return isServerSide;
    }

    public void setServerSide(boolean serverSide) {
        isServerSide = serverSide;
    }

    protected void setPolicyRegistryFromService(AxisService axisService) {
        PolicyInclude policyInclude = axisService.getPolicyInclude();
        this.registry = policyInclude.getPolicyRegistry();
    }

    protected XmlSchema getXMLSchema(Element element, String baseUri) {
        XmlSchemaCollection schemaCollection = new XmlSchemaCollection();

        if (baseUri != null) {
            schemaCollection.setBaseUri(baseUri);
        }

        if (customResolver != null) {
            schemaCollection.setSchemaResolver(customResolver);
        }

        return schemaCollection.read(element);
    }

    /**
     * Find the XML schema prefix
     *
     * @return the active schema prefix, or the default schema prefix if not declared
     */
    protected String findSchemaPrefix() {
        String xsdPrefix = null;
        Map declaredNameSpaces = axisService.getNamespaceMap();
        if (declaredNameSpaces.containsValue(XMLSCHEMA_NAMESPACE_URI)) {
            //loop and find the prefix
            Iterator it = declaredNameSpaces.keySet().iterator();
            String key;
            while (it.hasNext()) {
                key = (String) it.next();
                if (XMLSCHEMA_NAMESPACE_URI.equals(declaredNameSpaces.get(key))) {
                    xsdPrefix = key;
                    break;
                }
            }
        } else {
            xsdPrefix = XMLSCHEMA_NAMESPACE_PREFIX; //default prefix
        }
        return xsdPrefix;
    }

    public abstract AxisService populateService() throws AxisFault;

    /**
     * Utility method that returns a DOM Builder
     *
     * @return a namespace-aware DOM DocumentBuilder
     */
    protected DocumentBuilder getDOMDocumentBuilder() {
        DocumentBuilder documentBuilder;
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                    .newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        return documentBuilder;
    }

    /**
     * Get a temporary namespace prefix.  NOT threadsafe.
     *
     * @return a new namespace prefix of the form "nsX"
     */
    protected String getTemporaryNamespacePrefix() {
        return "ns" + nsCount++;
    }

    /**
     * Gets the URI associated with the base document
     * for the WSDL definition.  Note that this URI
     * is for the base document, not the imports.
     * 
     * @return The URI as a String
     */
    public String getBaseUri() {
        return baseUri;
    }

    /**
     * Sets the URI associated with the base document
     * for the WSDL definition.  Note that this URI
     * is for the base document, not the imports.
     * 
     * @param baseUri  The URI as a String
     */
    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }

    public boolean isCodegen() {
        return isCodegen;
    }

    public void setCodegen(boolean codegen) {
        isCodegen = codegen;
    }

    public QName getServiceName() {
        return serviceName;
    }

    public void setServiceName(QName serviceName) {
        this.serviceName = serviceName;
    }
    
    /**
     * Get a string containing the stack of the current location
     *
     * @return String
     */
    protected static String stackToString() {
        return stackToString(new RuntimeException());
    }

    /**
     * Get a string containing the stack of the specified exception
     *
     * @param e
     * @return
     */
    protected static String stackToString(Throwable e) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.BufferedWriter bw = new java.io.BufferedWriter(sw);
        java.io.PrintWriter pw = new java.io.PrintWriter(bw);
        e.printStackTrace(pw);
        pw.close();
        String text = sw.getBuffer().toString();
        // Jump past the throwable
        text = text.substring(text.indexOf("at"));
        text = replace(text, "at ", "DEBUG_FRAME = ");
        return text;
    }
    
    /**
     * replace: Like String.replace except that the old new items are strings.
     *
     * @param name string
     * @param oldT old text to replace
     * @param newT new text to use
     * @return replacement string
     */
    protected static final String replace(String name,
                                       String oldT, String newT) {

        if (name == null) return "";

        // Create a string buffer that is twice initial length.
        // This is a good starting point.
        StringBuffer sb = new StringBuffer(name.length() * 2);

        int len = oldT.length();
        try {
            int start = 0;
            int i = name.indexOf(oldT, start);

            while (i >= 0) {
                sb.append(name.substring(start, i));
                sb.append(newT);
                start = i + len;
                i = name.indexOf(oldT, start);
            }
            if (start < name.length())
                sb.append(name.substring(start));
        } catch (NullPointerException e) {
        }

        return new String(sb);
    }
    
    public void useAxisConfiguration(AxisConfiguration axisConfig) {
        this.axisConfig = axisConfig;
    }
    
    protected Map createHttpLocationTable() {
       // Set a comparator so the httpLocations are stored in decending order
       Map httpLocationTable = new TreeMap(new Comparator(){
          public int compare(Object o1, Object o2) {
             return (-1 * ((Comparable)o1).compareTo(o2));
          }
       });
       return httpLocationTable;
    }
}
