package org.wso2.carbon.hazelcast.utils;

import com.hazelcast.core.Member;
import com.hazelcast.osgi.HazelcastOSGiInstance;

public class CarbonHazelcastUtils {
    private CarbonHazelcastUtils() {

    }

    public static boolean isCoordinator(HazelcastOSGiInstance hazelcastOSGiInstance) {
        Member oldestMember = hazelcastOSGiInstance.getCluster().getMembers().iterator().next();
        if (oldestMember.localMember()) {
            return true;
        }
        return false;
    }
}
