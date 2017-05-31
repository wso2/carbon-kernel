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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.core.services.internal.CarbonServicesServiceComponent;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

/**
 * An authenticator which will authenticate users based on the cookie value passed in the
 * HTTP header. If the passed cookie contains in the user manager database and if the user name is correct
 * this will return true.
 */
public class CookieAuthenticator extends AbstractAuthenticator {

    private static final String AUTHENTICATOR_NAME = "CookieAuthenticator";

    private static final Log log = LogFactory.getLog(BasicAccessAuthenticator.class);

    /**
     * @inheritDoc
     */
    @Override
    protected String getUserNameFromRequest(MessageContext msgContext) {

        String cookieData = getCookieData(msgContext);

        int index = cookieData.indexOf('-');
        return cookieData.substring(0, index);
    }

    protected String getCookieData(MessageContext msgContext) {

        return AuthenticationUtil.getHeader("RememberMeCookieData", msgContext);
    }

    /**
     * @inheritDoc
     */
    @Override
    protected void doAuthentication(String userNameInRequest, int tenantId, MessageContext msgContext)
            throws AuthenticationFailureException {
        String userName = MultitenantUtils.getTenantAwareUsername(userNameInRequest);

        String cookieData = getCookieData(msgContext);

        int index = cookieData.indexOf('-');
        String uuid = cookieData.substring(index + 1);
        UserRealm realm;
        try {
            realm = getRealmService().getTenantUserRealm(tenantId);
        } catch (Exception e) {
            log.error("Error retrieving user realm for authentication. Tenant id " +
                    tenantId + " user name " + userNameInRequest, e);
            throw new AuthenticationFailureException
                    (AuthenticationFailureException.AuthenticationFailureReason.SYSTEM_ERROR, userNameInRequest);
        }
        if (realm != null) {
            try {
                boolean isAuthenticated = realm.getUserStoreManager().isValidRememberMeToken(userName,
                        uuid);

                if (!isAuthenticated) {
                    if (log.isDebugEnabled()) {
                        log.debug("Failed authentication for user " + userNameInRequest);
                    }

                    throw new AuthenticationFailureException
                            (AuthenticationFailureException.AuthenticationFailureReason.INVALID_PASSWORD,
                                    userNameInRequest);
                }

            } catch (UserStoreException e) {
                log.error("Error retrieving user store manager for authentication. Tenant id " +
                        tenantId + " user name " + userNameInRequest, e);
                throw new AuthenticationFailureException
                        (AuthenticationFailureException.AuthenticationFailureReason.SYSTEM_ERROR, userNameInRequest);
            }
        } else {

            log.error("Error retrieving user realm for authentication. Tenant id " +
                    tenantId + " user name " + userNameInRequest);
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
         * If HTTP header RememberMeCookieData is present this will return true.
         */
        String cookieData = getCookieData(msgContext);

        return cookieData != null;

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
     */
    public int getPriority() {

        // checks whether assigned priority is equal to default
        int configuredPriority = super.getPriority();

        if (configuredPriority == -1) {
            return DEFAULT_PRIORITY_LEVEL + 1;
        } else {
            return configuredPriority;
        }

    }
    
    protected boolean isRememberMeRequest(String userNameInRequest, MessageContext messageContext) {
        return true;
    }
    
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
}
