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

package org.wso2.carbon.registry.core.servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.clustering.NodeGroupLock;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.EmbeddedRegistryService;
import org.wso2.carbon.registry.core.jdbc.utils.Transaction;
import org.wso2.carbon.registry.core.servlet.utils.Utils;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.AccessControlConstants;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.UserStoreException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;

/**
 * Servlet for providing REST API for the registry.
 */
@Deprecated
public class RegistryServlet extends HttpServlet {
    private static final long serialVersionUID = 2000065602498609086L;
    private static final Log log = LogFactory.getLog(RegistryServlet.class);
    protected transient ServletConfig servletConfig;

    //To store the context path
    private String contextRoot = null;

    public void init(final ServletConfig config) throws ServletException {
        super.init(config);
        EmbeddedRegistryService embeddedRegistryService;

        try {
            // read the registry.xml file from the configured location. if not configured, read the
            // default registry.xml file bundled with the webapp.
            String configPath = config.getInitParameter(RegistryConstants.REGISTRY_CONFIG_PATH);
            InputStream configStream;
            if (configPath != null) {
                try {
                    configStream = new FileInputStream(configPath);
                } catch (FileNotFoundException e) {
                    throw new ServletException("Couldn't find specified config file '" +
                            configPath + "'", e);
                }
            } else {
                configStream =
                        config.getServletContext().getResourceAsStream("/WEB-INF/registry.xml");
            }

            RegistryContext registryContext = RegistryContext.getBaseInstance(configStream,
                    new RegistryContext.RegURLSupplier() {
                        public String getURL() {
                            return config.getServletContext().getRealPath("/WEB-INF");
                        }
                    });

            embeddedRegistryService = registryContext.getEmbeddedRegistryService();

            // create a system registry and put it in the context
            UserRegistry systemRegistry = embeddedRegistryService.getConfigSystemRegistry();

            // add configured handers to the jdbc registry
            // note: no need to do this here. this is done inside the registry context
            //Iterator<HandlerConfiguration> handlers =
            //        registryContext.getHandlerConfigurations().iterator();
            //while (handlers.hasNext()) {
            //    HandlerConfiguration handlerConfiguration = handlers.next();
            //    registryContext.getHandlerManager().addHandler(0,
            //            handlerConfiguration.getFilter(), handlerConfiguration.getHandler());
            //}

            // create system resources

            NodeGroupLock.lock(NodeGroupLock.INITIALIZE_LOCK);

            if (log.isTraceEnabled()) {
                log.trace("Creating system collections used in WSO2 Registry server.");
            }

            if (!systemRegistry.resourceExists("/system")) {

                try {
                    boolean inTransaction = Transaction.isStarted();
                    if (!inTransaction) {
                        systemRegistry.beginTransaction();
                    }
                    Collection systemCollection = systemRegistry.newCollection();
                    String systemDesc = "This collection is used to store system data of the " +
                            "WSO2 Registry server. User nor the admins of the registry are not expected " +
                            "to edit any content of this collection. Changing content of this collection " +
                            "may result in unexpected behaviors.";
                    systemCollection.setDescription(systemDesc);
                    systemRegistry.put("/system", systemCollection);

                    Collection advancedQueryCollection = systemRegistry.newCollection();
                    String advaceDesc = "This collection is used to store auto generated queries " +
                            "to support various combinations of advanced search criteria. " +
                            "This is initialy empty and gets filled as advanced search is " +
                            "executed from the web UI.";
                    advancedQueryCollection.setDescription(advaceDesc);
                    systemRegistry.put("/system/queries/advanced", advancedQueryCollection);
                    if (!inTransaction) {
                        systemRegistry.commitTransaction();
                    }
                } catch (Exception e) {
                    String msg = "Unable to setup system collections used by the Carbon server.";
                    log.error(msg, e);
                    systemRegistry.rollbackTransaction();
                    throw new RegistryException(e.getMessage(), e);
                }
            }

            try {
                AuthorizationManager ac = systemRegistry.getUserRealm().getAuthorizationManager();
                RealmConfiguration realmConfig;
                realmConfig = registryContext.getRealmService().getBootstrapRealmConfiguration();
                String systemUserName = CarbonConstants.REGISTRY_SYSTEM_USERNAME;


                ac.clearResourceAuthorizations("/system");

                ac.authorizeUser(systemUserName, "/system", ActionConstants.GET);
                ac.authorizeUser(systemUserName, "/system", ActionConstants.PUT);
                ac.authorizeUser(systemUserName, "/system", ActionConstants.DELETE);
                ac.authorizeUser(systemUserName, "/system", AccessControlConstants.AUTHORIZE);

                String adminUserName = CarbonConstants.REGISTRY_SYSTEM_USERNAME;

                ac.authorizeUser(adminUserName, "/system", ActionConstants.GET);

                String adminRoleName = realmConfig.getAdminRoleName();
                ac.authorizeRole(adminRoleName, "/system", ActionConstants.GET);

                // any user should be able to execute auto generated queries, though the results
                // of such queries are filtered to match current users permission level.
                String everyoneRoleName = realmConfig.getEveryOneRoleName();
                ac.authorizeRole(everyoneRoleName,
                        "/system/queries/advanced", ActionConstants.GET);

            } catch (UserStoreException e) {
                String msg = "Failed to set permissions for the system collection.";
                log.fatal(msg, e);
                throw new RegistryException(msg, e);
            }

            if (log.isTraceEnabled()) {
                log.trace("System collections for WSO2 Registry server created successfully.");
            }

            NodeGroupLock.unlock(NodeGroupLock.INITIALIZE_LOCK);

            // todo: we should make this decision according to the registry.xml or web.xml config
            //try {
            //    RemoteRegistry remote = new RemoteRegistry(new URL("http://localhost:8080/wso2registry/atom"));
            //    config.getServletContext().setAttribute(RegistryConstants.REGISTRY, remote);
            //} catch (MalformedURLException e) {
            //    config.getServletContext().setAttribute(RegistryConstants.REGISTRY, coreRegistry);
            //}

            config.getServletContext()
                    .setAttribute(RegistryConstants.REGISTRY, embeddedRegistryService);

            //config.getServletContext()
            //        .setAttribute(RegistryConstants.REGISTRY_REALM, userRealm);
            System.getProperties().put(RegistryConstants.REGISTRY, embeddedRegistryService);
            System.getProperties().put(RegistryConstants.SYSTEM_REGISTRY, systemRegistry);
            //System.getProperties().put(RegistryConstants.REGISTRY_REALM, userRealm);

        } catch (RegistryException e) {
            String msg = "Registry initialization failed. " + e.getMessage();
            log.fatal(msg, e);
            throw new ServletException(msg, e);
        }
        /*if (log.isDebugEnabled()) {
            log.debug(Messages.getMessage("server.initalized"));
        }*/
    }

    public void init() throws ServletException {
        if (servletConfig != null) {
            init(servletConfig);
        }
    }

    protected void doPost(HttpServletRequest httpServletRequest,
                          HttpServletResponse httpServletResponse)
            throws ServletException, IOException {
        initContextRoot(httpServletRequest);
        String uri = httpServletRequest.getRequestURI();
        String p1 = uri.substring(contextRoot.length(), uri.length());
        if (p1.equals("/web/addResource")) {
            handleFileUpload(httpServletRequest, httpServletResponse);
        }
    }

    private void handleFileUpload(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        try {
            Registry registry = Utils.getSecureRegistry(request);
            ResourceImpl fileElement =
                    FileUploadUtil.processUpload(request);
            String path = fileElement.getPath();
            registry.put(path, fileElement);
            request.getSession().setAttribute(RegistryConstants.STATUS_MESSAGE_NAME,
                    "Resource " + path +
                            " was successfully added to the registry.");
            response.setContentType("text/html");
            request.getRequestDispatcher(RegistryConstants.RESOURCES_JSP)
                    .forward(request, response);

        } catch (RegistryException e) {

            request.getSession().setAttribute(RegistryConstants.STATUS_MESSAGE_NAME,
                    e.getMessage());
            response.setContentType("text/html");
            request.getRequestDispatcher(RegistryConstants.RESOURCES_JSP)
                    .forward(request, response);
        }
    }

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {
        initContextRoot(request);

        String uri = URLDecoder.decode(request.getRequestURI(), RegistryConstants.DEFAULT_CHARSET_ENCODING);

        String controlPart = uri.substring(contextRoot.length());

        if (controlPart.startsWith(
                RegistryConstants.PATH_SEPARATOR + RegistryConstants.RESOURCES_PATH)) {

            String path;

            if (uri.equals("") || uri.endsWith(RegistryConstants.RESOURCES_PATH +
                    RegistryConstants.PATH_SEPARATOR)) {
                path = RegistryConstants.ROOT_PATH;
            } else {
                path = uri.substring((contextRoot + RegistryConstants.PATH_SEPARATOR +
                        RegistryConstants.RESOURCES_PATH).length(), uri.length());
            }

            // if user is browsing an old version of the resource, we append it to the path, so that
            // the backend registry gives the details of the version
            //String qPart = request.getQueryString();
            //if (qPart != null && qPart.startsWith("v")) {
            //    path = path + "?" + qPart;
            //}

            ResourceRequestProcessor.processResourceGET(request, response, path);
        }
    }

    /**
     * To find the name of the war , so getting that from the request context path
     *
     * @param req The Http Servlet Request
     */
    public void initContextRoot(HttpServletRequest req) {
        if (contextRoot != null && contextRoot.trim().length() != 0) {
            return;
        }
        String contextPath = req.getContextPath();
        //handling ROOT scenario, for servlets in the default (root) context, this method returns ""
        if (contextPath != null && contextPath.length() == 0) {
            contextPath = "/";
        }
        this.contextRoot = contextPath;
    }

    protected void renderView(String jspName,
                              HttpServletRequest httpServletRequest,
                              HttpServletResponse httpServletResponse)
            throws IOException, ServletException {
        httpServletResponse.setContentType("text/html");
        httpServletRequest.getRequestDispatcher(jspName)
                .include(httpServletRequest, httpServletResponse);
    }

//    private void setErrorMessage(HttpServletRequest request, String message) {
//        request.getSession().setAttribute(RegistryConstants.ERROR_MESSAGE, message);
//    }
}
