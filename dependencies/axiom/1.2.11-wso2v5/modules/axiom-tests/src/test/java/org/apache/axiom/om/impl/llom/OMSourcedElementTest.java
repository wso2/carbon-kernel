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

package org.apache.axiom.om.impl.llom;

import org.apache.axiom.om.AbstractTestCase;
import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.OMDataSourceExt;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.ds.CharArrayDataSource;
import org.apache.axiom.om.impl.OMNamespaceImpl;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.llom.factory.OMLinkedListImplFactory;
import org.apache.axiom.om.impl.serialize.StreamingOMSerializer;
import org.apache.axiom.om.util.StAXUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Iterator;

/** Tests the characteristics of OMSourcedElementImpl. */
public class OMSourcedElementTest extends AbstractTestCase {
    private static String testDocument =
            "<library xmlns=\"http://www.sosnoski.com/uwjws/library\" books=\"1\">" +
                    "<type id=\"java\" category=\"professional\" deductable=\"true\">" +
                    "<name>Java Reference</name></type><type id=\"xml\" " +
                    "category=\"professional\" deductable=\"true\"><name>XML Reference</name>" +
                    "</type><book isbn=\"1930110111\" type=\"xml\"><title>XSLT Quickly</title>" +
                    "<author>DuCharme, Bob</author><publisher>Manning</publisher>" +
                    "<price>29.95</price></book></library>";

    // Same as testDocument except that an non-default prefix is used
    private static String testDocument2 =
            "<pre:library xmlns:pre=\"http://www.sosnoski.com/uwjws/library\" books=\"1\">" +
                    "<pre:type id=\"java\" category=\"professional\" deductable=\"true\">" +
                    "<pre:name>Java Reference</pre:name></pre:type><pre:type id=\"xml\" " +
                    "category=\"professional\" deductable=\"true\"><pre:name>XML Reference</pre:name>" +
                    "</pre:type><pre:book isbn=\"1930110111\" type=\"xml\"><pre:title>XSLT Quickly</pre:title>" +
                    "<pre:author>DuCharme, Bob</pre:author><pre:publisher>Manning</pre:publisher>" +
                    "<pre:price>29.95</pre:price></pre:book></pre:library>";

    // Same as testDocument exception that the elements are unqualified
    private static String testDocument3 =
            "<library books=\"1\">" +
                    "<type id=\"java\" category=\"professional\" deductable=\"true\">" +
                    "<name>Java Reference</name></type><type id=\"xml\" " +
                    "category=\"professional\" deductable=\"true\"><name>XML Reference</name>" +
                    "</type><book isbn=\"1930110111\" type=\"xml\"><title>XSLT Quickly</title>" +
                    "<author>DuCharme, Bob</author><publisher>Manning</publisher>" +
                    "<price>29.95</price></book></library>";

    private OMSourcedElementImpl element;
    private OMElement root;

    /** @param testName  */
    public OMSourcedElementTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        OMFactory f = new OMLinkedListImplFactory();
        OMNamespace ns = new OMNamespaceImpl("http://www.sosnoski.com/uwjws/library", "");
        OMNamespace rootNS = new OMNamespaceImpl("http://sampleroot", "rootPrefix");
        element = new OMSourcedElementImpl("library", ns, f, new TestDataSource(testDocument));
        root = f.createOMElement("root", rootNS);
        root.addChild(element);
    }

    /**
     * Make sure that the incomplete setting of an OMSE is not 
     * propogated to the root
     **/
    public void testComplete() {
        
        // Build a root element and child OMSE
        OMFactory f = new OMLinkedListImplFactory();
        OMNamespace ns = new OMNamespaceImpl("http://www.sosnoski.com/uwjws/library", "");
        OMNamespace rootNS = new OMNamespaceImpl("http://sampleroot", "rootPrefix");
        OMElement child = new OMSourcedElementImpl("library", ns, f, new TestDataSource(testDocument));
        OMElement root = f.createOMElement("root", rootNS);
        
        // Trigger expansion of the child OMSE
        // This will cause the child to be partially parsed (i.e. incomplete)
        child.getChildren();
        
        // Add the child OMSE to the root.
        root.addChild(child);
        
        // Normally adding an incomplete child to a parent will 
        // cause the parent to be marked as incomplete.
        // But OMSE's are self-contained...therefore the root
        // should still be complete
        assertTrue(!child.isComplete());
        assertTrue(root.isComplete());
        
        // Now repeat the test, but this time trigger the 
        // partial parsing of the child after adding it to the root.
        child = new OMSourcedElementImpl("library", ns, f, new TestDataSource(testDocument));
        root = f.createOMElement("root", rootNS);
        
        root.addChild(child);
        child.getChildren(); // causes partial parsing...i.e. incomplete child
    
        assertTrue(!child.isComplete());
        assertTrue(root.isComplete());
    }
    
    
    /** Ensure that each method of OMElementImpl is overridden in OMSourcedElementImpl */
    public void testMethodOverrides() {
        Method[] submeths = OMSourcedElementImpl.class.getDeclaredMethods();
        Method[] supmeths = OMElementImpl.class.getDeclaredMethods();
        outer:
        for (int i = 0; i < supmeths.length; i++) {
            Method supmeth = supmeths[i];
            Class[] params = supmeth.getParameterTypes();
            if (!Modifier.isPrivate(supmeth.getModifiers())) {
                for (int j = 0; j < submeths.length; j++) {
                    Method submeth = submeths[j];
                    if (supmeth.getName().equals(submeth.getName())) {
                        if (Arrays.equals(params, submeth.getParameterTypes())) {
                            continue outer;
                        }
                    }
                }
                fail("OMSourcedElementImpl must override method " + supmeth +
                        "\nSee class JavaDocs for details");
            }
        }
    }

    private int countItems(Iterator iter) {
        int count = 0;
        while (iter.hasNext()) {
            count++;
            iter.next();
        }
        return count;
    }

    /**
     * Test serialization of OMSourcedElementImpl to a Stream
     *
     * @throws Exception
     */
    public void testSerializeToStream() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        element.serialize(bos);
        String newText = new String(bos.toByteArray());
        assertXMLIdentical("Serialized text error", compareXML(testDocument, newText), true);
        assertTrue("Element not expanded when serializing", element.isExpanded());

        bos = new ByteArrayOutputStream();
        element.serialize(bos);
        assertXMLIdentical("Serialized text error", compareXML(testDocument,
                     new String(bos.toByteArray())), true);
        assertTrue("Element not expanded when serializing", element.isExpanded());
    }

    /**
     * Test serialization of OMSourcedElementImpl to a Stream
     *
     * @throws Exception
     */
    public void testSerializeAndConsumeToStream() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        element.serializeAndConsume(bos);
        assertXMLIdentical("Serialized text error", compareXML(testDocument,
                     new String(bos.toByteArray())), true);
        assertFalse("Element expansion when serializing", element.isExpanded());
    }

    /**
     * Test serialization of OMSourcedElementImpl to a Writer
     *
     * @throws Exception
     */
    public void testSerializeToWriter() throws Exception {
        StringWriter writer = new StringWriter();
        element.serialize(writer);
        String result = writer.toString();
        assertXMLIdentical("Serialized text error", compareXML(testDocument, result), true);
        assertTrue("Element not expanded when serializing", element.isExpanded());

        writer = new StringWriter();
        element.serialize(writer);
        result = writer.toString();
        assertXMLIdentical("Serialized text error", compareXML(testDocument, result), true);
        assertTrue("Element not expanded when serializing", element.isExpanded());
    }

    /**
     * Test serialization of OMSourcedElementImpl to a Writer
     *
     * @throws Exception
     */
    public void testSerializeAndConsumeToWriter() throws Exception {
        StringWriter writer = new StringWriter();
        element.serializeAndConsume(writer);
        String result = writer.toString();
        assertXMLIdentical("Serialized text error", compareXML(testDocument, result), true);
        assertFalse("Element expansion when serializing", element.isExpanded());
    }

    /**
     * Test serialization of OMSourcedElementImpl to an XMLWriter
     *
     * @throws Exception
     */
    public void testSerializeToXMLWriter() throws Exception {
        StringWriter writer = new StringWriter();
        XMLStreamWriter xmlwriter = StAXUtils.createXMLStreamWriter(writer);
        element.serialize(writer);
        xmlwriter.flush();
        assertXMLIdentical("Serialized text error", compareXML(testDocument, writer.toString()), true);
        assertTrue("Element not expanded when serializing", element.isExpanded());

        writer = new StringWriter();
        xmlwriter = StAXUtils.createXMLStreamWriter(writer);
        element.serialize(writer);
        xmlwriter.flush();
        assertXMLIdentical("Serialized text error", compareXML(testDocument, writer.toString()), true);
        assertTrue("Element not expanded when serializing", element.isExpanded());
    }

    /**
     * Test serialization of OMSourcedElementImpl to an XMLWriter
     *
     * @throws Exception
     */
    public void testSerializeAndConsumeToXMLWriter() throws Exception {
        StringWriter writer = new StringWriter();
        XMLStreamWriter xmlwriter = StAXUtils.createXMLStreamWriter(writer);
        element.serializeAndConsume(writer);
        xmlwriter.flush();
        assertXMLIdentical("Serialized text error", compareXML(testDocument, writer.toString()), true);
        assertFalse("Element expansion when serializing", element.isExpanded());
    }

    /**
     * Tests OMSourcedElement serialization when the root (parent) is serialized.
     *
     * @throws Exception
     */
    public void testSerializeToXMLWriterEmbedded() throws Exception {
        StringWriter writer = new StringWriter();
        XMLStreamWriter xmlwriter = StAXUtils.createXMLStreamWriter(writer);
        root.serialize(writer);
        xmlwriter.flush();
        String result = writer.toString();
        // We can't test for equivalence because the underlying OMSourceElement is 
        // streamed as it is serialized.  So I am testing for an internal value.
        assertTrue("Serialized text error" + result, result.indexOf("1930110111") > 0);
        assertTrue("Element not expanded when serializing", element.isExpanded());

        writer = new StringWriter();
        xmlwriter = StAXUtils.createXMLStreamWriter(writer);
        root.serialize(writer);
        xmlwriter.flush();
        result = writer.toString();
        // We can't test for equivalence because the underlying OMSourceElement is 
        // streamed as it is serialized.  So I am testing for an internal value.
        assertTrue("Serialized text error" + result, result.indexOf("1930110111") > 0);
        assertTrue("Element not expanded when serializing", element.isExpanded());
    }

    /**
     * Tests the OMSourcedElement localName, namespace and prefix settings before and after
     * serialization Document: testDocument (which uses the default namespace) Type of
     * Serialization: Serialize and cache Prefix test
     *
     * @throws Exception
     */
    public void testName1DefaultPrefix() throws Exception {

        OMFactory f = new OMLinkedListImplFactory();

        // Create OMSE with a DUMMYPREFIX prefix even though the underlying element uses the default prefix
        OMNamespace rootNS = new OMNamespaceImpl("http://sampleroot", "rootPrefix");
        OMNamespace ns =
                new OMNamespaceImpl("http://www.sosnoski.com/uwjws/library", "DUMMYPREFIX");
        OMElement element =
                new OMSourcedElementImpl("library", ns, f, new TestDataSource(testDocument));
        OMElement root = f.createOMElement("root", rootNS);
        root.addChild(element);

        // Test getting the namespace, localpart and prefix.  This should used not result in expansion
        assertTrue(element.getLocalName().equals("library"));
        assertTrue(element.getNamespace().getNamespaceURI().equals(
                "http://www.sosnoski.com/uwjws/library"));
        assertTrue(element.getNamespace().getPrefix().equals("DUMMYPREFIX"));

        // Serialize and cache.  This should cause expansion.  The prefix should be updated to match the testDocument string
        StringWriter writer = new StringWriter();
        XMLStreamWriter xmlwriter = StAXUtils.createXMLStreamWriter(writer);
        root.serialize(writer);
        xmlwriter.flush();
        String result = writer.toString();

        assertTrue(element.getLocalName().equals("library"));
        assertTrue(element.getNamespace().getNamespaceURI().equals(
                "http://www.sosnoski.com/uwjws/library"));
        assertTrue(element.getNamespace().getPrefix().equals(""));
        assertTrue(element.getDefaultNamespace() != null);
        assertTrue(result.indexOf("DUMMYPREFIX") <
                0);  // Make sure that the serialized string does not contain DUMMYPREFIX
        assertTrue("Serialized text error" + result, result.indexOf("1930110111") > 0);

        // Serialize again
        writer = new StringWriter();
        xmlwriter = StAXUtils.createXMLStreamWriter(writer);
        root.serialize(writer);
        xmlwriter.flush();
        result = writer.toString();

        assertTrue(element.getLocalName().equals("library"));
        assertTrue(element.getNamespace().getNamespaceURI().equals(
                "http://www.sosnoski.com/uwjws/library"));
        assertTrue(element.getNamespace().getPrefix().equals(""));
        assertTrue(element.getDefaultNamespace() != null);
        assertTrue(result.indexOf("DUMMYPREFIX") <
                0);  // Make sure that the serialized string does not contain DUMMYPREFIX
        assertTrue("Serialized text error" + result, result.indexOf("1930110111") > 0);

    }

    /**
     * Tests the OMSourcedElement localName, namespace and prefix settings before and after
     * serialization Document: testDocument (which uses the default namespace) Type of
     * Serialization: Serialize and consume Tests update of prefix
     *
     * @throws Exception
     */
    public void testName2DefaultPrefix() throws Exception {

        OMFactory f = new OMLinkedListImplFactory();

        // Create OMSE with a DUMMYPREFIX prefix even though the underlying element uses the default prefix
        OMNamespace rootNS = new OMNamespaceImpl("http://sampleroot", "rootPrefix");
        OMNamespace ns =
                new OMNamespaceImpl("http://www.sosnoski.com/uwjws/library", "DUMMYPREFIX");
        OMElement element =
                new OMSourcedElementImpl("library", ns, f, new TestDataSource(testDocument));
        OMElement root = f.createOMElement("root", rootNS);
        root.addChild(element);

        // Test getting the namespace, localpart and prefix.  This should used not result in expansion
        assertTrue(element.getLocalName().equals("library"));
        assertTrue(element.getNamespace().getNamespaceURI().equals(
                "http://www.sosnoski.com/uwjws/library"));
        assertTrue(element.getNamespace().getPrefix().equals("DUMMYPREFIX"));

        // Serialize and consume.  This should not cause expansion and currently won't update
        // the name of the element.
        StringWriter writer = new StringWriter();
        XMLStreamWriter xmlwriter = StAXUtils.createXMLStreamWriter(writer);
        root.serializeAndConsume(writer);
        xmlwriter.flush();
        String result = writer.toString();

        assertTrue(element.getLocalName().equals("library"));
        assertTrue(element.getNamespace().getNamespaceURI().equals(
                "http://www.sosnoski.com/uwjws/library"));
        assertTrue(element.getNamespace().getPrefix().equals("DUMMYPREFIX"));
        assertTrue(result.indexOf("DUMMYPREFIX") <
                0);   // Make sure that the serialized string does not contain DUMMYPREFIX

        assertTrue("Serialized text error" + result, result.indexOf("1930110111") > 0);
    }

    /**
     * Tests the OMSourcedElement localName, namespace and prefix settings before and after
     * serialization Document: testDocument (which uses the default namespace) Type of
     * Serialization: Serialize and cache Tests attempt to rename namespace and localpart, which is
     * not allowed
     *
     * @throws Exception
     */
    public void testName3DefaultPrefix() throws Exception {

        OMFactory f = new OMLinkedListImplFactory();

        // Create OMSE with a DUMMYPREFIX prefix even though the underlying element uses the default prefix
        OMNamespace rootNS = new OMNamespaceImpl("http://sampleroot", "rootPrefix");
        OMNamespace ns = new OMNamespaceImpl("http://DUMMYNS", "DUMMYPREFIX");
        OMElement element =
                new OMSourcedElementImpl("DUMMYNAME", ns, f, new TestDataSource(testDocument));
        OMElement root = f.createOMElement("root", rootNS);
        root.addChild(element);

        // Test getting the namespace, localpart and prefix.  This should used not result in expansion
        assertTrue(element.getLocalName().equals("DUMMYNAME"));
        assertTrue(element.getNamespace().getNamespaceURI().equals("http://DUMMYNS"));
        assertTrue(element.getNamespace().getPrefix().equals("DUMMYPREFIX"));

        // Serialize and cache.  This should cause expansion and update the name to match the testDocument string
        StringWriter writer = new StringWriter();
        XMLStreamWriter xmlwriter = StAXUtils.createXMLStreamWriter(writer);

        try {
            root.serialize(writer);
        } catch (Exception e) {
            // Current Behavior
            // The current OMSourceElementImpl ensures that the namespace and localName
            // are consistent with the original setting.
            return;
        }

        xmlwriter.flush();
        String result = writer.toString();

        assertTrue(element.getLocalName().equals("library"));
        assertTrue(element.getNamespace().getNamespaceURI().equals(
                "http://www.sosnoski.com/uwjws/library"));
        assertTrue(element.getNamespace().getPrefix().equals(""));
        assertTrue(result.indexOf("DUMMY") <
                0);   // Make sure that the serialized string does not contain the DUMMY values

        assertTrue("Serialized text error" + result, result.indexOf("1930110111") > 0);

        // Serialize again
        writer = new StringWriter();
        xmlwriter = StAXUtils.createXMLStreamWriter(writer);
        root.serialize(writer);
        xmlwriter.flush();
        result = writer.toString();

        assertTrue(element.getLocalName().equals("library"));
        assertTrue(element.getNamespace().getNamespaceURI().equals(
                "http://www.sosnoski.com/uwjws/library"));
        assertTrue(element.getNamespace().getPrefix().equals(""));
        assertTrue(result.indexOf("DUMMY") <
                0);   // Make sure that the serialized string does not contain the DUMMY values

        assertTrue("Serialized text error" + result, result.indexOf("1930110111") > 0);


    }

    /**
     * Tests the OMSourcedElement localName, namespace and prefix settings before and after
     * serialization Document: testDocument (which uses the default namespace) Type of
     * Serialization: Serialize and consume Tests that the namespace and localName are not affected
     * by the serializeAndConsume
     *
     * @throws Exception
     */
    public void testName4DefaultPrefix() throws Exception {

        OMFactory f = new OMLinkedListImplFactory();

        // Create OMSE with a DUMMYPREFIX prefix even though the underlying element uses the default prefix
        OMNamespace rootNS = new OMNamespaceImpl("http://sampleroot", "rootPrefix");
        OMNamespace ns = new OMNamespaceImpl("http://DUMMYNS", "DUMMYPREFIX");
        OMElement element =
                new OMSourcedElementImpl("DUMMYNAME", ns, f, new TestDataSource(testDocument));
        OMElement root = f.createOMElement("root", rootNS);
        root.addChild(element);

        // Test getting the namespace, localpart and prefix.  This should used not result in expansion
        assertTrue(element.getLocalName().equals("DUMMYNAME"));
        assertTrue(element.getNamespace().getNamespaceURI().equals("http://DUMMYNS"));
        assertTrue(element.getNamespace().getPrefix().equals("DUMMYPREFIX"));

        // Serialize and consume.  This should not cause expansion and currently won't update
        // the name of the element.
        StringWriter writer = new StringWriter();
        XMLStreamWriter xmlwriter = StAXUtils.createXMLStreamWriter(writer);
        root.serializeAndConsume(writer);
        xmlwriter.flush();
        String result = writer.toString();

        assertTrue(element.getLocalName().equals("DUMMYNAME"));
        assertTrue(element.getNamespace().getNamespaceURI().equals("http://DUMMYNS"));
        assertTrue(element.getNamespace().getPrefix().equals("DUMMYPREFIX"));
        assertTrue(result.indexOf("DUMMY") <
                0);   // Make sure that the serialized string does not contain the DUMMY values

        assertTrue("Serialized text error" + result, result.indexOf("1930110111") > 0);
    }

    /**
     * Tests the OMSourcedElement localName, namespace and prefix settings before and after
     * serialization Document: testDocument2 (which uses a qualified prefix) Type of Serialization:
     * Serialize and cache Prefix test
     *
     * @throws Exception
     */
    public void testName1QualifiedPrefix() throws Exception {

        OMFactory f = new OMLinkedListImplFactory();

        // Create OMSE with a DUMMYPREFIX prefix even though the underlying element uses the default prefix
        OMNamespace rootNS = new OMNamespaceImpl("http://sampleroot", "rootPrefix");
        OMNamespace ns = new OMNamespaceImpl("http://www.sosnoski.com/uwjws/library", "");
        OMElement element =
                new OMSourcedElementImpl("library", ns, f, new TestDataSource(testDocument2));
        OMElement root = f.createOMElement("root", rootNS);
        root.addChild(element);

        // Test getting the namespace, localpart and prefix.  This should used not result in expansion
        assertTrue(element.getLocalName().equals("library"));
        assertTrue(element.getNamespace().getNamespaceURI().equals(
                "http://www.sosnoski.com/uwjws/library"));
        assertTrue(element.getNamespace().getPrefix().equals(""));

        // Serialize and cache.  This should cause expansion and update the name to match the testDocument string
        StringWriter writer = new StringWriter();
        XMLStreamWriter xmlwriter = StAXUtils.createXMLStreamWriter(writer);
        root.serialize(writer);
        xmlwriter.flush();
        String result = writer.toString();

        assertTrue(element.getLocalName().equals("library"));
        assertTrue(element.getNamespace().getNamespaceURI().equals(
                "http://www.sosnoski.com/uwjws/library"));
        assertTrue(element.getNamespace().getPrefix().equals("pre"));
        assertTrue(element.getDefaultNamespace() == null);
        assertTrue(result.indexOf("xmlns=") <
                0);// Make sure that the serialized string does not contain default prefix declaration
        assertTrue("Serialized text error" + result, result.indexOf("1930110111") > 0);

        // Serialize again
        writer = new StringWriter();
        xmlwriter = StAXUtils.createXMLStreamWriter(writer);
        root.serialize(writer);
        xmlwriter.flush();
        result = writer.toString();

        assertTrue(element.getLocalName().equals("library"));
        assertTrue(element.getNamespace().getNamespaceURI().equals(
                "http://www.sosnoski.com/uwjws/library"));
        assertTrue(element.getNamespace().getPrefix().equals("pre"));
        assertTrue(result.indexOf("xmlns=") <
                0); // Make sure that the serialized string does not contain default prefix declaration
        assertTrue(element.getDefaultNamespace() == null);
        assertTrue("Serialized text error" + result, result.indexOf("1930110111") > 0);

    }

    /**
     * Tests the OMSourcedElement localName, namespace and prefix settings before and after
     * serialization Document: testDocument2 (which uses a qualified prefix) Type of Serialization:
     * Serialize and consume Tests update of prefix
     *
     * @throws Exception
     */
    public void testName2QualifiedPrefix() throws Exception {

        OMFactory f = new OMLinkedListImplFactory();

        // Create OMSE with a DUMMYPREFIX prefix even though the underlying element uses the default prefix
        OMNamespace rootNS = new OMNamespaceImpl("http://sampleroot", "rootPrefix");
        OMNamespace ns = new OMNamespaceImpl("http://www.sosnoski.com/uwjws/library", "");
        OMElement element =
                new OMSourcedElementImpl("library", ns, f, new TestDataSource(testDocument2));
        OMElement root = f.createOMElement("root", rootNS);
        root.addChild(element);

        // Test getting the namespace, localpart and prefix.  This should used not result in expansion
        assertTrue(element.getLocalName().equals("library"));
        assertTrue(element.getNamespace().getNamespaceURI().equals(
                "http://www.sosnoski.com/uwjws/library"));
        assertTrue(element.getNamespace().getPrefix().equals(""));

        // Serialize and consume.  This should not cause expansion and currently won't update
        // the name of the element.
        StringWriter writer = new StringWriter();
        XMLStreamWriter xmlwriter = StAXUtils.createXMLStreamWriter(writer);
        root.serializeAndConsume(writer);
        xmlwriter.flush();
        String result = writer.toString();

        assertTrue(element.getLocalName().equals("library"));
        assertTrue(element.getNamespace().getNamespaceURI().equals(
                "http://www.sosnoski.com/uwjws/library"));
        assertTrue(element.getNamespace().getPrefix().equals(""));
        assertTrue(result.indexOf("xmlns=") <
                0);  // Make sure that the serialized string does not contain default prefix declaration

        assertTrue("Serialized text error" + result, result.indexOf("1930110111") > 0);
    }

    /**
     * Tests the OMSourcedElement localName, namespace and prefix settings before and after
     * serialization Document: testDocument2 (which uses a qualified prefix) Type of Serialization:
     * Serialize and cache Tests attempt to rename namespace and localpart, which is not allowed
     *
     * @throws Exception
     */
    public void testName3QualifiedPrefix() throws Exception {

        OMFactory f = new OMLinkedListImplFactory();

        // Create OMSE with a DUMMYPREFIX prefix even though the underlying element uses the default prefix
        OMNamespace rootNS = new OMNamespaceImpl("http://sampleroot", "rootPrefix");
        OMNamespace ns = new OMNamespaceImpl("http://DUMMYNS", "DUMMYPREFIX");
        OMElement element =
                new OMSourcedElementImpl("DUMMYNAME", ns, f, new TestDataSource(testDocument2));
        OMElement root = f.createOMElement("root", rootNS);
        root.addChild(element);

        // Test getting the namespace, localpart and prefix.  This should used not result in expansion
        assertTrue(element.getLocalName().equals("DUMMYNAME"));
        assertTrue(element.getNamespace().getNamespaceURI().equals("http://DUMMYNS"));
        assertTrue(element.getNamespace().getPrefix().equals("DUMMYPREFIX"));

        // Serialize and cache.  This should cause expansion and update the name to match the testDocument string
        StringWriter writer = new StringWriter();
        XMLStreamWriter xmlwriter = StAXUtils.createXMLStreamWriter(writer);

        try {
            root.serialize(writer);
        } catch (Exception e) {
            // Current Behavior
            // The current OMSourceElementImpl ensures that the namespace and localName
            // are consistent with the original setting.
            return;
        }


        xmlwriter.flush();
        String result = writer.toString();

        assertTrue(element.getLocalName().equals("library"));
        assertTrue(element.getNamespace().getNamespaceURI().equals(
                "http://www.sosnoski.com/uwjws/library"));
        assertTrue(element.getNamespace().getPrefix().equals(""));
        assertTrue(result.indexOf("DUMMY") <
                0);// Make sure that the serialized string does not contain the DUMMY values

        assertTrue("Serialized text error" + result, result.indexOf("1930110111") > 0);

        // Serialize again
        writer = new StringWriter();
        xmlwriter = StAXUtils.createXMLStreamWriter(writer);
        root.serialize(writer);
        xmlwriter.flush();
        result = writer.toString();

        assertTrue(element.getLocalName().equals("library"));
        assertTrue(element.getNamespace().getNamespaceURI().equals(
                "http://www.sosnoski.com/uwjws/library"));
        assertTrue(element.getNamespace().getPrefix().equals(""));
        assertTrue(result.indexOf("DUMMY") <
                0);// Make sure that the serialized string does not contain the DUMMY values

        assertTrue("Serialized text error" + result, result.indexOf("1930110111") > 0);


    }

    /**
     * Tests the OMSourcedElement localName, namespace and prefix settings before and after
     * serialization Document: testDocument2 (which uses a qualified prefix) Type of Serialization:
     * Serialize and cache Tests attempt to rename namespace and localpart, which is not allowed
     *
     * @throws Exception
     */
    public void testName4QualifiedPrefix() throws Exception {

        OMFactory f = new OMLinkedListImplFactory();

        // Create OMSE with a DUMMYPREFIX prefix even though the underlying element uses the default prefix
        OMNamespace rootNS = new OMNamespaceImpl("http://sampleroot", "rootPrefix");
        OMNamespace ns = new OMNamespaceImpl("http://DUMMYNS", "");
        OMElement element =
                new OMSourcedElementImpl("DUMMYNAME", ns, f, new TestDataSource(testDocument2));
        OMElement root = f.createOMElement("root", rootNS);
        root.addChild(element);

        // Test getting the namespace, localpart and prefix.  This should used not result in expansion
        assertTrue(element.getLocalName().equals("DUMMYNAME"));
        assertTrue(element.getNamespace().getNamespaceURI().equals("http://DUMMYNS"));
        assertTrue(element.getNamespace().getPrefix().equals(""));

        // Serialize and consume.  This should not cause expansion and currently won't update
        // the name of the element.
        StringWriter writer = new StringWriter();
        XMLStreamWriter xmlwriter = StAXUtils.createXMLStreamWriter(writer);
        root.serializeAndConsume(writer);
        xmlwriter.flush();
        String result = writer.toString();

        assertTrue(element.getLocalName().equals("DUMMYNAME"));
        assertTrue(element.getNamespace().getNamespaceURI().equals("http://DUMMYNS"));
        assertTrue(element.getNamespace().getPrefix().equals(""));
        assertTrue(result.indexOf("DUMMY") <
                0); // Make sure that the serialized string does not contain the DUMMY values
        assertTrue(result.indexOf("xmlns=") <
                0);// Make sure that the serialized string does not contain the default prefix declaration

        assertTrue("Serialized text error" + result, result.indexOf("1930110111") > 0);
    }

    /**
     * Tests the OMSourcedElement localName, namespace and prefix settings before and after
     * serialization Document: testDocument3 (which uses unqualified names) Type of Serialization:
     * Serialize and cache Prefix test
     *
     * @throws Exception
     */
    public void testName1Unqualified() throws Exception {

        OMFactory f = new OMLinkedListImplFactory();

        // Create OMSE with a DUMMYPREFIX prefix even though the underlying element uses the default prefix
        OMNamespace rootNS = new OMNamespaceImpl("http://sampleroot", "rootPrefix");
        OMNamespace ns = new OMNamespaceImpl("", "");
        OMElement element =
                new OMSourcedElementImpl("library", ns, f, new TestDataSource(testDocument3));
        OMElement root = f.createOMElement("root", rootNS);
        root.addChild(element);

        // Test getting the namespace, localpart and prefix.  This should used not result in expansion
        assertTrue(element.getLocalName().equals("library"));
        assertTrue(element.getNamespace().getNamespaceURI().equals(""));
        assertTrue(element.getNamespace().getPrefix().equals(""));

        // Serialize and cache.  This should cause expansion and update the name to match the testDocument string
        StringWriter writer = new StringWriter();
        XMLStreamWriter xmlwriter = StAXUtils.createXMLStreamWriter(writer);
        root.serialize(writer);
        xmlwriter.flush();
        String result = writer.toString();

        assertTrue(element.getLocalName().equals("library"));
        assertTrue(element.getNamespace().getNamespaceURI().equals(""));
        assertTrue(element.getNamespace().getPrefix().equals(""));
        assertTrue(element.getDefaultNamespace() == null ||
                element.getDefaultNamespace().getNamespaceURI().isEmpty());
        assertTrue(result.indexOf("xmlns=") <
                0); // Make sure that the serialized string does not contain default prefix declaration
        assertTrue("Serialized text error" + result, result.indexOf("1930110111") > 0);

        // Serialize again
        writer = new StringWriter();
        xmlwriter = StAXUtils.createXMLStreamWriter(writer);
        root.serialize(writer);
        xmlwriter.flush();
        result = writer.toString();

        assertTrue(element.getLocalName().equals("library"));
        assertTrue(element.getNamespace().getNamespaceURI().equals(""));
        assertTrue(element.getNamespace().getPrefix().equals(""));
        assertTrue(result.indexOf("xmlns=") <
                0);// Make sure that the serialized string does not contain default prefix declaration
        assertTrue(element.getDefaultNamespace() == null ||
                element.getDefaultNamespace().getNamespaceURI().isEmpty());
        assertTrue("Serialized text error" + result, result.indexOf("1930110111") > 0);

    }

    /**
     * Tests the OMSourcedElement localName, namespace and prefix settings before and after
     * serialization Document: testDocument3 (which uses unqualified names) Type of Serialization:
     * Serialize and consume Tests update of prefix
     *
     * @throws Exception
     */
    public void testName2Unqualified() throws Exception {

        OMFactory f = new OMLinkedListImplFactory();

        // Create OMSE with a DUMMYPREFIX prefix even though the underlying element uses the default prefix
        OMNamespace rootNS = new OMNamespaceImpl("http://sampleroot", "rootPrefix");
        OMNamespace ns = new OMNamespaceImpl("", "");
        OMElement element =
                new OMSourcedElementImpl("library", ns, f, new TestDataSource(testDocument3));
        OMElement root = f.createOMElement("root", rootNS);
        root.addChild(element);

        // Test getting the namespace, localpart and prefix.  This should used not result in expansion
        assertTrue(element.getLocalName().equals("library"));
        assertTrue(element.getNamespace().getNamespaceURI().equals(""));
        assertTrue(element.getNamespace().getPrefix().equals(""));

        // Serialize and consume.  This should not cause expansion and currently won't update
        // the name of the element.
        StringWriter writer = new StringWriter();
        XMLStreamWriter xmlwriter = StAXUtils.createXMLStreamWriter(writer);
        root.serializeAndConsume(writer);
        xmlwriter.flush();
        String result = writer.toString();

        assertTrue(element.getLocalName().equals("library"));
        assertTrue(element.getNamespace().getNamespaceURI().equals(""));
        assertTrue(element.getNamespace().getPrefix().equals(""));
        assertTrue(result.indexOf("xmlns=") <
                0);// Make sure that the serialized string does not contain default prefix declaration

        assertTrue("Serialized text error" + result, result.indexOf("1930110111") > 0);
    }

    /**
     * Tests the OMSourcedElement localName, namespace and prefix settings before and after
     * serialization Document: testDocument3 (which uses unqualified names) Type of Serialization:
     * Serialize and cache Tests attempt to rename namespace and localpart, which is not allowed
     *
     * @throws Exception
     */
    public void testName3Unqualified() throws Exception {

        OMFactory f = new OMLinkedListImplFactory();

        // Create OMSE with a DUMMYPREFIX prefix even though the underlying element uses the default prefix
        OMNamespace rootNS = new OMNamespaceImpl("http://sampleroot", "rootPrefix");
        OMNamespace ns = new OMNamespaceImpl("http://DUMMYNS", "DUMMYPREFIX");
        OMElement element =
                new OMSourcedElementImpl("DUMMYNAME", ns, f, new TestDataSource(testDocument3));
        OMElement root = f.createOMElement("root", rootNS);
        root.addChild(element);

        // Test getting the namespace, localpart and prefix.  This should used not result in expansion
        assertTrue(element.getLocalName().equals("DUMMYNAME"));
        assertTrue(element.getNamespace().getNamespaceURI().equals("http://DUMMYNS"));
        assertTrue(element.getNamespace().getPrefix().equals("DUMMYPREFIX"));

        // Serialize and cache.  This should cause expansion and update the name to match the testDocument string
        StringWriter writer = new StringWriter();
        XMLStreamWriter xmlwriter = StAXUtils.createXMLStreamWriter(writer);

        try {
            root.serialize(writer);
        } catch (Exception e) {
            // Current Behavior
            // The current OMSourceElementImpl ensures that the namespace and localName
            // are consistent with the original setting.
            return;
        }


        xmlwriter.flush();
        String result = writer.toString();

        assertTrue(element.getLocalName().equals("library"));
        assertTrue(element.getNamespace().getNamespaceURI().equals(""));
        assertTrue(element.getNamespace().getPrefix().equals(""));
        assertTrue(result.indexOf("DUMMY") <
                0); // Make sure that the serialized string does not contain the DUMMY values

        assertTrue("Serialized text error" + result, result.indexOf("1930110111") > 0);

        // Serialize again
        writer = new StringWriter();
        xmlwriter = StAXUtils.createXMLStreamWriter(writer);
        root.serialize(writer);
        xmlwriter.flush();
        result = writer.toString();

        assertTrue(element.getLocalName().equals("library"));
        assertTrue(element.getNamespace().getNamespaceURI().equals(
                "http://www.sosnoski.com/uwjws/library"));
        assertTrue(element.getNamespace().getPrefix().equals(""));
        assertTrue(result.indexOf("DUMMY") <
                0);  // Make sure that the serialized string does not contain the DUMMY values

        assertTrue("Serialized text error" + result, result.indexOf("1930110111") > 0);


    }

    /**
     * Tests the OMSourcedElement localName, namespace and prefix settings before and after
     * serialization Document: testDocument3 (which uses unqualified names) Type of Serialization:
     * Serialize and cache Tests attempt to rename namespace and localpart, which is not allowed
     *
     * @throws Exception
     */
    public void testName4Unqualified() throws Exception {

        OMFactory f = new OMLinkedListImplFactory();

        // Create OMSE with a DUMMYPREFIX prefix even though the underlying element uses the default prefix
        OMNamespace rootNS = new OMNamespaceImpl("http://sampleroot", "rootPrefix");
        OMNamespace ns = new OMNamespaceImpl("http://DUMMYNS", "");
        OMElement element =
                new OMSourcedElementImpl("DUMMYNAME", ns, f, new TestDataSource(testDocument3));
        OMElement root = f.createOMElement("root", rootNS);
        root.addChild(element);

        // Test getting the namespace, localpart and prefix.  This should used not result in expansion
        assertTrue(element.getLocalName().equals("DUMMYNAME"));
        assertTrue(element.getNamespace().getNamespaceURI().equals("http://DUMMYNS"));
        assertTrue(element.getNamespace().getPrefix().equals(""));

        // Serialize and consume.  This should not cause expansion and currently won't update
        // the name of the element.
        StringWriter writer = new StringWriter();
        XMLStreamWriter xmlwriter = StAXUtils.createXMLStreamWriter(writer);
        root.serializeAndConsume(writer);
        xmlwriter.flush();
        String result = writer.toString();

        assertTrue(element.getLocalName().equals("DUMMYNAME"));
        assertTrue(element.getNamespace().getNamespaceURI().equals("http://DUMMYNS"));
        assertTrue(element.getNamespace().getPrefix().equals(""));
        assertTrue(result.indexOf("DUMMY") <
                0);  // Make sure that the serialized string does not contain the DUMMY values
        assertTrue(result.indexOf("xmlns=") <
                0); // Make sure that the serialized string does not contain the default prefix declaration

        assertTrue("Serialized text error" + result, result.indexOf("1930110111") > 0);
    }


    /**
     * Tests OMSourcedElement serialization when the root (parent) is serialized.
     *
     * @throws Exception
     */
    public void testSerializeAndConsumeToXMLWriterEmbedded() throws Exception {
        StringWriter writer = new StringWriter();
        XMLStreamWriter xmlwriter = StAXUtils.createXMLStreamWriter(writer);
        root.serializeAndConsume(writer);
        xmlwriter.flush();
        String result = writer.toString();

        // We can't test for equivalence because the underlying OMSourceElement is 
        // streamed as it is serialized.  So I am testing for an internal value.
        assertTrue("Serialized text error" + result, result.indexOf("1930110111") > 0);
        assertFalse("Element expansion when serializing", element.isExpanded());
    }

    /**
     * Tests OMSourcedElement getReader support
     *
     * @throws Exception
     */
    public void testSerializeToXMLWriterFromReader() throws Exception {
        StringWriter writer = new StringWriter();
        XMLStreamWriter xmlwriter = StAXUtils.createXMLStreamWriter(writer);

        StAXOMBuilder builder = new StAXOMBuilder(element.getXMLStreamReader());
        OMDocument omDocument = builder.getDocument();
        Iterator it = omDocument.getChildren();
        while (it.hasNext()) {
            OMNode omNode = (OMNode) it.next();
            // TODO: quick fix required because OMChildrenIterator#next() no longer builds the node
            omNode.getNextOMSibling();
            omNode.serializeAndConsume(xmlwriter);
        }

        xmlwriter.flush();
        String result = writer.toString();

        // We can't test for equivalence because the underlying OMSourceElement is 
        // changed as it is serialized.  So I am testing for an internal value.
        assertTrue("Serialized text error" + result, result.indexOf("1930110111") > 0);
        assertFalse("Element expansion when serializing", element.isExpanded());
    }

    /**
     * Tests OMSourcedElement processing when the getReader() of the parent is accessed.
     *
     * @throws Exception
     */
    public void testSerializeToXMLWriterFromReaderEmbedded() throws Exception {
        StringWriter writer = new StringWriter();
        XMLStreamWriter xmlwriter = StAXUtils.createXMLStreamWriter(writer);

        StAXOMBuilder builder = new StAXOMBuilder(root.getXMLStreamReader());
        OMDocument omDocument = builder.getDocument();
        Iterator it = omDocument.getChildren();
        while (it.hasNext()) {
            OMNode omNode = (OMNode) it.next();
            // TODO: quick fix required because OMChildrenIterator#next() no longer builds the node
            omNode.getNextOMSibling();
            omNode.serializeAndConsume(xmlwriter);
        }
        xmlwriter.flush();
        String result = writer.toString();
        // We can't test for equivalence because the underlying OMSourceElement is 
        // changed as it is serialized.  So I am testing for an internal value.
        assertTrue("Serialized text error" + result, result.indexOf("1930110111") > 0);
        // The implementation uses OMNavigator to walk the tree.  Currently OMNavigator must 
        // expand the OMSourcedElement to correctly walk the elements. (See OMNavigator._getFirstChild)
        //assertFalse("Element expansion when serializing", element.isExpanded());
    }

    /**
     * Make sure the expanded OMSourcedElement behaves like a normal OMElement.
     *
     * @throws Exception
     */
    public void testExpand() throws Exception {
        element.getAllDeclaredNamespaces();
        assertEquals("Expanded namespace count error", 1,
                     countItems(element.getAllDeclaredNamespaces()));
        assertEquals("Expanded attribute count error", 1,
                     countItems(element.getAllAttributes()));
        assertEquals("Expanded attribute value error", "1",
                     element.getAttributeValue(new QName("books")));
        OMElement child = element.getFirstElement();
        assertEquals("Child element name", "type", child.getLocalName());
        assertEquals("Child element namespace",
                     "http://www.sosnoski.com/uwjws/library",
                     child.getNamespace().getNamespaceURI());
        OMNode next = child.getNextOMSibling();
        assertTrue("Expected child element", next instanceof OMElement);
        next = next.getNextOMSibling();
        assertTrue("Expected child element", next instanceof OMElement);
        child = (OMElement) next;
        assertEquals("Child element name", "book", child.getLocalName());
        assertEquals("Child element namespace",
                     "http://www.sosnoski.com/uwjws/library",
                     child.getNamespace().getNamespaceURI());
        assertEquals("Attribute value error", "xml",
                     child.getAttributeValue(new QName("type")));
    }

    public void testSetDataSourceOnAlreadyExpandedElement() {
        // Make sure the OMSourcedElement is expanded
        element.getFirstOMChild();
        assertTrue(element.isExpanded());
        // Now set a new data source
        element.setDataSource(new TestDataSource(testDocument2));
        assertFalse(element.isExpanded());
        // getNextOMSibling should not expand the element
        assertNull(element.getNextOMSibling());
        assertFalse(element.isExpanded());
    }

    public void testSerializeModifiedOMSEWithNonDestructiveDataSource() throws Exception {
        OMDataSourceExt ds = new CharArrayDataSource("<element><child/></element>".toCharArray());
        assertFalse(ds.isDestructiveWrite());
        
        OMFactory f = new OMLinkedListImplFactory();
        OMElement element = new OMSourcedElementImpl("element", null, f, ds);
        
        element.getFirstElement().setText("TEST");
        
        StringWriter sw = new StringWriter();
        element.serialize(sw);
        assertTrue(sw.toString().indexOf("TEST") != -1);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        element.serialize(baos);
        assertTrue(new String(baos.toByteArray(), "UTF-8").indexOf("TEST") != -1);
        
        assertTrue(element.toString().indexOf("TEST") != -1);
    }

    private static class TestDataSource implements OMDataSource {
        // The data source is a ByteArrayInputStream so that we can verify that the datasource 
        // is only accessed once.  Currently there is no way to identify a destructive vs. non-destructive OMDataSource.
        private final ByteArrayInputStream data;

        private TestDataSource(String data) {
            this.data = new ByteArrayInputStream(data.getBytes());
            this.data.mark(0);
        }

        /* (non-Javadoc)
         * @see org.apache.axiom.om.OMDataSource#serialize(java.io.OutputStream, org.apache.axiom.om.OMOutputFormat)
         */
        public void serialize(OutputStream output, OMOutputFormat format)
                throws XMLStreamException {
            try {
                output.write(getBytes());
            } catch (IOException e) {
                throw new XMLStreamException(e);
            }
        }

        /* (non-Javadoc)
         * @see org.apache.axiom.om.OMDataSource#serialize(java.io.Writer, org.apache.axiom.om.OMOutputFormat)
         */
        public void serialize(Writer writer, OMOutputFormat format) throws XMLStreamException {
            try {
                writer.write(getString());
            } catch (IOException e) {
                throw new XMLStreamException(e);
            }
        }

        /* (non-Javadoc)
         * @see org.apache.axiom.om.OMDataSource#serialize(javax.xml.stream.XMLStreamWriter)
         */
        public void serialize(XMLStreamWriter xmlWriter) throws XMLStreamException {
            StreamingOMSerializer serializer = new StreamingOMSerializer();
            serializer.serialize(getReader(), xmlWriter);
        }

        /* (non-Javadoc)
         * @see org.apache.axiom.om.OMDataSource#getReader()
         */
        public XMLStreamReader getReader() throws XMLStreamException {
            return StAXUtils.createXMLStreamReader(new StringReader(getString()));
        }

        private byte[] getBytes() throws XMLStreamException {
            try {
                // The data from the data source should only be accessed once
                //data.reset();
                byte[] rc = new byte[data.available()];
                data.read(rc);
                return rc;
            } catch (IOException io) {
                throw new XMLStreamException(io);
            }
        }

        private String getString() throws XMLStreamException {
            String text = new String(getBytes());
            return text;
        }
    }
}