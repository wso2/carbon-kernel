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

import org.apache.abdera.protocol.server.context.SimpleResponseContext;
import org.wso2.carbon.registry.core.secure.AuthorizationFailedException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.HttpURLConnection;

/**
 * This is an extension of the {@link SimpleResponseContext} in Abdera. This is used for outputting
 * exceptions along with stack traces.
 */
public class StackTraceResponseContext extends SimpleResponseContext {

    /**
     * Set this to false to squelch stack traces
     */
    private static boolean emitStackTraces = true;

    private Exception e;

    /**
     * Method to set whether stack traces must be emitted.
     *
     * @param emitStackTraces whether stack traces must be emitted.
     */
    @SuppressWarnings("unused")
    public static void setEmitStackTraces(boolean emitStackTraces) {
        StackTraceResponseContext.emitStackTraces = emitStackTraces;
    }

    /**
     * Creates a response that can contain a stack trace.
     *
     * @param e the exception.
     */
    public StackTraceResponseContext(Exception e) {
        this.e = e;
        if (e instanceof AuthorizationFailedException) {
            setStatus(HttpURLConnection.HTTP_UNAUTHORIZED);
            setHeader("WWW-Authenticate", "Basic realm=\"WSO2-Registry\"");
        } else {
            setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }

    /**
     * Write either the full stack trace or just the Exception message, depending on config
     *
     * @param writer the Writer connected to the HTTP response
     *
     * @throws IOException if an error occurred.
     */
    protected void writeEntity(Writer writer) throws IOException {
        if (emitStackTraces) {
            e.printStackTrace(new PrintWriter(writer));
        } else {
            writer.write(e.getMessage());
        }
        writer.flush();
    }

    /**
     * Whether the response contains an entity.
     *
     * @return true if the response contains an entity
     */
    public boolean hasEntity() {
        return true;
    }
}
