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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class IncomingAttachmentInputStream extends InputStream {
    private HashMap _headers = null;

    private HashMap _headersLowerCase = null;
    
    private InputStream _stream = null;
    private IncomingAttachmentStreams parentContainer;

    public static final String HEADER_CONTENT_DESCRIPTION = "content-description";
    public static final String HEADER_CONTENT_TYPE = "content-type";
    public static final String HEADER_CONTENT_TRANSFER_ENCODING = "content-transfer-encoding";
    public static final String HEADER_CONTENT_TYPE_JMS = "contentType";
    public static final String HEADER_CONTENT_LENGTH = "content-length";
    public static final String HEADER_CONTENT_LOCATION = "content-location";
    public static final String HEADER_CONTENT_ID = "content-id";

    /** @param in  */
    public IncomingAttachmentInputStream(InputStream in,
                                         IncomingAttachmentStreams parentContainer) {
        _stream = in;
        this.parentContainer = parentContainer;
    }

    /** @return MIME headers for this attachment. May be null if no headers were set. */
    public Map getHeaders() {
        return _headers;
    }

    /**
     * Add a header.
     *
     * @param name
     * @param value
     */
    public void addHeader(String name, String value) {
        if (_headers == null) {
            _headers = new HashMap();
            _headersLowerCase = new HashMap();
        }
        _headers.put(name, value);
        _headersLowerCase.put(name.toLowerCase(), value);
    }

    /**
     * Get a header value.
     *
     * @param name
     * @return The header found or null if not found.
     */
    public String getHeader(String name) {
        Object header = null;
        if (_headersLowerCase == null || (header = _headersLowerCase.get(name.toLowerCase())) == null) {
            return null;
        }
        return header.toString();
    }

    /** @return The header with HTTPConstants.HEADER_CONTENT_ID as the key. */
    public String getContentId() {
        return getHeader(HEADER_CONTENT_ID);
    }

    /** @return The header with HTTPConstants.HEADER_CONTENT_LOCATION as the key. */
    public String getContentLocation() {
        return getHeader(HEADER_CONTENT_LOCATION);
    }

    /** @return The header with HTTPConstants.HEADER_CONTENT_TYPE as the key. */
    public String getContentType() {
        return getHeader(HEADER_CONTENT_TYPE);
    }

    /**
     * Don't want to support mark and reset since this may get us into concurrency problem when
     * different pieces of software may have a handle to the underlying InputStream.
     */
    public boolean markSupported() {
        return false;
    }

    public void reset() throws IOException {
        throw new IOException("markNotSupported");
    }

    public void mark(int readLimit) {
        // do nothing
    }

    public int read() throws IOException {
        int retval = _stream.read();
        parentContainer.setReadyToGetNextStream(retval == -1);
        return retval;
    }

    public int read(byte[] b) throws IOException {
        int retval = _stream.read(b);
        parentContainer.setReadyToGetNextStream(retval == -1);
        return retval;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        int retval = _stream.read(b, off, len);
        parentContainer.setReadyToGetNextStream(retval == -1);
        return retval;
    }
}
