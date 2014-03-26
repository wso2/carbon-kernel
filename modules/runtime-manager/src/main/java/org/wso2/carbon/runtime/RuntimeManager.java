/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.runtime.spi.Runtime;

import java.util.ArrayList;
import java.util.List;

public class RuntimeManager {
    private static Logger logger = LoggerFactory.getLogger(RuntimeManager.class);
    private List<Runtime> runtimeList = new ArrayList<Runtime>();


    /**
     * Register runtime instance on RuntimeManager
     *
     * @param runtime - runtime to be registered
     */
    public void registerRuntime(Runtime runtime) {
        runtimeList.add(runtime);
    }

    /**
     * Un-register runtime instance on RuntimeManager
     *
     * @param runtime - runtime to be un-registered
     */
    public void unRegisterRuntime(Runtime runtime) {
        runtimeList.remove(runtime);
    }

    /**
     * Return registered runtime list
     *
     * @return
     */
    public List<Runtime> getRuntimeList() {
        return runtimeList;
    }
}
