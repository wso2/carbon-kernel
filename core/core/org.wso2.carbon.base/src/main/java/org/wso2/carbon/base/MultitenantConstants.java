/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
 *                                                                             
 * Licensed under the Apache License, Version 2.0 (the "License");             
 * you may not use this file except in compliance with the License.            
 * You may obtain a copy of the License at                                     
 *                                                                             
 *      http://www.apache.org/licenses/LICENSE-2.0                             
 *                                                                             
 * Unless required by applicable law or agreed to in writing, software         
 * distributed under the License is distributed on an "AS IS" BASIS,           
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.    
 * See the License for the specific language governing permissions and         
 * limitations under the License.                                              
 */
package org.wso2.carbon.base;

import javax.xml.namespace.QName;

/**
 * Constants used in the Carbon multitenancy implementation
 */
public class MultitenantConstants {
    public static String IS_SUPER_TENANT = "is.super.tenant";

    public static final String MULTITENANT_DISPATCHER_SERVICE = "__MultitenantDispatcherService";
    public static final QName MULTITENANT_DISPATCHER_OPERATION = new QName("dispatch");

    public static final String TENANT_MR_STARTED_FAULT = "tenantMRStartedFault";
    public static final String TENANT_DOMAIN = "tenantDomain";
    public static final String TENANT_ID = "tenantId";
    public static final String SUPER_TENANT_NAME = "super";
    public static final String TENANT_AWARE_URL_PREFIX = "t";
    public static final int SUPER_TENANT_ID = -1234;
    public static final String TENANT_DOMAIN_HEADER_NAMESPACE = "http://cloud.wso2.com/";
    public static final String TENANT_DOMAIN_HEADER_NAME = "TenantDomain";
    public static final String SUPER_TENANT_DOMAIN_NAME = "carbon.super";
    public static final int INVALID_TENANT_ID = -1;

    public static final String REQUIRE_SUPER_TENANT = "require-super-tenant";
    public static final String REQUIRE_NOT_SUPER_TENANT = "require-not-super-tenant";
    public static String IS_MASTER_TENANT = "is-master-tenant";

    public static final String TENANT_MODULE_BUNDLES = "tenant.module.bundles";
    public static final String TENANT_MODULE_LOADED = "tenant.module.loaded";

    /**
     * Last time at which this tenant was accessed
     */
    public static final String LAST_ACCESSED = "last.accessed.time";

    /**
     * The allowed tenant idle time before the tenant code is undeployed
     */
    public static final String TENANT_IDLE_TIME = "tenant.idle.time";

    public static final String TRANSPORT_OUT_DESCRIPTION = "TRANSPORT_OUT_DESCRIPTION";
    public static final String TENANT_REQUEST_MSG_CTX = "TENANT_REQUEST_MSG_CTX";

    public static final String SSO_AUTH_SESSION_ID = "SSOAuthSessionID";
}
