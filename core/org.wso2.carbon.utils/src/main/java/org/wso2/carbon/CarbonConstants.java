/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
package org.wso2.carbon;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.File;

/**
 *  This class contains all the significant constants used by Carbon core & Carbon components
 */
public final class CarbonConstants {

	private CarbonConstants() {
	    //disable external instantiation
	}

    public static final Log AUDIT_LOG = LogFactory.getLog("AUDIT_LOG");

    /**
     * This is used to get root context within CarbonJNDIContext when we need to operate
     * with LDAP.
     */
    public static final String REQUEST_BASE_CONTEXT = "org.wso2.carbon.context.RequestBaseContext";

    public static final String REGISTRY_SYSTEM_USERNAME = "wso2.system.user";
    
    public static final String REGISTRY_ANONNYMOUS_USERNAME = "wso2.anonymous.user";
    public static final String REGISTRY_ANONNYMOUS_ROLE_NAME = "system/wso2.anonymous.role";

    public static final String UI_PERMISSION_NAME = "permission";
    public static final String UI_PERMISSION_COLLECTION = "/" + UI_PERMISSION_NAME;
    public static final String UI_ADMIN_PERMISSION_COLLECTION = UI_PERMISSION_COLLECTION + "/admin";
    public static final String UI_PROTECTED_PERMISSION_COLLECTION = UI_PERMISSION_COLLECTION + "/protected";
    public static final String UI_PERMISSION_ACTION = "ui.execute";
    public static final String UI_USER_PERMISSIONS = "user-permissions";

    public static final String AUTHZ_FAULT_CODE = "WSO2CarbonAuthorizationFailure";
    public static final String MODULE_NOT_FOUND_FAULT_CODE = "Axis2ModuleNotFound";


    public static final String START_TIME = "wso2carbon.start.time";
    public static final String START_UP_DURATION = "wso2carbon.startup.duration";

    public static final String COMMAND_EXT = ".command";
    public static final String WSO2CARBON_NS = "http://products.wso2.org/carbon";
    public static final String COMPONENT_XML = "component.xml";
    public static final String UI_FILE = "ui";

    public static final String COMPONENT_ELE = "component";
    public static final String TAG_LIBS_ELE = "taglibs";
    public static final String TAG_LIB_ELE = "taglib";
    public static final String JS_FILES_ELE = "js-files";
    public static final String JS_FILE_ELE = "js-file";

    public static final String URL_ATTR = "url";
    public static final String PREFIX_ATTR = "prefix";

    public static final String GENERAL_ELE = "general";

    public static final String MENUS_ELE = "menus";
    public static final String MENUE_ELE = "menu";
    //ADD MENUS is used when component is in RESOLVE/INSTALLED phase
    //REMOVE MENUS is used when component is being UNINSTALLED/STOPPED
//    public static final String ADD_MENUS = "add-menus";
//    public static final String REMOVE_MENUS = "remove-menus";

    public static final String ADD_UI_COMPONENT = "add-ui-component";
    public static final String REMOVE_UI_COMPONENT = "remove-ui-component";

    public static final String ACTION_REF_ATTR = "action-ref";
    public static final String LEVEL_ATTR = "level";
    public static final String ACTION_ELE = "action";
    public static final String ACTIONS_ELE = "actions";
    public static final String VIEW_ATTR = "view";
    public static final String CLASS_ATTR = "class";
    public static final String METHOD_ATTR = "method";
    public static final String METHOD_ATTR_DEFAULT_VALUE = "execute";

    public static final String RELATIVE_TEMPLATE_LOCATION = "repository" + File.separator + "conf" + File.separator + "templates";

    public static final String HEADER_XSL = "header.xsl";
    public static final String INDEX_XSL = "index.xsl";
    public static final String MENU_XSL = "menue.xsl";

    public static final String NAME_ATTR = "name";

    public static final String HTTP_TRANSPORT = "http";
    public static final String HTTPS_TRANSPORT = "https";

    public static final String HOST_NAME = "host-name";

    public static final String KEY_REGISTRY_INSTANCE = "WSO2Registry";

    public static final String WEB_CONTEXT = "WebContext";

    //Remember me
    public static final String REMEMBER_ME_COOKIE_VALUE = "wso2.carbon.rememberme.value";
    public static final String REMEMBER_ME_COOKIE_AGE = "wso2.carbon.rememberme.age";
    public static final String REMEMBER_ME_COOKE_NAME = "wso2.carbon.rememberme";
    public static final int REMEMBER_ME_COOKIE_TTL = 604800; //in seconds // 7 days
    
    //Names of Attributes that are set in the ServletContext
    public static final String CONFIGURATION_CONTEXT = "ConfigurationContext";
    public static final String CLIENT_CONFIGURATION_CONTEXT = "ClientConfigurationContext";
    public static final String SERVER_CONFIGURATION = "ServerConfiguration";

    //To mark whether the UI framewrok in running on local transport mode.
    public static final String LOCAL_TRANSPORT_MODE_ENABLED = "localTransportModeEnabled";    

    public static final String SERVER_URL = "ServerURL";
    public static final String INDEX_PAGE_URL = "IndexPageURL";
    public static final String DEFAULT_HOME_PAGE = "defaultHomePage";
    public static final String REGISTRY = "registry";
    public static final String BUNDLE_CLASS_LOADER = "BundleClassLoader";
    public static final String FEATURE_REPO_URL = "FeatureRepository.RepositoryURL";

    public static final String ADMIN_SERVICE_COOKIE = "wso2carbon.admin.service.cookie";

    //Constants used in File Uploading
    public static final String FILE_UPLOAD_CONFIG = "FileUploadConfig";
    public static final String MAPPING = "Mapping";
    public static final String ACTIONS = "Actions";
    public static final String ACTION = "Action";
    public static final String CLASS = "Class";

    //Constants used for servlet definition in component.xml
    public static final String SERVLETS = "servlets";
    public static final String SERVLET = "servlet";
    public static final String SERVLET_ID = "id";
    public static final String SERVLET_NAME = "servlet-name";
    public static final String SERVLET_DISPLAY_NAME = "display-name";
    public static final String SERVLET_URL_PATTERN = "url-pattern";
    public static final String SERVLET_ATTRIBUTES = "servlet-attribute";
    public static final String ASSOCIATED_FILTER = "associated-filter";
    public static final String SERVLET_PARAMS = "servlet-params";
    public static final String SERVLET_CLASS = "servlet-class";
    public static final String ADD_SERVLET = "add-servlet";
    public static final String REMOVE_SERVLET = "remove-servlet";

    //Constants useing for framework config
    public static final String FRAMEWORK_CONFIG = "framework-configuration";
    public static final String BYPASS = "bypass";
    public static final String AUTHENTICATION = "authentication";
    public static final String LINK = "link";
    public static final String TILES = "tiles";
    public static final String HTTP_URLS = "httpUrls";

    // Ghost Deployment related constants
    public static final String GHOST_REGISTRY = "ghostRegistry";
    public static final String GHOST_DEPLOYER = "ghostDeployer";
    public static final String GHOST_SERVICE_PARAM = "GhostService";
    public static final String GHOST_SERVICES_FOLDER = "ghostServices";
    public static final String GHOST_SERVICE_GROUP = "serviceGroup";
    public static final String GHOST_SERVICE = "service";
    public static final String GHOST_ATTR_NAME = "name";
    public static final String GHOST_ATTR_SERVICE_TYPE = "serviceType";
    public static final String GHOST_ATTR_MEP = "mep";
    public static final String GHOST_ATTR_SECURITY_SCENARIO = "securityScenario";
    public static final String GHOST_SERVICE_OPERATIONS = "operations";
    public static final String GHOST_SERVICE_ENDPOINTS = "endpoints";

    public static final String SERVICE_DEPLOYMENT_TIME_PARAM = "serviceDeploymentTime";

    public static final String SERVICE_LAST_USED_TIME = "lastUsedTime";
    public static final String GHOST_WEBAPP_PARAM = "GhostWebApp";
    public static final String GHOST_WEBAPPS_FOLDER = "ghostWebapps";
    public static final String WEB_APP_LAST_USED_TIME = "lastUsedTime";
    public static final String GHOST_WEBAPP = "webApplication";
    public static final String GHOST_ATTR_WEBAPP_CONTEXT_PATH = "contextPath";
    public static final String GHOST_ATTR_WEBAPP_DISPLAY_NAME = "displayName";
    public static final String GHOST_ATTR_WEBAPP_FILE = "file";
    public static final String GHOST_ATTR_LAST_MODIFIED_TIME = "lastModifiedTime";
    public static final String GHOST_ATTR_WEBAPP_SESSIONS = "sessions";
    public static final String TOMCAT_GENERIC_WEBAPP_DEPLOYER = "tomcatGenericWebappsDeplyer";

    public static final String SERVICES_HOTDEPLOYMENT_DIR = "axis2services";
    public static final String MODULES_DEPLOYMENT_DIR = "axis2modules";
    public static final String JS_SERVICES_HOTDEPLOYMENT_DIR = "jsservices";
    public static final String DSS_SERVICES_HOTDEPLOYMENT_DIR = "dataservices";

    @Deprecated
    public static final String SERVICE_METAFILE_HOTDEPLOYMENT_DIR = "servicemetafiles";
    @Deprecated
    public static final String MODULE_METAFILE_HOTDEPLOYMENT_DIR = "modulemetafiles";
    public static final String GHOST_METAFILE_DIR = "ghostmetafiles";
    public static final String WEBAPP_DEPLOYMENT_FOLDER = "webapps";
    public static final String JAX_WEBAPP_DEPLOYMENT_FOLDER = "jaxwebapps";
    public static final String JAGGERY_WEBAPP_DEPLOYMENT_FOLDER = "jaggeryapps";
    public static final String TENANTS_REPO = "repository" + File.separator + "tenants";

    /**
     * The allowed idle time for a service deployed
     */
    public static final String SERVICE_IDLE_TIME = "service.idle.time";
    public static final int SERVICE_CLEANUP_PERIOD_SECS = 60;

    // parameter to indicate whether a service/webapp is being unloaded and loaded as ghost
    public static final String IS_ARTIFACT_BEING_UNLOADED = "isBeingUnloaded";
    /**
     * The allowed idle time for a webapp deployed
     */
    public static final String WEBAPP_IDLE_TIME = "webapp.idle.time";
    public static final int WEBAPP_CLEANUP_PERIOD_SECS = 60;

    /**
     * Map to hold the session managers of webapps which will be called from session replication
     * message to execute the received cluster message
     */
    public static final String TOMCAT_SESSION_MANAGER_MAP = "CarbonTomcatSessionManagerMap";

    //Constants useing for context config
    public static final String CONTEXTS = "contexts";
    public static final String CONTEXT = "context";
    public static final String CONTEXT_ID = "context-id";
    public static final String CONTEXT_NAME = "context-name";
    public static final String PROTOCOL = "protocol";
    public static final String DESCRIPTION = "description";

    public static final String ADMIN_SERVICE_PARAM_NAME = "adminService";
    public static final String HIDDEN_SERVICE_PARAM_NAME = "hiddenService";
    public static final String DYNAMIC_SERVICE_PARAM_NAME = "dynamicService";
    public static final String ADMIN_MODULE_PARAM_NAME = "adminModule";
    public static final String MANAGED_MODULE_PARAM_NAME = "managedModule";

    //Constants used for menu definition in component.xml
    public static final String REQUIRE_PERMISSION = "require-permission";
    public static final String REQUIRE_NOT_LOGGED_IN = "require-not-logged-in";

    public static final String THEME_URL_RANDOM_SUFFIX_SESSION_KEY = "theme-suffix";

    //multi tenant related details

    /**
     * @deprecated use MultitenantConstants.REQUIRE_SUPER_TENANT
     */
    @Deprecated
    public static final String REQUIRE_SUPER_TENANT = MultitenantConstants.REQUIRE_SUPER_TENANT;
    /**
     * @deprecated use MultitenantConstants.REQUIRE_NOT_SUPER_TENANT
     */
    @Deprecated
    public static final String REQUIRE_NOT_SUPER_TENANT = MultitenantConstants.REQUIRE_NOT_SUPER_TENANT;
    /**
     * @deprecated use MultitenantConstants.TENANT_DOMAIN
     */
    @Deprecated
    public static final String TENANT_DOMAIN = MultitenantConstants.TENANT_DOMAIN;
    /**
     * @deprecated use MultitenantConstants.TENANT_AWARE_URL_PREFIX
     */
    @Deprecated
    public static final String TENANT_AWARE_URL_PREFIX = MultitenantConstants.TENANT_AWARE_URL_PREFIX;
    /**
     * @deprecated use MultitenantConstants.SUPER_TENANT_ID
     */
    @Deprecated
    public static final int SUPER_TENANT_ID = MultitenantConstants.SUPER_TENANT_ID;
    /**
     * @deprecated use MultitenantConstants.TENANT_DOMAIN_HEADER_NAMESPACE
     */
    @Deprecated
    public static final String TENANT_DOMAIN_HEADER_NAMESPACE = MultitenantConstants.TENANT_DOMAIN_HEADER_NAMESPACE;
    /**
     * @deprecated use MultitenantConstants.TENANT_DOMAIN_HEADER_NAME
     */
    @Deprecated
    public static final String TENANT_DOMAIN_HEADER_NAME = MultitenantConstants.TENANT_DOMAIN_HEADER_NAME;
    /**
     * @deprecated use MultitenantConstants.SUPER_TENANT_DOMAIN
     */
   
    public static final String NAME_REGULAR_EXPRESSION = "^[^~!@#$;%^*+={}\\|\\\\<>]{3,30}$";
    //Axis2 related constants.
    public static final String AXIS2_CONFIG_SERVICE = "org.apache.axis2.osgi.config.service";
    public static final String AXIS2_WS = "org.apache.axis2.osgi.ws";
    public static final int POLICY_ADDED = -1;
    public static final String HTTP_GET_REQUEST_PROCESSOR_SERVICE = "org.wso2.carbon.osgi.httpGetRequestProcessorService";

    // A Map to store the faulty services in the Axis2 ConfigurationContext
    public static final String FAULTY_SERVICES_MAP = "local_carbon.faulty.services.map";

    public static final String KEEP_SERVICE_HISTORY_PARAM = "keepServiceHistory";
    public static final String PRESERVE_SERVICE_HISTORY_PARAM = "preserveServiceHistory";

    /**
     * The Carbon UI bundle context
     */
    public static final String UI_BUNDLE_CONTEXT = "carbon.ui.bundle.context";

    public static final String SERVER_START_TIME = "wso2carbon.server.start.time";

    /**
     * Location where the Web resources within AAR files are expanded into
     */
    public static final String WEB_RESOURCE_LOCATION = "web.location";
    public static final String AXIS2_CONFIG_PARAM = "Axis2Config";

    //Permissions to a Role

    public static class Permission {
        public static final String LOGIN_TO_ADMIN_UI = "Login to Admin UI";
        public static final String MANAGE_SYSTEM_CONFIGURATION = "Manage system configuration";
        public static final String MANAGE_SECURITY = "Manage security";
        public static final String UPLOAD_SERVICES = "Upload Service";
        public static final String MANAGE_SERVICES = "Manage Services";
        public static final String MANAGE_MEDIATION = "Manage Mediation";
    }

    public static final String PRODUCT_XML = "product.xml";
    public static final String PRODUCT_XML_WSO2CARBON = "WSO2Carbon";
    public static final String PRODUCT_XML_PROPERTY = "property";
    public static final String PRODUCT_XML_PROPERTIES = "properties";
    public static final String PRODUCT_XML_USERFORUM = "userforum";
    public static final String PRODUCT_XML_USERGUIDE = "userguide";
    public static final String PRODUCT_XML_MAILINGLIST = "mailinglist";
    public static final String PRODUCT_XML_ISSUETRACKER = "issuetracker";
    public static final String PRODUCT_XML_WEB_ADMIN_CONSOLE_TITLE = "webAdminConsoleTitle";

    public static final String PRODUCT_STYLES_CONTEXT = "styles";

    public static final String CARBON_FAULTY_SERVICE = "carbonFaultyService";
    public static final String CARBON_FAULTY_SERVICE_DUE_TO_MODULE =
            "This service is cannot be started due to missing modules";

    public static class CarbonManifestHeaders {
        public static final String AXIS2_MODULE = "Axis2Module";
        public static final String AXIS2_DEPLOYER = "Axis2Deployer";
        public static final String AXIS2_INIT_REQUIRED_SERVICE = "Axis2RequiredServices";
        public static final String LISTENER_MANAGER_INIT_REQUIRED_SERVICE =
                "ListenerManager-RequiredServices";
        public static final String CAPP_MANGER_INIT_REQUIRED_SERVICE =
                "CAPP_MANAGER-RequiredServices";
    }

    public static final String CARBON_HOME_ENV = "CARBON_HOME";
    public static final String CARBON_HOME_PARAMETER = "${carbon.home}";
    public static final String CARBON_CONFIG_DIR_PATH_ENV = "CARBON_CONFIG_DIR_PATH";
    public static final String CARBON_CATALINA_DIR_PATH_ENV = "CARBON_CATALINA_DIR_PATH";
    public static final String CARBON_TENANTS_DIR_PATH_ENV = "CARBON_TENANTS_DIR_PATH";
    public static final String CARBON_LOGS_PATH_ENV = "CARBON_LOGS";
    public static final String AXIS2_REPO_ENV = "AXIS2_REPO";
    public static final String COMPONENT_REP0_ENV = "COMPONENTS_REPO";
    
    public static final String AUTHENTICATOR_TYPE = "authenticator.type";
    
    // A Service group parameter
    public static final String FORCE_EXISTING_SERVICE_INIT = "forceExistingServiceInit";

    /**
     * Name of the property which is used for storing the WebApplicationsHolder
     */
    public static final String WEB_APPLICATIONS_HOLDER = "carbon.webapps.holder";
    /**
     * Name of the property which is used for storing web applications holders list
     */
    public static final String WEB_APPLICATIONS_HOLDER_LIST = "carbon.webapps.holderlist";
    /**
     * Name of the property to hold the servletContextParameters list
     */
    public static final String SERVLET_CONTEXT_PARAMETER_LIST = "servlet.context.parameters.list";

    /**
     * The attribute stored at UI session
     */
    public static final String LOGGED_USER = "logged-user";

    /*Constants used in handling multiple user store operations*/
    public static final String DOMAIN_SEPARATOR = "/";

    public static final String NAME_COMBINER = "|";

    /*Constants used in handling shared group operations. There must be a separator to separate role name and tenant domain*/
    public static final String ROLE_TENANT_DOMAIN_SEPARATOR = "@";
    
    /**
     * Custom Axis2 events
     **/
    public static class AxisEvent {
        public static final int TRANSPORT_BINDING_ADDED = 100;
        public static final int TRANSPORT_BINDING_REMOVED = 101;
    }

    public static final String REGISTRY_HTTP_PORT = "RegistryHttpPort";

    public static final String HIDE_ADMIN_SERVICE_WSDLS = "HideAdminServiceWSDLs";

    public static final String CARBON_UI_DEFAULT_HOME_PAGE = "../admin/index.jsp";
    
    public static final String CARBON_ADMIN_SERVICE_PERMISSIONS = "carbon.permissions";

    public static final String REQUIRE_CLOUD_DEPLOYMENT = "require-cloud-deployment";

    public static final String IS_CLOUD_DEPLOYMENT = "IsCloudDeployment";

	public static final String HTTP_ADMIN_SERVICES = "HttpAdminServices";

    // parameter to indicate whether HTTP access to Admin Console is enabled
    public static final String ENABLE_HTTP_ADMIN_CONSOLE = "EnableHTTPAdminConsole";

}
