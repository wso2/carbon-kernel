/*
*  Copyright (c), WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.core.common.AuthenticationException;
import org.wso2.carbon.core.security.AuthenticatorsConfiguration;
import org.wso2.carbon.core.services.internal.CarbonServicesServiceComponent;
import org.wso2.carbon.core.services.util.CarbonAuthenticationUtil;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.AuthenticationObserver;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * An abstract class which implements {@link org.wso2.carbon.core.services.authentication.ServerAuthenticator}
 * interface. This class implements basic ground work during an authentication. The actual authentication
 * logic must be implemented within a subclass.
 */
public abstract class AbstractAuthenticator extends AbstractAdmin implements ServerAuthenticator {

    protected static final int DEFAULT_PRIORITY_LEVEL = 4;

    private static final Log log = LogFactory.getLog(AbstractAuthenticator.class);

    /**
     * @inheritDoc
     */
    public boolean isAuthenticated(MessageContext msgContext) {

        HttpSession httpSession = getHttpSession(msgContext);

        if (httpSession != null) {
            String userLoggedIn = (String) httpSession.getAttribute(ServerConstants.USER_LOGGED_IN);

            if (userLoggedIn != null) {
                try {
                    onSuccessLogin(httpSession, userLoggedIn);
                    return true;
                } catch (Exception e) {
                    log.error("Error occurred while initializing user session.", e);
                }
            }
        }

        return false;
    }

    public static boolean continueProcessing(MessageContext messageContext) {

        String continueProcessing = (String) messageContext.getProperty(ServerAuthenticator.CONTINUE_PROCESSING);
        return !(continueProcessing != null && continueProcessing.equals("false"));
    }

    /**
     * @inheritDoc
     */
    public void authenticate(MessageContext msgContext) throws AuthenticationFailureException {

        String userNameInRequest = getUserNameFromRequest(msgContext);

        if (userNameInRequest == null) {

            log.debug("Could not retrieve user name for authentication from request");
            throw new AuthenticationFailureException
                    (AuthenticationFailureException.AuthenticationFailureReason.INVALID_USER_NAME);
        }

        String tenantDomain = MultitenantUtils.getTenantDomain(userNameInRequest);

        int tenantId;
        try {
            tenantId = getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            log.error("Unable retrieve tenant id for tenant domain " + tenantDomain, e);
            throw new AuthenticationFailureException
                    (AuthenticationFailureException.AuthenticationFailureReason.SYSTEM_ERROR,
                            userNameInRequest);
        }

        if(tenantId == MultitenantConstants.INVALID_TENANT_ID){
            log.error("Invalid domain  : " + tenantDomain);
            throw new AuthenticationFailureException
                    (AuthenticationFailureException.AuthenticationFailureReason.SYSTEM_ERROR,
                            userNameInRequest);
        }

        if (log.isDebugEnabled()) {
            log.debug("Doing authentication for tenant id " + tenantId + " and user "
                    + userNameInRequest);
        }

        // Do actions before authenticating user
        try {
            // TODO should we continue if pre-authenticating tasks failed ?
            notifyAuthenticationStarted(tenantId);
        } catch (Exception e) {
            log.error("An error occurred while executing pre authenticating tasks.", e);
            throw new AuthenticationFailureException
                    (AuthenticationFailureException.AuthenticationFailureReason.SYSTEM_ERROR,
                            userNameInRequest);

        }

        String remoteAddress = getRemoteAddress(msgContext);

        String tenantLessUserName = MultitenantUtils.getTenantAwareUsername(userNameInRequest);

        // Do actual authentication
        try {
            doAuthentication(userNameInRequest, tenantId, msgContext);
        } catch (AuthenticationFailureException e) {

            if (log.isDebugEnabled()) {
                StringBuilder stringBuilder = new StringBuilder("Did authentication for user ")
                        .append(userNameInRequest).append(" and for tenant id ").append(tenantId)
                        .append(" result is - authentication failed !!").append(" reason - ").append(e.getMessage());
                log.debug(stringBuilder.toString());
            }

            try {
                // post authentication actions on a failed login
                onFailedLogin(tenantLessUserName, tenantId, remoteAddress, e.getMessage(), msgContext);
            } catch (Exception e1) {
                log.error("Unable to execute post authentication operation on failed login attempt. User - "
                        + userNameInRequest, e1);
            }

            throw e;
        }

        if (!(tenantLessUserName.indexOf(CarbonConstants.DOMAIN_SEPARATOR) > 0) && StringUtils.isNotEmpty(UserCoreUtil.getDomainFromThreadLocal())) {
            tenantLessUserName = UserCoreUtil.getDomainFromThreadLocal() + CarbonConstants.DOMAIN_SEPARATOR + tenantLessUserName;
        }

        try {
            // post authentication actions on a success login
            onSuccessLogin(tenantLessUserName, tenantId, tenantDomain, remoteAddress, msgContext);
        } catch (Exception e) {
            log.error("Unable to execute post authentication operations in a successful login. " +
                    "User - " + userNameInRequest, e);
            // TODO should we throw authentication failed exception OR should we continue ?
            // Cos the authentication is success it is post auth operations which are failing ...
            throw new AuthenticationFailureException(
                    AuthenticationFailureException.AuthenticationFailureReason.SYSTEM_ERROR, userNameInRequest);
        }

        // Handle remember me functionality
        if (this.isRememberMeRequest(userNameInRequest, msgContext)) {
            try {
                RememberMeData rememberMeData = persistsRememberMeData(userNameInRequest);
                populateRememberMeDataInReply(rememberMeData.getValue(), rememberMeData.getMaxAge(), msgContext);
            } catch (Exception e) {
                log.error("Unable to persists RememberMe cookie.", e);
                throw new AuthenticationFailureException(
                    AuthenticationFailureException.AuthenticationFailureReason.SYSTEM_ERROR, userNameInRequest);

            }
        }
    }

    protected RememberMeData persistsRememberMeData(String userName) throws Exception {
        String uuid = UUID.randomUUID().toString();

        RememberMeData data = new RememberMeData();
        data.setMaxAge(CarbonConstants.REMEMBER_ME_COOKIE_TTL);
        data.setValue(userName + "-" + uuid);

        RealmService realmService = CarbonServicesServiceComponent.getRealmService();
        String tenantDomain = MultitenantUtils.getTenantDomain(userName);

        int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);

        UserRealm realm = realmService.getTenantUserRealm(tenantId);
        realm.getUserStoreManager().addRememberMe(userName, uuid);

        data.setAuthenticated(true);

        return data;
    }

    /**
     * There are some stuff needs to be done in a success login, like setting values in the http session
     * etc ... This method will call respective other methods to do those actions.
     *
     * @param userName      Name of the user who logged in.
     * @param tenantId      The respective user's tenant id.
     * @param tenantDomain The tenant domain.
     * @param remoteAddress Remote address which user is accessing
     * @param msgContext The request message context
     * @throws AuthenticationException If an error occurred while executing post login actions.
     */
    protected void onSuccessLogin(String userName, int tenantId, String tenantDomain,
                                  String remoteAddress, MessageContext msgContext)
            throws Exception {

        // TODO why this is called onSuccessAdminLogin ? why "Admin" ?
        CarbonAuthenticationUtil.onSuccessAdminLogin(getHttpSession(msgContext), userName, tenantId,
                tenantDomain, remoteAddress);

        notifyAuthenticationCompleted(tenantId, true);

    }

    /**
     * Sets the root registry for an authenticated user.
     * @param httpSession The http session
     * @param userName The user name
     * @throws Exception If an error occurred while creating the root registry.
     */
    protected void onSuccessLogin(HttpSession httpSession, String userName)
            throws Exception {

        if (httpSession.getAttribute(RegistryConstants.ROOT_REGISTRY_INSTANCE) != null) {
            return;
        }

        // If user is already authenticated then set the root registry instance
        // this is useful when server is restarted and browser keeps the same session
        // for e.g. :- restart carbon server and in the same browser session click registry UI

        String tenantDomain = getTenantDomain();
        int tenantId = getTenantId(tenantDomain);

        CarbonAuthenticationUtil.initializeLoggedInUserRegistry(httpSession, userName, tenantId, tenantDomain);
    }

    /**
     * Gets the HTTP session from message context. If HTTP request is null this will
     * return null.
     * @param msgContext The incoming message context.
     * @return The HTTPSession if HttpServletRequest is not null in message context, else <code>null</code>.
     */
    protected HttpSession getHttpSession(MessageContext msgContext) {

        HttpServletRequest request = (HttpServletRequest) msgContext
                .getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);

        if (request != null) {
            return request.getSession();
        }

        return null;
    }

    /**
     * There are some stuff needs to be done in a failed login, like logging failed attempt etc ...
     * This method will call those subsequent operations.
     *
     * @param userName      Name of the user who logged in.
     * @param tenantId      The respective user's tenant id.
     * @param remoteAddress Remote address which user is accessing
     * @param reason        Reason for authentication failure, could be a system error or invalid user name/password.
     * @param messageContext The request message context.
     * @throws AuthenticationException If an error occurred while executing post login actions.
     */
    protected void onFailedLogin(String userName, int tenantId, String remoteAddress,
                                 String reason, MessageContext messageContext) throws Exception {

        CarbonAuthenticationUtil.onFailedAdminLogin(getHttpSession(messageContext), userName, tenantId,
                remoteAddress, reason);

        notifyAuthenticationCompleted(tenantId, false);

    }

    /**
     * Gets the remote address from the message context. This is not the remote address of user's client (browser)
     * but this is where our FE component is running (in the case of management console). So this is not the
     * best method to get remote address.
     *
     * @param msgCtx The request message context.
     * @return Remote address (IP/Hostname) as a string.
     */
    protected String getRemoteAddress(MessageContext msgCtx) {
        try {
            return AuthenticationUtil.getRemoteAddress(msgCtx);
        } catch (AuthenticationException e) {
            log.error("Invalid remote address detected.", e);
        }

        return null;
    }

    /**
     * Other components may need perform some actions upon user authentication. In such situation the external component
     * can register himself as a AuthenticationObserver and get notifications. Following method will send notifications
     * to all registered observers, before user is authenticated.
     *
     * @param tenantId The tenant id which user authenticating user belongs to.
     * @throws AuthenticationException If an error occurred while updating observers.
     * @see org.wso2.carbon.utils.AuthenticationObserver
     */
    protected void notifyAuthenticationStarted(int tenantId) throws Exception {
        BundleContext bundleContext = getBundleContext();

        if (bundleContext != null) {

            @SuppressWarnings({"unchecked"}) ServiceTracker tracker = new ServiceTracker(bundleContext,
                    AuthenticationObserver.class.getName(), null);
            tracker.open();
            Object[] services = tracker.getServices();
            if (services != null) {
                for (Object service : services) {
                    ((AuthenticationObserver) service).startedAuthentication(tenantId);
                }
            }
            tracker.close();
        } else {
            log.debug("BundleContext is null. Could not update AuthenticationObservers !!");
        }
    }

    /**
     * Other components may need perform some actions after user authentication. In such situation the external component
     * can register himself as a AuthenticationObserver and get notifications. Following method will send notifications
     * to all registered observers, after user is authenticated.
     *
     * @param tenantId   The tenant id which user authenticating user belongs to.
     * @param successful <code>true</code> if authentication is successful else <code>false</code>.
     * @throws AuthenticationException If an error occurred while updating observers.
     * @see org.wso2.carbon.utils.AuthenticationObserver
     */
    protected void notifyAuthenticationCompleted(int tenantId, boolean successful) throws Exception {
        BundleContext bundleContext = getBundleContext();

        if (bundleContext != null) {
            @SuppressWarnings({"unchecked"}) ServiceTracker tracker = new ServiceTracker(bundleContext,
                    AuthenticationObserver.class.getName(), null);
            tracker.open();
            Object[] services = tracker.getServices();
            if (services != null) {
                for (Object service : services) {
                    ((AuthenticationObserver) service).completedAuthentication(tenantId,
                            successful);
                }
            }
            tracker.close();
        } else {
            log.debug("BundleContext is null. Could not update AuthenticationObservers !!");
        }
    }

    /**
     * Gets the tenant id which given user belongs to.
     *
     * @param tenantDomain The tenant domain.
     * @return The tenant id which user belongs to.
     * @throws UserStoreException If an exception occurred while getting tenant id for the user.
     */
    protected int getTenantId(String tenantDomain) throws UserStoreException {

        try {
            return getRealmService().getTenantManager().getTenantId(tenantDomain);
        } catch (Exception e) {
            throw new UserStoreException("Unable to retrieve Realm service for authentication");
        }
    }

    /**
     * This method reads the configuration relevant to given authenticator name and will return the
     * priority level. This is a helper method for child classes. If the priority is not defined in
     * the configuration this will return the default priority level. {@see #DEFAULT_PRIORITY_LEVEL}.
     *
     * @return Priority defined for the given authenticator. If authenticator is not found in the configuration
     *         this will return -1.
     */
    public int getPriority() {
        AuthenticatorsConfiguration authenticatorsConfiguration = AuthenticatorsConfiguration.getInstance();
        AuthenticatorsConfiguration.AuthenticatorConfig authenticatorConfig =
                authenticatorsConfiguration.getAuthenticatorConfig(getAuthenticatorName());
        if (authenticatorConfig != null && authenticatorConfig.getPriority() > 0) {
            return authenticatorConfig.getPriority();
        }

        return -1;

    }

    /**
     * The implementer should extract the user name from incoming message context and return in this method. Different
     * authenticators will have different ways of getting user name from request.
     *
     * @param msgContext The request message context.
     * @return User name as in the request message context. <code>null</code> if user name is not found.
     */
    protected abstract String getUserNameFromRequest(MessageContext msgContext);

    /**
     * Implementer should write actual authenticating logic in following method.
     *
     * @param userNameInRequest The user name returned by {@see #getUserNameFromRequest}.
     * @param tenantId          The tenant id which user belongs to.
     * @param msgContext        The request message context.
     * @throws AuthenticationFailureException if authentication is failed. Failure reason can be taken
     * from the exception object.
     * @see AuthenticationFailureException
     */
    protected abstract void doAuthentication(String userNameInRequest, int tenantId, MessageContext msgContext)
            throws AuthenticationFailureException;

    /**
     * Checks whether given request contains remember me option.
     * @param userNameInRequest User name as in the request.
     * @param messageContext The incoming message context.
     * @return <code>true</code> if the request asks to remember else <code>false</code>.
     */
    protected boolean isRememberMeRequest(String userNameInRequest, MessageContext messageContext) {
        return false;
    }

    /**
     * This method will populate data necessary for cookie authentication in the response.
     * @param uidValue The generated UID value.
     * @param maxAge Age of the cookie.
     * @param messageContext The message context.
     */
    protected void populateRememberMeDataInReply(String uidValue, final int maxAge, MessageContext messageContext) {
        // Do nothing in the default implementation
    }

    /**
     * Gets the RealmService. This is needed to find tenant id/domain of a user. So implementer should
     * return the RealmService after getting it from OSGI environment.
     *
     * @return The RealmService
     * @throws Exception If an error occurred while getting RealmService.
     */
    protected abstract RealmService getRealmService() throws Exception;

    /**
     * The implementer should return the BundleContext. BundleContext is needed to track AuthenticationObservers.
     *
     * @return BundleContext The bundle context which implementer component belongs.
     * @throws AuthenticationException If an error occurred while retrieving the bundle context.
     */
    protected abstract BundleContext getBundleContext() throws Exception;


}
