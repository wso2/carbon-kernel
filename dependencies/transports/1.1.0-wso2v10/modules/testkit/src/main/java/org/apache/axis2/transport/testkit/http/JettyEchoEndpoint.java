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
import java.util.Map;

import junit.framework.Assert;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.testkit.axis2.MessageContextValidator;
import org.apache.axis2.transport.testkit.endpoint.EndpointErrorListener;
import org.apache.axis2.transport.testkit.endpoint.InOutEndpoint;
import org.apache.commons.io.IOUtils;
import org.mortbay.http.HttpException;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;

public class JettyEchoEndpoint extends JettyEndpoint implements InOutEndpoint, MessageContextValidator {
    @Override
    protected void handle(String pathParams, HttpRequest request,
            HttpResponse response) throws HttpException, IOException {
        
        response.setContentType(request.getContentType());
        response.addField("X-Test-Header", "test value");
        IOUtils.copy(request.getInputStream(), response.getOutputStream());
    }

    public void validate(MessageContext msgContext, boolean isResponse) throws Exception {
        Map<?,?> trpHeaders = (Map<?,?>)msgContext.getProperty(MessageContext.TRANSPORT_HEADERS);
        Assert.assertNotNull(trpHeaders);
        Assert.assertEquals("test value", trpHeaders.get("X-Test-Header"));
    }

    public void addEndpointErrorListener(EndpointErrorListener listener) {
        // Ignore this as endpoint errors are not detected yet
    }

    public void removeEndpointErrorListener(EndpointErrorListener listener) {
        // Ignore this as endpoint errors are not detected yet
    }
}
