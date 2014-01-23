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

import org.apache.axiom.util.UIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.externalize.ActivateUtils;
import org.apache.axis2.context.externalize.ExternalizeConstants;
import org.apache.axis2.context.externalize.SafeObjectInputStream;
import org.apache.axis2.context.externalize.SafeObjectOutputStream;
import org.apache.axis2.context.externalize.SafeSerializable;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.util.MetaDataEntry;
import org.apache.axis2.wsdl.WSDLConstants.WSDL20_2004_Constants;
import org.apache.axis2.wsdl.WSDLConstants.WSDL20_2006Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * An OperationContext represents a running "instance" of an operation, which is
 * represented by an AxisOperation object. This concept is needed to allow
 * messages to be grouped into operations as in WSDL 2.0-speak operations are
 * essentially arbitrary message exchange patterns. So as messages are being
 * exchanged the OperationContext remembers the state of where in the message
 * exchange pattern it is in.
 * <p/>
 * The base implementation of OperationContext
 * supports MEPs which have one input message and/or one output message. That
 * is, it supports the all the MEPs that are in the WSDL 2.0 specification. In
 * order to support another MEP one must extend this class and register its
 * creation in the OperationContexFactory.
 */
public class OperationContext extends AbstractContext 
    implements Externalizable, SafeSerializable {

    /*
     * setup for logging
     */
    private static final Log log = LogFactory.getLog(OperationContext.class);

    private static final String myClassName = "OperationContext";

    private static boolean debugEnabled = log.isDebugEnabled();

    /**
     * An ID which can be used to correlate operations on an instance of
     * this object in the log files
     */
    private String logCorrelationIDString = null;


    /**
     * @serial The serialization version ID tracks the version of the class.
     * If a class definition changes, then the serialization/externalization
     * of the class is affected. If a change to the class is made which is
     * not compatible with the serialization/externalization of the class,
     * then the serialization version ID should be updated.
     * Refer to the "serialVer" utility to compute a serialization
     * version ID.
     */
    private static final long serialVersionUID = -7264782778333554350L;

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


    /**
     * @serial isComplete flag
     */
    private boolean isComplete;

    /**
     * @serial key string
     */
    //The key value of the operationContextMap;
    private String key;

    // the AxisOperation of which this is a running instance. The MEP of this
    // AxisOperation must be one of the 8 predefined ones in WSDL 2.0.
    private transient AxisOperation axisOperation;

    /**
     * the set of message contexts associated with this operation
     */
    private transient HashMap<String, MessageContext> messageContexts;

    //----------------------------------------------------------------
    // MetaData for data to be restored in activate after readExternal
    //----------------------------------------------------------------

    /**
     * Indicates whether the message context has been reconstituted
     * and needs to have its object references reconciled
     */
    private transient boolean needsToBeReconciled = false;

    /**
     * Suppresses warning messages for activation
     * when doing internal reconciliation
     */
    private transient boolean suppressWarnings = false;

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
     * The ServiceContext metadata will be used during
     * activate to match up with an existing object
     */
    private transient ServiceContext metaParent = null;


    /**
     * This is used to hold information about message context objects
     * that are in the messageContexts map.  This allows message context
     * objects to be isolated from the object graph so that duplicate
     * copies of objects are not saved/restored.
     */
    private HashMap metaMessageContextMap = null;

    /**
     * This is used to hold temporarily any message context objects
     * that were isolated from the messageContexts map.
     */
    private transient HashMap isolatedMessageContexts = null;

    /**
     * This is used to hold temporarily any message context objects
     * from the messageContexts map for save/restore activities.
     */
    private transient HashMap workingSet = null;

    //----------------------------------------------------------------
    // end MetaData section
    //----------------------------------------------------------------

    /**
     * Simple constructor (needed for deserialization, shouldn't be used otherwise!)
     */
    public OperationContext() {
        super(null);
        this.messageContexts = new HashMap<String, MessageContext>();
    }

    /**
     * Constructs a new OperationContext.
     *
     * @param axisOperation  the AxisOperation whose running instances' state this
     *                       OperationContext represents.
     * @param serviceContext the parent ServiceContext representing any state related to
     *                       the set of all operations of the service.
     */
    public OperationContext(AxisOperation axisOperation,
                            ServiceContext serviceContext) {
        super(serviceContext);
        this.messageContexts = new HashMap<String, MessageContext>();
        this.axisOperation = axisOperation;
        this.setParent(serviceContext);
    }

    /**
     * When a new message is added to the <code>MEPContext</code> the logic
     * should be included remove the MEPContext from the table in the
     * <code>EngineContext</code>. Example: IN_IN_OUT At the second IN
     * message the MEPContext should be removed from the AxisOperation.
     *
     * @param msgContext
     */
    public void addMessageContext(MessageContext msgContext) throws AxisFault {
        if (axisOperation != null) {
            axisOperation.addMessageContext(msgContext, this);
            touch();
        }
    }


    /**
     * Removes the pointers to this <code>OperationContext</code> in the
     * <code>ConfigurationContext</code>'s OperationContextMap so that this
     * <code>OperationContext</code> will eventually get garbage collected
     * along with the <code>MessageContext</code>'s it contains. Note that if
     * the caller wants to make sure its safe to clean up this OperationContext
     * he should call isComplete() first. However, in cases like IN_OPTIONAL_OUT
     * and OUT_OPTIONAL_IN, it is possibe this will get called without the MEP
     * being complete due to the optional nature of the MEP.
     */
    public void cleanup() {
        ServiceContext serv = getServiceContext();

        if (serv != null) {
            serv.getConfigurationContext().unregisterOperationContext(key);
        }
    }

    /**
     * @return Returns the axisOperation.
     */
    public AxisOperation getAxisOperation() {
        if (needsToBeReconciled && !suppressWarnings && debugEnabled) {
            log.debug(getLogCorrelationIDString() +
                    ":getAxisOperation(): ****WARNING**** OperationContext.activate(configurationContext) needs to be invoked.");
        }

        return axisOperation;
    }

    /**
     * Returns the EngineContext in which the parent ServiceContext lives.
     *
     * @return Returns parent ServiceContext's parent EngineContext.
     */
    public ConfigurationContext getConfigurationContext() {
        if (parent != null) {
            return ((ServiceContext) parent).getConfigurationContext();
        } else {
            return null;
        }
    }


    /**
     * Get the message context identified by a given label.
     * 
     * @param messageLabel The label of the message context to retrieve.
     *                     This should be one of the <code>MESSAGE_LABEL_xxx</code> constants
     *                     defined in {@link WSDLConstants}.
     * @return the message context for the given label, or <code>null</code> if no
     *         message context was found
     * @throws AxisFault never
     */
    public MessageContext getMessageContext(String messageLabel)
            throws AxisFault {
        if (messageContexts == null) {
            return null;
        }

        return messageContexts.get(messageLabel);

    }

    /**
     * Remove the indicated message context.
     * Example Usage: The exchange is aborted and we need to 
     * undo the work and free resources.
     * @param label
     * @throws AxisFault
     */
    public void removeMessageContext(String label) throws AxisFault {

        MessageContext mc = getMessageContext(label);
        if (mc != null) {
            messageContexts.remove(label);
            setComplete(false); 
            touch();
        }
    }
        

    public HashMap<String, MessageContext> getMessageContexts() {
        return messageContexts;
    }

    /**
     * Returns the ServiceContext in which this OperationContext lives.
     *
     * @return Returns parent ServiceContext.
     */
    public ServiceContext getServiceContext() {
        return (ServiceContext) parent;
    }

    /**
     * Checks to see if the MEP is complete. i.e. whether all the messages that
     * are associated with the MEP has arrived and MEP is complete.
     */
    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }

    public void setKey(String key) {
        this.key = key;
    }

    /* ===============================================================
    * Externalizable support
    * ===============================================================
    */


    /**
     * Save the contents of this object.
     * <p/>
     * NOTE: Transient fields and static fields are not saved.
     * Also, objects that represent "static" data are
     * not saved, except for enough information to be
     * able to find matching objects when the message
     * context is re-constituted.
     *
     * @param out The stream to write the object contents to
     * @throws IOException
     */
    public void writeExternal(ObjectOutput o) throws IOException {
        SafeObjectOutputStream out = SafeObjectOutputStream.install(o);
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

        out.writeLong(getLastTouchedTime());

        out.writeBoolean(isComplete);
        out.writeObject(key);
        out.writeObject(logCorrelationIDString);

        //---------------------------------------------------------
        // properties
        //---------------------------------------------------------
        out.writeUTF("properties");  // write marker
        out.writeMap(getProperties());

        //---------------------------------------------------------
        // AxisOperation axisOperation
        //---------------------------------------------------------
        out.writeUTF("metaAxisOperation"); // write marker
        metaAxisOperation = null;
        if (axisOperation != null) {
            metaAxisOperation = new MetaDataEntry(axisOperation.getClass().getName(),
                                                  axisOperation.getName().toString());
        }
        out.writeObject(metaAxisOperation);

        //---------------------------------------------------------
        // AxisOperation axisService
        //---------------------------------------------------------
        // save the meta data for the corresponding axis service to better
        // match up the axis operation
        out.writeUTF("metaAxisService"); // write marker
        metaAxisService = null;
        AxisService axisService = axisOperation.getAxisService();

        if (axisService != null) {
            String serviceAndPortNames = ActivateUtils.getAxisServiceExternalizeExtraName(axisService);
            // If there is a service & port QName stored on the AxisService then write it out so 
            // it can be used during deserialization to hook up the message context to the 
            // correct AxisService.
            metaAxisService =
                    new MetaDataEntry(axisService.getClass().getName(), axisService.getName(),
                            serviceAndPortNames);
        }
        out.writeObject(metaAxisService);

        //---------------------------------------------------------
        // parent
        //---------------------------------------------------------
        out.writeUTF("parent"); // write marker
        out.writeObject(this.getServiceContext());

        //---------------------------------------------------------
        // HashMap messageContexts table
        //---------------------------------------------------------

        // NOTES: The assumption is that the table contains message contexts
        // that are in the OperationContext hierarchy.  To reduce overlap
        // of object information that is being saved, extract the
        // message context objects from the hierachy before saving.
        // When the OperationContext is restored, the "slimmed down"
        // message context objects are plugged back into the hierachy
        // using the restored OperationContext as a basis.

        // first deal with the original messageContexts table
        HashMap tmpMsgCtxMap = null;

        if ((messageContexts != null) && (!messageContexts.isEmpty())) {
            // create a table of the non-isolated message contexts
            workingSet = new HashMap();
            tmpMsgCtxMap = new HashMap();

            Set keySet = messageContexts.keySet();
            Iterator itKeys = keySet.iterator();

            while (itKeys.hasNext()) {
                // expect the key to be a string
                String keyObj = (String) itKeys.next();

                // get the message context associated with that label
                MessageContext value = (MessageContext) messageContexts.get(keyObj);

                boolean addToWorkingSet = true;

                // check to see if this message context was isolated
                if (isolatedMessageContexts != null) {
                    if (!isolatedMessageContexts.isEmpty()) {
                        // see if the message context was previously isolated
                        MessageContext valueIsolated =
                                (MessageContext) isolatedMessageContexts.get(keyObj);

                        if (valueIsolated != null) {
                            String idIsol = valueIsolated.getMessageID();

                            if (idIsol != null) {
                                if (idIsol.equals(value.getMessageID())) {
                                    // don't add to working set
                                    addToWorkingSet = false;
                                }
                            }
                        }
                    }
                }

                if (addToWorkingSet) {
                    // put the meta data entry in the list
                    workingSet.put(keyObj, value);
                }

            }

            // now we have a working set

            Set keySet2 = workingSet.keySet();
            Iterator itKeys2 = keySet2.iterator();

            while (itKeys2.hasNext()) {
                // expect the key to be a string
                String keyObj2 = (String) itKeys2.next();

                // get the message context associated with that label
                MessageContext mc = (MessageContext) workingSet.get(keyObj2);

                // construct a copy of the message context
                // that has been extracted from the object hierarchy
                MessageContext copyMC = mc.extractCopyMessageContext();
                
                // Don't persist the message of the other message contexts
                copyMC.setEnvelope(null);

                // put the modified entry in the list
                tmpMsgCtxMap.put(keyObj2, copyMC);

                // trace point
                if (log.isTraceEnabled()) {
                    log.trace(getLogCorrelationIDString() +
                              ":writeExternal():  getting working set entry  key [" + keyObj2 +
                              "]   message context ID[" + copyMC.getMessageID() + "]");
                }
            }

        }

        out.writeUTF("messagecontexts"); // write marker
        out.writeMap(tmpMsgCtxMap);
        out.writeUTF("metaMessageContextMap");
        out.writeMap(metaMessageContextMap);

        //---------------------------------------------------------
        // done
        //---------------------------------------------------------

    }


    /**
     * Restore the contents of the object that was previously saved.
     * <p/>
     * NOTE: The field data must read back in the same order and type
     * as it was written.  Some data will need to be validated when
     * resurrected.
     *
     * @param in The stream to read the object contents from
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
        log.trace(myClassName + ":readExternal():  BEGIN  bytes available in stream [" +
                in.available() + "]  ");

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

        long time = in.readLong();
        setLastTouchedTime(time);

        isComplete = in.readBoolean();
        key = (String) in.readObject();
        logCorrelationIDString = (String) in.readObject();
       
        // trace point
        if (log.isTraceEnabled()) {
            log.trace(myClassName + ":readExternal():  reading input stream for [" +
                      getLogCorrelationIDString() + "]  ");
        }

        //---------------------------------------------------------
        // properties
        //---------------------------------------------------------
        in.readUTF(); // read marker
        properties = in.readMap(new HashMap());

        //---------------------------------------------------------
        // axis operation meta data
        //---------------------------------------------------------

        // axisOperation is not usable until the meta data has been reconciled
        axisOperation = null;
        in.readUTF(); // read marker
        metaAxisOperation = (MetaDataEntry) in.readObject();

        //---------------------------------------------------------
        // axis service meta data
        //---------------------------------------------------------
        // axisService is not usable until the meta data has been reconciled
        in.readUTF(); // read marker
        metaAxisService = (MetaDataEntry) in.readObject();

        //---------------------------------------------------------
        // parent
        //---------------------------------------------------------
        // ServiceContext is not usable until it has been activated
        in.readUTF(); // read marker
        metaParent = (ServiceContext) in.readObject();

        //---------------------------------------------------------
        // HashMap messageContexts table
        //---------------------------------------------------------

        // set to empty until this can be activiated
        messageContexts = new HashMap();
        in.readUTF(); // read marker
        workingSet = in.readHashMap();
        in.readUTF(); // read marker
        metaMessageContextMap = in.readHashMap();
        
        //---------------------------------------------------------
        // done
        //---------------------------------------------------------
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

        // get the axis configuration
        AxisConfiguration axisConfig = cc.getAxisConfiguration();

        // We previously saved metaAxisService; restore it
        AxisService axisService = null;

        if (metaAxisService != null) {
            axisService = ActivateUtils.findService(axisConfig, metaAxisService.getClassName(),
                                                       metaAxisService.getQNameAsString(),
                                                       metaAxisService.getExtraName());
        }

        // We previously saved metaAxisOperation; restore it
        if (metaAxisOperation != null) {
            if (axisService != null) {
                this.axisOperation = ActivateUtils.findOperation(axisService,
                                                                    metaAxisOperation.getClassName(),
                                                                    metaAxisOperation.getQName());
            } else {
                this.axisOperation = ActivateUtils.findOperation(axisConfig,
                                                                    metaAxisOperation.getClassName(),
                                                                    metaAxisOperation.getQName());
            }
        }

        // the parent ServiceContext object was saved
        // either use the restored object or sync up with
        // an existing ServiceContext object
        if (metaParent != null) {
            // find out if a copy of the ServiceContext object exists on this
            // engine where this OperationContext is being restored/activated
            // if so, use that object instead of the restored object
            // in order to make sure that future changes to service-level
            // properties are preserved for future operations
            String groupName = metaParent.getGroupName();
            String serviceName = metaParent.getName();

            ServiceContext existingSC = null;

            // first look for the ServiceContext via the ServiceContextGroup
            ServiceGroupContext sgc = cc.getServiceGroupContext(groupName);

            if (sgc != null) {
                existingSC = sgc.findServiceContext(serviceName);
            }

            if (existingSC == null) {
                // we couldn't find the ServiceContext via the ServiceContextGroup
                // try via the set of existing operation contexts
                OperationContext existingOC =
                        cc.findOperationContext(getOperationName(), serviceName, groupName);

                if (existingOC != null) {
                    existingSC = (ServiceContext) existingOC.getParent();
                }
            }

            if (existingSC == null) {
                // could not find an existing ServiceContext
                // use the restored object
                metaParent.activate(cc);

                // set parent
                this.setParent(metaParent);
            } else {
                // switch over to the existing object
                this.setParent(existingSC);

                // do a copy of the properties from the restored object
                // to the existing ServiceContext
                // Should the copy be a non-destructive one?  That is,
                // if the key already exists in the properties table of the
                // existing object, should the value be overwritten from the
                // restored ojbect? For now, the decision is that the state
                // that has been preserved for a saved context object is
                // is important to be restored.
                metaParent.putContextProperties(existingSC, true);
            }
        } else {
            // set parent  to null
            this.setParent(metaParent);
        }

        // reseed the operation context map

        ServiceContext serv = getServiceContext();
        ConfigurationContext activeCC;
        if (serv != null) {
            activeCC = serv.getConfigurationContext();
        } else {
            activeCC = cc;
        }

        if (key != null) {
            // We only want to (re)register this if it's an outbound message
            String mepString = getAxisOperation().getMessageExchangePattern();
            if (mepString.equals(WSDL20_2006Constants.MEP_URI_OUT_ONLY)
                || mepString.equals(WSDL20_2004_Constants.MEP_URI_OUT_ONLY)
                || ((mepString.equals(WSDL20_2006Constants.MEP_URI_OUT_IN)
                    || mepString.equals(WSDL20_2004_Constants.MEP_URI_OUT_IN))
                    && !isComplete)) {
                    
                // make sure this OperationContext object is registered in the 
                // list maintained by the ConfigurationContext object
                boolean registrationSuceeded = activeCC.registerOperationContext(key, this, true);
                if (!registrationSuceeded) {
                    // trace point
                    if (log.isTraceEnabled()) {
                        log.trace(getLogCorrelationIDString()+ ":activate():  " +
                                        "OperationContext key [" + key
                              + "] already exists in ConfigurationContext map.  " +
                                        "This OperationContext ["
                              + this.toString() + "] was not added to the table.");
                    }
                }
            }
        }

        //-------------------------------------------------------
        // update the modified entries in the messageContexts table
        //-------------------------------------------------------
        // NOTE: an entry in the metaMessageContextMap must wait
        // for its corresponding active message context object
        // to call this operation context object so we don't
        // need to handle the metaMessagecontextMap table here

        if ((workingSet != null) && (!workingSet.isEmpty())) {
            Set keySet = workingSet.keySet();
            Iterator itKeys = keySet.iterator();

            while (itKeys.hasNext()) {
                // expect the key to be a string
                String keyObj = (String) itKeys.next();

                // get the message context associated with that label
                MessageContext value = (MessageContext) workingSet.get((Object) keyObj);

                // activate that object
                if (value != null) {
                    // trace point
                    if (log.isTraceEnabled()) {
                        log.trace(getLogCorrelationIDString() + ":activate():  key [" + keyObj +
                            "]  message id [" + value.getMessageID() + "]");
                    }

                    suppressWarnings = true;
                    value.activateWithOperationContext(this);
                    suppressWarnings = false;

                    if (messageContexts == null) {
                        messageContexts = new HashMap();
                    }
                }

                // put the entry in the "real" table
                // note that the key or the value could be null
                messageContexts.put(keyObj, value);
            }
        }

        //-------------------------------------------------------
        // done, reset the flag
        //-------------------------------------------------------
        needsToBeReconciled = false;

    }

    /**
     * Isolate the specified message context object
     * to prepare for serialization.  Instead of
     * saving the entire message context object,
     * just setup some metadata about the message
     * context.
     * <p/>
     * Note: this will remove the specified
     * message context object from the message context
     * table.
     *
     * @param mc The message context object
     */
    public void isolateMessageContext(MessageContext mc) {
        if (mc == null) {
            return;
        }

        if ((messageContexts == null) || (messageContexts.isEmpty())) {
            return;
        }

        // get the message ID from the message context
        String messageID = mc.getMessageID();

        if (messageID == null) {
            // can't locate it without identification
            return;
        }


        Iterator it = messageContexts.keySet().iterator();

        while (it.hasNext()) {
            // get the key
            Object keyObj = it.next();

            // get the value associated with that key
            MessageContext value = (MessageContext) messageContexts.get(keyObj);

            if (value != null) {
                String valueID = value.getMessageID();

                if ((valueID != null) && valueID.equals(messageID)) {
                    // found the input message context in our table

                    if (metaMessageContextMap == null) {
                        metaMessageContextMap = new HashMap();
                    }

                    // build a meta data entry
                    //           MessageContext class name
                    //           MessageContext messageID
                    //           key used in the original hashmap that is associated with this MessageContext
                    //                    note: this is typically something like "In", "Out", "Fault"
                    //
                    MetaDataEntry metaData = new MetaDataEntry(value.getClass().getName(),
                                                               value.getMessageID(),
                                                               keyObj.toString());

                    // put the meta data entry in the list
                    // note that if the entry was already in the list,
                    // this will replace that entry
                    metaMessageContextMap.put(keyObj, metaData);

                    // don't change the original table - there's potentially lots of areas that
                    // grab the table
                    //  // now remove the original entry from the messageContexts table
                    //  messageContexts.remove(keyObj);

                    // put the original entry in the temporary list
                    if (isolatedMessageContexts == null) {
                        isolatedMessageContexts = new HashMap();
                    }

                    // note that if the entry was already in the list,
                    // this will replace that entry
                    isolatedMessageContexts.put(keyObj, value);

                    // trace point
                    if (log.isTraceEnabled()) {
                        log.trace(getLogCorrelationIDString() +
                            ":isolateMessageContext():  set up message context id[" + valueID +
                            "]  with key [" + keyObj.toString() +
                            "] from messageContexts table to prepare for serialization.");
                    }

                    break;
                }
            }
        }
    }


    /**
     * Restore the specified MessageContext object in the
     * table used to hold the message contexts associated
     * with this operation.
     *
     * @param msg The message context object
     */
    public void restoreMessageContext(MessageContext msg) {
        // see if the activation has been done
        if (needsToBeReconciled) {
            // nope, need to do the activation first
            if (debugEnabled) {
                log.debug(getLogCorrelationIDString() +
                          ":restoreMessageContext(): *** WARNING : need to invoke activate() prior to restoring the MessageContext to the list.");
            }

            return;
        }

        if (msg == null) {
            return;
        }

        String msgID = msg.getMessageID();

        if (msgID == null) {
            if (debugEnabled) {
                // can't identify message context
                log.debug(getLogCorrelationIDString() +
                        ":restoreMessageContext(): *** WARNING : MessageContext does not have a message ID.");
            }
            return;
        }

        // first check the metaMessageContextMap to see if
        // the specified message context object matches any
        // of the metadata entries.

        if ((metaMessageContextMap != null) && (!metaMessageContextMap.isEmpty())) {
            Iterator itMeta = metaMessageContextMap.keySet().iterator();

            while (itMeta.hasNext()) {
                String keyM = (String) itMeta.next();

                MetaDataEntry valueM = (MetaDataEntry) metaMessageContextMap.get(keyM);
                String valueM_ID;

                if (valueM != null) {
                    valueM_ID = valueM.getQNameAsString();

                    if (msgID.equals(valueM_ID)) {
                        String label = valueM.getExtraName();

                        if (messageContexts == null) {
                            messageContexts = new HashMap();
                        }

                        // put the message context into the messageContexts table
                        messageContexts.put(label, msg);

                        // remove the metadata from the metadata table
                        metaMessageContextMap.remove(keyM);

                        if (log.isTraceEnabled()) {
                            log.trace(getLogCorrelationIDString() +
                                ":restoreMessageContext():  restored   label [" + label +
                                "]    message ID [" + msg.getMessageID() + "]");
                        }

                        break;
                    }
                }
            }
        } else
            // see if we can put the msg directly in the messageContexts table
            if ((messageContexts != null) && (!messageContexts.isEmpty())) {
                Iterator itList = messageContexts.keySet().iterator();

                while (itList.hasNext()) {
                    String key = (String) itList.next();

                    MessageContext value = (MessageContext) messageContexts.get(key);
                    String valueID;

                    if (value != null) {
                        valueID = value.getMessageID();

                        if (msgID.equals(valueID)) {
                            // update the entry
                            messageContexts.put(key, msg);
                        }
                    }
                }
            }

    }

    /**
     * Get the name associated with the operation.
     *
     * @return The name String
     */
    public String getOperationName() {
        String opName = null;

        if (axisOperation != null) {
            QName qname = axisOperation.getName();
            if (qname != null) {
                opName = qname.getLocalPart();
            }
        }

        return opName;
    }

    /**
     * Get the name associated with the service.
     *
     * @return The name String
     */
    public String getServiceName() {
        String srvName = null;

        ServiceContext sc = (ServiceContext) getParent();

        if (sc == null) {
            sc = metaParent;
        }

        if (sc != null) {
            srvName = sc.getName();
        }

        return srvName;
    }

    /**
     * Get the name associated with the service group.
     *
     * @return The name String
     */
    public String getServiceGroupName() {
        String srvGroupName = null;

        ServiceContext sc = (ServiceContext) getParent();

        if (sc == null) {
            sc = metaParent;
        }

        if (sc != null) {
            srvGroupName = sc.getGroupName();
        }

        return srvGroupName;
    }


    /**
     * Compares key parts of the state from the current instance of
     * this class with the specified instance to see if they are
     * equivalent.
     * <p/>
     * This differs from the java.lang.Object.equals() method in
     * that the equals() method generally looks at both the
     * object identity (location in memory) and the object state
     * (data).
     * <p/>
     *
     * @param ctx The object to compare with
     * @return TRUE if this object is equivalent with the specified object
     *         that is, key fields match
     *         FALSE, otherwise
     */
    public boolean isEquivalent(OperationContext ctx) {
        // NOTE: the input object is expected to exist (ie, be non-null)

        if (this.isComplete != ctx.isComplete()) {
            return false;
        }

        if (!this.axisOperation.equals(ctx.getAxisOperation())) {
            return false;
        }

        // TODO: consider checking the parent objects for equivalency

        // TODO: consider checking fields from the super class for equivalency

        return true;
    }


    /**
     * Get the ID associated with this object instance.
     *
     * @return A string that can be output to a log file as an identifier
     *         for this object instance.  It is suitable for matching related log
     *         entries.
     */
    public String getLogCorrelationIDString() {
        if (logCorrelationIDString == null) {
            logCorrelationIDString = myClassName + "@" + UIDGenerator.generateUID();
        }
        return logCorrelationIDString;
    }

    public ConfigurationContext getRootContext() {
        return this.getConfigurationContext();
    }

}
