/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.kernel.transports;

/**
 * This class represents a transport in Carbon. When adding a new transport to the kernel, this class needs to be
 * extended, implement the start, stop, beginMaintenance, endMaintenance methods and register as an OSGi Service.
 *
 * @since 5.0.0
 */
public abstract class CarbonTransport {

    /**
     * Unique ID representing a transport.
     */
    protected String id;

    protected State state = State.UNINITIALIZED;

    public CarbonTransport(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public State getState() {
        return state;
    }

    void startTransport() {
        if (state.equals(State.UNINITIALIZED) || state.equals(State.IN_MAINTENANCE) || state.equals(State.STOPPED)) {
            start();
            state = State.STARTED;
        } else {
            throw new IllegalStateException("Cannot start transport " + id + ". Current state: " + state);
        }
    }

    /**
     * Implementation of the transport start process.
     */
    protected abstract void start();

    void stopTransport() {
        if (state.equals(State.STARTED)) {
            stop();
            state = State.STOPPED;
        } else {
            throw new IllegalStateException("Cannot stop transport " + id + ". Current state: " + state);
        }
    }

    /**
     * Implementation of the transport stop process.
     */
    protected abstract void stop();

    void beginTransportMaintenance() {
        if (state.equals(State.STARTED)) {
            beginMaintenance();
            state = State.IN_MAINTENANCE;
        } else {
            throw new IllegalStateException("Cannot put transport " + id +
                    " into maintenance. Current state: " + state);
        }
    }

    /**
     * Implementation of the transport start maintenance process.
     */
    protected abstract void beginMaintenance();

    void endTransportMaintenance() {
        if (state.equals(State.IN_MAINTENANCE)) {
            endMaintenance();
            state = State.STARTED;
        } else {
            throw new IllegalStateException("Cannot end maintenance of transport " + id + ". Current state: " + state);
        }
    }

    /**
     * Implementation of the transport end maintenance process.
     */
    protected abstract void endMaintenance();

    /**
     * Enum to holds the state of Transport.
     */
    public enum State {
        UNINITIALIZED, STARTED, STOPPED, IN_MAINTENANCE;

        @Override
        public String toString() {
            return name();
        }
    }
}
