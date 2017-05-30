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
package org.wso2.carbon.core.services.loggeduserinfo;

import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.core.common.LoggedUserInfo;
import org.wso2.carbon.core.services.authentication.AuthenticationAdmin;
import org.wso2.carbon.core.services.util.CarbonAuthenticationUtil;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class LoggedUserInfoAdmin extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(AuthenticationAdmin.class);

    public LoggedUserInfo getUserInfo() throws Exception {
        try {
            MessageContext messageContext = MessageContext.getCurrentMessageContext();
            HttpServletRequest request = (HttpServletRequest) messageContext
                    .getProperty("transport.http.servletRequest");
            String userName = (String) request.getSession().getAttribute(
                    ServerConstants.USER_LOGGED_IN);

            int index = userName.indexOf(CarbonConstants.DOMAIN_SEPARATOR);
            if (index < 0) {
                String domainName = (String) request.getSession().getAttribute(
                        CarbonAuthenticationUtil.LOGGED_IN_DOMAIN);

                if (domainName != null) {
                    userName = domainName + CarbonConstants.DOMAIN_SEPARATOR + userName;
                }
            }
            LoggedUserInfo loggedUserInfo = new LoggedUserInfo();
            UserRealm userRealm = (UserRealm) PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm();
            List<String> userPermissions = getUserPermissions(userName, userRealm);
            String[] permissions = userPermissions.toArray(new String[userPermissions.size()]);
            loggedUserInfo.setUIPermissionOfUser(permissions);
            Date date = userRealm.getUserStoreManager().getPasswordExpirationTime(userName);
            loggedUserInfo.setUserName(userName);
            if (date != null) {
                DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
                DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
                String passwordExpiration = timeFormat.format(date) + " on "
                        + dateFormat.format(date);
                loggedUserInfo.setPasswordExpiration(passwordExpiration);
            }
            return loggedUserInfo;
        } catch (Exception e) {
            log.error(e);
            throw e;
        }
    }

    private List<String> getUserPermissions(String username, UserRealm realm) throws Exception {
        AuthorizationManager authManager = realm.getAuthorizationManager();
        String[] permissions = authManager.getAllowedUIResourcesForUser(username, RegistryUtils
                .getUnChrootedPath("/"));
        List<String> userPermissions = Arrays.asList(permissions);
        return userPermissions;
    }

}
