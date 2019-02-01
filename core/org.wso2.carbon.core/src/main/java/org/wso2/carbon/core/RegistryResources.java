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
package org.wso2.carbon.core;

/**
 * Contains all the constants related to the registry artifacts stored in the registry
 */
public final class RegistryResources {

    public static final String ROOT = "/repository/";
    public static final String COMPONENTS = ROOT + "components/";
    public static final String TRANSPORTS = ROOT + "transports/";
    public static final String SERVICE_GROUPS = ROOT + "axis2/service-groups/";
    public static final String MODULES = ROOT + "axis2/modules/";
    public static final String CONFIG = ROOT + "config/";
    public static final String LOGGING = COMPONENTS + "org.wso2.carbon.logging/";

    public static final String SERVICES = "/services/";
    public static final String OPERATIONS = "/operations/";
    public static final String MESSAGES = "/messages/";
    public static final String PARAMETERS = "/parameters/";
    public static final String POLICIES = "/policies/";
    public static final String LOGGERS = LOGGING + "loggers/";
    public static final String APPENDERS = LOGGING + "appenders/";

    public static final String CONNECTION_PROPS = ROOT + "connection/props";
    public static final String NAME = "name";
    public static final String SUCCESSFULLY_ADDED = "successfully.added";
    public static final String OWNER_APPLICATION = "owner.app";

    public static final class ServiceProperties {
        public static final String OPERATIONS = "/operations/";
        public static final String BINDINGS = "/bindings/";

        public static final String DOCUMENTATION = "service.documentation";
        public static final String ACTIVE = "service.active";
        public static final String POLICY_TYPE = "policy.type";
        public static final String POLICY_UUID = "policy.uuid";
        public static final String MESSAGE_IN_POLICY_UUID = "message.in.policy.uuid";
        public static final String MESSAGE_OUT_POLICY_UUID = "message.out.policy.uuid";
        public static final String EXPOSED_ON_ALL_TANSPORTS = "exposed.all.transports";
        public static final String IS_UT_ENABLED = "ut.enabled";

        public static final String OPERATION_NAME = "service.operation.name";
        public static final String BINDING_NAME = "service.binding.name";
        public static final String MESSAGE_TYPE = "service.message.type";
        public static final String DEPLOYED_TIME = "service.deployed.time";
    }

    public static final class ServiceGroupProperties {
        public static final String LAST_UPDATED = "last.updated";
        public static final String HASH_VALUE = "hash.value";
    }

    public static final class ModuleProperties {
        public static final String NAME = "module.name";
        public static final String VERSION = "module.version";
        public static final String GLOBALLY_ENGAGED = "globally.engaged";
        public static final String POLICY_TYPE = "policy.type";
        public static final String POLICY_UUID = "policy.uuid";
        public static final String UNDEFINED = "undefined";
    }
    
    public static final class ParameterProperties {
        public static final String NAME = "param.name";
        public static final String VALUE = "param.value";
        public static final String TYPE = "param.type";
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
        /**
         * ENGAGED_MODULES association is different from other association like
         * EXPOSED_TRANSPORTS because here we don't use <association> tag. It simply
         * has a <module> tag with name, version, type.
         */
        public static final String ENGAGED_MODULES = "engaged.modules";
        public static final String EXPOSED_TRANSPORTS = "exposed.transports";
        public static final String USER_STORE = "user.store";
        public static final String REQUIRED_MODULES = "required.modules";
        public static final String PRIVATE_KEYSTORE = "service-keystore";
    }

    public static final class Transports {
        public static final String PROTOCOL_NAME = "Protocol.Name";
        public static final String IS_ENABLED = "Transport.Enabled";
    }

    
    public static final class SecurityManagement {
        //resources
      
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

        public static final String DEFAULT_SECURITY_CERTIFICATE_ALIAS = "wso2carbon";

        //Registry store
        public static final String SERVER_REGISTRY_KEYSTORE_FILE = "Security.RegistryKeyStore.Location";
        public static final String SERVER_REGISTRY_KEYSTORE_PASSWORD = "Security.RegistryKeyStore.Password";
        public static final String SERVER_REGISTRY_KEYSTORE_KEY_ALIAS = "Security.RegistryKeyStore.KeyAlias";
        public static final String SERVER_REGISTRY_KEY_PASSWORD = "Security.RegistryKeyStore.KeyPassword";
        public static final String SERVER_REGISTRY_KEYSTORE_TYPE = "Security.RegistryKeyStore.Type";

        //Internal key store which is used for encryption and decryption purpose
        public static final String SERVER_INTERNAL_KEYSTORE_FILE = "Security.InternalKeyStore.Location";
        public static final String SERVER_INTERNAL_KEYSTORE_PASSWORD = "Security.InternalKeyStore.Password";
        public static final String SERVER_INTERNAL_KEYSTORE_KEY_ALIAS = "Security.InternalKeyStore.KeyAlias";
        public static final String SERVER_INTERNAL_PRIVATE_KEY_PASSWORD = "Security.InternalKeyStore.KeyPassword";
        public static final String SERVER_INTERNAL_KEYSTORE_TYPE = "Security.InternalKeyStore.Type";

        //generated pub. key - multitenancy scenario
        public static final String TENANT_PUBKEY_RESOURCE = RegistryResources.ROOT +
                                                            "security/pub-key";
    }

}
