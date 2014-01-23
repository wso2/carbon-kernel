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

package org.apache.axis2.tool.ant;

import org.apache.axis2.util.CommandLineOption;
import org.apache.axis2.util.CommandLineOptionConstants;
import org.apache.axis2.util.CommandLineOptionParser;
import org.apache.axis2.util.URLProcessor;
import org.apache.axis2.wsdl.codegen.CodeGenerationEngine;
import org.apache.axis2.wsdl.util.ConfigPropertyFileLoader;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Parameter;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

public class AntCodegenTask extends Task {

    private String wsdlFileName = null;
    private String output = ".";
    private String language = ConfigPropertyFileLoader.getDefaultLanguage();
    private String packageName = URLProcessor.DEFAULT_PACKAGE;
    private String databindingName = ConfigPropertyFileLoader.getDefaultDBFrameworkName();
    private String portName = null;
    private String serviceName = null;

    private boolean asyncOnly = false;
    private boolean syncOnly = false;
    private boolean serverSide = false;
    private boolean testcase = false;
    private boolean generateServiceXml = false;
    private boolean generateAllClasses = false;
    private boolean unpackClasses = false;
    private boolean serverSideInterface = false;

    private boolean allPorts = false;
    private boolean backwardCompatible = false;
    private boolean flattenFiles = false;
    private boolean skipMessageReceiver = false;
    private boolean skipBuildXML = false;
    private boolean skipWSDL = false;
    private boolean overWrite = false;
    private boolean suppressPrefixes = false;
    private Properties props = new Properties();

    private String repositoryPath = null;
    private String externalMapping = null;
    private String wsdlVersion = null;
    private String targetSourceFolderLocation = null;
    private String targetResourcesFolderLocation = null;
    private boolean unwrap = false;

    private String namespaceToPackages = null;

    private Path classpath;


    /**
     * 
     */
    public AntCodegenTask() {
        super();
    }


    /**
     * Sets the classpath.
     *
     * @return Returns Path.
     */
    public Path createClasspath() {
        if (classpath == null) {
            classpath = new Path(getProject());
            classpath = classpath.concatSystemClasspath();
        }
        return classpath.createPath();
    }

    /**
     * Set the reference to an optional classpath
     *
     * @param r the id of the Ant path instance to act as the classpath
     */
    public void setClasspathRef(Reference r) {
        createClasspath().setRefid(r);
    }

    /** @return  */
    public boolean isServerSideInterface() {
        return serverSideInterface;
    }

    /** @param serverSideInterface  */
    public void setServerSideInterface(boolean serverSideInterface) {
        this.serverSideInterface = serverSideInterface;
    }

    /** Fills the option map. This map is passed onto the code generation API to generate the code. */
    private Map fillOptionMap() {
        Map optionMap = new HashMap();

        ////////////////////////////////////////////////////////////////
        //WSDL file name
        optionMap.put(
                CommandLineOptionConstants.WSDL2JavaConstants.WSDL_LOCATION_URI_OPTION,
                new CommandLineOption(
                        CommandLineOptionConstants.WSDL2JavaConstants.WSDL_LOCATION_URI_OPTION,
                        getStringArray(wsdlFileName)));

        //WSDL version
        if (wsdlVersion != null) {
            optionMap.put(
                    CommandLineOptionConstants.WSDL2JavaConstants.WSDL_VERSION_OPTION,
                    new CommandLineOption(
                            CommandLineOptionConstants.WSDL2JavaConstants.WSDL_VERSION_OPTION,
                            getStringArray(wsdlVersion)));
        }

        // repository path
        if (repositoryPath != null) {
            optionMap.put(
                    CommandLineOptionConstants.WSDL2JavaConstants.REPOSITORY_PATH_OPTION,
                    new CommandLineOption(
                            CommandLineOptionConstants.WSDL2JavaConstants.REPOSITORY_PATH_OPTION,
                            getStringArray(repositoryPath)));
        }

        // external mapping
        if (externalMapping != null) {
            optionMap.put(
                    CommandLineOptionConstants.WSDL2JavaConstants.EXTERNAL_MAPPING_OPTION,
                    new CommandLineOption(
                            CommandLineOptionConstants.WSDL2JavaConstants.EXTERNAL_MAPPING_OPTION,
                            getStringArray(externalMapping)));
        }

        // target source folder location
        if (targetSourceFolderLocation != null) {
            optionMap.put(
                    CommandLineOptionConstants.WSDL2JavaConstants.SOURCE_FOLDER_NAME_OPTION,
                    new CommandLineOption(
                            CommandLineOptionConstants.WSDL2JavaConstants.SOURCE_FOLDER_NAME_OPTION,
                            getStringArray(targetSourceFolderLocation)));
        }

        // target source folder location
        if (targetResourcesFolderLocation != null) {
            optionMap.put(
                    CommandLineOptionConstants.WSDL2JavaConstants.RESOURCE_FOLDER_OPTION,
                    new CommandLineOption(
                            CommandLineOptionConstants.WSDL2JavaConstants.RESOURCE_FOLDER_OPTION,
                            getStringArray(targetResourcesFolderLocation)));
        }

        // target source folder location
        if (unwrap) {
            optionMap.put(
                    CommandLineOptionConstants.WSDL2JavaConstants.UNWRAP_PARAMETERS,
                    new CommandLineOption(
                            CommandLineOptionConstants.WSDL2JavaConstants.UNWRAP_PARAMETERS,
                            new String[0]));
        }

        //output location
        optionMap.put(
                CommandLineOptionConstants.WSDL2JavaConstants.OUTPUT_LOCATION_OPTION,
                new CommandLineOption(
                        CommandLineOptionConstants.WSDL2JavaConstants.OUTPUT_LOCATION_OPTION,
                        getStringArray(output)));
        //////////////////////////////////////////////////////////////////
        // Databinding type
        optionMap.put(
                CommandLineOptionConstants.WSDL2JavaConstants.DATA_BINDING_TYPE_OPTION,
                new CommandLineOption(
                        CommandLineOptionConstants.WSDL2JavaConstants.DATA_BINDING_TYPE_OPTION,
                        getStringArray(databindingName)));

        // Async only option - forcing to generate async methods only
        if (asyncOnly) {
            optionMap.put(
                    CommandLineOptionConstants.WSDL2JavaConstants.CODEGEN_ASYNC_ONLY_OPTION,
                    new CommandLineOption(
                            CommandLineOptionConstants.WSDL2JavaConstants.CODEGEN_ASYNC_ONLY_OPTION,
                            new String[0]));
        }
        // Sync only option - forcing to generate Sync methods only
        if (syncOnly) {
            optionMap.put(
                    CommandLineOptionConstants.WSDL2JavaConstants.CODEGEN_SYNC_ONLY_OPTION,
                    new CommandLineOption(
                            CommandLineOptionConstants.WSDL2JavaConstants.CODEGEN_SYNC_ONLY_OPTION,
                            new String[0]));
        }

        //Package
        optionMap.put(
                CommandLineOptionConstants.WSDL2JavaConstants.PACKAGE_OPTION,
                new CommandLineOption(
                        CommandLineOptionConstants.WSDL2JavaConstants.PACKAGE_OPTION,
                        getStringArray(packageName)));

        //stub language
        optionMap.put(
                CommandLineOptionConstants.WSDL2JavaConstants.STUB_LANGUAGE_OPTION,
                new CommandLineOption(
                        CommandLineOptionConstants.WSDL2JavaConstants.STUB_LANGUAGE_OPTION,
                        getStringArray(language)));

        //server side and generate services.xml options
        if (serverSide) {
            optionMap.put(
                    CommandLineOptionConstants.WSDL2JavaConstants.SERVER_SIDE_CODE_OPTION,
                    new CommandLineOption(
                            CommandLineOptionConstants.WSDL2JavaConstants.SERVER_SIDE_CODE_OPTION,
                            new String[0]));

            //services XML generation - effective only when specified as the server side
            if (generateServiceXml) {
                optionMap.put(
                        CommandLineOptionConstants.WSDL2JavaConstants
                                .GENERATE_SERVICE_DESCRIPTION_OPTION,
                        new CommandLineOption(
                                CommandLineOptionConstants.WSDL2JavaConstants
                                        .GENERATE_SERVICE_DESCRIPTION_OPTION,
                                new String[0]));
            }
            //generate all option - Only valid when generating serverside code
            if (generateAllClasses) {
                optionMap.put(
                        CommandLineOptionConstants.WSDL2JavaConstants.GENERATE_ALL_OPTION,
                        new CommandLineOption(
                                CommandLineOptionConstants.WSDL2JavaConstants.GENERATE_ALL_OPTION,
                                new String[0]));
            }

        }

        //generate the test case
        if (testcase) {
            optionMap.put(
                    CommandLineOptionConstants.WSDL2JavaConstants.GENERATE_TEST_CASE_OPTION,
                    new CommandLineOption(
                            CommandLineOptionConstants.WSDL2JavaConstants.GENERATE_TEST_CASE_OPTION,
                            new String[0]));
        }

        //Unwrap classes option - this determines whether the generated classes are inside the stub/MR
        //or gets generates as seperate classes
        if (unpackClasses) {
            optionMap.put(
                    CommandLineOptionConstants.WSDL2JavaConstants.UNPACK_CLASSES_OPTION,
                    new CommandLineOption(
                            CommandLineOptionConstants.WSDL2JavaConstants.UNPACK_CLASSES_OPTION,
                            new String[0]));
        }

        //server side interface option
        if (serverSideInterface) {
            optionMap.put(
                    CommandLineOptionConstants.WSDL2JavaConstants.SERVER_SIDE_INTERFACE_OPTION,
                    new CommandLineOption(
                            CommandLineOptionConstants.WSDL2JavaConstants.SERVER_SIDE_INTERFACE_OPTION,
                            new String[0]));
        }

        if (allPorts) {
            optionMap.put(
                    CommandLineOptionConstants.WSDL2JavaConstants.All_PORTS_OPTION,
                    new CommandLineOption(
                            CommandLineOptionConstants.WSDL2JavaConstants.All_PORTS_OPTION,
                            new String[0]));
        }

        if (backwardCompatible) {
            optionMap.put(
                    CommandLineOptionConstants.WSDL2JavaConstants.BACKWORD_COMPATIBILITY_OPTION,
                    new CommandLineOption(
                            CommandLineOptionConstants.WSDL2JavaConstants.BACKWORD_COMPATIBILITY_OPTION,
                            new String[0]));
        }

        if (flattenFiles) {
            optionMap.put(
                    CommandLineOptionConstants.WSDL2JavaConstants.FLATTEN_FILES_OPTION,
                    new CommandLineOption(
                            CommandLineOptionConstants.WSDL2JavaConstants.FLATTEN_FILES_OPTION,
                            new String[0]));
        }

        if (skipMessageReceiver) {
            optionMap.put(
                    CommandLineOptionConstants.WSDL2JavaConstants.NO_MESSAGE_RECEIVER_OPTION_LONG,
                    new CommandLineOption(
                            CommandLineOptionConstants.WSDL2JavaConstants.NO_MESSAGE_RECEIVER_OPTION_LONG,
                            new String[0]));
        }

        if (skipBuildXML) {
            optionMap.put(
                    CommandLineOptionConstants.WSDL2JavaConstants.NO_BUILD_XML_OPTION_LONG,
                    new CommandLineOption(
                            CommandLineOptionConstants.WSDL2JavaConstants.NO_BUILD_XML_OPTION_LONG,
                            new String[0]));
        }

        if (skipWSDL) {
            optionMap.put(
                    CommandLineOptionConstants.WSDL2JavaConstants.NO_WSDLS_OPTION_LONG,
                    new CommandLineOption(
                            CommandLineOptionConstants.WSDL2JavaConstants.NO_WSDLS_OPTION_LONG,
                            new String[0]));
        }

        if (overWrite) {
            optionMap.put(
                    CommandLineOptionConstants.WSDL2JavaConstants.OVERRIDE_OPTION,
                    new CommandLineOption(
                            CommandLineOptionConstants.WSDL2JavaConstants.OVERRIDE_OPTION,
                            new String[0]));
        }

        if (suppressPrefixes) {
            optionMap.put(
                    CommandLineOptionConstants.WSDL2JavaConstants.SUPPRESS_PREFIXES_OPTION,
                    new CommandLineOption(
                            CommandLineOptionConstants.WSDL2JavaConstants.SUPPRESS_PREFIXES_OPTION,
                            new String[0]));
        }

        Iterator iterator = props.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String key = CommandLineOptionConstants.WSDL2JavaConstants.EXTRA_OPTIONTYPE_PREFIX + entry.getKey();
            optionMap.put(
                    key,
                    new CommandLineOption(
                            key,
                            new String[]{(String) entry.getValue()}));
        }

        optionMap.put(
                CommandLineOptionConstants.WSDL2JavaConstants.SERVICE_NAME_OPTION,
                new CommandLineOption(
                        CommandLineOptionConstants.WSDL2JavaConstants.SERVICE_NAME_OPTION,
                        new String[] { serviceName }));

        optionMap.put(
                CommandLineOptionConstants.WSDL2JavaConstants.PORT_NAME_OPTION,
                new CommandLineOption(
                        CommandLineOptionConstants.WSDL2JavaConstants.PORT_NAME_OPTION,
                        new String[] { portName }));
        // set the namespaces
        optionMap.put(
                CommandLineOptionConstants.WSDL2JavaConstants.NAME_SPACE_TO_PACKAGE_OPTION,
                new CommandLineOption(
                        CommandLineOptionConstants.WSDL2JavaConstants.NAME_SPACE_TO_PACKAGE_OPTION,
                        new String[] { namespaceToPackages }));

        return optionMap;
    }

    /**
     * Utility method to convert a string into a single item string[]
     *
     * @param value
     * @return Returns String[].
     */
    private String[] getStringArray(String value) {
        String[] values = new String[1];
        values[0] = value;
        return values;
    }

    public void execute() throws BuildException {
        try {
            /*
             * This needs the ClassLoader we use to load the task
             * have all the dependancies set, hope that
             * is ok for now
             */

            AntClassLoader cl = new AntClassLoader(
                    getClass().getClassLoader(),
                    getProject(),
                    classpath == null ? createClasspath() : classpath,
                    false);

            Thread.currentThread().setContextClassLoader(cl);

            Map commandLineOptions = this.fillOptionMap();
            CommandLineOptionParser parser =
                    new CommandLineOptionParser(commandLineOptions);
            new CodeGenerationEngine(parser).generate();
        } catch (Throwable e) {
            throw new BuildException(e);
        }

    }

    public void setPortName(String portName) {
        this.portName = portName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setGenerateAllClasses(boolean generateAllClasses) {
        this.generateAllClasses = generateAllClasses;
    }

    public void setUnpackClasses(boolean unpackClasses) {
        this.unpackClasses = unpackClasses;
    }

    public void setWsdlFileName(String wsdlFileName) {
        this.wsdlFileName = wsdlFileName;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setAsyncOnly(boolean asyncOnly) {
        this.asyncOnly = asyncOnly;
    }

    public void setSyncOnly(boolean syncOnly) {
        this.syncOnly = syncOnly;
    }

    public void setServerSide(boolean serverSide) {
        this.serverSide = serverSide;
    }

    public void setTestcase(boolean testcase) {
        this.testcase = testcase;
    }

    public void setGenerateServiceXml(boolean generateServiceXml) {
        this.generateServiceXml = generateServiceXml;
    }

    public void setRepositoryPath(String repositoryPath) {
        this.repositoryPath = repositoryPath;
    }

    public void setExternalMapping(String externalMapping) {
        this.externalMapping = externalMapping;
    }

    public void setWsdlVersion(String wsdlVersion) {
        this.wsdlVersion = wsdlVersion;
    }

    public void setTargetSourceFolderLocation(String targetSourceFolderLocation) {
        this.targetSourceFolderLocation = targetSourceFolderLocation;
    }

    public void setTargetResourcesFolderLocation(String targetResourcesFolderLocation) {
        this.targetResourcesFolderLocation = targetResourcesFolderLocation;
    }

    public void setUnwrap(boolean unwrap) {
        this.unwrap = unwrap;
    }

    /** @return Returns Path. */
    public Path getClasspath() {
        return classpath;
    }

    /** @param path  */
    public void setClasspath(Path path) {
        classpath = path;
    }

    public String getDatabindingName() {
        return databindingName;
    }

    public void setDatabindingName(String databindingName) {
        this.databindingName = databindingName;
    }

    public String getNamespaceToPackages() {
        return namespaceToPackages;
    }

    public void setNamespaceToPackages(String namespaceToPackages) {
        this.namespaceToPackages = namespaceToPackages;
    }

    public void addConfiguredParameter(Parameter prop) {
        props.setProperty(prop.getName(), prop.getValue());
    }

    public void setSuppressPrefixes(boolean suppressPrefixes) {
        this.suppressPrefixes = suppressPrefixes;
    }

    public void setOverWrite(boolean overWrite) {
        this.overWrite = overWrite;
    }

    public void setSkipWSDL(boolean skipWSDL) {
        this.skipWSDL = skipWSDL;
    }

    public void setSkipBuildXML(boolean skipBuildXML) {
        this.skipBuildXML = skipBuildXML;
    }

    public void setSkipMessageReceiver(boolean skipMessageReceiver) {
        this.skipMessageReceiver = skipMessageReceiver;
    }

    public void setFlattenFiles(boolean flattenFiles) {
        this.flattenFiles = flattenFiles;
    }

    public void setBackwardCompatible(boolean backwardCompatible) {
        this.backwardCompatible = backwardCompatible;
    }

    public void setAllPorts(boolean allPorts) {
        this.allPorts = allPorts;
    }
}
