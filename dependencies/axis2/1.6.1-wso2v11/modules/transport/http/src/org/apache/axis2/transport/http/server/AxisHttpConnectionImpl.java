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

package org.apache.axis2.transport.http.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpConnectionMetrics;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.ProtocolVersion;
import org.apache.http.entity.ContentLengthStrategy;
import org.apache.http.impl.DefaultHttpRequestFactory;
import org.apache.http.impl.entity.StrictContentLengthStrategy;
import org.apache.http.impl.io.ChunkedInputStream;
import org.apache.http.impl.io.ChunkedOutputStream;
import org.apache.http.impl.io.ContentLengthInputStream;
import org.apache.http.impl.io.ContentLengthOutputStream;
import org.apache.http.impl.io.HttpRequestParser;
import org.apache.http.impl.io.HttpResponseWriter;
import org.apache.http.impl.io.IdentityInputStream;
import org.apache.http.impl.io.IdentityOutputStream;
import org.apache.http.impl.io.SocketInputBuffer;
import org.apache.http.impl.io.SocketOutputBuffer;
import org.apache.http.io.HttpMessageParser;
import org.apache.http.io.HttpMessageWriter;
import org.apache.http.io.SessionInputBuffer;
import org.apache.http.io.SessionOutputBuffer;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

public class AxisHttpConnectionImpl implements AxisHttpConnection {

    private static final Log HEADERLOG =
        LogFactory.getLog("org.apache.axis2.transport.http.server.wire");

    private final Socket socket;
    private final SessionOutputBuffer outbuffer;
    private final SessionInputBuffer inbuffer;
    private final HttpMessageParser requestParser;
    private final HttpMessageWriter responseWriter;
    private final ContentLengthStrategy contentLenStrategy;

    private OutputStream out = null;
    private InputStream in = null;
    
    public AxisHttpConnectionImpl(final Socket socket, final HttpParams params) 
            throws IOException {
        super();
        if (socket == null) {
            throw new IllegalArgumentException("Socket may not be null"); 
        }
        if (params == null) {
            throw new IllegalArgumentException("HTTP parameters may not be null"); 
        }
        socket.setTcpNoDelay(HttpConnectionParams.getTcpNoDelay(params));
        socket.setSoTimeout(HttpConnectionParams.getSoTimeout(params));
        
        int linger = HttpConnectionParams.getLinger(params);
        if (linger >= 0) {
            socket.setSoLinger(linger > 0, linger);
        }
        
        int buffersize = HttpConnectionParams.getSocketBufferSize(params);
        this.socket = socket;
        this.outbuffer = new SocketOutputBuffer(socket, buffersize, params); 
        this.inbuffer = new SocketInputBuffer(socket, buffersize, params); 
        this.contentLenStrategy = new StrictContentLengthStrategy();
        this.requestParser = new HttpRequestParser(
                this.inbuffer, null, new DefaultHttpRequestFactory(), params);
        this.responseWriter = new HttpResponseWriter(
                this.outbuffer, null, params);
    }

    public void close() throws IOException {
        this.outbuffer.flush();
        try {
            this.socket.shutdownOutput();
        } catch (IOException ignore) {
        }
        try {
            this.socket.shutdownInput();
        } catch (IOException ignore) {
        }
        this.socket.close();
    }

    public boolean isOpen() {
        return !this.socket.isClosed();
    }

    public boolean isStale() {
        try {
            this.inbuffer.isDataAvailable(1);
            return false;
        } catch (IOException ex) {
            return true;
        }
    }

    public void shutdown() throws IOException {
        Socket tmpsocket = this.socket;
        if (tmpsocket != null) {
            tmpsocket.close();
        }
    }

    public HttpRequest receiveRequest() throws HttpException, IOException {
        HttpRequest request = (HttpRequest) this.requestParser.parse();
        if (HEADERLOG.isDebugEnabled()) {
            HEADERLOG.debug(">> " + request.getRequestLine().toString());
            for (HeaderIterator it = request.headerIterator(); it.hasNext(); ) {
                HEADERLOG.debug(">> " + it.nextHeader().toString());
            }
        }
        
        // Prepare input stream
        this.in = null;
        if (request instanceof HttpEntityEnclosingRequest) {
            long len = this.contentLenStrategy.determineLength(request);
            if (len == ContentLengthStrategy.CHUNKED) {
                this.in = new ChunkedInputStream(this.inbuffer);
            } else if (len == ContentLengthStrategy.IDENTITY) {
                this.in = new IdentityInputStream(this.inbuffer);                            
            } else {
                this.in = new ContentLengthInputStream(inbuffer, len);
            }
        }
        return request;
    }
    
    public void sendResponse(final HttpResponse response) 
            throws HttpException, IOException {
        if (response == null) {
            throw new IllegalArgumentException("HTTP response may not be null");
        }

        if (HEADERLOG.isDebugEnabled()) {
            HEADERLOG.debug("<< " + response.getStatusLine().toString());
            for (HeaderIterator it = response.headerIterator(); it.hasNext(); ) {
                HEADERLOG.debug("<< " + it.nextHeader().toString());
            }
        }
        
        this.responseWriter.write(response);

        // Prepare output stream
        this.out = null;
        ProtocolVersion ver = response.getStatusLine().getProtocolVersion();
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            long len = entity.getContentLength();
            if (entity.isChunked() && ver.greaterEquals(HttpVersion.HTTP_1_1)) {
                this.out = new ChunkedOutputStream(this.outbuffer);
            } else if (len >= 0) {
                this.out = new ContentLengthOutputStream(this.outbuffer, len);
            } else {
                this.out = new IdentityOutputStream(this.outbuffer); 
            }
        } else {
            this.outbuffer.flush();
        }
    }
    
    public InputStream getInputStream() {
        return this.in;
    }

    public OutputStream getOutputStream() {
        return this.out;
    }
    
    public void flush() throws IOException {
        if (this.out != null) {
            this.out.flush();
        } else {
            this.outbuffer.flush();
        }
    }

    public void reset() throws IOException {
        if (this.in != null) {
            this.in.close();
            this.in = null;
        }
        if (this.out != null) {
            this.out.flush();
            this.out.close();
            this.out = null;
        }
    }
    
    public int getSocketTimeout() {
        try {
            return this.socket.getSoTimeout();
        } catch (SocketException ex) {
            return -1;
        }
    }

    public void setSocketTimeout(int timeout) {
        try {
            this.socket.setSoTimeout(timeout);
        } catch (SocketException ex) {
        }
    }

    public InetAddress getLocalAddress() {
        if (this.socket != null) {
            return this.socket.getLocalAddress();
        } else {
            return null;
        }
    }

    public int getLocalPort() {
        if (this.socket != null) {
            return this.socket.getLocalPort();
        } else {
            return -1;
        }
    }

    public InetAddress getRemoteAddress() {
        if (this.socket != null) {
            return this.socket.getInetAddress();
        } else {
            return null;
        }
    }

    public int getRemotePort() {
        if (this.socket != null) {
            return this.socket.getPort();
        } else {
            return -1;
        }
    }

    public HttpConnectionMetrics getMetrics() {
        return null;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("[");
        if (isOpen()) {
            buffer.append(this.socket.getInetAddress());
        } else {
            buffer.append("closed");
        }
        buffer.append("]");
        return buffer.toString();
    }

}
