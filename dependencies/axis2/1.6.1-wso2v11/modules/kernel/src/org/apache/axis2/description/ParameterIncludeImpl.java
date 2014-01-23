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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.externalize.ExternalizeConstants;
import org.apache.axis2.context.externalize.SafeObjectInputStream;
import org.apache.axis2.context.externalize.SafeObjectOutputStream;
import org.apache.axis2.context.externalize.SafeSerializable;
import org.apache.axis2.deployment.DeploymentConstants;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * Class ParameterIncludeImpl
 */
public class ParameterIncludeImpl 
    implements ParameterInclude, Externalizable, SafeSerializable {

    /*
     * setup for logging
     */
    private static final Log log = LogFactory.getLog(ParameterIncludeImpl.class);
    private static boolean DEBUG_ENABLED = log.isTraceEnabled();
    private static boolean DEBUG_PROPERTY_SET = log.isDebugEnabled();

    private static final String myClassName = "ParameterIncludeImpl";

    /**
     * @serial The serialization version ID tracks the version of the class.
     * If a class definition changes, then the serialization/externalization
     * of the class is affected. If a change to the class is made which is
     * not compatible with the serialization/externalization of the class,
     * then the serialization version ID should be updated.
     * Refer to the "serialVer" utility to compute a serialization
     * version ID.
     */
    private static final long serialVersionUID = 8153736719090126891L;

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
     * Field parmeters
     */
    protected Map<String, Parameter> parameters;

    /**
     * Constructor ParameterIncludeImpl.
     */
    public ParameterIncludeImpl() {
        // Use a capacity large enough to prevent
        // resizing
        parameters = new HashMap<String, Parameter>(64);
    }

    /**
     * Method addParameter
     *
     * @param param
     */
    public void addParameter(Parameter param) {
        if (param != null) {
            synchronized (parameters) {
                parameters.put(param.getName(), param);
                try {
                    parameters.put(param.getName(), param);
                } catch (ConcurrentModificationException cme) {
                    // The ParameteterIncludeImpl is supposed to be immutable after it is populated.
                    // But alas, sometimes the callers forget and try to add new items.  If
                    // this occurs, swap over to the slower ConcurrentHashMap and continue.
                    if (log.isDebugEnabled()) {
                        log.debug("ConcurrentModificationException Occured...changing to ConcurrentHashMap");
                        log.debug("The exception is: " + cme);
                    }

                    Map newMap = new ConcurrentHashMap(parameters);
                    newMap.put(param.getName(), param);
                    parameters = newMap;
                }

                if (DEBUG_ENABLED) {
                    this.debugParameterAdd(param);
                }

            }
            
            if (DEBUG_ENABLED) {
                this.debugParameterAdd(param);
            }
        }
    }

    public void removeParameter(Parameter param) throws AxisFault {
        synchronized (parameters) {
            try {
                parameters.remove(param.getName());
            } catch (ConcurrentModificationException cme) {
                // The ParameteterIncludeImpl is supposed to be immutable after it is populated.
                // But alas, sometimes the callers forget and try to add new items.  If
                // this occurs, swap over to the slower ConcurrentHashMap and continue.
                if (log.isDebugEnabled()) {
                    log.debug("ConcurrentModificationException Occured...changing to ConcurrentHashMap");
                    log.debug("The exception is: " + cme);
                }

                Map newMap = new ConcurrentHashMap(parameters);
                newMap.remove(param.getName());
                parameters = newMap;
            }
        } 
    }

    /**
     * Since at runtime it parameters may be modified
     * to get the original state this method can be used
     *
     * @param parameters <code>OMElement</code>
     * @throws AxisFault
     */
    public void deserializeParameters(OMElement parameters) throws AxisFault {
        Iterator iterator =
                parameters.getChildrenWithName(new QName(DeploymentConstants.TAG_PARAMETER));

        while (iterator.hasNext()) {

            // this is to check whether some one has locked the parmeter at the top level
            OMElement parameterElement = (OMElement) iterator.next();
            Parameter parameter = new Parameter();

            // setting parameterElement
            parameter.setParameterElement(parameterElement);

            // setting parameter Name
            OMAttribute paraName =
                    parameterElement.getAttribute(new QName(DeploymentConstants.ATTRIBUTE_NAME));

            parameter.setName(paraName.getAttributeValue());

            // setting parameter Value (the child element of the parameter)
            OMElement paraValue = parameterElement.getFirstElement();

            if (paraValue != null) {
                parameter.setValue(parameterElement);
                parameter.setParameterType(Parameter.OM_PARAMETER);
            } else {
                String paratextValue = parameterElement.getText();

                parameter.setValue(paratextValue);
                parameter.setParameterType(Parameter.TEXT_PARAMETER);
            }

            // setting locking attribute
            OMAttribute paraLocked =
                    parameterElement.getAttribute(new QName(DeploymentConstants.ATTRIBUTE_LOCKED));

            if (paraLocked != null) {
                String lockedValue = paraLocked.getAttributeValue();

                if ("true".equals(lockedValue)) {
                    parameter.setLocked(true);
                } else {
                    parameter.setLocked(false);
                }
            }

            addParameter(parameter);
        }
    }

    /**
     * Method getParameter.
     *
     * @param name
     * @return Returns parameter.
     */
    public Parameter getParameter(String name) {
        return parameters.get(name);
    }

    public ArrayList<Parameter> getParameters() {
        synchronized(parameters) {
            return new ArrayList<Parameter>(parameters.values());
        }
    }

    // to check whether the parameter is locked at any level
    public boolean isParameterLocked(String parameterName) {
        return false;
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
        // write out contents of this object

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
        // collection of parameters
        //---------------------------------------------------------
        out.writeMap(parameters);

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
        // collection of parameters
        //---------------------------------------------------------
        in.readMap(parameters);

        //---------------------------------------------------------
        // done
        //---------------------------------------------------------
    }

    /**
     * Debug for for property key and value.
     * @param key
     * @param value
     */
    private void debugParameterAdd(Parameter parameter) {
        if (DEBUG_PROPERTY_SET) {
            String key = parameter.getName();
            Object value = parameter.getValue();
            String className = (value == null) ? "null" : value.getClass().getName();
            String classloader = "null";
            if(value != null) {
                ClassLoader cl = Utils.getObjectClassLoader(value);
                if(cl != null) {
                    classloader = cl.toString();
                }
            }
            String valueText = (value instanceof String) ? value.toString() : null;
            String identity = getClass().getName() + '@' + 
                Integer.toHexString(System.identityHashCode(this));
            
            log.debug("==================");
            log.debug(" Parameter add on object " + identity);
            log.debug("  Key =" + key);
            if (valueText != null) {
                log.debug("  Value =" + valueText);
            }
            log.debug("  Value Class = " + className);
            log.debug("  Value Classloader = " + classloader);
            log.debug(  "Call Stack = " + JavaUtils.callStackToString());
            log.debug("==================");
        }
    }
}
