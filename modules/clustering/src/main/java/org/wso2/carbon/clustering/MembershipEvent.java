/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.clustering;

/**
 * An event class used when notifying the membership listeners for changes in current membership
 */
public class MembershipEvent {
    public static final int MEMBER_ADDED = 1;

    public static final int MEMBER_REMOVED = 2;

    private final ClusterMember member;

    private final int eventType;


    public MembershipEvent(ClusterMember member, int eventType) {
        this.member = member;
        this.eventType = eventType;
    }


    /**
     * Returns the membership event type; #MEMBER_ADDED or #MEMBER_REMOVED
     *
     * @return the membership event type
     */
    public int getEventType() {
        return eventType;
    }

    /**
     * Returns the removed or added member.
     *
     * @return member which is removed/added
     */
    public ClusterMember getMember() {
        return member;
    }

    @Override
    public String toString() {
        return "MembershipEvent {" + member + "} "
               + ((eventType == MEMBER_ADDED) ? "added" : "removed");
    }
}
