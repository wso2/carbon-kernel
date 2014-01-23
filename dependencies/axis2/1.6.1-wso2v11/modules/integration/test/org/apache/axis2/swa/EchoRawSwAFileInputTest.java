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

package org.apache.axis2.swa;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axis2.Constants;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.integration.TestingUtils;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilServerBasedTestCase;
import org.apache.axis2.receivers.RawXMLINOutMessageReceiver;
import org.apache.axis2.wsdl.WSDLConstants;

import javax.xml.namespace.QName;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

public class EchoRawSwAFileInputTest extends UtilServerBasedTestCase {

    private QName serviceName = new QName("EchoSwAService");

    private QName operationName = new QName("echoAttachment");

    public EchoRawSwAFileInputTest() {
        super(EchoRawSwAFileInputTest.class.getName());
    }

    public EchoRawSwAFileInputTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return getTestSetup2(new TestSuite(EchoRawSwAFileInputTest.class),
                             TestingUtils.prefixBaseDirectory(Constants.TESTING_PATH + "MTOM-enabledRepository"));
    }

    protected void setUp() throws Exception {
        AxisService service = new AxisService(serviceName.getLocalPart());
        service.setClassLoader(Thread.currentThread().getContextClassLoader());
        service.addParameter(new Parameter(
                Constants.SERVICE_CLASS, EchoSwA.class
                .getName()));
        AxisOperation axisOp = new InOutAxisOperation(operationName);
        axisOp.setMessageReceiver(new RawXMLINOutMessageReceiver());
        axisOp.setStyle(WSDLConstants.STYLE_DOC);
        service.addOperation(axisOp);
        UtilServer.deployService(service);

    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
    }

    public void testEchoXMLSync() throws Exception {
        Socket socket = new Socket("127.0.0.1", 5555);
        OutputStream outStream = socket.getOutputStream();
        socket.getInputStream();
        InputStream requestMsgInStream = new FileInputStream(TestingUtils.prefixBaseDirectory("test-resources/swa/swainput.bin"));
        int data;
        while ((data = requestMsgInStream.read()) != -1) {
            outStream.write(data);
        }
        outStream.flush();
        socket.shutdownOutput();
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket
                .getInputStream()));
        StringBuffer sb = new StringBuffer();

        String response = reader.readLine();
        while (null != response) {
            try {
                sb.append(response.trim());
                response = reader.readLine();
            } catch (SocketException e) {
                break;
            }
        }

        assertTrue(sb.toString().indexOf(
                "Apache Axis2 - The NExt Generation Web Services Engine") > 0);
        assertTrue(sb.toString().indexOf("multipart/related") > 0);
    }

    private InputStream getResourceAsStream(String path) {
        return this.getClass().getResourceAsStream(path);
    }


}
