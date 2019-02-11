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
import org.wso2.carbon.coordination.core.sync.Lock;

public class LockTest extends BaseTestCase {

	private boolean flag; 
	
	public void testLock1() throws Exception {
		System.out.println("\n*** LOCK TEST 1 ***");
		this.flag = false;
		this.createThreads(3, 1);
		Thread.sleep(500);
		this.doLock1();
		this.joinThreads();
	}
	
	private void doLock1() throws Exception {
		CoordinationService service = this.getCoordinationService();
		Lock l1 = service.createLock("l1", 10000);
		l1.acquire();
		assertFalse(this.flag);
		this.flag = true;
		Thread.sleep(1000);
		this.flag = false;
		l1.release();
		service.close();
	}
	
	@Override
	protected void execute(int i, int state) throws Exception {
		switch (state) {
		case 1:
			doLock1();
			break;
		}
	}
	
}
