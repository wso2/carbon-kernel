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
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.description.*;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.PolicyUtil;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyComponent;
import org.apache.neethi.PolicyEngine;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.core.RegistryResources;
import org.wso2.carbon.core.Resources;
import org.wso2.carbon.core.transports.TransportPersistenceManager;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.utils.deployment.GhostDeployerUtils;

import javax.xml.namespace.QName;
import java.util.*;

/**
 * Use the serviceGroupName when accessing lower layer ServiceGroupFilePersistenceManager
 * serviceGroupName = axisService.getAxisServiceGroup().getServiceGroupName();
 */
public class ServicePersistenceManager extends AbstractPersistenceManager {

    private static final String ADDRESSING_MODULE = "addressing";

    private static final Log log = LogFactory.getLog(ServicePersistenceManager.class);

    /**
     * Constructor gets the axis config and calls the super constructor.
     *
     * @param axisConfig - AxisConfiguration
     * @param pf         PersistenceFactory instance
     * @throws AxisFault - if the config registry is not found
     */
    public ServicePersistenceManager(AxisConfiguration axisConfig, PersistenceFactory pf) throws AxisFault {
        super(axisConfig, pf.getServiceGroupFilePM(), pf);
    }

    /**
     * Constructor gets the axis config and calls the super constructor.
     *
     * @param axisConfig - AxisConfiguration
     * @throws AxisFault - if the config registry is not found
     */
    public ServicePersistenceManager(AxisConfiguration axisConfig) throws AxisFault {
        super(axisConfig);
        try {
            if (this.pf == null) {
                this.pf = PersistenceFactory.getInstance(axisConfig);
            }
            this.fpm = this.pf.getServiceGroupFilePM();
        } catch (Exception e) {
            log.error("Error getting PersistenceFactory instance", e);
        }
    }

    /**
     * Returns the registry Resource for the specified AxisService
     *
     * @param axisService - AxisService instance
     * @return - service resource
     * @throws Exception - on registry transaction error
     */
    public OMElement getService(AxisService axisService) throws Exception {
        try {
            String xpathStr = PersistenceUtils.getResourcePath(axisService);
            String sgName = axisService.getAxisServiceGroup().getServiceGroupName();
            if (getServiceGroupFilePM().isTransactionStarted(sgName)
                    && getServiceGroupFilePM().elementExists(sgName, xpathStr)) {
                if (log.isDebugEnabled()) {
                    log.debug("Successfully retrieved resource for " +
                            axisService.getName() + " Service");
                }
                return (OMElement) getServiceGroupFilePM().get(sgName, xpathStr);
            } else if (getCurrentFPM().fileExists(sgName)) {
                OMElement serviceElement;
                try {
                    serviceElement = (OMElement) getCurrentFPM().get(sgName, xpathStr);
                } catch (PersistenceDataNotFoundException e) {
                    return null;
                }
                if (serviceElement != null &&
                        serviceElement.getAttributeValue(new QName(Resources.SUCCESSFULLY_ADDED)) != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully retrieved resource for " +
                                axisService.getName() + " Service");
                    }
                    return serviceElement;
                }
            }
        } catch (Throwable e) {
            handleException("Could not get the Service resource from file ", e);
        }
        return null;
    }

    /**
     * Deletes the registry resource of the specified service
     *
     * @param axisService - AxisService instance
     * @throws Exception - on registry transaction error
     */
    public void deleteService(AxisService axisService) throws Exception {
        Parameter param = axisService.getParameter(CarbonConstants.PRESERVE_SERVICE_HISTORY_PARAM);
        String xpathStr = PersistenceUtils.getResourcePath(axisService);
        String sgName = axisService.getAxisServiceGroup().getServiceGroupName();
        try {
            getServiceGroupFilePM().beginTransaction(sgName);
            if (getServiceGroupFilePM().fileExists(sgName)
                    && (param == null || !JavaUtils.isTrue(param.getValue().toString()))) {
                getServiceGroupFilePM().delete(sgName, xpathStr);
            }
            getServiceGroupFilePM().commitTransaction(sgName);
        } catch (PersistenceDataNotFoundException e) {
            log.debug(sgName + " deleteService exception", e);
            handleExceptionWithRollback(sgName, "Could not delete Service " +
                    "resource from file", e);
        }
        if (log.isDebugEnabled()) {
            log.debug("Successfully deleted " + axisService.getName() + " Service");
        }
    }

    /**
     * When a new service is deployed, persist all it's contents (operations, policies etc.)
     * into the registry
     *
     * @param axisService - AxisService instance
     * @throws Exception - on error
     */
    public void handleNewServiceAddition(AxisService axisService) throws Exception {
        if (axisService.isClientSide()) {
            return;
        }
        String sgName = axisService.getAxisServiceGroup().getServiceGroupName();
        boolean isProxyService = PersistenceUtils.isProxyService(axisService);
        synchronized (WRITE_LOCK) {
            try {
                getServiceGroupFilePM().beginTransaction(sgName);
                configRegistry.beginTransaction();
                //Add service
                OMElement serviceElement = omFactory.createOMElement(Resources.ServiceProperties.SERVICE_XML_TAG, null);
                serviceElement.addAttribute(Resources.NAME, axisService.getName(), null);
                if (axisService.getDocumentation() != null) {
                    serviceElement.addAttribute(
                            Resources.ServiceProperties.DOCUMENTATION, axisService.getDocumentation(), null);
                }
                serviceElement.addAttribute(Resources.ServiceProperties.EXPOSED_ON_ALL_TANSPORTS,
                        String.valueOf(axisService.isEnableAllTransports()), null);

                long serviceDeployedTime = new Date().getTime();
                axisService.addParameter(new Parameter(CarbonConstants
                        .SERVICE_DEPLOYMENT_TIME_PARAM, serviceDeployedTime));
                serviceElement.addAttribute(Resources.ServiceProperties.DEPLOYED_TIME,
                        String.valueOf(serviceDeployedTime), null);

                getServiceGroupFilePM().put(sgName, serviceElement, Resources.ServiceGroupProperties.ROOT_XPATH);

                // Add Service Operations
                String xpathToService = PersistenceUtils.getResourcePath(axisService);
                for (Iterator iter = axisService.getOperations(); iter.hasNext(); ) {
                    AxisOperation axisOperation = (AxisOperation) iter.next();
                    //write Operation
                    OMElement operation = PersistenceUtils.createOperation(
                            axisOperation, axisOperation.getName().getLocalPart());
                    serviceElement.addChild(operation);
                    writeParameters(sgName, axisOperation.getParameters(), PersistenceUtils.
                            getResourcePath(axisOperation)); //using axisService because it's the parent element here.
                }

                // Add Service Bindings
                Map endPointMap = axisService.getEndpoints();
                for (Object o : endPointMap.entrySet()) {
                    Map.Entry entry = (Map.Entry) o;
                    AxisBinding axisBinding = ((AxisEndpoint) entry.getValue()).getBinding();

                    //  ROOT_XPATH[@name="xxx"]/bindings
                    String bindingsPath = PersistenceUtils.getResourcePath(axisService) +
                            "/" + Resources.ServiceProperties.BINDINGS;
                    if (!getServiceGroupFilePM().elementExists(sgName, bindingsPath +
                            "/" + Resources.ServiceProperties.BINDING_XML_TAG +
                            PersistenceUtils.getXPathAttrPredicate(
                                    Resources.NAME, axisBinding.getName().getLocalPart()))) {
                        handleNewBindingAddition(axisService, axisBinding, bindingsPath);
                    }
                }

                // Add the Service Policies
                List<OMElement> servicePolicies = getServicePolicies(axisService);
                String policiesPath = PersistenceUtils.
                        getResourcePath(axisService) + "/" + Resources.POLICIES;
                if (!getServiceGroupFilePM().elementExists(sgName, policiesPath)) {
                    OMElement policiesEl = omFactory.createOMElement(Resources.POLICIES, null);
                    getServiceGroupFilePM().put(sgName, policiesEl, PersistenceUtils.getResourcePath(axisService));
                }
                for (OMElement servicePolicy : servicePolicies) {
                    getServiceGroupFilePM().put(sgName, servicePolicy, policiesPath);
                }

                //write the policy to registry as well if it's a proxy service
                if (isProxyService && servicePolicies != null && !servicePolicies.isEmpty()) {
                    org.wso2.carbon.registry.core.Resource serviceResource = configRegistry.newCollection();
                    String serviceResourcePath = PersistenceUtils.getRegistryResourcePath(axisService);
                    configRegistry.put(serviceResourcePath, serviceResource);

                    for (OMElement wrappedServicePolicyElement : servicePolicies) {
                        Policy servicePolicy = PolicyEngine.getPolicy(wrappedServicePolicyElement.getFirstChildWithName(
                                new QName(Resources.WS_POLICY_NAMESPACE, "Policy")));  //note that P is capital

                        Resource servicePolicyResource = PersistenceUtils.createPolicyResource(
                                configRegistry, servicePolicy,
                                servicePolicy.getId(),
                                "" + servicePolicy.getType());

                        configRegistry.put(serviceResourcePath + RegistryResources.POLICIES +
                                servicePolicyResource.getProperty(RegistryResources.ModuleProperties.POLICY_UUID),
                                servicePolicyResource);
                    }
                }

                // If the service scope='soapsession', engage addressing if not already engaged.
                if (axisService.getScope().equals(Constants.SCOPE_SOAP_SESSION) &&
                        !axisService.isEngaged(ADDRESSING_MODULE)) {
                    axisService.engageModule(axisService.getAxisConfiguration().getModule(
                            ADDRESSING_MODULE));
                }

                // Add the Modules Engaged to this service
                //this is how you handle associations of registry
                for (Object node : axisService.getEngagedModules()) {
                    AxisModule axisModule = (AxisModule) node;
                    //we just put each modules inside top-level service element
                    String version = PersistenceUtils.getModuleVersion(axisModule);
                    if (!isGloballyEngaged(axisModule.getName(), version)
                            && !axisService.getParent().isEngaged(axisModule.getName())) {
                        OMElement module = omFactory.createOMElement(
                                Resources.ModuleProperties.MODULE_XML_TAG, null);
                        module.addAttribute(Resources.NAME, axisModule.getName(), null);
                        module.addAttribute(Resources.VERSION, version, null);
                        module.addAttribute(Resources.ModuleProperties.TYPE,
                                Resources.Associations.ENGAGED_MODULES, null);

                        getServiceGroupFilePM().put(sgName, module, Resources.ServiceProperties.ROOT_XPATH);
                    }
                }

                // Save the operation-module engagements
                for (Iterator iter = axisService.getOperations(); iter.hasNext(); ) {
                    AxisOperation axisOperation = (AxisOperation) iter.next();
                    for (Object o : axisOperation.getEngagedModules()) {
                        AxisModule axisModule = (AxisModule) o;
                        String version = PersistenceUtils.getModuleVersion(axisModule);
                        if (!isGloballyEngaged(axisModule.getName(), version)
                                && !axisService.getParent().isEngaged(axisModule.getName())
                                && !axisService.isEngaged(axisModule.getName())) {
                            OMElement module = PersistenceUtils.createModule(axisModule.getName(),
                                    version,
                                    Resources.Associations.ENGAGED_MODULES);

                            getServiceGroupFilePM().put(sgName, module, PersistenceUtils
                                    .getResourcePath(axisOperation));
                        }
                    }
                }

                // add the service parameters
                writeParameters(sgName, axisService.getParameters(), xpathToService);

                // add transport associations
                if (!axisService.isEnableAllTransports()) {
                    List<String> exposedTransports = axisService.getExposedTransports();
                    for (String exposedTransport : exposedTransports) {
                        Resource transportResource =
                                new TransportPersistenceManager(axisConfig).
                                        getTransportResource(exposedTransport);
                        if (transportResource == null) {
                            throw new CarbonException("The configuration resource " +
                                    "for " + exposedTransport + " transport does not exist in Registry");
                        }
                        OMElement association = omFactory.createOMElement(
                                Resources.Associations.ASSOCIATION_XML_TAG, null);
                        association.addAttribute(Resources.Associations.DESTINATION_PATH,
                                transportResource.getPath(), null);
                        association.addAttribute(
                                Resources.ModuleProperties.TYPE, Resources.Associations.EXPOSED_TRANSPORTS, null);
                        getServiceGroupFilePM().put(sgName, association, xpathToService);
                    }
                }
                serviceElement = (OMElement) getServiceGroupFilePM().get(sgName, xpathToService);
                if (serviceElement != null) {
                    serviceElement.addAttribute(Resources.SUCCESSFULLY_ADDED, "true", null);
                    getServiceGroupFilePM().setMetaFileModification(sgName);
                }

                getServiceGroupFilePM().commitTransaction(sgName);
                configRegistry.commitTransaction();
                if (log.isDebugEnabled()) {
                    log.debug("Added new service - " + axisService.getName());
                }
            } catch (Throwable e) {
                configRegistry.rollbackTransaction();
                handleExceptionWithRollback(sgName, "Unable to handle new service addition. Service: " +
                        axisService.getName(), e);
            }
        }
    }

    /**
     * Bindings xml format
     * <pre>
     * {@code <bindings>
     *     <binding name="xxx">
     *         <operation name="yyy>
     *
     *         </operation>
     *     </binding>
     * </bindings>
     * }
     * </pre>
     * <br/>
     *
     * @param axisService     the service
     * @param axisBinding     The binding to be handled
     * @param xpathToBindings The xpath where this binding should be put
     * @throws PersistenceException if error exists in operation
     */
    private void handleNewBindingAddition(AxisService axisService,
                                          AxisBinding axisBinding, String xpathToBindings) throws PersistenceException {
        String serviceGroupId = axisService.getAxisServiceGroup().getServiceGroupName();
        OMElement bindings = (OMElement) getServiceGroupFilePM().get(serviceGroupId, xpathToBindings);
        if (bindings == null) { //bindings element does not exist
            bindings = omFactory.createOMElement(Resources.ServiceProperties.BINDINGS, null);
        } else {
            /**
             * we detach this from parent element because we will be adding this again at the end of
             * method. Otherwise, there will be duplicated items.
             */
            bindings.detach();
        }

        OMElement bindingElement = omFactory.createOMElement(Resources.ServiceProperties.BINDING_XML_TAG, null, bindings);
        bindingElement.addAttribute(Resources.NAME, axisBinding.getName().getLocalPart(), null);

        //Add binding operations
        Iterator operations = axisBinding.getChildren();
        while (operations.hasNext()) {
            AxisBindingOperation bo = (AxisBindingOperation) operations.next();
            bindingElement.addChild(PersistenceUtils.createOperation(
                    bo, bo.getName().getLocalPart()));
        }
        getServiceGroupFilePM().put(serviceGroupId, bindings,
                Resources.ServiceProperties.ROOT_XPATH + PersistenceUtils.getXPathAttrPredicate(
                        Resources.NAME,
                        axisService.getName()));
    }

    /**
     * Handle initialization of an already existing service in regsitry. Loads all parameters,
     * policies etc. into the service instance.
     *
     * @param axisService - AxisService instance
     * @throws Exception - on error
     */
    public void handleExistingServiceInit(AxisService axisService)
            throws Exception {

        AxisService actualService = null;
        Parameter isGhostService = axisService.getParameter(CarbonConstants.GHOST_SERVICE_PARAM);
        if (isGhostService != null && "true".equals(isGhostService.getValue())) {
            actualService = GhostDeployerUtils.deployActualService(axisConfig, axisService);
            if (actualService != null) {
                axisService = actualService;
            }
        }

        boolean isProxyService = PersistenceUtils.isProxyService(axisService);
        boolean wsdlChangeDetected = false;

        String serviceGroupId = axisService.getAxisServiceGroup().getServiceGroupName();

        try {
            boolean isTransactionStarted = getServiceGroupFilePM().isTransactionStarted(serviceGroupId);
            if(!isTransactionStarted) {
                getServiceGroupFilePM().beginTransaction(serviceGroupId);
            }
            String serviceElementPath = PersistenceUtils.getResourcePath(axisService);
            OMElement serviceElement = (OMElement) getServiceGroupFilePM().get(serviceGroupId, serviceElementPath);

            // Fetch and attach Service policies
            if (!isProxyService) {
                loadPolicies(serviceGroupId, axisService, getServiceGroupFilePM().getAll(serviceGroupId,
                        serviceElementPath + "/" + Resources.ServiceProperties.POLICY_UUID), serviceElementPath);
            }

            for (Iterator iter = axisService.getOperations(); iter.hasNext(); ) {
                AxisOperation axisOperation = (AxisOperation) iter.next();
                String operationXPath = PersistenceUtils.getResourcePath(axisOperation);

                // check whether the operation exists
                if (getServiceGroupFilePM().elementExists(serviceGroupId, operationXPath)) {
                    // Fetch and attach Operation and Message Policies
                    if (!axisOperation.isControlOperation()) {
                        // First load operation policies
                        if (!isProxyService) {
                            loadPolicies(serviceGroupId, axisOperation, getServiceGroupFilePM().getAll(serviceGroupId,
                                    operationXPath + "/" + Resources.ServiceProperties.POLICY_UUID),
                                    serviceElementPath);
                        }

                        // Fetch and attach MessageIn policies for this operation
                        if (!(axisOperation instanceof OutOnlyAxisOperation) && !isProxyService) {
                            loadPolicies(serviceGroupId, axisOperation.getMessage(
                                    WSDLConstants.MESSAGE_LABEL_IN_VALUE), getServiceGroupFilePM().getAll(serviceGroupId,
                                    operationXPath + "/" + Resources.ServiceProperties.MESSAGE_IN_POLICY_UUID),
                                    serviceElementPath);
                        }

                        // Fetch and attach MessageOut policies for this operation
                        if (!(axisOperation instanceof InOnlyAxisOperation) && !isProxyService) {
                            loadPolicies(serviceGroupId, axisOperation.getMessage(
                                    WSDLConstants.MESSAGE_LABEL_OUT_VALUE), getServiceGroupFilePM().getAll(serviceGroupId,
                                    operationXPath + "/" + Resources.ServiceProperties.MESSAGE_OUT_POLICY_UUID),
                                    serviceElementPath);
                        }

                        // Disengage all the statically engaged modules (i.e. those module engaged
                        // from the services.xml file)
                        for (AxisModule axisModule : axisOperation.getEngagedModules()) {
                                axisOperation.disengageModule(axisModule);
                        }
//                        axisOperation.getEngagedModules().clear();

                        List moduleList = getServiceGroupFilePM().getAll(serviceGroupId,
                                operationXPath + "/" + Resources.ModuleProperties.MODULE_XML_TAG);

//                        Associat ion[] associations = configRegistry.getAssociations(operationPath,
//                                Resources.Associations.ENGAGED_MODULES);
                        for (Object node : moduleList) {
                            OMElement module = (OMElement) node;
                            String modName = module.getAttributeValue(new QName(Resources.NAME));
                            String modVersion = module.getAttributeValue(new QName(Resources.VERSION));
                            AxisModule axisModule = getExistingAxisModule(modName, modVersion);
                            if (!isGloballyEngaged(modName, modVersion) && !axisService.isEngaged(axisModule)) {
                                axisOperation.engageModule(axisModule);
                            }
                        }
                        // Handle operation parameters
                        loadParameters(serviceGroupId, axisOperation, operationXPath +
                                "/" + Resources.ParameterProperties.PARAMETER);

                        // Handle operation documentation
                        loadDocumentation(serviceGroupId, axisOperation, operationXPath);
                    }
                } else {
                    wsdlChangeDetected = true;

                    OMElement operationElement = PersistenceUtils.createOperation(
                            axisOperation, axisOperation.getName().getLocalPart());
                    getServiceGroupFilePM().put(serviceGroupId, operationElement, serviceElementPath);
                    writeParameters(serviceGroupId, axisOperation.getParameters(),
                            PersistenceUtils.getResourcePath(axisOperation));

                    for (Object o : axisOperation.getEngagedModules()) {
                        AxisModule axisModule = (AxisModule) o;
                        String version = PersistenceUtils.getModuleVersion(axisModule);

                        if (!isGloballyEngaged(axisModule.getName(), version)
                                && !axisService.getParent().isEngaged(axisModule.getName())
                                && !axisService.isEngaged(axisModule.getName())) {
                            OMElement moduleElement = PersistenceUtils.
                                    createModule(axisModule.getName(), version, Resources.Associations.ENGAGED_MODULES);
                            getServiceGroupFilePM().put(serviceGroupId, moduleElement,
                                    PersistenceUtils.getResourcePath(axisOperation));
                        }
                    }
                }
            }

            // sync up the operations, required by the proxy services : Ruwan
            String operationsPath = serviceElementPath + "/" + Resources.OPERATION;
            List operations = getServiceGroupFilePM().getAll(serviceGroupId, operationsPath);
            for (Object node : operations) {
                OMElement operation = (OMElement) node;
                String opName = operation.getAttributeValue(new QName(Resources.NAME));
                if (axisService.getOperation(new QName(opName)) == null) {
                    wsdlChangeDetected = true;
                    // new service do not have the operation
                    operation.detach();
                }
            }

            // Fetch and attach Binding, Binding operation and their Message policies
            Map endPointMap = axisService.getEndpoints();
            for (Object o : endPointMap.entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                AxisEndpoint point = (AxisEndpoint) entry.getValue();
                if (point == null || point.getBinding() == null || point.getBinding().getName() == null) {
                    if (log.isDebugEnabled()) {
                        if (point != null) {
                            log.debug("The axis binding does not exist for SG " + serviceGroupId);
                        } else {
                            log.debug("The axis binding does not exist for " + point.getName() + serviceGroupId);
                        }
                    }
                    continue;
                }

                AxisBinding currentAxisBinding = point.getBinding();

                // Fetch binding policies
                String bindingXPath = serviceElementPath + "/" + Resources.ServiceProperties.BINDINGS +
                        "/" + Resources.ServiceProperties.BINDING_XML_TAG +
                        PersistenceUtils.getXPathAttrPredicate(
                                Resources.NAME, currentAxisBinding.getName().getLocalPart());
                if (getServiceGroupFilePM().elementExists(serviceGroupId, bindingXPath)) {
                    if (isProxyService) {
                        // This is to ensure that binding level policies applied from the UI
                        // get precedence over service level policies for proxy services
                        java.util.Collection<PolicyComponent> attachedPolicies = axisService.getPolicySubject().
                                getAttachedPolicyComponents();
                        if (attachedPolicies != null && !attachedPolicies.isEmpty()) {
                            List tmpProperties = getServiceGroupFilePM().getAll(serviceGroupId,
                                    bindingXPath + "/" + Resources.ServiceProperties.POLICY_UUID + "/text()");
                            List<String> properties = new ArrayList<String>(tmpProperties.size());

                            for (Object node : tmpProperties) {
                                if (node instanceof OMText) {
                                    properties.add(((OMText) node).getText());
                                }
                            }
                            if (properties != null && properties.size() > 0) {
                                List<String> removablePolicies = new ArrayList<String>();
                                for (PolicyComponent pc : attachedPolicies) {
                                    if (pc instanceof Policy) {
                                        String id = ((Policy) pc).getId();
                                        if (properties.contains(id)) {
                                            removablePolicies.add(id);
                                        }
                                    }
                                }
                                for (String id : removablePolicies) {
                                    axisService.getPolicySubject().detachPolicyComponent(id);
                                }
                            }
                        }
                    }

                    loadPolicies(serviceGroupId, currentAxisBinding, getServiceGroupFilePM().getAll(serviceGroupId,
                            bindingXPath + "/" + Resources.ServiceProperties.POLICY_UUID),
                            serviceElementPath);
                    Iterator operationsItr = currentAxisBinding.getChildren();
                    while (operationsItr.hasNext()) {
                        AxisBindingOperation bindingOp = (AxisBindingOperation) operationsItr.next();

                        // Fetch and attach binding operation policies
                        String bindingOpPath = PersistenceUtils
                                .getBindingOperationPath(serviceElementPath, bindingOp);
                        if (getServiceGroupFilePM().elementExists(serviceGroupId, bindingOpPath)) {
                            loadPolicies(serviceGroupId, bindingOp, getServiceGroupFilePM().getAll(serviceGroupId,
                                    bindingOpPath + "/" + Resources.ServiceProperties.
                                            POLICY_UUID),
                                    serviceElementPath);
                            // Fetch and attach MessageIn policies for this operation
                            loadPolicies(serviceGroupId, bindingOp.getChild(
                                    WSDLConstants.MESSAGE_LABEL_IN_VALUE), getServiceGroupFilePM().getAll(serviceGroupId,
                                    bindingOpPath + "/" + Resources.ServiceProperties.
                                            MESSAGE_IN_POLICY_UUID),
                                    serviceElementPath);
                            // Fetch and attach MessageOut policies for this operation
                            loadPolicies(serviceGroupId, bindingOp.getChild(
                                    WSDLConstants.MESSAGE_LABEL_OUT_VALUE),
                                    getServiceGroupFilePM().getAll(serviceGroupId,
                                            bindingOpPath + "/" + Resources.ServiceProperties.
                                                    MESSAGE_OUT_POLICY_UUID),
                                    serviceElementPath);
                        } else {
                            OMElement opElement = PersistenceUtils.
                                    createOperation(bindingOp, bindingOp.getName().getLocalPart());
                            //get the parent of the binding operation by using getAxisBinding
                            getServiceGroupFilePM().put(serviceGroupId, opElement,
                                    PersistenceUtils.getBindingPath(serviceElementPath, bindingOp.getAxisBinding()));
                        }
                    }
                } else {
//                    if (! getServiceGroupFilePM().elementExists(serviceGroupId, bindingXPath)) {
//                        handleNewBindingAddition(serviceGroupId, currentAxisBinding, serviceElementPath+"/"+Resources.ServiceProperties.BINDINGS);
//                    }
                    handleNewBindingAddition(axisService, currentAxisBinding,
                            serviceElementPath + "/" + Resources.ServiceProperties.BINDINGS);
                }

            }
            // Disengage all the statically engaged modules (i.e. those module
            // engaged from the services.xml file)
            for (AxisModule axisModule : axisService.getEngagedModules()) {
                    axisService.disengageModule(axisModule);
            }
//            axisService.getEngagedModules().clear();

            // Engage modules to service
            List engModules = getServiceGroupFilePM().getAll(serviceGroupId, serviceElementPath + "/" +
                    Resources.ModuleProperties.MODULE_XML_TAG);
            for (Object node : engModules) {
                String modName = ((OMElement) node).getAttributeValue(new QName(Resources.NAME));
                String modVersion = ((OMElement) node).getAttributeValue(new QName(Resources.VERSION));
                AxisModule axisModule = getExistingAxisModule(modName, modVersion);
                if (!isGloballyEngaged(modName, modVersion)) {
                    axisService.disengageModule(axisModule);
                    axisService.engageModule(axisModule);
                }
            }

            // add description
            if (wsdlChangeDetected) {
                if (axisService.getDocumentation() != null) {
                    serviceElement.addAttribute(Resources.ServiceProperties.DOCUMENTATION,
                            axisService.getDocumentation(), null);
                }
            } else {
                loadDocumentation(serviceGroupId, axisService, serviceElementPath);
            }

            // If the current service is proxy service, write existing params into registry, because the proxy
            // editing also supports parameter editing, to which we should give the precedence
            if (isProxyService) {
                ArrayList<Parameter> availableParameters = axisService.getParameters();
                // Adding the parameters to the configRegistry
                ListIterator<Parameter> ite2 = availableParameters.listIterator();
                while (ite2.hasNext()) {
                    Parameter serviceParameter = ite2.next();
                    if (serviceParameter.getParameterType() != Parameter.ANY_PARAMETER) {
                        updateServiceParameter(axisService, serviceParameter);
                    }
                }
            }
            loadParameters(serviceGroupId, axisService,
                    serviceElementPath + "/" + Resources.ParameterProperties.PARAMETER);

            // Handle existing transports
            if (isProxyService) {
                List<String> availableTransports = axisService.getExposedTransports();
                ListIterator<String> transportItr = availableTransports.listIterator();

                // Removing transports from file
                List associations = getServiceGroupFilePM().getAssociations(serviceGroupId,
                        serviceElementPath, Resources.Associations.EXPOSED_TRANSPORTS);
                for (Object node : associations) {
                    ((OMElement) node).detach();
                }

                // set EXPOSED_ON_ALL_TANSPORTS property to 'false'
                String allTransports = serviceElement.getAttributeValue(new QName(
                        Resources.ServiceProperties.EXPOSED_ON_ALL_TANSPORTS));
                if (allTransports != null && "true".equals(allTransports)) {
                    serviceElement.addAttribute(Resources.ServiceProperties
                            .EXPOSED_ON_ALL_TANSPORTS, String.valueOf(false), null);
                }

                // Adding the transports to the file
                while (transportItr.hasNext()) {
                    String transport = transportItr.next();
                    Resource transportResource =
                            new TransportPersistenceManager(axisConfig).
                                    getTransportResource(transport);
                    if (transportResource == null) {
                        throw new CarbonException("The configuration resource for " + transport +
                                " transport does not exist");
                    }
                    OMElement association = PersistenceUtils.createAssociation(
                            transportResource.getPath(), Resources.Associations.EXPOSED_TRANSPORTS);
                    getServiceGroupFilePM().put(serviceGroupId, association, serviceElementPath);
                    if (log.isDebugEnabled()) {
                        log.debug("Added " + transport + " transport binding for " +
                                axisService.getName() + " service");
                    }
                }
            } else {
                if (!Boolean.valueOf(serviceElement.getAttributeValue(
                        new QName(Resources.ServiceProperties.EXPOSED_ON_ALL_TANSPORTS)))) {
                    axisService.setExposedTransports(new ArrayList());
                    List associations = getServiceGroupFilePM().getAssociations(serviceGroupId, serviceElementPath,
                            Resources.Associations.EXPOSED_TRANSPORTS);
                    for (Object node : associations) {
                        String destinationPath = ((OMElement) node).getAttributeValue(
                                new QName(Resources.Associations.DESTINATION_PATH));
                        Resource resource = configRegistry.get(destinationPath);
                        String transportProtocol = resource
                                .getProperty(RegistryResources.Transports.PROTOCOL_NAME);
                        axisService.addExposedTransport(transportProtocol);
                        resource.discard();
                    }
                }
            }

            // Set Deployment Time
            String serviceDeployedTime = serviceElement.getAttributeValue(
                    new QName(Resources.ServiceProperties.DEPLOYED_TIME));
            if (serviceDeployedTime != null) {
                axisService.addParameter(new Parameter(CarbonConstants
                        .SERVICE_DEPLOYMENT_TIME_PARAM, Long.parseLong(serviceDeployedTime)));
            }
            // Activate/Deactivate service
            String serviceState = serviceElement.getAttributeValue(new QName(Resources.ServiceProperties.ACTIVE));
            if (serviceState == null || serviceState.trim().length() == 0) {
                serviceState = "true";
            }
            axisService.setActive(Boolean.parseBoolean(serviceState));

            if(!isTransactionStarted) {
                getServiceGroupFilePM().commitTransaction(serviceGroupId);
            }

            if (log.isDebugEnabled()) {
                log.debug("Initialized service - " + axisService.getName());
            }
        } catch (Throwable e) {
            handleExceptionWithRollback(axisService.getAxisServiceGroup().getServiceGroupName(),
                    "Unable to handle service initialization. Service: " +
                            axisService.getName(), e);
        }
    }

    /**
     * Handle the engagement of the module to service at the registry level
     *
     * @param module  - AxisModule instance
     * @param service - AxisService instance
     * @throws Exception - on error
     */
    public void engageModuleForService(AxisModule module, AxisService service) throws Exception {
        try {
            handleModuleForAxisDescription(service.getAxisServiceGroup().getServiceGroupName(),
                    module, PersistenceUtils.getResourcePath(service), true);
            if (log.isDebugEnabled()) {
                log.debug("Successfully engaged " + module.getName() + " module for " +
                        service.getName() + "service");
            }
        } catch (Throwable e) {
            handleExceptionWithRollback(module.getName(), "Unable to engage " + module.getName() +
                    " module to " + service.getName() + " service", e);
        }
    }

    /**
     * Handle the dis-engagement of the module to service at the registry level
     *
     * @param module  - AxisModule instance
     * @param service - AxisService instance
     * @throws Exception - on error
     */
    public void disengageModuleForService(AxisModule module, AxisService service) throws Exception {
        try {
            handleModuleForAxisDescription(service.getAxisServiceGroup().getServiceGroupName(),
                    module, PersistenceUtils.getResourcePath(service), false);
            if (log.isDebugEnabled()) {
                log.debug("Successfully disengaged " + module.getName() + " module from " +
                        service.getName() + "service");
            }
        } catch (Throwable e) {
            handleExceptionWithRollback(module.getName(), "Unable to disengage " + module.getName() +
                    " module from " + service.getName() + " service", e);
        }
    }

    /**
     * Remove the specified parameter from the given service
     *
     * @param service   - AxisService instance
     * @param parameter - parameter to remove
     * @throws Exception - on error
     */
    public void removeServiceParameter(AxisService service, Parameter parameter) throws Exception {
        removeParameter(service.getAxisServiceGroup().getServiceGroupName(),
                parameter.getName(), PersistenceUtils.getResourcePath(service));
    }

    /**
     * Set the given property to the service resource in the registry
     *
     * @param service       - AxisService instance
     * @param propertyName  - name of the property to set
     * @param propertyValue - value to set
     * @throws Exception - on error
     */
    public void setServiceProperty(AxisService service, String propertyName,
                                   String propertyValue) throws Exception {
        try {
            String serviceXPath = PersistenceUtils.getResourcePath(service);
            String sgName = service.getAxisServiceGroup().getServiceGroupName();
            boolean transactionStarted = getServiceGroupFilePM().isTransactionStarted(sgName);
            if (!transactionStarted) {
                getServiceGroupFilePM().beginTransaction(sgName);
            }

            if (getServiceGroupFilePM().elementExists(sgName, serviceXPath)) {
                OMElement serviceElement = (OMElement) getServiceGroupFilePM().get(sgName, serviceXPath);
                serviceElement.addAttribute(propertyName, propertyValue, null);
                getServiceGroupFilePM().setMetaFileModification(sgName);
            }

            if (!transactionStarted) {
                getServiceGroupFilePM().commitTransaction(sgName);
            }
            if (log.isDebugEnabled()) {
                log.debug("Successfully set " + propertyName + " property for " +
                        service.getName() + "service");
            }
        } catch (Throwable e) {
            handleExceptionWithRollback(service.getAxisServiceGroup().getServiceGroupName(),
                    "Unable to set property " + propertyName +
                            " to service " + service.getName(), e);
        }
    }

    /**
     * Set the given property to the service resource in the registry
     *
     * @param service      - AxisService instance
     * @param propertyName - name of the property to delete
     * @throws Exception - on error
     */
    public void deleteServiceProperty(AxisService service, String propertyName) throws Exception {
        try {
            String serviceResourcePath = PersistenceUtils.getResourcePath(service);
            String sgName = service.getAxisServiceGroup().getServiceGroupName();
            boolean transactionStarted = getServiceGroupFilePM().isTransactionStarted(sgName);
            if (!transactionStarted) {
                getServiceGroupFilePM().beginTransaction(sgName);
            }

            if (getServiceGroupFilePM().elementExists(sgName, serviceResourcePath)) {
                OMElement serviceElement = (OMElement) getServiceGroupFilePM().get(sgName, serviceResourcePath);
                serviceElement.removeAttribute(serviceElement.getAttribute(new QName(propertyName)));
                getServiceGroupFilePM().setMetaFileModification(sgName);
            }

            if (!transactionStarted) {
                getServiceGroupFilePM().commitTransaction(sgName);
            }
            if (log.isDebugEnabled()) {
                log.debug("Successfully set " + propertyName + " property for " +
                        service.getName() + "service");
            }
        } catch (Throwable e) {
            handleExceptionWithRollback(service.getAxisServiceGroup().getServiceGroupName(),
                    "Unable to set property " + propertyName +
                            " to service " + service.getName(), e);
        }
    }

    /**
     * NOTE: This <b>must</b> not be used for adding module associations.
     * For modules, see PersistenceUtils#createModule
     *
     * @param service
     * @param destinationPath
     * @param type
     */
    public void updateServiceAssociation(AxisService service, String destinationPath, String type) throws
            PersistenceException {
        OMElement assoc = PersistenceUtils.createAssociation(destinationPath, type);
        String serviceGroupId = service.getAxisServiceGroup().getServiceGroupName();
        String serviceXPath = PersistenceUtils.getResourcePath(service);
        String assocXPath = serviceXPath +
                "/" + Resources.Associations.ASSOCIATION_XML_TAG +
                PersistenceUtils.getXPathAttrPredicate(Resources.Associations.DESTINATION_PATH, destinationPath);
        PersistenceUtils.getXPathAttrPredicate(Resources.ModuleProperties.TYPE, type);

        boolean transactionStarted = getServiceGroupFilePM().isTransactionStarted(serviceGroupId);
        if (!transactionStarted) {
            getServiceGroupFilePM().beginTransaction(serviceGroupId);
        }

        if (getServiceGroupFilePM().elementExists(serviceGroupId, assocXPath)) {
            return;
        } else {
            getServiceGroupFilePM().put(serviceGroupId, assoc, serviceXPath);
        }

        if (!transactionStarted) {
            getServiceGroupFilePM().commitTransaction(serviceGroupId);
        }

    }

    /**
     * Persist the given service parameter. If the parameter already exists in registry, update
     * it. Otherwise, create a new parameter.
     *
     * @param service   - AxisService instance
     * @param parameter - parameter to persist
     * @throws Exception - on registry call errors
     */
    public void updateServiceParameter(AxisService service, Parameter parameter) throws Exception {
        try {
            updateParameter(service.getAxisServiceGroup().getServiceGroupName(), parameter,
                    PersistenceUtils.getResourcePath(service));
        } catch (Exception e) {
            handleExceptionWithRollback(service.getAxisServiceGroup().getServiceGroupName(),
                    "Unable to update the service parameter " +
                            parameter.getName() + " of service " + service.getName(), e);
        }
    }

    /**
     * Check whether the specified parameter already exists. If yes, return the value of it. If
     * not, write a new parameter with the specified value
     *
     * @param service    - AxisService instance
     * @param paramName  - name of the parameter to check
     * @param paramValue - value to be set
     * @return - if param found, stored value. Otherwise the original value
     * @throws Exception - on error
     */
    /*public String getExistingValueOrUpdateParameter(AxisService service, String
            paramName, String paramValue) throws Exception {
        String serviceResourcePath = PersistenceUtils.getResourcePath(service);
//        String serviceParamResourcePath = serviceResourcePath + Resources.PARAMETERS
//                + paramName;

        String returnValue = paramValue;
        try {
        */
    /*
    todo uncomment and fix errors getExistingValueOrUpdateParameter
    configRegistry.beginTransaction();
    Resource serviceParamResource;
    if (!configRegistry.resourceExists(serviceParamResourcePath)) {
        serviceParamResource = configRegistry.newResource();
        serviceParamResource
                .addProperty(Resources.ParameterProperties.NAME, paramName);
        serviceParamResource
                .addProperty(Resources.ParameterProperties.VALUE, paramValue);
        serviceParamResource.setContent("<parameter name=\"" + paramName
                + "\">" + paramValue + "</parameter>");
        configRegistry.put(serviceParamResourcePath, serviceParamResource);
        serviceParamResource.discard();
    } else {
        serviceParamResource = configRegistry.get(serviceParamResourcePath);
        returnValue = serviceParamResource
                .getProperty(Resources.ParameterProperties.VALUE);
    }
    configRegistry.commitTransaction();
    */
    /* } catch (Throwable e) {
            handleExceptionWithRollback(service.getAxisServiceGroup().getServiceGroupName(),
                    "Unable to update the service parameter " +
                            paramName + " to the service " + service.getName(), e);
        }
        return returnValue;
    }*/

    /**
     * Removes an exposed transport from a given service.
     *
     * @param serviceName       - Name of the service where new transport to be removed.
     * @param transportProtocol - Name of the transport to be removed.
     * @throws Exception - on error
     */
    public void removeExposedTransports(String serviceName,
                                        String transportProtocol) throws Exception {
        AxisService axisService = axisConfig.getServiceForActivation(serviceName);

        if (axisService == null) {
            handleException("No service found for the provided service name : " + serviceName);
            return;
        }

        String serviceGroupId = axisService.getAxisServiceGroup().getServiceGroupName();

        try {
            Resource transportResource =
                    new TransportPersistenceManager(axisConfig).
                            getTransportResource(transportProtocol);

            boolean transactionStarted = getServiceGroupFilePM().isTransactionStarted(serviceGroupId);
            if (!transactionStarted) {
                getServiceGroupFilePM().beginTransaction(serviceGroupId);
            }

            //OMElement serviceElement = getService(axisService);
            if (transportResource != null) {
                String transportXPath = PersistenceUtils.getResourcePath(axisService) +
                        "/" + Resources.Associations.ASSOCIATION_XML_TAG +
                        PersistenceUtils.getXPathAttrPredicate(
                                Resources.Associations.DESTINATION_PATH, transportResource.getPath()) +
                        PersistenceUtils.getXPathAttrPredicate(
                                Resources.ModuleProperties.TYPE, Resources.Associations.EXPOSED_TRANSPORTS);
                if (getServiceGroupFilePM().elementExists(serviceGroupId, transportXPath)) {
                    getServiceGroupFilePM().delete(serviceGroupId,
                            PersistenceUtils.getResourcePath(axisService) +
                                    "/" + Resources.Associations.ASSOCIATION_XML_TAG +
                                    PersistenceUtils.getXPathAttrPredicate(
                                            Resources.Associations.DESTINATION_PATH, transportResource.getPath()) +
                                    PersistenceUtils.getXPathAttrPredicate(
                                            Resources.ModuleProperties.TYPE, Resources.Associations.EXPOSED_TRANSPORTS));
                }
                transportResource.discard();
            }

            List<String> exposedTrps = axisService.getExposedTransports();
            for (String transport : exposedTrps) {
                transportResource =
                        new TransportPersistenceManager(axisConfig).getTransportResource(transport);
                if (transportResource == null) {
                    throw new CarbonException("The configuration resource for " + transport +
                            " transport does not exist");
                }
                OMElement assocElement = PersistenceUtils.createAssociation(
                        transportResource.getPath(), Resources.Associations.EXPOSED_TRANSPORTS);
                String assocPath = PersistenceUtils.getResourcePath(axisService) +
                        "/" + Resources.Associations.ASSOCIATION_XML_TAG +
                        PersistenceUtils.getXPathAttrPredicate(
                                Resources.Associations.DESTINATION_PATH, transportResource.getPath()) +
                        PersistenceUtils.getXPathAttrPredicate(
                                Resources.ModuleProperties.TYPE, Resources.Associations.EXPOSED_TRANSPORTS);

                if (!getServiceGroupFilePM().elementExists(serviceGroupId, assocPath)) {
                    getServiceGroupFilePM().put(
                            serviceGroupId, assocElement, PersistenceUtils.getResourcePath(axisService));
                }
                transportResource.discard();
            }

            setServiceProperty(axisService, Resources.ServiceProperties.EXPOSED_ON_ALL_TANSPORTS, String.valueOf(false));

            if (!transactionStarted) {
                getServiceGroupFilePM().commitTransaction(serviceGroupId);
            }

            if (log.isDebugEnabled()) {
                log.debug("Successfully removed " + transportProtocol + " transport from " +
                        serviceName + "service");
            }
        } catch (Exception e) {
            handleExceptionWithRollback(serviceGroupId, "Error while removing exposed transport : " +
                    transportProtocol, e);
        }
    }

    /**
     * Extract all the policies from the AxisService and create registry Resources for them.
     *
     * @param axisService Service to get policies
     * @return A list of "wrapped" policy elements
     * @throws Exception on error
     */
    private List<OMElement> getServicePolicies(AxisService axisService) throws Exception {
        // List of policy resources to be returned
        List<OMElement> policyElements = new ArrayList<OMElement>();
        String serviceGroupId = axisService.getAxisServiceGroup().getServiceGroupName();
        String serviceXPath = PersistenceUtils.getResourcePath(axisService);

        // Get Service Policy
        List<PolicyComponent> servicePolicyList = new ArrayList<PolicyComponent>(axisService
                .getPolicySubject().getAttachedPolicyComponents());
        Policy servicePolicy = PolicyUtil.getMergedPolicy(servicePolicyList, axisService);

        if (servicePolicy != null) {
            // Add this policy as a resource to the list
            addPolicyElement(policyElements, servicePolicy, PolicyInclude.AXIS_SERVICE_POLICY);
            // Refer this policy from the service
            setResourcePolicyId(axisService.getAxisServiceGroup().getServiceGroupName(),
                    serviceXPath, servicePolicy.getId());
        }

        // Get Service Operation Policies
        Iterator serviceOperations = axisService.getOperations();
        while (serviceOperations.hasNext()) {
            AxisOperation axisOperation = (AxisOperation) serviceOperations.next();
            String opXPath = PersistenceUtils.getResourcePath(axisOperation);
            if (!getServiceGroupFilePM().elementExists(
                    axisService.getAxisServiceGroup().getServiceGroupName(), opXPath)) {
                continue;
            }

            OMElement operationElement = (OMElement) getServiceGroupFilePM().get(
                    axisService.getAxisServiceGroup().getServiceGroupName(), opXPath);
            //Get the operation policy
            List<PolicyComponent> opPolicyList = new ArrayList<PolicyComponent>(
                    axisOperation.getPolicySubject().getAttachedPolicyComponents());
            Policy operationPolicy = PolicyUtil.getMergedPolicy(opPolicyList, axisOperation);

            if (operationPolicy != null) {
                // Add this policy as a resource to the list
                addPolicyElement(policyElements, operationPolicy, PolicyInclude.AXIS_OPERATION_POLICY);
                // Refer this policy from the operation resource
                OMElement idElement = omFactory.createOMElement(Resources.ServiceProperties.POLICY_UUID, null);
                idElement.setText(operationPolicy.getId());
                operationElement.addChild(idElement);
//                operationElement.addAttribute(Resources.ServiceProperties.POLICY_UUID,
//                        operationPolicy.getId(), null);
            }

            if (!(axisOperation instanceof OutOnlyAxisOperation)) {
                // Get Service Operation Message Policies
                AxisMessage axisInMessage = axisOperation
                        .getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);

                // Get the message in policy
                List<PolicyComponent> messageInPolicyList = new ArrayList<PolicyComponent>(
                        axisInMessage.getPolicySubject().getAttachedPolicyComponents());
                Policy messageInPolicy = PolicyUtil.getMergedPolicy(messageInPolicyList, axisInMessage);

                if (messageInPolicy != null) {
                    // Add this policy as a resource to the list
                    addPolicyElement(policyElements, messageInPolicy, PolicyInclude.AXIS_MESSAGE_POLICY);
                    // Refer this policy from the operation resource
                    operationElement.addAttribute(Resources.ServiceProperties
                            .MESSAGE_IN_POLICY_UUID, messageInPolicy.getId(), null);
                }
            }

            // Get the message out policy
            if (!(axisOperation instanceof InOnlyAxisOperation)) {
                AxisMessage axisOutMessage = axisOperation
                        .getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                List<PolicyComponent> messageOutPolicyList = new ArrayList<PolicyComponent>(
                        axisOutMessage.getPolicySubject().getAttachedPolicyComponents());
                Policy messageOutPolicy = PolicyUtil
                        .getMergedPolicy(messageOutPolicyList, axisOutMessage);

                if (messageOutPolicy != null) {
                    // Add this policy as a resource to the list
                    addPolicyElement(policyElements, messageOutPolicy, PolicyInclude.AXIS_MESSAGE_POLICY);
                    // Refer this policy from the operation resource
                    operationElement.addAttribute(Resources.ServiceProperties
                            .MESSAGE_OUT_POLICY_UUID, messageOutPolicy.getId(), null);
                }
            }

            // Update the operation resource in configRegistry
            getServiceGroupFilePM().put(serviceGroupId, operationElement,
                    PersistenceUtils.getResourcePath(axisService));
        }

        // Get binding policies
        Map endPointMap = axisService.getEndpoints();

        /**
         * We don't have a way of accessing all bindings directly from axis service. Therefore,
         * we have to access those through endpoints. So the same binding can be found again and
         * again. To remove that overhead, we memorize the treated bindings.
         */
        ArrayList<String> bindingsList = new ArrayList<String>();
        for (Object o : endPointMap.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            AxisBinding currentAxisBinding = ((AxisEndpoint) entry.getValue()).getBinding();

            if (bindingsList.contains(currentAxisBinding.getName().getLocalPart())) {
                continue;
            }
            // If we process this binding, add it's name to our list
            bindingsList.add(currentAxisBinding.getName().getLocalPart());

            // Get current binding Policy
            List<PolicyComponent> bindingPolicyList = new ArrayList<PolicyComponent>(
                    currentAxisBinding.getPolicySubject().getAttachedPolicyComponents());
            Policy bindingPolicy = PolicyUtil
                    .getMergedPolicy(bindingPolicyList, currentAxisBinding);

            if (bindingPolicy != null) {
                // Add this policy as a resource to the list
                addPolicyElement(policyElements, bindingPolicy, PolicyInclude.BINDING_POLICY);
                // Refer this policy from the binding resource
                setResourcePolicyId(axisService.getAxisServiceGroup().getServiceGroupName(),
                        PersistenceUtils.getBindingPath(serviceXPath, currentAxisBinding),
                        bindingPolicy.getId());
            }

            // Get Binding Operation Policies
            Iterator operations = currentAxisBinding.getChildren();
            while (operations.hasNext()) {
                AxisBindingOperation currentOperation = (AxisBindingOperation) operations.next();
                String opPath = PersistenceUtils
                        .getBindingOperationPath(serviceXPath, currentOperation);
                if (!getServiceGroupFilePM().elementExists(serviceGroupId, opPath)) {
                    continue;
                }
                OMElement bindingOperationElement = (OMElement) getServiceGroupFilePM().get(serviceGroupId, opPath);

                // Get current binding operation policy
                List<PolicyComponent> boPolicyList = new ArrayList<PolicyComponent>(
                        currentOperation.getPolicySubject().getAttachedPolicyComponents());
                Policy boPolicy = PolicyUtil.getMergedPolicy(boPolicyList, currentOperation);

                if (boPolicy != null) {
                    // Add this policy as a resource to the list
                    addPolicyElement(policyElements,
                            boPolicy, PolicyInclude.BINDING_OPERATION_POLICY);
                    // Refer this policy from the binding operation
                    OMElement idElement = omFactory.createOMElement(Resources.ServiceProperties.POLICY_UUID, null);
                    idElement.setText(boPolicy.getId());
                    bindingOperationElement.addChild(idElement);
//                    bindingOperationElement.addAttribute(Resources
//                            .ServiceProperties.POLICY_UUID, boPolicy.getId(), null);
                }

                // Get Binding Operation Message Policies
                AxisDescription boMessageIn = currentOperation
                        .getChild(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                if (boMessageIn != null) {
                    List<PolicyComponent> boMessageInPolicyList = new ArrayList<PolicyComponent>(
                            boMessageIn.getPolicySubject().getAttachedPolicyComponents());
                    Policy boMessageInPolicy = PolicyUtil
                            .getMergedPolicy(boMessageInPolicyList, boMessageIn);

                    if (boMessageInPolicy != null) {
                        // Add this policy as a resource to the list
                        addPolicyElement(policyElements,
                                boMessageInPolicy, PolicyInclude.BINDING_INPUT_POLICY);
                        // Refer this policy from the binding operation
                        bindingOperationElement.addAttribute(Resources.ServiceProperties
                                .MESSAGE_IN_POLICY_UUID, boMessageInPolicy.getId(), null);
                    }
                }

                // Get binding operaion out policy
                AxisDescription boMessageOut = currentOperation
                        .getChild(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                if (boMessageOut != null) {
                    List<PolicyComponent> boMessageOutPolicyList = new ArrayList<PolicyComponent>(
                            boMessageOut.getPolicySubject().getAttachedPolicyComponents());
                    Policy boMessageOutPolicy = PolicyUtil
                            .getMergedPolicy(boMessageOutPolicyList, boMessageOut);

                    if (boMessageOutPolicy != null) {
                        // Add this policy as a resource to the list
                        addPolicyElement(policyElements,
                                boMessageOutPolicy, PolicyInclude.BINDING_OUTPUT_POLICY);
                        // Refer this policy from the binding operation
                        bindingOperationElement.addAttribute(Resources.ServiceProperties
                                .MESSAGE_OUT_POLICY_UUID, boMessageOutPolicy.getId(), null);
                    }
                }

                // Update binding operation resource in configRegistry
                getServiceGroupFilePM().put(serviceGroupId, bindingOperationElement,
                        PersistenceUtils.getBindingPath(serviceXPath, currentAxisBinding));
            }
        }
        return policyElements;
    }

    /**
     * Sets the policy Id for a resource of a service, operation, binding etc..
     *
     * @param serviceGroupId - The service group id string
     * @param xpathStr       - xpath to policy element
     * @param policyId       - policy Id to be set
     * @throws PersistenceException - error on updating resource
     */
    private void setResourcePolicyId(String serviceGroupId, String xpathStr,
                                     String policyId) throws PersistenceException {
        if (getServiceGroupFilePM().elementExists(serviceGroupId, xpathStr)) {
            OMElement idElement = omFactory.createOMElement(Resources.ServiceProperties.POLICY_UUID, null);
            idElement.setText(policyId);
            ((OMElement) getServiceGroupFilePM().
                    get(serviceGroupId, xpathStr)).
                    addChild(idElement);
        }
    }

    /**
     * Add the given Policy as a resource to the given policy list if it doesn't already exist
     * This logic is somewhat different from ModulePersistenceManager#handleNewModuleAddition
     *
     * @param policyElements - list of policy resource
     * @param policy         - Policy instance
     * @param policyType     - policy type
     * @throws Exception - on creating policy resource
     * @see ModulePersistenceManager#handleNewModuleAddition(org.apache.axis2.description.AxisModule, String, String)
     */
    private void addPolicyElement(List<OMElement> policyElements,
                                  Policy policy, int policyType) throws Exception {
        OMElement policyWrapperElement = omFactory.createOMElement(Resources.POLICY, null);
        policyWrapperElement.addAttribute(Resources.ServiceProperties.POLICY_TYPE, "" + policyType, null);
        //we don't need the version?
//        policyWrapperElement.addAttribute(Resources.VERSION, version, null);

        if (policy.getId() == null) {
            // Generate an ID
            policy.setId(UUIDGenerator.getUUID());

            OMElement idElement = omFactory.createOMElement(Resources.ServiceProperties.POLICY_UUID, null);
            idElement.setText("" + policy.getId());
            policyWrapperElement.addChild(idElement);
            OMElement policyElement = PersistenceUtils.createPolicyElement(policy);
            policyWrapperElement.addChild(policyElement);
            policyElements.add(policyWrapperElement);
        } else if (PersistenceUtils.getPolicyElementFromList(policy.getId(),
                policyElements) == null) {
            OMElement policyElement = PersistenceUtils.createPolicyElement(policy);

            OMElement idElement = omFactory.createOMElement(Resources.ServiceProperties.POLICY_UUID, null);
            idElement.setText("" + policy.getId());
            policyWrapperElement.addChild(idElement);

            policyWrapperElement.addChild(policyElement);
            policyElements.add(policyWrapperElement);
        }
    }

    /**
     * @param serviceGroupId
     * @param policy
     * @param policyUuid
     * @param policyType
     * @param servicePath
     * @param engagementPath The xpath to where the policy is applied. This could be serviceXPath, or operationPath etc.
     * @throws Exception
     */
    public void persistServicePolicy(String serviceGroupId, Policy policy, String policyUuid,
                                     String policyType, String servicePath, String engagementPath) throws Exception {
        if (engagementPath == null) {
            engagementPath = servicePath;
        }
        boolean transactionStarted = getServiceGroupFilePM().isTransactionStarted(serviceGroupId);
        if (!transactionStarted) {
            getServiceGroupFilePM().beginTransaction(serviceGroupId);
        }
        OMFactory omFactory = OMAbstractFactory.getOMFactory();
        OMElement policyWrapperElement = omFactory.createOMElement(Resources.POLICY, null);
        policyWrapperElement.addAttribute(Resources.ServiceProperties.POLICY_TYPE, policyType, null);

        OMElement idElement = omFactory.createOMElement(Resources.ServiceProperties.POLICY_UUID, null);
        idElement.setText("" + policyUuid);
        policyWrapperElement.addChild(idElement);

        OMElement policyElementToPersist = PersistenceUtils.createPolicyElement(policy);
        policyWrapperElement.addChild(policyElementToPersist);

        if (!getServiceGroupFilePM().elementExists(serviceGroupId, servicePath + "/" + Resources.POLICIES)) {
            getServiceGroupFilePM().put(serviceGroupId,
                    omFactory.createOMElement(Resources.POLICIES, null), serviceGroupId);
        } else {
            //you must manually delete the existing policy before adding new one.
            String pathToPolicy = servicePath + "/" + Resources.POLICIES +
                    "/" + Resources.POLICY +
                    PersistenceUtils.getXPathTextPredicate(
                            Resources.ServiceProperties.POLICY_UUID, policyUuid);
            if (getServiceGroupFilePM().elementExists(serviceGroupId, pathToPolicy)) {
                getServiceGroupFilePM().delete(serviceGroupId, pathToPolicy);
            }
        }
        getServiceGroupFilePM().put(serviceGroupId, policyWrapperElement, servicePath +
                "/" + Resources.POLICIES);

        if (!getServiceGroupFilePM().elementExists(serviceGroupId, engagementPath +
                PersistenceUtils.getXPathTextPredicate(
                        Resources.ServiceProperties.POLICY_UUID, policy.getId()))) {
            getServiceGroupFilePM().put(serviceGroupId, idElement.cloneOMElement(), engagementPath);
        }

        if (!transactionStarted) {
            getServiceGroupFilePM().commitTransaction(serviceGroupId);
        }
        if (log.isDebugEnabled()) {
            log.debug("Policy is saved in the file system for " + servicePath + policyUuid);
        }
    }
}
