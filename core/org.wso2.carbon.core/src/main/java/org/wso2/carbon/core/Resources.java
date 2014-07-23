package org.wso2.carbon.core;

/**
 * Contains all the constants related to the registry artifacts stored in the registry
 */
public class Resources {

    @Deprecated
    public static final String SERVICES_METAFILES_DIR = "servicemetafiles";
    @Deprecated
    public static final String MODULES_METAFILES_DIR = "modulemetafiles";
    public static final String SERVICES_DIRECTORY = "ServicesDirectory";
    public static final String METAFILE_EXTENSION = ".xml";
    public static final String PERSISTENCE_FACTORY_PARAM_NAME = "PersistenceFactory";

    public static final String NAME = "name";
    public static final String VERSION = "version";
    public static final String POLICIES = "policies";
    public static final String POLICY = "policy";

    public static final String WS_POLICY_NAMESPACE = "http://schemas.xmlsoap.org/ws/2004/09/policy";

    public static final String SERVICES = "/services/";
    public static final String OPERATION = "operation";
    public static final String MESSAGES = "/messages/";
    public static final String PARAMETERS = "parameters";
    public static final String SUCCESSFULLY_ADDED = "successfullyAdded";

//    public static final String LOGGERS = LOGGING + "loggers/";
//    public static final String APPENDERS = LOGGING + "appenders/";

    public static final String ROOT = "/repository/";
    public static final String COMPONENTS = ROOT + "components/";
    public static final String TRANSPORTS = ROOT + "transports/";
    public static final String SERVICE_GROUPS = ROOT + "axis2/service-groups/";
    public static final String MODULES = ROOT + "axis2/modules/";
    public static final String CONFIG = ROOT + "config/";
    public static final String LOGGING = COMPONENTS + "org.wso2.carbon.logging/";


    public static final String CONNECTION_PROPS = ROOT + "connection/props";
    public static final String OWNER_APPLICATION = "ownerApp";

    public static final class ServiceProperties {
        public static final String SERVICE_XML_TAG = "service";
        /**
         * Append the condition for 'name' attribute as necessary to get the exact service needed.
         * ex. ROOT_XPATH+[@name="xxx"]
         */
        public static final String ROOT_XPATH = ServiceGroupProperties.ROOT_XPATH + SERVICE_XML_TAG;

        //        public static final String OPERATIONS = "/operations/";
        public static final String BINDINGS = "bindings";
        public static final String BINDING_XML_TAG = "binding";

        public static final String DOCUMENTATION = "serviceDocumentation";
        public static final String ACTIVE = "serviceActive";
        public static final String POLICY_TYPE = "policyType";
        public static final String POLICY_UUID = "policyUUID";
        public static final String MESSAGE_IN_POLICY_UUID = "messageInPolicyUuid";
        public static final String MESSAGE_OUT_POLICY_UUID = "messageOutPolicyUuid";
        public static final String EXPOSED_ON_ALL_TANSPORTS = "exposedAllTransports";
        public static final String IS_UT_ENABLED = "utEnabled";

        public static final String OPERATION_NAME = "serviceOperationName";
        public static final String BINDING_NAME = "serviceBindingName";
        public static final String MESSAGE_TYPE = "serviceMessageType";
        public static final String DEPLOYED_TIME = "serviceDeployedTime";
    }

    public static final class ServiceGroupProperties {
        public static final Integer TYPE = 0;
        public static final String SERVICE_GROUP_XML_TAG = "serviceGroup";
        /**
         * Since we are using OMElement instead of OMDocuments, we should refer /serviceGroup by xpath "/"
         */
        public static final String ROOT_XPATH = "/";
        //        public static final String ROOT_XPATH = "/"+ SERVICE_GROUP_XML_TAG +"[1]";
        public static final String LAST_UPDATED = "lastUpdated";
        public static final String HASH_VALUE = "hashValue";
        public static final String ID = "name";
    }

    public static final class ModuleProperties {
        public static final String TYPE = "type";
        public static final String MODULE_XML_TAG = "module";
        public static final String ROOT_XPATH = "/";
        //        public static final String ROOT_XPATH = "/"+ MODULE_XML_TAG +"[1]";
        public static final String VERSION_XPATH = ROOT_XPATH + Resources.VERSION;
        //        public static final String FILE_NAME = "modules";
        public static final String VERSION_ID = "id";
        //        public static final String NAME = "module.name";
//        public static final String VERSION = "module.version";
        public static final String GLOBALLY_ENGAGED = "globallyEngaged";
        public static final String POLICY_TYPE = "policyType";
        //        public static final String POLICY_UUID = "uuid";
        public static final String UNDEFINED = "undefined";
    }

    public static final class ParameterProperties {
        public static final String PARAMETER = "parameter";
        public static final String NAME = "name";
        public static final String VALUE = "value";
        public static final String TYPE = "type";
        public static final String LOCKED = "locked";
    }

    public static final class LoggerProperties {
        public static final String NAME = "name";
        public static final String LOG_LEVEL = "log.level";
        public static final String ADDITIVITY = "additivity";
    }

    public static final class AppenderProperties {
        public static final String NAME = "name";
        public static final String PATTERN = "pattern";
        public static final String LOG_FILE_NAME = "log.file.name";
        public static final String IS_FILE_APPENDER = "is.file.appender";
        public static final String THRESHOLD = "threshold";
        public static final String FACILITY = "facility";
        public static final String SYS_LOG_HOST = "sys.log.host";
        public static final String IS_SYS_LOG_APPENDER = "is.sys.log.appender";
    }

    public static final class Associations {
        public static final String ASSOCIATION_XML_TAG = "association";
        public static final String DESTINATION_PATH = "destinationPath";
        public static final String ENGAGED_MODULES = "engagedModules";
        public static final String EXPOSED_TRANSPORTS = "exposedTransports";
        public static final String USER_STORE = "userStore";
        public static final String REQUIRED_MODULES = "requiredModules";
        public static final String PRIVATE_KEYSTORE = "service-keystore";

        public static final String TYPE = ModuleProperties.TYPE;
    }

    public static final class Transports {
        //       public static final String PROTOCOL_NAME = "Protocol.Name";
//        public static final String IS_ENABLED = "Transport.Enabled";
    }


    public static final class SecurityManagement {
        //resources
        public static final String ROLE_XML_TAG = "role";
        public static final String ROLENAME_XML_ATTR = "rolename";

        public static final String DEFAULT_STORE = "default";
        public static final String ASSOCIATION_STORE_GROUP = "user-group-store";
        public static final String KEY_STORES = RegistryResources.ROOT + "security/key-stores";
        public static final String ADMIN_ROLE = "admin";

        //phantom resource
        public static final String PRIMARY_KEYSTORE_PHANTOM_RESOURCE = RegistryResources.ROOT +
                "security/key-stores/carbon-primary-ks";

        //properties
        public static final String PROP_PASSWORD = "password";
        public static final String PROP_ROLE = "role";
        public static final String PROP_DESCRIPTION = "description";
        public static final String PROP_USERS = "users";
        public static final String PROP_PRIVATE_KEY_ALIAS = "privatekeyAlias";
        public static final String PROP_TYPE = "type";
        public static final String PROP_PRIVATE_KEY_PASS = "privatekeyPass";

        //primary key store
        public static final String SERVER_PRIMARY_KEYSTORE_FILE = "Security.KeyStore.Location";
        public static final String SERVER_PRIMARY_KEYSTORE_PASSWORD = "Security.KeyStore.Password";
        public static final String SERVER_PRIMARY_KEYSTORE_KEY_ALIAS = "Security.KeyStore.KeyAlias";
        public static final String SERVER_PRIVATE_KEY_PASSWORD = "Security.KeyStore.KeyPassword";
        public static final String SERVER_PRIMARY_KEYSTORE_TYPE = "Security.KeyStore.Type";

        //generated pub. key - multitenancy scenario
        public static final String TENANT_PUBKEY_RESOURCE = RegistryResources.ROOT +
                "security/pub-key";
    }


}

