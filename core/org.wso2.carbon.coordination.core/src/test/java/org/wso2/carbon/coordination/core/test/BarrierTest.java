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
import org.wso2.carbon.coordination.core.sync.Barrier;

public class BarrierTest extends BaseTestCase {
	
	private int countE1 = 0;
	
	private int countL1 = 4;
	
	private int countE2 = 0;
	
	public void testDummy() {
	}
	
	public void xtestBarrier1() throws Exception {
		System.out.println("\n*** BARRIER TEST 1 ***");
		this.createThreads(3, 1);
		this.doB1();
	}
	
	public void xtestBarrier2() throws Exception {
		System.out.println("\n*** BARRIER TEST 2 ***");
		this.createThreads(2, 2);
		this.doB2();
	}
	
	private void doB1() throws Exception {
		CoordinationService service = this.getCoordinationService();
		Barrier b1 = service.createBarrier("b1", 4, 10000);
		assertEquals(this.countE1, 0);
		b1.enter();
		this.countE1++;
		Thread.sleep(100);
		this.countL1--;
		b1.leave();
		assertEquals(this.countL1, 0);
		service.close();
	}
	
	private void doB2() throws Exception {
		CoordinationService service = this.getCoordinationService();
		Barrier b2 = service.createBarrier("b2", 3, 10000);
		assertEquals(this.countE2, 0);
		b2.waitOnBarrier();
		this.countE2++;
		service.close();
	}

	@Override
	protected void execute(int i, int state) throws Exception {
		switch (state) {
		case 1:
			this.doB1();
			break;
		case 2:
			this.doB2();
			break;
		}
		
	}
	
}

