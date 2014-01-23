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

import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.transport.testkit.channel.AsyncChannel;
import org.apache.axis2.transport.testkit.client.AsyncTestClient;
import org.apache.axis2.transport.testkit.endpoint.AsyncEndpoint;
import org.apache.axis2.transport.testkit.message.XMLMessage;
import org.apache.axis2.transport.testkit.name.Name;

@Name("AsyncSOAPLarge")
// TODO: maybe we should use XMLUnit to construct these kind of tests
public class LargeSOAPAsyncMessageTestCase extends AsyncMessageTestCase<XMLMessage> {
    public LargeSOAPAsyncMessageTestCase(AsyncChannel channel, AsyncTestClient<XMLMessage> client, AsyncEndpoint<XMLMessage> endpoint, Object... resources) {
        super(channel, client, endpoint, XMLMessage.Type.SOAP11.getContentType(), "UTF-8", resources);
    }
    
    @Override
    protected XMLMessage prepareMessage() throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement root = factory.createOMElement(new QName("root"));
        for (int i=0; i<1000; i++) {
            OMElement child = factory.createOMElement(new QName("child"));
            child.setText("text");
            root.addChild(child);
        }
        return new XMLMessage(root, XMLMessage.Type.SOAP11);
    }

    @Override
    protected void checkMessageData(XMLMessage expected, XMLMessage actual) throws Exception {
        OMElement element = actual.getPayload();
        OMElement orgElement = expected.getPayload();
        assertEquals(orgElement.getQName(), element.getQName());
        assertEquals(1000, countChildren(element));
    }
    
    private static int countChildren(OMElement element) {
        int count = 0;
        for (Iterator<?> it = element.getChildElements(); it.hasNext(); count++) {
            it.next();
        }
        return count;
    }
}
