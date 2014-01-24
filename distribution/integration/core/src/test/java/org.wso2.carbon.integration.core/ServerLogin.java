/**
 *  Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

// Returning sessionCookie to authenticate other admin services

package org.wso2.carbon.integration.core;

import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.httpclient.auth.AuthenticationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.utils.NetworkUtils;


public class ServerLogin {
    private AuthenticationAdminStub authenticationAdminStub;
    private static final Log log = LogFactory.getLog(ServerLogin.class);

    public String login() throws Exception {
        log.debug("Server login class login method called");
        FrameworkSettings.getProperty();
        String authenticationServiceURL = FrameworkSettings.SERVICE_URL + "AuthenticationAdmin";
        log.debug("AuthenticationAdminService URL = " + authenticationServiceURL);
        authenticationAdminStub = new AuthenticationAdminStub(authenticationServiceURL);
        ServiceClient client = authenticationAdminStub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);

        String userName = FrameworkSettings.USER_NAME;
        String password = FrameworkSettings.PASSWORD;

        String hostName = NetworkUtils.getLocalHostname();
        log.debug("UserName : " + FrameworkSettings.USER_NAME + " Password : " + FrameworkSettings.PASSWORD + " HostName : " + hostName);
        authenticationAdminStub.login(userName, password, hostName);
        log.debug("getting sessionCookie");
        ServiceContext serviceContext = authenticationAdminStub.
                _getServiceClient().getLastOperationContext().getServiceContext();
        String sessionCookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
        log.debug("sessionCookie : " + sessionCookie);
        log.info("Successfully logged : " + sessionCookie);
        log.debug("exit from login method");
        return sessionCookie;
    }

    public void logout() throws Exception {
        try {
            log.debug("Logout method called in authentication class");
            authenticationAdminStub.logout();
        }
        catch (Exception e) {
            String msg = "Error occurred while logging out";
            log.debug(msg);
            throw new AuthenticationException(msg, e);
        }
    }
}
