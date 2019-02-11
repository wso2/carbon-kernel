/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.registry.core.servlet.utils;

import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.EmbeddedRegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.RealmConfiguration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Deprecated
public class Utils {

    /**
     * Extract the paramters from the query string. e.g. for the URL http://localhost:8080/registry/d1/myresource?action=tag,tag_value=support
     * project we have the query string "action=tag,tag_value=support project" it should be
     * extracted to map as key=action, value=tag key=tag_value, value=support project
     *
     * @param queryString query string of the URL
     *
     * @return Map containg paramter name --> paramter value pairs
     */
    public static Map getParameters(String queryString) {
        Map paramMap = new HashMap();
        String[] params = queryString.split(",");
        for (int i = 0; i < params.length; i++) {
            String[] param = params[i].split("=");
            paramMap.put(param[0].trim(), param[1].trim());
        }
        return paramMap;
    }

    /**
     * Returns the registry associated with the current session. If a user registry is not found,
     * new SecureRegistry instance is created with anonymous user and associated for the current
     * session.
     *
     * @param request Servlet request
     *
     * @return SecureRegistry instance for the current session.
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *          : if something went wrong
     */
    public static synchronized UserRegistry getSecureRegistry(HttpServletRequest request)
            throws RegistryException {
        UserRegistry registry;
        Object o = request.getSession().getAttribute(RegistryConstants.ROOT_REGISTRY_INSTANCE);
        if (o != null) {
            registry = (UserRegistry) o;
        } else {
            EmbeddedRegistryService embeddedRegistryService = (EmbeddedRegistryService) request.
                    getSession().getServletContext().getAttribute(RegistryConstants.REGISTRY);

            registry = embeddedRegistryService.getRegistry();

            request.getSession().setAttribute(RegistryConstants.ROOT_REGISTRY_INSTANCE, registry);
        }
        return registry;
    }

    public static boolean isLoggedIn(HttpServletRequest request) {

        UserRegistry userRegistry =
                (UserRegistry) request.getSession()
                        .getAttribute(RegistryConstants.ROOT_REGISTRY_INSTANCE);

        String anonymousUser;
        RegistryContext registryContext = RegistryContext.getBaseInstance();
        if (registryContext != null) {
            anonymousUser = CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME;
        } else {
            return false;
        }
        return userRegistry != null &&
                !userRegistry.getUserName().equals(anonymousUser);
    }

    public static void logInUser(HttpServletRequest request, String userName, String password)
            throws RegistryException {

        ServletContext context = request.getSession().getServletContext();
        EmbeddedRegistryService embeddedRegistryService =
                (EmbeddedRegistryService) context.getAttribute(RegistryConstants.REGISTRY);

        UserRegistry userRegistry =
                embeddedRegistryService.getConfigUserRegistry(userName, password);

        request.getSession().setAttribute(RegistryConstants.ROOT_REGISTRY_INSTANCE, userRegistry);
    }
}
