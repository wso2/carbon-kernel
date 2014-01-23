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

package org.apache.ws.java2wsdl;

import org.apache.axis2.description.java2wsdl.Java2WSDLConstants;
import org.apache.axis2.description.java2wsdl.Java2WSDLUtils;
import org.apache.ws.java2wsdl.utils.Java2WSDLCommandLineOption;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

public class Java2WSDLCodegenEngine implements Java2WSDLConstants {
    private Java2WSDLBuilder java2WsdlBuilder;
    public static final String WSDL_FILENAME_SUFFIX = ".wsdl";
    public static final String COMMA = ",";
    private File outputFile;

    public Java2WSDLCodegenEngine(Map<String,Java2WSDLCommandLineOption> optionsMap) throws Exception {
        //create a new  Java2WSDLBuilder and populate it
        Java2WSDLCommandLineOption option = loadOption(Java2WSDLConstants.CLASSNAME_OPTION, Java2WSDLConstants.CLASSNAME_OPTION_LONG, optionsMap);
        String className = option == null ? null : option.getOptionValue();

        if (className == null || className.length() == 0) {
            throw new Exception("class name must be present!");
        }

        //Now we are done with loading the basic values - time to create the builder
        java2WsdlBuilder = new Java2WSDLBuilder(resolveOutputStream(className, optionsMap),
                                                className,
                                                resolveClassLoader(optionsMap));
        
        configureJava2WSDLBuilder(optionsMap, className);
    }

    public void generate() throws Exception {
        java2WsdlBuilder.generateWSDL();
    }

    private FileOutputStream resolveOutputStream(String className, Map<String,Java2WSDLCommandLineOption> optionsMap) throws Exception
    {
        Java2WSDLCommandLineOption option = loadOption(Java2WSDLConstants.OUTPUT_LOCATION_OPTION,
                                                       Java2WSDLConstants.OUTPUT_LOCATION_OPTION_LONG, optionsMap);
        String outputFolderName = option == null ? System.getProperty("user.dir") : option.getOptionValue();

        File outputFolder;
        outputFolder = new File(outputFolderName);
        if (!outputFolder.exists()) {
            outputFolder.mkdirs();
        } else if (!outputFolder.isDirectory()) {
            throw new Exception("The specivied location " + outputFolderName + "is not a folder");
        }
        
        option = loadOption(Java2WSDLConstants.OUTPUT_FILENAME_OPTION,
                            Java2WSDLConstants.OUTPUT_FILENAME_OPTION_LONG, optionsMap);
        String outputFileName = option == null ? null : option.getOptionValue();
        //derive a file name from the class name if the filename is not specified
        if (outputFileName == null) {
            outputFileName = Java2WSDLUtils.getSimpleClassName(className) + WSDL_FILENAME_SUFFIX;
        }
    
        //first create a file in the given location
        outputFile = new File(outputFolder, outputFileName);
        FileOutputStream out;
        try {
            if (!outputFile.exists()) {
                outputFile.createNewFile();
            }
            out = new FileOutputStream(outputFile);
        } catch (IOException e) {
            throw new Exception(e);
        }
        
        return out;
    }
    
    private ClassLoader resolveClassLoader(Map<String,Java2WSDLCommandLineOption> optionsMap) throws Exception
    {
        // if the class path is present, create a URL class loader with those
        //class path entries present. if not just take the  TCCL
        Java2WSDLCommandLineOption option = loadOption(Java2WSDLConstants.CLASSPATH_OPTION,
                Java2WSDLConstants.CLASSPATH_OPTION_LONG, optionsMap);

        ClassLoader classLoader;

        if (option != null) {
            ArrayList<String> optionValues = option.getOptionValues();
            URL[] urls = new URL[optionValues.size()];
            String[] classPathEntries = optionValues.toArray(new String[optionValues.size()]);

            try {
                for (int i = 0; i < classPathEntries.length; i++) {
                    String classPathEntry = classPathEntries[i];
                    if(classPathEntry == null) {
                        continue;
                    }
                    //this should be a file(or a URL)
                    if (Java2WSDLUtils.isURL(classPathEntry)) {
                        urls[i] = new URL(classPathEntry);
                    } else {
                        urls[i] = new File(classPathEntry).toURL();
                    }
                }
            } catch (MalformedURLException e) {
                throw new Exception(e);
            }

            classLoader = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());

        } else {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        
        return classLoader;
    }

    private void configureJava2WSDLBuilder(Map<String,Java2WSDLCommandLineOption> optionsMap, String className) throws Exception
    {
        //set the other parameters to the builder
        Java2WSDLCommandLineOption option = loadOption(Java2WSDLConstants.SCHEMA_TARGET_NAMESPACE_OPTION,
                Java2WSDLConstants.SCHEMA_TARGET_NAMESPACE_OPTION_LONG, optionsMap);
        java2WsdlBuilder.setSchemaTargetNamespace(option == null ? null : option.getOptionValue());

        option = loadOption(Java2WSDLConstants.SCHEMA_TARGET_NAMESPACE_PREFIX_OPTION,
                Java2WSDLConstants.SCHEMA_TARGET_NAMESPACE_PREFIX_OPTION_LONG, optionsMap);
        java2WsdlBuilder.setSchemaTargetNamespacePrefix(option == null ? null : option.getOptionValue());

        option = loadOption(Java2WSDLConstants.TARGET_NAMESPACE_OPTION,
                Java2WSDLConstants.TARGET_NAMESPACE_OPTION_LONG, optionsMap);
        java2WsdlBuilder.setTargetNamespace(option == null ? null : option.getOptionValue());

        option = loadOption(Java2WSDLConstants.TARGET_NAMESPACE_PREFIX_OPTION,
                Java2WSDLConstants.TARGET_NAMESPACE_PREFIX_OPTION_LONG, optionsMap);
        java2WsdlBuilder.setTargetNamespacePrefix(option == null ? null : option.getOptionValue());

        option = loadOption(Java2WSDLConstants.SERVICE_NAME_OPTION,
                Java2WSDLConstants.SERVICE_NAME_OPTION_LONG, optionsMap);
        java2WsdlBuilder.setServiceName(option == null ? Java2WSDLUtils.getSimpleClassName(className) : option.getOptionValue());

        option = loadOption(Java2WSDLConstants.STYLE_OPTION,
                Java2WSDLConstants.STYLE_OPTION, optionsMap);
        if (option != null) {
            java2WsdlBuilder.setStyle(option.getOptionValue());
        }

        option = loadOption(Java2WSDLConstants.LOCATION_OPTION,
                Java2WSDLConstants.LOCATION_OPTION, optionsMap);
        if (option != null) {
            java2WsdlBuilder.setLocationUri(option.getOptionValue());
        }

        option = loadOption(Java2WSDLConstants.USE_OPTION,
                Java2WSDLConstants.USE_OPTION, optionsMap);
        if (option != null) {
            java2WsdlBuilder.setUse(option.getOptionValue());
        }
        
        option = loadOption(Java2WSDLConstants.ATTR_FORM_DEFAULT_OPTION,
                Java2WSDLConstants.ATTR_FORM_DEFAULT_OPTION_LONG, optionsMap);
        java2WsdlBuilder.setAttrFormDefault(option == null ? null : option.getOptionValue());
        
        option = loadOption(Java2WSDLConstants.ELEMENT_FORM_DEFAULT_OPTION,
                Java2WSDLConstants.ELEMENT_FORM_DEFAULT_OPTION_LONG, optionsMap);
        java2WsdlBuilder.setElementFormDefault(option == null ? null : option.getOptionValue());
        
        option = loadOption(Java2WSDLConstants.EXTRA_CLASSES_DEFAULT_OPTION,
                            Java2WSDLConstants.EXTRA_CLASSES_DEFAULT_OPTION_LONG, optionsMap);
        java2WsdlBuilder.setExtraClasses(option == null ? new ArrayList<String>() : option.getOptionValues());
        
        option = loadOption(Java2WSDLConstants.NAMESPACE_GENERATOR_OPTION,
                            Java2WSDLConstants.NAMESPACE_GENERATOR_OPTION_LONG, optionsMap);
        if ( option != null ) {
            java2WsdlBuilder.setNsGenClassName(option.getOptionValue());
        }
        
        option = loadOption(Java2WSDLConstants.SCHEMA_GENERATOR_OPTION,
                            Java2WSDLConstants.SCHEMA_GENERATOR_OPTION_LONG, optionsMap);
        if ( option != null ) {
            java2WsdlBuilder.setSchemaGenClassName(option.getOptionValue());
        }

        option = loadOption(Java2WSDLConstants.JAVA_PKG_2_NSMAP_OPTION,
                            Java2WSDLConstants.JAVA_PKG_2_NSMAP_OPTION_LONG, optionsMap);
        java2WsdlBuilder.setPkg2nsMap(loadJavaPkg2NamespaceMap(option));

        option = loadOption(Java2WSDLConstants.WSDL_VERSION_OPTION,
                           Java2WSDLConstants.WSDL_VERSION_OPTION_LONG,
                           optionsMap);
        if (option != null) {
            String optionValue = option.getOptionValue();
            if (Java2WSDLConstants.WSDL_VERSION_2.equals(optionValue) ||
                    Java2WSDLConstants.WSDL_VERSION_2_OPTIONAL.equals(optionValue)) {
                //users can say either 2.0 or 2 - we  just set it to the constant
                java2WsdlBuilder.setWSDLVersion(Java2WSDLConstants.WSDL_VERSION_2);
            } //ignore the other cases - they'll be taken as 1.1
        }

        option = loadOption(Java2WSDLConstants.DOC_LIT_BARE,
                           Java2WSDLConstants.DOC_LIT_BARE_LONG,
                           optionsMap);
        if (option != null) {
            java2WsdlBuilder.setGenerateDocLitBare(true);
        }

        option = loadOption(Java2WSDLConstants.CUSTOM_SCHEMA_LOCATION,
                           Java2WSDLConstants.CUSTOM_SCHEMA_LOCATION_LONG,
                           optionsMap);
        if (option != null) {
            java2WsdlBuilder.setCustomSchemaLocation(option.getOptionValue());
        }
        option = loadOption(Java2WSDLConstants.SCHEMA_MAPPING_FILE_LOCATION,
                           Java2WSDLConstants.SCHEMA_MAPPING_FILE_LOCATION_LONG,
                           optionsMap);
        if (option != null) {
            java2WsdlBuilder.setMappingFileLocation(option.getOptionValue());
        }

        option = loadOption(Java2WSDLConstants.DISALLOW_NILLABLE_ELEMENTS_OPTION,
                           Java2WSDLConstants.DISALLOW_NILLABLE_ELEMENTS_OPTION_LONG,
                           optionsMap);
        if (option != null) {
            java2WsdlBuilder.setNillableElementsAllowed(false);
        }

        option = loadOption(Java2WSDLConstants.DISABLE_BINDING_SOAP11, null, optionsMap);
        if (option != null) {
            java2WsdlBuilder.setDisableSOAP11(true);
        }
        option = loadOption(Java2WSDLConstants.DISABLE_BINDING_SOAP12, null, optionsMap);
        if (option != null) {
            java2WsdlBuilder.setDisableSOAP12(true);
        }
        option = loadOption(Java2WSDLConstants.DISABLE_BINDING_REST, null, optionsMap);
        if (option != null) {
            java2WsdlBuilder.setDisableREST(true);
        }

        option = loadOption(Java2WSDLConstants.MESSAGE_PART_NAME_OPTION,
                           Java2WSDLConstants.MESSAGE_PART_NAME_OPTION_LONG,
                           optionsMap);
        if (option != null) {
            java2WsdlBuilder.setMessagePartName(option.getOptionValue());
        }

        option = loadOption(Java2WSDLConstants.PORT_TYPE_NAME_OPTION,
                           Java2WSDLConstants.PORT_TYPE_NAME_OPTION_LONG,
                           optionsMap);
        if (option != null) {
            java2WsdlBuilder.setPortTypeName(option.getOptionValue());
        }

        option = loadOption(null,Java2WSDLConstants.SOAP11_BINDING_NAME_OPTION_LONG, optionsMap);
        if (option != null) {
            java2WsdlBuilder.setSoap11BindingName(option.getOptionValue());
        }

        option = loadOption(null,Java2WSDLConstants.SOAP12_BINDING_NAME_OPTION_LONG, optionsMap);
        if (option != null) {
            java2WsdlBuilder.setSoap12BindingName(option.getOptionValue());
        }

        option = loadOption(null,Java2WSDLConstants.REST_BINDING_NAME_OPTION_LONG, optionsMap);
        if (option != null) {
            java2WsdlBuilder.setRestBindingName(option.getOptionValue());
        }

        option = loadOption(Java2WSDLConstants.REQUEST_ELEMENT_SUFFIX_OPTION,
                           Java2WSDLConstants.REQUEST_ELEMENT_SUFFIX_OPTION_LONG,
                           optionsMap);
        if (option != null) {
            java2WsdlBuilder.setRequestElementSuffix(option.getOptionValue());
        }

        option = loadOption(Java2WSDLConstants.DISALLOW_ANON_TYPES_OPTION,
                Java2WSDLConstants.DISALLOW_ANON_TYPES_OPTION_LONG,
                optionsMap);
        if (option != null) {
            java2WsdlBuilder.setAnonymousTypesAllowed(false);
        }

    }
    
    private Java2WSDLCommandLineOption loadOption(String shortOption, String longOption,
                Map<String,Java2WSDLCommandLineOption> options) {
        
        //short option gets precedence
        Java2WSDLCommandLineOption option = null;
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
    
    protected void addToSchemaLocationMap(String optionValue) throws Exception
    {
        
        
        
    }
    
    protected Map<String,String> loadJavaPkg2NamespaceMap(Java2WSDLCommandLineOption option) throws Exception 
    { 
        Map<String,String> pkg2nsMap = new Hashtable<String,String>();
        if (option != null) 
        {
            ArrayList<String> optionValues = option.getOptionValues();
            String anOptionValue ;
            for ( int count = 0 ; count < optionValues.size() ; ++count )
            {
                anOptionValue = optionValues.get(count).trim();
                
                //an option value will be of the form [java package, namespace]
                //hence we take the two substrings starting after '[' and upto ',' and
                //starting after ',' and upto ']'
                if (anOptionValue.charAt(0) == '[' && anOptionValue.charAt(anOptionValue.length()-1) == ']') {
                    pkg2nsMap.put(anOptionValue.substring(1, anOptionValue.indexOf(COMMA)).trim(),
                                            anOptionValue.substring(anOptionValue.indexOf(COMMA) + 1, anOptionValue.length() - 1).trim());
                } else {
                    throw new Exception("Invalid syntax for the " + Java2WSDLConstants.JAVA_PKG_2_NSMAP_OPTION
                            + " (" + Java2WSDLConstants.JAVA_PKG_2_NSMAP_OPTION_LONG
                            + ") option; must be [package,namespace]");
                }
            }
        }
        return pkg2nsMap;
    }

	public File getOutputFile() {
    	return outputFile;
    }
    
}
