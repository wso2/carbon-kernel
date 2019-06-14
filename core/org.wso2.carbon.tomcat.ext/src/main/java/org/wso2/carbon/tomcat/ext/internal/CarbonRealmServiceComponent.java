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
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

@Component(name = "tomcat.ext.service.comp", immediate = true)
public class CarbonRealmServiceComponent {
    private static Log log = LogFactory.getLog(CarbonRealmServiceComponent.class);

    @Reference(name = "user.realm.provider", cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC, 
            unbind = "unsetRealmService")
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

    @Reference(name = "registry.service.provider", cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC, 
            unbind = "unsetRegistryService")
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

