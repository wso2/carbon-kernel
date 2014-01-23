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

package org.apache.axis2.jaxws.addressing.util;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.util.XMLUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class is used to provide a read-only list of reference parameters
 * via the JAX-WS 2.1 api.
 *
 * @see MessageContext#REFERENCE_PARAMETERS
 */
public class ReferenceParameterList extends AbstractList<Element> {
    private static final Log log = LogFactory.getLog(ReferenceParameterList.class);
    private static final Element[] EMPTY_ARRAY = new Element[0];
    
    private String namespace = AddressingConstants.Final.WSA_NAMESPACE;
    private SOAPHeader header;
    private Element[] referenceParameters;

    public ReferenceParameterList() {
        super();
    }
    
    public ReferenceParameterList(SOAPHeader header) {
        super();
        this.header = header;
    }

    @Override
    public Element get(int index) {
        if (referenceParameters == null)
            initialize();
        
        return referenceParameters[index];
    }

    @Override
    public int size() {
        if (referenceParameters == null)
            initialize();
        
        return referenceParameters.length;
    }
    
    private void initialize() {
        if (header == null) {
            if (log.isTraceEnabled()) {
                log.trace("initialize: No SOAP header to check for reference parameters.");
            }
            
            referenceParameters = EMPTY_ARRAY;            
        }
        else {
            if (log.isTraceEnabled()) {
                log.trace("initialize: Checking SOAP header for reference parameters.");
            }
            
            List<Element> list = new ArrayList<Element>();
            Iterator headerBlocks = header.getChildElements();
            while (headerBlocks.hasNext()) {
                OMElement headerElement = (OMElement)headerBlocks.next();
                OMAttribute isRefParamAttr =
                        headerElement.getAttribute(new QName(namespace, "IsReferenceParameter"));
                if (log.isTraceEnabled()) {
                    log.trace("initialize: Checking header element: " + headerElement.getQName());
                }
                
                if (isRefParamAttr != null && 
                    ("true".equals(isRefParamAttr.getAttributeValue()) ||
                     "1".equals(isRefParamAttr.getAttributeValue()))) {
                    try {
                        Element element = XMLUtils.toDOM(headerElement);
                        list.add(element);
                    }
                    catch (Exception e) {
                        throw ExceptionFactory.
                          makeWebServiceException(Messages.getMessage("referenceParameterConstructionErr"),
                                                  e);
                    }
                    
                    if (log.isTraceEnabled()) {
                        log.trace("initialize: Header: " + headerElement.getQName() +
                                " has IsReferenceParameter attribute. Adding to toEPR.");
                    }
                }
            }
            
            referenceParameters = list.toArray(EMPTY_ARRAY);
        }
    }
}
