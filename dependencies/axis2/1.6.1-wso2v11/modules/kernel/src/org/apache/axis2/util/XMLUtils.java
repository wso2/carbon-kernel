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

package org.apache.axis2.util;

import com.ibm.wsdl.Constants;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.util.Base64;
import org.w3c.dom.Attr;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Stack;


public class XMLUtils {
    public static final String charEncoding = "ISO-8859-1";
    private static final String saxParserFactoryProperty =
            "javax.xml.parsers.SAXParserFactory";

    private static DocumentBuilderFactory dbf = getDOMFactory();
    private static SAXParserFactory saxFactory;
    private static Stack saxParsers = new Stack();

    private static String empty = "";
    private static ByteArrayInputStream bais = new ByteArrayInputStream(empty.getBytes());

    static {
        // Initialize SAX Parser factory defaults
        initSAXFactory(null, true, false);
    }

    /**
     * Initializes the SAX parser factory.
     *
     * @param factoryClassName The (optional) class name of the desired
     *                         SAXParserFactory implementation. Will be
     *                         assigned to the system property
     *                         <b>javax.xml.parsers.SAXParserFactory</b>
     *                         unless this property is already set.
     *                         If <code>null</code>, leaves current setting
     *                         alone.
     * @param namespaceAware   true if we want a namespace-aware parser
     * @param validating       true if we want a validating parser
     */
    public static void initSAXFactory(String factoryClassName,
                                      boolean namespaceAware,
                                      boolean validating) {
        if (factoryClassName != null) {
            try {
                saxFactory = (SAXParserFactory) Loader.loadClass(factoryClassName).
                        newInstance();
                /*
                 * Set the system property only if it is not already set to
                 * avoid corrupting environments in which Axis is embedded.
                 */
                if (System.getProperty(saxParserFactoryProperty) == null) {
                    System.setProperty(saxParserFactoryProperty,
                                       factoryClassName);
                }
            } catch (Exception e) {
                saxFactory = SAXParserFactory.newInstance();
            }
        } else {
            saxFactory = SAXParserFactory.newInstance();
        }
        saxFactory.setNamespaceAware(namespaceAware);
        saxFactory.setValidating(validating);

        // Discard existing parsers
        saxParsers.clear();
    }

    private static DocumentBuilderFactory getDOMFactory() {
        DocumentBuilderFactory dbf;
        try {
            dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
        }
        catch (Exception e) {
            //log.error(Messages.getMessage("exception00"), e );
            dbf = null;
        }
        return (dbf);
    }

    private static boolean tryReset = true;

    /**
     * Returns a SAX parser for reuse.
     *
     * @param parser A SAX parser that is available for reuse
     */
    public static void releaseSAXParser(SAXParser parser) {
        if (!tryReset) {
            return;
        }

        //Free up possible ref. held by past contenthandler.
        try {
            XMLReader xmlReader = parser.getXMLReader();
            if (null != xmlReader) {
                synchronized (XMLUtils.class) {
                    saxParsers.push(parser);
                }
            } else {
                tryReset = false;
            }
        } catch (org.xml.sax.SAXException e) {
            tryReset = false;
        }
    }

    /**
     * Gets an empty new Document.
     *
     * @return Returns Document.
     * @throws ParserConfigurationException if construction problems occur
     */
    public static Document newDocument()
            throws ParserConfigurationException {
        synchronized (dbf) {
            return dbf.newDocumentBuilder().newDocument();
        }
    }

    /**
     * Gets a new Document read from the input source.
     *
     * @return Returns Document.
     * @throws ParserConfigurationException if construction problems occur
     * @throws SAXException                 if the document has xml sax problems
     * @throws IOException                  if i/o exceptions occur
     */
    public static Document newDocument(InputSource inp)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilder db;
        synchronized (dbf) {
            try {
                db = dbf.newDocumentBuilder();
            } catch (Exception e){
                // Under some peculiar conditions (classloader issues), just scrap the old dbf, create a new one and try again.
                dbf = getDOMFactory();
                db = dbf.newDocumentBuilder();
            }
        }
        db.setEntityResolver(new DefaultEntityResolver());
        db.setErrorHandler(new ParserErrorHandler());
        return (db.parse(inp));
    }

    /**
     * Gets a new Document read from the input stream
     *
     * @return Returns Document.
     * @throws ParserConfigurationException if construction problems occur
     * @throws SAXException                 if the document has xml sax problems
     * @throws IOException                  if i/o exceptions occur
     */
    public static Document newDocument(InputStream inp)
            throws ParserConfigurationException, SAXException, IOException {
        return XMLUtils.newDocument(new InputSource(inp));
    }

    /**
     * Gets a new Document read from the indicated uri
     *
     * @return Returns Document.
     * @throws ParserConfigurationException if construction problems occur
     * @throws SAXException                 if the document has xml sax problems
     * @throws IOException                  if i/o exceptions occur
     */
    public static Document newDocument(String uri)
            throws ParserConfigurationException, SAXException, IOException {
        // call the authenticated version as there might be 
        // username/password info embeded in the uri.
        return XMLUtils.newDocument(uri, null, null);
    }

    /**
     * Creates a new document from the given URI. Uses the username and password
     * if the URI requires authentication.
     *
     * @param uri      the resource to get
     * @param username basic auth username
     * @param password basic auth password
     * @throws ParserConfigurationException if construction problems occur
     * @throws SAXException                 if the document has xml sax problems
     * @throws IOException                  if i/o exceptions occur
     */
    public static Document newDocument(String uri, String username, String password)
            throws ParserConfigurationException, SAXException, IOException {
        InputSource ins = XMLUtils.getInputSourceFromURI(uri, username, password);
        Document doc = XMLUtils.newDocument(ins);
        // Close the Stream
        if (ins.getByteStream() != null) {
            ins.getByteStream().close();
        } else if (ins.getCharacterStream() != null) {
            ins.getCharacterStream().close();
        }
        return doc;
    }


    public static String getPrefix(String uri, Node e) {
        while (e != null && (e.getNodeType() == Element.ELEMENT_NODE)) {
            NamedNodeMap attrs = e.getAttributes();
            for (int n = 0; n < attrs.getLength(); n++) {
                Attr a = (Attr) attrs.item(n);
                String name;
                if ((name = a.getName()).startsWith("xmlns:") &&
                        a.getNodeValue().equals(uri)) {
                    return name.substring(6);
                }
            }
            e = e.getParentNode();
        }
        return null;
    }

    public static String getNamespace(String prefix, Node e) {
        while (e != null && (e.getNodeType() == Node.ELEMENT_NODE)) {
            Attr attr =
                    ((Element) e).getAttributeNodeNS(Constants.NS_URI_XMLNS, prefix);
            if (attr != null) {
                return attr.getValue();
            }
            e = e.getParentNode();
        }
        return null;
    }

    /**
     * Returns a QName when passed a string like "foo:bar" by mapping
     * the "foo" prefix to a namespace in the context of the given Node.
     *
     * @return Returns a QName generated from the given string representation.
     */
    public static QName getQNameFromString(String str, Node e) {
        if (str == null || e == null) {
            return null;
        }

        int idx = str.indexOf(':');
        if (idx > -1) {
            String prefix = str.substring(0, idx);
            String ns = getNamespace(prefix, e);
            if (ns == null) {
                return null;
            }
            return new QName(ns, str.substring(idx + 1));
        } else {
            return new QName("", str);
        }
    }

    /**
     * Returns a string for a particular QName, mapping a new prefix
     * if necessary.
     */
    public static String getStringForQName(QName qname, Element e) {
        String uri = qname.getNamespaceURI();
        String prefix = getPrefix(uri, e);
        if (prefix == null) {
            int i = 1;
            prefix = "ns" + i;
            while (getNamespace(prefix, e) != null) {
                i++;
                prefix = "ns" + i;
            }
            e.setAttributeNS(Constants.NS_URI_XMLNS,
                             "xmlns:" + prefix, uri);
        }
        return prefix + ":" + qname.getLocalPart();
    }

    /**
     * Concatinates all the text and cdata node children of this elem and returns
     * the resulting text.
     * (by Matt Duftler)
     *
     * @param parentEl the element whose cdata/text node values are to
     *                 be combined.
     * @return Returns the concatinated string.
     */
    public static String getChildCharacterData(Element parentEl) {
        if (parentEl == null) {
            return null;
        }
        Node tempNode = parentEl.getFirstChild();
        StringBuffer strBuf = new StringBuffer();
        CharacterData charData;

        while (tempNode != null) {
            switch (tempNode.getNodeType()) {
                case Node.TEXT_NODE :
                case Node.CDATA_SECTION_NODE:
                    charData = (CharacterData) tempNode;
                    strBuf.append(charData.getData());
                    break;
            }
            tempNode = tempNode.getNextSibling();
        }
        return strBuf.toString();
    }

    public static class ParserErrorHandler implements ErrorHandler {
        /**
         * Returns a string describing parse exception details
         */
        private String getParseExceptionInfo(SAXParseException spe) {
            String systemId = spe.getSystemId();
            if (systemId == null) {
                systemId = "null";
            }
            return "URI=" + systemId +
                    " Line=" + spe.getLineNumber() +
                    ": " + spe.getMessage();
        }

        // The following methods are standard SAX ErrorHandler methods.
        // See SAX documentation for more info.

        public void warning(SAXParseException spe) throws SAXException {
        }

        public void error(SAXParseException spe) throws SAXException {
            String message = "Error: " + getParseExceptionInfo(spe);
            throw new SAXException(message);
        }

        public void fatalError(SAXParseException spe) throws SAXException {
            String message = "Fatal Error: " + getParseExceptionInfo(spe);
            throw new SAXException(message);
        }
    }


    /**
     * Utility to get the bytes uri.
     * Does NOT handle authenticated URLs,
     * use getInputSourceFromURI(uri, username, password)
     *
     * @param uri the resource to get
     */
    public static InputSource getInputSourceFromURI(String uri) {
        return new InputSource(uri);
    }


    /**
     * Utility to get the bytes at a protected uri
     * <p/>
     * Retrieves the URL if a username and password are provided.
     * The java.net.URL class does not do Basic Authentication, so we have to
     * do it manually in this routine.
     * <p/>
     * If no username is provided, creates an InputSource from the uri
     * and lets the InputSource go fetch the contents.
     *
     * @param uri      the resource to get
     * @param username basic auth username
     * @param password basic auth password
     */
    private static InputSource getInputSourceFromURI(String uri,
                                                     String username,
                                                     String password)
            throws IOException, ProtocolException, UnsupportedEncodingException {
        URL wsdlurl = null;
        try {
            wsdlurl = new URL(uri);
        } catch (MalformedURLException e) {
            // we can't process it, it might be a 'simple' foo.wsdl
            // let InputSource deal with it
            return new InputSource(uri);
        }

        // if no authentication, just let InputSource deal with it
        if (username == null && wsdlurl.getUserInfo() == null) {
            return new InputSource(uri);
        }

        // if this is not an HTTP{S} url, let InputSource deal with it
        if (!wsdlurl.getProtocol().startsWith("http")) {
            return new InputSource(uri);
        }

        URLConnection connection = wsdlurl.openConnection();
        // Does this work for https???
        if (!(connection instanceof HttpURLConnection)) {
            // can't do http with this URL, let InputSource deal with it
            return new InputSource(uri);
        }
        HttpURLConnection uconn = (HttpURLConnection) connection;
        String userinfo = wsdlurl.getUserInfo();
        uconn.setRequestMethod("GET");
        uconn.setAllowUserInteraction(false);
        uconn.setDefaultUseCaches(false);
        uconn.setDoInput(true);
        uconn.setDoOutput(false);
        uconn.setInstanceFollowRedirects(true);
        uconn.setUseCaches(false);

        // username/password info in the URL overrides passed in values 
        String auth = null;
        if (userinfo != null) {
            auth = userinfo;
        } else if (username != null) {
            auth = (password == null) ? username : username + ":" + password;
        }

        if (auth != null) {
            uconn.setRequestProperty("Authorization",
                                     "Basic " +
                                             base64encode(auth.getBytes(charEncoding)));
        }

        uconn.connect();

        return new InputSource(uconn.getInputStream());
    }

    public static String base64encode(byte[] bytes) {
        return Base64.encode(bytes);
    }

    public static InputSource getEmptyInputSource() {
        return new InputSource(bais);
    }

    /**
     * Finds a Node with a given QNameb.
     *
     * @param node parent node
     * @param name QName of the child we need to find
     * @return Returns child node.
     */
    public static Node findNode(Node node, QName name) {
        if (name.getNamespaceURI().equals(node.getNamespaceURI()) &&
                name.getLocalPart().equals(node.getLocalName())) {
            return node;
        }
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node ret = findNode(children.item(i), name);
            if (ret != null) {
                return ret;
            }
        }
        return null;
    }

    /**
     * Convert DOM Element into a fully built OMElement
     * @param element dom Element
     * @return OMElement
     * @throws Exception
     */
    public static OMElement toOM(Element element) throws Exception {
        return toOM(element, true);
    }
    
    /**
     * Convert DOM Element into a fully built OMElement
     * @param element
     * @param buildAll if true, full OM tree is immediately built. if false, caller is responsible 
     * for building the tree and closing the parser.
     * @return
     * @throws Exception
     */
    public static OMElement toOM(Element element, boolean buildAll) throws Exception {

        Source source = new DOMSource(element);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Result result = new StreamResult(baos);

        Transformer xformer = TransformerFactory.newInstance().newTransformer();
        xformer.transform(source, result);

        ByteArrayInputStream is = new ByteArrayInputStream(baos.toByteArray());

        OMXMLParserWrapper builder = OMXMLBuilderFactory.createOMBuilder(is);
        builder.setCache(true);

        OMElement omElement = builder.getDocumentElement();
        if (buildAll) {
            omElement.build();
            builder.close();
        }
        return omElement;
    }


    /**
     * Converts a given OMElement to a DOM Element.
     *
     * @param element
     * @return Returns Element.
     * @throws Exception
     */
    public static Element toDOM(OMElement element) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        element.serialize(baos);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory.newDocumentBuilder().parse(bais).getDocumentElement();
    }


    /**
     * Converts a given inputstream to an OMNode
     * The reurned OMNode is fully built.
     *
     * @param inputStream
     * @return OMNode
     * @throws javax.xml.stream.XMLStreamException
     *
     */
    public static OMNode toOM(InputStream inputStream) throws XMLStreamException {
        return toOM(inputStream, true);
    }
    
    /**
     * Converts a given inputstream to an OMNode
     * The reurned OMNode is fully built if buildAll is true.
     * If buildAll is false, the caller is responsible for closing the parser.
     *
     * @param inputStream
     * @param buildAll
     * @return OMNode
     * @throws javax.xml.stream.XMLStreamException
     *
     */
    public static OMNode toOM(InputStream inputStream, boolean buildAll) throws XMLStreamException {
        OMXMLParserWrapper builder = OMXMLBuilderFactory.createOMBuilder(inputStream);
        builder.setCache(true);
        OMNode omNode = builder.getDocumentElement();
        
        if (buildAll) {
            omNode.build();
            builder.close();
        }
        
        return omNode;
    }

    /**
     * Converts a given Reader to an OMNode.
     * The reurned OMNode is fully built.
     *
     * @param reader
     * @return
     * @throws XMLStreamException
     */
    public static OMNode toOM(Reader reader) throws XMLStreamException {
        return toOM(reader, true);
    }
    
    /**
     * Converts a given Reader to an OMNode.
     * The reurned OMNode is fully built if buildAll is true.
     * If buildAll is false, the caller is responsible for closing the parser.
     *
     * @param reader
     * @param buildAll
     * @return OMNode
     * @throws XMLStreamException
     */
    public static OMNode toOM(Reader reader, boolean buildAll) throws XMLStreamException {
        OMXMLParserWrapper builder = OMXMLBuilderFactory.createOMBuilder(reader);
        builder.setCache(true);
        OMNode omNode = builder.getDocumentElement();
        
        if (buildAll) {
            omNode.build();
            builder.close();
        }
        
        return omNode;
    }
}
