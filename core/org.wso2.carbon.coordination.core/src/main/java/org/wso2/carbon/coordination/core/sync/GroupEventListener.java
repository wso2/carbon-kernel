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

import org.wso2.carbon.coordination.common.CoordinationException;

/**
 * This interface represents an event listener for a group.
 * @see Group
 */
public interface GroupEventListener {

	public void onLeaderChange(String newLeaderId);
	
	public void onMemberArrival(String newMemberId);
	
	public void onMemberDeparture(String oldMemberId);
	
	public void onGroupMessage(byte[] data);
	
	public byte[] onPeerMessage(byte[] data) throws CoordinationException;
	
}
