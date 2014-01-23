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

package org.apache.axis2.jaxws.description.impl;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;

import org.apache.axis2.jaxws.description.xml.handler.HandlerChainType;
import org.apache.axis2.jaxws.description.xml.handler.HandlerChainsType;
import org.apache.axis2.jaxws.description.xml.handler.HandlerType;
import org.apache.axis2.util.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class HandlerChainsParser {

    private static final String JAVA_EE_NS = "http://java.sun.com/xml/ns/javaee";
    private static final QName QNAME_HANDLER_CHAINS = new QName(JAVA_EE_NS, "handler-chains");
    private static final QName QNAME_HANDLER_CHAIN = new QName(JAVA_EE_NS, "handler-chain");
    private static JAXBContext context;
    
    public HandlerChainsType loadHandlerChains(InputStream in) throws Exception {       
        Document document = XMLUtils.newDocument(in);
        Element el = document.getDocumentElement();
        if (!JAVA_EE_NS.equals(el.getNamespaceURI()) ||
            !"handler-chains".equals(el.getLocalName())) {
                throw new WebServiceException("Unexpected element {" + el.getNamespaceURI() + "}" + el.getLocalName() + ". Expected " + QNAME_HANDLER_CHAINS + " element");
        }

        HandlerChainsType handlerChains = new HandlerChainsType();
        Node node = el.getFirstChild();
        while (node != null) {
            if (node instanceof Element) {
                el = (Element)node;
                if (!JAVA_EE_NS.equals(el.getNamespaceURI()) ||
                    !el.getLocalName().equals("handler-chain")) {                
                    throw new WebServiceException("Unexpected element {" + el.getNamespaceURI() + "}" + el.getLocalName() + ". Expected " + QNAME_HANDLER_CHAIN + " element");
                }
                handlerChains.getHandlerChain().add(processHandlerChainElement(el));
            }
            node = node.getNextSibling();
        }

        return handlerChains;
    }
    
    private HandlerChainType processHandlerChainElement(Element el) throws Exception {
        HandlerChainType handler = new HandlerChainType();
        Node node = el.getFirstChild();
        while (node != null) {
            Node cur = node;
            node = node.getNextSibling();
            if (cur instanceof Element) {
                el = (Element)cur;
                if (!JAVA_EE_NS.equals(el.getNamespaceURI())) {
                    throw new WebServiceException();
                }
                String name = el.getLocalName();
                if ("port-name-pattern".equals(name)) {
                    handler.setPortNamePattern(processPatternElement(el));
                } else if ("service-name-pattern".equals(name)) {
                    handler.setServiceNamePattern(processPatternElement(el));
                } else if ("protocol-bindings".equals(name)) {
                    handler.getProtocolBindings().addAll(processProtocolBindingsElement(el));
                } else if ("handler".equals(name)) {
                    handler.getHandler().add(processHandlerElement(el));
                }
            }
        }
        return handler;
    }
    
    private List<String> processProtocolBindingsElement(Element el) {
        String protocolBindingsString = el.getTextContent().trim();
        String [] protocolBindings = protocolBindingsString.split("\\s+");
        return Arrays.asList(protocolBindings);
    }
    
    private QName processPatternElement(Element el) throws Exception {
        String namePattern = el.getTextContent().trim();
        
        // see BaseHandlerResolver.validatePattern for valid strings
        
        if ("*".equals(namePattern)) {
            return new QName("*");
        }
        
        if (!namePattern.contains(":")) {
            return new QName("", namePattern, "");
        }
        
        String localPart = namePattern.substring(namePattern.indexOf(':') + 1,
                                                 namePattern.length());
        String pfx = namePattern.substring(0, namePattern.indexOf(':'));
        String ns = el.lookupNamespaceURI(pfx);
        if (ns == null) {
            ns = pfx;
        }
        // populate prefix so BaseHandlerResolver.validatePattern can validate it
        // QName ctor is QName(namespace, localpart, prefix)
        return new QName(ns, localPart, pfx);
    }
    
    private HandlerType processHandlerElement(Element el) throws Exception {      
        JAXBContext ctx = getContextForHandlerType();
        Unmarshaller unmarshaller = ctx.createUnmarshaller();
        HandlerType handler = unmarshaller.unmarshal(el, HandlerType.class).getValue();
        return handler;
    }
    
    private static synchronized JAXBContext getContextForHandlerType()
        throws JAXBException {
        if (context == null) {
            context = JAXBContext.newInstance(HandlerType.class);
        }
        return context;
    }
}
