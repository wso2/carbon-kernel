/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.security.util;

import org.wso2.carbon.security.jaas.CarbonPermission;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * In memory user store manager.
 */
public class InMemoryUserStoreManager {

    private static Map<String, char[]> userStore = new HashMap<>();

    private static Map<String, CarbonPermission[]> permissionStore = new HashMap<>();

    private static InMemoryUserStoreManager instance = new InMemoryUserStoreManager();


    private InMemoryUserStoreManager() {

        userStore.put("admin", new char[]{'a', 'd', 'm', 'i', 'n'});

        permissionStore.put("admin", new CarbonPermission[]{
                new CarbonPermission("org.wso2.carbon.mss.stockquote.StockQuoteService.getQuote", "GET")});
    }

    public static InMemoryUserStoreManager getInstance() {
        return instance;
    }

    /**
     * Authenticates a user, given the username and password.
     *
     * @param username String username
     * @param password user password in char array
     * @return true if the authentication is success, else false.
     */
    public boolean authenticate(String username, char[] password) {

        if (username != null && !username.isEmpty()) {

            char[] userPassword = userStore.get(username);
            if (userPassword == null) {
                return false;
            }

            return Arrays.equals(password, userPassword);
        }

        return false;
    }

    /**
     * Checks whether a given principal is authorized against a provided permission.
     *
     * @param principalName      Name of the principal.
     * @param requiredPermission Permission which the principal should be checked against.
     * @return
     */
    public boolean authorizePrincipal(String principalName, CarbonPermission requiredPermission) {

        if (principalName != null && !principalName.isEmpty()) {

            CarbonPermission[] carbonPermissions = permissionStore.get(principalName);
            if (carbonPermissions == null) {
                return false;
            }

            for (CarbonPermission carbonPermission : carbonPermissions) {
                if (carbonPermission.getName().equals(requiredPermission.getName())) {
                    return true;
                    //TODO actions are ignored temporally
                }
            }
        }

        return false;
    }

}
