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

package org.wso2.carbon.runtime.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.runtime.RuntimeManager;

public class RuntimeActivator  implements BundleActivator {

    private static Logger logger = LoggerFactory.getLogger(RuntimeActivator.class);


    public void start(BundleContext bundleContext) throws Exception {
            logger.debug("Starting Carbon Runtime Manager");
            RuntimeManager runtimeManager = new RuntimeManager();
            // Add runtime manager to the data holder for later usages/references of this object
            DataHolder.getInstance().setRuntimeManager(runtimeManager);
    }

    public void stop(BundleContext bundleContext) throws Exception {

    }

}
