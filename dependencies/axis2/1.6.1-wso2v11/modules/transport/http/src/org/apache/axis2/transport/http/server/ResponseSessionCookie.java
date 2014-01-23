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

import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.message.BufferedHeader;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.CharArrayBuffer;

import java.io.IOException;

public class ResponseSessionCookie implements HttpResponseInterceptor {

    public void process(final HttpResponse response, final HttpContext context)
            throws HttpException, IOException {
        if (response == null) {
            throw new IllegalArgumentException("HTTP response may not be null");
        }
        if (context == null) {
            throw new IllegalArgumentException("HTTP context may not be null");
        }

        String sessionCookie = null;
        MessageContext msgctx = (MessageContext) context.getAttribute(AxisParams.MESSAGE_CONTEXT);
        if (msgctx != null) {
            sessionCookie = (String) msgctx.getProperty(HTTPConstants.COOKIE_STRING);
        }
        if (sessionCookie == null) {
            sessionCookie = (String) context.getAttribute(HTTPConstants.COOKIE_STRING);
        }
        if (sessionCookie != null) {
            // Generate Netscape style cookie header
            CharArrayBuffer buffer1 = new CharArrayBuffer(sessionCookie.length() + 40);
            buffer1.append(HTTPConstants.HEADER_SET_COOKIE);
            buffer1.append(": ");
            buffer1.append(Constants.SESSION_COOKIE_JSESSIONID);
            buffer1.append("=");
            buffer1.append(sessionCookie);
            response.addHeader(new BufferedHeader(buffer1));

            // Generate RFC2965 cookie2 header
            CharArrayBuffer buffer2 = new CharArrayBuffer(sessionCookie.length() + 50);
            buffer2.append(HTTPConstants.HEADER_SET_COOKIE2);
            buffer2.append(": ");
            buffer2.append(Constants.SESSION_COOKIE_JSESSIONID);
            buffer2.append("=");
            buffer2.append(sessionCookie);
            buffer2.append("; ");
            int port = response.getParams().getIntParameter(AxisParams.LISTENER_PORT, 0);
            if (port > 0) {
                buffer2.append("Port=\"");
                buffer2.append(Integer.toString(port));
                buffer2.append("\"; ");
            }
            buffer2.append("Version=1");
            response.addHeader(new BufferedHeader(buffer2));
        }
    }

}
