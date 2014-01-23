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

package org.apache.axiom.util.stax.dialect;

import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

class XLXPInputFactoryWrapper extends NormalizingXMLInputFactoryWrapper {
    public XLXPInputFactoryWrapper(XMLInputFactory parent, AbstractStAXDialect dialect) {
        super(parent, dialect);
    }

    public XMLStreamReader createXMLStreamReader(InputStream stream) throws XMLStreamException {
        return createXMLStreamReader(null, stream);
    }

    public XMLStreamReader createXMLStreamReader(String systemId, InputStream stream)
            throws XMLStreamException {
        // Both versions of XLXP have issues with documents using UTF-16 without byte
        // order markers, although this type of document is explicitly supported by the XML
        // specification:
        // * XLXP parses the document, XMLStreamReader#getEncoding incorrectly reports
        //   UTF-8 as the detected encoding. 
        // * XLXP2 simply fails on UTF-16 documents without BOM.
        EncodingDetectionHelper helper = new EncodingDetectionHelper(stream);
        stream = helper.getInputStream();
        String encoding = helper.detectEncoding();
        if (encoding.startsWith("UTF-16")) {
            if (systemId == null) {
                return super.createXMLStreamReader(stream, encoding);
            } else {
                // Here we have an issue because it is not possible to specify the
                // systemId and the encoding at the same time...
                return super.createXMLStreamReader(systemId, stream);
            }
        } else {
            if (systemId == null) {
                return super.createXMLStreamReader(stream);
            } else {
                return super.createXMLStreamReader(systemId, stream);
            }
        }
    }
}
