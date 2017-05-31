/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.core.services.authentication.stats;

import java.util.Arrays;

public class LoginAttempts {
    private LoginAttempt[] loginAttempts;
    private int totalAttempts;
    private int totalFailedAttempts;

    public LoginAttempts(LoginAttempt[] loginAttempts, int totalAttempts, int totalFailedAttempts){
        this.loginAttempts = Arrays.copyOf(loginAttempts, loginAttempts.length);
        this.totalAttempts = totalAttempts;
        this.totalFailedAttempts = totalFailedAttempts;
    }

    public LoginAttempt[] getLoginAttempts() {
        return Arrays.copyOf(loginAttempts, loginAttempts.length);
    }

    public void setLoginAttempts(LoginAttempt[] loginAttempts) {
        this.loginAttempts = Arrays.copyOf(loginAttempts, loginAttempts.length);
    }

    public int getTotalAttempts() {
        return totalAttempts;
    }

    public void setTotalAttempts(int totalAttempts) {
        this.totalAttempts = totalAttempts;
    }

    public int getTotalFailedAttempts() {
        return totalFailedAttempts;
    }

    public void setTotalFailedAttempts(int totalFailedAttempts) {
        this.totalFailedAttempts = totalFailedAttempts;
    }
}
