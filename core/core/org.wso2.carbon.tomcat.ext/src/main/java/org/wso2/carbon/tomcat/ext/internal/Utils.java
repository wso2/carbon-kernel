/*
 *  Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.tomcat.ext.internal;

import org.apache.catalina.connector.Request;
import org.wso2.carbon.tomcat.ext.utils.URLMappingHolder;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;

/**
 * A collection of useful utility methods
 */
public class Utils {
    private static final String WEB_APP_PATTERN = "/webapps/";
    private static final String JAGGERY_APP_PATTERN = "/jaggeryapps/";
    private static final String JAX_APP_PATTERN = "/jaxwebapps/";

	public static String getTenantDomain(HttpServletRequest request) {
		String requestURI = request.getRequestURI();
		String domain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;

		if (!requestURI.contains("/t/")) {
			// check for admin services - tenant admin services are deployed in
			// super tenant flow
			HttpSession session = request.getSession(false);
			if (session != null && session.getAttribute(MultitenantConstants.TENANT_DOMAIN) != null) {
				domain = (String) session.getAttribute(MultitenantConstants.TENANT_DOMAIN);
			}
		} else {
			String temp = requestURI.substring(requestURI.indexOf("/t/") + 3);
			if (temp.indexOf('/') != -1) {
				temp = temp.substring(0, temp.indexOf('/'));
				domain = temp;
			}
		}
		return domain;
	}

	public static String getServiceName(String requestURI) {
		String serviceName = "";
		if (requestURI.contains("/services/")) {
			String temp = requestURI.substring(requestURI.indexOf("/services/") + 9);
			if (temp.indexOf('/') != -1) {
				temp = temp.substring(0, temp.length());
				serviceName = temp;
			}
			if (serviceName.contains("/t/")) {
				String temp2[] = serviceName.split("/");
				if (temp2.length > 3) {
					serviceName = temp2[3];
				}
			}
			if (serviceName.contains(".")) {
				serviceName = serviceName.substring(0, serviceName.indexOf('.'));
			} else if (serviceName.contains("?")) {
				serviceName = serviceName.substring(0, serviceName.indexOf('?'));
			}
		}
        serviceName = serviceName.substring(serviceName.indexOf('/')+1, serviceName.length());
		return serviceName;
	}
    
    public static String getAppNameFromRequest(Request request) {
        String appName = null;
        String uri = request.getRequestURI();
        String temp = null;

        if (uri.startsWith("/services/")) {
            //setting the application id for services
            return Utils.getServiceName(uri);
        } else if (uri.contains(WEB_APP_PATTERN)) {
            //if the request from webapps, getting appName from uri
            temp = uri.substring(uri.indexOf(WEB_APP_PATTERN) + 9);
        } else if (uri.contains(JAGGERY_APP_PATTERN)) {
            //if the request from jaggeryapps, getting appName from uri
            temp = uri.substring(uri.indexOf(JAGGERY_APP_PATTERN) + 13);
        } else if(uri.contains(JAX_APP_PATTERN)) {
            //if the request from jaxapps, getting appName from uri
            temp = uri.substring(uri.indexOf(JAX_APP_PATTERN) + 12);
        } else {
            //if ST request getting the appName from the contex
            appName = request.getContext().getName();
            if ("".equals(appName)) {
                return appName;
            } else if(!appName.equals("/")) {
                return appName.substring(1);
            } else {
                return null;
            }
        }

        if(temp.contains("/")) {
            appName = temp.substring(0, temp.indexOf("/"));
        } else {
            //if the request from jenkins then back slash wont be there for root access of jenkins
            appName = temp;
        }
        return appName;
    }


    public static File createDummyTenantContextDir() {
        File dummyCtxFile = new File(CarbonUtils.getTmpDir() + File.separator + "t");
        if (!dummyCtxFile.exists() && !dummyCtxFile.mkdir()) {
            return null;
        } else {
            return dummyCtxFile;
        }
    }

    public static String getTenantDomainFromURLMapping(Request request) {
        String requestURI = request.getRequestURI();
        String domain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        //if the request is from a url mapping(https://apptest.wso2.com/),
        // need to get the tenant domain from the map
        String serverName = request.getHost().getName();
        String appContext = URLMappingHolder.getInstance().getApplicationFromUrlMapping(serverName);
        if(appContext != null) {
            requestURI = appContext;
        }
        if (!requestURI.contains("/t/")) {
            // check for admin services - tenant admin services are deployed in
            // super tenant flow
            HttpSession session = request.getSession(false);
            if (session != null && session.getAttribute(MultitenantConstants.TENANT_DOMAIN) != null) {
                domain = (String) session.getAttribute(MultitenantConstants.TENANT_DOMAIN);
            }
        } else {
            String temp = requestURI.substring(requestURI.indexOf("/t/") + 3);
            if (temp.indexOf('/') != -1) {
                temp = temp.substring(0, temp.indexOf('/'));
                domain = temp;
            }
        }
        return domain;
    }
    
    public static String getAppNameForURLMapping(Request request) {
        String appName = null;
        String temp = null;
        String hostName = request.getHost().getName();
        String uri = URLMappingHolder.getInstance().getApplicationFromUrlMapping(hostName);
        if (uri != null) {
            //setting application id for the webapps which has deployed in a virtual host
            if(uri.startsWith("/services/")) {
                appName = getServiceName(uri);
            } else if (uri.contains(WEB_APP_PATTERN)) {
                //if the request from webapps, getting appName from uri
                temp = uri.substring(uri.indexOf(WEB_APP_PATTERN) + 9);
            } else if (uri.contains(JAGGERY_APP_PATTERN)) {
                //if the request from jaggeryapps, getting appName from uri
                temp = uri.substring(uri.indexOf(JAGGERY_APP_PATTERN) + 13);
            } else if(uri.contains(JAX_APP_PATTERN)) {
                //if the request from jaxapps, getting appName from uri
                temp = uri.substring(uri.indexOf(JAX_APP_PATTERN) + 12);
            } else {
                temp = uri.substring(1);
            }

            if(temp.endsWith("/")) {
                appName = temp.substring(0, temp.indexOf("/"));
            } else {
                //if the request from jenkins then back slash wont be there for root access of jenkins
                appName = temp;
            }
        }
        return appName;
    }

}
