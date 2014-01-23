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
package org.apache.axis2.clustering.control.wka;

import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.control.ControlCommand;
import org.apache.axis2.clustering.tribes.MembershipManager;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.catalina.tribes.Member;

import java.util.Arrays;

/**
 * This is the notification message a member will send to all others in the group after it has
 * joined the group. When the other members received this message, they will add the newly joined
 * member to their member list
 */
public class MemberJoinedCommand extends ControlCommand {

    private static final long serialVersionUID = -6596472883950279349L;
    private Member member;
    private transient MembershipManager membershipManager;

    public void setMembershipManager(MembershipManager membershipManager) {
        this.membershipManager = membershipManager;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public Member getMember() {
        return member;
    }

    public void execute(ConfigurationContext configurationContext) throws ClusteringFault {
        Member localMember = membershipManager.getLocalMember();
        if (localMember == null || !(Arrays.equals(localMember.getHost(), member.getHost()) &&
              localMember.getPort() == member.getPort())) {
            membershipManager.memberAdded(member);
        }
    }

    public String toString() {
        return "MemberJoinedCommand: " + member;
    }
}
