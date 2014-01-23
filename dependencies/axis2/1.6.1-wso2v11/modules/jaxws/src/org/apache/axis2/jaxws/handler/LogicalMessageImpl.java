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

package org.apache.axis2.jaxws.handler;

import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.databinding.JAXBBlockContext;
import org.apache.axis2.jaxws.message.factory.BlockFactory;
import org.apache.axis2.jaxws.message.factory.JAXBBlockFactory;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.message.factory.SourceBlockFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.LogicalMessage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;

public class LogicalMessageImpl implements LogicalMessage {

    private static final Log log = LogFactory.getLog(LogicalMessageImpl.class);

    private MEPContext mepCtx;

    protected LogicalMessageImpl(MEPContext m) {
        mepCtx = m;
    }
    
    /*
     * (non-Javadoc)
     * @see javax.xml.ws.LogicalMessage#getPayload()
     */
    public Source getPayload() {
        BlockFactory factory = (SourceBlockFactory) FactoryRegistry.getFactory(SourceBlockFactory.class);
        Source payload = (Source) _getPayload(null, factory);
        return payload;
    }

    /*
     * (non-Javadoc)
     * @see javax.xml.ws.LogicalMessage#getPayload(javax.xml.bind.JAXBContext)
     */
    public Object getPayload(JAXBContext context) {
        if (log.isDebugEnabled()) {
            log.debug("Retreiving the message payload as a Source object");
        }
        
        BlockFactory factory = (JAXBBlockFactory) FactoryRegistry.getFactory(JAXBBlockFactory.class);
        JAXBBlockContext jbc = new JAXBBlockContext(context);
        Object payload = _getPayload(jbc, factory);
        return payload;
    }
    
    private Object _getPayload(Object context, BlockFactory factory) {
        Object payload = null;
        try {
            Block block = mepCtx.getMessageObject().getBodyBlock(context, factory);
            if (block != null) {
               if (log.isDebugEnabled()) {
                       log.debug("A message payload was found.");
               }
               Object content = block.getBusinessObject(true);
               
               // For now, we have to create a new Block from the original content
               // and set that back on the message.  The Block is not currently
               // able to create a copy of itself just yet.
               Payloads payloads = createPayloads(content);
               _setPayload(payloads.CACHE_PAYLOAD, context, factory);
  
               payload = payloads.HANDLER_PAYLOAD;             
            }
            else {
                // If the block was null, then let's return an empty
                // Source object rather than a null.
                if (log.isDebugEnabled()) {
                    log.debug("There was no payload to be found.  Returning an empty Source object");
                }
                byte[] bytes = new byte[0];
                payload = new StreamSource(new ByteArrayInputStream(bytes));
           }
   
        } catch (XMLStreamException e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
        
        return payload;
    }

    /*
     * (non-Javadoc)
     * @see javax.xml.ws.LogicalMessage#setPayload(java.lang.Object, javax.xml.bind.JAXBContext)
     */
    public void setPayload(Object obj, JAXBContext context) {
        BlockFactory factory = (JAXBBlockFactory) FactoryRegistry.getFactory(JAXBBlockFactory.class);
        JAXBBlockContext jbc = new JAXBBlockContext(context);
        _setPayload(obj, jbc, factory);
    }

    /*
     * (non-Javadoc)
     * @see javax.xml.ws.LogicalMessage#setPayload(javax.xml.transform.Source)
     */
    public void setPayload(Source source) {
        BlockFactory factory = (SourceBlockFactory) FactoryRegistry.getFactory(SourceBlockFactory.class);
        _setPayload(source, null, factory);
    }
    
    private void _setPayload(Object object, Object context, BlockFactory factory) {
        Block block = factory.createFrom(object, context, null);
        
        if (mepCtx.getMessageObject() != null) {
            if (!mepCtx.getMessageObject().isFault()) {
                mepCtx.getMessageObject().setBodyBlock(block);
            }
            else {
                if (log.isDebugEnabled()) {
                    log.debug("The payload contains a fault");
                }
                
                mepCtx.getMessageObject().setBodyBlock(block);
                
                // If the payload is a fault, then we can't set it back on the message
                // as a block.  Blocks are OMSourcedElements, and faults cannot be OMSourcedElements.  
                try {
                    SOAPEnvelope env = (SOAPEnvelope) mepCtx.getMessageObject().getAsOMElement();
                    String content = env.toStringWithConsume();
                   
                    MessageFactory mf = (MessageFactory) FactoryRegistry.getFactory(MessageFactory.class);
                    StringReader sr = new StringReader(content);
                    XMLStreamReader stream = StAXUtils.createXMLStreamReader(sr);
                    Message msg = mf.createFrom(stream, mepCtx.getMessageObject().getProtocol());
                   
                    // This is required for proper serialization of the OM structure.
                    msg.getAsOMElement().build();
                    
                    mepCtx.setMessage(msg);
                } catch (Exception e) {
                    throw ExceptionFactory.makeWebServiceException(e);
                }
            }
        }
    }

    private Payloads createPayloads(Object content) {
        if (content == null) {
            return null;
        }
                
        Payloads payloads = new Payloads();
                
        if (Source.class.isAssignableFrom(content.getClass())) {
            try {
                Transformer trans = TransformerFactory.newInstance().newTransformer();
                        
                // First we have to get the content out of the original
                // Source object so we can build the cache from there.
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                StreamResult result = new StreamResult(baos);
                        
                Source source = (Source) content;
                trans.transform(source, result);
                trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                byte[] bytes = baos.toByteArray();
                        
                // Given that we've consumed the original Source object, 
                // we need to create another one with the original content
                // and assign it back.
                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                try {
                    // The Source object returned to the handler should be a
                    // DOMSource so that the handler programmer can read the data
                    // multiple times and (as opposed to using a StreamSource) and
                    // they can more easily access the data in DOM form.
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    dbf.setNamespaceAware(true);
                    
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    Document dom = db.parse(bais);
                    payloads.HANDLER_PAYLOAD = new DOMSource(dom);
                } catch (ParserConfigurationException e) {
                    throw ExceptionFactory.makeWebServiceException(e);
                } catch (IOException e) {
                    throw ExceptionFactory.makeWebServiceException(e);
                } catch (SAXException e) {
                    throw ExceptionFactory.makeWebServiceException(e);
                }
                        
                // We need a different byte[] for the cache so that we're not just
                // building two Source objects that point to the same array.
                byte[] cacheBytes = new byte[bytes.length];
                System.arraycopy(bytes, 0, cacheBytes, 0, bytes.length);

                // Now build the Soure object for the cache.
                ByteArrayInputStream cacheBais = new ByteArrayInputStream(cacheBytes);
                payloads.CACHE_PAYLOAD = new StreamSource(cacheBais);
            } catch (TransformerConfigurationException e) {
                throw ExceptionFactory.makeWebServiceException(e);
            } catch (TransformerFactoryConfigurationError e) {
                throw ExceptionFactory.makeWebServiceException(e);
            } catch (TransformerException e) {
                throw ExceptionFactory.makeWebServiceException(e);
            }
        } else {
            // no cache implemented yet
            payloads.HANDLER_PAYLOAD = content;
            payloads.CACHE_PAYLOAD = content;
        }

        return payloads;
    }
            
    /*
     * A simple holder for the different payload objects.
     */
    class Payloads {
        Object HANDLER_PAYLOAD;    // The payload object that will be returned to the handler
        Object CACHE_PAYLOAD;      // The payload object that will be used for the cache
    }    

}