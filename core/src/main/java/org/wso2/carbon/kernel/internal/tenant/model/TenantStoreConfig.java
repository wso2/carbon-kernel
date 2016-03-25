/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.kernel.internal.tenant.model;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Tenant Store Bean Definition.
 *
 * @since 5.1.0
 */

@XmlRootElement(name = "Tenants")
@XmlAccessorType(XmlAccessType.FIELD)
public class TenantStoreConfig {

    @XmlElement(name = "Tenant")
    private List<TenantConfig> tenantConfigs = new ArrayList<>(0);

    public List<TenantConfig> getTenantConfigs() {
        return tenantConfigs;
    }

    public void addTenantConfig(TenantConfig tenantConfig) {
        this.tenantConfigs.add(tenantConfig);
    }
}
