/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.clustering.tribes;

import org.apache.axis2.clustering.ClusteringCommand;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.MessageSender;
import org.apache.catalina.tribes.ByteMessage;
import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.Member;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;

public class ChannelSender implements MessageSender {

    private static Log log = LogFactory.getLog(ChannelSender.class);
    private Channel channel;
    private boolean synchronizeAllMembers;
    private MembershipManager membershipManager;

    public ChannelSender(Channel channel,
                         MembershipManager membershipManager,
                         boolean synchronizeAllMembers) {
        this.channel = channel;
        this.membershipManager = membershipManager;
        this.synchronizeAllMembers = synchronizeAllMembers;
    }

    public synchronized void sendToGroup(ClusteringCommand msg,
                            MembershipManager membershipManager,
                            int additionalOptions) throws ClusteringFault {
        if (channel == null) {
            return;
        }
        Member[] members = membershipManager.getMembers();

        // Keep retrying, since at the point of trying to send the msg, a member may leave the group
        // causing a view change. All nodes in a view should get the msg
        if (members.length > 0) {
            try {
                if (synchronizeAllMembers) {
                    channel.send(members, toByteMessage(msg),
                                 Channel.SEND_OPTIONS_USE_ACK |
                                 Channel.SEND_OPTIONS_SYNCHRONIZED_ACK |
                                 Channel.SEND_OPTIONS_BYTE_MESSAGE |
                                 TribesConstants.MSG_ORDER_OPTION |
                                 TribesConstants.AT_MOST_ONCE_OPTION |
                                 additionalOptions);
                } else {
                    channel.send(members, toByteMessage(msg),
                                 Channel.SEND_OPTIONS_ASYNCHRONOUS |
                                 TribesConstants.MSG_ORDER_OPTION |
                                 Channel.SEND_OPTIONS_BYTE_MESSAGE |
                                 TribesConstants.AT_MOST_ONCE_OPTION |
                                 additionalOptions);
                }
                if (log.isDebugEnabled()) {
                    log.debug("Sent " + msg + " to group");
                }
            } catch (NotSerializableException e) {
                String message = "Could not send command message " + msg +
                                 " to group since it is not serializable.";
                log.error(message, e);
                throw new ClusteringFault(message, e);
            } catch (ChannelException e) {
                log.error("Could not send message to some members", e);
                ChannelException.FaultyMember[] faultyMembers = e.getFaultyMembers();
                for (ChannelException.FaultyMember faultyMember : faultyMembers) {
                    Member member = faultyMember.getMember();
                    log.error("Member " + TribesUtil.getName(member) + " is faulty",
                              faultyMember.getCause());
                }
            } catch (Exception e) {
                String message = "Error sending command message : " + msg +
                                 ". Reason " + e.getMessage();
                log.warn(message, e);
            }
        }
    }

    public void sendToGroup(ClusteringCommand msg) throws ClusteringFault {
         sendToGroup(msg, this.membershipManager, 0);
    }

    private ByteMessage toByteMessage(ClusteringCommand msg) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(msg);
        out.flush();
        out.close();
        return new ByteMessage(bos.toByteArray());
    }

    public void sendToSelf(ClusteringCommand msg) throws ClusteringFault {
        if (channel == null) {
            return;
        }
        try {
            channel.send(new Member[]{channel.getLocalMember(true)},
                         toByteMessage(msg),
                         Channel.SEND_OPTIONS_USE_ACK |
                         Channel.SEND_OPTIONS_BYTE_MESSAGE);
            if (log.isDebugEnabled()) {
                log.debug("Sent " + msg + " to self");
            }
        } catch (Exception e) {
            throw new ClusteringFault(e);
        }
    }

    public void sendToMember(ClusteringCommand cmd, Member member) throws ClusteringFault {
        try {
            if (member.isReady()) {
                channel.send(new Member[]{member}, toByteMessage(cmd),
                             Channel.SEND_OPTIONS_USE_ACK |
                             Channel.SEND_OPTIONS_SYNCHRONIZED_ACK |
                             Channel.SEND_OPTIONS_BYTE_MESSAGE |
                             TribesConstants.MSG_ORDER_OPTION |
                             TribesConstants.AT_MOST_ONCE_OPTION);
                if (log.isDebugEnabled()) {
                    log.debug("Sent " + cmd + " to " + TribesUtil.getName(member));
                }
            }
        } catch (NotSerializableException e) {
            String message = "Could not send command message to " + TribesUtil.getName(member) +
                             " since it is not serializable.";
            log.error(message, e);
            throw new ClusteringFault(message, e);
        } catch (ChannelException e) {
            log.error("Could not send message to " + TribesUtil.getName(member));
            ChannelException.FaultyMember[] faultyMembers = e.getFaultyMembers();
            log.error("Member " + TribesUtil.getName(member) + " is faulty",
                      faultyMembers[0].getCause());
        } catch (Exception e) {
            String message = "Could not send message to " + TribesUtil.getName(member) +
                             ". Reason " + e.getMessage();
            log.warn(message, e);
        }
    }
}
