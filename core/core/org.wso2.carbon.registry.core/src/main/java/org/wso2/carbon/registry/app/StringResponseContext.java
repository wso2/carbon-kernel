/*
 * Copyright (c) 2007, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.registry.app;

import org.apache.abdera.protocol.server.context.AbstractResponseContext;
import org.wso2.carbon.registry.core.RegistryConstants;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Custom ResponseContext which allows us to return a simple string message inside a &lt;result&gt;
 * element, with a specified response code.
 */
public class StringResponseContext extends AbstractResponseContext {

    String message;

    /**
     * Creates a response that can contain a message.
     *
     * @param message the message.
     * @param status  the status
     */
    public StringResponseContext(String message, int status) {
        this.message = message;
        setStatus(status);
    }

    /**
     * Creates a response that can contain a message.
     *
     * @param e      the exception.
     * @param status the status
     */
    public StringResponseContext(Exception e, int status) {
        // TODO: FIXME: Check whether this constructor is really useful or replace all occurrences
        // with StackTraceResponseContext. - Senaka
        StringWriter writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));
        message = writer.getBuffer().toString();
        setStatus(status);
    }

    /**
     * Whether the response contains an entity.
     *
     * @return true if the response contains an entity
     */
    public boolean hasEntity() {
        return message != null;
    }

    /**
     * Method to write the message in to given output.
     *
     * @param outputStream the output stream of the HTTP response
     *
     * @throws IOException if an error occurred.
     */
    public void writeTo(OutputStream outputStream) throws IOException {
        writeTo(new OutputStreamWriter(outputStream, RegistryConstants.DEFAULT_CHARSET_ENCODING));
    }

    /**
     * Method to write the message in to given output.
     *
     * @param writer the Writer connected to the HTTP response
     *
     * @throws IOException if an error occurred.
     */
    public void writeTo(Writer writer) throws IOException {
        if (hasEntity()) {
            writer.write(message);
            writer.flush();
        }
    }

    /**
     * Method to write the message in to given output.
     *
     * @param abderaWriter the abdera writer.
     * @param outputStream the output stream of the HTTP response.
     *
     * @throws IOException if an error occurred.
     */
    public void writeTo(OutputStream outputStream, org.apache.abdera.writer.Writer abderaWriter)
            throws IOException {
        throw new UnsupportedOperationException("This method is not supported");
    }

    /**
     * Method to write the message in to given output.
     *
     * @param abderaWriter the abdera writer.
     * @param writer       the Writer connected to the HTTP response.
     *
     * @throws IOException if an error occurred.
     */
    public void writeTo(Writer writer, org.apache.abdera.writer.Writer abderaWriter)
            throws IOException {
        throw new UnsupportedOperationException("This method is not supported");
    }
}
