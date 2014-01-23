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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.impl.dom.AttrImpl;
import org.apache.axiom.om.impl.dom.ElementImpl;
import org.apache.axiom.om.impl.dom.NamespaceImpl;
import org.apache.axiom.om.impl.dom.ParentNode;
import org.apache.axiom.om.impl.dom.factory.OMDOMFactory;
import org.apache.axiom.soap.SOAPConstants;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axiom.soap.SOAPProcessingException;

import javax.xml.namespace.QName;

public abstract class SOAPHeaderBlockImpl extends ElementImpl implements SOAPHeaderBlock {

    private boolean processed = false;

    /**
     * @param localName
     * @param ns
     * @param parent
     */
    public SOAPHeaderBlockImpl(String localName, OMNamespace ns,
                               SOAPHeader parent, SOAPFactory factory)
            throws SOAPProcessingException {
        super((ParentNode) parent, localName, (NamespaceImpl) ns, factory);
        this.setNamespace(ns);
    }

    public SOAPHeaderBlockImpl(String localName, OMNamespace ns,
                               SOAPFactory factory) throws SOAPProcessingException {
        super(((OMDOMFactory) factory).getDocument(), localName, (NamespaceImpl) ns, factory);
        this.setNamespace(ns);
    }

    /**
     * Constructor SOAPHeaderBlockImpl.
     *
     * @param localName
     * @param ns
     * @param parent
     * @param builder
     */
    public SOAPHeaderBlockImpl(String localName, OMNamespace ns,
                               OMElement parent, OMXMLParserWrapper builder, SOAPFactory factory) {
        super((ParentNode) parent, localName, (NamespaceImpl) ns, builder, factory);
        this.setNamespace(ns);
    }

    /**
     * @param attributeName
     * @param attrValue
     * @param soapEnvelopeNamespaceURI
     */
    protected void setAttribute(String attributeName,
                                String attrValue,
                                String soapEnvelopeNamespaceURI) {
        OMAttribute omAttribute = this.getAttribute(
                new QName(soapEnvelopeNamespaceURI, attributeName));
        if (omAttribute != null) {
            omAttribute.setAttributeValue(attrValue);
        } else {
            OMAttribute attribute = new AttrImpl(this.ownerNode, attributeName,
                                                 new NamespaceImpl(soapEnvelopeNamespaceURI,
                                                                   SOAPConstants.SOAP_DEFAULT_NAMESPACE_PREFIX),
                                                 attrValue, this.factory);
            this.addAttribute(attribute);
        }
    }

    /**
     * Method getAttribute.
     *
     * @param attrName
     * @return Returns String.
     */
    protected String getAttribute(String attrName,
                                  String soapEnvelopeNamespaceURI) {
        OMAttribute omAttribute = this.getAttribute(
                new QName(soapEnvelopeNamespaceURI, attrName));
        return (omAttribute != null)
                ? omAttribute.getAttributeValue()
                : null;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed() {
        processed = true;
    }

    public OMDataSource getDataSource() {
        throw new UnsupportedOperationException();
    }

    public boolean isExpanded() {
        throw new UnsupportedOperationException();
    }

    public OMDataSource setDataSource(OMDataSource dataSource) {
        throw new UnsupportedOperationException();
    }
}
