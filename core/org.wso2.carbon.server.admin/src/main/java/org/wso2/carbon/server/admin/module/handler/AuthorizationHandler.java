/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.server.admin.module.handler;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * This Axis2 handler checks whether the caller is authorized to invoke the
 * admin service.
 */
public class AuthorizationHandler extends AbstractHandler {

    private static Log log = LogFactory.getLog(AuthorizationHandler.class.getClass());
    private static Log audit = CarbonConstants.AUDIT_LOG;

    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
        if (this.callToGeneralService(msgContext) || skipAuthentication(msgContext) ) {
            return InvocationResponse.CONTINUE;
        }
        if(CarbonUtils.isWorkerNode()){  // You are not allowed to invoke admin services on worker nodes
            HttpServletResponse response =
                    (HttpServletResponse) msgContext.getProperty(HTTPConstants.MC_HTTP_SERVLETRESPONSE);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            log.warn("Invoking admin services on worker node is forbidden...");
            return InvocationResponse.ABORT;
        }

        CarbonContext carbonCtx = CarbonContext.getThreadLocalCarbonContext();
        AxisService service = msgContext.getAxisService();
        AxisOperation operation = msgContext.getAxisOperation();
        String opName = operation.getName().getLocalPart();

        Parameter actionParam = operation.getParameter("AuthorizationAction");
        if (actionParam == null) {
            audit.warn("Unauthorized call by tenant " + carbonCtx.getTenantDomain() +
                       ",user " + carbonCtx.getUsername() + " to service:" + service.getName() +
                       ",operation:" + opName);
            throw new AxisFault("Unauthorized call!. AuthorizationAction has not been specified for service:" +
                                service.getName() + ", operation:" + opName);
        }

        String serviceName = service.getName();

        try {
            String action = ((String) actionParam.getValue()).trim();
            String authzResourceId = null;
            String authzAction = null;

            if (action.startsWith("/")) {
                authzResourceId = action;
                authzAction = "ui.execute";
            }
            doAuthorization(msgContext, authzResourceId, authzAction, serviceName, opName);
        } catch (AxisFault e) {
            throw e; // to preserve the previous context
        } catch (Throwable e) {
            String msg = "System failure.";
            log.error(msg + e.getMessage(), e);
            throw new AxisFault(msg, ServerConstants.AUTHORIZATION_FAULT_CODE);
        }

        return InvocationResponse.CONTINUE;
    }

    private void doAuthorization(MessageContext msgContext, String resourceId, String action,
                                 String serviceName, String opName) throws AxisFault {
        HttpServletRequest request = (HttpServletRequest) msgContext
                .getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
        String username = null;
        HttpSession httpSession = request.getSession(false);
        try {
            if (httpSession != null) {
                username = (String) httpSession.getAttribute(ServerConstants.USER_LOGGED_IN);
                UserRealm realm =
                        (UserRealm) PrivilegedCarbonContext.
                                getCurrentContext(httpSession).getUserRealm();

                if (realm == null) {
                    log.error("The realm is null for username: " + username + ".");
                    throw new AxisFault("System failed to authorize.",
                                        ServerConstants.AUTHORIZATION_FAULT_CODE);
                }

                resourceId = resourceId.trim();
                AuthorizationManager authMan = realm.getAuthorizationManager();
                if (!isAuthorized(authMan, username, resourceId, action)) {
                    log.error("Access Denied. Failed authorization attempt to access service '"
                              + serviceName + "' operation '" + opName + "' by '" + username + "'");
                    AxisFault afault = new AxisFault("Access Denied.");
                    afault.setFaultCode(ServerConstants.AUTHORIZATION_FAULT_CODE);
                    throw afault;
                }
            }
        } catch (AxisFault e) {
            throw e; // to preserve the previous context
        } catch (Exception e) {
            log.error("System failed to authorize." + e.getMessage(), e);
            throw new AxisFault("System failed to authorize.",
                                ServerConstants.AUTHORIZATION_FAULT_CODE);
        }
    }

    private boolean isAuthorized(AuthorizationManager authManager, String username,
                                 String authString, String action) throws UserStoreException {
        boolean isAuthzed = false;
        String[] resourceIds = authString.trim().split(",");
        for (String resourceId : resourceIds) {
            if (authManager.isUserAuthorized(username, resourceId, action)) {
                isAuthzed = true;
                break;
            }
        }
        return isAuthzed;
    }

    private boolean callToGeneralService(MessageContext msgContext) {
        boolean isGeneral = true;
        AxisService service = msgContext.getAxisService();
        Parameter param = service.getParameter("adminService");
        if (param != null && "true".equals(param.getValue())) {
            isGeneral = false;
        }
        return isGeneral;
    }
    
    
    private boolean skipAuthentication(MessageContext msgContext) {
        boolean skipAuth  = false;
        AxisService service = msgContext.getAxisService();
        Parameter param = service.getParameter("DoAuthentication");
        if (param != null && "false".equals(param.getValue())) {
        	skipAuth = true;
        }
        return skipAuth;
    }
}
