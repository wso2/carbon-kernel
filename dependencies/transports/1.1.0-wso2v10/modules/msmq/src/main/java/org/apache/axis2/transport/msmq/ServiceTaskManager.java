/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.axis2.transport.msmq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.base.threads.WorkerPool;
import org.apache.axis2.transport.msmq.util.IMSMQClient;
import org.apache.axis2.transport.msmq.util.MSMQCamelClient;
import org.apache.axis2.transport.msmq.util.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Each service will have one ServiceTaskManager instance that will create, manage and also destroy
 * idle tasks created for it, for message receipt.
 *
 * This also acts as the ExceptionListener for all MSMQ connections made on behalf of the service.
 * Since the ExceptionListener is notified by a JMS provider on a "serious" error, we simply try
 * to re-connect. Thus a connection failure for a single task, will re-initialize the state afresh
 * for the service, by discarding all connections. 
 */
public class ServiceTaskManager {
	private static final Log log = LogFactory.getLog(ServiceTaskManager.class);
	private static final int STATE_STOPPED = 0;
	private static final int STATE_STARTED = 1;
	private static final int STATE_PAUESED = 2;
	private static final int STATE_SHUTTING_DOWN = 3;
	private static final int STATE_FAILURE = 4;
	private WorkerPool workerPool = null;
	private volatile int activeTaskCount = 0;
	private int concurrentConsumers = 1;
	private String destinationQueue;
	private volatile int serviceTaskManagerState = STATE_STOPPED;
	private String serviceName;
	private MSMQMessageReceiver msmqMessageReceiver = null;
	private IMSMQClient msmqClient;
	/** The list of active tasks thats managed by this instance */
	private final List<MessageListenerTask> pollingTasks = Collections.synchronizedList(new ArrayList<MessageListenerTask>());

	private volatile int workerState = STATE_STOPPED;

	public int getServiceTaskManagerState() {
		return serviceTaskManagerState;
	}

	public void setServiceTaskManagerState(int serviceTaskManagerState) {
		this.serviceTaskManagerState = serviceTaskManagerState;
	}

	public MSMQMessageReceiver getMsmqMessageReceiver() {
		return msmqMessageReceiver;
	}

	public void setMsmqMessageReceiver(MSMQMessageReceiver msmqMessageReceiver) {
		this.msmqMessageReceiver = msmqMessageReceiver;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getDestinationQueue() {
		return destinationQueue;
	}

	public void setDestinationQueue(String destinationQueue) {
		this.destinationQueue = destinationQueue;
	}

	public IMSMQClient getMsmqClient() {
		return msmqClient;
	}

	public void setMsmqClient(IMSMQClient msmqClient) {
		this.msmqClient = msmqClient;
	}

	public int getActiveTaskCount() {
		return activeTaskCount;
	}

	public void setActiveTaskCount(int activeTaskCount) {
		this.activeTaskCount = activeTaskCount;
	}

	public int getConcurrentConsumers() {
		return concurrentConsumers;
	}

	public void setConcurrentConsumers(int concurrentConsumers) {
		this.concurrentConsumers = concurrentConsumers;
	}

	public WorkerPool getWorkerPool() {
		return workerPool;
	}

	public void setWorkerPool(WorkerPool workerPool) {
		this.workerPool = workerPool;
	}

	public synchronized void start() {
		// Separate sharable thread is initiating the work under the
		// MSMQLinstener
		for (int i = 0; i < concurrentConsumers; i++) {
			workerPool.execute(new MessageListenerTask());
		}

	}

	
	/**
     * Start or re-start the Task Manager by shutting down any existing worker tasks and
     * re-creating them. However, if this is STM is PAUSED, a start request is ignored.
     * This applies for any connection failures during paused state as well, which then will
     * not try to auto recover
     */
	public synchronized void stop() {
		if (log.isDebugEnabled()) {
			log.debug("Stopping ServiceTaskManager for service : " + serviceName);
		}

		if (serviceTaskManagerState != STATE_FAILURE) {
			serviceTaskManagerState = STATE_SHUTTING_DOWN;
		}

		// TODO:implement the logic to shutdown the active resourcesrs
		synchronized (pollingTasks) {
			for (MessageListenerTask lstTask : pollingTasks) {
				lstTask.requestShutdown();
			}
		}

		if (serviceTaskManagerState != STATE_FAILURE) {
			serviceTaskManagerState = STATE_STOPPED;
		}
		
		msmqClient = null;
		
		synchronized (pollingTasks) {
			pollingTasks.clear();
		}
		
		log.info("Task manager for service : " + serviceName + " shutdown");
	}

	/**
     * The actual threads/tasks that perform MSMQ message polling
     */
	private class MessageListenerTask implements Runnable {

		private static final String ECHO = "echo";

		protected void requestShutdown() {
			workerState = STATE_SHUTTING_DOWN;
		}

		private boolean isActive() {
			return workerState == STATE_STARTED;
		}

		/**
		 * As soon as we create a new polling task, add it to the STM for
		 * control later
		 */
		MessageListenerTask() {
			synchronized (pollingTasks) {
				pollingTasks.add(this);
			}
		}

		public void run() {
			if (destinationQueue.equals(ECHO) || serviceName.equals(ECHO)) {
				return;
			}
			if (log.isDebugEnabled()) {
				log.info("[[[MSMQ Listner has started...... MSMSQ QUEUS IS ]]]]:" + destinationQueue);
			}
			workerState = STATE_STARTED;
			Message mqMessage = null;
			msmqClient = new MSMQCamelClient();
			try {
				msmqClient.open(destinationQueue,org.apache.axis2.transport.msmq.util.IMSMQClient.Access.RECEIVE);
			} catch (AxisFault axisFault) {
				log.error("Error while opening queue!" + destinationQueue);
			}

			if (log.isDebugEnabled()) {
				log.info("Open the destination with the name: " + destinationQueue);
			}
			activeTaskCount++;
			if (log.isDebugEnabled()) {
				log.debug("New poll task starting: thread id = " + Thread.currentThread().getId());
			}

			// read messages from the queue and process them for forever..
			try {
				while (isActive() && msmqClient != null && (getServiceName() != null && !getServiceName().isEmpty())) {
					try {
						mqMessage = msmqClient.receive(MSMQConstants.MSMQ_RECEIVE_TIME_OUT);
					} catch (AxisFault axisFault) {
						// just ignore
					}
					if (log.isTraceEnabled()) {
						if (mqMessage != null) {
							log.trace("Read a message from : + " + destinationQueue + "by Thread ID : " + Thread.currentThread().getId());
						} else {
							log.trace("No message received by Thread ID : " + Thread.currentThread().getId() + " for destination : " +
							          destinationQueue);
						}
					}
					if (mqMessage != null) {
						handleMessage(mqMessage);
					}

				}
			} finally {
		        mqMessage = null;
				workerState = STATE_STOPPED;
				activeTaskCount--;
				synchronized (pollingTasks) {
					pollingTasks.remove(this);
				}
			}

		}
	}

	private void handleMessage(Message msmqMessage) {
		msmqMessageReceiver.onMessage(msmqMessage);
	}

	private void scheduleNewTaskIfAppropiate() {
		// TODO
	}

	private void handleException(String msg, Exception e) {
		log.error(msg);
		throw new RuntimeException(msg, e);
	}
}