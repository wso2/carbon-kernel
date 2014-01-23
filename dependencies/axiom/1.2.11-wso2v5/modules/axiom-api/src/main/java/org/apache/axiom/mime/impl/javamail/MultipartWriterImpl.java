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

package org.apache.axiom.mime.impl.javamail;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;

import org.apache.axiom.mime.MultipartWriter;
import org.apache.axiom.util.blob.BlobDataSource;
import org.apache.axiom.util.blob.MemoryBlob;
import org.apache.axiom.util.blob.WritableBlob;

class MultipartWriterImpl implements MultipartWriter {
    private static final byte[] DASH_DASH = { '-', '-' };
    private static final byte[] CR_LF = { 13, 10 };
    
    class PartOutputStream extends OutputStream {
        private final String contentType;
        private final String contentTransferEncoding;
        private final String contentID;
        private final WritableBlob blob;
        private final OutputStream parent;

        public PartOutputStream(String contentType, String contentTransferEncoding,
                String contentID) {
            this.contentType = contentType;
            this.contentTransferEncoding = contentTransferEncoding;
            this.contentID = contentID;
            blob = new MemoryBlob();
            parent = blob.getOutputStream();
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
            parent.close();
            writePart(new DataHandler(new BlobDataSource(blob, contentType)),
                    contentTransferEncoding, contentID);
        }
    }
    
    private final OutputStream out;
    private final byte[] boundary;

    public MultipartWriterImpl(OutputStream out, String boundary) {
        this.out = out;
        try {
            this.boundary = boundary.getBytes("ascii");
        } catch (UnsupportedEncodingException ex) {
            // If we ever get here, then there is something really wrong with the JRE...
            throw new RuntimeException(ex);
        }
    }
    
    public OutputStream writePart(String contentType, String contentTransferEncoding,
            String contentID) throws IOException {
        return new PartOutputStream(contentType, contentTransferEncoding, contentID);
    }

    public void writePart(DataHandler dataHandler, String contentTransferEncoding,
            String contentID) throws IOException {
        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        try {
            mimeBodyPart.setDataHandler(dataHandler);
            mimeBodyPart.addHeader("Content-ID", "<" + contentID + ">");
            mimeBodyPart.addHeader("Content-Type", dataHandler.getContentType());
            mimeBodyPart.addHeader("Content-Transfer-Encoding", contentTransferEncoding);
        } catch (MessagingException ex) {
            IOException ex2 = new IOException("Unable to create MimeBodyPart");
            ex2.initCause(ex);
            throw ex2;
        }
        out.write(DASH_DASH);
        out.write(boundary);
        out.write(CR_LF);
        try {
            mimeBodyPart.writeTo(out);
        } catch (MessagingException ex) {
            IOException ex2 = new IOException("Unable to write the MimeBodyPart object");
            ex2.initCause(ex);
            throw ex2;
        }
        out.write(CR_LF);
    }

    public void complete() throws IOException {
        out.write(DASH_DASH);
        out.write(boundary);
        out.write(DASH_DASH);
        out.write(CR_LF);
    }
}
