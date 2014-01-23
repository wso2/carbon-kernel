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
import org.apache.axiom.om.OMComment;
import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.OMDataSourceExt;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMSourcedElement;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.axiom.soap.SOAPFaultDetail;
import org.apache.axiom.soap.SOAPFaultNode;
import org.apache.axiom.soap.SOAPFaultReason;
import org.apache.axiom.soap.SOAPFaultRole;
import org.apache.axiom.soap.SOAPFaultSubCode;
import org.apache.axiom.soap.SOAPFaultText;
import org.apache.axiom.soap.SOAPFaultValue;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import java.util.Iterator;

/**
 * CopyUtils provides static utility methods that are useful for creating a copy of 
 * an OM/SOAPEnvelope tree.  
 * During the expansion, the Source tree retains its shape
 * (OMSourcedElement nodes are not expanded).  
 * The Target tree has nodes that retain the class identity of the source node.  For 
 * example, a SOAPFault in the source tree will have a SOAPFault in the target tree.
 */
public class CopyUtils {

    private static Log log = LogFactory.getLog(CopyUtils.class);
    private static final boolean IS_DEBUG_ENABLED = log.isDebugEnabled();
    
    
    /**
     * Private Constructor
     */
    private CopyUtils() {
    }


    /**
     * Creates a copy of the source envelope.
     * If there are OMSourcedElements in the source tree, 
     * similar MSourcedElements are used in the target tree.
     *
     * @param sourceEnv
     * @return targetEnv
     */
    public static SOAPEnvelope copy(SOAPEnvelope sourceEnv) {

        // Make sure to build the whole sourceEnv
        if (log.isDebugEnabled()) {
            log.debug("start copy SOAPEnvelope");
        }
        
        SOAPFactory factory = (SOAPFactory) sourceEnv.getOMFactory();
        // Create envelope with the same prefix
        SOAPEnvelope targetEnv = factory.createSOAPEnvelope(sourceEnv.getNamespace());

        // Copy the attributes and namespaces from the source
        // envelope to the target envelope.
        copyTagData(sourceEnv, targetEnv);

        Iterator i = sourceEnv.getChildren();
        while (i.hasNext()) {
            OMNode node = (OMNode) i.next();
            if (node instanceof SOAPHeader) {
                // Copy the SOAPHeader tree
                SOAPHeader targetHeader = factory.createSOAPHeader(targetEnv);
                Iterator j = ((SOAPHeader)node).getChildren();
                while (j.hasNext()) {
                    OMNode child = (OMNode) j.next();
                    copy(factory, targetHeader, child);
                }
            } else if (node instanceof SOAPBody) {
                // Copy the SOAPBody tree
                SOAPBody targetBody = factory.createSOAPBody(targetEnv);
                Iterator j = ((SOAPBody)node).getChildren();
                while (j.hasNext()) {
                    OMNode child = (OMNode) j.next();
                    copy(factory, targetBody, child);
                }
                
            } else {
                // Comments, text, etc.
                copy(factory, targetEnv, node);
            }  
        }

        if (log.isDebugEnabled()) {
            log.debug("end copy SOAPEnvelope");
        }
        return targetEnv;
    }

    /**
     * Simple utility that takes an XMLStreamReader and writes it
     * to an XMLStreamWriter
     * @param reader
     * @param writer
     * @throws XMLStreamException
     */
    public static void reader2writer(XMLStreamReader reader, 
                                     XMLStreamWriter writer)
    throws XMLStreamException {
        StAXOMBuilder builder = new StAXOMBuilder(reader);
        builder.releaseParserOnClose(true);
        try {
            OMDocument omDocument = builder.getDocument();
            Iterator it = omDocument.getChildren();
            while (it.hasNext()) {
                OMNode omNode = (OMNode) it.next();
                omNode.serializeAndConsume(writer);
            }
        } finally {
            builder.close();
        }
    }

    /**
     * Create a copy of the sourceNode and attach it to the targetParent
     * @param factory OMFactory
     * @param targetParent
     * @param sourceNode
     */
    private static void copy(SOAPFactory factory, 
                             OMContainer targetParent, 
                             OMNode sourceNode) {

        // Create and attach a node of the same class
        // TODO It would be nice if you could do this directly from the
        // OMNode, but OMNode.clone() does not gurantee that an object of the correct
        // class is created.
        if (sourceNode instanceof SOAPHeaderBlock) {
            copySOAPHeaderBlock(factory, targetParent, (SOAPHeaderBlock) sourceNode);
        } else if (sourceNode instanceof SOAPFault) {
            copySOAPFault(factory, targetParent, (SOAPFault) sourceNode);
        } else if (sourceNode instanceof OMSourcedElement) {
            copyOMSourcedElement(factory, targetParent, (OMSourcedElement) sourceNode);
        } else {
            int type = sourceNode.getType();
            switch (type) {
                case OMNode.ELEMENT_NODE:
                    copyOMElement(factory, targetParent, (OMElement) sourceNode);
                    break;
                case OMNode.TEXT_NODE:
                    copyOMText(factory, targetParent, (OMText) sourceNode);
                    break;
                case OMNode.COMMENT_NODE:
                    copyOMComment(factory, targetParent, (OMComment) sourceNode);
                    break;
                default:
                    throw new OMException("Internal Failure: Cannot make a copy of "
                                          + sourceNode.getClass().getName());

            }
        }
    }

    /**
     * Create a copy of the source OMComment
     * @param factory
     * @param targetParent
     * @param sourceComment
     */
    private static void copyOMComment(SOAPFactory factory, 
                                      OMContainer targetParent, 
                                      OMComment sourceComment) {
        // Create and attach the comment
        factory.createOMComment(targetParent, sourceComment.getValue());
    }

    /**
     * Create a copy of the OM Text
     * @param factory
     * @param targetParent
     * @param sourceText
     */
    private static void copyOMText(SOAPFactory factory, 
                                   OMContainer targetParent, 
                                   OMText sourceText) {
        if (IS_DEBUG_ENABLED) {
            log.debug("start copyOMText");
        }
        if (sourceText.isBinary()) {
            // This forces a load of the datahandler so that it is saved on the copy.
            Object dh = sourceText.getDataHandler();
            if (IS_DEBUG_ENABLED) {
                String dhclass = (dh == null) ? "null" : dh.getClass().toString();
                log.debug("The source text's binary data handler is " + dhclass);
            }
        }
        factory.createOMText(targetParent, sourceText);
        if (IS_DEBUG_ENABLED) {
            log.debug("end copyOMText");
        }
    }

    /**
     * Create a copy of an ordinary OMElement
     * @param factory
     * @param targetParent
     * @param sourceElement
     */
    private static void copyOMElement(SOAPFactory factory, 
                                      OMContainer targetParent, 
                                      OMElement sourceElement) {
        // Clone and attach the OMElement.
        // REVIEW This clone will expand the underlying tree.  We may want consider traversing
        // a few levels deeper to see if there are any additional OMSourcedElements.
        targetParent.addChild(sourceElement.cloneOMElement());
    }

    /**
     * Create a copy of the OMSourcedElement
     * @param factory
     * @param targetParent
     * @param sourceOMSE
     */
    private static void copyOMSourcedElement(SOAPFactory factory, 
                                             OMContainer targetParent,
                                             OMSourcedElement sourceOMSE) {
        // If already expanded or this is not an OMDataSourceExt, then
        // create a copy of the OM Tree
        OMDataSource ds = sourceOMSE.getDataSource();
        if (ds == null || 
            sourceOMSE.isExpanded() || 
            !(ds instanceof OMDataSourceExt)) {
            copyOMElement(factory, targetParent, sourceOMSE);
            return;
        }
        
        // If copying is destructive, then copy the OM tree
        OMDataSourceExt sourceDS = (OMDataSourceExt) ds;
        if (sourceDS.isDestructiveRead() ||
            sourceDS.isDestructiveWrite()) {
            copyOMElement(factory, targetParent, sourceOMSE);
            return;
        }
        OMDataSourceExt targetDS = ((OMDataSourceExt) ds).copy();
        if (targetDS == null) {
            copyOMElement(factory, targetParent, sourceOMSE);
            return;
        }
        // Otherwise create a target OMSE with the copied DataSource
        OMSourcedElement targetOMSE =
            factory.createOMElement(targetDS, 
                                    sourceOMSE.getLocalName(), 
                                    sourceOMSE.getNamespace());
        targetParent.addChild(targetOMSE);

    }

    /**
     * Create a copy of the SOAPHeaderBlock
     * @param factory
     * @param targetParent
     * @param sourceSHB
     */
    private static void copySOAPHeaderBlock(SOAPFactory factory, 
                                            OMContainer targetParent,
                                            SOAPHeaderBlock sourceSHB) {
        // If already expanded or this is not an OMDataSourceExt, then
        // create a copy of the OM Tree
        OMDataSource ds = sourceSHB.getDataSource();
        if (ds == null || 
            sourceSHB.isExpanded() || 
            !(ds instanceof OMDataSourceExt)) {
            copySOAPHeaderBlock_NoDataSource(factory, targetParent, sourceSHB);
            return;
        }
        
        // If copying is destructive, then copy the OM tree
        OMDataSourceExt sourceDS = (OMDataSourceExt) ds;
        if (sourceDS.isDestructiveRead() ||
            sourceDS.isDestructiveWrite()) {
            copySOAPHeaderBlock_NoDataSource(factory, targetParent, sourceSHB);
            return;
        }
        
        // Otherwise create a copy of the OMDataSource
        OMDataSourceExt targetDS = ((OMDataSourceExt) ds).copy();
        SOAPHeaderBlock targetSHB =
            factory.createSOAPHeaderBlock(sourceSHB.getLocalName(), 
                                          sourceSHB.getNamespace(), 
                                          targetDS);
        targetParent.addChild(targetSHB);
        copySOAPHeaderBlockData(sourceSHB, targetSHB);
    }
    
    /**
     * Create a copy of the SOAPHeaderBlock
     * @param factory
     * @param targetParent
     * @param sourceSHB
     */
    private static void copySOAPHeaderBlock_NoDataSource(SOAPFactory factory, 
                                            OMContainer targetParent,
                                            SOAPHeaderBlock sourceSHB) {
        
        
        SOAPHeader header = (SOAPHeader) targetParent;
        String localName = sourceSHB.getLocalName();
        OMNamespace ns = sourceSHB.getNamespace();
        SOAPHeaderBlock targetSHB = factory.createSOAPHeaderBlock(localName, ns, header);
        
        // A SOAPHeaderBlock has tag data, plus extra header processing flags
        copyTagData(sourceSHB, targetSHB);
        copySOAPHeaderBlockData(sourceSHB, targetSHB);
        Iterator i = sourceSHB.getChildren();
        while (i.hasNext()) {
            OMNode node = (OMNode) i.next();
            copy(factory, targetSHB, node);
        }
    }

    /**
     * Create a copy of a SOAPFault
     * @param factory
     * @param targetParent
     * @param sourceSOAPFault
     */
    private static void copySOAPFault(SOAPFactory factory, 
                                      OMContainer targetParent,
                                      SOAPFault sourceSOAPFault) {
        Exception e = sourceSOAPFault.getException();
        
        SOAPFault newSOAPFault = (e == null) ?
                    factory.createSOAPFault((SOAPBody) targetParent):
                    factory.createSOAPFault((SOAPBody) targetParent, e);
                                        
        copyTagData(sourceSOAPFault, newSOAPFault);
        Iterator i = sourceSOAPFault.getChildren();
        while (i.hasNext()) {
            OMNode node = (OMNode) i.next();
            // Copy the tree under the SOAPFault
            copyFaultData(factory, newSOAPFault, node);
        }
    }

    /**
     * Copy the source Node, which is a child fo a SOAPFault,
     * to the target SOAPFault
     * @param factory
     * @param targetFault
     * @param sourceNode
     */
    private static void copyFaultData(SOAPFactory factory, 
                                      SOAPFault targetFault, 
                                      OMNode sourceNode) {

        if (sourceNode instanceof SOAPFaultCode) {
            copySOAPFaultCode(factory, targetFault, (SOAPFaultCode) sourceNode);
        } else if (sourceNode instanceof SOAPFaultDetail) {
            copySOAPFaultDetail(factory, targetFault, (SOAPFaultDetail) sourceNode);
        } else if (sourceNode instanceof SOAPFaultNode) {
            copySOAPFaultNode(factory, targetFault, (SOAPFaultNode) sourceNode);
        } else if (sourceNode instanceof SOAPFaultReason) {
            copySOAPFaultReason(factory, targetFault, (SOAPFaultReason) sourceNode);
        } else if (sourceNode instanceof SOAPFaultRole) {
            copySOAPFaultRole(factory, targetFault, (SOAPFaultRole) sourceNode);
        } else if (sourceNode.getType() == OMNode.TEXT_NODE) {
            copyOMText(factory, targetFault, (OMText) sourceNode);
        } else if (sourceNode.getType() == OMNode.COMMENT_NODE) {
            copyOMComment(factory, targetFault, (OMComment) sourceNode);
        } else {
            throw new OMException("Internal Failure: Cannot make a copy of "
                    + sourceNode.getClass().getName() + " object found in a SOAPFault.");
        }
    }

    /**
     * Create a copy of a SOAPFaultRole
     * @param factory
     * @param targetFault
     * @param sourceRole
     */
    private static void copySOAPFaultRole(SOAPFactory factory, 
                                          SOAPFault targetFault, 
                                          SOAPFaultRole sourceRole) {
        SOAPFaultRole targetRole = factory.createSOAPFaultRole(targetFault);
        copyTagData(sourceRole, targetRole);
        targetRole.setRoleValue(sourceRole.getRoleValue());
    }

    /**
     * Create a copy of a SOAPFaultNode
     * @param factory
     * @param targetFault
     * @param sourceNode
     */
    private static void copySOAPFaultNode(SOAPFactory factory, 
                                          SOAPFault targetFault, 
                                          SOAPFaultNode sourceNode) {
        SOAPFaultNode targetNode = factory.createSOAPFaultNode(targetFault);
        copyTagData(sourceNode, targetNode);
        targetNode.setNodeValue(sourceNode.getNodeValue());
    }

    /**
     * Create a copy of a SOAPFaultDetail
     * @param factory
     * @param targetFault
     * @param sourceDetail
     */
    private static void copySOAPFaultDetail(SOAPFactory factory, 
                                            SOAPFault targetFault,
                                            SOAPFaultDetail sourceDetail) {
        SOAPFaultDetail targetDetail = factory.createSOAPFaultDetail(targetFault);
        copyTagData(sourceDetail, targetDetail);
        
        // Copy the detail entries
        Iterator i = sourceDetail.getChildren();
        while (i.hasNext()) {
            OMNode node = (OMNode) i.next();
            copy(factory, targetDetail, node);
        }
    }

    /**
     * Create a copy of the SOAPFaultReason
     * @param factory
     * @param targetFault
     * @param sourceReason
     */
    private static void copySOAPFaultReason(SOAPFactory factory, 
                                            SOAPFault targetFault,
                                            SOAPFaultReason sourceReason) {
        SOAPFaultReason targetReason = factory.createSOAPFaultReason(targetFault);
        copyTagData(sourceReason, targetReason);
        Iterator i = sourceReason.getChildren();
        while (i.hasNext()) {
            OMNode node = (OMNode) i.next();
            if (node instanceof SOAPFaultText) {
                SOAPFaultText oldText = (SOAPFaultText) node;
                SOAPFaultText newText = factory.createSOAPFaultText(targetReason);
                copyTagData(oldText, newText); // The lang is copied as an attribute
            } else {
                // Copy any comments or child nodes
                copy(factory, targetReason, node);
            }
        }
    }

    /**
     * Copy the SOAPFaultCode tree
     * @param factory
     * @param targetFault
     * @param sourceCode
     */
    private static void copySOAPFaultCode(SOAPFactory factory, 
                                          SOAPFault targetFault, 
                                          SOAPFaultCode sourceCode) {
        SOAPFaultCode targetCode = factory.createSOAPFaultCode(targetFault);
        copyTagData(sourceCode, targetCode);

        // Create the Value
        SOAPFaultValue sourceValue = sourceCode.getValue();
        SOAPFaultValue targetValue = factory.createSOAPFaultValue(targetCode);
        copyTagData(sourceValue, targetValue);
        
        // There should only be a text node for the value, but in case there is more
        Iterator i = sourceValue.getChildren();
        while (i.hasNext()) {
            OMNode node = (OMNode) i.next();
            copy(factory, targetValue, node);
        }

        // Now get process the SubCode
        SOAPFaultSubCode sourceSubCode = sourceCode.getSubCode();
        if (sourceSubCode != null) {
            copySOAPFaultSubCode(factory, targetCode, sourceSubCode);
        }
    }

    /**
     * Copy the SOAPFaultSubCode tree
     * @param factory
     * @param targetParent (SOAPFaultCode or SOAPFaultSubCode)
     * @param sourceSubCode
     */
    private static void copySOAPFaultSubCode(SOAPFactory factory, 
                                             OMElement targetParent,
                                             SOAPFaultSubCode sourceSubCode) {
        SOAPFaultSubCode targetSubCode;
        if (targetParent instanceof SOAPFaultSubCode) {
            targetSubCode = factory.createSOAPFaultSubCode((SOAPFaultSubCode) targetParent);
        } else {
            targetSubCode = factory.createSOAPFaultSubCode((SOAPFaultCode) targetParent);
        }
        copyTagData(sourceSubCode, targetSubCode);

        // Process the SOAP FaultValue
        SOAPFaultValue sourceValue = sourceSubCode.getValue();
        SOAPFaultValue targetValue = factory.createSOAPFaultValue(targetSubCode);
        copyTagData(sourceValue, targetValue);
        // There should only be a text node for the value, but in case there is more
        Iterator i = sourceValue.getChildren();
        while (i.hasNext()) {
            OMNode node = (OMNode) i.next();
            copy(factory, targetValue, node);
        }

        // Now process the SubCode of the SubCode
        SOAPFaultSubCode sourceSubSubCode = sourceSubCode.getSubCode();
        if (sourceSubSubCode != null) {
            copySOAPFaultSubCode(factory, targetSubCode, sourceSubSubCode);
        }
    }


    /**
     * Copy the tag data (attributes and namespaces) from the source
     * element to the target element.
     * @param sourceElement
     * @param targetElement
     */
    private static void copyTagData(OMElement sourceElement, 
                                    OMElement targetElement) {
        for (Iterator i = sourceElement.getAllDeclaredNamespaces(); i.hasNext();) {
            OMNamespace ns = (OMNamespace) i.next();
            targetElement.declareNamespace(ns);
        }

        for (Iterator i = sourceElement.getAllAttributes(); i.hasNext();) {
            OMAttribute attr = (OMAttribute) i.next();
            targetElement.addAttribute(attr);
        }
    }

    /**
     * Copy Header data (currently only the processed flag) from the
     * source SOAPHeaderBlock to the target SOAPHeaderBlock
     * @param sourceSHB
     * @param targetSHB
     */
    private static void copySOAPHeaderBlockData(SOAPHeaderBlock sourceSHB, 
                                                SOAPHeaderBlock targetSHB) {
        // Copy the processed flag.  The other SOAPHeaderBlock information 
        //(e.g. role, mustUnderstand) are attributes on the tag and are copied in copyTagData.
        if (sourceSHB.isProcessed()) {
            targetSHB.setProcessed();
        }
    }
}
