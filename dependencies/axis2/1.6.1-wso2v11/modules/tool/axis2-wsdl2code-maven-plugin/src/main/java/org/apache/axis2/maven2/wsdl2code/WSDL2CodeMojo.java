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

package org.apache.axis2.maven2.wsdl2code;

import org.apache.axis2.util.CommandLineOption;
import org.apache.axis2.util.CommandLineOptionConstants;
import org.apache.axis2.util.CommandLineOptionParser;
import org.apache.axis2.wsdl.codegen.CodeGenerationEngine;
import org.apache.axis2.wsdl.codegen.CodeGenerationException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;


/**
 * @description A Mojo for generating Java sources from a WSDL.
 * @goal wsdl2code
 * @phase generate-sources
 * @requiresDependencyResolution test
 */
public class WSDL2CodeMojo extends AbstractMojo {
    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @read-only
     * @required
     */
    private MavenProject project;

    /**
     * The WSDL file, which is being read.
     *
     * @parameter expression="${axis2.wsdl2code.wsdlFile}" default-value="src/main/axis2/service.wsdl"
     */
    private String wsdlFile;

    /**
     * The output directory, where the generated sources are being created.
     *
     * @parameter expression="${axis2.wsdl2code.target}" default-value="${project.build.directory}/generated-sources/axis2/wsdl2code"
     */
    private File outputDirectory;

    /**
     * Package name of the generated sources; will be used to create a package structure below the
     * output directory.
     *
     * @parameter expression="${axis2.wsdl2code.package}"     * 
     */
    private String packageName;

    /**
     * The programming language of the generated sources.
     *
     * @parameter expression="${axis2.wsdl2code.language}" default-value="java"
     */
    private String language;

    /**
     * The databinding framework, which is being used.
     *
     * @parameter expression="${axis2.wsdl2code.databindingName}" default-value="adb"
     */
    private String databindingName;

    /**
     * Port name, for which to generate sources. By default, sources will be generated for all
     * ports.
     *
     * @parameter expression="${axis2.wsdl2code.portName}"
     */
    private String portName;

    /**
     * Service name, for which to generate sources. By default, sources will be generated for all
     * services.
     *
     * @parameter expression="${axis2.wsdl2code.serviceName}"
     */
    private String serviceName;

    /**
     * Mode, for which sources are being generated; either of "sync", "async" or "both".
     *
     * @parameter expression="${axis2.wsdl2code.syncMode}" default-value="both"
     */
    private String syncMode;

    /**
     * Whether server side sources are being generated.
     *
     * @parameter expression="${axis2.wsdl2code.generateServerSide}" default-value="false"
     */
    private boolean generateServerSide;

    /**
     * Whether to generate sources for a test case.
     *
     * @parameter expression="${axis2.wsdl2code.generateTestCase}" default-value="false"
     */
    private boolean generateTestcase;

    /**
     * Whether to generate a "services.xml" file.
     *
     * @parameter expression="${axis2.wsdl2code.generateServicesXml}" default-value="false"
     */
    private boolean generateServicesXml;

    /**
     * Whether to generate simply all classes. This is only valid in conjunction with
     * "generateServerSide".
     *
     * @parameter expression="${axis2.wsdl2code.generateAllClasses}" default-value="false"
     */
    private boolean generateAllClasses;

    /**
     * Whether to unpack classes.
     *
     * @parameter expression="${axis2.wsdl2code.unpackClasses}" default-value="false"
     */
    private boolean unpackClasses;

    /**
     * Whether to generate the server side interface.
     *
     * @parameter expression="${axis2.wsdl2code.generateServerSideInterface}" default-value="false"
     */
    private boolean generateServerSideInterface = false;

    /**
     * @parameter expression="${axis2.wsdl2code.repositoryPath}"
     */
    private String repositoryPath = null;

    /**
     * @parameter expression="${axis2.wsdl2code.externalMapping}"
     */
    private String externalMapping = null;

    /**
     * @parameter expression="${axis2.wsdl2code.wsdlVersion}"
     */
    private String wsdlVersion = null;

    /**
     * @parameter expression="${axis2.wsdl2code.targetSourceFolderLocation}"
     */
    private String targetSourceFolderLocation = null;

    /**
     * @parameter expression="${axis2.wsdl2code.targetResourcesFolderLocation}"
     */
    private String targetResourcesFolderLocation = null;

    /**
     * @parameter expression="${axis2.wsdl2code.unwrap}" default-value="false" *
     */
    private boolean unwrap = false;

    /**
     * @parameter expression="${axis2.wsdl2code.allPorts}" default-value="false" *
     */
    private boolean allPorts = false;

    /**
     * @parameter expression="${axis2.wsdl2code.backwardCompatible}" default-value="false" *
     */
    private boolean backwardCompatible = false;

    /**
     * @parameter expression="${axis2.wsdl2code.flattenFiles}" default-value="false" *
     */
    private boolean flattenFiles = false;

    /**
     * @parameter expression="${axis2.wsdl2code.skipMessageReceiver}" default-value="false" *
     */
    private boolean skipMessageReceiver = false;

    /**
     * @parameter expression="${axis2.wsdl2code.skipBuildXML}" default-value="false" *
     */
    private boolean skipBuildXML = false;

    /**
     * @parameter expression="${axis2.wsdl2code.skipWSDL}" default-value="false" *
     */
    private boolean skipWSDL = false;

    /**
     * @parameter expression="${axis2.wsdl2code.overWrite}" default-value="false" *
     */
    private boolean overWrite = false;

    /**
     * @parameter expression="${axis2.wsdl2code.suppressPrefixes}" default-value="false" *
     */
    private boolean suppressPrefixes = false;

    /**
     * Specify databinding specific extra options
     *
     * @parameter expression="${axis2.java2wsdl.options}"
     */
    private Properties options;

    /** @parameter expression="${axis2.wsdl2code.namespaceToPackages}" */
    private String namespaceToPackages = null;

    /** @parameter */
    private NamespaceURIMapping[] namespaceURIs = null;

    /** Fills the option map. This map is passed onto the code generation API to generate the code. */
    private Map fillOptionMap() throws MojoFailureException {
        Map optionMap = new HashMap();

        ////////////////////////////////////////////////////////////////
        //WSDL file name
        // here we need to set the project base uri to relative paths.
        if (wsdlFile.indexOf(":") == -1){
           //i.e this is not a uri
           File file = new File(wsdlFile);
           if (!file.isAbsolute()){
               wsdlFile = project.getBasedir() + File.separator + wsdlFile; 
           }
        }

        optionMap.put(
                CommandLineOptionConstants.WSDL2JavaConstants.WSDL_LOCATION_URI_OPTION,
                new CommandLineOption(
                        CommandLineOptionConstants.WSDL2JavaConstants.WSDL_LOCATION_URI_OPTION,
                        getStringArray(wsdlFile)));
        //output location
        optionMap.put(
                CommandLineOptionConstants.WSDL2JavaConstants.OUTPUT_LOCATION_OPTION,
                new CommandLineOption(
                        CommandLineOptionConstants.WSDL2JavaConstants.OUTPUT_LOCATION_OPTION,
                        getStringArray(outputDirectory.getPath())));
        //////////////////////////////////////////////////////////////////
        // Databinding type
        optionMap.put(
                CommandLineOptionConstants.WSDL2JavaConstants.DATA_BINDING_TYPE_OPTION,
                new CommandLineOption(
                        CommandLineOptionConstants.WSDL2JavaConstants.DATA_BINDING_TYPE_OPTION,
                        getStringArray(databindingName)));

        if ("async".equals(syncMode)) {
            // Async only option - forcing to generate async methods only
            optionMap.put(
                    CommandLineOptionConstants.WSDL2JavaConstants.CODEGEN_ASYNC_ONLY_OPTION,
                    new CommandLineOption(
                            CommandLineOptionConstants.WSDL2JavaConstants.CODEGEN_ASYNC_ONLY_OPTION,
                            new String[0]));
        } else if ("sync".equals(syncMode)) {
            // Sync only option - forcing to generate Sync methods only
            optionMap.put(
                    CommandLineOptionConstants.WSDL2JavaConstants.CODEGEN_SYNC_ONLY_OPTION,
                    new CommandLineOption(
                            CommandLineOptionConstants.WSDL2JavaConstants.CODEGEN_SYNC_ONLY_OPTION,
                            new String[0]));
        } else if ("both".equals(syncMode)) {
            // Do nothing
        } else {
            throw new MojoFailureException("Invalid syncMode: " + syncMode +
                    ", expected either of 'sync', 'async' or 'both'.");
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
        if (generateServerSide) {
            optionMap.put(
                    CommandLineOptionConstants.WSDL2JavaConstants.SERVER_SIDE_CODE_OPTION,
                    new CommandLineOption(
                            CommandLineOptionConstants.WSDL2JavaConstants.SERVER_SIDE_CODE_OPTION,
                            new String[0]));

            //services XML generation - effective only when specified as the server side
            if (generateServicesXml) {
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
        if (generateTestcase) {
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
        if (generateServerSideInterface) {
            optionMap.put(
                    CommandLineOptionConstants.WSDL2JavaConstants.SERVER_SIDE_INTERFACE_OPTION,
                    new CommandLineOption(
                            CommandLineOptionConstants.WSDL2JavaConstants.SERVER_SIDE_INTERFACE_OPTION,
                            new String[0]));
        }

        if (unwrap) {
            optionMap.put(
                    CommandLineOptionConstants.WSDL2JavaConstants.UNWRAP_PARAMETERS,
                    new CommandLineOption(
                            CommandLineOptionConstants.WSDL2JavaConstants.UNWRAP_PARAMETERS,
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

        if (repositoryPath != null) {
            optionMap.put(
                    CommandLineOptionConstants.WSDL2JavaConstants.REPOSITORY_PATH_OPTION,
                    new CommandLineOption(
                            CommandLineOptionConstants.WSDL2JavaConstants.REPOSITORY_PATH_OPTION,
                            new String[]{repositoryPath}));
        }

        if (externalMapping != null) {
            optionMap.put(
                    CommandLineOptionConstants.WSDL2JavaConstants.EXTERNAL_MAPPING_OPTION,
                    new CommandLineOption(
                            CommandLineOptionConstants.WSDL2JavaConstants.EXTERNAL_MAPPING_OPTION,
                            new String[]{externalMapping}));
        }

        if (wsdlVersion != null) {
            optionMap.put(
                    CommandLineOptionConstants.WSDL2JavaConstants.WSDL_VERSION_OPTION,
                    new CommandLineOption(
                            CommandLineOptionConstants.WSDL2JavaConstants.WSDL_VERSION_OPTION,
                            new String[]{wsdlVersion}));
        }

        if (targetSourceFolderLocation != null) {
            optionMap.put(
                    CommandLineOptionConstants.WSDL2JavaConstants.SOURCE_FOLDER_NAME_OPTION,
                    new CommandLineOption(
                            CommandLineOptionConstants.WSDL2JavaConstants.SOURCE_FOLDER_NAME_OPTION,
                            new String[]{targetSourceFolderLocation}));
        }

        if (targetResourcesFolderLocation != null) {
            optionMap.put(
                    CommandLineOptionConstants.WSDL2JavaConstants.RESOURCE_FOLDER_OPTION,
                    new CommandLineOption(
                            CommandLineOptionConstants.WSDL2JavaConstants.RESOURCE_FOLDER_OPTION,
                            new String[]{targetResourcesFolderLocation}));
        }

        if(options != null) {
            Iterator iterator = options.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                String key = CommandLineOptionConstants.WSDL2JavaConstants.EXTRA_OPTIONTYPE_PREFIX + entry.getKey();
                String value = (String) entry.getValue();
                optionMap.put(
                        key,
                        new CommandLineOption(
                                key,
                                new String[]{value}));
            }
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
        if (!((namespaceToPackages == null) && (namespaceURIs == null))) {
            optionMap.put(
                    CommandLineOptionConstants.WSDL2JavaConstants.NAME_SPACE_TO_PACKAGE_OPTION,
                    new CommandLineOption(
                            CommandLineOptionConstants.WSDL2JavaConstants.NAME_SPACE_TO_PACKAGE_OPTION,
                            new String[] { getNamespaceToPackagesMap() }));
        }
        return optionMap;
    }

    private String getNamespaceToPackagesMap() throws MojoFailureException {
        StringBuffer sb = new StringBuffer();
        if (namespaceToPackages != null) {
            sb.append(namespaceToPackages);
        }
        if (namespaceURIs != null) {
            for (int i = 0; i < namespaceURIs.length; i++) {
                NamespaceURIMapping mapping = namespaceURIs[i];
                String uri = mapping.getUri();
                if (uri == null) {
                    throw new MojoFailureException(
                            "A namespace to package mapping requires an uri child element.");
                }
                String uriPackageName = mapping.getPackageName();
                if (uriPackageName == null) {
                    throw new MojoFailureException(
                            "A namespace to package mapping requires a packageName child element.");
                }
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(uri);
                sb.append('=');
                sb.append(uriPackageName);
            }
        }
        return (sb.length() != 0) ? sb.toString() : null;
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

    public void execute() throws MojoFailureException, MojoExecutionException {

        fixCompileSourceRoots();
        showDependencies();

        Map commandLineOptions = this.fillOptionMap();
        CommandLineOptionParser parser =
                new CommandLineOptionParser(commandLineOptions);
        try {
            new CodeGenerationEngine(parser).generate();
        } catch (CodeGenerationException e) {
            Throwable t = e;
            while (t.getCause() != null) {
                t = t.getCause();
            }
            t.printStackTrace();
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private void showDependencies() {
        Log log = getLog();
        if (!log.isDebugEnabled()) {
            return;
        }
        log.debug("The projects dependency artifacts are: ");
        for (Iterator iter = project.getDependencyArtifacts().iterator(); iter.hasNext();) {
            Artifact artifact = (Artifact)iter.next();
            log.debug("    " + artifact.getGroupId() + ":" + artifact.getArtifactId() +
                    ":" + artifact.getVersion() + ":" + artifact.getClassifier() +
                    ":" + artifact.getScope() + ":" + artifact.getType());
        }
        log.debug("The projects transitive artifacts are: ");
        for (Iterator iter = project.getArtifacts().iterator(); iter.hasNext();) {
            Artifact artifact = (Artifact)iter.next();
            log.debug("    " + artifact.getGroupId() + ":" + artifact.getArtifactId() +
                    ":" + artifact.getVersion() + ":" + artifact.getClassifier() +
                    ":" + artifact.getScope() + ":" + artifact.getType());
        }
    }

    private void fixCompileSourceRoots() {
        File srcDir = new File(outputDirectory, "src");
        project.addCompileSourceRoot(srcDir.getPath());
	}
}
