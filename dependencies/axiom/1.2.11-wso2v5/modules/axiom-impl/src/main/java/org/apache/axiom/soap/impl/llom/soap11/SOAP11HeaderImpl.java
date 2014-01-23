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

package org.apache.axiom.soap.impl.llom.soap11;

import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.impl.OMNodeEx;
import org.apache.axiom.om.impl.traverse.OMChildrenWithSpecificAttributeIterator;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axiom.soap.SOAPProcessingException;
import org.apache.axiom.soap.impl.llom.SOAPHeaderImpl;

import javax.xml.namespace.QName;
import java.util.Iterator;

public class SOAP11HeaderImpl extends SOAPHeaderImpl {

    public SOAP11HeaderImpl(SOAPFactory factory)
            throws SOAPProcessingException {
        super(factory.getNamespace(), factory);
    }

    /** @param envelope  */
    public SOAP11HeaderImpl(SOAPEnvelope envelope, SOAPFactory factory)
            throws SOAPProcessingException {
        super(envelope, factory);
    }

    /**
     * Constructor SOAPHeaderImpl
     *
     * @param envelope
     * @param builder
     */
    public SOAP11HeaderImpl(SOAPEnvelope envelope, OMXMLParserWrapper builder,
                            SOAPFactory factory) {
        super(envelope, builder, factory);
    }

    public SOAPHeaderBlock addHeaderBlock(String localName, OMNamespace ns)
            throws OMException {

        if (ns == null || ns.getNamespaceURI() == null || ns.getNamespaceURI().isEmpty()) {
            throw new OMException(
                    "All the SOAP Header blocks should be namespace qualified");
        }

        OMNamespace namespace = findNamespace(ns.getNamespaceURI(), ns.getPrefix());
        if (namespace != null) {
            ns = namespace;
        }

        SOAPHeaderBlock soapHeaderBlock = null;
        try {
            soapHeaderBlock = new SOAP11HeaderBlockImpl(localName, ns, this,
                                                        (SOAPFactory) this.factory);
        } catch (SOAPProcessingException e) {
            throw new OMException(e);
        }
        ((OMNodeEx) soapHeaderBlock).setComplete(true);
        return soapHeaderBlock;
    }

    public Iterator extractHeaderBlocks(String role) {
        return new OMChildrenWithSpecificAttributeIterator(getFirstOMChild(),
                                                           new QName(
                                                                   SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI,
                                                                   SOAP11Constants.ATTR_ACTOR),
                                                           role,
                                                           true);

    }

}
