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

package org.apache.axis2.saaj;

import junit.framework.Assert;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Iterator;

/**
 * 
 */
@RunWith(SAAJTestRunner.class)
public class NodeTest extends Assert {
    private SOAPMessage msg = null;
    private SOAPPart sp = null;
    private SOAPBody body = null;
    private SOAPEnvelope envelope = null;
    private SOAPHeader header = null;
    private SOAPHeaderElement headerEle = null;

    @Before
    public void setUp() throws Exception {
        msg = MessageFactory.newInstance().createMessage();
        sp = msg.getSOAPPart();
        envelope = sp.getEnvelope();
        body = envelope.getBody();
        header = envelope.getHeader();
        headerEle = header.addHeaderElement(envelope.createName("foo", "f", "foo-URI"));
        headerEle.setActor("actor-URI");
    }

    @Validated @Test
    public void testDetachNode() {
        try {
            headerEle.detachNode();
            Iterator iterator = header.examineHeaderElements("actor-URI");
            assertFalse("SOAPHeader element is not detached - unexpected", iterator.hasNext());
        } catch (Exception e) {
            fail("Exception: " + e);
        }
    }

    @Validated @Test
    public void testParentElement() {
        try {
            headerEle.detachNode();
            SOAPElement parentElement = headerEle.getParentElement();
            assertNull("Parent is not null : " + parentElement, parentElement);
        } catch (Exception e) {
            fail("Exception: " + e);
        }
    }

    @Validated @Test
    public void testSetParentElement1() {
        try {
            headerEle.detachNode();
            headerEle.setParentElement(header);
            SOAPElement se = headerEle.getParentElement();
            assertTrue("SOAPHeader element not returned - unexpected",
                       se instanceof SOAPHeader);
            SOAPHeader sh = (SOAPHeader)se;
            assertTrue("SOAPHeader element does not match", sh.equals(header));
        } catch (Exception e) {
            fail("Exception: " + e);
        }
    }

    // TODO: test fails with Sun's SAAJ implementation
    @Test
    public void testSetParentElement2() {
        try {
            try {
                headerEle.setParentElement(body);
                fail("Expected IllegalArgumentException did not occur");
            } catch (IllegalArgumentException e) {
                assertTrue("Expected IllegalArgumentException occurred", true);
            }
        } catch (Exception e) {
            fail("Exception: " + e);
        }
    }

    @Validated @Test
    public void testSetValue() {
        try {
            headerEle.addTextNode("foo-bar");
            String value = headerEle.getValue();
            assertNotNull(value);
            assertEquals("foo-bar", value);

            headerEle.setValue("foo-bar2");
            value = headerEle.getValue();
            assertNotNull(value);
            assertEquals("foo-bar2", value);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception: " + e);
        }
    }
}
