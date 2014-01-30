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

package org.wso2.carbon.runtime;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.runtime.api.RuntimeService;
import org.wso2.carbon.runtime.exception.RuntimeServiceException;
import org.wso2.carbon.runtime.spi.Runtime;

import java.util.List;

/**
 * Implementation class for the RuntimeService interface
 */

public class CarbonRuntimeService implements RuntimeService {
    private static final Log log = LogFactory.getLog(CarbonRuntimeService.class);
    RuntimeManager runtimeManager;

    public CarbonRuntimeService(RuntimeManager runtimeManager) {
        this.runtimeManager = runtimeManager;
    }

    /**
     * Starts registered runtime
     * @throws RuntimeServiceException - thrown if any issues occur during the process
     */
    @Override
    public void startRuntimes() throws RuntimeServiceException {
        List<Runtime> runtimeMap = runtimeManager.getRuntimeList();
        for (Runtime runtime : runtimeMap) {
            runtime.start();
        }
    }

    /**
     * Stops registered runtime
     * @throws RuntimeServiceException - thrown if any issues occur during the process
     */
    @Override
    public void stopRuntimes() throws RuntimeServiceException {
        List<Runtime> runtimeMap = runtimeManager.getRuntimeList();
        for (Runtime runtime : runtimeMap) {
            runtime.stop();
        }
    }

    /**
     * Puts registered runtime into MAINTENANCE state
     * @throws RuntimeServiceException - thrown if any issues occur during the process
     */
    @Override
    public void beginMaintenance() throws RuntimeServiceException {
        List<Runtime> runtimeMap = runtimeManager.getRuntimeList();
        for (Runtime runtime : runtimeMap) {
            runtime.startMaintenance();
        }
    }

    /**
     * Puts registered runtime into MAINTENANCE state
     * @throws RuntimeServiceException - thrown if any issues occur during the process
     */
    @Override
    public void endMaintenance() {
        List<Runtime> runtimeMap = runtimeManager.getRuntimeList();
        for (Runtime runtime : runtimeMap) {
            runtime.stopMaintenance();
        }
    }

}
