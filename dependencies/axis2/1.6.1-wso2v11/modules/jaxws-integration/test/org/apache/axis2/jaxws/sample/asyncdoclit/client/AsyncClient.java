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
package org.apache.axis2.jaxws.sample.asyncdoclit.client;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import javax.xml.ws.BindingProvider;

public class AsyncClient {

    private static final String DOCLITWR_ASYNC_ENDPOINT =
        "http://localhost:6060/axis2/services/AsyncService2.DocLitWrappedPortImplPort";

	private static final int max_isasleep_check = 30;
	
	/**
	 * Auxiliary method used for doiing isAsleep checks. Will perform isAsleep
	 * up to a MAX_ISASLEEP_CHECK number of checks. Will sleep for
	 * SLEEP_ISASLEEP_SEC seconds in between requests. If reaches maximum number
	 * fo retries then will fail the test
	 */
	public static boolean isAsleepCheck(String MESSAGE, AsyncPort port) throws Exception {
		boolean asleep = false;
		int check = 5; //Constants.MAX_ISASLEEP_CHECK;
		String msg = null;

		final long start = System.currentTimeMillis();
		
		System.out.println("AsyncClient.isAsleepCheck(" + MESSAGE + ") Enter");

		do {
			try {
				msg = port.isAsleep();
			} catch (Exception e){
				System.out.println("AsyncClient.isAsleepCheck Exception on isAsleep:" + e);
				throw e;
			}
			
			asleep = (msg != null);

			// fail the test if we ran out of checks
			if ((check--) == 0) {
				System.out.println("AsyncClient.isAsleepCheck=" + asleep
						+ " after " + (/*Constants.MAX_ISASLEEP_CHECK*/ max_isasleep_check - check)
						+ " tries");
				throw new RuntimeException("Server did not receive sleep after several retries");
			}

			// sleep for a bit
			try {
				Thread.sleep(/*Constants.SLEEP_ISASLEEP_SEC*/ 1 * 1000);
			} catch (InterruptedException e) {
				System.out.println("AsyncClient.isAsleepCheck (ignored error) "
						+ e);
			}

		} while (!asleep);

		System.out.println("AsyncClient.isAsleepCheck() asleep=" + asleep + " after "
				+ (/*Constants.MAX_ISASLEEP_CHECK*/ max_isasleep_check - check) + " tries");

		if (asleep) {
			System.out.println("AsyncClient.isAsleepCheck sleeping on:" + msg);
			if (!MESSAGE.equals(msg)) {
				throw new RuntimeException("Sleeping on an incorrect message");
			}
		}

		long mins = (System.currentTimeMillis() - start) / 1000;
		System.out.println("AsyncClient.isAsleepCheck() Exit, time=" + mins + "min");

		return true;
	}

	/**
	 * Auxiliary method used for obtaining a proxy pre-configured with a
	 * specific Executor
	 */
	public static AsyncPort getPort(Executor ex) {
		AsyncService service = new AsyncService();

		if (ex != null) service.setExecutor(ex);

		AsyncPort port = service.getAsyncPort();
		if (port == null) {
			throw new RuntimeException("service.getAsyncPort() is null");
		}

		Map<String, Object> rc = ((BindingProvider) port).getRequestContext();
		rc.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
				DOCLITWR_ASYNC_ENDPOINT); //Constants.DOCLITWR_ASYNC_ENDPOINT);

		System.out.println("AsyncClient.getPort() = "
				+ DOCLITWR_ASYNC_ENDPOINT); //Constants.DOCLITWR_ASYNC_ENDPOINT);

		return port;
	}

	/**
	 * Auxiliary method used to wait for a monitor for a certain amount of time
	 * before timing out
	 * 
	 * @param monitor
	 */
	public static void waitBlocking(Future<?> monitor) throws Exception {

		System.out.println("AsyncClient.waitBlocking() Enter");

		// wait for request to complete
		int sec = /*Constants.CLIENT_MAX_SLEEP_SEC*/ max_isasleep_check;
		while (!monitor.isDone() && !monitor.isCancelled()) {
			Thread.sleep(1000);
			sec--;
			if (sec <= 0) break;
		}

		if (sec <= 0) {
			System.out.println("AsyncClient.waitBlocking Exit, timeout after"
					+ /*Constants.CLIENT_MAX_SLEEP_SEC*/ max_isasleep_check + " sec");
			
			throw new TimeoutException(
					"Stopped waiting for Async response after "
							+ /*Constants.CLIENT_MAX_SLEEP_SEC*/ max_isasleep_check + " sec");
		} else {
			System.out.println("AsyncClient.waitBlocking Exit, " + sec
					+ "sec remaining");
		}
	}
}
