/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.core.services.authentication;

/**
 * If authentication failure occurred this exception will be thrown. Also the caller needs to specify a reason
 * for authentication failure.
 */
public class AuthenticationFailureException extends Exception {

    public enum AuthenticationFailureReason {
        INVALID_USER_NAME,
        INVALID_PASSWORD,
        SYSTEM_ERROR,
        RESERVED_TENANT_DOMAIN,
    }

    private AuthenticationFailureReason authenticationFailureReason;

    private String parameter;

    public AuthenticationFailureException(AuthenticationFailureReason reason) {
        this.authenticationFailureReason = reason;
    }

    public AuthenticationFailureException(AuthenticationFailureReason reason, String parameter) {
        this.authenticationFailureReason = reason;
        this.parameter = parameter;
    }

    public AuthenticationFailureReason getAuthenticationFailureReason() {
        return authenticationFailureReason;
    }

    public String getParameter() {
        return parameter;
    }

    public String getMessage() {

        if (authenticationFailureReason == AuthenticationFailureReason.INVALID_USER_NAME) {
            return "Authentication failed - Invalid user name provided." + parameter;
        }

        if (authenticationFailureReason == AuthenticationFailureReason.INVALID_PASSWORD) {
            return "Authentication failed - Invalid credentials provided.";
        }

        if (authenticationFailureReason == AuthenticationFailureReason.SYSTEM_ERROR) {
            return "Authentication failed - System error occurred. Please check server logs for more details.";
        }

        if (authenticationFailureReason == AuthenticationFailureReason.RESERVED_TENANT_DOMAIN) {
            return "Authentication failed - System error occurred. Tenant domain name is reserved.";
        }

        throw new RuntimeException("Un-identified authentication failure reason");

    }

}
