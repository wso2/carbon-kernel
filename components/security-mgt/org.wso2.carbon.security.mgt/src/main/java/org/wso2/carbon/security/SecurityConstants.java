/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.security;

import org.wso2.carbon.core.RegistryResources;

import javax.xml.namespace.QName;

public class SecurityConstants {

    public static final String SECURITY_NAMESPACE = "http://www.wso2.org/products/carbon/security";
    public static final QName SUMMARY_QN = new QName(SECURITY_NAMESPACE, "Summary");
    public static final QName DESCRIPTION_QN = new QName(SECURITY_NAMESPACE, "Description");
    public static final QName ID_QN = new QName("id");
    public static final QName CATEGORY_QN = new QName(SECURITY_NAMESPACE, "Category");
    public static final QName MODULES_QN = new QName(SECURITY_NAMESPACE, "Modules");
    public static final QName WSUID_QN = new QName(SECURITY_NAMESPACE, "WsuId");
    public static final QName TYPE_QN = new QName(SECURITY_NAMESPACE, "Type");
    public static final QName IS_GEN_POLICY_QN = new QName(SECURITY_NAMESPACE, "isGeneralPolicy");

    //Resources
    public static final String SECURITY_POLICY = RegistryResources.COMPONENTS
            + "org.wso2.carbon.security.mgt/policy";

    //Basic scenarios
    public static final String CUSTOM_SECURITY_SCENARIO = "customScenario";
    public static final String USERNAME_TOKEN_SCENARIO_ID = "scenario1";
    public static final String CONFIDENTIALITY_SCENARIO_ID = "scenario5";
    public static final String INTEGRITY_SCENARIO_ID = "scenario4";
    public static final String NONREPUDIATION_SCENARIO_ID = "scenario2";
    public static final String SCENARIO_DISABLE_SECURITY = "DisableSecurity";
    public static final String POLICY_FROM_REG_SCENARIO = "policyFromRegistry";

    public static final String CUSTOM_SECURITY_SCENARIO_SUMMARY = "Custom security policy";

    public static final String PROP_USER_STORE = "service.userstore";

    public static final String EMPTY_SERVICE_VERSION = "SNAPSHOT";
    public static final String EMPTY_MODULE_VERSION = "SNAPSHOT";

    public static final String RAMPART_MODULE_NAME = "rampart";
    public static final String ASSOCIATION_TRUSTED_KEYSTORE = "trusted-keystore";
    public static final String ASSOCIATION_PRIVATE_KEYSTORE = RegistryResources.Associations.PRIVATE_KEYSTORE;
    public static final String ASSOCIATION_SERVICE_SECURING_POLICY = "service-secpolicy";
    public static final String ASSOCIATION_STORE_TYPE = "service-secpolicy";

    public static final String USER = "rampart.config.user";

    public static final String HTTPS_TRANSPORT = "https";

    public static final String KEY_STORES = RegistryResources.SecurityManagement.KEY_STORES;
    //properties
    public static final String PROP_ROLE = RegistryResources.SecurityManagement.PROP_ROLE;
    public static final String PROP_PASSWORD = RegistryResources.SecurityManagement.PROP_PASSWORD;
    public static final String PROP_TYPE = RegistryResources.SecurityManagement.PROP_TYPE;
    public static final String PROP_USERS = RegistryResources.SecurityManagement.PROP_USERS;
    public static final String PROP_PRIVATE_KEY_ALIAS = RegistryResources.SecurityManagement.PROP_PRIVATE_KEY_ALIAS;
    public static final String PROP_PRIVATE_KEY_PASS = RegistryResources.SecurityManagement.PROP_PRIVATE_KEY_PASS;
    public static final String PROP_DESCRIPTION = "description";
    public static final String PROP_AUTHENTICATOR_CLASS = "class";
    public static final String PROP_INTERNAL = "internal";
    public static final String PROP_PROVIDER = "provider";
    public static final String PROP_SELECT_ALL = "selectAll";
    public static final String PROP_RAHAS_SCT_ISSUER = "rahas.sctissuer";
    public static final String PROP_TENANT_PUB_KEY_FILE_NAME_APPENDER = "tenant.pub.key.file.name.appender";
    //associations
    public static final String ASSOCIATION_STORE_GROUP = RegistryResources.SecurityManagement.ASSOCIATION_STORE_GROUP;
    public static final String ASSOCIATION_TENANT_KS_PUB_KEY = "assoc.tenant.ks.pub.key";
    public static final String DEFAULT_STORE = RegistryResources.SecurityManagement.DEFAULT_STORE;
    public static final String ADMIN_USER = "admin";
    public static final String ADMIN_GROUP = "admin-group";
    // registry identifiers
    public static final String CONFIG_REGISTRY_IDENTIFIER = "conf";
    public static final String GOVERNANCE_REGISTRY_IDENTIFIER = "gov";
    public static final String SECURITY_POLICY_PATH = "secPolicyRegistryPath";
    public static final int MAX_USER_COUNT = 50;
    public static final String WS_SEC_UTILITY_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-" +
            "200401-wss-wssecurity-utility-1.0.xsd";
    public static final String POLICY_ID = "Id";
    public static final QName POLICY_ID_QNAME = new QName(WS_SEC_UTILITY_NS, POLICY_ID);
    public static final int DEFAULT_ITEMS_PER_PAGE = 50;
    public static final int ITEMS_PER_PAGE = 10;
    public static final int CACHING_PAGE_SIZE = 5;
    //AxisService parameter names
    public static final String SCENARIO_ID_PARAM_NAME = "ScenarioID";
    public static final String SCENARIO_ID_SET_PARAM_NAME = "ScenarioIDSet";
    public static final String ROLE_ADMIN = RegistryResources.SecurityManagement.ADMIN_ROLE;
    public static final String ROLE_GENERAL = "General User";

    public static final String TRUST_MODULE = "rahas";
    public static final String ALLOWED_ROLES_PARAM_NAME = "org.wso2.carbon.security.allowedroles";

    // Constants related to security policy parts
    public static final String CARBON_SEC_CONFIG = "CarbonSecConfig";
    public static final String TRUST = "Trust";
    public static final String ENCRYPTED = "encrypted";
    public static final String AUTHORIZATION = "Authorization";
    public static final String KERBEROS = "Kerberos";
    public static final String PROPERTY_LABEL = "property";
    public static final String NAME_LABEL = "name";

    private SecurityConstants(){}

}
