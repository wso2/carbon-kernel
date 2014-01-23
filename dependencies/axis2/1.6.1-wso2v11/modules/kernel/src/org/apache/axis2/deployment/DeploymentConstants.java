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


package org.apache.axis2.deployment;

import org.apache.axis2.Constants;

/**
 * Constants used during service/module deployment.
 */
public interface DeploymentConstants {
    public static String META_INF = "META-INF";
    public static String SERVICES_XML = "META-INF/services.xml";
    public static String MODULE_XML = "META-INF/module.xml";
    public static String SERVICE_PATH = "services";
    public static String SERVICE_DIR_PATH = "ServicesDirectory";
    public static String MODULE_PATH = "modules";
    public static String MODULE_DRI_PATH = "ModulesDirectory";

    String TAG_AXISCONFIG = "axisconfig";
    String TAG_PHASE_ORDER = "phaseOrder";
    String TAG_PARAMETER = "parameter";
    String TAG_MAPPING = "mapping";
    String TAG_PACKAGE_NAME = "packageName";
    String TAG_QNAME = "qName";
    String TAG_PACKAGE2QNAME = "packageMapping";
    String TAG_MODULE = "module";
    String TAG_MODULE_CONFIG = "moduleConfig";
    String TAG_MESSAGE = "message";
    String TAG_LISTENER = "listener";
    String TAG_LABEL = "label";
    String TAG_HANDLER = "handler";
    String TAG_TYPE = "type";
    String TAG_TARGET_RESOLVERS = "targetResolvers";
    String TAG_TARGET_RESOLVER = "targetResolver";
    String TAG_THREAD_CONTEXT_MIGRATORS = "threadContextMigrators";
    String TAG_THREAD_CONTEXT_MIGRATOR = "threadContextMigrator";
    String TAG_TRANSPORT_SENDER = "transportSender";
    String TAG_TRANSPORT_RECEIVER = "transportReceiver";
    String TAG_SERVICE_GROUP = "serviceGroup";
    String TAG_SERVICE = "service";
    String TAG_REFERENCE = "ref";
    String TAG_PHASE_LAST = "phaseLast";
    String TAG_PHASE_FIRST = "phaseFirst";
    String TAG_ORDER = "order";           // to resolve the order tag
    String TAG_OPERATION = "operation";       // operation start tag
    String TAG_PHASE = "phase";       // operation start tag
    String TAG_OBJECT_SUPPLIER = "ObjectSupplier";       // operation start tag
    String TAG_EXCLUDE_OPERATIONS = "excludeOperations";
    String TAG_MESSAGE_RECEIVER = "messageReceiver";
    String TAG_MESSAGE_RECEIVERS = "messageReceivers";
    String TAG_TRANSPORTS = "transports";
    String TAG_TRANSPORT = "transport";
    String TAG_MEP = "mep";
    String TAG_DEFAULT_MODULE_VERSION = "defaultModuleVersions";
    String TAG_CLUSTER = "clustering";
    String TAG_TRANSACTION = "transaction";
    String TAG_TRANSACTION_CONFIGURATION_CLASS = "transactionConfigurationClass";
    String TAG_TIMEOUT = "timeout";
    String TAG_MESSAGE_BUILDERS =
            "messageBuilders"; //used to add pluggable support for diffrent wire formats
    String TAG_MESSAGE_BUILDER = "messageBuilder";
    String TAG_CONTENT_TYPE = "contentType";
    String TAG_MESSAGE_FORMATTERS =
            "messageFormatters"; //used to add pluggable support for diffrent wire formats
    String TAG_MESSAGE_FORMATTER = "messageFormatter";

    String TAG_FLOW_IN = "InFlow";         // inflow start tag
    String TAG_FLOW_OUT = "OutFlow";         // outflow start tag
    String TAG_FLOW_OUT_FAULT = "OutFaultFlow";    // faultflow start tag
    String TAG_FLOW_IN_FAULT = "InFaultFlow";    // faultflow start tag

    String TAG_HOT_UPDATE = "hotupdate";
    String TAG_ANTI_JAR_LOCKING = "antiJARLocking";
    String TAG_HOT_DEPLOYMENT = "hotdeployment";
    String TAG_ALLOWOVERRIDE = "allowOverride";
    String TAG_EXPOSE = "expose";
    String TAG_EXTRACT_SERVICE_ARCHIVE = "extractServiceArchive";
    String TAG_DISPATCH_ORDER = "dispatchOrder";
    String TAG_DISPATCHER = "dispatcher";
    String TAG_DESCRIPTION = "Description";
    String TAG_CLASS_NAME = "class";
    String TAG_LIST_ID = "listId";
    String TAG_EXCLUDE_PROPERTIES= "excludeProperties";
    String TAG_INCLUDE_PROPERTIES= "includeProperties";
    String TAG_AFTER = "after";
    String TAG_BEFORE = "before";
    String TAG_SUPPORTED_POLICY_NAMESPACES = "supported-policy-namespaces";
    String TAG_NAMESPACES = "namespaces";

    //ClusterBuilder
    String TAG_NODE_MANAGER = "nodeManager";
    String TAG_STATE_MANAGER = "stateManager";
    String TAG_REPLICATION = "replication";
    String TAG_DEFAULTS = "defaults";
    String TAG_CONTEXT = "context";
    String TAG_EXCLUDE = "exclude";
    String ATTRIBUTE_CLASS = "class";

    //Deployer related cons
    String DIRECTORY = "directory";
    String EXTENSION = "extension";
    String DEPLOYER = "deployer";

    //Attachments LifecycleManager 
    String ATTACHMENTS_LIFECYCLE_MANAGER = "attachmentsLifecycleManager";

    // for parameters
    String ATTRIBUTE_NAME = "name";
    String ATTRIBUTE_WSADDRESSING = "wsaddressing";
    String TARGET_NAME_SPACE = "targetNamespace";
    String SCHEMA_NAME_SPACE = "schemaNamespace";
    String SCHEMA_ELEMENT_QUALIFIED = "elementFormDefaultQualified";
    String SCHEMA = "schema";
    String MAPPING = "mapping";
    String ATTRIBUTE_NAMESPACE = "namespace";
    String ATTRIBUTE_PACKAGE = "package";

    String ATTRIBUTE_DEFAULT_VERSION = "version";
    String ATTRIBUTE_SCOPE = "scope";
    String ATTRIBUTE_LOCKED = "locked";

    // Whether to activate a deployed service.
    String ATTRIBUTE_ACTIVATE = "activate";

    String PROPERTY_TEMP_DIR = "java.io.tmpdir";
    String DIRECTORY_CONF = "conf";
    String DIRECTORY_AXIS2_HOME = ".axis2";
    String RESOURCE_MODULES = "modules/";
    String SUFFIX_MAR = ".mar";
    String SUFFIX_JAR = ".jar";
    String SUFFIX_WSDL = ".wsdl";
    /**
     * Resource that contains the configuration.
     */
    String AXIS2_CONFIGURATION_RESOURCE =
            "org/apache/axis2/deployment/axis2_default.xml";
    String AXIS2_REPO = "repository";
    String AXIS2_CONFIGURATION_XML = "axis2.xml";
    String BOOLEAN_TRUE = "true";
    String BOOLEAN_FALSE = "false";
    char SEPARATOR_DOT = '.';
    char SEPARATOR_COLON = ':';

    String POLICY_NS_URI = Constants.URI_POLICY;
    String TAG_POLICY = "Policy";
    String TAG_POLICY_REF = "PolicyReference";
    
    String TAG_POLICY_ATTACHMENT = "PolicyAttachment";
    String TAG_APPLIES_TO = "AppliesTo";
}
