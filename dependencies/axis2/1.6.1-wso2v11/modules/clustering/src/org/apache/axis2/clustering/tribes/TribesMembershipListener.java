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

import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.MembershipListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Membership changes are notified using this class
 */
public class TribesMembershipListener implements MembershipListener {

    private static Log log = LogFactory.getLog(TribesMembershipListener.class);
    private final MembershipManager membershipManager;

    public TribesMembershipListener(MembershipManager membershipManager) {
        this.membershipManager = membershipManager;
    }

    public void memberAdded(Member member) {
        if (membershipManager.memberAdded(member)) {
            log.info("New member " + TribesUtil.getName(member) + " joined cluster.");
            /*if (TribesUtil.toAxis2Member(member).isActive()) {
            } else {
            }*/
        }
        //        System.err.println("++++++ IS COORD="+TribesClusteringAgent.nbc.isCoordinator());
    }

    public void memberDisappeared(Member member) {
        log.info("Member " + TribesUtil.getName(member) + " left cluster");
        membershipManager.memberDisappeared(member);

//        System.err.println("++++++ IS COORD="+TribesClusteringAgent.nbc.isCoordinator());
        
    }
}
