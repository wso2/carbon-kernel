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

package org.apache.axiom.util.blob;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Random;

import org.apache.commons.io.IOUtils;

import junit.framework.TestCase;

public abstract class WritableBlobTestBase extends TestCase {
    private final static Random random = new Random();
    
    protected abstract WritableBlob createBlob();
    protected abstract void releaseBlob(WritableBlob blob);
    
    private void doTestRandomReadWrite(int size) throws IOException {
        byte[] data = new byte[size];
        random.nextBytes(data);
        WritableBlob blob = createBlob();
        try {
            OutputStream out = blob.getOutputStream();
            // Write the test data in chunks with random size
            int offset = 0;
            while (offset < data.length) {
                int c = Math.min(512 + random.nextInt(1024), data.length - offset);
                out.write(data, offset, c);
                offset += c;
            }
            out.close();
            assertEquals(size, blob.getLength());
            // Reread the test data, again in chunks with random size
            InputStream in = blob.getInputStream();
            offset = 0;
            byte[] data2 = new byte[data.length];
            byte[] buffer = new byte[2048];
            while (true) {
                int bufferOffset = random.nextInt(512);
                int c = 512 + random.nextInt(1024);
                int read = in.read(buffer, bufferOffset, c);
                if (read == -1) {
                    break;
                }
                int newOffset = offset + read;
                assertTrue(newOffset <= data2.length);
                System.arraycopy(buffer, bufferOffset, data2, offset, read);
                offset = newOffset;
            }
            assertEquals(data2.length, offset);
            in.close();
            assertTrue(Arrays.equals(data, data2));
        }
        finally {
            releaseBlob(blob);
        }
    }
    
    public void testRandomReadWriteLength10000() throws IOException {
        doTestRandomReadWrite(10000);
    }
    
    public void testRandomReadWriteLength100000() throws IOException {
        doTestRandomReadWrite(100000);
    }
    
    public void testMarkReset() throws IOException {
        byte[] sourceData1 = new byte[2000];
        byte[] sourceData2 = new byte[2000];
        random.nextBytes(sourceData1);
        random.nextBytes(sourceData2);
        WritableBlob blob = createBlob();
        try {
            OutputStream out = blob.getOutputStream();
            out.write(sourceData1);
            out.write(sourceData2);
            out.close();
            DataInputStream in = new DataInputStream(blob.getInputStream());
            byte[] data1 = new byte[sourceData1.length];
            byte[] data2 = new byte[sourceData2.length];
            in.readFully(data1);
            in.mark(sourceData2.length);
            in.readFully(data2);
            in.reset();
            in.readFully(data2);
            assertTrue(Arrays.equals(sourceData1, data1));
            assertTrue(Arrays.equals(sourceData2, data2));
        } finally {
            releaseBlob(blob);
        }
    }
    
    private void testReadFrom(int size) throws IOException {
        byte[] data = new byte[size];
        random.nextBytes(data);
        WritableBlob blob = createBlob();
        try {
            blob.readFrom(new ByteArrayInputStream(data), -1);
            InputStream in = blob.getInputStream();
            try {
                assertTrue(Arrays.equals(data, IOUtils.toByteArray(in)));
            }
            finally {
                in.close();
            }
        }
        finally {
            releaseBlob(blob);
        }
    }
    
    public void testReadFromLength10000() throws IOException {
        testReadFrom(10000);
    }
    
    public void testReadFromLength100000() throws IOException {
        testReadFrom(100000);
    }
    
    public void testGetOutputStreamUncommitted() throws Exception {
        WritableBlob blob = createBlob();
        try {
            OutputStream out = blob.getOutputStream();
            try {
                try {
                    blob.getOutputStream();
                    fail("Expected IllegalStateException");
                } catch (IllegalStateException ex) {
                    // Expected
                }
            } finally {
                out.close();
            }
        } finally {
            releaseBlob(blob);
        }
    }
    
    public void testGetOutputStreamCommitted() throws Exception {
        WritableBlob blob = createBlob();
        try {
            OutputStream out = blob.getOutputStream();
            out.close();
            try {
                blob.getOutputStream();
                fail("Expected IllegalStateException");
            } catch (IllegalStateException ex) {
                // Expected
            }
        } finally {
            releaseBlob(blob);
        }
    }
    
    public void testGetInputStreamNew() throws Exception {
        WritableBlob blob = createBlob();
        try {
            if (blob.isSupportingReadUncommitted()) {
                // The order of instructions is important here: we first get
                // the input stream (when the blob is still in state NEW) and
                // only then we request the output stream (which will put the
                // stream in state UNCOMMITTED).
                InputStream in = blob.getInputStream();
                OutputStream out = blob.getOutputStream();
                assertEquals(-1, in.read());
                // Check that any data written to the output stream immediately becomes available
                // on the input stream.
                byte[] data = new byte[1000];
                random.nextBytes(data);
                out.write(data);
                assertTrue(Arrays.equals(data, IOUtils.toByteArray(in)));
                random.nextBytes(data);
                out.write(data);
                assertTrue(Arrays.equals(data, IOUtils.toByteArray(in)));
            } else {
                try {
                    blob.getInputStream();
                    fail("Expected IllegalStateException");
                } catch (IllegalStateException ex) {
                    // Expected
                }
            }
        } finally {
            releaseBlob(blob);
        }
    }
    
    public void testGetInputStreamUncommitted() throws Exception {
        WritableBlob blob = createBlob();
        try {
            OutputStream out = blob.getOutputStream();
            try {
                byte[] data = new byte[1000];
                random.nextBytes(data);
                out.write(data);
                if (blob.isSupportingReadUncommitted()) {
                    InputStream in = blob.getInputStream();
                    try {
                        assertTrue(Arrays.equals(data, IOUtils.toByteArray(in)));
                    } finally {
                        in.close();
                    }
                } else {
                    try {
                        blob.getInputStream();
                        fail("Expected IllegalStateException");
                    } catch (IllegalStateException ex) {
                        // Expected
                    }
                }
            } finally {
                out.close();
            }
        } finally {
            releaseBlob(blob);
        }
    }
}
