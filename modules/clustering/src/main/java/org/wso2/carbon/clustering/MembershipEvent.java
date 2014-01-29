package org.wso2.carbon.clustering;


public class MembershipEvent {
    public static final int MEMBER_ADDED = 1;

    public static final int MEMBER_REMOVED = 2;

    private final ClusterMember member;

    private final int eventType;


    public MembershipEvent(ClusterMember member, int eventType) {
        this.member = member;
        this.eventType = eventType;
    }


    /**
     * Returns the membership event type; #MEMBER_ADDED or #MEMBER_REMOVED
     *
     * @return the membership event type
     */
    public int getEventType() {
        return eventType;
    }

    /**
     * Returns the removed or added member.
     *
     * @return member which is removed/added
     */
    public ClusterMember getMember() {
        return member;
    }

    @Override
    public String toString() {
        return "MembershipEvent {" + member + "} "
               + ((eventType == MEMBER_ADDED) ? "added" : "removed");
    }
}
