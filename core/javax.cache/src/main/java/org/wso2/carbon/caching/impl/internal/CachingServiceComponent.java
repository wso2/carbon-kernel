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
package org.wso2.carbon.caching.impl.internal;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.caching.impl.DataHolder;
import org.wso2.carbon.caching.impl.DistributedMapProvider;

/**
 * @scr.component name="org.wso2.carbon.caching.impl.internal.CachingServiceComponent" immediate="true"
 * @scr.reference name="distributedMapProvider" interface="org.wso2.carbon.caching.impl.DistributedMapProvider"
 * cardinality="0..1" policy="dynamic"  bind="setDistributedMapProvider" unbind="unsetDistributedMapProvider"
 */
public class CachingServiceComponent {
    private static final Log log = LogFactory.getLog(CachingServiceComponent.class);
    private DataHolder dataHolder = DataHolder.getInstance();

    protected void activate(ComponentContext ctx) {
       if(log.isDebugEnabled()){
           log.debug("CachingServiceComponent activated");
       }
    }

    protected void deactivate(ComponentContext ctx) {
    }

    protected void setDistributedMapProvider(DistributedMapProvider mapProvider) {
        dataHolder.setDistributedMapProvider(mapProvider);
    }

    protected void unsetDistributedMapProvider(DistributedMapProvider mapProvider) {
        dataHolder.setDistributedMapProvider(null);
    }
}
