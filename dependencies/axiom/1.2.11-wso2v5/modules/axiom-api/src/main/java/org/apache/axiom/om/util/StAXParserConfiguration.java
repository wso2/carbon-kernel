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

package org.apache.axiom.om.util;

import java.io.ByteArrayInputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.util.stax.dialect.StAXDialect;
import org.apache.axiom.util.stax.dialect.StAXDialectDetector;

/**
 * Defines a particular StAX parser configuration. An implementation of this
 * interface must satisfy the following requirements:
 * <ol>
 * <li>It MUST be immutable.
 * <li>It MUST either be a singleton or properly implement
 * {@link Object#equals(Object)} and {@link Object#hashCode()}.
 * </ol>
 * These two requirements ensure that instances of this interface may be used as
 * cache keys.
 * 
 * @see StAXWriterConfiguration
 */
public interface StAXParserConfiguration {
    /**
     * The default configuration.
     */
    StAXParserConfiguration DEFAULT = new StAXParserConfiguration() {
        public XMLInputFactory configure(XMLInputFactory factory, StAXDialect dialect) {
            return factory;
        }

        public String toString() {
            return "DEFAULT";
        }
    };
    
    /**
     * Configuration that forces the parser to process the XML document as
     * standalone. In this configuration, the parser will ignore any references
     * to external entities, in particular DTDs. This is especially useful to
     * process documents referencing DTDs with system IDs that are network
     * locations, because parsing these documents would otherwise fail on nodes
     * detached from the network. This configuration should be used with care
     * because the resulting representation of the document may be incomplete.
     * E.g. default attribute values defined in the DTD will not be reported.
     */
    StAXParserConfiguration STANDALONE = new StAXParserConfiguration() {
        public XMLInputFactory configure(XMLInputFactory factory, StAXDialect dialect) {
            factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
            // Some StAX parser such as Woodstox still try to load the external DTD subset,
            // even if IS_SUPPORTING_EXTERNAL_ENTITIES is set to false. To work around this,
            // we add a custom XMLResolver that returns empty documents. See WSTX-117 for
            // an interesting discussion about this.
            factory.setXMLResolver(new XMLResolver() {
                public Object resolveEntity(String publicID, String systemID, String baseURI,
                        String namespace) throws XMLStreamException {
                    return new ByteArrayInputStream(new byte[0]);
                }
            });
            return factory;
        }

        public String toString() {
            return "STANDALONE";
        }
    };

    /**
     * Configuration that sets up the parser in non coalescing mode.
     */
    StAXParserConfiguration NON_COALESCING = new StAXParserConfiguration() {
        public XMLInputFactory configure(XMLInputFactory factory, StAXDialect dialect) {
            factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
            return factory;
        }

        public String toString() {
            return "NON_COALESCING";
        }
    };
    
    /**
     * Configuration that sets up the parser to preserve CDATA sections. This configuration will
     * also put the parser in non coalescing mode.
     */
    StAXParserConfiguration PRESERVE_CDATA_SECTIONS = new StAXParserConfiguration() {
        public XMLInputFactory configure(XMLInputFactory factory, StAXDialect dialect) {
            return dialect.enableCDataReporting(factory);
        }
        
        public String toString() {
            return "PRESERVE_CDATA_SECTIONS";
        }
    };
    
    /**
     * Configuration suitable for SOAP messages. This will configure the parser
     * to throw an exception when it encounters a document type declaration. The
     * SOAP 1.1 specification indeed prescribes that
     * "A SOAP message MUST NOT contain a Document Type Declaration." The
     * difference between the {@link #STANDALONE} configuration and this
     * configuration is that with {@link #STANDALONE}, the parser silently
     * ignores references to external entities but doesn't throw any exception.
     * 
     * @see StAXDialect#disallowDoctypeDecl(XMLInputFactory)
     */
    StAXParserConfiguration SOAP = new StAXParserConfiguration() {
        public XMLInputFactory configure(XMLInputFactory factory, StAXDialect dialect) {
            return dialect.disallowDoctypeDecl(factory);
        }

        public String toString() {
            return "SOAP";
        }
    };
    
    /**
     * Apply the configuration to the given factory. The method MAY optionally
     * wrap the factory, e.g. to modify the behavior of the
     * {@link javax.xml.stream.XMLStreamReader} instances created by the
     * factory.
     * 
     * @param factory
     *            the factory to configure
     * @param dialect
     *            The dialect of the StAX implementation as detected by
     *            {@link StAXDialectDetector}. The implementation may use this
     *            information to configure implementation specific settings.
     * @return The configured factory. This may be the original factory (if the
     *         implementation only changes the factory properties), or a
     *         wrapper.
     */
    XMLInputFactory configure(XMLInputFactory factory, StAXDialect dialect);
}
