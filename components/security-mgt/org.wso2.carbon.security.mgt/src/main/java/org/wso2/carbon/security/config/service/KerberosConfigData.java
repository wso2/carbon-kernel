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

package org.wso2.carbon.security.config.service;

/**
 * This class encapsulates data about kerberos configurations.
 * E.g :- service principle name, password, KDC etc ...
 */
@SuppressWarnings({"UnusedDeclaration"})
public class KerberosConfigData {

    public static final String KERBEROS_CONFIG_RESOURCE = "kerberos";
    public static final String KERBEROS_CONFIG_FILE_NAME = "krb5.conf";
    public static final String KERBEROS_CONFIG_FILE_SYSTEM_PROPERTY = "java.security.krb5.conf";

    private String servicePrincipleName;
    private String servicePrinciplePassword;
    private String realmName;
    private String kdcAddress;
    private boolean useSubjectCredentialsOnly;

    public String getServicePrincipleName() {
        return servicePrincipleName;
    }

    public void setServicePrincipleName(String servicePrincipleName) {
        this.servicePrincipleName = servicePrincipleName;
    }

    public String getServicePrinciplePassword() {
        return servicePrinciplePassword;
    }

    public void setServicePrinciplePassword(String servicePrinciplePassword) {
        this.servicePrinciplePassword = servicePrinciplePassword;
    }

    public String getRealmName() {
        return realmName;
    }

    public void setRealmName(String realmName) {
        this.realmName = realmName;
    }

    public String getKdcAddress() {
        return kdcAddress;
    }

    public void setKdcAddress(String kdcAddress) {
        this.kdcAddress = kdcAddress;
    }

    public boolean isUseSubjectCredentialsOnly() {
        return useSubjectCredentialsOnly;
    }

    public void setUseSubjectCredentialsOnly(boolean useSubjectCredentialsOnly) {
        this.useSubjectCredentialsOnly = useSubjectCredentialsOnly;
    }
}
