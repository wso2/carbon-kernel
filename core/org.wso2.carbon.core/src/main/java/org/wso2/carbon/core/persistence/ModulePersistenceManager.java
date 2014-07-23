/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.core.persistence;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.PolicySubject;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisConfigurator;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyComponent;
import org.apache.neethi.PolicyEngine;
import org.apache.neethi.PolicyReference;
import org.wso2.carbon.core.CarbonAxisConfigurator;
import org.wso2.carbon.core.Resources;
import org.wso2.carbon.core.multitenancy.TenantAxisConfigurator;
import org.wso2.carbon.core.util.ParameterUtil;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * The idea -
 * <p/>
 * create a module files like
 * <module name="xxx">
 * <version id="1.2" globally.engaged="false">
 * <parameter name="managedModule">true</parameter>
 * <parameter name="adminModule" locked="true">true</parameter>
 * <policies>
 * <policy policy.uuid="RMPolicy" policy.type="2" version="xxx">
 * <wsp:Policy>
 * ---
 * </wsp:Policy>
 * </policy>
 * </policies>
 * </version>
 * <version v="2.2">
 * ...
 * </version>
 * </module>
 */
@Deprecated
public class ModulePersistenceManager extends AbstractPersistenceManager {

    private static final Log log = LogFactory.getLog(ModulePersistenceManager.class);

    /**
     * Constructor gets the axis config and calls the super constructor.
     *
     * @param axisConfig - AxisConfiguration
     * @param pf
     * @throws AxisFault - if the config registry is not found
     */
    public ModulePersistenceManager(AxisConfiguration axisConfig, PersistenceFactory pf) throws AxisFault {
        super(axisConfig, pf.getModuleFilePM(), pf);
    }

    /*
     * It's recommended to use the other constructor by passing the relevant PM.
     *
     * @param axisConfig - AxisConfiguration
     * @throws AxisFault - if the config registry is not found
     */
    public ModulePersistenceManager(AxisConfiguration axisConfig) throws AxisFault {
        super(axisConfig);
        try {
            if (this.pf == null) {
                this.pf = PersistenceFactory.getInstance(axisConfig);
            }
            this.fpm = this.pf.getModuleFilePM();
        } catch (Exception e) {
            log.error("Error getting PersistenceFactory instance", e);
        }
    }

    /**
     * Returns the registry Resource for the specified Axis2 Module
     *
     * @param moduleName    - Module name
     * @param moduleVersion - Module version
     * @return - module resource
     * @throws Exception - on registry transaction error
     */
    public OMElement getModule(String moduleName, String moduleVersion) throws Exception {
        try {
            if (getCurrentFPM().fileExists(moduleName)) {
                String moduleXPath = Resources.ModuleProperties.VERSION_XPATH +
                        PersistenceUtils.getXPathAttrPredicate(Resources.ModuleProperties.VERSION_ID, moduleVersion);

                OMElement sgElement = (OMElement) getCurrentFPM().get(moduleName, moduleXPath);
                if (getModuleFilePM().getAttribute(moduleName,  moduleXPath +
                        "/@" + Resources.SUCCESSFULLY_ADDED) != null) {
                    return sgElement;
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Successfully retrieved " + moduleName + " module resource");
            }
        } catch (Throwable e) {
            handleException("Could not get the Module resource", e);
        }
        return null;
    }

    /**
     * Update the module resource with a 'globally engaged' property set to true
     *
     * @param module - AxisModule instance
     * @throws Exception - on error
     */
    public void globallyEngageModule(AxisModule module) throws Exception {
        try {
            handleGlobalModule(module, true);
            if (log.isDebugEnabled()) {
                log.debug(module.getName() + " is globally engaged");
            }
        } catch (Throwable e) {
            handleExceptionWithRollback(module.getName(), "Unable to engage " + module.getName() + " globally", e);
        }
    }

    /**
     * Update the module resource with a 'globlally engaged' property set to false
     *
     * @param module - AxisModule instance
     * @throws Exception - on error
     */
    public void globallyDisengageModule(AxisModule module) throws Exception {
        try {
            handleGlobalModule(module, false);
            if (log.isDebugEnabled()) {
                log.debug(module.getName() + " is globally disengaged");
            }
        } catch (Throwable e) {
            handleExceptionWithRollback(module.getName(), "Unable to disengage " + module.getName() + " globally", e);
        }
    }

    /**
     * Handle initialization of a new module in regsitry. Writes all parameters and engaged
     * policies into the registry.
     *
     * @param axisModule    - AxisModule instance to be persisted
     * @param moduleName    - module name
     * @param moduleVersion - module version
     * @throws Exception - on error
     */
    public void handleNewModuleAddition(AxisModule axisModule, String moduleName,
                                        String moduleVersion) throws Exception {
        try {
            //new: one file per module
            getModuleFilePM().beginTransaction(moduleName);

            getModuleFilePM().put(moduleName,
                    omFactory.createOMAttribute(Resources.NAME, null, moduleName),
                    Resources.ModuleProperties.ROOT_XPATH);
            //Create a new module version
            OMElement module = omFactory.createOMElement(Resources.VERSION, null);


            module.addAttribute(Resources.ModuleProperties.VERSION_ID, moduleVersion, null);

            AxisConfigurator configurator = axisConfig.getConfigurator();
            boolean isGloballyEngaged = false;
            if (configurator instanceof CarbonAxisConfigurator) {
                isGloballyEngaged =
                        ((CarbonAxisConfigurator) configurator).isGlobalyEngaged(axisModule);
            } else if (configurator instanceof TenantAxisConfigurator) {
                isGloballyEngaged =
                        ((TenantAxisConfigurator) configurator).isGlobalyEngaged(axisModule);
            }
            module.addAttribute(Resources.ModuleProperties.GLOBALLY_ENGAGED,
                    String.valueOf(isGloballyEngaged), null);

            getModuleFilePM().put(moduleName, module, Resources.ModuleProperties.ROOT_XPATH);

            String xpathOfCurrentModuleVersion = Resources.ModuleProperties.VERSION_XPATH +
                    PersistenceUtils.getXPathAttrPredicate(Resources.ModuleProperties.VERSION_ID, moduleVersion);
            String policiesXpath = PersistenceUtils.getResourcePath(axisModule) + "/" + Resources.POLICIES;

            // add the module parameters. ROOT_XPATH/version[@id="xxx"]
            writeParameters(moduleName, axisModule.getParameters(), xpathOfCurrentModuleVersion);
//            getModuleFilePM().put(moduleName, policiesEl, xpathOfCurrentModuleVersion);

            // Persist module policies
            List<Policy> modulePolicies = getModulePolicies(axisModule);
            if (modulePolicies != null && modulePolicies.size() > 0 &&
                    !getModuleFilePM().elementExists(moduleName, policiesXpath)) {
                OMElement policiesEl = omFactory.createOMElement(Resources.POLICIES, null);
                getModuleFilePM().put(moduleName, policiesEl, xpathOfCurrentModuleVersion);
            }
            for (Policy modulePolicy : modulePolicies) {

                if (modulePolicy.getId() == null) {
//                Generate an ID
                    modulePolicy.setId(UUIDGenerator.getUUID());
                }
//            Create a configRegistry resource from the merged module policy
                OMElement policyElement = PersistenceUtils.createPolicyElement(modulePolicy);

                /**
                 * The relevant wsp:Policy will be inside this policy element. The policy element will hold
                 * some attributes and wrap the real policy omelement
                 */
                OMElement policyWrapperElement = omFactory.createOMElement(Resources.POLICY, null);

                OMElement idElement = omFactory.createOMElement(Resources.ServiceProperties.POLICY_UUID, null);
                idElement.setText("" + modulePolicy.getId());
                policyWrapperElement.addChild(idElement);
                policyWrapperElement.addAttribute(
                        Resources.ServiceProperties.POLICY_TYPE, "" + modulePolicy.getType(), null);
                policyWrapperElement.addAttribute(Resources.VERSION, moduleVersion, null);
                policyWrapperElement.addChild(policyElement);

                getModuleFilePM().put(moduleName, policyWrapperElement, policiesXpath);
            }
            module.addAttribute(Resources.SUCCESSFULLY_ADDED, "true", null);

            getModuleFilePM().commitTransaction(moduleName);
            if (log.isDebugEnabled()) {
                log.debug("Added new module - " + axisModule.getName() + "- version " + moduleVersion);
            }
        } catch (Throwable e) {
            handleExceptionWithRollback(moduleName, "Unable to handle new module addition. Module: " +
                    Utils.getModuleName(moduleName, moduleVersion), e);
            PersistenceUtils.markFaultyModule(axisModule);
        }
    }

    /**
     * Handle initialization of an already existing module in regsitry. Writes all parameters
     * and engaged policies into the registry.
     * <p/>
     * We only need (one) relevant /version element, not the whole module with all versions.
     *
     * @param moduleResource - resource for the module
     * @param axisModule     - AxisModule instance
     * @throws Exception - on registry transaction error
     */
    public void handleExistingModuleInit(OMElement moduleResource,
                                         AxisModule axisModule) throws Exception {
        String moduleName = axisModule.getName();
        String version = PersistenceUtils.getModuleVersion(axisModule);
        try {
            getModuleFilePM().beginTransaction(moduleName);

            // Add the Module Parameters
            if (getModuleFilePM().fileExists(moduleName)) {
                //"/version/parameter";
                String xpathString = PersistenceUtils.getResourcePath(axisModule) +
                        "/" + Resources.ParameterProperties.PARAMETER;
                AXIOMXPath xpathExpression = new AXIOMXPath(xpathString);

                if (log.isDebugEnabled()) {
                    log.debug("Handling existing module " + moduleName + version +
                            "evaluating xpath " + xpathExpression);
                }

                List matchedNodes = xpathExpression.selectNodes(moduleResource);

                for (Object node : matchedNodes) {
                    OMElement paramEl = (OMElement) node;
                    Parameter parameter = ParameterUtil.createParameter(paramEl);
                    Parameter p = axisModule.getParameter(paramEl.getAttributeValue(new QName(Resources.NAME)));
                    // don't override the param if it already exists and locked..
                    if (!(p != null && p.isLocked())) {
                        axisModule.addParameter(parameter);
                    }
                }
            }
            axisModule.getPolicySubject().clear();

            // Load policies from file system into AxisModule.
            String policiesXpath = PersistenceUtils.getResourcePath(axisModule) + "/" + Resources.POLICIES + "/" + Resources.POLICY;
            if (getModuleFilePM().elementExists(moduleName, policiesXpath)) {
                AXIOMXPath xpathExpr = new AXIOMXPath(policiesXpath);
                List policyElements = xpathExpr.selectNodes(moduleResource);

                for (Object node : policyElements) {
                    OMElement policyElement = (OMElement) node;
                    /**
                     * //This is assuming that first element is a <wsp:Policy> element.
                     */
                    Policy policy = PolicyEngine.getPolicy(policyElement.getFirstChildWithName(
                            new QName(Resources.WS_POLICY_NAMESPACE, "Policy")));  //note that P is capital);
                    axisModule.getPolicySubject().attachPolicy(policy);
                }
            }

            PersistenceUtils.handleGlobalParams(axisModule, moduleResource);
            getModuleFilePM().commitTransaction(moduleName);

            if (log.isDebugEnabled()) {
                log.debug("Initialized module - " + Utils.getModuleName(axisModule.getName(),
                        version));
            }
        } catch (Throwable e) {
            handleExceptionWithRollback(moduleName, "Unable to handle module init. Module: " + Utils
                    .getModuleName(axisModule.getName(), version), e);
            PersistenceUtils.markFaultyModule(axisModule);
        }
    }

    /**
     * Remove the specified parameter from the given module
     *
     * @param module    - AxisModule instance
     * @param parameter - parameter to remove
     * @throws Exception - on error
     */
    public void removeModuleParameter(AxisModule module, Parameter parameter) throws Exception {
        String version = PersistenceUtils.getModuleVersion(module);
        removeParameter(module.getName(), parameter.getName(), Resources.ModuleProperties.VERSION_XPATH +
                PersistenceUtils.getXPathAttrPredicate(
                        Resources.ModuleProperties.VERSION_ID, version));
    }

    /**
     * Persist the given module parameter. If the parameter already exists in registry,
     * update it. Otherwise, create a new parameter.
     *
     * @param module    - AxisModule instance
     * @param parameter - parameter to persist
     * @throws Exception - on registry call errors
     */
    public void updateModuleParameter(AxisModule module, Parameter parameter) throws Exception {
        try {
            String version = PersistenceUtils.getModuleVersion(module);
//            /module/version[id="2.32"]
            String xpath = Resources.ModuleProperties.VERSION_XPATH +
                    PersistenceUtils.getXPathAttrPredicate(Resources.ModuleProperties.VERSION_ID, version);
            updateParameter(module.getName(), parameter, xpath);
        } catch (Throwable e) {
            handleExceptionWithRollback(module.getName(), "Unable to update module parameter " +
                    parameter.getName() + " of module " + module.getName(), e);
        }
    }

    /**
     * Delete the module from the registry
     *
     * @param module - AxisModule instance
     * @throws Exception - on error
     */
    public void removeModule(AxisModule module) throws Exception {
        String version = PersistenceUtils.getModuleVersion(module);
        //call removeresource with xpath
        removeResource(module.getName(), Resources.ModuleProperties.VERSION_XPATH +
                PersistenceUtils.getXPathAttrPredicate(
                        Resources.ModuleProperties.VERSION_ID, version));
    }

    /**
     * Extract all the service policies from the AxisService.
     *
     * @param axisModule the Axis2 module
     * @return module policies
     * @throws Exception on error
     */
    private List<Policy> getModulePolicies(AxisModule axisModule) throws Exception {
        // List of policies to return
        List<Policy> modulePolicies = new ArrayList<Policy>();

        PolicySubject modulePolicySubject = axisModule.getPolicySubject();
        List<PolicyComponent> policyList = new ArrayList<PolicyComponent>(modulePolicySubject
                .getAttachedPolicyComponents());

        // Get the merged module policy
        Policy policy = null;
        for (Object policyElement : policyList) {
            if (policyElement instanceof Policy) {
                policy = (policy == null) ?
                        (Policy) policyElement : policy.merge((Policy) policyElement);
            } else {
                PolicyReference policyReference = (PolicyReference) policyElement;
                String key = policyReference.getURI();
                int pos = key.indexOf('#');
                if (pos == 0) {
                    key = key.substring(1);
                } else if (pos > 0) {
                    key = key.substring(0, pos);
                }

                PolicyComponent attachedPolicyComponent = modulePolicySubject
                        .getAttachedPolicyComponent(key);

                if (attachedPolicyComponent instanceof Policy) {
                    policy = (Policy) attachedPolicyComponent;
                }
            }
        }
        if (policy != null) {
            modulePolicies.add(policy);
        }

        return modulePolicies;
    }

    /**
     * Engage or disengage module globally
     *
     * @param module - AxisModule instance
     * @param engage - whether to engage or disengage the given module
     * @throws Exception - on registry transaction errors
     */
    private void handleGlobalModule(AxisModule module, boolean engage) throws Exception {

        getCurrentFPM().beginTransaction(module.getName());
        if (getCurrentFPM().elementExists(module.getName(), PersistenceUtils.getResourcePath(module))) {
            OMElement moduleElement = getModule(module.getName(), PersistenceUtils.getModuleVersion(module));
            moduleElement.addAttribute(Resources.ModuleProperties.GLOBALLY_ENGAGED, String.valueOf(engage), null);
            getModuleFilePM().setMetaFileModification(module.getName());
        } else {
            handleException("Trying to engage or disengage unavailable module " + module.getName());
        }
        getCurrentFPM().commitTransaction(module.getName());
    }

    public void persistModulePolicy(String moduleName, String moduleVersion, Policy policy,
                                    String policyUuid, String policyType, String modulePath) throws Exception {
        boolean transactionStarted = getModuleFilePM().isTransactionStarted(moduleName);
        if (!transactionStarted) {
            getModuleFilePM().beginTransaction(moduleName);
        }
        OMFactory omFactory = OMAbstractFactory.getOMFactory();
        OMElement policyWrapperElement = omFactory.createOMElement(Resources.POLICY, null);
        policyWrapperElement.addAttribute(Resources.ServiceProperties.POLICY_TYPE, policyType, null);

        OMElement idElement = omFactory.createOMElement(Resources.ServiceProperties.POLICY_UUID, null);
        idElement.setText("" + policyUuid);
        policyWrapperElement.addChild(idElement);

        policyWrapperElement.addAttribute(Resources.VERSION, moduleVersion, null);

        OMElement policyElementToPersist = PersistenceUtils.createPolicyElement(policy);
        policyWrapperElement.addChild(policyElementToPersist);

        if (!getModuleFilePM().elementExists(moduleName, modulePath + "/" + Resources.POLICIES)) {
            getModuleFilePM().put(moduleName,
                    omFactory.createOMElement(Resources.POLICIES, null), modulePath);
        } else {
            //you must manually delete the existing policy before adding new one.
            String pathToPolicy = modulePath + "/" + Resources.POLICIES +
                    "/" + Resources.POLICY +
                    PersistenceUtils.getXPathTextPredicate(
                            Resources.ServiceProperties.POLICY_UUID, policyUuid);
            if (getModuleFilePM().elementExists(moduleName, pathToPolicy)) {
                getModuleFilePM().delete(moduleName, pathToPolicy);
            }
        }
        getModuleFilePM().put(moduleName, policyWrapperElement, modulePath +
                "/" + Resources.POLICIES);
        if (!transactionStarted) {
            getModuleFilePM().commitTransaction(moduleName);
        }
        if (log.isDebugEnabled()) {
            log.debug("Policy is saved in the file system for " + moduleName);
        }
    }

}
