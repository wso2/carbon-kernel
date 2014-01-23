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

package org.apache.axis2.jaxws.message.factory;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.jaxws.message.Block;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.WebServiceException;

/**
 * BlockFactory
 * <p/>
 * Interface to create Block objects An object is created from either a reader, another Block or the
 * BusinessObject. Some business objects (like JAXB) have an associated context object
 * (JAXBContext), these are also passed to the createMethods.
 * <p/>
 * The implementation of BlockFactories should always be singleton objects and never carry any
 * instance data.
 * <p/>
 * The FactoryRegistry should be used to get access to a Factory
 *
 * @see org.apache.axis2.jaxws.registry.FactoryRegistry
 */
public interface BlockFactory {

    /**
     * createBlock from XMLStreamReader
     *
     * @param reader  XMLStreamReader
     * @param context Associated Context or null
     * @param QName   must be supplied if known
     * @throws XMLStreamException
     * @throws WebServiceException
     */
    public Block createFrom(XMLStreamReader reader, Object context, QName qName)
            throws XMLStreamException, WebServiceException;

    /**
     * createBlock from XMLStreamReader
     *
     * @param omElement OMElement
     * @param context   Associated Context or null
     * @param QName     must be supplied if known
     * @throws XMLStreamException
     * @throws WebServiceException
     */
    public Block createFrom(OMElement omElement, Object context, QName qName)
            throws XMLStreamException, WebServiceException;

    /**
     * createBlock from another Block If the other Block was created with the same factory and has the
     * same context, the other Block is returned. If the other Block was created by a different factory
     * or diffent context, a new block is returned (and the other block is consumed)
     *
     * @param other   Block
     * @param context Associated Context or null
     * @throws XMLStreamException
     * @throws WebServiceException
     */
    public Block createFrom(Block other, Object context)
            throws XMLStreamException, WebServiceException;

    /**
     * Create from business object
     *
     * @param businessObject
     * @param context        Associated Context or null
     * @param QName          must be supplied if known
     * @throws XMLStreamException
     * @throws WebServiceException
     */
    public Block createFrom(Object businessObject, Object context, QName qName)
            throws WebServiceException;

    /**
     * @return true if business object must be rendered as an element
     * example JAXBFactory ...true
     * example SourceFactory...false
     */
    public boolean isElement();
}
