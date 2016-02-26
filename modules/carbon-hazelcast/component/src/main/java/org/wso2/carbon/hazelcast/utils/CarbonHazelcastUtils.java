package org.wso2.carbon.hazelcast.utils;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;

public class CarbonHazelcastUtils {
    private CarbonHazelcastUtils() {

    }

    public static boolean isCoordinator(HazelcastInstance hazelcastInstance) {
        Member oldestMember = hazelcastInstance.getCluster().getMembers().iterator().next();
        if (oldestMember.localMember()) {
            return true;
        }
        return false;
    }
}
