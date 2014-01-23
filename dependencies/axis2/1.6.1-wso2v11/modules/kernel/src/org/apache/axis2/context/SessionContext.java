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

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.externalize.ExternalizeConstants;
import org.apache.axis2.context.externalize.SafeObjectInputStream;
import org.apache.axis2.context.externalize.SafeObjectOutputStream;
import org.apache.axis2.context.externalize.SafeSerializable;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.DependencyManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * All the engine components are stateless across the executions and all the states should be kept in the
 * Contexts, there are three context Global, Session and Message.
 */
public class SessionContext extends AbstractContext 
    implements Externalizable, SafeSerializable {

    /**
     * @serial The serialization version ID tracks the version of the class.
     * If a class definition changes, then the serialization/externalization
     * of the class is affected. If a change to the class is made which is
     * not compatible with the serialization/externalization of the class,
     * then the serialization version ID should be updated.
     * Refer to the "serialVer" utility to compute a serialization
     * version ID.
     */
    private static final long serialVersionUID = -1100610673067568556L;

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


    // TODO: investigate whether these collections need to be saved
    private transient Map<String, ServiceContext> serviceContextMap = new HashMap<String, ServiceContext>();
    private transient Map<String, ServiceGroupContext> serviceGroupContextMap = new HashMap<String, ServiceGroupContext>();

    private String cookieID;

    private static final Log log = LogFactory.getLog(SessionContext.class);
    private static final String myClassName = "SessionContext";

    // current time out interval is 30 secs. Need to make this configurable
    public long sessionContextTimeoutInterval = 30 * 1000;

    /**
     * @param parent
     */
    public SessionContext(AbstractContext parent) {
        super(parent);
    }

    public SessionContext() {
    }

    public void init(AxisConfiguration axisConfiguration) throws AxisFault {
    }

    public ServiceContext getServiceContext(AxisService axisService) {
        return (ServiceContext) serviceContextMap.get(axisService.getName());
    }

    public void addServiceContext(ServiceContext serviceContext) {
        serviceContextMap.put(serviceContext.getAxisService().getName(), serviceContext);
    }

    public void addServiceGroupContext(ServiceGroupContext serviceGroupContext) {
        String serviceGroupID = serviceGroupContext.getDescription().getServiceGroupName();
        serviceGroupContextMap.put(serviceGroupID, serviceGroupContext);
    }

    public ServiceGroupContext getServiceGroupContext(String serviceGroupID) {
        return (ServiceGroupContext) serviceGroupContextMap.get(serviceGroupID);
    }

    public String getCookieID() {
        return cookieID;
    }

    public void setCookieID(String cookieID) {
        this.cookieID = cookieID;
    }

    /**
     * ServiceContext and ServiceGroupContext are not getting automatically garbage collectible. And there
     * is no specific way for some one to go and make it garbage collectable.
     * So the current solution is to make them time out. So the logic is that, there is a timer task
     * in each and every service group which will check for the last touched time. And if it has not
     * been touched for some time, the timer task will remove it from the memory.
     * The touching logic happens like this. Whenever there is a call to addMessageContext in the operationContext
     * it will go and update operationCOntext -> serviceContext -> serviceGroupContext.
     */
    public void touch() {
        lastTouchedTime = new Date().getTime();
        if (parent != null) {
            parent.touch();
        }
    }

    public long getLastTouchedTime() {
        return lastTouchedTime;
    }

    public Iterator<ServiceGroupContext> getServiceGroupContext() {
        if (serviceGroupContextMap != null) {
            if (serviceGroupContextMap.isEmpty()) {
                return null;
            }
            return serviceGroupContextMap.values().iterator();
        } else {
            return null;
        }
    }

    protected void finalize() throws Throwable {
        super.finalize();
        if (serviceGroupContextMap != null && !serviceGroupContextMap.isEmpty()) {
            Iterator<ServiceGroupContext> valuse = serviceGroupContextMap.values().iterator();
            while (valuse.hasNext()) {
                ServiceGroupContext serviceGroupContext = (ServiceGroupContext) valuse.next();
                cleanupServiceContextes(serviceGroupContext);
            }
        }
    }

    private void cleanupServiceContextes(ServiceGroupContext serviceGroupContext) {
        Iterator<ServiceContext> serviceContecxtes = serviceGroupContext.getServiceContexts();
        while (serviceContecxtes.hasNext()) {
            ServiceContext serviceContext = (ServiceContext) serviceContecxtes.next();
            DependencyManager.destroyServiceObject(serviceContext);
        }
    }

    /* ===============================================================
    * Externalizable support
    * ===============================================================
    */

    /**
     * Save the contents of this object.
     * <p/>
     * NOTE: Transient fields and static fields are not saved.
     *
     * @param out The stream to write the object contents to
     * @throws IOException
     */
    public void writeExternal(ObjectOutput o) throws IOException {
        SafeObjectOutputStream out = SafeObjectOutputStream.install(o);
        // write out contents of this object

        // NOTES: For each item, where appropriate,
        //        write out the following information, IN ORDER:
        //           the class name
        //           the active or empty flag
        //           the data length, if appropriate
        //           the data   

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

        out.writeLong(sessionContextTimeoutInterval);
        out.writeObject(cookieID);

        //---------------------------------------------------------
        // properties
        //---------------------------------------------------------
        out.writeMap(getProperties());

        //---------------------------------------------------------
        // "nested"
        //---------------------------------------------------------
        out.writeObject(parent);

    }


    /**
     * Restore the contents of the MessageContext that was
     * previously saved.
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
        // trace point
        if (log.isTraceEnabled()) {
            log.trace(myClassName + ":readExternal():  BEGIN  bytes available in stream [" +
                    in.available() + "]  ");
        }

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

        sessionContextTimeoutInterval = in.readLong();
        cookieID = (String) in.readObject();

        //---------------------------------------------------------
        // properties
        //---------------------------------------------------------
        properties = in.readMap(new HashMap());

        //---------------------------------------------------------
        // "nested"
        //---------------------------------------------------------

        // parent
        parent = (AbstractContext) in.readObject();

        //---------------------------------------------------------
        // done
        //---------------------------------------------------------

        serviceContextMap = new HashMap<String, ServiceContext>();
        serviceGroupContextMap = new HashMap<String, ServiceGroupContext>();
    }

    public ConfigurationContext getRootContext() {
        // Session Context does not live within the hierarchy
        return null;
    }


}
