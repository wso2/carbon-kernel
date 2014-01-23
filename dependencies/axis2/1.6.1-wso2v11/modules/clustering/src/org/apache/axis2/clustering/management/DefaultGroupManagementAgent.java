/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
 *                                                                             
 * Licensed under the Apache License, Version 2.0 (the "License");             
 * you may not use this file except in compliance with the License.            
 * You may obtain a copy of the License at                                     
 *                                                                             
 *      http://www.apache.org/licenses/LICENSE-2.0                             
 *                                                                             
 * Unless required by applicable law or agreed to in writing, software         
 * distributed under the License is distributed on an "AS IS" BASIS,           
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.    
 * See the License for the specific language governing permissions and         
 * limitations under the License.                                              
 */
package org.apache.axis2.clustering.management;

import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.Member;
import org.apache.axis2.clustering.tribes.ChannelSender;
import org.apache.axis2.clustering.tribes.MembershipManager;
import org.apache.axis2.clustering.tribes.TribesConstants;
import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.group.RpcChannel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * The default implementation of {@link GroupManagementAgent}
 */
public class DefaultGroupManagementAgent implements GroupManagementAgent {

    private static final Log log = LogFactory.getLog(DefaultGroupManagementAgent.class);
    private static final String PORT_MAPPING_PREFIX = "port.mapping.";
    private final List<Member> members = new ArrayList<Member>();
    private ChannelSender sender;
    private MembershipManager membershipManager;
    private RpcChannel rpcChannel; //TODO
    private String description;
    private String domain;
    private String subDomain;

    public void setSender(ChannelSender sender) {
        this.sender = sender;
    }

    public void setMembershipManager(MembershipManager membershipManager) {
        this.membershipManager = membershipManager;
    }

	public MembershipManager getMembershipManager(){
        return membershipManager;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getSubDomain() {
        return subDomain;
    }

    public void setSubDomain(String subDomain) {
        this.subDomain = subDomain;
    }

    public void setGroupMgtPort(int groupMgtPort) {
        // Nothing to do
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void applicationMemberAdded(Member member) {
        if (!members.contains(member)) {
            Thread th = new Thread(new MemberAdder(member));
            th.setPriority(Thread.MAX_PRIORITY);
            th.start();
			try {
                th.join();
            } catch (InterruptedException ignore) {
            }
        }
    }

	public void applicationMemberRemoved(Member member) {
		if (members.remove(member)) {
			log.info("Application member " + member + " left cluster.");
		}
	}

    public List<Member> getMembers() {
        return members;
    }

    public void addMember(Member member){
       // Nothing to do
    }

    public void send(GroupManagementCommand command) throws ClusteringFault {
        sender.sendToGroup(command,
                           membershipManager,
                           Channel.SEND_OPTIONS_ASYNCHRONOUS |
                           TribesConstants.MEMBERSHIP_MSG_OPTION);
    }

    private class MemberAdder implements Runnable {

        private final Member member;

        private MemberAdder(Member member) {
            this.member = member;
        }

        public void run() {
            if (members.contains(member)) {
                return;
            }
            if (canConnect(member)) {
                try {
                    Thread.sleep(10000);   // Sleep for sometime to allow complete initialization of the node
                } catch (InterruptedException ignored) {
                }
                // there's a possibility that this member leaves the cluster, while the thread at sleep.
                if(!canConnect(member)){
                	log.error("Could not add application member " + member);
                	return;
                }
                if (!members.contains(member)) {
                    members.add(member);
                    log.info("Application member " + member + " joined application cluster");
                }
            } else {
                log.error("Could not add application member " + member);
            }
        }

        /**
         * Before adding a member, we will try to verify whether we can connect to it
         *
         * @param member The member whose connectvity needs to be verified
         * @return true, if the member can be contacted; false, otherwise.
         */
        private boolean canConnect(Member member) {
            if (log.isDebugEnabled()) {
                log.debug("Trying to connect to member " + member + "...");
            }
            for (int retries = 30; retries > 0; retries--) {
                try {
                    InetAddress addr = InetAddress.getByName(member.getHostName());
                    
                    // keep mapped ports
                    List<String> mappedPorts = new ArrayList<String>();
                    
                    // extract mapped ports
                    Properties memberProperties = member.getProperties();
                    for (String property : memberProperties.stringPropertyNames()) {
                        if(property.contains(PORT_MAPPING_PREFIX)){
                            mappedPorts.add(memberProperties.getProperty(property));
                        }
                    }
                    
                    // checking the connection to the mapped ports
                    for (String portStr : mappedPorts) {
                        if (log.isDebugEnabled()) {
                            log.debug("Mapped Port=" + portStr);
                        }
                        int port = Integer.parseInt(portStr);
                        SocketAddress httpSockaddr = new InetSocketAddress(addr, port);
                        new Socket().connect(httpSockaddr, 10000);
                    }
                    
                    int httpPort = member.getHttpPort();
                    if (log.isDebugEnabled()) {
                        log.debug("HTTP Port=" + httpPort);
                    }
                    if (httpPort != -1) {
                        SocketAddress httpSockaddr = new InetSocketAddress(addr, httpPort);
                        new Socket().connect(httpSockaddr, 10000);
                    }
                    int httpsPort = member.getHttpsPort();
                    if (log.isDebugEnabled()) {
                        log.debug("HTTPS Port=" + httpsPort);
                    }
                    if (httpsPort != -1) {
                        SocketAddress httpsSockaddr = new InetSocketAddress(addr, httpsPort);
                        new Socket().connect(httpsSockaddr, 10000);
                    }
                    if(httpPort == -1 && httpsPort == -1){
                        return false;
                    }
                    return true;
                } catch (IOException e) {
                    if (log.isDebugEnabled()) {
                        log.debug("", e);
                    }
                    String msg = e.getMessage();
                    if (!msg.contains("Connection refused") && !msg.contains("connect timed out")) {
                        log.error("Cannot connect to member " + member, e);
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
            return false;
        }
    }
}
