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


package org.apache.axis2.description;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.modules.Module;
import org.apache.axis2.util.Utils;

import javax.xml.namespace.QName;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * <p>This holds the information about a Module. </p>
 * <ol>
 * <li>parameters<li>
 * <li>handlers<li>
 * <ol>
 * <p>Handler are registered once they are available. They are available to all services if axis2.xml
 * has a module ref="." or available to a single service if services.xml have module ref=".."</p>
 */
public class AxisModule implements ParameterInclude {

    /**
     * Field flowInclude
     */
    private final FlowInclude flowInclude = new FlowInclude();

    /**
     * Field parameters
     */
    private final ParameterInclude parameters = new ParameterIncludeImpl();
    private Module module;
    private ClassLoader moduleClassLoader;
    // To keep the File that module came from
    private URL fileName;

    /**
     * Field name
     */
    private String name;

    //This is to keep the version number of the module, if the module name is a-b-c-1.3.mar ,
    // then the module version would be 1.3
    private Version version;

    // to store module operations , which are suppose to be added to a service if it is engaged to a service
    private HashMap<QName, AxisOperation> operations = new HashMap<QName, AxisOperation>();
    private AxisConfiguration parent;

    /*
    * to store policies which are valid for any service for which the module is engaged
    */
    private PolicyInclude policyInclude = null;

    // Small description about the module
    private String moduleDescription;

    private String[] supportedPolicyNames;

    private QName[] localPolicyAssertions;
    public static final String VERSION_SNAPSHOT = "SNAPSHOT";
    public static final String MODULE_SERVICE = "moduleService";

    private PolicySubject policySubject = new PolicySubject();

    /**
     * Constructor ModuleDescription.
     */
    public AxisModule() {
    }

    /**
     * Constructor ModuleDescription.
     *
     * @param name : Name of the module
     */
    public AxisModule(String name) {
        this.name = name;
    }

    /**
     * Get the name of this module.
     * Note that it is possible to deploy several versions of the same module. Therefore,
     * the name of a module is not unique in the scope of a given {@link AxisConfiguration}.
     * 
     * @return the name of the module
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of this module.
     * 
     * @param name the name of the module
     */
    public void setName(String name) {
        this.name = name;
    }

    private String archiveName;

    /**
     * Get the archive name of this module. The archive name is the combination
     * of the module name and version (if available). In general it is equal to the
     * name of the module archive file without the suffix.
     *
     * @return the archive name of the module
     */
    public String getArchiveName() {
        if (archiveName == null){
            if(version == null){
                archiveName = name;
            } else {
                archiveName = name + "-" + version;
            }
        }
        return archiveName;
    }

    /**
     * Set the archive name of this module. This method will split the archive name
     * to extract the module name and version (which can be retrieved using
     * {@link #getName()} and {@link #getVersion()}).
     * 
     * @param archiveName the archive name of the module
     */
    public void setArchiveName(String archiveName) {
        int index = 0;
        // First look for a part that starts with a digit
        while ((index = archiveName.indexOf('-', index)) != -1) {
            char c = archiveName.charAt(++index);
            if ('0' <= c && c <= '9') {
                break;
            }
        }
        // Also support SNAPSHOT
        if (index == -1 && archiveName.endsWith("-SNAPSHOT")) {
            index = archiveName.length()-8;
        }
        
        if (index == -1) {
            name = archiveName;
            version = null;
        } else {
            try {
                version = new Version(archiveName.substring(index));
            } catch (ParseException ex) {
                version = null;
            } catch (NumberFormatException ex) {
                version = null;
            }
            if (version == null) {
                name = archiveName;
            } else {
                name = archiveName.substring(0, index-1);
            }
        }
    }

    /**
     * Get the version of this module.
     * 
     * @return the version of the module, or <code>null</code> if the module doesn't have a
     *         version number
     */
    public Version getVersion() {
        return version;
    }

    /**
     * Set the version of this module.
     * 
     * @param version the version of the module
     */
    public void setVersion(Version version) {
        this.version = version;
    }

    public void addOperation(AxisOperation axisOperation) {
        operations.put(axisOperation.getName(), axisOperation);
    }

    /**
     * @param param : Parameter to be added
     */
    public void addParameter(Parameter param) throws AxisFault {
        if (isParameterLocked(param.getName())) {
            throw new AxisFault(Messages.getMessage("paramterlockedbyparent", param.getName()));
        } else {
            parameters.addParameter(param);
        }
    }

    public void removeParameter(Parameter param) throws AxisFault {
        if (isParameterLocked(param.getName())) {
            throw new AxisFault(Messages.getMessage("paramterlockedbyparent", param.getName()));
        } else {
            parameters.removeParameter(param);
        }
    }

    public void deserializeParameters(OMElement parameterElement) throws AxisFault {
        this.parameters.deserializeParameters(parameterElement);
    }

    /**
     * @return Returns Flow.
     */
    public Flow getFaultInFlow() {
        return flowInclude.getFaultInFlow();
    }

    public Flow getFaultOutFlow() {
        return flowInclude.getFaultOutFlow();
    }

    /**
     * @return Returns Flow.
     */
    public Flow getInFlow() {
        return flowInclude.getInFlow();
    }

    /**
     * @return Returns Module.
     */
    public Module getModule() {
        return module;
    }

    public ClassLoader getModuleClassLoader() {
        return moduleClassLoader;
    }

    public HashMap<QName, AxisOperation> getOperations() {
        return operations;
    }

    /**
     * @return Returns Flow.
     */
    public Flow getOutFlow() {
        return flowInclude.getOutFlow();
    }

    /**
     * @return Returns Parameter.
     */
    public Parameter getParameter(String name) {
        return parameters.getParameter(name);
    }

    public ArrayList<Parameter> getParameters() {
        return parameters.getParameters();
    }

    public AxisConfiguration getParent() {
        return parent;
    }

    // to check whether a given parameter is locked
    public boolean isParameterLocked(String parameterName) {

        // checking the locked value of parent
        boolean loscked = false;

        if (this.parent != null) {
            loscked = this.parent.isParameterLocked(parameterName);
        }

        if (loscked) {
            return true;
        } else {
            Parameter parameter = getParameter(parameterName);

            return (parameter != null) && parameter.isLocked();
        }
    }

    /**
     * @param faultFlow : Arryalist of handlerDescriptions
     */
    public void setFaultInFlow(Flow faultFlow) {
        flowInclude.setFaultInFlow(faultFlow);
    }

    /**
     * @param faultFlow : Arryalist of HandlerDescriptions
     */
    public void setFaultOutFlow(Flow faultFlow) {
        flowInclude.setFaultOutFlow(faultFlow);
    }

    public void setInFlow(Flow inFlow) {
        flowInclude.setInFlow(inFlow);
    }

    /**
     * @param module : AxisModule
     */
    public void setModule(Module module) {
        this.module = module;
    }

    public void setModuleClassLoader(ClassLoader moduleClassLoader) {
        this.moduleClassLoader = moduleClassLoader;
    }

    public void setOutFlow(Flow outFlow) {
        flowInclude.setOutFlow(outFlow);
    }

    public void setParent(AxisConfiguration parent) {
        this.parent = parent;
    }

    public void setPolicyInclude(PolicyInclude policyInclude) {
        this.policyInclude = policyInclude;
    }

    public PolicyInclude getPolicyInclude() {
        if (policyInclude == null) {
            policyInclude = new PolicyInclude();
        }
        return policyInclude;
    }
    
    public PolicySubject getPolicySubject() {
    	return policySubject;
    }

    public String getModuleDescription() {
        return moduleDescription;
    }

    public void setModuleDescription(String moduleDescription) {
        this.moduleDescription = moduleDescription;
    }

    public String[] getSupportedPolicyNamespaces() {
        return supportedPolicyNames;
    }

    public void setSupportedPolicyNamespaces(String[] supportedPolicyNamespaces) {
        this.supportedPolicyNames = supportedPolicyNamespaces;
    }

    public QName[] getLocalPolicyAssertions() {
        return localPolicyAssertions;
    }

    public void setLocalPolicyAssertions(QName[] localPolicyAssertions) {
        this.localPolicyAssertions = localPolicyAssertions;
    }

    public URL getFileName() {
        return fileName;
    }

    public void setFileName(URL fileName) {
        this.fileName = fileName;
    }
}
