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
package org.wso2.carbon.sample.runtime.mgt;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 */
@Component(
        name = "org.wso2.carbon.sample.runtime.mgt.RuntimeManager",
        immediate = true
)
public class RuntimeManager {

    private static List<Runtime> runtimeList = new ArrayList<>();

    @Activate
    public void activate(BundleContext bundleContext) {
    }

    @Deactivate
    public void deactivate(BundleContext bundleContext) {

    }

    @Reference(
            name = "sample.runtime.service.reference",
            service = Runtime.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "deregisterRuntime"
    )
    public void registerRuntime(Runtime runtime) {
        runtimeList.add(runtime);
    }

    public void deregisterRuntime(Runtime runtime) {
        runtimeList.remove(runtime);
    }

    public int getRuntimeCount() {
        return runtimeList.size();
    }

}
