/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.hazelcast.sample.internal;

import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.hazelcast.utils.CarbonHazelcastUtils;

public class SampleMembershipListener implements MembershipListener {
    private static final Logger logger = LoggerFactory.getLogger(HazelcastSampleServiceComponent.class);

    @Override
    public void memberAdded(MembershipEvent membershipEvent) {
        if (CarbonHazelcastUtils.isCoordinator(DataHolder.getInstance().getHazelcastOSGiInstance())) {
            logger.info("This node become the coordinator");
        }
    }

    @Override
    public void memberRemoved(MembershipEvent membershipEvent) {
        if (CarbonHazelcastUtils.isCoordinator(DataHolder.getInstance().getHazelcastOSGiInstance())) {
            logger.info("This node become the coordinator");
        }
    }

    @Override
    public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {

    }
}
