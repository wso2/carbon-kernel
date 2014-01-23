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

import org.apache.axis2.clustering.MembershipListener;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.group.interceptors.NonBlockingCoordinator;

/**
 * The non-blocking coordinator interceptor
 */
public class Axis2Coordinator extends NonBlockingCoordinator {

    private final MembershipListener membershipListener;

    public Axis2Coordinator(MembershipListener membershipListener) {
        this.membershipListener = membershipListener;
    }

    public void memberAdded(Member member) {
        super.memberAdded(member);
        if (membershipListener != null &&
            TribesUtil.areInSameDomain(getLocalMember(true), member)) {
            membershipListener.memberAdded(TribesUtil.toAxis2Member(member), isCoordinator());
        }
    }

    public void memberDisappeared(Member member) {
        super.memberDisappeared(member);
        if(!TribesUtil.areInSameDomain(getLocalMember(true), member)){
            return;
        }
        if (isCoordinator()) {
            if (TribesUtil.toAxis2Member(member).isActive()) {

                // If the local member is PASSIVE, we try to activate it
                if (!TribesUtil.toAxis2Member(getLocalMember(true)).isActive()) {
                    //TODO: ACTIVATE local member

                } else {
                    Member[] members = getMembers();
                    for (Member aMember : members) {
                        if (!TribesUtil.toAxis2Member(member).isActive()) {
                            // TODO: Send ACTIVATE message to this passive member
                        }
                    }
                }
            } else {
                //TODO If a PASSIVE member disappeared, we may need to startup another
                // passive node
            }
        }
        if (membershipListener != null) {
            membershipListener.memberDisappeared(TribesUtil.toAxis2Member(member), isCoordinator());
        }
    }
}
