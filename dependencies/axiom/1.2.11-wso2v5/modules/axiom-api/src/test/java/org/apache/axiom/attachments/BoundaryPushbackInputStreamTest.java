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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PushbackInputStream;

import junit.framework.TestCase;

public class BoundaryPushbackInputStreamTest extends TestCase {
    /**
     * Check that the implementation consistently skips the newline sequence at the end of
     * an attachment by initializing the stream with various values for pushBackSize and
     * using various buffer sizes to read from the stream.
     * 
     * This provides regression testing for WSCOMMONS-328.
     * 
     * @throws Exception
     */
    public void testReadWithNewline() throws Exception {
        byte[] boundary = "--boundary".getBytes("ascii");
        byte[] data = "xxxxxxxxxxxxxxxxxxxxxxxxxxx\r\n--boundary\r\nyyyyyyyyyyyyyy".getBytes("ascii");
        for (int bufferSize = 1; bufferSize < data.length; bufferSize++) {
            byte[] buffer = new byte[bufferSize];
            for (int pushBackSize = 0; pushBackSize < data.length; pushBackSize++) {
                PushbackInputStream pbis = new PushbackInputStream(new ByteArrayInputStream(data), 20);
                InputStream bpbis = new BoundaryPushbackInputStream(pbis, boundary, pushBackSize);
                int count = 0;
                int read;
                while ((read = bpbis.read(buffer)) != -1) {
                    count += read;
                }
                assertEquals(27, count);
            }
        }
    }
}
