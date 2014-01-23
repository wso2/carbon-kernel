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

package org.apache.axis2.transport.xmpp.util;


public class XMPPConstants {	
	public static final String XMPP = "xmpp";
    //The prefix indicating an Axis XMPP URL
    public static final String XMPP_PREFIX = "xmpp://";

    //properties related to XMPP server connection
    public static final String XMPP_SERVER_USERNAME = "transport.xmpp.ServerAccountUserName";
    public static final String XMPP_SERVER_PASSWORD = "transport.xmpp.ServerAccountPassword";    
    public static final String XMPP_SERVER_URL = "transport.xmpp.ServerUrl";
    public static final String XMPP_DOMAIN_NAME = "transport.xmpp.domain";
    
    //Google talk attributes
    public static final String GOOGLETALK_URL = "talk.google.com";
    public static final int GOOGLETALK_PORT = 5222;
    public static final String GOOGLETALK_SERVICE_NAME = "gmail.com";
    public static final String GOOGLETALK_FROM = "gmail.com";
    
    
    //XMPP Server Types
    public static final String XMPP_SERVER_TYPE = "transport.xmpp.ServerType";
    public static final String XMPP_SERVER_TYPE_JABBER = "transport.xmpp.ServerType.Jabber";
    public static final String XMPP_SERVER_TYPE_GOOGLETALK = "transport.xmpp.ServerType.GoogleTalk";   
    
    public static final String IS_SERVER_SIDE = "isServerSide";
    public static final String IN_REPLY_TO = "inReplyTo";
    public static final String SERVICE_NAME = "ServiceName";
    public static final String ACTION = "Action";
    public static final String CONTENT_TYPE = "ContentType";
    //This is set to true, if a request message is sent through XMPPSender
    //Used to distinguish messages coming from chat clients.
    public static final String CONTAINS_SOAP_ENVELOPE = "transport.xmpp.containsSOAPEnvelope";
    public static final String MESSAGE_FROM_CHAT = "transport.xmpp.message.from.chat";
    public static final String SEQUENCE_ID = "transport.xmpp.sequenceID";  
    
    public static final String XMPP_CONTENT_TYPE_STRING = "xmpp/text";
}
