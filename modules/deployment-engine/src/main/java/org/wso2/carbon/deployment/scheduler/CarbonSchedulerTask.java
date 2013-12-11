/*
 *  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.deployment.scheduler;

import org.wso2.carbon.deployment.CarbonRepositoryListener;

import java.util.TimerTask;

/**
 * Scheduler Task associated with the carbon deployment engine, which does the repository
 * update when called periodically.
 *
 */
public class CarbonSchedulerTask {
    static final int SCHEDULED = 1;
    static final int CANCELLED = 2;
    final Object lock = new Object();
    int state = 0;
    TimerTask timerTask;
    private CarbonRepositoryListener repositoryListener;

    /**
     * Creates a new carbonScheduler task.
     */
    public CarbonSchedulerTask(CarbonRepositoryListener listener) {
        this.repositoryListener = listener;
    }

    /**
     * Cancels this carbonScheduler task.
     * <p/>
     * This method may be called repeatedly; the second and subsequent calls have no effect.
     *
     * @return Returns true if this task was already scheduled to run.
     */
    public boolean cancel() {
        synchronized (lock) {
            if (timerTask != null) {
                timerTask.cancel();
            }
            boolean result = (state == SCHEDULED);
            state = CANCELLED;
            return result;
        }
    }

    private void checkRepository() {
        repositoryListener.checkArtifacts();
        repositoryListener.update();
    }

    /**
     * The action to be performed by this carbonScheduler task.
     * This will call the "check and update repository" methods
     */
    public void run() {
        checkRepository();
    }
}
