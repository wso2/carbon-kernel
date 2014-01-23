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
package org.apache.axiom.om;

import java.io.InputStream;
import java.io.StringReader;
import java.lang.ref.WeakReference;

import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.testutils.io.CloseSensorInputStream;
import org.apache.axiom.testutils.io.CloseSensorReader;

public class OMXMLParserWrapperTestBase extends AbstractTestCase {
    private final OMMetaFactory omMetaFactory;

    public OMXMLParserWrapperTestBase(OMMetaFactory omMetaFactory) {
        this.omMetaFactory = omMetaFactory;
    }
    
    public void testCloseWithInputStream() throws Exception {
        CloseSensorInputStream in = new CloseSensorInputStream(getTestResource(TestConstants.TEST));
        try {
            OMXMLParserWrapper builder = OMXMLBuilderFactory.createOMBuilder(omMetaFactory.getOMFactory(), in);
            builder.getDocument().build();
            builder.close();
            // OMXMLParserWrapper#close() does _not_ close the underlying input stream
            assertFalse(in.isClosed());
        } finally {
            in.close();
        }
    }
    
    public void testCloseWithReader() throws Exception {
        CloseSensorReader in = new CloseSensorReader(new StringReader("<root><child/></root>"));
        try {
            OMXMLParserWrapper builder = OMXMLBuilderFactory.createOMBuilder(omMetaFactory.getOMFactory(), in);
            builder.getDocument().build();
            builder.close();
            // OMXMLParserWrapper#close() does _not_ close the underlying input stream
            assertFalse(in.isClosed());
        } finally {
            in.close();
        }
    }
    
    public void testCloseWithXMLStreamReader() throws Exception {
        InputStream in = getTestResource(TestConstants.TEST);
        try {
            XMLStreamReader reader = StAXUtils.createXMLStreamReader(in);
            OMXMLParserWrapper builder = omMetaFactory.createStAXOMBuilder(omMetaFactory.getOMFactory(), reader);
            WeakReference readerWeakRef = new WeakReference(reader);
            reader = null;
            builder.getDocument().build();
            builder.close();
            for (int i=0; i<10; i++) {
                Thread.sleep(500);
                if (readerWeakRef.get() == null) {
                    return;
                }
                System.gc();
            }
            fail("Builder didn't release reference to the underlying parser");
        } finally {
            in.close();
        }
    }
}
