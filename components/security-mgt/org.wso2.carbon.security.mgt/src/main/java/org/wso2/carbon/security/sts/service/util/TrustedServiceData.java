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

package org.wso2.carbon.security.sts.service.util;

/**
 * Trusted service information
 */
public class TrustedServiceData {

    private String serviceAddress;
    private String certAlias;

    /**
     * Create a new data instance with the service address and cert alias
     *
     * @param serviceAddress Address of the trusted service
     * @param certAlias      Alias of the cert of the service
     */
    public TrustedServiceData(String serviceAddress, String certAlias) {
        this.serviceAddress = serviceAddress;
        this.certAlias = certAlias;
    }

    public String getServiceAddress() {
        return serviceAddress;
    }

    public String getCertAlias() {
        return certAlias;
    }


}