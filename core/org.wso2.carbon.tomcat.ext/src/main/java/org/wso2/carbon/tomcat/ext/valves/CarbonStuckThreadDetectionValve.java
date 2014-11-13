/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.tomcat.ext.valves;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.Constants;
import org.apache.catalina.valves.StuckThreadDetectionValve;
import org.apache.catalina.valves.ValveBase;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.res.StringManager;
import org.wso2.carbon.tomcat.ext.internal.Utils;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tomcat valve for detecting stuck or long running threads
 */
@SuppressWarnings("unused")
public class CarbonStuckThreadDetectionValve extends ValveBase {

    /**
     * The descriptive information related to this implementation.
     */
    private static final String info =
        "org.apache.catalina.valves.StuckThreadDetectionValve/1.0";
    /**
     * Logger
     */
    private static final Log    log  = LogFactory.getLog(StuckThreadDetectionValve.class);

    /**
     * The string manager for this package.
     */
    private static final StringManager sm =
        StringManager.getManager(Constants.Package);

    /**
     * Keeps count of the number of stuck threads detected
     */
    private final AtomicInteger stuckCount = new AtomicInteger(0);

    /**
     * In seconds. Default 600 (10 minutes).
     */
    private int threshold = 600;

    /**
     * The only references we keep to actual running Thread objects are in
     * this Map (which is automatically cleaned in invoke()s finally clause).
     * That way, Threads can be GC'ed, eventhough the Valve still thinks they
     * are stuck (caused by a long monitor interval)
     */
    private ConcurrentHashMap<Long, MonitoredThread>
                                        activeThreads = new ConcurrentHashMap<Long, MonitoredThread>();

    public CarbonStuckThreadDetectionValve() {
        //enable async support
        super(true);
    }

    /**
     * Specify the threshold (in seconds) used when checking for stuck threads.
     * If &lt;=0, the detection is disabled. The default is 600 seconds.
     *
     * @param threshold The new threshold in seconds
     */
    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    @Override
    protected void initInternal() throws LifecycleException {
        super.initInternal();

        if (log.isDebugEnabled()) {
            log.debug("Monitoring stuck threads with threshold = "
                          + threshold
                          + " sec");
        }
    }

    /**
     * Return descriptive information about this Valve implementation.
     */
    @Override
    public String getInfo() {
        return info;
    }

    private void handleStuckThread(MonitoredThread monitoredThread,
                                   long activeTime, int numStuckThreads) {
       String msg = sm.getString(
                "stuckThreadDetectionValve.notifyStuckThreadDetected",
                monitoredThread.getThread().getName(), activeTime,
                monitoredThread.getStartTime(), numStuckThreads,
                monitoredThread.getRequestUri(), threshold);
       msg += ", tenantDomain=" + monitoredThread.getTenantDomain();
       // msg += "\n" + getStackTraceAsString(trace);
       Throwable th = new Throwable();
       th.setStackTrace(monitoredThread.getThread().getStackTrace());
       log.warn(msg, th);
       monitoredThread.getThread().interrupt();
       monitoredThread.getThread().stop();  // TODO: Not a good practice, but we are using this as a last resort to kill rogue tenant threads
       activeThreads.remove(monitoredThread.getThread().getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void invoke(Request request, Response response)
        throws IOException, ServletException {

        if (threshold <= 0) {
            // short-circuit if not monitoring stuck threads
            getNext().invoke(request, response);
            return;
        }

        // Save the thread/runnable
        // Keeping a reference to the thread object here does not prevent
        // GC'ing, as the reference is removed from the Map in the finally clause

        Long key = Thread.currentThread().getId();
        StringBuffer requestUrl = request.getRequestURL();
        if (request.getQueryString() != null) {
            requestUrl.append("?");
            requestUrl.append(request.getQueryString());
        }
        String tenantDomain = Utils.getTenantDomain(request);
        MonitoredThread monitoredThread = new MonitoredThread(Thread.currentThread(),
                                                              requestUrl.toString(),
                                                              tenantDomain);
        activeThreads.put(key, monitoredThread);

        try {
            getNext().invoke(request, response);
        } finally {
            activeThreads.remove(key);
        }
    }

    @Override
    public void backgroundProcess() {
        super.backgroundProcess();

        long thresholdInMillis = (long) threshold * 1000;

        // Check monitored threads, being careful that the request might have
        // completed by the time we examine it
        for (MonitoredThread monitoredThread : activeThreads.values()) {
            long activeTime = monitoredThread.getActiveTimeInMillis();

            if (activeTime >= thresholdInMillis && monitoredThread.markAsStuckIfStillRunning()) {
                int numStuckThreads = stuckCount.incrementAndGet();
                handleStuckThread(monitoredThread, activeTime, numStuckThreads);
            }
        }
    }

    private static class MonitoredThread {

        /**
         * Reference to the thread to get a stack trace from background task
         */
        private final Thread thread;
        private final String requestUri;
        private final long   start;
        private       String tenantDomain;
        private final AtomicInteger state = new AtomicInteger(
            MonitoredThreadState.RUNNING.ordinal());

        public MonitoredThread(Thread thread, String requestUri, String tenantDomain) {
            this.thread = thread;
            this.requestUri = requestUri;
            this.tenantDomain = tenantDomain;
            this.start = System.currentTimeMillis();
        }

        public Thread getThread() {
            return this.thread;
        }

        public String getRequestUri() {
            return requestUri;
        }

        public long getActiveTimeInMillis() {
            return System.currentTimeMillis() - start;
        }

        public Date getStartTime() {
            return new Date(start);
        }

        public String getTenantDomain() {
            return tenantDomain;
        }

        public boolean markAsStuckIfStillRunning() {
            return this.state.compareAndSet(MonitoredThreadState.RUNNING.ordinal(),
                                            MonitoredThreadState.STUCK.ordinal());
        }
    }

    private enum MonitoredThreadState {
        RUNNING, STUCK;
    }
}
