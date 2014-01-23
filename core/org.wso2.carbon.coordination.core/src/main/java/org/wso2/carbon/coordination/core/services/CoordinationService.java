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
package org.wso2.carbon.coordination.core.services;

import org.wso2.carbon.coordination.common.CoordinationException;
import org.wso2.carbon.coordination.core.sync.Barrier;
import org.wso2.carbon.coordination.core.sync.Group;
import org.wso2.carbon.coordination.core.sync.IntegerCounter;
import org.wso2.carbon.coordination.core.sync.Lock;
import org.wso2.carbon.coordination.core.sync.Queue;

/**
 * Coordination service interface.
 */
public interface CoordinationService {
	
	/**
	 * Checks if the coordination service is available or not, 
	 * if it's not available, other coordination service features should
	 * not be used.
	 * @return True if the coordination service is available
	 */
	public boolean isEnabled();
	
	/**
	 * Creates/Joins a single barrier.
	 * @param id The id used to create the barrier, all participants should have the same id
	 * @param count The number of participants
	 * @param waitTimeout The timeout (milliseconds) used to wait for other participants
	 * @return The created Barrier object
	 * @throws CoordinationException 
	 */
	public Barrier createBarrier(String id, int count, int waitTimeout) throws CoordinationException;
	
	/**
	 * Creates/Joins a node group.
	 * @param id The id of the node group, all the participants should provide the same id
	 * @return The created Group object
	 * @throws CoordinationException
	 */
	public Group createGroup(String id) throws CoordinationException;
	
	/**
	 * Creates/Joins a queue.
	 * @param id The id of the queue, all the participants should provide the same id
	 * @param waitTimeout The timeout (milliseconds) used to wait for a blocking dequeue operation 
	 * @return The created Queue object
	 * @throws CoordinationException
	 */
	public Queue createQueue(String id, int waitTimeout) throws CoordinationException;
	
	/**
	 * Creates a lock.
	 * @param id The lock id
	 * @param waitTimeout The timeout (milliseconds) used to wait for the lock to be released
	 * @return The created Lock object
	 * @throws CoordinationException
	 */
	public Lock createLock(String id, int waitTimeout) throws CoordinationException;
	
	/**
	 * Creates an integer counter.
	 * @param id The counter id
	 * @return The created IntegerCounter object
	 * @throws CoordinationException
	 */
	public IntegerCounter createIntegerCounter(String id) throws CoordinationException;
	
	/**
	 * Close the service connection.
	 * @throws CoordinationException 
	 */
	public void close() throws CoordinationException;
	
}
