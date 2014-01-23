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
import org.apache.catalina.tribes.group.interceptors.DomainFilterInterceptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;

/**
 *  Represents a member running in application mode
 */
public class ApplicationMode implements OperationMode {

    private static final Log log = LogFactory.getLog(ClusterManagementMode.class);

    private final byte[] domain;
    private final MembershipManager membershipManager;

    public ApplicationMode(byte[] domain, MembershipManager membershipManager) {
        this.domain = domain;
        this.membershipManager = membershipManager;
    }

    public void addInterceptors(Channel channel) {
        DomainFilterInterceptor dfi = new DomainFilterInterceptor();
        dfi.setOptionFlag(TribesConstants.MEMBERSHIP_MSG_OPTION);
        dfi.setDomain(domain);
        channel.addInterceptor(dfi);
        if (log.isDebugEnabled()) {
            log.debug("Added Domain Filter Interceptor");
        }
    }

    public void init(Channel channel) {
        // Nothing to be done
    }

    public List<MembershipManager> getMembershipManagers() {
        return new ArrayList<MembershipManager>();
    }

    public void notifyMemberJoin(final Member member) {
        Thread th = new Thread(){
            public void run() {
                membershipManager.sendMemberJoinedToAll(member);
            }
        };
        th.start();
    }
}
