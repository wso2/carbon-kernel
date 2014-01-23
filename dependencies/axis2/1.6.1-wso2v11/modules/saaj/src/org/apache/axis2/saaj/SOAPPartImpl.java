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

package org.apache.axis2.saaj;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.om.impl.MTOMConstants;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.impl.builder.MTOMStAXSOAPModelBuilder;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axiom.soap.impl.dom.soap11.SOAP11Factory;
import org.apache.axiom.soap.impl.dom.soap12.SOAP12Factory;
import org.apache.axis2.saaj.util.IDGenerator;
import org.apache.axis2.saaj.util.SAAJUtil;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.w3c.dom.UserDataHandler;

import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Iterator;

public class SOAPPartImpl extends SOAPPart {

    private static final Log log = LogFactory.getLog(SOAPPartImpl.class);

    private Document document;
    private SOAPMessage soapMessage;
    private SOAPEnvelopeImpl envelope;
    private final MimeHeaders mimeHeaders;

    public SOAPPartImpl(SOAPMessageImpl parentSoapMsg,
                        SOAPEnvelopeImpl soapEnvelope) {
        //setMimeHeader(HTTPConstants.HEADER_CONTENT_ID, IDGenerator.generateID());
        //setMimeHeader(HTTPConstants.HEADER_CONTENT_TYPE, "text/xml");
        this.mimeHeaders = parentSoapMsg.getMimeHeaders();
        soapMessage = parentSoapMsg;
        envelope = soapEnvelope;
        document = soapEnvelope.getOwnerDocument();
        envelope.setSOAPPartParent(this);
    }

    /**
     * Construct a SOAP part from the given input stream.
     * The content type (as provided by the MIME headers) must be SOAP 1.1, SOAP 1.2
     * or XOP (MTOM). MIME packages (multipart/related) are not supported and should be
     * parsed using {@link SOAPMessageImpl#SOAPMessageImpl(InputStream, MimeHeaders).
     * <p>
     * If the content type is XOP, xop:Include elements will only be replaced if
     * the <code>attachments</code> parameter is not null.
     *
     * @see MessageFactoryImpl#setProcessMTOM(boolean)
     * 
     * @param parentSoapMsg the parent SOAP message
     * @param inputStream the input stream with the content of the SOAP part
     * @param mimeHeaders the MIME headers
     * @param attachments the set of attachments to be used to substitute xop:Include elements
     * @throws SOAPException
     */
    public SOAPPartImpl(SOAPMessageImpl parentSoapMsg, InputStream inputStream,
                        MimeHeaders mimeHeaders, Attachments attachments) throws SOAPException {
        ContentType contentType = null;
        if (mimeHeaders == null) {
            //TODO : read string from constants
            this.mimeHeaders = new MimeHeaders();
            this.mimeHeaders.addHeader("Content-ID", IDGenerator.generateID());
            this.mimeHeaders.addHeader("content-type", HTTPConstants.MEDIA_TYPE_APPLICATION_SOAP_XML);
        } else {
            String contentTypes[] = mimeHeaders.getHeader(HTTPConstants.CONTENT_TYPE);
            if (contentTypes != null && contentTypes.length > 0) {
                try {
                    contentType = new ContentType(contentTypes[0]);
                } catch (ParseException ex) {
                    throw new SOAPException("Invalid content type '" + contentTypes[0] + "'");
                }
            }
            this.mimeHeaders = SAAJUtil.copyMimeHeaders(mimeHeaders);
        }

        soapMessage = parentSoapMsg;

        String charset;
        boolean isMTOM;
        String soapEnvelopeNamespaceURI;
        SOAPFactory soapFactory;
        if (contentType == null) {
            charset = null;
            isMTOM = false;
            soapFactory = new SOAP11Factory();
            soapEnvelopeNamespaceURI = null;
        } else {
            String baseType = contentType.getBaseType().toLowerCase();
            String soapContentType;
            if (baseType.equals(MTOMConstants.MTOM_TYPE)) {
                isMTOM = true;
                String typeParam = contentType.getParameter("type");
                if (typeParam == null) {
                    throw new SOAPException("Missing 'type' parameter in XOP content type");
                } else {
                    soapContentType = typeParam.toLowerCase();
                }
            } else {
                isMTOM = false;
                soapContentType = baseType;
            }
            
            if (soapContentType.equals(HTTPConstants.MEDIA_TYPE_TEXT_XML)) {
                soapEnvelopeNamespaceURI = SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;
                soapFactory = new SOAP11Factory();
            } else if (soapContentType.equals(HTTPConstants.MEDIA_TYPE_APPLICATION_SOAP_XML)) {
                soapEnvelopeNamespaceURI = SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI;
                soapFactory = new SOAP12Factory();
            } else {
                throw new SOAPException("Unrecognized content type '" + soapContentType + "'");
            }
            
            charset = contentType.getParameter("charset");
        }
        
        XMLStreamReader streamReader;
        try {
            if (charset != null) {
            	streamReader = StAXUtils.createXMLStreamReader(inputStream, charset);
            } else {
            	streamReader = StAXUtils.createXMLStreamReader(inputStream);                	
            }
        } catch (XMLStreamException e) {
            throw new SOAPException(e);
        }

        StAXSOAPModelBuilder builder;
        if (isMTOM && attachments != null) {
            builder = new MTOMStAXSOAPModelBuilder(streamReader,
                                                   soapFactory,
                                                   attachments,
                                                   soapEnvelopeNamespaceURI);
        } else {
            builder = new StAXSOAPModelBuilder(streamReader,
                                               soapFactory,
                                               soapEnvelopeNamespaceURI);
        }
        
        try {
            org.apache.axiom.soap.SOAPEnvelope soapEnvelope = builder.getSOAPEnvelope();
            envelope = new SOAPEnvelopeImpl(
                    (org.apache.axiom.soap.impl.dom.SOAPEnvelopeImpl)soapEnvelope);
            envelope.element.build();
            this.document = envelope.getOwnerDocument();
            envelope.setSOAPPartParent(this);
        } catch (Exception e) {
            throw new SOAPException(e);
        }
    }

    /**
     * Obtain the SOAPMessage
     *
     * @return the related SOAPMessage
     */
    public SOAPMessage getSoapMessage() {
        return soapMessage;
    }

    /**
     * Gets the <CODE>SOAPEnvelope</CODE> object associated with this <CODE>SOAPPart</CODE> object.
     * Once the SOAP envelope is obtained, it can be used to get its contents.
     *
     * @return the <CODE>SOAPEnvelope</CODE> object for this <CODE> SOAPPart</CODE> object
     * @throws SOAPException if there is a SOAP error
     */
    public SOAPEnvelope getEnvelope() throws SOAPException {
        return envelope;
    }

    /**
     * Removes all MIME headers that match the given name.
     *
     * @param header a <CODE>String</CODE> giving the name of the MIME header(s) to be removed
     */
    public void removeMimeHeader(String header) {
        mimeHeaders.removeHeader(header);
    }

    /** Removes all the <CODE>MimeHeader</CODE> objects for this <CODE>SOAPEnvelope</CODE> object. */
    public void removeAllMimeHeaders() {
        mimeHeaders.removeAllHeaders();
    }

    /**
     * Gets all the values of the <CODE>MimeHeader</CODE> object in this <CODE>SOAPPart</CODE>
     * object that is identified by the given <CODE>String</CODE>.
     *
     * @param name the name of the header; example: "Content-Type"
     * @return a <CODE>String</CODE> array giving all the values for the specified header
     * @see #setMimeHeader(String, String) setMimeHeader(java.lang.String,
     *      java.lang.String)
     */
    public String[] getMimeHeader(String name) {
        return mimeHeaders.getHeader(name);
    }

    /**
     * Changes the first header entry that matches the given header name so that its value is the
     * given value, adding a new header with the given name and value if no existing header is a
     * match. If there is a match, this method clears all existing values for the first header that
     * matches and sets the given value instead. If more than one header has the given name, this
     * method removes all of the matching headers after the first one.
     * <p/>
     * <P>Note that RFC822 headers can contain only US-ASCII characters.</P>
     *
     * @param name  a <CODE>String</CODE> giving the header name for which to search
     * @param value a <CODE>String</CODE> giving the value to be set. This value will be substituted
     *              for the current value(s) of the first header that is a match if there is one. If
     *              there is no match, this value will be the value for a new
     *              <CODE>MimeHeader</CODE> object.
     * @throws IllegalArgumentException
     *          if there was a problem with the specified mime header name or value
     * @throws IllegalArgumentException
     *          if there was a problem with the specified mime header name or value
     * @see #getMimeHeader(String) getMimeHeader(java.lang.String)
     */
    public void setMimeHeader(String name, String value) {
        mimeHeaders.setHeader(name, value);
    }

    /**
     * Creates a <CODE>MimeHeader</CODE> object with the specified name and value and adds it to
     * this <CODE>SOAPPart</CODE> object. If a <CODE>MimeHeader</CODE> with the specified name
     * already exists, this method adds the specified value to the already existing value(s).
     * <p/>
     * <P>Note that RFC822 headers can contain only US-ASCII characters.</P>
     *
     * @param header a <CODE>String</CODE> giving the header name
     * @param value  a <CODE>String</CODE> giving the value to be set or added
     * @throws IllegalArgumentException if there was a problem with the specified mime header name
     *                                  or value
     */
    public void addMimeHeader(String header, String value) {
        mimeHeaders.addHeader(header, value);
    }

    /**
     * Retrieves all the headers for this <CODE>SOAPPart</CODE> object as an iterator over the
     * <CODE>MimeHeader</CODE> objects.
     *
     * @return an <CODE>Iterator</CODE> object with all of the Mime headers for this
     *         <CODE>SOAPPart</CODE> object
     */
    public Iterator getAllMimeHeaders() {
        return mimeHeaders.getAllHeaders();
    }

    /**
     * Retrieves all <CODE>MimeHeader</CODE> objects that match a name in the given array.
     *
     * @param names a <CODE>String</CODE> array with the name(s) of the MIME headers to be returned
     * @return all of the MIME headers that match one of the names in the given array, returned as
     *         an <CODE>Iterator</CODE> object
     */
    public Iterator getMatchingMimeHeaders(String[] names) {
        return mimeHeaders.getMatchingHeaders(names);
    }

    /**
     * Retrieves all <CODE>MimeHeader</CODE> objects whose name does not match a name in the given
     * array.
     *
     * @param names a <CODE>String</CODE> array with the name(s) of the MIME headers not to be
     *              returned
     * @return all of the MIME headers in this <CODE>SOAPPart</CODE> object except those that match
     *         one of the names in the given array. The nonmatching MIME headers are returned as an
     *         <CODE>Iterator</CODE> object.
     */
    public Iterator getNonMatchingMimeHeaders(String[] names) {
        return mimeHeaders.getNonMatchingHeaders(names);
    }

    public void setContent(Source source) throws SOAPException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLStreamReader reader;

            if (source instanceof StreamSource) {
                reader = inputFactory.createXMLStreamReader(source);
            } else {
                Result result = new StreamResult(baos);
                Transformer xformer = TransformerFactory.newInstance().newTransformer();
                xformer.transform(source, result);
                InputStream is = new ByteArrayInputStream(baos.toByteArray());
                reader = inputFactory.createXMLStreamReader(is);
            }

            StAXSOAPModelBuilder builder1 = null;
            if (this.envelope.element.getOMFactory() instanceof SOAP11Factory) {
                builder1 = new StAXSOAPModelBuilder(reader,
                                                    (SOAP11Factory)this.envelope.element
                                                            .getOMFactory(), null);
            } else if (this.envelope.element.getOMFactory() instanceof SOAP12Factory) {
                builder1 = new StAXSOAPModelBuilder(reader,
                                                    (SOAP12Factory)this.envelope.element
                                                            .getOMFactory(), null);
            }

            org.apache.axiom.soap.SOAPEnvelope soapEnvelope = builder1.getSOAPEnvelope();
            envelope = new SOAPEnvelopeImpl(
                    (org.apache.axiom.soap.impl.dom.SOAPEnvelopeImpl)soapEnvelope);
            envelope.element.build();
            this.document = envelope.getOwnerDocument();
            envelope.setSOAPPartParent(this);
        } catch (TransformerFactoryConfigurationError e) {
            log.error(e);
            throw new SOAPException(e);
        } catch (Exception e) {
            log.error(e);
            throw new SOAPException(e);
        }
    }

    /**
     * Returns the content of the SOAPEnvelope as a JAXP <CODE> Source</CODE> object.
     *
     * @return the content as a <CODE> javax.xml.transform.Source</CODE> object
     * @throws SOAPException if the implementation cannot convert the specified <CODE>Source</CODE>
     *                       object
     * @see #setContent(javax.xml.transform.Source) setContent(javax.xml.transform.Source)
     */
    public Source getContent() throws SOAPException {
        return new DOMSource(this.document);
    }

    /**
     * The Document Type Declaration (see <code>DocumentType</code>) associated with this document.
     * For HTML documents as well as XML documents without a document type declaration this returns
     * <code>null</code>. The DOM Level 2 does not support editing the Document Type Declaration.
     * <code>docType</code> cannot be altered in any way, including through the use of methods
     * inherited from the <code>Node</code> interface, such as <code>insertNode</code> or
     * <code>removeNode</code>.
     */
    public DocumentType getDoctype() {
        return document.getDoctype();
    }

    /**
     * The <code>DOMImplementation</code> object that handles this document. A DOM application may
     * use objects from multiple implementations.
     */
    public DOMImplementation getImplementation() {
        return document.getImplementation();
    }

    /**
     * This is a convenience attribute that allows direct access to the child node that is the root
     * element of the document. For HTML documents, this is the element with the tagName "HTML".
     */
    public Element getDocumentElement() {
        return document.getDocumentElement();
    }

    /**
     * Creates an element of the type specified. Note that the instance returned implements the
     * <code>Element</code> interface, so attributes can be specified directly on the returned
     * object. <br>In addition, if there are known attributes with default values, <code>Attr</code>
     * nodes representing them are automatically created and attached to the element. <br>To create
     * an element with a qualified name and namespace URI, use the <code>createElementNS</code>
     * method.
     *
     * @param tagName The name of the element type to instantiate. For XML, this is case-sensitive.
     *                For HTML, the <code>tagName</code> parameter may be provided in any case, but
     *                it must be mapped to the canonical uppercase form by the DOM implementation.
     * @return A new <code>Element</code> object with the <code>nodeName</code> attribute set to
     *         <code>tagName</code>, and <code>localName</code>, <code>prefix</code>, and
     *         <code>namespaceURI</code> set to <code>null</code>.
     * @throws DOMException INVALID_CHARACTER_ERR: Raised if the specified name contains an illegal
     *                      character.
     */
    public Element createElement(String tagName) throws DOMException {
        return document.createElement(tagName);
    }

    /**
     * Creates an empty <code>DocumentFragment</code> object.
     *
     * @return A new <code>DocumentFragment</code>.
     */
    public DocumentFragment createDocumentFragment() {
        return document.createDocumentFragment();
    }

    /**
     * Creates a <code>Text</code> node given the specified string.
     *
     * @param data The data for the node.
     * @return The new <code>Text</code> object.
     */
    public Text createTextNode(String data) {
        return document.createTextNode(data);
    }

    /**
     * Creates a <code>CDATASection</code> node whose value is the specified string.
     *
     * @param data The data for the <code>CDATASection</code> contents.
     * @return The new <code>CDATASection</code> object.
     * @throws DOMException NOT_SUPPORTED_ERR: Raised if this document is an HTML document.
     */
    public Comment createComment(String data) {
        return document.createComment(data);
    }

    /**
     * Creates a <code>CDATASection</code> node whose value is the specified string.
     *
     * @param data The data for the <code>CDATASection</code> contents.
     * @return The new <code>CDATASection</code> object.
     * @throws DOMException NOT_SUPPORTED_ERR: Raised if this document is an HTML document.
     */
    public CDATASection createCDATASection(String data) throws DOMException {
        return document.createCDATASection(data);
    }

    /**
     * Creates a <code>ProcessingInstruction</code> node given the specified name and data strings.
     *
     * @param target The target part of the processing instruction.
     * @param data   The data for the node.
     * @return The new <code>ProcessingInstruction</code> object.
     * @throws DOMException INVALID_CHARACTER_ERR: Raised if the specified target contains an
     *                      illegal character. <br>NOT_SUPPORTED_ERR: Raised if this document is an
     *                      HTML document.
     */
    public ProcessingInstruction createProcessingInstruction(String target, String data)
            throws DOMException {
        return document.createProcessingInstruction(target, data);
    }

    /**
     * Creates an <code>Attr</code> of the given name. Note that the <code>Attr</code> instance can
     * then be set on an <code>Element</code> using the <code>setAttributeNode</code> method. <br>To
     * create an attribute with a qualified name and namespace URI, use the
     * <code>createAttributeNS</code> method.
     *
     * @param name The name of the attribute.
     * @return A new <code>Attr</code> object with the <code>nodeName</code> attribute set to
     *         <code>name</code>, and <code>localName</code>, <code>prefix</code>, and
     *         <code>namespaceURI</code> set to <code>null</code>. The value of the attribute is the
     *         empty string.
     * @throws DOMException INVALID_CHARACTER_ERR: Raised if the specified name contains an illegal
     *                      character.
     */
    public Attr createAttribute(String name) throws DOMException {
        return document.createAttribute(name);
    }

    /**
     * Creates an <code>EntityReference</code> object. In addition, if the referenced entity is
     * known, the child list of the <code>EntityReference</code> node is made the same as that of
     * the corresponding <code>Entity</code> node.If any descendant of the <code>Entity</code> node
     * has an unbound namespace prefix, the corresponding descendant of the created
     * <code>EntityReference</code> node is also unbound; (its <code>namespaceURI</code> is
     * <code>null</code>). The DOM Level 2 does not support any mechanism to resolve namespace
     * prefixes.
     *
     * @param name The name of the entity to reference.
     * @return The new <code>EntityReference</code> object.
     * @throws DOMException INVALID_CHARACTER_ERR: Raised if the specified name contains an illegal
     *                      character. <br>NOT_SUPPORTED_ERR: Raised if this document is an HTML
     *                      document.
     */
    public EntityReference createEntityReference(String name) throws DOMException {
        return document.createEntityReference(name);
    }

    /**
     * Returns a <code>NodeList</code> of all the <code>Elements</code> with a given tag name in the
     * order in which they are encountered in a preorder traversal of the <code>Document</code>
     * tree.
     *
     * @param tagname The name of the tag to match on. The special value "*" matches all tags.
     * @return A new <code>NodeList</code> object containing all the matched <code>Elements</code>.
     */
    public NodeList getElementsByTagName(String tagname) {
        return document.getElementsByTagName(tagname);
    }

    /**
     * Imports a node from another document to this document. The returned node has no parent;
     * (<code>parentNode</code> is <code>null</code>). The source node is not altered or removed
     * from the original document; this method creates a new copy of the source node. <br>For all
     * nodes, importing a node creates a node object owned by the importing document, with attribute
     * values identical to the source node's <code>nodeName</code> and <code>nodeType</code>, plus
     * the attributes related to namespaces (<code>prefix</code>, <code>localName</code>, and
     * <code>namespaceURI</code>). As in the <code>cloneNode</code> operation on a
     * <code>Node</code>, the source node is not altered. <br>Additional information is copied as
     * appropriate to the <code>nodeType</code>, attempting to mirror the behavior expected if a
     * fragment of XML or HTML source was copied from one document to another, recognizing that the
     * two documents may have different DTDs in the XML case. The following list describes the
     * specifics for each type of node. <dl> <dt>ATTRIBUTE_NODE</dt> <dd>The
     * <code>ownerElement</code> attribute is set to <code>null</code> and the
     * <code>specified</code> flag is set to <code>true</code> on the generated <code>Attr</code>.
     * The descendants of the source <code>Attr</code> are recursively imported and the resulting
     * nodes reassembled to form the corresponding subtree. Note that the <code>deep</code>
     * parameter has no effect on <code>Attr</code> nodes; they always carry their children with
     * them when imported.</dd> <dt>DOCUMENT_FRAGMENT_NODE</dt> <dd>If the <code>deep</code> option
     * was set to <code>true</code>, the descendants of the source element are recursively imported
     * and the resulting nodes reassembled to form the corresponding subtree. Otherwise, this simply
     * generates an empty <code>DocumentFragment</code>.</dd> <dt>DOCUMENT_NODE</dt>
     * <dd><code>Document</code> nodes cannot be imported.</dd> <dt>DOCUMENT_TYPE_NODE</dt>
     * <dd><code>DocumentType</code> nodes cannot be imported.</dd> <dt>ELEMENT_NODE</dt>
     * <dd>Specified attribute nodes of the source element are imported, and the generated
     * <code>Attr</code> nodes are attached to the generated <code>Element</code>. Default
     * attributes are not copied, though if the document being imported into defines default
     * attributes for this element name, those are assigned. If the <code>importNode</code>
     * <code>deep</code> parameter was set to <code>true</code>, the descendants of the source
     * element are recursively imported and the resulting nodes reassembled to form the
     * corresponding subtree.</dd> <dt>ENTITY_NODE</dt> <dd><code>Entity</code> nodes can be
     * imported, however in the current release of the DOM the <code>DocumentType</code> is
     * readonly. Ability to add these imported nodes to a <code>DocumentType</code> will be
     * considered for addition to a future release of the DOM.On import, the <code>publicId</code>,
     * <code>systemId</code>, and <code>notationName</code> attributes are copied. If a
     * <code>deep</code> import is requested, the descendants of the the source <code>Entity</code>
     * are recursively imported and the resulting nodes reassembled to form the corresponding
     * subtree.</dd> <dt> ENTITY_REFERENCE_NODE</dt> <dd>Only the <code>EntityReference</code>
     * itself is copied, even if a <code>deep</code> import is requested, since the source and
     * destination documents might have defined the entity differently. If the document being
     * imported into provides a definition for this entity name, its value is assigned.</dd>
     * <dt>NOTATION_NODE</dt> <dd> <code>Notation</code> nodes can be imported, however in the
     * current release of the DOM the <code>DocumentType</code> is readonly. Ability to add these
     * imported nodes to a <code>DocumentType</code> will be considered for addition to a future
     * release of the DOM.On import, the <code>publicId</code> and <code>systemId</code> attributes
     * are copied. Note that the <code>deep</code> parameter has no effect on <code>Notation</code>
     * nodes since they never have any children.</dd> <dt> PROCESSING_INSTRUCTION_NODE</dt> <dd>The
     * imported node copies its <code>target</code> and <code>data</code> values from those of the
     * source node.</dd> <dt>TEXT_NODE, CDATA_SECTION_NODE, COMMENT_NODE</dt> <dd>These three types
     * of nodes inheriting from <code>CharacterData</code> copy their <code>data</code> and
     * <code>length</code> attributes from those of the source node.</dd> </dl>
     *
     * @param importedNode The node to import.
     * @param deep         If <code>true</code>, recursively import the subtree under the specified
     *                     node; if <code>false</code>, import only the node itself, as explained
     *                     above. This has no effect on <code>Attr</code> , <code>EntityReference</code>,
     *                     and <code>Notation</code> nodes.
     * @return The imported node that belongs to this <code>Document</code>.
     * @throws DOMException NOT_SUPPORTED_ERR: Raised if the type of node being imported is not
     *                      supported.
     * @since DOM Level 2
     */
    public Node importNode(Node importedNode, boolean deep) throws DOMException {
        return document.importNode(importedNode, deep);
    }

    /**
     * Creates an element of the given qualified name and namespace URI.
     *
     * @param namespaceURI  The namespace URI of the element to create.
     * @param qualifiedName The qualified name of the element type to instantiate.
     * @return A new <code>Element</code> object with the following attributes: <table border='1'
     *         summary="Description of attributes and values for the new Element object"> <tr>
     *         <th>Attribute</th> <th>Value</th> </tr> <tr> <td valign='top'><code>Node.nodeName</code></td>
     *         <td valign='top'> <code>qualifiedName</code></td> </tr> <tr> <td
     *         valign='top'><code>Node.namespaceURI</code></td> <td valign='top'>
     *         <code>namespaceURI</code></td> </tr> <tr> <td valign='top'><code>Node.prefix</code></td>
     *         <td valign='top'>prefix, extracted from <code>qualifiedName</code>, or
     *         <code>null</code> if there is no prefix</td> </tr> <tr> <td
     *         valign='top'><code>Node.localName</code></td> <td valign='top'>local name, extracted
     *         from <code>qualifiedName</code></td> </tr> <tr> <td valign='top'><code>Element.tagName</code></td>
     *         <td valign='top'> <code>qualifiedName</code></td> </tr> </table>
     * @throws DOMException INVALID_CHARACTER_ERR: Raised if the specified qualified name contains
     *                      an illegal character, per the XML 1.0 specification . <br>NAMESPACE_ERR:
     *                      Raised if the <code>qualifiedName</code> is malformed per the Namespaces
     *                      in XML specification, if the <code>qualifiedName</code> has a prefix and
     *                      the <code>namespaceURI</code> is <code>null</code>, or if the
     *                      <code>qualifiedName</code> has a prefix that is "xml" and the
     *                      <code>namespaceURI</code> is different from " http://www.w3.org/XML/1998/namespace"
     *                      . <br>NOT_SUPPORTED_ERR: Always thrown if the current document does not
     *                      support the <code>"XML"</code> feature, since namespaces were defined by
     *                      XML.
     * @since DOM Level 2
     */
    public Element createElementNS(String namespaceURI, String qualifiedName) throws DOMException {
        return document.createElementNS(namespaceURI, qualifiedName);
    }

    /**
     * Creates an attribute of the given qualified name and namespace URI.
     *
     * @param namespaceURI  The namespace URI of the attribute to create.
     * @param qualifiedName The qualified name of the attribute to instantiate.
     * @return A new <code>Attr</code> object with the following attributes: <table border='1'
     *         summary="Description of attributes and values for the new Attr object"> <tr> <th>
     *         Attribute</th> <th>Value</th> </tr> <tr> <td valign='top'><code>Node.nodeName</code></td>
     *         <td valign='top'>qualifiedName</td> </tr> <tr> <td valign='top'>
     *         <code>Node.namespaceURI</code></td> <td valign='top'><code>namespaceURI</code></td>
     *         </tr> <tr> <td valign='top'> <code>Node.prefix</code></td> <td valign='top'>prefix,
     *         extracted from <code>qualifiedName</code>, or <code>null</code> if there is no
     *         prefix</td> </tr> <tr> <td valign='top'><code>Node.localName</code></td> <td
     *         valign='top'>local name, extracted from <code>qualifiedName</code></td> </tr> <tr>
     *         <td valign='top'><code>Attr.name</code></td> <td valign='top'>
     *         <code>qualifiedName</code></td> </tr> <tr> <td valign='top'><code>Node.nodeValue</code></td>
     *         <td valign='top'>the empty string</td> </tr> </table>
     * @throws DOMException INVALID_CHARACTER_ERR: Raised if the specified qualified name contains
     *                      an illegal character, per the XML 1.0 specification . <br>NAMESPACE_ERR:
     *                      Raised if the <code>qualifiedName</code> is malformed per the Namespaces
     *                      in XML specification, if the <code>qualifiedName</code> has a prefix and
     *                      the <code>namespaceURI</code> is <code>null</code>, if the
     *                      <code>qualifiedName</code> has a prefix that is "xml" and the
     *                      <code>namespaceURI</code> is different from " http://www.w3.org/XML/1998/namespace",
     *                      or if the <code>qualifiedName</code>, or its prefix, is "xmlns" and the
     *                      <code>namespaceURI</code> is different from " http://www.w3.org/2000/xmlns/".
     *                      <br>NOT_SUPPORTED_ERR: Always thrown if the current document does not
     *                      support the <code>"XML"</code> feature, since namespaces were defined by
     *                      XML.
     * @since DOM Level 2
     */
    public Attr createAttributeNS(String namespaceURI, String qualifiedName) throws DOMException {
        return document.createAttributeNS(namespaceURI, qualifiedName);
    }

    /**
     * Returns a <code>NodeList</code> of all the <code>Elements</code> with a given local name and
     * namespace URI in the order in which they are encountered in a preorder traversal of the
     * <code>Document</code> tree.
     *
     * @param namespaceURI The namespace URI of the elements to match on. The special value "*"
     *                     matches all namespaces.
     * @param localName    The local name of the elements to match on. The special value "*" matches
     *                     all local names.
     * @return A new <code>NodeList</code> object containing all the matched <code>Elements</code>.
     * @since DOM Level 2
     */
    public NodeList getElementsByTagNameNS(String namespaceURI, String localName) {
        return document.getElementsByTagNameNS(namespaceURI, localName);
    }

    /**
     * Returns the <code>Element</code> whose <code>ID</code> is given by <code>elementId</code>. If
     * no such element exists, returns <code>null</code>. Behavior is not defined if more than one
     * element has this <code>ID</code>. The DOM implementation must have information that says
     * which attributes are of type ID. Attributes with the name "ID" are not of type ID unless so
     * defined. Implementations that do not know whether attributes are of type ID or not are
     * expected to return <code>null</code>.
     *
     * @param elementId The unique <code>id</code> value for an element.
     * @return The matching element.
     * @since DOM Level 2
     */
    public Element getElementById(String elementId) {
        return document.getElementById(elementId);
    }

    public String getInputEncoding() {
        //return ((DeferredDocumentImpl)(((DOMSource)this.source).getNode())).getInputEncoding();
        return this.envelope.getEncodingStyle();
    }

    public String getXmlEncoding() {
        return document.getXmlEncoding();
    }

    public boolean getXmlStandalone() {
        return document.getXmlStandalone();
    }

    public void setXmlStandalone(boolean xmlStandalone) throws DOMException {
        document.setXmlStandalone(xmlStandalone);
    }

    public String getXmlVersion() {
        return document.getXmlVersion();
    }

    public void setXmlVersion(String xmlVersion) throws DOMException {
        document.setXmlVersion(xmlVersion);
    }

    public boolean getStrictErrorChecking() {
        return document.getStrictErrorChecking();
    }

    public void setStrictErrorChecking(boolean strictErrorChecking) {
        document.setStrictErrorChecking(strictErrorChecking);
    }

    public String getDocumentURI() {
        return document.getDocumentURI();
    }

    public void setDocumentURI(String documentURI) {
        document.setDocumentURI(documentURI);
    }

    public Node adoptNode(Node source) throws DOMException {
        return document.adoptNode(source);
    }

    public DOMConfiguration getDomConfig() {
        return document.getDomConfig();
    }

    public void normalizeDocument() {
        document.normalizeDocument();
    }

    public Node renameNode(Node n, String namespaceURI, String qualifiedName) throws DOMException {
        return document.renameNode(n, namespaceURI, qualifiedName);
    }

    /** The name of this node, depending on its type; see the table above. */
    public String getNodeName() {
        return document.getNodeName();
    }

    /**
     * The value of this node, depending on its type; see the table above. When it is defined to be
     * <code>null</code>, setting it has no effect.
     *
     * @throws DOMException NO_MODIFICATION_ALLOWED_ERR: Raised when the node is readonly.
     * @throws DOMException DOMSTRING_SIZE_ERR: Raised when it would return more characters than fit
     *                      in a <code>DOMString</code> variable on the implementation platform.
     */
    public String getNodeValue() throws DOMException {
        return document.getNodeValue();
    }

    /**
     * The value of this node, depending on its type; see the table above. When it is defined to be
     * <code>null</code>, setting it has no effect.
     *
     * @throws DOMException NO_MODIFICATION_ALLOWED_ERR: Raised when the node is readonly.
     * @throws DOMException DOMSTRING_SIZE_ERR: Raised when it would return more characters than fit
     *                      in a <code>DOMString</code> variable on the implementation platform.
     */
    public void setNodeValue(String arg0) throws DOMException {
        document.setNodeValue(arg0);
    }

    /** A code representing the type of the underlying object, as defined above. */
    public short getNodeType() {
        return document.getNodeType();
    }

    /**
     * The parent of this node. All nodes, except <code>Attr</code>, <code>Document</code>,
     * <code>DocumentFragment</code>, <code>Entity</code>, and <code>Notation</code> may have a
     * parent. However, if a node has just been created and not yet added to the tree, or if it has
     * been removed from the tree, this is <code>null</code>.
     */
    public Node getParentNode() {
        return toSAAJNode(document.getParentNode());
    }

    /**
     * A <code>NodeList</code> that contains all children of this node. If there are no children,
     * this is a <code>NodeList</code> containing no nodes.
     */
    public NodeList getChildNodes() {
        NodeList childNodes = document.getChildNodes();
        NodeListImpl nodes = new NodeListImpl();
        for (int i = 0; i < childNodes.getLength(); i++) {
            nodes.addNode(toSAAJNode(childNodes.item(i)));
        }
        return nodes;
    }

    /** The first child of this node. If there is no such node, this returns <code>null</code>. */
    public Node getFirstChild() {
        return toSAAJNode(document.getFirstChild());
    }

    /** The last child of this node. If there is no such node, this returns <code>null</code>. */
    public Node getLastChild() {
        return toSAAJNode(document.getLastChild());
    }

    /**
     * The node immediately preceding this node. If there is no such node, this returns
     * <code>null</code>.
     */
    public Node getPreviousSibling() {
        return toSAAJNode(document.getPreviousSibling());
    }

    /**
     * The node immediately following this node. If there is no such node, this returns
     * <code>null</code>.
     */
    public Node getNextSibling() {
        return toSAAJNode(document.getNextSibling());
    }

    /**
     * A <code>NamedNodeMap</code> containing the attributes of this node (if it is an
     * <code>Element</code>) or <code>null</code> otherwise.
     */
    public NamedNodeMap getAttributes() {
        return document.getAttributes();
    }

    /**
     * The <code>Document</code> object associated with this node. This is also the
     * <code>Document</code> object used to create new nodes. When this node is a
     * <code>Document</code> or a <code>DocumentType</code> which is not used with any
     * <code>Document</code> yet, this is <code>null</code>.
     */
    public Document getOwnerDocument() {
        return document.getOwnerDocument();
    }

    /**
     * Inserts the node <code>newChild</code> before the existing child node <code>refChild</code>.
     * If <code>refChild</code> is <code>null</code>, insert <code>newChild</code> at the end of the
     * list of children. <br>If <code>newChild</code> is a <code>DocumentFragment</code> object, all
     * of its children are inserted, in the same order, before <code>refChild</code>. If the
     * <code>newChild</code> is already in the tree, it is first removed.
     *
     * @param newChild The node to insert.
     * @param refChild The reference node, i.e., the node before which the new node must be
     *                 inserted.
     * @return The node being inserted.
     * @throws DOMException HIERARCHY_REQUEST_ERR: Raised if this node is of a type that does not
     *                      allow children of the type of the <code>newChild</code> node, or if the
     *                      node to insert is one of this node's ancestors or this node itself.
     *                      <br>WRONG_DOCUMENT_ERR: Raised if <code>newChild</code> was created from
     *                      a different document than the one that created this node.
     *                      <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly or if
     *                      the parent of the node being inserted is readonly. <br>NOT_FOUND_ERR:
     *                      Raised if <code>refChild</code> is not a child of this node.
     */
    public Node insertBefore(Node newChild, Node refChild) throws DOMException {
        return document.insertBefore(newChild, refChild);
    }

    /**
     * Replaces the child node <code>oldChild</code> with <code>newChild</code> in the list of
     * children, and returns the <code>oldChild</code> node. <br>If <code>newChild</code> is a
     * <code>DocumentFragment</code> object, <code>oldChild</code> is replaced by all of the
     * <code>DocumentFragment</code> children, which are inserted in the same order. If the
     * <code>newChild</code> is already in the tree, it is first removed.
     *
     * @param newChild The new node to put in the child list.
     * @param oldChild The node being replaced in the list.
     * @return The node replaced.
     * @throws DOMException HIERARCHY_REQUEST_ERR: Raised if this node is of a type that does not
     *                      allow children of the type of the <code>newChild</code> node, or if the
     *                      node to put in is one of this node's ancestors or this node itself.
     *                      <br>WRONG_DOCUMENT_ERR: Raised if <code>newChild</code> was created from
     *                      a different document than the one that created this node.
     *                      <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node or the parent of
     *                      the new node is readonly. <br>NOT_FOUND_ERR: Raised if
     *                      <code>oldChild</code> is not a child of this node.
     */
    public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
        return document.replaceChild(newChild, oldChild);
    }

    /**
     * Removes the child node indicated by <code>oldChild</code> from the list of children, and
     * returns it.
     *
     * @param oldChild The node being removed.
     * @return The node removed.
     * @throws DOMException NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     *                      <br>NOT_FOUND_ERR: Raised if <code>oldChild</code> is not a child of
     *                      this node.
     */
    public Node removeChild(Node oldChild) throws DOMException {
        if (oldChild instanceof SOAPElementImpl) {
            oldChild = ((SOAPElementImpl)oldChild).getElement();
        } else if (oldChild instanceof TextImplEx) {
            // TODO: handle text nodes somehow
        }
        return document.removeChild(oldChild);
    }

    /**
     * Adds the node <code>newChild</code> to the end of the list of children of this node. If the
     * <code>newChild</code> is already in the tree, it is first removed.
     *
     * @param newChild The node to add.If it is a <code>DocumentFragment</code> object, the entire
     *                 contents of the document fragment are moved into the child list of this node
     * @return The node added.
     * @throws DOMException HIERARCHY_REQUEST_ERR: Raised if this node is of a type that does not
     *                      allow children of the type of the <code>newChild</code> node, or if the
     *                      node to append is one of this node's ancestors or this node itself.
     *                      <br>WRONG_DOCUMENT_ERR: Raised if <code>newChild</code> was created from
     *                      a different document than the one that created this node.
     *                      <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly or if
     *                      the previous parent of the node being inserted is readonly.
     */
    public Node appendChild(Node newChild) throws DOMException {
        return document.appendChild(newChild);
    }

    /**
     * Returns whether this node has any children.
     *
     * @return <code>true</code> if this node has any children, <code>false</code> otherwise.
     */
    public boolean hasChildNodes() {
        return document.hasChildNodes();
    }

    /**
     * Returns a duplicate of this node, i.e., serves as a generic copy constructor for nodes. The
     * duplicate node has no parent; ( <code>parentNode</code> is <code>null</code>.). <br>Cloning
     * an <code>Element</code> copies all attributes and their values, including those generated by
     * the XML processor to represent defaulted attributes, but this method does not copy any text
     * it contains unless it is a deep clone, since the text is contained in a child
     * <code>Text</code> node. Cloning an <code>Attribute</code> directly, as opposed to be cloned
     * as part of an <code>Element</code> cloning operation, returns a specified attribute (
     * <code>specified</code> is <code>true</code>). Cloning any other type of node simply returns a
     * copy of this node. <br>Note that cloning an immutable subtree results in a mutable copy, but
     * the children of an <code>EntityReference</code> clone are readonly . In addition, clones of
     * unspecified <code>Attr</code> nodes are specified. And, cloning <code>Document</code>,
     * <code>DocumentType</code>, <code>Entity</code>, and <code>Notation</code> nodes is
     * implementation dependent.
     *
     * @param deep If <code>true</code>, recursively clone the subtree under the specified node; if
     *             <code>false</code>, clone only the node itself (and its attributes, if it is an
     *             <code>Element</code>).
     * @return The duplicate node.
     */
    public Node cloneNode(boolean deep) {
        return document.cloneNode(deep);
    }

    /**
     * Puts all <code>Text</code> nodes in the full depth of the sub-tree underneath this
     * <code>Node</code>, including attribute nodes, into a "normal" form where only structure
     * (e.g., elements, comments, processing instructions, CDATA sections, and entity references)
     * separates <code>Text</code> nodes, i.e., there are neither adjacent <code>Text</code> nodes
     * nor empty <code>Text</code> nodes. This can be used to ensure that the DOM view of a document
     * is the same as if it were saved and re-loaded, and is useful when operations (such as
     * XPointer  lookups) that depend on a particular document tree structure are to be used.In
     * cases where the document contains <code>CDATASections</code>, the normalize operation alone
     * may not be sufficient, since XPointers do not differentiate between <code>Text</code> nodes
     * and <code>CDATASection</code> nodes.
     */
    public void normalize() {
        document.normalize();
    }

    /**
     * Tests whether the DOM implementation implements a specific feature and that feature is
     * supported by this node.
     *
     * @param feature The name of the feature to test. This is the same name which can be passed to
     *                the method <code>hasFeature</code> on <code>DOMImplementation</code>.
     * @param version This is the version number of the feature to test. In Level 2, version 1, this
     *                is the string "2.0". If the version is not specified, supporting any version
     *                of the feature will cause the method to return <code>true</code>.
     * @return Returns <code>true</code> if the specified feature is supported on this node,
     *         <code>false</code> otherwise.
     * @since DOM Level 2
     */
    public boolean isSupported(String feature, String version) {
        return document.isSupported(feature, version);
    }

    /**
     * The namespace URI of this node, or <code>null</code> if it is unspecified. <br>This is not a
     * computed value that is the result of a namespace lookup based on an examination of the
     * namespace declarations in scope. It is merely the namespace URI given at creation time.
     * <br>For nodes of any type other than <code>ELEMENT_NODE</code> and
     * <code>ATTRIBUTE_NODE</code> and nodes created with a DOM Level 1 method, such as
     * <code>createElement</code> from the <code>Document</code> interface, this is always
     * <code>null</code>.Per the Namespaces in XML Specification  an attribute does not inherit its
     * namespace from the element it is attached to. If an attribute is not explicitly given a
     * namespace, it simply has no namespace.
     *
     * @since DOM Level 2
     */
    public String getNamespaceURI() {
        return document.getNamespaceURI();
    }

    /**
     * The namespace prefix of this node, or <code>null</code> if it is unspecified. <br>Note that
     * setting this attribute, when permitted, changes the <code>nodeName</code> attribute, which
     * holds the qualified name, as well as the <code>tagName</code> and <code>name</code>
     * attributes of the <code>Element</code> and <code>Attr</code> interfaces, when applicable.
     * <br>Note also that changing the prefix of an attribute that is known to have a default value,
     * does not make a new attribute with the default value and the original prefix appear, since
     * the <code>namespaceURI</code> and <code>localName</code> do not change. <br>For nodes of any
     * type other than <code>ELEMENT_NODE</code> and <code>ATTRIBUTE_NODE</code> and nodes created
     * with a DOM Level 1 method, such as <code>createElement</code> from the <code>Document</code>
     * interface, this is always <code>null</code>.
     *
     * @throws DOMException INVALID_CHARACTER_ERR: Raised if the specified prefix contains an
     *                      illegal character, per the XML 1.0 specification .
     *                      <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     *                      <br>NAMESPACE_ERR: Raised if the specified <code>prefix</code> is
     *                      malformed per the Namespaces in XML specification, if the
     *                      <code>namespaceURI</code> of this node is <code>null</code>, if the
     *                      specified prefix is "xml" and the <code>namespaceURI</code> of this node
     *                      is different from "http://www.w3.org/XML/1998/namespace", if this node
     *                      is an attribute and the specified prefix is "xmlns" and the
     *                      <code>namespaceURI</code> of this node is different from "
     *                      http://www.w3.org/2000/xmlns/", or if this node is an attribute and the
     *                      <code>qualifiedName</code> of this node is "xmlns" .
     * @since DOM Level 2
     */
    public String getPrefix() {
        return document.getPrefix();
    }

    /**
     * The namespace prefix of this node, or <code>null</code> if it is unspecified. <br>Note that
     * setting this attribute, when permitted, changes the <code>nodeName</code> attribute, which
     * holds the qualified name, as well as the <code>tagName</code> and <code>name</code>
     * attributes of the <code>Element</code> and <code>Attr</code> interfaces, when applicable.
     * <br>Note also that changing the prefix of an attribute that is known to have a default value,
     * does not make a new attribute with the default value and the original prefix appear, since
     * the <code>namespaceURI</code> and <code>localName</code> do not change. <br>For nodes of any
     * type other than <code>ELEMENT_NODE</code> and <code>ATTRIBUTE_NODE</code> and nodes created
     * with a DOM Level 1 method, such as <code>createElement</code> from the <code>Document</code>
     * interface, this is always <code>null</code>.
     *
     * @throws DOMException INVALID_CHARACTER_ERR: Raised if the specified prefix contains an
     *                      illegal character, per the XML 1.0 specification .
     *                      <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     *                      <br>NAMESPACE_ERR: Raised if the specified <code>prefix</code> is
     *                      malformed per the Namespaces in XML specification, if the
     *                      <code>namespaceURI</code> of this node is <code>null</code>, if the
     *                      specified prefix is "xml" and the <code>namespaceURI</code> of this node
     *                      is different from "http://www.w3.org/XML/1998/namespace", if this node
     *                      is an attribute and the specified prefix is "xmlns" and the
     *                      <code>namespaceURI</code> of this node is different from "
     *                      http://www.w3.org/2000/xmlns/", or if this node is an attribute and the
     *                      <code>qualifiedName</code> of this node is "xmlns" .
     * @since DOM Level 2
     */
    public void setPrefix(String arg0) throws DOMException {
        document.setPrefix(arg0);
    }

    /**
     * Returns the local part of the qualified name of this node. <br>For nodes of any type other
     * than <code>ELEMENT_NODE</code> and <code>ATTRIBUTE_NODE</code> and nodes created with a DOM
     * Level 1 method, such as <code>createElement</code> from the <code>Document</code> interface,
     * this is always <code>null</code>.
     *
     * @since DOM Level 2
     */
    public String getLocalName() {
        return document.getLocalName();
    }

    /**
     * Returns whether this node (if it is an element) has any attributes.
     *
     * @return <code>true</code> if this node has any attributes, <code>false</code> otherwise.
     * @since DOM Level 2
     */
    public boolean hasAttributes() {
        return document.hasAttributes();
    }

    protected void setMessage(SOAPMessageImpl message) {
        soapMessage = message;
    }

    /*
     * DOM-Level 3 methods
     */

    public String getBaseURI() {
        return document.getBaseURI();
    }

    public short compareDocumentPosition(Node node) throws DOMException {
        return document.compareDocumentPosition(node);
    }

    public String getTextContent() throws DOMException {
        return document.getTextContent();
    }

    public void setTextContent(String textContent) throws DOMException {
        document.setTextContent(textContent);
    }

    public boolean isSameNode(Node other) {
        return document.isSameNode(other);
    }

    public String lookupPrefix(String namespaceURI) {
        return document.lookupPrefix(namespaceURI);
    }

    public boolean isDefaultNamespace(String namespaceURI) {
        return document.isDefaultNamespace(namespaceURI);
    }

    public String lookupNamespaceURI(String prefix) {
        return document.lookupNamespaceURI(prefix);
    }

    public boolean isEqualNode(Node node) {
        return document.isEqualNode(node);
    }

    public Object getFeature(String feature, String version) {
        return document.getFeature(feature, version);
    }

    public Object setUserData(String key, Object data, UserDataHandler handler) {
        return document.setUserData(key, data, handler);
    }

    public Object getUserData(String key) {
        return document.getUserData(key);
    }

    public String getValue() {
    	//There are no immediate child text nodes to soap part
        return null;        
    }        


    public void setParentElement(SOAPElement parent) throws SOAPException {
    	throw new SOAPException("Cannot set the parent element of SOAPPart");
    }

    public SOAPElement getParentElement() {
        return null;  //SOAP part is the root element
    }

    public void detachNode() {
        //nothing to do here
    }

    public void recycleNode() {
        //nothing to do here
    }

    public void setValue(String value) {
    	throw new IllegalStateException("Cannot set value of SOAPPart.");
    }
    
    javax.xml.soap.Node toSAAJNode(org.w3c.dom.Node domNode) {
        return NodeImplEx.toSAAJNode(domNode, this);
    }
}
