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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpException;
import org.mortbay.http.HttpHandler;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.SocketListener;
import org.mortbay.http.handler.AbstractHttpHandler;
import org.mortbay.jetty.Server;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.io.IOException;
import java.net.URL;

/**
 * 
 */
@RunWith(SAAJTestRunner.class)
public class SOAPConnectionTest extends Assert {
    @Validated @Test
    public void testClose() {
        try {
            SOAPConnection sCon = SOAPConnectionFactory.newInstance().createConnection();
            sCon.close();
        } catch (SOAPException e) {
            fail("Unexpected Exception " + e);
        }
    }

    @Validated @Test
    public void testCloseTwice() {
        SOAPConnectionFactory soapConnectionFactory = null;
        try {
            soapConnectionFactory = SOAPConnectionFactory.newInstance();
        } catch (SOAPException e) {
            fail("Unexpected Exception " + e);
        }

        SOAPConnection sCon = null;
        try {
            sCon = soapConnectionFactory.createConnection();
            sCon.close();
        } catch (SOAPException e) {
            fail("Unexpected Exception " + e);
        }

        try {
            sCon.close();
            fail("Expected Exception did not occur");
        } catch (SOAPException e) {
            assertTrue(true);
        }
    }

    @Validated @Test
    public void testCallOnCloseConnection() {
        SOAPConnectionFactory soapConnectionFactory = null;
        try {
            soapConnectionFactory = SOAPConnectionFactory.newInstance();
        } catch (SOAPException e) {
            fail("Unexpected Exception " + e);
        }

        SOAPConnection sCon = null;
        try {
            sCon = soapConnectionFactory.createConnection();
            sCon.close();
        } catch (SOAPException e) {
            fail("Unexpected Exception " + e);
        }

        try {
            sCon.call(null, new Object());
            fail("Expected Exception did not occur");
        } catch (SOAPException e) {
            assertTrue(true);
        }
    }


    @Validated @Test
    public void testGet() throws Exception {
        Server server = new Server();
        SocketListener listener = new SocketListener();
        server.addListener(listener);
        HttpContext context = new HttpContext(server, "/*");
        HttpHandler handler = new AbstractHttpHandler() {
            public void handle(String pathInContext, String pathParams,
                    HttpRequest request, HttpResponse response) throws HttpException, IOException {

                try {
                    SOAPMessage message = MessageFactory.newInstance().createMessage();
                    SOAPBody body = message.getSOAPBody();
                    body.addChildElement("root");
                    response.setContentType(SOAPConstants.SOAP_1_1_CONTENT_TYPE);
                    message.writeTo(response.getOutputStream());
                    request.setHandled(true);
                } catch (SOAPException ex) {
                    throw new RuntimeException("Failed to generate SOAP message", ex);
                }
            }
        };
        context.addHandler(handler);
        server.start();
        try {
            SOAPConnectionFactory sf = new SOAPConnectionFactoryImpl();
            SOAPConnection con = sf.createConnection();
            URL urlEndpoint = new URL("http", "localhost", listener.getPort(), "/test");
            SOAPMessage reply = con.get(urlEndpoint);
            SOAPElement bodyElement = (SOAPElement)reply.getSOAPBody().getChildElements().next();
            assertEquals("root", bodyElement.getLocalName());
        } finally {
            server.stop();
        }
    }
}
