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

package org.wso2.carbon.auth.interceptor;

import com.google.common.collect.ArrayListMultimap;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.mss.HttpResponder;
import org.wso2.carbon.mss.Interceptor;
import org.wso2.carbon.mss.ServiceMethodInfo;
import org.wso2.carbon.security.annotation.RequirePermission;
import org.wso2.carbon.security.exception.CarbonSecurityException;
import org.wso2.carbon.security.jaas.callback.CarbonCallbackHandlerFactory;
import org.wso2.carbon.security.jaas.permission.CarbonPermission;

import java.security.AccessControlException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

/**
 *
 */
@Component(
        name = "org.wso2.carbon.auth.interceptor.MSAuthInterceptor",
        service = Interceptor.class,
        immediate = true
)
public class MSAuthInterceptor implements Interceptor {

    private static final Logger log = LoggerFactory.getLogger(MSAuthInterceptor.class);

    @Override
    public boolean preCall(HttpRequest httpRequest, HttpResponder httpResponder, ServiceMethodInfo serviceMethodInfo) {

        CallbackHandler callbackHandler;
        try {
            callbackHandler = CarbonCallbackHandlerFactory.getCallbackHandler(httpRequest);

        } catch (CarbonSecurityException e) {
            log.error("Error occurred while retrieving callback handler.", e);
            sendUnauthorized(httpResponder);
            return false;
        }

        LoginContext loginContext;
        try {
            loginContext = new LoginContext("CarbonSecurityConfig", callbackHandler);

        } catch (LoginException e) {
            log.error("Error occurred while initiating login context.", e);
            sendInternalServerError(httpResponder);
            return false;
        }

        try {
            loginContext.login();
            //TODO set LoginContext to CarbonContext

        } catch (LoginException e) {
            sendUnauthorized(httpResponder);
            return false;
        }

        // Authorization - File Based

        RequirePermission requirePermission = serviceMethodInfo.getMethod().getAnnotation(RequirePermission.class);

        if (requirePermission != null) {
            CarbonPermission carbonPermission = new CarbonPermission(requirePermission.permission(),
                                                                     getActionsString(requirePermission.actions()));
            if (!this.isAuthorized(loginContext.getSubject(), carbonPermission)) {
                sendUnauthorized(httpResponder);
            }
        } else {
            //TODO
            sendUnauthorized(httpResponder);
            return false;
        }

        return true;
    }

    @Override
    public void postCall(HttpRequest httpRequest, HttpResponseStatus httpResponseStatus,
                         ServiceMethodInfo serviceMethodInfo) {

    }

    private boolean isAuthorized(Subject subject, final CarbonPermission permission) {

        final SecurityManager securityManager;
        if (System.getSecurityManager() == null) {
            securityManager = new SecurityManager();
        } else {
            securityManager = System.getSecurityManager();
        }

        try {
            Subject.doAsPrivileged(subject, (PrivilegedExceptionAction) () -> {
                securityManager.checkPermission(permission);
                return null;
            }, null);
            return true;
        } catch (AccessControlException ace) {
            return false;
        } catch (PrivilegedActionException pae) {
            return false;
        }
    }

    private void sendUnauthorized(HttpResponder httpResponder) {
        httpResponder.sendStatus(HttpResponseStatus.UNAUTHORIZED, ArrayListMultimap.create());
    }

    private void sendInternalServerError(HttpResponder httpResponder) {
        httpResponder.sendStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR, ArrayListMultimap.create());
    }

    private String getActionsString(String[] actions) {

        if (actions.length > 0) {
            StringBuilder actionsStr = new StringBuilder();
            for (String action : actions) {
                actionsStr.append(action).append("',");
            }
            actionsStr.deleteCharAt(actionsStr.length() - 1);
            return actionsStr.toString();
        } else {
            throw new IllegalArgumentException("Actions cannot be empty");
        }
    }

}
