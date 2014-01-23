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

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

/**
 * Checks that {@link XMLStreamReader#getEncoding()} returns accurate information for XML documents
 * without an encoding specified in the XML declaration. For this type of document, the parser
 * should implement the detection algorithm described in Appendix F.1 of the XML 1.0 specifications
 * (Fifth Edition).
 */
public class GetEncodingFromDetectionTestCase extends DialectTestCase {
    private final String javaEncoding;
    private final Set xmlEncodings;

    public GetEncodingFromDetectionTestCase(String javaEncoding, String[] xmlEncodings) {
        this.javaEncoding = javaEncoding;
        this.xmlEncodings = new HashSet(Arrays.asList(xmlEncodings));
        setName(getClass().getName() + " [" + javaEncoding + "]");
    }
    
    public GetEncodingFromDetectionTestCase(String javaEncoding, String xmlEncoding) {
        this(javaEncoding, new String[] { xmlEncoding });
    }

    protected void runTest() throws Throwable {
        XMLInputFactory factory = newNormalizedXMLInputFactory();
        XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(
                "<?xml version=\"1.0\"?><root/>".getBytes(javaEncoding)));
        String actualEncoding = reader.getEncoding();
        assertTrue("Expected one of " + xmlEncodings + ", but got " + actualEncoding,
                   xmlEncodings.contains(actualEncoding));
    }
}
