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
package org.wso2.carbon.coordination.core.test;

import org.wso2.carbon.coordination.core.services.CoordinationService;
import org.wso2.carbon.coordination.core.sync.Group;
import org.wso2.carbon.coordination.core.sync.GroupEventListener;

public class GroupTest extends BaseTestCase implements GroupEventListener {

	private String leaderId;
	
	private int groupCount;
	
	public void testGroup1() throws Exception {
		System.out.println("\n*** GROUP TEST 1 ***");
		CoordinationService service = this.getCoordinationService();
		Group g1 = service.createGroup("g1");
		assertEquals(g1.getMemberId(), g1.getLeaderId());
		this.createThreads(1, 1);
		Thread.sleep(2000);
		service.close();
		Thread.sleep(2000);
		assertNotNull(this.leaderId);
		assertNotSame(g1.getMemberId(), this.leaderId);
	}
	
	public void testGroup2() throws Exception {
		System.out.println("\n*** GROUP TEST 2 ***");
		CoordinationService service = this.getCoordinationService();
		Group g1 = service.createGroup("g2");
		this.groupCount = 0;
		this.createThreads(3, 2);
		Thread.sleep(3000);
		g1.broadcast(new String("2").getBytes());
		Thread.sleep(3000);
		assertEquals(this.groupCount, 6);
		service.close();
	}
	
	@Override
	protected void execute(int i, int state) throws Exception {
		switch (state) {
		case 1:
			Group g1 = this.getCoordinationService().createGroup("g1");
			g1.setGroupEventListener(this);
			break;
		case 2:
			Group g2 = this.getCoordinationService().createGroup("g2");
			g2.setGroupEventListener(this);
			break;
		}
	}

	@Override
	public void onLeaderChange(String newLeaderId) {
		this.leaderId = newLeaderId;
	}

	@Override
	public synchronized void onGroupMessage(byte[] data) {
		this.groupCount += Integer.parseInt(new String(data));
	}

	@Override
	public byte[] onPeerMessage(byte[] data) {
		return null;
	}

	@Override
	public void onMemberArrival(String newMemberId) {
		System.out.println("Member Arrived: " + newMemberId);
	}

	@Override
	public void onMemberDeparture(String oldMemberId) {
		System.out.println("Member Departed: " + oldMemberId);
	}
	
}
