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

package org.apache.axis2.wsdl.util;

import org.apache.axis2.util.CommandLineOption;
import org.apache.axis2.util.CommandLineOptionConstants;
import org.apache.axis2.util.OptionsValidator;
import org.apache.axis2.wsdl.codegen.extension.XMLBeansExtension;

public class WSDL2JavaOptionsValidator implements CommandLineOptionConstants, OptionsValidator {

    public boolean isInvalid(CommandLineOption option) {

        boolean invalid;
        String optionType = option.getOptionType();

        if (optionType.startsWith(WSDL2JavaConstants.EXTRA_OPTIONTYPE_PREFIX)) {
            invalid = false;
        } else {
            invalid = !(WSDL2JavaConstants.All_PORTS_OPTION.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.All_PORTS_OPTION_LONG.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.BACKWORD_COMPATIBILITY_OPTION.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.BACKWORD_COMPATIBILITY_OPTION_LONG.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.CODEGEN_ASYNC_ONLY_OPTION.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.CODEGEN_ASYNC_ONLY_OPTION_LONG.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.CODEGEN_SYNC_ONLY_OPTION.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.CODEGEN_SYNC_ONLY_OPTION_LONG.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.DATA_BINDING_TYPE_OPTION.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.DATA_BINDING_TYPE_OPTION_LONG.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.EXTERNAL_MAPPING_OPTION.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.EXTERNAL_MAPPING_OPTION_LONG.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.FLATTEN_FILES_OPTION.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.FLATTEN_FILES_OPTION_LONG.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.GENERATE_ALL_OPTION.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.GENERATE_ALL_OPTION_LONG.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.GENERATE_SERVICE_DESCRIPTION_OPTION.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.GENERATE_SERVICE_DESCRIPTION_OPTION_LONG.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.GENERATE_TEST_CASE_OPTION.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.GENERATE_TEST_CASE_OPTION_LONG.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.NAME_SPACE_TO_PACKAGE_OPTION.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.NAME_SPACE_TO_PACKAGE_OPTION_LONG.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.NO_BUILD_XML_OPTION_LONG.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.NO_MESSAGE_RECEIVER_OPTION_LONG.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.NO_WSDLS_OPTION_LONG.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.OUTPUT_LOCATION_OPTION.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.OUTPUT_LOCATION_OPTION_LONG.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.OVERRIDE_OPTION.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.OVERRIDE_OPTION_LONG.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.EXCEPTION_BASE_CLASS_OPTION.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.EXCEPTION_BASE_CLASS_OPTION_LONG.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.OVERRIDE_ABSOLUTE_ADDRESS_OPTION.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.OVERRIDE_ABSOLUTE_ADDRESS_OPTION_LONG.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.PACKAGE_OPTION.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.PACKAGE_OPTION_LONG.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.PORT_NAME_OPTION.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.PORT_NAME_OPTION_LONG.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.REPOSITORY_PATH_OPTION.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.REPOSITORY_PATH_OPTION_LONG.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.RESOURCE_FOLDER_OPTION.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.RESOURCE_FOLDER_OPTION_LONG.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.SERVER_SIDE_CODE_OPTION.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.SERVER_SIDE_CODE_OPTION_LONG.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.SERVER_SIDE_INTERFACE_OPTION.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.SERVER_SIDE_INTERFACE_OPTION_LONG.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.SERVICE_NAME_OPTION.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.SERVICE_NAME_OPTION_LONG.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.SOURCE_FOLDER_NAME_OPTION.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.SOURCE_FOLDER_NAME_OPTION_LONG.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.STUB_LANGUAGE_OPTION.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.STUB_LANGUAGE_OPTION_LONG.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.SUPPRESS_PREFIXES_OPTION.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.SUPPRESS_PREFIXES_OPTION_LONG.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.UNPACK_CLASSES_OPTION.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.UNPACK_CLASSES_OPTION_LONG.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.UNWRAP_PARAMETERS.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.UNWRAP_PARAMETERS_LONG.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.WSDL_LOCATION_URI_OPTION.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.WSDL_VERSION_OPTION.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.WSDL_VERSION_OPTION_LONG.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.HTTP_PROXY_HOST_OPTION_LONG.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.HTTP_PROXY_PORT_OPTION_LONG.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.EXCLUDE_PAKAGES_OPTION.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.EXCLUDE_PAKAGES_OPTION_LONG.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.SKELTON_INTERFACE_NAME_OPTION.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.SKELTON_INTERFACE_NAME_OPTION_LONG.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.SKELTON_CLASS_NAME_OPTION.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.SKELTON_CLASS_NAME_OPTION_LONG.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.USE_OPERATION_NAME.equalsIgnoreCase(optionType) ||
                    WSDL2JavaConstants.USE_OPERATION_NAME_LONG.equalsIgnoreCase(optionType) ||
                    XMLBeansExtension.XSDCONFIG_OPTION.equalsIgnoreCase(optionType) ||
                    XMLBeansExtension.XSDCONFIG_OPTION_LONG.equalsIgnoreCase(optionType)
            );

        }

        return invalid;
    }


}
