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
package org.apache.axiom.attachments.impl;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.util.ByteArrayDataSource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import junit.framework.TestCase;

/**
 * Simple test for the BufferUtils copying code
 */
public class BufferUtilsTest extends TestCase {

    byte[] bytes;
    static final int MAX = 1024 * 1024;
   
    protected void setUp() throws Exception {
        bytes = new byte[MAX];
        for (int i = 0; i < MAX /20; i++) {
            for (int j = 0; j < 20; j++) {
                bytes[i*20 + j] = (byte) j;
            }
        }
    }

    /**
     * Create a temp file, and write to it using buffer utils
     * @throws Exception
     */
    public void test() throws Exception {
        // Create temp file
        File file =  File.createTempFile("bufferUtils", "tst");
        file.deleteOnExit();
        try {
            OutputStream fos = new FileOutputStream(file, true);
            for (int i = 0; i < 20; i++) {
                InputStream bais = new ByteArrayInputStream(bytes);
                BufferUtils.inputStream2OutputStream(bais, fos);
                fos.flush();
            }
            fos.close();
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[20];
            fis.read(buffer);
            for (int i = 0; i < buffer.length; i++) {
                assertTrue(buffer[i] == (byte) i);
            }
        } finally {
            file.delete();
        }    
    }
    
    public void testDataSourceBackedDataHandlerExceedLimit() throws IOException {
        File file =  File.createTempFile("bufferUtils", "tst");
        file.deleteOnExit();
        try {
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            try {
                raf.setLength(5000);
            } finally {
                raf.close();
            }
            FileDataSource fds = new FileDataSource(file);
            DataHandler dh = new DataHandler(fds);
            int unsupported= BufferUtils.doesDataHandlerExceedLimit(dh, 0);
            assertEquals(-1, unsupported);
            int doesExceed = BufferUtils.doesDataHandlerExceedLimit(dh, 1000);
            assertEquals(1, doesExceed);
            int doesNotExceed = BufferUtils.doesDataHandlerExceedLimit(dh, 100000);
            assertEquals(0, doesNotExceed);
        } finally {
            file.delete();
        }    
    }
    
    public void testObjectBackedDataHandlerExceedLimit() throws Exception {
        String str = "This is a test String";
        DataHandler dh = new DataHandler(str, "text/plain");          
        int unsupported= BufferUtils.doesDataHandlerExceedLimit(dh, 0);
        assertEquals(-1, unsupported);
        int doesExceed = BufferUtils.doesDataHandlerExceedLimit(dh, 10);
        assertEquals(1, doesExceed);
        int doesNotExceed = BufferUtils.doesDataHandlerExceedLimit(dh, 100);
        assertEquals(0, doesNotExceed);
    }
    
    public void testByteArrayDataSourceBackedDataHandlerExceedLimit() throws Exception {
        String str = "This is a test String";
        byte[] b = str.getBytes();
        ByteArrayDataSource bads = new ByteArrayDataSource(b, "text/plain");
        DataHandler dh = new DataHandler(bads);          
        int unsupported= BufferUtils.doesDataHandlerExceedLimit(dh, 0);
        assertEquals(-1, unsupported);
        int doesExceed = BufferUtils.doesDataHandlerExceedLimit(dh, 10);
        assertEquals(1, doesExceed);
        int doesNotExceed = BufferUtils.doesDataHandlerExceedLimit(dh, 100);
        assertEquals(0, doesNotExceed);
    }
}
