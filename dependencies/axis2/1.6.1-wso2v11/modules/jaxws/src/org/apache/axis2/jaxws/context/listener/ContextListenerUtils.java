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

package org.apache.axis2.jaxws.context.listener;
/**
 * Utility Class that holds a ways to registerProviderOMListener and oter operations 
 * used by ParsedEntityCustom Builder.
 */
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.message.databinding.ParsedEntityReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ContextListenerUtils {
    private static final Log log = 
        LogFactory.getLog(ContextListenerUtils.class);
    public static void registerProviderOMListener(MessageContext mc){
        if(log.isDebugEnabled()){
            log.debug("Creating ProviderOMContextListener");
        }
        ProviderOMContextListener.create(mc.getAxisMessageContext().getServiceContext());
    }
    
    public static InputStream createPayloadElement(InputStream payloadContent, OMNamespace ns, String localPart, OMContainer parent, HashMap<String, String> nsElementDecls, HashMap<String, String> attrElementDecls){
        CompositeInputStream inputStream = new CompositeInputStream();
        InputStream startTag = getStartTag(ns, localPart, parent, nsElementDecls, attrElementDecls);
        InputStream endTag = getEndTag(ns, localPart);
        //Add Element startTag
        ((CompositeInputStream)inputStream).append(startTag);
        //Add Element content
        ((CompositeInputStream)inputStream).append(payloadContent);
        //Add Element endTag
        ((CompositeInputStream)inputStream).append(endTag);

        return inputStream;

    }
        
    public static int skipEventsTo(int targetEvent, XMLStreamReader parser) throws XMLStreamException {
        int eventType = 0;
        while (parser.hasNext()) {
            eventType = parser.next();
            if (eventType == targetEvent)
                return eventType;
        }
        return eventType; // return END_DOCUMENT;
    }
    
    
    private static InputStream getEndTag(OMNamespace ns, String localPart){
        if(log.isDebugEnabled()){
            log.debug("Start ParsedEntityDataSource.Data.getEndTag()");
        }
        String endElement = null;
        String prefix = (ns!=null)?ns.getPrefix():null;
        String uri = (ns!=null)?ns.getNamespaceURI():null;
        if(prefix!=null && prefix.length()>0){
            endElement = "</"+prefix+":"+localPart+">";
        }else{
            endElement = "</"+localPart+">";
        }
        if(log.isDebugEnabled()){
            log.debug("End ParsedEntityDataSource.Data.getEndTag()");
        }
        return new ByteArrayInputStream(endElement.getBytes());
    }
    /*
     * get startElement using namespace and local part. Add all namespace prefixes from parent elements.
     */
    private static InputStream getStartTag(OMNamespace ns, String localPart, OMContainer parent, HashMap<String, String> nsElementDecls, HashMap<String, String> attrElementDecls){
        if(log.isDebugEnabled()){
            log.debug("Start ParsedEntityDataSource.Data.getStartTag()");
        }            
        //Start creating the element.
        StringBuffer startElement = new StringBuffer();
        String prefix = (ns!=null)?ns.getPrefix():null;
        String uri = (ns!=null)?ns.getNamespaceURI():null;
        
        HashMap<String, String> nsDecls = new HashMap<String, String>();
        //Get all of the namespaces associated with Body, envelope, etc
        getParentnsdeclarations(nsDecls, parent);
        
        nsDecls.putAll(nsElementDecls);
        
        if(prefix!=null && prefix.length()>0){
            startElement.append("<"+prefix+":"+localPart+ " ");
            if (!nsDecls.containsKey(prefix) || !nsDecls.get(prefix).equals(uri)){
              nsDecls.put(prefix, uri);
            }
        }else{
            startElement.append("<"+localPart + " ");
        }
        addParentNs(startElement, parent, nsDecls);
        addAttrs(startElement, attrElementDecls);
        
        if(log.isDebugEnabled()){
          log.debug("StartElement ="+startElement);
        }

        if(log.isDebugEnabled()){
            log.debug("End ParsedEntityDataSource.Data.getStartTag()");
        }
        return new ByteArrayInputStream(startElement.toString().getBytes());
    }
    /*
     * fetch all prent namespace declarations
     */
    private static void getParentnsdeclarations(HashMap<String, String> nsDecls, OMContainer parent){
        if(nsDecls == null){
            nsDecls = new HashMap<String, String>();
        }
        while (parent instanceof OMElement){
            OMElement omElement = (OMElement) parent;
            Iterator ite = omElement.getAllDeclaredNamespaces();
            while (ite.hasNext()) {
                OMNamespace omn = (OMNamespace) ite.next();
                String prefix = omn.getPrefix();
                String nsUri = omn.getNamespaceURI();
                if (!nsDecls.containsKey(prefix)) {
                    nsDecls.put(prefix, nsUri);
                }
            }
            parent = omElement.getParent();
        }
    }
    /*
     * add all parent namespace declarations to the element
     */
    private static void addParentNs(StringBuffer startElement, OMContainer parent, HashMap<String, String> nsDecls){
        Iterator<Map.Entry<String, String>> iter = nsDecls.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, String> entry = iter.next();
            String prefix = entry.getKey();
            String uri = entry.getValue();
            if ("".equals(prefix))
                startElement.append(" xmlns=\"");
            else {
                startElement.append(" xmlns:");
                startElement.append(prefix);
                startElement.append("=\"");
            }
            startElement.append(uri);
            startElement.append("\"");
        }
    }

    private static void addAttrs(StringBuffer startElement, HashMap<String, String> attrDecls)
    {
      Iterator<Map.Entry<String, String>> iter = attrDecls.entrySet().iterator();
      while (iter.hasNext()) {
        Map.Entry<String, String> entry = iter.next();
        String compoundName = entry.getKey();
        String value = entry.getValue();
        startElement.append(" ");
        startElement.append(compoundName);
        startElement.append("=\"");
        startElement.append(value);
        startElement.append("\"");
      }
      startElement.append(">");
    }
}
