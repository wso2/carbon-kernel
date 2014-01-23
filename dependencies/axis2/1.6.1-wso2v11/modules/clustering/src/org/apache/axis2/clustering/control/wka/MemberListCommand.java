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
import org.apache.axis2.clustering.tribes.TribesUtil;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.catalina.tribes.Member;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Arrays;

/**
 * When a new member wishes to join a group, it will send a {@link JoinGroupCommand} message to
 * a known member. Then this known member will respond with this MemberListCommand message.
 * This message will contain a list of all current members.
 */
public class MemberListCommand extends ControlCommand {

    private static final Log log = LogFactory.getLog(MemberListCommand.class);
    private static final long serialVersionUID = 5687720124889269491L;

    private Member[] members;
    private transient MembershipManager membershipManager;

    public void setMembershipManager(MembershipManager membershipManager) {
        this.membershipManager = membershipManager;
    }

    public void setMembers(Member[] members) {
        this.members = members;
    }

    public void execute(ConfigurationContext configurationContext) throws ClusteringFault {
        if(log.isDebugEnabled()){
            log.debug("MembershipManager#domain: " + new String(membershipManager.getDomain()));
        }
        Member localMember = membershipManager.getLocalMember();
        for (Member member : members) {
            addMember(localMember, member);
        }
    }

    private void addMember(Member localMember, Member member) {
        if(log.isDebugEnabled()){
            log.debug("Trying to add member " + TribesUtil.getName(member) + "...");
        }
        if (localMember == null ||
            (!(Arrays.equals(localMember.getHost(), member.getHost()) &&
              localMember.getPort() == member.getPort()))) {
            if(log.isDebugEnabled()){
                log.debug("Added member " + TribesUtil.getName(member));
            }
            membershipManager.memberAdded(member);
        }
    }
}
