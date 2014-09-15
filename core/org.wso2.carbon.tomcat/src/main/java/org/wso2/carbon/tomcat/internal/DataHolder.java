/*
 * Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.tomcat.internal;

import org.osgi.framework.BundleContext;

import javax.servlet.ServletContainerInitializer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holds information of an OSGi bundle, like bundle context.Here it contains methods to
 * add, remove. get ServletContainerInitializers.
 */
public class DataHolder {
    private static DataHolder instance = new DataHolder();

    private BundleContext bundleContext;
    private List<ServletContainerInitializer> sciList = new ArrayList<ServletContainerInitializer>();

    public static DataHolder getInstance() {
        return instance;
    }

    private DataHolder() {
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void addServletContainerInitializer(ServletContainerInitializer sci){
        sciList.add(sci);
    }

    public void removeServletContainerInitializer(ServletContainerInitializer sci){
        sciList.remove(sci);
    }

    public List<ServletContainerInitializer> getServiceContainerInitializers(){
        return Collections.unmodifiableList(sciList);
    }
}
