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

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.transport.RequestResponseTransport;
import org.apache.axis2.transport.base.AbstractPollingTransportListener;
import org.apache.axis2.transport.base.BaseConstants;
import org.apache.axis2.transport.base.ManagementSupport;
import org.apache.axis2.transport.base.event.TransportErrorListener;
import org.apache.axis2.transport.base.event.TransportErrorSource;
import org.apache.axis2.transport.base.event.TransportErrorSourceSupport;

import javax.mail.*;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.ParseException;
import javax.xml.stream.XMLStreamException;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.lang.reflect.Method;

/**
 * This mail transport lister implementation uses the base transport framework and is a polling
 * transport. i.e. a service can register itself with custom a custom mail configuration (i.e.
 * pop3 or imap) and specify its polling duration, and what action to be taken after processing
 * messages. The transport always deletes processed mails from the folder they were fetched from
 * and can be configured to be optionally moved to a different folder, if the server supports it
 * (e.g. with imap). When checking for new mail, the transport ignores messages already flaged as
 * SEEN and DELETED
 */

public class MailTransportListener extends AbstractPollingTransportListener<PollTableEntry>
    implements ManagementSupport, TransportErrorSource {

    public static final String DELETE = "DELETE";
    public static final String MOVE = "MOVE";
    
    private final TransportErrorSourceSupport tess = new TransportErrorSourceSupport(this);

    @Override
    protected void doInit() throws AxisFault {
        super.doInit();
        // set the synchronise callback table
        if (cfgCtx.getProperty(BaseConstants.CALLBACK_TABLE) == null){
            cfgCtx.setProperty(BaseConstants.CALLBACK_TABLE, new ConcurrentHashMap());
        }
    }

    @Override
    protected void poll(PollTableEntry entry) {
        checkMail(entry, entry.getEmailAddress());
    }

    /**
     * Check mail for a particular service that has registered with the mail transport
     *
     * @param entry        the poll table entry that stores service specific informaiton
     * @param emailAddress the email address checked
     */
    private void checkMail(final PollTableEntry entry, InternetAddress emailAddress) {

        if (log.isDebugEnabled()) {
            log.debug("Checking mail for account : " + emailAddress);
        }

        boolean connected = false;
        int retryCount = 0;
        int maxRetryCount = entry.getMaxRetryCount();
        long reconnectionTimeout = entry.getReconnectTimeout();
        Session session = entry.getSession();
        Store store = null;
        Folder folder = null;
        boolean mailProcessingStarted = false;

        while (!connected) {
            try {
                retryCount++;
                if (log.isDebugEnabled()) {
                    log.debug("Attempting to connect to POP3/IMAP server for : " +
                        entry.getEmailAddress() + " using " + session.getProperties());
                }

                store = session.getStore(entry.getProtocol());

                if (entry.getUserName() != null && entry.getPassword() != null) {
                    store.connect(entry.getUserName(), entry.getPassword());
                } else {
                    handleException("Unable to locate username and password for mail login", null);
                }

                // were we able to connect?
                connected = store.isConnected();

                if (connected) {
                    if (entry.getFolder() != null) {
                        folder = store.getFolder(entry.getFolder());
                    } else {
                        folder = store.getFolder(MailConstants.DEFAULT_FOLDER);
                    }
                    if (folder == null) {
                        folder = store.getDefaultFolder();
                    }
                }

            } catch (Exception e) {
                log.error("Error connecting to mail server for address : " + emailAddress, e);
                if (maxRetryCount <= retryCount) {
                    processFailure("Error connecting to mail server for address : " +
                        emailAddress + " :: " + e.getMessage(), e, entry);
                    return;
                }
            }

            if (!connected) {
                try {
                    log.warn("Connection to mail server for account : " + entry.getEmailAddress() +
                        " failed. Retrying in : " + reconnectionTimeout / 1000 + " seconds");
                    Thread.sleep(reconnectionTimeout);
                } catch (InterruptedException ignore) {
                }
            }
        }

        if (connected && folder != null) {

            CountDownLatch latch = null;
            Runnable onCompletion = new MailCheckCompletionTask(folder, store, emailAddress, entry);

            try {
                if (log.isDebugEnabled()) {
                    log.debug("Connecting to folder : " + folder.getName() +
                        " of email account : " + emailAddress);
                }

                folder.open(Folder.READ_WRITE);
                int total = folder.getMessageCount();
                Message[] messages = folder.getMessages();

                if (log.isDebugEnabled()) {
                    log.debug(messages.length + " messgaes in folder : " + folder);
                }

                latch = new CountDownLatch(total);
                for (int i = 0; i < total; i++) {

                    try {
                        String[] status = messages[i].getHeader("Status");
                        if (status != null && status.length == 1 && status[0].equals("RO")) {
                            // some times the mail server sends a special mail message which is
                            // not relavent in processing. ignore this message.
                            if (log.isDebugEnabled()) {
                                log.debug("Skipping message # : " + messages[i].getMessageNumber()
                                    + " : " + messages[i].getSubject() + " - Status: RO");
                            }
                            latch.countDown();
                        } else if (messages[i].isSet(Flags.Flag.SEEN)) {
                            if (log.isDebugEnabled()) {
                                log.debug("Skipping message # : " + messages[i].getMessageNumber()
                                    + " : " + messages[i].getSubject() + " - already marked SEEN");
                            }
                            latch.countDown();
                        } else if (messages[i].isSet(Flags.Flag.DELETED)) {
                            if (log.isDebugEnabled()) {
                                log.debug("Skipping message # : " + messages[i].getMessageNumber()
                                    + " : " +  messages[i].getSubject() + " - already marked DELETED");
                            }
                            latch.countDown();

                        } else {
                            processMail(entry, folder, store, messages[i], latch, onCompletion);
                            mailProcessingStarted = true;
                        }
                    } catch (MessageRemovedException ignore) {
                        // while reading the meta information, this mail was deleted, thats ok
                        if (log.isDebugEnabled()) {
                            log.debug("Skipping message # : " + messages[i].getMessageNumber() +
                                " as it has been DELETED by another thread after processing");
                        }
                        latch.countDown();
                    }
                }

                if (!mailProcessingStarted) {
                    // if we didnt process any mail in this run, the onCompletion will not
                    // run from the mail processor by default
                    onCompletion.run();
                }

            } catch (MessagingException me) {
                processFailure("Error checking mail for account : " +
                    emailAddress + " :: " + me.getMessage(), me, entry);
            }

        } else {
            processFailure("Unable to access mail folder", null, entry);
        }
    }

    /**
     * Invoke the actual message processor in the current thread or another worker thread
     * @param entry PolltableEntry
     * @param folder mail folder
     * @param store mail store, to move or delete after processing
     * @param message message to process
     * @param pos the message position seen initially
     * @param mp the MailProcessor object
     * @param latch the completion latch to notify
     * @param onCompletion the tasks to run on the completion of mail processing
     */
    private void processMail(PollTableEntry entry, Folder folder, Store store, Message message,
                             CountDownLatch latch, Runnable onCompletion) {

        MailProcessor mp = new MailProcessor(entry, message, store, folder, latch, onCompletion);

        // should messages be processed in parallel?
        if (entry.isConcurrentPollingAllowed()) {

            // try to locate the UID of the message
            String uid = getMessageUID(folder, message);

            if (uid != null) {
                if (entry.isProcessingUID(uid)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Skipping message # : " + message.getMessageNumber() + " : UIDL " +
                            uid + " - already being processed by another thread");
                    }
                    latch.countDown();

                } else {
                    entry.processingUID(uid);
                    mp.setUID(uid);
                    
                    if (entry.isProcessingMailInParallel()) {
                        if (log.isDebugEnabled()) {
                            log.debug("Processing message # : " + message.getMessageNumber() +
                                " with UID : " + uid + " with a worker thread");
                        }
                        workerPool.execute(mp);
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("Processing message # : " + message.getMessageNumber() +
                                " with UID : " + uid + " in same thread");
                        }
                        mp.run();
                    }
                }
            } else {
                log.warn("Cannot process mail in parallel as the " +
                    "folder does not support UIDs. Processing message # : " +
                    message.getMessageNumber() + " in the same thread");
                entry.setConcurrentPollingAllowed(false);
                mp.run();
            }

        } else {
            if (entry.isProcessingMailInParallel()) {
                if (log.isDebugEnabled()) {
                    log.debug("Processing message # : " + message.getMessageNumber() +
                        " with a worker thread");
                }
                workerPool.execute(mp);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Processing message # : " + message.getMessageNumber() + " in same thread");
                }
                mp.run();
            }
        }
    }

    /**
     * Handle processing of a message, possibly in a new thread
     */
    private class MailProcessor implements Runnable {

        private PollTableEntry entry = null;
        private Message message = null;
        private Store store = null;
        private Folder folder = null;
        private String uid = null;
        private CountDownLatch doneSignal = null;
        private Runnable onCompletion = null;

        MailProcessor(PollTableEntry entry, Message message, Store store, Folder folder,
                      CountDownLatch doneSignal, Runnable onCompletion) {
            this.entry = entry;
            this.message = message;
            this.store = store;
            this.folder = folder;
            this.doneSignal = doneSignal;
            this.onCompletion = onCompletion;
        }

        public void setUID(String uid) {
            this.uid = uid;
        }

        public void run() {

            entry.setLastPollState(PollTableEntry.NONE);
            try {
                processMail(message, entry);
                entry.setLastPollState(PollTableEntry.SUCCSESSFUL);
                metrics.incrementMessagesReceived();

            } catch (Exception e) {
                log.error("Failed to process message", e);
                entry.setLastPollState(PollTableEntry.FAILED);
                metrics.incrementFaultsReceiving();
                tess.error(entry.getService(), e);

            } finally {
                if (uid != null) {
                    entry.removeUID(uid);
                }
            }
            try {
                moveOrDeleteAfterProcessing(entry, store, folder, message);
            } catch (Exception e) {
                log.error("Failed to move or delete email message", e);
                tess.error(entry.getService(), e);
            }

            doneSignal.countDown();

            if (doneSignal.getCount() == 0) {
                onCompletion.run();
            }
        }
    }

    /**
     * Handle optional logic of the mail transport, that needs to happen once all messages in
     * a check mail cycle has ended.
     */
    private class MailCheckCompletionTask implements Runnable {
        private final Folder folder;
        private final Store store;
        private final InternetAddress emailAddress;
        private final PollTableEntry entry;
        private boolean taskStarted = false;

        public MailCheckCompletionTask(Folder folder, Store store,
                                       InternetAddress emailAddress, PollTableEntry entry) {
            this.folder = folder;
            this.store = store;
            this.emailAddress = emailAddress;
            this.entry = entry;
        }

        public void run() {
            synchronized(this) {
                if (taskStarted) {
                    return;
                } else {
                    taskStarted = true;
                }
            }
            
            if (log.isDebugEnabled()) {
                log.debug("Executing onCompletion task for the mail download of : " + emailAddress);
            }

            if (folder != null) {
                try {
                    folder.close(true /** expunge messages flagged as DELETED*/);
                    if (log.isDebugEnabled()) {
                        log.debug("Mail folder closed, and deleted mail expunged");
                    }
                } catch (MessagingException e) {
                    log.warn("Error closing mail folder : " +
                        folder + " for account : " + emailAddress + " :: "+ e.getMessage());
                }
            }

            if (store != null) {
                try {
                    store.close();
                    if (log.isDebugEnabled()) {
                        log.debug("Mail store closed for : " + emailAddress);
                    }
                } catch (MessagingException e) {
                    log.warn("Error closing mail store for account : " +
                        emailAddress + " :: " + e.getMessage(), e);
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("Scheduling next poll for : " + emailAddress);
            }
            onPollCompletion(entry);
        }
    }

    /**
     * Process a mail message through Axis2
     *
     * @param message the email message
     * @param entry   the poll table entry
     * @throws MessagingException on error
     * @throws IOException        on error
     */
    private void processMail(Message message, PollTableEntry entry)
        throws MessagingException, IOException {

        updateMetrics(message);

        // populate transport headers using the mail headers
        Map trpHeaders = getTransportHeaders(message, entry);

        // Allow the content type to be overridden by configuration.
        String contentType = entry.getContentType();
        Part messagePart;
        if (contentType != null) {
            messagePart = message;
        } else {
            messagePart = getMessagePart(message, cfgCtx.getAxisConfiguration());
            contentType = messagePart.getContentType();
        }
        
        // FIXME: remove this ugly hack when Axis2 has learned that content types are case insensitive...
        int idx = contentType.indexOf(';');
        if (idx == -1) {
            contentType = contentType.toLowerCase();
        } else {
            contentType = contentType.substring(0, idx).toLowerCase() + contentType.substring(idx);
        }
        
        // if the content type was not found, we have an error
        if (contentType == null) {
            processFailure("Unable to determine Content-type for message : " +
                message.getMessageNumber() + " :: " + message.getSubject(), null, entry);
            return;
        } else if (log.isDebugEnabled()) {
            log.debug("Processing message as Content-Type : " + contentType);
        }

        MessageContext msgContext = entry.createMessageContext();

        // Extract the charset encoding from the configured content type and
        // set the CHARACTER_SET_ENCODING property as e.g. SOAPBuilder relies on this.
        String charSetEnc;
        try {
            charSetEnc = new ContentType(contentType).getParameter("charset");
        } catch (ParseException ex) {
            // ignore
            charSetEnc = null;
        }
        msgContext.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING, charSetEnc);
        
        MailOutTransportInfo outInfo = buildOutTransportInfo(message, entry);

        // save out transport information
        msgContext.setProperty(Constants.OUT_TRANSPORT_INFO, outInfo);
        // this property only useful for supporting smtp with Sandesha2.
        msgContext.setProperty(RequestResponseTransport.TRANSPORT_CONTROL, new MailRequestResponseTransport());

        // set message context From
        if (outInfo.getFromAddress() != null) {
            msgContext.setFrom(
                new EndpointReference(MailConstants.TRANSPORT_PREFIX +
                    outInfo.getFromAddress().getAddress()));
        }

        // save original mail message id message context MessageID
        msgContext.setMessageID(outInfo.getRequestMessageID());

        // set the message payload to the message context
        InputStream in = messagePart.getInputStream();
        try {
            try {
                msgContext.setEnvelope(TransportUtils.createSOAPMessage(msgContext, in, contentType));
            } catch (XMLStreamException ex) {
                handleException("Error parsing message", ex);
            }

            String soapAction = (String) trpHeaders.get(BaseConstants.SOAPACTION);
            if (soapAction == null && message.getSubject() != null &&
                message.getSubject().startsWith(BaseConstants.SOAPACTION)) {
                soapAction = message.getSubject().substring(BaseConstants.SOAPACTION.length());
                if (soapAction.startsWith(":")) {
                    soapAction = soapAction.substring(1).trim();
                }
            }
    
            handleIncomingMessage(
                msgContext,
                trpHeaders,
                soapAction,
                contentType
            );
        } finally {
            in.close();
        }

        if (log.isDebugEnabled()) {
            log.debug("Processed message : " + message.getMessageNumber() +
                " :: " + message.getSubject());
        }
    }

    private void updateMetrics(Message message) throws IOException, MessagingException {
        if (message instanceof MimeMessage) {
            MimeMessage mimeMessage = (MimeMessage) message;
            if (mimeMessage.getContent() instanceof Multipart) {
                Multipart mp = (Multipart) mimeMessage.getContent();
                for (int i=0; i<mp.getCount(); i++) {
                    MimeBodyPart mbp = (MimeBodyPart) mp.getBodyPart(i);
                    int size = mbp.getSize();
                    if (size != -1) {
                        metrics.incrementBytesReceived(size);
                    }
                }
            } else {
                int size = mimeMessage.getSize();
                if (size != -1) {
                    metrics.incrementBytesReceived(size);
                }
            }
        }
    }

    private Map getTransportHeaders(Message message, PollTableEntry entry) {

        //use a comaprator to ignore the case for headers.
        Comparator comparator = new Comparator(){
            public int compare(Object o1, Object o2) {
                String string1 = (String) o1;
                String string2 = (String) o2;
                return string1.compareToIgnoreCase(string2);
            }
        };

        Map trpHeaders = new TreeMap(comparator);
        try {
            Enumeration e = message.getAllHeaders();
            while (e.hasMoreElements()) {
                Header h = (Header) e.nextElement();
                if (entry.retainHeader(h.getName())) {
                    trpHeaders.put(h.getName(), h.getValue());
                }
            }
        } catch (MessagingException ignore) {}
        return trpHeaders;
    }
    
    /**
     * Extract the part from the mail that contains the message to be processed.
     * This method supports multipart/mixed messages that contain a text/plain
     * part alongside the message.
     * 
     * @param message
     * @return
     * @throws MessagingException
     * @throws IOException 
     */
    private Part getMessagePart(Message message, AxisConfiguration axisCfg)
            throws MessagingException, IOException {
        
        ContentType contentType = new ContentType(message.getContentType());
        if (contentType.getBaseType().equalsIgnoreCase("multipart/mixed")) {
            Multipart multipart = (Multipart)message.getContent();
            Part textMainPart = null;
            for (int i=0; i<multipart.getCount(); i++) {
                MimeBodyPart bodyPart = (MimeBodyPart)multipart.getBodyPart(i);
                ContentType partContentType = new ContentType(bodyPart.getContentType());
                if (axisCfg.getMessageBuilder(partContentType.getBaseType()) != null) {
                    if (partContentType.getBaseType().equalsIgnoreCase("text/plain")) {
                        // If it's a text/plain part, remember it. We will return
                        // it later if we don't find something more interesting.
                        textMainPart = bodyPart;
                    } else {
                        return bodyPart;
                    }
                }
            }
            if (textMainPart != null) {
                return textMainPart;
            } else {
                // We have nothing else to return!
                return message;
            }
        } else {
            return message;
        }
    }

    private MailOutTransportInfo buildOutTransportInfo(Message message,
            PollTableEntry entry) throws MessagingException {
        MailOutTransportInfo outInfo = new MailOutTransportInfo(entry.getEmailAddress());

        // determine reply address
        if (message.getReplyTo() != null) {
            outInfo.setTargetAddresses((InternetAddress[]) message.getReplyTo());
        } else if (message.getFrom() != null) {
            outInfo.setTargetAddresses((InternetAddress[]) message.getFrom());
        } else {
            // does the service specify a default reply address ?
            InternetAddress replyAddress = entry.getReplyAddress();
            if (replyAddress != null) {
                outInfo.setTargetAddresses(new InternetAddress[] { replyAddress });
            }
        }

        // save CC addresses
        if (message.getRecipients(Message.RecipientType.CC) != null) {
            outInfo.setCcAddresses(
                (InternetAddress[]) message.getRecipients(Message.RecipientType.CC));
        }

        // determine and subject for the reply message
        if (message.getSubject() != null) {
            outInfo.setSubject("Re: " + message.getSubject());
        }

        // save original message ID if one exists, so that replies can be correlated
        if (message.getHeader(MailConstants.MAIL_HEADER_X_MESSAGE_ID) != null) {
            outInfo.setRequestMessageID(message.getHeader(MailConstants.MAIL_HEADER_X_MESSAGE_ID)[0]);
        } else if (message instanceof MimeMessage && ((MimeMessage) message).getMessageID() != null) {
            outInfo.setRequestMessageID(((MimeMessage) message).getMessageID());
        }
        return outInfo;
    }

    /**
     * Take specified action to either move or delete the processed email
     *
     * @param entry   the PollTableEntry for the email that has been processed
     * @param store   the mail store
     * @param folder  mail folder
     * @param message the email message to be moved or deleted
     */
    private void moveOrDeleteAfterProcessing(final PollTableEntry entry, Store store,
                                             Folder folder, Message message) {

        String moveToFolder = null;
        try {
            switch (entry.getLastPollState()) {
                case PollTableEntry.SUCCSESSFUL:
                    if (entry.getActionAfterProcess() == PollTableEntry.MOVE) {
                        moveToFolder = entry.getMoveAfterProcess();
                    }
                    break;

                case PollTableEntry.FAILED:
                    if (entry.getActionAfterFailure() == PollTableEntry.MOVE) {
                        moveToFolder = entry.getMoveAfterFailure();
                    }
                    break;
                case PollTableEntry.NONE:
                    return;
            }

            if (moveToFolder != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Moving processed email to folder :" + moveToFolder);
                }
                Folder dFolder = store.getFolder(moveToFolder);
                if (!dFolder.exists()) {
                    dFolder.create(Folder.HOLDS_MESSAGES);
                }
                folder.copyMessages(new Message[]{message}, dFolder);
            }

            if (log.isDebugEnabled()) {
                log.debug("Deleting email :" + message.getMessageNumber());
            }

            message.setFlag(Flags.Flag.DELETED, true);

        } catch (MessagingException e) {
            log.error("Error deleting or resolving folder to move after processing : "
                + moveToFolder, e);
        }
    }

    @Override
    protected PollTableEntry createEndpoint() {
        return new PollTableEntry(log);
    }

    public void addErrorListener(TransportErrorListener listener) {
        tess.addErrorListener(listener);
    }

    public void removeErrorListener(TransportErrorListener listener) {
        tess.removeErrorListener(listener);
    }

    /**
     * Return the UID of a message from the given folder
     * @param folder the POP3 or IMAP folder
     * @param message the message
     * @return UID as a String (long is converted to a String) or null
     */
    private String getMessageUID(Folder folder, Message message) {
        String uid = null;
        if (folder instanceof UIDFolder) {
            try {
                uid = Long.toString(((UIDFolder) folder).getUID(message));
            } catch (MessagingException ignore) {}
        } else {
            try {
                Method m = folder.getClass().getMethod(
                    "getUID", Message.class);
                Object o = m.invoke(folder, new Object[]{message});
                if (o != null && o instanceof Long) {
                    uid = Long.toString((Long) o);
                } else if (o != null && o instanceof String) {
                    uid = (String) o;
                }
            } catch (Exception ignore) {}
        }
        return uid;
    }
}
