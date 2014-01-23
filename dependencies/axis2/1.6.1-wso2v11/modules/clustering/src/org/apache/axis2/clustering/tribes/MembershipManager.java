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

import org.apache.axis2.clustering.ClusteringConstants;
import org.apache.axis2.clustering.MembershipListener;
import org.apache.axis2.clustering.control.wka.MemberJoinedCommand;
import org.apache.axis2.clustering.control.wka.MemberListCommand;
import org.apache.axis2.clustering.management.GroupManagementAgent;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.RemoteProcessException;
import org.apache.catalina.tribes.group.GroupChannel;
import org.apache.catalina.tribes.group.Response;
import org.apache.catalina.tribes.group.RpcChannel;
import org.apache.catalina.tribes.group.interceptors.StaticMembershipInterceptor;
import org.apache.catalina.tribes.membership.MemberImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Responsible for managing the membership. Handles membership changes.
 */
public class MembershipManager {

    private static final Log log = LogFactory.getLog(MembershipManager.class);

    private RpcChannel rpcMembershipChannel;
    private StaticMembershipInterceptor staticMembershipInterceptor;

    /**
     * The domain corresponding to the membership handled by this MembershipManager
     */
    private byte[] domain;
    private GroupManagementAgent groupManagementAgent;
    private ConfigurationContext configContext;


    /**
     * List of current members in the cluster. Only the members who are alive will be in this
     * list
     */
    private final List<Member> members = new ArrayList<Member>();

    /**
     * List of Well-Known members. These members may or may not be alive at a given moment.
     */
    private final List<Member> wkaMembers = new ArrayList<Member>();

    /**
     * Set of Well-Known members which have not responded to the MEMBER_LIST message.
     * We need to retry sending the MEMBER_LIST message to these members until they respond,
     * otherwise, we cannot be sure whether these WKA members added the members in the MEMBER_LIST
     */
    private final Set<Member> nonRespondingWkaMembers = new CopyOnWriteArraySet<Member>();

    private List<MembershipListener> membershipListeners = new ArrayList<MembershipListener>();

    /**
     * The member representing this node
     */
    private Member localMember;

    /**
     *
     */
    private boolean isMemberListResponseReceived;

    public MembershipManager(ConfigurationContext configContext) {
        this.configContext = configContext;
    }

    public MembershipManager() {
    }

    public void setMembershipListeners(List<MembershipListener> membershipListeners) {
        this.membershipListeners = membershipListeners;
    }

    public void setRpcMembershipChannel(RpcChannel rpcMembershipChannel) {
        this.rpcMembershipChannel = rpcMembershipChannel;
    }

    public RpcChannel getRpcMembershipChannel() {
        return rpcMembershipChannel;
    }

    public void setupStaticMembershipManagement(StaticMembershipInterceptor staticMembershipInterceptor) {
        this.staticMembershipInterceptor = staticMembershipInterceptor;
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleWithFixedDelay(new MemberListSenderTask(), 5, 5, TimeUnit.SECONDS);
    }

    public void setGroupManagementAgent(GroupManagementAgent groupManagementAgent) {
        this.groupManagementAgent = groupManagementAgent;
    }

    public void setDomain(byte[] domain) {
        this.domain = domain;
    }

    public byte[] getDomain() {
        return domain;
    }

    public Member getLocalMember() {
        return localMember;
    }

    public void setLocalMember(Member localMember) {
        this.localMember = localMember;
    }

    public void addWellKnownMember(Member wkaMember) {
        if(!wkaMembers.contains(wkaMember)) wkaMembers.add(wkaMember);
    }

    public void removeWellKnownMember(Member wkaMember) {
        wkaMembers.remove(wkaMember);
    }

    /**
     * A new member is added
     *
     * @param member The new member that joined the cluster
     * @return true  If the member was added to the <code>members</code> array; false, otherwise.
     */
    public boolean memberAdded(Member member) {

        if (log.isDebugEnabled()) {
            log.debug("members.contains(member) =" + members.contains(member));
            log.debug("Is in my domain: " + TribesUtil.isInDomain(member, domain));
        }

        // If this member already exists or if the member belongs to another domain,
        // there is no need to add it
        if (members.contains(member) || !TribesUtil.isInDomain(member, domain)) {
            return false;
        }

        if (staticMembershipInterceptor != null) { // this interceptor is null when multicast based scheme is used
            staticMembershipInterceptor.addStaticMember(member);
            if (log.isDebugEnabled()) {
                log.debug("Added static member " + TribesUtil.getName(member));
            }
        }

        boolean shouldAddMember = localMember == null ||
                                  TribesUtil.areInSameDomain(localMember, member);

        // If this member is a load balancer, notify the respective load balance event handler?
		if (groupManagementAgent != null) {

			synchronized (groupManagementAgent) {
				int memberCount = groupManagementAgent.getMembers().size();

				groupManagementAgent.applicationMemberAdded(TribesUtil
						.toAxis2Member(member));

				if (memberCount == groupManagementAgent.getMembers().size()) {
					// we shouldn't add the member, if the GroupManagementAgent
					// refuses to add it.
					shouldAddMember = false;
				} else {
					log.info("Application member " + TribesUtil.getName(member)
							+ " joined group " + new String(member.getDomain()));

				}
			}
		}

        if (shouldAddMember) {
            boolean wkaMemberBelongsToLocalDomain = true;
            if (rpcMembershipChannel != null && isLocalMemberInitialized() &&
                wkaMembers.contains(member)) { // if it is a well-known member

                log.info("A WKA member " + TribesUtil.getName(member) +
                         " just joined the group. Sending MEMBER_LIST message.");
                wkaMemberBelongsToLocalDomain = sendMemberListToWellKnownMember(member);
            }
            if (wkaMemberBelongsToLocalDomain) {
                members.add(member);
                if (rpcMembershipChannel != null) {
                    GroupChannel channel = (GroupChannel) rpcMembershipChannel.getChannel();
                    channel.memberAdded(member);
                    log.debug("Member added to the channel: "+member);
                }
                for (MembershipListener membershipListener : membershipListeners) {
                    membershipListener.memberAdded(TribesUtil.toAxis2Member(member), false);
                }
                if (log.isDebugEnabled()) {
                    log.debug("Added group member " + TribesUtil.getName(member) + " to domain " +
                              new String(member.getDomain()));
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Task which send MEMBER_LIST messages to WKA members which have not yet responded to the
     * MEMBER_LIST message
     */
    private class MemberListSenderTask implements Runnable {
        public void run() {
            try {
                if (nonRespondingWkaMembers != null && !nonRespondingWkaMembers.isEmpty()) {
                    for (Member wkaMember : nonRespondingWkaMembers) {
                        if (wkaMember != null) {
                            sendMemberListToWellKnownMember(wkaMember);
                        }
                    }
                }
            } catch (Throwable e) {
                log.error("Could not send MemberList to WKA Members", e);
            }
        }
    }

    /**
     * Send MEMBER_LIST message to WKA member
     *
     * @param wkaMember The WKA member to whom the MEMBER_LIST has to be sent
     * @return true - if the WKA member belongs to the domain of this local member
     */
    private boolean sendMemberListToWellKnownMember(Member wkaMember) {
        /*if (wkaMember.isFailing() || wkaMember.isSuspect()) {
            return false;
        }*/
        // send the member list to it
        MemberListCommand memListCmd;
        try {
            memListCmd = new MemberListCommand();
            List<Member> members = new ArrayList<Member>(this.members);
            members.add(localMember); // Need to set the local member too
            memListCmd.setMembers(members.toArray(new Member[members.size()]));

            Response[] responses =
                    rpcMembershipChannel.send(new Member[]{wkaMember}, memListCmd,
                                              RpcChannel.ALL_REPLY,
                                              Channel.SEND_OPTIONS_ASYNCHRONOUS |
                                              TribesConstants.MEMBERSHIP_MSG_OPTION, 10000);

            // Once a response is received from the WKA member to the MEMBER_LIST message,
            // if it does not belong to this domain, simply remove it from the members
            if (responses != null && responses.length > 0 && responses[0] != null) {
                nonRespondingWkaMembers.remove(wkaMember);
                Member source = responses[0].getSource();
                if (!TribesUtil.areInSameDomain(source, wkaMember)) {
                    if (log.isDebugEnabled()) {
                        log.debug("WKA Member " + TribesUtil.getName(source) +
                                  " does not belong to local domain " + new String(domain) +
                                  ". Hence removing it from the list.");
                    }
                    return false;
                }
            } else { // No response from WKA member
                if(nonRespondingWkaMembers.add(wkaMember)){
                    log.debug("No response from WKA member added to the list of non-responsive members : " + wkaMember);
                }
                // we still don't know the details of this WKA member, hence should return false
                return false;
            }
        } catch (Exception e) {
            String errMsg = "Could not send MEMBER_LIST to well-known member " +
                            TribesUtil.getName(wkaMember);
            log.error(errMsg, e);
            throw new RemoteProcessException(errMsg, e);
        }
        return true;
    }

    /**
     * Send the list of members to the <code>member</code>
     *
     * @param member The member to whom the member list has to be sent
     */
    public void sendMemberList(Member member) {
        try {
            MemberListCommand memListCmd = new MemberListCommand();
            List<Member> members = new ArrayList<Member>(this.members);
            memListCmd.setMembers(members.toArray(new Member[members.size()]));
            rpcMembershipChannel.send(new Member[]{member}, memListCmd, RpcChannel.ALL_REPLY,
                                      Channel.SEND_OPTIONS_ASYNCHRONOUS |
                                      TribesConstants.MEMBERSHIP_MSG_OPTION, 10000);
            if (log.isDebugEnabled()) {
                log.debug("Sent MEMBER_LIST to " + TribesUtil.getName(member));
            }
        } catch (Exception e) {
            String errMsg = "Could not send MEMBER_LIST to member " + TribesUtil.getName(member);
            log.error(errMsg, e);
            throw new RemoteProcessException(errMsg, e);
        }
    }

    /**
     * Inform all members that a particular member just joined
     *
     * @param member The member who just joined
     */
    public void sendMemberJoinedToAll(Member member) {
        try {

            MemberJoinedCommand cmd = new MemberJoinedCommand();
            cmd.setMember(member);
            ArrayList<Member> membersToSend = (ArrayList<Member>) (((ArrayList) members).clone());
            membersToSend.remove(member); // Do not send MEMBER_JOINED to the new member who just joined

            if (membersToSend.size() > 0) {
                rpcMembershipChannel.send(membersToSend.toArray(new Member[membersToSend.size()]), cmd,
                                          RpcChannel.ALL_REPLY,
                                          Channel.SEND_OPTIONS_ASYNCHRONOUS |
                                          TribesConstants.MEMBERSHIP_MSG_OPTION,
                                          10000);
                if (log.isDebugEnabled()) {
                    log.debug("Sent MEMBER_JOINED[" + TribesUtil.getName(member) +
                              "] to all members in domain " + new String(domain));
                }
            }
        } catch (Exception e) {
            String errMsg = "Could not send MEMBER_JOINED[" + TribesUtil.getName(member) +
                            "] to all members ";
            log.error(errMsg, e);
            throw new RemoteProcessException(errMsg, e);
        }
    }

    private boolean isLocalMemberInitialized() {
        if (configContext == null) {
            return false;
        }
        Object clusterInitialized =
                configContext.getPropertyNonReplicable(ClusteringConstants.CLUSTER_INITIALIZED);
        return clusterInitialized != null && clusterInitialized.equals("true");
    }

    /**
     * A member disappeared
     *
     * @param member The member that left the cluster
     */
    public void memberDisappeared(Member member) {
        members.remove(member);
        nonRespondingWkaMembers.remove(member);
        if (member != null && localMember != null) {
            if(TribesUtil.areInSameDomain(member, this.localMember)){
                for (MembershipListener membershipListener : membershipListeners) {
                    membershipListener.memberDisappeared(TribesUtil.toAxis2Member(member), false);
                }
            }
        }

        // Is this an application domain member?
        if (groupManagementAgent != null) {
            groupManagementAgent.applicationMemberRemoved(TribesUtil.toAxis2Member(member));
        }
    }

    /**
     * Get the list of current members
     *
     * @return list of current members
     */
    public Member[] getMembers() {
        return members.toArray(new Member[members.size()]);
    }
    
    public void removeAllMembers(){
        members.clear();
    }

    /**
     * Get the member that has been alive for the longest time
     *
     * @return The member that has been alive for the longest time
     */
    public Member getLongestLivingMember() {
        Member longestLivingMember = null;
        if (members.size() > 0) {
            Member member0 = members.get(0);
            long longestAliveTime = member0.getMemberAliveTime();
            longestLivingMember = member0;
            for (Member member : members) {
                if (longestAliveTime < member.getMemberAliveTime()) {
                    longestAliveTime = member.getMemberAliveTime();
                    longestLivingMember = member;
                }
            }
        }
        return longestLivingMember;
    }

    /**
     * Get a random member from the list of current members
     *
     * @return A random member from the list of current members
     */
    public Member getRandomMember() {
        if (members.size() == 0) {
            return null;
        }
        int memberIndex = new Random().nextInt(members.size());
        return members.get(memberIndex);
    }

    /**
     * Check whether there are any members
     *
     * @return true if there are other members, false otherwise
     */
    public boolean hasMembers() {
        return members.size() > 0;
    }

    /**
     * Get a member
     *
     * @param member The member to be found
     * @return The member, if it is found
     */
    public Member getMember(Member member) {
        if (hasMembers()) {
            MemberImpl result = null;
            for (int i = 0; i < this.members.size() && result == null; i++) {
                if (members.get(i).equals(member)) {
                    result = (MemberImpl) members.get(i);
                }
            }
            return result;
        }
        return null;
    }
}
