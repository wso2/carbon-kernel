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

import javax.mail.Session;

public class MailConstants {

    public static final String TRANSPORT_NAME = "mailto";
    public static final String TRANSPORT_PREFIX = "mailto:";
    
    public static final String TEXT_PLAIN = "text/plain";
    public static final String APPLICATION_BINARY = "application/binary";

    public static String DEFAULT_FOLDER = "INBOX";
    public static final String MAIL_POP3 = "pop3";
    public static final String MAIL_IMAP = "imap";

    public static final String TRANSPORT_MAIL_ACTION_AFTER_PROCESS = "transport.mail.ActionAfterProcess";
    public static final String TRANSPORT_MAIL_ACTION_AFTER_FAILURE = "transport.mail.ActionAfterFailure";

    public static final String TRANSPORT_MAIL_MOVE_AFTER_PROCESS = "transport.mail.MoveAfterProcess";
    public static final String TRANSPORT_MAIL_MOVE_AFTER_FAILURE = "transport.mail.MoveAfterFailure";

    public static final String TRANSPORT_MAIL_PROCESS_IN_PARALLEL = "transport.mail.ProcessInParallel";

    public static final String MAX_RETRY_COUNT   = "transport.mail.MaxRetryCount";
    public static final String RECONNECT_TIMEOUT = "transport.mail.ReconnectTimeout";

    public static final int    DEFAULT_MAX_RETRY_COUNT    = 3;
    public static final long   DEFAULT_RECONNECT_TIMEOUT = 30000;

    public static final String TRANSPORT_MAIL_ADDRESS  = "transport.mail.Address";
    
    public static final String TRANSPORT_MAIL_DEBUG = "transport.mail.Debug";
    
    /**
     * Key for the mail store protocol parameter.
     * The mail store protocol identifier is used in calls to {@link Session#getStore()}.
     */
    public static final String TRANSPORT_MAIL_PROTOCOL = "transport.mail.Protocol";

    public static final String TRANSPORT_MAIL_FORMAT = "transport.mail.Format";
    public static final String TRANSPORT_FORMAT_TEXT = "Text";
    public static final String TRANSPORT_FORMAT_MP   = "Multipart";
    public static final String TRANSPORT_FORMAT_ATTACHMENT   = "Attachment";
    public static final String TRANSPORT_FORMAT_ATTACHMENT_FILE   = "AttachmentFile";

    public static final String TRANSPORT_MAIL_FOLDER           = "transport.mail.Folder";
    public static final String TRANSPORT_MAIL_CONTENT_TYPE     = "transport.mail.ContentType";
    public static final String TRANSPORT_MAIL_REPLY_ADDRESS    = "transport.mail.ReplyAddress";

    public static final String TRANSPORT_MAIL_PRESERVE_HEADERS = "transport.mail.PreserveHeaders";
    public static final String TRANSPORT_MAIL_REMOVE_HEADERS   = "transport.mail.RemoveHeaders";

    // POP3 and IMAP properties
    public static final String MAIL_POP3_USERNAME = "mail.pop3.user";
    public static final String MAIL_POP3_PASSWORD = "mail.pop3.password";
    public static final String MAIL_IMAP_USERNAME = "mail.imap.user";
    public static final String MAIL_IMAP_PASSWORD = "mail.imap.password";

    // SMTP properties
    public static final String MAIL_SMTP_FROM     = "mail.smtp.from";
    public static final String MAIL_SMTP_USERNAME = "mail.smtp.user";
    public static final String MAIL_SMTP_PASSWORD = "mail.smtp.password";
    public static final String MAIL_SMTP_BCC      = "transport.mail.SMTPBccAddresses";

    // transport / mail headers
    public static final String MAIL_HEADER_TO          = "To";
    public static final String MAIL_HEADER_FROM        = "From";
    public static final String MAIL_HEADER_CC          = "Cc";
    public static final String MAIL_HEADER_BCC         = "Bcc";
    public static final String MAIL_HEADER_REPLY_TO    = "Reply-To";
    public static final String MAIL_HEADER_IN_REPLY_TO = "In-Reply-To";
    public static final String MAIL_HEADER_SUBJECT     = "Subject";
    public static final String MAIL_HEADER_MESSAGE_ID  = "Message-ID";
    public static final String MAIL_HEADER_REFERENCES  = "References";

    // Custom headers
    /** @see org.apache.axis2.transport.mail.WSMimeMessage */
    public static final String MAIL_HEADER_X_MESSAGE_ID= "X-Message-ID";
    public static final String TRANSPORT_MAIL_CUSTOM_HEADERS     = "transport.mail.custom.headers";
    
}
