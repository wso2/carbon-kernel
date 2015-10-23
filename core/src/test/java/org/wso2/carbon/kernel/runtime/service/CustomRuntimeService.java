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
package org.wso2.carbon.kernel.runtime.service;

import org.wso2.carbon.kernel.internal.runtime.CarbonRuntimeService;
import org.wso2.carbon.kernel.internal.runtime.RuntimeManager;
import org.wso2.carbon.kernel.runtime.exception.RuntimeServiceException;

public class CustomRuntimeService extends CarbonRuntimeService {
//    CarbonRuntimeService carbonRuntimeService;

    public CustomRuntimeService(RuntimeManager runtimeManager) {
        super(runtimeManager);
    }

    @Override
    public void startRuntimes() throws RuntimeServiceException {
        super.startRuntimes();
    }

    @Override
    public void stopRuntimes() throws RuntimeServiceException {
        super.stopRuntimes();
    }

    @Override
    public void beginMaintenance() throws RuntimeServiceException {
        super.beginMaintenance();
    }

    @Override
    public void endMaintenance() throws RuntimeServiceException {
        super.endMaintenance();
    }
}
