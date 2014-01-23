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

package org.apache.axiom.om.ds.custombuilder;

import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.ds.ByteArrayDataSource;
import org.apache.axiom.om.impl.builder.CustomBuilder;
import org.apache.axiom.om.impl.serialize.StreamingOMSerializer;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeader;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import java.io.ByteArrayOutputStream;


/**
 * CustomBuilder that creates an OMSourcedElement backed by a ByteArrayDataSource.
 * If you have a payload or header that will consume a lot of space, it 
 * may be beneficial to plug in this CustomBuilder.
 * 
 * Use this CustomBuilder as a pattern for other CustomBuilders.
 */
public class ByteArrayCustomBuilder implements CustomBuilder {
    private String encoding = null;
    
    /**
     * Constructor
     * @param encoding 
     */
    public ByteArrayCustomBuilder(String encoding) {
        this.encoding = (encoding == null) ? "utf-8" :encoding;
    }

    /* 
     * Create an OMSourcedElement back by a ByteArrayDataSource
     */
    public OMElement create(String namespace, 
                            String localPart, 
                            OMContainer parent, 
                            XMLStreamReader reader,
                            OMFactory factory) throws OMException {
        try {
            // Get the prefix of the start tag
            String prefix = reader.getPrefix();
            
            // Stream the events to a writer starting with the current event
            StreamingOMSerializer ser = new StreamingOMSerializer();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLStreamWriter writer = StAXUtils.createXMLStreamWriter(baos, encoding);
            ser.serialize(reader, writer, false);
            writer.flush();
            
            // Capture the written byte array as a ByteArrayDataSource
            byte[] bytes = baos.toByteArray();
            String text = new String(bytes, "utf-8");
            ByteArrayDataSource ds = new ByteArrayDataSource(bytes, encoding);
            
            // Create an OMSourcedElement backed by the ByteArrayDataSource
            OMNamespace ns = factory.createOMNamespace(namespace, prefix);
            
            OMElement om = null;
            if (parent instanceof SOAPHeader && factory instanceof SOAPFactory) {
                om = ((SOAPFactory)factory).createSOAPHeaderBlock(localPart, ns, ds);
            } else {
                om = factory.createOMElement(ds, localPart, ns);
            }
            
            // Add the new OMSourcedElement ot the parent
            parent.addChild(om);
            return om;
        } catch (XMLStreamException e) {
            throw new OMException(e);
        } catch (OMException e) {
            throw e;
        } catch (Throwable t) {
            throw new OMException(t);
        }
    }

}
