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

package org.wso2.carbon.tomcat.ext.internal;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * @scr.component name="tomcat.ext.service.comp" immediate="true"
 * @scr.reference name="user.realm.provider"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService"
 * unbind="unsetRealmService"
 * @scr.reference name="registry.service.provider"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService"
 * unbind="unsetRegistryService"
 */
public class CarbonRealmServiceComponent {
    private static Log log = LogFactory.getLog(CarbonRealmServiceComponent.class);

    protected void setRealmService(RealmService userRealmService) {
        CarbonRealmServiceHolder.setRealmService(userRealmService);
        if (log.isDebugEnabled()) {
            log.debug(userRealmService + "is being set");
        }
    }

    protected void unsetRealmService(RealmService userRealmService) {
        CarbonRealmServiceHolder.setRealmService(null);
        if (log.isDebugEnabled()) {
            log.debug(userRealmService + "is being unset");
        }
    }

    protected void setRegistryService(RegistryService registryService) {
        CarbonRealmServiceHolder.setRegistryService(registryService);
        if (log.isDebugEnabled()) {
            log.debug(registryService + "is being set");
        }
    }

    protected void unsetRegistryService(RegistryService registryService) {
        CarbonRealmServiceHolder.setRegistryService(null);
        if (log.isDebugEnabled()) {
            log.debug(registryService + "is being unset");
        }
    }

}

