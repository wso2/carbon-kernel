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

package org.wso2.carbon.registry.core;

import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.caching.impl.CachingConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * Constants used in the registry which are exposed to be used in APIs + used across packages.
 */
public final class RegistryConstants {

    private RegistryConstants() {
    }

    /**
     * The session resource path
     */
    @SuppressWarnings("unused")
    public static final String SESSION_RESOURCE_PATH = "session.resource.path";

    /**
     * System property to indicate to run create table scripts
     */
    public static final String SETUP_PROPERTY = "setup";

    /**
     * The configuration path for the registry is used to specify the core registry to use in the
     * registry server. This was  specified as a init parameter of the Registry servlet.
     */
    public static final String REGISTRY_CONFIG_PATH = "registry.config.path";

    /**
     * The registry type is used to specify the core registry to use in the registry server. This
     * was specified as a init parameter of the Registry servlet.
     */
    @SuppressWarnings("unused")
    @Deprecated
    public static final String REGISTRY_TYPE_PARAMETER = "registryType";

    /**
     * The JDBC registry type which is used to specify the core registry to use in the registry
     * server. This was specified as a init parameter of the Registry servlet.
     */
    @SuppressWarnings("unused")
    @Deprecated
    public static final String JDBC_REGISTRY_TYPE = "EmbeddedRegistry";

    /**
     * The remote registry type which is used to specify the core registry to use in the registry
     * server. This was specified as a init parameter of the Registry servlet.
     */
    @SuppressWarnings("unused")
    @Deprecated
    public static final String REMOTE_REGISTRY_TYPE = "RemoteRegistry";

    /**
     * The status message.
     */
    @SuppressWarnings("unused")
    @Deprecated
    public static final String STATUS_MESSAGE_NAME = "edit_status";

    /**
     * The constant to identify the registry context.
     */
    @SuppressWarnings("unused")
    @Deprecated
    public static final String REGISTRY_CONTEXT = "registry.context";

    /**
     * The name of the session and request attribute to retrieve the tenant domain.
     * @deprecated use MultitenantConstants.TENANT_DOMAIN
     */
    @SuppressWarnings("unused")
    @Deprecated
    public static final String TENANT_DOMAIN = MultitenantConstants.TENANT_DOMAIN;

    /*
     * Built in user name, Anonymous.
     */
    @SuppressWarnings("unused")
    @Deprecated
    public static final String ANONYMOUS_USER = CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME;

    /*
     * Built in user Anonymous password.
     */
    @SuppressWarnings("unused")
    @Deprecated
    public static final String ANONYMOUS_PASSWORD = "guest";

    /*
     * Built in role guests
     */
    @SuppressWarnings("unused")
    @Deprecated
    public static final String GUESTS_ROLE = "guests";

    /*
     * Built in role everyone
     */
    @SuppressWarnings("unused")
    @Deprecated
    public static final String EVERYONE_ROLE = "everyone";

    /*
     * Built in user name: admin
     * Note that this is only used in tests and should not assume the
     * admin user name as a constant in any implementation.
     */
    @SuppressWarnings("unused")
    @Deprecated
    public static final String ADMIN_USER = "admin";

    /*
     * Built in password: admin
     * Note that this is only used in tests and should not assume the
     * admin password as a constant in any implementation.
     */
    @SuppressWarnings("unused")
    @Deprecated
    public static final String ADMIN_PASSWORD = "admin";

    /*
     * Built in role admin.
     */
    @SuppressWarnings("unused")
    @Deprecated
    public static final String ADMIN_ROLE = "admin";

    /**
     * Known path parameter names
     */
    public static final String VERSION_PARAMETER_NAME = "version";

    /** carbon system properties */
    /**
     * System property of carbon collection.
     */
    @SuppressWarnings("unused")
    @Deprecated
    public static final String CARBON_COLLECTION_NAME = "carbon";
    /**
     * System property for the registry root.
     */
    @SuppressWarnings("unused")
    @Deprecated
    public static final String CARBON_COLLECTION_PARENT_PROPERTY = "carbon.registry.root";
    /**
     * System property to clean the registry
     */
    public static final String CARBON_REGISTRY_CLEAN = "carbon.registry.clean";

    /**
     * The name of the session attribute that keeps the root registry instance.
     */
    public static final String ROOT_REGISTRY_INSTANCE = "WSO2RegistryRoot";

    /**
     * Default size of byte buffers used inside the registry kernel.
     */
    public static final int DEFAULT_BUFFER_SIZE = 1024;

/**
     * Default identifier for utf-8.
     */
    public static final String DEFAULT_CHARSET_ENCODING = "utf-8";


    ////////////////////////////////////////////////////////
    // Base Collection paths
    ////////////////////////////////////////////////////////

    /**
     * THe root path of the registry resource tree.
     */
    public static final String ROOT_PATH = "/";

    /**
     * The base path for the system collection , where all the local, config, governance registries
     * kept.
     */
    public static final String SYSTEM_COLLECTION_BASE_PATH = "/_system";

    /**
     * The base path for the local registry.
     */
    public static final String LOCAL_REPOSITORY_BASE_PATH = SYSTEM_COLLECTION_BASE_PATH + "/local";

    /**
     * The base path for the config registry.
     */
    public static final String CONFIG_REGISTRY_BASE_PATH = SYSTEM_COLLECTION_BASE_PATH + "/config";

    /**
     * The base path for the governance registry.
     */
    public static final String GOVERNANCE_REGISTRY_BASE_PATH = SYSTEM_COLLECTION_BASE_PATH +
            "/governance";

    ////////////////////////////////////////////////////////
    // Configuration resource paths
    ////////////////////////////////////////////////////////

    /**
     * The path to store registry configurations.
     */
    public static final String REGISTRY_COMPONENT_PATH =
            "/repository/components/org.wso2.carbon.registry";

    /**
     * The path to store the governance configurations.
     */
    public static final String GOVERNANCE_COMPONENT_PATH =
            "/repository/components/org.wso2.carbon.governance";

    /**
     * The path to store the mount meta data
     */
    public static final String SYSTEM_MOUNT_PATH = REGISTRY_COMPONENT_PATH + "/mount";

    /**
     * The media type of a mount entry
     */
    public static final String MOUNT_MEDIA_TYPE = "application/vnd.wso2.mount";

    /**
     * The path to store the users profiles.
     */
    public static final String PROFILES_PATH = "/users/";

    /**
     * The path to store the lifecycle configurations.
     */
    @SuppressWarnings("unused")
    public static final String LIFECYCLE_CONFIGURATION_PATH = GOVERNANCE_COMPONENT_PATH +
            "/lifecycles/";

    /**
     * The path to store the handler configurations.
     */
    @SuppressWarnings("unused")
    public static final String HANDLER_CONFIGURATION_PATH = GOVERNANCE_COMPONENT_PATH +
            "/handlers/";

    /**
     * The path to store the queries in custom queries.
     */
    @SuppressWarnings("unused")
    public static final String QUERIES_COLLECTION_PATH = REGISTRY_COMPONENT_PATH + "/queries";

    /**
     * The path to store the governance service ui configurations.
     */
    public static final String GOVERNANCE_SERVICES_CONFIG_PATH = GOVERNANCE_COMPONENT_PATH +
            "/configuration/services/";

    /**
     * The path to store the governance people ui configurations.
     */
    public static final String GOVERNANCE_PEOPLE_CONFIG_PATH = GOVERNANCE_COMPONENT_PATH +
            "/configuration/people/";

    /**
     * The path to store the user management (including realm) configuration, valid only for tenant
     * id != 0
     */
    public static final String REALM_CONFIGURATION_PATH =
            "/repository/components/org.wso2.carbon.user.mgt";

    /**
     * The name of the resource to keep the configurations, valid only for tenant id != 0
     */
    public static final String REALM_CONFIGURATION_RESOURCE = "user-mgt.xml";

    /**
     * The path to store the services
     */
    public static final String GOVERNANCE_SERVICE_PATH = "/trunk/services";

    /**
     * The path to store People artifacts
     */
    public static final String GOVERNANCE_PEOPLE_PATH = "/people";

    /**
     * The path to store system collection
     */
    @SuppressWarnings("unused")
    @Deprecated
    public static final String SYSTEM_PATH = "/system";

    @SuppressWarnings("unused")
    @Deprecated
    public static final String PROFILE_RESOURCE_NAME = "/profiles";

    @SuppressWarnings("unused")
    @Deprecated
    public static final String QUERIES_COLLECTION_NAME = "queries";

    @SuppressWarnings("unused")
    @Deprecated
    public static final String USERS_COLLECTION_NAME = "users";

    /**
     * The separator of the paths of the registry resources.
     */
    public static final String PATH_SEPARATOR = "/";

    /**
     * The tag property
     */
    @SuppressWarnings("unused")
    public static final String TAG_PROPERTY = "tag";

    /**
     * The core registry
     */
    public static final String REGISTRY = "CoreRegistry";

    /**
     * The name of the session attribute for the system registry.
     */
    @Deprecated
    public static final String SYSTEM_REGISTRY = "SystemRegistry";

    /**
     * The name of the session attribute for the user registry.
     */
    @SuppressWarnings("unused")
    @Deprecated
    public static final String USER_REGISTRY = "user_registry";

    /**
     * The name of the session attribute to store the tenant id.
     * @deprecated use MultitenantConstants.TENANT_ID
     */
    @SuppressWarnings("unused")
    @Deprecated
    // This is used outside the registry kernel
    public static final String TENANT_ID = MultitenantConstants.TENANT_ID;

    @SuppressWarnings("unused")
    @Deprecated
    public static final String REGISTRY_REALM = "registry_realm";


    @SuppressWarnings("unused")
    @Deprecated
    public static final String SESSION_PROPERTY = "SessionObject";

    @SuppressWarnings("unused")
    @Deprecated
    public static final String TAG_REGISTRY = "/view";

    @SuppressWarnings("unused")
    @Deprecated
    public static final String TAG_UPLOAD = "/upload";

    @SuppressWarnings("unused")
    @Deprecated
    public static final String ACTION = "action";

    /**
     * Custom UI registry UI modes
     */
    public static final String CONTENT_UI_MODE = "registry.ui.mode";

    /**
     * Custom UI registry UI mode: browse mode
     */
    public static final String BROWSE_MODE = "browse.mode";

    /**
     * Custom UI registry UI mode: edit mode
     */
    public static final String EDIT_MODE = "edit.mode";

    /**
     * Custom UI registry UI mode: new mode
     */
    public static final String NEW_MODE = "new.mode";


    @SuppressWarnings("unused")
    @Deprecated
    public static final String USER_TOKEN = "user.token";

    ////////////////////////////////////////////////////////
    // Rendering options in the UI.
    ////////////////////////////////////////////////////////

    /**
     * Custom ui rendering methods.
     */
    public static final String UI_RENDERING_METHOD_PROPERTY = "registry.ui.renderingMethod";

    /**
     * Rendering row
     */
    @SuppressWarnings("unused")
    public static final String R_RAW = "raw";

    /**
     * Rendering view text.
     */
    public static final String R_VIEW_TEXT = "view.text";

    /**
     * Rendering edit text.
     */
    public static final String R_EDIT_TEXT = "edit.text";

    /**
     * Rendering new text.
     */
    public static final String R_NEW_TEXT = "new.text";

    /**
     * Rendering view xml.
     */
    public static final String R_VIEW_XML = "view.xml";

    /**
     * Rendering edit xml.
     */
    public static final String R_EDIT_XML = "edit.xml";

    /**
     * Rendering new xml.
     */
    public static final String R_NEW_XML = "new.xml";

    /**
     * Rendering general.
     */
    public static final String R_GENERAL = "general";

    ////////////////////////////////////////////////////////
    // Built-in view/edit names
    ////////////////////////////////////////////////////////

    /**
     * Built-in default view..
     */
    public static final String DEFAULT_VIEW_NAME = "default";
    /**
     * Built-in raw view.
     */
    public static final String RAW_VIEW_NAME = "raw";
    /**
     * Built-in text view.
     */
    public static final String TEXT_VIEW_NAME = "text";
    /**
     * Built-in xml view.
     */
    public static final String XML_VIEW_NAME = "xml";

    @SuppressWarnings("unused")
    @Deprecated
    public static final String UNDEFINED_VIEW_NAME = "undefined";

    ////////////////////////////////////////////////////////
    // Directory listing
    ////////////////////////////////////////////////////////

    @SuppressWarnings("unused")
    @Deprecated
    public static final String DIRECTORY_ELEMENT = "directory";

    @SuppressWarnings("unused")
    @Deprecated
    public static final String FILE_ELEMENT = "file";

    @SuppressWarnings("unused")
    @Deprecated
    public static final String ATT_NAME = "name";

    @SuppressWarnings("unused")
    @Deprecated
    public static final String ATT_PATH = "path";

    /**
     * the url component to resolve registry in atom support
     */
    public static final String REGISTRY_INSTANCE = "registry";

    @SuppressWarnings("unused")
    @Deprecated
    public static final String PATH = "path";

    /**
     * The flag to indicate the active state in atom feeds.
     */
    @SuppressWarnings("unused")
    public static final int ACTIVE_STATE = 100;

    /**
     * The flag to indicate the deleted state in atom feeds.
     */
    public static final int DELETED_STATE = 101;

    /**
     * Number of items per pages entry.
     */
    @SuppressWarnings("unused")
    // This is used outside the registry kernel
    public static final int ITEMS_PER_PAGE = 10;

    ////////////////////////////////////////////////////////
    // Dependency types for setting dependencies between
    // resources
    ////////////////////////////////////////////////////////

    @SuppressWarnings("unused")
    @Deprecated
    public static final String DEFAULT_DEPENDENCY = "default";

    ////////////////////////////////////////////////////////
    // Internal media types
    ////////////////////////////////////////////////////////

    @SuppressWarnings("unused")
    @Deprecated
    public static final String DEFAULT_MEDIA_TYPE = "default";

    /**
     * SQL Query media type.
     */
    public static final String SQL_QUERY_MEDIA_TYPE = "application/vnd.sql.query";

    @SuppressWarnings("unused")
    @Deprecated
    public static final String COMMENT_MEDIA_TYPE = "comment";

    /**
     * Media type: rating
     */
    public static final String RATING_MEDIA_TYPE = "rating";

    /**
     * Media type: tag
     */
    public static final String TAG_MEDIA_TYPE = "tag";

    ////////////////////////////////////////////////////////
    // Built-in media types
    ////////////////////////////////////////////////////////

    /**
     * The media type for WSDLs
     */
    @SuppressWarnings("unused")
    public static final String WSDL_MEDIA_TYPE = "application/wsdl+xml";

    /**
     * The media type for XSDs
     */
    @SuppressWarnings("unused")
    public static final String XSD_MEDIA_TYPE = "application/x-xsd+xml";

    /**
     * The media type for Policies
     */
    @SuppressWarnings("unused")
    public static final String POLICY_MEDIA_TYPE = "application/policy+xml";

//    @SuppressWarnings("unused")
//    @Deprecated
//    public static final String SYNPASE_REPOSITORY_MEDIA_TYPE = "application/vnd.apache.synapse";

    @SuppressWarnings("unused")
    @Deprecated
    public static final String SYNAPSE_CONF_COLLECTION_MEDIA_TYPE = "synapse-conf";

    @SuppressWarnings("unused")
    @Deprecated
    public static final String SYNAPSE_SEQUENCE_COLLECTION_MEDIA_TYPE = "synapse-sequences";

    @SuppressWarnings("unused")
    @Deprecated
    public static final String SYNAPSE_ENDPOINT_COLLECTION_MEDIA_TYPE = "synapse-endpoints";

    @SuppressWarnings("unused")
    @Deprecated
    public static final String SYNAPSE_PROXY_SERVICES_COLLECTION_MEDIA_TYPE =
            "synapse-proxy-services";

    @SuppressWarnings("unused")
    @Deprecated
    public static final String SYNAPSE_TASKS_COLLECTION_MEDIA_TYPE = "synapse-tasks";

    @SuppressWarnings("unused")
    @Deprecated
    public static final String SYNAPSE_ENTRIES_COLLECTION_MEDIA_TYPE = "synapse-entries";


    @SuppressWarnings("unused")
    @Deprecated
    public static final String SYNAPSE_CONF_COLLECTION_NAME = "conf";

    @SuppressWarnings("unused")
    @Deprecated
    public static final String SYNAPSE_SEQUENCES_COLLECTION_NAME = "sequences";

    @SuppressWarnings("unused")
    @Deprecated
    public static final String SYNAPSE_ENDPOINT_COLLECTION_NAME = "endpoints";

    @SuppressWarnings("unused")
    @Deprecated
    public static final String SYNAPSE_PROXY_SERVICES_COLLECTION_NAME = "proxy-services";

    @SuppressWarnings("unused")
    @Deprecated
    public static final String SYNAPSE_TASKS_COLLECTION_NAME = "tasks";

    @SuppressWarnings("unused")
    @Deprecated
    public static final String SYNAPSE_ENTRIES_COLLECTION_NAME = "entries";

    @SuppressWarnings("unused")
    @Deprecated
    public static final String AXIS2_CONF_COLLECTION_MEDIA_TYPE = "axis2-conf";

    @SuppressWarnings("unused")
    @Deprecated
    public static final String AXIS2_SERVICES_COLLECTION_MEDIA_TYPE = "axis2-services";

    @SuppressWarnings("unused")
    @Deprecated
    public static final String AXIS2_MODULES_COLLECTION_MEDIA_TYPE = "axis2-modules";


    @SuppressWarnings("unused")
    @Deprecated
    public static final String AXIS2_CONF_COLLECTION_NAME = "conf";

    @SuppressWarnings("unused")
    @Deprecated
    public static final String AXIS2_SERVICES_COLLECTION_NAME = "services";

    @SuppressWarnings("unused")
    @Deprecated
    public static final String AXIS2_MODULES_COLLECTION_NAME = "modules";

    @SuppressWarnings("unused")
    public static final String MEX_MEDIA_TYPE = "application/vnd.wso2-mex+xml";

    @SuppressWarnings("unused")
    @Deprecated
    public static final String MEX_WSDLS_COLLECTION_NAME = "wsdls";

    /**
     * Media type: wsdls.
     */
    @SuppressWarnings("unused")
    public static final String MEX_WSDLS_COLLECTION_MEDIA_TYPE = "application/vnd.wso2-wsdls";

    @SuppressWarnings("unused")
    @Deprecated
    public static final String MEX_ENDPOINTS_COLLECTION_NAME = "endpoints";

    /**
     * Media type - endpoints.
     */
    @SuppressWarnings("unused")
    public static final String MEX_ENDPOINTS_COLLECTION_MEDIA_TYPE =
            "application/vnd.wso2-endpoints";

    @SuppressWarnings("unused")
    @Deprecated
    public static final String MEX_URLS_COLLECTION_NAME = "addresses";

    @SuppressWarnings("unused")
    @Deprecated
    public static final String MEX_URL_MEDIA_TYPE = "application/vnd.wso2-url";

    @SuppressWarnings("unused")
    @Deprecated
    public static final String MEX_URLS_COLLECTION_MEDIA_TYPE = "application/vnd.wso2-addresses";

    /**
     * The media type for Profiles
     */
    @SuppressWarnings("unused")
    public static final String PROFILES_MEDIA_TYPE = "application/vnd.wso2-profiles";

    /**
     * The media type for Services
     */
    @SuppressWarnings("unused")
    public static final String SERVICE_MEDIA_TYPE = "application/vnd.wso2-service+xml";

    /**
     * Result types of dynamic queries
     */
    public static final String RESULT_TYPE_PROPERTY_NAME = "resultType";

    /**
     * Result type: resource
     */
    public static final String RESOURCES_RESULT_TYPE = "Resource";

    /**
     * Result type: resource UUID
     */
    public static final String RESOURCE_UUID_RESULT_TYPE = "ResourceUUID";

    /**
     * Result type: comments
     */
    public static final String COMMENTS_RESULT_TYPE = "Comments";

    /**
     * Result type: ratings
     */
    public static final String RATINGS_RESULT_TYPE = "Ratings";

    /**
     * Result type: tags
     */
    public static final String TAGS_RESULT_TYPE = "Tags";

    /**
     *  Result type : summary count of all tags
     */
    public static final String TAG_SUMMARY_RESULT_TYPE = "TagSummary";

    /**
     * Service UI states.
     */
    @SuppressWarnings("unused")
    public static final String GOVERNANCE_SERVICE_STATES = "Created,Tested,Deployed,Deprecated";

    /**
     * Service UI contact types.
     */
    @SuppressWarnings("unused")
    public static final String GOVERNANCE_CONTACT_TYPES = "Business Owner,Technical Owner";

    /**
     * Service UI transport prototypes.
     */
    @SuppressWarnings("unused")
    public static final String GOVERNANCE_TRANSPORT_PROTOCOLS = "HTTPS,HTTP,SMTP,TCP,XMPP,JMS";

    /**
     * Service UI message formats.
     */
    @SuppressWarnings("unused")
    public static final String GOVERNANCE_MESSAGE_FORMATS =
            "SOAP 1.1,SOAP 1.2,XML,JSON,HTTP-REST,CSV,BINARY";

    /**
     * Service UI message exchange patterns.
     */
    @SuppressWarnings("unused")
    public static final String GOVERNANCE_MESSAGE_EXCHANGE_PATTERN = "Request Response,One Way";

    /**
     * Service UI authentication platforms.
     */
    @SuppressWarnings("unused")
    public static final String GOVERNANCE_AUTHENTICATION_PLATFORMS =
            "None,TAM_WEBSEAL,XTS-WS TRUST,BuiltIn," +
                    "WSO2 Identity Server";

    /**
     * Service UI authentication mechanism.
     */
    @SuppressWarnings("unused")
    public static final String GOVERNANCE_AUTHENTICATION_MECHANISMS =
            "None,OpenID,InfoCard,Client Certificates," +
                    "HTTPS Basic Authentication,IP Address Filtering," +
                    "WS-Security/WS-Trust Token,Others";

    /**
     * Service UI message integrity.
     */
    @SuppressWarnings("unused")
    public static final String GOVERNANCE_MESSAGE_INTEGRITY =
            "None,SSL,WS-Security,XML Digital Signatures,Other";

    /**
     * Service UI message encryption.
     */
    @SuppressWarnings("unused")
    public static final String GOVERNANCE_MESSAGE_ENCRYPTION =
            "None,SSL,WS-Security,XML Digital Signatures,Other";

    /**
     * Service UI authentication platforms.
     */
    @SuppressWarnings("unused")
    public static final String GOVERNANCE_AUTHORIZATION_PLATFORMS =
            "None,TAM_WEBSEAL,XTS-WS TRUST,BuiltIn," +
                    "WSO2 Identity Server";

    /**
     * Service UI governance endpoints.
     */
    @SuppressWarnings("unused")
    public static final String GOVERNANCE_ENDPOINTS = "Unknown,Dev,QA,Test";


    /**
     * Service UI governance service config.
     */
    @SuppressWarnings("unused")
    public static final String GOVERNANCE_SERVICE_CONFIG =
            "states;contact_types;transport_protocols;" +
                    "message_formats;message_exchange_patterns;authentication_platforms;" +
                    "authentication_mechanisms;authorization_platforms;message_integrity;" +
                    "message_encryption";

    /**
     * resource jsp path.
     */
    public static final String RESOURCES_JSP = "/admin/resources.jsp";

    @SuppressWarnings("unused")
    @Deprecated
    public static final String RESOURCE_DETAILS_JSP = "/admin/resources_details.jsp";


    @SuppressWarnings("unused")
    @Deprecated
    public static final String RESOURCE_CONTENT_JSP = "resource";

    /**
     * The name of the session attribute for error message in registry servlets.
     */
    public static final String ERROR_MESSAGE = "error.message";

    /**
     * The name of the session attribute for error jsp in registry servlets.
     */
    public static final String ERROR_JSP = "/admin/error.jsp";

    /**
     * Name of the symbolic link property.
     */
    @SuppressWarnings("unused")
    public static final String SYMLINK_PROPERTY_NAME = "SymlinkPropertyName";


    /**
     * Used to pass handler throughout request context
     */
    public static final String SYMLINK_TO_REMOVE_PROPERTY_NAME = "SymlinkToRemovePropertyName";

    /**
     * Separator used to access Registry meta data - i.e. "/resource$tags"
     */
    public static final String URL_SEPARATOR = ";";

    /**
     * Parameter separator used to access Registry meta data - i.e. "/resource$tags"
     */
    public static final String URL_PARAMETER_SEPARATOR = ":";


    /**
     * Url parameter of the view action
     */
    @SuppressWarnings("unused")
    public static final String VIEW_ACTION = URL_SEPARATOR + "view";

    /**
     * Property name of the browse view.
     */
    public static final String BROWSE_PROPERTY = "view";

    /**
     * Url parameter of the edit action
     */
    @SuppressWarnings("unused")
    public static final String EDIT_ACTION = URL_SEPARATOR + "edit";
    /**
     * Property name of the edit view.
     */
    public static final String EDIT_PROPERTY = "edit";

    /**
     * Property name of the new view.
     */
    public static final String NEW_PROPERTY = "new";

    /**
     * Property name of the UI content.
     */
    @SuppressWarnings("unused")
    public static final String UI_CONTENT_PROPERTY = "UI.content";

    /**
     * Property name of the UI html.
     */
    @SuppressWarnings("unused")
    public static final String UI_HTML = "UI.HTML";

    /**
     * Property name of the UI text.
     */
    @SuppressWarnings("unused")
    public static final String UI_TEXT = "UI.text";

    /**
     * Property name of the UI xml.
     */
    @SuppressWarnings("unused")
    public static final String UI_XML = "UI.XML";

    /**
     * Property name of empty UI.
     */
    @SuppressWarnings("unused")
    public static final String UI_NONE = "UI.none";

    /**
     * Text Editor: Text input name
     */
    public static final String TEXT_INPUT_NAME = "generic-text-input";

    /**
     * Text Editor: processor key
     */
    public static final String TEXT_EDIT_PROCESSOR_KEY = "system.text.edit.processor";

    /**
     * Custom Editor: processor key
     */
    public static final String CUSTOM_EDIT_PROCESSOR_KEY = "edit-processor";

    /**
     * Servlet parameter for view key.
     */
    public static final String VIEW_KEY = "view-key";

    /**
     * Servlet parameter for view type.
     */
    public static final String VIEW_TYPE = "view-type";

    /**
     * Servlet parameter for edit view type.
     */
    public static final String EDIT_VIEW_TYPE = "edit";

    /**
     * Servlet parameter for new view type.
     */
    public static final String NEW_VIEW_TYPE = "new";

    /**
     * Servlet parameter for resources in registry servlet.
     */
    public static final String RESOURCES_PATH = "resources";

    /**
     * Namespace of the custom registry specific feed fields/entries.
     */
    public static final String REGISTRY_NAMESPACE = "http://wso2.org/registry";

    @SuppressWarnings("unused")
    // This is used outside the registry kernel.
    public static final String REGISTRY_UNAUTHORIZED_ERROR =
            "You are not Authorized to perform this operation !";

    /**
     * The error message the result set close fail.
     */
    public static final String RESULT_SET_PREPARED_STATEMENT_CLOSE_ERROR =
            "A SQLException error has occurred " +
                    "when trying to close result set or prepared statement";

    /**
     * When caching atom feeds, the maximum size of the cache.
     */
    public static final long MAX_REG_CLIENT_CACHE_SIZE = 50000;
    /*
     * The name of the registry cache manager
     */
    public static final String REGISTRY_CACHE_MANAGER = "registryCacheManager";
    /**
     * The id of the path cache for registry resources.
     */
    public static final String PATH_CACHE_ID = CachingConstants.LOCAL_CACHE_PREFIX + "REG_PATH_CACHE";
    /**
     * The id of the UUID cache for registry paths.
     */
    public static final String UUID_CACHE_ID = "REG_UUID_CACHE";
    /**
     * The id of the registry  cache. Switch to distributed mode when clustering is enabled.
     */
    public static final String REGISTRY_CACHE_BACKED_ID = "REG_CACHE_BACKED_ID";

    /**
     * The id of the registry  local cache.
     */
    public static final String REGISTRY_LOCAL_CACHE_ID =
            CachingConstants.LOCAL_CACHE_PREFIX + "REG_LOCAL_CACHE_BACKED_ID";

    /**
     * Symbolic link property: actual path
     */
    public static final String REGISTRY_ACTUAL_PATH = "registry.actualpath";

    /**
     * Symbolic link property: mount point
     */
    public static final String REGISTRY_MOUNT_POINT = "registry.mountpoint";

    /**
     * Symbolic link property: target point
     */
    public static final String REGISTRY_TARGET_POINT = "registry.targetpoint";

    /**
     * Symbolic link property: registry link
     */
    public static final String REGISTRY_LINK = "registry.link";

    /**
     * Symbolic link property: registry link restoration details
     */
    public static final String REGISTRY_LINK_RESTORATION = "registry.linkrestoration";

    /**
     * Symbolic link property: registry user
     */
    public static final String REGISTRY_USER = "registry.user";

    /**
     * Symbolic link property: registry author
     */
    public static final String REGISTRY_AUTHOR = "registry.author";

    /**
     * Symbolic link property: registry mount
     */
    public static final String REGISTRY_MOUNT = "registry.mount";

    /**
     * Symbolic link property: registry fixed mount.
     */
    public static final String REGISTRY_FIXED_MOUNT = "registry.fixedmount";

    /**
     * Symbolic link property: real path
     */
    public static final String REGISTRY_REAL_PATH = "registry.realpath";

    /**
     * Symbolic link property: prevents recursive operations on the resource
     */
    public static final String REGISTRY_NON_RECURSIVE = "registry.nonrecursive";

    /**
     * Symbolic link property: existing resource.
     */
    public static final String REGISTRY_EXISTING_RESOURCE = "registry.existingresource";

    /**
     * Execution Identifier of the add service authorize role listener.
     */
    @SuppressWarnings("unused")
    public static final int ADD_SERVICE_AUTHORIZE_ROLE_LISTENER_EXECUTION_ORDER_ID = 100;

    /**
     * Execution Identifier of the add policy authorize role listener.
     */
    @SuppressWarnings("unused")
    public static final int ADD_POLICY_AUTHORIZE_ROLE_LISTENER_EXECUTION_ORDER_ID = 101;

    /**
     * Execution Identifier of the add WSDL authorize role listener.
     */
    @SuppressWarnings("unused")
    public static final int ADD_WSDL_AUTHORIZE_ROLE_LISTENER_EXECUTION_ORDER_ID = 102;

    /**
     * Execution Identifier of the add schema authorize role listener.
     */
    @SuppressWarnings("unused")
    public static final int ADD_SCHEMA_AUTHORIZE_ROLE_LISTENER_EXECUTION_ORDER_ID = 103;

    /**
     * Execution Identifier of the add endpoint authorize role listener.
     */
    @SuppressWarnings("unused")
    public static final int ADD_ENDPOINT_AUTHORIZE_ROLE_LISTENER_EXECUTION_ORDER_ID = 104;

    /**
     * Execution Identifier of the list service authorize role listener.
     */
    @SuppressWarnings("unused")
    public static final int LIST_SERVICE_AUTHORIZE_ROLE_LISTENER_EXECUTION_ORDER_ID = 110;

    /**
     * Execution Identifier of the list policy authorize role listener.
     */
    @SuppressWarnings("unused")
    public static final int LIST_POLICY_AUTHORIZE_ROLE_LISTENER_EXECUTION_ORDER_ID = 111;

    /**
     * Execution Identifier of the list WSDL authorize role listener.
     */
    @SuppressWarnings("unused")
    public static final int LIST_WSDL_AUTHORIZE_ROLE_LISTENER_EXECUTION_ORDER_ID = 112;

    /**
     * Execution Identifier of the list endpoint authorize role listener.
     */
    @SuppressWarnings("unused")
    public static final int LIST_SCHEMA_AUTHORIZE_ROLE_LISTENER_EXECUTION_ORDER_ID = 113;

    /**
     * Execution Identifier of the list schema authorize role listener.
     */
    @SuppressWarnings("unused")
    public static final int LIST_ENDPOINT_AUTHORIZE_ROLE_LISTENER_EXECUTION_ORDER_ID = 114;


    /**
     * The meta directory of a check-in client dump. The reserved name for resource/collection name
     */
    public static final String CHECK_IN_META_DIR = ".meta";

    /**
     * Identifier to distinguish operations performed by the mount handler at a remote mounting instance.
     */
    public static final String REMOTE_MOUNT_OPERATION = "registry.remotemount.operation";

    /**
        * Identifier to identify and separate "version"  string
        */
    public static final String VERSION_SEPARATOR = ";version:";

    /**
     * Defines the media type used for symlink and remote link resources.
     * */
    public static final String LINK_MEDIA_TYPE = "application/vnd.wso2-link";

}
