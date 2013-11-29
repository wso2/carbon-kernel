<!--
 ~ Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->
<TenantManagers>
    <TenantManager class="org.wso2.carbon.user.core.tenant.JDBCTenantManager">
        <Property name="MultiTenantRealmConfigBuilder">org.wso2.carbon.user.core.config.multitenancy.SimpleRealmConfigBuilder</Property>
    </TenantManager>

    <!--When the primary user store is using LDAP user store, in MT mode following tenant manager will be used.-->
    <TenantManager class="org.wso2.carbon.user.core.tenant.CommonHybridLDAPTenantManager">
        <Property name="MultiTenantRealmConfigBuilder">org.wso2.carbon.user.core.config.multitenancy.CommonLDAPRealmConfigBuilder</Property>
        <Property name="RootPartition">dc=wso2,dc=org</Property>
        <Property name="OrganizationalObjectClass">organizationalUnit</Property>
        <Property name="OrganizationalAttribute">ou</Property>
        <Property name="OrganizationalSubContextObjectClass">organizationalUnit</Property>
        <Property name="OrganizationalSubContextAttribute">ou</Property>
    </TenantManager>
</TenantManagers>

