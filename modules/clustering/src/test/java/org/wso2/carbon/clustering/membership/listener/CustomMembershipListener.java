package org.wso2.carbon.clustering.membership.listener;

import org.wso2.carbon.clustering.MembershipEvent;
import org.wso2.carbon.clustering.api.MembershipListener;


public class CustomMembershipListener implements MembershipListener {
    @Override
    public void memberAdded(MembershipEvent event) {
        System.out.println("Event type : " + event.getEventType());
    }

    @Override
    public void memberRemoved(MembershipEvent event) {
        System.out.println("Event type : " + event.getEventType());
    }
}
