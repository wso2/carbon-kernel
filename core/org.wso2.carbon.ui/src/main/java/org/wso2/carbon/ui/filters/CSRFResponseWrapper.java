/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.ui.filters;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * This class wraps up the original servlet response in order to allow modification of original content
 */
public final class CSRFResponseWrapper extends HttpServletResponseWrapper {

    private HttpServletResponse originalResponse = null;
    private ByteArrayOutputStream output = null;
    private ResponseWrapperOutputStream stream = null;
    private PrintWriter writer = null;

    public CSRFResponseWrapper(HttpServletResponse response) {

        super(response);

        this.originalResponse = response;
        reset();
    }

    @Override
    public void flushBuffer() throws IOException {
        writer.flush();
        stream.flush();
        output.flush();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return stream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return writer;
    }

    @Override
    public void reset() {
        this.output = new ByteArrayOutputStream();
        this.stream = new ResponseWrapperOutputStream(output);
        this.writer = new PrintWriter(stream);
    }

    @Override
    public void resetBuffer() {
        reset();
    }

    public HttpServletResponse getOriginalResponse() {
        return originalResponse;
    }

    public byte[] getContent() throws IOException {
        flushBuffer();

        return output.toByteArray();
    }

    public void setContent(byte[] content) throws IOException {
        reset();

        stream.write(content);
    }

    public void setContent(String s) throws IOException {
        setContent(s.getBytes());
    }

    public void write() throws IOException {
        writeContent();
    }

    private void writeContent() throws IOException {
        byte[] content = getContent();
        ServletResponse response = getResponse();
        OutputStream os = response.getOutputStream();

        response.setContentLength(content.length);
        os.write(content);
        os.close();
    }
}
