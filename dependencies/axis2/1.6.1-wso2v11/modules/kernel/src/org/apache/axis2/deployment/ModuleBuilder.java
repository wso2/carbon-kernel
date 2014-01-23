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


package org.apache.axis2.deployment;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.deployment.util.PhasesInfo;
import org.apache.axis2.description.*;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.Deployable;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.modules.Module;
import org.apache.axis2.phaseresolver.PhaseMetadata;
import org.apache.axis2.util.Loader;
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.io.StringWriter;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Builds a module description from OM
 */
public class ModuleBuilder extends DescriptionBuilder {
    private static final Log log = LogFactory.getLog(ModuleBuilder.class);
    private AxisModule module;

    public ModuleBuilder(InputStream serviceInputStream, AxisModule module,
                         AxisConfiguration axisConfig) {
        super(serviceInputStream, axisConfig);
        this.module = module;
    }

    private void loadModuleClass(AxisModule module, String moduleClassName)
            throws DeploymentException {
        Class moduleClass;

        try {
            if ((moduleClassName != null) && !"".equals(moduleClassName)) {
                moduleClass = Loader.loadClass(module.getModuleClassLoader(), moduleClassName);
                final Class fmoduleClass = moduleClass;
                final AxisModule fmodule = module;
                try {
                    AccessController.doPrivileged(new PrivilegedExceptionAction() {
                        public Object run() throws IllegalAccessException, InstantiationException {
                            Module new_module = (Module) fmoduleClass.newInstance();
                            fmodule.setModule(new_module);
                            return null;
                        }
                    });
                } catch (PrivilegedActionException e) {
                    throw e.getException();
                }
            }
        } catch (Exception e) {
            throw new DeploymentException(e.getMessage(), e);
        }
    }

    /**
     * Fill in the AxisModule I'm holding from the module.xml configuration.
     *
     * @throws DeploymentException if there's a problem with the module.xml
     */
    public void populateModule() throws DeploymentException {
        try {
            OMElement moduleElement = buildOM();
            // Setting Module Class , if it is there
            OMAttribute moduleClassAtt = moduleElement.getAttribute(new QName(TAG_CLASS_NAME));
            // processing Parameters
            // Processing service level parameters
            Iterator itr = moduleElement.getChildrenWithName(new QName(TAG_PARAMETER));

            processParameters(itr, module, module.getParent());

            Parameter childFirstClassLoading =
                    module.getParameter(Constants.Configuration.ENABLE_CHILD_FIRST_CLASS_LOADING);
            if (childFirstClassLoading != null){
                DeploymentClassLoader deploymentClassLoader = (DeploymentClassLoader) module.getModuleClassLoader();
                if (JavaUtils.isTrueExplicitly(childFirstClassLoading.getValue())){
                    deploymentClassLoader.setChildFirstClassLoading(true);
                } else if (JavaUtils.isFalseExplicitly(childFirstClassLoading.getValue())){
                    deploymentClassLoader.setChildFirstClassLoading(false);
                }
            }

            if (moduleClassAtt != null) {
                String moduleClass = moduleClassAtt.getAttributeValue();

                if ((moduleClass != null) && !"".equals(moduleClass)) {
                    loadModuleClass(module, moduleClass);
                }
            }

            // Get our name and version.  If this is NOT present, we'll try to figure it out
            // from the file name (e.g. "addressing-1.0.mar").  If the attribute is there, we
            // always respect it.
            //TODO: Need to check whether ths name is getting overridden by the file name of the MAR
            OMAttribute nameAtt = moduleElement.getAttribute(new QName("name"));
            if (nameAtt != null) {
                String moduleName = nameAtt.getAttributeValue();
                if (moduleName != null && !"".equals(moduleName)){
                    module.setName(moduleName);
                }
            }
            
            if (log.isDebugEnabled()) {
              log.debug("populateModule: Building module description for: "
                        + module.getName());
            }
                        
            // Process service description
            OMElement descriptionElement =
                    moduleElement.getFirstChildWithName(new QName(TAG_DESCRIPTION));

            if (descriptionElement != null) {
                OMElement descriptionValue = descriptionElement.getFirstElement();

                if (descriptionValue != null) {
                    StringWriter writer = new StringWriter();

                    descriptionValue.build();
                    descriptionValue.serialize(writer);
                    writer.flush();
                    module.setModuleDescription(writer.toString());
                } else {
                    module.setModuleDescription(descriptionElement.getText());
                }
            } else {
                module.setModuleDescription("module description not found");
            }

            // Processing Dynamic Phase
            Iterator phaseItr = moduleElement.getChildrenWithName(new QName(TAG_PHASE));
            processModulePhase(phaseItr);
            
            // setting the PolicyInclude

            // processing <wsp:Policy> .. </..> elements
            Iterator policyElements =
                    moduleElement.getChildrenWithName(new QName(POLICY_NS_URI, TAG_POLICY));

            if (policyElements != null && policyElements.hasNext()) {
                processPolicyElements(policyElements, module.getPolicySubject());
            }

            // processing <wsp:PolicyReference> .. </..> elements
            Iterator policyRefElements =
                    moduleElement.getChildrenWithName(new QName(POLICY_NS_URI, TAG_POLICY_REF));

            if (policyRefElements != null && policyRefElements.hasNext()) {
                processPolicyRefElements(policyRefElements, module.getPolicySubject());
            }

            // process flows (case-insensitive)
            
            Iterator flows = moduleElement.getChildElements();
            while (flows.hasNext()) {
                OMElement flowElement = (OMElement)flows.next();
                final String flowName = flowElement.getLocalName();
                if (flowName.compareToIgnoreCase(TAG_FLOW_IN) == 0) {
                    module.setInFlow(processFlow(flowElement, module));
                } else if (flowName.compareToIgnoreCase(TAG_FLOW_OUT) == 0) {
                    module.setOutFlow(processFlow(flowElement, module));
                } else if (flowName.compareToIgnoreCase(TAG_FLOW_IN_FAULT) == 0) {
                    module.setFaultInFlow(processFlow(flowElement, module));
                } else if (flowName.compareToIgnoreCase(TAG_FLOW_OUT_FAULT) == 0) {
                    module.setFaultOutFlow(processFlow(flowElement, module));
                }
            }

            OMElement supportedPolicyNamespaces =
                    moduleElement.getFirstChildWithName(new QName(TAG_SUPPORTED_POLICY_NAMESPACES));
            if (supportedPolicyNamespaces != null) {
                module.setSupportedPolicyNamespaces(
                        processSupportedPolicyNamespaces(supportedPolicyNamespaces));
            }

            /*
            * Module description should contain a list of QName of the assertions that are local to the system.
            * These assertions are not exposed to the outside.
            */
            OMElement localPolicyAssertionElement =
                    moduleElement.getFirstChildWithName(new QName("local-policy-assertions"));
            if (localPolicyAssertionElement != null) {
                module.setLocalPolicyAssertions(
                        getLocalPolicyAssertionNames(localPolicyAssertionElement));
            }

            // processing Operations
            Iterator op_itr = moduleElement.getChildrenWithName(new QName(TAG_OPERATION));
            ArrayList<AxisOperation> operations = processOperations(op_itr);

            for (AxisOperation op : operations) {
                module.addOperation(op);
            }

            if (log.isDebugEnabled()) {
              log.debug("populateModule: Done building module description");
            }
                        
        } catch (XMLStreamException e) {
            throw new DeploymentException(e);
        } catch(AxisFault e) {
            throw new DeploymentException(e);
        }
    }

    private ArrayList<AxisOperation> processOperations(Iterator operationsIterator)
            throws DeploymentException {
        ArrayList operations = new ArrayList();

        while (operationsIterator.hasNext()) {
            OMElement operation = (OMElement) operationsIterator.next();
            AxisOperation op_descrip;

            //getting operation name
            String opname = operation.getAttributeValue(new QName(ATTRIBUTE_NAME));

            if (opname == null) {
                throw new DeploymentException(
                        Messages.getMessage(
                                Messages.getMessage(
                                        DeploymentErrorMsgs.INVALID_OP, "operation name missing")));
            }

            String mepURL = operation.getAttributeValue(new QName(TAG_MEP));

            if (mepURL == null) {
                op_descrip = new InOnlyAxisOperation();
            } else {
                try {
                    op_descrip = AxisOperationFactory.getOperationDescription(mepURL);
                } catch (AxisFault axisFault) {
                    throw new DeploymentException(
                            Messages.getMessage(
                                    Messages.getMessage(
                                            DeploymentErrorMsgs.OPERATION_PROCESS_ERROR,
                                            axisFault.getMessage())));
                }
            }

            op_descrip.setName(new QName(opname));

            //Check for the allowOverride attribute
            OMAttribute op_allowOverride_att = operation.getAttribute(new QName(TAG_ALLOWOVERRIDE));
            if (op_allowOverride_att != null) {
              try {
                op_descrip.addParameter(TAG_ALLOWOVERRIDE, op_allowOverride_att.getAttributeValue());
              } catch (AxisFault axisFault) {
                throw new DeploymentException(
                            Messages.getMessage(
                                    Messages.getMessage(
                                            DeploymentErrorMsgs.PARAMETER_LOCKED,
                                            axisFault.getMessage())));
                      
              }
              if (log.isDebugEnabled()) {
                log.debug("processOperations: allowOverride set to "
                          + op_allowOverride_att.getAttributeValue()
                          + " for operation: "+opname);
              }
            }
            
            // Operation Parameters
            Iterator parameters = operation.getChildrenWithName(new QName(TAG_PARAMETER));
            processParameters(parameters, op_descrip, module);

            //To process wsamapping;
            processActionMappings(operation, op_descrip);

            // setting the MEP of the operation
            // loading the message receivers
            OMElement receiverElement =
                    operation.getFirstChildWithName(new QName(TAG_MESSAGE_RECEIVER));

            if (receiverElement != null) {
                MessageReceiver messageReceiver =
                        loadMessageReceiver(module.getModuleClassLoader(), receiverElement);
                op_descrip.setMessageReceiver(messageReceiver);
            } else {
                // setting default message receiver
                MessageReceiver msgReceiver = loadDefaultMessageReceiver(mepURL, null);
                op_descrip.setMessageReceiver(msgReceiver);
            }

            // Process Module Refs
            Iterator modules = operation.getChildrenWithName(new QName(TAG_MODULE));
            processOperationModuleRefs(modules, op_descrip);
            
//          processing <wsp:Policy> .. </..> elements
            Iterator policyElements =
                    operation.getChildrenWithName(new QName(POLICY_NS_URI, TAG_POLICY));

            if (policyElements != null && policyElements.hasNext()) {
                processPolicyElements(policyElements, op_descrip.getPolicySubject());
            }

            // processing <wsp:PolicyReference> .. </..> elements
            Iterator policyRefElements =
                    operation.getChildrenWithName(new QName(POLICY_NS_URI, TAG_POLICY_REF));

            if (policyRefElements != null && policyRefElements.hasNext()) {
                processPolicyRefElements(policyRefElements, module.getPolicySubject());
            }

            // setting Operation phase
            PhasesInfo info = axisConfig.getPhasesInfo();
            try {
                info.setOperationPhases(op_descrip);
            } catch (AxisFault axisFault) {
                throw new DeploymentException(axisFault);
            }
            

            // adding the operation
            operations.add(op_descrip);
        }

        return operations;
    }

    /**
     * This will process the phase list and then add the specified phases to
     * our AxisConfiguration.  The format of a phase element looks like this:
     *
     *  &lt;phase name="Foo" after="After_phase_Name" before="Before_Phase_Name"
     *  flow="[InFlow,OutFlow,OutFaultFlow,InFaultFlow]"/&gt;
     *
     *  Here bef
     *
     * @param phases : OMElement iterator
     * @throws AxisFault : If something went wrong
     */
    private void processModulePhase(Iterator phases) throws AxisFault {
        if (phases == null){
            return;
        }

        while (phases.hasNext()) {
            OMElement element = (OMElement) phases.next();
            String phaseName = element.getAttributeValue(new QName(ATTRIBUTE_NAME));

            Deployable d = new Deployable(phaseName);
            String after = element.getAttributeValue(new QName(TAG_AFTER));
            if (after != null) {
                String [] afters = after.split(",");
                for (String s : afters) {
                    d.addPredecessor(s);
                }
            }
            String before = element.getAttributeValue(new QName(TAG_BEFORE));
            if (before != null) {
                String [] befores = before.split(",");
                for (String s : befores) {
                    d.addSuccessor(s);
                }
            }
            String flowName = element.getAttributeValue(new QName("flow"));
            if (flowName == null) {
                throw new DeploymentException("Flow can not be null for the phase name " +
                                              phaseName);
            }
            String[] flows = flowName.split(",");
            for (String flow : flows) {
                int flowIndex;
                if (TAG_FLOW_IN.equalsIgnoreCase(flow)) {
                    flowIndex = PhaseMetadata.IN_FLOW;
                } else if (TAG_FLOW_OUT.equalsIgnoreCase(flow)) {
                    flowIndex = PhaseMetadata.OUT_FLOW;
                } else if (TAG_FLOW_OUT_FAULT.equalsIgnoreCase(flow)) {
                    flowIndex = PhaseMetadata.FAULT_OUT_FLOW;
                } else if (TAG_FLOW_IN_FAULT.equalsIgnoreCase(flow)) {
                    flowIndex = PhaseMetadata.FAULT_IN_FLOW;
                } else {
                    throw new DeploymentException("Unknown flow name '" + flow + "'");
                }
                axisConfig.insertPhase(d, flowIndex);
            }
        }
    }
}
