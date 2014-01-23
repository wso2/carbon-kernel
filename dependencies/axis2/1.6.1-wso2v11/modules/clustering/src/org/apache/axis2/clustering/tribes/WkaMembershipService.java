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
package org.apache.axis2.clustering.tribes;

import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.ChannelMessage;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.MembershipListener;
import org.apache.catalina.tribes.MembershipService;
import org.apache.catalina.tribes.membership.StaticMember;
import org.apache.catalina.tribes.util.UUIDGenerator;

import java.io.IOException;
import java.util.Properties;

/**
 * This is the MembershipService which manages group membership based on a Well-Known Addressing (WKA)
 * scheme.
 */
public class WkaMembershipService implements MembershipService {

    private final MembershipManager membershipManager;


    /**
     * The implementation specific properties
     */
    protected Properties properties = new Properties();

    /**
     * This payload contains some membership information, such as some member specific properties
     * e.g. HTTP/S ports
     */
    protected byte[] payload;

    /**
     * The domain name of this cluster
     */
    protected byte[] domain;

    public WkaMembershipService(MembershipManager membershipManager) {
        this.membershipManager = membershipManager;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public Properties getProperties() {
        return properties;
    }

    public void start() throws Exception {
        // Nothing to do here
    }

    public void start(int i) throws Exception {
        // Nothing to do here
    }

    public void stop(int i) {
        // Nothing to do here
    }

    public boolean hasMembers() {
        return membershipManager.hasMembers();
    }

    public Member getMember(Member member) {
        return membershipManager.getMember(member);
    }

    public Member[] getMembers() {
        return membershipManager.getMembers();
    }

    public Member getLocalMember(boolean b) {
        return membershipManager.getLocalMember();
    }

    public String[] getMembersByName() {
        Member[] currentMembers = getMembers();
        String[] membernames;
        if (currentMembers != null) {
            membernames = new String[currentMembers.length];
            for (int i = 0; i < currentMembers.length; i++) {
                membernames[i] = currentMembers[i].toString();
            }
        } else {
            membernames = new String[0];
        }
        return membernames;
    }

    public Member findMemberByName(String name) {
        Member[] currentMembers = getMembers();
        for (Member currentMember : currentMembers) {
            if (name.equals(currentMember.toString())) {
                return currentMember;
            }
        }
        return null;
    }

    public void setLocalMemberProperties(String s, int i, int i1, int i2) {
        //Nothing to implement at the moment
    }

    public void setLocalMemberProperties(String listenHost, int listenPort) {
        properties.setProperty("tcpListenHost", listenHost);
        properties.setProperty("tcpListenPort", String.valueOf(listenPort));
        StaticMember localMember = (StaticMember) membershipManager.getLocalMember();
        try {
            if (localMember != null) {
                localMember.setHostname(listenHost);
                localMember.setPort(listenPort);
            } else {
                localMember = new StaticMember(listenHost, listenPort, 0);
                localMember.setUniqueId(UUIDGenerator.randomUUID(true));
                localMember.setPayload(payload);
                localMember.setDomain(domain);
                membershipManager.setLocalMember(localMember);
            }
            localMember.getData(true, true);
        } catch (IOException x) {
            throw new IllegalArgumentException(x);
        }
    }

    public void setMembershipListener(MembershipListener membershipListener) {
        // Nothing to do
    }

    public void removeMembershipListener() {
        // Nothing to do
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
        ((StaticMember) membershipManager.getLocalMember()).setPayload(payload);
    }

    public void setDomain(byte[] domain) {
        this.domain = domain;
        ((StaticMember) membershipManager.getLocalMember()).setDomain(domain);
    }

    public void broadcast(ChannelMessage channelMessage) throws ChannelException {
        //Nothing to implement at the moment
    }
}
