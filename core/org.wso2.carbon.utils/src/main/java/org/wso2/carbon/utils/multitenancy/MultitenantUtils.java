/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.utils.multitenancy;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.userinfo.UserInfoHandler;

@SuppressWarnings("unused")
public class MultitenantUtils {

    private static final Log log = LogFactory.getLog(MultitenantUtils.class);

    public static String getTenantAwareUsername(String username) {
        //Use UserInfoHandler extension to derive the functionality, if its configured.
        // Also if the extension returns null, it will resume the normal logic.
        UserInfoHandler userInfoHandlerHandler = getUserInfoHandler();
        if (userInfoHandlerHandler != null) {
            String tenantAwareUserName = userInfoHandlerHandler.getTenantAwareUsername(username);
            if(tenantAwareUserName != null) {
                return tenantAwareUserName;
            }
        }

        if (username.contains("@") && !isEmailUserName()) {
            username = username.substring(0, username.lastIndexOf('@')); // then pick user name, which is preceding last '@' sign
        } else if (isEmailUserName()) {
            if (username.indexOf("@") == username.lastIndexOf("@")) {
                //do nothing - this is super tenant login
            } else {
                username = username.substring(0, username.lastIndexOf('@'));
            }
        }
        return username;
    }

    public static String getTenantDomain(HttpServletRequest request) {
        String tenantDomain = (String) request.getAttribute(MultitenantConstants.TENANT_DOMAIN);
        if (tenantDomain == null || tenantDomain.trim().length() == 0) {
            tenantDomain = getTenantDomainFromRequestURL(request.getRequestURI());
        }
        if (tenantDomain == null || tenantDomain.trim().length() == 0) {
        	// if the tenant domain is null, assume super tenant
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        return tenantDomain;
    }

    public static String getTenantDomain(String username) {
        //Use UserInfoHandler extension to derive the functionality, if its configured.
        // Also if the extension returns null, it will resume the normal logic.
        UserInfoHandler userInfoHandlerHandler = getUserInfoHandler();
        if (userInfoHandlerHandler != null) {
            String tenantDomain = userInfoHandlerHandler.getTenantDomain(username);
            if (tenantDomain != null) {
                return tenantDomain;
            }
        }

        String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        if (username.contains("@") && !isEmailUserName()) {
            tenantDomain = username.substring(username.lastIndexOf('@') + 1);
        } else if (isEmailUserName()) {
            if (username.indexOf("@") == username.lastIndexOf("@")) {
                //do nothing - this is super tenant login
            } else {
                tenantDomain = username.substring(username.lastIndexOf('@') + 1);
            }
        }
        if (tenantDomain == null || tenantDomain.trim().length() == 0) {
        	// if the tenant domain is null, assume super tenant
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        return tenantDomain.toLowerCase();
    }

    /**
     * Obtain the domain name from an OpenID
     * @param openId the OpenID.
     * @return domain name
     */
    public static String getDomainNameFromOpenId(String openId) {
        openId = openId.trim();
        String tenantDomain = null;
        if (openId.indexOf('@')>0){
        	tenantDomain = openId.substring(openId.indexOf('@')+1);
        }
        if (tenantDomain == null || tenantDomain.trim().length() == 0) {
        	// if the tenant domain is null, assume super tenant
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
//      TODO   resolve openid patterns
        return tenantDomain;
    }

    public static String getTenantDomainFromRequestURL(String requestURI) {
        String domain = null;
        if (requestURI.contains("/" + MultitenantConstants.TENANT_AWARE_URL_PREFIX + "/")) {
            int index = requestURI.indexOf("/" +
                    MultitenantConstants.TENANT_AWARE_URL_PREFIX + "/");
            int endIndex = requestURI.indexOf("/", index + 3);
            domain = (endIndex != -1) ?
                     requestURI.substring(index + 3, endIndex) :
                     requestURI.substring(index + 3);
        }
        return domain;
    }

    private static String getHostName() {
        ServerConfiguration serverConfig = ServerConfiguration.getInstance();
        if (serverConfig.getFirstProperty("HostName") != null) {
            return serverConfig.getFirstProperty("HostName");
        } else {
            return "localhost";
        }
    }

    public static String getTenantDomainFromUrl(String url) {
        int tenantDelimiterIndex = url.indexOf("/t/");
        String tenant;
        if (tenantDelimiterIndex != -1) {
            String temp = url.substring(tenantDelimiterIndex + 3);  // 3 = length("/t/")
            int indexOfSlash = temp.indexOf('/');
            tenant = (indexOfSlash !=-1) ? temp.substring(0, indexOfSlash) : temp;
            return tenant;
        }
        return url;
    }

    /**
     * Get tenant ID from config context
     *
     * @param configCtx The config context
     *
     * @return The tenant ID
     */
    public static int getTenantId(ConfigurationContext configCtx) {
        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
    }

    private static Map<Integer, String> tenantIdToAxisRepoMap = new HashMap<Integer, String>();

    /**
     * Get the Axis2 repository path of a particular tenant
     *
     * @param tenantId The tenant ID
     * @return The absolute path of the Axis2 repository used by the tenant
     */
    public static String getAxis2RepositoryPath(int tenantId) {
        if (tenantIdToAxisRepoMap.containsKey(tenantId)) {
            return tenantIdToAxisRepoMap.get(tenantId);
        }

        String relRepoPath;
        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            relRepoPath = ServerConfiguration.getInstance().getFirstProperty(
                    ServerConfiguration.AXIS2_CONFIG_REPO_LOCATION);
        } else {
            relRepoPath = CarbonUtils.getCarbonTenantsDirPath() + File.separator + tenantId;
        }

        String repoPath = (new File(relRepoPath)).getAbsolutePath() + File.separator;
        tenantIdToAxisRepoMap.put(tenantId, repoPath);
        return repoPath;
    }

    public static boolean isEmailUserName(){

        String enableEmailUserName = ServerConfiguration.getInstance().
                                getFirstProperty(MultitenantConstants.ENABLE_EMAIL_USER_NAME);
        return enableEmailUserName != null && "true".equals(enableEmailUserName.trim());

    }

    private static UserInfoHandler getUserInfoHandler() {
        String userInfoHandlerClass = ServerConfiguration.getInstance().getFirstProperty(
                MultitenantConstants.USER_INFO_HANDLER);
        if(userInfoHandlerClass == null || "".equals(userInfoHandlerClass)) {
            return null;
        }
        try {
            Class userInfoHandler = Class.forName(userInfoHandlerClass);
            return (UserInfoHandler) userInfoHandler.newInstance();
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.error("Error getting user info handler.", e);
            }
            return null;
        }
    }
}
