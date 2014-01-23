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

package org.apache.axis2.maven2.java2wsdl;

import org.apache.axis2.description.java2wsdl.Java2WSDLConstants;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.ws.java2wsdl.Java2WSDLCodegenEngine;
import org.apache.ws.java2wsdl.utils.Java2WSDLCommandLineOption;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;


/**
 * Takes a Java class as input and converts it into an equivalent
 * WSDL file.
 * 
 * @goal java2wsdl
 * @phase process-classes
 * @requiresDependencyResolution compile
 */
public class Java2WSDLMojo extends AbstractMojo {
    public static final String OPEN_BRACKET = "[";
    public static final String CLOSE_BRACKET = "]";
    public static final String COMMA = ",";

    /**
     * The maven project.
     * @parameter expression="${project}"
     * @read-only
     * @required
     */
    private MavenProject project;

    /**
     * Fully qualified name of the class, which is being inspected.
     * @parameter expression="${axis2.java2wsdl.className}"
     * @required
     */
    private String className;

    /**
     * Target namespace of the generated WSDL.
     * @parameter expression="${axis2.java2wsdl.targetNamespace}"
     */
    private String targetNamespace;

    /**
     * The namespace prefix, which is being used for the WSDL's
     * target namespace.
     * @parameter expression="${axis2.java2wsdl.targetNamespacePrefix}"
     */
    private String targetNamespacePrefix;

    /**
     * The generated schemas target namespace.
     * @parameter expression="${axis2.java2wsdl.schemaTargetNamespace}"
     */
    private String schemaTargetNamespace;

    /**
     * The generated schemas target namespace prefix.
     * @parameter expression="${axis2.java2wsdl.schemaTargetNamespacePrefix}"
     */
    private String schemaTargetNamespacePrefix;

    /**
     * Name of the generated service.
     * @parameter expression="${axis2.java2wsdl.serviceName}"
     */
    private String serviceName;

    /**
     * Name of the service file, which is being generated.
     * @parameter expression="${axis2.java2wsdl.outputFileName}" default-value="${project.build.directory}/generated-resources/service.wsdl"
     */
    private String outputFileName;

    /**
     * Style for the wsdl
     * @parameter expression="${axis2.java2wsdl.style}"
     */
    private String style;

    /**
     * Use for the wsdl
     * @parameter expression="${axis2.java2wsdl.use}"
     */
    private String use;

    /**
     * Version for the wsdl
     * @parameter expression="${axis2.java2wsdl.wsdlVersion}"
     */
    private String wsdlVersion;

    /**
     * Namespace Generator
     * @parameter expression="${axis2.java2wsdl.nsGenClassName}"
     */
    private String nsGenClassName;

    /**
     * Schema Generator
     * @parameter expression="${axis2.java2wsdl.nsGenClassName}"
     */
    private String schemaGenClassName;

    /**
     * Location URI in the wsdl
     * @parameter expression="${axis2.java2wsdl.locationUri}"
     */
    private String locationUri;

    /**
     * attrFormDefault setting for the schema
     * @parameter expression="${axis2.java2wsdl.attrFormDefault}"
     */
    private String attrFormDefault;

    /**
     * elementFormDefault setting for the schema
     * @parameter expression="${axis2.java2wsdl.elementFormDefault}"
     */
    private String elementFormDefault;

    /**
     * Switch on the Doc/Lit/Bare style schema
     * @parameter expression="${axis2.java2wsdl.docLitBare}"
     */
    private String docLitBare;

    /**
     * Additional classes for which we need to generate schema
     * @parameter expression="${axis2.java2wsdl.extraClasses}"
     */
    private String[] extraClasses;

    /**
     * Specify namespaces explicitly for packages
     * @parameter expression="${axis2.java2wsdl.package2Namespace}"
     */
    private Properties package2Namespace;

    private void addToOptionMap(Map map, String option, String value) {
        addToOptionMap(map, option, new String[]{value});
    }

    private void addToOptionMap(Map map, String option, String[] value) {
        if (value != null) {
            map.put(option,
                    new Java2WSDLCommandLineOption(option, value));
        }
    }

    private void addToOptionMap(Map map, String option, ArrayList values) {
        if (values != null && !values.isEmpty()) {
            map.put(option,
                    new Java2WSDLCommandLineOption(option, values));
        }
    }

    /**
     * Fills the option map. This map is passed onto
     * the code generation API to generate the code.
     */
    private Map fillOptionMap() throws MojoFailureException {
        Map optionMap = new HashMap();

        if (className == null) {
            throw new MojoFailureException("You must specify a classname");
        }
        addToOptionMap( optionMap,
                        Java2WSDLConstants.CLASSNAME_OPTION,
                        className);
        addToOptionMap( optionMap,
                        Java2WSDLConstants.TARGET_NAMESPACE_OPTION,
                        targetNamespace);
        addToOptionMap( optionMap,
                        Java2WSDLConstants.TARGET_NAMESPACE_PREFIX_OPTION,
                        targetNamespacePrefix);
        addToOptionMap( optionMap,
                        Java2WSDLConstants.SCHEMA_TARGET_NAMESPACE_OPTION,
                        schemaTargetNamespace);
        addToOptionMap( optionMap,
                        Java2WSDLConstants.SCHEMA_TARGET_NAMESPACE_PREFIX_OPTION,
                        schemaTargetNamespacePrefix);
        addToOptionMap( optionMap,
                        Java2WSDLConstants.SERVICE_NAME_OPTION,
                        serviceName);
        File outputFile = new File(outputFileName);
        if(!outputFile.isAbsolute()){
            outputFile = new File(project.getBasedir(), outputFileName);
        }
        File dir = outputFile.getParentFile();
        if (!dir.isDirectory()) {
            dir.mkdirs();
        }
        addToOptionMap( optionMap,
                        Java2WSDLConstants.OUTPUT_LOCATION_OPTION,
                        dir.getPath() );
        addToOptionMap( optionMap,
                        Java2WSDLConstants.OUTPUT_FILENAME_OPTION,
                        outputFile.getName() );

        Artifact artifact = project.getArtifact();
        Set artifacts = project.getArtifacts();
        String[] artifactFileNames = new String[artifacts.size() + (artifact == null ? 0 : 1)];
        int j = 0;
        for(Iterator i = artifacts.iterator(); i.hasNext(); j++) {
            artifactFileNames[j] = ((Artifact) i.next()).getFile().getAbsolutePath();
        }
        if(artifact != null) {
            File file = artifact.getFile();
            if(file != null){
                artifactFileNames[j] = file.getAbsolutePath();
            }
        }

        addToOptionMap( optionMap,
                        Java2WSDLConstants.CLASSPATH_OPTION,
                        artifactFileNames);

        if (style != null) {
            addToOptionMap(optionMap,
                    Java2WSDLConstants.STYLE_OPTION,
                    style);
        }

        if (use != null) {
            addToOptionMap(optionMap,
                    Java2WSDLConstants.USE_OPTION,
                    use);
        }

        if (wsdlVersion != null) {
            addToOptionMap(optionMap,
                    Java2WSDLConstants.WSDL_VERSION_OPTION,
                    wsdlVersion);
        }

        if (docLitBare != null) {
            addToOptionMap(optionMap,
                    Java2WSDLConstants.DOC_LIT_BARE,
                    docLitBare);
        }

        if (locationUri != null) {
            addToOptionMap(optionMap,
                    Java2WSDLConstants.LOCATION_OPTION,
                    locationUri);
        }

        if (nsGenClassName != null) {
            addToOptionMap(optionMap,
                    Java2WSDLConstants.NAMESPACE_GENERATOR_OPTION,
                    nsGenClassName);
        }

        if (schemaGenClassName != null) {
            addToOptionMap(optionMap,
                    Java2WSDLConstants.SCHEMA_GENERATOR_OPTION,
                    schemaGenClassName);
        }

        if (attrFormDefault != null) {
            addToOptionMap(optionMap,
                    Java2WSDLConstants.ATTR_FORM_DEFAULT_OPTION,
                    attrFormDefault);
        }

        if (elementFormDefault != null) {
            addToOptionMap(optionMap,
                    Java2WSDLConstants.ELEMENT_FORM_DEFAULT_OPTION,
                    elementFormDefault);
        }

        if (extraClasses != null && extraClasses.length > 0) {
            addToOptionMap(optionMap,
                    Java2WSDLConstants.EXTRA_CLASSES_DEFAULT_OPTION,
                    extraClasses);
        }

        ArrayList list = new ArrayList();
        if(package2Namespace != null){
            Iterator iterator = package2Namespace.entrySet().iterator();
    
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                String packageName = (String) entry.getKey();
                String namespace = (String) entry.getValue();
                list.add(OPEN_BRACKET +
                        packageName +
                        COMMA +
                        namespace +
                        CLOSE_BRACKET);
            }
        }
        addToOptionMap(optionMap,
                Java2WSDLConstants.JAVA_PKG_2_NSMAP_OPTION,
                list);

        return optionMap;
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        Map commandLineOptions = fillOptionMap();
        try {
            new Java2WSDLCodegenEngine(commandLineOptions).generate();
        } catch (Exception e) {
            e.printStackTrace();
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }
}
