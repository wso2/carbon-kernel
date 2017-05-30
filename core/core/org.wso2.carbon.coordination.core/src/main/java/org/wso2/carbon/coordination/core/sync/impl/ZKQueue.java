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
import java.util.Formatter;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;
import org.wso2.carbon.coordination.common.CoordinationException;
import org.wso2.carbon.coordination.common.CoordinationException.ExceptionCode;
import org.wso2.carbon.coordination.core.sync.Queue;

/**
 * ZooKeeper based queue / priority queue implementation.
 * @see Queue
 */
public class ZKQueue extends ZKSyncPrimitive implements Queue {

	public ZKQueue(ZooKeeper zooKeeper, String queueId, int waitTimeout)
			throws CoordinationException {
		super(zooKeeper, ZKQueue.class.getCanonicalName(), queueId, waitTimeout);
	}

	@Override
	public void enqueue(byte[] data) throws CoordinationException {
		try {
			this.getZooKeeper().create(this.getRootPath() + "/b", 
					data, Ids.OPEN_ACL_UNSAFE,
			        CreateMode.PERSISTENT_SEQUENTIAL);
		} catch (Exception e) {
			throw new CoordinationException(ExceptionCode.GENERIC_ERROR, e);
		}
	}
	
	@Override
	public void enqueue(byte[] data, int priority) throws CoordinationException {
		try {
			/* aXXX will be smaller than bXXX lexicographically, so priority enqueue entries will always be
			 * dequeued before normal enqueues */
			this.getZooKeeper().create(
					this.getRootPath() + "/a" + new Formatter().format("%010d", priority), 
					data, Ids.OPEN_ACL_UNSAFE,
			        CreateMode.PERSISTENT);
		} catch (Exception e) {
			throw new CoordinationException(ExceptionCode.GENERIC_ERROR, e);
		}
	}

	private byte[] extractEntry(List<String> entries) throws CoordinationException {
		String fullPath;
		Collections.sort(entries);
		for (String entryPath : entries) {
			fullPath = this.getRootPath() + "/" + entryPath;
			try {
				byte[] content = this.getZooKeeper().getData(fullPath, false, null);
				/* check if it's already taken, exception is thrown, if it is */
				this.getZooKeeper().delete(fullPath, -1);
				/* all clear, we own this, return the data */
				return content;
			} catch (KeeperException e) {
				if (e.code() != Code.NONODE) {
					throw new CoordinationException(ExceptionCode.GENERIC_ERROR, e);
				}
				continue;
			} catch (InterruptedException e) {
				throw new CoordinationException(ExceptionCode.GENERIC_ERROR, e);
			}
		}
		return null;
	}
	
	@Override
	public byte[] dequeue() throws CoordinationException {
		List<String> entries;
		byte[] content;
		try {
			while (true) {
				entries = this.getZooKeeper().getChildren(this.getRootPath(), false);
				if (entries.size() == 0) {
					return null;
				}
				content = this.extractEntry(entries);
				if (content != null) {
					return content;
				}
			}
		} catch (Exception e) {
			throw new CoordinationException(ExceptionCode.GENERIC_ERROR, e);
		}
	}

	@Override
	public byte[] blockingDequeue() throws CoordinationException {
		List<String> entries;
		byte[] content;
		try {
			entries = this.getZooKeeper().getChildren(this.getRootPath(), this);
			content = this.extractEntry(entries);
			if (content != null) {
				return content;
			}
			while (true) {
				this.takeQueuedEvent();
				entries = this.getZooKeeper().getChildren(this.getRootPath(), this);
				content = this.extractEntry(entries);
				if (content != null) {
					return content;
				}
			}
		} catch (Exception e) {
			throw new CoordinationException(ExceptionCode.GENERIC_ERROR, e);
		}
	}

	@Override
	public void close() {
		this.releaseResources();
	}

	@Override
	public String getQueueId() {
		return this.getId();
	}	
	
}
