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

import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.Member;

import java.util.List;

/**
 * The mode in which this member is operating such a loadBalance or application
 */
public interface OperationMode {

    /**
     * Add channel interecptors
     *
     * @param channel The Channel to which interceptors need to be added
     */
    public void addInterceptors(Channel channel);

    /**
     * Initialize this mode
     *
     * @param channel The channel related to this member
     */
    void init(Channel channel);

    /**
     * Get all the membership managers associated with a particular mode
     *
     * @return membership managers associated with a particular mode
     */
    List<MembershipManager> getMembershipManagers();

    /**
     * Notify to the relevant parties that a member <code>member</code> joined
     *
     * @param member The member to whom this member lists have to be sent
     */
    void notifyMemberJoin(Member member);
}
