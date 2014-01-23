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

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.factory.SAAJConverterFactory;
import org.apache.axis2.jaxws.message.util.SAAJConverter;
import org.apache.axis2.jaxws.registry.FactoryRegistry;

import javax.xml.soap.SOAPEnvelope;
import javax.xml.ws.WebServiceException;

/**
 * XMLPartImpl
 * <p/>
 * This class extends the implementation of the XMLPartBase so that it can define the
 * transformations between OM, SAAJ SOAPEnvelope and XMLSpine.
 *
 * @see org.apache.axis2.jaxws.impl.XMLPartBase
 */
public class XMLPartImpl extends XMLPartBase {

    SAAJConverter converter = null;

    /**
     * XMLPart should be constructed via the XMLPartFactory. This constructor constructs an empty
     * XMLPart with the specified protocol
     *
     * @param protocol
     * @throws WebServiceException
     */
    XMLPartImpl(Protocol protocol) throws WebServiceException {
        super(protocol);
    }

    /**
     * XMLPart should be constructed via the XMLPartFactory. This constructor creates an XMLPart from
     * the specified root.
     *
     * @param root
     * @param protocol (if null, the soap protocol is inferred from the namespace)
     * @throws WebServiceException
     */
    XMLPartImpl(OMElement root, Protocol protocol) throws WebServiceException {
        super(root, protocol);
    }

    /**
     * XMLPart should be constructed via the XMLPartFactory. This constructor creates an XMLPart from
     * the specified root.
     *
     * @param root
     * @throws WebServiceException
     */
    XMLPartImpl(SOAPEnvelope root) throws WebServiceException {
        super(root);
    }

    @Override
    protected OMElement _convertSE2OM(SOAPEnvelope se) throws WebServiceException {
        Attachments attachments = (parent == null) ? 
                null : parent.attachments;
        return getSAAJConverter().toOM(se, attachments);
    }

    @Override
    protected OMElement _convertSpine2OM(XMLSpine spine) throws WebServiceException {

        OMElement omEnvelope = spine.getAsOMElement();
        return omEnvelope;
    }

    @Override
    protected SOAPEnvelope _convertOM2SE(OMElement om) throws WebServiceException {
        return getSAAJConverter().toSAAJ((org.apache.axiom.soap.SOAPEnvelope)om);
    }

    @Override
    protected SOAPEnvelope _convertSpine2SE(XMLSpine spine) throws WebServiceException {
        return _convertOM2SE(_convertSpine2OM(spine));
    }

    @Override
    protected XMLSpine _convertOM2Spine(OMElement om) throws WebServiceException {
        return new XMLSpineImpl((org.apache.axiom.soap.SOAPEnvelope)om, getStyle(),
                                getIndirection(), getProtocol());
    }

    @Override
    protected XMLSpine _convertSE2Spine(SOAPEnvelope se) throws WebServiceException {
        return _convertOM2Spine(_convertSE2OM(se));
    }

    /**
     * Load the SAAJConverter
     * @return SAAJConverter
     */
    protected SAAJConverter getSAAJConverter() {
        if (converter == null) {
            SAAJConverterFactory factory = (
                    SAAJConverterFactory)FactoryRegistry.getFactory(SAAJConverterFactory.class);
            converter = factory.getSAAJConverter();
        }
        return converter;
    }
	
}
