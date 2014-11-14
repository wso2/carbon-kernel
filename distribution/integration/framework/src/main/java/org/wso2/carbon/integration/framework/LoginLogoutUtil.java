/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.integration.framework;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.httpclient.auth.AuthenticationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.core.commons.stub.loggeduserinfo.LoggedUserInfoAdminStub;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.server.admin.ui.ServerAdminClient;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.NetworkUtils;

/**
 * A utility for logging into & logging out of Carbon servers
 */
public final class LoginLogoutUtil {
    private static final Log log = LogFactory.getLog(LoginLogoutUtil.class);
    private String sessionCookie;
    private int portOffset;

    public LoginLogoutUtil(int portOffset) {
        this.portOffset = portOffset;
    }

    public LoginLogoutUtil() {
    }

    /**
     * @deprecated Now we do not need to call AuthenticationAdmin.login method before calling an admin service.
     * We can directly call an admin service after setting basic auth security headers. To set basic auth
     * security headers please use CarbonUtils.setBasicAccessSecurityHeaders method.
     * @see CarbonUtils.setBasicAccessSecurityHeaders(String, String, ServiceClient);
     * Log in to a Carbon server
     *
     * @return The session cookie on successful login
     * @throws Exception If an error occurs while logging in
     */
    @Deprecated
    public String login() throws Exception {

        return login(NetworkUtils.getLocalHostname());
    }

     /**
     * @param hostName The client host name.
      * @deprecated Now we do not need to call AuthenticationAdmin.login method before calling an admin service.
     * We can directly call an admin service after setting basic auth security headers. To set basic auth
     * security headers please use CarbonUtils.setBasicAccessSecurityHeaders method.
     * @see CarbonUtils.setBasicAccessSecurityHeaders(String, String, ServiceClient);
     * Log in to a Carbon server
     *
     * @return The session cookie on successful login
     * @throws Exception If an error occurs while logging in
     */
    @Deprecated
    public String login(String hostName) throws Exception  {
    	return login(hostName, null);
    }
    
    /**
    * @param hostName The client host name.
    * @param carbonManagementContext context of the application
    * @deprecated Now we do not need to call AuthenticationAdmin.login method before calling an admin service.
    * We can directly call an admin service after setting basic auth security headers. To set basic auth
    * security headers please use CarbonUtils.setBasicAccessSecurityHeaders method.
    * @see CarbonUtils.setBasicAccessSecurityHeaders(String, String, ServiceClient);
    * Log in to a Carbon server
    *
    * @return The session cookie on successful login
    * @throws Exception If an error occurs while logging in
    */
   @Deprecated
   public String login(String hostName, String carbonManagementContext) throws Exception  {

       ClientConnectionUtil.waitForPort(Integer.parseInt(FrameworkSettings.HTTPS_PORT) + portOffset);
       AuthenticationAdminStub authAdminStub;
       if(carbonManagementContext == null || carbonManagementContext.trim().equals("")) {
    	   authAdminStub = getAuthAdminStub();
       }else {
    	   authAdminStub = getAuthAdminStub(carbonManagementContext);
       }
       if (log.isDebugEnabled()) {
           log.debug("UserName : " + FrameworkSettings.USER_NAME + " Password : " +
                     FrameworkSettings.PASSWORD + " HostName : " + hostName);
       }
       boolean isLoggedIn = authAdminStub.login(FrameworkSettings.USER_NAME,
                                                FrameworkSettings.PASSWORD, hostName);
       assert isLoggedIn : "Login failed!";
       log.debug("getting sessionCookie");
       ServiceContext serviceContext = authAdminStub.
               _getServiceClient().getLastOperationContext().getServiceContext();
       sessionCookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
       assert sessionCookie != null : "Logged in session cookie is null";
       if (log.isDebugEnabled()) {
           log.debug("sessionCookie : " + sessionCookie);
       }
       log.info("Successfully logged in : " + sessionCookie);
       return sessionCookie;

   }

    public String login(String hostName, String username, String password, String serverUrl)
            throws Exception  {

        ClientConnectionUtil.waitForPort(Integer.parseInt(FrameworkSettings.HTTPS_PORT) + portOffset);

        String authenticationServiceURL = serverUrl.concat("AuthenticationAdmin");
        AuthenticationAdminStub authenticationAdminStub = new AuthenticationAdminStub(authenticationServiceURL);
        ServiceClient client = authenticationAdminStub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);

        if (log.isDebugEnabled()) {
            log.debug("UserName : " + username + " Password : " + password + " HostName : " + hostName);
        }
        boolean isLoggedIn = authenticationAdminStub.login(username, password, hostName);
        assert isLoggedIn : "Login failed!";

        log.debug("getting sessionCookie");
        ServiceContext serviceContext = authenticationAdminStub._getServiceClient().getLastOperationContext().getServiceContext();

        sessionCookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
        assert sessionCookie != null : "Logged in session cookie is null";

        if (log.isDebugEnabled()) {
            log.debug("sessionCookie : " + sessionCookie);
        }
        log.info("Successfully logged in : " + sessionCookie);

        return sessionCookie;
    }

    public boolean loginWithBasicAuth() {

        ClientConnectionUtil.waitForPort(Integer.parseInt(FrameworkSettings.HTTPS_PORT) + portOffset);

        String authenticationServiceURL =
                "https://localhost:" + (Integer.parseInt(FrameworkSettings.HTTPS_PORT) + portOffset) +
                        "/services/";


        LoggedUserInfoAdminStub stub = null;
        try {
            stub = getLoggedUserInfoAdminStub(authenticationServiceURL);
        } catch (AxisFault axisFault) {
            log.error("Unable to create LoggedUserInfoAdmin stub", axisFault);
            return false;
        }

        ServiceClient client = stub._getServiceClient();

        CarbonUtils.setBasicAccessSecurityHeaders(FrameworkSettings.USER_NAME, FrameworkSettings.PASSWORD, client);


        try {
            stub.getUserInfo();
            return true;
        } catch (Exception e) {
            log.error("Unable to retrieve data from LoggedUserInfoAdmin", e);
        }

        return false;
    }

    /**
     * Log out from a Carbon server you logged in to by calling the {@link #login} method
     * @throws Exception If an error occurs while logging out
     */
    public void logout() throws Exception {
    	logout(null);
    }
    
    /**
     * Log out from a Carbon server you logged in to by calling the {@link #login} method
     * @param carbonManagementContext context of the application
     * @throws Exception If an error occurs while logging out
     */
    public void logout(String carbonManagementContext) throws Exception {
        AuthenticationAdminStub authenticationAdminStub;
        if(carbonManagementContext == null || carbonManagementContext.trim().equals("")) {
        	authenticationAdminStub = getAuthAdminStub();
        }else {
        	authenticationAdminStub = getAuthAdminStub(carbonManagementContext);
        }
        try {
            Options options = authenticationAdminStub._getServiceClient().getOptions();
            options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                                sessionCookie);
            authenticationAdminStub.logout();
        } catch (Exception e) {
            String msg = "Error occurred while logging out";
            log.error(msg, e);
            throw new AuthenticationException(msg, e);
        }
    }

    private AuthenticationAdminStub getAuthAdminStub(String carbonManagementContext) throws AxisFault {
        String authenticationServiceURL;
        if(carbonManagementContext == null || carbonManagementContext.trim().equals("")) {
            authenticationServiceURL =
                    "https://localhost:" + (Integer.parseInt(FrameworkSettings.HTTPS_PORT) + portOffset) +
                    "/services/AuthenticationAdmin";
        }else {
            authenticationServiceURL =
                    "https://localhost:" + (Integer.parseInt(FrameworkSettings.HTTPS_PORT) + portOffset) +
                    "/" + carbonManagementContext + "/services/AuthenticationAdmin";
        }
        
        if (log.isDebugEnabled()) {
            log.debug("AuthenticationAdminService URL = " + authenticationServiceURL);
        }
        AuthenticationAdminStub authenticationAdminStub =
                new AuthenticationAdminStub(authenticationServiceURL);
        ServiceClient client = authenticationAdminStub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        return authenticationAdminStub;
    }

    private AuthenticationAdminStub getAuthAdminStub() throws AxisFault {
    	return getAuthAdminStub(null);
    }

    private LoggedUserInfoAdminStub getLoggedUserInfoAdminStub(String backendServerURL)
            throws AxisFault {

        return new LoggedUserInfoAdminStub(backendServerURL + "LoggedUserInfoAdmin");
    }

    public static ServerAdminClient getServerAdminClient(int portOffset) throws AxisFault {
        return new ServerAdminClient("https://localhost:" +
                                     (Integer.parseInt(FrameworkSettings.HTTPS_PORT) + portOffset) +
                                     "/services/ServerAdmin/",
                                     FrameworkSettings.USER_NAME, FrameworkSettings.PASSWORD);
    }

}
