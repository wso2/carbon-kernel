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

package org.apache.axiom.om.impl.builder;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMHierarchyException;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.impl.OMContainerEx;
import org.apache.axiom.om.impl.OMNodeEx;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.util.stax.XMLEventUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.Location;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;


/**
 * StAX based builder that produces a pure XML infoset compliant object model.
 * <p>
 * This class supports the {@link XMLStreamReader} extension defined by
 * {@link org.apache.axiom.ext.stax.datahandler.DataHandlerReader} as well as the legacy extension mechanism
 * defined in the documentation of {@link org.apache.axiom.util.stax.XMLStreamReaderUtils}.
 * <h3>Error handling</h3>
 * Usually, code that uses StAX directly just stops processing of an XML document
 * once the first parsing error has been reported. However, since Axiom
 * uses deferred parsing, and client code accesses the XML infoset using
 * an object model, things are more complicated. Indeed, if the XML
 * document is not well formed, the corresponding error might be reported
 * as a runtime exception by any call to a method of an OM node.
 * <p>
 * Typically the client code will have some error handling that will intercept
 * runtime exceptions and take appropriate action. Very often this error handling
 * code might want to access the object model again, for example to log the request that caused the
 * failure. This causes no problem except if the runtime exception was caused by a
 * parsing error, in which case Axiom would again try to pull events from the parser.
 * <p>
 * This would lead to a situation where Axiom accesses a parser that has reported a parsing
 * error before. While one would expect that after a first error reported by the parser, all
 * subsequent invocations of the parser will fail, this is not the case for all parsers
 * (at least not in all situations). Instead, the parser might be left in an inconsistent
 * state after the error. E.g. WSCOMMONS-372 describes a case where Woodstox
 * encounters an error in {@link XMLStreamReader#getText()} but continues to return
 * (incorrect) events afterwards. The explanation for this behaviour might be that
 * the situation described here is quite uncommon when StAX is used directly (i.e. not through
 * Axiom).
 * <p>
 * To avoid this, the builder remembers exceptions thrown by the parser and rethrows
 * them during a call to {@link #next()}.
 */
public class StAXOMBuilder extends StAXBuilder {
    /** Field document */

    private static final Log log = LogFactory.getLog(StAXOMBuilder.class);
    private boolean doTrace = log.isDebugEnabled();
    private static int nsCount = 0;

    // namespaceURI interning
    // default is false because most XMLStreamReader implementations don't do interning
    // due to performance impacts.  Thus a customer should not assume that a namespace
    // on an OMElement is interned.
    boolean namespaceURIInterning = false;
    
    int lookAheadToken = -1;
    
    /**
     * Constructor StAXOMBuilder.
     *
     * @param ombuilderFactory
     * @param parser
     */
    public StAXOMBuilder(OMFactory ombuilderFactory, XMLStreamReader parser) {
        super(ombuilderFactory, parser);
        document = createDocument();
    }

    /**
     * Constructor linked to existing element.
     *
     * @param factory
     * @param parser
     * @param element
     * @param characterEncoding of existing element
     */
    public StAXOMBuilder(OMFactory factory, 
                         XMLStreamReader parser, 
                         OMElement element, 
                         String characterEncoding) {
        // Use this constructor because the parser is passed the START_DOCUMENT state.
        super(factory, parser, characterEncoding);  
        doTrace = log.isDebugEnabled();
        namespaceURIInterning = false;
        lookAheadToken = -1;
        document = createDocument();
        lastNode = element;
        document.setOMDocumentElement(element);
        populateOMElement(element);
    }
    
    /**
     * Constructor linked to existing element.
     *
     * @param factory
     * @param parser
     * @param element
     */
    public StAXOMBuilder(OMFactory factory, XMLStreamReader parser, OMElement element) {
        this(factory, parser, element, null);
    }

    /**
     * @param filePath - Path to the XML file
     * @throws XMLStreamException
     * @throws FileNotFoundException
     */
    public StAXOMBuilder(String filePath) throws XMLStreamException, FileNotFoundException {
        this(StAXUtils.createXMLStreamReader(new FileInputStream(filePath)));
    }

    /**
     * Constructor StAXOMBuilder.
     *
     * @param parser
     */
    public StAXOMBuilder(XMLStreamReader parser) {
        super(parser);
        doTrace = log.isDebugEnabled();
        namespaceURIInterning = false;
        lookAheadToken = -1;
        omfactory = OMAbstractFactory.getOMFactory();
        document = createDocument();
    }

    /**
     * @param inStream - instream which contains the XML
     * @throws XMLStreamException
     */
    public StAXOMBuilder(InputStream inStream) throws XMLStreamException {
        this(StAXUtils.createXMLStreamReader(inStream));
    }

    /**
     * Init() *must* be called after creating the builder using this constructor.
     */
    public StAXOMBuilder() {
        super();
        doTrace = log.isDebugEnabled();
        namespaceURIInterning = false;
        lookAheadToken = -1;
    }

    private OMDocument createDocument() {
        OMDocument document = omfactory.createOMDocument(this);
        if (charEncoding != null) {
            document.setCharsetEncoding(charEncoding);
        }
        if (parser.getEventType() == XMLStreamConstants.START_DOCUMENT) {
            document.setXMLVersion(parser.getVersion());
            document.setStandalone(parser.isStandalone() ? "yes" : "no");
        } else {
            // We allow creating a StAXOMWrapper from a parser in state START_ELEMENT. In that
            // case, we must not call getVersion or isStandalone since this is forbidden by the
            // StAX specs. Set some reasonable defaults.
            document.setXMLVersion("1.0");
            document.setStandalone("yes");
        }
        return document;
    }

    /**
     * Method next.
     *
     * @return Returns int.
     * @throws OMException
     */
    public int next() throws OMException {
        try {
            // We need a loop here because we may decide to skip an event
            while (true) {
                if (done) {
                    throw new OMException();
                }
                int token = parserNext();
                if (!cache) {
                    return token;
                }
               
                // The current token should be the same as the 
                // one just obtained.  This bit of code is used to 
                // detect invalid parser state.
                if (doTrace) {
                    int currentParserToken = parser.getEventType();
                    if (currentParserToken != token) {
    
    
                        log.debug("WARNING: The current state of the parser is not equal to the " +
                                  "state just received from the parser. The current state in the paser is " +
                                  XMLEventUtils.getEventTypeString(currentParserToken) + " the state just received is " +
                                  XMLEventUtils.getEventTypeString(token));
    
                        /*
                          throw new OMException("The current token " + token + 
                                         " does not match the current event " +
                                         "reported by the parser token.  The parser did not update its state correctly.  " +
                                         "The parser is " + parser);
                         */
                    }
                }
                
                // Now log the current state of the parser
                if (doTrace) {
                    logParserState();
                }
               
                switch (token) {
                    case XMLStreamConstants.START_ELEMENT:
                        elementLevel++;
                        lastNode = createNextOMElement();
                        break;
                    case XMLStreamConstants.CHARACTERS:
                        lastNode = createOMText(XMLStreamConstants.CHARACTERS);
                        break;
                    case XMLStreamConstants.CDATA:
                        lastNode = createOMText(XMLStreamConstants.CDATA);
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        endElement();
                        elementLevel--;
                        break;
                    case XMLStreamConstants.END_DOCUMENT:
                        done = true;
                        ((OMContainerEx) this.document).setComplete(true);
                        break;
                    case XMLStreamConstants.SPACE:
                        try {
                            lastNode = createOMText(XMLStreamConstants.SPACE);
                            if (lastNode == null) {
                                continue;
                            }
                        } catch (OMHierarchyException ex) {
                            // The OM implementation doesn't allow text nodes at the current
                            // position in the tree. Since it is only whitespace, we can safely
                            // skip this event.
                            continue;
                        }
                        break;
                    case XMLStreamConstants.COMMENT:
                        lastNode = createComment();
                        break;
                    case XMLStreamConstants.DTD:
                        createDTD();
                        break;
                    case XMLStreamConstants.PROCESSING_INSTRUCTION:
                        lastNode = createPI();
                        break;
                    case XMLStreamConstants.ENTITY_REFERENCE:
                        lastNode = createOMText(XMLStreamConstants.ENTITY_REFERENCE);
                        break;
                    default :
                        throw new OMException();
                }
                return token;
            }
        } catch (XMLStreamException e) {
            throw new OMException(e);
        }
    }
    
    /**
     * Creates a new OMElement using either a CustomBuilder or 
     * the default Builder mechanism.
     * @return TODO
     */
    protected OMNode createNextOMElement() {
        OMNode newElement = null;
        if (elementLevel == 1 && this.customBuilderForPayload != null) {
            newElement = createWithCustomBuilder(customBuilderForPayload,  omfactory);
        } else if (customBuilders != null && elementLevel <= this.maxDepthForCustomBuilders) {
            String namespace = parser.getNamespaceURI();
            String localPart = parser.getLocalName();
            CustomBuilder customBuilder = getCustomBuilder(namespace, localPart);
            if (customBuilder != null) {
                newElement = createWithCustomBuilder(customBuilder, omfactory);
            }
        }
        if (newElement == null) {
            newElement = createOMElement();
        } else {
            elementLevel--; // Decrease level since custom builder read the end element event
        }
        return newElement;
    }
    
    protected OMNode createWithCustomBuilder(CustomBuilder customBuilder, OMFactory factory) {
        
        String namespace = parser.getNamespaceURI();
        String localPart = parser.getLocalName();
        
        if (log.isDebugEnabled()) {
            log.debug("Invoking CustomBuilder, " + customBuilder.toString() + 
                      ", to the OMNode for {" + namespace + "}" + localPart);
        }
        OMContainer parent = null;
        if (lastNode != null) {
            if (lastNode.isComplete()) {
                parent = lastNode.getParent();
            } else {
                parent = (OMContainer)lastNode;
            }
        } else {
            parent = document;
        }
        OMNode node = customBuilder.create(namespace, localPart, parent, parser, factory);
        if (log.isDebugEnabled()) {
            if (node != null) {
                log.debug("The CustomBuilder, " + customBuilder.toString() + 
                          "successfully constructed the OMNode for {" + namespace + "}" + localPart);
            } else {
                log.debug("The CustomBuilder, " + customBuilder.toString() + 
                          " did not construct an OMNode for {" + namespace + "}" + localPart +
                          ". The OMNode will be constructed using the installed stax om builder");
            }
            log.debug("The current state of the parser is: ");
            logParserState();
        }
        return node;
    }
    
    /**
     * Dump the current event of the parser.
     */
    protected void logParserState() {
        if (doTrace) {
            int currentEvent = parser.getEventType();
            
            switch (currentEvent) {
            case XMLStreamConstants.START_ELEMENT:
                log.trace("START_ELEMENT: ");
                log.trace("  QName: " + parser.getName());
                break;
            case XMLStreamConstants.START_DOCUMENT:
                log.trace("START_DOCUMENT: ");
                break;
            case XMLStreamConstants.CHARACTERS:
                log.trace("CHARACTERS: ");
                // This can bust up a datahandler
                //log.trace(   "[" + parser.getText() + "]");
                break;
            case XMLStreamConstants.CDATA:
                log.trace("CDATA: ");
                // This can but
                //log.trace(   "[" + parser.getText() + "]");
                break;
            case XMLStreamConstants.END_ELEMENT:
                log.trace("END_ELEMENT: ");
                log.trace("  QName: " + parser.getName());
                break;
            case XMLStreamConstants.END_DOCUMENT:
                log.trace("END_DOCUMENT: ");
                break;
            case XMLStreamConstants.SPACE:
                log.trace("SPACE: ");
                //log.trace(   "[" + parser.getText() + "]");
                break;
            case XMLStreamConstants.COMMENT:
                log.trace("COMMENT: ");
                //log.trace(   "[" + parser.getText() + "]");
                break;
            case XMLStreamConstants.DTD:
                log.trace("DTD: ");
                log.trace(   "[" + parser.getText() + "]");
                break;
            case XMLStreamConstants.PROCESSING_INSTRUCTION:
                log.trace("PROCESSING_INSTRUCTION: ");
                log.trace("   [" + parser.getPITarget() + "][" +
                            parser.getPIData() + "]");
                break;
            case XMLStreamConstants.ENTITY_REFERENCE:
                log.trace("ENTITY_REFERENCE: ");
                log.trace("    " + parser.getLocalName() + "[" +
                            parser.getText() + "]");
                break;
            default :
                log.trace("UNKNOWN_STATE: " + currentEvent);
            
            }
        }
    }

    /**
     * Populate element with data from parser START_ELEMENT event. This is used when the source of
     * data for an element needs to be parsed on demand. The supplied element must already be set to
     * the proper name and namespace.
     *
     * @param node element to be populated
     */
    private void populateOMElement(OMElement node) {
        // create the namespaces
        processNamespaceData(node);
        // fill in the attributes
        processAttributes(node);
        Location location = parser.getLocation();
        if(location != null) {
            node.setLineNumber(location.getLineNumber());
        }
    }

    /**
     * Method createOMElement.
     *
     * @return Returns OMNode.
     * @throws OMException
     */
    protected OMNode createOMElement() throws OMException {
        OMElement node;
        String elementName = parser.getLocalName();
        if (lastNode == null) {
            node = omfactory.createOMElement(elementName, null, document, this);
        } else if (lastNode.isComplete()) {
            node = omfactory.createOMElement(elementName, null,
                                             lastNode.getParent(), this);
            ((OMNodeEx) lastNode).setNextOMSibling(node);
            ((OMNodeEx) node).setPreviousOMSibling(lastNode);
        } else {
            OMContainerEx e = (OMContainerEx) lastNode;
            node = omfactory.createOMElement(elementName, null,
                                             (OMElement) lastNode, this);
            e.setFirstChild(node);
        }
        populateOMElement(node);
        return node;
    }

    /**
     * Method createOMText.
     *
     * @return Returns OMNode.
     * @throws OMException
     */
    protected OMNode createComment() throws OMException {
        OMNode node;
        if (lastNode == null) {
            node = omfactory.createOMComment(document, parser.getText());
        } else if (lastNode.isComplete()) {
            node = omfactory.createOMComment(lastNode.getParent(), parser.getText());
        } else {
            node = omfactory.createOMComment((OMElement) lastNode, parser.getText());
        }
        return node;
    }

    /**
     * Method createDTD.
     *
     * @return Returns OMNode.
     * @throws OMException
     */
    protected OMNode createDTD() throws OMException {
        if (!parser.hasText()) {
            return null;
        }
        String dtdText = getDTDText();
        lastNode = omfactory.createOMDocType(document, dtdText);
        return lastNode;
    }
    
    /**
     * The getText() method for a DOCTYPE returns the 
     * subset of the DOCTYPE (not the direct infoset).
     * This may force the parser to get information from 
     * the network.
     * @return doctype subset
     * @throws OMException
     */
    private String getDTDText() throws OMException { 
        String text = null;
        try {
            text = parser.getText();
        } catch (RuntimeException e) {
            // Woodstox (and perhaps other parsers)
            // attempts to load the external subset even if
            // external enties is false.  So ignore this error
            // if external entity support is explicitly disabled.
            Boolean b = (Boolean) parser.getProperty(
                   XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES);
            if (b == null || b == Boolean.TRUE) {
                throw e;
            }
            if (log.isDebugEnabled()) {
                log.debug("An exception occurred while calling getText() for a DOCTYPE.  " +
                                "The exception is ignored because external " +
                                "entites support is disabled.  " +
                                "The ignored exception is " + e);
            }
        }
        return text;
    }

    /**
     * Method createPI.
     *
     * @return Returns OMNode.
     * @throws OMException
     */
    protected OMNode createPI() throws OMException {
        OMNode node;
        String target = parser.getPITarget();
        String data = parser.getPIData();
        if (lastNode == null) {
            node = omfactory.createOMProcessingInstruction(document, target, data);
        } else if (lastNode.isComplete()) {
            node = omfactory.createOMProcessingInstruction(lastNode.getParent(), target, data);
        } else if (lastNode.getType() == OMNode.TEXT_NODE) {
            node = omfactory.createOMProcessingInstruction(lastNode.getParent(), target, data);
        } else {
            node = omfactory.createOMProcessingInstruction((OMContainer) lastNode, target, data);
        }
        return node;
    }

    protected void endElement() {
        if (lastNode.isComplete()) {
            OMNodeEx parent = (OMNodeEx) lastNode.getParent();
            parent.setComplete(true);
            lastNode = parent;
        } else {
            OMNodeEx e = (OMNodeEx) lastNode;
            e.setComplete(true);
        }

        //return lastNode;
    }

    public OMElement getDocumentElement() {
        return getDocumentElement(false);
    }

    public OMElement getDocumentElement(boolean discardDocument) {
        OMElement element = document.getOMDocumentElement();
        if (discardDocument) {
            OMNodeEx nodeEx = (OMNodeEx)element;
            nodeEx.setParent(null);
            nodeEx.setPreviousOMSibling(null);
            nodeEx.setNextOMSibling(null);
        }
        return element;
    }

    /**
     * Method processNamespaceData.
     *
     * @param node
     */
    protected void processNamespaceData(OMElement node) {
        // set the own namespace
        String namespaceURI = parser.getNamespaceURI();
        String prefix = parser.getPrefix();


        int namespaceCount = parser.getNamespaceCount();
        for (int i = 0; i < namespaceCount; i++) {
            String nsprefix = parser.getNamespacePrefix(i);

            //if the namespace is not defined already when we write the start tag declare it
            // check whether this is the default namespace and make sure we have not declared that earlier
            String namespaceURIFromParser = parser.getNamespaceURI(i);
            if (nsprefix == null || nsprefix.isEmpty()) {
                String nsuri = parser.getNamespaceURI(i);
                node.declareDefaultNamespace(nsuri == null ? "" : nsuri);
            } else {
                // NOTE_A:
                // By default most parsers don't intern the namespace.
                // Unfortunately the property to detect interning on the delegate parsers is hard to detect.
                // Woodstox has a proprietary property on the XMLInputFactory.
                // IBM has a proprietary property on the XMLStreamReader.
                // For now only force the interning if requested.
                if (isNamespaceURIInterning()) {
                    namespaceURIFromParser = namespaceURIFromParser.intern();
                }
                node.declareNamespace(namespaceURIFromParser, nsprefix);
            }
        }

        if (namespaceURI != null && !namespaceURI.isEmpty()) {
            OMNamespace namespace = node.findNamespaceURI(prefix == null ? "" : prefix);
            if (namespace == null || !namespace.getNamespaceURI().equals(namespaceURI)) {
                // See NOTE_A above
                if (isNamespaceURIInterning()) {
                    namespaceURI = namespaceURI.intern();
                }
                if (prefix == null || prefix.isEmpty()) {
                    namespace = node.declareDefaultNamespace(namespaceURI);
                } else {
                    namespace = node.declareNamespace(namespaceURI, prefix);
                }
            }
            node.setNamespaceWithNoFindInCurrentScope(namespace);
        }
    }

    /**
     * @param doDebug
     * @deprecated
     */
    public void setDoDebug(boolean doDebug) {
        this.doTrace = doDebug;
    }

    protected String createPrefix() {
        return "ns" + nsCount++;
    }

    /**
     * Set namespace uri interning
     * @param b
     */
    public void setNamespaceURIInterning(boolean b) {
        this.namespaceURIInterning = b;
    }
    
    /**
     * @return if namespace uri interning 
     */
    public boolean isNamespaceURIInterning() {
        return this.namespaceURIInterning;
    }
    
    /**
     * Pushes the virtual parser ahead one token.
     * If a look ahead token was calculated it is returned.
     * @return next token
     * @throws XMLStreamException
     */
    private int parserNext() throws XMLStreamException {
        if (lookAheadToken >= 0) {
            int token = lookAheadToken;
            lookAheadToken = -1; // Reset
            return token;
        } else {
            if (parserException != null) {
                log.warn("Attempt to access a parser that has thrown a parse exception before; " +
                		"rethrowing the original exception.");
                if (parserException instanceof XMLStreamException) {
                    throw (XMLStreamException)parserException;
                } else {
                    throw (RuntimeException)parserException;
                }
            }
            try {
                return parser.next();
            } catch (XMLStreamException ex) {
                parserException = ex;
                throw ex;
            }
        }
    }
    
    /**
     * This method looks ahead to the next start element.
     * @return true if successful
     */
    public boolean lookahead()  {
        try {
            while (true) {
                if (lookAheadToken < 0) {
                    lookAheadToken = parserNext();
                }
                if (lookAheadToken == XMLStreamConstants.START_ELEMENT) {
                    return true;
                } else if (lookAheadToken == XMLStreamConstants.END_ELEMENT ||
                        lookAheadToken == XMLStreamConstants.START_DOCUMENT ||
                        lookAheadToken == XMLStreamConstants.END_DOCUMENT) {
                    next();
                    return false;  // leaving scope...start element not found
                } else {
                    next();  // continue looking past whitespace etc.
                }
            }
        } catch (XMLStreamException e) {
            throw new OMException(e);
        }
    }
    
    /**
     * Check if the node for the current token has already been created or if the parser is ahead
     * of the builder.
     * 
     * @return A return value of <code>true</code> indicates that the parser is one token ahead
     *         of the builder, i.e. that the node for the current token has not been created yet.
     *         In this case {@link #getLastNode()} returns the node corresponding to the previous
     *         token. This state can only be reached by a call to {@link #lookahead()}, and the
     *         current token is always a {@link XMLStreamConstants#START_ELEMENT START_ELEMENT}.
     *         The information related to that element can be obtained by calls to
     *         {@link #getName()}, {@link #getNamespace()}, {@link #getPrefix()},
     *         {@link #getAttributeCount()}, {@link #getAttributeName(int)},
     *         {@link #getAttributeNamespace(int)}, {@link #getAttributePrefix(int)},
     *         {@link #getNamespaceCount()}, {@link #getNamespacePrefix(int)} and
     *         {@link #getNamespaceUri(int)}.
     *         <p>
     *         A return value of <code>false</code> indicates that the node corresponding to the
     *         current token hold by the parser has already been created, i.e.
     *         {@link #getLastNode()} returns the node corresponding to the current token.
     */
    public boolean isLookahead() {
        return lookAheadToken >= 0;
    }
}
