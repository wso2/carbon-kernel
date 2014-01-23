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

package org.apache.axis2.transport.mail;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Collections;

import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.ParameterInclude;
import org.apache.axis2.transport.base.AbstractPollTableEntry;
import org.apache.axis2.transport.base.BaseConstants;
import org.apache.axis2.transport.base.ParamUtils;
import org.apache.commons.logging.Log;

/**
 * Holds information about an entry in the VFS transport poll table used by the
 * VFS Transport Listener
 */
public class PollTableEntry extends AbstractPollTableEntry {
    private final Log log;

    // operation after mail check
    public static final int DELETE = 0;
    public static final int MOVE   = 1;

    /** The email address mapped to the service */
    private InternetAddress emailAddress = null;

    /** account username to check mail */
    private String userName = null;
    /** account password to check mail */
    private String password = null;
    /** The protocol to be used - pop3 or imap */
    private String protocol = null;
    /** The JavaMail session used to connect to the mail store */
    private Session session;

    /** The mail folder from which to check mail */
    private String folder;
    /** X-Service-Path custom header */
    // FIXME: this value of this property is never set nor retrieved
    private String xServicePath;
    /** Content-Type to use for the message */
    private String contentType;
    /** default reply address */
    private InternetAddress replyAddress = null;

    /** list of mail headers to be preserved into the Axis2 message as transport headers */
    private List<String> preserveHeaders = null;
    /** list of mail headers to be removed from the Axis2 message transport headers */
    private List<String> removeHeaders = null;

    /** action to take after a successful poll */
    private int actionAfterProcess = DELETE;
    /** action to take after a failed poll */
    private int actionAfterFailure = DELETE;

    /** folder to move the email after processing */
    private String moveAfterProcess;
    /** folder to move the email after failure */
    private String moveAfterFailure;
    /** Should mail be processed in parallel? e.g. with IMAP */
    private boolean processingMailInParallel = false;
    /** UIDs of messages currently being processed */
    private List<String> uidList = Collections.synchronizedList(new ArrayList<String>());

    private int maxRetryCount;
    private long reconnectTimeout;

    public PollTableEntry(Log log) {
        this.log = log;
    }

    @Override
    public EndpointReference[] getEndpointReferences(AxisService service, String ip) {
        return new EndpointReference[] { new EndpointReference(MailConstants.TRANSPORT_PREFIX
                + emailAddress) };
    }

    public InternetAddress getEmailAddress() {
        return emailAddress;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getXServicePath() {
        return xServicePath;
    }

    public String getContentType() {
        return contentType;
    }

    public int getActionAfterProcess() {
        return actionAfterProcess;
    }

    public int getActionAfterFailure() {
        return actionAfterFailure;
    }

    public String getMoveAfterProcess() {
        return moveAfterProcess;
    }

    public String getMoveAfterFailure() {
        return moveAfterFailure;
    }

    public int getMaxRetryCount() {
      return maxRetryCount;
    }

    public long getReconnectTimeout() {
      return reconnectTimeout;
    }

    public String getFolder() {
        return folder;
    }

    public InternetAddress getReplyAddress() {
        return replyAddress;
    }

    /**
     * Get the mail store protocol.
     * This protocol identifier is used in calls to {@link Session#getStore()}.
     * 
     * @return the mail store protocol
     */
    public String getProtocol() {
        return protocol;
    }

    public Session getSession() {
        return session;
    }

    private void addPreserveHeaders(String headerList) {
        if (headerList == null) return;
        StringTokenizer st = new StringTokenizer(headerList, " ,");
        preserveHeaders = new ArrayList<String>();
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.length() != 0) {
                preserveHeaders.add(token);
            }
        }
    }

    private void addRemoveHeaders(String headerList) {
        if (headerList == null) return;
        StringTokenizer st = new StringTokenizer(headerList, " ,");
        removeHeaders = new ArrayList<String>();
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.length() != 0) {
                removeHeaders.add(token);
            }
        }
    }

    public boolean retainHeader(String name) {
        if (preserveHeaders != null) {
            return preserveHeaders.contains(name);
        } else if (removeHeaders != null) {
            return !removeHeaders.contains(name);
        } else {
            return true;
        }
    }

    public boolean isProcessingMailInParallel() {
        return processingMailInParallel;
    }

    @Override
    public boolean loadConfiguration(ParameterInclude paramIncl) throws AxisFault {
        String address =
            ParamUtils.getOptionalParam(paramIncl, MailConstants.TRANSPORT_MAIL_ADDRESS);
        if (address == null) {
            return false;
        } else {
            try {
                emailAddress = new InternetAddress(address);
            } catch (AddressException e) {
                throw new AxisFault("Invalid email address specified by '" +
                        MailConstants.TRANSPORT_MAIL_ADDRESS + "' parameter :: " + e.getMessage());
            }

            List<Parameter> params = paramIncl.getParameters();
            Properties props = new Properties();
            for (Parameter p : params) {
                if (p.getName().startsWith("mail.")) {
                    props.setProperty(p.getName(), (String) p.getValue());
                }

                if (MailConstants.MAIL_POP3_USERNAME.equals(p.getName()) ||
                    MailConstants.MAIL_IMAP_USERNAME.equals(p.getName())) {
                    userName = (String) p.getValue();
                }
                if (MailConstants.MAIL_POP3_PASSWORD.equals(p.getName()) ||
                    MailConstants.MAIL_IMAP_PASSWORD.equals(p.getName())) {
                    password = (String) p.getValue();
                }
                if (MailConstants.TRANSPORT_MAIL_PROTOCOL.equals(p.getName())) {
                    protocol = (String) p.getValue();
                }
            }

            session = Session.getInstance(props, null);
            MailUtils.setupLogging(session, log, paramIncl);

            contentType =
                ParamUtils.getOptionalParam(paramIncl, MailConstants.TRANSPORT_MAIL_CONTENT_TYPE);
            try {
                String replyAddress = 
                    ParamUtils.getOptionalParam(paramIncl, MailConstants.TRANSPORT_MAIL_REPLY_ADDRESS);
                if (replyAddress != null) {
                    this.replyAddress = new InternetAddress(replyAddress);   
                }
            } catch (AddressException e) {
                throw new AxisFault("Invalid email address specified by '" +
                        MailConstants.TRANSPORT_MAIL_REPLY_ADDRESS + "' parameter :: " +
                        e.getMessage());
            }

            folder =
                ParamUtils.getOptionalParam(paramIncl, MailConstants.TRANSPORT_MAIL_FOLDER);

            addPreserveHeaders(
                ParamUtils.getOptionalParam(paramIncl, MailConstants.TRANSPORT_MAIL_PRESERVE_HEADERS));
            addRemoveHeaders(
                ParamUtils.getOptionalParam(paramIncl, MailConstants.TRANSPORT_MAIL_REMOVE_HEADERS));

            String option = ParamUtils.getOptionalParam(
                paramIncl, MailConstants.TRANSPORT_MAIL_ACTION_AFTER_PROCESS);
            actionAfterProcess =
                MailTransportListener.MOVE.equals(option) ? PollTableEntry.MOVE : PollTableEntry.DELETE;
            option = ParamUtils.getOptionalParam(
                paramIncl, MailConstants.TRANSPORT_MAIL_ACTION_AFTER_FAILURE);
            actionAfterFailure =
                MailTransportListener.MOVE.equals(option) ? PollTableEntry.MOVE : PollTableEntry.DELETE;

            moveAfterProcess = ParamUtils.getOptionalParam(
                paramIncl, MailConstants.TRANSPORT_MAIL_MOVE_AFTER_PROCESS);
            moveAfterFailure = ParamUtils.getOptionalParam(
                paramIncl, MailConstants.TRANSPORT_MAIL_MOVE_AFTER_FAILURE);

            String processInParallel = ParamUtils.getOptionalParam(
                paramIncl, MailConstants.TRANSPORT_MAIL_PROCESS_IN_PARALLEL);
            if (processInParallel != null) {
                processingMailInParallel = Boolean.parseBoolean(processInParallel);
                if (log.isDebugEnabled() && processingMailInParallel) {
                    log.debug("Parallel mail processing enabled for : " + address);
                }
            }

            String pollInParallel = ParamUtils.getOptionalParam(
                paramIncl, BaseConstants.TRANSPORT_POLL_IN_PARALLEL);
            if (pollInParallel != null) {
                setConcurrentPollingAllowed(Boolean.parseBoolean(pollInParallel));
                if (log.isDebugEnabled() && isConcurrentPollingAllowed()) {
                    log.debug("Concurrent mail polling enabled for : " + address);
                }
            }

            String strMaxRetryCount = ParamUtils.getOptionalParam(
                paramIncl, MailConstants.MAX_RETRY_COUNT);
            if (strMaxRetryCount != null) {
                maxRetryCount = Integer.parseInt(strMaxRetryCount);
            }

            String strReconnectTimeout = ParamUtils.getOptionalParam(
                paramIncl, MailConstants.RECONNECT_TIMEOUT);
            if (strReconnectTimeout != null) {
                reconnectTimeout = Integer.parseInt(strReconnectTimeout) * 1000;
            }

            return super.loadConfiguration(paramIncl);
        }
    }

    public synchronized void processingUID(String uid) {
        this.uidList.add(uid);
    }

    public synchronized boolean isProcessingUID(String uid) {
        return this.uidList.contains(uid);
    }

    public synchronized void removeUID(String uid) {
        this.uidList.remove(uid);
    }
}
