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

package org.apache.axis2.jaxws.sample.asyncdoclit.server;

import javax.jws.WebService;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceException;

import org.test.asyncdoclit.ExceptionTypeEnum;
import org.test.asyncdoclit.ThrowExceptionFaultBean;

/*
import jaxws.async.wsfvt.common.Constants;
import jaxws.async.wsfvt.common.doclitwr.AsyncPort;
import jaxws.async.wsfvt.common.doclitwr.ExceptionTypeEnum;
import jaxws.async.wsfvt.common.doclitwr.ThrowExceptionFault;
import jaxws.async.wsfvt.common.doclitwr.ThrowExceptionFaultBean;
*/
/**
 * Async endpoint used for Async client side tests. Clients will invokeAsync
 * sleep method to force the server to block until wakeUp is called. The client
 * can call isAsleep to verify that sleep has been called by the async thread.
 */
@WebService(serviceName="AsyncService2",
			endpointInterface="org.apache.axis2.jaxws.sample.asyncdoclit.server.AsyncPort")
public class DocLitWrappedPortImpl implements AsyncPort {

	private static final boolean DEBUG = true;

	// message on which the sleep method is sleeping on
	private static String msg = "";

	// thread instance that is currently sleeping so it can be interrupted from
	// wakeup
	private static Thread sleeper = null;

	// interrupt flag for wakeUp to set
	private static boolean doCancell = false;

	public void sleep(Holder<String> message) {

	try {
		if (message == null || message.value == null) {
			System.out.println("DocLitWrappedPortImpl.sleep(null) Enter");
		}
		
		if (DEBUG) System.out.println("DocLitWrappedPortImpl.sleep(" + message.value + ") Enter");

		boolean cancelRequested = false;

		msg = message.value;

		synchronized (msg) {

			Thread myThread = Thread.currentThread();
			sleeper = myThread;

			try {

				doCancell = false;

				if (DEBUG)
					System.out.println("DocLitWrappedPortImpl.sleep: going to sleep on "
							+ myThread.getId() + " " + msg);

				// wait until either a timeout or client releases us
				// or if another request begins (lockId changes)
				long sec = 15; //Constants.SERVER_SLEEP_SEC;

				while (sec > 0 && !doCancell && sleeper == myThread) {
					if (DEBUG)
						System.out.println("DocLitWrappedPortImpl.sleep: " + myThread.getId()
								+ " timeLeft=" + sec);
					sec--;

					msg.wait(1000);
				}
			} catch (InterruptedException e) {
				System.out.println("DocLitWrappedPortImpl.sleep: interrupted on " + myThread.getId());
			} finally {

				if (DEBUG)
					System.out.println("DocLitWrappedPortImpl.sleep: WokeUp " + myThread.getId());

				// if we timed out while waiting then
				// release the wait
				if (sleeper == myThread) {
					cancelRequested = doCancell;
					doCancell = false;
					sleeper = null;
				}

				// only notify if cancel was requested
				if (cancelRequested) {
					if (DEBUG)
						System.out.println("DocLitWrappedPortImpl.sleep: Notify " + myThread.getId()
								+ " isDone");

					// wake up the release thread
					msg.notify();
				}
			}
		}// synch

		msg = null;

		if (DEBUG) System.out.println("DocLitWrappedPortImpl.sleep(" + message.value + ") Exit");
		} catch (Exception e){
			System.out.println("DocLitWrappedPortImpl.sleep: " + e);
			e.printStackTrace(System.out);
		}
	}

	public String isAsleep() {
		if (DEBUG) System.out.println("DocLitWrappedPortImpl.isAsleep() Enter");
		
		String ret = (sleeper != null) ? msg : null;
		
		if (DEBUG) System.out.println("DocLitWrappedPortImpl.isAsleep() = " + ret);
		
		return ret;
	}

	public String wakeUp() {

		if (DEBUG) System.out.println("DocLitWrappedPortImpl.wakeUp() Enter");

		String wakeUp = null;

		if (sleeper == null) {
			if (DEBUG) System.out.println("DocLitWrappedPortImpl.wakeUp: No one to wake up");
		} else {
			if (DEBUG) System.out.println("DocLitWrappedPortImpl.wakeUp: Interrupting " + sleeper.getId());

			// interrupt the sleeper & set inteerupt flag in case
			// the sleep isn't sleeping but actually doing work
			wakeUp = msg;
			sleeper.interrupt();
			doCancell = true;
			
			if (DEBUG)
				System.out.println("DocLitWrappedPortImpl.wakeUp: about to enter sync block "
						+ System.currentTimeMillis());

			// block until sleep completes
			if (msg != null) {
				synchronized (msg) {

					if (DEBUG)
						System.out.println("DocLitWrappedPortImpl.wakeUp: enter sync block "
								+ System.currentTimeMillis());

					doCancell = false;
				}
			}

			msg = null;
		}

		if (DEBUG) System.out.println("DocLitWrappedPortImpl.wakeUp() = " + wakeUp);

		return wakeUp;
	}

	/**
	 * client side tests for remapping operation names, on the server side all
	 * we need to do is roundtrip the message
	 */

	public String invokeAsync(String request) {
		System.out.println("DocLitWrappedPortImpl.invokeAsync " + request);
		return request;
	}

	public String customAsync(String request) {
		System.out.println("DocLitWrappedPortImpl.customAsync " + request);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return request;
	}

	public String another(String request) {
		System.out.println("DocLitWrappedPortImpl.another " + request);
		return request;
	}

	public String ping(String message) {
		System.out.println("DocLitWrappedPortImpl.ping " + message);
		return message;
	}

	public String throwException(ExceptionTypeEnum exceptionType)
			throws ThrowExceptionFault {
		System.out.println("DocLitWrappedPortImpl.throwException " + exceptionType);
		
		switch (exceptionType) {
		case DIVIDE_BY_ZERO:
			int div = 10 / 0;
			break;
		case NPE:
			String x = null;
			x.toString();
			break;
		case WSE:
			throw new WebServiceException("WebServiceFault");
		case WSDL_FAULT:
			ThrowExceptionFaultBean tefb = new ThrowExceptionFaultBean();
			tefb.setText("faultBean");
			throw new ThrowExceptionFault("message", tefb);
		}

		return null;
	}

}
