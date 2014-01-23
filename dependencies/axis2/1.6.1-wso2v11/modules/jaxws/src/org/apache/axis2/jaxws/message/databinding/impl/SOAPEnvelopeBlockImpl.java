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

/**
 * 
 */
package org.apache.axis2.jaxws.message.databinding.impl;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.databinding.SOAPEnvelopeBlock;
import org.apache.axis2.jaxws.message.factory.BlockFactory;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.message.impl.BlockImpl;
import org.apache.axis2.jaxws.message.util.SOAPElementReader;
import org.apache.axis2.jaxws.registry.FactoryRegistry;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.WebServiceException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * 
 *
 */
public class SOAPEnvelopeBlockImpl extends BlockImpl implements SOAPEnvelopeBlock {

    /**
     * Called by SOAPEnvelopeBlockFactory
     *
     * @param busObject
     * @param busContext
     * @param qName
     * @param factory
     */
    public SOAPEnvelopeBlockImpl(Object busObject, Object busContext,
                                 QName qName, BlockFactory factory) {
        super(busObject,
              busContext,
              (qName == null) ? getQName((SOAPEnvelope)busObject) : qName,
              factory);
    }

    /**
     * Called by SOAPEnvelopeBlockFactory
     *
     * @param omElement
     * @param busContext
     * @param qName
     * @param factory
     */
    public SOAPEnvelopeBlockImpl(OMElement omElement, Object busContext,
                                 QName qName, BlockFactory factory) {
        super(omElement, busContext, qName, factory);
    }

    /* (non-Javadoc)
      * @see org.apache.axis2.jaxws.message.impl.BlockImpl#_getBOFromReader(javax.xml.stream.XMLStreamReader, java.lang.Object)
      */
    @Override
    protected Object _getBOFromReader(XMLStreamReader reader, Object busContext)
            throws XMLStreamException, WebServiceException {
        MessageFactory mf = (MessageFactory)FactoryRegistry.getFactory(MessageFactory.class);
        Message message = mf.createFrom(reader, null);
        SOAPEnvelope env = message.getAsSOAPEnvelope();
        this.setQName(getQName(env));
        return env;
    }

    /* (non-Javadoc)
      * @see org.apache.axis2.jaxws.message.impl.BlockImpl#_getReaderFromBO(java.lang.Object, java.lang.Object)
      */
    @Override
    protected XMLStreamReader _getReaderFromBO(Object busObj, Object busContext)
            throws XMLStreamException, WebServiceException {
        return new SOAPElementReader((SOAPElement)busObj);
    }

    /* (non-Javadoc)
      * @see org.apache.axis2.jaxws.message.impl.BlockImpl#_outputFromBO(java.lang.Object, java.lang.Object, javax.xml.stream.XMLStreamWriter)
      */
    @Override
    protected void _outputFromBO(Object busObject, Object busContext,
                                 XMLStreamWriter writer)
            throws XMLStreamException, WebServiceException {
        XMLStreamReader reader = _getReaderFromBO(busObject, busContext);
        _outputFromReader(reader, writer);
    }

    /**
     * Get the QName of the envelope
     *
     * @param env
     * @return QName
     */
    private static QName getQName(SOAPEnvelope env) {
        return new QName(env.getNamespaceURI(), env.getLocalName(), env.getPrefix());
    }

    public boolean isElementData() {
        return true;
    }
    
    public void close() {
        return; // Nothing to close
    }

    public InputStream getXMLInputStream(String encoding) throws UnsupportedEncodingException {
        byte[] bytes = getXMLBytes(encoding);
        return new ByteArrayInputStream(bytes);
    }

    public Object getObject() {
        try {
            return getBusinessObject(false);
        } catch (XMLStreamException e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

    public boolean isDestructiveRead() {
        return false;
    }

    public boolean isDestructiveWrite() {
        return false;
    }

    public byte[] getXMLBytes(String encoding) throws UnsupportedEncodingException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OMOutputFormat format = new OMOutputFormat();
        format.setCharSetEncoding(encoding);
        try {
            serialize(baos, format);
            baos.flush();
            return baos.toByteArray();
        } catch (XMLStreamException e) {
            throw ExceptionFactory.makeWebServiceException(e);
        } catch (IOException e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }
}
