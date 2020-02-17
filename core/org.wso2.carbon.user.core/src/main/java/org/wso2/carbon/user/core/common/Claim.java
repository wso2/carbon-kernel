/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.user.core.common;

public class Claim {

    private String claimUrl;
    private String claimValue;

    public Claim(String claimUrl, String value) {

        this.claimUrl = claimUrl;
        this.claimValue = value;
    }

    public String getClaimUrl() {

        return claimUrl;
    }

    public void setClaimUrl(String claimUrl) {

        this.claimUrl = claimUrl;
    }

    public String getClaimValue() {

        return claimValue;
    }

    public void setClaimValue(String claimValue) {

        this.claimValue = claimValue;
    }
}
