package org.wso2.carbon.clustering.api;

/**
 * Represents a coordinated activity. Such an activity will only be performed on the coordinator member.
 */
public interface CoordinatedActivity {

    /**
     * When this member becomes a coordinator, this {@link #execute()}  method will get called
     */
    void execute();
}
