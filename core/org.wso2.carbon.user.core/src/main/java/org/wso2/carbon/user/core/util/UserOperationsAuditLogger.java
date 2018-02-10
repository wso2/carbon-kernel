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

package org.wso2.carbon.user.core.util;

import org.apache.commons.logging.Log;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;

/**
 * This is the logger class which logs user operations to an audit log.
 */
public class UserOperationsAuditLogger {

    public static final String USER_OPERATION_ADD_USER = "Add User";
    public static final String USER_OPERATION_DELETE_USER = "Delete User";
    public static final String USER_OPERATION_CHANGE_PASSWORD_BY_USER = "Change Password by User";
    public static final String USER_OPERATION_CHANGE_PASSWORD_BY_ADMINISTRATOR = "Change Password by Administrator";
    public static final String USER_OPERATION_DELETE_ROLE = "Delete Role";
    public static final String USER_OPERATION_ADD_ROLE = "Add Role";
    public static final String USER_OPERATION_UPDATE_ROLE_NAME = "Update Role Name";
    public static final String USER_OPERATION_UPDATE_USERS_OF_ROLE = "Update Users of Role";
    public static final String USER_OPERATION_UPDATE_ROLES_OF_USER = "Update Roles of User";
    public static final String USER_OPERATION_UPDATE_PERMISSIONS_OF_ROLE = "Update Permissions of Role";

    private static Log AUDIT_LOG = CarbonConstants.AUDIT_LOG;
    private static String AUDIT_MESSAGE = "Initiator : %s | Action : %s | Target : %s | Data : { %s } | Result : %s ";

    /**
     * Logs the given action/operation and context data and result of the operation/action.
     * The initiator of the action would be logged in user.
     *
     * @param action The action/operation which was performed against a user or a role.
     * @param target The user or the role which was performed the action upon.
     * @param data   The context data of the operation. e.g. If it is a rename then data would be old name and new name.
     * @param result The result of the performed operation. This can be either ActionResult.SUCCESS or
     *               ActionResult.FAILURE
     */
    public static void log(String action, String target, String data, ActionResult result) {

        log(getLoggedInUser(), action, target, data, result);
    }

    /**
     * Logs the given action/operation, initiator of the action/operation, context data
     * and result of the operation/action.
     *
     * @param initiator The name of the user who performed the action.
     * @param action    The action/operation which was performed against a user or a role.
     * @param target    The user or the role which was performed the action upon.
     * @param data      The context data of the operation. e.g. If it is a rename then data would be old name and new name.
     * @param result    The result of the performed operation. This can be either ActionResult.SUCCESS or
     *                  ActionResult.FAILURE
     */
    public static void log(String initiator, String action, String target, String data, ActionResult result) {

        if (AUDIT_LOG.isInfoEnabled()) {
            AUDIT_LOG.info(String.format(AUDIT_MESSAGE, initiator, action, target, data, result.displayName));
        }
    }

    private static String getLoggedInUser() {

        String user = CarbonContext.getThreadLocalCarbonContext().getUsername();
        if (user != null) {
            user = user + "@" + CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        } else {
            user = CarbonConstants.REGISTRY_SYSTEM_USERNAME;
        }
        return user;
    }

    /**
     * The enumeration which represent the result of an action/operation.
     */
    public enum ActionResult {

        SUCCESS("Success"),
        FAILURE("Failure");

        private String displayName;

        ActionResult(String displayName) {

            this.displayName = displayName;
        }
    }
}
