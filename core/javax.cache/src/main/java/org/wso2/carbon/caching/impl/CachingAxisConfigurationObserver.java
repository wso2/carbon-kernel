/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.caching.impl;

import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;

/**
 * TODO: class description
 */
public class CachingAxisConfigurationObserver implements Axis2ConfigurationContextObserver {
    @Override
    public void creatingConfigurationContext(int tenantId) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void createdConfigurationContext(ConfigurationContext configurationContext) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void terminatingConfigurationContext(ConfigurationContext configurationContext) {
        //To change body of implemented methods use File | Settings | File Templates.
        // TODO: Stop & remove all caches belonging to this tenant
        // Issue: if the tenant is active on other nodes, those also may get removed?
    }

    @Override
    public void terminatedConfigurationContext(ConfigurationContext configurationContext) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
