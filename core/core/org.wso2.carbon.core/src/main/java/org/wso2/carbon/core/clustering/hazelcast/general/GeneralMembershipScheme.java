package org.wso2.carbon.core.clustering.hazelcast.general;

import com.hazelcast.core.*;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.ClusteringMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.clustering.hazelcast.HazelcastCarbonClusterImpl;
import org.wso2.carbon.core.clustering.hazelcast.HazelcastMembershipScheme;
import org.wso2.carbon.core.clustering.hazelcast.HazelcastUtil;
import org.wso2.carbon.core.clustering.hazelcast.multicast.MulticastBasedMembershipScheme;
import org.wso2.carbon.core.clustering.hazelcast.util.MemberUtils;

import java.util.List;
import java.util.Map;

/**
 * This membership scheme is used to update carbon cluster when
 * hazelcast.xml is used for hazelcast configuration.
 */
public class GeneralMembershipScheme implements HazelcastMembershipScheme {
    private static final Log log = LogFactory.getLog(MulticastBasedMembershipScheme.class);
    private String primaryDomain;
    private final List<ClusteringMessage> messageBuffer;
    private HazelcastCarbonClusterImpl carbonCluster;
    private HazelcastInstance primaryHazelcastInstance;

    public GeneralMembershipScheme(String primaryDomain,
                                   List<ClusteringMessage> messageBuffer) {
        this.primaryDomain = primaryDomain;
        this.messageBuffer = messageBuffer;
    }

    @Override
    public void init() throws ClusteringFault {
        // Nothing to do
    }

    @Override
    public void joinGroup() throws ClusteringFault {
        primaryHazelcastInstance.getCluster().addMembershipListener(new GeneralMembershipListener());
    }

    @Override
    public void setPrimaryHazelcastInstance(HazelcastInstance primaryHazelcastInstance) {
        this.primaryHazelcastInstance = primaryHazelcastInstance;
    }

    @Override
    public void setLocalMember(Member localMember) {
        // Nothing to do
    }

    @Override
    public void setCarbonCluster(HazelcastCarbonClusterImpl hazelcastCarbonCluster) {
        this.carbonCluster = hazelcastCarbonCluster;
    }

    private class GeneralMembershipListener implements MembershipListener {
        private Map<String, org.apache.axis2.clustering.Member> members;

        public GeneralMembershipListener() {
            members = MemberUtils.getMembersMap(primaryHazelcastInstance, primaryDomain);
        }

        @Override
        public void memberAdded(MembershipEvent membershipEvent) {
            Member member = membershipEvent.getMember();

            // send all cluster messages
            carbonCluster.memberAdded(member);
            log.info("Member joined [" + member.getUuid() + "]: " + member.getInetSocketAddress().toString());
            // Wait for sometime for the member to completely join before replaying messages
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ignored) {
            }
            HazelcastUtil.sendMessagesToMember(messageBuffer, member, carbonCluster);
        }

        @Override
        public void memberRemoved(MembershipEvent membershipEvent) {
            Member member = membershipEvent.getMember();
            carbonCluster.memberRemoved(member);
            log.info("Member left [" + member.getUuid() + "]: " + member.getInetSocketAddress().toString());
            members.remove(member.getUuid());
        }

        @Override
        public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
        }

    }
}
