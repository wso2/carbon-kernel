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

package org.apache.axis2.tools.bean;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.axis2.description.java2wsdl.Java2WSDLConstants;
import org.apache.ws.java2wsdl.Java2WSDLCodegenEngine;
import org.apache.ws.java2wsdl.utils.Java2WSDLCommandLineOption;

import java.util.HashMap;
import java.util.Map;

public class WsdlgenBean {

    private String ClassName;
    private String[] ClassPathList;
    private String TargetNamespace;
    private String TargetNamespacePrefix;
    private String SchemaTargetNamespace;
    private String ServiceName;
    private String SchemaTargetNamespacePrefix;
    private String OutputLocation ;
    private String OutputWSDLName ;
    private Project project;


    public String getClassName() {
        return ClassName ;
    }

    public void setClassName(String className){
        this.ClassName=className ;
    }

    public String[] getClassPathList(){
        return ClassPathList ;
    }

    public void setClassPathList(String[] classPathList) {
        this.ClassPathList = classPathList;
    }

    public String getTargetNamespace() {
        return TargetNamespace ;
    }

    public void setTargetNamespace(String targetNamespace) {
        this.TargetNamespace = targetNamespace;
    }

    public String getTargetNamespacePrefix() {
        return TargetNamespacePrefix ;
    }

    public void setTargetNamespacePrefix(String targetNamespacePrefix) {
        this.TargetNamespacePrefix = targetNamespacePrefix;
    }

    public String getSchemaTargetNamespace() {
        return SchemaTargetNamespace ;
    }

    public void setSchemaTargetNamespace (String schemaTargetNameSpace) {
        this.SchemaTargetNamespace = schemaTargetNameSpace ;
    }

    public String getSchemaTargetNamespacePrefix () {
        return SchemaTargetNamespacePrefix ;
    }

    public void setSchemaTargetNamespacePrefix (String schemaTargetNameSpacePrefix) {
        this.SchemaTargetNamespacePrefix = schemaTargetNameSpacePrefix ;
    }

    public String getOutputLocation(){
        return OutputLocation ;
    }

    public void setOutputLocation(String outputLoaction){
        this.OutputLocation =outputLoaction ;
    }

    public String getOutputWSDLName(){
        return OutputWSDLName ;
    }

    public void setOutputWSDLName(String outputWSDLName){
        this.OutputWSDLName =outputWSDLName ;
    }

    public String getServiceName(){
        return ServiceName ;
    }

    public void setServiceName(String serviceName){
        this.ServiceName =serviceName ;
    }

    public Map fillOptionMap()  {

        Map optionMap = new HashMap();

        optionMap.put(Java2WSDLConstants .CLASSNAME_OPTION ,
                new Java2WSDLCommandLineOption(Java2WSDLConstants.CLASSNAME_OPTION ,
                        new String[]{getClassName() })
        );

        optionMap.put(Java2WSDLConstants.CLASSPATH_OPTION ,
                new Java2WSDLCommandLineOption(Java2WSDLConstants .CLASSPATH_OPTION ,
                        getClassPathList())
        );

        optionMap.put(Java2WSDLConstants .TARGET_NAMESPACE_OPTION ,
                new Java2WSDLCommandLineOption(Java2WSDLConstants .TARGET_NAMESPACE_OPTION ,
                        new String[]{getTargetNamespace() })
        );

        optionMap.put(Java2WSDLConstants .TARGET_NAMESPACE_PREFIX_OPTION ,
                new Java2WSDLCommandLineOption(Java2WSDLConstants .TARGET_NAMESPACE_PREFIX_OPTION ,
                        new String[]{getTargetNamespacePrefix()})
        );

        optionMap.put(Java2WSDLConstants .SCHEMA_TARGET_NAMESPACE_OPTION ,
                new Java2WSDLCommandLineOption(Java2WSDLConstants .SCHEMA_TARGET_NAMESPACE_OPTION ,
                        new String[]{getSchemaTargetNamespace() })
        );

        optionMap.put(Java2WSDLConstants .SERVICE_NAME_OPTION ,
                new Java2WSDLCommandLineOption(Java2WSDLConstants .SERVICE_NAME_OPTION ,
                        new String[]{getServiceName() })
        );

        optionMap.put(Java2WSDLConstants .SCHEMA_TARGET_NAMESPACE_PREFIX_OPTION ,
                new Java2WSDLCommandLineOption(Java2WSDLConstants .SCHEMA_TARGET_NAMESPACE_PREFIX_OPTION ,
                        new String[]{getSchemaTargetNamespacePrefix() })
        );

        optionMap.put(Java2WSDLConstants .OUTPUT_LOCATION_OPTION ,
                new Java2WSDLCommandLineOption(Java2WSDLConstants .OUTPUT_LOCATION_OPTION ,
                        new String[]{getOutputLocation()})
        );

        optionMap.put(Java2WSDLConstants .OUTPUT_FILENAME_OPTION ,
                new Java2WSDLCommandLineOption(Java2WSDLConstants .OUTPUT_FILENAME_OPTION ,
                        new String[]{getOutputWSDLName()})
        );

        return optionMap;

    }

    public void generate() throws Exception {

        try {

            Java2WSDLCodegenEngine java2WSDL=new Java2WSDLCodegenEngine(fillOptionMap());
            java2WSDL.generate();

        } catch (Throwable e) {

            throw new Exception("Code generation failed due to " + e.getLocalizedMessage());
        }

    }
    public Project getActiveProject() {
        return project;

    }

    public void setProject(Project project) {
        this.project = project;
    }
    public Module[] getModules() {

        Project project = getActiveProject();
        if (project != null) {
            return ModuleManager.getInstance(project).getModules();
        }
        return null;
    }

    public String[] getModuleSrc(String name) {
        Project project = getActiveProject();
        if (project != null) {
            Module module = ModuleManager.getInstance(project).findModuleByName(name);
            ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
            VirtualFile virtualFiles[] = moduleRootManager.getSourceRoots();
            String src[] = new String[virtualFiles.length];
            for (int count = 0; count < src.length; count++) {
                src[count] = virtualFiles[count].getPresentableUrl();
            }
            return src;
        }
        return null;
    }
}
