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
import java.util.Collection;
import java.util.List;


public class DeleteAttachmentTest extends AbstractTestCase {
    File temp;

    public void testIncomingAttachmentInputStreamFunctions() throws Exception {
        InputStream inStream = getTestResource(TestConstants.MTOM_MESSAGE);
        Attachments attachments = new Attachments(inStream, TestConstants.MTOM_MESSAGE_CONTENT_TYPE);

        Collection list = attachments.getContentIDSet();
        assertEquals(3, list.size());
        
        assertTrue(list.contains("1.urn:uuid:A3ADBAEE51A1A87B2A11443668160943@apache.org"));
        assertTrue(list.contains("2.urn:uuid:A3ADBAEE51A1A87B2A11443668160994@apache.org"));
        
        attachments.removeDataHandler("1.urn:uuid:A3ADBAEE51A1A87B2A11443668160943@apache.org");

        List list2 = attachments.getContentIDList();
        assertEquals(2, list2.size());
        assertEquals(2, attachments.getMap().size());

        assertFalse(list2.contains("1.urn:uuid:A3ADBAEE51A1A87B2A11443668160943@apache.org"));
        assertTrue(list2.contains("2.urn:uuid:A3ADBAEE51A1A87B2A11443668160994@apache.org"));
    }
}