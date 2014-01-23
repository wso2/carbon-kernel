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

package org.apache.axis2.async;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.async.AxisCallback;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.Echo;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilServerBasedTestCase;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.Utils;
import org.apache.axis2.util.threadpool.ThreadPool;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AsyncService2Test extends UtilServerBasedTestCase implements TestConstants {

    private static final Log log = LogFactory.getLog(AsyncService2Test.class);
    protected QName transportName = new QName("http://localhost/my",
                                              "NullTransport");
    EndpointReference targetEPR = new EndpointReference(
            "http://127.0.0.1:" + (UtilServer.TESTING_PORT)
//            "http://127.0.0.1:" + 5556
+ "/axis2/services/EchoXMLService/echoOMElement");

    protected AxisConfiguration engineRegistry;
    protected MessageContext mc;
    protected ServiceContext serviceContext;
    protected AxisService service;
    Exception error;
    // A (synchronized) place to hold the responses
    final Map<String, String> responses =
            Collections.synchronizedMap(new HashMap<String, String>());

    protected final String PREFIX_TEXT = "Request number ";

    public static Test suite() {
        return getTestSetup(new TestSuite(AsyncService2Test.class));
    }

    protected void setUp() throws Exception {
        service = Utils.createSimpleService(serviceName,
                                            new AsyncMessageReceiver(),
                                            Echo.class.getName(),
                                            operationName);
        UtilServer.deployService(service);
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.unDeployClientService();
    }

    private static final int MILLISECONDS = 1000;
    private static final Integer TIMEOUT = 200 * MILLISECONDS;
    private static final int MAX_REQUESTS = 9;

    public void testEchoXMLCompleteASyncWithLimitedNumberOfConnections() throws Exception {
        AxisService service =
                Utils.createSimpleServiceforClient(serviceName,
                                                   Echo.class.getName(),
                                                   operationName);

        ConfigurationContext configcontext = UtilServer.createClientConfigurationContext();

        // Use max of 3 threads for the async thread pool
        configcontext.setThreadPool(new ThreadPool(1, 3));

        OMFactory fac = OMAbstractFactory.getOMFactory();
        ServiceClient sender = null;
        try {
            Options options = new Options();
            options.setTo(targetEPR);
            options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
            options.setUseSeparateListener(true);
            options.setAction(operationName.getLocalPart());

            options.setTimeOutInMilliSeconds(200 * MILLISECONDS);
            options.setProperty(HTTPConstants.CHUNKED, Boolean.TRUE);
            options.setProperty(HTTPConstants.SO_TIMEOUT, TIMEOUT);
            options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, TIMEOUT);
            options.setProperty(HTTPConstants.REUSE_HTTP_CLIENT,
                                Boolean.TRUE);
            options.setProperty(HTTPConstants.AUTO_RELEASE_CONNECTION,
                                Boolean.TRUE);
//            options.setProperty(ServiceClient.AUTO_OPERATION_CLEANUP, true);

            AxisCallback callback = new AxisCallback() {

                public void onMessage(MessageContext msgContext) {
                    final OMElement responseElement =
                            msgContext.getEnvelope().getBody().getFirstElement();
                    assertNotNull(responseElement);
                    String textValue = responseElement.getFirstElement().getText();
                    assertTrue(textValue.startsWith(PREFIX_TEXT));
                    String whichOne = textValue.substring(PREFIX_TEXT.length());
                    assertNull(responses.get(whichOne));
                    responses.put(whichOne, textValue);
                    synchronized (responses) {
                        if (responses.size() == MAX_REQUESTS) {
                            // All done!
                            responses.notifyAll();
                        }
                    }
                }

                public void onFault(MessageContext msgContext) {
                    // Whoops.
                    synchronized (responses) {
                        if (error != null) return; // Only take first error
                        error = msgContext.getEnvelope().getBody().getFault().getException();
                        responses.notify();
                    }
                }

                public void onComplete() {
                }

                public void onError(Exception e) {
                    log.info(e.getMessage());
                    synchronized (responses) {
                        if (error != null) return; // Only take first error
                        error = e;
                        responses.notify();
                    }
                }
            };

            sender = new ServiceClient(configcontext, service);
            sender.setOptions(options);
            for (int i = 0; i < MAX_REQUESTS; i++) {
                OMNamespace omNs = fac.createOMNamespace("http://localhost/my", "my");
                OMElement method = fac.createOMElement("echoOMElement", omNs);
                OMElement value = fac.createOMElement("myValue", omNs);
                value.setText(PREFIX_TEXT + i);
                method.addChild(value);
                sender.sendReceiveNonBlocking(operationName, method, callback);
                log.trace("sent the request # : " + i);
            }
            log.trace("waiting (max 1min)");
            synchronized (responses) {
                responses.wait(60 * 1000);
                // Someone kicked us, so either we have a problem
                if (responses.size() < MAX_REQUESTS) {
                    if (error != null)
                        throw error;
                    throw new AxisFault("Timeout, did not receive all responses.");
                }
            }
        } finally {
            if (sender != null)
                sender.cleanup();
        }

    }
}
