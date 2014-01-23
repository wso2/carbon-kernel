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
package org.apache.axiom.ts.om.document;

import java.io.InputStream;

import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMMetaFactory;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.impl.OMStAXWrapper;
import org.apache.axiom.om.impl.RootWhitespaceFilter;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.testutils.stax.XMLStreamReaderComparator;
import org.apache.axiom.ts.ConformanceTestCase;

/**
 * Test comparing the output of {@link OMStAXWrapper} with that of a native StAX parser.
 */
public class TestGetXMLStreamReader extends ConformanceTestCase {
    private final boolean cache;
    
    public TestGetXMLStreamReader(OMMetaFactory metaFactory, String file, boolean cache) {
        super(metaFactory, file);
        this.cache = cache;
        setName(getName() + " [cache=" + cache + "]");
    }
    
    protected void runTest() throws Throwable {
        InputStream in1 = getFileAsStream();
        InputStream in2 = getFileAsStream();
        try {
            XMLStreamReader expected = StAXUtils.createXMLStreamReader(in1);
            try {
                OMXMLParserWrapper builder = OMXMLBuilderFactory.createOMBuilder(metaFactory.getOMFactory(), in2);
                try {
                    XMLStreamReader actual = builder.getDocument().getXMLStreamReader(cache);
                    new XMLStreamReaderComparator(new RootWhitespaceFilter(expected),
                            new RootWhitespaceFilter(actual)).compare();
                } finally {
                    builder.close();
                }
            } finally {
                expected.close();
            }
        } finally {
            in1.close();
            in2.close();
        }
    }
}
