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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;
import org.wso2.carbon.coordination.common.CoordinationException;
import org.wso2.carbon.coordination.common.CoordinationException.ExceptionCode;
import org.wso2.carbon.coordination.core.services.impl.ZKCoordinationService;
import org.wso2.carbon.coordination.core.sync.Group;
import org.wso2.carbon.coordination.core.sync.GroupEventListener;

/**
 * ZooKeeper based node group implementation.
 * @see Group
 */
public class ZKGroup extends ZKSyncPrimitive implements Group {

	private static final Log log = LogFactory.getLog(ZKGroup.class);
	
	public static final String COMM_BASE_NAME = "__COMM__";
	
	public static final String GROUP_COMM_NODE_ID = COMM_BASE_NAME + "/GROUP_COMMUNICATION";
	
	public static final String PEER_RESULTS_BASE_NAME = COMM_BASE_NAME + "/PEER_RESULTS";
	
	public static final String PEER_REQUESTS_BASE_NAME = COMM_BASE_NAME + "/PEER_REQUESTS";
	
	private GroupEventListener groupEventListener;
			
	private String memberPath;
	
	private String memberId;
		
	private List<String> lastProcessedMemberIds;
	
	private String leaderId;
	
	private boolean active;
	
	private CommunicationChannel groupCommChannel;
	
	private CommunicationChannel myRequestChannel;
	
	private Map<String, CommunicationChannel> peerRequestChannels;
	
	private static Marshaller peerRequestMarshaller;
	
	private static Marshaller peerResponseMarshaller;
	
	private static Unmarshaller peerRequestUnmarshaller;
	
	private static Unmarshaller peerResponseUnmarshaller;
	
	private Object memberArrivalCountLock = new Object();
		
	public ZKGroup(ZooKeeper zooKeeper, String groupId)
			throws CoordinationException {
		super(zooKeeper, ZKGroup.class.getCanonicalName(), groupId, -1);
		this.lastProcessedMemberIds = new ArrayList<String>();
		this.initMessageSerializers();
		this.peerRequestChannels = new HashMap<String, ZKGroup.CommunicationChannel>();
		this.initGroupCommChannel();
		this.initPeerResults();
		this.join(this.getGroupId());
		this.initMyRequestCommChannel();
		this.active = true;
	}
	
	private void initMessageSerializers() throws CoordinationException {
		try {
			if (peerRequestMarshaller == null || peerRequestUnmarshaller == null ||
					peerResponseMarshaller == null || peerRequestUnmarshaller == null) {
			    JAXBContext requestCtx = JAXBContext.newInstance(PeerRequestMessage.class);
			    JAXBContext responseCtx = JAXBContext.newInstance(PeerResponseMessage.class);
			    peerRequestMarshaller = requestCtx.createMarshaller();
			    peerRequestUnmarshaller = requestCtx.createUnmarshaller();
			    peerResponseMarshaller = responseCtx.createMarshaller();
			    peerResponseUnmarshaller = responseCtx.createUnmarshaller();
			}
		} catch (JAXBException e) {
			throw new CoordinationException("Error in initializing peer message serializers", 
					ExceptionCode.GENERIC_ERROR, e);
		}

	}
	
	public static Marshaller getPeerRequestMarshaller() {
		return peerRequestMarshaller;
	}

	public static Marshaller getPeerResponseMarshaller() {
		return peerResponseMarshaller;
	}

	public static Unmarshaller getPeerRequestUnmarshaller() {
		return peerRequestUnmarshaller;
	}

	public static Unmarshaller getPeerResponseUnmarshaller() {
		return peerResponseUnmarshaller;
	}

	public Map<String, ZKGroup.CommunicationChannel> getPeerRequestChannels() {
		return peerRequestChannels;
	}
	
	private CommunicationChannel createRequestChannelForPeer(String peerId) 
			throws CoordinationException {
		return new CommunicationChannel(this.getRootPath() + "/" + PEER_REQUESTS_BASE_NAME + 
				"/" + peerId, CommunicationChannel.CHANNEL_TYPE_PEER, false);
	}
	
	public CommunicationChannel retrievePeerRequestChannel(String peerId) 
			throws CoordinationException {
		CommunicationChannel channel = this.getPeerRequestChannels().get(peerId);
		if (channel == null) {
			channel = this.createRequestChannelForPeer(peerId);
			this.getPeerRequestChannels().put(peerId, channel);
		}
		return channel;
	}
	
	public CommunicationChannel getGroupCommChannel() {
		return groupCommChannel;
	}
	
	public CommunicationChannel getMyRequestChannel() {
		return myRequestChannel;
	}
	
	private void initGroupCommChannel() throws CoordinationException {
		this.groupCommChannel = new CommunicationChannel(this.getRootPath() + "/" +
	            GROUP_COMM_NODE_ID, CommunicationChannel.CHANNEL_TYPE_BROADCAST, true);
	}
	
	private void initPeerResults() throws CoordinationException {
		this.createRecursive(this.getRootPath() + "/" + PEER_RESULTS_BASE_NAME);
	}
	
	private void initMyRequestCommChannel() throws CoordinationException {
		this.myRequestChannel = new CommunicationChannel(this.getRootPath() + "/" +
	            PEER_REQUESTS_BASE_NAME + "/" + this.getMemberId(),
	            CommunicationChannel.CHANNEL_TYPE_PEER, true);
		/* schedule for deletion when we shutdown */
		ZKCoordinationService.scheduleOnCloseZNodeDeletion(
				this.getMyRequestChannel().getCommRootPath());
	}
	
	public boolean isActive() {
		return active;
	}
	
	private void setActive(boolean active) {
		this.active = active;
	}
	
	public String getMemberPath() {
		return memberPath;
	}
	
	private void join(String groupId) throws CoordinationException {
		try {
			this.memberPath = this.getZooKeeper().create(this.getRootPath() + "/node",
					new byte[0], Ids.OPEN_ACL_UNSAFE,
			        CreateMode.EPHEMERAL_SEQUENTIAL);
			this.memberId = this.getMemberPath().substring(
					this.getMemberPath().lastIndexOf("/") + 1);
			this.processMemberNodes();
		} catch (CoordinationException e) {
			throw e;
		} catch (Exception e) {
			throw new CoordinationException(ExceptionCode.GENERIC_ERROR, e);
		}
	}
	
	@Override
	public List<String> getMemberIds() throws CoordinationException {
		return this.lookupMemberIds(false);
	}
	
	public List<String> getLastProcessedMemberIds() {
		return lastProcessedMemberIds;
	}
	
	private void processMemberNodes() throws CoordinationException {
		List<String> oldMembers = this.getLastProcessedMemberIds();
		String oldLeaderId = this.getLeaderId();
		try {
			List<String> newMembers = this.lookupMemberIds(true);
			/* smallest id will be the leader, atleast myself should be here */
			this.lastProcessedMemberIds = newMembers;
			this.processArrivals(oldMembers);
			this.processDepartures(oldMembers);
			this.processLeader(oldLeaderId);			
			/* notify any wait for members situation */
			synchronized (this.memberArrivalCountLock) {
				this.memberArrivalCountLock.notifyAll();
			}
		} catch (Exception e) {
			throw new CoordinationException(ExceptionCode.GENERIC_ERROR, e);
		}
	}
	
	private void processLeader(String oldLeaderId) throws CoordinationException {
		this.leaderId = this.getMemberIds().get(0);
		if (!this.leaderId.equals(oldLeaderId)) {
			if (this.getGroupEventListener() != null) {
				this.getGroupEventListener().onLeaderChange(this.getLeaderId());
			}
		}
	}
	
	private void processArrivals(List<String> oldMembers) throws CoordinationException {
		List<String> arrivedMembers = new ArrayList<String>(this.getMemberIds());
		if (oldMembers != null) {
			arrivedMembers.removeAll(oldMembers);
		}
		if (this.getGroupEventListener() != null) {
			for (String mid : arrivedMembers) {
			    this.getGroupEventListener().onMemberArrival(mid);
			}
		}
	}
	
	private void processDepartures(List<String> oldMembers) throws CoordinationException {
		if (oldMembers == null) {
			return;
		}
		List<String> departedMembers = new ArrayList<String>(oldMembers);
		departedMembers.removeAll(this.getMemberIds());
		if (this.getGroupEventListener() != null) {
			for (String mid : departedMembers) {
			    this.getGroupEventListener().onMemberDeparture(mid);
			}
		}
	}
	
	private String getPathFromMemberId(String memberId) {
		return this.getRootPath() + "/" + memberId;
	}

	@Override
	public String getLeaderId() {
		return leaderId;
	}

	@Override
	public void broadcast(byte[] data) throws CoordinationException {
		this.getGroupCommChannel().sendMessage(data);
	}
	
	@Override
	public byte[] sendReceive(String targetMemberId, byte[] data) throws CoordinationException {
		PeerRequestMessage msg = new PeerRequestMessage();
		String correlationId = UUID.randomUUID().toString();
		msg.setCorrelationId(correlationId);
		msg.setData(data);
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		try {
			getPeerRequestMarshaller().marshal(msg, byteOut);
		} catch (JAXBException e) {
			throw new CoordinationException("Error in marshalling the peer request message", 
					ExceptionCode.GENERIC_ERROR, e);
		}
		this.retrievePeerRequestChannel(targetMemberId).sendMessage(byteOut.toByteArray());
		String resultNodePath = this.getRootPath() + "/" + PEER_RESULTS_BASE_NAME + "/" +
		        correlationId;
		String targetMemberPath = this.getPathFromMemberId(targetMemberId);
		try {
			PollingDataNode dataNode = new PollingDataNode(resultNodePath, targetMemberPath);
			if (dataNode.waitForData()) {
				return this.processDataNodeReply(dataNode.getData());
			} else {
				throw new CoordinationException("sendReceive failed in retrieving a reply " +
						"from member with id: " + targetMemberId);
			}
		} catch (Exception e) {
			throw new CoordinationException("Error in retrieving data from polling data node", 
					ExceptionCode.GENERIC_ERROR, e);
		}
	}
	
	private byte[] processDataNodeReply(byte[] data) throws CoordinationException {
		ByteArrayInputStream byteIn = new ByteArrayInputStream(data);
		PeerResponseMessage responseMsg;
		try {
		    responseMsg = (PeerResponseMessage) getPeerResponseUnmarshaller().
				unmarshal(byteIn);
		} catch (JAXBException e) {
			throw new CoordinationException("Error in unmarshalling peer response message: " +
		            e.getMessage(), ExceptionCode.GENERIC_ERROR, e);
		}
		if (!responseMsg.isSuccess()) {
			throw new CoordinationException("Error occured in target peer processing: " + 
		            responseMsg.getMessage(), ExceptionCode.GENERIC_ERROR);
		}
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		for (String msgId : responseMsg.getMessageIds()) {
			try {
				byteOut.write(this.retrieveResultData(msgId));
			} catch (IOException e) {
				throw new CoordinationException("Error creating result buffer: " + 
			            e.getMessage(),	ExceptionCode.GENERIC_ERROR, e);
			}
		}
		return byteOut.toByteArray();
	}
	
	private byte[] retrieveResultData(String id) throws CoordinationException {
		String path = this.getRootPath() + "/" + PEER_RESULTS_BASE_NAME + "/" + id;
		byte[] data = null;
		try {
			data = getZooKeeper().getData(path, false, null);
		} catch (KeeperException e) {
			throw new CoordinationException("Coordination error in retrieving peer result data", 
					ExceptionCode.GENERIC_ERROR, e);
		} catch (Exception e) {
			throw new CoordinationException("Unknown error in retrieving peer result data", 
					ExceptionCode.GENERIC_ERROR, e);
		}
		try {
			getZooKeeper().delete(path, -1);
		} catch (Exception ignore) {
			// ignore
		}
		return data;
	}

	@Override
	public void leave() throws CoordinationException {
		this.setActive(false);
		try {
			this.getZooKeeper().delete(this.getMemberPath(), -1);
		} catch (Exception e) {
			throw new CoordinationException(ExceptionCode.GENERIC_ERROR, e);
		}
	}

	@Override
	public GroupEventListener getGroupEventListener() {
		return groupEventListener;
	}
	
	@Override
	public void setGroupEventListener(GroupEventListener groupEventListener) {
		this.groupEventListener = groupEventListener;
	}

	@Override
	public String getMemberId() {
		return memberId;
	}

	@Override
	public String getGroupId() {
		return this.getId();
	}
	
	private boolean isCommPath(String path) {
		return path.startsWith(COMM_BASE_NAME);
	}
	
	@Override
	public void process(WatchedEvent event) {
		if (!this.isActive()) {
			return;
		}
		String path = event.getPath();

		if (!this.isCommPath(path)) {
			new Thread(new Runnable() {
				/* we are creating a new thread here because, from zookeeper, when we are
				 * in the same thread as the watch event happens, another watch event cannot
				 * be received later */
				public void run() {
					try {
						processMemberNodes();
					} catch (Exception e) {
						log.error("Error in processing WatchedEvent: "
										+ e.getMessage(), e);
					}
				}
			}).start();
		}
	}

	private List<String> lookupMemberIds(boolean notify) throws CoordinationException {
		try {
			List<String> ids = new ArrayList<String>();
			List<String> childPaths;
			if (notify) {
			    childPaths = this.getZooKeeper().getChildren(this.getRootPath(), this);
			} else {
				childPaths = this.getZooKeeper().getChildren(this.getRootPath(), false);
			}
			String tmpId;
			for (String path : childPaths) {
				tmpId = path.substring(path.lastIndexOf("/") + 1);
				if (!tmpId.equals(COMM_BASE_NAME)) {
				    ids.add(tmpId);
				}
			}
			Collections.sort(ids);
			return ids;
		} catch (Exception e) {
			throw new CoordinationException(ExceptionCode.GENERIC_ERROR, e);
		}
	}
	
	/**
	 * This class represents a communication channel, to be used for parsing messages in the group.
	 */
	private class CommunicationChannel implements Watcher {

		public static final int CHANNEL_TYPE_BROADCAST = 0;
		
		public static final int CHANNEL_TYPE_PEER = 1;
		
		private String commRootPath;
		
		private String lastProcessedMessageId;
		
		private int channelType;
		
		private boolean incoming;
		
		public CommunicationChannel(String commRootPath, int channelType, boolean incoming) 
				throws CoordinationException {
			this.commRootPath = commRootPath;
			this.channelType = channelType;
			this.incoming = incoming;
			createRecursive(this.getCommRootPath());
			try {
				/* we don't register a watch for getChildren because, it won't be scalable,
				 * where the data complexity of that operation is proportional group messages sent,
				 * i.e. getChildren result size = number of child nodes = number of messages sent.
				 * So the approach is, we do an update on the data of the root node, and others do a
				 * getData and registers a watch, so watch re-registration would be always be a constant
				 * time operation (we always update the data with 0 bytes) */
				if (this.isIncoming()) {
				    getZooKeeper().getData(this.getCommRootPath(), this, null);
				}
			} catch (Exception e) {
				throw new CoordinationException(ExceptionCode.GENERIC_ERROR, e);
			}
		}
		
		public boolean isIncoming() {
			return incoming;
		}
		
		public int getChannelType() {
			return channelType;
		}
		
		public String getCommRootPath() {
			return commRootPath;
		}
		
		/**
		 * Checks if the message data exists with the given id, if not, it will return null,
		 * it also deletes the node after the data is retrieved, if this is a peer message.
		 * @param msgId The message id to check for existence
		 * @return Node data if it exists, or else, null
		 */
		private byte[] validateAndReturnDataFromId(String msgId) throws Exception {
			String path = this.getCommRootPath() + "/" + msgId;
			byte[] data = null;
			try {
				data = getZooKeeper().getData(path, false, null);
				if (this.getChannelType() == CHANNEL_TYPE_PEER) {
					getZooKeeper().delete(path, -1);
				}
				return data;
			} catch (KeeperException e) {
				if (e.code() == Code.NONODE) {
					return data;
				}
				throw e;
			}
		}
		
		private byte[] getNextMessage() throws Exception {
			List<String> msgPaths = getZooKeeper().getChildren(this.getCommRootPath(), false);
			Collections.sort(msgPaths);
			if (this.lastProcessedMessageId == null) {
				if (msgPaths.size() > 0) {
					this.lastProcessedMessageId = msgPaths.get(msgPaths.size() - 1);
					return validateAndReturnDataFromId(this.lastProcessedMessageId);
				} else {
					return null;
				}
			} else {
				for (String path : msgPaths) {
					if (path.compareTo(this.lastProcessedMessageId) > 0) {
						this.lastProcessedMessageId = path;
						return validateAndReturnDataFromId(this.lastProcessedMessageId);
					}
				}
			}
			return null;
		}
		
		private void processMessageData(byte[] msgData) throws Exception {
			if (this.getChannelType() == CHANNEL_TYPE_BROADCAST) {
			    this.handleBroadcastMessage(msgData);
			} else if (this.getChannelType() == CHANNEL_TYPE_PEER) {
				this.handlePeerMessage(msgData);
			}
		}
		
		private void handleBroadcastMessage(byte[] data) {
			GroupEventListener listener = getGroupEventListener();
			if (listener != null) {
				listener.onGroupMessage(data);
			}
		}

		private void handlePeerMessage(byte[] data) {
			String correlationId = null;
			PeerResponseMessage responseMsg;
			try {
			    GroupEventListener listener = getGroupEventListener();
			    if (listener == null) {
				    throw new CoordinationException("No listener registered for peer requests");
			    }
			    ByteArrayInputStream byteIn = new ByteArrayInputStream(data);
			    PeerRequestMessage requestMsg;
			    try {
				    requestMsg = (PeerRequestMessage) getPeerRequestUnmarshaller().unmarshal(byteIn);
			    } catch (JAXBException e) {
				    throw new CoordinationException("Error in unmarshalling peer message", 
						    ExceptionCode.GENERIC_ERROR, e);
			    }
			    /* get the request messages correlation id, 
			     * this correlation id is used to create the response message */
			    correlationId = requestMsg.getCorrelationId();
			    byte[] requestData = requestMsg.getData();
			    byte[] responseData = listener.onPeerMessage(requestData);
			    /* the response is broken down to several data chunks and separate znodes
			     * are created for each, and the node ids in the result area are returned */
			    List<String> dataNodeIds = this.createResultDataNodesFromData(responseData);
			    /* a peer response message is created with the created data node ids, and
			     * the response message is serialized to an znode with the earlier correlation id */
			    responseMsg = new PeerResponseMessage();
			    responseMsg.setMessageIds(dataNodeIds.toArray(new String[dataNodeIds.size()]));
			    responseMsg.setSuccess(true);
			    this.createResultDataNode(correlationId, this.marshalPeerResponse(responseMsg));
			} catch (Exception e) {
				if (correlationId != null) {
					/* send back the error message to the caller */
					responseMsg = new PeerResponseMessage();
					responseMsg.setSuccess(false);
					responseMsg.setMessage(e.getMessage());
					try {
					    this.createResultDataNode(correlationId, this.marshalPeerResponse(responseMsg));
					} catch (Exception e2) {
						log.error("Error in creating peer error result node: " +
					            e2.getMessage(), e2);
					}
				}
			}
		}
		
		private byte[] marshalPeerResponse(PeerResponseMessage responseMsg) throws Exception {
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		    getPeerResponseMarshaller().marshal(responseMsg, byteOut);
		    return byteOut.toByteArray();
		}
		
		private List<String> createResultDataNodesFromData(byte[] data) throws Exception {
			List<byte[]> dataList = new ArrayList<byte[]>();
			byte[] tmpBuff;
			for (int i = 0; i < data.length;) {
				tmpBuff = Arrays.copyOfRange(data, i, Math.min(data.length, i + 
						ZKCoordinationService.MAX_ZK_MESSAGE_SIZE));
				dataList.add(tmpBuff);
				i += ZKCoordinationService.MAX_ZK_MESSAGE_SIZE;
			}
			List<String> idList = new ArrayList<String>();
			String tmpId;
			for (byte[] buff : dataList) {
				tmpId = UUID.randomUUID().toString();
				this.createResultDataNode(tmpId, buff);
				idList.add(tmpId);
			}
			return idList;
		}
		
		private void createResultDataNode(String id, byte[] data) throws Exception {
			String path = getZooKeeper().create(getRootPath() + "/" + PEER_RESULTS_BASE_NAME + "/" + id, data, 
					Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
			ZKCoordinationService.scheduleTimedZNodeDeletion(path);
		}
		
		@Override
		public void process(WatchedEvent event) {
			byte[] msgData;
			try {
				getZooKeeper().getData(this.getCommRootPath(), this, null);
			    while ((msgData = this.getNextMessage()) != null) {
			    	this.processMessageData(msgData);
			    }
			} catch (Exception e) {
				log.error("Error in receiving group messages: " + e.getMessage(), e);
			}
		}
		
		public void sendMessage(byte[] data) throws CoordinationException {
			try {
				/* insert the data node */
				String path = getZooKeeper().create(this.getCommRootPath() + "/node", data, 
						Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
				/* schedule for deletion when a specific timeout happens */
				ZKCoordinationService.scheduleTimedZNodeDeletion(path);
				/* this notifies the others */
				getZooKeeper().setData(this.getCommRootPath(), new byte[0], -1);
			} catch (Exception e) {
				throw new CoordinationException(ExceptionCode.GENERIC_ERROR, e);
			}
		}
		
		public void clearMessages() throws CoordinationException {
			List<String> msgIds;
			try {
				msgIds = getZooKeeper().getChildren(this.getCommRootPath(), false);
			} catch (Exception e) {
				throw new CoordinationException(ExceptionCode.GENERIC_ERROR, e);
			}
			for (String id : msgIds) {
				try {
					getZooKeeper().delete(this.getCommRootPath() + "/" + id, -1);
				} catch (KeeperException e) {
					if (e.code() != Code.NONODE) {
						throw new CoordinationException(ExceptionCode.GENERIC_ERROR, e);
					}
				} catch (Exception e) {
					throw new CoordinationException(ExceptionCode.GENERIC_ERROR, e);
				}
			}
		}
		
	}

	@Override
	public void clearGroupMessages() throws CoordinationException {
		this.getGroupCommChannel().clearMessages();
	}
	
	/**
	 * This class represents a request message sent to a peer.
	 */
	@XmlRootElement (name = "peerRequestMessage")
	public static class PeerRequestMessage {
		
		private String correlationId;
		
		private byte[] data;
		
		public PeerRequestMessage() {
		}

		public String getCorrelationId() {
			return correlationId;
		}

		public void setCorrelationId(String correlationId) {
			this.correlationId = correlationId;
		}

		public byte[] getData() {
			return data;
		}

		public void setData(byte[] data) {
			this.data = data;
		}

	}
	
	/**
	 * This class represents a response message sent to a peer.
	 */
	@XmlRootElement (name = "peerResponseMessage")
	public static class PeerResponseMessage {
		
		private boolean success;
		
		private String message;
		
		private String[] messageIds;
		
		public PeerResponseMessage() {
			/* by default, successful reply */
			this.success = true;
		}

		public String[] getMessageIds() {
			return messageIds;
		}

		public void setMessageIds(String[] messageIds) {
			this.messageIds = messageIds;
		}

		public boolean isSuccess() {
			return success;
		}

		public void setSuccess(boolean success) {
			this.success = success;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
		
	}
	
	public class PollingDataNode implements Watcher {
		
		public static final int TOTAL_TIMEOUT = 1000 * 60 * 2;
		
		public static final int CHECK_ALIVE_TIMEOUT = 1000 * 5;
		
		private Object lock = new Object();
		
		private byte[] data;
		
		private boolean ready;
		
		private String dataPath;
		
		private String testPath;

		public PollingDataNode(String dataPath, String testPath) throws Exception {
			this.dataPath = dataPath;
			this.testPath = testPath;
			Stat stat = getZooKeeper().exists(this.getDataPath(), this);
			if (stat != null) {
				try {
				    this.data = getZooKeeper().getData(dataPath, false, null);
				    this.ready = true;
				} finally {
					/* this will delete the data node and also remove the watch,
					 * since the registered watch gets triggered with the delete */
					try {
					    getZooKeeper().delete(dataPath, -1);
					} catch (Exception ignore) {
						// ignore
					}
				}
			}
		}
		
		public String getDataPath() {
			return dataPath;
		}
		
		public String getTestPath() {
			return testPath;
		}
		
		public byte[] getData() {
			return data;
		}
		
		public boolean checkTestPath() {
			if (this.getTestPath() != null) {
				try {
					return getZooKeeper().exists(this.getTestPath(), false) != null;
				} catch (Exception e) {
					return false;
				}
			}
			return true;
		}
		
		public boolean waitForData() throws Exception {
			int totalTime = 0;
			while (true) {
				synchronized (this.lock) {
					if (this.isReady()) {
						return true;
					}
					this.lock.wait(CHECK_ALIVE_TIMEOUT);
					if (this.isReady()) {
						return true;
					} else {
						if (!this.checkTestPath()) {
							return false;
						}
					}
				}
				totalTime += CHECK_ALIVE_TIMEOUT;
				if (totalTime > TOTAL_TIMEOUT) {
					this.cleanupDataNode();
					return false;
				}
			}
		}
		
		private void cleanupDataNode() {
			/* since no one is creating the data node, we've to create
			 * and delete it to clear the watch */
			try {
			    getZooKeeper().create(this.getDataPath(), new byte[0], Ids.OPEN_ACL_UNSAFE, 
					CreateMode.EPHEMERAL);
			} catch (Exception ignore) {
				// ignore
			}
			try {
			    getZooKeeper().delete(this.getDataPath(), -1);
			} catch (Exception ignore) {
				// ignore
			}
		}
		
		public boolean isReady() {
			return ready;
		}
		
		@Override
		public void process(WatchedEvent event) {
			synchronized (this.lock) {
				try {
				    this.data = getZooKeeper().getData(this.getDataPath(), false, null);
				    getZooKeeper().delete(dataPath, -1);
				} catch (Exception ignore) {
					// ignore
				}
				this.ready = true;
				this.lock.notifyAll();
			}
		}
		
	}

	@Override
	public void waitForMemberCount(int count) throws CoordinationException {
		synchronized (this.memberArrivalCountLock) {
			while (this.getMemberIds().size() < count) {
				try {
					this.memberArrivalCountLock.wait();
				} catch (InterruptedException ignore) {
					// ignore
				}
			}
		}
	}
	
}
