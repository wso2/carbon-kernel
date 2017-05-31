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

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;
import org.wso2.carbon.coordination.common.CoordinationException;
import org.wso2.carbon.coordination.common.CoordinationException.ExceptionCode;
import org.wso2.carbon.coordination.core.sync.Lock;

/**
 * ZooKeeper based lock implementation.
 * @see Lock
 */
public class ZKLock extends ZKSyncPrimitive implements Lock {

	private String createdPath;
	
	public ZKLock(ZooKeeper zooKeeper, String lockId, int waitTimeout)
			throws CoordinationException {
		super(zooKeeper, ZKLock.class.getCanonicalName(), lockId, waitTimeout);
	}
	
	public String getCreatedPath() {
		return createdPath;
	}
	
	private void setCreatedPath(String createdPath) {
		this.createdPath = createdPath;
	}

	private String getPathBeforeMine(List<String> ids, String myId) {
		String currentId = ids.get(0);
		for (int i = 1; i < ids.size(); i++) {
			if (ids.get(i).equals(myId)) {
				return this.getRootPath() + "/" + currentId;
			}
		}
		return null;
	}
	
	@Override
	public void acquire() throws CoordinationException {
		try {
			String myPath = this.getZooKeeper().create(this.getRootPath() + "/node", new byte[0], 
					Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
			this.setCreatedPath(myPath);
			String myId = this.getCreatedPath().substring(
					this.getCreatedPath().lastIndexOf("/") + 1);
			while (true) {
				List<String> ids = this.getZooKeeper().getChildren(this.getRootPath(), false);
				Collections.sort(ids);
				if (myId.equals(ids.get(0))) {
					return;
				} else {
					String predNodePath = this.getPathBeforeMine(ids, myId);
					if (this.getZooKeeper().exists(predNodePath, this) != null) {
						while (true) {
							this.takeQueuedEvent();
							if (this.getZooKeeper().exists(predNodePath, this) == null) {
								break;
							}
						}
					} else {
						continue;
					}
				}
			}
		} catch (Exception e) {
			throw new CoordinationException(ExceptionCode.GENERIC_ERROR, e);
		}
	}

	@Override
	public void release() throws CoordinationException {
		if (this.getCreatedPath() == null) {
			throw new CoordinationException(
					"Lock#acquire() has to be called first before Lock#release()",
					ExceptionCode.GENERIC_ERROR);
		}
		try {
			this.getZooKeeper().delete(this.getCreatedPath(), -1);
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

}
