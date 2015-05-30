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
package org.wso2.carbon.tomcat.ext.valves;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.tomcat.ext.internal.CarbonRealmServiceHolder;
import org.wso2.carbon.tomcat.ext.internal.Utils;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This valves forces HTTP basic auth for all incoming requests
 *
 * Add this valve to catalina-server.xml as follows if you want to secure the world
 * <Valve className="org.wso2.carbon.tomcat.ext.valves.SecureTheWorldValve"/>
 */
@SuppressWarnings("unused")
public class SecureTheWorldValve extends ValveBase {

    private static final Log log = LogFactory.getLog(SecureTheWorldValve.class);

    public SecureTheWorldValve() {
        //enable async support
        super(true);
    }

    @Override
    public void invoke(Request req, Response res) throws IOException, ServletException {
        // Skip carbon auth calls
        String carbonWebContext = ServerConfiguration.getInstance().getFirstProperty("WebContextRoot");
        String contextPath = req.getContextPath();
        String requestPath = req.getCoyoteRequest().toString();
        if(!requestPath.contains("/services/t/") &&
           ((carbonWebContext.equals("/") && contextPath.equals("")) || carbonWebContext.equals(contextPath))){
            getNext().invoke(req, res);
            return;
        }
        if (authenticate(req)) {
            getNext().invoke(req, res);
        } else {
            //***We weren't sent a valid username/password in the header, so ask for one***
            res.setHeader("WWW-Authenticate", "Basic realm=\"WSO2 Carbon Authentication\"");
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "");
        }
    }

    /**
     * This method returns true if the HttpServletRequest contains a valid
     * authorisation header
     *
     * @param req The HttpServletRequest to test
     * @return true if the Authorisation header is valid
     */
    private boolean authenticate(HttpServletRequest req) {
        String authhead = req.getHeader("Authorization");

        if (authhead != null) {
            //*****Decode the authorisation String*****
            String usernpass = decode(authhead.substring(6));
            //*****Split the username from the password*****
            String userName = usernpass.substring(0, usernpass.indexOf(":"));
            String password = usernpass.substring(usernpass.indexOf(":") + 1);

            String tenantlessUserName;
            if (userName.lastIndexOf('@') > -1) {
                tenantlessUserName = userName.substring(0, userName.lastIndexOf('@'));
            } else {
                tenantlessUserName = userName;
            }
            String tenantFromUserName = null;
            if (userName.lastIndexOf('@') > -1) {
                tenantFromUserName = userName.substring(userName.indexOf('@') + 1);
            }

            String requestTenantDomain =
                    CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            String tenantDomain = Utils.getTenantDomain(req);
            // do not allow unauthorized access from other tenants
            if (tenantDomain != null &&
                    !tenantDomain.equals(requestTenantDomain) ||
                    (!(tenantFromUserName.equals("") && (tenantDomain == null)) &&
                            !tenantFromUserName.equals(requestTenantDomain))) {
                if (requestTenantDomain == null || requestTenantDomain.trim().length() == 0) {
                    requestTenantDomain = "super-tenant";
                }
                log.warn("Illegal access attempt by " + userName +
                         " to secured resource hosted by tenant [" + requestTenantDomain + "]");
                return false;
            }
            RealmService realmService = CarbonRealmServiceHolder.getRealmService();
            try {
                int tenantId = realmService.getTenantManager().getTenantId(tenantFromUserName);
                if(tenantId == -1){  // tenant does not exist?
                    return false;
                }
                UserRealm userRealm = realmService.getTenantUserRealm(tenantId);
                if (realmService.getTenantUserRealm(tenantId).getUserStoreManager().
                        authenticate(tenantlessUserName, password)) {
                    return true;
                }
            } catch (UserStoreException e) {
                log.error("Error occurred while authenticating", e);
            }
        }
        return false;
    }

    /**
     * decode a Base 64 encoded String.
     * <p><h4>String to byte conversion</h4>
     * This method uses a naive String to byte interpretation, it simply gets each
     * char of the String and calls it a byte.</p>
     * <p>Since we should be dealing with Base64 encoded Strings that is a reasonable
     * assumption.</p>
     * <p><h4>End of data</h4>
     * We don't try to stop the conversion when we find the "=" end of data padding char.
     * We simply add zero bytes to the unencode buffer.</p>
     * @param encoded The encoded String
     * @return the decoded String
     */
    public static String decode(String encoded) {
        StringBuilder sb = new StringBuilder();
        int maxturns;
        //work out how long to loop for.
        if (encoded.length() % 3 == 0)
            maxturns = encoded.length();
        else
            maxturns = encoded.length() + (3 - (encoded.length() % 3));
        //tells us whether to include the char in the unencode
        boolean skip;
        //the unencode buffer
        byte[] unenc = new byte[4];
        byte b;
        for (int i = 0, j = 0; i < maxturns; i++) {
            skip = false;
            //get the byte to convert or 0
            if (i < encoded.length())
                b = (byte) encoded.charAt(i);
            else
                b = 0;
            //test and convert first capital letters, lowercase, digits then '+' and '/'
            if (b >= 65 && b < 91)
                unenc[j] = (byte) (b - 65);
            else if (b >= 97 && b < 123)
                unenc[j] = (byte) (b - 71);
            else if (b >= 48 && b < 58)
                unenc[j] = (byte) (b + 4);
            else if (b == '+')
                unenc[j] = 62;
            else if (b == '/')
                unenc[j] = 63;
                //if we find "=" then data has finished, we're not really dealing with this now
            else if (b == '=')
                unenc[j] = 0;
            else {
                char c = (char) b;
                if (c == '\n' || c == '\r' || c == ' ' || c == '\t')
                    skip = true;
            }
            //once the array has boiled convert the bytes back into chars
            if (!skip && ++j == 4) {
                //shift the 6 bit bytes into a single 4 octet word
                int res = (unenc[0] << 18) + (unenc[1] << 12) + (unenc[2] << 6) + unenc[3];
                byte c;
                int k = 16;
                //shift each octet down to read it as char and add to StringBuffer
                while (k >= 0) {
                    c = (byte) (res >> k);
                    if (c > 0)
                        sb.append((char) c);
                    k -= 8;
                }
                //reset j and the unencode buffer
                j = 0;
                unenc[0] = 0;
                unenc[1] = 0;
                unenc[2] = 0;
                unenc[3] = 0;
            }
        }
        return sb.toString();
    }
}
