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

package org.apache.axis2.jaxws.addressing.factory.impl;

import org.apache.axis2.addressing.AddressingConstants.Final;
import org.apache.axis2.addressing.AddressingConstants.Submission;
import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.addressing.SubmissionEndpointReference;
import org.apache.axis2.jaxws.addressing.factory.JAXWSEndpointReferenceFactory;
import org.apache.axis2.jaxws.i18n.Messages;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import java.security.PrivilegedExceptionAction;

/**
 * This class is used to generate instances of the following subclasses of
 * {@link EndpointReference}.
 *
 * @see javax.xml.ws.wsaddressing.W3CEndpointReference
 * @see org.apache.axis2.jaxws.addressing.SubmissionEndpointReference
 */
public class JAXWSEndpointReferenceFactoryImpl implements JAXWSEndpointReferenceFactory {
    private JAXBContext jaxbContext;

    /**
     * Constructor
     */
    public JAXWSEndpointReferenceFactoryImpl() {
        super();

        try { 
            jaxbContext =(JAXBContext)
              AccessController.doPrivileged(
                 new PrivilegedExceptionAction() {
                                                
                     public Object run() throws JAXBException {
                         return JAXBContext.newInstance(W3CEndpointReference.class,
                                                        SubmissionEndpointReference.class);
                     }
                 });
        }
        catch (Exception e) {
            throw new WebServiceException(Messages.getMessage("jaxbContextFailure", 
                                                              e.toString()));
        }
    }
    
    /*
     *  (non-Javadoc)
     * @see org.apache.axis2.jaxws.addressing.factory.JAXWSEndpointReferenceFactory#createEndpointReference(javax.xml.transform.Source)
     */
    public EndpointReference createEndpointReference(Source eprInfoset) throws JAXBException {
        Unmarshaller um = jaxbContext.createUnmarshaller();
        return (EndpointReference) um.unmarshal(eprInfoset);
    }

    /*
     *  (non-Javadoc)
     * @see org.apache.axis2.jaxws.addressing.factory.JAXWSEndpointReferenceFactory#getAddressingNamespace(java.lang.Class)
     */
    public <T extends EndpointReference> String getAddressingNamespace(Class<T> clazz) {
        String addressingNamespace = null;
        
        if (W3CEndpointReference.class.isAssignableFrom(clazz)) {
            addressingNamespace = Final.WSA_NAMESPACE;
        } else if (SubmissionEndpointReference.class.isAssignableFrom(clazz)) {
            addressingNamespace = Submission.WSA_NAMESPACE;
        } else  {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("unknownClassType", 
                                                           clazz.toString()));
        }
        
        return addressingNamespace;
    }
}
