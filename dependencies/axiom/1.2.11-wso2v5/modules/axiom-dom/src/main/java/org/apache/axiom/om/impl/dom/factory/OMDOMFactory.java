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

package org.apache.axiom.om.impl.dom.factory;

import org.apache.axiom.ext.stax.datahandler.DataHandlerProvider;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMComment;
import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.OMDocType;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMHierarchyException;
import org.apache.axiom.om.OMMetaFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMProcessingInstruction;
import org.apache.axiom.om.OMSourcedElement;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.impl.dom.AttrImpl;
import org.apache.axiom.om.impl.dom.CDATASectionImpl;
import org.apache.axiom.om.impl.dom.CommentImpl;
import org.apache.axiom.om.impl.dom.DocumentFragmentImpl;
import org.apache.axiom.om.impl.dom.DocumentImpl;
import org.apache.axiom.om.impl.dom.DocumentTypeImpl;
import org.apache.axiom.om.impl.dom.ElementImpl;
import org.apache.axiom.om.impl.dom.NamespaceImpl;
import org.apache.axiom.om.impl.dom.OMDOMException;
import org.apache.axiom.om.impl.dom.ParentNode;
import org.apache.axiom.om.impl.dom.ProcessingInstructionImpl;
import org.apache.axiom.om.impl.dom.TextImpl;
import org.apache.axiom.om.impl.dom.TextNodeImpl;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;

/**
 * OM factory implementation for DOOM. It creates nodes that implement
 * DOM as defined by the interfaces in {@link org.w3c.dom}.
 * <p>
 * Since DOM requires every node to have an owner document even if it has not yet
 * been added to a tree, this factory internally maintains a reference to a
 * {@link DocumentImpl} instance. The document can be set explicitly using the
 * {@link #OMDOMFactory(DocumentImpl)} constructor or the {@link #setDocument(DocumentImpl)}
 * method. If none is set, it will be implicitly created when the first node is created.
 * All nodes created by this factory will have this {@link DocumentImpl} instance as owner
 * document.
 * <p>
 * This has several important consequences:
 * <ul>
 *   <li>The same instance of this class should not be used to parse or construct
 *       multiple documents unless {@link #setDocument(DocumentImpl)} is used
 *       to reset the {@link DocumentImpl} instance before processing the next document.</li>
 *   <li>Instances of this class are not thread safe and using a single instance concurrently
 *       will lead to undefined results.</li>
 * </ul>
 */
public class OMDOMFactory implements OMFactory {
    private final OMDOMMetaFactory metaFactory;

    protected DocumentImpl document;

    public OMDOMFactory(OMDOMMetaFactory metaFactory) {
        this.metaFactory = metaFactory;
    }

    public OMDOMFactory() {
        this(new OMDOMMetaFactory());
    }

    public OMDOMFactory(DocumentImpl doc) {
        this(new OMDOMMetaFactory());
        this.document = doc;
    }

    public OMMetaFactory getMetaFactory() {
        return metaFactory;
    }

    public OMDocument createOMDocument() {
        if (this.document == null)
            this.document = new DocumentImpl(this);

        return this.document;
    }

    /**
     * Configure this factory to use the given document. Use with care.
     *
     * @param document
     */
    public void setDocument(DocumentImpl document) {
        this.document = document;
    }

    public OMElement createOMElement(String localName, OMNamespace ns) {
        return new ElementImpl((DocumentImpl) this.createOMDocument(),
                               localName, (NamespaceImpl) ns, this);
    }

    public OMElement createOMElement(String localName, OMNamespace ns,
                                     OMContainer parent) throws OMDOMException {
        if (parent == null) {
            return new ElementImpl((DocumentImpl) this.createOMDocument(),
                               localName, (NamespaceImpl) ns, this);
        }

        switch (((ParentNode) parent).getNodeType()) {
            case Node.ELEMENT_NODE: // We are adding a new child to an elem
                ElementImpl parentElem = (ElementImpl) parent;
                ElementImpl elem = new ElementImpl((DocumentImpl) parentElem
                        .getOwnerDocument(), localName, (NamespaceImpl) ns, this);
                parentElem.appendChild(elem);
                return elem;

            case Node.DOCUMENT_NODE:
                DocumentImpl docImpl = (DocumentImpl) parent;
                return new ElementImpl(docImpl, localName,
                                       (NamespaceImpl) ns, this);

            case Node.DOCUMENT_FRAGMENT_NODE:
                DocumentFragmentImpl docFragImpl = (DocumentFragmentImpl) parent;
                return new ElementImpl((DocumentImpl) docFragImpl
                        .getOwnerDocument(), localName, (NamespaceImpl) ns, this);
            default:
                throw new OMDOMException(
                        "The parent container can only be an ELEMENT, DOCUMENT " +
                                "or a DOCUMENT FRAGMENT");
        }
    }

    /** Creates an OMElement with the builder. */
    public OMElement createOMElement(String localName, OMNamespace ns,
                                     OMContainer parent, OMXMLParserWrapper builder) {
        switch (((ParentNode) parent).getNodeType()) {
            case Node.ELEMENT_NODE: // We are adding a new child to an elem
                ElementImpl parentElem = (ElementImpl) parent;
                ElementImpl elem = new ElementImpl((DocumentImpl) parentElem
                        .getOwnerDocument(), localName, (NamespaceImpl) ns,
                                             builder, this);
                parentElem.appendChild(elem);
                return elem;
            case Node.DOCUMENT_NODE:
                DocumentImpl docImpl = (DocumentImpl) parent;
                ElementImpl elem2 = new ElementImpl(docImpl, localName,
                                                    (NamespaceImpl) ns, builder, this);
                docImpl.appendChild(elem2);
                return elem2;

            case Node.DOCUMENT_FRAGMENT_NODE:
                DocumentFragmentImpl docFragImpl = (DocumentFragmentImpl) parent;
                return new ElementImpl((DocumentImpl) docFragImpl
                        .getOwnerDocument(), localName, (NamespaceImpl) ns,
                                             builder, this);
            default:
                throw new OMDOMException(
                        "The parent container can only be an ELEMENT, DOCUMENT " +
                                "or a DOCUMENT FRAGMENT");
        }
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMFactory#createOMElement(org.apache.axiom.om.OMDataSource, java.lang.String, org.apache.axiom.om.OMNamespace, org.apache.axiom.om.OMContainer)
     */
    public OMElement createOMElement(OMDataSource source, String localName, OMNamespace ns,
                                     OMContainer parent) {
        throw new UnsupportedOperationException("Not supported for DOM");
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMFactory#createOMElement(org.apache.axiom.om.OMDataSource, java.lang.String, org.apache.axiom.om.OMNamespace)
     */
    public OMSourcedElement createOMElement(OMDataSource source, String localName, OMNamespace ns) {
        throw new UnsupportedOperationException("Not supported for DOM");
    }

    /**
     * Unsupported.
     */
    public OMSourcedElement createOMElement(OMDataSource source, QName qname) {
        throw new UnsupportedOperationException("Not supported for DOM");
    }

    /**
     * Creates an OMElement.
     *
     * @see org.apache.axiom.om.OMFactory#createOMElement(String, String,
     *      String)
     */
    public OMElement createOMElement(String localName, String namespaceURI,
                                     String namespacePrefix) {
        NamespaceImpl ns = new NamespaceImpl(namespaceURI, namespacePrefix);
        return this.createOMElement(localName, ns);
    }

    /**
     * Creates a new OMDOM Element node and adds it to the given parent.
     *
     * @see #createOMElement(String, OMNamespace, OMContainer)
     * @see org.apache.axiom.om.OMFactory#createOMElement( javax.xml.namespace.QName,
     *      org.apache.axiom.om.OMContainer)
     */
    public OMElement createOMElement(QName qname, OMContainer parent)
            throws OMException {
        NamespaceImpl ns;
        if (qname.getNamespaceURI().isEmpty()) {
            ns = null;
        } else if (qname.getPrefix() != null) {
            ns = new NamespaceImpl(qname.getNamespaceURI(), qname.getPrefix());
        } else {
            ns = new NamespaceImpl(qname.getNamespaceURI());
        }
        return createOMElement(qname.getLocalPart(), ns, parent);
    }

    /**
     * Create an OMElement with the given QName
     * <p/>
     * If the QName contains a prefix, we will ensure that an OMNamespace is created mapping the
     * given namespace to the given prefix.  If no prefix is passed, we'll create a generated one.
     *
     * @param qname
     * @return the new OMElement.
     */
    public OMElement createOMElement(QName qname) throws OMException {
        return createOMElement(qname, null);
    }

    /**
     * Creates a new OMNamespace.
     *
     * @see org.apache.axiom.om.OMFactory#createOMNamespace(String, String)
     */
    public OMNamespace createOMNamespace(String uri, String prefix) {
        return new NamespaceImpl(uri, prefix);
    }

    public OMText createOMText(OMContainer parent, String text) {
        return createOMText(parent, text, OMNode.TEXT_NODE);
    }

    public OMText createOMText(OMContainer parent, QName text) {
        return new TextImpl(parent, text, this);
    }

    public OMText createOMText(OMContainer parent, QName text, int type) {
        return new TextImpl(parent, text, type, this);
    }

    public OMText createOMText(OMContainer parent, String text, int type) {
        if (parent instanceof DocumentImpl) {
            throw new OMHierarchyException(
                    "DOM doesn't support text nodes as children of a document");
        }
        DocumentImpl ownerDocument = (DocumentImpl)((ElementImpl)parent).getOwnerDocument(); 
        TextNodeImpl txt;
        if (type == OMNode.CDATA_SECTION_NODE) {
            txt = new CDATASectionImpl(ownerDocument, text, this);
        } else {
            txt = new TextImpl(ownerDocument, text, type, this);
        }
        parent.addChild(txt);
        return txt;
    }
    
    
    public OMText createOMText(OMContainer parent, OMText source) {
        return new TextImpl(parent, (TextImpl) source, this);
    }

    public OMText createOMText(OMContainer parent, char[] charArary, int type) {
        ElementImpl parentElem = (ElementImpl) parent;
        TextImpl txt = new TextImpl((DocumentImpl) parentElem
                .getOwnerDocument(), charArary, this);
        parentElem.addChild(txt);
        return txt;
    }

    /**
     * Creates a OMDOM Text node carrying the given value.
     *
     * @see org.apache.axiom.om.OMFactory#createOMText(String)
     */
    public OMText createOMText(String s) {
        return new TextImpl(this.document, s, this);
    }

    /**
     * Creates a Character node of the given type.
     *
     * @see org.apache.axiom.om.OMFactory#createOMText(String, int)
     */
    public OMText createOMText(String text, int type) {
        switch (type) {
            case OMNode.TEXT_NODE:
                return new TextImpl(this.document, text, this);
            default:
                throw new OMDOMException("Only Text nodes are supported right now");
        }
    }

    /**
     * Creates a new OMDOM Text node with the value of the given text value along with the MTOM
     * optimization parameters and returns it.
     *
     * @see org.apache.axiom.om.OMFactory#createOMText(String, String, boolean)
     */
    public OMText createOMText(String text, String mimeType, boolean optimize) {
        return new TextImpl(this.document, text, mimeType, optimize, this);
    }

    /**
     * Creates a new OMDOM Text node with the given datahandler and the given MTOM optimization
     * configuration and returns it.
     *
     * @see org.apache.axiom.om.OMFactory#createOMText(Object, boolean)
     */
    public OMText createOMText(Object dataHandler, boolean optimize) {
        return new TextImpl(this.document, dataHandler, optimize, this);
    }

    public OMText createOMText(String contentID, DataHandlerProvider dataHandlerProvider,
            boolean optimize) {
        return new TextImpl(this.document, contentID, dataHandlerProvider, optimize, this);
    }

    /**
     * Creates an OMDOM Text node, adds it to the give parent element and returns it.
     *
     * @see org.apache.axiom.om.OMFactory#createOMText(OMContainer, String,
     *      String, boolean)
     */
    public OMText createOMText(OMContainer parent, String s, String mimeType,
                               boolean optimize) {
        TextImpl text = new TextImpl((DocumentImpl) ((ElementImpl) parent)
                .getOwnerDocument(), s, mimeType, optimize, this);
        parent.addChild(text);
        return text;
    }

    public OMText createOMText(String contentID, OMContainer parent,
                               OMXMLParserWrapper builder) {
        TextImpl text = new TextImpl(contentID, parent, builder, this);
        parent.addChild(text);
        return text;
    }

    public OMAttribute createOMAttribute(String localName, OMNamespace ns,
                                         String value) {
        return new AttrImpl(this.getDocument(), localName, ns, value, this);
    }

    public OMDocType createOMDocType(OMContainer parent, String content) {
        DocumentTypeImpl docType = new DocumentTypeImpl(this.getDocument(), this);
        docType.setValue(content);
        parent.addChild(docType);
        return docType;
    }

    public OMProcessingInstruction createOMProcessingInstruction(
            OMContainer parent, String piTarget, String piData) {
        ProcessingInstructionImpl pi =
            new ProcessingInstructionImpl(getDocumentFromParent(parent), piTarget, piData, this);
        parent.addChild(pi);
        return pi;
    }

    public OMComment createOMComment(OMContainer parent, String content) {
        CommentImpl comment = new CommentImpl(getDocumentFromParent(parent), content, this);
        parent.addChild(comment);
        return comment;
    }

    public DocumentImpl getDocument() {
        return (DocumentImpl) this.createOMDocument();
    }

    public OMDocument createOMDocument(OMXMLParserWrapper builder) {
        this.document = new DocumentImpl(builder, this);
        return this.document;
    }

    private DocumentImpl getDocumentFromParent(OMContainer parent) {
        if (parent instanceof DocumentImpl) {
            return (DocumentImpl) parent;
        } else {
            return (DocumentImpl) ((ParentNode) parent).getOwnerDocument();
        }
    }
}
