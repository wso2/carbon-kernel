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

import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMMetaFactory;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.StAXParserConfiguration;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.xml.sax.InputSource;

/**
 * Base class for {@link OMMetaFactory} implementations that make use of the standard builders
 * ({@link org.apache.axiom.om.impl.builder.StAXOMBuilder} and its subclasses).
 */
public abstract class AbstractOMMetaFactory implements OMMetaFactory {
    private XMLStreamReader createXMLStreamReader(StAXParserConfiguration configuration, InputSource is) {
        try {
            if (is.getByteStream() != null) {
                String encoding = is.getEncoding();
                if (encoding == null) {
                    return StAXUtils.createXMLStreamReader(configuration, is.getByteStream());
                } else {
                    return StAXUtils.createXMLStreamReader(configuration, is.getByteStream(), encoding);
                }
            } else if (is.getCharacterStream() != null) {
                return StAXUtils.createXMLStreamReader(configuration, is.getCharacterStream());
            } else {
                throw new IllegalArgumentException();
            }
        } catch (XMLStreamException ex) {
            throw new OMException(ex);
        }
    }
    
    public OMXMLParserWrapper createStAXOMBuilder(OMFactory omFactory, XMLStreamReader parser) {
        StAXOMBuilder builder = new StAXOMBuilder(omFactory, parser);
        // StAXOMBuilder defaults to the "legacy" behavior, which is to keep a reference to the
        // parser after the builder has been closed. Since releasing this reference is a good idea
        // we default to releaseParserOnClose=true for builders created through the OMMetaFactory
        // API.
        builder.releaseParserOnClose(true);
        return builder;
    }

    public OMXMLParserWrapper createOMBuilder(OMFactory omFactory, StAXParserConfiguration configuration, InputSource is) {
        return createStAXOMBuilder(omFactory, createXMLStreamReader(configuration, is));
    }

    public OMXMLParserWrapper createStAXSOAPModelBuilder(XMLStreamReader parser) {
        StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(this, parser);
        builder.releaseParserOnClose(true);
        return builder;
    }

    public OMXMLParserWrapper createSOAPModelBuilder(StAXParserConfiguration configuration, InputSource is) {
        return createStAXSOAPModelBuilder(createXMLStreamReader(configuration, is));
    }
}
