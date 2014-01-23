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

import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.StAXUtils;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Iterator;

public class OMDTDTest extends AbstractTestCase {

    private OMDocument document;

    protected void setUp() throws Exception {
        try {
            StAXOMBuilder stAXOMBuilder = new StAXOMBuilder(getTestResource("dtd.xml"));
            document = this.document = stAXOMBuilder.getDocument();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void tearDown() throws Exception {
        document.close(false);
    }

    public void testDTDSerialization() {
        try {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.serialize(baos);
            String serializedString = new String(baos.toByteArray());

            assertTrue(serializedString.indexOf("<!ENTITY foo \"bar\">") > -1);
            assertTrue(serializedString.indexOf("<!ENTITY bar \"foo\">") > -1);
            assertTrue(
                    serializedString.indexOf("<feed xmlns=\"http://www.w3.org/2005/Atom\">") > -1);
        } catch (XMLStreamException e) {
            fail("Bug in serializing OMDocuments which have DTDs, text and a document element");
        }
    }
    
    public void testDTDInWebXML() throws Exception{
        
        // The JSR 173 (StAX) Specification did not do a very good job
        // of defining how DOCTYPE entities are processed.  According
        // to the reference implementation, the external entities of the DOCTYPE
        // are always followed.  This is wrong for a number of reasons.
        //   a) You cannot read the file unless you are network attached ...which
        //      might not be the case.
        //   b) You incur an expensive network access even though in many cases
        //      the DOCTYPE contents are unimportant.
        //  
        // StAX should have allowed the caller to process the DOCTYPE as information
        // only and allow the caller to request external information.  Perhaps this
        // will be addressed in future versions of the specification.
        //
        // For now, we have a work-around.  A "network detached" XMLStreamReader
        // can be obtained from StAXUtils and used to process configuration files
        // (like a web.xml) that may contain DTD information.
        //
        // The following test reads an XML file that has a DTD with a system ID
        // that intentionally points to a non existing URL. With a network
        // detached reader this should not produce errors.
        
        InputStream is = getTestResource("web_w_dtd2.xml");
        XMLStreamReader reader = StAXUtils.createNetworkDetachedXMLStreamReader(is);
        StAXOMBuilder builder = new StAXOMBuilder(reader);
        OMElement root = builder.getDocumentElement();
        assertTrue(root.getLocalName().equals("web-app"));
        OMDocument document = builder.getDocument();
        Iterator i = document.getChildren();
        OMDocType docType = null;
        while (docType == null && i.hasNext()) {
           Object obj = i.next();
           if (obj instanceof OMDocType) {
               docType = (OMDocType) obj;
           }
        }
        assertTrue(docType != null);
        root.close(false);
    }
}
