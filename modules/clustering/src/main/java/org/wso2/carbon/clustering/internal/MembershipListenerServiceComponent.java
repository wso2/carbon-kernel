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

package org.wso2.carbon.clustering.internal;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.clustering.internal.ClusterContext;
import org.wso2.carbon.clustering.api.MembershipListener;


@Component(
        name = "org.wso2.carbon.clustering.internal.MembershipListenerServiceComponent",
        description = "This service  component is responsible for retrieving the MembershipListener " +
                      "OSGi service and add them to clustering module",
        immediate = true
)

@Reference(
        name = "carbon.cluster.membership.listener",
        referenceInterface = MembershipListener.class,
        cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE,
        policy = ReferencePolicy.DYNAMIC,
        bind = "addMembershipListener",
        unbind = "removeMembershipListener"
)

public class MembershipListenerServiceComponent {
    private static Logger logger = LoggerFactory.
            getLogger(MembershipListenerServiceComponent.class);
    private ClusterContext clusterContext = DataHolder.getInstance().getClusterContext();


    protected void addMembershipListener(MembershipListener membershipListener) {
        logger.info("Adding MembershipListener");
        clusterContext.addMembershipListener(membershipListener);
    }

    protected void removeMembershipListener(MembershipListener membershipListener) {
        logger.info("Removing MembershipListener");
        clusterContext.removeMembershipListener(membershipListener);

    }
}
