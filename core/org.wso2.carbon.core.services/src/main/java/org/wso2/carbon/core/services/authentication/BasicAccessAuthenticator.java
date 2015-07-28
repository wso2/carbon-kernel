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

package org.wso2.carbon.core.services.authentication;

import org.apache.axiom.util.base64.Base64Utils;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.RequestResponseTransport;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.core.services.internal.CarbonServicesServiceComponent;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.servlet.http.HttpServletResponse;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is the default authenticator used in Carbon framework.
 */
public class BasicAccessAuthenticator extends AbstractAuthenticator {

    private static final String AUTHENTICATOR_NAME = "BasicAccessAuthenticator";

    private static final String CARBON_BASIC_AUTH_PASSWORD = "CARBON_BASIC_AUTH_PASSWORD";

    private static final Log log = LogFactory.getLog(BasicAccessAuthenticator.class);

    /**
     * @inheritDoc
     */
    @Override
    protected String getUserNameFromRequest(MessageContext msgContext) {

        String authorizationHeader = AuthenticationUtil.getHeader("Authorization", msgContext);

        if (authorizationHeader == null) {
            log.debug("Authorization header missing !!");
            // Send 401 error
            createUnAuthorizedResponse(msgContext);
        } else {
            String[] userNamePassword = decodeAuthorizationHeader(authorizationHeader);

            if (userNamePassword.length != 2) {
                log.debug("Invalid authorization header received");
                // Send 401 error
                createUnAuthorizedResponse(msgContext);
            } else {
                if (userNamePassword[0] == null || userNamePassword[0].isEmpty()) {
                    // Send 401 error
                    createUnAuthorizedResponse(msgContext);

                } else {

                    // If password is missing, send a 401 error
                    if (userNamePassword[1] == null || userNamePassword[1].isEmpty()) {
                        // Send 401 error
                        createUnAuthorizedResponse(msgContext);
                    } else {
                        msgContext.setProperty(CARBON_BASIC_AUTH_PASSWORD, userNamePassword[1]);
                        return userNamePassword[0];
                    }

                }
            }

        }

        return null;
    }

    /**
     * This method will create a 401 unauthorized response to be sent.We also need
     * to set ServerAuthenticator.CONTINUE_PROCESSING to "false" as we need to make sure that message will not passed
     * to other handlers.
     * @param msgContext The request message context
     */
    private void createUnAuthorizedResponse(MessageContext msgContext) {

        String serverName = ServerConfiguration.getInstance().getFirstProperty("Name");

        HttpServletResponse response = (HttpServletResponse) msgContext
                .getProperty(HTTPConstants.MC_HTTP_SERVLETRESPONSE);

        if (response != null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.addHeader("Server", "WSO2 Server");
            response.addDateHeader("Date", Calendar.getInstance().getTimeInMillis());
            response.addHeader("WWW-Authenticate", "Basic realm=\""+ serverName + "\"");
            response.setContentType("text/html");
        } else {
            // if not servlet transport assume it to be nhttp transport
            msgContext.setProperty("NIO-ACK-Requested", "true");
            msgContext.setProperty("HTTP_SC", HttpServletResponse.SC_UNAUTHORIZED);
            Map<String, String> responseHeaders = new HashMap<String, String>();
            responseHeaders.put("WWW-Authenticate",
                    "Basic realm=\"" + serverName + "\"");
            responseHeaders.put("Server", "WSO2 Server");
            responseHeaders.put("Date", Long.toString(Calendar.getInstance().getTimeInMillis()));
            msgContext.setProperty(MessageContext.TRANSPORT_HEADERS, responseHeaders);
        }


        msgContext.setProperty(ServerAuthenticator.CONTINUE_PROCESSING, "false");

        RequestResponseTransport transportControl = (RequestResponseTransport)
                msgContext.getProperty(RequestResponseTransport.TRANSPORT_CONTROL);

        if (transportControl != null) {
            transportControl.setResponseWritten(true);
        }

    }

    private String[] decodeAuthorizationHeader(String authorizationHeader) {

        String[] splitValues = authorizationHeader.trim().split(" ");
        byte[] decodedBytes = Base64Utils.decode(splitValues[1].trim());
        if (decodedBytes != null) {
            String userNamePassword = new String(decodedBytes);
            String username = userNamePassword.substring(0, userNamePassword.indexOf(":"));
            String password = userNamePassword.substring(userNamePassword.indexOf(":") + 1);
            if(username != null && password != null &&
                    !"".equals(username.trim()) && !"".equals(password.trim())) {
                return new String[]{username, password};
            }
        }
        log.debug("Error decoding authorization header. Could not retrieve user name and password.");
        return new String[]{null, null};

    }

    /**
     * @inheritDoc
     */
    @Override
    protected void doAuthentication(String userNameInRequest, int tenantId, MessageContext msgContext)
            throws AuthenticationFailureException {

        RegistryService registryService;
        UserRealm realm;
        try {
            registryService = CarbonServicesServiceComponent.getRegistryService();
            realm = AuthenticatorHelper.getUserRealm(tenantId, getRealmService(), registryService);

        } catch (Exception e) {
            log.error("Error retrieving user realm for authentication. Tenant id " +
                     tenantId + " user name " + userNameInRequest, e);
            throw new AuthenticationFailureException
                    (AuthenticationFailureException.AuthenticationFailureReason.SYSTEM_ERROR, userNameInRequest);
        }

        String userName = MultitenantUtils.getTenantAwareUsername(userNameInRequest);
        String password = (String) msgContext.getProperty(CARBON_BASIC_AUTH_PASSWORD);

        if (password == null) {
            throw new AuthenticationFailureException
                    (AuthenticationFailureException.AuthenticationFailureReason.INVALID_PASSWORD, userNameInRequest);
        }

        try {
            boolean isAuthenticated = realm.getUserStoreManager().authenticate(userName, password);

            if (!isAuthenticated) {
                if (log.isDebugEnabled()) {
                    log.debug("Failed authentication for user " + userNameInRequest);
                }

                throw new AuthenticationFailureException
                        (AuthenticationFailureException.AuthenticationFailureReason.INVALID_PASSWORD, userNameInRequest);
            }

        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            log.error("Unable to get UserStoreManager for authentication. User - " + userNameInRequest, e);
            throw new AuthenticationFailureException
                    (AuthenticationFailureException.AuthenticationFailureReason.SYSTEM_ERROR, userNameInRequest);

        }

    }

    /**
     * @inheritDoc
     */
    @Override
    protected RealmService getRealmService() throws Exception {

        return CarbonServicesServiceComponent.getRealmService();
    }

    /**
     * @inheritDoc
     */
    @Override
    protected BundleContext getBundleContext() throws Exception {

        return CarbonServicesServiceComponent.getBundleContext();
    }

    /**
     * @inheritDoc
     */
    public boolean canHandle(MessageContext msgContext) {

        /**
         * This authenticator is capable of handling basic access authentication requests.
         * So the criteria for checking whether it can handle a request is as follows,
         * 1. If request does not contain an Authorization header this is capable of handling the request - Cos
         * a client might request access to a resource without giving credentials. In that case server need to respond
         * with a 401 status code (Un-authorized)
         * 2. If there is an Authorization header and if it has "Basic" tag
         */

        String authorizationHeader = AuthenticationUtil.getHeader("Authorization", msgContext);

        if (authorizationHeader == null) {
            return true;
        } else {
            String authType = getAuthType(authorizationHeader);
            if (authType != null && authType.equalsIgnoreCase("Basic")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Gets the authentication type in authorization header.
     * @param authorizationHeader The authorization header - Authorization: Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ=="
     * @return The authentication type mentioned in authorization header.
     */
    protected String getAuthType(String authorizationHeader) {

        String[] splitValues = authorizationHeader.trim().split(" ");

        if (splitValues == null || splitValues.length == 0) {
            return null;
        }

        return splitValues[0].trim();
    }

    /**
     * @inheritDoc
     */
    public String getAuthenticatorName() {
        return AUTHENTICATOR_NAME;
    }

    /**
     * @inheritDoc
     */
    public boolean isDisabled() {
        return false;
    }

    /**
     * @inheritDoc
     * In this implementation we check whether there is a HTTP header for "RememberMe" and its value
     * is set to true. If value is set to true we need to persist remember me cookie.
     */
    protected boolean isRememberMeRequest(String userNameInRequest, MessageContext messageContext) {

        String rememberMeHeader = AuthenticationUtil.getHeader("RememberMe", messageContext);

        return rememberMeHeader != null && rememberMeHeader.equals("true");
    }


    /**
     * @inheritDoc
     * In this implementation we store remember me UID values and its age as HTTP headers in
     * the reply.
     */
    protected void populateRememberMeDataInReply(String uidValue, final int maxAge, MessageContext messageContext) {
        HttpServletResponse response = (HttpServletResponse) messageContext
                .getProperty(HTTPConstants.MC_HTTP_SERVLETRESPONSE);

        if (response != null) {
            response.addHeader("RememberMeCookieValue", uidValue);
            response.addHeader("RememberMeCookieAge", Integer.toString(maxAge));
        }
        
        Map<String, String> responseHeaders = new HashMap<String, String>();
        responseHeaders.put("RememberMeCookieValue", uidValue);
        responseHeaders.put("RememberMeCookieAge", Integer.toString(maxAge));
        messageContext.setProperty(MessageContext.TRANSPORT_HEADERS, responseHeaders);

    }

    /**
     * @inheritDoc
     * If priority is not configured in the file get the default priority.
     */
    public int getPriority() {

        // checks whether assigned priority is equal to default
        int configuredPriority = super.getPriority();

        if (configuredPriority == -1) {
            return DEFAULT_PRIORITY_LEVEL;
        } else {
            return configuredPriority;
        }

    }
}
