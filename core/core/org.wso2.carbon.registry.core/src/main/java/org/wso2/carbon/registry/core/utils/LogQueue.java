/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.registry.core.utils;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Implementation of a Queue for logs.
 */
public class LogQueue extends LinkedBlockingQueue<Object> {

    /**
     * Clears to queue.
     */
    public void clear() {
        super.clear();
    }

    /**
     * Retrieves the head of the queue, or null if this queue is empty.
     *
     * @return the top most object, or null if this queue is empty.
     */
    public Object peek() {
        return super.peek();
    }

    /**
     * Retrieves and removes the head of this queue, or null if this queue is empty.
     *
     * @return the top most object, or null if this queue is empty.
     */
    public Object poll() {
        return super.poll();
    }

    /**
     * Adds an item into this queue.
     *
     * @param object the object to add
     *
     * @throws InterruptedException if the operation failed.
     */
    public void put(Object object) throws InterruptedException {
        super.put(object);
    }

    /**
     * Obtains the size of this queue.
     *
     * @return the size of this queue.
     */
	public int size() {
		return super.size();
	}

}
