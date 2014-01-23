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

package org.apache.axis2.schema;

import org.apache.axis2.schema.i18n.SchemaCompilerMessages;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is a bean class that captures all the compiler options.
 * Right now the compiler options consist of the following
 * 1. output file location - A folder with necessary rights for the
 * schema compiler to write the files
 * 2.package name
 * 3.namespace to package map
 * 4.boolean flag marking whether to wrap or unwrap
 * 4.boolean flag marking whether to write classes or not
 */
public class CompilerOptions {

    /**
     * Generated output file
     */
    private File outputLocation;
    private String packageName = null;
    private boolean generateAll = false;

    private boolean offStrictValidation = false;
    private boolean isUseWrapperClasses = false;

    /**
     * Package for the mapper
     */
    private String mapperClassPackage = null;

    public String getMapperClassPackage() {
        return mapperClassPackage;
    }

    public void setMapperClassPackage(String mapperClassPackage) {
        this.mapperClassPackage = mapperClassPackage;
    }

    /**
     * get whether the mapper class package
     * name is present
     */
    public boolean isMapperClassPackagePresent(){
        return   (mapperClassPackage!=null &&
                       !"".equals(mapperClassPackage));
    }
    /**
     * The flag keeping whether seperate helpers
     * need to be generated or not
     */
    private boolean helperMode = false;
    
    public boolean isHelperMode() {
        return helperMode;
    }

    public void setHelperMode(boolean helperMode) {
        this.helperMode = helperMode;
    }
    
    /**
     * Keep track of the namespace and packages mapping
     */
    private Map<String,String> ns2PackageMap = new HashMap<String,String>();

    public Map<String,String> getNs2PackageMap() {
        return ns2PackageMap;
    }

    public void setNs2PackageMap(Map<String,String> ns2PackageMap) {
        this.ns2PackageMap = ns2PackageMap;
    }

    /**
     * This flag tells the databinder to either write the output or
     * not. if this is set to true it will write the output at once.
     * if not the outputter will populate the 
     */
    private boolean writeOutput = false;

    /**
     * This flag determines whether the generated classes are wrapped or not
     * if the wrapper flag is true, then only a single file will be generated
     */
    private boolean wrapClasses = false;

    public boolean isWriteOutput() {
        return writeOutput;
    }

    public void setWriteOutput(boolean writeOutput) {
        this.writeOutput = writeOutput;
    }

    public boolean isWrapClasses() {
        return wrapClasses;
    }

    public void setWrapClasses(boolean wrapClasses) {
        this.wrapClasses = wrapClasses;
    }

    public String getPackageName() {
        return packageName;
    }

    public CompilerOptions setPackageName(String packageName) {
        // Validate the package name.
        if (packageName != null && testValue(packageName)) {
            this.packageName = packageName;
        } else {
            throw new RuntimeException(SchemaCompilerMessages.getMessage("schema.unsupportedvalue"));
        }
        return this;
    }

    public File getOutputLocation() {
        return outputLocation;
    }

    public CompilerOptions setOutputLocation(File outputLocation) {
        this.outputLocation = outputLocation;
        return this;
    }

    private boolean testValue(String wordToMatch) {
        Pattern pat = Pattern.compile("^(\\w+\\.)+$");
        Matcher m = pat.matcher(wordToMatch);
        return m.matches();
    }
    
    public boolean isGenerateAll() {
        return generateAll;
    }

    public void setGenerateAll(boolean generateAll) {
        this.generateAll = generateAll;
    }
	/**
	 * This flag determines whether the generated classes are expected to be
	 * backword compatible with Axis 1.x
	 */
	private boolean backwordCompatibilityMode = false;

	public boolean isBackwordCompatibilityMode() {
		return backwordCompatibilityMode;
	}

	public void setBackwordCompatibilityMode(boolean backwordCompatibilityMode) {
		this.backwordCompatibilityMode = backwordCompatibilityMode;
	}

    /**
     * Should we suppress namespace prefixes
     */
    private boolean suppressPrefixesMode = false;

    public boolean isSuppressPrefixesMode() {
        return suppressPrefixesMode;
    }

    public void setSuppressPrefixesMode(boolean suppressPrefixesMode) {
        this.suppressPrefixesMode = suppressPrefixesMode;
    }

    public boolean isOffStrictValidation() {
        return offStrictValidation;
    }

    public void setOffStrictValidation(boolean offStrictValidation) {
        this.offStrictValidation = offStrictValidation;
    }

    public boolean isUseWrapperClasses() {
        return isUseWrapperClasses;
    }

    public void setUseWrapperClasses(boolean useWrapperClasses) {
        this.isUseWrapperClasses = useWrapperClasses;
    }
}
