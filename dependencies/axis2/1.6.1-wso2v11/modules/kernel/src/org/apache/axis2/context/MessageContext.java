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

package org.apache.axis2.context;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.util.DetachableInputStream;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.util.UIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.Constants.Configuration;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.RelatesTo;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.externalize.ActivateUtils;
import org.apache.axis2.context.externalize.ExternalizeConstants;
import org.apache.axis2.context.externalize.MessageExternalizeUtils;
import org.apache.axis2.context.externalize.SafeObjectInputStream;
import org.apache.axis2.context.externalize.SafeObjectOutputStream;
import org.apache.axis2.context.externalize.SafeSerializable;
import org.apache.axis2.description.AxisBinding;
import org.apache.axis2.description.AxisBindingMessage;
import org.apache.axis2.description.AxisBindingOperation;
import org.apache.axis2.description.AxisEndpoint;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.ModuleConfiguration;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisError;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.engine.Phase;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.MetaDataEntry;
import org.apache.axis2.util.SelfManagedDataHolder;
import org.apache.axis2.util.PolicyUtil;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.wsdl.WSDLUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyComponent;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * <p>Axis2 states are held in two information models, called description hierarchy
 *  and context hierarchy. Description hierarchy hold deployment configuration
 *  and it's values does not change unless deployment configuration change
 *  occurs where Context hierarchy hold run time information. Both hierarchies
 *  consists four levels, Global, Service Group, Operation and Message. Please
 *  look at "Information Model" section  of "Axis2 Architecture Guide" for more
 *  information.</p>
 *  <p>MessageContext hold run time information about one Message invocation. It
 *  hold reference to OperationContext, ServiceGroupContext, and Configuration
 *  Context tied with current message. For an example if you need accesses to other
 *  messages of the current invocation, you can get to them via OperationContext.
 *  Addition to class attributes define in Message context, message context stores
 *  the information as name value pairs. Those name value pairs,and class attributes
 *  tweak the execution behavior of message context and some of them can be find in
 *  org.apache.axis2.Constants class. (TODO we should provide list of supported
 *  options). You may set them at any level of context hierarchy and they will
 *  affect invocations related to their child elements. </p>
 */
public class MessageContext extends AbstractContext
    implements Externalizable, SafeSerializable {

    /*
     * setup for logging
     */
    private static final Log log = LogFactory.getLog(MessageContext.class);

    /**
     * @serial An ID which can be used to correlate operations on a single
     * message in the log files, irrespective of thread switches, persistence,
     * etc.
     */
    private String logCorrelationID = null;

    /**
     * This string will be used to hold a form of the logCorrelationID that
     * is more suitable for output than its generic form.
     */
    private transient String logCorrelationIDString = null;

    private static final String myClassName = "MessageContext";

    /**
     * @serial The serialization version ID tracks the version of the class.
     * If a class definition changes, then the serialization/externalization
     * of the class is affected. If a change to the class is made which is
     * not compatible with the serialization/externalization of the class,
     * then the serialization version ID should be updated.
     * Refer to the "serialVer" utility to compute a serialization
     * version ID.
     */
    private static final long serialVersionUID = -7753637088257391858L;

    /**
     * @serial Tracks the revision level of a class to identify changes to the
     * class definition that are compatible to serialization/externalization.
     * If a class definition changes, then the serialization/externalization
     * of the class is affected.
     * Refer to the writeExternal() and readExternal() methods.
     */
    // supported revision levels, add a new level to manage compatible changes
    private static final int REVISION_2 = 2;
    // current revision level of this object
    private static final int revisionID = REVISION_2;

    private static final boolean DEBUG_ENABLED = log.isDebugEnabled() || log.isTraceEnabled();

    /**
     * A place to store the current MessageContext
     */
    public static ThreadLocal<MessageContext> currentMessageContext = new ThreadLocal<MessageContext>();

    public static MessageContext getCurrentMessageContext() {
        return currentMessageContext.get();
    }

    public static void destroyCurrentMessageContext() {
        currentMessageContext.remove();
    }

    public static void setCurrentMessageContext(MessageContext ctx) {
        currentMessageContext.set(ctx);
    }


    /**
     * @serial Options on the message
     */
    protected Options options;

    public final static int IN_FLOW = 1;
    public final static int IN_FAULT_FLOW = 3;

    public final static int OUT_FLOW = 2;
    public final static int OUT_FAULT_FLOW = 4;

    public static final String REMOTE_ADDR = "REMOTE_ADDR";
    public static final String TRANSPORT_ADDR = "TRANSPORT_ADDR";
    public static final String TRANSPORT_HEADERS = "TRANSPORT_HEADERS";

    /**
     * Constant used as the key for the property which stores the In MessageContext in the
     * Out MessageContext/FaultMessageContext. This is needed in cases where an OperationContext
     * is not created, for example, since the request never gets dispatched to the service
     * operation, either due to a security failure or a request coming in for a non-existing
     * endpoint
     */
    public static final String IN_MESSAGE_CONTEXT = "axis2.inMsgContext";


    /**
     * message attachments
     * NOTE: Serialization of message attachments is handled as part of the
     * overall message serialization.  If this needs to change, then
     * investigate having the Attachment class implement the
     * java.io.Externalizable interface.
     */
    public transient Attachments attachments;

    /**
     * Field TRANSPORT_OUT
     */
    public static final String TRANSPORT_OUT = "TRANSPORT_OUT";

    /**
     * Field TRANSPORT_IN
     */
    public static final String TRANSPORT_IN = "TRANSPORT_IN";

    /**
     * Field CHARACTER_SET_ENCODING
     */
    public static final String CHARACTER_SET_ENCODING = "CHARACTER_SET_ENCODING";

    /**
     * Field UTF_8. This is the 'utf-8' value for CHARACTER_SET_ENCODING
     * property.
     */
    public static final String UTF_8 = "UTF-8";

    /**
     * Field UTF_16. This is the 'utf-16' value for CHARACTER_SET_ENCODING
     * property.
     */
    public static final String UTF_16 = "utf-16";

    /**
     * Field TRANSPORT_SUCCEED
     */
    public static final String TRANSPORT_SUCCEED = "TRANSPORT_SUCCEED";

    /**
     * Field DEFAULT_CHAR_SET_ENCODING. This is the default value for
     * CHARACTER_SET_ENCODING property.
     */
    public static final String DEFAULT_CHAR_SET_ENCODING = UTF_8;

    /**
     * @serial The direction flow in use to figure out which path the message is in
     * (send or receive)
     */
    public int FLOW = IN_FLOW;

    /**
      * To invoke fireAndforget method we have to hand over transport sending logic to a thread
      * other wise user has to wait till it get transport response (in the case of HTTP its HTTP
      * 202)
      * 202). This was eariler named TRANSPORT_NON_BLOCKING, but that name is wrong as transport non blocking is NIO,
      * which has nothing to do with this property. See https://issues.apache.org/jira/browse/AXIS2-4196. 
      * Renaming this to CLIENT_API_NON_BLOCKING instead.  
      * 
     */
    public static final String CLIENT_API_NON_BLOCKING = "ClientApiNonBlocking";

    /**
     * This property allows someone (e.g. RM) to disable an async callback from
     * being invoked if a fault occurs during message transmission.  If this is
     * not set, it can be assumed that the fault will be delivered via
     * Callback.onError(...).
     */
    public static final String DISABLE_ASYNC_CALLBACK_ON_TRANSPORT_ERROR =
            "disableTransmissionErrorCallback";

    /**
     * @serial processingFault
     */
    private boolean processingFault;

    /**
     * @serial paused
     */
    private boolean paused;

    /**
     * @serial outputWritten
     */
    public boolean outputWritten;

    /**
     * @serial newThreadRequired
     */
    private boolean newThreadRequired;

    /**
     * @serial isSOAP11
     */
    private boolean isSOAP11 = true;

    /**
     * @serial The chain of Handlers/Phases for processing this message
     */
    private ArrayList<Handler> executionChain;

    /**
     * @serial The chain of executed Handlers/Phases from processing
     */
    private LinkedList<Handler> executedPhases;

    /**
     * @serial Flag to indicate if we are doing REST
     */
    private boolean doingREST;

    /**
     * @serial Flag to indicate if we are doing MTOM
     */
    private boolean doingMTOM;

    /**
     * @serial Flag to indicate if we are doing SWA
     */
    private boolean doingSwA;

    /**
     * AxisMessage associated with this message context
     */
    private transient AxisMessage axisMessage;

    /**
     * AxisOperation associated with this message context
     */
    private transient AxisOperation axisOperation;

    /**
     * AxisService
     */
    private transient AxisService axisService;

    /**
     * AxisServiceGroup
     * <p/>
     * Note the service group can be set independently of the service
     * so the service might not match up with this serviceGroup
     */
    private transient AxisServiceGroup axisServiceGroup;

    /**
     * ConfigurationContext
     */
    private transient ConfigurationContext configurationContext;

    /**
     * @serial Index into the executuion chain of the currently executing handler
     */
    private int currentHandlerIndex;

    /**
     * @serial Index into the current Phase of the currently executing handler (if any)
     */
    private int currentPhaseIndex;

    /**
     * If we're processing this MC due to flowComplete() being called in the case
     * of an Exception, this will hold the Exception which caused the problem.
     */
    private Exception failureReason;

    /**
     * @serial SOAP envelope
     */
    private SOAPEnvelope envelope;

    /**
     * @serial OperationContext
     */
    private OperationContext operationContext;

    /**
     * @serial responseWritten
     */
    private boolean responseWritten;

    /**
     * @serial serverSide
     */
    private boolean serverSide;

    /**
     * @serial ServiceContext
     */
    private ServiceContext serviceContext;

    /**
     * @serial service context ID
     */
    private String serviceContextID;

    /**
     * @serial service group context
     */
    private ServiceGroupContext serviceGroupContext;

    /**
     * @serial Holds a key to retrieve the correct ServiceGroupContext.
     */
    private String serviceGroupContextId;

    /**
     * @serial sessionContext
     */
    private SessionContext sessionContext;


    /**
     * transport out description
     */
    private transient TransportOutDescription transportOut;

    /**
     * transport in description
     */
    private transient TransportInDescription transportIn;


    /**
     * @serial incoming transport name
     */
    //The value will be set by the transport receiver and there will be validation for the transport
    //at the dispatch phase (its post condition)
    private String incomingTransportName;


    /*
     * SelfManagedData will hold message-specific data set by handlers
     * Note that this list is not explicitly saved by the MessageContext, but
     * rather through the SelfManagedDataManager interface implemented by handlers
     */
    private transient LinkedHashMap<String, Object> selfManagedDataMap = null;

    //-------------------------------------------------------------------------
    // MetaData for data to be restored in activate() after readExternal()
    //-------------------------------------------------------------------------

    /**
     * Indicates whether the message context has been reconstituted
     * and needs to have its object references reconciled
     */
    private transient boolean needsToBeReconciled = false;

    /**
     * selfManagedDataHandlerCount is a count of the number of handlers
     * that actually saved data during serialization
     */
    private transient int selfManagedDataHandlerCount = 0;

    /**
     * SelfManagedData cannot be restored until the configurationContext
     * is available, so we have to hold the data from readExternal until
     * activate is called.
     */
    private transient ArrayList<SelfManagedDataHolder> selfManagedDataListHolder = null;

    /**
     * The ordered list of metadata for handlers/phases
     * used during re-constitution of the message context
     */
    private transient ArrayList<MetaDataEntry> metaExecutionChain = null;

    /**
     * The ordered list of metadata for executed phases
     * used during re-constitution of the message context
     */
    private transient LinkedList<MetaDataEntry> metaExecuted = null;

    /**
     * Index into the executuion chain of the currently executing handler
     */
    private transient int metaHandlerIndex = 0;

    /**
     * Index into the current Phase of the currently executing handler (if any)
     */
    private transient int metaPhaseIndex = 0;

    /**
     * The AxisOperation metadata will be used during
     * activate to match up with an existing object
     */
    private transient MetaDataEntry metaAxisOperation = null;

    /**
     * The AxisService metadata will be used during
     * activate to match up with an existing object
     */
    private transient MetaDataEntry metaAxisService = null;

    /**
     * The AxisServiceGroup metadata will be used during
     * activate to match up with an existing object
     */
    private transient MetaDataEntry metaAxisServiceGroup = null;

    /**
     * The TransportOutDescription metadata will be used during
     * activate to match up with an existing object
     */
    private transient MetaDataEntry metaTransportOut = null;

    /**
     * The TransportInDescription metadata will be used during
     * activate to match up with an existing object
     */
    private transient MetaDataEntry metaTransportIn = null;

    /**
     * The AxisMessage metadata will be used during
     * activate to match up with an existing object
     */
    private transient MetaDataEntry metaAxisMessage = null;

    /**
     * Indicates whether this message context has an
     * AxisMessage object associated with it that needs to
     * be reconciled
     */
    private transient boolean reconcileAxisMessage = false;

    /**
     * Indicates whether the executed phase list
     * was reset before the restored list has been reconciled
     */
    private transient boolean executedPhasesReset = false;

    //----------------------------------------------------------------
    // end MetaData section
    //----------------------------------------------------------------


    /**
     * Constructor
     */
    public MessageContext() {
        super(null);
        options = new Options();
    }

    /**
     * Constructor has package access
     *
     * @param configContext the associated ConfigurationContext
     */
    MessageContext(ConfigurationContext configContext) {
        this();
        setConfigurationContext(configContext);
    }

    public String toString() {
        return getLogIDString();
    }

    /**
     * Get a "raw" version of the logCorrelationID.  The logCorrelationID
     * is guaranteed to be unique and may be persisted along with the rest
     * of the message context.
     *
     * @return A string that can be output to a log file as an identifier
     *         for this MessageContext.  It is suitable for matching related log
     *         entries.
     */
    public String getLogCorrelationID() {
        if (logCorrelationID == null) {
            logCorrelationID = UIDGenerator.generateUID();
        }
        return logCorrelationID;
    }

    /**
     * Get a formatted version of the logCorrelationID.
     *
     * @return A string that can be output to a log file as an identifier
     *         for this MessageContext.  It is suitable for matching related log
     *         entries.
     */
    public String getLogIDString() {
        if (logCorrelationIDString == null) {
            logCorrelationIDString = "[MessageContext: logID=" + getLogCorrelationID() + "]";
        }
        return logCorrelationIDString;
    }


    /**
     * Pause the execution of the current handler chain
     */
    public void pause() {
        paused = true;
    }

    public AxisOperation getAxisOperation() {
        if (DEBUG_ENABLED) {
            checkActivateWarning("getAxisOperation");
        }
        return axisOperation;
    }

    public AxisService getAxisService() {
        if (DEBUG_ENABLED) {
            checkActivateWarning("getAxisService");
        }
        return axisService;
    }

    /*
     * <P>
     * Note the service group can be set independently of the service
     * so the service might not match up with this serviceGroup
    */
    public AxisServiceGroup getAxisServiceGroup() {
        if (DEBUG_ENABLED) {
            checkActivateWarning("getAxisServiceGroup");
        }
        return axisServiceGroup;
    }

    public ConfigurationContext getConfigurationContext() {
        if (DEBUG_ENABLED) {
            checkActivateWarning("getConfigurationContext");
        }
        return configurationContext;
    }

    public int getCurrentHandlerIndex() {
        return currentHandlerIndex;
    }

    public int getCurrentPhaseIndex() {
        return currentPhaseIndex;
    }

    /**
     * @return Returns SOAPEnvelope.
     */
    public SOAPEnvelope getEnvelope() {
        return envelope;
    }

    public ArrayList<Handler> getExecutionChain() {
        if (DEBUG_ENABLED) {
            checkActivateWarning("getExecutionChain");
        }
        return executionChain;
    }

    /**
     * Add a Phase to the collection of executed phases for the path.
     * Phases will be inserted in a LIFO data structure.
     *
     * @param phase The phase to add to the list.
     */
    public void addExecutedPhase(Handler phase) {
        if (executedPhases == null) {
            executedPhases = new LinkedList<Handler>();
        }
        executedPhases.addFirst(phase);
    }

    /**
     * Remove the first Phase in the collection of executed phases
     */
    public void removeFirstExecutedPhase() {
        if (executedPhases != null) {
            executedPhases.removeFirst();
        }
    }

    /**
     * Get an iterator over the executed phase list.
     *
     * @return An Iterator over the LIFO data structure.
     */
    public Iterator<Handler> getExecutedPhases() {
        if (DEBUG_ENABLED) {
            checkActivateWarning("getExecutedPhases");
        }
        if (executedPhases == null) {
            executedPhases = new LinkedList<Handler>();
        }
        return executedPhases.iterator();
    }

    /**
     * Reset the list of executed phases.
     * This is needed because the OutInAxisOperation currently invokes
     * receive() even when a fault occurs, and we will have already executed
     * the flowComplete on those before receiveFault() is called.
     */
    public void resetExecutedPhases() {
        executedPhasesReset = true;
        executedPhases = new LinkedList<Handler>();
    }

    /**
     * @return Returns EndpointReference.
     */
    public EndpointReference getFaultTo() {
        return options.getFaultTo();
    }

    /**
     * @return Returns EndpointReference.
     */
    public EndpointReference getFrom() {
        return options.getFrom();
    }

    /**
     * @return Returns message id.
     */
    public String getMessageID() {
        return options.getMessageId();
    }

    /**
     * Retrieves both module specific configuration parameters as well as other
     * parameters. The order of search is as follows:
     * <ol>
     * <li> Search in module configurations inside corresponding operation
     * description if its there </li>
     * <li> Search in corresponding operation if its there </li>
     * <li> Search in module configurations inside corresponding service
     * description if its there </li>
     * <li> Next search in Corresponding Service description if its there </li>
     * <li> Next search in module configurations inside axisConfiguration </li>
     * <li> Search in AxisConfiguration for parameters </li>
     * <li> Next get the corresponding module and search for the parameters
     * </li>
     * <li> Search in HandlerDescription for the parameter </li>
     * </ol>
     * <p/> and the way of specifying module configuration is as follows
     * <moduleConfig name="addressing"> <parameter name="addressingPara"
     * >N/A</parameter> </moduleConfig>
     *
     * @param key        :
     *                   Parameter Name
     * @param moduleName :
     *                   Name of the module
     * @param handler    <code>HandlerDescription</code>
     * @return Parameter <code>Parameter</code>
     */
    public Parameter getModuleParameter(String key, String moduleName,
                                        HandlerDescription handler) {
        Parameter param;
        ModuleConfiguration moduleConfig;

        AxisOperation opDesc = getAxisOperation();

        if (opDesc != null) {

            moduleConfig = opDesc.getModuleConfig(moduleName);

            if (moduleConfig != null) {
                param = moduleConfig.getParameter(key);

                if (param != null) {
                    return param;
                } else {
                    param = opDesc.getParameter(key);

                    if (param != null) {
                        return param;
                    }
                }
            }
        }

        AxisService axisService = getAxisService();

        if (axisService != null) {

            moduleConfig = axisService.getModuleConfig(moduleName);

            if (moduleConfig != null) {
                param = moduleConfig.getParameter(key);

                if (param != null) {
                    return param;
                } else {
                    param = axisService.getParameter(key);

                    if (param != null) {
                        return param;
                    }
                }
            }
        }

        AxisServiceGroup axisServiceDesc = getAxisServiceGroup();

        if (axisServiceDesc != null) {

            moduleConfig = axisServiceDesc.getModuleConfig(moduleName);

            if (moduleConfig != null) {
                param = moduleConfig.getParameter(key);

                if (param != null) {
                    return param;
                } else {
                    param = axisServiceDesc.getParameter(key);

                    if (param != null) {
                        return param;
                    }
                }
            }
        }

        AxisConfiguration baseConfig = configurationContext.getAxisConfiguration();

        moduleConfig = baseConfig.getModuleConfig(moduleName);

        if (moduleConfig != null) {
            param = moduleConfig.getParameter(key);

            if (param != null) {
                return param;
            } else {
                param = baseConfig.getParameter(key);

                if (param != null) {
                    return param;
                }
            }
        }

        AxisModule module = baseConfig.getModule(moduleName);

        if (module != null) {
            param = module.getParameter(key);

            if (param != null) {
                return param;
            }
        }

        param = handler.getParameter(key);

        return param;
    }

    public OperationContext getOperationContext() {
        if (DEBUG_ENABLED) {
            checkActivateWarning("getOperationContext");
        }
        return operationContext;
    }

    /**
     * Retrieves configuration descriptor parameters at any level. The order of
     * search is as follows:
     * <ol>
     * <li> Search in message description if it exists </li>
     * <li> If parameter is not found or if axisMessage is null, search in
     * AxisOperation </li>
     * <li> If parameter is not found or if operationContext is null, search in
     * AxisService </li>
     * <li> If parameter is not found or if axisService is null, search in
     * AxisConfiguration </li>
     * </ol>
     *
     * @param key name of desired parameter
     * @return Parameter <code>Parameter</code>
     */
    public Parameter getParameter(String key) {

        if( axisMessage != null ) {
            return axisMessage.getParameter(key);
        }

        if (axisOperation != null) {
            return axisOperation.getParameter(key);
        }

        if (axisService != null) {
            return axisService.getParameter(key);
        }

        if (axisServiceGroup != null) {
            return axisServiceGroup.getParameter(key);
        }

        if (configurationContext != null) {
            AxisConfiguration baseConfig = configurationContext
                    .getAxisConfiguration();
            return baseConfig.getParameter(key);
        }
        return null;
    }

    /**
     * Retrieves a property value. The order of search is as follows: search in
     * my own map and then look at my options. Does not search up the hierarchy.
     *
     * @param name name of the property to search for
     * @return the value of the property, or null if the property is not found
     */
    public Object getLocalProperty(String name) {
        return getLocalProperty(name, true);
    }
    public Object getLocalProperty(String name, boolean searchOptions) {
        if (DEBUG_ENABLED) {
            checkActivateWarning("getProperty");
        }

        // search in my own options
        Object obj = super.getLocalProperty(name);
        if (obj != null) {
            return obj;
        }

        if (searchOptions) {
            obj = options.getProperty(name);
            if (obj != null) {
                return obj;
            }
        }

        // tough
        return null;
    }

    /**
     * Retrieves a property value. The order of search is as follows: search in
     * my own map and then look in my context hierarchy, and then in options.
     * Since its possible
     * that the entire hierarchy is not present, I will start at whatever level
     * has been set.
     *
     * @param name name of the property to search for
     * @return the value of the property, or null if the property is not found
     */
    public Object getProperty(String name) {
        if (DEBUG_ENABLED) {
            checkActivateWarning("getProperty");
        }

        // search in my own options
        Object obj = super.getProperty(name);
        if (obj != null) {
            return obj;
        }

        obj = options.getProperty(name);
        if (obj != null) {
            return obj;
        }

        // My own context hierarchy may not all be present. So look for whatever
        // nearest level is present and ask that to find the property.
        //
        // If the context is already an ancestor, it was checked during
        // the super.getProperty call.  In such cases, the second check
        // is not performed.
        if (operationContext != null) {
            if (!isAncestor(operationContext)) {
                obj = operationContext.getProperty(name);
            }
        } else if (serviceContext != null) {
            if (!isAncestor(serviceContext)) {
                obj = serviceContext.getProperty(name);
            }
        } else if (serviceGroupContext != null) {
            if (!isAncestor(serviceGroupContext)) {
                obj =  serviceGroupContext.getProperty(name);
            }
        } else if (configurationContext != null) {
            if (!isAncestor(configurationContext)) {
                obj = configurationContext.getProperty(name);
            }
        }

        return obj;
    }

    /**
     * Check if a given property is true.  Will return false if the property
     * does not exist or is not an explicit "true" value.
     *
     * @param name name of the property to check
     * @return true if the property exists and is Boolean.TRUE, "true", 1, etc.
     */
    public boolean isPropertyTrue(String name) {
        return isPropertyTrue(name, false);
    }

    /**
     * Check if a given property is true.  Will return the passed default if the property
     * does not exist.
     *
     * @param name name of the property to check
     * @param defaultVal the default value if the property doesn't exist
     * @return true if the property exists and is Boolean.TRUE, "true", 1, etc.
     */
    public boolean isPropertyTrue(String name, boolean defaultVal) {
        return JavaUtils.isTrueExplicitly(getProperty(name), defaultVal);
    }

    /**
     * Retrieves all property values. The order of search is as follows: search in
     * my own options and then look in my context hierarchy. Since its possible
     * that the entire hierarchy is not present, it will start at whatever level
     * has been set and start there.
     * The returned map is unmodifiable, so any changes to the properties have
     * to be done by calling {@link #setProperty(String,Object)}. In addition,
     * any changes to the properties are not reflected on this map.
     *
     * @return An unmodifiable map containing the combination of all available
     *         properties or an empty map.
     */
    public Map<String, Object> getProperties() {
        final Map<String, Object> resultMap = new HashMap<String, Object>();

        // My own context hierarchy may not all be present. So look for whatever
        // nearest level is present and add the properties
        // We have to access the contexts in reverse order, in order to allow
        // a nearer context to overwrite values from a more distant context
        if (configurationContext != null) {
            resultMap.putAll(configurationContext.getProperties());
        }
        if (serviceGroupContext != null) {
            resultMap.putAll(serviceGroupContext.getProperties());
        }
        if (serviceContext != null) {
            resultMap.putAll(serviceContext.getProperties());
        }
        if (operationContext != null) {
            resultMap.putAll(operationContext.getProperties());
        }
        // and now add options
        resultMap.putAll(options.getProperties());
        return Collections.unmodifiableMap(resultMap);
    }

    /**
     * @return Returns RelatesTo array.
     */
    public RelatesTo[] getRelationships() {
        return options.getRelationships();
    }

    /**
     * Get any RelatesTos of a particular type associated with this MessageContext
     * TODO: Shouldn't this return a List?
     *
     * @param type the relationship type
     * @return Returns RelatesTo.
     */
    public RelatesTo getRelatesTo(String type) {
        return options.getRelatesTo(type);
    }

    /**
     * @return Returns RelatesTo.
     */
    public RelatesTo getRelatesTo() {
        return options.getRelatesTo();
    }

    /**
     * @return Returns EndpointReference.
     */
    public EndpointReference getReplyTo() {
        return options.getReplyTo();
    }

    /**
     * @return Returns ServiceContext.
     */
    public ServiceContext getServiceContext() {
        if (DEBUG_ENABLED) {
            checkActivateWarning("getServiceContext");
        }
        return serviceContext;
    }

    /**
     * @return Returns the serviceContextID.
     */
    public String getServiceContextID() {
        return serviceContextID;
    }

    public ServiceGroupContext getServiceGroupContext() {
        if (DEBUG_ENABLED) {
            checkActivateWarning("getServiceGroupContext");
        }
        return serviceGroupContext;
    }

    public String getServiceGroupContextId() {
        return serviceGroupContextId;
    }

    /**
     * @return Returns SessionContext.
     */
    public SessionContext getSessionContext() {
        return sessionContext;
    }

    public void setSessionContext(SessionContext sessionContext) {
        this.sessionContext = sessionContext;
    }


    /**
     * @return Returns soap action.
     */
    public String getSoapAction() {
        String action = options.getAction();
        if (log.isDebugEnabled()) {
            log.debug("SoapAction is (" + action + ")");
        }
        return action;
    }

    /**
     * @return Returns EndpointReference.
     */
    public EndpointReference getTo() {
        return options.getTo();
    }

    /**
     * @return Returns TransportInDescription.
     */
    public TransportInDescription getTransportIn() {
        if (DEBUG_ENABLED) {
            checkActivateWarning("getTransportIn");
        }
        return transportIn;
    }

    /**
     * @return Returns TransportOutDescription.
     */
    public TransportOutDescription getTransportOut() {
        if (DEBUG_ENABLED) {
            checkActivateWarning("getTransportOut");
        }
        return transportOut;
    }

    public String getWSAAction() {
        String action = options.getAction();
        if (log.isDebugEnabled()) {
            log.debug("WASAction is (" + action + ")");
        }
        return action;
    }

    /**
     * @return Returns boolean.
     */
    public boolean isDoingMTOM() {
        return doingMTOM;
    }

    /**
     * @return Returns boolean.
     */
    public boolean isDoingREST() {
        return doingREST;
    }

    /**
     * @return Returns boolean.
     */
    public boolean isDoingSwA() {
        return doingSwA;
    }

    /**
     * @return Returns boolean.
     */
    public boolean isNewThreadRequired() {
        return newThreadRequired;
    }

    /**
     * @return Returns boolean.
     */
    public boolean isOutputWritten() {
        return outputWritten;
    }

    /**
     * @return Returns boolean.
     */
    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    /**
     * @return Returns boolean.
     */
    public boolean isProcessingFault() {
        return processingFault;
    }

    /**
     * @return Returns boolean.
     */
    public boolean isResponseWritten() {
        return responseWritten;
    }

    public boolean isSOAP11() {
        return isSOAP11;
    }

    /**
     * @return inbound content length of 0
     */
    public long getInboundContentLength() throws IOException {
        // If there is an attachment map, the Attachments keep track
        // of the inbound content length.
        if (attachments != null) {
//            return attachments.getContentLength();
        }

        // Otherwise the length is accumulated by the DetachableInputStream.
        DetachableInputStream dis =
            (DetachableInputStream) getProperty(Constants.DETACHABLE_INPUT_STREAM);
        if (dis != null) {
            return dis.length();
        }
        return 0;
    }
    /**
     * @return Returns boolean.
     */
    public boolean isServerSide() {
        return serverSide;
    }

    public AxisMessage getAxisMessage() {
        if (reconcileAxisMessage) {
            if (DEBUG_ENABLED && log.isWarnEnabled()) {
                log.warn(this.getLogIDString() +
                    ":getAxisMessage(): ****WARNING**** MessageContext.activate(configurationContext) needs to be invoked.");
            }
        }

        return axisMessage;
    }

    public void setAxisMessage(AxisMessage axisMessage) {
        this.axisMessage = axisMessage;
    }

    public void setAxisOperation(AxisOperation axisOperation) {
        this.axisOperation = axisOperation;
    }

    public void setAxisService(AxisService axisService) {
        this.axisService = axisService;
        if (this.axisService != null) {
            this.axisServiceGroup = axisService.getAxisServiceGroup();
        } else {
            this.axisServiceGroup = null;
        }
    }

    /*
     * note setAxisServiceGroup() does not verify that the service is associated with the service group!
     */
    public void setAxisServiceGroup(AxisServiceGroup axisServiceGroup) {
        // need to set the axis service group object to null when necessary
        // for example, when extracting the message context object from
        // the object graph
        this.axisServiceGroup = axisServiceGroup;
    }

    /**
     * @param context
     */
    public void setConfigurationContext(ConfigurationContext context) {
        configurationContext = context;
    }

    public void setCurrentHandlerIndex(int currentHandlerIndex) {
        this.currentHandlerIndex = currentHandlerIndex;
    }

    public void setCurrentPhaseIndex(int currentPhaseIndex) {
        this.currentPhaseIndex = currentPhaseIndex;
    }

    /**
     * @param b
     */
    public void setDoingMTOM(boolean b) {
        doingMTOM = b;
    }

    /**
     * @param b
     */
    public void setDoingREST(boolean b) {
        doingREST = b;
    }

    /**
     * @param b
     */
    public void setDoingSwA(boolean b) {
        doingSwA = b;
    }

    /**
     * @param envelope
     */
    public void setEnvelope(SOAPEnvelope envelope) throws AxisFault {
        this.envelope = envelope;

        if (this.envelope != null) {
            String soapNamespaceURI = envelope.getNamespace().getNamespaceURI();

            if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI
                    .equals(soapNamespaceURI)) {
                isSOAP11 = false;
            } else if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI
                    .equals(soapNamespaceURI)) {
                isSOAP11 = true;
            } else {
                throw new AxisFault(
                        "Unknown SOAP Version. Current Axis handles only SOAP 1.1 and SOAP 1.2 messages");
            }
            // Inform the listeners of an attach envelope event
            if (getAxisService() != null) {
                getAxisService().attachEnvelopeEvent(this);
            }
        }
    }

    /**
     * Set the execution chain of Handler in this MessageContext. Doing this
     * causes the current handler/phase indexes to reset to 0, since we have new
     * Handlers to execute (this usually only happens at initialization and when
     * a fault occurs).
     *
     * @param executionChain
     */
    public void setExecutionChain(ArrayList<Handler> executionChain) {
        this.executionChain = executionChain;
        currentHandlerIndex = -1;
        currentPhaseIndex = 0;
    }

    /**
     * @param reference
     */
    public void setFaultTo(EndpointReference reference) {
        options.setFaultTo(reference);
    }

    /**
     * @param reference
     */
    public void setFrom(EndpointReference reference) {
        options.setFrom(reference);
    }

    /**
     * @param messageId
     */
    public void setMessageID(String messageId) {
        options.setMessageId(messageId);
    }

    /**
     * @param b
     */
    public void setNewThreadRequired(boolean b) {
        newThreadRequired = b;
    }

    /**
     * @param context The OperationContext
     */
    public void setOperationContext(OperationContext context) {
        // allow setting the fields to null
        // useful when extracting the messge context from the object graph
        operationContext = context;

        this.setParent(operationContext);

        if (operationContext != null) {
            if (serviceContext == null) {
                setServiceContext(operationContext.getServiceContext());
            } else {
                if (operationContext.getParent() != serviceContext) {
                    throw new AxisError("ServiceContext in OperationContext does not match !");
                }
            }

            this.setAxisOperation(operationContext.getAxisOperation());
        }
    }

    /**
     * @param b
     */
    public void setOutputWritten(boolean b) {
        outputWritten = b;
    }

    /**
     * @param b
     */
    public void setProcessingFault(boolean b) {
        processingFault = b;
    }

    /**
     * Add a RelatesTo
     *
     * @param reference RelatesTo describing how we relate to another message
     */
    public void addRelatesTo(RelatesTo reference) {
        options.addRelatesTo(reference);
    }

    /**
     * Set ReplyTo destination
     *
     * @param reference the ReplyTo EPR
     */
    public void setReplyTo(EndpointReference reference) {
        options.setReplyTo(reference);
    }

    /**
     * @param b
     */
    public void setResponseWritten(boolean b) {
        responseWritten = b;
    }

    /**
     * @param b
     */
    public void setServerSide(boolean b) {
        serverSide = b;
    }

    /**
     * @param context
     */
    public void setServiceContext(ServiceContext context) {

        // allow the service context to be set to null
        // this allows the message context object to be extraced from
        // the object graph

        serviceContext = context;

        if (serviceContext != null) {
            if ((operationContext != null)
                    && (operationContext.getParent() != context)) {
                throw new AxisError("ServiceContext and OperationContext.parent do not match!");
            }
            // setting configcontext using configuration context in service context
            if (configurationContext == null) {
                // setting configcontext
                configurationContext = context.getConfigurationContext();
            }
            if (serviceGroupContext == null) {
                // setting service group context
                serviceGroupContext = context.getServiceGroupContext();
            }
            AxisService axisService = context.getAxisService();
            this.setAxisService(axisService);

            // Inform the listeners of an attach event
            if (axisService != null) {
                axisService.attachServiceContextEvent(serviceContext, this);
            }
        }
    }

    /**
     * Sets the service context id.
     *
     * @param serviceContextID
     */
    public void setServiceContextID(String serviceContextID) {
        this.serviceContextID = serviceContextID;
    }

    public void setServiceGroupContext(ServiceGroupContext serviceGroupContext) {
        // allow the service group context to be set to null
        // this allows the message context object to be extraced from
        // the object graph

        this.serviceGroupContext = serviceGroupContext;

        if (this.serviceGroupContext != null) {
            this.axisServiceGroup = serviceGroupContext.getDescription();
        }
    }

    public void setServiceGroupContextId(String serviceGroupContextId) {
        this.serviceGroupContextId = serviceGroupContextId;
    }

    /**
     * @param soapAction
     */
    public void setSoapAction(String soapAction) {
        if (log.isDebugEnabled()) {
            log.debug("Old SoapAction is (" + options.getAction() + ")");
            log.debug("New SoapAction is (" + soapAction + ")");
        }
        options.setAction(soapAction);
    }

    /**
     * @param to
     */
    public void setTo(EndpointReference to) {
        options.setTo(to);
    }

    /**
     * @param in
     */
    public void setTransportIn(TransportInDescription in) {
        this.transportIn = in;
    }

    /**
     * @param out
     */
    public void setTransportOut(TransportOutDescription out) {
        transportOut = out;
    }

    /**
     * setWSAAction
     */
    public void setWSAAction(String actionURI) {
        if (log.isDebugEnabled()) {
            log.debug("Old WSAAction is (" + options.getAction() + ")");
            log.debug("New WSAAction is (" + actionURI + ")");
        }
        options.setAction(actionURI);
    }

    public void setWSAMessageId(String messageID) {
        options.setMessageId(messageID);
    }

    // to get the flow inwhich the execution chain below
    public int getFLOW() {
        return FLOW;
    }

    public void setFLOW(int FLOW) {
        this.FLOW = FLOW;
    }

    public Options getOptions() {
        if (DEBUG_ENABLED) {
            checkActivateWarning("getOptions");
        }
        return options;
    }

    /**
     * Set the options for myself. I make the given options my own options'
     * parent so that that becomes the default. That allows the user to override
     * specific options on a given message context and not affect the overall
     * options.
     *
     * @param options the options to set
     */
    public void setOptions(Options options) {
        this.options.setParent(options);
    }

    public String getIncomingTransportName() {
        return incomingTransportName;
    }

    public void setIncomingTransportName(String incomingTransportName) {
        this.incomingTransportName = incomingTransportName;
    }

    public void setRelationships(RelatesTo[] list) {
        options.setRelationships(list);
    }


    public Policy getEffectivePolicy() {
        if (DEBUG_ENABLED) {
            checkActivateWarning("getEffectivePolicy");
        }

        AxisBindingMessage bindingMessage =
        	(AxisBindingMessage) getProperty(Constants.AXIS_BINDING_MESSAGE);

        AxisBinding binding;

        // If AxisBindingMessage is not set, try to find the binding message from the AxisService
        if (bindingMessage == null) {
        	bindingMessage = findBindingMessage();
        }

        if (bindingMessage != null) {
            return bindingMessage.getEffectivePolicy();
            // If we can't find the AxisBindingMessage, then try the AxisBinding
        } else if ((binding = findBinding()) != null) {
            return binding.getEffectivePolicy();
            // If we can't find the AxisBindingMessage, then try the AxisMessage
        } else if (axisMessage != null) {
        		return axisMessage.getEffectivePolicy();
        } else {
            if (axisService != null){
                List<PolicyComponent> policyList = new ArrayList<PolicyComponent>();
                policyList.addAll(axisService.getPolicySubject().getAttachedPolicyComponents());
                AxisConfiguration axisConfiguration = axisService.getAxisConfiguration();
                policyList.addAll(axisConfiguration.getPolicySubject().getAttachedPolicyComponents());
		        return PolicyUtil.getMergedPolicy(policyList, axisService);
            } else {
               return null;
            }
        }
    }

    private AxisBinding findBinding() {
        if (axisService != null) {
            if (axisService.getEndpointName() != null) {
                AxisEndpoint axisEndpoint = axisService
                        .getEndpoint(axisService.getEndpointName());
                if (axisEndpoint != null) {
                    return axisEndpoint.getBinding();
                }
            }
        }
        return null;
    }

    private AxisBindingMessage findBindingMessage() {
    	if (axisService != null && axisOperation != null ) {
			if (axisService.getEndpointName() != null) {
				AxisEndpoint axisEndpoint = axisService
						.getEndpoint(axisService.getEndpointName());
				if (axisEndpoint != null) {
					AxisBinding axisBinding = axisEndpoint.getBinding();
                    AxisBindingOperation axisBindingOperation = (AxisBindingOperation) axisBinding
							.getChild(axisOperation.getName());

                    //If Binding Operation is not found, just return null
                    if (axisBindingOperation == null) {
                       return null;
                    }

                    String direction = axisMessage.getDirection();
					AxisBindingMessage axisBindingMessage = null;
					if (WSDLConstants.WSDL_MESSAGE_DIRECTION_IN
							.equals(direction)
							&& WSDLUtil
									.isInputPresentForMEP(axisOperation
											.getMessageExchangePattern())) {
						axisBindingMessage = (AxisBindingMessage) axisBindingOperation
								.getChild(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
						return axisBindingMessage;

					} else if (WSDLConstants.WSDL_MESSAGE_DIRECTION_OUT
							.equals(direction)
							&& WSDLUtil
									.isOutputPresentForMEP(axisOperation
											.getMessageExchangePattern())) {
						axisBindingMessage = (AxisBindingMessage) axisBindingOperation
								.getChild(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
						return axisBindingMessage;
					}
				}

			}
		}
    	return null;
    }


    public boolean isEngaged(String moduleName) {
        if (DEBUG_ENABLED) {
            checkActivateWarning("isEngaged");
        }
        boolean isEngaged;
        if (configurationContext != null) {
            AxisConfiguration axisConfig = configurationContext.getAxisConfiguration();
            AxisModule module = axisConfig.getModule(moduleName);
            if (module == null) {
                return false;
            }
            isEngaged = axisConfig.isEngaged(module);
            if (isEngaged) {
                return true;
            }
            if (axisServiceGroup != null) {
                isEngaged = axisServiceGroup.isEngaged(module);
                if (isEngaged) {
                    return true;
                }
            }
            if (axisService != null) {
                isEngaged = axisService.isEngaged(module);
                if (isEngaged) {
                    return true;
                }
            }
            if (axisOperation != null) {
                isEngaged = axisOperation.isEngaged(module);
                if (isEngaged) {
                    return true;
                }
            }
            if (axisMessage != null) {
                isEngaged = axisMessage.isEngaged(module);
                if (isEngaged) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets the first child of the envelope, check if it is a soap:Body, which means there is no header.
     * We do this basically to make sure we don't parse and build the om tree of the whole envelope
     * looking for the soap header. If this method returns true, there still is no guarantee that there is
     * a soap:Header present, use getHeader() and also check for null on getHeader() to be absolutely sure.
     *
     * @return boolean
     * @deprecated The bonus you used to get from this is now built in to SOAPEnvelope.getHeader()
     */
    public boolean isHeaderPresent() {
        // If there's no envelope there can't be a header.
        if (this.envelope == null) {
            return false;
        }
        return (this.envelope.getHeader() != null);
    }

    /**
     * Setting of the attachments map should be performed at the receipt of a
     * message only. This method is only meant to be used by the Axis2
     * internals.
     *
     * @param attachments
     */
    public void setAttachmentMap(Attachments attachments) {
        this.attachments = attachments;
    }

    /**
     * You can directly access the attachment map of the message context from
     * here. Returned attachment map can be empty.
     *
     * @return attachment
     */
    public Attachments getAttachmentMap() {
        if (attachments == null) {
            attachments = new Attachments();
        }
        return attachments;
    }

    /**
     * Adds an attachment to the attachment Map of this message context. This
     * attachment gets serialised as a MIME attachment when sending the message
     * if SOAP with Attachments is enabled.
     *
     * @param contentID   :
     *                    will be the content ID of the MIME part (without the "cid:" prefix)
     * @param dataHandler
     */
    public void addAttachment(String contentID, DataHandler dataHandler) {
        if (attachments == null) {
            attachments = new Attachments();
        }
        attachments.addDataHandler(contentID, dataHandler);
    }

    /**
     * Adds an attachment to the attachment Map of this message context. This
     * attachment gets serialised as a MIME attachment when sending the message
     * if SOAP with Attachments is enabled. Content ID of the MIME part will be
     * auto generated by Axis2.
     *
     * @param dataHandler
     * @return the auto generated content ID of the MIME attachment
     */
    public String addAttachment(DataHandler dataHandler) {
        String contentID = UIDGenerator.generateContentId();
        addAttachment(contentID, dataHandler);
        return contentID;
    }

    /**
     * Access the DataHandler of the attachment contained in the map corresponding to the given
     * content ID. Returns "NULL" if a attachment cannot be found by the given content ID.
     *
     * @param contentID :
     *                  Content ID of the MIME attachment (without the "cid:" prefix)
     * @return Data handler of the attachment
     */
    public DataHandler getAttachment(String contentID) {
        if (attachments == null) {
            attachments = new Attachments();
        }
        return attachments.getDataHandler(contentID);
    }

    /**
     * Removes the attachment with the given content ID from the Attachments Map
     * Do nothing if a attachment cannot be found by the given content ID.
     *
     * @param contentID of the attachment (without the "cid:" prefix)
     */
    public void removeAttachment(String contentID) {
        if (attachments != null) {
            attachments.removeDataHandler(contentID);
        }
    }

    /*
     * ===============================================================
     * SelfManagedData Section
     * ===============================================================
     */

    /*
    * character to delimit strings
    */
    private String selfManagedDataDelimiter = "*";


    /**
     * Set up a unique key in the form of
     * <OL>
     * <LI>the class name for the class that owns the key
     * <LI>delimitor
     * <LI>the key as a string
     * <LI>delimitor
     * <LI>the key's hash code as a string
     * </OL>
     *
     * @param clazz The class that owns the supplied key
     * @param key   The key
     * @return A string key
     */
    private String generateSelfManagedDataKey(Class clazz, Object key) {
        return clazz.getName() + selfManagedDataDelimiter + key.toString() +
                selfManagedDataDelimiter + Integer.toString(key.hashCode());
    }

    /**
     * Add a key-value pair of self managed data to the set associated with
     * this message context.
     * <p/>
     * This is primarily intended to allow handlers to manage their own
     * message-specific data when the message context is saved/restored.
     *
     * @param clazz The class of the caller that owns the key-value pair
     * @param key   The key for this data object
     * @param value The data object
     */
    public void setSelfManagedData(Class clazz, Object key, Object value) {
        if (selfManagedDataMap == null) {
            selfManagedDataMap = new LinkedHashMap<String, Object>();
        }

        // make sure we have a unique key and a delimiter so we can
        // get the classname and hashcode for serialization/deserialization
        selfManagedDataMap.put(generateSelfManagedDataKey(clazz, key), value);
    }

    /**
     * Retrieve a value of self managed data previously saved with the specified key.
     *
     * @param clazz The class of the caller that owns the key-value pair
     * @param key   The key for the data
     * @return The data object associated with the key, or NULL if not found
     */
    public Object getSelfManagedData(Class clazz, Object key) {
        if (selfManagedDataMap != null) {
            return selfManagedDataMap.get(generateSelfManagedDataKey(clazz, key));
        }
        return null;
    }

    /**
     * Check to see if the key for the self managed data is available
     *
     * @param clazz The class of the caller that owns the key-value pair
     * @param key   The key to look for
     * @return TRUE if the key exists, FALSE otherwise
     */
    public boolean containsSelfManagedDataKey(Class clazz, Object key) {
        if (selfManagedDataMap == null) {
            return false;
        }
        return selfManagedDataMap.containsKey(generateSelfManagedDataKey(clazz, key));
    }

    /**
     * Removes the mapping of the specified key if the specified key
     * has been set for self managed data
     *
     * @param clazz The class of the caller that owns the key-value pair
     * @param key   The key of the object to be removed
     */
    public void removeSelfManagedData(Class clazz, Object key) {
        if (selfManagedDataMap != null) {
            selfManagedDataMap.remove(generateSelfManagedDataKey(clazz, key));
        }
    }

    /**
     * Flatten the phase list into a list of just unique handler instances
     *
     * @param list the list of handlers
     * @param map  users should pass null as this is just a holder for the recursion
     * @return a list of unigue object instances
     */
    private ArrayList<Handler> flattenPhaseListToHandlers(ArrayList<Handler> list, LinkedHashMap<String, Handler> map) {

        if (map == null) {
            map = new LinkedHashMap<String, Handler>();
        }

        Iterator<Handler> it = list.iterator();
        while (it.hasNext()) {
            Handler handler = (Handler) it.next();

            String key = null;
            if (handler != null) {
                key = handler.getClass().getName() + "@" + handler.hashCode();
            }

            if (handler instanceof Phase) {
                // add its handlers to the list
                flattenHandlerList(((Phase) handler).getHandlers(), map);
            } else {
                // if the same object is already in the list,
                // then it won't be in the list multiple times
                map.put(key, handler);
            }
        }

        if (DEBUG_ENABLED && log.isTraceEnabled()) {
            Iterator<String> it2 = map.keySet().iterator();
            while (it2.hasNext()) {
                Object key = it2.next();
                Handler value = (Handler) map.get(key);
                String name = value.getName();
                log.trace(getLogIDString() + ":flattenPhaseListToHandlers():  key [" + key +
                        "]    handler name [" + name + "]");
            }
        }


        return new ArrayList<Handler>(map.values());
    }


    /**
     * Flatten the handler list into just unique handler instances
     * including phase instances.
     *
     * @param list the list of handlers/phases
     * @param map  users should pass null as this is just a holder for the recursion
     * @return a list of unigue object instances
     */
    private ArrayList<Handler> flattenHandlerList(List<Handler> list, LinkedHashMap<String, Handler> map) {

        if (map == null) {
            map = new LinkedHashMap<String, Handler>();
        }

        Iterator<Handler> it = list.iterator();
        while (it.hasNext()) {
            Handler handler = (Handler) it.next();

            String key = null;
            if (handler != null) {
                key = handler.getClass().getName() + "@" + handler.hashCode();
            }

            if (handler instanceof Phase) {
                // put the phase in the list
                map.put(key, handler);

                // add its handlers to the list
                flattenHandlerList(((Phase) handler).getHandlers(), map);
            } else {
                // if the same object is already in the list,
                // then it won't be in the list multiple times
                map.put(key, handler);
            }
        }

        return new ArrayList<Handler>(map.values());
    }


    /**
     * Calls the serializeSelfManagedData() method of each handler that
     * implements the <bold>SelfManagedDataManager</bold> interface.
     * Handlers for this message context are identified via the
     * executionChain list.
     *
     * @param out The output stream
     */
    private void serializeSelfManagedData(ObjectOutput out) {
        selfManagedDataHandlerCount = 0;

        try {
            if ((selfManagedDataMap == null)
                    || (executionChain == null)
                    || (selfManagedDataMap.size() == 0)
                    || (executionChain.size() == 0)) {
                out.writeBoolean(ExternalizeConstants.EMPTY_OBJECT);

                if (DEBUG_ENABLED && log.isTraceEnabled()) {
                    log.trace(getLogIDString() + ":serializeSelfManagedData(): No data : END");
                }

                return;
            }

            // let's create a temporary list with the handlers
            ArrayList<Handler> flatExecChain = flattenPhaseListToHandlers(executionChain, null);

            //ArrayList selfManagedDataHolderList = serializeSelfManagedDataHelper(flatExecChain.iterator(), new ArrayList());
            ArrayList<SelfManagedDataHolder> selfManagedDataHolderList = serializeSelfManagedDataHelper(flatExecChain);

            if (selfManagedDataHolderList.size() == 0) {
                out.writeBoolean(ExternalizeConstants.EMPTY_OBJECT);

                if (DEBUG_ENABLED && log.isTraceEnabled()) {
                    log.trace(getLogIDString() + ":serializeSelfManagedData(): No data : END");
                }

                return;
            }

            out.writeBoolean(ExternalizeConstants.ACTIVE_OBJECT);

            // SelfManagedData can be binary so won't be able to treat it as a
            // string - need to treat it as a byte []

            // how many handlers actually
            // returned serialized SelfManagedData
            out.writeInt(selfManagedDataHolderList.size());

            for (int i = 0; i < selfManagedDataHolderList.size(); i++) {
                out.writeObject(selfManagedDataHolderList.get(i));
            }

        }
        catch (IOException e) {
            if (DEBUG_ENABLED && log.isTraceEnabled()) {
                log.trace("MessageContext:serializeSelfManagedData(): Exception [" +
                    e.getClass().getName() + "]  description [" + e.getMessage() + "]", e);
            }
        }

    }


    /**
     * This is the helper method to do the recursion for serializeSelfManagedData()
     *
     * @param handlers
     * @return ArrayList
     */
    private ArrayList<SelfManagedDataHolder> serializeSelfManagedDataHelper(ArrayList<Handler> handlers) {
        ArrayList<SelfManagedDataHolder> selfManagedDataHolderList = new ArrayList<SelfManagedDataHolder>();
        Iterator<Handler> it = handlers.iterator();

        try {
            while (it.hasNext()) {
                Handler handler = (Handler) it.next();

                //if (handler instanceof Phase)
                //{
                //    selfManagedDataHolderList = serializeSelfManagedDataHelper(((Phase)handler).getHandlers().iterator(), selfManagedDataHolderList);
                //}
                //else if (SelfManagedDataManager.class.isAssignableFrom(handler.getClass()))
                if (SelfManagedDataManager.class.isAssignableFrom(handler.getClass())) {
                    // only call the handler's serializeSelfManagedData if it implements SelfManagedDataManager

                    if (DEBUG_ENABLED && log.isTraceEnabled()) {
                        log.trace(
                                "MessageContext:serializeSelfManagedDataHelper(): calling handler  [" +
                                        handler.getClass().getName() + "]  name [" +
                                        handler.getName() + "]   serializeSelfManagedData method");
                    }

                    ByteArrayOutputStream baos_fromHandler =
                            ((SelfManagedDataManager) handler).serializeSelfManagedData(this);

                    if (baos_fromHandler != null) {
                        baos_fromHandler.close();

                        try {
                            SelfManagedDataHolder selfManagedDataHolder = new SelfManagedDataHolder(
                                    handler.getClass().getName(), handler.getName(),
                                    baos_fromHandler.toByteArray());
                            selfManagedDataHolderList.add(selfManagedDataHolder);
                            selfManagedDataHandlerCount++;
                        }
                        catch (Exception exc) {
                            if (DEBUG_ENABLED && log.isTraceEnabled()) {
                                log.trace("MessageContext:serializeSelfManagedData(): exception [" +
                                    exc.getClass().getName() + "][" + exc.getMessage() +
                                    "]  in setting up SelfManagedDataHolder object for [" +
                                    handler.getClass().getName() + " / " + handler.getName() + "] ",
                                      exc);
                            }
                        }
                    }
                }
            }

            return selfManagedDataHolderList;
        }
        catch (Exception ex) {
            if (DEBUG_ENABLED && log.isTraceEnabled()) {
                log.trace("MessageContext:serializeSelfManagedData(): exception [" +
                    ex.getClass().getName() + "][" + ex.getMessage() + "]", ex);
            }
            return null;
        }

    }

    /**
     * During deserialization, the executionChain will be
     * re-constituted before the SelfManagedData is restored.
     * This means the handler instances are already available.
     * This method lets us find the handler instance from the
     * executionChain so we can call each one's
     * deserializeSelfManagedData method.
     *
     * @param it            The iterator from the executionChain object
     * @param classname     The class name
     * @param qNameAsString The QName in string form
     * @return SelfManagedDataManager handler
     */
    private SelfManagedDataManager deserialize_getHandlerFromExecutionChain(Iterator<Handler> it,
                                                                            String classname,
                                                                            String qNameAsString) {
        SelfManagedDataManager handler_toreturn = null;

        try {
            while ((it.hasNext()) && (handler_toreturn == null)) {
                Handler handler = (Handler) it.next();

                if (handler instanceof Phase) {
                    handler_toreturn = deserialize_getHandlerFromExecutionChain(
                            ((Phase) handler).getHandlers().iterator(), classname, qNameAsString);
                } else if ((handler.getClass().getName().equals(classname))
                        && (handler.getName().equals(qNameAsString))) {
                    handler_toreturn = (SelfManagedDataManager) handler;
                }
            }
            return handler_toreturn;
        }
        catch (ClassCastException e) {
            // Doesn't seem likely to happen, but just in case...
            // A handler classname in the executionChain matched up with our parameter
            // classname, but the existing class in the executionChain is a different
            // implementation than the one we saved during serializeSelfManagedData.
            // NOTE: the exception gets absorbed!

            if (DEBUG_ENABLED && log.isTraceEnabled()) {
                log.trace(
                    "MessageContext:deserialize_getHandlerFromExecutionChain(): ClassCastException thrown: " +
                            e.getMessage(), e);
            }
            return null;
        }
    }


    /*
    * We don't need to create new instances of the handlers
    * since the executionChain is rebuilt after readExternal().
    * We just have to find them in the executionChain and
    * call each handler's deserializeSelfManagedData method.
    */
    private void deserializeSelfManagedData() throws IOException {
        try {
            for (int i = 0;
                 (selfManagedDataListHolder != null) && (i < selfManagedDataListHolder.size()); i++)
            {
                SelfManagedDataHolder selfManagedDataHolder =
                        (SelfManagedDataHolder) selfManagedDataListHolder.get(i);

                String classname = selfManagedDataHolder.getClassname();
                String qNameAsString = selfManagedDataHolder.getId();

                SelfManagedDataManager handler = deserialize_getHandlerFromExecutionChain(
                        executionChain.iterator(), classname, qNameAsString);

                if (handler == null) {
                    if (DEBUG_ENABLED && log.isTraceEnabled()) {
                        log.trace(getLogIDString() + ":deserializeSelfManagedData():  [" +
                                classname +
                                "]  was not found in the executionChain associated with the message context.");
                    }

                    throw new IOException("The class [" + classname +
                            "] was not found in the executionChain associated with the message context.");
                }

                ByteArrayInputStream handlerData =
                        new ByteArrayInputStream(selfManagedDataHolder.getData());

                // the handler implementing SelfManagedDataManager is responsible for repopulating
                // the SelfManagedData in the MessageContext (this)

                if (DEBUG_ENABLED && log.isTraceEnabled()) {
                    log.trace(getLogIDString() +
                            ":deserializeSelfManagedData(): calling handler [" + classname + "] [" +
                            qNameAsString + "]  deserializeSelfManagedData method");
                }

                handler.deserializeSelfManagedData(handlerData, this);
                handler.restoreTransientData(this);
            }
        }
        catch (IOException ioe) {
            if (DEBUG_ENABLED && log.isTraceEnabled()) {
                log.trace(getLogIDString() + ":deserializeSelfManagedData(): IOException thrown: " +
                        ioe.getMessage(), ioe);
            }
            throw ioe;
        }

    }

    /* ===============================================================
    * Externalizable support
    * ===============================================================
    */


    /**
     * Save the contents of this MessageContext instance.
     * <p/>
     * NOTE: Transient fields and static fields are not saved.
     * Also, objects that represent "static" data are
     * not saved, except for enough information to be
     * able to find matching objects when the message
     * context is re-constituted.
     *
     * @param o The stream to write the object contents to
     * @throws IOException
     */
    public void writeExternal(ObjectOutput o) throws IOException {
        SafeObjectOutputStream out = SafeObjectOutputStream.install(o);
        String logCorrelationIDString = getLogIDString();

        if (DEBUG_ENABLED && log.isTraceEnabled()) {
            log.trace(logCorrelationIDString + ":writeExternal(): writing to output stream");
        }

        //---------------------------------------------------------
        // in order to handle future changes to the message
        // context definition, be sure to maintain the
        // object level identifiers
        //---------------------------------------------------------
        // serialization version ID
        out.writeLong(serialVersionUID);

        // revision ID
        out.writeInt(revisionID);

        //---------------------------------------------------------
        // various simple fields
        //---------------------------------------------------------

        // the type of execution flow for the message context
        out.writeInt(FLOW);

        // various flags
        out.writeBoolean(processingFault);
        out.writeBoolean(paused);
        out.writeBoolean(outputWritten);
        out.writeBoolean(newThreadRequired);
        out.writeBoolean(isSOAP11);
        out.writeBoolean(doingREST);
        out.writeBoolean(doingMTOM);
        out.writeBoolean(doingSwA);
        out.writeBoolean(responseWritten);
        out.writeBoolean(serverSide);

        out.writeLong(getLastTouchedTime());
        out.writeObject(getLogCorrelationID());

        //-----------------------------------------------------------------------
        // Create and initialize the OMOutputFormat for Message Externalization
        //-----------------------------------------------------------------------

        OMOutputFormat outputFormat= new OMOutputFormat();
        outputFormat.setSOAP11(isSOAP11);
        boolean persistOptimized = getPersistOptimized();
        if (persistOptimized) {
            outputFormat.setDoOptimize(true);
        }
        String charSetEnc = (String) getProperty(MessageContext.CHARACTER_SET_ENCODING);
        if (charSetEnc == null) {
            OperationContext opContext = getOperationContext();
            if (opContext != null) {
                charSetEnc =
                        (String) opContext.getProperty(MessageContext.CHARACTER_SET_ENCODING);
            }
        }
        if (charSetEnc == null) {
            charSetEnc = MessageContext.DEFAULT_CHAR_SET_ENCODING;
        }
        outputFormat.setCharSetEncoding(charSetEnc);

        // ----------------------------------------------------------
        // Externalize the Message
        // ----------------------------------------------------------
        MessageExternalizeUtils.writeExternal(out, this, logCorrelationIDString, outputFormat);

        // ---------------------------------------------------------
        // ArrayList executionChain
        //     handler and phase related data
        //---------------------------------------------------------
        // The strategy is to save some metadata about each
        // member of the list and the order of the list.
        // Then when the message context is re-constituted,
        // try to match up with phases and handlers on the
        // engine.
        //
        // Non-null list:
        //    UTF          - description string
        //    boolean      - active flag
        //    int          - current handler index
        //    int          - current phase index
        //    int          - expected number of entries in the list
        //    objects      - MetaDataEntry object per list entry
        //                        last entry will be empty MetaDataEntry
        //                        with MetaDataEntry.LAST_ENTRY marker
        //    int          - adjusted number of entries in the list
        //                        includes the last empty entry
        //
        // Empty list:
        //    UTF          - description string
        //    boolean      - empty flag
        //---------------------------------------------------------
        out.writeUTF("executionChain");
        if (executionChain != null && executionChain.size() > 0) {
            // start writing data to the output stream
            out.writeBoolean(ExternalizeConstants.ACTIVE_OBJECT);
            out.writeInt(currentHandlerIndex);
            out.writeInt(currentPhaseIndex);
            out.writeInt(executionChain.size());

            // put the metadata on each member of the list into a buffer

            // match the current index with the actual saved list
            int nextIndex = 0;

            Iterator<Handler> i = executionChain.iterator();

            while (i.hasNext()) {
                Object obj = i.next();
                String objClass = obj.getClass().getName();
                // start the meta data entry for this object
                MetaDataEntry mdEntry = new MetaDataEntry();
                mdEntry.setClassName(objClass);

                // get the correct object-specific name
                String qnameAsString;

                if (obj instanceof Phase) {
                    Phase phaseObj = (Phase) obj;
                    qnameAsString = phaseObj.getName();

                    // add the list of handlers to the meta data
                    setupPhaseList(phaseObj, mdEntry);
                } else if (obj instanceof Handler) {
                    Handler handlerObj = (Handler) obj;
                    qnameAsString = handlerObj.getName();
                } else {
                    // TODO: will there be any other kinds of objects in the execution Chain?
                    qnameAsString = "NULL";
                }

                mdEntry.setQName(qnameAsString);

                // update the index for the entry in the chain

                if (DEBUG_ENABLED && log.isTraceEnabled()) {
                    log.trace(logCorrelationIDString +
                            ":writeExternal(): ***BEFORE OBJ WRITE*** executionChain entry class [" +
                            objClass + "] qname [" + qnameAsString + "]");
                }

                out.writeObject(mdEntry);

                // update the index so that the index
                // now indicates the next entry that
                // will be attempted
                nextIndex++;

                if (DEBUG_ENABLED && log.isTraceEnabled()) {
                    log.trace(logCorrelationIDString +
                            ":writeExternal(): ***AFTER OBJ WRITE*** executionChain entry class [" +
                            objClass + "] qname [" + qnameAsString + "]");
                }

            } // end while entries in execution chain

            // done with the entries in the execution chain
            // add the end-of-list marker
            MetaDataEntry lastEntry = new MetaDataEntry();
            lastEntry.setClassName(MetaDataEntry.END_OF_LIST);

            out.writeObject(lastEntry);
            nextIndex++;

            // nextIndex also gives us the number of entries
            // that were actually saved as opposed to the
            // number of entries in the executionChain
            out.writeInt(nextIndex);

        } else {
            // general case: handle "null" or "empty"
            out.writeBoolean(ExternalizeConstants.EMPTY_OBJECT);

            if (DEBUG_ENABLED && log.isTraceEnabled()) {
                log.trace(logCorrelationIDString + ":writeExternal(): executionChain is NULL");
            }
        }

        //---------------------------------------------------------
        // LinkedList executedPhases
        //---------------------------------------------------------
        // The strategy is to save some metadata about each
        // member of the list and the order of the list.
        // Then when the message context is re-constituted,
        // try to match up with phases and handlers on the
        // engine.
        //
        // Non-null list:
        //    UTF          - description string
        //    boolean      - active flag
        //    int          - expected number of entries in the list
        //    objects      - MetaDataEntry object per list entry
        //                        last entry will be empty MetaDataEntry
        //                        with MetaDataEntry.LAST_ENTRY marker
        //    int          - adjusted number of entries in the list
        //                        includes the last empty entry
        //
        // Empty list:
        //    UTF          - description string
        //    boolean      - empty flag
        //---------------------------------------------------------
        out.writeUTF("executedPhases");
        if (executedPhases != null && executedPhases.size() > 0) {

            // start writing data to the output stream
            out.writeBoolean(ExternalizeConstants.ACTIVE_OBJECT);
            out.writeInt(executedPhases.size());

            // put the metadata on each member of the list into a buffer

            int execNextIndex = 0;

            Iterator<Handler> iterator = executedPhases.iterator();

            while (iterator.hasNext()) {
                Object obj = iterator.next();
                String objClass = obj.getClass().getName();
                // start the meta data entry for this object
                MetaDataEntry mdEntry = new MetaDataEntry();
                mdEntry.setClassName(objClass);

                // get the correct object-specific name
                String qnameAsString;

                if (obj instanceof Phase) {
                    Phase inPhaseObj = (Phase) obj;
                    qnameAsString = inPhaseObj.getName();

                    // add the list of handlers to the meta data
                    setupPhaseList(inPhaseObj, mdEntry);
                } else if (obj instanceof Handler) {
                    Handler inHandlerObj = (Handler) obj;
                    qnameAsString = inHandlerObj.getName();
                } else {
                    // TODO: will there be any other kinds of objects in the list
                    qnameAsString = "NULL";
                }

                mdEntry.setQName(qnameAsString);

                if (DEBUG_ENABLED && log.isTraceEnabled()) {
                    log.trace(logCorrelationIDString +
                            ":writeExternal(): ***BEFORE Executed List OBJ WRITE*** executedPhases entry class [" +
                            objClass + "] qname [" + qnameAsString + "]");
                }

                out.writeObject(mdEntry);

                // update the index so that the index
                // now indicates the next entry that
                // will be attempted
                execNextIndex++;

                if (DEBUG_ENABLED && log.isTraceEnabled()) {
                    log.trace(logCorrelationIDString + ":writeExternal(): " +
                            "***AFTER Executed List OBJ WRITE*** " +
                            "executedPhases entry class [" + objClass + "] " +
                            "qname [" + qnameAsString + "]");
                }
            } // end while entries in execution chain

            // done with the entries in the execution chain
            // add the end-of-list marker
            MetaDataEntry lastEntry = new MetaDataEntry();
            lastEntry.setClassName(MetaDataEntry.END_OF_LIST);

            out.writeObject(lastEntry);
            execNextIndex++;

            // execNextIndex also gives us the number of entries
            // that were actually saved as opposed to the
            // number of entries in the executedPhases
            out.writeInt(execNextIndex);

        } else {
            // general case: handle "null" or "empty"
            out.writeBoolean(ExternalizeConstants.EMPTY_OBJECT);

            if (DEBUG_ENABLED && log.isTraceEnabled()) {
                log.trace(
                        logCorrelationIDString + ":writeExternal(): executedPhases is NULL");
            }
        }

        //---------------------------------------------------------
        // options
        //---------------------------------------------------------
        // before saving the Options, make sure there is a message ID
        String tmpID = getMessageID();
        if (tmpID == null) {
            // get an id to use when restoring this object
            tmpID = UIDGenerator.generateUID();
            setMessageID(tmpID);
        }

        if (DEBUG_ENABLED && log.isTraceEnabled()) {
            log.trace(logCorrelationIDString + ":writeExternal():   message ID [" + tmpID + "]");
        }

        out.writeUTF("options");
        out.writeObject(options);

        //---------------------------------------------------------
        // operation
        //---------------------------------------------------------
        // axis operation
        //---------------------------------------------------------
        out.writeUTF("axisOperation");
        metaAxisOperation = null;
        if (axisOperation != null) {
            // TODO: may need to include the meta data for the axis service that is
            //       the parent of the axis operation
            // make sure the axis operation has a name associated with it
            QName aoTmpQName = axisOperation.getName();

            if (aoTmpQName == null) {
                aoTmpQName = new QName(ExternalizeConstants.EMPTY_MARKER);
                axisOperation.setName(aoTmpQName);
            }

            metaAxisOperation = new MetaDataEntry(axisOperation.getClass().getName(),
                                                  axisOperation.getName().toString());
        }
        out.writeObject(metaAxisOperation);

        //---------------------------------------------------------
        // operation context
        //---------------------------------------------------------
        // The OperationContext has pointers to MessageContext objects.
        // In order to avoid having multiple copies of the object graph
        // being saved at different points in the serialization,
        // it is important to isolate this message context object.
        out.writeUTF("operationContext");
        if (operationContext != null) {
            operationContext.isolateMessageContext(this);
        }

        out.writeObject(operationContext);


        //---------------------------------------------------------
        // service
        //---------------------------------------------------------
        // axis service
        //-------------------------
        // this is expected to be the parent of the axis operation object
        out.writeUTF("axisService");
        metaAxisService = null;
        if (axisService != null) {
            String serviceAndPortNames = ActivateUtils.getAxisServiceExternalizeExtraName(axisService);
            // If there is a service & port QName stored on the AxisService then write it out so 
            // it can be used during deserialization to hook up the message context to the 
            // correct AxisService.
            metaAxisService = new MetaDataEntry(axisService.getClass().getName(), 
                    axisService.getName(), serviceAndPortNames);
        }
        out.writeObject(metaAxisService);

        //-------------------------
        // serviceContextID string
        //-------------------------
        out.writeObject(serviceContextID);

        //-------------------------
        // serviceContext
        //-------------------------
        // is this the same as the parent of the OperationContext?
        boolean isParent = false;
        out.writeUTF("serviceContext");

        if (operationContext != null) {
            ServiceContext opctxParent = operationContext.getServiceContext();

            if (serviceContext != null) {
                if (serviceContext.equals(opctxParent)) {
                    // the ServiceContext is the parent of the OperationContext
                    isParent = true;
                }
            }
        }

        if (serviceContext == null) {
            out.writeBoolean(ExternalizeConstants.EMPTY_OBJECT);
        } else {
            out.writeBoolean(ExternalizeConstants.ACTIVE_OBJECT);
            out.writeBoolean(isParent);

            // only write out the object if it is not the parent
            if (!isParent) {
                out.writeObject(serviceContext);
            }
        }

        //---------------------------------------------------------
        // axisServiceGroup
        //---------------------------------------------------------
        out.writeUTF("axisServiceGroup");
        metaAxisServiceGroup = null;
        if (axisServiceGroup != null) {
            metaAxisServiceGroup = new MetaDataEntry(axisServiceGroup.getClass().getName(),
                                                     axisServiceGroup.getServiceGroupName());
        }
        out.writeObject(metaAxisServiceGroup);

        //-----------------------------
        // serviceGroupContextId string
        //-----------------------------
        out.writeObject(serviceGroupContextId);

        //-------------------------
        // serviceGroupContext
        //-------------------------
        // is this the same as the parent of the ServiceContext?
        isParent = false;
        out.writeUTF("serviceGroupContext");

        if (serviceContext != null) {
            ServiceGroupContext srvgrpctxParent = (ServiceGroupContext) serviceContext.getParent();

            if (serviceGroupContext != null) {
                if (serviceGroupContext.equals(srvgrpctxParent)) {
                    // the ServiceGroupContext is the parent of the ServiceContext
                    isParent = true;
                }
            }
        }

        if (serviceGroupContext == null) {
            out.writeBoolean(ExternalizeConstants.EMPTY_OBJECT);
        } else {
            out.writeBoolean(ExternalizeConstants.ACTIVE_OBJECT);
            out.writeBoolean(isParent);

            // only write out the object if it is not the parent
            if (!isParent) {
                out.writeObject(serviceGroupContext);
            }
        }

        //---------------------------------------------------------
        // axis message
        //---------------------------------------------------------
        out.writeUTF("axisMessage");
        metaAxisMessage = null;
        if (axisMessage != null) {
            // This AxisMessage is expected to belong to the AxisOperation
            // that has already been recorded for this MessageContext.
            // If an AxisMessage associated with this Messagecontext is
            // associated with a different AxisOperation, then more
            // meta information would need to be saved

            // make sure the axis message has a name associated with it
            String amTmpName = axisMessage.getName();

            if (amTmpName == null) {
                amTmpName = ExternalizeConstants.EMPTY_MARKER;
                axisMessage.setName(amTmpName);
            }

            // get the element name if there is one
            QName amTmpElementQName = axisMessage.getElementQName();
            String amTmpElemQNameString = null;

            if (amTmpElementQName != null) {
                amTmpElemQNameString = amTmpElementQName.toString();
            }

            metaAxisMessage = new MetaDataEntry(axisMessage.getClass().getName(),
                                                axisMessage.getName(), amTmpElemQNameString);

        }
        out.writeObject(metaAxisMessage);

        //---------------------------------------------------------
        // configuration context
        //---------------------------------------------------------

        // NOTE: Currently, there does not seem to be any
        //       runtime data important to this message context
        //       in the configuration context.
        //       if so, then need to save that runtime data and reconcile
        //       it with the configuration context on the system when
        //       this message context object is restored

        //---------------------------------------------------------
        // session context
        //---------------------------------------------------------
        out.writeObject(sessionContext);

        //---------------------------------------------------------
        // transport
        //---------------------------------------------------------

        //------------------------------
        // incomingTransportName string
        //------------------------------
        out.writeObject(incomingTransportName);

        // TransportInDescription transportIn
        metaTransportIn = null;
        if (transportIn != null) {
            metaTransportIn = new MetaDataEntry(null, transportIn.getName());
        }
        out.writeObject(metaTransportIn);

        // TransportOutDescription transportOut
        metaTransportOut = null;
        if (transportOut != null) {
            metaTransportOut = new MetaDataEntry(null, transportOut.getName());
        }
        out.writeObject(metaTransportOut);


        //---------------------------------------------------------
        // properties
        //---------------------------------------------------------
        // Write out the local properties on the MessageContext
        // Don't write out the properties from other hierarchical layers.
        // (i.e. don't use getProperties())
        out.writeUTF("properties"); // write marker
        out.writeMap(properties);

        //---------------------------------------------------------
        // special data
        //---------------------------------------------------------
        out.writeUTF("selfManagedData");
        serializeSelfManagedData(out);

        //---------------------------------------------------------
        // done
        //---------------------------------------------------------

        if (DEBUG_ENABLED && log.isTraceEnabled()) {
            log.trace(logCorrelationIDString +
                    ":writeExternal(): completed writing to output stream for " +
                    logCorrelationIDString);
        }

    }

    /**
     * @return true if the data should be persisted as optimized attachments
     */
    private boolean getPersistOptimized() {
        boolean persistOptimized = false;
        if (attachments != null && attachments.getContentIDList().size() > 1) {
            persistOptimized = true;
            if (DEBUG_ENABLED && log.isTraceEnabled())
                log.trace(getLogIDString()
                        + ":getPersistOptimized(): attachments present; persist optimized");
        }
        if (!persistOptimized) {
            Object property = getProperty(Configuration.ENABLE_MTOM);
            if (property != null && JavaUtils.isTrueExplicitly(property)) {
                persistOptimized = true;
                if (DEBUG_ENABLED && log.isTraceEnabled())
                    log.trace(getLogIDString()
                            + ":getPersistOptimized(): ENBABLE_MTOM is set; persist optimized");
            }
        }
        if (!persistOptimized) {
            Object property = getProperty(Configuration.ENABLE_SWA);
            if (property != null && JavaUtils.isTrueExplicitly(property)) {
                persistOptimized = true;
                if (DEBUG_ENABLED && log.isTraceEnabled())
                    log.trace(getLogIDString()
                            + ":getPersistOptimized(): ENBABLE_SWA is set; persist optimized");
            }
        }
        if (!persistOptimized && DEBUG_ENABLED && log.isTraceEnabled())
            log.trace(getLogIDString()
                    + ":getPersistOptimized(): No attachments or attachment settings; persist non-optimized");
        return persistOptimized;
    }


    /**
     * Restore the contents of the MessageContext that was
     * previously saved.
     * <p/>
     * NOTE: The field data must read back in the same order and type
     * as it was written.  Some data will need to be validated when
     * resurrected.
     *
     * @param inObject The stream to read the object contents from
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void readExternal(ObjectInput inObject) throws IOException, ClassNotFoundException {
        SafeObjectInputStream in = SafeObjectInputStream.install(inObject);
        // set the flag to indicate that the message context is being
        // reconstituted and will need to have certain object references
        // to be reconciled with the current engine setup
        needsToBeReconciled = true;

        // trace point
        if (DEBUG_ENABLED && log.isTraceEnabled()) {
            log.trace(myClassName + ":readExternal():  BEGIN  bytes available in stream [" +
                    in.available() + "]  ");
        }

        //---------------------------------------------------------
        // object level identifiers
        //---------------------------------------------------------

        // serialization version ID
        long suid = in.readLong();

        // revision ID
        int revID = in.readInt();

        // make sure the object data is in a version we can handle
        if (suid != serialVersionUID) {
            throw new ClassNotFoundException(ExternalizeConstants.UNSUPPORTED_SUID);
        }

        // make sure the object data is in a revision level we can handle
        if (revID != REVISION_2) {
            throw new ClassNotFoundException(ExternalizeConstants.UNSUPPORTED_REVID);
        }

        //---------------------------------------------------------
        // various simple fields
        //---------------------------------------------------------

        // the type of execution flow for the message context
        FLOW = in.readInt();

        // various flags
        processingFault = in.readBoolean();
        paused = in.readBoolean();
        outputWritten = in.readBoolean();
        newThreadRequired = in.readBoolean();
        isSOAP11 = in.readBoolean();
        doingREST = in.readBoolean();
        doingMTOM = in.readBoolean();
        doingSwA = in.readBoolean();
        responseWritten = in.readBoolean();
        serverSide = in.readBoolean();

        long time = in.readLong();
        setLastTouchedTime(time);
        logCorrelationID = (String) in.readObject();

        // trace point
        if (DEBUG_ENABLED && log.isTraceEnabled()) {
            logCorrelationIDString = "[MessageContext: logID=" + getLogCorrelationID() + "]";
            log.trace(myClassName + ":readExternal():  reading the input stream for  " +
                      getLogIDString());
        }

        //---------------------------------------------------------
        // Message
        // Read the message and attachments
        //---------------------------------------------------------
        envelope = MessageExternalizeUtils.readExternal(in, this, getLogIDString());

        //---------------------------------------------------------
        // ArrayList executionChain
        //     handler and phase related data
        //---------------------------------------------------------
        // Restore the metadata about each member of the list
        // and the order of the list.
        // This metadata will be used to match up with phases
        // and handlers on the engine.
        //
        // Non-null list:
        //    UTF          - description string
        //    boolean      - active flag
        //    int          - current handler index
        //    int          - current phase index
        //    int          - expected number of entries in the list
        //                        not including the last entry marker
        //    objects      - MetaDataEntry object per list entry
        //                        last entry will be empty MetaDataEntry
        //                        with MetaDataEntry.LAST_ENTRY marker
        //    int          - adjusted number of entries in the list
        //                        includes the last empty entry
        //
        // Empty list:
        //    UTF          - description string
        //    boolean      - empty flag
        //---------------------------------------------------------

        // the local chain is not enabled until the
        // list has been reconstituted
        executionChain = null;
        currentHandlerIndex = -1;
        currentPhaseIndex = 0;
        metaExecutionChain = null;

        String marker = in.readUTF();
        if (DEBUG_ENABLED && log.isTraceEnabled()) {
            log.trace(getLogIDString() +
                      ": readExternal(): About to read executionChain, marker is: " + marker);
        }
        boolean gotChain = in.readBoolean();

        if (gotChain == ExternalizeConstants.ACTIVE_OBJECT) {
            metaHandlerIndex = in.readInt();
            metaPhaseIndex = in.readInt();

            int expectedNumberEntries = in.readInt();

            if (DEBUG_ENABLED && log.isTraceEnabled()) {
                log.trace(getLogIDString() +
                        ":readExternal(): execution chain:  expected number of entries [" +
                        expectedNumberEntries + "]");
            }

            // setup the list
            metaExecutionChain = new ArrayList<MetaDataEntry>();

            // process the objects
            boolean keepGoing = true;
            int count = 0;

            while (keepGoing) {
                // stop when we get to the end-of-list marker

                // get the object
                Object tmpObj = in.readObject();

                count++;

                MetaDataEntry mdObj = (MetaDataEntry) tmpObj;

                // get the class name, then add it to the list
                String tmpClassNameStr;
                String tmpQNameAsStr;

                if (mdObj != null) {
                    tmpClassNameStr = mdObj.getClassName();

                    if (tmpClassNameStr.equalsIgnoreCase(MetaDataEntry.END_OF_LIST)) {
                        // this is the last entry
                        keepGoing = false;
                    } else {
                        // add the entry to the meta data list
                        metaExecutionChain.add(mdObj);

                        tmpQNameAsStr = mdObj.getQNameAsString();

                        if (DEBUG_ENABLED && log.isTraceEnabled()) {
                            String tmpHasList = mdObj.isListEmpty() ? "no children" : "has children";

                            if (log.isTraceEnabled()) {
                                log.trace(getLogIDString() +
                                    ":readExternal(): meta data class [" + tmpClassNameStr +
                                    "] qname [" + tmpQNameAsStr + "]  index [" + count + "]   [" +
                                    tmpHasList + "]");
                            }
                        }
                    }
                } else {
                    // some error occurred
                    keepGoing = false;
                }

            } // end while keep going

            int adjustedNumberEntries = in.readInt();

            if (DEBUG_ENABLED && log.isTraceEnabled()) {
                log.trace(getLogIDString() +
                        ":readExternal(): adjusted number of entries ExecutionChain [" +
                        adjustedNumberEntries + "]    ");
            }
        }

        if ((metaExecutionChain == null) || (metaExecutionChain.isEmpty())) {
            if (DEBUG_ENABLED && log.isTraceEnabled()) {
                log.trace(getLogIDString() +
                        ":readExternal(): meta data for Execution Chain is NULL");
            }
        }

        //---------------------------------------------------------
        // LinkedList executedPhases
        //
        // Note that in previous versions of Axis2, this was
        // represented by two lists: "inboundExecutedPhases", "outboundExecutedPhases",
        // however since the message context itself represents a flow
        // direction, one of these lists was always null.  This was changed
        // around 2007-06-08 revision r545615.  For backward compatability
        // with streams saved in previous versions of Axis2, we need
        // to be able to process both the old style and new style.
        //---------------------------------------------------------
        // Restore the metadata about each member of the list
        // and the order of the list.
        // This metadata will be used to match up with phases
        // and handlers on the engine.
        //
        // Non-null list:
        //    UTF          - description string
        //    boolean      - active flag
        //    int          - expected number of entries in the list
        //                        not including the last entry marker
        //    objects      - MetaDataEntry object per list entry
        //                        last entry will be empty MetaDataEntry
        //                        with MetaDataEntry.LAST_ENTRY marker
        //    int          - adjusted number of entries in the list
        //                        includes the last empty entry
        //
        // Empty list:
        //    UTF          - description string
        //    boolean      - empty flag
        //---------------------------------------------------------

        // the local chain is not enabled until the
        // list has been reconstituted
        executedPhases = null;
        metaExecuted = null;

        marker = in.readUTF();
        if (DEBUG_ENABLED && log.isTraceEnabled()) {
            log.trace(getLogIDString() +
                      ": readExternal(): About to read executedPhases, marker is: " + marker);
        }

        // Previous versions of Axis2 saved two phases in the stream, although one should
        // always have been null.  The two phases and their associated markers are, in this order:
        // "inboundExecutedPhases", "outboundExecutedPhases".
        boolean gotInExecList = in.readBoolean();
        boolean oldStyleExecutedPhases = false;
        if (marker.equals("inboundExecutedPhases")) {
            oldStyleExecutedPhases = true;
        }

        if (oldStyleExecutedPhases && (gotInExecList == ExternalizeConstants.EMPTY_OBJECT)) {
            // There are an inboundExecutedPhases and an outboundExecutedPhases and this one
            // is empty, so skip over it and read the next one
            marker = in.readUTF();
            if (DEBUG_ENABLED && log.isTraceEnabled()) {
                log.trace(getLogIDString() +
                          ": readExternal(): Skipping over oldStyle empty inboundExecutedPhases");
                log.trace(getLogIDString() +
                          ": readExternal(): About to read executedPhases, marker is: " + marker);
            }
            gotInExecList = in.readBoolean();
        }

        /*
         * At this point, the stream should point to either "executedPhases" if this is the
         * new style of serialization.  If it is the oldStyle, it should point to whichever
         * of "inbound" or "outbound" executed phases contains an active object, since only one
         * should
         */
        if (gotInExecList == ExternalizeConstants.ACTIVE_OBJECT) {
            int expectedNumberInExecList = in.readInt();

            if (DEBUG_ENABLED && log.isTraceEnabled()) {
                log.trace(getLogIDString() +
                        ":readExternal(): executed phases:  expected number of entries [" +
                        expectedNumberInExecList + "]");
            }

            // setup the list
            metaExecuted = new LinkedList<MetaDataEntry>();

            // process the objects
            boolean keepGoing = true;
            int count = 0;

            while (keepGoing) {
                // stop when we get to the end-of-list marker

                // get the object
                Object tmpObj = in.readObject();

                count++;

                MetaDataEntry mdObj = (MetaDataEntry) tmpObj;

                // get the class name, then add it to the list
                String tmpClassNameStr;
                String tmpQNameAsStr;
                String tmpHasList = "no list";

                if (mdObj != null) {
                    tmpClassNameStr = mdObj.getClassName();

                    if (tmpClassNameStr.equalsIgnoreCase(MetaDataEntry.END_OF_LIST)) {
                        // this is the last entry
                        keepGoing = false;
                    } else {
                        // add the entry to the meta data list
                        metaExecuted.add(mdObj);

                        tmpQNameAsStr = mdObj.getQNameAsString();

                        if (!mdObj.isListEmpty()) {
                            tmpHasList = "has list";
                        }

                        if (DEBUG_ENABLED && log.isTraceEnabled()) {
                            log.trace(getLogIDString() +
                                    ":readExternal(): meta data class [" + tmpClassNameStr +
                                    "] qname [" + tmpQNameAsStr + "]  index [" + count + "]   [" +
                                    tmpHasList + "]");
                        }
                    }
                } else {
                    // some error occurred
                    keepGoing = false;
                }

            } // end while keep going

            int adjustedNumberInExecList = in.readInt();

            if (DEBUG_ENABLED && log.isTraceEnabled()) {
                log.trace(getLogIDString() +
                        ":readExternal(): adjusted number of entries executedPhases [" +
                        adjustedNumberInExecList + "]    ");
            }
        }

        if ((metaExecuted == null) || (metaExecuted.isEmpty())) {
            if (DEBUG_ENABLED && log.isTraceEnabled()) {
                log.trace(getLogIDString() +
                        ":readExternal(): meta data for executedPhases list is NULL");
            }
        }

        marker = in.readUTF(); // Read marker
        if (DEBUG_ENABLED && log.isTraceEnabled()) {
            log.trace(getLogIDString() +
                      ": readExternal(): After reading executedPhases, marker is: " + marker);
        }

        // If this is an oldStyle that contained both an inbound and outbound executed phases,
        // and the outbound phases wasn't read above, then we need to skip over it
        if (marker.equals("outboundExecutedPhases")) {
            Boolean gotOutExecList = in.readBoolean();
            if (DEBUG_ENABLED && log.isTraceEnabled()) {
                log.trace(getLogIDString() +
                          ": readExternal(): Skipping over outboundExecutedPhases, marker is: " + marker +
                          ", is list an active object: " + gotOutExecList);
            }
            if (gotOutExecList != ExternalizeConstants.EMPTY_OBJECT) {
                throw new IOException("Both inboundExecutedPhases and outboundExecutedPhases had active objects");
            }

            marker = in.readUTF();
            if (DEBUG_ENABLED && log.isTraceEnabled()) {
                log.trace(getLogIDString() +
                          ": readExternal(): After skipping ooutboundExecutePhases, marker is: " + marker);
            }
        }

        //---------------------------------------------------------
        // options
        //---------------------------------------------------------

        options = (Options) in.readObject();

        if (options != null) {
            if (DEBUG_ENABLED && log.isTraceEnabled()) {
                log.trace(getLogIDString() + ":readExternal(): restored Options [" +
                        options.getLogCorrelationIDString() + "]");
            }
        }

        //---------------------------------------------------------
        // operation
        //---------------------------------------------------------

        // axisOperation is not usable until the meta data has been reconciled
        axisOperation = null;
        marker = in.readUTF();  // Read Marker
        if (DEBUG_ENABLED && log.isTraceEnabled()) {
            log.trace(getLogIDString() +
                      ": readExternal(): About to read axisOperation, marker is: " + marker);
        }
        metaAxisOperation = (MetaDataEntry) in.readObject();

        // operation context is not usable until it has been activated
        // NOTE: expect this to be the parent
        marker = in.readUTF();  // Read marker
        if (DEBUG_ENABLED && log.isTraceEnabled()) {
            log.trace(getLogIDString() +
                      ": readExternal(): About to read operationContext, marker is: " + marker);
        }
        operationContext = (OperationContext) in.readObject();

        if (operationContext != null) {
            if (DEBUG_ENABLED && log.isTraceEnabled()) {
                log.trace(getLogIDString() + ":readExternal(): restored OperationContext [" +
                        operationContext.getLogCorrelationIDString() + "]");
            }
        }

        //---------------------------------------------------------
        // service
        //---------------------------------------------------------

        // axisService is not usable until the meta data has been reconciled
        axisService = null;
        marker = in.readUTF(); // Read marker
        if (DEBUG_ENABLED && log.isTraceEnabled()) {
            log.trace(getLogIDString() +
                      ": readExternal(): About to read axisService, marker is: " + marker);
        }
        metaAxisService = (MetaDataEntry) in.readObject();

        //-------------------------
        // serviceContextID string
        //-------------------------
        serviceContextID = (String) in.readObject();

        //-------------------------
        // serviceContext
        //-------------------------
        marker = in.readUTF(); // Read marker
        if (DEBUG_ENABLED && log.isTraceEnabled()) {
            log.trace(getLogIDString() +
                      ": readExternal(): About to read serviceContext, marker is: " + marker);
        }

        boolean servCtxActive = in.readBoolean();

        if (servCtxActive == ExternalizeConstants.EMPTY_OBJECT) {
            // empty object

            serviceContext = null;
        } else {
            // active object

            boolean isParent = in.readBoolean();

            // there's an object to read in if it is not the parent of the operation context
            if (!isParent) {
                serviceContext = (ServiceContext) in.readObject();
            } else {
                // the service context is the parent of the operation context
                // so get it from the operation context during activate
                serviceContext = null;
            }
        }

        //---------------------------------------------------------
        // serviceGroup
        //---------------------------------------------------------

        // axisServiceGroup is not usable until the meta data has been reconciled
        axisServiceGroup = null;
        marker = in.readUTF(); // Read marker
        if (DEBUG_ENABLED && log.isTraceEnabled()) {
            log.trace(getLogIDString() +
                      ": readExternal(): About to read AxisServiceGroup, marker is: " + marker);
        }
        metaAxisServiceGroup = (MetaDataEntry) in.readObject();

        //-----------------------------
        // serviceGroupContextId string
        //-----------------------------
        serviceGroupContextId = (String) in.readObject();

        //-----------------------------
        // serviceGroupContext
        //-----------------------------
        marker = in.readUTF();
        if (DEBUG_ENABLED && log.isTraceEnabled()) {
            log.trace(getLogIDString() +
                      ": readExternal(): About to read ServiceGroupContext, marker is: " + marker);
        }

        boolean servGrpCtxActive = in.readBoolean();

        if (servGrpCtxActive == ExternalizeConstants.EMPTY_OBJECT) {
            // empty object

            serviceGroupContext = null;
        } else {
            // active object

            boolean isParentSGC = in.readBoolean();

            // there's an object to read in if it is not the parent of the service group context
            if (!isParentSGC) {
                serviceGroupContext = (ServiceGroupContext) in.readObject();
            } else {
                // the service group context is the parent of the service context
                // so get it from the service context during activate
                serviceGroupContext = null;
            }
        }

        //---------------------------------------------------------
        // axis message
        //---------------------------------------------------------

        // axisMessage is not usable until the meta data has been reconciled
        axisMessage = null;
        marker = in.readUTF();  // Read marker
        if (DEBUG_ENABLED && log.isTraceEnabled()) {
            log.trace(getLogIDString() +
                      ": readExternal(): About to read AxisMessage, marker is: " + marker);
        }
        metaAxisMessage = (MetaDataEntry) in.readObject();
        reconcileAxisMessage = (metaAxisMessage != null);


        //---------------------------------------------------------
        // configuration context
        //---------------------------------------------------------

        // TODO: check to see if there is any runtime data important to this
        //       message context in the configuration context
        //       if so, then need to restore the saved runtime data and reconcile
        //       it with the configuration context on the system when
        //       this message context object is restored

        //---------------------------------------------------------
        // session context
        //---------------------------------------------------------
        sessionContext = (SessionContext) in.readObject();

        //---------------------------------------------------------
        // transport
        //---------------------------------------------------------

        //------------------------------
        // incomingTransportName string
        //------------------------------
        incomingTransportName = (String) in.readObject();

        // TransportInDescription transportIn
        // is not usable until the meta data has been reconciled
        transportIn = null;
        metaTransportIn = (MetaDataEntry) in.readObject();

        // TransportOutDescription transportOut
        // is not usable until the meta data has been reconciled
        transportOut = null;
        metaTransportOut = (MetaDataEntry) in.readObject();

        //---------------------------------------------------------
        // properties
        //---------------------------------------------------------
        // read local properties
        marker = in.readUTF(); // Read marker
        if (DEBUG_ENABLED && log.isTraceEnabled()) {
            log.trace(getLogIDString() +
                      ": readExternal(): About to read properties, marker is: " + marker);
        }
        properties = in.readMap(new HashMap());


        //---------------------------------------------------------
        // special data
        //---------------------------------------------------------
        marker = in.readUTF(); // Read marker
        if (DEBUG_ENABLED && log.isTraceEnabled()) {
            log.trace(getLogIDString() +
                      ": readExternal(): About to read SpecialData, marker is: " + marker);
        }

        boolean gotSelfManagedData = in.readBoolean();

        if (gotSelfManagedData == ExternalizeConstants.ACTIVE_OBJECT) {
            selfManagedDataHandlerCount = in.readInt();

            if (selfManagedDataListHolder == null) {
                selfManagedDataListHolder = new ArrayList<SelfManagedDataHolder>();
            } else {
                selfManagedDataListHolder.clear();
            }

            for (int i = 0; i < selfManagedDataHandlerCount; i++) {
                selfManagedDataListHolder.add((SelfManagedDataHolder) in.readObject());
            }
        }

        //---------------------------------------------------------
        // done
        //---------------------------------------------------------

        // trace point
        if (DEBUG_ENABLED && log.isTraceEnabled()) {
            log.trace(getLogIDString() +
                    ":readExternal():  message context object created for  " +
                    getLogIDString());
        }
    }


    /**
     * This method checks to see if additional work needs to be
     * done in order to complete the object reconstitution.
     * Some parts of the object restored from the readExternal()
     * cannot be completed until we have a configurationContext
     * from the active engine. The configurationContext is used
     * to help this object to plug back into the engine's
     * configuration and deployment objects.
     *
     * @param cc The configuration context object representing the active configuration
     */
    public void activate(ConfigurationContext cc) {
        // see if there's any work to do
        if (!needsToBeReconciled) {
            // return quick
            return;
        }

        // use the supplied configuration context
        setConfigurationContext(cc);

        // get the axis configuration
        AxisConfiguration axisConfig = configurationContext.getAxisConfiguration();

        // We previously saved metaAxisService; restore it
        if (metaAxisService != null) {
            this.setAxisService(ActivateUtils.findService(axisConfig,
                                                metaAxisService.getClassName(),
                                                metaAxisService.getQNameAsString(),
                                                metaAxisService.getExtraName()));
        }

        // We previously saved metaAxisServiceGroup; restore it
        if (metaAxisServiceGroup != null) {
            this.setAxisServiceGroup(
                                     ActivateUtils.findServiceGroup(axisConfig,
                                                metaAxisServiceGroup.getClassName(),
                                                metaAxisServiceGroup.getQNameAsString()));
        }

        // We previously saved metaAxisOperation; restore it
        if (metaAxisOperation != null) {
            AxisService serv = axisService;

            if (serv != null) {
                // TODO: check for the empty name
                this.setAxisOperation(ActivateUtils.findOperation(serv,
                                                                  metaAxisOperation.getClassName(),
                                                                  metaAxisOperation.getQName()));
            } else {
                this.setAxisOperation(ActivateUtils.findOperation(axisConfig,
                                                                  metaAxisOperation.getClassName(),
                                                                  metaAxisOperation.getQName()));
            }
        }

        // We previously saved metaAxisMessage; restore it
        if (metaAxisMessage != null) {
            AxisOperation op = axisOperation;

            if (op != null) {
                // TODO: check for the empty name
                this.setAxisMessage(ActivateUtils.findMessage(op,
                                                              metaAxisMessage.getQNameAsString(),
                                                              metaAxisMessage.getExtraName()));
            }
        }

        //---------------------------------------------------------------------
        // operation context
        //---------------------------------------------------------------------
        // this will do a full hierarchy, so do it first
        // then we can re-use its objects

        if (operationContext != null) {
            operationContext.activate(cc);

            // this will be set as the parent of the message context
            // after the other context objects have been activated
        }

        //---------------------------------------------------------------------
        // service context
        //---------------------------------------------------------------------

        if (serviceContext == null) {
            // get the parent serviceContext of the operationContext
            if (operationContext != null) {
                serviceContext = operationContext.getServiceContext();
            }
        }

        // if we have a service context, make sure it is usable
        if (serviceContext != null) {
            // for some reason, the service context might be set differently from
            // the operation context parent
            serviceContext.activate(cc);
        }

        //---------------------------------------------------------------------
        // service group context
        //---------------------------------------------------------------------

        if (serviceGroupContext == null) {
            // get the parent serviceGroupContext of the serviceContext
            if (serviceContext != null) {
                serviceGroupContext = (ServiceGroupContext) serviceContext.getParent();
            }
        }

        // if we have a service group context, make sure it is usable
        if (serviceGroupContext != null) {
            // for some reason, the service group context might be set differently from
            // the service context parent
            serviceGroupContext.activate(cc);
        }

        //---------------------------------------------------------------------
        // other context-related reconciliation
        //---------------------------------------------------------------------

        this.setParent(operationContext);

        //---------------------------------------------------------------------
        // options
        //---------------------------------------------------------------------
        if (options != null) {
            options.activate(cc);
        }

        String tmpID = getMessageID();
        String logCorrelationIDString = getLogIDString();

        if (DEBUG_ENABLED && log.isTraceEnabled()) {
            log.trace(logCorrelationIDString + ":activate():   message ID [" + tmpID + "] for " +
                    logCorrelationIDString);
        }

        //---------------------------------------------------------------------
        // transports
        //---------------------------------------------------------------------

        // We previously saved metaTransportIn; restore it
        if (metaTransportIn != null) {
            QName qin = metaTransportIn.getQName();
            TransportInDescription tmpIn = null;
            try {
                tmpIn = axisConfig.getTransportIn(qin.getLocalPart());
            }
            catch (Exception exin) {
                // if a fault is thrown, log it and continue
                log.trace(logCorrelationIDString +
                        "activate():  exception caught when getting the TransportInDescription [" +
                        qin.toString() + "]  from the AxisConfiguration [" +
                        exin.getClass().getName() + " : " + exin.getMessage() + "]");
            }

            if (tmpIn != null) {
                transportIn = tmpIn;
            } else {
                transportIn = null;
            }
        } else {
            transportIn = null;
        }

        // We previously saved metaTransportOut; restore it
        if (metaTransportOut != null) {
            // TODO : Check if this should really be a QName?
            QName qout = metaTransportOut.getQName();
            TransportOutDescription tmpOut = null;
            try {
                tmpOut = axisConfig.getTransportOut(qout.getLocalPart());
            }
            catch (Exception exout) {
                // if a fault is thrown, log it and continue
                if (DEBUG_ENABLED && log.isTraceEnabled()) {
                    log.trace(logCorrelationIDString +
                        "activate():  exception caught when getting the TransportOutDescription [" +
                        qout.toString() + "]  from the AxisConfiguration [" +
                        exout.getClass().getName() + " : " + exout.getMessage() + "]");
                }
            }

            if (tmpOut != null) {
                transportOut = tmpOut;
            } else {
                transportOut = null;
            }
        } else {
            transportOut = null;
        }

        //-------------------------------------------------------
        // reconcile the execution chain
        //-------------------------------------------------------
        if (metaExecutionChain != null) {
            if (DEBUG_ENABLED && log.isTraceEnabled()) {
                log.trace(
                        logCorrelationIDString + ":activate(): reconciling the execution chain...");
            }

            currentHandlerIndex = metaHandlerIndex;
            currentPhaseIndex = metaPhaseIndex;

            executionChain = restoreHandlerList(metaExecutionChain);

            try {
                deserializeSelfManagedData();
            }
            catch (Exception ex) {
                // log the exception
                if (DEBUG_ENABLED && log.isTraceEnabled()) {
                    log.trace(logCorrelationIDString +
                        ":activate(): *** WARNING *** deserializing the self managed data encountered Exception [" +
                        ex.getClass().getName() + " : " + ex.getMessage() + "]", ex);
                }
            }
        }

        //-------------------------------------------------------
        // reconcile the lists for the executed phases
        //-------------------------------------------------------
        if (metaExecuted != null) {
            if (DEBUG_ENABLED && log.isTraceEnabled()) {
                log.trace(logCorrelationIDString +
                        ":activate(): reconciling the executed chain...");
            }

            if (!(executedPhasesReset)) {
                executedPhases =
                        restoreExecutedList(executedPhases, metaExecuted);
            }
        }

        if (executedPhases == null) {
            executedPhases = new LinkedList<Handler>();
        }


        //-------------------------------------------------------
        // finish up remaining links
        //-------------------------------------------------------
        if (operationContext != null) {
            operationContext.restoreMessageContext(this);
        }

        //-------------------------------------------------------
        // done, reset the flag
        //-------------------------------------------------------
        needsToBeReconciled = false;

    }


    /**
     * This method checks to see if additional work needs to be
     * done in order to complete the object reconstitution.
     * Some parts of the object restored from the readExternal()
     * cannot be completed until we have an object that gives us
     * a view of the active object graph from the active engine.
     * <p/>
     * NOTE: when activating an object, you only need to call
     * one of the activate methods (activate() or activateWithOperationContext())
     * but not both.
     *
     * @param operationCtx The operation context object that is a member of the active object graph
     */
    public void activateWithOperationContext(OperationContext operationCtx) {
        // see if there's any work to do
        if (!(needsToBeReconciled)) {
            // return quick
            return;
        }

        String logCorrelationIDString = getLogIDString();
        // trace point
        if (DEBUG_ENABLED && log.isTraceEnabled()) {
            log.trace(logCorrelationIDString + ":activateWithOperationContext():  BEGIN");
        }

        if (operationCtx == null) {
            // won't be able to finish
            if (DEBUG_ENABLED && log.isTraceEnabled()) {
                log.trace(logCorrelationIDString +
                    ":activateWithOperationContext():  *** WARNING ***  No active OperationContext object is available.");
            }
            return;
        }

        //---------------------------------------------------------------------
        // locate the objects in the object graph
        //---------------------------------------------------------------------
        ConfigurationContext configCtx = operationCtx.getConfigurationContext();

        if (configCtx == null) {
            // won't be able to finish
            if (DEBUG_ENABLED && log.isTraceEnabled()) {
                log.trace(logCorrelationIDString +
                    ":activateWithOperationContext():  *** WARNING ***  No active ConfigurationContext object is available.");
            }
            return;
        }

        AxisConfiguration axisCfg = configCtx.getAxisConfiguration();

        AxisOperation axisOp = operationCtx.getAxisOperation();
        ServiceContext serviceCtx = operationCtx.getServiceContext();

        ServiceGroupContext serviceGroupCtx = null;
        AxisService axisSrv = null;
        AxisServiceGroup axisSG = null;

        if (serviceCtx != null) {
            serviceGroupCtx = serviceCtx.getServiceGroupContext();
            axisSrv = serviceCtx.getAxisService();
        }

        if (serviceGroupCtx != null) {
            axisSG = serviceGroupCtx.getDescription();
        }

        //---------------------------------------------------------------------
        // link to the objects in the object graph
        //---------------------------------------------------------------------

        setConfigurationContext(configCtx);

        setAxisOperation(axisOp);
        setAxisService(axisSrv);
        setAxisServiceGroup(axisSG);

        setServiceGroupContext(serviceGroupCtx);
        setServiceContext(serviceCtx);
        setOperationContext(operationCtx);

        //---------------------------------------------------------------------
        // reconcile the remaining objects
        //---------------------------------------------------------------------

        // We previously saved metaAxisMessage; restore it
        if (metaAxisMessage != null) {
            if (axisOp != null) {
                // TODO: check for the empty name
                this.setAxisMessage(ActivateUtils.findMessage(axisOp,
                                                 metaAxisMessage.getQNameAsString(),
                                                 metaAxisMessage.getExtraName()));
            }
        }

        //---------------------------------------------------------------------
        // options
        //---------------------------------------------------------------------
        if (options != null) {
            options.activate(configCtx);
        }

        String tmpID = getMessageID();

        if (DEBUG_ENABLED && log.isTraceEnabled()) {
            log.trace(logCorrelationIDString + ":activateWithOperationContext():   message ID [" +
                    tmpID + "]");
        }

        //---------------------------------------------------------------------
        // transports
        //---------------------------------------------------------------------

        // We previously saved metaTransportIn; restore it
        if (metaTransportIn != null) {
            QName qin = metaTransportIn.getQName();
            TransportInDescription tmpIn = null;
            try {
                tmpIn = axisCfg.getTransportIn(qin.getLocalPart());
            }
            catch (Exception exin) {
                // if a fault is thrown, log it and continue
                if (DEBUG_ENABLED && log.isTraceEnabled()) {
                    log.trace(logCorrelationIDString +
                        "activateWithOperationContext():  exception caught when getting the TransportInDescription [" +
                        qin.toString() + "]  from the AxisConfiguration [" +
                        exin.getClass().getName() + " : " + exin.getMessage() + "]");
                }

            }

            if (tmpIn != null) {
                transportIn = tmpIn;
            } else {
                transportIn = null;
            }
        } else {
            transportIn = null;
        }

        // We previously saved metaTransportOut; restore it
        if (metaTransportOut != null) {
            QName qout = metaTransportOut.getQName();
            TransportOutDescription tmpOut = null;
            try {
                tmpOut = axisCfg.getTransportOut(qout.getLocalPart());
            }
            catch (Exception exout) {
                // if a fault is thrown, log it and continue
                if (DEBUG_ENABLED && log.isTraceEnabled()) {
                    log.trace(logCorrelationIDString +
                        "activateWithOperationContext():  exception caught when getting the TransportOutDescription [" +
                        qout.toString() + "]  from the AxisConfiguration [" +
                        exout.getClass().getName() + " : " + exout.getMessage() + "]");
                }
            }

            if (tmpOut != null) {
                transportOut = tmpOut;
            } else {
                transportOut = null;
            }
        } else {
            transportOut = null;
        }

        //-------------------------------------------------------
        // reconcile the execution chain
        //-------------------------------------------------------
        if (metaExecutionChain != null) {
            if (DEBUG_ENABLED && log.isTraceEnabled()) {
                log.trace(logCorrelationIDString +
                        ":activateWithOperationContext(): reconciling the execution chain...");
            }

            currentHandlerIndex = metaHandlerIndex;
            currentPhaseIndex = metaPhaseIndex;

            executionChain = restoreHandlerList(metaExecutionChain);

            try {
                deserializeSelfManagedData();
            }
            catch (Exception ex) {
                // log the exception
                if (DEBUG_ENABLED && log.isTraceEnabled()) {
                    log.trace(logCorrelationIDString +
                        ":activateWithOperationContext(): *** WARNING *** deserializing the self managed data encountered Exception [" +
                        ex.getClass().getName() + " : " + ex.getMessage() + "]", ex);
                }
            }
        }

        //-------------------------------------------------------
        // reconcile the lists for the executed phases
        //-------------------------------------------------------
        if (metaExecuted != null) {
            if (DEBUG_ENABLED && log.isTraceEnabled()) {
                log.trace(logCorrelationIDString +
                        ":activateWithOperationContext(): reconciling the executed chain...");
            }

            if (!(executedPhasesReset)) {
                executedPhases =
                        restoreExecutedList(executedPhases, metaExecuted);
            }
        }

        if (executedPhases == null) {
            executedPhases = new LinkedList<Handler>();
        }

        //-------------------------------------------------------
        // done, reset the flag
        //-------------------------------------------------------
        needsToBeReconciled = false;

        if (DEBUG_ENABLED && log.isTraceEnabled()) {
            log.trace(logCorrelationIDString + ":activateWithOperationContext():  END");
        }
    }


    /**
     * @param metaDataEntries ArrayList of MetaDataEntry objects
     * @return ArrayList of Handlers based on our list of handlers from the reconstituted deserialized list, and the existing handlers in the AxisConfiguration object.  May return null.
     */
    private ArrayList<Handler> restoreHandlerList(ArrayList<MetaDataEntry> metaDataEntries) {
        AxisConfiguration axisConfig = configurationContext.getAxisConfiguration();

        List<Handler> existingHandlers = new ArrayList<Handler>();

        // TODO: I'm using clone for the ArrayList returned from axisConfig object.
        //     Does it do a deep clone of the Handlers held there?  Does it matter?
        switch (FLOW) {
            case IN_FLOW:
                existingHandlers.addAll(axisConfig.getInFlowPhases());
                break;

            case OUT_FLOW:
            	existingHandlers.addAll(axisConfig.getOutFlowPhases());
                break;

            case IN_FAULT_FLOW:
            	existingHandlers.addAll(axisConfig.getInFaultFlowPhases());
                break;

            case OUT_FAULT_FLOW:
            	existingHandlers.addAll(axisConfig.getOutFaultFlowPhases());
                break;
        }

        existingHandlers = flattenHandlerList(existingHandlers, null);

        ArrayList<Handler> handlerListToReturn = new ArrayList<Handler>();

        for (int i = 0; i < metaDataEntries.size(); i++) {
            Handler handler = (Handler) ActivateUtils
                    .findHandler(existingHandlers, (MetaDataEntry) metaDataEntries.get(i));

            if (handler != null) {
                handlerListToReturn.add(handler);
            }
        }

        return handlerListToReturn;
    }


    /**
     * Using meta data for phases/handlers, create a linked list of actual
     * phase/handler objects.  The created list is composed of the objects
     * from the base list at the top of the created list followed by the
     * restored objects.
     *
     * @param base            Linked list of phase/handler objects
     * @param metaDataEntries Linked list of MetaDataEntry objects
     * @return LinkedList of objects or NULL if none available
     */
    private LinkedList<Handler> restoreExecutedList(LinkedList<Handler> base, LinkedList<MetaDataEntry> metaDataEntries) {
        if (metaDataEntries == null) {
            return base;
        }

        // get a list of existing handler/phase objects for the restored objects

        ArrayList<MetaDataEntry> tmpMetaDataList = new ArrayList<MetaDataEntry>(metaDataEntries);

        ArrayList<Handler> existingList = restoreHandlerList(tmpMetaDataList);

        if ((existingList == null) || (existingList.isEmpty())) {
            return base;
        }

        // set up a list to return

        LinkedList<Handler> returnedList = new LinkedList<Handler>();

        if (base != null) {
            returnedList.addAll(base);
        }

        returnedList.addAll(existingList);

        return returnedList;
    }


    /**
     * Process the list of handlers from the Phase object
     * into the appropriate meta data.
     *
     * @param phase   The Phase object containing a list of handlers
     * @param mdPhase The meta data object associated with the specified Phase object
     */
    private void setupPhaseList(Phase phase, MetaDataEntry mdPhase) {
        // get the list from the phase object
        List<Handler> handlers = phase.getHandlers();

        if (handlers.isEmpty()) {
            // done, make sure there is no list in the given meta data
            mdPhase.removeList();
            return;
        }

        // get the metadata on each member of the list

        int listSize = handlers.size();

        if (listSize > 0) {

            Iterator<Handler> i = handlers.iterator();

            while (i.hasNext()) {
                Object obj = i.next();
                String objClass = obj.getClass().getName();

                // start the meta data entry for this object
                MetaDataEntry mdEntry = new MetaDataEntry();
                mdEntry.setClassName(objClass);

                // get the correct object-specific name
                String qnameAsString;

                if (obj instanceof Phase) {
                    // nested condition, the phase object contains another phase!
                    Phase phaseObj = (Phase) obj;
                    qnameAsString = phaseObj.getName();

                    // add the list of handlers to the meta data
                    setupPhaseList(phaseObj, mdEntry);
                } else if (obj instanceof Handler) {
                    Handler handlerObj = (Handler) obj;
                    qnameAsString = handlerObj.getName();
                } else {
                    // TODO: will there be any other kinds of objects
                    // in the list?
                    qnameAsString = "NULL";
                }

                mdEntry.setQName(qnameAsString);

                // done with setting up the meta data for the list entry
                // so add it to the parent
                mdPhase.addToList(mdEntry);

                if (DEBUG_ENABLED && log.isTraceEnabled()) {
                    log.trace(getLogIDString() + ":setupPhaseList(): list entry class [" +
                            objClass + "] qname [" + qnameAsString + "]");
                }

            } // end while entries in list
        } else {
            // a list with no entries
            // done, make sure there is no list in the given meta data
            mdPhase.removeList();
        }
    }


    /**
     * Return a Read-Only copy of this message context
     * that has been extracted from the object
     * hierachy.  In other words, the message context
     * copy does not have links to the object graph.
     * <p/>
     * NOTE: The copy shares certain objects with the original.
     * The intent is to use the copy to read values but not
     * modify them, especially since the copy is not part
     * of the normal *Context and Axis* object graph.
     *
     * @return A copy of the message context that is not in the object graph
     */
    public MessageContext extractCopyMessageContext() {
        MessageContext copy = new MessageContext();
        String logCorrelationIDString = getLogIDString();
        if (DEBUG_ENABLED && log.isTraceEnabled()) {
            log.trace(logCorrelationIDString + ":extractCopyMessageContext():  based on " +
                    logCorrelationIDString + "   into copy " + copy.getLogIDString());
        }

        //---------------------------------------------------------
        // various simple fields
        //---------------------------------------------------------

        copy.setFLOW(FLOW);

        copy.setProcessingFault(processingFault);
        copy.setPaused(paused);
        copy.setOutputWritten(outputWritten);
        copy.setNewThreadRequired(newThreadRequired);
        copy.setDoingREST(doingREST);
        copy.setDoingMTOM(doingMTOM);
        copy.setDoingSwA(doingSwA);
        copy.setResponseWritten(responseWritten);
        copy.setServerSide(serverSide);

        copy.setLastTouchedTime(getLastTouchedTime());

        //---------------------------------------------------------
        // message
        //---------------------------------------------------------
        try {
            copy.setEnvelope(envelope);
        }
        catch (Exception ex) {
            if (DEBUG_ENABLED && log.isTraceEnabled()) {
                log.trace(logCorrelationIDString +
                    ":extractCopyMessageContext():  Exception caught when setting the copy with the envelope",
                      ex);
            }
        }

        copy.setAttachmentMap(attachments);

        copy.setIsSOAP11Explicit(isSOAP11);

        //---------------------------------------------------------
        // ArrayList executionChain
        //     handler and phase related data
        //---------------------------------------------------------
        copy.setExecutionChain(executionChain);

        // the setting of the execution chain is actually a reset
        // so copy the indices after putting in the execution chain
        copy.setCurrentHandlerIndex(currentHandlerIndex);
        copy.setCurrentPhaseIndex(currentPhaseIndex);

        //---------------------------------------------------------
        // LinkedList executedPhases
        //---------------------------------------------------------
        copy.setExecutedPhasesExplicit(executedPhases);

        //---------------------------------------------------------
        // options
        //---------------------------------------------------------
        copy.setOptionsExplicit(options);

        //---------------------------------------------------------
        // axis operation
        //---------------------------------------------------------
        copy.setAxisOperation(null);

        //---------------------------------------------------------
        // operation context
        //---------------------------------------------------------
        copy.setOperationContext(null);

        //---------------------------------------------------------
        // axis service
        //---------------------------------------------------------
        copy.setAxisService(null);

        //-------------------------
        // serviceContextID string
        //-------------------------
        copy.setServiceContextID(serviceContextID);

        //-------------------------
        // serviceContext
        //-------------------------
        copy.setServiceContext(null);

        //---------------------------------------------------------
        // serviceGroup
        //---------------------------------------------------------
        copy.setServiceGroupContext(null);

        //-----------------------------
        // serviceGroupContextId string
        //-----------------------------
        copy.setServiceGroupContextId(serviceGroupContextId);

        //---------------------------------------------------------
        // axis message
        //---------------------------------------------------------
        copy.setAxisMessage(axisMessage);

        //---------------------------------------------------------
        // configuration context
        //---------------------------------------------------------
        copy.setConfigurationContext(configurationContext);

        //---------------------------------------------------------
        // session context
        //---------------------------------------------------------
        copy.setSessionContext(sessionContext);

        //---------------------------------------------------------
        // transport
        //---------------------------------------------------------

        //------------------------------
        // incomingTransportName string
        //------------------------------
        copy.setIncomingTransportName(incomingTransportName);

        copy.setTransportIn(transportIn);
        copy.setTransportOut(transportOut);

        //---------------------------------------------------------
        // properties
        //---------------------------------------------------------
        // Only set the local properties (i.e. don't use getProperties())
        copy.setProperties(properties);

        //---------------------------------------------------------
        // special data
        //---------------------------------------------------------

        copy.setSelfManagedDataMapExplicit(selfManagedDataMap);

        //---------------------------------------------------------
        // done
        //---------------------------------------------------------

        return copy;
    }

    //------------------------------------------------------------------------
    // additional setter methods needed to copy the message context object
    //------------------------------------------------------------------------

    public void setIsSOAP11Explicit(boolean t) {
        isSOAP11 = t;
    }

    public void setExecutedPhasesExplicit(LinkedList<Handler> inb) {
        executedPhases = inb;
    }

    public void setSelfManagedDataMapExplicit(LinkedHashMap<String, Object> map) {
        selfManagedDataMap = map;
    }

    public void setOptionsExplicit(Options op) {
        this.options = op;
    }


    /**
     * Trace a warning message, if needed, indicating that this
     * object needs to be activated before accessing certain fields.
     *
     * @param methodname The method where the warning occurs
     */
    private void checkActivateWarning(String methodname) {
        if (needsToBeReconciled) {
            if (DEBUG_ENABLED && log.isWarnEnabled()) {
                log.warn(getLogIDString() + ":" + methodname + "(): ****WARNING**** " + myClassName +
                    ".activate(configurationContext) needs to be invoked.");
            }
        }
    }

    public ConfigurationContext getRootContext() {
        return configurationContext;
    }

    public boolean isFault() {
        try {
            return getEnvelope().hasFault();
        } catch (Exception e) {
            // TODO: What should we be doing here?  No envelope certainly seems bad....
            return false;
        }
    }


    /**
     * Obtain the Exception which caused the processing chain to halt.
     * @return null, or an Exception.
     */
    public Exception getFailureReason() {
        return failureReason;
    }

    /**
     * Set the failure reason.  Only AxisEngine should ever do this.
     *
     * @param failureReason an Exception which caused processing to halt.
     */
    public void setFailureReason(Exception failureReason) {
        this.failureReason = failureReason;
    }
}
