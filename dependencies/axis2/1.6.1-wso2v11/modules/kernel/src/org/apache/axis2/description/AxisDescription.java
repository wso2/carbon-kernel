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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMText;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisEvent;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.modules.Module;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.Utils;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.wsdl.WSDLUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Assertion;
import org.apache.neethi.Policy;

import javax.xml.stream.XMLStreamException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AxisDescription implements ParameterInclude, DescriptionConstants {

    protected AxisDescription parent = null;

    private ParameterInclude parameterInclude;

    private PolicyInclude policyInclude = null;

    private PolicySubject policySubject = null;

    private Map<Object, AxisDescription> children;

    /**
     * Map of modules engaged on this object. The key is the archive name as defined by
     * {@link AxisModule#getArchiveName()}.
     */
    protected Map<String, AxisModule> engagedModules;

    /** List of ParameterObservers who want to be notified of changes */
    protected List<ParameterObserver> parameterObservers = null;

    private OMFactory omFactory = OMAbstractFactory.getOMFactory();

    // Holds the documentation details for each element
    private OMNode documentation;

    // creating a logger instance
    private static Log log = LogFactory.getLog(AxisDescription.class);

    public AxisDescription() {
        parameterInclude = new ParameterIncludeImpl();
        children = new ConcurrentHashMap<Object, AxisDescription>();
        policySubject = new PolicySubject();
    }

    public void addParameterObserver(ParameterObserver observer) {
        if (parameterObservers == null)
            parameterObservers = new ArrayList<ParameterObserver>();
        parameterObservers.add(observer);
    }

    public void removeParameterObserver(ParameterObserver observer) {
        if (parameterObservers != null) {
            parameterObservers.remove(observer);
        }
    }

    public void addParameter(Parameter param) throws AxisFault {
        if (param == null) {
            return;
        }

        if (isParameterLocked(param.getName())) {
            throw new AxisFault(Messages.getMessage("paramterlockedbyparent",
                                                    param.getName()));
        }

        parameterInclude.addParameter(param);

        // Tell anyone who wants to know
        if (parameterObservers != null) {
            for (Object parameterObserver : parameterObservers) {
                ParameterObserver observer = (ParameterObserver)parameterObserver;
                observer.parameterChanged(param.getName(), param.getValue());
            }
        }
    }

    public void addParameter(String name, Object value) throws AxisFault {
        addParameter(new Parameter(name, value));
    }

    public void removeParameter(Parameter param) throws AxisFault {
        parameterInclude.removeParameter(param);
    }

    public void deserializeParameters(OMElement parameterElement)
            throws AxisFault {

        parameterInclude.deserializeParameters(parameterElement);

    }

    /**
     * If the parameter is found in the current description then the Parameter will be writable else
     * it will be read only
     *
     * @param name name of Parameter to retrieve
     * @return the Parameter, if found anywhere in the stack, or null if not
     */
    public Parameter getParameter(String name) {
        Parameter parameter = parameterInclude.getParameter(name);
        if (parameter != null) {
            parameter.setEditable(true);
            return parameter;
        }
        if (parent != null) {
            parameter = parent.getParameter(name);
            if (parameter != null) {
                parameter.setEditable(false);
            }
            return parameter;
        }
        return null;
    }

    public Object getParameterValue(String name) {
        Parameter param = getParameter(name);
        if (param == null) {
            return null;
        }
        return param.getValue();
    }

    public boolean isParameterTrue(String name) {
        Parameter param = getParameter(name);
        return param != null && JavaUtils.isTrue(param.getValue());
    }

    public ArrayList<Parameter> getParameters() {
        return parameterInclude.getParameters();
    }

    public boolean isParameterLocked(String parameterName) {

        if (this.parent != null && this.parent.isParameterLocked(parameterName)) {
            return true;
        }

        Parameter parameter = getParameter(parameterName);
        return parameter != null && parameter.isLocked();
    }

    public String getDocumentation() {
        if (documentation != null) {
            if (documentation instanceof OMText) {
                return ((OMText)documentation).getText();
            } else if (documentation instanceof OMElement) {
                StringWriter writer = new StringWriter();
                documentation.build();
                try {
                    ((OMElement)documentation).serialize(writer);
                } catch (XMLStreamException e) {
                    log.error(e);
                }
                writer.flush();
                return writer.toString();
            }
        }
        return null;
    }

    public OMNode getDocumentationNode() {
        return documentation;
    }

    public void setDocumentation(OMNode documentation) {
        this.documentation = documentation;
    }

    public void setDocumentation(String documentation) {
        if (!"".equals(documentation)) {
            this.documentation = omFactory.createOMText(documentation);
        }
    }

    public void setParent(AxisDescription parent) {
        this.parent = parent;
    }

    public AxisDescription getParent() {
        return parent;
    }

    /**
     * @param policyInclude PolicyInclude value
     * @see org.apache.axis2.description.AxisDescription#setPolicyInclude(PolicyInclude)
     * @deprecated As of release 1.4, if you want to access the policy cache of a particular
     *             AxisDescription object use {@link #getPolicySubject()} instead.
     */
    public void setPolicyInclude(PolicyInclude policyInclude) {
        this.policyInclude = policyInclude;
    }


    /**
     * @return the active PolicyInclue
     * @see org.apache.axis2.description.AxisDescription#getPolicySubject()
     * @deprecated As of release 1.4, replaced by {@link #getPolicySubject()}
     */
    public PolicyInclude getPolicyInclude() {
        if (policyInclude == null) {
            policyInclude = new PolicyInclude(this);
        }
        return policyInclude;
    }


    // NOTE - These are NOT typesafe!
    public void addChild(AxisDescription child) {
        if (child.getKey() == null) {
            // FIXME: Several classes that extend AxisDescription pass null in their getKey method.
//            throw new IllegalArgumentException("Please specify a key in the child");
        } else {
            children.put(child.getKey(), child);
        }
    }


    public void addChild(Object key, AxisDescription child) {
        children.put(key, child);
    }

    public Iterator<? extends AxisDescription> getChildren() {
        return children.values().iterator();
    }

    public AxisDescription getChild(Object key) {
        if (key == null) {
            // FIXME: Why are folks sending in null?
            return null;
        }
        return (AxisDescription)children.get(key);
    }

    public void removeChild(Object key) {
        children.remove(key);
    }

    /**
     * This method sets the policy as the default of this AxisDescription instance. Further more
     * this method does the followings. <p/> (1) Engage whatever modules necessary to execute new
     * the effective policy of this AxisDescription instance. (2) Disengage whatever modules that
     * are not necessary to execute the new effective policy of this AxisDescription instance. (3)
     * Check whether each module can execute the new effective policy of this AxisDescription
     * instance. (4) If not throw an AxisFault to notify the user. (5) Else notify each module about
     * the new effective policy.
     *
     * @param policy the new policy of this AxisDescription instance. The effective policy is the
     *               merge of this argument with effective policy of parent of this
     *               AxisDescription.
     * @throws AxisFault if any module is unable to execute the effective policy of this
     *                   AxisDescription instance successfully or no module to execute some portion
     *                   (one or more PrimtiveAssertions ) of that effective policy.
     */
    public void applyPolicy(Policy policy) throws AxisFault {
        // sets AxisDescription policy
        getPolicySubject().clear();
        getPolicySubject().attachPolicy(policy);

        /*
           * now we try to engage appropriate modules based on the merged policy
           * of axis description object and the corresponding axis binding
           * description object.
           */
        applyPolicy();
    }

    /**
     * Applies the policies on the Description Hierarchy recursively.
     *
     * @throws AxisFault an error occurred applying the policy
     */
    public void applyPolicy() throws AxisFault {
        AxisConfiguration configuration = getAxisConfiguration();
        if (configuration == null) {
            return;
        }

        Policy applicablePolicy = getApplicablePolicy(this);
        if (applicablePolicy != null) {
            engageModulesForPolicy(applicablePolicy, configuration);
        }

        for (Iterator<? extends AxisDescription> children = getChildren(); children.hasNext();) {
            AxisDescription child = children.next();
            child.applyPolicy();
        }
    }

    private boolean canSupportAssertion(Assertion assertion, List<AxisModule> moduleList) {

        Module module;

        for (AxisModule axisModule : moduleList) {
            // FIXME is this step really needed ??
            // Shouldn't axisMoudle.getModule always return not-null value ??
            module = axisModule.getModule();

            if (!(module == null || module.canSupportAssertion(assertion))) {
                log.debug(axisModule.getName() + " says it can't support " + assertion.getName());
                return false;
            }
        }

        return true;
    }

    private void engageModulesForPolicy(Policy policy, AxisConfiguration axisConfiguration)
            throws AxisFault {
        /*
           * for the moment we consider policies with only one alternative. If the
           * policy contains multiple alternatives only the first alternative will
           * be considered.
           */
        Iterator iterator = policy.getAlternatives();
        if (!iterator.hasNext()) {
            throw new AxisFault("Policy doesn't contain any policy alternatives");
        }

        List assertionList = (List)iterator.next();

        Assertion assertion;
        String namespaceURI;

        List moduleList;

        List namespaceList = new ArrayList();
        List modulesToEngage = new ArrayList();

        for (Object anAssertionList : assertionList) {
            assertion = (Assertion)anAssertionList;
            namespaceURI = assertion.getName().getNamespaceURI();

            moduleList = axisConfiguration.getModulesForPolicyNamesapce(namespaceURI);

            if (moduleList == null) {
                log.debug("can't find any module to process " + assertion.getName() +
                          " type assertions");
                continue;
            }

            if (!canSupportAssertion(assertion, moduleList)) {
                throw new AxisFault("atleast one module can't support " + assertion.getName());
            }

            if (!namespaceList.contains(namespaceURI)) {
                namespaceList.add(namespaceURI);
                modulesToEngage.addAll(moduleList);
            }
        }
        engageModulesToAxisDescription(modulesToEngage, this);
    }

    private void engageModulesToAxisDescription(List<AxisModule> moduleList, AxisDescription description)
            throws AxisFault {

        AxisModule axisModule;
        Module module;

        for (Object aModuleList : moduleList) {
            axisModule = (AxisModule)aModuleList;
            // FIXME is this step really needed ??
            // Shouldn't axisMoudle.getModule always return not-null value ??
            module = axisModule.getModule();

            if (!(module == null || description.isEngaged(axisModule.getName()))) {
                // engages the module to AxisDescription
                description.engageModule(axisModule);
                // notifies the module about the engagement
                axisModule.getModule().engageNotify(description);
            }
        }
    }

    public AxisConfiguration getAxisConfiguration() {

        if (this instanceof AxisConfiguration) {
            return (AxisConfiguration)this;
        }

        if (this.parent != null) {
            return this.parent.getAxisConfiguration();
        }

        return null;
    }

    public abstract Object getKey();


    /**
     * Engage a Module at this level
     *
     * @param axisModule the Module to engage
     * @throws AxisFault if there's a problem engaging
     */
    public void engageModule(AxisModule axisModule) throws AxisFault {
        engageModule(axisModule, this);
        AxisConfiguration config = getAxisConfiguration();
        config.notifyObservers(new AxisEvent(AxisEvent.MODULE_ENGAGED , this) , axisModule);
    }

    /**
     * Engage a Module at this level, keeping track of which level the engage was originally called
     * from.  This is meant for internal use only.
     *
     * @param axisModule module to engage
     * @param source     the AxisDescription which originally called engageModule()
     * @throws AxisFault if there's a problem engaging
     */
    public void engageModule(AxisModule axisModule, AxisDescription source) throws AxisFault {
        if (engagedModules == null) engagedModules = new ConcurrentHashMap<String, AxisModule>();
        String moduleName = axisModule.getName();
        for (Object o : engagedModules.values()) {
            AxisModule tempAxisModule = ((AxisModule)o);
            String tempModuleName = tempAxisModule.getName();

            if (moduleName.equals(tempModuleName)) {
                Version existing = tempAxisModule.getVersion();
                if (!Utils.checkVersion(axisModule.getVersion(), existing)) {
                    throw new AxisFault(Messages.getMessage("mismatchedModuleVersions",
                                                            getClass().getName(),
                                                            moduleName,
                                                            String.valueOf(existing)));
                }
            }

        }

        // Let the Module know it's being engaged.  If it's not happy about it, it can throw.
        Module module = axisModule.getModule();
        if (module != null) {
            module.engageNotify(this);
        }

        // If we have anything specific to do, let that happen
        onEngage(axisModule, source);

        engagedModules.put(axisModule.getArchiveName(), axisModule);
    }

    protected void onEngage(AxisModule module, AxisDescription engager)
            throws AxisFault {
        // Default version does nothing, feel free to override
    }

    static Collection<AxisModule> NULL_MODULES = new ArrayList<AxisModule>(0);

    public Collection<AxisModule> getEngagedModules() {
        return engagedModules == null ? NULL_MODULES : engagedModules.values();
    }

    /**
     * Check if a given module is engaged at this level.
     *
     * @param moduleName module to investigate.
     * @return true if engaged, false if not. TODO: Handle versions? isEngaged("addressing") should
     *         be true even for versioned modulename...
     */
    public boolean isEngaged(String moduleName) {
        return engagedModules != null
               && engagedModules.keySet().contains(moduleName);
    }

    public boolean isEngaged(AxisModule axisModule) {
        String id = axisModule.getArchiveName();
        return engagedModules != null && engagedModules.keySet().contains(id);
    }

    public void disengageModule(AxisModule module) throws AxisFault {
        if (module == null || engagedModules == null)
            return;
        // String id = Utils.getModuleName(module.getName(),
        // module.getVersion());
        if (isEngaged(module)) {
            onDisengage(module);
            engagedModules.remove(module.getArchiveName());
            /**
             * if a Disengaged module belogs to an AxisService or an Operation
             * notify with a serviceUpdate
             */
            getAxisConfiguration().notifyObservers(new AxisEvent(AxisEvent.MODULE_DISENGAGED, this), module);
        }
    }

    protected void onDisengage(AxisModule module) throws AxisFault {
        // Base version does nothing
    }

    private Policy getApplicablePolicy(AxisDescription axisDescription) {
        if (axisDescription instanceof AxisMessage) {
            AxisMessage axisMessage = (AxisMessage)axisDescription;
            AxisOperation axisOperation = axisMessage.getAxisOperation();
            if (axisOperation != null) {
                AxisService axisService = axisOperation.getAxisService();
                if (axisService != null) {
                    if (axisService.getEndpointName() != null) {
                        AxisEndpoint axisEndpoint =
                                axisService.getEndpoint(axisService.getEndpointName());
                        if (axisEndpoint != null) {
                            AxisBinding axisBinding = axisEndpoint.getBinding();
                            AxisBindingOperation axisBindingOperation =
                                    (AxisBindingOperation)axisBinding
                                            .getChild(axisOperation.getName());
                            String direction = axisMessage.getDirection();
                            AxisBindingMessage axisBindingMessage;
                            if (WSDLConstants.WSDL_MESSAGE_DIRECTION_IN.equals(direction)
                                && WSDLUtil
                                    .isInputPresentForMEP(axisOperation
                                            .getMessageExchangePattern())) {
                                axisBindingMessage = (AxisBindingMessage)axisBindingOperation
                                        .getChild(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                                return axisBindingMessage.getEffectivePolicy();

                            } else if (WSDLConstants.WSDL_MESSAGE_DIRECTION_OUT
                                    .equals(direction)
                                       && WSDLUtil
                                    .isOutputPresentForMEP(axisOperation
                                            .getMessageExchangePattern())) {
                                axisBindingMessage = (AxisBindingMessage)axisBindingOperation
                                        .getChild(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                                return axisBindingMessage.getEffectivePolicy();
                            }
                        }

                    }
                }
            }
            return ((AxisMessage)axisDescription).getEffectivePolicy();
        }
        return null;
    }

    public PolicySubject getPolicySubject() {
        return policySubject;
    }

    

}
