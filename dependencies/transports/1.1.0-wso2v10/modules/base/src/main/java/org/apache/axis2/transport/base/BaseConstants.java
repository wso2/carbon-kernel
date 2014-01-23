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

package org.apache.axis2.transport.base;


import javax.xml.namespace.QName;

public class BaseConstants {
    // -- status of a transport --
    public final static int STOPPED = 0;
    public final static int STARTED = 1;
    public final static int PAUSED  = 2;

    /**
     * A message property specifying the SOAP Action
     */
    public static final String SOAPACTION = "SOAPAction";
    /**
     * A message property specifying the content type
     */
    public static final String CONTENT_TYPE = "Content-Type";
    /** 
     * A message context property indicating "TRUE", if a transport or the message builder
     * has information that the current message is a fault (e.g. SOAP faults, non-HTTP 2xx, etc)
     */
    public static final String FAULT_MESSAGE = "FAULT_MESSAGE";
    /**
     * content type identifier for multipart / MTOM messages
     */
    public static final String MULTIPART_RELATED = "multipart/related";
    /**
     * character set marker to identify charset from a Content-Type string
     */
    public static final String CHARSET_PARAM = "; charset=";
    /**
     * The property specifying an optional message level metrics collector
     */
    public static final String METRICS_COLLECTOR = "METRICS_COLLECTOR";    

    //------------------------------------ defaults ------------------------------------
    /**
     * The default operation name to be used for non SOAP/XML messages
     * if the operation cannot be determined
     */
    public static final QName DEFAULT_OPERATION = new QName("urn:mediate");
    /**
     * The name of the element which wraps binary content into a SOAP envelope
     */

    // This has to match org.apache.synapse.util.PayloadHelper
    // at some future point this can be merged into Axiom as a common base
    public final static String AXIOMPAYLOADNS = "http://ws.apache.org/commons/ns/payload";

   
    public static final QName DEFAULT_BINARY_WRAPPER =
            new QName(AXIOMPAYLOADNS, "binary");
    /**
     * The name of the element which wraps plain text content into a SOAP envelope
     */
    public static final QName DEFAULT_TEXT_WRAPPER =
            new QName(AXIOMPAYLOADNS, "text");

    //-------------------------- services.xml parameters --------------------------------
    /**
     * The Parameter name indicating the operation to dispatch non SOAP/XML messages
     */
    public static final String OPERATION_PARAM = "Operation";
    /**
     * The Parameter name indicating the wrapper element for non SOAP/XML messages
     */
    public static final String WRAPPER_PARAM = "Wrapper";
    /**
     * the parameter in the services.xml that specifies the poll interval for a service
     */
    public static final String TRANSPORT_POLL_INTERVAL = "transport.PollInterval";
    /**
     * Could polling take place in parallel, i.e. starting at fixed intervals?
     */
    public static final String TRANSPORT_POLL_IN_PARALLEL = "transport.ConcurrentPollingAllowed";
    /**
     * The default poll interval in milliseconds.
     */
    public static final int DEFAULT_POLL_INTERVAL = 5 * 60 * 1000; // 5 mins by default

    public static final String CALLBACK_TABLE = "callbackTable";
    public static final String HEADER_IN_REPLY_TO = "In-Reply-To";

    // this is an property required by axis2
    // FIXME: where is this required in Axis2?
    public final static String MAIL_CONTENT_TYPE = "mail.contenttype";

    /** Service transaction level - non-transactional */
    public static final int TRANSACTION_NONE  = 0;
    /** Service transaction level - use non-JTA (i.e. local) transactions */
    public static final int TRANSACTION_LOCAL = 1;
    /** Service transaction level - use JTA transactions */
    public static final int TRANSACTION_JTA   = 2;
    /** Service transaction level - non-transactional */
    public static final String STR_TRANSACTION_NONE  = "none";
    /** Service transaction level - use non-JTA (i.e. local) transactions */
    public static final String STR_TRANSACTION_LOCAL = "local";
    /** Service transaction level - use JTA transactions */
    public static final String STR_TRANSACTION_JTA   = "jta";

    /** The Parameter name indicating the transactionality of a service */
    public static final String PARAM_TRANSACTIONALITY = "transport.Transactionality";
    /** Parameter name indicating the JNDI name to get a UserTransaction from JNDI */
    public static final String PARAM_USER_TXN_JNDI_NAME = "transport.UserTxnJNDIName";
    /** Parameter that indicates if a UserTransaction reference could be cached - default yes */
    public static final String PARAM_CACHE_USER_TXN = "transport.CacheUserTxn";

    /** The UserTransaction associated with this message */
    public static final String USER_TRANSACTION = "UserTransaction";
    /** A message level property indicating a request to rollback the transaction associated with the message */
    public static final String SET_ROLLBACK_ONLY = "SET_ROLLBACK_ONLY";
    /** A message level property indicating a commit is required after the next immidiate send over a transport */
    public static final String JTA_COMMIT_AFTER_SEND = "JTA_COMMIT_AFTER_SEND";    
}
