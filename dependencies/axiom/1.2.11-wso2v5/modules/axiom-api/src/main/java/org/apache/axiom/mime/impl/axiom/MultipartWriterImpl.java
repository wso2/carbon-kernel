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
package org.apache.axiom.mime.impl.axiom;

import java.io.IOException;
import java.io.OutputStream;

import javax.activation.DataHandler;

import org.apache.axiom.mime.MultipartWriter;
import org.apache.axiom.util.base64.Base64EncodingOutputStream;

class MultipartWriterImpl implements MultipartWriter {
    class PartOutputStream extends OutputStream {
        private final OutputStream parent;

        public PartOutputStream(OutputStream parent) {
            this.parent = parent;
        }

        public void write(int b) throws IOException {
            parent.write(b);
        }

        public void write(byte[] b, int off, int len) throws IOException {
            parent.write(b, off, len);
        }

        public void write(byte[] b) throws IOException {
            parent.write(b);
        }
        
        public void close() throws IOException {
            if (parent instanceof Base64EncodingOutputStream) {
                ((Base64EncodingOutputStream)parent).complete();
            }
            writeAscii("\r\n");
        }
    }
    
    private final OutputStream out;
    private final String boundary;
    private final byte[] buffer = new byte[256];

    public MultipartWriterImpl(OutputStream out, String boundary) {
        this.out = out;
        this.boundary = boundary;
    }

    void writeAscii(String s) throws IOException {
        int count = 0;
        for (int i=0, len=s.length(); i<len; i++) {
            char c = s.charAt(i);
            if (c >= 128) {
                throw new IOException("Illegal character '" + c + "'");
            }
            buffer[count++] = (byte)c;
            if (count == buffer.length) {
                out.write(buffer);
                count = 0;
            }
        }
        if (count > 0) {
            out.write(buffer, 0, count);
        }
    }
    
    public OutputStream writePart(String contentType, String contentTransferEncoding,
            String contentID) throws IOException {
        OutputStream transferEncoder;
        if (contentTransferEncoding.equals("8bit") || contentTransferEncoding.equals("binary")) {
            transferEncoder = out;
        } else {
            // We support no content transfer encodings other than 8bit, binary and base64.
            transferEncoder = new Base64EncodingOutputStream(out);
            contentTransferEncoding = "base64";
        }
        writeAscii("--");
        writeAscii(boundary);
        // TODO: specify if contentType == null is legal and check what to do
        if (contentType != null) {
            writeAscii("\r\nContent-Type: ");
            writeAscii(contentType);
        }
        writeAscii("\r\nContent-Transfer-Encoding: ");
        writeAscii(contentTransferEncoding);
        // TODO: specify that the content ID may be null
        if (contentID != null) {
            writeAscii("\r\nContent-ID: <");
            writeAscii(contentID);
            out.write('>');
        }
        writeAscii("\r\n\r\n");
        return new PartOutputStream(transferEncoder);
    }
    
    public void writePart(DataHandler dataHandler, String contentTransferEncoding, String contentID)
            throws IOException {
        OutputStream partOutputStream = writePart(dataHandler.getContentType(), contentTransferEncoding, contentID);
        dataHandler.writeTo(partOutputStream);
        partOutputStream.close();
    }

    public void complete() throws IOException {
        writeAscii("--");
        writeAscii(boundary);
        writeAscii("--\r\n");
    }
}
