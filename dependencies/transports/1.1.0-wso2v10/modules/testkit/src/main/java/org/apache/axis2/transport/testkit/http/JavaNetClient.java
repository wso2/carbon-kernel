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

import org.apache.axis2.transport.testkit.client.AsyncTestClient;
import org.apache.axis2.transport.testkit.client.ClientOptions;
import org.apache.axis2.transport.testkit.name.Name;
import org.apache.axis2.transport.testkit.tests.Setup;
import org.apache.axis2.transport.testkit.tests.Transient;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.mail.internet.ContentType;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

@Name("java.net")
public class JavaNetClient implements AsyncTestClient<byte[]> {
    private static final Log log = LogFactory.getLog(JavaNetClient.class);
    
    private @Transient HttpChannel channel;
    
    @Setup @SuppressWarnings("unused")
    private void setUp(HttpChannel channel) {
        this.channel = channel;
    }
    
    public ContentType getContentType(ClientOptions options, ContentType contentType) {
        return contentType;
    }

    public void sendMessage(ClientOptions options, ContentType contentType, byte[] message) throws Exception {
        URL url = new URL(channel.getEndpointReference().getAddress());
        log.debug("Opening connection to " + url + " using " + URLConnection.class.getName());
        try {
            URLConnection connection = url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestProperty("Content-Type", contentType.toString());
            if (contentType.getBaseType().equals("text/xml")) {
                connection.setRequestProperty("SOAPAction", "");
            }
            OutputStream out = connection.getOutputStream();
            out.write(message);
            out.close();
            if (connection instanceof HttpURLConnection) {
                HttpURLConnection httpConnection = (HttpURLConnection)connection;
                log.debug("Response code: " + httpConnection.getResponseCode());
                log.debug("Response message: " + httpConnection.getResponseMessage());
                int i = 0;
                String headerValue;
                while ((headerValue = httpConnection.getHeaderField(i)) != null) {
                    String headerName = httpConnection.getHeaderFieldKey(i);
                    if (headerName != null) {
                        log.debug(headerName + ": " + headerValue);
                    } else {
                        log.debug(headerValue);
                    }
                    i++;
                }
            }
            InputStream in = connection.getInputStream();
            IOUtils.copy(in, System.out);
            in.close();
        } catch (IOException ex) {
            log.debug("Got exception", ex);
            throw ex;
        }
    }
}