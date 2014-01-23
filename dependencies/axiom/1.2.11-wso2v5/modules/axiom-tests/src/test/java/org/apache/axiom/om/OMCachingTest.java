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
import java.io.FileNotFoundException;

public class OMCachingTest extends AbstractTestCase {
    private XMLStreamReader xmlStreamReader;

    /** @param testName  */
    public OMCachingTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {

    }

    /** This will first serialize the element without caching. Then it tries to serialize again . */
    public void testCachingOne() {

        OMElement documentElement = null;
        try {
            // first build the OM tree without caching and see whether up can cosume it again
            StAXOMBuilder builder = new StAXOMBuilder(getXMLStreamReader());
            documentElement = builder.getDocumentElement();
            String envelopeString = documentElement.toStringWithConsume();
        } catch (XMLStreamException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        try {
            String envelopeString = documentElement.toStringWithConsume();
            fail("Parser should fail as its already being accessed without caching");
        } catch (XMLStreamException e) {
            assertTrue(true);
        }

        documentElement.close(false);
    }

    /** This will first serialize the OMElement with caching and again will try to serialize. */
    public void testCachingTwo() {

        OMElement documentElement = null;
        try {
            // first build the OM tree without caching and see whether up can cosume it again
            StAXOMBuilder builder = new StAXOMBuilder(getXMLStreamReader());
            documentElement = builder.getDocumentElement();
            String envelopeString = documentElement.toString();
            envelopeString = documentElement.toStringWithConsume();
            assertTrue(true);
        } catch (XMLStreamException e) {
            fail("Parser should not failt as the element was serialized with caching");
        } catch (FileNotFoundException e) {
            fail("Parser should not failt as the element was serialized with caching");
        }

        documentElement.close(false);
    }

    private XMLStreamReader getXMLStreamReader() throws XMLStreamException, FileNotFoundException {
        return StAXUtils.createXMLStreamReader(getTestResource(TestConstants.SOAP_SOAPMESSAGE));
    }
}
