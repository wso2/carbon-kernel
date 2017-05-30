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
import org.wso2.carbon.coordination.core.sync.Queue;

public class QueueTest extends BaseTestCase {

	private int testSum;
	
	private Object lock = new Object();
	
	public void testQueue1() throws Exception {
		System.out.println("\n*** QUEUE TEST 1 ***");
		this.testSum = 0;
		CoordinationService service = this.getCoordinationService();
		Queue q1 = service.createQueue("q1", 10000);
		this.createThreads(3, 1);
		Thread.sleep(4000);
		int tmp;
		for (int i = 0; i < 100; i++) {
			tmp = (int) (Math.random() * 100);
			synchronized (this.lock) {
				this.testSum += tmp;
			}
			q1.enqueue(("" + tmp).getBytes());
		}
		Thread.sleep(2000);
		assertEquals(this.testSum, 0);
		service.close();
	}
	
	public void testQueue2() throws Exception {
		System.out.println("\n*** QUEUE TEST 2 ***");
		Queue q2 = this.getCoordinationService().createQueue("q2", 10000);
		this.createThreads(1, 2);
		Thread.sleep(3000);
		assertEquals("A", new String(q2.blockingDequeue()));
		assertEquals("B", new String(q2.blockingDequeue()));
		assertEquals("C", new String(q2.blockingDequeue()));
	}
	
	@Override
	protected void execute(int i, int state) throws Exception {
		switch (state) {
		case 1:
			Queue q1 = this.getCoordinationService().createQueue("q1", -1);
			byte[] data;
			while (true) {
				data = q1.blockingDequeue();
				synchronized (this.lock) {
				    this.testSum -= Integer.parseInt(new String(data));
				}
			}
		case 2:
			CoordinationService service = this.getCoordinationService();
			Queue q2 = service.createQueue("q2", 10000);
			q2.enqueue(new String("C").getBytes(), 3);
			q2.enqueue(new String("A").getBytes(), 1);
			q2.enqueue(new String("B").getBytes(), 2);
			service.close();
			break;
		}
	}

}
