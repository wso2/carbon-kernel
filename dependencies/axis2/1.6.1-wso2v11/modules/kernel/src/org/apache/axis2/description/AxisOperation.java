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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.axiom.util.UIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisError;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.phaseresolver.PhaseResolver;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AxisOperation extends AxisDescription
        implements WSDLConstants {

    public static final String STYLE_RPC = "rpc";
    public static final String STYLE_MSG = "msg";
    public static final String STYLE_DOC = "doc";

    private static final Log log = LogFactory.getLog(AxisOperation.class);
    /** message exchange pattern */
    private int mep = WSDLConstants.MEP_CONSTANT_INVALID;

    // to hide control operation , operation which added by RM like module
    private boolean controlOperation = false;
    private String style = STYLE_DOC;

    // to store mepURL
    protected String mepURI;

    private MessageReceiver messageReceiver;

    private HashMap<String, ModuleConfiguration> moduleConfigmap;

    // To store deploy-time module refs
    private ArrayList<String> modulerefs;

    private ArrayList<AxisMessage> faultMessages;

    private QName name;

    private ArrayList<String> wsamappingList;
    private String outputAction;
    private LinkedHashMap<String, String> faultActions = new LinkedHashMap<String, String>();

    private String soapAction;


    /** Default constructor */
    public AxisOperation() {
        mepURI = WSDL2Constants.MEP_URI_IN_OUT;
        modulerefs = new ArrayList<String>();
        moduleConfigmap = new HashMap<String, ModuleConfiguration>();
        faultMessages = new ArrayList<AxisMessage>();
        //setup a temporary name
        QName tmpName = new QName(this.getClass().getName() + "_" + UIDGenerator.generateUID());
        this.setName(tmpName);
    }

    public AxisOperation(QName name) {
        this();
        this.setName(name);
    }

    public abstract void addMessage(AxisMessage message, String label);

    /**
     * Adds a message context into an operation context. Depending on MEPs, this method has to be
     * overridden. Depending on the MEP operation description know how to fill the message context
     * map in operationContext. As an example, if the MEP is IN-OUT then depending on messagable
     * operation description should know how to keep them in correct locations.
     *
     * @param msgContext <code>MessageContext</code>
     * @param opContext  <code>OperationContext</code>
     * @throws AxisFault <code>AxisFault</code>
     */
    public abstract void addMessageContext(MessageContext msgContext, OperationContext opContext)
            throws AxisFault;

    public abstract void addFaultMessageContext(MessageContext msgContext,
                                                OperationContext opContext)
            throws AxisFault;

    public void addModule(String moduleName) {
        modulerefs.add(moduleName);
    }

    /**
     * Adds module configuration, if there is moduleConfig tag in operation.
     *
     * @param moduleConfiguration a ModuleConfiguration which will be added (by name)
     */
    public void addModuleConfig(ModuleConfiguration moduleConfiguration) {
        moduleConfigmap.put(moduleConfiguration.getModuleName(), moduleConfiguration);
    }

    /**
     * This is called when a module is engaged on this operation.  Handle operation-specific tasks.
     *
     * @param axisModule AxisModule being engaged
     * @param engager    the AxisDescription where the engage occurred - could be us or a parent
     * @throws AxisFault
     */
    public final void onEngage(AxisModule axisModule, AxisDescription engager) throws AxisFault {
        // Am I the source of this engagement?
        boolean selfEngaged = (engager == this);

        // If I'm not, the operations will already have been added by someone above, so don't
        // do it again.
        if (selfEngaged) {
            AxisService service = getAxisService();
            if (service != null) {
                service.addModuleOperations(axisModule);
            }
        }
        AxisConfiguration axisConfig = getAxisConfiguration();
        PhaseResolver phaseResolver = new PhaseResolver(axisConfig);
        phaseResolver.engageModuleToOperation(this, axisModule);
    }

    protected void onDisengage(AxisModule module) {
        AxisService service = getAxisService();
        if (service == null) return;

        AxisConfiguration axisConfiguration = service.getAxisConfiguration();
        PhaseResolver phaseResolver = new PhaseResolver(axisConfiguration);
        if (!service.isEngaged(module.getName()) &&
            (axisConfiguration != null && !axisConfiguration.isEngaged(module.getName()))) {
            phaseResolver.disengageModuleFromGlobalChains(module);
        }
        phaseResolver.disengageModuleFromOperationChain(module, this);

        //removing operations added at the time of module engagemnt
        HashMap<QName, AxisOperation> moduleOperations = module.getOperations();
        if (moduleOperations != null) {
            Iterator<AxisOperation> moduleOperations_itr = moduleOperations.values().iterator();
            while (moduleOperations_itr.hasNext()) {
                AxisOperation operation = moduleOperations_itr.next();
                service.removeOperation(operation.getName());
            }
        }
    }

    /**
     * To remove module from engage  module list
     *
     * @param module module to remove
     * @deprecated please use disengageModule(), this method will disappear after 1.3
     */
    public void removeFromEngagedModuleList(AxisModule module) {
        try {
            disengageModule(module);
        } catch (AxisFault axisFault) {
            // Can't do much here...
            log.error(axisFault.getMessage(), axisFault);
        }
    }

//  Note - removed this method which was dead code.
//    private AxisOperation copyOperation(AxisOperation axisOperation) throws AxisFault {

    /**
     * Returns as existing OperationContext related to this message if one exists.
     * <p/>
     * TODO - why both this and findOperationContext()? (GD)
     *
     * @param msgContext the MessageContext for which we'd like an OperationContext
     * @return the OperationContext, or null
     * @throws AxisFault
     */
    public OperationContext findForExistingOperationContext(MessageContext msgContext)
            throws AxisFault {
        OperationContext operationContext;

        if ((operationContext = msgContext.getOperationContext()) != null) {
            return operationContext;
        }

        // If this message is not related to another one, or it is but not one emitted
        // from the same operation, don't further look for an operation context or fault.
        if (null != msgContext.getRelatesTo()) {
            // So this message may be part of an ongoing MEP
            ConfigurationContext configContext = msgContext.getConfigurationContext();

            operationContext =
                    configContext.getOperationContext(msgContext.getRelatesTo().getValue());

            if (null == operationContext && log.isDebugEnabled()) {
                log.debug(msgContext.getLogIDString() +
                          " Cannot correlate inbound message RelatesTo value [" +
                          msgContext.getRelatesTo() + "] to in-progree MEP");
            }
        }

        return operationContext;
    }

    /**
     * Finds an OperationContext for an incoming message. An incoming message can be of two states.
     * <p/>
     * 1)This is a new incoming message of a given MEP. 2)This message is a part of an MEP which has
     * already begun.
     * <p/>
     * The method is special cased for the two MEPs
     * <p/>
     * #IN_ONLY #IN_OUT
     * <p/>
     * for two reasons. First reason is the wide usage and the second being that the need for the
     * MEPContext to be saved for further incoming messages.
     * <p/>
     * In the event that MEP of this operation is different from the two MEPs defaulted above the
     * decision of creating a new or this message relates to a MEP which already in business is
     * decided by looking at the WSA Relates TO of the incoming message.
     *
     * @param msgContext     MessageContext to search
     * @param serviceContext ServiceContext (TODO - why pass this? (GD))
     * @return the active OperationContext
     */
    public OperationContext findOperationContext(MessageContext msgContext,
                                                 ServiceContext serviceContext)
            throws AxisFault {
        OperationContext operationContext;

        if (null == msgContext.getRelatesTo()) {

            // Its a new incoming message so get the factory to create a new
            // one
            operationContext = serviceContext.createOperationContext(this);
        } else {

            // So this message is part of an ongoing MEP
            ConfigurationContext configContext = msgContext.getConfigurationContext();

            operationContext =
                    configContext.getOperationContext(msgContext.getRelatesTo().getValue());

            if (null == operationContext) {
                throw new AxisFault(Messages.getMessage("cannotCorrelateMsg",
                                                        this.name.toString(),
                                                        msgContext.getRelatesTo().getValue()));
            }
        }
        return operationContext;
    }

    public void registerOperationContext(MessageContext msgContext,
                                         OperationContext operationContext)
            throws AxisFault {
        msgContext.setAxisOperation(this);
        msgContext.getConfigurationContext().registerOperationContext(msgContext.getMessageID(),
                                                                      operationContext);
        operationContext.addMessageContext(msgContext);
        msgContext.setOperationContext(operationContext);
        if (operationContext.isComplete()) {
            operationContext.cleanup();
        }
    }

    public void registerMessageContext(MessageContext msgContext,
                                       OperationContext operationContext) throws AxisFault {
        msgContext.setAxisOperation(this);
        operationContext.addMessageContext(msgContext);
        msgContext.setOperationContext(operationContext);
        if (operationContext.isComplete()) {
            operationContext.cleanup();
        }
    }

    /**
     * Maps the String URI of the Message exchange pattern to an integer. Further, in the first
     * lookup, it will cache the looked up value so that the subsequent method calls are extremely
     * efficient.
     *
     * @return an MEP constant from WSDLConstants
     */
    public int getAxisSpecificMEPConstant() {
        if (this.mep != WSDLConstants.MEP_CONSTANT_INVALID) {
            return this.mep;
        }

        int temp = WSDLConstants.MEP_CONSTANT_INVALID;

        if (WSDL2Constants.MEP_URI_IN_OUT.equals(mepURI)) {
            temp = WSDLConstants.MEP_CONSTANT_IN_OUT;
        } else if (WSDL2Constants.MEP_URI_IN_ONLY.equals(mepURI)) {
            temp = WSDLConstants.MEP_CONSTANT_IN_ONLY;
        } else if (WSDL2Constants.MEP_URI_IN_OPTIONAL_OUT.equals(mepURI)) {
            temp = WSDLConstants.MEP_CONSTANT_IN_OPTIONAL_OUT;
        } else if (WSDL2Constants.MEP_URI_OUT_IN.equals(mepURI)) {
            temp = WSDLConstants.MEP_CONSTANT_OUT_IN;
        } else if (WSDL2Constants.MEP_URI_OUT_ONLY.equals(mepURI)) {
            temp = WSDLConstants.MEP_CONSTANT_OUT_ONLY;
        } else if (WSDL2Constants.MEP_URI_OUT_OPTIONAL_IN.equals(mepURI)) {
            temp = WSDLConstants.MEP_CONSTANT_OUT_OPTIONAL_IN;
        } else if (WSDL2Constants.MEP_URI_ROBUST_IN_ONLY.equals(mepURI)) {
            temp = WSDLConstants.MEP_CONSTANT_ROBUST_IN_ONLY;
        } else if (WSDL2Constants.MEP_URI_ROBUST_OUT_ONLY.equals(mepURI)) {
            temp = WSDLConstants.MEP_CONSTANT_ROBUST_OUT_ONLY;
        }

        if (temp == WSDLConstants.MEP_CONSTANT_INVALID) {
            throw new AxisError(Messages.getMessage("mepmappingerror"));
        }

        this.mep = temp;

        return this.mep;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.axis2.description.AxisService#getEngadgedModules()
     */

    public abstract AxisMessage getMessage(String label);

    public String getMessageExchangePattern() {
        return mepURI;
    }

    public MessageReceiver getMessageReceiver() {
        return messageReceiver;
    }

    public ModuleConfiguration getModuleConfig(String moduleName) {
        return moduleConfigmap.get(moduleName);
    }

    public ArrayList<String> getModuleRefs() {
        return modulerefs;
    }

    public QName getName() {
        return name;
    }

    public abstract ArrayList getPhasesInFaultFlow();

    public abstract ArrayList getPhasesOutFaultFlow();

    public abstract ArrayList getPhasesOutFlow();

    public abstract ArrayList getRemainingPhasesInFlow();

    public String getStyle() {
        return style;
    }

    public ArrayList<String> getWSAMappingList() {
        return wsamappingList;
    }

    public boolean isControlOperation() {
        return controlOperation;
    }

    // to check whether a given parameter is locked
    public boolean isParameterLocked(String parameterName) {

        // checking the locked value of parent
        boolean locked = false;

        if (getParent() != null) {
            locked = getParent().isParameterLocked(parameterName);
        }

        if (locked) {
            return true;
        } else {
            Parameter parameter = getParameter(parameterName);

            return (parameter != null) && parameter.isLocked();
        }
    }

    public void setControlOperation(boolean controlOperation) {
        this.controlOperation = controlOperation;
    }

    public void setMessageExchangePattern(String mepURI) {
        this.mepURI = mepURI;
    }

    public void setMessageReceiver(MessageReceiver messageReceiver) {
        this.messageReceiver = messageReceiver;
    }

    public void setName(QName name) {
        this.name = name;
    }

    public abstract void setPhasesInFaultFlow(ArrayList list);

    public abstract void setPhasesOutFaultFlow(ArrayList list);

    public abstract void setPhasesOutFlow(ArrayList list);

    public abstract void setRemainingPhasesInFlow(ArrayList list);

    public void setStyle(String style) {
        if (!"".equals(style)) {
            this.style = style;
        }
    }

    public void setWsamappingList(ArrayList wsamappingList) {
        if (log.isDebugEnabled()) {
            log.debug("setWsamappinglist");
        }
        this.wsamappingList = wsamappingList;
        if (wsamappingList != null && !wsamappingList.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug(" wsa action is " + (String)wsamappingList.get(0));
            }
        }
     }

    /**
     * Return an OperationClient suitable for this AxisOperation.
     *
     * @param sc      active ServiceContext
     * @param options active Options
     * @return an OperationClient set up appropriately for this operation
     */
    public abstract OperationClient createClient(ServiceContext sc, Options options);

    public Object getKey() {
        return this.name;
    }

    public ArrayList<AxisMessage> getFaultMessages() {
        return faultMessages;
    }

    public void setFaultMessages(AxisMessage faultMessage) {
        faultMessage.setParent(this);
        faultMessages.add(faultMessage);
        if (getFaultAction(faultMessage.getName()) == null) {
            addFaultAction(faultMessage.getName(),
                           "urn:" + name.getLocalPart() + faultMessage.getName());
        }
    }

    public void setSoapAction(String soapAction) {
        if(log.isDebugEnabled()) {
            log.debug("Entry: AxisOperation::setSoapAction, previous soapAction: " + this.soapAction + " updated soapAction: " + soapAction);
        }
        this.soapAction = soapAction;
        if(log.isDebugEnabled()) {
            log.debug("Exit: AxisOperation::setSoapAction");
        }
    }

    /*
    * Convenience method to access the WS-A Input Action per the
    * WS-A spec. Effectively use the soapAction if available else
    * use the first entry in the WSA Mapping list.
    *
    * Use getSoapAction when you want to get the soap action and this
    * when you want to get the wsa input action.
    */
    public String getInputAction() {
        if(log.isDebugEnabled()) {
            log.debug("Entry: AxisOperation::getInputAction");
        }
        String result = null;
        if (soapAction != null) {
            if(log.isDebugEnabled()) {
                log.debug("Debug: AxisOperation::getInputAction - using soapAction");  // so we know which path was taken
                // log wsa map to see if it matches or is set
                if (wsamappingList != null && !wsamappingList.isEmpty()) {
                    log.debug(" but WSA map indicates " + (String)wsamappingList.get(0));
                }
            }
            result = soapAction;
        } else {
            if (wsamappingList != null && !wsamappingList.isEmpty()) {
                if(log.isDebugEnabled()) {
                    log.debug("Debug: AxisOperation::getInputAction - using wsamappingList");
                }
                result = wsamappingList.get(0);
            }
        }
        if(log.isDebugEnabled()) {
            log.debug("Exit: AxisOperation::getInputAction " + result);
        }
        return result;
    }

    public String getOutputAction() {
        return outputAction;
    }

    public void setOutputAction(String act) {
        outputAction = act;
    }

    public void addFaultAction(String faultName, String action) {
        faultActions.put(faultName, action);
    }

    public void removeFaultAction(String faultName) {
        faultActions.remove(faultName);
    }

    public String getFaultAction(String faultName) {
        return faultActions.get(faultName);
    }

    public String[] getFaultActionNames() {
        Set<String> keys = faultActions.keySet();
        String[] faultActionNames = new String[keys.size()];
        faultActionNames = keys.toArray(faultActionNames);
        return faultActionNames;
    }

    public String getFaultAction() {
        String result = null;
        Iterator<String> iter = faultActions.values().iterator();
        if (iter.hasNext()) {
            result = iter.next();
        }
        return result;
    }

    /**
     * Get the messages referenced by this operation
     *
     * @return an Iterator of all the AxisMessages we deal with
     */
    public Iterator<AxisMessage> getMessages() {
        return (Iterator<AxisMessage>)getChildren();
    }

    /**
     * Typesafe access to parent service
     *
     * @return the AxisService which contains this AxisOperation
     */
    public AxisService getAxisService() {
        return (AxisService)getParent();
    }

    public String getSoapAction() {
        if(log.isDebugEnabled()) {
            log.debug("AxisOperation::getSoapAction " + soapAction);
        }
        /*
         * This AxisOperation instance may be used for the client OUT-IN or for
         * the server IN-OUT.  If the below code were changed to getInputActions, and the
         * result of getInputAction were put in the SOAP action header on a client outbound
         * message, the server would receive an INCORRECT SOAP action header.  We should leave
         * this as 'return soapAction;' OR make it client/server aware.
         */
        return soapAction;
    }
}
