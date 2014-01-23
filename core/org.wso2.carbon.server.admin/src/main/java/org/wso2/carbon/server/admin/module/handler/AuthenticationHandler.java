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
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.services.authentication.*;
import org.wso2.carbon.server.admin.auth.AuthenticatorServerRegistry;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 */
public class AuthenticationHandler extends AbstractHandler {
    private static final Log log = LogFactory.getLog(AuthenticationHandler.class);
    private static final Log audit = CarbonConstants.AUDIT_LOG;

    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {

        // do not authenticate it is a call  to a generic service
        if (callToGeneralService(msgContext) || skipAuthentication(msgContext)) {
            return InvocationResponse.CONTINUE;
        }

        //HTTP_SC code will be set only by Synapse PTT and NHTTP transport
        //Unlike CommonsHTTPTransport those transports pass HTTP 202 messages to axis2, which will cause
        //authentication failure in dual channel admin service calls ( ex: add new repository in feature manager)
        //Following check is added to ignore these 202 responses.
        Object http_sc = msgContext.getProperty("HTTP_SC");
        if (http_sc != null && http_sc instanceof Integer &&
                http_sc.equals(202) && msgContext.getTransportIn().getReceiver().getClass().getName().contains("org.apache.synapse.transport")) {
            return InvocationResponse.ABORT;
        }


        authenticate(msgContext, (String) msgContext.getProperty(MessageContext.REMOTE_ADDR));

        if (!AbstractAuthenticator.continueProcessing(msgContext)) {
            return InvocationResponse.ABORT;
        }

        // Setting the tenant domain in the MessageContext since it is transport
        // independent
        // unlike in the case of HttpSession. Used by multitenant service
        // hosting
        HttpServletRequest request =
            (HttpServletRequest) msgContext.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
        HttpSession session;
        if (request != null && (session = request.getSession(false)) != null) {
            String domain = (String) session.getAttribute(MultitenantConstants.TENANT_DOMAIN);
            if (domain != null) {
                msgContext.setProperty(MultitenantConstants.TENANT_DOMAIN, domain);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(domain);
            }
            String username = (String) session.getAttribute(ServerConstants.USER_LOGGED_IN);
            if(username != null){
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(username);
            }
        }

        return InvocationResponse.CONTINUE;
    }

    protected void authenticate(MessageContext msgContext, String remoteIP) throws AxisFault {
        try {
            if (!isAuthenticated(msgContext, remoteIP) && AbstractAuthenticator.continueProcessing(msgContext)) {
                    throw new AxisFault("Access Denied. Please login first.",
                            ServerConstants.AUTHENTICATION_FAULT_CODE);
            }
        } catch (AxisFault e) {
            throw e;
        } catch (AuthenticationFailureException e) {

            if (AbstractAuthenticator.continueProcessing(msgContext)) {

                if (e.getAuthenticationFailureReason()
                        == AuthenticationFailureException.AuthenticationFailureReason.INVALID_USER_NAME ||
                        e.getAuthenticationFailureReason()
                                == AuthenticationFailureException.AuthenticationFailureReason.INVALID_PASSWORD) {
                    AxisFault axisFault = new AxisFault("Access Denied. " + e.getMessage(),
                            ServerConstants.AUTHENTICATION_FAULT_CODE);
                    axisFault.setFaultType(Constants.APPLICATION_FAULT);
                    throw axisFault;

                } else {
                    throw new AxisFault("Access Denied. " + e.getMessage(),
                            ServerConstants.AUTHENTICATION_FAILURE_CODE);
                }
            }

        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            throw new AxisFault("Authentication failure", ServerConstants.AUTHENTICATION_FAULT_CODE);
        }
    }

    protected boolean isRestrictedOperation(MessageContext msgContext) {

        AxisOperation operation = msgContext.getAxisOperation();

        Parameter authenticationParameter = operation.getParameter("DoAuthentication");
        return !(authenticationParameter != null && "false".equals(authenticationParameter.getValue()));

    }

    private String getServiceName(MessageContext msgContext) {
        AxisService service = msgContext.getAxisService();
        return service.getName();
    }

    private boolean isAuthenticated(MessageContext msgContext,
                                    String remoteIP) throws AuthenticationFailureException {

        // If operation is unrestricted we dont need to do authentication
        if (!isRestrictedOperation(msgContext)) {
            return true;
        }

        BackendAuthenticator authenticator =
                AuthenticatorServerRegistry.getCarbonAuthenticator(msgContext);

        //We must always get an authenticator.
        // If none matching found the default authenticator will be used
        if (authenticator == null) {
            String message = "System error : 0 active authenticators registered in the system. The system should have" +
                    " at least 1 active authenticator service registered.";
            throw new RuntimeException(message);
        }

        boolean isAuthenticated;

        // TODO we need to cleanup following code in a future release ..
        if (authenticator instanceof ServerAuthenticator) {

            ServerAuthenticator serverAuthenticator = (ServerAuthenticator) authenticator;

            isAuthenticated = serverAuthenticator.isAuthenticated(msgContext);

            if (!isAuthenticated) {
                try {
                    serverAuthenticator.authenticate(msgContext);
                    return true;
                } catch (AuthenticationFailureException e) {
                    SimpleDateFormat date = new SimpleDateFormat("'['yyyy-MM-dd HH:mm:ss,SSSS']'");
                    invalidateSession(msgContext);
                    String serviceName = getServiceName(msgContext);
                    String msg = "Illegal access attempt at " + date.format(new Date()) + " from IP address "
                                 + remoteIP + " while trying to authenticate access to service " + serviceName;
                    log.warn(msg);
                    audit.error(msg);
                    throw e;
                }
            }
        } else {
            CarbonServerAuthenticator carbonServerAuthenticator = (CarbonServerAuthenticator) authenticator;

            isAuthenticated = carbonServerAuthenticator.isAuthenticated(msgContext);

            if (!isAuthenticated) {
                isAuthenticated = carbonServerAuthenticator.authenticateWithRememberMe(msgContext);

                SimpleDateFormat date = new SimpleDateFormat("'['yyyy-MM-dd HH:mm:ss,SSSS']'");
                invalidateSession(msgContext);

                if (AbstractAuthenticator.continueProcessing(msgContext)) {
                    String serviceName = getServiceName(msgContext);
                    String msg = "Illegal access attempt at " + date.format(new Date()) + " from IP address "
                               + remoteIP + " : Service is " + serviceName;
                    log.warn(msg);
                    audit.warn(msg);
                }
            }
        }

        return isAuthenticated;
    }

    private void invalidateSession(MessageContext msgContext) {

        // First check for HTTP request
        HttpServletRequest request = (HttpServletRequest) msgContext
                .getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);

        if (request == null) {
            HttpSession session = request.getSession();
            if (session != null) {
                try {
                    session.invalidate();
                } catch (IllegalStateException e) {
                    log.debug("Unable to invalidate session ", e);
                }
            }
        }
        // This could be nhttp transport
        // We do not need to anything to clear sessions
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
