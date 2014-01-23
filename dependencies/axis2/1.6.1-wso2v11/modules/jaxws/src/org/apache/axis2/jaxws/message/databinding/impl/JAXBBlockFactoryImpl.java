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
import org.apache.axiom.om.OMSourcedElement;
import org.apache.axis2.datasource.jaxb.JAXBDSContext;
import org.apache.axis2.datasource.jaxb.JAXBDataSource;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.databinding.JAXBBlockContext;
import org.apache.axis2.jaxws.message.factory.JAXBBlockFactory;
import org.apache.axis2.jaxws.message.impl.BlockFactoryImpl;
import org.apache.axis2.jaxws.utility.XMLRootElementUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.ws.WebServiceException;

/** JAXBBlockFactoryImpl Creates a JAXBBlock */
public class JAXBBlockFactoryImpl extends BlockFactoryImpl implements JAXBBlockFactory {
    private static final Log log = LogFactory.getLog(JAXBBlockFactoryImpl.class);

    /** Default Constructor required for Factory */
    public JAXBBlockFactoryImpl() {
        super();
    }

    /* (non-Javadoc)
      * @see org.apache.axis2.jaxws.message.BlockFactory#createFrom(org.apache.axiom.om.OMElement, java.lang.Object, javax.xml.namespace.QName)
      */
    public Block createFrom(OMElement omElement, Object context, QName qName)
            throws XMLStreamException, WebServiceException {
        // The context for a JAXBFactory must be non-null and should be a JAXBBlockContext.
        if (context == null) {
            // JAXWS spec 4.3.4 conformance requires a WebServiceException whose cause is JAXBException
            throw ExceptionFactory.makeWebServiceException(
                    new JAXBException(Messages.getMessage("JAXBBlockFactoryErr1", "null")));
        } else if (context instanceof JAXBBlockContext) {
            ;
        } else {
            // JAXWS spec 4.3.4 conformance requires a WebServiceException whose cause is JAXBException
            throw ExceptionFactory.makeWebServiceException(new JAXBException(
                    Messages.getMessage("JAXBBlockFactoryErr1", context.getClass().getName())));
        }
        if (qName == null) {
            qName = omElement.getQName();
        }
        
        if (omElement instanceof OMSourcedElement) {
            
            if ( ((OMSourcedElement) omElement).getDataSource() instanceof JAXBDataSource) {
                JAXBDataSource ds = (JAXBDataSource) ((OMSourcedElement)omElement).getDataSource();
                JAXBDSContext dsContext = ds.getContext();
                try {
                    if (dsContext.getJAXBContext() == ((JAXBBlockContext)context).getJAXBContext()) {
                        // Shortcut, use existing JAXB object
                        Object jaxb = ds.getObject();
                        return new JAXBBlockImpl(jaxb, (JAXBBlockContext)context, qName, this);
                    }
                } catch (JAXBException e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Falling back to using normal unmarshalling approach. " + e.getMessage());
                    }
                }
            } else if ( ((OMSourcedElement) omElement).getDataSource() instanceof JAXBBlockImpl) {
                JAXBBlockImpl block = (JAXBBlockImpl) ((OMSourcedElement)omElement).getDataSource();
                JAXBBlockContext blockContext = (JAXBBlockContext) block.getBusinessContext();
                try {
                    if (blockContext.getJAXBContext() == ((JAXBBlockContext)context).getJAXBContext()) {
                        // Shortcut, use existing JAXB object
                        Object jaxb = block.getObject();
                        return new JAXBBlockImpl(jaxb, (JAXBBlockContext)context, qName, this);
                    }
                } catch (JAXBException e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Falling back to using normal unmarshalling approach. " + e.getMessage());
                    }
                }
            }
        }
        
        return new JAXBBlockImpl(omElement, (JAXBBlockContext)context, qName, this);
    }

    /* (non-Javadoc)
      * @see org.apache.axis2.jaxws.message.BlockFactory#createFrom(java.lang.Object, java.lang.Object, javax.xml.namespace.QName)
      */
    public Block createFrom(Object businessObject, Object context, QName qName)
            throws WebServiceException {

        // The context must be non-null and should be a JAXBBlockContext.
        // For legacy reasons, a JAXBContext is also supported (and wrapped into a JAXBBlockContext)
        if (context == null) {
            // JAXWS spec 4.3.4 conformance requires a WebServiceException whose cause is JAXBException
            throw ExceptionFactory.makeWebServiceException(
                    new JAXBException(Messages.getMessage("JAXBBlockFactoryErr1", "null")));
        } else if (context instanceof JAXBBlockContext) {
            ;
        } else {
            // JAXWS spec 4.3.4 conformance requires a WebServiceException whose cause is JAXBException
            throw ExceptionFactory.makeWebServiceException(new JAXBException(
                    Messages.getMessage("JAXBBlockFactoryErr1", context.getClass().getName())));
        }

        // The business object must be either a JAXBElement or a block with an @XmlRootElement qname.  
        // (Checking this is expensive, so it is assumed)
        // The input QName must be set otherwise we have to look it up, which kills performance.
        if (qName == null) {
            qName = XMLRootElementUtil.getXmlRootElementQNameFromObject(businessObject);
        }

        try {
            return new JAXBBlockImpl(businessObject, (JAXBBlockContext)context, qName, this);
        } catch (JAXBException e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

    public boolean isElement() {
        return true;
    }

}
