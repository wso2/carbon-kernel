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
package org.wso2.carbon.coordination.core.services.impl;

import java.io.IOException;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.wso2.carbon.coordination.common.CoordinationException;
import org.wso2.carbon.coordination.common.CoordinationException.ExceptionCode;
import org.wso2.carbon.coordination.core.CoordinationConfiguration;
import org.wso2.carbon.coordination.core.services.CoordinationService;
import org.wso2.carbon.coordination.core.sync.Barrier;
import org.wso2.carbon.coordination.core.sync.Group;
import org.wso2.carbon.coordination.core.sync.IntegerCounter;
import org.wso2.carbon.coordination.core.sync.Lock;
import org.wso2.carbon.coordination.core.sync.Queue;
import org.wso2.carbon.coordination.core.sync.impl.ZKBarrier;
import org.wso2.carbon.coordination.core.sync.impl.ZKGroup;
import org.wso2.carbon.coordination.core.sync.impl.ZKIntegerCounter;
import org.wso2.carbon.coordination.core.sync.impl.ZKLock;
import org.wso2.carbon.coordination.core.sync.impl.ZKQueue;
import org.wso2.carbon.coordination.core.utils.CoordinationUtils;
import org.wso2.carbon.core.CarbonThreadFactory;

/**
 * Coordination service implementation class.
 */
public class ZKCoordinationService implements CoordinationService, Watcher {

	public static final int MAX_ZK_MESSAGE_SIZE = 1024 * 800;
	
	public static final int MAX_SCHEDULER_THREADS = 10;
	
	public static final int ZNODE_CLEANUP_DELAY = 1000 * 60 * 2;
	
	public static final int ZNODE_CLEANUP_TASK_INTERVAL = 1000 * 30;
	
	private static final Log log = LogFactory.getLog(ZKCoordinationService.class);
	
	private ZooKeeper zooKeeper;
	
	private boolean enabled;
	
	private boolean closed;
	
	private static ScheduledExecutorService scheduler;
	
	private static List<ZNodeDeletionEntry> znodeTimerDeletionList;
	
	private static List<String> znodeOnCloseDeletionList;
	
	public ZKCoordinationService(CoordinationConfiguration conf) throws CoordinationException {
		this.closed = false;
		this.enabled = conf.isEnabled();
		if (this.isEnabled()) {
		    try {
			    this.zooKeeper = new ZooKeeper(conf.getConnectionString(), 
			    		conf.getSessionTimeout(), this);
			    if (znodeOnCloseDeletionList == null) {
			    	znodeOnCloseDeletionList = new Vector<String>();
			    }
			    if (scheduler == null || scheduler.isShutdown()) {
			    	znodeTimerDeletionList = new Vector<ZKCoordinationService.ZNodeDeletionEntry>();
					scheduler = Executors.newScheduledThreadPool(MAX_SCHEDULER_THREADS, new CarbonThreadFactory(
							new ThreadGroup("CoordinationThread")));
					scheduler.scheduleWithFixedDelay(new ZNodeDeletionTask(),
			        		ZNODE_CLEANUP_TASK_INTERVAL, ZNODE_CLEANUP_TASK_INTERVAL,
			        		TimeUnit.MILLISECONDS);
			    }
			    log.info("Coordination service connection established with ZooKeeper.");
		    } catch (IOException e) {
			    new CoordinationException(ExceptionCode.IO_ERROR, e);
		    }
		} else {
			log.info("Coordination service disabled.");
		}
	}
	
	public ZKCoordinationService(String configurationFilePath) throws CoordinationException {
		this(CoordinationUtils.loadCoordinationClientConfig(configurationFilePath));
	}
	
	public static List<ZNodeDeletionEntry> getZNodeTimerDeletionList() {
		return znodeTimerDeletionList;
	}
	
	public static List<String> getZNodeOnCloseDeletionList() {
		return znodeOnCloseDeletionList;
	}
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}

	private void checkService() throws CoordinationException {
		if (!this.isEnabled()) {
			throw new CoordinationException(ExceptionCode.COORDINATION_SERVICE_NOT_ENABLED);
		}
	}
	
	@Override
	public Barrier createBarrier(String id, int count, int waitTimeout) throws CoordinationException {
		this.checkService();
		return new ZKBarrier(this.getZooKeeper(), id, count, waitTimeout);
	}

	@Override
	public Group createGroup(String id) throws CoordinationException {
		this.checkService();
		if (ZKGroup.GROUP_COMM_NODE_ID.equals(id)) {
			throw new CoordinationException("'" + ZKGroup.GROUP_COMM_NODE_ID + 
					"' cannot be used a group id, since it is reserved.", ExceptionCode.GENERIC_ERROR);
		}
		return new ZKGroup(this.getZooKeeper(), id);
	}

	@Override
	public Queue createQueue(String id, int waitTimeout) throws CoordinationException {
		this.checkService();
		return new ZKQueue(this.getZooKeeper(), id, waitTimeout);
	}
	
	public ZooKeeper getZooKeeper() {
		return zooKeeper;
	}

	public boolean isClosed() {
		return closed;
	}
	
	@Override
	public void close() throws CoordinationException {
		if (this.isClosed()) {
			return;
		}
		try {
			if (this.isEnabled()) {
				this.cleanupOnCloseZNodes();
			    this.getZooKeeper().close();
			}
			this.closed = true;
		} catch (InterruptedException e) {
			throw new CoordinationException(ExceptionCode.GENERIC_ERROR, e);
		}
	}
	
	private void cleanupOnCloseZNodes() {
		for (String path : getZNodeOnCloseDeletionList()) {
			this.deleteZNode(path);
		}
	}

	@Override
	public Lock createLock(String id, int waitTimeout) throws CoordinationException {
		this.checkService();
		return new ZKLock(this.getZooKeeper(), id, waitTimeout);
	}
	
	public static void scheduleOnCloseZNodeDeletion(String path) {
		getZNodeOnCloseDeletionList().add(path);
	}
	
	public static void scheduleTimedZNodeDeletion(String path) {
		getZNodeTimerDeletionList().add(new ZNodeDeletionEntry(path, System.currentTimeMillis()));
	}
	
	private void deleteZNode(String path) {
		try {
			getZooKeeper().delete(path, -1);
		} catch (Exception ignore) {
			// ignore
		}
	}
	
	private static class ZNodeDeletionEntry {
		
		private long createdTime;
		
		private String path;
		
		public ZNodeDeletionEntry(String path, long createdTime) {
			this.path = path;
			this.createdTime = createdTime;
		}

		public long getCreatedTime() {
			return createdTime;
		}

		public String getPath() {
			return path;
		}
		
	}
	
	private class ZNodeDeletionTask implements Runnable {
		
		public void run() {
			for (ZNodeDeletionEntry entry : getZNodeTimerDeletionList()) {
				if (this.readyToDelete(entry)) {
					deleteZNode(entry.getPath());
				}
			}
		}
		
		private boolean readyToDelete(ZNodeDeletionEntry entry) {
			return (System.currentTimeMillis() - entry.getCreatedTime()) > ZNODE_CLEANUP_DELAY;
		}
		
	}

	@Override
	public void process(WatchedEvent event) {
		if (log.isDebugEnabled()) {
			log.debug("At ZKCoordinationService#process: " + event.toString());
		}
	}

	@Override
	public IntegerCounter createIntegerCounter(String id)
			throws CoordinationException {
		this.checkService();
		return new ZKIntegerCounter(this.getZooKeeper(), id);
	}

}
