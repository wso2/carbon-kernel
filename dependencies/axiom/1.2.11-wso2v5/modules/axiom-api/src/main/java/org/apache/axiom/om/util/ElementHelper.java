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

package org.apache.axiom.om.util;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMSourcedElement;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.ds.ByteArrayDataSource;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axiom.util.stax.XMLStreamReaderUtils;
import org.apache.axiom.util.stax.xop.XOPUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.Iterator;

/**
 * Helper class to provide extra utility stuff against elements. The code is designed to work with
 * any element implementation.
 */

public class ElementHelper {

    private OMElement element;

    /**
     * Constructs and binds to an element.
     *
     * @param element element to work with
     */
    public ElementHelper(OMElement element) {
        this.element = element;
    }

    /**
     * Turns a prefix:local qname string into a proper QName, evaluating it in the OMElement
     * context.
     *
     * @param qname                    qname to resolve
     * @param defaultToParentNameSpace flag that controls behaviour when there is no namespace.
     * @return Returns null for any failure to extract a qname.
     */
    public QName resolveQName(String qname, boolean defaultToParentNameSpace) {
        int colon = qname.indexOf(':');
        if (colon < 0) {
            if (defaultToParentNameSpace) {
                //get the parent ns and use it for the child
                OMNamespace namespace = element.getNamespace();
                if (namespace != null) {
                    // Guard against QName implementation sillyness.
                    if (namespace.getPrefix() == null)
                        return new QName(namespace.getNamespaceURI(), qname);
                    else
                        return new QName(namespace.getNamespaceURI(), qname, namespace.getPrefix());
                }
            }
            //else things without no prefix are local.
            return new QName(qname);
        }
        String prefix = qname.substring(0, colon);
        String local = qname.substring(colon + 1);
        if (local.isEmpty()) {
            //empy local, exit accordingly
            return null;
        }

        OMNamespace namespace = element.findNamespaceURI(prefix);
        if (namespace == null) {
            return null;
        }
        return new QName(namespace.getNamespaceURI(), local, prefix);
    }

    /**
     * Turns a prefix:local qname string into a proper QName, evaluating it in the OMElement
     * context. Unprefixed qnames resolve to the local namespace.
     *
     * @param qname prefixed qname string to resolve
     * @return Returns null for any failure to extract a qname.
     */
    public QName resolveQName(String qname) {
        return resolveQName(qname, true);
    }

    public static void setNewElement(OMElement parent,
                                     OMElement myElement,
                                     OMElement newElement) {
        if (myElement != null) {
            myElement.discard();
        }
        parent.addChild(newElement);
    }

    /**
     * @deprecated please use OMElement.getFirstChildWithName(qname) instead!
     */
    public static OMElement getChildWithName(OMElement parent,
                                             String childName) {
        Iterator childrenIter = parent.getChildren();
        while (childrenIter.hasNext()) {
            OMNode node = (OMNode) childrenIter.next();
            if (node.getType() == OMNode.ELEMENT_NODE &&
                    childName.equals(((OMElement) node).getLocalName())) {
                return (OMElement) node;
            }
        }
        return null;
    }

    /**
     * @deprecated use {@link #getContentID(XMLStreamReader)} instead (see WSCOMMONS-429)
     */
    public static String getContentID(XMLStreamReader parser, String charsetEncoding) {
        return getContentID(parser);
    }

    public static String getContentID(XMLStreamReader parser) {
        if (parser.getAttributeCount() > 0 &&
                parser.getAttributeLocalName(0).equals("href")) {
            return getContentIDFromHref(parser.getAttributeValue(0));
        } else {
            throw new OMException(
                    "Href attribute not found in XOP:Include element");
        }
    }

    /**
     * Extract the content ID from a href attribute value, i.e. from a URI following the
     * cid: scheme defined by RFC2392.
     * 
     * @param href the value of the href attribute
     * @return the corresponding content ID
     */
    public static String getContentIDFromHref(String href) {
        return XOPUtils.getContentIDFromURL(href);
    }
    
    /**
     * Some times two OMElements needs to be added to the same object tree. But in Axiom, a single
     * tree should always contain object created from the same type of factory (eg:
     * LinkedListImplFactory, DOMFactory, etc.,). If one OMElement is created from a different
     * factory than that of the factory which was used to create the object in the existing tree, we
     * need to convert the new OMElement to match to the factory of existing object tree. This
     * method will convert omElement to the given omFactory.
     *
     * @see AttributeHelper#importOMAttribute(OMAttribute, OMElement) to convert instances of
     *      OMAttribute
     */
    public static OMElement importOMElement(OMElement omElement, OMFactory omFactory) {
        // first check whether the given OMElement has the same omFactory
        if (omElement.getOMFactory().getClass().isInstance(omFactory)) {
            return omElement;
        } else {
            OMElement documentElement = new StAXOMBuilder(omFactory, omElement.getXMLStreamReader())
                    .getDocumentElement();
            documentElement.build();
            return documentElement;
        }
    }

    /**
     * This is a method to convert regular OMElements to SOAPHeaderBlocks.
     * 
     * @param omElement
     * @param factory
     * @return TODO
     * @throws Exception
     */
    public static SOAPHeaderBlock toSOAPHeaderBlock(OMElement omElement, SOAPFactory factory) throws Exception {
        if (omElement instanceof SOAPHeaderBlock)
            return (SOAPHeaderBlock) omElement;
        
        QName name = omElement.getQName();
        String localName = name.getLocalPart();
        OMNamespace namespace = factory.createOMNamespace(name.getNamespaceURI(), name.getPrefix());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        omElement.serialize(baos);
        ByteArrayDataSource bads = new ByteArrayDataSource(baos.toByteArray(), "utf-8");
        SOAPHeaderBlock block = factory.createSOAPHeaderBlock(localName, namespace, bads);
        
        return block;
    }
    
    /**
     * Returns a stream representing the concatenation of the text nodes that are children of a
     * given element.
     * The stream returned by this method produces exactly the same character sequence as the
     * the stream created by the following expression:
     * <pre>new StringReader(element.getText())</pre>
     * The difference is that the stream implementation returned by this method is guaranteed
     * to have constant memory usage and is optimized for performance.
     * 
     * @param element the element to read the text nodes from
     * @param cache whether to enable caching when accessing the element
     * @return a stream representing the concatenation of the text nodes
     * 
     * @see OMElement#getText()
     */
    public static Reader getTextAsStream(OMElement element, boolean cache) {
        // If the element is not an OMSourcedElement and has not more than one child, then the most
        // efficient way to get the Reader is to build a StringReader
        if (!(element instanceof OMSourcedElement) && (!cache || element.isComplete())) {
            OMNode child = element.getFirstOMChild();
            if (child == null) {
                return new StringReader("");
            } else if (child.getNextOMSibling() == null) {
                return new StringReader(child instanceof OMText ? ((OMText)child).getText() : "");
            }
        }
        // In all other cases, extract the data from the XMLStreamReader
        try {
            XMLStreamReader reader = element.getXMLStreamReader(cache);
            if (reader.getEventType() == XMLStreamReader.START_DOCUMENT) {
                reader.next();
            }
            return XMLStreamReaderUtils.getElementTextAsStream(reader, true);
        } catch (XMLStreamException ex) {
            throw new OMException(ex);
        }
    }
    
    /**
     * Write the content of the text nodes that are children of a given element to a
     * {@link Writer}.
     * If <code>cache</code> is true, this method has the same effect as the following instruction:
     * <pre>out.write(element.getText())</pre>
     * The difference is that this method is guaranteed to have constant memory usage and is
     * optimized for performance.
     * 
     * @param element the element to read the text nodes from
     * @param out the stream to write the content to
     * @param cache whether to enable caching when accessing the element
     * @throws XMLStreamException if an error occurs when reading from the element
     * @throws IOException if an error occurs when writing to the stream
     * 
     * @see OMElement#getText()
     */
    public static void writeTextTo(OMElement element, Writer out, boolean cache)
            throws XMLStreamException, IOException {
        
        XMLStreamReader reader = element.getXMLStreamReader(cache);
        int depth = 0;
        while (reader.hasNext()) {
            switch (reader.next()) {
                case XMLStreamReader.CHARACTERS:
                case XMLStreamReader.CDATA:
                    if (depth == 1) {
                        out.write(reader.getText());
                    }
                    break;
                case XMLStreamReader.START_ELEMENT:
                    depth++;
                    break;
                case XMLStreamReader.END_ELEMENT:
                    depth--;
            }
        }
    }
}
