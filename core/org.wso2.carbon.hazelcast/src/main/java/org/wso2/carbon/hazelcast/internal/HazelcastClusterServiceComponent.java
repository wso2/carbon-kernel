/*
 * Copyright (c) 2022, WSO2 LLC (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.hazelcast.internal;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.core.clustering.api.CoordinatedActivity;
import org.wso2.carbon.utils.ConfigurationContextService;

@Component(name = "org.wso2.carbon.hazelcast.internal.HazelcastClusterServiceComponent", immediate = true)
public class HazelcastClusterServiceComponent {

    private HazelcastClusterDataHolder dataHolder = HazelcastClusterDataHolder.getInstance();

    @Activate
    protected void activate(ComponentContext ctxt) {

    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {

    }

    @Reference(name = "coordinatedActivity", cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC, unbind = "removeCoordinatedActivity")
    protected void addCoordinatedActivity(CoordinatedActivity coordinatedActivity) {
        dataHolder.addCoordinatedActivity(coordinatedActivity);
    }

    protected void removeCoordinatedActivity(CoordinatedActivity coordinatedActivity) {
        dataHolder.removeCoordinatedActivity(coordinatedActivity);
    }

    @Reference(name = "config.context.service", cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigurationContextService")
    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        dataHolder.setMainServerConfigContext(contextService.getServerConfigContext());
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        dataHolder.setMainServerConfigContext(null);
    }
}
