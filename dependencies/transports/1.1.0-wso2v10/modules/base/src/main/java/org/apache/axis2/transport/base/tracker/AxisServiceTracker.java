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

package org.apache.axis2.transport.base.tracker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisEvent;
import org.apache.axis2.engine.AxisObserver;

/**
 * <p>Tracks services deployed in a given {@link AxisConfiguration}.
 * The tracker is configured with references to three objects:</p>
 * <ol>
 *   <li>An {@link AxisConfiguration} to watch.</li>
 *   <li>An {@link AxisServiceFilter} restricting the services to track.</li>
 *   <li>An {@link AxisServiceTrackerListener} receiving tracking events.</li>
 * </ol>
 * <p>An instance of this class maintains an up-to-date list of services
 * satisfying all of the following criteria:</p>
 * <ol>
 *   <li>The service is deployed in the given {@link AxisConfiguration}.</li>
 *   <li>The service is started, i.e. {@link AxisService#isActive()} returns true.</li>
 *   <li>The service matches the criteria specified by the given
 *       {@link AxisServiceFilter} instance.</li>
 * </ol>
 * <p>Whenever a service appears on the list, the tracker will call
 * {@link AxisServiceTrackerListener#serviceAdded(AxisService)}. When a service disappears, it
 * will call {@link AxisServiceTrackerListener#serviceRemoved(AxisService)}.</p>
 * <p>When the tracker is created, it is initially in the stopped state. In this state no
 * events will be sent to the listener. It can be started using {@link #start()} and stopped again
 * using {@link #stop()}. The tracker list is defined to be empty when the tracker is in the
 * stopped state. This implies that a call to {@link #start()} will generate
 * {@link AxisServiceTrackerListener#serviceAdded(AxisService)} events for all services that meet
 * the above criteria at that point in time. In the same way, {@link #stop()} will generate
 * {@link AxisServiceTrackerListener#serviceRemoved(AxisService)} events for the current entries
 * in the list.</p>
 * <p>As a corollary the tracker guarantees that during a complete lifecycle (start-stop),
 * there will be exactly one {@link AxisServiceTrackerListener#serviceRemoved(AxisService)} event
 * for every {@link AxisServiceTrackerListener#serviceAdded(AxisService)} event and vice-versa.
 * This property is important when the tracker is used to allocate resources for a dynamic set
 * of services.</p>
 * 
 * <h2>Limitations</h2>
 *
 * <p>The tracker is not able to detect property changes on services. E.g. if a service initially
 * matches the filter criteria, but later changes so that it doesn't match the criteria any more,
 * the tracker will not be able to detect this and the service will not be removed from the tracker
 * list.</p>
 */
public class AxisServiceTracker {
    private final AxisObserver observer = new AxisObserver() {
        public void init(AxisConfiguration axisConfig) {}

        public void serviceUpdate(AxisEvent event, final AxisService service) {
            switch (event.getEventType()) {
                case AxisEvent.SERVICE_DEPLOY:
                case AxisEvent.SERVICE_START:
                    if (filter.matches(service)) {
                        boolean pending;
                        synchronized (lock) {
                            if (pending = (pendingActions != null)) {
                                pendingActions.add(new Runnable() {
                                    public void run() {
                                        serviceAdded(service);
                                    }
                                });
                            }
                        }
                        if (!pending) {
                            serviceAdded(service);
                        }
                    }
                    break;
                case AxisEvent.SERVICE_REMOVE:
                case AxisEvent.SERVICE_STOP:
                    // Don't check filter here because the properties of the service may have
                    // changed in the meantime.
                    boolean pending;
                    synchronized (lock) {
                        if (pending = (pendingActions != null)) {
                            pendingActions.add(new Runnable() {
                                public void run() {
                                    serviceRemoved(service);
                                }
                            });
                        }
                    }
                    if (!pending) {
                        serviceRemoved(service);
                    }
            }
        }

        public void moduleUpdate(AxisEvent event, AxisModule module) {}
        public void addParameter(Parameter param) throws AxisFault {}
        public void removeParameter(Parameter param) throws AxisFault {}
        public void deserializeParameters(OMElement parameterElement) throws AxisFault {}
        public Parameter getParameter(String name) { return null; }
        public ArrayList<Parameter> getParameters() { return null; }
        public boolean isParameterLocked(String parameterName) { return false; }
        public void serviceGroupUpdate(AxisEvent event, AxisServiceGroup serviceGroup) {}
    };
    
    private final AxisConfiguration config;
    final AxisServiceFilter filter;
    private final AxisServiceTrackerListener listener;
    
    /**
     * Object used to synchronize access to {@link #pendingActions} and {@link #services}.
     */
    final Object lock = new Object();
    
    /**
     * Queue for notifications received by the {@link AxisObserver} during startup of the tracker.
     * We need this because the events may already be reflected in the list of services returned
     * by {@link AxisConfiguration#getServices()} (getting the list of currently deployed services
     * and adding the observer can't be done atomically). It also allows us to make sure that
     * events are sent to the listener in the right order, e.g. when a service is being removed
     * during startup of the tracker.
     */
    Queue<Runnable> pendingActions;
    
    /**
     * The current list of services. <code>null</code> if the tracker is stopped.
     */
    private Set<AxisService> services;
    
    public AxisServiceTracker(AxisConfiguration config, AxisServiceFilter filter,
            AxisServiceTrackerListener listener) {
        this.config = config;
        this.filter = filter;
        this.listener = listener;
    }
    
    /**
     * Check whether the tracker is started.
     * 
     * @return <code>true</code> if the tracker is started
     */
    public boolean isStarted() {
        return services != null;
    }

    /**
     * Start the tracker.
     * 
     * @throws IllegalStateException if the tracker has already been started
     */
    public void start() {
        if (services != null) {
            throw new IllegalStateException();
        }
        synchronized (lock) {
            pendingActions = new LinkedList<Runnable>();
            config.addObservers(observer);
            services = new HashSet<AxisService>();
        }
        for (AxisService service : config.getServices().values()) {
            if (service.isActive() && filter.matches(service)) {
                serviceAdded(service);
            }
        }
        while (true) {
            Runnable action;
            synchronized (lock) {
                action = pendingActions.poll();
                if (action == null) {
                    pendingActions = null;
                    break;
                }
            }
            action.run();
        }
    }
    
    void serviceAdded(AxisService service) {
        // callListener may be false because the observer got an event for a service that
        // was already in the initial list of services retrieved by AxisConfiguration#getServices.
        boolean callListener;
        synchronized (lock) {
            callListener = services.add(service);
        }
        if (callListener) {
            listener.serviceAdded(service);
        }
    }
    
    void serviceRemoved(AxisService service) {
        // callListener may be false because the observer invokes this method without applying the
        // filter.
        boolean callListener;
        synchronized (lock) {
            callListener = services.remove(service);
        }
        if (callListener) {
            listener.serviceRemoved(service);
        }
    }
    
    /**
     * Stop the tracker.
     * 
     * @throws IllegalStateException if the tracker is not started
     */
    public void stop() {
        if (services == null) {
            throw new IllegalStateException();
        }
        config.removeObserver(observer);
        for (AxisService service : services) {
            listener.serviceRemoved(service);
        }
        services = null;
    }
}
