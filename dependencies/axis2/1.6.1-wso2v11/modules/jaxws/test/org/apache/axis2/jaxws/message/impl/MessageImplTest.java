package org.apache.axis2.jaxws.message.impl;

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

import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.Protocol;

import javax.xml.stream.XMLStreamException;
import javax.xml.ws.WebServiceException;

import java.util.HashMap;

import junit.framework.TestCase;

/**
 * Test some low-level specifics of the MessageImpl class.  Note that this is testing specific 
 * low level methods and IS NOT representative of the way MessageImpl should be used.  
 * The MessageFactory should be used to create MessageImpl instances. 
 */
public class MessageImplTest extends TestCase {

    /**
     * Verify that if the TransportHeaders Map contains keys with null values that it doesn't
     * cause any problems in the getAsSOAPMessage() method.  
     */
    public void testGetAsSOAPMessageTransportHeadersWithNullValues() {
        try {
            Message msg = new MessageImpl(Protocol.soap11);
            HashMap map = new HashMap();
            map.put("key1", null);
            map.put("key2", null);
            msg.setMimeHeaders(map);
            msg.getAsSOAPMessage();
        } catch (WebServiceException e) {
            e.printStackTrace();
            fail("Caught unexpected exception " + e.toString());
        } catch (XMLStreamException e) {
            e.printStackTrace();
            fail("Caught unexpected exception " + e.toString());
        }
    }

}
