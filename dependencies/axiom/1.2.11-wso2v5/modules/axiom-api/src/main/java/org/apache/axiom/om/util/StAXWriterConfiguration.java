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

import javax.xml.stream.XMLOutputFactory;

import org.apache.axiom.util.stax.dialect.StAXDialect;
import org.apache.axiom.util.stax.dialect.StAXDialectDetector;

/**
 * Defines a particular StAX writer configuration. An implementation of this
 * interface must satisfy the following requirements:
 * <ol>
 * <li>It MUST be immutable.
 * <li>It MUST either be a singleton or properly implement
 * {@link Object#equals(Object)} and {@link Object#hashCode()}.
 * </ol>
 * These two requirements ensure that instances of this interface may be used as
 * cache keys.
 * 
 * @see StAXParserConfiguration
 */
public interface StAXWriterConfiguration {
    /**
     * The default configuration.
     */
    StAXWriterConfiguration DEFAULT = new StAXWriterConfiguration() {
        public XMLOutputFactory configure(XMLOutputFactory factory, StAXDialect dialect) {
            return factory;
        }

        public String toString() {
            return "DEFAULT";
        }
    };

    /**
     * Apply the configuration to the given factory. The method MAY optionally
     * wrap the factory.
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
    XMLOutputFactory configure(XMLOutputFactory factory, StAXDialect dialect);
}
