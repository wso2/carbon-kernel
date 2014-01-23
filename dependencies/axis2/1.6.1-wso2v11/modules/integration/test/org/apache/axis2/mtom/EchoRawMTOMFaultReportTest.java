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

package org.apache.axis2.mtom;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axis2.Constants;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.OutInAxisOperation;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.integration.TestingUtils;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilServerBasedTestCase;
import org.apache.axis2.receivers.RawXMLINOutMessageReceiver;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NoHttpResponseException;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import javax.xml.namespace.QName;
import java.io.FileInputStream;
import java.io.IOException;

public class EchoRawMTOMFaultReportTest extends UtilServerBasedTestCase {

    private QName serviceName = new QName("EchoService");

    private QName operationName = new QName("mtomSample");

    public EchoRawMTOMFaultReportTest() {
        super(EchoRawMTOMFaultReportTest.class.getName());
    }

    public EchoRawMTOMFaultReportTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return getTestSetup2(new TestSuite(EchoRawMTOMFaultReportTest.class),
                             TestingUtils.prefixBaseDirectory(Constants.TESTING_PATH + "MTOM-enabledRepository"));
    }


    protected void setUp() throws Exception {
        AxisService service = new AxisService(serviceName.getLocalPart());
        service.setClassLoader(Thread.currentThread().getContextClassLoader());
        service.addParameter(new Parameter(Constants.SERVICE_CLASS,
                                           EchoService.class.getName()));

        AxisOperation axisOp = new OutInAxisOperation(operationName);
        axisOp.setMessageReceiver(new RawXMLINOutMessageReceiver());
        axisOp.setStyle(WSDLConstants.STYLE_DOC);
        service.addOperation(axisOp);
        UtilServer.deployService(service);
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
    }

    public void testEchoFaultSync() throws Exception {
        HttpClient client = new HttpClient();

        PostMethod httppost = new PostMethod("http://127.0.0.1:"
                + (UtilServer.TESTING_PORT)
                + "/axis2/services/EchoService/mtomSample");

        HttpMethodRetryHandler myretryhandler = new HttpMethodRetryHandler() {
            public boolean retryMethod(final HttpMethod method,
                                       final IOException exception,
                                       int executionCount) {
                if (executionCount >= 10) {
                    return false;
                }
                if (exception instanceof NoHttpResponseException) {
                    return true;
                }
                if (!method.isRequestSent()) {
                    return true;
                }
                // otherwise do not retry
                return false;
            }
        };
        httppost.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                                          myretryhandler);
        httppost.setRequestEntity(new InputStreamRequestEntity(
                new FileInputStream(TestingUtils.prefixBaseDirectory("test-resources/mtom/wmtom.bin"))));

        httppost.setRequestHeader("Content-Type",
                                  "multipart/related; boundary=--MIMEBoundary258DE2D105298B756D; type=\"application/xop+xml\"; start=\"<0.15B50EF49317518B01@apache.org>\"; start-info=\"application/soap+xml\"");
        try {
            client.executeMethod(httppost);

            if (httppost.getStatusCode() ==
                    HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                
                // TODO: There is a missing wsa:Action header in the SOAP message.  Fix or look for correct fault text!

//                assertEquals("HTTP/1.1 500 Internal server error",
//                             httppost.getStatusLine().toString());
            }
        } catch (NoHttpResponseException e) {
        } finally {
            httppost.releaseConnection();
        }
    }
}
