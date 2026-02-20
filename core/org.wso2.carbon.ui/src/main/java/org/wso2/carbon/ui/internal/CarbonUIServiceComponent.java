/*
 * Copyright (c) 2005-2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.ui.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.catalina.Loader;
import org.apache.catalina.core.DefaultInstanceManager;
import org.apache.catalina.core.NamingContextListener;
import org.apache.catalina.core.StandardContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.InstanceManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.context.ServletContextHelper;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.tomcat.api.CarbonTomcatService;
import org.wso2.carbon.ui.BasicAuthUIAuthenticator;
import org.wso2.carbon.ui.CarbonProtocol;
import org.wso2.carbon.ui.CarbonSSOSessionManager;
import org.wso2.carbon.ui.CarbonSecuredHttpContext;
import org.wso2.carbon.ui.CarbonServletContextInitializer;
import org.wso2.carbon.ui.CarbonUIAuthenticator;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.ui.DefaultCarbonAuthenticator;
import org.wso2.carbon.ui.TextJavascriptHandler;
import org.wso2.carbon.ui.TilesJspServlet;
import org.wso2.carbon.ui.UIAuthenticationExtender;
import org.wso2.carbon.ui.UIResourceRegistry;
import org.wso2.carbon.ui.deployment.UIBundleDeployer;
import org.wso2.carbon.ui.deployment.beans.CarbonUIDefinitions;
import org.wso2.carbon.ui.deployment.beans.Context;
import org.wso2.carbon.ui.deployment.beans.CustomUIDefenitions;
import org.wso2.carbon.ui.tracker.AuthenticatorRegistry;
import org.wso2.carbon.ui.transports.FileDownloadServlet;
import org.wso2.carbon.ui.transports.FileUploadServlet;
import org.wso2.carbon.ui.util.UIAnnouncementDeployer;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.net.ContentHandler;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletContextListener;

@Component(name = "core.ui.dscomponent", immediate = true)
public class CarbonUIServiceComponent {

    private static Log log = LogFactory.getLog(CarbonUIServiceComponent.class);

    private static PackageAdmin packageAdminInstance;
    private static RegistryService registryServiceInstance;
    private static HttpService httpServiceInstance;
    private static ConfigurationContextService ccServiceInstance;
    private static ServerConfigurationService serverConfiguration;
    private static RealmService realmService;
    private static CarbonTomcatService carbonTomcatService;
    private static List<UIAuthenticationExtender> authenticationExtenders = new LinkedList<>();

    private BundleContext bundleContext;

    private Servlet adaptedJspServlet;
    
    @Activate
    protected void activate(ComponentContext ctxt) {
        try {
            start(ctxt.getBundleContext());
            String webContextRoot = serverConfiguration.getFirstProperty("WebContextRoot");
            if (webContextRoot == null || webContextRoot.isEmpty()) {
                throw new RuntimeException(
                        "WebContextRoot can't be null or empty. It should be either '/' or '/[some value]'");
            }
            String adminConsoleURL = CarbonUIUtil.getAdminConsoleURL(webContextRoot);

            //Retrieving available contexts
            ServiceReference reference =
                    ctxt.getBundleContext().getServiceReference(CarbonUIDefinitions.class.getName());
            CarbonUIDefinitions carbonUIDefinitions = null;
            if (reference != null) {
                carbonUIDefinitions = (CarbonUIDefinitions) ctxt.getBundleContext().getService(reference);
                if (carbonUIDefinitions != null && carbonUIDefinitions.getContexts() != null) {
                    //Get the default context URL
                    if ("/".equals(webContextRoot)) {
                        webContextRoot = "";
                    }
                    int index = adminConsoleURL.lastIndexOf("carbon");
                    String defContextUrl = adminConsoleURL.substring(0, index);
                    //Remove the custom WebContextRoot from URL
                    if (!"".equals(webContextRoot)) {
                        defContextUrl = defContextUrl.replace(webContextRoot, "");
                    }

                    //Print additional URLs
                    for (String key : carbonUIDefinitions.getContexts().keySet()) {
                        printAdditionalContext(carbonUIDefinitions.getContexts().get(key), defContextUrl);
                    }
                }
            }

            DefaultCarbonAuthenticator authenticator = new DefaultCarbonAuthenticator();
            Hashtable<String, String> props = new Hashtable<String, String>();
            props.put(AuthenticatorRegistry.AUTHENTICATOR_TYPE, authenticator.getAuthenticatorName());
            ctxt.getBundleContext().registerService(CarbonUIAuthenticator.class.getName(), authenticator, props);
            
            BasicAuthUIAuthenticator basicAuth = new BasicAuthUIAuthenticator();
            props = new Hashtable<String, String>();
            props.put(AuthenticatorRegistry.AUTHENTICATOR_TYPE, authenticator.getAuthenticatorName());
            ctxt.getBundleContext().registerService(CarbonUIAuthenticator.class.getName(), basicAuth, props);

            AuthenticatorRegistry.init(ctxt.getBundleContext());

            // register a SSOSessionManager instance as an OSGi Service.
            ctxt.getBundleContext().registerService(CarbonSSOSessionManager.class.getName(),
                                                     CarbonSSOSessionManager.getInstance(), null);
            log.debug("Carbon UI bundle is activated ");
        } catch (Throwable e) {
            log.error("Failed to activate Carbon UI bundle ", e);
        }
    }

    private void printAdditionalContext(Context additionalContext, String defContextRoot) {
        if (additionalContext != null && !"".equals(additionalContext.getContextName()) &&
            !"null".equals(additionalContext.getContextName())) {
            String defContextUrl = defContextRoot + additionalContext.getContextName();

            if (additionalContext.getDescription() != null) {
                if (additionalContext.getProtocol() != null && "http".equals(additionalContext.getProtocol())) {
                    log.info(additionalContext.getDescription() + " : " + CarbonUIUtil.https2httpURL(defContextUrl));
                } else {
                    log.info(additionalContext.getDescription() + " : " + defContextUrl);
                }
            } else {
                log.info("Default Context : " + defContextUrl);
            }
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {
        log.debug("Carbon UI bundle is deactivated ");
    }

    public static CarbonTomcatService getCarbonTomcatService() {
        return carbonTomcatService;
    }

    public void start(BundleContext context) throws Exception {
        this.bundleContext = context;

        ServerConfigurationService serverConfig = getServerConfiguration();

        boolean isLocalTransportMode = checkForLocalTransportMode(serverConfig);
        //TODO set a system property

        ConfigurationContext clientConfigContext = getConfigurationContextService().getClientConfigContext();
        //This is applicable only when the FE and BE runs in the same JVM.
        ConfigurationContext serverConfigContext = getConfigurationContextService().getServerConfigContext();

        CarbonUIDefinitions carbonUIDefinitions = new CarbonUIDefinitions();
        context.registerService(CarbonUIDefinitions.class.getName(), carbonUIDefinitions, null);

        // create a CustomUIDefinitions object and set it as a osgi service. UIBundleDeployer can access this
        // service and populate it with custom UI definitions of the deployed UI bundle, if available.
        CustomUIDefenitions customUIDefenitions = new CustomUIDefenitions();
        context.registerService(CustomUIDefenitions.class.getName(), customUIDefenitions, null);

        Hashtable<String, String[]> properties1 = new Hashtable<String, String[]>();
        properties1.put(URLConstants.URL_HANDLER_PROTOCOL, new String[]{"carbon"});
        context.registerService(URLStreamHandlerService.class.getName(),
                                new CarbonProtocol(context), properties1);

        Hashtable<String, String[]> properties3 = new Hashtable<String, String[]>();
        properties3.put(URLConstants.URL_CONTENT_MIMETYPE, new String[]{"text/javascript"});
        context.registerService(ContentHandler.class.getName(), new TextJavascriptHandler(),
                                properties3);

        String webContext = "carbon"; // The subcontext for the Carbon Mgt Console

        String serverURL = CarbonUIUtil.getServerURL(serverConfig);
        String indexPageURL = CarbonUIUtil.getIndexPageURL(serverConfig);
        if (indexPageURL == null) {
            indexPageURL = "/carbon/admin/index.jsp";
        }
        RegistryService registryService = getRegistryService();
        Registry registry = registryService.getLocalRepository();

        UIBundleDeployer uiBundleDeployer = new UIBundleDeployer();
        UIResourceRegistry uiResourceRegistry = new UIResourceRegistry();
        uiResourceRegistry.initialize(bundleContext);
        uiResourceRegistry.setDefaultUIResourceProvider(
                uiBundleDeployer.getBundleBasedUIResourcePrvider());

        HttpContext commonContext =
                new CarbonSecuredHttpContext(context.getBundle(), "/web", uiResourceRegistry, registry);

        uiBundleDeployer.deploy(bundleContext, commonContext);
        context.addBundleListener(uiBundleDeployer);

        // Register ServletContextHelper at / path - all servlets/filters/resources use this single context
        // This ensures forwarding works correctly between components
        Dictionary<String, String> contextProps = new Hashtable<>();
        contextProps.put("osgi.http.whiteboard.context.name", "carbonContext");
        contextProps.put("osgi.http.whiteboard.context.path", "/");
        context.registerService(ServletContextHelper.class, (ServletContextHelper) commonContext, contextProps);

        // Register file download servlet using HTTP Whiteboard pattern
        Servlet fileDownloadServlet = new FileDownloadServlet(context, getConfigurationContextService());
        Dictionary<String, String> fileDownloadServletProperties = new Hashtable<>();
        fileDownloadServletProperties.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN, 
                "/carbon/filedownload/*");
        fileDownloadServletProperties.put("osgi.http.whiteboard.context.select", 
                "(osgi.http.whiteboard.context.name=carbonContext)");
        context.registerService(Servlet.class, fileDownloadServlet, fileDownloadServletProperties);

        // Register file upload servlet using HTTP Whiteboard pattern
        Servlet fileUploadServlet;
        if (isLocalTransportMode) {
            fileUploadServlet = new FileUploadServlet(context, serverConfigContext, webContext);
        } else {
            fileUploadServlet = new FileUploadServlet(context, clientConfigContext, webContext);
        }
        Dictionary<String, String> fileUploadServletProperties = new Hashtable<>();
        fileUploadServletProperties.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN, 
                "/carbon/fileupload/*");
        fileUploadServletProperties.put("osgi.http.whiteboard.context.select", 
                "(osgi.http.whiteboard.context.name=carbonContext)");
        context.registerService(Servlet.class, fileUploadServlet, fileUploadServletProperties);

        // Register static resources using HTTP Whiteboard with carbonContext
        // Pattern /carbon/* will serve static files under /carbon/
        Dictionary<String, Object> resourceProperties = new Hashtable<>();
        resourceProperties.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_RESOURCE_PATTERN, "/carbon/*");
        resourceProperties.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_RESOURCE_PREFIX, "/");
        resourceProperties.put("osgi.http.whiteboard.context.select", 
                "(osgi.http.whiteboard.context.name=carbonContext)");
        bundleContext.registerService(Object.class, new Object(), resourceProperties);

        adaptedJspServlet = new TilesJspServlet(context.getBundle(), uiResourceRegistry);

        // Register TilesJspServlet with carbonContext for /carbon/*.jsp pattern
        Dictionary<String, String> carbonInitparams = new Hashtable<>();
        carbonInitparams.put("servlet.init.strictQuoteEscaping", "false");
        carbonInitparams.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN, "/carbon/*.jsp");
        carbonInitparams.put("osgi.http.whiteboard.context.select", "(osgi.http.whiteboard.context.name=carbonContext)");
        context.registerService(Servlet.class, adaptedJspServlet, carbonInitparams);

        // Determine which configuration context to use based on transport mode
        ConfigurationContext contextToUse = isLocalTransportMode ? serverConfigContext : clientConfigContext;
        
        // Create and register ServletContextListener to initialize the ServletContext when it's created
        CarbonServletContextInitializer contextInitializer = new CarbonServletContextInitializer(
                getTomcatInstanceManager(),
                registryService,
                serverConfig,
                contextToUse,
                clientConfigContext,
                bundleContext,
                serverURL,
                indexPageURL,
                customUIDefenitions,
                this.getClass().getClassLoader(),
                uiBundleDeployer
        );
        
        Dictionary<String, String> listenerProps = new Hashtable<>();
        listenerProps.put("osgi.http.whiteboard.context.select", "(osgi.http.whiteboard.context.name=carbonContext)");
        listenerProps.put("osgi.http.whiteboard.listener", "true");
        context.registerService(ServletContextListener.class, contextInitializer, listenerProps);

        Dictionary<String, String> listenerPropsForDefaultContext = new Hashtable<>();
        listenerPropsForDefaultContext.put("osgi.http.whiteboard.listener", "true");
        context.registerService(ServletContextListener.class, contextInitializer, listenerPropsForDefaultContext);

        //saving bundle context for future reference within CarbonUI Generation
        CarbonUIUtil.setBundleContext(context);
        UIAnnouncementDeployer.deployNotificationSources();

        if (log.isDebugEnabled()) {
            log.debug("Starting web console using context : " + webContext);
        }
    }

    private InstanceManager getTomcatInstanceManager() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        String webContextRoot = getServerConfiguration().getFirstProperty("WebContextRoot");
        NamingContextListener ncl = new NamingContextListener();
        ncl.setName("/Catalina/localhost" + webContextRoot);
        ncl.setExceptionOnFailedWrite(true);
        javax.naming.Context context = ncl.getEnvContext();

        Map<String, Map<String, String>> injectionMap = new HashMap<String, Map<String, String>>();
        ClassLoader containerClassloader = this.getClass().getClassLoader();

        //create a dummy context, and use it to create the tomcat InstanceManager instance.
        org.apache.catalina.Context dummyCtx = new StandardContext();
        dummyCtx.setName("carbon");
        dummyCtx.setPath(webContextRoot);
        dummyCtx.setDocBase(System.getProperty("java.io.tmpdir"));

        dummyCtx.setLoader((Loader) Class.forName("org.apache.catalina.loader.WebappLoader").newInstance());

        return new DefaultInstanceManager(context, injectionMap, dummyCtx, containerClassloader);
    }

    public static synchronized Bundle getBundle(Class clazz) {
        if (packageAdminInstance == null) {
            throw new IllegalStateException("Not started");
        } //$NON-NLS-1$
        return packageAdminInstance.getBundle(clazz);
    }
    
    @Reference(name = "config.context.service", cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC,
    unbind = "unsetConfigurationContextService")
    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        ccServiceInstance = contextService;
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        ccServiceInstance = null;
    }
    
    @Reference(name = "tomcat.service.provider", cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC,
    unbind = "unsetCarbonTomcatService")
    protected void setCarbonTomcatService(CarbonTomcatService contextService) {
        carbonTomcatService = contextService;
    }

    protected void unsetCarbonTomcatService(CarbonTomcatService contextService) {
        carbonTomcatService = null;
    }
    
    @Reference(name = "registry.service", cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registryService) {
        registryServiceInstance = registryService;
    }

    protected void unsetRegistryService(RegistryService registryService) {
        registryServiceInstance = null;
    }

    @Reference(name = "server.configuration", cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetServerConfigurationService")
    protected void setServerConfigurationService(ServerConfigurationService serverConfiguration) {
        CarbonUIServiceComponent.serverConfiguration = serverConfiguration;
    }

    protected void unsetServerConfigurationService(ServerConfigurationService serverConfiguration) {
        CarbonUIServiceComponent.serverConfiguration = null;
    }

    @Reference(name = "package.admin", cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetPackageAdmin")
    protected void setPackageAdmin(PackageAdmin packageAdmin) {
        packageAdminInstance = packageAdmin;
    }

    protected void unsetPackageAdmin(PackageAdmin packageAdmin) {
        packageAdminInstance = null;
    }

    @Reference(name = "http.service", cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetHttpService")
    protected void setHttpService(HttpService httpService) {
        httpServiceInstance = httpService;
    }

    protected void unsetHttpService(HttpService httpService) {
        httpServiceInstance = null;
    }

    @Reference(name = "user.realmservice.default", cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {
        CarbonUIServiceComponent.realmService = realmService;
    }

    protected void unsetRealmService(RealmService realmService) {
        CarbonUIServiceComponent.realmService = null;
    }

    public static RealmService getRealmService() {
        return realmService;
    }

    @Reference(name = "ui.authentication.extender", cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, 
            unbind = "unsetUIAuthenticationExtender")
    protected void setUIAuthenticationExtender(UIAuthenticationExtender authenticationExtender) {
        CarbonUIServiceComponent.authenticationExtenders.add(authenticationExtender);
    }

    protected void unsetUIAuthenticationExtender(UIAuthenticationExtender authenticationExtender) {
        CarbonUIServiceComponent.authenticationExtenders.remove(authenticationExtender);
    }

    public static UIAuthenticationExtender[] getUIAuthenticationExtenders() {
        return authenticationExtenders.toArray(
                new UIAuthenticationExtender[authenticationExtenders.size()]);
    }

    public static HttpService getHttpService() {
        if (httpServiceInstance == null) {
            String msg = "Before activating Carbon UI bundle, an instance of "
                         + HttpService.class.getName() + " should be in existence";
            log.error(msg);
            throw new RuntimeException(msg);
        }
        return httpServiceInstance;
    }

    public static ConfigurationContextService getConfigurationContextService() {
        if (ccServiceInstance == null) {
            String msg = "Before activating Carbon UI bundle, an instance of "
                         + "UserRealm service should be in existence";
            log.error(msg);
            throw new RuntimeException(msg);
        }
        return ccServiceInstance;
    }

    public static RegistryService getRegistryService() {
        if (registryServiceInstance == null) {
            String msg = "Before activating Carbon UI bundle, an instance of "
                         + "RegistryService should be in existence";
            log.error(msg);
            throw new RuntimeException(msg);
        }
        return registryServiceInstance;
    }

    public static ServerConfigurationService getServerConfiguration() {
        if (serverConfiguration == null) {
            String msg = "Before activating Carbon UI bundle, an instance of "
                         + "ServerConfiguration Service should be in existence";
            log.error(msg);
            throw new RuntimeException(msg);
        }
        return serverConfiguration;
    }

    public static PackageAdmin getPackageAdmin() throws Exception {
        if (packageAdminInstance == null) {
            String msg = "Before activating Carbon UI bundle, an instance of "
                         + "PackageAdmin Service should be in existance";
            log.error(msg);
            throw new Exception(msg);
        }
        return packageAdminInstance;
    }

    /**
     * This method checks whether the management console is configured to run on the local transport.
     * Check the ServerURL property in the carbon.xml.
     * Set a system property if and only if the system is running on local transport.
     * 
     * @param serverConfiguration Service configuration.
     * @return boolean; true if running on local transport
     */
    private boolean checkForLocalTransportMode(ServerConfigurationService serverConfiguration) {
        String serverURL = serverConfiguration.getFirstProperty(CarbonConstants.SERVER_URL);
        if(serverURL != null && (serverURL.startsWith("local") ||
                serverURL.startsWith("Local") || serverURL.startsWith("LOCAL"))) {
            System.setProperty(CarbonConstants.LOCAL_TRANSPORT_MODE_ENABLED, "true");
            return true;
        }
        return false;
    }
}
