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

package org.apache.axiom.util.stax.xop;

import java.io.ByteArrayInputStream;

import javax.imageio.ImageIO;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.om.AbstractTestCase;
import org.apache.axiom.om.TestConstants;
import org.apache.axiom.om.impl.builder.OMAttachmentAccessorMimePartProvider;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.testutils.stax.XMLStreamReaderComparator;
import org.apache.axiom.util.base64.Base64Utils;

public class XOPDecodingStreamReaderTest extends AbstractTestCase {
    private XMLStreamReader getXOPDecodingStreamReader() throws Exception {
        Attachments attachments = new Attachments(getTestResource(TestConstants.MTOM_MESSAGE),
                TestConstants.MTOM_MESSAGE_CONTENT_TYPE);
        return new XOPDecodingStreamReader(
                StAXUtils.createXMLStreamReader(attachments.getSOAPPartInputStream()),
                new OMAttachmentAccessorMimePartProvider(attachments));
    }
    
    public void testCompareToInlined() throws Exception {
        XMLStreamReader expected = StAXUtils.createXMLStreamReader(
                getTestResource(TestConstants.MTOM_MESSAGE_INLINED));
        XMLStreamReader actual = getXOPDecodingStreamReader();
        XMLStreamReaderComparator comparator = new XMLStreamReaderComparator(expected, actual);
        comparator.addPrefix("xop");
        comparator.compare();
        expected.close();
        actual.close();
    }
    
    public void testGetElementText() throws Exception {
        XMLStreamReader reader = getXOPDecodingStreamReader();
        while (!reader.isStartElement() || !reader.getLocalName().equals("image1")) {
            reader.next();
        }
        String base64 = reader.getElementText();
        byte[] data = Base64Utils.decode(base64);
        // The data is actually a JPEG image. Try to decode it to check that the data is not
        // corrupted.
        ImageIO.read(new ByteArrayInputStream(data));
        reader.close();
    }
}
