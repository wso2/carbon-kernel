/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.user.core.common;

import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.utils.xml.StringUtils;

import java.io.Serializable;

/**
 * Represents an login identifier details which uses for user login.
 *
 * @since 4.6.0
 */
public class LoginIdentifier implements Serializable {

    private static final long serialVersionUID = -2865352084324385186L;
    private String loginKey;
    private String loginValue;
    private String profileName;
    private LoginIdentifierType loginIdentifierType;

    public LoginIdentifier(String loginKey, String loginValue, String profileName,
            LoginIdentifierType loginIdentifierType) {

        this.loginKey = loginKey;
        this.loginValue = loginValue;
        if (StringUtils.isEmpty(profileName)) {
            this.profileName = UserCoreConstants.DEFAULT_PROFILE;
        } else {
            this.profileName = profileName;
        }
        this.loginIdentifierType = loginIdentifierType;
    }

    public String getLoginKey() {

        return loginKey;
    }

    public void setLoginKey(String loginKey) {

        this.loginKey = loginKey;
    }

    public String getLoginValue() {

        return loginValue;
    }

    public void setLoginValue(String loginValue) {

        this.loginValue = loginValue;
    }

    public String getProfileName() {

        return profileName;
    }

    public void setProfileName(String profileName) {

        if (StringUtils.isEmpty(profileName)) {
            this.profileName = UserCoreConstants.DEFAULT_PROFILE;
        } else {
            this.profileName = profileName;
        }
    }

    public LoginIdentifierType getLoginIdentifierType() {
        return loginIdentifierType;
    }

    public void setLoginIdentifierType(LoginIdentifierType loginIdentifierType) {
        this.loginIdentifierType = loginIdentifierType;
    }

    public enum LoginIdentifierType {
        CLAIM_URI, ATTRIBUTE
    }
}
