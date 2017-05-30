/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.registry.core.secure;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.common.AbstractAuthorizationManagerListener;
import org.wso2.carbon.user.core.listener.AuthorizationManagerListener;

import java.util.Arrays;
import java.util.List;

/**
 * This is a registry-based implementation of a listener that can be attached to the authorization
 * manager of a Carbon server. An authorize role listener is capable of mapping Carbon UI
 * permissions to registry resource permissions and thereby manage accessibility to various resource
 * paths based on the amount of permissions available to a given role.
 */
public class AuthorizeRoleListener extends AbstractAuthorizationManagerListener
        implements AuthorizationManagerListener {

    private int executionId = AuthorizationManagerListener.REGISTRY_AUTH_ROLE_LISTENER;
    private String path = null;
    private String permission = null;
    private String executeAction = null;
    private List<String> actions =
            Arrays.asList(ActionConstants.GET, ActionConstants.PUT, ActionConstants.DELETE);

    private ThreadLocal<Boolean> clearRoleActionOnAllResourcesStarted =
            new ThreadLocal<Boolean>() {
                protected Boolean initialValue() {
                    return false;
                }
            };

    private ThreadLocal<Boolean> authorizeRoleStarted =
            new ThreadLocal<Boolean>() {
                protected Boolean initialValue() {
                    return false;
                }
            };

    private static final Log log = LogFactory.getLog(AuthorizeRoleListener.class);

    /**
     * Creates an instance of an authorize role listener.
     *
     * @param executionId   the execution order identifier
     * @param path          the resource (or collection) path
     * @param permission    the permission. This should not be prefixed with the registry root.
     * @param executeAction the execute action required.
     * @param actions       the actions to which the role would be authorized.
     */
    public AuthorizeRoleListener(int executionId, String path, String permission,
                                 String executeAction, String[] actions) {
        this.executionId = executionId;
        this.path = path;
        this.permission = permission;
        this.executeAction = executeAction;
        if (actions != null) {
            this.actions = Arrays.asList(actions);
        }
    }

    /**
     * Method to get the execution order identifier.
     *
     * @return the execution order identifier.
     */
    public int getExecutionOrderId() {
        return executionId;
    }

    /**
     * Deletes the role's right to perform the action on all resources.
     *
     * @param roleName             the name of the role.
     * @param action               the action of the granted permission.
     * @param authorizationManager the authorization manager to use.
     *
     * @throws UserStoreException if an error occurs.
     */
    @Override
    public boolean clearRoleActionOnAllResources(String roleName, String action,
                                                 AuthorizationManager authorizationManager)
            throws UserStoreException {
        if (clearRoleActionOnAllResourcesStarted.get() != null
                && clearRoleActionOnAllResourcesStarted.get()) {
            return true;
        }
        clearRoleActionOnAllResourcesStarted.set(true);
        authorizationManager.clearRoleActionOnAllResources(roleName, action);
        clearRoleActionOnAllResourcesStarted.set(false);
        try {
            if (executeAction.equals(action)) {
                for (String actionName : actions) {
                    boolean isDenied = false;
                    String[] deniedRoles = authorizationManager.getDeniedRolesForResource(path, actionName);
                    for (String deniedRole : deniedRoles) {
                        if (deniedRole.equals(roleName)) {
                            isDenied = true;
                        }
                    }
                    if (!isDenied) {
                        authorizationManager.clearRoleAuthorization(roleName, path, actionName);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Unable to clear role authorization", e);
            log.debug("Caused by: ", e);
            return true;
        }
        return false;
    }

    /**
     * Grants authorization to a role to perform an action on a resource.
     *
     * @param roleName             the name of the role
     * @param resourceId           resource identification string
     * @param action               the action of the granted permission.
     * @param authorizationManager the authorization manager to use.
     *
     * @throws UserStoreException if an error occurs.
     */
    @Override
    public boolean authorizeRole(String roleName, String resourceId, String action,
                                 AuthorizationManager authorizationManager)
            throws UserStoreException {
        if (authorizeRoleStarted.get() != null && authorizeRoleStarted.get()) {
            return true;
        }
        authorizeRoleStarted.set(true);
        authorizationManager.authorizeRole(roleName, resourceId, action);
        try {
            if (permission.startsWith(RegistryUtils.getRelativePath(
                    RegistryContext.getBaseInstance(), resourceId))
                    && executeAction.equals(action)) {
                for (String actionName : actions) {
                    boolean isDenied = false;
                    String[] deniedRoles = authorizationManager.getDeniedRolesForResource(path, actionName);
                    for (String deniedRole : deniedRoles) {
                        if (deniedRole.equals(roleName)) {
                            isDenied = true;
                        }
                    }
                    if (!isDenied) {
                        authorizationManager.authorizeRole(roleName, path, actionName);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Unable to set role authorization", e);
            log.debug("Caused by: ", e);
            return true;
        } finally {
            authorizeRoleStarted.set(false);
        }
        return false;

    }
}
