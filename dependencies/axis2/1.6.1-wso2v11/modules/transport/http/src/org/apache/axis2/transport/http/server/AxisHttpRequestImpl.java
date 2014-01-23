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

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.ProtocolVersion;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;

import java.io.IOException;
import java.io.InputStream;

public class AxisHttpRequestImpl implements AxisHttpRequest {

    private final HttpRequest request;
    private final AxisHttpConnection conn;
    private final HttpProcessor httpproc;
    private final HttpContext context;
    
    public AxisHttpRequestImpl(
            final AxisHttpConnection conn,
            final HttpRequest request, 
            final HttpProcessor httpproc,
            final HttpContext context) {
        super();
        if (conn == null) {
            throw new IllegalArgumentException("HTTP connection may not be null");
        }
        if (request == null) {
            throw new IllegalArgumentException("HTTP request may not be null");
        }
        if (httpproc == null) {
            throw new IllegalArgumentException("HTTP processor may not be null");
        }
        if (context == null) {
            throw new IllegalArgumentException("HTTP context may not be null");
        }
        this.request = request;
        this.conn = conn;
        this.httpproc = httpproc;
        this.context = context;
    }
    
    public void prepare() throws IOException, HttpException {
        this.context.setAttribute(ExecutionContext.HTTP_CONNECTION, this.conn);
        this.context.setAttribute(ExecutionContext.HTTP_REQUEST, this.request);
        
        this.httpproc.process(this.request, this.context);
    }

    public String getMethod() {
        return this.request.getRequestLine().getMethod();
    }

    public String getRequestURI() {
        return this.request.getRequestLine().getUri();
    }

    public ProtocolVersion getProtocolVersion() {
        return this.request.getRequestLine().getProtocolVersion();
    }

    public String getContentType() {
        Header header = this.request.getFirstHeader(HTTP.CONTENT_TYPE);
        if (header != null) {
            return header.getValue();
        } else {
            return null;
        }
    }

    public InputStream getInputStream() {
        return this.conn.getInputStream();
    }

    public void addHeader(final Header header) {
        this.request.addHeader(header);
    }

    public void addHeader(final String name, final String value) {
        this.request.addHeader(name, value);
    }

    public boolean containsHeader(final String name) {
        return this.request.containsHeader(name);
    }

    public Header[] getAllHeaders() {
        return this.request.getAllHeaders();
    }

    public Header getFirstHeader(final String name) {
        return this.request.getFirstHeader(name);
    }

    public Header[] getHeaders(String name) {
        return this.request.getHeaders(name);
    }

    public Header getLastHeader(final String name) {
        return this.request.getLastHeader(name);
    }

    public HeaderIterator headerIterator() {
        return this.request.headerIterator();
    }

    public HeaderIterator headerIterator(final String name) {
        return this.request.headerIterator(name);
    }

    public void removeHeader(final Header header) {
        this.request.removeHeader(header);
    }

    public void removeHeaders(final String name) {
        this.request.removeHeaders(name);
    }

    public void setHeader(final Header header) {
        this.request.setHeader(header);
    }

    public void setHeader(final String name, final String value) {
        this.request.setHeader(name, value);
    }

    public void setHeaders(Header[] headers) {
        this.request.setHeaders(headers);
    }

    public HttpParams getParams() {
        return this.request.getParams();
    }

    public void setParams(final HttpParams params) {
        this.request.setParams(params);
    }
    
}
