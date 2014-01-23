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

package org.apache.axis2.jaxws.message.impl;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.factory.BlockFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.WebServiceException;

/** BlockFactoryImpl Abstract Base Class for the Block Factories */
public abstract class BlockFactoryImpl implements BlockFactory {

    public BlockFactoryImpl() {
        super();
    }

    /* (non-Javadoc)
      * @see org.apache.axis2.jaxws.message.BlockFactory#createFrom(org.apache.axis2.jaxws.message.Block, java.lang.Object)
      */
    public Block createFrom(Block other, Object context)
            throws XMLStreamException, WebServiceException {
        // This is the default behavior.  Derived Factories may
        // provide a more performant implementation.
        if (other.getBlockFactory().equals(this)) {
            if (other.getBusinessContext() == null &&
                    context == null) {
                return other;
            } else if (other.getBusinessContext() != null &&
                    other.getBusinessContext().equals(context)) {
                return other;
            }
        }
        QName qName = null;
        if (other.isQNameAvailable()) {
            qName = other.getQName();
        }
        Block newBlock = createFrom(other.getXMLStreamReader(true), context, qName);
        return newBlock;
    }

    public Block createFrom(XMLStreamReader reader, Object context, QName qName)
            throws XMLStreamException, WebServiceException {
        StAXOMBuilder builder = new StAXOMBuilder(reader);
        OMElement omElement = builder.getDocumentElement();
        return createFrom(omElement, context, omElement.getQName());
    }


}
