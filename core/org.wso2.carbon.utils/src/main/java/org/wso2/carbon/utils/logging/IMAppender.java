/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

/*
 * $Id: IMAppender.java,v 1.2 2003/07/02 22:00:08 luque Exp $
 *
 * Copyright (c) 2003. Orange Software S.L. All Rights Reserved.
 * Authored by Rafael Luque & Ruth Zamorano
 *
 * You may study, use, modify, and distribute this software for any
 * purpose provided that this copyright notice appears in all copies.
 *
 * This software is provided WITHOUT WARRANTY either expressed or
 * implied.
 *
 */

package org.wso2.carbon.utils.logging;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.helpers.CyclicBuffer;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.TriggeringEventEvaluator;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;


/**
 * IMAppender appends tracing requests through instant
 * messaging network.
 *
 * @author Rafael Luque & Ruth Zamorano
 * @version $Revision: 1.2 $
 */

public class IMAppender extends AppenderSkeleton {

    // ----------------------------------------------- Variables

    private static final int BUFFER_SIZE = 16;
	private static final int PORT = 5222;
	private String host;
    private int port = PORT;
    private String username;
    private String password;
    private String recipient;
    private boolean chatroom = false;
    private String nickname;
    private boolean enableSSL = false;
    private int bufferSize = BUFFER_SIZE;

    private TriggeringEventEvaluator evaluator;
    private CyclicBuffer cb;
    private XMPPConnection con;
    private Chat chat;
    private MultiUserChat groupchat;

    // -------------------------------------------- Constructors

    /**
     * The default constructor will instantiate the appender with a
     * default TriggeringEventEvaluator that will trigger on events
     * with level ERROR or higher.
     */
    public IMAppender() {
        this(new DefaultEvaluator());
    }

    public IMAppender(TriggeringEventEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    // ------------------------------- Setter and getter methods

    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRecipient() {
        return this.recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public boolean isChatroom() {
        return this.chatroom;
    }

    public void setChatroom(boolean chatroom) {
        this.chatroom = chatroom;
    }

    public String getNickname() {
        return this.nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public boolean isSSL() {
        return this.enableSSL;
    }

    public void setSSL(boolean enableSSL) {
        this.enableSSL = enableSSL;
    }

    public int getBufferSize() {
        return this.bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    /**
     * The <b>EvaluatorClass</b> option takes a string value
     * representing the name of the class implementing the {@link
     * TriggeringEventEvaluator} interface. A corresponding object will
     * be instantiated and assigned as the triggering event evaluator
     * for the SMTPAppender.
     */
    public void setEvaluatorClass(String value) {
        evaluator = (TriggeringEventEvaluator)
                OptionConverter.instantiateByClassName(value,
                        TriggeringEventEvaluator.class, evaluator);
    }

    public String getEvaluatorClass() {
        return evaluator == null ? null : evaluator.getClass().getName();
    }

    // ---------------------------------- Log4j callback methods

    /**
     * Options are activated and become effective only after calling
     * this method.
     */
    public void activateOptions() {
        try {
            cb = new CyclicBuffer(bufferSize);

            // Create a connection to the XMPP server
            LogLog.debug("Stablishing connection with XMPP server");
            ConnectionConfiguration xmppConf = new ConnectionConfiguration(host, port);
            if (enableSSL) {
            	xmppConf.setSecurityMode(ConnectionConfiguration.SecurityMode.enabled);
            }else {
            	xmppConf.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
            }
            con = new XMPPConnection(xmppConf);

            // Most servers require you to login before performing other tasks
            LogLog.debug("About to login as [" + username + "/" + password + "]");
            con.login(username, password);

            // Start a conversation with IMAddress
            if (chatroom) {
                LogLog.debug("About to create ChatGroup");
                groupchat = new MultiUserChat(con, recipient);
                LogLog.debug("About to join room");
                groupchat.join(nickname != null ? nickname : username);
            } else {
                chat = con.getChatManager().createChat(recipient, new MessageListener(){
                	public void processMessage(Chat chat, Message message) {
                		//do nothing 
                	}
                });
            }

        } catch (Exception e) {
            errorHandler.error("Error while activating options for appender named [" +
                    name + "]", e, ErrorCode.GENERIC_FAILURE);
        }
    }


    /**
     * Close this IMAppender. Closing all resources used by the
     * appender. A closed appender cannot be re-opened.
     */
    public synchronized void close() {
        if (this.closed) {
            return;
        }

        LogLog.debug("Closing appender [" + name + "]");
        this.closed = true;

        // Closes the connection by setting presence to unavailable
        // then closing the stream to the XMPP server.
        if (con != null) {
            con.disconnect();
        }
        
        // Help GC
        con = null;
        chat = null;
        groupchat = null;
    }


    /**
     * This method called by {@link AppenderSkeleton#doAppend} method
     * does most of the real appending work. Adds the event to a buffer
     * and checks if the event triggers a message to be sent.
     */
    public void append(LoggingEvent event) {

        // check pre-conditions
        if (!checkEntryConditions()) {
            return;
        }

        cb.add(event);
        if (evaluator.isTriggeringEvent(event)) {
            sendBuffer();
        }
    }


    /**
     * Send the contents of the cyclic buffer as an IM message.
     */
    protected void sendBuffer() {
        try {
            StringBuffer buf = new StringBuffer();

            int len = cb.length();
            for (int i = 0; i < len; i++) {
                LoggingEvent event = cb.get();
                buf.append(layout.format(event));
                // if layout doesn't handle exception, the appender has to do it
                if (layout.ignoresThrowable()) {
                    String[] s = event.getThrowableStrRep();
                    if (s != null) {
                        for (int j = 0; j < s.length; j++) {
                            buf.append(Layout.LINE_SEP);
                            buf.append(s[j]);
                        }
                    }
                }
            }

            if (chatroom) {
                groupchat.sendMessage(buf.toString());
            } else {
                chat.sendMessage(buf.toString());
            }

        } catch (Exception e) {
            errorHandler.error("Could not send message in IMAppender [" +
                    name + "]", e, ErrorCode.GENERIC_FAILURE);
        }
    }


    /**
     * This method determines if there is a sense in attempting to append.
     * <p/>
     * <p>It checks whether there is an output chat available  and also if
     * there is a set layout. If these checks fail, then the boolean
     * value <code>false</code> is returned.
     */
    protected boolean checkEntryConditions() {
        if ((this.chat == null) && (this.groupchat == null)) {
            errorHandler.error("Chat object not configured");
            return false;
        }

        if (this.layout == null) {
            errorHandler.error("No layout set for appender named [" + name + "]");
            return false;
        }
        return true;
    }


    /**
     * The IMAppender requires a layout. Hence, this method returns
     * <code>true</code>.
     */
    public boolean requiresLayout() {
        return true;
    }
}
