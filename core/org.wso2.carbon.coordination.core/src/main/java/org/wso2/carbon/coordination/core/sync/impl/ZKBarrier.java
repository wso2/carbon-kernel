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
package org.wso2.carbon.coordination.core.sync.impl;

import java.util.Collections;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.wso2.carbon.coordination.common.CoordinationException;
import org.wso2.carbon.coordination.common.CoordinationException.ExceptionCode;
import org.wso2.carbon.coordination.core.sync.Barrier;

/**
 * ZooKeeper based barrier / double barrier implementation.
 * @see Barrier
 */
public class ZKBarrier extends ZKSyncPrimitive implements Barrier {

	private int count;
	
	private String createdPath;
		
	public ZKBarrier(ZooKeeper zooKeeper, String barrierId, int count, int waitTimeout) 
			throws CoordinationException {
		super(zooKeeper, Barrier.class.getCanonicalName(), barrierId, waitTimeout);
		this.count = count;
	}
		
	public int getCount() {
		return count;
	}
	
	public String getCreatedPath() {
		return createdPath;
	}
	
	public void setCreatedPath(String createdPath) {
		this.createdPath = createdPath;
	}

	@Override
	public void waitOnBarrier() throws CoordinationException {
		this.enter();
		this.leave();
	}
	
	@Override
	public void enter() throws CoordinationException {
		try {
			this.getZooKeeper().exists(this.getRootPath() + "/ready", this);
			String tmpPath = this.getZooKeeper().create(this.getRootPath() + "/node", 
					new byte[0], Ids.OPEN_ACL_UNSAFE,
			        CreateMode.EPHEMERAL_SEQUENTIAL);
			this.setCreatedPath(tmpPath);
			List<String> ids = this.getZooKeeper().getChildren(this.getRootPath(), false);
			Collections.sort(ids);
			/* check if i'm the last participant or not, another node may have added the last entry, not me,
			 * so check if i'm at the end of the list */
			if (ids.size() < this.getCount() || !this.getCreatedPath().endsWith(ids.get(ids.size() - 1))) {
				while (true) {
					this.takeQueuedEvent();
					if (this.getZooKeeper().getChildren(this.getRootPath(), 
							false).size() >= this.getCount()) {
						break;
					}
				}
			} else {
				this.getZooKeeper().create(this.getRootPath() + "/ready", 
						new byte[0], Ids.OPEN_ACL_UNSAFE,
				        CreateMode.EPHEMERAL);
				return;
			}
		} catch (CoordinationException e) {
			this.releaseResources();
			throw e;
		} catch (Exception e) {
			this.releaseResources();
			throw new CoordinationException(ExceptionCode.GENERIC_ERROR, e);
		}
	}
	
	@Override
	public void leave() throws CoordinationException {
		try {
			this.getZooKeeper().delete(this.getCreatedPath(), -1);
			if (this.getZooKeeper().getChildren(this.getRootPath(), this).size() > 1) {
				while (this.takeQueuedEvent() != null) {
					if (this.getZooKeeper().getChildren(this.getRootPath(), this).size() <= 1) {
						break;
					}
				}
			}
		} catch (KeeperException e) {
			if (e.code() != Code.NONODE) {
				throw new CoordinationException(ExceptionCode.GENERIC_ERROR, e);
			}
		} catch (Exception e) {
			throw new CoordinationException(ExceptionCode.GENERIC_ERROR, e);
		} finally {
			this.setCreatedPath(null);
		}
	}

	@Override
	public String getBarrierId() {
		return this.getId();
	}
	
}
