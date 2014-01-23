/**
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
package interop;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.ws.axis.oasis.ping.PingPort;
import org.apache.ws.axis.oasis.ping.PingServiceLocator;
import org.apache.ws.security.handler.WSS4JHandler;

import javax.xml.namespace.QName;
import javax.xml.rpc.handler.HandlerInfo;
import javax.xml.rpc.handler.HandlerRegistry;
import javax.xml.rpc.holders.StringHolder;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestJAXRPCHandler extends TestCase {
    /**
     * @param name name of the test
     */
    public TestJAXRPCHandler(String name) {
        super(name);
    }

    /**
     * @return a junit test suite
     */
    public static Test suite() {
        return new TestSuite(TestJAXRPCHandler.class);
    }

    public void testScenario1() throws Exception {
        Map config = new HashMap();
        config.put("deployment", "client");
        config.put("flow", "request-only");
        config.put("action", "UsernameToken");
        config.put("user", "Chris");
        config.put("passwordType", "PasswordText");
        config.put("passwordCallbackClass", "org.apache.ws.axis.oasis.PWCallback1");
        invokeService (config, 1);
    }
    public void testScenario2() throws Exception {
        Map config = new HashMap();
        config.put("deployment", "client");
        config.put("flow", "request-only");
        config.put("user", "Chris");
        config.put("passwordType", "PasswordText");
        config.put("action", "UsernameToken Encrypt");
        config.put("addUTElements", "Nonce Created");
        config.put("encryptionPropFile", "wsstest.properties");
        config.put("encryptionKeyIdentifier", "SKIKeyIdentifier");
        config.put("encryptionUser", "bob");
        config.put("passwordCallbackClass", "org.apache.ws.axis.oasis.PWCallback1");
        config.put("encryptionParts", "{Element}{http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd}UsernameToken");  
        invokeService (config, 2);
    }
    public void testScenario3() throws Exception {
        Map config = new HashMap();
        config.put("deployment", "client");
        config.put("action", "Signature Encrypt Timestamp");
        config.put("user", "alice");
        config.put("passwordCallbackClass", "org.apache.ws.axis.oasis.PWCallback1");
        config.put("signatureKeyIdentifier", "DirectReference");
        config.put("signaturePropFile", "wsstest.properties");
        config.put("encryptionKeyIdentifier", "SKIKeyIdentifier");
        config.put("encryptionUser", "bob");
        invokeService (config, 3);
    }
    public void testScenario4() throws Exception {
        Map config = new HashMap();
        config.put("deployment", "client");
        config.put("action", "Signature Encrypt Timestamp");
        config.put("user", "alice");
        config.put("passwordCallbackClass", "org.apache.ws.axis.oasis.PWCallback1");
        config.put("signatureKeyIdentifier", "DirectReference");
        config.put("signaturePropFile", "wsstest.properties");
        config.put("encryptionKeyIdentifier", "EmbeddedKeyName");
        config.put("encryptionSymAlgorithm", "http://www.w3.org/2001/04/xmlenc#tripledes-cbc");
        config.put("EmbeddedKeyCallbackClass", "org.apache.ws.axis.oasis.PWCallback1");
        config.put("EmbeddedKeyName", "SessionKey");
        invokeService (config, 4);
    }
    
    // testScenario5 - Ping5 fails because there is now way in JAXRPC to 
    // specifiy the parameter signatureKeyIdentifier with different values 
    // for request and response flows
/*    public void testScenario5() throws Exception {
        Map config = new HashMap();
        config.put("deployment", "client");
        config.put("action", "Signature NoSerialization");
        config.put("user", "alice");
        config.put("passwordCallbackClass", "org.apache.ws.axis.oasis.PWCallback1");
        config.put("signatureKeyIdentifier", "DirectReference");
        config.put("signaturePropFile", "wsstest.properties");
        config.put("signatureParts", "{}{http://xmlsoap.org/Ping}ticket");
        invokeService (config, 5);
    }
*/    public void testScenario6() throws Exception {
        Map config = new HashMap();
        config.put("deployment", "client");
        config.put("action", "Encrypt Signature Timestamp");
        config.put("user", "alice");
        config.put("passwordCallbackClass", "org.apache.ws.axis.oasis.PWCallback1");
        config.put("signatureKeyIdentifier", "DirectReference");
        config.put("signaturePropFile", "wsstest.properties");
        config.put("encryptionKeyIdentifier", "SKIKeyIdentifier");
        config.put("encryptionUser", "bob");
        invokeService (config, 6);
    }
    public void testScenario7() throws Exception {
        Map config = new HashMap();
        config.put("deployment", "client");
        config.put("action", "Signature Encrypt Timestamp");
        config.put("user", "alice");
        config.put("passwordCallbackClass", "org.apache.ws.axis.oasis.PWCallback1");
        config.put("signatureKeyIdentifier", "DirectReference");
        config.put("signaturePropFile", "wsstest.properties");
        config.put("encryptionKeyIdentifier", "SKIKeyIdentifier");
        config.put("encryptionUser", "bob");
        config.put("encryptionPropFile", "wsstest.properties");
        config.put("signatureParts", "{}{http://schemas.xmlsoap.org/soap/envelope/}Body;STRTransform");
        invokeService (config, 7);
    }
    
    public void invokeService (Map config, int interopNum) throws Exception {
        PingServiceLocator service = new PingServiceLocator();

        List handlerChain = new ArrayList();
        handlerChain.add(new HandlerInfo( WSS4JHandler.class, config, null));

        HandlerRegistry registry = service.getHandlerRegistry();
        registry.setHandlerChain(new QName("Ping" + interopNum), handlerChain);

        service.getHandlerRegistry().getHandlerChain(new QName("http://xmlsoap.org/Ping", "ticketType"));
        
        java.lang.reflect.Method method = service.getClass().getMethod("getPing" + interopNum, new Class[] {URL.class});

        PingPort port = (PingPort) method.invoke (service, new Object[] {new URL("http://localhost:8080/axis/services/Ping" + interopNum)});
        StringHolder text =
                new StringHolder("WSS4J - Scenario" + interopNum + " @ [" + new java.util.Date(System.currentTimeMillis()) + "]");
        port.ping(new org.apache.ws.axis.oasis.ping.TicketType("WSS4J" + interopNum), text);
        System.out.println(text.value);
    }
}
