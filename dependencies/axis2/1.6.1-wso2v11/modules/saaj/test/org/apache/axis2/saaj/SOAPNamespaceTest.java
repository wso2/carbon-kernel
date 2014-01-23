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
import javax.xml.soap.SOAPMessage;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/** Ref: JIRA- Axis2-517 */
@RunWith(SAAJTestRunner.class)
public class SOAPNamespaceTest extends Assert {
    @Validated @Test
    public void test() {
        try {
            String xml =
                    "<?xml version='1.0' ?><env:Envelope xmlns:env=\"http://schemas.xmlsoap.org/soap/envelope/\"><env:Body/></env:Envelope>";

            SOAPMessage msg = MessageFactory.newInstance()
                    .createMessage(null, new ByteArrayInputStream(xml.getBytes()));
            msg.setProperty(SOAPMessage.WRITE_XML_DECLARATION, "true");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            msg.writeTo(baos);
            String producedMag = new String(baos.toByteArray());
            String [] splitParts = producedMag.split("http://schemas.xmlsoap.org/soap/envelope");
            assertEquals("Extra namespace declaration", 2, splitParts.length);
        }
        catch (Exception e) {
            fail(e.getMessage());
        }
    }

}
