/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.axis2.transport.msmq;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.ParameterInclude;

/**
 * MSMQ Connection manager which invokes the listerner host parameters and
 * register for the during the MSMQ context initialization
 * 
 * 
 */
public class MSMQConnectionManager {

	public enum ConnectionType {
		SENDER, RECIVER
	}

	private static boolean receiverInit = false;

	private static String receiverHost;

	/*<!-- MSMQ Configuration  -->
    <transportSender name="msmq" class="org.apache.axis2.transport.msmq.MSMQSender"/>
    <transportReceiver name="msmq" class="org.apache.axis2.transport.msmq.MSMQListener">
         <parameter name="msmq.receiver.host" locked="false">localhost</parameter>
    </transportReceiver> */
	/**
	 * Initialize the receiver host information which is defined in the axis2.xml as properties
	 * 
	 * @param trpInDesc
	 * @param connectionType
	 * @throws AxisFault
	 */
	public static void init(ParameterInclude trpInDesc, ConnectionType connectionType) throws AxisFault {
		if (!receiverInit && connectionType.equals(ConnectionType.RECIVER)) {
			// extracting message...
			for (Parameter parameter : trpInDesc.getParameters()) {
				if (parameter.getName().equalsIgnoreCase(MSMQConstants.MSMQ_RECEIVER_HOST)) {
					receiverHost = (String) parameter.getValue();
				} 
			}
			receiverInit = true;
		} 

	}


	/**
	 * Generating full name (standard MSMQ full path which is listen to the MSMQ service)
	 * 
	 * @param queueName
	 * @return
	 */
	public static String getReceiverQueueFullName(String queueName) {
		String h1 = receiverHost;
		String a1 = "OS";
		if ((h1 == null) || h1.equals(""))
			h1 = ".";
		char[] c = h1.toCharArray();
		if ((c[0] >= '1') && (c[0] <= '9'))
			a1 = "TCP";

		return "DIRECT=" + a1 + ":" + h1 + "\\private$\\" + queueName;
	}


	public static String getReceiverHost() {
		return receiverHost;
	}

}
