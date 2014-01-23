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
import org.apache.axis2.builder.Builder;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Flow;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.ParameterInclude;
import org.apache.axis2.description.PhaseRule;
import org.apache.axis2.description.PolicyInclude;
import org.apache.axis2.description.PolicySubject;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.util.Loader;
import org.apache.axis2.util.XMLUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.apache.neethi.PolicyReference;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * This class does the common tasks for all *Builder class.
 */
public class DescriptionBuilder implements DeploymentConstants {

    private static final Log log = LogFactory.getLog(DescriptionBuilder.class);

    protected ConfigurationContext configCtx;

    protected AxisConfiguration axisConfig;

    protected InputStream descriptionStream;

    public DescriptionBuilder() {
    }

    public DescriptionBuilder(InputStream serviceInputStream,
                              ConfigurationContext configCtx) {
        this.configCtx = configCtx;
        this.descriptionStream = serviceInputStream;
        this.axisConfig = this.configCtx.getAxisConfiguration();
    }

    public DescriptionBuilder(InputStream serviceInputStream,
                              AxisConfiguration axisConfig) {
        this.descriptionStream = serviceInputStream;
        this.axisConfig = axisConfig;
    }

    /**
     * Creates OMElement for a given description document (axis2.xml ,
     * services.xml and module.xml).
     *
     * @return Returns <code>OMElement</code> .
     * @throws javax.xml.stream.XMLStreamException
     *
     */
    public OMElement buildOM() throws XMLStreamException {
        OMElement element = (OMElement) XMLUtils.toOM(descriptionStream);
        element.build();
        return element;
    }

    /**
     * Loads default message receivers. First searches in Axiservice for the
     * given mepURL, if not found searches in AxisConfiguration with the given
     * mepURL.
     *
     * @param mepURL  :
     *                can be null
     * @param service :
     *                This can be null <code>AxisService</code>
     */
    protected MessageReceiver loadDefaultMessageReceiver(String mepURL, AxisService service) {
        MessageReceiver messageReceiver;
        if (mepURL == null) {
            mepURL = WSDL2Constants.MEP_URI_IN_OUT;
        }
        if (service != null) {
            messageReceiver = service.getMessageReceiver(mepURL);
            if (messageReceiver != null) {
                return messageReceiver;
            }
        }
        return axisConfig.getMessageReceiver(mepURL);
    }

    /**
     * Processes default message receivers specified either in axis2.xml or
     * services.xml.
     *
     */
    protected HashMap<String,MessageReceiver> processMessageReceivers(OMElement messageReceivers)
            throws DeploymentException {
        HashMap<String,MessageReceiver> mr_mep = new HashMap<String,MessageReceiver>();
        Iterator msgReceivers = messageReceivers.getChildrenWithName(new QName(
                TAG_MESSAGE_RECEIVER));
        while (msgReceivers.hasNext()) {
            OMElement msgReceiver = (OMElement) msgReceivers.next();
            final OMElement tempMsgReceiver = msgReceiver;
            MessageReceiver receiver = null;
            try {
                receiver = org.apache.axis2.java.security.AccessController
                        .doPrivileged(new PrivilegedExceptionAction<MessageReceiver>() {
                            public MessageReceiver run()
                                    throws org.apache.axis2.deployment.DeploymentException {
                                return loadMessageReceiver(
                                        Thread.currentThread().getContextClassLoader(),
                                        tempMsgReceiver);
                            }
                        });
            } catch (PrivilegedActionException e) {
                throw (DeploymentException) e.getException();
            }
            OMAttribute mepAtt = msgReceiver.getAttribute(new QName(TAG_MEP));
            mr_mep.put(mepAtt.getAttributeValue(), receiver);
        }
        return mr_mep;
    }

    /**
     * Processes default message receivers specified either in axis2.xml or
     * services.xml.
     *
     */
    protected HashMap<String,MessageReceiver> processMessageReceivers(ClassLoader loader,
                                              OMElement element) throws DeploymentException {
        HashMap<String,MessageReceiver> meps = new HashMap<String,MessageReceiver>();
        Iterator iterator = element.getChildrenWithName(new QName(
                TAG_MESSAGE_RECEIVER));
        while (iterator.hasNext()) {
            OMElement receiverElement = (OMElement) iterator.next();
            MessageReceiver receiver = loadMessageReceiver(loader,
                                                           receiverElement);
            OMAttribute mepAtt = receiverElement
                    .getAttribute(new QName(TAG_MEP));
            meps.put(mepAtt.getAttributeValue(), receiver);
        }
        return meps;
    }

    protected MessageReceiver loadMessageReceiver(ClassLoader loader,
                                                  OMElement element) throws DeploymentException {
        OMAttribute receiverName = element.getAttribute(new QName(
                TAG_CLASS_NAME));
        String className = receiverName.getAttributeValue();
        MessageReceiver receiver = null;

        try {
            Class messageReceiver;

            if ((className != null) && !"".equals(className)) {
                messageReceiver = Loader.loadClass(loader, className);
                receiver = (MessageReceiver) messageReceiver.newInstance();
            }
        } catch (ClassNotFoundException e) {
            throw new DeploymentException(Messages.getMessage(
                    DeploymentErrorMsgs.ERROR_IN_LOADING_MESSAGE_RECEIVER,
                    "ClassNotFoundException", className), e);
        } catch (IllegalAccessException e) {
            throw new DeploymentException(Messages.getMessage(
                    DeploymentErrorMsgs.ERROR_IN_LOADING_MESSAGE_RECEIVER,
                    "IllegalAccessException", className), e);
        } catch (InstantiationException e) {
            throw new DeploymentException(Messages.getMessage(
                    DeploymentErrorMsgs.ERROR_IN_LOADING_MESSAGE_RECEIVER,
                    "InstantiationException", className), e);
        }

        return receiver;
    }

    /**
     * Processes the message builders specified in axis2.xml or services.xml.
     *
     * @param messageBuildersElement
     */
    protected HashMap processMessageBuilders(OMElement messageBuildersElement)
            throws DeploymentException {
        HashMap builderSelector = new HashMap();
        Iterator msgBuilders = messageBuildersElement
                .getChildrenWithName(new QName(TAG_MESSAGE_BUILDER));
        while (msgBuilders.hasNext()) {
            OMElement msgBuilderElement = (OMElement) msgBuilders.next();
            OMAttribute builderName = msgBuilderElement.getAttribute(new QName(TAG_CLASS_NAME));
            String className = builderName.getAttributeValue();
            Class builderClass = null;
            Builder builderObject;
            try {
                builderClass = findAndValidateSelectorClass(className,
                                                            DeploymentErrorMsgs.ERROR_LOADING_MESSAGE_BUILDER);
                builderObject = (Builder) builderClass.newInstance();
            } catch (PrivilegedActionException e) {
                throw (DeploymentException) e.getException();
            } catch (InstantiationException e) {
                throw new DeploymentException(
                        "Cannot instantiate the specified Builder Class  : "
                                + builderClass.getName() + ".", e);
            } catch (IllegalAccessException e) {
                throw new DeploymentException(
                        "Cannot instantiate the specified Builder Class : "
                                + builderClass.getName() + ".", e);
            }
            OMAttribute contentTypeAtt = msgBuilderElement
                    .getAttribute(new QName(TAG_CONTENT_TYPE));
            builderSelector.put(contentTypeAtt.getAttributeValue(),
                                builderObject);
        }
        return builderSelector;
    }


    /**
     * Processes the message builders specified in axis2.xml or services.xml.
     */
    protected HashMap processMessageFormatters(OMElement messageFormattersElement)
            throws DeploymentException {
        HashMap messageFormatters = new HashMap();
        Iterator msgFormatters = messageFormattersElement
                .getChildrenWithName(new QName(TAG_MESSAGE_FORMATTER));
        while (msgFormatters.hasNext()) {
            OMElement msgFormatterElement = (OMElement) msgFormatters.next();
            OMElement tempMsgFormatter = msgFormatterElement;
            OMAttribute formatterName = tempMsgFormatter.getAttribute(new QName(TAG_CLASS_NAME));
            String className = formatterName.getAttributeValue();
            MessageFormatter formatterObject;
            Class formatterClass = null;
            try {
                formatterClass = findAndValidateSelectorClass(className,
                                                              DeploymentErrorMsgs.ERROR_LOADING_MESSAGE_FORMATTER);
                formatterObject = (MessageFormatter) formatterClass.newInstance();
            } catch (PrivilegedActionException e) {
                throw (DeploymentException) e.getException();
            } catch (InstantiationException e) {
                throw new DeploymentException(
                        "Cannot instantiate the specified Formatter Class  : "
                                + formatterClass.getName() + ".", e);
            } catch (IllegalAccessException e) {
                throw new DeploymentException(
                        "Cannot instantiate the specified Formatter Class : "
                                + formatterClass.getName() + ".", e);
            }
            OMAttribute contentTypeAtt = msgFormatterElement
                    .getAttribute(new QName(TAG_CONTENT_TYPE));
            messageFormatters.put(contentTypeAtt.getAttributeValue(),
                                  formatterObject);
        }
        return messageFormatters;
    }

    protected Class findAndValidateSelectorClass(final String className, final String errorMsg)
            throws PrivilegedActionException {
        return (Class) org.apache.axis2.java.security.AccessController
                .doPrivileged(new PrivilegedExceptionAction() {
                    public Object run()
                            throws org.apache.axis2.deployment.DeploymentException {
                        Class selectorClass;
                        try {
                            if ((className != null) && !"".equals(className)) {
                                selectorClass = Loader.loadClass(Thread.currentThread()
                                        .getContextClassLoader(), className);
                            } else {
                                throw new DeploymentException(Messages.getMessage(errorMsg,
                                                                                  "Invalid Class Name",
                                                                                  className));
                            }
                        } catch (ClassNotFoundException e) {
                            throw new DeploymentException(Messages.getMessage(errorMsg,
                                                                              "ClassNotFoundException",
                                                                              className), e);
                        }
                        return selectorClass;
                    }
                });
    }

    /**
     * Processes flow elements in services.xml .
     *
     * @param flowelement <code>OMElement</code>
     * @return Returns Flow.
     * @throws DeploymentException <code>DeploymentException</code>
     */
    protected Flow processFlow(OMElement flowelement, ParameterInclude parent)
            throws DeploymentException {
        Flow flow = new Flow();

        if (flowelement == null) {
            return flow;
        }

        Iterator handlers = flowelement.getChildrenWithName(new QName(
                TAG_HANDLER));

        while (handlers.hasNext()) {
            OMElement handlerElement = (OMElement) handlers.next();

            flow.addHandler(processHandler(handlerElement, parent));
        }

        return flow;
    }

    protected String[] processSupportedPolicyNamespaces(
            OMElement supportedPolicyElements) {
        OMAttribute namespaces = supportedPolicyElements
                .getAttribute(new QName(TAG_NAMESPACES));
        if (namespaces != null) {
            String value = namespaces.getAttributeValue();
            if (value.trim().length() != 0) {
                return value.split(" ");
            }
        }
        return null;
    }

    protected QName[] getLocalPolicyAssertionNames(OMElement localPolicyAssertionsElement) {

        Iterator iterator = localPolicyAssertionsElement.getChildElements();
        if (! iterator.hasNext()) {
            return null;
        }

        ArrayList qnames = new ArrayList();
        OMElement childElement;

        for (; iterator.hasNext();) {
            childElement = (OMElement) iterator.next();
            qnames.add(childElement.getQName());
        }

        QName[] buffer = new QName[qnames.size()];
        System.arraycopy(qnames.toArray(), 0, buffer, 0, qnames.size());
        return buffer;

    }

    protected HandlerDescription processHandler(OMElement handler_element, ParameterInclude parent)
            throws DeploymentException
    {
        return processHandler(handler_element, parent, null);
    }
    /**
     * Processes Handler element.
     *
     * @param handler_element <code>OMElement</code>
     * @return Returns HandlerDescription.
     * @throws DeploymentException <code>DeploymentException</code>
     */
    protected HandlerDescription processHandler(OMElement handler_element,
                                                ParameterInclude parent,
                                                String containingPhase)
            throws DeploymentException {

        // Setting handler name
        OMAttribute name_attribute = handler_element.getAttribute(new QName(
                ATTRIBUTE_NAME));

        if (name_attribute == null || name_attribute.getAttributeValue().equals("")) {
            throw new DeploymentException(Messages.getMessage(
                    DeploymentErrorMsgs.INVALID_HANDLER, "Unknown", "Name missing"));
        }

        HandlerDescription handler = new HandlerDescription(name_attribute.getAttributeValue());

        // Setting handler class name
        OMAttribute class_attribute = handler_element.getAttribute(new QName(
                TAG_CLASS_NAME));

        if (class_attribute == null) {
            throw new DeploymentException((Messages.getMessage(
                    DeploymentErrorMsgs.INVALID_HANDLER, name_attribute.getAttributeValue(),
                    "class name is missing")));
        } else {
            handler.setClassName(class_attribute.getAttributeValue());
        }

        // processing phase rules (order)
        OMElement order_element = handler_element
                .getFirstChildWithName(new QName(TAG_ORDER));

        PhaseRule rules = handler.getRules();
        if (order_element == null) {
            if (containingPhase == null) {
                // TODO : Include more information (which handler?) in message!
                throw new DeploymentException((Messages.getMessage(
                        DeploymentErrorMsgs.INVALID_HANDLER, name_attribute.getAttributeValue(),
                        "phase rule has not been specified")));
            }
            rules.setPhaseName(containingPhase);
        } else {
            Iterator order_itr = order_element.getAllAttributes();

            while (order_itr.hasNext()) {
                OMAttribute orderAttribute = (OMAttribute) order_itr.next();
                String name = orderAttribute.getQName().getLocalPart();
                String value = orderAttribute.getAttributeValue();

                if (TAG_AFTER.equals(name)) {
                    rules.setAfter(value);
                } else if (TAG_BEFORE.equals(name)) {
                    rules.setBefore(value);
                } else if (TAG_PHASE.equals(name)) {
                    rules.setPhaseName(value);
                } else if (TAG_PHASE_FIRST.equals(name)) {
                    String boolval = getValue(value);

                    if (boolval.equals(BOOLEAN_TRUE)) {
                        rules.setPhaseFirst(true);
                    } else if (boolval.equals(BOOLEAN_FALSE)) {
                        rules.setPhaseFirst(false);
                    }
                } else if (TAG_PHASE_LAST.equals(name)) {
                    String boolval = getValue(value);

                    if (boolval.equals(BOOLEAN_TRUE)) {
                        rules.setPhaseLast(true);
                    } else if (boolval.equals(BOOLEAN_FALSE)) {
                        rules.setPhaseLast(false);
                    }
                }
            }

            Iterator parameters = handler_element
                    .getChildrenWithName(new QName(TAG_PARAMETER));

            try {
                processParameters(parameters, handler, parent);
            } catch (AxisFault axisFault) {
                throw new DeploymentException(axisFault);
            }
        }

        handler.setParent(parent);

        return handler;
    }

    protected void processOperationModuleRefs(Iterator moduleRefs,
                                              AxisOperation operation) throws DeploymentException {
//        try {
            while (moduleRefs.hasNext()) {
                OMElement moduleref = (OMElement) moduleRefs.next();
                OMAttribute moduleRefAttribute = moduleref
                        .getAttribute(new QName(TAG_REFERENCE));

                if (moduleRefAttribute != null) {
                    String refName = moduleRefAttribute.getAttributeValue();
                    operation.addModule(refName);

//                    if (axisConfig.getModule(refName) == null) {
//                        throw new DeploymentException(Messages.getMessage(
//                                DeploymentErrorMsgs.MODULE_NOT_FOUND, refName));
//                    } else {
//                        operation.addModule(refName);
//                    }
                }
            }
//        } catch (AxisFault axisFault) {
//            throw new DeploymentException(Messages.getMessage(
//                    DeploymentErrorMsgs.MODULE_NOT_FOUND, axisFault
//                    .getMessage()), axisFault);
//        }
    }

    /**
     * Gets the Parameter object from the OM.
     *
     * @param parameters       <code>Parameter</code>
     * @param parameterInclude <code>ParameterInclude</code>
     * @param parent           <code>ParameterInclude</code>
     */
    protected void processParameters(Iterator parameters,
                                     ParameterInclude parameterInclude,
                                     ParameterInclude parent)
            throws DeploymentException {
        while (parameters.hasNext()) {
            // this is to check whether some one has locked the parmeter at the
            // top level
            OMElement parameterElement = (OMElement) parameters.next();
            Parameter parameter = new Parameter();
            // setting parameterElement
            parameter.setParameterElement(parameterElement);
            // setting parameter Name
            OMAttribute paramName = parameterElement.getAttribute(new QName(ATTRIBUTE_NAME));
            if (paramName == null) {
                throw new DeploymentException(Messages.getMessage(
                        DeploymentErrorMsgs.BAD_PARAMETER_ARGUMENT,
                        parameterElement.toString()));
            }
            parameter.setName(paramName.getAttributeValue());
            // setting parameter Value (the child element of the parameter)
            OMElement paramValue = parameterElement.getFirstElement();
            if (paramValue != null) {
                parameter.setValue(parameterElement);
                parameter.setParameterType(Parameter.OM_PARAMETER);
            } else {
                String paratextValue = parameterElement.getText();

                parameter.setValue(paratextValue);
                parameter.setParameterType(Parameter.TEXT_PARAMETER);
            }
            // setting locking attribute
            OMAttribute paramLocked = parameterElement.getAttribute(new QName(
                    ATTRIBUTE_LOCKED));
            Parameter parentParam = null;
            if (parent != null) {
                parentParam = parent.getParameter(parameter.getName());
            }
            if (paramLocked != null) {
                String lockedValue = paramLocked.getAttributeValue();
                if (BOOLEAN_TRUE.equals(lockedValue)) {
                    // if the parameter is locked at some level parameter value
                    // replace by that
                    if ((parent != null)
                            && parent.isParameterLocked(parameter.getName())) {
                        throw new DeploymentException(Messages.getMessage(
                                DeploymentErrorMsgs.PARAMETER_LOCKED, parameter.getName()));
                    } else {
                        parameter.setLocked(true);
                    }
                } else {
                    parameter.setLocked(false);
                }
            }
            try {
                if (parent != null) {
                    if ((parentParam == null)
                            || !parent.isParameterLocked(parameter.getName())) {
                        parameterInclude.addParameter(parameter);
                    }
                } else {
                    parameterInclude.addParameter(parameter);
                }
            } catch (AxisFault axisFault) {
                throw new DeploymentException(axisFault);
            }
        }
    }

    /**
     * Populate the AxisOperation with details from the actionMapping,
     * outputActionMapping and faultActionMapping elements from the operation
     * element.
     *
     * @param operation
     * @param op_descrip
     */
    protected void processActionMappings(OMElement operation,
                                         AxisOperation op_descrip) {
        Iterator mappingIterator = operation.getChildrenWithName(new QName(
                Constants.ACTION_MAPPING));
        ArrayList mappingList = new ArrayList();
        while (mappingIterator.hasNext()) {
            OMElement mappingElement = (OMElement) mappingIterator.next();
            String inputActionString = mappingElement.getText().trim();
            if (log.isTraceEnabled()) {
                log.trace("Input Action Mapping found: " + inputActionString);
            }
            if (!"".equals(inputActionString)) {
                mappingList.add(inputActionString);
            } else {
                if (log.isTraceEnabled()) {
                    log.trace("Zero length input action string found. Not added to mapping");
                }
            }
        }
        op_descrip.setWsamappingList(mappingList);

        OMElement outputAction = operation.getFirstChildWithName(new QName(
                Constants.OUTPUT_ACTION_MAPPING));
        if ((outputAction != null) && (outputAction.getText() != null)) {
            String outputActionString = outputAction.getText().trim();
            if (log.isTraceEnabled()) {
                log.trace("Output Action Mapping found: " + outputActionString);
            }
            op_descrip.setOutputAction(outputActionString);
        }
        Iterator faultActionsIterator = operation
                .getChildrenWithName(new QName(Constants.FAULT_ACTION_MAPPING));
        while (faultActionsIterator.hasNext()) {
            OMElement faultMappingElement = (OMElement) faultActionsIterator
                    .next();
            String faultActionString = faultMappingElement.getText().trim();
            String faultActionName = faultMappingElement
                    .getAttributeValue(new QName(Constants.FAULT_ACTION_NAME));
            if (faultActionName != null && faultActionString != null) {
                if (log.isTraceEnabled()) {
                    log.trace("Fault Action Mapping found: " + faultActionName
                            + ", " + faultActionString);
                }
                op_descrip.addFaultAction(faultActionName, faultActionString);
            }
        }
    }

    protected void processPolicyElements(Iterator policyElements,
                                         PolicySubject policySubject) {
        while (policyElements.hasNext()) {
            Policy p = PolicyEngine
                    .getPolicy((OMElement) policyElements.next());
            policySubject.attachPolicy(p);
        }
    }

    protected void processPolicyRefElements(Iterator policyRefElements,
                                            PolicySubject policySubject) {
        while (policyRefElements.hasNext()) {
            PolicyReference policyReference = PolicyEngine
                    .getPolicyReference((OMElement) policyRefElements.next());
            policySubject.attachPolicyReference(policyReference);
        }
    }

    /**
     * Gets the short file name. Short file name is the name before the dot.
     *
     * @param fileName
     * @return Returns String.
     */
    public static String getShortFileName(String fileName) {
        char seperator = SEPARATOR_DOT;
        String value;
        int index = fileName.lastIndexOf(seperator);

        if (index > 0) {
            value = fileName.substring(0, index);

            return value;
        }

        return fileName;
    }

    /**
     * Gets the value of an attribute. eg xsd:anyVal --> anyVal
     *
     * @return Returns String.
     */
    protected String getValue(String in) {
        char seperator = SEPARATOR_COLON;
        String value;
        int index = in.indexOf(seperator);

        if (index > 0) {
            value = in.substring(index + 1, in.length());

            return value;
        }

        return in;
    }
}
