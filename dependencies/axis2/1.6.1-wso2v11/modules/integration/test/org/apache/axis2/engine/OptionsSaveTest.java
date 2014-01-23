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

import junit.framework.TestCase;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.transport.http.CommonsHTTPTransportSender;
import org.apache.axis2.transport.http.SimpleHTTPServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class OptionsSaveTest extends TestCase {
    protected static final Log log = LogFactory.getLog(OptionsSaveTest.class);

    private transient QName serviceName = new QName("NullService");
    private transient QName operationName = new QName("DummyOp");


    private String testArg = null;


    public OptionsSaveTest(String arg0) {
        super(arg0);
        testArg = new String(arg0);

        initAll();
    }


    protected void initAll() {
    }


    protected void setUp() throws Exception {
        //org.apache.log4j.BasicConfigurator.configure();
    }

    public void testSaveAndRestore() throws Exception {
        File theFile = null;
        String theFilename = null;
        boolean saved = false;
        boolean restored = false;
        boolean done = false;
        boolean comparesOk = false;

        AxisConfiguration axisConfiguration = new AxisConfiguration();
        ConfigurationContext configurationContext = new ConfigurationContext(axisConfiguration);


        log.debug("OptionsSaveTest:testSaveAndRestore():  BEGIN ---------------");

        // ---------------------------------------------------------
        // setup an options object to use
        // ---------------------------------------------------------
        Options options = new Options();

        options.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        options.setExceptionToBeThrownOnSOAPFault(true);
        options.setTimeOutInMilliSeconds(5000L);
        options.setUseSeparateListener(false);
        options.setAction("SoapAction");
        options.setFaultTo(new EndpointReference("http://ws.apache.org/axis2/faultTo"));
        options.setFrom(new EndpointReference("http://ws.apache.org/axis2/from"));
        options.setTo(new EndpointReference("http://ws.apache.org/axis2/to"));
        options.setReplyTo(new EndpointReference(AddressingConstants.Final.WSA_ANONYMOUS_URL));

        TransportOutDescription transportOut = new TransportOutDescription("null");
        TransportOutDescription transportOut2 = new TransportOutDescription("happy");
        TransportOutDescription transportOut3 = new TransportOutDescription("golucky");
        transportOut.setSender(new CommonsHTTPTransportSender());
        transportOut2.setSender(new CommonsHTTPTransportSender());
        transportOut3.setSender(new CommonsHTTPTransportSender());
        options.setTransportOut(transportOut);
        axisConfiguration.addTransportOut(transportOut3);
        axisConfiguration.addTransportOut(transportOut2);
        axisConfiguration.addTransportOut(transportOut);

        TransportInDescription transportIn = new TransportInDescription("null");
        TransportInDescription transportIn2 = new TransportInDescription("always");
        TransportInDescription transportIn3 = new TransportInDescription("thebest");
        transportIn.setReceiver(new SimpleHTTPServer());
        transportIn2.setReceiver(new SimpleHTTPServer());
        transportIn3.setReceiver(new SimpleHTTPServer());
        options.setTransportIn(transportIn);
        axisConfiguration.addTransportIn(transportIn2);
        axisConfiguration.addTransportIn(transportIn);
        axisConfiguration.addTransportIn(transportIn3);

        options.setMessageId("msgId012345");

        options.setProperty("key01", "value01");
        options.setProperty("key02", "value02");
        options.setProperty("key03", "value03");
        options.setProperty("key04", "value04");
        options.setProperty("key05", "value05");
        options.setProperty("key06", "value06");
        options.setProperty("key07", "value07");
        options.setProperty("key08", "value08");
        options.setProperty("key09", "value09");
        options.setProperty("key10", "value10");

        // TODO: setup a parent

        // ---------------------------------------------------------
        // setup a temporary file to use
        // ---------------------------------------------------------
        try {
            theFile = File.createTempFile("optionsSave", null);
            theFilename = theFile.getName();
            log.debug("OptionsSaveTest:testSaveAndRestore(): temp file = [" + theFilename + "]");
        }
        catch (Exception ex) {
            log.debug("OptionsSaveTest:testSaveAndRestore(): error creating temp file = [" +
                    ex.getMessage() + "]");
            theFile = null;
        }

        if (theFile != null) {
            // ---------------------------------------------------------
            // save to the temporary file
            // ---------------------------------------------------------
            try {
                // setup an output stream to a physical file
                FileOutputStream outStream = new FileOutputStream(theFile);

                // attach a stream capable of writing objects to the 
                // stream connected to the file
                ObjectOutputStream outObjStream = new ObjectOutputStream(outStream);

                // try to save the message context
                log.debug("OptionsSaveTest:testSaveAndRestore(): saving .....");
                saved = false;
                outObjStream.writeObject(options);

                // close out the streams
                outObjStream.flush();
                outObjStream.close();
                outStream.flush();
                outStream.close();

                saved = true;
                log.debug(
                        "OptionsSaveTest:testSaveAndRestore(): ....save operation completed.....");

                long filesize = theFile.length();
                log.debug("OptionsSaveTest:testSaveAndRestore(): file size after save [" +
                        filesize + "]   temp file = [" + theFilename + "]");
            }
            catch (Exception ex2) {
                if (saved != true) {
                    log.debug("OptionsSaveTest:testSaveAndRestore(): error during save [" +
                            ex2.getClass().getName() + " : " + ex2.getMessage() + "]");
                    ex2.printStackTrace();
                } else {
                    log.debug("OptionsSaveTest:testSaveAndRestore(): error during restore [" +
                            ex2.getClass().getName() + " : " + ex2.getMessage() + "]");
                    ex2.printStackTrace();
                }
            }

            assertTrue(saved);

            // ---------------------------------------------------------
            // restore from the temporary file
            // ---------------------------------------------------------
            try {
                // setup an input stream to the file
                FileInputStream inStream = new FileInputStream(theFile);

                // attach a stream capable of reading objects from the 
                // stream connected to the file
                ObjectInputStream inObjStream = new ObjectInputStream(inStream);

                // try to restore the options
                log.debug("OptionsSaveTest:testSaveAndRestore(): restoring .....");
                restored = false;
                Options options_restored = (Options) inObjStream.readObject();
                inObjStream.close();
                inStream.close();

                options_restored.activate(configurationContext);

                restored = true;
                log.debug(
                        "OptionsSaveTest:testSaveAndRestore(): ....restored operation completed.....");

                comparesOk = options_restored.isEquivalent(options);
                log.debug("OptionsSaveTest:testSaveAndRestore():   Options equivalency [" +
                        comparesOk + "]");
            }
            catch (Exception ex2) {
                log.debug("OptionsSaveTest:testSaveAndRestore(): error during restore [" +
                        ex2.getClass().getName() + " : " + ex2.getMessage() + "]");
                ex2.printStackTrace();
            }

            assertTrue(restored);

            assertTrue(comparesOk);

            // if the save/restore of the object succeeded,
            // then don't keep the temporary file around
            boolean removeTmpFile = saved && restored && comparesOk;
            if (removeTmpFile) {
                try {
                    theFile.delete();
                }
                catch (Exception e) {
                    // just absorb it
                }
            }

            // indicate that the temp file was created ok
            done = true;
        }

        // this is false when there are problems with the temporary file
        assertTrue(done);

        log.debug("OptionsSaveTest:testSaveAndRestore():  END ---------------");
    }


}
