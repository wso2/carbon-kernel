/*
*Copyright (c) 2014​, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.integration.common.utils;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.httpclient.auth.AuthenticationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.wso2.carbon.core.commons.stub.loggeduserinfo.LoggedUserInfoAdminStub;
import org.wso2.carbon.utils.CarbonUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

/**
 * A utility for logging into & logging out of Carbon servers
 */
public final class LoginLogoutUtil {
    private static final Log log = LogFactory.getLog(LoginLogoutUtil.class);
    private String sessionCookie;

    public LoginLogoutUtil() {
    }


    public String login(String username, char[] password, String backendURL)
            throws MalformedURLException, RemoteException, LoginAuthenticationExceptionException {

        String authenticationServiceURL = backendURL + "AuthenticationAdmin";
        AuthenticationAdminStub authenticationAdminStub = new AuthenticationAdminStub(authenticationServiceURL);
        ServiceClient client = authenticationAdminStub._getServiceClient();
        String hostName = new URL(backendURL).getHost();
        Options options = client.getOptions();
        options.setManageSession(true);


        if (log.isDebugEnabled()) {
            log.debug("UserName : " + username + " Password : " + String.valueOf(password) + " HostName : " + hostName);
        }
        boolean isLoggedIn = authenticationAdminStub.login(username, String.valueOf(password), hostName);
        assert isLoggedIn : "Login failed!";

        log.debug("getting sessionCookie");
        ServiceContext serviceContext =
                authenticationAdminStub._getServiceClient().getLastOperationContext().getServiceContext();

        sessionCookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
        assert sessionCookie != null : "Logged in session cookie is null";

        if (log.isDebugEnabled()) {
            log.debug("sessionCookie : " + sessionCookie);
        }
        log.info("Successfully logged in : " + sessionCookie);

        return sessionCookie;
    }


    /**
     * Log out from a Carbon server you logged in to by calling the {@link #login} method
     *
     * @param backendURL - service URL of the carbon server
     * @throws AuthenticationException                - If an error occurs while logging out
     * @throws RemoteException                        - If an error occurs while logging out
     * @throws LogoutAuthenticationExceptionException - If an error occurs while logging out
     */
    public void logout(String backendURL) throws AuthenticationException, RemoteException,
                                                 LogoutAuthenticationExceptionException {

        AuthenticationAdminStub authenticationAdminStub =
                new AuthenticationAdminStub(backendURL + "AuthenticationAdmin");

        Options options = authenticationAdminStub._getServiceClient().getOptions();
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, sessionCookie);
        authenticationAdminStub.logout();

    }


    public boolean loginWithBasicAuth(String userName, char[] password, String backendServerURL) {

        LoggedUserInfoAdminStub stub;
        String authenticationServiceURL = backendServerURL + "LoggedUserInfoAdmin";
        try {
            stub = getLoggedUserInfoAdminStub(authenticationServiceURL);
        } catch (AxisFault axisFault) {
            log.error("Unable to create LoggedUserInfoAdmin stub", axisFault);
            return false;
        }

        ServiceClient client = stub._getServiceClient();
        CarbonUtils.setBasicAccessSecurityHeaders(userName, String.valueOf(password), client);

        try {
            stub.getUserInfo();
            return true;
        } catch (Exception e) {
            log.error("Unable to retrieve data from LoggedUserInfoAdmin", e);
        }

        return false;
    }

    private LoggedUserInfoAdminStub getLoggedUserInfoAdminStub(String backendServerURL)
            throws AxisFault {

        return new LoggedUserInfoAdminStub(backendServerURL);
    }

}