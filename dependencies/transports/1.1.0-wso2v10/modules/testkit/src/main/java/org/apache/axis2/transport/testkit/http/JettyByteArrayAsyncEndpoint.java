/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.axis2.transport.testkit.http;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;

import org.apache.axis2.transport.testkit.message.IncomingMessage;
import org.apache.axis2.transport.testkit.tests.Setup;
import org.apache.axis2.transport.testkit.tests.Transient;
import org.apache.axis2.transport.testkit.util.LogManager;
import org.apache.commons.io.IOUtils;
import org.mortbay.http.HttpException;
import org.mortbay.http.HttpRequest;

public class JettyByteArrayAsyncEndpoint extends JettyAsyncEndpoint<byte[]> {
    private @Transient LogManager logManager;

    @Setup @SuppressWarnings("unused")
    private void setUp(LogManager logManager) throws Exception {
        this.logManager = logManager;
    }
    
    @Override
    protected IncomingMessage<byte[]> handle(HttpRequest request) throws HttpException, IOException {
        byte[] data = IOUtils.toByteArray(request.getInputStream());
        logRequest(request, data);
        ContentType contentType;
        try {
            contentType = new ContentType(request.getContentType());
        } catch (ParseException ex) {
            throw new HttpException(500, "Unparsable Content-Type");
        }
        return new IncomingMessage<byte[]>(contentType, data);
    }

    private void logRequest(HttpRequest request, byte[] data) throws IOException {
        OutputStream out = logManager.createLog("jetty");
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(out), false);
        for (Enumeration<?> e = request.getFieldNames(); e.hasMoreElements(); ) {
            String name = (String)e.nextElement();
            for (Enumeration<?> e2 = request.getFieldValues(name); e2.hasMoreElements(); ) {
                pw.print(name);
                pw.print(": ");
                pw.println((String)e2.nextElement());
            }
        }
        pw.println();
        pw.flush();
        out.write(data);
    }
}
