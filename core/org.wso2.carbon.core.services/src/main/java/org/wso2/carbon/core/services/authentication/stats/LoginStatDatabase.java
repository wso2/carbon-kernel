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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This is ONLY a quick requirement fulfillment a demo.
 * TODO : Please persist properly.
 */
public class LoginStatDatabase {

    private static final Log log = LogFactory.getLog(LoginStatDatabase.class);

    /**
     * limit the harm can be done by this hack
     * and prevent resource exhaustion and there by DOS
     */
    public static final int MAX_COUNT = 50000;

    private static Queue<LoginAttempt> attempts = new ConcurrentLinkedQueue<LoginAttempt>();
    private static Map<String, UserAttempts> attemptsByUsers = new ConcurrentHashMap<String, UserAttempts>();
    public static volatile int count = 0;
    public static volatile int failedCount = 0;

    public static void recordLoginAttempt(LoginAttempt loginAttempt) {
        if (count > MAX_COUNT) {
            attempts.poll();
            count--;
        }
        try {
            attempts.add(loginAttempt);
            count++;
            if (!loginAttempt.isSuccessful()) {
                failedCount++;
            }
            addUserAttemp(loginAttempt);
        } catch (Exception e) {
            log.error("Error recording stats" + e.getMessage(), e);
        }
    }

    private static void addUserAttemp(LoginAttempt loginAttempt) {
        String userName = loginAttempt.getUserName();
        UserAttempts userAttempts;
        if(attemptsByUsers.containsKey(userName)){
            userAttempts = attemptsByUsers.get(userName);
            if(loginAttempt.isSuccessful()){
                userAttempts.setTotalLogins(userAttempts.getTotalLogins() +1);
            }else{
                userAttempts.setTotalLogins(userAttempts.getTotalLogins() +1);
                userAttempts.setFailedLogins(userAttempts.getFailedLogins() +1);
            }
        }else{
             if (loginAttempt.isSuccessful()) {
                userAttempts = new UserAttempts(userName, 1, 0);
                attemptsByUsers.put(userName, userAttempts);
            }else{
                userAttempts = new UserAttempts(userName, 1, 1);
                attemptsByUsers.put(userName, userAttempts);
            }
        }

    }

    public static LoginAttempt[] getAllAttempts() {
        return attempts.toArray(new LoginAttempt[0]);
    }

    public static int getCount() {
        return count;
    }

    public static int getFailedCount() {
        return failedCount;
    }

    public static UserAttempts[] getUserBasedLoginDetails(){
        return attemptsByUsers.values().toArray(new UserAttempts[attemptsByUsers.values().size()]);
    }
}
