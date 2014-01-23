package org.wso2.carbon.core.clustering.hazelcast;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.management.GroupManagementCommand;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 */
public class GroupManagementCommandListener implements MessageListener<GroupManagementCommand> {
    private static final Log log = LogFactory.getLog(GroupManagementCommandListener.class);
    private ConfigurationContext configurationContext;

    public GroupManagementCommandListener(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
    }

    @Override
    public void onMessage(Message<GroupManagementCommand> clusteringMessage) {
        try {
            GroupManagementCommand msg = clusteringMessage.getMessageObject();
                log.info("Received GroupManagementCommand: " + msg);
                msg.execute(configurationContext);
        } catch (ClusteringFault e) {
            log.error("Cannot process ClusteringMessage", e);
        }
    }
}
