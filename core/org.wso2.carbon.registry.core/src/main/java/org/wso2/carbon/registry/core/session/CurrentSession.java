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

package org.wso2.carbon.registry.core.session;

import org.wso2.carbon.user.core.UserRealm;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * A class to keep track of the session associated with a registry operation. This session values
 * are only valid inside it is call tree that root any operation go trough user registry.
 */
public final class CurrentSession {

    private CurrentSession() {
    }

    /**
     * Classes which are allowed to directly call secured methods in this class
     *
     * Note that we have to use the String form of the class name and not, for example,
     * RegistryResolver.class.getName() since this may unnecessarily cause NoClassDefFoundErrors
     *
     * This is not needed any more.
     * TODO remove this later

    private static final List<String> allowedClasses =
            Arrays.asList("org.wso2.carbon.registry.app.RegistryResolver",
                          "org.wso2.carbon.registry.app.ResourceServlet",
                          "org.wso2.carbon.registry.core.ResourceImpl",
                          "org.wso2.carbon.registry.core.jdbc.handlers.builtin.MountHandler",
                          "org.wso2.carbon.registry.core.session.UserRegistry",
                          "org.wso2.carbon.registry.core.utils.AuthorizationUtils",
                          "org.wso2.carbon.registry.core.utils.RegistryUtils",
                          "org.wso2.carbon.registry.core.jdbc.Repository",
                          CurrentSession.class.getName());
     */
    
    ////////////////////////////////////////////////////////
    // series of thread local stacks for user id, tenant id,
    // user realm and for chroot
    ////////////////////////////////////////////////////////

    private static ThreadLocal<Stack<String>> tUserIdStack = new ThreadLocal<Stack<String>>() {
        protected Stack<String> initialValue() {
            return new Stack<String>();
        }
    };

    private static ThreadLocal<Stack<UserRealm>> tUserRealmStack =
            new ThreadLocal<Stack<UserRealm>>() {
                protected Stack<UserRealm> initialValue() {
                    return new Stack<UserRealm>();
                }
            };

    private static ThreadLocal<Stack<Integer>> tTenantIdStack = new ThreadLocal<Stack<Integer>>() {
        protected Stack<Integer> initialValue() {
            return new Stack<Integer>();
        }
    };


    private static ThreadLocal<Stack<Integer>> tCallerTenantIdStack =
            new ThreadLocal<Stack<Integer>>() {
                protected Stack<Integer> initialValue() {
                    return new Stack<Integer>();
                }
            };


    private static ThreadLocal<Stack<String>> tChrootStack = new ThreadLocal<Stack<String>>() {
        protected Stack<String> initialValue() {
            return new Stack<String>();
        }
    };

    private static ThreadLocal<Stack<UserRegistry>> tUserRegistryStack =
            new ThreadLocal<Stack<UserRegistry>>() {
                protected Stack<UserRegistry> initialValue() {
                    return new Stack<UserRegistry>();
                }
            };

    private static ThreadLocal<Stack<Map<String, String>>> tLocalPathMapStack =
            new ThreadLocal<Stack<Map<String, String>>>() {
                protected Stack<Map<String, String>> initialValue() {
                    return new Stack<Map<String, String>>();
                }
            };

    private static ThreadLocal<Map<String, Object>> tAttributes =
            new ThreadLocal<Map<String, Object>>() {
                protected Map<String, Object> initialValue() {
                    return new HashMap<String, Object>();
                }
            };

    /**
     * Methods that abstract the set and get of the stack values of user realm.
     *
     * @return the current associated with the session.
     */
    public static String getUser() {
        Stack<String> userStack = tUserIdStack.get();
        if (userStack == null) {
            tUserIdStack.remove();
            userStack = tUserIdStack.get();
        }
        if (userStack.isEmpty()) {
            return null;
        }
        return userStack.peek();
    }

    /**
     * Inserting the user to the session stack.
     *
     * @param userID the id of th user
     */
    public static void setUser(String userID) {
        Stack<String> userStack = tUserIdStack.get();
        if (userStack == null) {
            tUserIdStack.remove();
            userStack = tUserIdStack.get();
        }
        userStack.push(userID);
    }

    /**
     * Remove the user from the session stack.
     */
    public static void removeUser() {
        Stack<String> userStack = tUserIdStack.get();
        if (userStack != null && !userStack.isEmpty()) {
            userStack.pop();
        }
    }

    /**
     * Methods that abstract the set and get of the stack values of user realm.
     *
     * @return the user realm associated with the session.
     */
    public static UserRealm getUserRealm() {
        Stack<UserRealm> userRealmStack = tUserRealmStack.get();
        if (userRealmStack == null) {
            tUserRealmStack.remove();
            userRealmStack = tUserRealmStack.get();
        }
        if (userRealmStack.isEmpty()) {
            return null;
        }
        return userRealmStack.peek();
    }

    /**
     * Inserting the user realm to the session stack.
     *
     * @param userRealm the user realm.
     */
    public static void setUserRealm(UserRealm userRealm) {
        Stack<UserRealm> userRealmStack = tUserRealmStack.get();
        if (userRealmStack == null) {
            tUserRealmStack.remove();
            userRealmStack = tUserRealmStack.get();
        }
        userRealmStack.push(userRealm);
    }

    /**
     * Remove the user realm from the session stack.
     */
    public static void removeUserRealm() {
        Stack<UserRealm> userRealmStack = tUserRealmStack.get();
        if (userRealmStack != null && !userRealmStack.isEmpty()) {
            userRealmStack.pop();
        }
    }

    /**
     * Methods that abstract the set and get of the stack values.
     *
     * @return the tenant id associated with the session.
     */
    public static int getTenantId() {
        Stack<Integer> tenantIdStack = tTenantIdStack.get();
        if (tenantIdStack == null) {
            tTenantIdStack.remove();
            tenantIdStack = tTenantIdStack.get();
        }
        if (tenantIdStack.isEmpty()) {
            return -1;
        }
        return tenantIdStack.peek();
    }

    /**
     * Inserting the tenant id to the session stack.
     *
     * @param tenantId the tenant id.
     */
    public static void setTenantId(int tenantId) {
        Stack<Integer> tenantIdStack = tTenantIdStack.get();
        if (tenantIdStack == null) {
            tTenantIdStack.remove();
            tenantIdStack = tTenantIdStack.get();
        }
        tenantIdStack.push(tenantId);
    }

    /**
     * Remove the tenant id from the session stack.
     */
    public static void removeTenantId() {
        Stack<Integer> tenantIdStack = tTenantIdStack.get();
        if (tenantIdStack != null && !tenantIdStack.isEmpty()) {
            tenantIdStack.pop();
        }
    }

    /**
     * Methods that abstract the set and get of the stack values.
     *
     * @return the callers tenant id associated with the session.
     */
    public static int getCallerTenantId() {
        Stack<Integer> tenantIdStack = tCallerTenantIdStack.get();
        if (tenantIdStack == null) {
            tCallerTenantIdStack.remove();
            tenantIdStack = tCallerTenantIdStack.get();
        }
        if (tenantIdStack.isEmpty()) {
            return -1;
        }
        return tenantIdStack.peek();
    }

    /**
     * Inserting the callers tenant id to the session stack.
     *
     * @param callerTenantId the callers tenant id
     */
    public static void setCallerTenantId(int callerTenantId) {
        Stack<Integer> tenantIdStack = tCallerTenantIdStack.get();
        if (tenantIdStack == null) {
            tCallerTenantIdStack.remove();
            tenantIdStack = tCallerTenantIdStack.get();
        }
        tenantIdStack.push(callerTenantId);
    }

    /**
     * Remove the callers tenant id from the session stack.
     */
    public static void removeCallerTenantId() {
        Stack<Integer> tenantIdStack = tCallerTenantIdStack.get();
        if (tenantIdStack != null && !tenantIdStack.isEmpty()) {
            tenantIdStack.pop();
        }
    }

    /**
     * Methods that abstract the set and get of the stack values.
     *
     * @return the chroot associated with the session.
     */
    public static String getChroot() {
        Stack<String> chrootStack = tChrootStack.get();
        if (chrootStack == null) {
            tChrootStack.remove();
            chrootStack = tChrootStack.get();
        }
        if (chrootStack.isEmpty()) {
            return null;
        }
        return chrootStack.peek();
    }

    /**
     * Inserting the chroot to the session stack.
     *
     * @param chroot the chroot.
     */
    public static void setChroot(String chroot) {
        Stack<String> chrootStack = tChrootStack.get();
        if (chrootStack == null) {
            tChrootStack.remove();
            chrootStack = tChrootStack.get();
        }
        chrootStack.push(chroot);
    }

    /**
     * Remove the chroot from the session stack.
     */
    public static void removeChroot() {
        Stack<String> chrootStack = tChrootStack.get();
        if (chrootStack != null && !chrootStack.isEmpty()) {
            chrootStack.pop();
        }
    }

    /**
     * Methods that abstract the set and get of the stack values.
     *
     * @return the user registry associated with the session.
     */
    public static UserRegistry getUserRegistry() {
        Stack<UserRegistry> userRegistryStack = tUserRegistryStack.get();
        if (userRegistryStack == null) {
            tUserRegistryStack.remove();
            userRegistryStack = tUserRegistryStack.get();
        }
        if (userRegistryStack.isEmpty()) {
            return null;
        }
        return userRegistryStack.peek();
    }

    /**
     * Inserting the user registry to the session stack.
     *
     * @param userRegistry the user registry.
     */
    public static void setUserRegistry(UserRegistry userRegistry) {
        Stack<UserRegistry> userRegistryStack = tUserRegistryStack.get();
        if (userRegistryStack == null) {
            tUserRegistryStack.remove();
            userRegistryStack = tUserRegistryStack.get();
        }
        userRegistryStack.push(userRegistry);
    }

    /**
     * Remove the user registry from the session stack.
     */
    public static void removeUserRegistry() {
        Stack<UserRegistry> userRegistryStack = tUserRegistryStack.get();
        if (userRegistryStack != null && !userRegistryStack.isEmpty()) {
            userRegistryStack.pop();
        }
    }

    /**
     * Methods that abstract the set and get of the stack values.
     *
     * @return the local path map associated with the session.
     */
    public static Map<String, String> getLocalPathMap() {
        Stack<Map<String, String>> localPathMapStack = tLocalPathMapStack.get();
        if (localPathMapStack == null) {
            tLocalPathMapStack.remove();
            localPathMapStack = tLocalPathMapStack.get();
        }
        if (localPathMapStack.isEmpty()) {
            return null;
        }
        return localPathMapStack.peek();
    }

    /**
     * Inserting the local path map to the session stack.
     *
     * @param localPathMap the local path map.
     */
    public static void setLocalPathMap(Map<String, String> localPathMap) {
        Stack<Map<String, String>> localPathMapStack = tLocalPathMapStack.get();
        if (localPathMapStack == null) {
            tLocalPathMapStack.remove();
            localPathMapStack = tLocalPathMapStack.get();
        }
        localPathMapStack.push(localPathMap);
    }

    /**
     * Remove the local path map from the session stack.
     */
    public static void removeLocalPathMap() {
        Stack<Map<String, String>> localPathMapStack = tLocalPathMapStack.get();
        if (localPathMapStack != null && !localPathMapStack.isEmpty()) {
            localPathMapStack.pop();
        }
    }


    /**
     * Methods to return the session attribute.
     *
     * @param key the parameter key
     *
     * @return the object of the attribute
     */
    public static Object getAttribute(String key) {
        return tAttributes.get().get(key);
    }

    /**
     * Method to set session attributes
     *
     * @param key the session attribute key
     * @param value the session attribute value
     */
    public static void setAttribute(String key, Object value) {
        tAttributes.get().put(key, value);
    }

    /**
     * Remove all the session attributes.
     */
    public static void removeAttributes() {
        tAttributes.set(new HashMap<String, Object>());
    }

    /**
     * Remove named session attribute.
     *
     * @param key key of attribute to be removed
     */
    public static void removeAttribute(String key) {
        tAttributes.get().remove(key);
    }
}
