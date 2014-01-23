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

package org.apache.axiom.attachments;

import org.apache.axiom.om.AbstractTestCase;
import org.apache.axiom.om.TestConstants;

import java.io.File;
import java.io.InputStream;


/** Test the PartOnFile class */

public class IncomingAttachmentTest extends AbstractTestCase {
    File temp;

    public void testIncomingAttachmentInputStreamFunctions() throws Exception {
        InputStream inStream = getTestResource(TestConstants.MTOM_MESSAGE);
        Attachments attachments = new Attachments(inStream, TestConstants.MTOM_MESSAGE_CONTENT_TYPE);

        // Get the inputstream container
        IncomingAttachmentStreams ias = attachments.getIncomingAttachmentStreams();

        IncomingAttachmentInputStream dataIs;

        // Img1 stream
        dataIs = ias.getNextStream();

        // Make sure it was the first attachment
        assertEquals("<1.urn:uuid:A3ADBAEE51A1A87B2A11443668160943@apache.org>",
                     dataIs.getContentId());

        // Consume the stream
        while (dataIs.read() != -1) ;

        // Img2 stream
        dataIs = ias.getNextStream();
        assertEquals("<2.urn:uuid:A3ADBAEE51A1A87B2A11443668160994@apache.org>",
                     dataIs.getContentId());

        // Test if getContentType() works..
        assertEquals("image/jpeg", dataIs.getContentType());

        // Test if a adding/getting a header works
        dataIs.addHeader("new-header", "test-value");
        assertEquals("test-value", dataIs.getHeader("new-header"));
    }
}
