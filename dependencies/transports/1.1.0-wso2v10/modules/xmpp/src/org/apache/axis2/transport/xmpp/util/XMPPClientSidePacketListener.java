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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.axis2.context.MessageContext;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

public class XMPPClientSidePacketListener implements PacketListener {
	private static Log log = LogFactory.getLog(XMPPClientSidePacketListener.class);
	private MessageContext messageContext = null;
	private boolean responseReceived;

	public XMPPClientSidePacketListener(MessageContext messageContext){
		this.messageContext = messageContext;
	}



    /**
	 * This method will be triggered, when a message is arrived at client side
	 */
	public void processPacket(Packet packet) {		
		Message message = (Message)packet;
		String xml = StringEscapeUtils.unescapeXml(message.getBody());
		log.info("Client received message : "+xml);
		this.responseReceived = true;
		InputStream inputStream = new ByteArrayInputStream(xml.getBytes());
		messageContext.setProperty(MessageContext.TRANSPORT_IN, inputStream);
	}

	/**
	 * Indicates response message is received at client side.
	 * @see processPacket(Packet packet)
	 * @return
	 */
	public boolean isResponseReceived() {
		return responseReceived;
	}

}
