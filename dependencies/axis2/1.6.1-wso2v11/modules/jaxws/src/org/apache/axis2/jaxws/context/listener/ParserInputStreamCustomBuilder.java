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

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;

import javax.xml.soap.SOAPConstants;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMSourcedElement;
import org.apache.axiom.om.ds.ParserInputStreamDataSource;
import org.apache.axiom.om.impl.builder.CustomBuilder;
import org.apache.axis2.datasource.jaxb.JAXBCustomBuilderMonitor;
import org.apache.axis2.jaxws.handler.HandlerUtils;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.jaxws.message.databinding.ParsedEntityReader;
import org.apache.axis2.jaxws.message.factory.ParsedEntityReaderFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**
 * A custom builder to that does the following:
 * 1) Uses the parser to fetch the inputStream if parser supports reading of element contents
 * 2) Use the inputStream to create a DataSource backed by the InputStream read from Parser.
 * 3) Use the OMFactory to create OMSourcedElement, OMSourcedElement is backed by ParsedEntityDataSource.
 */

public class ParserInputStreamCustomBuilder implements CustomBuilder {
    private static final Log log = 
        LogFactory.getLog(ParserInputStreamCustomBuilder.class);

    private String encoding = null;

    /**
     * Constructor
     * @param encoding 
     */
    public ParserInputStreamCustomBuilder(String encoding) {
        this.encoding = (encoding == null) ? "utf-8" :encoding;
    }
    /*
     * (non-Javadoc)
     * @see org.apache.axiom.om.impl.builder.CustomBuilder#create(java.lang.String, java.lang.String, org.apache.axiom.om.OMContainer, javax.xml.stream.XMLStreamReader, org.apache.axiom.om.OMFactory)
     */
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
        
        if (!shouldUnmarshal(namespace, localPart)) {
            if (log.isDebugEnabled()) {
                log.debug("This element won't be unmarshalled with the custom builder");
            }
            return null;
        }
        
        /*
         * 1) Use the the parser to fetch the inputStream
         * 2) Use the inputStream to create a DataSource, delay reading of content as much as you can.
         * 3) Use the OMFactory to create OMSourcedElement, OMSourcedElement is backed by ParsedEntityDataSource.
         */
        try{
            ParsedEntityReaderFactory perf = (ParsedEntityReaderFactory)FactoryRegistry.getFactory(ParsedEntityReaderFactory.class);
            ParsedEntityReader entityReader = perf.getParsedEntityReader();
            if (log.isDebugEnabled()) {
                log.debug("ParsedEntityReader = " + entityReader);
            }
            //Do not user custom builder if Parser does not have ability to read sub content.
            if(!entityReader.isParsedEntityStreamAvailable()){
                if (log.isDebugEnabled()) {
                    log.debug("ParsedEntityStream is not available, defaulting to normal build");
                }
                return null;
            }
            // Create an OMSourcedElement backed by the ParsedData
            InputStream parsedStream = getPayloadContent(reader, entityReader);
            if(parsedStream == null){
                //cant read content from EntityReader, returning null.
                if (log.isDebugEnabled()) {
                    log.debug("Unable to read content from the entity reader, defaulting to normal build");
                }
                return null;
            }
            HashMap<String, String> nsElementDecls = getElementNamespaceDeclarations(reader);
            HashMap<String, String> attrElementDecls = getElementAttributeDeclarations(reader);
            
            //read the payload. Lets move the parser forward.
            if(reader.hasNext()){
                reader.next();
            }
            if(namespace == null){
                //lets look for ns in reader
                namespace = reader.getNamespaceURI();
                if(namespace == null){
                    //still cant find the namespace, just set it to "";
                    namespace = "";
                }
            }
            OMNamespace ns = factory.createOMNamespace(namespace, reader.getPrefix());
            InputStream payload = ContextListenerUtils.createPayloadElement(parsedStream, ns, localPart, parent, 
                        nsElementDecls, attrElementDecls);

            ParserInputStreamDataSource ds = new ParserInputStreamDataSource(payload, encoding);
            OMSourcedElement om = null;
            if (parent instanceof SOAPHeader && factory instanceof SOAPFactory) {
                om = ((SOAPFactory)factory).createSOAPHeaderBlock(localPart, ns, ds);
            } else {
                om = factory.createOMElement(ds, localPart, ns);
            }           
            //Add the new OMSourcedElement ot the parent
            parent.addChild(om); 
            /*
            //Lets Mark the body as complete so Serialize calls dont fetch data from parser for body content.
            if(parent instanceof SOAPBodyImpl){
                ((SOAPBodyImpl)parent).setComplete(true);
            }
            */
            return om;
        } catch (OMException e) {
            throw e;
        } catch (Throwable t) {
            throw new OMException(t);
        }
    }

    public OMElement create(String namespace, 
        String localPart, 
        OMContainer parent,
        XMLStreamReader reader, 
        OMFactory factory,
        InputStream payload) throws OMException {

        if (log.isDebugEnabled()) {
            log.debug("create namespace = " + namespace);
            log.debug("  localPart = " + localPart);
            log.debug("  reader = " + reader.getClass());
        }
        /*
         * 1) Use the the parser to fetch the inputStream
         * 2) Use the inputStream to create a DataSource, delay reading of content as much as you can.
         * 3) Use the OMFactory to create OMSourcedElement, OMSourcedElement is backed by ParsedEntityDataSource.
         */
        try{
            if(namespace == null){
                //lets look for ns in reader
                namespace = reader.getNamespaceURI();
                if(namespace == null){
                    //still cant find the namespace, just set it to "";
                    namespace = "";
                }
            }
            if (!shouldUnmarshal(namespace, localPart)) {
                if (log.isDebugEnabled()) {
                    log.debug("This element won't be unmarshalled with the custom builder");
                }
                return null;
            }
            OMNamespace ns = factory.createOMNamespace(namespace, reader.getPrefix());
            ParserInputStreamDataSource ds = new ParserInputStreamDataSource(payload, encoding);
            OMSourcedElement om = null;
            if (parent instanceof SOAPHeader && factory instanceof SOAPFactory) {
                om = ((SOAPFactory)factory).createSOAPHeaderBlock(localPart, ns, ds);
            } else {
                om = factory.createOMElement(ds, localPart, ns);
            }           
            //Add the new OMSourcedElement ot the parent
            parent.addChild(om); 
            return om;
        } catch (OMException e) {
            throw e;
        } catch (Throwable t) {
            throw new OMException(t);
        }
    }

    private HashMap<String, String> getElementNamespaceDeclarations(XMLStreamReader reader)
    {
      HashMap<String, String> nsElementDecls = new HashMap<String, String>();
      int count = reader.getNamespaceCount();
      for (int i = 0; i < count; i++){
        String prefix = reader.getNamespacePrefix(i);
        String namespace = reader.getNamespaceURI(i);
        if (namespace != null && namespace.length() > 0){
          nsElementDecls.put(prefix == null ? "":prefix, namespace);
        }
      }
      return nsElementDecls;
    }
    
    private HashMap<String, String> getElementAttributeDeclarations(XMLStreamReader reader)
    {
      HashMap<String, String> attrElementDecls = new HashMap<String, String>();
      int count = reader.getAttributeCount();

      for (int i = 0; i < count; i++) {
        String prefix = reader.getAttributePrefix(i);
        String name = reader.getAttributeLocalName(i);
        String value = convertEntityReferences(reader.getAttributeValue(i));
        String compoundName;
        if (prefix != null && prefix.length() > 0){
          compoundName = prefix+":"+name;
        }
        else {
          compoundName = name;
        }
        attrElementDecls.put(compoundName, value);
      }
      return attrElementDecls;
    }
    
    protected String convertEntityReferences(String value)
    {
      if ((value == null) || (value.length() == 0))
        return value;
      
      int valueLen = value.length();
      
      int[] positionsToChange = null;
      int numChanged = 0;
      
      for (int i = 0; i < valueLen; i++) {
        switch (value.charAt(i)) {
          case '<':
          case '>':
          case '&':
          case '\"':
          case '\'':
            if (positionsToChange == null)
            {
              positionsToChange = new int[valueLen];
            }
            positionsToChange[numChanged++]=i;
            break;
        }
      }

      if (numChanged == 0) {
        if(log.isDebugEnabled())
        {
          log.debug("No entity references were found in "+value);
        }
        return value;
      }
      else {
        if(log.isDebugEnabled())
        {
          log.debug("Found "+numChanged+" entity references in "+value);
        }
        
        //We'll create the new builder assuming the size of the worst case
        StringBuilder changedValue = new StringBuilder(valueLen+numChanged*5);
        int changedPos = 0; 
        for (int i = 0; i < valueLen; i++) {
          if (i == positionsToChange[changedPos]) {
            switch (value.charAt(i)) {
              case '<':
                changedValue.append("&lt;");
                changedPos++;
                break;
              case '>':
                changedValue.append("&gt;");
                changedPos++;
                break;
              case '&':
                changedValue.append("&amp;");
                changedPos++;
                break;
              case '\'':
                changedValue.append("&apos;");
                changedPos++;
                break;
              case '\"':
                changedValue.append("&quot;");
                changedPos++;
                break;
            }
          }
          else {
            changedValue.append(value.charAt(i));
          }
        }

        if(log.isDebugEnabled())
        {
          log.debug("Converted to "+changedValue.toString());
        }

        return changedValue.toString();
      }
    }
    /*
     * Read content from entityReader.
     */
    private InputStream getPayloadContent(XMLStreamReader parser, ParsedEntityReader entityReader){
        int event = parser.getEventType();
        //Make sure its start element event.
        if(log.isDebugEnabled()){
            log.debug("checking if event is START_ELEMENT");
        }
        InputStream parsedStream = null;
        if(event == XMLStreamConstants.START_ELEMENT){
            if(log.isDebugEnabled()){
                log.debug("event is START_ELEMENT");
            }
            parsedStream = entityReader.readParsedEntityStream(parser);
            if(parsedStream!=null){
                if(log.isDebugEnabled()){
                    log.debug("Read Parsed EntityStream");
                }
            }
        }
        return parsedStream;
    }
    
    /**
     * @param namespace
     * @param localPart
     * @return true if this ns and local part is acceptable for unmarshalling
     */
    private boolean shouldUnmarshal(String namespace, String localPart) {
        
        /**
         * The stream preserves the original message, so I think
         * we want to do unmarshal even if high fidelity is specified.
         
        boolean isHighFidelity = HandlerUtils.isHighFidelity(msgContext);

        if (isHighFidelity) {
            return false;
        }
        */
        
        // Don't unmarshal SOAPFaults.
        // If there is no localPart, this also indicates a potential problem...so don't 
        // use the custom builder
        if (localPart == null || 
            (localPart.equals("Fault") &&
             (SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE.equals(namespace) ||
              SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE.equals(namespace)))) {
            return false;
        }
       
        
        /**
         * For JAXB custom building, we ignore security elements.
         * I don't think it matters for parsed entities since they preserve all the content
        if (localPart.equals("EncryptedData")) {
            return false;
        }
        */
        
        return true;
                
    }
}
