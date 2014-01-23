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

package org.apache.axis2.jaxws.addressing.util;

import junit.framework.TestCase;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.w3c.dom.Element;

import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReferenceParameterListTests extends TestCase {
    private String testResourceDir = System.getProperty("basedir", ".") + "/test-resources";
    private String resourceFileName = "xml/referenceparameters.xml";
    private SOAPHeader header;

    public void setUp() throws Exception {
        File resourceFile = new File(testResourceDir, resourceFileName);
        XMLStreamReader parser = StAXUtils.createXMLStreamReader(new FileReader(resourceFile));
        StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(parser, null);
        header = ((SOAPEnvelope)builder.getDocumentElement()).getHeader();
    }
    
    public void testReferenceParameterList() throws Exception {
        List<Element> emptyList = new ReferenceParameterList();
        assertTrue(emptyList.isEmpty());
        assertEquals(0, emptyList.size());
        
        Set<String>results = new HashSet<String>();
        results.add("0123456789");
        results.add("ABCDEFG");
        results.add("abcdefg");
        results.add("xyz");
        
        List<Element> rpList = new ReferenceParameterList(header);
        assertFalse(rpList.isEmpty());
        assertEquals(results.size(), rpList.size());

        for (Element rp : rpList) {
            String value = rp.getTextContent();
            if (results.contains(value)) {
                results.remove(value);
            }
            else {
                fail("Value not recognized: " + value);
            }
        }
        
        assertEquals(0, results.size());
    }
}
