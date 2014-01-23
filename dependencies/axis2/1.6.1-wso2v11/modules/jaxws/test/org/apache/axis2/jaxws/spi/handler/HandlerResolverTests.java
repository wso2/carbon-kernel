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

package org.apache.axis2.jaxws.spi.handler;

import junit.framework.TestCase;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;
import javax.xml.ws.soap.SOAPBinding;
import java.io.File;
import java.util.List;

public class HandlerResolverTests extends TestCase {
    private String testResourceDir = System.getProperty("basedir", ".") + "/" + "test-resources";

    public void testHandlerResolver() {
        String path = "/configuration/handlers/handler.xml";
        File file = new File(testResourceDir, path);
        HandlerResolver resolver = new HandlerResolverImpl(file);
        PortInfo pi = new DummyPortInfo();
        List<Handler> list = resolver.getHandlerChain(pi);
        assertEquals(2, list.size());
    }
    
    public class DummyPortInfo implements PortInfo {

        public String getBindingID() {
            return SOAPBinding.SOAP11HTTP_BINDING;
        }

        public QName getPortName() {
            return new QName("http://www.apache.org/test/namespace", "DummyPort");
        }

        public QName getServiceName() {
            return new QName("http://www.apache.org/test/namespace", "DummyService");
        }
    }
}
