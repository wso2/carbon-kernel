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

package org.wso2.carbon.utils;

public final class ServerConstants {

    private ServerConstants() {
    }

    public static final String AUTHORIZATION_FAULT_CODE = "50978";
    public static final String AUTHENTICATION_FAULT_CODE = "50977";
    public static final String AUTHENTICATION_FAILURE_CODE = "50976";

    public static final String CARBON_HOME = "carbon.home";
    public static final String STRATOS_INSTANCE = "stratos.instance";
    public static final String CARBON_CATALINA_HOME = "carbon.catalina.home";
    public static final String LOCAL_IP_ADDRESS = "carbon.local.ip";
    public static final String TRANSPORTS_XML_PATH = "wso2.transports.xml";
    public static final String WSO2WSAS_ADMIN_PASSWORD = "wso2wsas.admin.password";
    public static final String CARBON_CONFIG_DIR_PATH = "carbon.config.dir.path";
    public static final String CARBON_LOGS_PATH = "carbon.logs.path";
    public static final String AXIS2_REPO = "axis2.repo";
    public static final String COMPONENT_REP0 = "components.repo";
    public static final String REGISTRY_XML_PATH = "wso2.registry.xml";
    public static final String USER_MGT_XML_PATH = "wso2.user.mgt.xml";
    public static final String CARBON_TENANTS_DIR_PATH = "tenants.dir.path";

	//Constants related to Proxy Context Path
	public static final String PROXY_CONTEXT_PATH = "MgtProxyContextPath";
	public static final String WORKER_PROXY_CONTEXT_PATH = "ProxyContextPath";

    /**
     * Mode in which Carbon is started as a Repo writer, where the
     * metadata of the artifacts  in the repo are written to the Registry
     */
    public static final String REPO_WRITE_MODE = "carbon.repo.write.mode";

    public static final String AUTHENTICATION_ADMIN_SERVICE = "AuthenticationAdmin";
    public static final String AUTHENTICATION_SERVICE_NS = "http://authentication.services.core.carbon.wso2.org";
    public static final String AUTHENTICATION_SERVICE_USERNAME = "username";
    public static final String GENERAL_SERVICES = "GeneralServices";

    public static final String HTTP_TRANSPORT = "http";
    public static final String HTTPS_TRANSPORT = "https";
    public static final String LOCAL_TRANSPORT = "local";


    public static final String PASSWORD_EXPIRATION = "passwordExpires";
    public static final String SERVICE_TYPE = "serviceType";

    /**
     * This is the key of the System property which indicates whether the server is running
     * is standalone mode.
     */
    public static final String STANDALONE_MODE = "wso2.server.standalone";

    //=============================================
    //          Service Types
    //=============================================
    public static final String SERVICE_TYPE_EJB = "ejb_service";
    public static final String SERVICE_TYPE_POJO = "pojo_service";
    public static final String SERVICE_TYPE_SPRING = "spring_service";
    public static final String SERVICE_TYPE_DB = "data_service";
    public static final String SERVICE_TYPE_OTHER = "other_service";

    //=============================================
    //          HTTP Constants
    //=============================================
    public static class HTTPConstants {
        public static final String MEDIA_TYPE_X_WWW_FORM = "application/x-www-form-urlencoded";
        public static final String MEDIA_TYPE_APPLICATION_XML = "application/xml";
        public static final String MEDIA_TYPE_TEXT_XML = "text/xml";
        public static final String MEDIA_TYPE_MULTIPART_RELATED = "multipart/related";

        /**
         * @deprecated
         */
        public static final String HTTP_METHOD_GET = "GET";
        public static final String HTTP_METHOD = "HTTP_METHOD";
        /**
         * @deprecated
         */
        public static final String HTTP_METHOD_POST = "POST";
        public static final String CONTENT_TYPE = "ContentType";
        public static final String HTTP_RESPONSE_STATE = "HTTP_RESPONSE_STATE";

        public static final String ANNOTATION = "annotation";
    }

    //=============================================
    //          Tools Constants
    //=============================================
    public static final String XML_VALIDATOR_GROUP = "archive-validator";
    public static final String WSDL_CONVERTER = "wsdlconverter";
    public static final String WSDL_VIEW = "wsdlview";

    //=============================================
    //          Admin Constants
    //=============================================
    public static final String ADMIN_SERVICE_GROUP = "wso2wsas-administration";
    public static final String STATISTICS_SERVICE_GROUP = "wso2statistics";
    public static final String TRACER_SERVICE_GROUP = "wso2tracer";
    public static final String CODEGEN_SERVICE_GROUP = "wso2codegen";

    public static final String ADMIN_MODULE = "wso2wsas-admin";
    public static final String TRACER_MODULE = "wso2tracer";
    public static final String STATISTICS_MODULE = "wso2statistics";

    public static final String USER_LOGGED_IN = "wso2carbon.admin.logged.in";
    public static final String ADMIN_SERVICE_COOKIE = "wso2carbon.admin.service.cookie";
    public static final String ADMIN_SERVICE_AUTH_TOKEN = "wso2carbon.admin.service.cookie";
    public static final String PARAMETER_ADMIN_SERVICE = "adminService";
    public static final String USER_PERMISSIONS = "user-permissions";

    public static final String SERVER_REGISTERED = "wso2wsas.server.registered";

    public static final String CARBON_SERVER_XML_NAMESPACE = "http://wso2.org/projects/carbon/carbon.xml";

    public static final String ADMIN_ROLE = "admin";
    public static final String WSO2WSAS_HB_CONFIG_KEY = "wso2wsas.db.HibernateConfig";

    public static final String TRACING_SERVICE_GROUP_FILTERS = "wso2wsf.tracer.sg.filters";
    public static final String TRACING_SERVICE_FILTERS = "wso2wsf.tracer.service.filters";

    public static final String FILE_RESOURCE_MAP = "file.resource.map";
    public static final String WORK_DIR = "WORK_DIR";
    public static final String CARBON_INSTANCE = "local_WSO2_WSAS";
    public static final String SERVER_NAME = "WSO2";
    public static final String GENERATED_PAGES = "local_wso2wsas.generated.pages";
    public static final String CONFIGURATION_CONTEXT = "CONFIGURATION_CONTEXT";
    public static final String STS_NAME = "wso2carbon-sts";


    public static class Axis2ParameterNames {
        public static final String CONTEXT_ROOT = "contextRoot";
        public static final String SERVICE_PATH = "servicePath";
        public static final String REST_PATH = "restPath";
    }

    public static class ContextPaths {
        public static final String UPLOAD_PATH = "/fileupload";
        public static final String DOWNLOAD_PATH = "/filedownload";
        public static final String SERVER_PATH = "/server";
    }

//    public static class Logging {
//        public static final String LOG_FILE_PATTERN = "log.file.pattern";
//        public static final String LOG_CONSOLE_PATTERN = "log.console.pattern";
//        public static final String LOG_MEMORY_PATTERN = "log.memory.pattern";
//        public static final String MEMORY_APPENDER = "MemoryAppender";
//
//        // global system settings
//        public static final String SYSTEM_LOG_LEVEL = "wso2wsas.system.log.level";
//        public static final String SYSTEM_LOG_PATTERN = "wso2wsas.system.log.pattern";
//        public static final String SYSTEM_LOG_IS_LOADED = "wso2wsas.system.log.is.loaded";
//
//        public static final String WSO2WSAS_CONSOLE_APPENDER = "WSO2WSAS_CONSOLE";
//        public static final String WSO2WSAS_FILE_APPENDER = "WSO2WSAS_LOGFILE";
//        public static final String WSO2WSAS_MEMORY_APPENDER = "WSO2WSAS_MEMORY";
//
//        public static final int MEMORY_APPENDER_BUFFER_SZ = 200;
//    }

/*    public static class Security {
        public static final String SECURITY_NAMESPACE = "http://www.wso2.org/products/wsas/security";
        public static final QName SUMMARY_QN = new QName(SECURITY_NAMESPACE, "Summary");
        public static final QName DESCRIPTION_QN = new QName(SECURITY_NAMESPACE, "Description");
        public static final QName ID_QN = new QName("id");
        public static final QName CATEGORY_QN = new QName(SECURITY_NAMESPACE, "Category");
        public static final QName MODULES_QN = new QName(SECURITY_NAMESPACE, "Modules");

        public static final QName TYPE = new QName(CARBON_SERVER_XML_NAMESPACE, "Type");
        public static final QName PASSWORD = new QName(CARBON_SERVER_XML_NAMESPACE, "Password");
        public static final QName LOCATION = new QName(CARBON_SERVER_XML_NAMESPACE, "Location");
    }*/
}
