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

package org.apache.axiom.om.impl;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.impl.serialize.StreamingOMSerializer;
import org.apache.axiom.om.impl.util.OMSerializerUtil;

/**
 * Utility class with default implementations for some of the methods defined by the
 * {@link OMDocument} interface.
 */
public class OMDocumentImplUtil {
    private OMDocumentImplUtil() {}
    
    public static void internalSerialize(OMDocument document, XMLStreamWriter writer2,
            boolean cache, boolean includeXMLDeclaration) throws XMLStreamException {
        
        MTOMXMLStreamWriter writer = (MTOMXMLStreamWriter) writer2;
        if (includeXMLDeclaration) {
            //Check whether the OMOutput char encoding and OMDocument char
            //encoding matches, if not use char encoding of OMOutput
            String encoding = writer.getCharSetEncoding();
            if (encoding == null || encoding.isEmpty()) {
                encoding = document.getCharsetEncoding();
            }
            String version = document.getXMLVersion();
            if (version == null) {
                version = "1.0";
            }
            if (encoding == null) {
                writer.getXmlStreamWriter().writeStartDocument(version);
            } else {
                writer.getXmlStreamWriter().writeStartDocument(encoding, version);
            }
        }

        if (cache || document.isComplete() || document.getBuilder() == null) {
            OMSerializerUtil.serializeChildren(document, writer, cache);
        } else {
            StreamingOMSerializer streamingOMSerializer = new StreamingOMSerializer();
            XMLStreamReader reader = document.getXMLStreamReaderWithoutCaching();
            while (reader.getEventType() != XMLStreamReader.END_DOCUMENT) {
                streamingOMSerializer.serialize(reader, writer);
            }
        }
    }
}
