package org.wso2.carbon.clustering.internal;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.clustering.ClusterContext;
import org.wso2.carbon.clustering.api.MembershipListener;

import java.util.ArrayList;
import java.util.List;

@Component (
        name = "org.wso2.carbon.clustering.internal.CarbonClusterServiceComponent",
        description = "This service  component is responsible for retrieving the MembershipListener " +
                      "OSGi service and add them to clustering module",
        immediate = true
)

public class CarbonClusterServiceComponent {
    private static Logger logger = LoggerFactory.getLogger(CarbonClusterServiceComponent.class);
    private boolean isClusterContextAvailable = false;
    private List<MembershipListener> membershipListeners = new ArrayList<>();

    @Reference (
            name = "carbon.cluster.context.listener",
            referenceInterface = ClusterContext.class,
            cardinality = ReferenceCardinality.MANDATORY_UNARY,
            policy = ReferencePolicy.STATIC,
            bind = "setClusterContext",
            unbind = "unsetClusterContext"
    )
    private ClusterContext clusterContext;

    @Reference (
            name = "carbon.cluster.membership.listener",
            referenceInterface = MembershipListener.class,
            cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            bind = "addMembershipListener",
            unbind = "removeMembershipListener"
    )
    private MembershipListener membershipListener;

    @Activate
    protected void start() {
        if (!membershipListeners.isEmpty()) {
            for (MembershipListener listener : membershipListeners) {
                clusterContext.addMembershipListener(listener);
            }
        }
    }

    @Deactivate
    protected void stop() {
        if (!membershipListeners.isEmpty()) {
            for (MembershipListener listener : membershipListeners) {
                clusterContext.removeMembershipListener(listener);
            }
        }
    }

    protected void setClusterContext(ClusterContext clusterContext) {
        this.clusterContext = clusterContext;
        isClusterContextAvailable = true;
    }

    protected void unsetClusterContext(ClusterContext clusterContext) {
        this.clusterContext = null;
        isClusterContextAvailable = false;
    }

    protected void addMembershipListener(MembershipListener membershipListener) {
        if (isClusterContextAvailable) {
            logger.info("Adding MembershipListener");
            clusterContext.addMembershipListener(membershipListener);
        } else {
            membershipListeners.add(membershipListener);
        }
    }

    protected void removeMembershipListener(MembershipListener membershipListener) {
        if (isClusterContextAvailable) {
            logger.info("Removing MembershipListener");
            clusterContext.removeMembershipListener(membershipListener);
        } else {
            membershipListeners.remove(membershipListener);
        }
    }
}
