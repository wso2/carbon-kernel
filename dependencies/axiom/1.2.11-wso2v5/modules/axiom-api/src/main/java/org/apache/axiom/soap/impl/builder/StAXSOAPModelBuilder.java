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

package org.apache.axiom.soap.impl.builder;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMMetaFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.OMContainerEx;
import org.apache.axiom.om.impl.OMNodeEx;
import org.apache.axiom.om.impl.builder.CustomBuilder;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPConstants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPMessage;
import org.apache.axiom.soap.SOAPProcessingException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.XMLStreamReader;

/**
 * StAX based builder that produces a SOAP infoset model.
 * It builds SOAP specific objects such as {@link SOAPEnvelope}, {@link SOAPHeader},
 * {@link org.apache.axiom.soap.SOAPHeaderBlock} and {@link SOAPBody}.
 * <p>
 * This builder offers two different ways to handle SOAP versions:
 * <ul>
 *   <li>Either the SOAP version is specified when the builder is constructed. If the specified
 *       version doesn't match the envelope namespace of the actual message, an exception is
 *       triggered. This approach should be used when the SOAP version is known from information
 *       other than the content of the message. For example, in the HTTP case it is possible
 *       to identify the SOAP version based on the <tt>Content-Type</tt> header.</li>
 *   <li>If no SOAP version is specified, the builder will automatically detect it from the
 *       envelope namespace. It will then build the object model using the
 *       {@link SOAPFactory} implementation corresponding to that SOAP version.</li>
 * </ul>
 * In both cases, the {@link SOAPFactory} is retrieved either from the {@link OMMetaFactory}
 * specified when the builder is constructed, or if none is specified, from the default
 * meta factory returned by {@link OMAbstractFactory#getMetaFactory()}.
 */
public class StAXSOAPModelBuilder extends StAXOMBuilder {

    SOAPMessage soapMessage;
    /** Field envelope */
    private SOAPEnvelope envelope;
    private OMNamespace envelopeNamespace;
    private String namespaceURI;

    /**
     * The meta factory used to get the SOAPFactory implementation when SOAP version detection
     * is enabled. This is only used if <code>soapFactory</code> is <code>null</code>.
     */
    private OMMetaFactory metaFactory;

    private SOAPFactory soapFactory;

    /** Field headerPresent */
    private boolean headerPresent = false;

    /** Field bodyPresent */
    private boolean bodyPresent = false;

    /** Field log */
    private static final Log log = LogFactory.getLog(StAXSOAPModelBuilder.class);

    private boolean processingFault = false;


    //added
    /* This is used to indicate whether detail element is processing in soap 1.2 builderhelper
    */
    private boolean processingDetailElements = false;

    private SOAPBuilderHelper builderHelper;

    private String parserVersion = null;
    private static final boolean isDebugEnabled = log.isDebugEnabled();
    
    /**
     * Constructor.
     *
     * @param parser the parser to read the SOAP message from
     * @param soapVersion the namespace URI corresponding to the expected SOAP version
     *                    of the message
     */
    public StAXSOAPModelBuilder(XMLStreamReader parser, String soapVersion) {
        this(OMAbstractFactory.getMetaFactory(), parser, soapVersion);
    }

    /**
     * Constructor.
     *
     * @param metaFactory the meta factory used to get the appropriate {@link SOAPFactory}
     *                    implementation
     * @param parser the parser to read the SOAP message from
     * @param soapVersion the namespace URI corresponding to the expected SOAP version
     *                    of the message
     */
    public StAXSOAPModelBuilder(OMMetaFactory metaFactory, XMLStreamReader parser,
            String soapVersion) {
        super(metaFactory.getOMFactory(), parser);
        this.metaFactory = metaFactory;
        parserVersion = parser.getVersion();
        identifySOAPVersion(soapVersion);
    }
    
    /**
     * Constructor.
     * 
     * @param parser the parser to read the SOAP message from
     */
    public StAXSOAPModelBuilder(XMLStreamReader parser) {
        this(OMAbstractFactory.getMetaFactory(), parser);
    }
    
    /**
     * Constructor.
     * 
     * @param metaFactory the meta factory used to get the appropriate {@link SOAPFactory}
     *                    implementation
     * @param parser the parser to read the SOAP message from
     */
    public StAXSOAPModelBuilder(OMMetaFactory metaFactory, XMLStreamReader parser) {
        super(metaFactory.getOMFactory(), parser);
        this.metaFactory = metaFactory;
        parserVersion = parser.getVersion();
        SOAPEnvelope soapEnvelope = getSOAPEnvelope();
        envelopeNamespace = soapEnvelope.getNamespace();
    }

    /**
     * Constructor.
     * 
     * @param parser the parser to read the SOAP message from
     * @param factory the SOAP factory to use
     * @param soapVersion the namespace URI corresponding to the expected SOAP version
     *                    of the message
     */
    public StAXSOAPModelBuilder(XMLStreamReader parser, SOAPFactory factory, String soapVersion) {
        super(factory, parser);
        soapFactory = factory;
        parserVersion = parser.getVersion();
        identifySOAPVersion(soapVersion);
    }

    /** @param soapVersionURIFromTransport  */
    protected void identifySOAPVersion(String soapVersionURIFromTransport) {

        SOAPEnvelope soapEnvelope = getSOAPEnvelope();
        if (soapEnvelope == null) {
            throw new SOAPProcessingException("SOAP Message does not contain an Envelope",
                                              SOAPConstants.FAULT_CODE_VERSION_MISMATCH);
        }

        envelopeNamespace = soapEnvelope.getNamespace();

        if (soapVersionURIFromTransport != null) {
            String namespaceName = envelopeNamespace.getNamespaceURI();
            if (!(soapVersionURIFromTransport.equals(namespaceName))) {
                throw new SOAPProcessingException(
                        "Transport level information does not match with SOAP" +
                                " Message namespace URI", envelopeNamespace.getPrefix() + ":" +
                        SOAPConstants.FAULT_CODE_VERSION_MISMATCH);
            }
        }

    }

    /**
     * Method getSOAPEnvelope.
     *
     * @return Returns SOAPEnvelope.
     * @throws OMException
     */
    public SOAPEnvelope getSOAPEnvelope() throws OMException {
        while ((envelope == null) && !done) {
            next();
        }
        return envelope;
    }

    protected OMNode createNextOMElement() {
        OMNode newElement = null;
        
        
        if (elementLevel == 3 && 
            customBuilderForPayload != null) {
            
            OMNode parent = lastNode;
            if (parent != null && parent.isComplete()) {
                parent = (OMNode) lastNode.getParent();
            }
            if (parent instanceof SOAPBody) {
                newElement = createWithCustomBuilder(customBuilderForPayload,  soapFactory);
            }
        } 
        if (newElement == null && customBuilders != null && 
                elementLevel <= maxDepthForCustomBuilders) {
            String namespace = parser.getNamespaceURI();
            String localPart = parser.getLocalName();
            CustomBuilder customBuilder = getCustomBuilder(namespace, localPart);
            if (customBuilder != null) {
                newElement = createWithCustomBuilder(customBuilder, soapFactory);
            }
        }
        if (newElement == null) {
            newElement = createOMElement();
        } else {
            elementLevel--; // Decrease level since custom builder read the end element event
        }
        return newElement;
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
            node = constructNode(null, elementName, true);
            setSOAPEnvelope(node);
        } else if (lastNode.isComplete()) {
            OMContainer parent = lastNode.getParent();
            if (parent == document) {
                // If we get here, this means that we found the SOAP envelope, but that it was
                // preceded by a comment node. Since constructNode will create a new document
                // based on the SOAP version of the envelope, we simply discard the last node 
                // and do as if we just encountered the first node in the document.
                lastNode = null;
                node = constructNode(null, elementName, true);
                setSOAPEnvelope(node);
            } else {
                node = constructNode((OMElement)parent,
                                     elementName,
                                     false);
                ((OMNodeEx) lastNode).setNextOMSibling(node);
                ((OMNodeEx) node).setPreviousOMSibling(lastNode);
            }
        } else {
            OMContainerEx e = (OMContainerEx) lastNode;
            node = constructNode((OMElement) lastNode, elementName, false);
            e.setFirstChild(node);
        }

        if (isDebugEnabled) {
            log.debug("Build the OMElement " + node.getLocalName() +
                    " by the StaxSOAPModelBuilder");
        }
        return node;
    }

    protected void setSOAPEnvelope(OMElement node) {
        soapMessage.setSOAPEnvelope((SOAPEnvelope) node);
        soapMessage.setXMLVersion(parserVersion);
        soapMessage.setCharsetEncoding(charEncoding);
    }

    /**
     * Method constructNode
     *
     * @param parent
     * @param elementName
     * @param isEnvelope
     */
    protected OMElement constructNode(OMElement parent, String elementName,
                                      boolean isEnvelope) {
        OMElement element;
        if (parent == null) {

            // Now I've found a SOAP Envelope, now create SOAPDocument and SOAPEnvelope here.

            if (!elementName.equals(SOAPConstants.SOAPENVELOPE_LOCAL_NAME)) {
                throw new SOAPProcessingException("First Element must contain the local name, "
                        + SOAPConstants.SOAPENVELOPE_LOCAL_NAME + " , but found " + elementName,
                        SOAPConstants.FAULT_CODE_SENDER);
            }
	   

            // determine SOAP version and from that determine a proper factory here.
            if (soapFactory == null) {
                namespaceURI = this.parser.getNamespaceURI();
                if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(namespaceURI)) {
                    soapFactory = metaFactory.getSOAP12Factory();
                    if (isDebugEnabled) {
                        log.debug("Starting to process SOAP 1.2 message");
                    }
                } else if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(namespaceURI)) {
                    soapFactory = metaFactory.getSOAP11Factory();
                    if (isDebugEnabled) {
                        log.debug("Starting to process SOAP 1.1 message");
                    }
                } else {
                    throw new SOAPProcessingException(
                            "Only SOAP 1.1 or SOAP 1.2 messages are supported in the" +
                                    " system", SOAPConstants.FAULT_CODE_VERSION_MISMATCH);
                }
            } else {
                namespaceURI = soapFactory.getSoapVersionURI();
            }

            // create a SOAPMessage to hold the SOAP envelope and assign the SOAP envelope in that.
            soapMessage = soapFactory.createSOAPMessage(this);
            this.document = soapMessage;
            if (charEncoding != null) {
                document.setCharsetEncoding(charEncoding);
            }

            envelope = soapFactory.createSOAPEnvelope(this);
            element = envelope;
            processNamespaceData(element, true);
            // fill in the attributes
            processAttributes(element);

        } else if (elementLevel == 2) {
            // Must be in the right namespace regardless
            String elementNS = parser.getNamespaceURI();

            if (!(namespaceURI.equals(elementNS))) {
                if (!bodyPresent ||
                        !SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(namespaceURI)) {
                    throw new SOAPProcessingException("Disallowed element found inside Envelope : {"
                            + elementNS + "}" + elementName);
                }
            }

            // this is either a header or a body
            if (elementName.equals(SOAPConstants.HEADER_LOCAL_NAME)) {
                if (headerPresent) {
                    throw new SOAPProcessingException("Multiple headers encountered!",
                                                      getSenderFaultCode());
                }
                if (bodyPresent) {
                    throw new SOAPProcessingException("Header Body wrong order!",
                                                      getSenderFaultCode());
                }
                headerPresent = true;
                element =
                        soapFactory.createSOAPHeader((SOAPEnvelope) parent,
                                                     this);

                processNamespaceData(element, true);
                processAttributes(element);

            } else if (elementName.equals(SOAPConstants.BODY_LOCAL_NAME)) {
                if (bodyPresent) {
                    throw new SOAPProcessingException("Multiple body elements encountered",
                                                      getSenderFaultCode());
                }
                bodyPresent = true;
                element =
                        soapFactory.createSOAPBody((SOAPEnvelope) parent,
                                                   this);

                processNamespaceData(element, true);
                processAttributes(element);
            } else {
                throw new SOAPProcessingException(elementName
                        +
                        " is not supported here. Envelope can not have elements other than Header and Body.",
                                                  getSenderFaultCode());
            }
        } else if ((elementLevel == 3)
                &&
                parent.getLocalName().equals(SOAPConstants.HEADER_LOCAL_NAME)) {

            // this is a headerblock
            try {
                element =
                        soapFactory.createSOAPHeaderBlock(elementName, null,
                                                          (SOAPHeader) parent, this);
            } catch (SOAPProcessingException e) {
                throw new SOAPProcessingException("Can not create SOAPHeader block",
                                                  getReceiverFaultCode(), e);
            }
            processNamespaceData(element, false);
            processAttributes(element);

        } else if ((elementLevel == 3) &&
                parent.getLocalName().equals(SOAPConstants.BODY_LOCAL_NAME) &&
                elementName.equals(SOAPConstants.BODY_FAULT_LOCAL_NAME)) {
            // this is a headerblock
            element = soapFactory.createSOAPFault((SOAPBody) parent, this);
            processNamespaceData(element, false);
            processAttributes(element);


            processingFault = true;
            if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI
                    .equals(envelopeNamespace.getNamespaceURI())) {
                builderHelper = new SOAP12BuilderHelper(this);
            } else if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI
                    .equals(envelopeNamespace.getNamespaceURI())) {
                builderHelper = new SOAP11BuilderHelper(this);
            }

        } else if (elementLevel > 3 && processingFault) {
            element = builderHelper.handleEvent(parser, parent, elementLevel);
        } else {
            // this is neither of above. Just create an element
            element = soapFactory.createOMElement(elementName, null,
                                                  parent, this);
            processNamespaceData(element, false);
            processAttributes(element);

        }
        return element;
    }

    private String getSenderFaultCode() {
        return envelope.getVersion().getSenderFaultCode().getLocalPart();
    }

    private String getReceiverFaultCode() {
        return envelope.getVersion().getReceiverFaultCode().getLocalPart();
    }

    public void endElement() {
        if (lastNode.isComplete()) {
            OMElement parent = (OMElement) lastNode.getParent();
            ((OMNodeEx) parent).setComplete(true);
            lastNode = parent;
        } else {
            OMNode e = lastNode;
            ((OMNodeEx) e).setComplete(true);
        }
    }

    /** Method createDTD. Overriding the default behaviour as a SOAPMessage should not have a DTD. */
    protected OMNode createDTD() throws OMException {
        throw new OMException("SOAP message MUST NOT contain a Document Type Declaration(DTD)");
    }

    /** Method createPI. Overriding the default behaviour as a SOAP Message should not have a PI. */
    protected OMNode createPI() throws OMException {
        throw new OMException("SOAP message MUST NOT contain Processing Instructions(PI)");
    }

    /**
     * Method getDocumentElement.
     *
     * @return Returns OMElement.
     */
    public OMElement getDocumentElement() {
        return envelope != null ? envelope : getSOAPEnvelope();
    }

    /**
     * Method processNamespaceData.
     *
     * @param node
     * @param isSOAPElement
     */
    protected void processNamespaceData(OMElement node, boolean isSOAPElement) {

        super.processNamespaceData(node);

        if (isSOAPElement) {
            OMNamespace omNS = node.getNamespace();
            if (omNS != null) {
                String uri = omNS.getNamespaceURI();
                if (uri.equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI) ||
                    uri.equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
                    // okay
                } else {
                    throw new SOAPProcessingException("invalid SOAP namespace URI. " +
                            "Only " + SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI +
                            " and " + SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI +
                            " are supported.", SOAP12Constants.FAULT_CODE_SENDER);
                }
            }
        }

    }

/*these three methods to set and check detail element processing or mandatory fault element are present
*/

    public OMNamespace getEnvelopeNamespace() {
        return envelopeNamespace;
    }

    public boolean isProcessingDetailElements() {
        return processingDetailElements;
    }

    public void setProcessingDetailElements(boolean value) {
        processingDetailElements = value;
    }

    public SOAPMessage getSoapMessage() {
        return soapMessage;
    }

    public OMDocument getDocument() {
        return this.soapMessage;
    }

    /** @return Returns the soapFactory. */
    protected SOAPFactory getSoapFactory() {
        return soapFactory;
    }

    /**
     * Increase or decrease the element level by the desired amount.
     * This is needed by the SOAP11BuilderHelper to account for the different
     * depths for the SOAP fault sytax.
     * @param value
     */
    void adjustElementLevel(int value) {
        elementLevel = elementLevel + value;
    }
}
