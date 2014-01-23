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

import org.apache.axiom.om.OMConstants;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.impl.dom.ElementImpl;
import org.apache.axiom.om.impl.serialize.StreamWriterToContentHandlerConverter;
import org.apache.axiom.om.impl.util.OMSerializerUtil;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPConstants;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.axiom.soap.SOAPFaultDetail;
import org.apache.axiom.soap.SOAPFaultNode;
import org.apache.axiom.soap.SOAPFaultReason;
import org.apache.axiom.soap.SOAPFaultRole;
import org.apache.axiom.soap.SOAPProcessingException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;

public abstract class SOAPFaultImpl extends SOAPElement implements SOAPFault,
        OMConstants {

    protected Exception e;

    /**
     * Constructor SOAPFaultImpl
     *
     * @param parent
     * @param e
     */
    public SOAPFaultImpl(SOAPBody parent, Exception e, SOAPFactory factory)
            throws SOAPProcessingException {
        super(parent, SOAPConstants.SOAPFAULT_LOCAL_NAME, true, factory);
        setException(e);
    }

    public void setException(Exception e) {
        this.e = e;
        putExceptionToSOAPFault(e);
    }

    public SOAPFaultImpl(SOAPBody parent, SOAPFactory factory)
            throws SOAPProcessingException {
        super(parent, SOAPConstants.SOAPFAULT_LOCAL_NAME, true, factory);
    }

    /**
     * Constructor SOAPFaultImpl
     *
     * @param parent
     * @param builder
     */
    public SOAPFaultImpl(SOAPBody parent, OMXMLParserWrapper builder,
                         SOAPFactory factory) {
        super(parent, SOAPConstants.SOAPFAULT_LOCAL_NAME, builder, factory);
    }

    protected abstract SOAPFaultDetail getNewSOAPFaultDetail(SOAPFault fault)
            throws SOAPProcessingException;

    // --------------- Getters and Settors --------------------------- //

    public void setCode(SOAPFaultCode soapFaultCode)
            throws SOAPProcessingException {
        setNewElement(getCode(), soapFaultCode);
    }

    public void setReason(SOAPFaultReason reason)
            throws SOAPProcessingException {
        setNewElement(getReason(), reason);
    }

    public void setNode(SOAPFaultNode node) throws SOAPProcessingException {
        setNewElement(getNode(), node);
    }

    public void setRole(SOAPFaultRole role) throws SOAPProcessingException {
        setNewElement(getRole(), role);
    }

    public void setDetail(SOAPFaultDetail detail)
            throws SOAPProcessingException {
        setNewElement(getDetail(), detail);
    }

    /** If exception detailElement is not there we will return null */
    public Exception getException() throws OMException {
        SOAPFaultDetail detail = getDetail();
        if (detail == null) {
            return null;
        }

        OMElement exceptionElement = getDetail().getFirstChildWithName(
                new QName(SOAPConstants.SOAP_FAULT_DETAIL_EXCEPTION_ENTRY));
        if (exceptionElement != null && exceptionElement.getText() != null) {
            return new Exception(exceptionElement.getText());
        }
        return null;
    }

    protected void putExceptionToSOAPFault(Exception e)
            throws SOAPProcessingException {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        sw.flush();
        getDetail();
        if (getDetail() == null) {
            setDetail(getNewSOAPFaultDetail(this));

        }
        OMElement faultDetailEnty = new ElementImpl(this,
                                                    SOAPConstants.SOAP_FAULT_DETAIL_EXCEPTION_ENTRY,
                                                    null, this.factory);
        faultDetailEnty.setText(sw.getBuffer().toString());
    }

    protected void setNewElement(OMElement myElement, OMElement newElement) {
        if (myElement != null) {
            myElement.discard();
        }
        if (newElement != null && newElement.getParent() != null) {
            newElement.discard();
        }
        this.addChild(newElement);
    }

    public void internalSerialize(XMLStreamWriter writer,
                                     boolean cache) throws XMLStreamException {
        // select the builder
        short builderType = PULL_TYPE_BUILDER; // default is pull type
        if (builder != null) {
            builderType = this.builder.getBuilderType();
        }
        if ((builderType == PUSH_TYPE_BUILDER)
                && (builder.getRegisteredContentHandler() == null)) {
            builder
                    .registerExternalContentHandler(new StreamWriterToContentHandlerConverter(
                            writer));
        }

        // this is a special case. This fault element may contain its children
        // in any order. But spec mandates a specific order
        // the overriding of the method will facilitate that. Not sure this is
        // the best method to do this :(
        build();

        OMSerializerUtil.serializeStartpart(this, writer);
        SOAPFaultCode faultCode = getCode();
        if (faultCode != null) {
            (faultCode).serialize(writer);
        }
        SOAPFaultReason faultReason = getReason();
        if (faultReason != null) {
            (faultReason).serialize(writer);
        }

        serializeFaultNode(writer);

        SOAPFaultRole faultRole = getRole();
        if (faultRole != null) {
            (faultRole).serialize(writer);
        }

        SOAPFaultDetail faultDetail = getDetail();
        if (faultDetail != null) {
            (faultDetail).serialize(writer);
        }

        OMSerializerUtil.serializeEndpart(writer);
    }

    protected abstract void serializeFaultNode(
            XMLStreamWriter writer)
            throws XMLStreamException;

}
