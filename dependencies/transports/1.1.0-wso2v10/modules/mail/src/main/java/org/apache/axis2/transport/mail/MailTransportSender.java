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

import org.apache.axis2.format.MessageFormatterEx;
import org.apache.axis2.format.MessageFormatterExAdapter;
import org.apache.axis2.transport.base.*;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.*;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.transport.OutTransportInfo;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.util.CommonUtils;

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.DataHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.io.IOException;

/**
 * The mail transport sender sends mail using an SMTP server configuration defined
 * in the axis2.xml's transport sender definition
 */

public class MailTransportSender extends AbstractTransportSender
    implements ManagementSupport {

    private String smtpUsername = null;
    private String smtpPassword = null;
    /** Default from address for outgoing messages */
    private InternetAddress smtpFromAddress = null;
    /** A set of custom Bcc address for all outgoing messages */
    private InternetAddress[] smtpBccAddresses = null;
    /** Default mail format */
    private String defaultMailFormat = "Text";
    /** The default Session which can be safely shared */
    private Session session = null;

    /**
     * The public constructor
     */
    public MailTransportSender() {
        log = LogFactory.getLog(MailTransportSender.class);
    }

    /**
     * Initialize the Mail sender and be ready to send messages
     * @param cfgCtx the axis2 configuration context
     * @param transportOut the transport-out description
     * @throws org.apache.axis2.AxisFault on error
     */
    public void init(ConfigurationContext cfgCtx, TransportOutDescription transportOut) throws AxisFault {
        super.init(cfgCtx, transportOut);

        // initialize SMTP session
        Properties props = new Properties();
        List<Parameter> params = transportOut.getParameters();
        for (Parameter p : params) {
            props.put(p.getName(), p.getValue());
        }

        if (props.containsKey(MailConstants.MAIL_SMTP_FROM)) {
            try {
                smtpFromAddress = new InternetAddress(
                    (String) props.get(MailConstants.MAIL_SMTP_FROM));
            } catch (AddressException e) {
                handleException("Invalid default 'From' address : " +
                    props.get(MailConstants.MAIL_SMTP_FROM), e);
            }
        }

        if (props.containsKey(MailConstants.MAIL_SMTP_BCC)) {
            try {
                smtpBccAddresses = InternetAddress.parse(
                    (String) props.get(MailConstants.MAIL_SMTP_BCC));
            } catch (AddressException e) {
                handleException("Invalid default 'Bcc' address : " +
                    props.get(MailConstants.MAIL_SMTP_BCC), e);
            }
        }

        if (props.containsKey(MailConstants.TRANSPORT_MAIL_FORMAT)) {
            defaultMailFormat = (String) props.get(MailConstants.TRANSPORT_MAIL_FORMAT);
        }

        smtpUsername = (String) props.get(MailConstants.MAIL_SMTP_USERNAME);
        smtpPassword = (String) props.get(MailConstants.MAIL_SMTP_PASSWORD);

        if (smtpUsername != null && smtpPassword != null) {
            session = Session.getInstance(props, new Authenticator() {
                public PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(smtpUsername, smtpPassword);    
                }
            });
        } else {
            session = Session.getInstance(props, null);
        }

        MailUtils.setupLogging(session, log, transportOut);

        // set the synchronise callback table
        if (cfgCtx.getProperty(BaseConstants.CALLBACK_TABLE) == null){
            cfgCtx.setProperty(BaseConstants.CALLBACK_TABLE, new ConcurrentHashMap());
        }
    }

    /**
     * Send the given message over the Mail transport
     *
     * @param msgCtx the axis2 message context
     * @throws AxisFault on error
     */
    public void sendMessage(MessageContext msgCtx, String targetAddress,
        OutTransportInfo outTransportInfo) throws AxisFault {

        MailOutTransportInfo mailOutInfo = null;

        if (targetAddress != null) {
            if (targetAddress.startsWith(MailConstants.TRANSPORT_NAME)) {
                targetAddress = targetAddress.substring(MailConstants.TRANSPORT_NAME.length()+1);
            }

            if (msgCtx.getReplyTo() != null &&
                !AddressingConstants.Final.WSA_NONE_URI.equals(msgCtx.getReplyTo().getAddress()) &&
                !AddressingConstants.Final.WSA_ANONYMOUS_URL.equals(msgCtx.getReplyTo().getAddress())) {
                
                String replyTo = msgCtx.getReplyTo().getAddress();
                if (replyTo.startsWith(MailConstants.TRANSPORT_NAME)) {
                    replyTo = replyTo.substring(MailConstants.TRANSPORT_NAME.length()+1);
                }
                try {
                    mailOutInfo = new MailOutTransportInfo(new InternetAddress(replyTo));
                } catch (AddressException e) {
                    handleException("Invalid reply address/es : " + replyTo, e);
                }
            } else {
                mailOutInfo = new MailOutTransportInfo(smtpFromAddress);
            }

            try {
                mailOutInfo.setTargetAddresses(InternetAddress.parse(targetAddress));
            } catch (AddressException e) {
                handleException("Invalid target address/es : " + targetAddress, e);
            }
        } else if (outTransportInfo != null && outTransportInfo instanceof MailOutTransportInfo) {
            mailOutInfo = (MailOutTransportInfo) outTransportInfo;
        }

        if (mailOutInfo != null) {
            try {
                String messageID = sendMail(mailOutInfo, msgCtx);
                // this is important in axis2 client side if the mail transport uses anonymous addressing
                // the sender have to wait util the response comes.
                if (!msgCtx.getOptions().isUseSeparateListener() && !msgCtx.isServerSide()){
                    waitForReply(msgCtx, messageID);
                }
            } catch (MessagingException e) {
                handleException("Error generating mail message", e);
            } catch (IOException e) {
                handleException("Error generating mail message", e);
            }
        } else {
            handleException("Unable to determine out transport information to send message");
        }
    }

    private void waitForReply(MessageContext msgContext, String mailMessageID) throws AxisFault {
        // piggy back message constant is used to pass a piggy back
        // message context in asnych model
        if (!(msgContext.getAxisOperation() instanceof OutInAxisOperation) &&
                (msgContext.getProperty(org.apache.axis2.Constants.PIGGYBACK_MESSAGE) == null)) {
            return;
        }
        
        ConfigurationContext configContext = msgContext.getConfigurationContext();
        // if the mail message listner has not started we need to start it
        if (!configContext.getListenerManager().isListenerRunning(MailConstants.TRANSPORT_NAME)) {
            TransportInDescription mailTo =
                    configContext.getAxisConfiguration().getTransportIn(MailConstants.TRANSPORT_NAME);
            if (mailTo == null) {
                handleException("Could not find the transport receiver for " +
                    MailConstants.TRANSPORT_NAME);
            }
            configContext.getListenerManager().addListener(mailTo, false);
        }

        SynchronousCallback synchronousCallback = new SynchronousCallback(msgContext);
        Map callBackMap = (Map) msgContext.getConfigurationContext().
            getProperty(BaseConstants.CALLBACK_TABLE);
        callBackMap.put(mailMessageID, synchronousCallback);
        synchronized (synchronousCallback) {
            try {
                synchronousCallback.wait(msgContext.getOptions().getTimeOutInMilliSeconds());
            } catch (InterruptedException e) {
                handleException("Error occured while waiting ..", e);
            }
        }

        if (!synchronousCallback.isComplete()){
            // when timeout occurs remove this entry.
            callBackMap.remove(mailMessageID);
            handleException("Timeout while waiting for a response");
        }
    }

    /**
     * Populate email with a SOAP formatted message
     * @param outInfo the out transport information holder
     * @param msgContext the message context that holds the message to be written
     * @throws AxisFault on error
     * @return id of the send mail message
     */
    private String sendMail(MailOutTransportInfo outInfo, MessageContext msgContext)
        throws AxisFault, MessagingException, IOException {

        OMOutputFormat format = BaseUtils.getOMOutputFormat(msgContext);
        // Make sure that non textual attachements are sent with base64 transfer encoding
        // instead of binary.
        format.setProperty(OMOutputFormat.USE_CTE_BASE64_FOR_NON_TEXTUAL_ATTACHMENTS, true);
        
        MessageFormatter messageFormatter = BaseUtils.getMessageFormatter(msgContext);

        if (log.isDebugEnabled()) {
            log.debug("Creating MIME message using message formatter " +
                    messageFormatter.getClass().getSimpleName());
        }

        WSMimeMessage message = null;
        if (outInfo.getFromAddress() != null) {
            message = new WSMimeMessage(session, outInfo.getFromAddress().getAddress());
        } else {
            message = new WSMimeMessage(session, "");
        }


        Map trpHeaders = (Map) msgContext.getProperty(MessageContext.TRANSPORT_HEADERS);
        if (log.isDebugEnabled() && trpHeaders != null) {
            log.debug("Using transport headers: " + trpHeaders);
        }

        // set From address - first check if this is a reply, then use from address from the
        // transport out, else if any custom transport headers set on this message, or default
        // to the transport senders default From address        
        if (outInfo.getTargetAddresses() != null && outInfo.getFromAddress() != null) {
            if (log.isDebugEnabled()) {
                log.debug("Setting From header to " + outInfo.getFromAddress().getAddress() +
                        " from OutTransportInfo");
            }
            message.setFrom(outInfo.getFromAddress());
            message.setReplyTo((new Address []{outInfo.getFromAddress()}));
        } else if (trpHeaders != null && trpHeaders.containsKey(MailConstants.MAIL_HEADER_FROM)) {
            InternetAddress from =
                new InternetAddress((String) trpHeaders.get(MailConstants.MAIL_HEADER_FROM));
            if (log.isDebugEnabled()) {
                log.debug("Setting From header to " + from.getAddress() +
                        " from transport headers");
            }
            message.setFrom(from);
            message.setReplyTo(new Address[] { from });
        } else {
            if (smtpFromAddress != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Setting From header to " + smtpFromAddress.getAddress() +
                            " from transport configuration");
                }
                message.setFrom(smtpFromAddress);
                message.setReplyTo(new Address[] {smtpFromAddress});
            } else {
                handleException("From address for outgoing message cannot be determined");
            }
        }

        // set To address/es to any custom transport header set on the message, else use the reply
        // address from the out transport information
        if (trpHeaders != null && trpHeaders.containsKey(MailConstants.MAIL_HEADER_TO)) {
            Address[] to =
                InternetAddress.parse((String) trpHeaders.get(MailConstants.MAIL_HEADER_TO)); 
            if (log.isDebugEnabled()) {
                log.debug("Setting To header to " + InternetAddress.toString(to) +
                        " from transport headers");
            }
            message.setRecipients(Message.RecipientType.TO, to);
        } else if (outInfo.getTargetAddresses() != null) {
            if (log.isDebugEnabled()) {
                log.debug("Setting To header to " + InternetAddress.toString(
                        outInfo.getTargetAddresses()) + " from OutTransportInfo");
            }
            message.setRecipients(Message.RecipientType.TO, outInfo.getTargetAddresses());
        } else {
            handleException("To address for outgoing message cannot be determined");
        }

        // set Cc address/es to any custom transport header set on the message, else use the
        // Cc list from original request message
        if (trpHeaders != null && trpHeaders.containsKey(MailConstants.MAIL_HEADER_CC)) {
            Address[] cc =
                InternetAddress.parse((String) trpHeaders.get(MailConstants.MAIL_HEADER_CC)); 
            if (log.isDebugEnabled()) {
                log.debug("Setting Cc header to " + InternetAddress.toString(cc) +
                        " from transport headers");
            }
            message.setRecipients(Message.RecipientType.CC, cc);
        } else if (outInfo.getCcAddresses() != null) {
            if (log.isDebugEnabled()) {
                log.debug("Setting Cc header to " + InternetAddress.toString(
                        outInfo.getCcAddresses()) + " from OutTransportInfo");
            }
            message.setRecipients(Message.RecipientType.CC, outInfo.getCcAddresses());
        }

        // set Bcc address/es to any custom addresses set at the transport sender level + any
        // custom transport header
        if (trpHeaders != null && trpHeaders.containsKey(MailConstants.MAIL_HEADER_BCC)) {
            InternetAddress[] bcc =
                InternetAddress.parse((String) trpHeaders.get(MailConstants.MAIL_HEADER_BCC));
            if (log.isDebugEnabled()) {
                log.debug("Adding Bcc header values " + InternetAddress.toString(bcc) +
                        " from transport headers");
            }
            message.addRecipients(Message.RecipientType.BCC, bcc);
        }
        if (smtpBccAddresses != null) {
            if (log.isDebugEnabled()) {
                log.debug("Adding Bcc header values " + InternetAddress.toString(smtpBccAddresses) +
                        " from transport configuration");
            }
            message.addRecipients(Message.RecipientType.BCC, smtpBccAddresses);
        }

        // set subject
        if (trpHeaders != null && trpHeaders.containsKey(MailConstants.MAIL_HEADER_SUBJECT)) {
            if (log.isDebugEnabled()) {
                log.debug("Setting Subject header to '" + trpHeaders.get(
                        MailConstants.MAIL_HEADER_SUBJECT) + "' from transport headers");
            }
            message.setSubject((String) trpHeaders.get(MailConstants.MAIL_HEADER_SUBJECT));
        } else if (outInfo.getSubject() != null) {
            if (log.isDebugEnabled()) {
                log.debug("Setting Subject header to '" + outInfo.getSubject() +
                        "' from transport headers");
            }
            message.setSubject(outInfo.getSubject());
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Generating default Subject header from SOAP action");
            }
            message.setSubject(BaseConstants.SOAPACTION + ": " + msgContext.getSoapAction());
        }

        //TODO: use a combined message id for smtp so that it generates a unique id while
        // being able to support asynchronous communication.
        // if a custom message id is set, use it
//        if (msgContext.getMessageID() != null) {
//            message.setHeader(MailConstants.MAIL_HEADER_MESSAGE_ID, msgContext.getMessageID());
//            message.setHeader(MailConstants.MAIL_HEADER_X_MESSAGE_ID, msgContext.getMessageID());
//        }

        // if this is a reply, set reference to original message
        if (outInfo.getRequestMessageID() != null) {
            message.setHeader(MailConstants.MAIL_HEADER_IN_REPLY_TO, outInfo.getRequestMessageID());
            message.setHeader(MailConstants.MAIL_HEADER_REFERENCES, outInfo.getRequestMessageID());

        } else {
            if (trpHeaders != null &&
                trpHeaders.containsKey(MailConstants.MAIL_HEADER_IN_REPLY_TO)) {
                message.setHeader(MailConstants.MAIL_HEADER_IN_REPLY_TO,
                    (String) trpHeaders.get(MailConstants.MAIL_HEADER_IN_REPLY_TO));
            }
            if (trpHeaders != null && trpHeaders.containsKey(MailConstants.MAIL_HEADER_REFERENCES)) {
                message.setHeader(MailConstants.MAIL_HEADER_REFERENCES,
                    (String) trpHeaders.get(MailConstants.MAIL_HEADER_REFERENCES));
            }
        }

        // set Date
        message.setSentDate(new Date());


        // set SOAPAction header
        message.setHeader(BaseConstants.SOAPACTION, msgContext.getSoapAction());

        // write body
        MessageFormatterEx messageFormatterEx;
        if (messageFormatter instanceof MessageFormatterEx) {
            messageFormatterEx = (MessageFormatterEx)messageFormatter;
        } else {
            messageFormatterEx = new MessageFormatterExAdapter(messageFormatter);
        }
        
        DataHandler dataHandler = new DataHandler(messageFormatterEx.getDataSource(msgContext, format, msgContext.getSoapAction()));
        
        MimeMultipart mimeMultiPart = null;

        String mFormat = (String) msgContext.getProperty(MailConstants.TRANSPORT_MAIL_FORMAT);
        if (mFormat == null) {
            mFormat = defaultMailFormat;
        }

        if (log.isDebugEnabled()) {
            log.debug("Using mail format '" + mFormat + "'");
        }

        MimePart mainPart;
        if (MailConstants.TRANSPORT_FORMAT_MP.equals(mFormat)) {
            mimeMultiPart = new MimeMultipart();
            MimeBodyPart mimeBodyPart1 = new MimeBodyPart();
            mimeBodyPart1.setContent("Web Service Message Attached","text/plain");
            MimeBodyPart mimeBodyPart2 = new MimeBodyPart();
            mimeMultiPart.addBodyPart(mimeBodyPart1);
            mimeMultiPart.addBodyPart(mimeBodyPart2);
            message.setContent(mimeMultiPart);
			mainPart = mimeBodyPart2;
        } else if (MailConstants.TRANSPORT_FORMAT_ATTACHMENT.equals(mFormat)) {
            mimeMultiPart = new MimeMultipart();
            MimeBodyPart mimeBodyPart1 = new MimeBodyPart();
            mimeBodyPart1.setContent("Web Service Message Attached","text/plain");
            MimeBodyPart mimeBodyPart2 = new MimeBodyPart();
            mimeMultiPart.addBodyPart(mimeBodyPart1);
            mimeMultiPart.addBodyPart(mimeBodyPart2);
            message.setContent(mimeMultiPart);

            String fileName = (String) msgContext.getProperty(
                    MailConstants.TRANSPORT_FORMAT_ATTACHMENT_FILE);
            if (fileName != null) {
                mimeBodyPart2.setFileName(fileName);
            } else {
                mimeBodyPart2.setFileName("attachment");
            }

            mainPart = mimeBodyPart2;
	} else {
            mainPart = message;
        }

        try {
            mainPart.setHeader(BaseConstants.SOAPACTION, msgContext.getSoapAction());
            mainPart.setDataHandler(dataHandler);
            
            // AXIOM's idea of what is textual also includes application/xml and
            // application/soap+xml (which JavaMail considers as binary). For these content types
            // always use quoted-printable transfer encoding. Note that JavaMail is a bit smarter
            // here because it can choose between 7bit and quoted-printable automatically, but it
            // needs to scan the entire content to determine this.
            if (msgContext.getOptions().getProperty("Content-Transfer-Encoding") != null) {
                mainPart.setHeader("Content-Transfer-Encoding",
                        (String) msgContext.getOptions().getProperty("Content-Transfer-Encoding"));
            } else {
                String contentType = dataHandler.getContentType().toLowerCase();
                if (!contentType.startsWith("multipart/") && CommonUtils.isTextualPart(contentType)) {
                    mainPart.setHeader("Content-Transfer-Encoding", "quoted-printable");
                }
            }

            //setting any custom headers defined by the user
            if (msgContext.getOptions().getProperty(MailConstants.TRANSPORT_MAIL_CUSTOM_HEADERS) != null) {
                Map customTransportHeaders = (Map)msgContext.getOptions().getProperty(MailConstants.TRANSPORT_MAIL_CUSTOM_HEADERS);
                for (Object header: customTransportHeaders.keySet()){
                    mainPart.setHeader((String)header,(String)customTransportHeaders.get(header));
                }
            }


            
            log.debug("Sending message");
            Transport.send(message);

            // update metrics
            metrics.incrementMessagesSent(msgContext);
            long bytesSent = message.getBytesSent();
            if (bytesSent != -1) {
                metrics.incrementBytesSent(msgContext, bytesSent);
            }

        } catch (MessagingException e) {
            metrics.incrementFaultsSending();
            handleException("Error creating mail message or sending it to the configured server", e);
            
        }
        return message.getMessageID();
    }
}
