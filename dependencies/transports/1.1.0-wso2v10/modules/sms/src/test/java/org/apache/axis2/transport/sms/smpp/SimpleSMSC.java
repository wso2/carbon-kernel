/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.axis2.transport.sms.smpp;

import org.jsmpp.session.*;
import org.jsmpp.util.MessageId;
import org.jsmpp.util.MessageIDGenerator;
import org.jsmpp.util.RandomMessageIDGenerator;
import org.jsmpp.bean.*;
import org.jsmpp.extra.ProcessRequestException;
import org.jsmpp.PDUStringException;
import org.jsmpp.SMPPConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeoutException;
import java.io.IOException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * This is a simulator of a Shot message service center
 * this is written for the test perposes of the SMPP
 * implementation of the SMS Transport
 *
 */
public class SimpleSMSC  extends ServerResponseDeliveryAdapter implements Runnable, ServerMessageReceiverListener {
    private static final Integer DEFAULT_PORT = 2776;
    private static final Logger logger = LoggerFactory.getLogger(SimpleSMSC.class);
    private final ExecutorService execService = Executors.newFixedThreadPool(5);
    private final MessageIDGenerator messageIDGenerator = new RandomMessageIDGenerator();
    private int port = DEFAULT_PORT;
    private SMSCMessageNotifier notifier;
    private boolean started =false;
    private SMPPServerSession serverSession = null;

    private Semaphore control = new Semaphore(0);

    public SimpleSMSC() {
        notifier = SMSCMessageNotifier.getInstence();
    }


    /**
     * Delever short message to a bineded ESMC s
     * @param sender
     * @param receiver
     * @param content
     * @throws Exception
     */
     public void deliverSMS(String sender , String receiver , String content) throws Exception{

        this.serverSession.deliverShortMessage(
                "CMT",
                TypeOfNumber.UNKNOWN ,
                NumberingPlanIndicator.UNKNOWN ,
                receiver ,
                TypeOfNumber.UNKNOWN ,
                NumberingPlanIndicator.UNKNOWN ,
                sender ,
                new ESMClass() ,
                (byte)0 ,
                (byte)0,
                new RegisteredDelivery(0),
                DataCoding.newInstance(0),
                content.getBytes());
    }

    public void run() {
        try {
            SMPPServerSessionListener sessionListener = null;
                sessionListener = new SMPPServerSessionListener(port);
                logger.info("Simple SMSC Listening on port :: " + port);
                control.release();
            while (true) {
                serverSession = sessionListener.accept();
                serverSession.setMessageReceiverListener(this);
                serverSession.setResponseDeliveryListener(this);

                // TODO: quick fix for build instability; if not set, the Hudson build
                //       may fail with a message such as "No response after waiting for 2000 millis
                //       when executing deliver_sm with sessionId 8cc2b5f5 and sequenceNumber 1"
                serverSession.setTransactionTimer(10000);

                execService.execute(new WaitBindTask(serverSession));
                Thread.sleep(1000);

            }
        } catch (Exception e) {
            logger.error("IO error occured", e);
        }
    }

    public MessageId onAcceptSubmitSm(SubmitSm submitSm, SMPPServerSession smppServerSession) throws ProcessRequestException {
        MessageId messageId = messageIDGenerator.newMessageId();
        notifier.notifyObservers(submitSm);
        return messageId;
    }

    public SubmitMultiResult onAcceptSubmitMulti(SubmitMulti submitMulti, SMPPServerSession smppServerSession) throws ProcessRequestException {
        return null;
    }

    public QuerySmResult onAcceptQuerySm(QuerySm querySm, SMPPServerSession smppServerSession) throws ProcessRequestException {
        return null;
    }

    public void onAcceptReplaceSm(ReplaceSm replaceSm, SMPPServerSession smppServerSession) throws ProcessRequestException {

    }

    public void onAcceptCancelSm(CancelSm cancelSm, SMPPServerSession smppServerSession) throws ProcessRequestException {

    }

    public DataSmResult onAcceptDataSm(DataSm dataSm) throws ProcessRequestException {
        return null;
    }

    private class WaitBindTask implements Runnable {
        private final SMPPServerSession serverSession;

        public WaitBindTask(SMPPServerSession serverSession) {
            this.serverSession = serverSession;
        }

        public void run() {
            try {
                BindRequest bindRequest = serverSession.waitForBind(1000);
                logger.info("Accepting bind for session {}", serverSession.getSessionId());
                try {
                    bindRequest.accept("sys");
                } catch (PDUStringException e) {
                    logger.error("Invalid system id", e);
                    bindRequest.reject(SMPPConstant.STAT_ESME_RSYSERR);
                }

            } catch (IllegalStateException e) {
                logger.error("System error", e);
            } catch (TimeoutException e) {
                logger.warn("Wait for bind has reach timeout", e);
            } catch (IOException e) {
                logger.error("Failed accepting bind request for session {}", serverSession.getSessionId());
            }
        }
    }

    /**
     * Start the SimpleSMSC server
     */
    public void startServer() {
        Thread t = new Thread(this);
        t.start();

    }


    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isStarted() {
        return started;
    }

    public Semaphore getControl() {
        return control;
    }
}
