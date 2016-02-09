/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.hazelcast.internal;

import com.hazelcast.osgi.HazelcastOSGiInstance;
import com.hazelcast.osgi.HazelcastOSGiService;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.hazelcast.CoordinatedActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * DataHolder.
 *
 * @since 1.0.0
 */
public class DataHolder {
    private static DataHolder instance = new DataHolder();
    private BundleContext bundleContext = null;
    private HazelcastOSGiService hazelcastOSGiService;
    private HazelcastOSGiInstance hazelcastOSGiInstance;
    private List<CoordinatedActivity> coordinatedActivityList = new ArrayList<>();

    public static DataHolder getInstance() {
        return instance;
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public HazelcastOSGiService getHazelcastOSGiService() {
        return hazelcastOSGiService;
    }

    public void setHazelcastOSGiService(HazelcastOSGiService hazelcastOSGiService) {
        this.hazelcastOSGiService = hazelcastOSGiService;
    }

    public HazelcastOSGiInstance getHazelcastOSGiInstance() {
        return hazelcastOSGiInstance;
    }

    public void setHazelcastOSGiInstance(HazelcastOSGiInstance hazelcastOSGiInstance) {
        this.hazelcastOSGiInstance = hazelcastOSGiInstance;
    }

    public List<CoordinatedActivity> getCoordinatedActivityList() {
        return coordinatedActivityList;
    }

    public void addCoordinatedActivity(CoordinatedActivity coordinatedActivity) {
        this.coordinatedActivityList.add(coordinatedActivity);
    }

    public void removeCoordinatedActivity(CoordinatedActivity coordinatedActivity) {
        this.coordinatedActivityList.remove(coordinatedActivity);
    }
}
