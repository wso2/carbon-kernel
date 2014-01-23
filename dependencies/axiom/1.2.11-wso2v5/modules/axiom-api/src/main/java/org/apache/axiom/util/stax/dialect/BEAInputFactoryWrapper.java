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
import java.io.Reader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;

import org.apache.axiom.util.stax.wrapper.XMLInputFactoryWrapper;

class BEAInputFactoryWrapper extends XMLInputFactoryWrapper {
    public BEAInputFactoryWrapper(XMLInputFactory parent) {
        super(parent);
    }

    public XMLStreamReader createXMLStreamReader(InputStream stream) throws XMLStreamException {
        return createXMLStreamReader(null, stream);
    }

    public XMLStreamReader createXMLStreamReader(String systemId, InputStream stream)
            throws XMLStreamException {
        // The getEncoding() method of the stream reader produced by the reference implementation
        // doesn't return complete information about the effective encoding. To work around this,
        // we need to implement the detection algorithm described in Appendix F.1 of the
        // XML 1.0 specifications (Fifth Edition). Note that the encoding determined here may be
        // overridden by the XML encoding declaration, if present in the XML document. This
        // information is already available from the stream reader, so that we don't need to
        // reimplement this part.
        // TODO: this needs some more unit testing!
        EncodingDetectionHelper helper = new EncodingDetectionHelper(stream);
        stream = helper.getInputStream();
        String encoding = helper.detectEncoding();
        XMLStreamReader reader;
        if (systemId == null) {
            reader = super.createXMLStreamReader(stream);
        } else {
            reader = super.createXMLStreamReader(systemId, stream);
        }
        return new BEAStreamReaderWrapper(reader, encoding);
    }

    public XMLStreamReader createXMLStreamReader(InputStream stream, String encoding)
            throws XMLStreamException {
        return new BEAStreamReaderWrapper(super.createXMLStreamReader(stream, encoding), null);
    }

    public XMLStreamReader createXMLStreamReader(Reader reader) throws XMLStreamException {
        return new BEAStreamReaderWrapper(super.createXMLStreamReader(reader), null);
    }

    public XMLStreamReader createXMLStreamReader(Source source) throws XMLStreamException {
        return new BEAStreamReaderWrapper(super.createXMLStreamReader(source), null);
    }

    public XMLStreamReader createXMLStreamReader(String systemId, Reader reader)
            throws XMLStreamException {
        return new BEAStreamReaderWrapper(super.createXMLStreamReader(systemId, reader), null);
    }
}
