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

package org.apache.axis2.datasource.jaxb;

import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMSourcedElement;
import org.apache.axiom.om.impl.builder.CustomBuilder;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.jaxws.Constants;
import org.apache.axis2.jaxws.handler.HandlerUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPConstants;
import javax.xml.stream.XMLStreamReader;

/**
 * JAXBCustomBuilder creates an OMSourcedElement backed by a JAXBDataSource
 * for the specified namespace and localPart.
 */
public class JAXBCustomBuilder implements CustomBuilder {

    private static final Log log = LogFactory.getLog(JAXBCustomBuilder.class);
    
    JAXBDSContext jdsContext;
    
    /**
     * Create a JAXBCustomBuilder
     * @param context JAXBDSContext
     */
    public JAXBCustomBuilder(JAXBDSContext context) {
        super();
        this.jdsContext = context;
        JAXBCustomBuilderMonitor.updateTotalBuilders();
    }


    public OMElement create(String namespace, 
                            String localPart, 
                            OMContainer parent,
                            XMLStreamReader reader, 
                            OMFactory factory) throws OMException {
        
        if (log.isDebugEnabled()) {
            log.debug("create namespace = " + namespace);
            log.debug("  localPart = " + localPart);
            log.debug("  reader = " + reader.getClass());
        }
        
        // There are some situations where we want to use normal
        // unmarshalling, so return null
        if (!shouldUnmarshal(namespace, localPart)) {
            JAXBCustomBuilderMonitor.updateTotalFailedCreates();
            return null;
        }
        try {
            // Create an OMSourcedElement backed by an unmarshalled JAXB object
            OMNamespace ns = factory.createOMNamespace(namespace, reader.getPrefix());
            
            Object jaxb = jdsContext.unmarshal(reader);
            if (log.isDebugEnabled()) {
                log.debug("Successfully unmarshalled jaxb object " + jaxb);
            }
            
            OMDataSource ds = new JAXBDataSource(jaxb, jdsContext);
            if (log.isDebugEnabled()) {
                log.debug("The JAXBDataSource is " + ds);
            }
            OMSourcedElement omse = factory.createOMElement(ds, localPart, ns);
            
            parent.addChild(omse);
            JAXBCustomBuilderMonitor.updateTotalCreates();
            return omse;
        } catch (JAXBException e) {
            JAXBCustomBuilderMonitor.updateTotalFailedCreates();
            throw new OMException(e);
        }
    }
    
    /**
     * @param namespace
     * @param localPart
     * @return true if this ns and local part is acceptable for unmarshalling
     */
    private boolean shouldUnmarshal(String namespace, String localPart) {
        boolean isHighFidelity = HandlerUtils.isHighFidelity(jdsContext.getMessageContext());

        if (isHighFidelity) {
            if (log.isDebugEnabled()) {
                log.debug("JAXB payload streaming disabled because high fidelity messages are requested.");
            }
            return false;

        }
        
        // Don't unmarshall SOAPFaults or anything else in the SOAP 
        // namespace.
        // Don't unmarshall elements that are unqualified
        if (localPart == null || namespace == null || namespace.length() == 0 ||
            SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE.equals(namespace) ||
            SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE.equals(namespace)) {
            return false;
        }
       
        // Don't unmarshal if this looks like encrypted data
        if (localPart.equals("EncryptedData")) {
            return false;
        }
        
        return true;
                
    }
}
