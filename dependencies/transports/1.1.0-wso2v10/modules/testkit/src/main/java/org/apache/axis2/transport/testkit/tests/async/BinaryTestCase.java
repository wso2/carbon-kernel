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
import java.util.Random;

import javax.mail.internet.ContentType;

import org.apache.axis2.transport.testkit.channel.AsyncChannel;
import org.apache.axis2.transport.testkit.client.AsyncTestClient;
import org.apache.axis2.transport.testkit.endpoint.AsyncEndpoint;
import org.apache.axis2.transport.testkit.name.Name;

@Name("AsyncBinary")
public class BinaryTestCase extends AsyncMessageTestCase<byte[]> {
    private static final Random random = new Random();
    
    public BinaryTestCase(AsyncChannel channel, AsyncTestClient<byte[]> client, AsyncEndpoint<byte[]> endpoint, Object... resources) {
        super(channel, client, endpoint, new ContentType("application", "octet-stream", null), null, resources);
    }
    
    @Override
    protected byte[] prepareMessage() throws Exception {
        byte[] content = new byte[8192];
        random.nextBytes(content);
        return content;
    }

    @Override
    protected void checkMessageData(byte[] expected, byte[] actual) throws Exception {
        assertTrue(Arrays.equals(expected, actual));
    }
}
