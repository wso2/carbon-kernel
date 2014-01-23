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

package org.apache.ws.java2wsdl.utils;

import org.apache.axis2.description.java2wsdl.Java2WSDLConstants;

public class Java2WSDLOptionsValidator implements Java2WSDLConstants {
    public boolean isInvalid(Java2WSDLCommandLineOption option) {
        boolean invalid;
        String optionType = option.getOptionType();

        invalid = !(
                Java2WSDLConstants.ATTR_FORM_DEFAULT_OPTION.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.ATTR_FORM_DEFAULT_OPTION_LONG.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.CLASSNAME_OPTION.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.CLASSNAME_OPTION_LONG.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.CLASSPATH_OPTION.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.CLASSPATH_OPTION_LONG.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.DOC_LIT_BARE.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.DOC_LIT_BARE_LONG.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.ELEMENT_FORM_DEFAULT_OPTION.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.ELEMENT_FORM_DEFAULT_OPTION_LONG.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.EXTRA_CLASSES_DEFAULT_OPTION.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.EXTRA_CLASSES_DEFAULT_OPTION_LONG.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.JAVA_PKG_2_NSMAP_OPTION.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.JAVA_PKG_2_NSMAP_OPTION_LONG.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.LOCATION_OPTION.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.LOCATION_OPTION_LONG.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.NAMESPACE_GENERATOR_OPTION.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.NAMESPACE_GENERATOR_OPTION_LONG.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.OUTPUT_FILENAME_OPTION.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.OUTPUT_FILENAME_OPTION_LONG.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.OUTPUT_LOCATION_OPTION.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.OUTPUT_LOCATION_OPTION_LONG.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.SCHEMA_GENERATOR_OPTION.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.SCHEMA_GENERATOR_OPTION_LONG.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.SCHEMA_TARGET_NAMESPACE_OPTION.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.SCHEMA_TARGET_NAMESPACE_OPTION_LONG.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.SCHEMA_TARGET_NAMESPACE_PREFIX_OPTION.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.SCHEMA_TARGET_NAMESPACE_PREFIX_OPTION_LONG.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.SERVICE_NAME_OPTION.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.SERVICE_NAME_OPTION_LONG.equalsIgnoreCase(optionType)||
                Java2WSDLConstants.STYLE_OPTION.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.STYLE_OPTION_LONG.equalsIgnoreCase(optionType)||
                Java2WSDLConstants.TARGET_NAMESPACE_OPTION.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.TARGET_NAMESPACE_OPTION_LONG.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.TARGET_NAMESPACE_PREFIX_OPTION.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.TARGET_NAMESPACE_PREFIX_OPTION_LONG.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.USE_OPTION.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.DISALLOW_NILLABLE_ELEMENTS_OPTION.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.DISALLOW_NILLABLE_ELEMENTS_OPTION_LONG.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.USE_OPTION_LONG.equalsIgnoreCase(optionType)||
                Java2WSDLConstants.WSDL_VERSION_OPTION.equalsIgnoreCase(optionType)||
                Java2WSDLConstants.CUSTOM_SCHEMA_LOCATION.equalsIgnoreCase(optionType)||
                Java2WSDLConstants.CUSTOM_SCHEMA_LOCATION_LONG.equalsIgnoreCase(optionType)||
                Java2WSDLConstants.DISABLE_BINDING_SOAP11.equalsIgnoreCase(optionType)||
                Java2WSDLConstants.DISABLE_BINDING_SOAP12.equalsIgnoreCase(optionType)||
                Java2WSDLConstants.DISABLE_BINDING_REST.equalsIgnoreCase(optionType)||
                Java2WSDLConstants.MESSAGE_PART_NAME_OPTION.equalsIgnoreCase(optionType)||
                Java2WSDLConstants.MESSAGE_PART_NAME_OPTION_LONG.equalsIgnoreCase(optionType)||
                Java2WSDLConstants.PORT_TYPE_NAME_OPTION.equalsIgnoreCase(optionType)||
                Java2WSDLConstants.PORT_TYPE_NAME_OPTION_LONG.equalsIgnoreCase(optionType)||
                Java2WSDLConstants.SOAP11_BINDING_NAME_OPTION_LONG.equalsIgnoreCase(optionType)||
                Java2WSDLConstants.SOAP12_BINDING_NAME_OPTION_LONG.equalsIgnoreCase(optionType)||
                Java2WSDLConstants.REST_BINDING_NAME_OPTION_LONG.equalsIgnoreCase(optionType)||
                Java2WSDLConstants.REQUEST_ELEMENT_SUFFIX_OPTION.equalsIgnoreCase(optionType)||
                Java2WSDLConstants.REQUEST_ELEMENT_SUFFIX_OPTION_LONG.equalsIgnoreCase(optionType)||
                Java2WSDLConstants.DISALLOW_ANON_TYPES_OPTION.equalsIgnoreCase(optionType)||
                Java2WSDLConstants.DISALLOW_ANON_TYPES_OPTION_LONG.equalsIgnoreCase(optionType)||
                Java2WSDLConstants.SCHEMA_MAPPING_FILE_LOCATION.equalsIgnoreCase(optionType)||
                Java2WSDLConstants.SCHEMA_MAPPING_FILE_LOCATION_LONG.equalsIgnoreCase(optionType)||
                Java2WSDLConstants.WSDL_VERSION_OPTION_LONG.equalsIgnoreCase(optionType)
        );
        return invalid;
    }
}
