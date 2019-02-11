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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.ZooDefs.Ids;
import org.wso2.carbon.coordination.common.CoordinationException;
import org.wso2.carbon.coordination.common.CoordinationException.ExceptionCode;
import org.wso2.carbon.coordination.core.utils.CoordinationUtils;

/**
 * Primitive synchronization structure to be used by other high level synchronization scenarios.
 */
public abstract class ZKSyncPrimitive implements Watcher {

	private static final Log log = LogFactory.getLog(ZKSyncPrimitive.class);
	
	private ZooKeeper zooKeeper;
		
	private String id;
	
	private String rootPath;
	
	private BlockingQueue<WatchedEvent> eventQueue;
	
	private int waitTimeout;
	
	/**
	 * Constructor for ZKSyncPrimitive.
	 * @param zooKeeper The ZooKeeper object to be used
	 * @param rootPath The root path of the znode created
	 * @param waitTimeout The wait timeout for polling a watch event in milliseconds
	 * @throws CoordinationException
	 */
	public ZKSyncPrimitive(ZooKeeper zooKeeper, String baseName, String id, int waitTimeout) throws CoordinationException {
		this.zooKeeper = zooKeeper;
		this.rootPath = CoordinationUtils.createPathFromId(baseName, id);
		this.eventQueue = new LinkedBlockingQueue<WatchedEvent>();
		this.waitTimeout = waitTimeout;
		this.createRecursive(this.getRootPath());
	}
	
	public String getRootPath() {
		return rootPath;
	}
	
	public int getWaitTimeout() {
		return waitTimeout;
	}
	
	/**
	 * Creates a node if it doesn't exists.
	 * @param path The path to create the node
	 * @return If it successfully creates the node or it already exist, returns true, or else,
	 * if the parent node doesn't exist, it returns false
	 * @throws CoordinationException
	 */
	private boolean createNode(String path) throws CoordinationException {
		try {
			/* creates the node if it does not exist, if it exist, we ignore the error */
		    this.getZooKeeper().create(path, new byte[0], 
		    		Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		    return true;
		} catch (KeeperException e) {
			if (e.code() == Code.NODEEXISTS) {
				return true;
			} else if (e.code() == Code.NONODE) {
				/* parent node doesn't exist */
				return false;
			} else {				
				throw new CoordinationException(ExceptionCode.GENERIC_ERROR, e);
			}
		} catch (Exception e) {
			throw new CoordinationException(ExceptionCode.GENERIC_ERROR, e);
		}
	}
	
	protected void createRecursive(String path) throws CoordinationException {
		ZKPath zkpath = new ZKPath(path);
		int steps = 0;
		while (!this.createNode(zkpath.getSubPath(steps))) {
			steps++;
		}
		while (steps > 0) {
			steps--;
			this.createNode(zkpath.getSubPath(steps));
		}
	}
	
	protected ZooKeeper getZooKeeper() {
		return zooKeeper;
	}
	
	protected void releaseResources() {
		try {
			this.getZooKeeper().delete(this.getRootPath(), -1);
		} catch (InterruptedException e) {
			log.error("Interrupted in releasing resources", e);
		} catch (KeeperException e) {
			if (e.code() != Code.NONODE) {
				log.error("Error in releasing resources", e);
			}
		}
	}
	
	/**
	 * Represents a ZooKeeper path. 
	 */
	private class ZKPath {
		
		private String value;
		
		private List<Integer> indices;
		
		public ZKPath(String value) {
			this.value = value;
			this.indices = new ArrayList<Integer>();
			this.indices.add(this.value.length());
			/* i > 0 is because we don't want the root node */
			for (int i = value.length() - 1; i > 0; i--) {
				if (this.value.charAt(i) == '/') {
					this.indices.add(i);
				}
			}
		}
		
		public String getValue() {
			return value;
		}
		
		public List<Integer> getIndices() {
			return indices;
		}
		
		/**
		 * Return the sub path of the current path,
		 * for example if the path value is "/org/wso2/carbon/tenants/0/org.wso2.carbon.coordination.core.sync.Barrier/b1",
		 * getSubPath(1) would be "/org/wso2/carbon/tenants/0/org.wso2.carbon.coordination.core.sync.Barrier" and so on,
		 * and getSubPath(0) would be itself.
		 * @param steps
		 * @return
		 */
		public String getSubPath(int steps) throws CoordinationException {
			List<Integer> indices = this.getIndices();
			if (steps > indices.size() - 1) {
				throw new CoordinationException("A subpath cannot be created from the path: '" + 
			            this.getValue() + "' by going back " + steps + " steps.");
			}
			return this.getValue().substring(0, indices.get(steps));
		}
		
	}
	
	public BlockingQueue<WatchedEvent> getEventQueue() {
		return eventQueue;
	}
	
	public WatchedEvent takeQueuedEvent() throws CoordinationException {
		try {
			int timeout = this.getWaitTimeout();
			if (timeout == -1) {
				return this.getEventQueue().take();
			} else {
			    WatchedEvent event = this.getEventQueue().poll(timeout, TimeUnit.MILLISECONDS);
			    if (event == null) {
			    	throw new CoordinationException(ExceptionCode.WAIT_TIMEOUT);
			    } else {
			    	return event;
			    }
			}
		} catch (InterruptedException e) {
			throw new CoordinationException(ExceptionCode.GENERIC_ERROR, e);
		}
	}
	
	public String getId() {
		return id;
	}
	
	@Override
	public void process(WatchedEvent event) {
		this.getEventQueue().add(event);
	}
	
}
