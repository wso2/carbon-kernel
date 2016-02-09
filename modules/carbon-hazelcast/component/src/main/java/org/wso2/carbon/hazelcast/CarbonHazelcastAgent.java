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
package org.wso2.carbon.hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;
import com.hazelcast.core.Member;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import com.hazelcast.osgi.HazelcastOSGiInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.hazelcast.internal.CarbonHazelcastException;
import org.wso2.carbon.hazelcast.internal.DataHolder;

import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.List;

/**
 * DataHolder.
 *
 * @since 1.0.0
 */
public class CarbonHazelcastAgent {
    private static final Logger logger = LoggerFactory.getLogger(CarbonHazelcastAgent.class);
    public static final String CLUSTER_COORDINATOR_LOCK = "$coordinator#@lock";
    private boolean isCoordinator;

    public CarbonHazelcastAgent() throws CarbonHazelcastException{
        String hazelcastFilePath = Paths.get(System.getProperty("carbon.home"),
                "conf", "hazelcast", "hazelcast.xml").toString();

        Config config;
        try {
            config = new XmlConfigBuilder(hazelcastFilePath).build();
        } catch (FileNotFoundException e) {
            throw new CarbonHazelcastException("Unable to find hazelcast.xml file in location " +
                    "'[PRODUCT_HOME]/conf/hazelcast/hazelcast.xml'", e);
        }

        HazelcastOSGiInstance hazelcastOSGiInstance = DataHolder.getInstance().getHazelcastOSGiService()
                .newHazelcastInstance(config);
        DataHolder.getInstance().setHazelcastOSGiInstance(hazelcastOSGiInstance);

        hazelcastOSGiInstance.getCluster().addMembershipListener(new CoordinatorElectionMembershipListener());

        //-- Cluster coordinator election algorithm implementation starts here.
        // Hazelcast Community confirms that the list of members is consistent across a given cluster.  Also the first
        // member of the member list you get from primaryHazelcastInstance.getCluster().getMembers() is consistent
        // across the cluster and first member usually is the oldest member.  Therefore we can safely assume first
        // member as the coordinator node.

        // Now this distributed lock is used to correctly identify the coordinator node during the member startup. The
        // node which acquires the lock checks whether it is the oldest member in the cluster. If it is the oldest
        // member then it elects itself as the coordinator node and then release the lock. If it is not the oldest
        // member them simply release the lock. This distributed lock is used to avoid any race conditions.
        ILock lock = hazelcastOSGiInstance.getLock(CLUSTER_COORDINATOR_LOCK);

        try {
            logger.debug("Trying to get the CLUSTER_COORDINATOR_LOCK lock.");

            lock.lock();
            logger.debug("Acquired the CLUSTER_COORDINATOR_LOCK lock.");

            Member oldestMember = hazelcastOSGiInstance.getCluster().getMembers().iterator().next();
            if (oldestMember.localMember() && !isCoordinator) {
                electCoordinatorNode();
            }
        } finally {
            lock.unlock();
            logger.debug("Released the CLUSTER_COORDINATOR_LOCK lock.");
        }
        //-- Coordinator election algorithm ends here.
    }

    public HazelcastInstance getHazelcastInstance() {
        return DataHolder.getInstance().getHazelcastOSGiInstance();
    }

    public boolean isCoordinator() {
        return  isCoordinator;
    }

    private void electCoordinatorNode() {
        isCoordinator = true;
        logger.info("Elected this member [" + DataHolder.getInstance().getHazelcastOSGiInstance().getCluster()
                .getLocalMember().getUuid() + "] " + "as the Coordinator node");

        // Notify all OSGi services which are waiting for this member to become the coordinator
        List<CoordinatedActivity> coordinatedActivities =
                DataHolder.getInstance().getCoordinatedActivityList();
        for (CoordinatedActivity coordinatedActivity : coordinatedActivities) {
            coordinatedActivity.execute();
        }
        logger.debug("Invoked all the coordinated activities after electing this member as the Coordinator");
    }

    /**
     * This membership listener is used to receive member added/remove events to implement coordination
     * election algorithm
     */
    private class CoordinatorElectionMembershipListener implements MembershipListener {

        /**
         * Checks whether there are multiple coordinator nodes in the cluster. There could be situations where this
         * node was elected as the coordinator because it was the oldest member at that time. But when a new
         * node is added, this node may not be the oldest member in the cluster. Following section explains how this
         * situation can occur.
         * <p/>
         * Sometimes Hazelcast cluster could get partitioned. When the cluster get partitioned, each partition will
         * elect its own coordinator node. Now when these partitions merge themselves we need to elect a new
         * coordinator and make sure there is only one coordinator in the merged partition. When partitions are getting
         * merged memberAdded events are invoked. Therefore in memberAdded event handling code we re-elect the
         * oldest member, first mode in the member list, as the coordinator.
         *
         * @param membershipEvent event
         */
        @Override
        public void memberAdded(MembershipEvent membershipEvent) {
            if (isCoordinator) {
                logger.debug("Member Added Event: Checking whether there are multiple Coordinator nodes in the cluster.");
                Member oldestMember = DataHolder.getInstance().getHazelcastOSGiInstance().getCluster()
                        .getMembers().iterator().next();
                if (!oldestMember.localMember()) {
                    logger.debug("This node is not the Coordinator now.");
                    isCoordinator = false;
                }
            }
        }

        /**
         * Checks whether this nodes became the oldest member in the cluster. If so elect this node as the
         * coordinator node.
         *
         * @param membershipEvent event
         */
        @Override
        public void memberRemoved(MembershipEvent membershipEvent) {
            if (!isCoordinator) {
                logger.debug("Member Removed Event: Checking whether this node became the Coordinator node");
                Member oldestMember = DataHolder.getInstance().getHazelcastOSGiInstance().getCluster()
                        .getMembers().iterator().next();

                if (oldestMember.localMember()) {
                    Member localMember = DataHolder.getInstance().getHazelcastOSGiInstance().getCluster()
                            .getLocalMember();
                    electCoordinatorNode();
                    logger.debug("Member Removed Event: This member is elected as the Coordinator node");
                }
            }
        }

        @Override
        public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
        }
    }
}
