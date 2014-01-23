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

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.util.InvokerThread;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilServerBasedTestCase;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class ThreadingTest extends UtilServerBasedTestCase implements TestConstants {


    private static final Log log = LogFactory.getLog(ThreadingTest.class);

    protected QName transportName = new QName("http://localhost/my",
                                              "NullTransport");

    protected AxisConfiguration engineRegistry;
    protected MessageContext mc;
    protected ServiceContext serviceContext;
    protected AxisService service;

    protected boolean finish = false;

    public static Test suite() {
        return getTestSetup(new TestSuite(ThreadingTest.class));
    }

    protected void setUp() throws Exception {
        service =
                Utils.createSimpleService(serviceName,
                                          Echo.class.getName(),
                                          operationName);
        UtilServer.deployService(service);
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.unDeployClientService();
    }

    public void testEchoXMLSync() throws Exception {
        int numberOfThreads = 5;
        InvokerThread[] invokerThreads = new InvokerThread[numberOfThreads];

        for (int i = 0; i < numberOfThreads; i++) {
            InvokerThread invokerThread = new InvokerThread(i + 1);
            invokerThreads[i] = invokerThread;
            invokerThread.start();
        }

        boolean threadsAreRunning;
        Calendar cal = new GregorianCalendar();
        int min = cal.get(Calendar.MINUTE);

        do {
            threadsAreRunning = false;
            for (int i = 0; i < numberOfThreads; i++) {
                if (invokerThreads[i].isAlive()) {
                    threadsAreRunning = true;
                    break;
                }
                Exception exception = invokerThreads[i].getThrownException();
                if (exception != null) {
                    throw new Exception("Exception thrown in thread " + i + " ....", exception);
                }
            }

            // waiting 3 seconds, if not finish, time out.
            if (Math.abs(min - new GregorianCalendar().get(Calendar.MINUTE)) > 1) {
                log.info("I'm timing out. Can't wait more than this to finish.");
                fail("Timing out");
            }

            Thread.sleep(100);
        } while (threadsAreRunning);

        assertTrue(true);
    }
}
