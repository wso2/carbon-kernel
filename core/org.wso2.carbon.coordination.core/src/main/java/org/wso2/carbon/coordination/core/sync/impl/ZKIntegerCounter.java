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
package org.wso2.carbon.coordination.core.sync.impl;

import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.wso2.carbon.coordination.common.CoordinationException;
import org.wso2.carbon.coordination.common.CoordinationException.ExceptionCode;
import org.wso2.carbon.coordination.core.sync.IntegerCounter;

/**
 * ZooKeeper based integer counter implementation.
 * @see IntegerCounter
 */
public class ZKIntegerCounter extends ZKSyncPrimitive implements IntegerCounter {

	public ZKIntegerCounter(ZooKeeper zooKeeper, String counterId) 
			throws CoordinationException {
		super(zooKeeper, ZKIntegerCounter.class.getCanonicalName(), counterId, -1);
	}

	@Override
	public int incrementAndGet() throws CoordinationException {
		try {
		    Stat stat = this.getZooKeeper().setData(this.getRootPath(), new byte[0], -1);
		    return stat.getVersion();
		} catch (Exception e) {
			throw new CoordinationException("Error in getting a new integer counter value: " + 
		            e.getMessage(), ExceptionCode.GENERIC_ERROR, e);
		}
	}

	@Override
	public void delete() throws CoordinationException {
		try {
			this.getZooKeeper().delete(this.getRootPath(), -1);
		} catch (Exception e) {
			throw new CoordinationException("Error in deleting the counter: " + 
		            e.getMessage(), ExceptionCode.GENERIC_ERROR, e);
		}
	}

}
