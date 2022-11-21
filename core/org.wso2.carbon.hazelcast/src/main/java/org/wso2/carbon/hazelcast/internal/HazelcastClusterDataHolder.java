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

import org.apache.axis2.context.ConfigurationContext;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.core.clustering.api.CoordinatedActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * This singleton data holder contains all the data required by the Hazelcast Cluster OSGi bundle
 */
public class HazelcastClusterDataHolder {
    private static final HazelcastClusterDataHolder instance = new HazelcastClusterDataHolder();
    private BundleContext bundleContext;
    private ConfigurationContext mainServerConfigContext;
    private List<CoordinatedActivity> coordinatedActivities = new ArrayList<CoordinatedActivity>();

    public static HazelcastClusterDataHolder getInstance() {
        return instance;
    }

    private HazelcastClusterDataHolder() {
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void setMainServerConfigContext(ConfigurationContext mainServerConfigContext) {
        this.mainServerConfigContext = mainServerConfigContext;
    }

    public ConfigurationContext getMainServerConfigContext() {
        return mainServerConfigContext;
    }

    public void addCoordinatedActivity(CoordinatedActivity coordinatedActivity) {
        coordinatedActivities.add(coordinatedActivity);
    }

    public void removeCoordinatedActivity(CoordinatedActivity coordinatedActivity) {
        coordinatedActivities.remove(coordinatedActivity);
    }

    public List<CoordinatedActivity> getCoordinatedActivities() {
        return coordinatedActivities;
    }
}
