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

package org.apache.axis2.transport.testkit.tests;

import javax.mail.internet.ContentType;

import org.apache.axis2.transport.testkit.Adapter;
import org.apache.axis2.transport.testkit.MessageExchangeValidator;
import org.apache.axis2.transport.testkit.client.ClientOptions;
import org.apache.axis2.transport.testkit.client.TestClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class MessageTestCase extends ManagedTestCase {
    private static final Log log = LogFactory.getLog(MessageTestCase.class);
    
    protected final ContentType contentType;
    protected final ClientOptions options;
    private @Transient MessageExchangeValidator[] validators;

    public MessageTestCase(TestClient client, ContentType contentType, String charset, Object... resources) {
        super(resources);
        if (client instanceof Adapter) {
            addResource(((Adapter)client).getTarget());
        } else {
            addResource(client);
        }
        this.contentType = contentType;
        try {
            options = new ClientOptions(client, contentType, charset);
        } catch (Exception ex) {
            // TODO: handle this in a better way
            throw new Error(ex);
        }
        addResource(options);
        addResource(this);
    }
    
    @Setup @SuppressWarnings("unused")
    private void setUp(MessageExchangeValidator[] validators) {
        this.validators = validators;
    }
    
    @Override
    protected void runTest() throws Throwable {
        for (MessageExchangeValidator validator : validators) {
            validator.beforeSend();
        }
        doRunTest();
        for (MessageExchangeValidator validator : validators) {
            log.debug("Invoking message exchange validator " + validator.getClass().getName());
            validator.afterReceive();
        }
    }

    protected abstract void doRunTest() throws Throwable;
}
