/**
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
package components;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.message.token.Reference;

/**
 * unit test for the Reference type
 */
public class TestReference extends TestCase {

    private static final String 
    TEST_REFERENCE_TEMPLATE = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        +   "<wss:Reference "
        +       "xmlns:wss=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" "
        +       "ValueType=\"TheValueType\" "
        +       "URI=\"TheURI\" "
        +       "/>"
        ;

    private static final String 
    BOGUS_REFERENCE_TEMPLATE = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        +   "<wss:Reference "
        +       "xmlns:wss=\"http://something-completely-different\" "
        +       "ValueType=\"TheValueType\" "
        +       "URI=\"TheURI\" "
        +       "/>"
        ;


    public TestReference(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(TestReference.class);
    }
    
    
    public void
    testConstructor() throws Exception {
        //
        // null input
        //
        try {
            new Reference((org.w3c.dom.Element) null);
            fail("Expected failure on null Element passed to ctor");
        } catch (final WSSecurityException e) {
            // complete
        }
        //
        // The XML doesn't conform to the WSS namespace
        //
        try {
            new Reference(
                createReferenceDocument(
                    BOGUS_REFERENCE_TEMPLATE,
                    "foo", "bar"
                ).getDocumentElement()
            );
            fail("Expected failure on bogus template");
        } catch (final Exception e) {
            // complete
        }
        //
        // create a Reference from valid XML
        //
        new Reference(
            createReferenceDocument(
                TEST_REFERENCE_TEMPLATE,
                "foo", "bar"
            )
        );
        new Reference(
            createReferenceDocument(
                TEST_REFERENCE_TEMPLATE,
                "foo", "bar"
            ).getDocumentElement()
        );
    }
    
    public void
    testAccessors() throws Exception {
        final Reference ref = new Reference(
            createReferenceDocument(
                TEST_REFERENCE_TEMPLATE,
                "foo", "bar"
            ).getDocumentElement()
        );
        assertEquals(ref.getValueType(), "foo");
        assertEquals(ref.getURI(), "bar");
    }
    
    private static org.w3c.dom.Document
    createReferenceDocument(
        final String template,
        final String valueType,
        final String uri
    ) throws javax.xml.parsers.ParserConfigurationException,
             org.xml.sax.SAXException,
             java.io.IOException {
        final java.io.InputStream in = 
            new java.io.ByteArrayInputStream(
                template.replaceFirst(
                    "TheValueType", valueType
                ).replaceFirst(
                    "TheURI", uri
                ).getBytes()
            );
        final javax.xml.parsers.DocumentBuilderFactory factory = 
            javax.xml.parsers.DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        final javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(in);
    }
}
