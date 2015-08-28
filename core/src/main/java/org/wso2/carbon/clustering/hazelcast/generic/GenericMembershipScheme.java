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
package org.wso2.carbon.clustering.hazelcast.generic;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import org.wso2.carbon.clustering.config.membership.scheme.GenericSchemeConfig;
import org.wso2.carbon.clustering.exception.MembershipFailedException;
import org.wso2.carbon.clustering.exception.MembershipInitializationException;
import org.wso2.carbon.clustering.hazelcast.HazelcastMembershipScheme;
import org.wso2.carbon.clustering.spi.MembershipScheme;
import org.wso2.carbon.internal.DataHolder;
import org.wso2.carbon.internal.clustering.ClusterContext;

/**
 * Generic membership scheme based on Hazelcast
 */
public class GenericMembershipScheme implements HazelcastMembershipScheme {
    private GenericSchemeConfig genericSchemeConfig;

    @Override
    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        MembershipScheme membershipScheme =
                DataHolder.getInstance().getGenericMembershipScheme(genericSchemeConfig.getName());
        if (membershipScheme instanceof HazelcastMembershipScheme) {
            HazelcastMembershipScheme hazelcastMembershipScheme = (HazelcastMembershipScheme) membershipScheme;
            hazelcastMembershipScheme.setHazelcastInstance(hazelcastInstance);
        }
    }

    @Override
    public void setLocalMember(Member localMember) {
        MembershipScheme membershipScheme =
                DataHolder.getInstance().getGenericMembershipScheme(genericSchemeConfig.getName());
        if (membershipScheme instanceof HazelcastMembershipScheme) {
            HazelcastMembershipScheme hazelcastMembershipScheme = (HazelcastMembershipScheme) membershipScheme;
            hazelcastMembershipScheme.setLocalMember(localMember);
        }
    }

    @Override
    public void init(ClusterContext clusterContext) throws MembershipInitializationException {
        genericSchemeConfig = (GenericSchemeConfig) clusterContext.getClusterConfiguration().
                getMembershipSchemeConfiguration().getMembershipScheme();

        // Look up the OSGi service which implements the membership scheme and call init on it
        String schemeName = genericSchemeConfig.getName();
        MembershipScheme membershipScheme =
                DataHolder.getInstance().getGenericMembershipScheme(schemeName);
        if (membershipScheme != null) {
            membershipScheme.init(clusterContext);
        } else {
            throw new MembershipInitializationException("MembershipScheme " + schemeName + " not found");
        }
    }

    @Override
    public void joinGroup() throws MembershipFailedException {
        MembershipScheme membershipScheme =
                DataHolder.getInstance().getGenericMembershipScheme(genericSchemeConfig.getName());
        if (membershipScheme != null) {
            membershipScheme.joinGroup();
        }
    }
}
