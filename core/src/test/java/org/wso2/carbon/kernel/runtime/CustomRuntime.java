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
package org.wso2.carbon.kernel.runtime;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * CustomRuntime class for used in test cases.
 *
 * @since 5.0.0
 */
public class CustomRuntime implements Runtime {
    private static Log log = LogFactory.getLog(CustomRuntime.class);

    private RuntimeState state = RuntimeState.PENDING;


    @Override
    public void init() {
        log.info("Initializing Runtime");
        state = RuntimeState.INACTIVE;
    }

    @Override
    public void start() {
        log.info("Starting Runtime");
        state = RuntimeState.ACTIVE;
    }

    @Override
    public void stop() {
        log.info("Stopping Runtime");
        state = RuntimeState.INACTIVE;
    }

    @Override
    public void beginMaintenance() {
        log.info("Stopping Runtime");
        state = RuntimeState.MAINTENANCE;
    }

    @Override
    public void endMaintenance() {
        log.info("Stopping Runtime");
        state = RuntimeState.INACTIVE;
    }

    @Override
    public Enum<RuntimeState> getState() {
        return state;
    }

    @Override
    public void setState(RuntimeState runtimeState) {
        this.state = runtimeState;
    }
}
