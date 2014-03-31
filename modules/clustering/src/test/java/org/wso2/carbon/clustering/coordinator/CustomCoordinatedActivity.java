package org.wso2.carbon.clustering.coordinator;

import org.wso2.carbon.clustering.api.CoordinatedActivity;

/**
 * CustomCoordinatedActivity used for testing CoordinatorActivity
 */
public class CustomCoordinatedActivity implements CoordinatedActivity {

    private String name;
    private boolean executionComplete;

    public CustomCoordinatedActivity(String name) {
        this.name = name;
    }

    @Override
    public void execute() {
        System.out.println("Executed CustomCoordinatedActivity:" + name);
        executionComplete = true;
    }

    public boolean isExecutionComplete() {
        return executionComplete;
    }
}
