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
package org.wso2.carbon.kernel.internal.runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.runtime.Runtime;
import org.wso2.carbon.kernel.runtime.RuntimeService;
import org.wso2.carbon.kernel.runtime.RuntimeState;
import org.wso2.carbon.kernel.runtime.exception.RuntimeServiceException;

import java.util.List;

/**
 * Implementation class for the RuntimeService interface.
 *
 * @since 5.0.0
 */

public class CarbonRuntimeService implements RuntimeService {
    private static Logger logger = LoggerFactory.getLogger(CarbonRuntimeService.class);
    RuntimeManager runtimeManager;

    public CarbonRuntimeService(RuntimeManager runtimeManager) {
        this.runtimeManager = runtimeManager;
    }

    /**
     * Starts registered runtime.
     *
     * @throws RuntimeServiceException - thrown if any issues occur during the process
     */
    @Override
    public void startRuntimes() throws RuntimeServiceException {
        List<Runtime> runtimeMap = runtimeManager.getRuntimeList();
        for (Runtime runtime : runtimeMap) {
            if (runtime.getState() == RuntimeState.INACTIVE) {
                runtime.start();
            } else if (runtime.getState() == RuntimeState.PENDING) {
                throw new RuntimeServiceException("Runtime not initialized." + runtime.getClass().getName());
            } else if (runtime.getState() == RuntimeState.MAINTENANCE) {
                throw new RuntimeServiceException("Runtime is in maintenance mode." + runtime.getClass().getName());
            } else {
                logger.error("Runtime already started : " + runtime.getClass().getName());
            }
        }
    }

    /**
     * Stops registered runtime.
     *
     * @throws RuntimeServiceException - thrown if any issues occur during the process
     */
    @Override
    public void stopRuntimes() throws RuntimeServiceException {
        List<Runtime> runtimeMap = runtimeManager.getRuntimeList();
        for (Runtime runtime : runtimeMap) {
            if (runtime.getState() == RuntimeState.PENDING) {
                throw new RuntimeServiceException("Runtime not initialized." + runtime.getClass().getName());
            } else {
                runtime.stop();
            }
        }
    }

    /**
     * Puts registered runtime into MAINTENANCE state.
     *
     * @throws RuntimeServiceException - thrown if any issues occur during the process
     */
    @Override
    public void beginMaintenance() throws RuntimeServiceException {
        List<Runtime> runtimeMap = runtimeManager.getRuntimeList();
        for (Runtime runtime : runtimeMap) {
            if (runtime.getState() == RuntimeState.PENDING) {
                throw new RuntimeServiceException("Runtime not initialized." + runtime.getClass().getName());
            } else {
                runtime.beginMaintenance();
            }
        }
    }

    /**
     * Puts registered runtime into MAINTENANCE state.
     *
     * @throws RuntimeServiceException - thrown if any issues occur during the process
     */
    @Override
    public void endMaintenance() throws RuntimeServiceException {
        List<Runtime> runtimeMap = runtimeManager.getRuntimeList();
        for (Runtime runtime : runtimeMap) {
            if (runtime.getState() == RuntimeState.PENDING) {
                throw new RuntimeServiceException("Runtime not initialized." + runtime.getClass().getName());
            } else {
                runtime.endMaintenance();
            }
        }
    }

}
