/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.user.core.common;

import java.io.Serializable;
import java.util.Optional;

/**
 * Represents an authentication result after a authentication attempt.
 *
 * @since 4.6.0
 */
public class AuthenticationResult implements Serializable {

    private static final long serialVersionUID = -4882553126120287712L;

    private AuthenticationStatus authenticationStatus;
    private User authenticatedUser;
    private String authenticatedSubjectIdentifier;
    private FailureReason failureReason;

    public AuthenticationResult(AuthenticationStatus authenticationStatus) {

        this.authenticationStatus = authenticationStatus;
    }

    public Optional<User> getAuthenticatedUser() {

        return Optional.of(authenticatedUser);
    }

    public void setAuthenticatedUser(User authenticatedUser) {

        this.authenticatedUser = authenticatedUser;
    }

    public String getAuthenticatedSubjectIdentifier() {

        return authenticatedSubjectIdentifier;
    }

    public void setAuthenticatedSubjectIdentifier(String authenticatedSubjectIdentifier) {

        this.authenticatedSubjectIdentifier = authenticatedSubjectIdentifier;
    }

    public AuthenticationStatus getAuthenticationStatus() {

        return authenticationStatus;
    }

    public void setAuthenticationStatus(AuthenticationStatus authenticationStatus) {

        this.authenticationStatus = authenticationStatus;
    }

    public Optional<FailureReason> getFailureReason() {

        return Optional.of(failureReason);
    }

    public void setFailureReason(FailureReason failureReason) {

        this.failureReason = failureReason;
    }

    public enum AuthenticationStatus {
        SUCCESS, FAIL
    }
}
