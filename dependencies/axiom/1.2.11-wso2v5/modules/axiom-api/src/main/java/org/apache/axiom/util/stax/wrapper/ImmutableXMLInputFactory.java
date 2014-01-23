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

package org.apache.axiom.util.stax.wrapper;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLReporter;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.util.XMLEventAllocator;

/**
 * Wraps an {@link XMLInputFactory} so that its state can no longer be changed. The state includes
 * the properties as well as the {@link XMLEventAllocator}, {@link XMLReporter} and
 * {@link XMLResolver} instances configured on the factory.
 */
public class ImmutableXMLInputFactory extends XMLInputFactoryWrapper {
    /**
     * Constructor.
     * 
     * @param parent the parent factory
     */
    public ImmutableXMLInputFactory(XMLInputFactory parent) {
        super(parent);
    }

    public void setEventAllocator(XMLEventAllocator allocator) {
        throw new IllegalStateException("This factory is immutable");
    }

    public void setProperty(String name, Object value) throws IllegalArgumentException {
        throw new IllegalStateException("This factory is immutable");
    }

    public void setXMLReporter(XMLReporter reporter) {
        throw new IllegalStateException("This factory is immutable");
    }

    public void setXMLResolver(XMLResolver resolver) {
        throw new IllegalStateException("This factory is immutable");
    }
}
