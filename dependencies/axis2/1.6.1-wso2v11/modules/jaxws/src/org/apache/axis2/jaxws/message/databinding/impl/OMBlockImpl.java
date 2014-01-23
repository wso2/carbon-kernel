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

package org.apache.axis2.jaxws.message.databinding.impl;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.message.databinding.OMBlock;
import org.apache.axis2.jaxws.message.factory.BlockFactory;
import org.apache.axis2.jaxws.message.impl.BlockImpl;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.WebServiceException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/** OMBlockImpl Block with a business object that is an OMElement */
public class OMBlockImpl extends BlockImpl implements OMBlock {


    /**
     * Called by OMBlockFactory
     *
     * @param busObject
     * @param factory
     */
    OMBlockImpl(OMElement busObject, BlockFactory factory) {
        super(busObject,
              null,
              busObject.getQName(),
              factory);
    }

    @Override
    protected Object _getBOFromReader(XMLStreamReader reader, Object busContext)
            throws XMLStreamException, WebServiceException {
        // Take a shortcut and return the OMElement
        return this.getOMElement();
    }

    @Override
    protected XMLStreamReader _getReaderFromBO(Object busObj, Object busContext)
            throws XMLStreamException, WebServiceException {
        OMElement om = (OMElement)busObj;
        return om.getXMLStreamReader();
    }
    
    @Override
    protected Object _getBOFromOM(OMElement om, Object busContext)
        throws XMLStreamException, WebServiceException {
        return om;
    }
    
    @Override
    protected OMElement _getOMFromBO(Object busObject, Object busContext)
        throws XMLStreamException, WebServiceException {
        return (OMElement) busObject;
    }

    @Override
    protected void _outputFromBO(Object busObject, Object busContext, XMLStreamWriter writer)
            throws XMLStreamException, WebServiceException {
        OMElement om = (OMElement)busObject;
        om.serialize(writer);
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
