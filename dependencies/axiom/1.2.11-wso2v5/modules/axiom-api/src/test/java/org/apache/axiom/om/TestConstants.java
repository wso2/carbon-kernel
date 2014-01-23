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

/** All the various files created 03-Nov-2005 12:02:12 */

public class TestConstants {
    public static final String SOAP_SOAPMESSAGE = "soap/soapmessage.xml";
    public static final String SOAP_SOAPMESSAGE1 = "soap/soapmessage1.xml";
    public static final String SAMPLE1 = "soap/sample1.xml";
    public static final String TEST = "soap/test.xml";
    public static final String WHITESPACE_MESSAGE = "soap/whitespacedMessage.xml";
    public static final String MINIMAL_MESSAGE = "soap/minimalMessage.xml";
    public static final String REALLY_BIG_MESSAGE = "soap/reallyReallyBigMessage.xml";
    public static final String EMPTY_BODY_MESSAGE = "soap/emtyBodymessage.xml";
    public static final String BAD_WRONG_SOAP_NS = "badsoap/wrongSoapNs.xml";
    public static final String BAD_TWO_HEADERS = "badsoap/twoheaders.xml";
    public static final String BAD_TWO_BODY = "badsoap/twoBodymessage.xml";
    public static final String BAD_ENVELOPE_MISSING = "badsoap/envelopeMissing.xml";
    public static final String BAD_HEADER_BODY_WRONG_ORDER = "badsoap/haederBodyWrongOrder.xml";

    public static final String MTOM_MESSAGE = "mtom/MTOMAttachmentStream.bin";
    public static final String MTOM_MESSAGE_BOUNDARY = "MIMEBoundaryurn:uuid:A3ADBAEE51A1A87B2A11443668160701";
    public static final String MTOM_MESSAGE_START = "0.urn:uuid:A3ADBAEE51A1A87B2A11443668160702@apache.org";
    public static final String MTOM_MESSAGE_CONTENT_TYPE =
                        "multipart/related; " +
                        "boundary=\"" + MTOM_MESSAGE_BOUNDARY + "\"; " +
                        "type=\"application/xop+xml\"; " +
                        "start=\"<" + MTOM_MESSAGE_START +">\"; " +
                        "start-info=\"application/soap+xml\"; " +
                        "charset=UTF-8;" +
                        "action=\"mtomSample\"";
    public static final String MTOM_MESSAGE_INLINED = "mtom/MTOMAttachmentStream_inlined.xml";
    
    
    private TestConstants() {
    }


}
