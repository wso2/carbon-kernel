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

package org.apache.axis2.databinding.utils;

import junit.framework.TestCase;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axis2.util.StreamWrapper;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;

public class NamedStaxOMBuilderTest extends TestCase {

    public void testNamedOMBuilder() throws Exception {

        String xmlDoc = "<wrapper><myIntVal>200</myIntVal></wrapper>";
        XMLStreamReader reader = StAXUtils.createXMLStreamReader(
                new StringReader(xmlDoc));

        NamedStaxOMBuilder sm = new NamedStaxOMBuilder(reader, new QName("wrapper"));
        OMElement elt = sm.getOMElement();

        assertNotNull(elt);
        assertEquals(elt.getLocalName(), "wrapper");


    }

    public void testNamedOMBuilder1() throws Exception {

        String xmlDoc = "<wrapper><myIntVal>200</myIntVal></wrapper>";
        XMLStreamReader reader = StAXUtils.createXMLStreamReader(
                new StringReader(xmlDoc));

        //move upto the  myIntVal start element first
        boolean done = false;
        QName nameToMatch = new QName("myIntVal");
        while (!done) {
            if (reader.isStartElement() && nameToMatch.equals(reader.getName())) {
                done = true;
            } else {
                reader.next();
            }
        }

        //we need the wrapper here - it is the nature of the builders that
        //they expect the *next* event to be the start element (not the
        //current one) So we need the wrapper to simulate a full fledged
        //
        NamedStaxOMBuilder sm = new NamedStaxOMBuilder(
                new StreamWrapper(reader), nameToMatch);
        OMElement elt = sm.getOMElement();

        assertNotNull(elt);
        assertEquals(elt.getLocalName(), "myIntVal");


    }


}
