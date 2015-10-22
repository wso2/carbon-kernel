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
package org.wso2.carbon.kernel.runtime.spi;

import org.wso2.carbon.kernel.runtime.RuntimeState;
import org.wso2.carbon.kernel.runtime.exception.RuntimeServiceException;

/**
 * <p>
 * This interface is used to register/integrate custom runtime into Carbon server, by extending this
 * interface you can integrate and run a custom Runtime instance on top of Carbon server
 * </p>
 * <p>
 * A developer who wants to integrate a custom runtime  with Carbon server should implement this.
 * The implementation should then be registered as an OSGi service using the Runtime interface for the
 * to find and add it to the configuration
 * </p>
 * The implementation of this interface can be different from one Runtime to another depending on its
 * requirements and behaviour.
 *
 * @since 5.0.0
 */

public interface Runtime {

    /**
     * <p>
     * Initialize the Runtime
     * </p>
     * This will contain all the code segments that necessary to be called during runtime initialization
     * process. This can be different from one Runtime to another
     *
     * @throws RuntimeServiceException - on error while trying to initializing the Runtime
     */
    void init() throws RuntimeServiceException;

    /**
     * <p>
     * Start the Runtime
     * </p>
     * This will contain all the code segments that necessary to be called during runtime start() get invoked
     * This implementation will not be necessary for all the runtimes
     *
     * @throws RuntimeServiceException - on error while trying to starting the Runtime
     */
    void start() throws RuntimeServiceException;

    /**
     * <p>
     * Stop the Runtime
     * </p>
     * This will contain all the code that need to be called when runtime need to be stopped
     *
     * @throws RuntimeServiceException - on error while trying to stopping the Runtime
     */
    void stop() throws RuntimeServiceException;

    /**
     * <p>
     * Put the Runtime into maintenance mode
     * </p>
     * This will contain all the code that need to be called when runtime starting its MAINTENANCE state
     *
     * @throws RuntimeServiceException - on error while trying to start maintenance of the Runtime
     */
    void beginMaintenance() throws RuntimeServiceException;

    /**
     * <p>
     * Put the Runtime into INACTIVE state form MAINTENANCE state
     * </p>
     * This will contain all the code that need to be called when runtime stops its maintenance mode
     *
     * @throws RuntimeServiceException - on error while trying to stop maintenance of the Runtime
     */
    void endMaintenance() throws RuntimeServiceException;

    /**
     * Return the current state of the runtime
     *
     * @return RuntimeState - current state of the Runtime
     * @see RuntimeState
     */
    Enum<RuntimeState> getState();

    /**
     * Set current state of a runtime
     *
     * @param runtimeState - new Runtime state
     * @see RuntimeState
     */
    void setState(RuntimeState runtimeState);

}
