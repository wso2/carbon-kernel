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

package org.apache.axis2.wsdl.codegen;

import org.apache.axis2.util.CommandLineOption;
import org.apache.axis2.util.CommandLineOptionConstants;
import org.apache.axis2.wsdl.codegen.extension.XMLBeansExtension;
import org.apache.axis2.wsdl.i18n.CodegenMessages;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

class CodegenConfigLoader implements CommandLineOptionConstants {

    public static void loadConfig(CodeGenConfiguration config, Map<String,CommandLineOption> optionMap) {
        String outputLocation = "."; //default output directory is the current working directory
        CommandLineOption commandLineOption = loadOption(WSDL2JavaConstants.OUTPUT_LOCATION_OPTION,
                                                         WSDL2JavaConstants.OUTPUT_LOCATION_OPTION_LONG,
                                                         optionMap);
        
        if (commandLineOption != null) {
        //set isoutputSourceLocation true when user specify an output source location
            config.setoutputSourceLocation(true);
            outputLocation = commandLineOption.getOptionValue();
        }
        File outputLocationFile = new File(outputLocation);
        config.setOutputLocation(outputLocationFile);

        //check and create the directories
        if (outputLocationFile.exists()) {//$NON-SEC-2
            if (outputLocationFile.isFile()) {//$NON-SEC-2
                throw new RuntimeException(
                        CodegenMessages.getMessage("options.notADirectoryException"));
            }
        } else {
            outputLocationFile.mkdirs();//$NON-SEC-2
        }

        config.setServerSide(loadOption(WSDL2JavaConstants.SERVER_SIDE_CODE_OPTION,
                                        WSDL2JavaConstants.SERVER_SIDE_CODE_OPTION_LONG,
                                        optionMap) != null);
        config.setGenerateDeployementDescriptor(
                loadOption(WSDL2JavaConstants.GENERATE_SERVICE_DESCRIPTION_OPTION,
                           WSDL2JavaConstants.GENERATE_SERVICE_DESCRIPTION_OPTION_LONG,
                           optionMap) !=
                        null);
        config.setWriteTestCase(loadOption(WSDL2JavaConstants.GENERATE_TEST_CASE_OPTION,
                                           WSDL2JavaConstants.GENERATE_TEST_CASE_OPTION_LONG,
                                           optionMap) != null);
        config.setSkipWriteWSDLs(loadOption(null,
                                           WSDL2JavaConstants.NO_WSDLS_OPTION_LONG,
                                           optionMap) != null);
        config.setSkipMessageReceiver(loadOption(null,
                                           WSDL2JavaConstants.NO_MESSAGE_RECEIVER_OPTION_LONG,
                                           optionMap) != null);
        config.setSkipBuildXML(loadOption(null,
                                           WSDL2JavaConstants.NO_BUILD_XML_OPTION_LONG,
                                           optionMap) != null);

        boolean asyncFlagPresent =
                (loadOption(WSDL2JavaConstants.CODEGEN_ASYNC_ONLY_OPTION,
                            WSDL2JavaConstants.CODEGEN_ASYNC_ONLY_OPTION_LONG, optionMap) != null);
        boolean syncFlagPresent =
                (loadOption(WSDL2JavaConstants.CODEGEN_SYNC_ONLY_OPTION,
                            WSDL2JavaConstants.CODEGEN_SYNC_ONLY_OPTION_LONG, optionMap) != null);
        if (asyncFlagPresent && !syncFlagPresent) {
            config.setAsyncOn(true);
            config.setSyncOn(false);
        }
        if (syncFlagPresent && !asyncFlagPresent) {
            config.setAsyncOn(false);
            config.setSyncOn(true);
        }

        commandLineOption = loadOption(WSDL2JavaConstants.PACKAGE_OPTION,
                                       WSDL2JavaConstants.PACKAGE_OPTION_LONG, optionMap);
        if (commandLineOption != null) {
            config.setPackageName(commandLineOption.getOptionValue());
        }

        commandLineOption = loadOption(WSDL2JavaConstants.STUB_LANGUAGE_OPTION,
                                       WSDL2JavaConstants.STUB_LANGUAGE_OPTION_LONG, optionMap);
        if (commandLineOption != null) {
            config.setOutputLanguage(commandLineOption.getOptionValue());
        }

        commandLineOption = loadOption(WSDL2JavaConstants.DATA_BINDING_TYPE_OPTION,
                                       WSDL2JavaConstants.DATA_BINDING_TYPE_OPTION_LONG, optionMap);
        if (commandLineOption != null) {
            config.setDatabindingType(commandLineOption.getOptionValue());
        }


        commandLineOption = loadOption(WSDL2JavaConstants.UNPACK_CLASSES_OPTION,
                                       WSDL2JavaConstants.UNPACK_CLASSES_OPTION_LONG, optionMap);
        if (commandLineOption != null) {
            config.setPackClasses(false);
        }

        // source folder
        commandLineOption = loadOption(WSDL2JavaConstants.SOURCE_FOLDER_NAME_OPTION,
                                       WSDL2JavaConstants.SOURCE_FOLDER_NAME_OPTION_LONG,
                                       optionMap);
        if (commandLineOption != null) {
            config.setSourceLocation(commandLineOption.getOptionValue());
        }

        // resource folder
        commandLineOption = loadOption(WSDL2JavaConstants.RESOURCE_FOLDER_OPTION,
                                       WSDL2JavaConstants.RESOURCE_FOLDER_OPTION_LONG, optionMap);
        if (commandLineOption != null) {
            config.setResourceLocation(commandLineOption.getOptionValue());
        }

        commandLineOption = loadOption(WSDL2JavaConstants.PORT_NAME_OPTION,
                                       WSDL2JavaConstants.PORT_NAME_OPTION_LONG, optionMap);
        config.setPortName(commandLineOption != null ? commandLineOption.getOptionValue() : null);

        commandLineOption = loadOption(WSDL2JavaConstants.SERVICE_NAME_OPTION,
                                       WSDL2JavaConstants.SERVICE_NAME_OPTION_LONG, optionMap);
        config.setServiceName(
                commandLineOption != null ? commandLineOption.getOptionValue() : null);

        commandLineOption = loadOption(WSDL2JavaConstants.REPOSITORY_PATH_OPTION,
                                       WSDL2JavaConstants.REPOSITORY_PATH_OPTION_LONG, optionMap);
        config.setRepositoryPath(
                commandLineOption != null ? commandLineOption.getOptionValue() : null);

        config.setServerSideInterface(loadOption(WSDL2JavaConstants.SERVER_SIDE_INTERFACE_OPTION,
                                                 WSDL2JavaConstants.SERVER_SIDE_INTERFACE_OPTION_LONG,
                                                 optionMap) != null);

        config.setGenerateAll(loadOption(WSDL2JavaConstants.GENERATE_ALL_OPTION,
                                         WSDL2JavaConstants.GENERATE_ALL_OPTION_LONG, optionMap) !=
                null);

        //populate the external mapping
        commandLineOption = loadOption(
                WSDL2JavaConstants.EXTERNAL_MAPPING_OPTION,
                WSDL2JavaConstants.EXTERNAL_MAPPING_OPTION_LONG,
                optionMap);
        if (commandLineOption != null) {
            try {
                config.setTypeMappingFile(new File(commandLineOption.getOptionValue()));
            } catch (Exception e) {
                throw new RuntimeException(
                        CodegenMessages.getMessage("options.nomappingFile"), e);
            }
        }

        // load the namespace to package list
        commandLineOption = loadOption(
                WSDL2JavaConstants.NAME_SPACE_TO_PACKAGE_OPTION,
                WSDL2JavaConstants.NAME_SPACE_TO_PACKAGE_OPTION_LONG,
                optionMap);
        if (commandLineOption != null) {
            //the syntax for the value of the namespaces and packages is
            //to be a comma seperated list with uri=packagename,uri=packagename...
            String value = commandLineOption.getOptionValue();
            if (value != null) {
                // Try treating the values as a name=value pair separated by comma's
                if (value.indexOf('=') != -1) {
                    String valuepairs[] = value.split(",");
                    if (valuepairs.length > 0) {
                        //put them in the hash map
                        HashMap<String,String> map = new HashMap<String,String>(valuepairs.length);
                        for (int i = 0; i < valuepairs.length; i++) {
                            String values[] = valuepairs[i].split("=");
                            if (values.length == 2) {
                                map.put(values[0], values[1]);
                            }
                        }
                        config.setUri2PackageNameMap(map);
                    }
                } else {
                    // Try loading the properties from the file specified
                    try {
                        Properties p = new Properties();//$NON-SEC-3
                        p.load(new FileInputStream(value));//$NON-SEC-2//$NON-SEC-3
                        Map<String,String> map = new HashMap<String,String>();
                        for (Map.Entry<Object,Object> entry : p.entrySet()) {
                            map.put((String)entry.getKey(), (String)entry.getValue());
                        }
                        config.setUri2PackageNameMap(map);
                    } catch (IOException e) {
                        throw new RuntimeException(
                                CodegenMessages.
                                        getMessage("options.noFile", value), e);
                    }
                }
            }
        }

        commandLineOption =
                loadOption(WSDL2JavaConstants.UNWRAP_PARAMETERS,
                           WSDL2JavaConstants.UNWRAP_PARAMETERS_LONG,
                           optionMap);
        if (commandLineOption != null) {
            config.setParametersWrapped(false);
        }

        commandLineOption =
                loadOption(WSDL2JavaConstants.WSDL_VERSION_OPTION,
                           WSDL2JavaConstants.WSDL_VERSION_OPTION_LONG,
                           optionMap);
        if (commandLineOption != null) {
            String optionValue = commandLineOption.getOptionValue();

            if (WSDL2JavaConstants.WSDL_VERSION_2.equals(optionValue) ||
                    WSDL2JavaConstants.WSDL_VERSION_2_OPTIONAL.equals(optionValue)) {
                //users can say either 2.0 or 2 - we  just set it to the constant
                config.setWSDLVersion(WSDL2JavaConstants.WSDL_VERSION_2);
            } //ignore the other cases - they'll be taken as 1.1

        }

        config
                .setFlattenFiles(loadOption(
                        WSDL2JavaConstants.FLATTEN_FILES_OPTION,
                        WSDL2JavaConstants.FLATTEN_FILES_OPTION_LONG, optionMap) != null);

        commandLineOption = loadOption(
                WSDL2JavaConstants.BACKWORD_COMPATIBILITY_OPTION,
                WSDL2JavaConstants.BACKWORD_COMPATIBILITY_OPTION_LONG,
                optionMap);
        if (commandLineOption != null) {
            config.setBackwordCompatibilityMode(true);
        }

        commandLineOption = loadOption(
                WSDL2JavaConstants.SUPPRESS_PREFIXES_OPTION,
                WSDL2JavaConstants.SUPPRESS_PREFIXES_OPTION_LONG,
                optionMap);
        if (commandLineOption != null) {
            config.setSuppressPrefixesMode(true);
        }

        commandLineOption = loadOption(XMLBeansExtension.XSDCONFIG_OPTION,
                                       XMLBeansExtension.XSDCONFIG_OPTION_LONG,
                                       optionMap);
        if (commandLineOption != null) {
            config.getProperties().put(XMLBeansExtension.XSDCONFIG_OPTION, 
                    commandLineOption.getOptionValue());
        }

        //setting http proxy host and http proxy port
        commandLineOption = loadOption(null, WSDL2JavaConstants.HTTP_PROXY_HOST_OPTION_LONG, optionMap);
        if (commandLineOption != null) {
            System.setProperty("http.proxyHost", commandLineOption.getOptionValue());//$NON-SEC-2
        }

        commandLineOption = loadOption(null, WSDL2JavaConstants.HTTP_PROXY_PORT_OPTION_LONG, optionMap);
        if (commandLineOption != null) {
            System.setProperty("http.proxyPort", commandLineOption.getOptionValue());//$NON-SEC-2
        }

        commandLineOption = loadOption(WSDL2JavaConstants.EXCLUDE_PAKAGES_OPTION,
                WSDL2JavaConstants.EXCLUDE_PAKAGES_OPTION_LONG, optionMap);
        if (commandLineOption != null){
            config.setExcludeProperties(commandLineOption.getOptionValue());
        }

        commandLineOption = loadOption(WSDL2JavaConstants.SKELTON_INTERFACE_NAME_OPTION,
                WSDL2JavaConstants.SKELTON_INTERFACE_NAME_OPTION_LONG, optionMap);
        if (commandLineOption != null){
            config.setSkeltonInterfaceName(commandLineOption.getOptionValue());
        }

        commandLineOption = loadOption(WSDL2JavaConstants.SKELTON_CLASS_NAME_OPTION,
                WSDL2JavaConstants.SKELTON_CLASS_NAME_OPTION_LONG, optionMap);
        if (commandLineOption != null){
            config.setSkeltonClassName(commandLineOption.getOptionValue());
        }

        commandLineOption = loadOption(WSDL2JavaConstants.EXCEPTION_BASE_CLASS_OPTION,
                WSDL2JavaConstants.EXCEPTION_BASE_CLASS_OPTION_LONG, optionMap);
        if (commandLineOption != null){
            config.setExceptionBaseClassName(commandLineOption.getOptionValue());
        }

        // setting the overrid and all ports options
        config.setAllPorts(loadOption(WSDL2JavaConstants.All_PORTS_OPTION,
                                      WSDL2JavaConstants.All_PORTS_OPTION_LONG,
                                      optionMap) != null);

        config.setOverride(loadOption(WSDL2JavaConstants.OVERRIDE_OPTION,
                                      WSDL2JavaConstants.OVERRIDE_OPTION_LONG,
                                      optionMap) != null);

        config.setOverrideAbsoluteAddress(loadOption(WSDL2JavaConstants.OVERRIDE_ABSOLUTE_ADDRESS_OPTION,
                                      WSDL2JavaConstants.OVERRIDE_ABSOLUTE_ADDRESS_OPTION_LONG,
                                      optionMap) != null);

        config.setUseOperationName(loadOption(WSDL2JavaConstants.USE_OPERATION_NAME,
                                     WSDL2JavaConstants.USE_OPERATION_NAME_LONG, optionMap) != null);

        // loop through the map and find parameters having the extra prefix.
        //put them in the property map
        for (Map.Entry<String,CommandLineOption> entry : optionMap.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(WSDL2JavaConstants.EXTRA_OPTIONTYPE_PREFIX)) {
                //add this to the property map
                config.getProperties().put(key.replaceFirst(
                        WSDL2JavaConstants.EXTRA_OPTIONTYPE_PREFIX, ""), entry.getValue().getOptionValue());
            }
        }


    }

    private static CommandLineOption loadOption(String shortOption, String longOption,
                                                Map<String,CommandLineOption> options) {
        //short option gets precedence
        CommandLineOption option = null;
        if (longOption != null) {
            option = options.get(longOption);
            if (option != null) {
                return option;
            }
        }
        if (shortOption != null) {
            option = options.get(shortOption);
        }

        return option;
    }

}
