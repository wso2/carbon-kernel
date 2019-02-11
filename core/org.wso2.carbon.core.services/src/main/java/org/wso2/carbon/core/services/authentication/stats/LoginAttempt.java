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
package org.wso2.carbon.core.services.authentication.stats;

import java.util.Date;


public class LoginAttempt {
    
    private String userName;
    private String remoteAddress;
    private Date timestamp;
    private boolean isSuccessful;
    private String failureReason;
    int tenantId;

    public LoginAttempt(String userName, int tenantId, String remoteAddress, Date timestamp,
            boolean isSuccessful, String failureReason) {
        this.userName = userName;
        this.remoteAddress = remoteAddress;
        this.timestamp = timestamp;
        this.isSuccessful = isSuccessful;
        this.failureReason = failureReason;
        this.tenantId = tenantId;
    }
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getRemoteAddress() {
        return remoteAddress;
    }
    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }
    public Date getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    public boolean isSuccessful() {
        return isSuccessful;
    }
    public void setSuccessful(boolean isSuccessful) {
        this.isSuccessful = isSuccessful;
    }
    public String getFailureReason() {
        return failureReason;
    }
    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }    
}
