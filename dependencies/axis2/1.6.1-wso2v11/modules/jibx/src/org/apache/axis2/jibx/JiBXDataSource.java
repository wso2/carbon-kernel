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

package org.apache.axis2.jibx;

import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.util.StAXUtils;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallable;
import org.jibx.runtime.IMarshaller;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IXMLWriter;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.impl.StAXWriter;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/** Data source for OM element backed by JiBX data bound object. */
public class JiBXDataSource implements OMDataSource {
    
    /** Mapping name, for when abstract mapping is used directly; <code>null</code> if not used). */
    private final String marshallerName;

    /** Element name (only used with {@link #marshallerName}). */
    private final String elementName;
    
    /** Element namespace URI (only used with {@link #marshallerName}). */
    private final String elementNamespace;

    /** Element namespace prefix (only used with {@link #marshallerName}). */
    private final String elementNamespacePrefix;
    
    /** Element namespace index (only used with {@link #marshallerName}). */
    private final int elementNamespaceIndex;

    /** Indexes of namespaces to be opened (only used with {@link #marshallerName}). */
    private final int[] openNamespaceIndexes;

    /** Prefixes of namespaces to be opened (only used with {@link #marshallerName}). */
    private final String[] openNamespacePrefixes;

    /** Data object for output. */
    private final Object dataObject;

    /** Binding factory for creating marshaller. */
    private final IBindingFactory bindingFactory;

    /**
     * Constructor from marshallable object and binding factory.
     *
     * @param obj
     * @param factory
     */
    public JiBXDataSource(IMarshallable obj, IBindingFactory factory) {
        marshallerName = null;
        dataObject = obj;
        bindingFactory = factory;
        elementName = elementNamespace = elementNamespacePrefix = null;
        elementNamespaceIndex = -1;
        openNamespaceIndexes = null;
        openNamespacePrefixes = null;
    }

    /**
     * Constructor from object with mapping index and binding factory.
     *
     * @param obj
     * @param mapping
     * @param name
     * @param uri
     * @param prefix
     * @param nsindexes
     * @param nsprefixes
     * @param factory
     */
    public JiBXDataSource(Object obj, String mapping, String name, String uri, String prefix,
                          int[] nsindexes, String[] nsprefixes, IBindingFactory factory) {
        if (mapping == null) {
            throw new
                    IllegalArgumentException("mapping name must be supplied");
        }
        marshallerName = mapping;
        dataObject = obj;
        bindingFactory = factory;
        boolean found = false;
        String[] nss = factory.getNamespaces();
        int nsidx = -1;
        for (int i = 0; i < nsindexes.length; i++) {
            if (uri.equals(nss[nsindexes[i]])) {
                nsidx = nsindexes[i];
                prefix = nsprefixes[i];
                found = true;
                break;
            }
        }
        elementName = name;
        elementNamespace = uri;
        elementNamespacePrefix = prefix;
        if (!found) {
            for (int i = 0; i < nss.length; i++) {
                if (uri.equals(nss[i])) {
                    nsidx = i;
                    break;
                }
            }
            if (nsidx >= 0) {
                int[] icpy = new int[nsindexes.length+1];
                icpy[0] = nsidx;
                System.arraycopy(nsindexes, 0, icpy, 1, nsindexes.length);
                nsindexes = icpy;
                String[] scpy = new String[nsprefixes.length+1];
                scpy[0] = prefix;
                System.arraycopy(nsprefixes, 0, scpy, 1, nsprefixes.length);
                nsprefixes = scpy;
            } else {
                throw new IllegalStateException("Namespace not found");
            }
        }
        elementNamespaceIndex = nsidx;
        openNamespaceIndexes = nsindexes;
        openNamespacePrefixes = nsprefixes;
    }

    /**
     * Internal method to handle the actual marshalling. If the source object is marshallable it's
     * it's just marshalled directly, without worrying about redundant namespace declarations and
     * such. If it needs to be handled with an abstract mapping, the handling is determined by the
     * 'full' flag. When this is <code>true</code> all namespaces are declared directly, while if
     * <code>false</code> the namespaces must have previously been declared on some enclosing
     * element of the output. This allows the normal case of marshalling within the context of a
     * SOAP message to be handled efficiently, while still generating correct XML if the element is
     * marshalled directly (as when building the AXIOM representation for use by WS-Security).
     * 
     * @param full
     * @param ctx
     * @throws JiBXException
     */
    private void marshal(boolean full, IMarshallingContext ctx) throws JiBXException {
        try {
            
            if (marshallerName == null) {
                if (dataObject instanceof IMarshallable) {
                    ((IMarshallable)dataObject).marshal(ctx);
                } else {
                    throw new IllegalStateException("Object of class " + dataObject.getClass().getName() +
                        " needs a JiBX <mapping> to be marshalled");
                }
            } else {
                IXMLWriter wrtr = ctx.getXmlWriter();
                String name = elementName;
                int nsidx = 0;
                if (full) {
                    
                    // declare all namespaces on start tag
                    nsidx = elementNamespaceIndex;
                    wrtr.startTagNamespaces(nsidx, name, openNamespaceIndexes, openNamespacePrefixes);
                    
                } else {
        
                    // configure writer with namespace declared in enclosing scope
                    wrtr.openNamespaces(openNamespaceIndexes, openNamespacePrefixes);
                    if (!"".equals(elementNamespacePrefix)) {
                        name = elementNamespacePrefix + ':' + name;
                    }
                    wrtr.startTagOpen(0, name);
                }
                
                // marshal object representation (may include attributes) into element
                IMarshaller mrsh = ctx.getMarshaller(marshallerName);
                mrsh.marshal(dataObject, ctx);
                wrtr.endTag(nsidx, name);
            }
            ctx.getXmlWriter().flush();

        } catch (IOException e) {
            throw new JiBXException("Error marshalling XML representation: " + e.getMessage(), e);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMDataSource#serialize(java.io.OutputStream, org.apache.axiom.om.OMOutputFormat)
     */
    public void serialize(OutputStream output, OMOutputFormat format) throws XMLStreamException {
        try {
            
            // marshal with all namespace declarations, since external state unknown
            IMarshallingContext ctx = bindingFactory.createMarshallingContext();
            ctx.setOutput(output, format == null ? null : format.getCharSetEncoding());
            marshal(true, ctx);
            
        } catch (JiBXException e) {
            throw new XMLStreamException("Error in JiBX marshalling: " + e.getMessage(), e);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMDataSource#serialize(java.io.Writer, org.apache.axiom.om.OMOutputFormat)
     */
    public void serialize(Writer writer, OMOutputFormat format) throws XMLStreamException {
        try {
            
            // marshal with all namespace declarations, since external state unknown
            IMarshallingContext ctx = bindingFactory.createMarshallingContext();
            ctx.setOutput(writer);
            marshal(true, ctx);
            
        } catch (JiBXException e) {
            throw new XMLStreamException("Error in JiBX marshalling: " + e.getMessage(), e);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMDataSource#serialize(javax.xml.stream.XMLStreamWriter)
     */
    public void serialize(XMLStreamWriter xmlWriter) throws XMLStreamException {
        try {
            
            // check if namespaces already declared for abstract mapping
            boolean full = true;
            String[] nss = bindingFactory.getNamespaces();
            if (marshallerName != null) {
                String prefix = xmlWriter.getPrefix(elementNamespace);
                if (elementNamespacePrefix.equals(prefix)) {
                    full = false;
                    for (int i = 0; i < openNamespaceIndexes.length; i++) {
                        String uri = nss[i];
                        prefix = xmlWriter.getPrefix(uri);
                        if (!openNamespacePrefixes[i].equals(prefix)) {
                            full = true;
                            break;
                        }
                    }
                }
            }
            
            // marshal with all namespace declarations, since external state unknown
            IXMLWriter writer = new StAXWriter(nss, xmlWriter);
            IMarshallingContext ctx = bindingFactory.createMarshallingContext();
            ctx.setXmlWriter(writer);
            marshal(full, ctx);
            
        } catch (JiBXException e) {
            throw new XMLStreamException("Error in JiBX marshalling: " + e.getMessage(), e);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMDataSource#getReader()
     */
    public XMLStreamReader getReader() throws XMLStreamException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        serialize(bos, null);
        return StAXUtils.createXMLStreamReader(new ByteArrayInputStream(bos.toByteArray()));
    }
}