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

package org.apache.axiom.soap.impl.dom;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.impl.dom.ElementImpl;
import org.apache.axiom.soap.SOAPConstants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axiom.soap.SOAPProcessingException;
import org.apache.axiom.soap.RolePlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public abstract class SOAPHeaderImpl extends SOAPElement implements SOAPHeader {


    /** @param envelope  */
    public SOAPHeaderImpl(SOAPEnvelope envelope, SOAPFactory factory)
            throws SOAPProcessingException {
        super(envelope, SOAPConstants.HEADER_LOCAL_NAME, true, factory);

    }

    /**
     * Constructor SOAPHeaderImpl
     *
     * @param envelope
     * @param builder
     */
    public SOAPHeaderImpl(SOAPEnvelope envelope, OMXMLParserWrapper builder,
                          SOAPFactory factory) {
        super(envelope, SOAPConstants.HEADER_LOCAL_NAME, builder, factory);
    }

    /**
     * Creates a new <CODE>SOAPHeaderBlock</CODE> object initialized with the specified name and
     * adds it to this <CODE>SOAPHeader</CODE> object.
     *
     * @param localName
     * @param ns
     * @return the new <CODE>SOAPHeaderBlock</CODE> object that was inserted into this
     *         <CODE>SOAPHeader</CODE> object
     * @throws org.apache.axiom.om.OMException
     *                     if a SOAP error occurs
     */
    public abstract SOAPHeaderBlock addHeaderBlock(String localName,
                                                   OMNamespace ns)
            throws OMException;

    /**
     * Get the appropriate set of headers for a RolePlayer.
     * <p/>
     * The RolePlayer indicates whether it is the ultimate destination (in which case headers with
     * no role or the explicit UltimateDestination role will be included), and any non-standard
     * roles it supports.  Headers targeted to "next" will always be included, and those targeted to
     * "none" (for SOAP 1.2) will never be included.
     *
     * @return an Iterator over all the HeaderBlocks this RolePlayer should process.
     */
    public Iterator getHeadersToProcess(RolePlayer rolePlayer) {
        return null; // TODO: Implement this!
    }

    public Iterator getHeadersToProcess(RolePlayer rolePlayer, String namespace) {
        return null; // TODO: Implement this!    
    }

    /**
     * Returns a list of all the <CODE>SOAPHeaderBlock</CODE> objects in this
     * <CODE>SOAPHeader</CODE> object that have the the specified actor. An actor is a global
     * attribute that indicates the intermediate parties to whom the message should be sent. An
     * actor receives the message and then sends it to the next actor. The default actor is the
     * ultimate intended recipient for the message, so if no actor attribute is included in a
     * <CODE>SOAPHeader</CODE> object, the message is sent to its ultimate destination.
     *
     * @param paramRole a <CODE>String</CODE> giving the URI of the actor for which to search
     * @return an <CODE>Iterator</CODE> object over all the <CODE> SOAPHeaderBlock</CODE> objects
     *         that contain the specified actor
     * @see #extractHeaderBlocks(String) extractHeaderBlocks(java.lang.String)
     */
    public Iterator examineHeaderBlocks(String paramRole) {
        /* Iterator headerBlocksIter = this.getChildren();
       ArrayList headersWithGivenActor = new ArrayList();

       if (paramRole == null || "".equals(paramRole)) {
           return returnAllSOAPHeaders(this.getChildren());
       }

       while (headerBlocksIter.hasNext()) {
           Object o = headerBlocksIter.next();
           if (o instanceof SOAPHeaderBlock) {
               SOAPHeaderBlock soapHeaderBlock = (SOAPHeaderBlock) o;
               String role = soapHeaderBlock.getRole();
               if ((role != null) && role.equalsIgnoreCase(paramRole)) {
                   headersWithGivenActor.add(soapHeaderBlock);
               }
           }
       }
       return headersWithGivenActor.iterator();*/

        if (paramRole == null || paramRole.trim().isEmpty()) {
            return examineAllHeaderBlocks();
        }
        Collection elements = new ArrayList();
        for (Iterator iter = examineAllHeaderBlocks(); iter.hasNext();) {
            SOAPHeaderBlock headerBlock = (SOAPHeaderBlock) iter.next();
            /*
            if (headerBlock.getRole() == null ||
                headerBlock.getRole().trim().isEmpty() ||
                headerBlock.getRole().equals(paramRole)) {
                elements.add(headerBlock);
            }
            */
            if (headerBlock.getRole() != null &&
                    headerBlock.getRole().trim().length() > 0 &&
                    headerBlock.getRole().equals(paramRole)) {
                elements.add(headerBlock);
            }

        }
        return elements.iterator();
    }

//    private Iterator returnAllSOAPHeaders(Iterator children) {
//        ArrayList headers = new ArrayList();
//        while (children.hasNext()) {
//            Object o = children.next();
//            if (o instanceof SOAPHeaderBlock) {
//                headers.add(o);
//            }
//        }
//
//        return headers.iterator();
//
//    }

    /**
     * Returns a list of all the <CODE>SOAPHeaderBlock</CODE> objects in this
     * <CODE>SOAPHeader</CODE> object that have the the specified role and detaches them from this
     * <CODE> SOAPHeader</CODE> object. <P>This method allows an role to process only the parts of
     * the <CODE>SOAPHeader</CODE> object that apply to it and to remove them before passing the
     * message on to the next role.
     *
     * @param role a <CODE>String</CODE> giving the URI of the role for which to search
     * @return an <CODE>Iterator</CODE> object over all the <CODE> SOAPHeaderBlock</CODE> objects
     *         that contain the specified role
     * @see #examineHeaderBlocks(String) examineHeaderBlocks(java.lang.String)
     */
    public abstract Iterator extractHeaderBlocks(String role);

    /**
     * Returns an <code>Iterator</code> over all the <code>SOAPHeaderBlock</code> objects in this
     * <code>SOAPHeader</code> object that have the specified actor and that have a MustUnderstand
     * attribute whose value is equivalent to <code>true</code>.
     *
     * @param actor a <code>String</code> giving the URI of the actor for which to search
     * @return an <code>Iterator</code> object over all the <code>SOAPHeaderBlock</code> objects
     *         that contain the specified actor and are marked as MustUnderstand
     */
    public Iterator examineMustUnderstandHeaderBlocks(String actor) {
        Iterator headerBlocksIter = this.getChildren();
        ArrayList mustUnderstandHeadersWithGivenActor = new ArrayList();
        while (headerBlocksIter.hasNext()) {
            Object o = headerBlocksIter.next();
            if (o instanceof SOAPHeaderBlock) {
                SOAPHeaderBlock soapHeaderBlock = (SOAPHeaderBlock) o;
                String role = soapHeaderBlock.getRole();
                boolean mustUnderstand = soapHeaderBlock.getMustUnderstand();
                if ((role != null) && role.equals(actor) && mustUnderstand) {
                    mustUnderstandHeadersWithGivenActor.add(soapHeaderBlock);
                }
            }
        }
        return mustUnderstandHeadersWithGivenActor.iterator();
    }

    /**
     * Returns an <code>Iterator</code> over all the <code>SOAPHeaderBlock</code> objects in this
     * <code>SOAPHeader</code> object. Not that this will return elements containing the QName
     * (http://schemas.xmlsoap.org/soap/envelope/, Header)
     *
     * @return an <code>Iterator</code> object over all the <code>SOAPHeaderBlock</code> objects
     *         contained by this <code>SOAPHeader</code>
     */
    public Iterator examineAllHeaderBlocks() {
        return this.getChildrenWithName(null);
    }

    /**
     * Returns an <code>Iterator</code> over all the <code>SOAPHeaderBlock</code> objects in this
     * <code>SOAPHeader </code> object and detaches them from this <code>SOAPHeader</code> object.
     *
     * @return an <code>Iterator</code> object over all the <code>SOAPHeaderBlock</code> objects
     *         contained by this <code>SOAPHeader</code>
     */
    public Iterator extractAllHeaderBlocks() {
        Collection result = new ArrayList();
        for (Iterator iter = getChildrenWithName(null); iter.hasNext();) {
            result.add(((ElementImpl) iter.next()).detach());
        }
        return result.iterator();
    }

    public ArrayList getHeaderBlocksWithNSURI(String nsURI) {
        ArrayList headers = null;
        OMNode node;
        OMElement header = this.getFirstElement();

        if (header != null) {
            headers = new ArrayList();
        }

        node = header;

        while (node != null) {
            if (node.getType() == OMNode.ELEMENT_NODE) {
                header = (OMElement) node;
                OMNamespace namespace = header.getNamespace();
                if (nsURI == null) {
                    if (namespace == null) {
                        headers.add(header);
                    }
                } else {
                    if (namespace != null) {
                        if (nsURI.equals(namespace.getNamespaceURI())) {
                            headers.add(header);
                        }
                    }
                }
            }
            node = node.getNextOMSibling();

        }
        return headers;

    }

    protected void checkParent(OMElement parent) throws SOAPProcessingException {
        if (!(parent instanceof SOAPEnvelopeImpl)) {
            throw new SOAPProcessingException(
                    "Expecting an implementation of SOAP Envelope as the " +
                            "parent. But received some other implementation");
        }
    }

}
