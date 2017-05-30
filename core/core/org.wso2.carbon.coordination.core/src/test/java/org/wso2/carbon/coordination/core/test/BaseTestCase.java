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

import java.util.ArrayList;
import java.util.List;

import org.wso2.carbon.coordination.common.CoordinationException;
import org.wso2.carbon.coordination.core.CoordinationConfiguration;
import org.wso2.carbon.coordination.core.services.CoordinationService;
import org.wso2.carbon.coordination.core.services.impl.ZKCoordinationService;
import org.wso2.carbon.coordination.core.utils.CoordinationUtils;

import junit.framework.TestCase;

public abstract class BaseTestCase extends TestCase {

	private static boolean started = false;
	
	private List<CoordinationService> serviceList = new ArrayList<CoordinationService>();
	
	private List<Executor> executorList = new ArrayList<BaseTestCase.Executor>();
	
	private static CoordinationConfiguration coordinationClientConfig;
	
	@Override
	protected void setUp() throws Exception {
		if (!started) {
			coordinationClientConfig = CoordinationUtils.loadCoordinationClientConfig(
					"src/test/resources/coordination_client_config.xml");
			started = true;
		}
	}
	
	@Override
	protected void tearDown() throws Exception {
		for (CoordinationService service : this.serviceList) {
			service.close();
		}		
	}
	
	protected void joinThreads() throws Exception {
		for (Executor ex : this.executorList) {
			ex.join();
		}
	}
	
	protected CoordinationService getCoordinationService() throws CoordinationException {
		CoordinationService service = new ZKCoordinationService(coordinationClientConfig);
		this.serviceList.add(service);
		return service;
	}
	
	protected void createThreads(int n, int state) {
		Executor ex;
		for (int i = 0; i < n; i++) {
			ex = new Executor(i, state);
			this.executorList.add(ex);
			ex.start();
		}
	}
		
	protected abstract void execute(int i, int state) throws Exception;

	public class Executor extends Thread {
		
		private int threadIndex;
		
		private int state;
		
		public Executor(int threadIndex, int state) {
			this.threadIndex = threadIndex;
			this.state = state;
		}
		
		public void run() {
			try {
			    execute(this.threadIndex, this.state);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
}
