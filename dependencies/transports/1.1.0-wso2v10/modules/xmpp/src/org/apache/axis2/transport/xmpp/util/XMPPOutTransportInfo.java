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

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.transport.OutTransportInfo;

/**
 * 
 * Holds XMPP transport out details
 *
 */
public class XMPPOutTransportInfo implements OutTransportInfo{
	private String contentType = null;
	private String destinationAccount = null;
	private String inReplyTo;
	private EndpointReference from;
	private XMPPConnectionFactory connectionFactory = null;
	private String sequenceID; 
	
	public XMPPOutTransportInfo(){
		
	}
	
	public XMPPOutTransportInfo(String transportUrl) throws AxisFault {
        this.destinationAccount = XMPPUtils.getAccountName(transportUrl);
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}		
		
    public XMPPConnectionFactory getConnectionFactory() {
		return connectionFactory;
	}

    public void setConnectionFactory(XMPPConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}
	
	public String getDestinationAccount() {
		return destinationAccount;
	}

	public EndpointReference getFrom() {
		return from;
	}

	public void setFrom(EndpointReference from) {
		this.from = from;
	}

	public String getInReplyTo() {
		return inReplyTo;
	}

	public void setInReplyTo(String inReplyTo) {
		this.inReplyTo = inReplyTo;
	}

	public void setDestinationAccount(String destinationAccount) {
		this.destinationAccount = destinationAccount;
	}

	public String getContentType() {
		return contentType;
	}

    public String getSequenceID() {
        return sequenceID;
    }

    public void setSequenceID(String sequenceID) {
        this.sequenceID = sequenceID;
    }	
	
}
