/**
 *  Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.util.HashSet;
import java.util.Set;

import org.wso2.carbon.coordination.core.services.CoordinationService;
import org.wso2.carbon.coordination.core.sync.IntegerCounter;

public class IntegerCounterTest extends BaseTestCase {
	
	private Set<Integer> values = new HashSet<Integer>();
	
	public void testCounter1() throws Exception {
		values.clear();
		System.out.println("\n*** INTEGER COUNTER TEST 1 ***");
		this.createThreads(3, 1);
		this.joinThreads();
		assertEquals(values.size(), 9);
		this.cleanup();
	}
	
	private void doIncrement() throws Exception {
		CoordinationService service = this.getCoordinationService();
		IntegerCounter c1 = service.createIntegerCounter("TEST_COUNTER1");
		long start = System.currentTimeMillis();
		int v1 = c1.incrementAndGet();
		int v2 = c1.incrementAndGet();
		int v3 = c1.incrementAndGet();
		long end = System.currentTimeMillis();
		System.out.println("VALUES: " + v1 + ", " + v2 + ", " + v3 + " - TIME PER OPERATION: " + (end - start) / 3 + " ms.");
		assertTrue(v1 < v2 && v2 < v3);
		values.add(v1);
		values.add(v2);
		values.add(v3);
		service.close();
	}
	
	private void cleanup() throws Exception {
		CoordinationService service = this.getCoordinationService();
		IntegerCounter c1 = service.createIntegerCounter("TEST_COUNTER1");
		c1.delete();
	}
	
	@Override
	protected void execute(int i, int state) throws Exception {
		switch (state) {
		case 1:
			doIncrement();
			break;
		}
	}
}
