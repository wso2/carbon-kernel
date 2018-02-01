/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreException;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/**
 * Abstract class for Java security manager secured classes.
 * @since 4.4.5
 */
public abstract class AbstractSecuredEntityManager {

    private static Log log = LogFactory.getLog(AbstractSecuredEntityManager.class);

    protected static final ThreadLocal<Boolean> isSecureCall = ThreadLocal.withInitial(() -> Boolean.FALSE);

    protected RealmConfiguration realmConfig = null;
    protected UserRolesCache userRolesCache = null;
    protected String cacheIdentifier;

    protected boolean userRolesCacheEnabled = true;

    /**
     * This method is used by the APIs' in the AbstractSecuredEntityManager extended classes to make compatible with
     * Java Security Manager.
     */
    protected Object callSecure(final String methodName, final Object[] objects, final Class[] argTypes)
            throws UserStoreException {

        isSecureCall.set(Boolean.TRUE);
        final Method method;

        try {
            Class clazz = this.getClass();
            method = clazz.getDeclaredMethod(methodName, argTypes);
        } catch (NoSuchMethodException e) {
            log.error("Error occurred when calling method " + methodName, e);
            throw new UserStoreException(e);
        }

        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                @Override
                public Object run() throws Exception {
                    return method.invoke(this, objects);
                }
            });
        } catch (PrivilegedActionException e) {
            if (e.getCause() != null && e.getCause().getCause() != null && e.getCause().getCause() instanceof
                    UserStoreException) {
                // Actual UserStoreException get wrapped with two exceptions.
                throw new UserStoreException(e.getCause().getCause().getMessage(), e);

            } else {
                String msg = "Error occurred while accessing Java Security Manager Privilege Block";
                log.error(msg);
                throw new UserStoreException(msg, e);
            }
        } finally {
            isSecureCall.set(Boolean.FALSE);
        }
    }

    /**
     * Initialize the user role cache for this instance.
     */
    protected void initUserRolesCache() {

        String userRolesCacheEnabledString = (realmConfig
                .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_ROLES_CACHE_ENABLED));

        String userCoreCacheIdentifier = realmConfig
                .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_USER_CORE_CACHE_IDENTIFIER);

        if (StringUtils.isNotEmpty(userCoreCacheIdentifier)) {
            cacheIdentifier = userCoreCacheIdentifier;
        } else {
            cacheIdentifier = UserCoreConstants.DEFAULT_CACHE_IDENTIFIER;
        }

        if (userRolesCacheEnabledString != null && !userRolesCacheEnabledString.equals("")) {
            userRolesCacheEnabled = Boolean.parseBoolean(userRolesCacheEnabledString);
            if (log.isDebugEnabled()) {
                log.debug("User Roles Cache is configured to:" + userRolesCacheEnabledString);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.info("User Roles Cache is not configured. Default value: " + userRolesCacheEnabled + " is taken.");
            }
        }

        if (userRolesCacheEnabled) {
            int timeOut = UserCoreConstants.USER_ROLE_CACHE_DEFAULT_TIME_OUT;
            String timeOutString = realmConfig.
                    getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_USER_ROLE_CACHE_TIME_OUT);
            if (timeOutString != null) {
                timeOut = Integer.parseInt(timeOutString);
            }
            userRolesCache = UserRolesCache.getInstance();
            userRolesCache.setTimeOut(timeOut);
        }
    }
}
