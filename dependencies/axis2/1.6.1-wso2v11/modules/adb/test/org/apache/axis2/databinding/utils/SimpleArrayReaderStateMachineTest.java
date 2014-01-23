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
import org.apache.axiom.om.util.StAXUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;

public class SimpleArrayReaderStateMachineTest extends TestCase {

    public void testStateMachine() throws Exception {
        String xmlDoc =
                "<wrapper><myIntVal>200</myIntVal><myIntVal>200</myIntVal><myIntVal>200</myIntVal>" +
                        "<myIntVal>200</myIntVal><myIntVal>200</myIntVal><myIntVal>200</myIntVal></wrapper>";
        XMLStreamReader reader = StAXUtils.createXMLStreamReader(
                new StringReader(xmlDoc));
        SimpleArrayReaderStateMachine sm = new SimpleArrayReaderStateMachine();
        sm.setElementNameToTest(new QName("myIntVal"));
        try {
            sm.read(reader);
        } catch (XMLStreamException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        assertEquals(6, sm.getTextArray().length);

    }

    public void testStateMachine3() throws Exception {
        String xmlDoc =
                "<wrapper><myIntVal>200</myIntVal><myIntVal>200</myIntVal><myIntVal>200</myIntVal>" +
                        "<myIntVal>200</myIntVal><myIntVal>200</myIntVal><myIntVal>200</myIntVal></wrapper>";
        XMLStreamReader reader = StAXUtils.createXMLStreamReader(
                new StringReader(xmlDoc));

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT &&
                    "myIntVal".equals(reader.getLocalName())) {
                break;
            }

        }

        SimpleArrayReaderStateMachine sm = new SimpleArrayReaderStateMachine();
        sm.setElementNameToTest(new QName("myIntVal"));
        try {
            sm.read(reader);
        } catch (XMLStreamException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        assertEquals(6, sm.getTextArray().length);

    }

    public void testStateMachine2() throws Exception {
        String xmlDoc = "<wrapper><myIntVal>200</myIntVal></wrapper>";
        XMLStreamReader reader = StAXUtils.createXMLStreamReader(
                new StringReader(xmlDoc));
        SimpleArrayReaderStateMachine sm = new SimpleArrayReaderStateMachine();
        sm.setElementNameToTest(new QName("myIntVal"));
        try {
            sm.read(reader);
        } catch (XMLStreamException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        assertEquals("200", sm.getTextArray()[0]);
    }

    public void testStateMachineEmptyArray() throws Exception {
        String xmlDoc = "<wrapper></wrapper>";
        XMLStreamReader reader = StAXUtils.createXMLStreamReader(
                new StringReader(xmlDoc));
        SimpleArrayReaderStateMachine sm = new SimpleArrayReaderStateMachine();
        sm.setElementNameToTest(new QName("myIntVal"));
        try {
            sm.read(reader);
        } catch (Exception e) {

        }

        assertEquals(0, sm.getTextArray().length);


    }

}
