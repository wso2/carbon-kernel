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

package org.apache.axiom.om.impl.dom;

import org.apache.axiom.om.OMException;
import org.apache.axiom.om.TestConstants;
import org.apache.axiom.soap.SOAPEnvelope;

public class MessagesTest extends OMDOMTestCase {
    SOAPEnvelope soapEnvelope;

    public void testMessageWithLotOfWhiteSpaces() throws OMException,
            Exception {
        soapEnvelope =
                (SOAPEnvelope) OMTestUtils.getOMBuilder(
                        getTestResource(TestConstants.WHITESPACE_MESSAGE))
                        .getDocumentElement();
        OMTestUtils.walkThrough(soapEnvelope);
        soapEnvelope.close(false);
    }

    public void testMinimalMessage() throws OMException, Exception {
        soapEnvelope =
                (SOAPEnvelope) OMTestUtils.getOMBuilder(
                        getTestResource(TestConstants.MINIMAL_MESSAGE))
                        .getDocumentElement();
        OMTestUtils.walkThrough(soapEnvelope);
        soapEnvelope.close(false);
    }

    public void testReallyBigMessage() throws OMException, Exception {
        soapEnvelope =
                (SOAPEnvelope) OMTestUtils.getOMBuilder(
                        getTestResource(TestConstants.REALLY_BIG_MESSAGE))
                        .getDocumentElement();
        OMTestUtils.walkThrough(soapEnvelope);
        soapEnvelope.close(false);
    }

    public void testEmptyBodiedMessage() throws OMException, Exception {
        soapEnvelope =
                (SOAPEnvelope) OMTestUtils.getOMBuilder(
                        getTestResource(TestConstants.EMPTY_BODY_MESSAGE))
                        .getDocumentElement();
        OMTestUtils.walkThrough(soapEnvelope);
        soapEnvelope.close(false);
    }


}
