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

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

class XLXP1Dialect extends AbstractStAXDialect {
    private final boolean isSetPrefixBroken;
    
    public XLXP1Dialect(boolean isSetPrefixBroken) {
        this.isSetPrefixBroken = isSetPrefixBroken;
    }
    
    public String getName() {
        return isSetPrefixBroken ? "XL XP-J (StAX non-compliant versions)"
                                 : "XL XP-J (StAX compliant versions)";
    }

    public XMLInputFactory enableCDataReporting(XMLInputFactory factory) {
        factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
        return factory;
    }

    public XMLInputFactory disallowDoctypeDecl(XMLInputFactory factory) {
        return StAXDialectUtils.disallowDoctypeDecl(factory);
    }

    public XMLInputFactory makeThreadSafe(XMLInputFactory factory) {
        // XLXP's factories are thread safe
        return factory;
    }

    public XMLOutputFactory makeThreadSafe(XMLOutputFactory factory) {
        // XLXP's factories are thread safe
        return factory;
    }

    public XMLStreamReader normalize(XMLStreamReader reader) {
        return new XLXP1StreamReaderWrapper(reader);
    }

    public XMLStreamWriter normalize(XMLStreamWriter writer) {
        XMLStreamWriter wrapper = new XLXPStreamWriterWrapper(writer);
        // Early versions of XLXP the scope of the prefix bindings defined by setPrefix
        // is incorrect
        if (isSetPrefixBroken) {
            wrapper = new NamespaceContextCorrectingXMLStreamWriterWrapper(wrapper);
        }
        return wrapper;
    }

    public XMLInputFactory normalize(XMLInputFactory factory) {
        return new XLXPInputFactoryWrapper(factory, this);
    }

    public XMLOutputFactory normalize(XMLOutputFactory factory) {
        return new NormalizingXMLOutputFactoryWrapper(factory, this);
    }
}
