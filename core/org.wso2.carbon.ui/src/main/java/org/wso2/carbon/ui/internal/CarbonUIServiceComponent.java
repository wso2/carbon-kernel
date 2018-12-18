/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.ui.internal;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.catalina.Loader;
import org.apache.catalina.core.DefaultInstanceManager;
import org.apache.catalina.core.NamingContextListener;
import org.apache.catalina.core.StandardContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.InstanceManager;
import org.eclipse.equinox.http.helper.ContextPathServletAdaptor;
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

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.ContentHandler;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import static org.wso2.carbon.CarbonConstants.PRODUCT_XML;
import static org.wso2.carbon.CarbonConstants.PRODUCT_XML_PROPERTIES;
import static org.wso2.carbon.CarbonConstants.PRODUCT_XML_PROPERTY;
import static org.wso2.carbon.CarbonConstants.PRODUCT_XML_WSO2CARBON;
import static org.wso2.carbon.CarbonConstants.WSO2CARBON_NS;

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
    private static List<UIAuthenticationExtender> authenticationExtenders =
            new LinkedList<UIAuthenticationExtender>();

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
            if (adminConsoleURL != null) {
                log.info("Mgt Console URL  : " + adminConsoleURL);
            }

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

        final HttpService httpService = getHttpService();

        Dictionary<String, String> initparams = new Hashtable<String, String>();
        initparams.put("servlet-name", "TilesServlet");
        initparams.put("definitions-config", "/WEB-INF/tiles/main_defs.xml");
        initparams.put("org.apache.tiles.context.TilesContextFactory",
                       "org.apache.tiles.context.enhanced.EnhancedContextFactory");
        initparams.put("org.apache.tiles.factory.TilesContainerFactory.MUTABLE", "true");
        initparams.put("org.apache.tiles.definition.DefinitionsFactory",
                       "org.wso2.carbon.tiles.CarbonUrlDefinitionsFactory");

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
//        BundleResourcePathRegistry resourcePathRegistry = uiBundleDeployer.getBundleResourcePathRegistry();

        HttpContext commonContext =
                new CarbonSecuredHttpContext(context.getBundle(), "/web", uiResourceRegistry, registry);

        //Registering filedownload servlet
        Servlet fileDownloadServlet = new ContextPathServletAdaptor(new FileDownloadServlet(
                context, getConfigurationContextService()), "/filedownload");
        httpService.registerServlet("/filedownload", fileDownloadServlet, null, commonContext);
        fileDownloadServlet.getServletConfig().getServletContext().setAttribute(
                CarbonConstants.SERVER_URL, serverURL);
        fileDownloadServlet.getServletConfig().getServletContext().setAttribute(
                CarbonConstants.INDEX_PAGE_URL, indexPageURL);

        //Registering fileupload servlet
        Servlet fileUploadServlet;
        if (isLocalTransportMode) {
            fileUploadServlet = new ContextPathServletAdaptor(new FileUploadServlet(
                    context, serverConfigContext, webContext), "/fileupload");
        } else {
            fileUploadServlet = new ContextPathServletAdaptor(new FileUploadServlet(
                    context, clientConfigContext, webContext), "/fileupload");
        }

        httpService.registerServlet("/fileupload", fileUploadServlet, null, commonContext);
        fileUploadServlet.getServletConfig().getServletContext().setAttribute(
                CarbonConstants.SERVER_URL, serverURL);
        fileUploadServlet.getServletConfig().getServletContext().setAttribute(
                CarbonConstants.INDEX_PAGE_URL, indexPageURL);

        uiBundleDeployer.deploy(bundleContext, commonContext);
        context.addBundleListener(uiBundleDeployer);

        httpService.registerServlet("/", new org.apache.tiles.web.startup.TilesServlet(),
                                    initparams,
                                    commonContext);
        httpService.registerResources("/" + webContext, "/", commonContext);

        adaptedJspServlet = new ContextPathServletAdaptor(
                new TilesJspServlet(context.getBundle(), uiResourceRegistry), "/" + webContext);
        httpService.registerServlet("/" + webContext + "/*.jsp", adaptedJspServlet, null,
                                    commonContext);

        ServletContext jspServletContext =
                adaptedJspServlet.getServletConfig().getServletContext();

        jspServletContext.setAttribute(
                InstanceManager.class.getName(), getTomcatInstanceManager());

        jspServletContext.setAttribute("registry", registryService);

        jspServletContext.setAttribute(CarbonConstants.SERVER_CONFIGURATION, serverConfig);
        jspServletContext.setAttribute(CarbonConstants.CLIENT_CONFIGURATION_CONTEXT, clientConfigContext);
        //If the UI is running on local transport mode, then we use the server-side config context.
        if(isLocalTransportMode) {
            jspServletContext.setAttribute(CarbonConstants.CONFIGURATION_CONTEXT, serverConfigContext);
        } else {
            jspServletContext.setAttribute(CarbonConstants.CONFIGURATION_CONTEXT, clientConfigContext);
        }

        jspServletContext.setAttribute(CarbonConstants.BUNDLE_CLASS_LOADER,
                                       this.getClass().getClassLoader());
        jspServletContext.setAttribute(CarbonConstants.SERVER_URL, serverURL);
        jspServletContext.setAttribute(CarbonConstants.INDEX_PAGE_URL, indexPageURL);
        jspServletContext.setAttribute(CarbonConstants.UI_BUNDLE_CONTEXT, bundleContext);

        // set the CustomUIDefinitions object as an attribute of servlet context, so that the Registry UI bundle
        // can access the custom UI details within JSPs.
        jspServletContext
                .setAttribute(CustomUIDefenitions.CUSTOM_UI_DEFENITIONS, customUIDefenitions);

        // Registering jspServletContext as a service so that UI components can use it
        bundleContext.registerService(ServletContext.class.getName(), jspServletContext, null);

        //saving bundle context for future reference within CarbonUI Generation
        CarbonUIUtil.setBundleContext(context);
        UIAnnouncementDeployer.deployNotificationSources();

        if (log.isDebugEnabled()) {
            log.debug("Starting web console using context : " + webContext);
        }

        //read product.xml
        readProductXML(jspServletContext, uiBundleDeployer);
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

    /**
     * reads product.xml contained within styles bundle of a product.
     * product.xml contains properties like userforum, mailing list,etc.. which are
     * specific to the product.
     *
     * @param jspServletContext
     * @throws IOException
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     */
    private void readProductXML(ServletContext jspServletContext, UIBundleDeployer uiBundleDeployer)
            throws IOException, XMLStreamException {
        Enumeration<URL> e = bundleContext.getBundle().findEntries("META-INF", PRODUCT_XML, true);
        // it is possible to make the styles bundle a UIBundle. But in that case we have to get the
        // product.xml file using BundleBasedUIResourceFinder. However product.xml file is not a UI
        // resource. rather it is  a config file. Hence I'm making this a fragment bundle.
        // actually styles bundle should be a fragment of carbon UI. -- Pradeep
        /*Bundle stylesBundle = ((BundleBasedUIResourceProvider) uiBundleDeployer.getBundleBasedUIResourcePrvider()).getBundle(CarbonConstants.PRODUCT_STYLES_CONTEXT);
        if (stylesBundle != null) {
            e = stylesBundle.findEntries("META-INF", PRODUCT_XML, true);
        }*/
        if (e != null) {
            URL url = (URL) e.nextElement();
            InputStream inputStream = url.openStream();
            XMLStreamReader streamReader =
                    XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(streamReader);
            OMElement document = builder.getDocumentElement();
            OMElement propsEle =
                    document.getFirstChildWithName(
                            new QName(WSO2CARBON_NS, PRODUCT_XML_PROPERTIES));
            if (propsEle != null) {
                Iterator<OMElement> properties = propsEle.getChildrenWithName(
                        new QName(WSO2CARBON_NS, PRODUCT_XML_PROPERTY));
                while (properties.hasNext()) {
                    OMElement property = properties.next();
                    String propertyName = property.getAttributeValue(new QName("name"));
                    String value = property.getText();
                    if (log.isDebugEnabled()) {
                        log.debug(PRODUCT_XML + ": " + propertyName + "=" + value);
                    }
                    //process collapsed menus in a different manner than other properties
                    if ("collapsedmenus".equals(propertyName)) {
                        ArrayList<String> collapsedMenuItems = new ArrayList<String>();
                        if (value != null && value.indexOf(',') > -1) {
                            //multiple menu items provided.Tokenize & add iteratively
                            StringTokenizer st = new StringTokenizer(value, ",");
                            while (st.hasMoreTokens()) {
                                collapsedMenuItems.add(st.nextToken());
                            }
                        } else {
                            //single menu item specified.add this
                            collapsedMenuItems.add(value);
                        }
                        jspServletContext.setAttribute(PRODUCT_XML_WSO2CARBON + propertyName, collapsedMenuItems);

                        /*
                        Sometimes the values loaded to the jspServletContext is not available.
                        i.e. when the request is sent to /carbon
                        it works only if teh request takes the patern such as /carbon/admin/index.jsp
                        in the case of /carbon the params are read from utils hashmap which is saved at this point.
                         */
                        CarbonUIUtil.setProductParam(PRODUCT_XML_WSO2CARBON + propertyName, collapsedMenuItems);
                    } else {
                        jspServletContext.setAttribute(PRODUCT_XML_WSO2CARBON + propertyName, value);
                        CarbonUIUtil.setProductParam(PRODUCT_XML_WSO2CARBON + propertyName, value);
                    }
                }
            }
        }
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
