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

import org.apache.axiom.ext.stax.datahandler.DataHandlerReader;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMConstants;
import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.impl.OMContainerEx;
import org.apache.axiom.om.impl.OMNodeEx;
import org.apache.axiom.om.impl.util.OMSerializerUtil;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * OM should be able to be built from any data source. And the model it builds may be a SOAP
 * specific one or just an XML model. This class will give some common functionality of OM Building
 * from StAX.
 */
public abstract class StAXBuilder implements OMXMLParserWrapper {

    private static Log log = LogFactory.getLog(StAXBuilder.class);
    
    /** Field parser */
    protected XMLStreamReader parser;

    /** Field omfactory */
    protected OMFactory omfactory;

    /** Field lastNode */
    protected OMNode lastNode;

    // returns the state of completion

    /** Field done */
    protected boolean done = false;

    // keeps the state of the cache

    /** Field cache */
    protected boolean cache = true;

    // keeps the state of the parser access. if the parser is
    // accessed atleast once,this flag will be set

    /** Field parserAccessed */
    protected boolean parserAccessed = false;
    protected OMDocument document;

    protected String charEncoding = null;
    
    protected boolean _isClosed = false;              // Indicate if parser is closed
    protected boolean _releaseParserOnClose = false;  // Defaults to legacy behavior, which is keep the reference

    // Fields for Custom Builder implementation
    protected CustomBuilder customBuilderForPayload = null;
    protected Map customBuilders = null;
    protected int maxDepthForCustomBuilders = -1;
    
    /**
     * Reference to the {@link DataHandlerReader} extension of the parser, or <code>null</code> if
     * the parser doesn't support this extension.
     */
    protected DataHandlerReader dataHandlerReader;
    
    /**
     * Element level is the depth of the element. 
     * The root element (i.e. envelope) is defined as 1.
     */
    protected int elementLevel = 0;
    
    /**
     * Stores exceptions thrown by the parser. Used to avoid accessing the parser
     * again after is has thrown a parse exception.
     */
    protected Exception parserException;
    
    /**
     * Constructor StAXBuilder.
     * This constructor is used if the parser is at the beginning (START_DOCUMENT).
     *
     * @param ombuilderFactory
     * @param parser
     */
    protected StAXBuilder(OMFactory ombuilderFactory, XMLStreamReader parser) {
        omfactory = ombuilderFactory;
        
        // The getCharacterEncodingScheme and getEncoding information are 
        // only available at the START_DOCUMENT event.
        charEncoding = parser.getCharacterEncodingScheme();
        if(charEncoding == null){
            charEncoding = parser.getEncoding();
        }

        initParser(parser);
    }
    
    /**
     * Constructor StAXBuilder.
     * This constructor is used if the parser is not at the START_DOCUMENT.
     *
     * @param ombuilderFactory
     * @param parser
     * @param characterEncoding
     */
    protected StAXBuilder(OMFactory ombuilderFactory, 
                          XMLStreamReader parser, 
                          String characterEncoding) {
        omfactory = ombuilderFactory;
        charEncoding = characterEncoding;
        initParser(parser);
    }

    private void initParser(XMLStreamReader parser) {
        if (parser instanceof BuilderAwareReader) {
            ((BuilderAwareReader) parser).setBuilder(this);
        }
        dataHandlerReader = DataHandlerReaderUtils.getDataHandlerReader(parser);
        this.parser = parser;
    }

    /**
     * Constructor StAXBuilder.
     *
     * @param parser
     */
    protected StAXBuilder(XMLStreamReader parser) {
        this(OMAbstractFactory.getOMFactory(), parser);
    }

    /** Init() *must* be called after creating the builder using this constructor. */
    protected StAXBuilder() {
    }

    /**
     * @deprecated Not used anywhere
     */
    public void init(InputStream inputStream, String charSetEncoding, String url,
                     String contentType) throws OMException {
        try {
            this.parser = StAXUtils.createXMLStreamReader(inputStream);
        } catch (XMLStreamException e1) {
            throw new OMException(e1);
        }
        omfactory = OMAbstractFactory.getOMFactory();
    }

    /**
     * Method setOMBuilderFactory.
     *
     * @param ombuilderFactory
     */
    public void setOMBuilderFactory(OMFactory ombuilderFactory) {
        this.omfactory = ombuilderFactory;
    }

    /**
     * Method processNamespaceData.
     *
     * @param node
     */
    protected abstract void processNamespaceData(OMElement node);

    // since the behaviors are different when it comes to namespaces
    // this must be implemented differently

    /**
     * Method processAttributes.
     *
     * @param node
     */
    protected void processAttributes(OMElement node) {
        int attribCount = parser.getAttributeCount();
        for (int i = 0; i < attribCount; i++) {
            String uri = parser.getAttributeNamespace(i);
            String prefix = parser.getAttributePrefix(i);


            OMNamespace namespace = null;
            if (uri != null && !uri.isEmpty()) {

                // prefix being null means this elements has a default namespace or it has inherited
                // a default namespace from its parent
                namespace = node.findNamespace(uri, prefix);
                if (namespace == null) {
                    if (prefix == null || prefix.isEmpty()) {
                        prefix = OMSerializerUtil.getNextNSPrefix();
                    }
                    namespace = node.declareNamespace(uri, prefix);
                }
            }

            // todo if the attributes are supposed to namespace qualified all the time
            // todo then this should throw an exception here

            OMAttribute attr = node.addAttribute(parser.getAttributeLocalName(i),
                              parser.getAttributeValue(i), namespace);
            attr.setAttributeType(parser.getAttributeType(i));
            
        }
    }

    /**
     * Method createOMText.
     *
     * @return Returns OMNode.
     * @throws OMException
     */
    protected OMNode createOMText(int textType) throws OMException {
        OMNode node;
        if (lastNode == null) {
            return null;
        } else if (!lastNode.isComplete()) {
            node = createOMText((OMElement) lastNode, textType);
        } else {
            node = createOMText(lastNode.getParent(), textType);
        }
        return node;
    }

    /**
     * This method will check whether the text can be optimizable using IS_BINARY flag. If that is
     * set then we try to get the data handler.
     *
     * @param omContainer
     * @param textType
     * @return omNode
     */
    private OMNode createOMText(OMContainer omContainer, int textType) {
        if (dataHandlerReader != null && dataHandlerReader.isBinary()) {
            Object dataHandlerObject;
            if (dataHandlerReader.isDeferred()) {
                dataHandlerObject = dataHandlerReader.getDataHandlerProvider();
            } else {
                try {
                    dataHandlerObject = dataHandlerReader.getDataHandler();
                } catch (XMLStreamException ex) {
                    throw new OMException(ex);
                }
            }
            OMText text = omfactory.createOMText(dataHandlerObject, dataHandlerReader.isOptimized());
            String contentID = dataHandlerReader.getContentID();
            if (contentID != null) {
                text.setContentID(contentID);
            }
            omContainer.addChild(text);
            return text;
        } else {
            // Some parsers (like Woodstox) parse text nodes lazily and may throw a
            // RuntimeException in getText()
            String text;
            try {
                text = parser.getText();
            } catch (RuntimeException ex) {
                parserException = ex;
                throw ex;
            }
            return omfactory.createOMText(omContainer, text, textType);
        }
    }

    /**
     * Method reset.
     *
     * @param node
     * @throws OMException
     */
    public void reset(OMNode node) throws OMException {
        lastNode = null;
    }

    /**
     * Method discard.
     *
     * @param element
     * @throws OMException
     */
    public void discard(OMElement element) throws OMException {

        if (element.isComplete() || !cache) {
            throw new OMException();
        }
        try {

            // We simply cannot use the parser instance from the builder for this case
            // it is not safe to assume that the parser inside the builder will be in
            // sync with the parser of the element in question
            // Note 1 - however  calling getXMLStreamReaderWithoutCaching sets off two flags
            // the cache flag for this builder and the parserAccessed flag. These flags will be
            // reset later in this procedure

            int event =0;
            XMLStreamReader elementParser = element.getXMLStreamReaderWithoutCaching();
            do{
               event = elementParser.next();
            }while(!(event == XMLStreamConstants.END_ELEMENT &&
                     element.getLocalName().equals(elementParser.getLocalName())));

            //at this point we are safely at the end_element event of the element we discarded
            lastNode = element.getPreviousOMSibling();

            // resetting the flags - see Note 1 above
            cache = true;
            parserAccessed = false;
            
            if (lastNode != null) {
                // if the last node is not an element, we are in trouble because leaf nodes
                // (such as text) cannot build themselves. worst the lastchild of the
                // currentparent is still the removed node! we have to correct it
                OMContainerEx ex = ((OMContainerEx) lastNode.getParent());
                ex.setLastChild(lastNode);
                 if (!(lastNode instanceof OMContainerEx)){
                     ex.buildNext();
                 }else{
                    ((OMNodeEx) lastNode).setNextOMSibling(null); 
                 }

            } else {
                OMElement parent = (OMElement) element.getParent();
                if (parent == null) {
                    throw new OMException();
                }
                ((OMContainerEx) parent).setFirstChild(null);
                lastNode = parent;
            }
            
        } catch (OMException e) {
            throw e;
        } catch (Exception e) {
            throw new OMException(e);
        } 
        // when an element is discarded the element index that was incremented
        //at creation needs to be decremented !
        elementLevel--;
    }

    /**
     * Method getText.
     *
     * @return Returns String.
     * @throws OMException
     */
    public String getText() throws OMException {
        return parser.getText();
    }

    /**
     * Method getNamespace.
     *
     * @return Returns String.
     * @throws OMException
     */
    public String getNamespace() throws OMException {
        return parser.getNamespaceURI();
    }

    /**
     * Method getNamespaceCount.
     *
     * @return Returns int.
     * @throws OMException
     */
    public int getNamespaceCount() throws OMException {
        try {
            return parser.getNamespaceCount();
        } catch (Exception e) {
            throw new OMException(e);
        }
    }

    /**
     * Method getNamespacePrefix.
     *
     * @param index
     * @return Returns String.
     * @throws OMException
     */
    public String getNamespacePrefix(int index) throws OMException {
        try {
            return parser.getNamespacePrefix(index);
        } catch (Exception e) {
            throw new OMException(e);
        }
    }

    /**
     * Method getNamespaceUri.
     *
     * @param index
     * @return Returns String.
     * @throws OMException
     */
    public String getNamespaceUri(int index) throws OMException {
        try {
            return parser.getNamespaceURI(index);
        } catch (Exception e) {
            throw new OMException(e);
        }
    }

    /**
     * Method setCache.
     *
     * @param b
     */
    public void setCache(boolean b) {
        if (parserAccessed && b) {
            throw new UnsupportedOperationException(
                    "parser accessed. cannot set cache");
        }
        cache = b;
    }
    
    /**
     * @return true if caching
     */
    public boolean isCache() {
        return cache;
    }

    /**
     * Method getName.
     *
     * @return Returns String.
     * @throws OMException
     */
    public String getName() throws OMException {
        return parser.getLocalName();
    }

    /**
     * Method getPrefix.
     *
     * @return Returns String.
     * @throws OMException
     */
    public String getPrefix() throws OMException {
        return parser.getPrefix();
    }

    /**
     * Method getAttributeCount.
     *
     * @return Returns int.
     * @throws OMException
     */
    public int getAttributeCount() throws OMException {
        return parser.getAttributeCount();
    }

    /**
     * Method getAttributeNamespace.
     *
     * @param arg
     * @return Returns String.
     * @throws OMException
     */
    public String getAttributeNamespace(int arg) throws OMException {
        return parser.getAttributeNamespace(arg);
    }

    /**
     * Method getAttributeName.
     *
     * @param arg
     * @return Returns String.
     * @throws OMException
     */
    public String getAttributeName(int arg) throws OMException {
        return parser.getAttributeNamespace(arg);
    }

    /**
     * Method getAttributePrefix.
     *
     * @param arg
     * @return Returns String.
     * @throws OMException
     */
    public String getAttributePrefix(int arg) throws OMException {
        return parser.getAttributeNamespace(arg);
    }

    /**
     * Get the underlying {@link XMLStreamReader} used by this builder. Note that for this type of
     * builder, accessing the underlying parser implies that can no longer be used, and any attempt
     * to call {@link #next()} will result in an exception.
     * 
     * @return The {@link XMLStreamReader} object used by this builder. Note that the constraints
     *         described in the Javadoc of the <code>reader</code> parameter of the
     *         {@link CustomBuilder#create(String, String, OMContainer, XMLStreamReader, OMFactory)}
     *         method also apply to the stream reader returned by this method, i.e.:
     *         <ul>
     *         <li>The caller should use
     *         {@link org.apache.axiom.util.stax.xop.XOPUtils#getXOPEncodedStream(XMLStreamReader)}
     *         to get an XOP encoded stream from the return value.
     *         <li>To get access to the bare StAX parser implementation, the caller should use
     *         {@link org.apache.axiom.util.stax.XMLStreamReaderUtils#getOriginalXMLStreamReader(XMLStreamReader)}.
     *         </ul>
     * @throws IllegalStateException
     *             if the parser has already been accessed
     */
    public Object getParser() {
        if (parserAccessed) {
            throw new IllegalStateException(
                    "Parser already accessed!");
        }
        if (!cache) {
            parserAccessed = true;
            return parser;
        } else {
            throw new IllegalStateException(
                    "cache must be switched off to access the parser");
        }
    }

    /**
     * Method isCompleted.
     *
     * @return Returns boolean.
     */
    public boolean isCompleted() {
        return done;
    }

    /**
     * This method is called with the XMLStreamConstants.START_ELEMENT event.
     *
     * @return Returns OMNode.
     * @throws OMException
     */
    protected abstract OMNode createOMElement() throws OMException;

    /**
     * Forwards the parser one step further, if parser is not completed yet. If this is called after
     * parser is done, then throw an OMException. If the cache is set to false, then returns the
     * event, *without* building the OM tree. If the cache is set to true, then handles all the
     * events within this, and builds the object structure appropriately and returns the event.
     *
     * @return Returns int.
     * @throws OMException
     */
    public abstract int next() throws OMException;
    
    /**
     * Register a CustomBuilder associated with the indicated QName.
     * The CustomBuilder will be used when an element of that qname is encountered.
     * @param qName
     * @param maxDepth indicate the maximum depth that this qname will be found. (root = 0)
     * @param customBuilder
     * @return replaced CustomBuilder or null
     */
    public CustomBuilder registerCustomBuilder(QName qName, int maxDepth, CustomBuilder customBuilder) {
        CustomBuilder old = null;
        if (customBuilders == null) {
            customBuilders = new HashMap();
        } else {
            old = (CustomBuilder) customBuilders.get(qName);
        }
        maxDepthForCustomBuilders = 
                (maxDepthForCustomBuilders > maxDepth) ?
                        maxDepthForCustomBuilders: maxDepth;
        customBuilders.put(qName, customBuilder);
        return old;
    }
    
    
    /**
     * Register a CustomBuilder for a payload.
     * The payload is defined as the elements inside a SOAPBody or the 
     * document element of a REST message.
     * @param customBuilder
     * @return replaced CustomBuilder or null
     */
    public CustomBuilder registerCustomBuilderForPayload(CustomBuilder customBuilder) {
        CustomBuilder old = null;
        this.customBuilderForPayload = customBuilder;
        return old;
    }
    
    /**
     * Return CustomBuilder associated with the namespace/localPart
     * @param namespace
     * @param localPart
     * @return CustomBuilder or null
     */ 
    protected CustomBuilder getCustomBuilder(String namespace, String localPart) {
        if (customBuilders == null) {
            return null;
        }
        QName qName = new QName(namespace, localPart);
        return (CustomBuilder) customBuilders.get(qName);
    }

    /** @return Returns short. */
    public short getBuilderType() {
        return OMConstants.PULL_TYPE_BUILDER;
    }

    /**
     * Method registerExternalContentHandler.
     *
     * @param obj
     */
    public void registerExternalContentHandler(Object obj) {
        throw new UnsupportedOperationException();
    }

    /**
     * Method getRegisteredContentHandler.
     *
     * @return Returns Object.
     */
    public Object getRegisteredContentHandler() {
        throw new UnsupportedOperationException();
    }

    public OMDocument getDocument() {
        return document;
    }

    public String getCharsetEncoding() {
        return document.getCharsetEncoding();
    }

    public OMNode getLastNode() {
        return this.lastNode;
    }

    public void close() {
        try {
            if (!isClosed()) {
                parser.close();
            }
        } catch (Throwable e) {
            // Can't see a reason why we would want to surface an exception
            // while closing the parser.
            if (log.isDebugEnabled()) {
                log.debug("Exception occurred during parser close.  " +
                                "Processing continues. " + e);
            }
        } finally {
            _isClosed = true;
            done = true;
            // Release the parser so that it can be GC'd or reused.
            if (_releaseParserOnClose) {
                parser = null;
            }
        }
    }

    /**
     * Get the value of a feature/property from the underlying XMLStreamReader implementation
     * without accessing the XMLStreamReader. https://issues.apache.org/jira/browse/WSCOMMONS-155
     *
     * @param name
     * @return TODO
     */
    public Object getReaderProperty(String name) throws IllegalArgumentException {
        if (!isClosed()) {
            return parser.getProperty(name);
        } 
        return null;
    }

    /**
     * Returns the encoding style of the XML data
     * @return the character encoding, defaults to "UTF-8"
     */
    public String getCharacterEncoding() {
        if(this.charEncoding == null){
            return "UTF-8";
        }
        return this.charEncoding;
    }
    
    
    /**
     * @return if parser is closed
     */
    public boolean isClosed() {
        return _isClosed;
    }
    
    /**
     * Indicate if the parser resource should be release when closed.
     * @param value boolean
     */
    public void releaseParserOnClose(boolean value) {
        
        // Release parser if already closed
        if (isClosed() && value) {
            parser = null;
        }
        _releaseParserOnClose = value;
        
    }
}
