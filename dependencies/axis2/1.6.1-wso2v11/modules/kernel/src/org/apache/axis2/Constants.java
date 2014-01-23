/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.axis2;

/**
 * Class Constants
 */
@SuppressWarnings({"UnusedDeclaration"})
public class Constants extends org.apache.axis2.namespace.Constants {

    /**
     * Field SOAP_STYLE_RPC_ENCODED
     */
    public static final int SOAP_STYLE_RPC_ENCODED = 1000;

    /**
     * Field SOAP_STYLE_RPC_LITERAL
     */
    public static final int SOAP_STYLE_RPC_LITERAL = 1001;

    /**
     * Field SOAP_STYLE_DOC_LITERAL_WRAPPED
     */
    public static final int SOAP_STYLE_DOC_LITERAL_WRAPPED = 1002;

    /**
     * Field SESSION_SCOPE
     */
    public static final String SESSION_SCOPE = "session";

    /**
     * Field SESSION_CONTEXT_PROPERTY
     */
    public static final String SESSION_CONTEXT_PROPERTY = "SessionContext";

    /**
     * Field PHASE_TRANSPORT
     */
    public static final String PHASE_TRANSPORT = "transport";

    /**
     * Field PHASE_SERVICE
     */
    public static final String PHASE_SERVICE = "service";

    /**
     * Field PHASE_GLOBAL
     */
    public static final String PHASE_GLOBAL = "global";

    /**
     * Field MESSAGE_SCOPE
     */
    public static final String MESSAGE_SCOPE = "message";

    public static final String AXIS_BINDING_OPERATION = "AxisBindingOperation";
    
    public static final String AXIS_BINDING_MESSAGE = "AxisBindingMessage";


    /**
     * To change the context path from axis2/service to something else
     */
    public static final String PARAM_CONTEXT_ROOT = "contextRoot";
    /**
     * To change the service path to something else
     */
    public static final String PARAM_SERVICE_PATH = "servicePath";
    /**
     * Parameter name for transport session management
     */
    public static final String MANAGE_TRANSPORT_SESSION = "manageTransportSession";

    public static final String HTTP_RESPONSE_STATE = "axis2.http.response.state";
    public static final String HTTP_BASIC_AUTH_REALM = "axis2.authentication.realm";

    /**
     * Field APPLICATION_SCOPE
     */
    public static final String SCOPE_APPLICATION = "application";
    public static final String SCOPE_SOAP_SESSION = "soapsession";
    public static final String SCOPE_TRANSPORT_SESSION = "transportsession";
    public static final String SCOPE_REQUEST = "request";

    public static final String AXIS2_REPO = "axis2.repo";
    public static final String AXIS2_CONF = "axis2.xml";
    public static final String USER_HOME = "user.home";

    /**
     * Field TRANSPORT_TCP
     */
    public static final String TRANSPORT_TCP = "tcp";
    public static final String TRANSPORT_MAIL = "mailto";
    public static final String TRANSPORT_LOCAL = "local";
    public static final String TRANSPORT_JMS = "jms";

    /**
     * Field TRANSPORT_HTTP
     */
    public static final String TRANSPORT_HTTP = "http";
    public static final String TRANSPORT_HTTPS = "https";

    //Parameter name of Service impl class
    public static final String SERVICE_CLASS = "ServiceClass";
    public static final String SERVICE_OBJECT_SUPPLIER = "ServiceObjectSupplier";
    public static final String SERVICE_TCCL = "ServiceTCCL";

    public static final String TCCL_DEFAULT = "default";
    public static final String TCCL_COMPOSITE = "composite";
    public static final String TCCL_SERVICE = "service";
    public static final String FAULT_NAME = "faultName";
    public static final String REQUEST_PARAMETER_MAP = "requestParameterMap";

    /**
     * Field REQUEST_URL_PREFIX
     */
    public static final String LIST_PHASES = "listPhases";
    public static final String LIST_MODULES = "listModules";
    public static final String LIST_GLOABLLY_ENGAGED_MODULES = "globalModules";
    public static final String LIST_CONTEXTS = "listContexts";
    public static final String ENGAGE_MODULE_TO_SERVICE_GROUP = "engageToServiceGroup";
    public static final String ENGAGE_MODULE_TO_SERVICE = "engageToService";
    public static final String ENGAGE_GLOBAL_MODULE = "engagingglobally";
    public static final String ADMIN_LOGIN = "adminlogin";
    public static final String AXIS_WEB_CONTENT_ROOT = "/axis2-web/";

    /**
     * List service for admin pages
     */
    public static final String ADMIN_LISTSERVICES = "listService";
    public static final String VIEW_GLOBAL_HANDLERS = "viewGlobalHandlers";
    public static final String SELECT_SERVICE_FOR_PARA_EDIT = "selectServiceParaEdit";
    public static final String SELECT_SERVICE = "selectService";
    public static final String EDIT_SERVICE_PARA = "editServicepara";
    public static final String VIEW_SERVICE_HANDLERS = "viewServiceHandlers";
    public static final String USER_NAME = "userName";
    public static final String ADMIN_SECURITY_DISABLED = "disableAdminSecurity";
    public static final String ADMIN_SERVICE_LISTING_DISABLED = "disableServiceList";

    /**
     * Field SINGLE_SERVICE
     */
    public static final String SINGLE_SERVICE = "singleservice";

    /**
     * @deprecated Please use org.apache.axis2.transport.http.HTTPConstants.MC_HTTP_SERVLETCONTEXT
     */
    public static final String SERVLET_CONTEXT = "transport.http.servletContext";
    /**
     * @deprecated Please use org.apache.axis2.transport.http.HTTPConstants.MC_HTTP_SERVLETREQUEST
     */
    public static final String HTTP_SERVLET_REQUEST = "transport.http.servletRequest";

    public static final String SERVICE_MAP = "servicemap";
    public static final String SERVICE_ROOT = "serviceRoot";
    public static final String SERVICE_PATH = "servicePath";
    public static final String SERVICE_HANDLERS = "serviceHandlers";
    public static final String SERVICE_GROUP_MAP = "serviceGroupmap";
    public static final String SERVICE = "service";
    public static final String SELECT_SERVICE_TYPE = "SELECT_SERVICE_TYPE";
    public static final String IN_ACTIVATE_SERVICE = "inActivateService";
    public static final String ACTIVATE_SERVICE = "activateService";
    public static final String PHASE_LIST = "phaseList";
    public static final String PASSWORD = "password";
    public static final String OPERATION_MAP = "operationmap";
    public static final String MODULE_MAP = "modulemap";
    public static final String MODULE_ADDRESSING = "addressing";
    public static final String LIST_SERVICE_GROUPS = "listServiceGroups";
    public static final String LIST_OPERATIONS_FOR_THE_SERVICE = "listOperations";
    public static final String IS_FAULTY = "Fault";
    public static final String GLOBAL_HANDLERS = "axisconfig";

    /**
     * Keys for service/module error maps
     */
    public static final String ERROR_SERVICE_MAP = "errprservicemap";
    public static final String ERROR_MODULE_MAP = "errormodulesmap";
    public static final String ENGAGE_STATUS = "engagestatus";
    public static final String CONFIG_CONTEXT = "config_context";
    public static final String WSDL_CONTENT = "wsdl";
    public static final String ACTION_MAPPING = "actionMapping";
    public static final String OUTPUT_ACTION_MAPPING = "outputActionMapping";
    public static final String FAULT_ACTION_MAPPING = "faultActionMapping";
    public static final String FAULT_ACTION_NAME = "faultName";
    public static final String VALUE_TRUE = "true";
    public static final String VALUE_FALSE = "false";
    public static final String VALUE_OPTIONAL = "optional";
    public static final String TESTING_PATH = "target/test-resources/";
    public static final String TESTING_REPOSITORY = TESTING_PATH + "samples";
    public static final char SERVICE_NAME_SPLIT_CHAR = ':';
    public static final String SERVICE_GROUP_ID = "ServiceGroupId";
    public static final String RESPONSE_WRITTEN = "RESPONSE_WRITTEN";
    //To have a floag if the replyTo is not annon one
    public static final String DIFFERENT_EPR = "DIFFERENT_EPR";

    /**
     * This can be set in the MessageContext to give an response code the transport should use when sending it out.
     */
    public static final String RESPONSE_CODE = "RESPONSE_CODE";

    /**
     * Transport Info
     */
    public static final String OUT_TRANSPORT_INFO = "OutTransportInfo";

    /**
     * Field METHOD_NAME_ESCAPE_CHARACTER
     */
    public static final char METHOD_NAME_ESCAPE_CHARACTER = '?';
    public static final String LOGGED = "Logged";
    public static final String CONTAINER_MANAGED = "ContainerManaged";

    public static final String FAULT_INFORMATION_FOR_HEADERS = "FaultHeaders";

    /**
     * @deprecated Please use org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING
     */
    public static final String COOKIE_STRING = "Cookie";
    public static final String SESSION_COOKIE = "axis_session";
    public static final String SESSION_COOKIE_JSESSIONID = "JSESSIONID";
    public static final String CUSTOM_COOKIE_ID = "customCookieID";

    /**
     * Addressing Constants
     */
    public static final String ADDRESSING_ACTION = "WS-Addressing:Action";
    public static final String HTTP_FRONTEND_HOST_URL = "httpFrontendHostUrl";
    public static final String DEFAULT_REST_PATH = "rest";
    public static final String DEFAULT_SERVICES_PATH = "services";

    public static final int APPLICATION_FAULT = 1;
    public static final int TRANSPORT_FAULT = 2;
    public static final int SOAP_PROCESSING_FAULT = 3;

    public static final String APPLICATION_FAULT_STRING = "applicationFault";
    public static final String TRANSPORT_FAULT_STRING = "transportFault";
    public static final String SOAP_PROCESSING_FAULT_STRING = "soapProcessingFault";

    // used to handle piggy back messages with mail transport
    public static final String PIGGYBACK_MESSAGE = "piggybackMessage";

    /**
     * Field Builder Selector
     */
    public static final String BUILDER_SELECTOR = "builderselector";

    /**
     * Property name for inbound fault processor to set a fault on the message
     * context to be thrown by the client code in favour of a simple translation
     * from SOAPFault to AxisFault
     */
    public static final String INBOUND_FAULT_OVERRIDE = "inboundFaultOverride";
    
    
    /**
     * On inbound requests, the detachable input stream can be queried to get
     * the inbound length.  It can also be "detached" from the inbound http stream
     * to allow resources to be freed.
     */
    public static final String DETACHABLE_INPUT_STREAM = 
        "org.apache.axiom.om.util.DetachableInputStream";

    /** SOAP Role Configuration */
    public static final String SOAP_ROLE_CONFIGURATION_ELEMENT = "SOAPRoleConfiguration";
    public static final String SOAP_ROLE_IS_ULTIMATE_RECEIVER_ATTRIBUTE = "isUltimateReceiver";
    public static final String SOAP_ROLE_ELEMENT = "role";
    public static final String SOAP_ROLE_PLAYER_PARAMETER = "rolePlayer";
    /**
     * This is used to store Header QNames that failed mustUnderstand check in AxisEngine.
     */
    public static final String UNPROCESSED_HEADER_QNAMES = "unprocessedHeaderQNames";

    // Keys to access JAXWS Request and Response SOAP Headers
    public static final String JAXWS_OUTBOUND_SOAP_HEADERS  = "jaxws.binding.soap.headers.outbound";
    public static final String JAXWS_INBOUND_SOAP_HEADERS = "jaxws.binding.soap.headers.inbound";
    
    // If the JAX-WS WebMethod throws an exception on the server, the exception is
    // stored on the server outbound MessageContext.  This is the key to access that Throwable object.
    public static final String JAXWS_WEBMETHOD_EXCEPTION = 
        "jaxws.outbound.response.webmethod.exception";

    /**
     * A MessageContext property or client Option stating the JMS correlation id
     */
    public static final String JMS_COORELATION_ID = "JMS_COORELATION_ID";

    public static final String MODULE_VERSION ="version";

    /**
     * Following constant are used for JTA transaction support in Axis2
     */
    public static final String USER_TRANSACTION = "UserTransaction";
    public static final String TRANSACTION_MANAGER = "TransactionManager";
    public static final String SUSPENDED_TRANSACTION = "SuspendedTransaction";
    /** A message level property indicating a request to rollback the transaction associated with the message */
    public static final String SET_ROLLBACK_ONLY = "SET_ROLLBACK_ONLY";

   public static final String JSR311_ANNOTATIONS="JAXRSAnnotaion";

    /**
     * Dispatching constants
     */
    public static int MAX_HIERARCHICAL_DEPTH = 10;
    public static final String HIDDEN_SERVICE_PARAM_NAME = "hiddenService";

    public static final String CUSTOM_SCHEMA_NAME_PREFIX ="customSchemaNamePrefix" ;

    public static interface Configuration {
        public static final String ENABLE_REST = "enableREST";
        public static final String ENABLE_HTTP_CONTENT_NEGOTIATION = "httpContentNegotiation";
        public static final String ENABLE_REST_THROUGH_GET = "restThroughGet";

        public static final String ARTIFACTS_TEMP_DIR = "artifactsDIR";

        //Attachment configurations
        public static final String ENABLE_MTOM = "enableMTOM";
        public static final String MTOM_THRESHOLD = "mtomThreshold";
        public static final String CACHE_ATTACHMENTS = "cacheAttachments";
        public static final String ATTACHMENT_TEMP_DIR = "attachmentDIR";
        public static final String FILE_SIZE_THRESHOLD = "sizeThreshold";
        public static final String ENABLE_SWA = "enableSwA";
        public static final String MIME_BOUNDARY = "mimeBoundary";
        public static final String MM7_COMPATIBLE = "MM7Compatible";
        public static final String MM7_INNER_BOUNDARY = "MM7InnerBoundary";
        public static final String MM7_PART_CID = "MM7PartCID";


        public static final String REDUCE_WSDL_MEMORY_CACHE = "reduceWSDLMemoryCache";
        public static final String REDUCE_WSDL_MEMORY_TYPE  = "reduceWSDLMemoryType";

        public static final String HTTP_METHOD_GET = "GET";
        public static final String HTTP_METHOD_DELETE = "DELETE";
        public static final String HTTP_METHOD_PUT = "PUT";
        public static final String HTTP_METHOD = "HTTP_METHOD";
        public static final String HTTP_METHOD_POST = "POST";
        public static final String HTTP_METHOD_HEAD="HEAD";

        public static final String CONTENT_TYPE = "ContentType";

        public static final String CONFIG_CONTEXT_TIMEOUT_INTERVAL = "ConfigContextTimeoutInterval";

        /** @deprecated MISSPELLING */
        public static final String CONFIG_CONTEXT_TIMOUT_INTERVAL = "ConfigContextTimeoutInterval";

        public static final String TRANSPORT_IN_URL = "TransportInURL";

        public static final String URL_PARAMETER_LIST = "URLParameterList";
        public static final String URL_HTTP_LOCATION_PARAMS_LIST = "HTTPLocationParamsList";

        public static final String SEND_STACKTRACE_DETAILS_WITH_FAULTS =
                "sendStacktraceDetailsWithFaults";

        public static final String DRILL_DOWN_TO_ROOT_CAUSE_FOR_FAULT_REASON =
                "drillDownToRootCauseForFaultReason";

        public static final String DISABLE_REST   = "disableREST";
        public static final String DISABLE_SOAP11   = "disableSOAP11";
        public static final String DISABLE_SOAP12 = "disableSOAP12";

        // this will contain the keys of all the properties that will be in the message context
        public static final String TRANSPORT_URL = "TransportURL";

        /**
         * @deprecated please use org.apache.axis2.addressing.AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES
         */
        public static final String DISABLE_ADDRESSING_FOR_OUT_MESSAGES =
                "disableAddressingForOutMessages";

        // if this property is set to Boolean.TRUE then the SOAPAction header, if present,
        // will NOT be set to the value of Options.getAction(). The empty value, "", will
        // be used instead.                            L
        public static final String DISABLE_SOAP_ACTION = "disableSoapAction";

        /**
         * Field CHARACTER_SET_ENCODING
         */
        public static final String CHARACTER_SET_ENCODING = "CHARACTER_SET_ENCODING";

        /**
         * If this is set to a Boolean 'true' value, the replyTo value will not be replaced in
         * an OutIn invocation. This is useful for modules that hope to get the reply message in
         * its own manner.
         */
        public static final String USE_CUSTOM_LISTENER = "UseCustomListener";

        /**
         * If this is set to a Boolean 'true' value, then OutIn operations will always be treated
         * as async. This is useful for modules that layer async behaviour on top of sync channels.
         */
        public static final String USE_ASYNC_OPERATIONS = "UseAsyncOperations";

        /**
         * This is used to specify the message format which the message needs to be serializes.
         *
         * @see org.apache.axis2.transport.MessageFormatter
         */
        public static final String MESSAGE_TYPE = "messageType";
        
        public static final String MESSAGE_FORMATTER = "messageFormatter";
        
        /**
         * This is used to enable/disable Axis2 default fall back builder. When enabled Axis2 
         * will build any message that has a content type which is not supported by the configured
         * Axis2 builder will be built using this builder.
         */
        public static final String USE_DEFAULT_FALLBACK_BUILDER = "useDefaultFallbackBuilder";

        public static final String SOAP_RESPONSE_MEP = "soapResponseMEP";
        
        /**
         * This will be used as a key for storing transport information.
         */
        public static final String TRANSPORT_INFO_MAP = "TransportInfoMap";
        
        /**
         * If this is set to a Boolean 'true' value, then RequestResponseTransport instances will
         * not be signalled by the Dispatch phase. This is useful for modules that add wish to
         * send extra messages in the back-channel.
         */
        public static final String DISABLE_RESPONSE_ACK = "DisableResponseAck";

        /**
         * This constant is used to add an deployment life cycle listener to Axis2
         */

        public static final String DEPLOYMENT_LIFE_CYCLE_LISTENER = "deploymentLifeCycleListener";

        public static final String GENERATE_ABSOLUTE_LOCATION_URIS = "generateAbsoluteLocationURIs";

        /*
        * These are the parameters introduced to Services XML in order flexible usage of REST support
        * available in AXIS2
        */
        public static final String REST_LOCATION_PARAM="RESTLocation";
        public static final String REST_METHOD_PARAM="RESTMethod";
        public static final String REST_INPUTSERIALIZE_PARAM="RESTInputSerialization";
        public static final String REST_OUTPUTSERIALIZE_PARAM="RESTOutputSerialization";

        /**
         *  this parameter enables child first class loading.
         *  so the modules and services first use the classes in their class loader first
         */

        public static final String ENABLE_CHILD_FIRST_CLASS_LOADING="EnableChildFirstClassLoading";

        public static final String APPLICATION_XML_BUILDER_ALLOW_DTD="ApplicationXMLBuilder.allowDTD";
    }
}
