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
package org.wso2.carbon.registry.server.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.server.internal.Utils;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.NetworkUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * An Admin Service exposing server-side information about the registry.
 */
@SuppressWarnings("unused")
public class RegistryAdmin extends AbstractAdmin {

    private static Log log = LogFactory.getLog(RegistryAdmin.class);

    /**
     * Method to determine whether the back-end registry instance is read-only.
     *
     * @return true if the back-end registry instance is read-only or false if not.
     */
    public boolean isRegistryReadOnly() {
        RegistryService registryService = Utils.getRegistryService();
        if (registryService == null) {
            log.error("Registry Service has not been set.");
        } else {
            try {
                RegistryContext context =
                        registryService.getConfigSystemRegistry().getRegistryContext();
                if (context != null) {
                    return context.isReadOnly();
                }
            } catch (Exception e) {
                log.error("An error occurred while obtaining registry instance", e);
            }
        }
        return false;
    }

    /**
     * Method to retrieve the HTTP Permalink to access the resource.
     *
     * @param path the resource path
     *
     * @return the HTTP permalink to access the given resource.
     */
    public String getHTTPPermalink(String path) {
        return getPermalink(path, "http");
    }

    /**
     * Method to retrieve the HTTPS Permalink to access the resource.
     *
     * @param path the resource path
     *
     * @return the HTTPS permalink to access the given resource.
     */
    public String getHTTPSPermalink(String path) {
        return getPermalink(path, "https");
    }

    // Method to get the permalink for a given resource path under the given scheme.
    private String getPermalink(String path, String scheme) {
        String host;
        try {
            host = NetworkUtils.getLocalHostname();
        } catch (Exception e) {
            log.error("An error occurred while constructing the permalink for the given path: " +
                    path, e);
            return null;
        }
        int port = CarbonUtils.getTransportProxyPort(getConfigContext(), scheme);
        if (port == -1) {
            port = CarbonUtils.getTransportPort(getConfigContext(), scheme);
        }
        String webContext = ServerConfiguration.getInstance().getFirstProperty("WebContextRoot");
        if (webContext == null || webContext.equals("/")) {
            webContext = "";
        }
        RegistryService registryService = Utils.getRegistryService();
        String version = "";
        if (registryService == null) {
            log.error("Registry Service has not been set.");
        } else if (path != null) {
            try {
                String[] versions = registryService.getRegistry(
                        CarbonConstants.REGISTRY_SYSTEM_USERNAME,
                        CarbonContext.getCurrentContext().getTenantId()).getVersions(path);
                if (versions != null && versions.length > 0) {
                    version = versions[0].substring(versions[0].lastIndexOf(";version:"));
                }
            } catch (RegistryException e) {
                log.error("An error occurred while determining the latest version of the " +
                        "resource at the given path: " + path, e);
            }
        }
        if (host != null && port != -1 && path != null) {
            String tenantDomain =
                    PrivilegedCarbonContext.getCurrentContext().getTenantDomain(true);
            return scheme + "://" + host + ":" + port + webContext + 
            		( (tenantDomain != null &&
            		!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) ?
            			"/" + MultitenantConstants.TENANT_AWARE_URL_PREFIX + "/" + tenantDomain : 
            			"") +
                    "/registry/resource" +
                    org.wso2.carbon.registry.app.Utils.encodeRegistryPath(path) + version;
        }
        return null;
    }
}
