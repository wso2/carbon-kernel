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

package org.apache.axis2.clustering.control.wka;

import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.tribes.MembershipManager;
import org.apache.axis2.clustering.tribes.TribesUtil;
import org.apache.axis2.clustering.tribes.WkaBasedMembershipScheme;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.RemoteProcessException;
import org.apache.catalina.tribes.group.RpcCallback;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;

/**
 * Handles RPC membership requests from members. This is used only in conjunction with WKA based
 * membership mamangement
 */
public class RpcMembershipRequestHandler implements RpcCallback {

    private static Log log = LogFactory.getLog(RpcMembershipRequestHandler.class);
    private MembershipManager membershipManager;
    private WkaBasedMembershipScheme membershipScheme;

    public RpcMembershipRequestHandler(MembershipManager membershipManager,
                                       WkaBasedMembershipScheme membershipScheme) {
        this.membershipManager = membershipManager;
        this.membershipScheme = membershipScheme;
    }

    public Serializable replyRequest(Serializable msg, Member sender) {
        String domain = new String(sender.getDomain());

        if (log.isDebugEnabled()) {
            log.debug("Membership request received by RpcMembershipRequestHandler for domain " +
                      domain);
            log.debug("local domain: " + new String(membershipManager.getDomain()));
        }

        if (msg instanceof JoinGroupCommand) {
            log.info("Received JOIN message from " + TribesUtil.getName(sender));
            
            // process this join request - handing this job over to a new thread,
            // since the reply doesn't need to contain the member we're just going to
            // add.
            Thread th = new Thread(new ProcessJoinCommand(sender));
            th.setPriority(Thread.MAX_PRIORITY);
            th.start();

            // Return the list of current members to the caller
            MemberListCommand memListCmd = new MemberListCommand();
            memListCmd.setMembers(membershipManager.getMembers());
            if(log.isDebugEnabled()){
                log.debug("Sent MEMBER_LIST to " + TribesUtil.getName(sender));
            }
            return memListCmd;
        } else if (msg instanceof MemberJoinedCommand) {
            log.info("Received MEMBER_JOINED message from " + TribesUtil.getName(sender));
            MemberJoinedCommand command = (MemberJoinedCommand) msg;
            if(log.isDebugEnabled()){
                log.debug(command);
            }

            try {
                command.setMembershipManager(membershipManager);
                command.execute(null);
            } catch (ClusteringFault e) {
                String errMsg = "Cannot handle MEMBER_JOINED notification";
                log.error(errMsg, e);
                throw new RemoteProcessException(errMsg, e);
            }
        } else if (msg instanceof MemberListCommand) {
            try {                    //TODO: What if we receive more than one member list message?
                log.info("Received MEMBER_LIST message from " + TribesUtil.getName(sender));
                MemberListCommand command = (MemberListCommand) msg;
                command.setMembershipManager(membershipManager);
                command.execute(null);

                return "Processed MEMBER_LIST message";
                //TODO Send MEMBER_JOINED messages to all nodes
            } catch (ClusteringFault e) {
                String errMsg = "Cannot handle MEMBER_LIST message from " +
                                TribesUtil.getName(sender);
                log.error(errMsg, e);
                throw new RemoteProcessException(errMsg, e);
            }
        }
        return null;
    }

    public void leftOver(Serializable msg, Member member) {
        //TODO: Method implementation

    }
    
    private class ProcessJoinCommand implements Runnable {

        private Member sender;
        
        public ProcessJoinCommand(Member sender) {
            this.sender = sender;
        }
        
        public void run() {
            membershipManager.memberAdded(sender);

            // do something specific for the membership scheme
            membershipScheme.processJoin(sender);
        }
        
    }
}