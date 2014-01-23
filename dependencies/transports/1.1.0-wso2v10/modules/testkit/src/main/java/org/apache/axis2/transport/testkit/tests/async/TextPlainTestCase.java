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

import javax.mail.internet.ContentType;

import org.apache.axis2.transport.testkit.MessageTestData;
import org.apache.axis2.transport.testkit.channel.AsyncChannel;
import org.apache.axis2.transport.testkit.client.AsyncTestClient;
import org.apache.axis2.transport.testkit.endpoint.AsyncEndpoint;
import org.apache.axis2.transport.testkit.name.Name;
import org.apache.axis2.transport.testkit.name.Named;

@Name("AsyncTextPlain")
public class TextPlainTestCase extends AsyncMessageTestCase<String> {
    private final MessageTestData data;
    
    public TextPlainTestCase(AsyncChannel channel, AsyncTestClient<String> client, AsyncEndpoint<String> endpoint, MessageTestData data, Object... resources) {
        super(channel, client, endpoint, new ContentType("text", "plain", null), data.getCharset(), resources);
        this.data = data;
    }
    
    @Named
    public MessageTestData getData() {
        return data;
    }
    
    @Override
    protected String prepareMessage() throws Exception {
        return data.getText();
    }

    @Override
    protected void checkMessageData(String expected, String actual) throws Exception {
        // Some transport protocols add a newline at the end of the payload. Therefore trim the
        // strings before comparison.
        // TODO: investigate this a bit further
        assertEquals(expected.trim(), actual.trim());
    }
}
