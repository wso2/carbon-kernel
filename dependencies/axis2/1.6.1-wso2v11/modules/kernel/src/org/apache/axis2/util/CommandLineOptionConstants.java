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

package org.apache.axis2.util;

public interface CommandLineOptionConstants {

    interface WSDL2JavaConstants {
        String All_PORTS_OPTION = "ap";
        String All_PORTS_OPTION_LONG = "all-ports";
        String BACKWORD_COMPATIBILITY_OPTION = "b";
        String BACKWORD_COMPATIBILITY_OPTION_LONG = "backword-compatible";
        String CODEGEN_ASYNC_ONLY_OPTION = "a";
        String CODEGEN_ASYNC_ONLY_OPTION_LONG = "async";
        String CODEGEN_SYNC_ONLY_OPTION = "s";
        String CODEGEN_SYNC_ONLY_OPTION_LONG = "sync";
        String DATA_BINDING_TYPE_OPTION = "d";
        String DATA_BINDING_TYPE_OPTION_LONG = "databinding-method";
        String EXTERNAL_MAPPING_OPTION = "em";
        String EXTERNAL_MAPPING_OPTION_LONG = "external-mapping";
        String FLATTEN_FILES_OPTION = "f";
        String FLATTEN_FILES_OPTION_LONG = "flatten-files";
        String GENERATE_ALL_OPTION = "g";
        String GENERATE_ALL_OPTION_LONG = "generate-all";
        String GENERATE_SERVICE_DESCRIPTION_OPTION = "sd";
        String GENERATE_SERVICE_DESCRIPTION_OPTION_LONG = "service-description";
        String GENERATE_TEST_CASE_OPTION = "t";
        String GENERATE_TEST_CASE_OPTION_LONG = "test-case";
        String NAME_SPACE_TO_PACKAGE_OPTION = "ns2p";
        String NAME_SPACE_TO_PACKAGE_OPTION_LONG = "namespace2package";
        String NO_BUILD_XML_OPTION_LONG = "noBuildXML";
        String NO_MESSAGE_RECEIVER_OPTION_LONG = "noMessageReceiver";
        String NO_WSDLS_OPTION_LONG = "noWSDL";
        String OUTPUT_LOCATION_OPTION = "o";
        String OUTPUT_LOCATION_OPTION_LONG = "output";
        String OVERRIDE_OPTION = "or";
        String OVERRIDE_OPTION_LONG = "over-ride";
        String EXCEPTION_BASE_CLASS_OPTION = "ebc";
        String EXCEPTION_BASE_CLASS_OPTION_LONG = "exception-base-class";
        String OVERRIDE_ABSOLUTE_ADDRESS_OPTION = "oaa";
        String OVERRIDE_ABSOLUTE_ADDRESS_OPTION_LONG = "override-absolute-address";
        String PACKAGE_OPTION = "p";
        String PACKAGE_OPTION_LONG = "package";
        String PORT_NAME_OPTION = "pn";
        String PORT_NAME_OPTION_LONG = "port-name";
        String REPOSITORY_PATH_OPTION = "r";
        String REPOSITORY_PATH_OPTION_LONG = "repository-path";
        String RESOURCE_FOLDER_OPTION = "R";
        String RESOURCE_FOLDER_OPTION_LONG = "resource-folder";
        String SERVER_SIDE_CODE_OPTION = "ss";
        String SERVER_SIDE_CODE_OPTION_LONG = "server-side";
        String SERVER_SIDE_INTERFACE_OPTION = "ssi";
        String SERVER_SIDE_INTERFACE_OPTION_LONG = "serverside-interface";
        String SERVICE_NAME_OPTION = "sn";
        String SERVICE_NAME_OPTION_LONG = "service-name";
        String SOURCE_FOLDER_NAME_OPTION = "S";
        String SOURCE_FOLDER_NAME_OPTION_LONG = "source-folder";
        String STUB_LANGUAGE_OPTION = "l";
        String STUB_LANGUAGE_OPTION_LONG = "language";
        String SUPPRESS_PREFIXES_OPTION = "sp";
        String SUPPRESS_PREFIXES_OPTION_LONG = "suppress-prefixes";
        String UNPACK_CLASSES_OPTION = "u";
        String UNPACK_CLASSES_OPTION_LONG = "unpack-classes";
        String UNWRAP_PARAMETERS = "uw";
        String UNWRAP_PARAMETERS_LONG = "unwrap-params";
        String WSDL_LOCATION_URI_OPTION = "uri";
        String WSDL_VERSION_OPTION = "wv";
        String WSDL_VERSION_OPTION_LONG = "wsdl-version";
        String HTTP_PROXY_HOST_OPTION_LONG = "http-proxy-host";
        String HTTP_PROXY_PORT_OPTION_LONG = "http-proxy-port";
        String EXCLUDE_PAKAGES_OPTION = "ep";
        String EXCLUDE_PAKAGES_OPTION_LONG = "exclude-packages";
        String SKELTON_INTERFACE_NAME_OPTION = "sin";
        String SKELTON_INTERFACE_NAME_OPTION_LONG = "skelton-interface-name";
        String USE_OPERATION_NAME = "uon";
        String USE_OPERATION_NAME_LONG = "use-operation-name";
        String SKELTON_CLASS_NAME_OPTION = "scn";
        String SKELTON_CLASS_NAME_OPTION_LONG = "skelton-class-name";

        String INVALID_OPTION = "INVALID_OPTION";
        String EXTRA_OPTIONTYPE_PREFIX = "E";

        String WSDL_VERSION_2 = "2.0";
        String WSDL_VERSION_2_OPTIONAL = "2";
        String WSDL_VERSION_1 = "1.1";
    }

    interface ExtensionArguments {
        String WITHOUT_DATABIND_CODE = "wdc";
    }

    public static final String SOLE_INPUT = "SOLE_INPUT";
}
