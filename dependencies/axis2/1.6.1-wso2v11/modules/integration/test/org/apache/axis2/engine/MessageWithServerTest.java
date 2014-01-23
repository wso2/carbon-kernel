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

package org.apache.axis2.engine;

//todo

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.OutInAxisOperation;
import org.apache.axis2.dispatchers.AddressingBasedDispatcher;
import org.apache.axis2.dispatchers.RequestURIBasedDispatcher;
import org.apache.axis2.dispatchers.SOAPActionBasedDispatcher;
import org.apache.axis2.dispatchers.SOAPMessageBodyBasedDispatcher;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilServerBasedTestCase;
import org.apache.axis2.receivers.RawXMLINOnlyMessageReceiver;
import org.apache.axis2.receivers.RawXMLINOutMessageReceiver;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.Socket;

public class MessageWithServerTest extends UtilServerBasedTestCase {
    private static final Log log = LogFactory.getLog(MessageWithServerTest.class);
    private QName serviceName = new QName("", "EchoService");
    private QName operationName =
            new QName("http://ws.apache.org/axis2", "echoVoid");

    private ClassLoader cl;

    public MessageWithServerTest(String testName) {
        super(testName);
        cl = Thread.currentThread().getContextClassLoader();
    }

    public static Test suite() {
        return getTestSetup(new TestSuite(MessageWithServerTest.class));
    }


    protected void setUp() throws Exception {
        AxisConfiguration config = new AxisConfiguration();

        AxisService service = Utils.createSimpleService(serviceName,
                                                        Echo.class.getName(),
                                                        operationName);
        config.addService(service);

        //service.setFaultInFlow(new MockFlow("service faultflow", 1));

        AxisModule m1 = new AxisModule("A Module 1");
        m1.setInFlow(new MockFlow("service module inflow", 4));

        //m1.setFaultInFlow(new MockFlow("service module faultflow", 1));
        config.addMessageReceiver(
                "http://www.w3.org/2004/08/wsdl/in-only", new RawXMLINOnlyMessageReceiver());
        config.addMessageReceiver(
                "http://www.w3.org/2004/08/wsdl/in-out", new RawXMLINOutMessageReceiver());

        DispatchPhase dispatchPhase = new DispatchPhase();

        dispatchPhase.setName("Dispatch");

        AddressingBasedDispatcher abd = new AddressingBasedDispatcher();

        abd.initDispatcher();

        RequestURIBasedDispatcher rud = new RequestURIBasedDispatcher();

        rud.initDispatcher();

        SOAPActionBasedDispatcher sabd = new SOAPActionBasedDispatcher();

        sabd.initDispatcher();

        SOAPMessageBodyBasedDispatcher smbd = new SOAPMessageBodyBasedDispatcher();

        smbd.initDispatcher();

        InstanceDispatcher id = new InstanceDispatcher();

        id.init(new HandlerDescription("InstanceDispatcher"));
        dispatchPhase.addHandler(abd);
        dispatchPhase.addHandler(rud);
        dispatchPhase.addHandler(sabd);
        dispatchPhase.addHandler(smbd);
        dispatchPhase.addHandler(id);
        config.getInFlowPhases().add(dispatchPhase);
        service.engageModule(m1);

        AxisOperation axisOperation = new OutInAxisOperation();
        axisOperation.setName(operationName);
        service.addOperation(axisOperation);

        UtilServer.deployService(service);
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
    }

    public void testEchoStringServer() throws Exception {
        InputStream in = cl.getResourceAsStream("soap/soapmessage.txt");

        if (in == null) {
            in = new FileInputStream(System.getProperty("basedir", ".") +
                    "/test-resources/soap/soapmessage.txt");
        }

        Socket socket = new Socket("127.0.0.1", UtilServer.TESTING_PORT);
        OutputStream out = socket.getOutputStream();
        byte[] buf = new byte[1024];
        int index;
        while ((index = in.read(buf)) > 0) {
            out.write(buf, 0, index);
        }

        InputStream respose = socket.getInputStream();
        Reader rReader = new InputStreamReader(respose);
        char[] charBuf = new char[1024];
        while ((rReader.read(charBuf)) > 0) {
            log.info(new String(charBuf));
        }

        in.close();
        out.close();

        rReader.close();
        socket.close();
    }
}
