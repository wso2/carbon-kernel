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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.axis2.transport.testkit.message.IncomingMessage;
import org.apache.axis2.transport.testkit.message.RESTMessage;
import org.apache.axis2.transport.testkit.message.RESTMessage.Parameter;
import org.mortbay.http.HttpException;
import org.mortbay.http.HttpRequest;

public class JettyRESTAsyncEndpoint extends JettyAsyncEndpoint<RESTMessage> {
    @Override
    protected IncomingMessage<RESTMessage> handle(HttpRequest request)
            throws HttpException, IOException {
        
        List<Parameter> parameters = new LinkedList<Parameter>();
        for (Map.Entry<String,List<String>> entry :
                ((Map<String,List<String>>)request.getParameters()).entrySet()) {
            for (String value : entry.getValue()) {
                parameters.add(new Parameter(entry.getKey(), value));
            }
        }
        return new IncomingMessage<RESTMessage>(null, new RESTMessage(parameters.toArray(
                new Parameter[parameters.size()])));
    }
}
