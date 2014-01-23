/*
*  Licensed to the Apache Software Foundation (ASF) under one
*  or more contributor license agreements.  See the NOTICE file
*  distributed with this work for additional information
*  regarding copyright ownership.  The ASF licenses this file
*  to you under the Apache License, Version 2.0 (the
*  "License"); you may not use this file except in compliance
*  with the License.  You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.apache.axis2.transport.base;

import org.apache.axis2.AxisFault;

import java.util.TimerTask;
import java.util.Timer;

public abstract class AbstractPollingTransportListener<T extends AbstractPollTableEntry>
        extends AbstractTransportListenerEx<T> {

    /** The main timer. */
    private Timer timer;

    @Override
    protected void doInit() throws AxisFault {
        timer = new Timer("PollTimer");
    }

    @Override
    public void destroy() {
        super.destroy();
        timer.cancel();
        timer = null;
    }

    /**
     * Schedule a repeated poll at the specified interval for a given service.
     * The method will schedule a single-shot timer task with executes a work
     * task on the worker pool. At the end of this work task, a new timer task
     * is scheduled for the next poll (except if the polling for the service
     * has been canceled). This effectively schedules the poll repeatedly
     * with fixed delay.
     * @param entry the poll table entry with the configuration for the service
     * @param pollInterval the interval between successive polls in milliseconds
     */
    void schedulePoll(final T entry) {
        final long pollInterval = entry.getPollInterval();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                workerPool.execute(new Runnable() {
                    public void run() {
                        if (state == BaseConstants.PAUSED) {
                            if (log.isDebugEnabled()) {
                                log.debug("Transport " + getTransportName() +
                                        " poll trigger : Transport is currently paused..");
                            }
                        } else {
                            poll(entry);
                        }
                    }
                });
            }
        };
        entry.timerTask = timerTask;
        if (entry.isConcurrentPollingAllowed()) {
            timer.scheduleAtFixedRate(timerTask, pollInterval, pollInterval);
        } else {
            timer.schedule(timerTask, pollInterval);
        }
    }

    @Override
    protected void startEndpoint(T endpoint) throws AxisFault {
        schedulePoll(endpoint);
    }

    @Override
    protected void stopEndpoint(T endpoint) {
        synchronized (endpoint) {
            endpoint.timerTask.cancel();
            endpoint.canceled = true;
        }
    }

    protected abstract void poll(T entry);

    protected void onPollCompletion(T entry) {
        if (!entry.isConcurrentPollingAllowed()) {
            synchronized (entry) {
                if (!entry.canceled) {
                    schedulePoll(entry);
                }
            }
        }
    }

    /**
     * method to log a failure to the log file and to update the last poll status and time
     * @param msg text for the log message
     * @param e optional exception encountered or null
     * @param entry the PollTableEntry
     */
    protected void processFailure(String msg, Exception e, T entry) {
        if (e == null) {
            log.error(msg);
        } else {
            log.error(msg, e);
        }
        long now = System.currentTimeMillis();
        entry.setLastPollState(AbstractPollTableEntry.FAILED);
        entry.setLastPollTime(now);
        entry.setNextPollTime(now + entry.getPollInterval());
        onPollCompletion(entry);
    }

    // -- jmx/management methods--
    /**
     * Pause the listener - Stop accepting/processing new messages, but continues processing existing
     * messages until they complete. This helps bring an instance into a maintenence mode
     * @throws org.apache.axis2.AxisFault on error
     */
    public void pause() throws AxisFault {
        if (state != BaseConstants.STARTED) return;
        state = BaseConstants.PAUSED;
        log.info("Listener paused");
    }

    /**
     * Resume the lister - Brings the lister into active mode back from a paused state
     * @throws AxisFault on error
     */
    public void resume() throws AxisFault {
        if (state != BaseConstants.PAUSED) return;
        state = BaseConstants.STARTED;
        log.info("Listener resumed");
    }

    /**
     * Stop processing new messages, and wait the specified maximum time for in-flight
     * requests to complete before a controlled shutdown for maintenence
     *
     * @param millis a number of milliseconds to wait until pending requests are allowed to complete
     * @throws AxisFault on error
     */
    public void maintenenceShutdown(long millis) throws AxisFault {
        if (state != BaseConstants.STARTED) return;
        stop();
        state = BaseConstants.STOPPED;
        log.info("Listener shutdown");
    }
}
