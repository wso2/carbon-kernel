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

import org.apache.axis2.description.java2wsdl.Java2WSDLConstants;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.apache.ws.java2wsdl.Java2WSDLCodegenEngine;
import org.apache.ws.java2wsdl.MappingSet;
import org.apache.ws.java2wsdl.NamespaceMapping;
import org.apache.ws.java2wsdl.utils.Java2WSDLCommandLineOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Java2WSDLTask extends Task implements Java2WSDLConstants {
    public static class ExtraClass {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
    
    public static final String OPEN_BRACKET = "[";
    public static final String CLOSE_BRACKET = "]";
    public static final String COMMA = ",";

    private String className = null;
    private String outputLocation = null;
    private String targetNamespace = null;
    private String targetNamespacePrefix = null;
    private String schemaTargetNamespace = null;
    private String schemaTargetNamespacePrefix = null;
    private String serviceName = null;
    private String outputFileName = null;
    private Path classpath = null;
    private String style = Java2WSDLConstants.DOCUMENT;
    private String use = Java2WSDLConstants.LITERAL;
    private String locationUri;
    private String attrFormDefault = null;
    private String elementFormDefault = null;
    private String wsdlVersion = null;
    private String docLitBare = null;

    //names of java types not used in the service defn. directly, but for which schema must be generated
    private String[] extraClasses;

    private final List<ExtraClass> extraClasses2 = new ArrayList<ExtraClass>();
    
    //namespace generator classname
    private String nsGenClassName = null;

    //package to namespace map
    private HashMap namespaceMap = new HashMap();

    //names of java types not used in the service defn. directly, but for which schema must be generated
    private ArrayList pkg2nsMappings = new ArrayList();

    private MappingSet mappings = new MappingSet();
    
    private String schemaGenClassName = null;

    public String getLocationUri() {
        return locationUri;
    }

    public void setLocationUri(String locationUri) {
        this.locationUri = locationUri;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getUse() {
        return use;
    }

    public void setUse(String use) {
        this.use = use;
    }

    /**
     *
     */
    public Java2WSDLTask() {
        super();
    }

    /**
     * Fills the option map. This map is passed onto
     * the code generation API to generate the code.
     */
    private Map fillOptionMap() {
        Map optionMap = new HashMap();

        // Check that critical options exist
        if (className == null) {
            throw new BuildException(
                    "You must specify a classname");
        }

        ////////////////////////////////////////////////////////////////

        // Classname
        addToOptionMap(optionMap,
                Java2WSDLConstants.CLASSNAME_OPTION,
                className);

        // Output location
        addToOptionMap(optionMap,
                Java2WSDLConstants.OUTPUT_LOCATION_OPTION,
                outputLocation);

        // Target namespace
        addToOptionMap(optionMap,
                Java2WSDLConstants.TARGET_NAMESPACE_OPTION,
                targetNamespace);

        // Target namespace prefix
        addToOptionMap(optionMap,
                Java2WSDLConstants.TARGET_NAMESPACE_PREFIX_OPTION,
                targetNamespacePrefix);

        // Schema target namespace
        addToOptionMap(optionMap,
                Java2WSDLConstants.SCHEMA_TARGET_NAMESPACE_OPTION,
                schemaTargetNamespace);

        // Schema target namespace prefix
        addToOptionMap(optionMap,
                Java2WSDLConstants.SCHEMA_TARGET_NAMESPACE_PREFIX_OPTION,
                schemaTargetNamespacePrefix);

        // Service name
        addToOptionMap(optionMap,
                Java2WSDLConstants.SERVICE_NAME_OPTION,
                serviceName);

        // Output file name
        addToOptionMap(optionMap,
                Java2WSDLConstants.OUTPUT_FILENAME_OPTION,
                outputFileName);

        addToOptionMap(optionMap,
                Java2WSDLConstants.STYLE_OPTION,
                getStyle());

        addToOptionMap(optionMap,
                Java2WSDLConstants.USE_OPTION,
                getUse());

        addToOptionMap(optionMap,
                Java2WSDLConstants.LOCATION_OPTION,
                getLocationUri());

        addToOptionMap(optionMap,
                Java2WSDLConstants.ATTR_FORM_DEFAULT_OPTION,
                getAttrFormDefault());

        addToOptionMap(optionMap,
                Java2WSDLConstants.ELEMENT_FORM_DEFAULT_OPTION,
                getElementFormDefault());

        addToOptionMap(optionMap,
                Java2WSDLConstants.EXTRA_CLASSES_DEFAULT_OPTION,
                getExtraClasses());

        addToOptionMap(optionMap,
                Java2WSDLConstants.NAMESPACE_GENERATOR_OPTION,
                getNsGenClassName());

        addToOptionMap(optionMap,
                Java2WSDLConstants.SCHEMA_GENERATOR_OPTION,
                getSchemaGenClassName());

        addToOptionMap(optionMap,
                Java2WSDLConstants.WSDL_VERSION_OPTION,
                getWSDLVersion());

        addToOptionMap(optionMap,
                Java2WSDLConstants.DOC_LIT_BARE,
                getDocLitBare());

        loadPkg2NsMap();
        addToOptionMap(optionMap,
                Java2WSDLConstants.JAVA_PKG_2_NSMAP_OPTION,
                getPkg2nsMappings());

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

    /**
     * Function to put arguments in the option map.
     * This functions skips adding of options that have a null value.
     *
     * @param map    The option map into which the option is to be added
     * @param option The option name
     * @param value  The value of the option
     */
    private void addToOptionMap(Map map, String option, String value) {
        if (value != null) {
            map.put(option,
                    new Java2WSDLCommandLineOption(option, getStringArray(value)));
        }
    }

    private void addToOptionMap(Map map, String option, String[] values) {
        if (values != null && values.length > 0) {
            map.put(option,
                    new Java2WSDLCommandLineOption(option, values));
        }
    }

    private void addToOptionMap(Map map, String option, ArrayList values) {
        if (values != null && !values.isEmpty()) {
            map.put(option,
                    new Java2WSDLCommandLineOption(option, values));
        }
    }

    public void execute() throws BuildException {
        try {

            Map commandLineOptions = this.fillOptionMap();
            ClassLoader conextClassLoader = Thread.currentThread().getContextClassLoader();
            AntClassLoader cl = new AntClassLoader(getClass().getClassLoader(),
                    getProject(),
                    classpath == null ? createClasspath() : classpath,
                    false);

            commandLineOptions.put(Java2WSDLConstants.CLASSPATH_OPTION, new Java2WSDLCommandLineOption(Java2WSDLConstants.CLASSPATH_OPTION, classpath.list()));

            Thread.currentThread().setContextClassLoader(cl);

            if (outputLocation != null) cl.addPathElement(outputLocation);

            new Java2WSDLCodegenEngine(commandLineOptions).generate();
            Thread.currentThread().setContextClassLoader(conextClassLoader);
        } catch (Throwable e) {
            throw new BuildException(e);
        }

    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setOutputLocation(String outputLocation) {
        this.outputLocation = outputLocation;
    }

    public void setTargetNamespace(String targetNamespace) {
        this.targetNamespace = targetNamespace;
    }

    public void setTargetNamespacePrefix(String targetNamespacePrefix) {
        this.targetNamespacePrefix = targetNamespacePrefix;
    }

    public void setSchemaTargetNamespace(String schemaTargetNamespace) {
        this.schemaTargetNamespace = schemaTargetNamespace;
    }

    public void setSchemaTargetNamespacePrefix(String schemaTargetNamespacePrefix) {
        this.schemaTargetNamespacePrefix = schemaTargetNamespacePrefix;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    /**
     * Set the optional classpath
     *
     * @param classpath the classpath to use when loading class
     */
    public void setClasspath(Path classpath) {
        createClasspath().append(classpath);
    }

    /**
     * Set the optional classpath
     *
     * @return a path instance to be configured by the Ant core.
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

    public String getAttrFormDefault() {
        return attrFormDefault;
    }

    public void setAttrFormDefault(String attrFormDefault) {
        this.attrFormDefault = attrFormDefault;
    }

    public String getElementFormDefault() {
        return elementFormDefault;
    }

    public void setElementFormDefault(String elementFormDefault) {
        this.elementFormDefault = elementFormDefault;
    }

    public String[] getExtraClasses() {
        List<String> list = new ArrayList<String>((extraClasses == null ? 0 : extraClasses.length)
                + extraClasses2.size());
        if (extraClasses != null) {
            list.addAll(Arrays.asList(extraClasses));
        }
        for (ExtraClass extraClass : extraClasses2) {
            list.add(extraClass.getName());
        }
        return list.toArray(new String[list.size()]);
    }

    public void setExtraClasses(String extraClasses) {
        this.extraClasses = extraClasses.split(",");
    }
    
    public ExtraClass createExtraClass() {
        ExtraClass extraClass = new ExtraClass();
        extraClasses2.add(extraClass);
        return extraClass;
    }

    public String getNsGenClassName() {
        return nsGenClassName;
    }

    public void setNsGenClassName(String nsGenClassName) {
        this.nsGenClassName = nsGenClassName;
    }

    public String getSchemaGenClassName() {
        return schemaGenClassName;
    }

    public void setSchemaGenClassName(String schemaGenClassName) {
        this.schemaGenClassName = schemaGenClassName;
    }

    public void loadPkg2NsMap() {
        mappings.execute(namespaceMap, true);
        Iterator packageNames = namespaceMap.keySet().iterator();
        String packageName = null;
        while (packageNames.hasNext()) {
            packageName = (String) packageNames.next();
            pkg2nsMappings.add(OPEN_BRACKET +
                    packageName +
                    COMMA +
                    namespaceMap.get(packageName) +
                    CLOSE_BRACKET);
        }
    }

    public ArrayList getPkg2nsMappings() {
        return pkg2nsMappings;
    }

    public void setPkg2nsMappings(ArrayList pkg2nsMappings) {
        this.pkg2nsMappings = pkg2nsMappings;
    }

    /**
     * add a mapping of namespaces to packages
     */
    public void addMapping(NamespaceMapping mapping) {
        mappings.addMapping(mapping);
    }

    /**
     * add a mapping of namespaces to packages
     */
    public void addMappingSet(MappingSet mappingset) {
        mappings.addMappingSet(mappingset);
    }

    public String getDocLitBare() {
        return docLitBare;
    }

    public void setDocLitBare(String docLitBare) {
        this.docLitBare = docLitBare;
    }

    public String getWSDLVersion() {
        return wsdlVersion;
    }

    public void setWSDLVersion(String wsdlVersion) {
        this.wsdlVersion = wsdlVersion;
    }
}

