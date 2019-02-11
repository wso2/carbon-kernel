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
 * This interface represents a concurrent FIFO distributed queue / priority queue.
 */
public interface Queue {

	/**
	 * Enqueues data into the queue.
	 * @param data The data to be put into the queue, there's a 1MB hard limit on the data size
	 * @throws CoordinationException
	 */
	public void enqueue(byte[] data) throws CoordinationException;
	
	/**
	 * Enqueues data into the queue, with a given priority.
	 * @param data The data to be put into the queue, there's a 1MB hard limit on the data size
	 * @param priority The priority of the inserted data
	 * @throws CoordinationException
	 */
	public void enqueue(byte[] data, int priority) throws CoordinationException;
	
	/**
	 * Dequeues data from the queue, this is a non-blocking operation, if there isn't any data
	 * available, this will return null.
	 * @return Dequeued data, or null, if the queue is empty
	 * @throws CoordinationException
	 */
	public byte[] dequeue() throws CoordinationException;
	
	/**
	 * Dequeues data from the queue, and it blocks until data is available.
	 * @return Dequeued data
	 * @throws CoordinationException
	 */
	public byte[] blockingDequeue() throws CoordinationException;
	
	/**
	 * Returns the queue id.
	 * @return The queue id
	 */
	public String getQueueId();
	
	/**
	 * Closes the queue and release any resources allocated.
	 */
	public void close();
	
}
