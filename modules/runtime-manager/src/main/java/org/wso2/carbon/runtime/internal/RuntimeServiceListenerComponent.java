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

package org.wso2.carbon.runtime.internal;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.runtime.internal.RuntimeManager;
import org.wso2.carbon.runtime.spi.Runtime;

@Component(
        name = "org.wso2.carbon.runtime.internal.RuntimeServiceListenerComponent",
        description = "This service  component is responsible for retrieving the Runtime OSGi " +
                "service and register each runtime with runtime manager",
        immediate = true
)
@Reference(
        name = "carbon.runtime.service",
        referenceInterface = Runtime.class,
        cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE,
        policy = ReferencePolicy.DYNAMIC,
        bind = "registerRuntime",
        unbind = "unRegisterRuntime"
)

public class RuntimeServiceListenerComponent {
    private static Logger logger = LoggerFactory.getLogger(RuntimeServiceListenerComponent.class);
    private RuntimeManager runtimeManager = DataHolder.getInstance().getRuntimeManager();

    /**
     * Register the runtime instance
     *
     * @param runtime - runtime instance
     */
    protected void registerRuntime(Runtime runtime) {
        try {
            runtimeManager.registerRuntime(runtime);
        } catch (Exception e) {
            logger.error("Error while adding runtime to the Runtime manager", e);
        }
    }

    /**
     * Un-register the runtime instance
     *
     * @param runtime - runtime instance
     */
    protected void unRegisterRuntime(Runtime runtime) {
        try {
            runtimeManager.unRegisterRuntime(runtime);
        } catch (Exception e) {
            logger.error("Error while removing runtime from Runtime manager", e);
        }
    }

}
