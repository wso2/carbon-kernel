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

/**
 *Test the discard method
 */
public class OMDiscardTest extends AbstractTestCase {


    public OMDiscardTest(String testName) {
        super(testName);   
    }

    
    public void testDiscard(){
        OMElement documentElement = null;
        try {
            // first build the OM tree without caching and see whether we can discard
            // an element from it
            StAXOMBuilder builder = new StAXOMBuilder(getXMLStreamReader());
            documentElement = builder.getDocumentElement();

            documentElement.getFirstElement().discard();

            String envelopeString = documentElement.toStringWithConsume();
        } catch (Exception e) {
            fail("discarding an element should work!");
        }
        documentElement.close(false);
    }

    private XMLStreamReader getXMLStreamReader() throws XMLStreamException, FileNotFoundException {
        return StAXUtils.createXMLStreamReader(getTestResource(TestConstants.SOAP_SOAPMESSAGE));
    }

}
