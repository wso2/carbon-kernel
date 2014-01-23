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

package org.apache.axis2.transport.testkit.tests.async;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.axis2.transport.testkit.channel.AsyncChannel;
import org.apache.axis2.transport.testkit.client.AsyncTestClient;
import org.apache.axis2.transport.testkit.endpoint.AsyncEndpoint;
import org.apache.axis2.transport.testkit.message.RESTMessage;
import org.apache.axis2.transport.testkit.message.RESTMessage.Parameter;
import org.apache.axis2.transport.testkit.name.Name;

@Name("REST")
public class RESTTestCase extends AsyncMessageTestCase<RESTMessage> {
    private final RESTMessage message;
    
    public RESTTestCase(AsyncChannel channel, AsyncTestClient<RESTMessage> client, AsyncEndpoint<RESTMessage> endpoint, RESTMessage message, Object... resources) {
        super(channel, client, endpoint, null, null, resources);
        this.message = message;
    }
    
    @Override
    protected RESTMessage prepareMessage() throws Exception {
        return message;
    }

    @Override
    protected void checkMessageData(RESTMessage expected, RESTMessage actual) throws Exception {
        Set<Parameter> expectedParameters = new HashSet<Parameter>(Arrays.asList(expected.getParameters()));
        for (Parameter actualParameter : actual.getParameters()) {
            assertTrue(expectedParameters.remove(actualParameter));
        }
        assertTrue(expectedParameters.isEmpty());
    }
}
