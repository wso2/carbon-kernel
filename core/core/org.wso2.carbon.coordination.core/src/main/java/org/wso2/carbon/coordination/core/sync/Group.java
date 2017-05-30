/**
 *  Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.coordination.core.sync;

import java.util.List;

import org.wso2.carbon.coordination.common.CoordinationException;

/**
 * This interface represents a node group, which can be used for group communication, leader election.
 */
public interface Group {
	
	/**
	 * Returns the group leader's id.
	 * @return The group leader id
	 * @throws CoordinationException
	 */
	public String getLeaderId() throws CoordinationException;
	
	/**
	 * Returns my member id.
	 * @return Current node's member id
	 * @throws CoordinationException
	 */
	public String getMemberId();
	
	/**
	 * Returns ids of all the members in the group.
	 * @return Group member ids.
     * @throws CoordinationException 
	 */
	public List<String> getMemberIds() throws CoordinationException;
	
	/**
	 * Returns the current group id.
	 * @return group id
	 */
	public String getGroupId();
	
	/**
	 * Broadcasts a message to all the members in the group.
	 * @param data The data to be broadcasted
	 * @throws CoordinationException
	 */
	public void broadcast(byte[] data) throws CoordinationException;
	
	/**
	 * Send and receives message to/from a peer in the group.
	 * @param memberId The member id of the peer the call should be made to
	 * @param data The data to be sent to the peer
	 * @throws CoordinationException If a communication error or a processing error
	 * occurs in the target 
	 */
	public byte[] sendReceive(String memberId, byte[] data) throws CoordinationException;
	
	/**
	 * Leave the group.
	 * @throws CoordinationException
	 */
	public void leave() throws CoordinationException;
	
	/**
	 * Set a group event listener to this group, to receive group notifications.
	 * @param listener The GroupEventListener implementation
	 * @see GroupEventListener
	 */
	public void setGroupEventListener(GroupEventListener listener);
	
	/**
	 * Returns the registered group event listener.
	 * @return group event listener
	 */
	public GroupEventListener getGroupEventListener();
	
	/**
	 * Clears all group messages that are stored.
	 * @throws CoordinationException 
	 */
	public void clearGroupMessages() throws CoordinationException;
	
	/**
	 * Waits for a specific number of members are arrived in the group.
	 * @param count The number of members
	 * @throws CoordinationException
	 */
	public void waitForMemberCount(int count) throws CoordinationException;
	
}
