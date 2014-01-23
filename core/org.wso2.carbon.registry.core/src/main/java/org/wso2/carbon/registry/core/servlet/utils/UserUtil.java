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

package org.wso2.carbon.registry.core.servlet.utils;

import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.AccessControlConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;

import javax.servlet.http.HttpServletRequest;

@Deprecated
public class UserUtil {

    public static boolean isPutAllowed(
            String userName, String resourcePath, HttpServletRequest request)
            throws RegistryException {

        boolean putAllowed = false;

        UserRegistry userRegistry = Utils.getSecureRegistry(request);
        UserRealm userRealm = userRegistry.getUserRealm();

        try {
            if (userRealm.getAuthorizationManager().isUserAuthorized(
                    userName, resourcePath, ActionConstants.PUT)) {
                putAllowed = true;
            }
        } catch (UserStoreException e) {

            String msg = "Could not the permission details for the user: " + userName +
                    " for the resource: " + resourcePath + ". Caused by: " + e.getMessage();
            throw new RegistryException(msg);
        }

        return putAllowed;
    }

    public static boolean isDeleteAllowed(
            String userName, String resourcePath, HttpServletRequest request)
            throws RegistryException {

        boolean putAllowed = false;

        UserRegistry userRegistry = Utils.getSecureRegistry(request);
        UserRealm userRealm = userRegistry.getUserRealm();

        try {
            if (userRealm.getAuthorizationManager().isUserAuthorized(
                    userName, resourcePath, ActionConstants.DELETE)) {
                putAllowed = true;
            }
        } catch (UserStoreException e) {

            String msg = "Could not the permission details for the user: " + userName +
                    " for the resource: " + resourcePath + ". Caused by: " + e.getMessage();
            throw new RegistryException(msg);
        }

        return putAllowed;
    }

    public static boolean isAuthorizeAllowed(
            String userName, String resourcePath, HttpServletRequest request)
            throws RegistryException {

        boolean putAllowed = false;

        UserRegistry userRegistry = Utils.getSecureRegistry(request);
        UserRealm userRealm = userRegistry.getUserRealm();

        try {
            if (userRealm.getAuthorizationManager().isUserAuthorized(
                    userName, resourcePath, AccessControlConstants.AUTHORIZE)) {
                putAllowed = true;
            }
        } catch (UserStoreException e) {

            String msg = "Could not the permission details for the user: " + userName +
                    " for the resource: " + resourcePath + ". Caused by: " + e.getMessage();
            throw new RegistryException(msg);
        }

        return putAllowed;
    }
}
