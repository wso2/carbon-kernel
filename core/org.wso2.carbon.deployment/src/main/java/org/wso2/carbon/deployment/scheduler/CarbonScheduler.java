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

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This will schedule/reschedule the timer task, which in turn will do the repository update.
 *
 */
public class CarbonScheduler {
    private final Timer timer = new Timer(true);

    /**
     * This will reschedule the task to run again.
     *
     * @param carbonSchedulerTask the task to reschedule
     * @param iterator the iterator used to get the next time interval
     */
    private void reschedule(CarbonSchedulerTask carbonSchedulerTask,
                            CarbonDeploymentIterator iterator) {
        Date time = iterator.next();

        if (time == null) {
            carbonSchedulerTask.cancel();
        } else {
            synchronized (carbonSchedulerTask.lock) {
                if (carbonSchedulerTask.state != CarbonSchedulerTask.CANCELLED) {
                    carbonSchedulerTask.timerTask = new SchedulerTimerTask(carbonSchedulerTask,
                                                                           iterator);
                    timer.schedule(carbonSchedulerTask.timerTask, time);
                }
            }
        }
    }

    /**
     * Schedules the specified task for execution according to the specified schedule.
     * If times specified by the <code>ScheduleIterator</code> are in the past they are
     * scheduled for immediate execution.
     *
     * @param carbonSchedulerTask task to be scheduled
     * @param iterator            iterator that describes the schedule
     * @throws IllegalStateException if task was already scheduled or cancelled,
     *                               carbonScheduler was cancelled, or carbonScheduler thread terminated.
     */
    public void schedule(CarbonSchedulerTask carbonSchedulerTask,
                         CarbonDeploymentIterator iterator) {
        Date time = iterator.next();

        if (time == null) {
            carbonSchedulerTask.cancel();
        } else {
            synchronized (carbonSchedulerTask.lock) {
                carbonSchedulerTask.state = CarbonSchedulerTask.SCHEDULED;
                carbonSchedulerTask.timerTask = new SchedulerTimerTask(carbonSchedulerTask,
                                                                       iterator);
                timer.schedule(carbonSchedulerTask.timerTask, time);
            }
        }
    }

    /**
     * The cleanup method which clean and cancels a given task
     * @param carbonSchedulerTask the task to clean
     */
    public void cleanup(CarbonSchedulerTask carbonSchedulerTask) {
        synchronized (carbonSchedulerTask.lock) {
            carbonSchedulerTask.state = CarbonSchedulerTask.CANCELLED;
            timer.cancel();
        }
    }

    public class SchedulerTimerTask extends TimerTask {
        private CarbonDeploymentIterator iterator;
        private CarbonSchedulerTask carbonSchedulerTask;

        public SchedulerTimerTask(CarbonSchedulerTask carbonSchedulerTask,
                                  CarbonDeploymentIterator iterator) {
            this.carbonSchedulerTask = carbonSchedulerTask;
            this.iterator = iterator;
        }

        public void run() {
            carbonSchedulerTask.run();
            reschedule(carbonSchedulerTask, iterator);
        }
    }
}
