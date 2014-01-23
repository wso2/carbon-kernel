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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.context.externalize.ExternalizeConstants;
import org.apache.axis2.context.externalize.SafeObjectInputStream;
import org.apache.axis2.context.externalize.SafeObjectOutputStream;
import org.apache.axis2.context.externalize.SafeSerializable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;


/**
 * Class Parameter
 */
public class Parameter implements Externalizable, SafeSerializable {

    /*
     * setup for logging
     */
    private static final Log log = LogFactory.getLog(Parameter.class);

    private static final String myClassName = "Parameter";

    /**
     * @serial The serialization version ID tracks the version of the class.
     * If a class definition changes, then the serialization/externalization
     * of the class is affected. If a change to the class is made which is
     * not compatible with the serialization/externalization of the class,
     * then the serialization version ID should be updated.
     * Refer to the "serialVer" utility to compute a serialization
     * version ID.
     */
    private static final long serialVersionUID = -6601664200673063531L;

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
     * Field  ANY_PARAMETER
     */
    public final static int ANY_PARAMETER = 0;
    /**
     * Field TEXT_PARAMETER
     */
    public final static int TEXT_PARAMETER = 1;

    /**
     * Field OM_PARAMETER
     */
    public final static int OM_PARAMETER = 2;

    /**
     * Field type
     */
    private int type = TEXT_PARAMETER;

    /**
     * Field locked
     */
    private boolean locked;

    /**
     * Field name
     */
    private String name;

    /**
     * to store the parameter element
     * <parameter name="ServiceClass1">
     * org.apache.axis2.sample.echo.EchoImpl</parameter>
     */
    private OMElement parameterElement;

    /**
     * Field value
     */
    private Object value;
    
    private boolean _transient;  // Indicates that the parameter is transient (not persisted)

    //To check whether the parameter is editable or not ,
    // if the value is false then no one can call setvalue
    // TODO
    // Currently the editable field is not persisted. This seems like a problem.
    private boolean editable = true;

    /**
     * Constructor.
     */
    public Parameter() {
    }

    /**
     * Constructor from name and value.
     *
     * @param name
     * @param value
     */
    public Parameter(String name, Object value) {
        this.name = name;
        this.value = value;
        parseValueForType(this.value);
    }

    /**
     * Method getName.
     *
     * @return Returns String.
     */
    public String getName() {
        return name;
    }

    public OMElement getParameterElement() {
        return this.parameterElement;
    }

    /**
     * Method getParameterType.
     *
     * @return Returns int.
     */
    public int getParameterType() {
        return type;
    }

    /**
     * Method getValue.
     *
     * @return Returns Object.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Method isLocked.
     *
     * @return Returns boolean.
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * Method setLocked.
     *
     * @param value
     */
    public void setLocked(boolean value) {
        locked = value;
    }

    /**
     * Method setName.
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    public void setParameterElement(OMElement element) {
        this.parameterElement = element;
    }

    public void setParameterType(int type) {
        this.type = type;
    }

    /**
     * Method setValue.
     *
     * @param value
     */
    public void setValue(Object value) {
        if(!editable) {
            log.debug("Parameter "  + getName() + "  can not be edit");
            return;
        }
        parseValueForType(value);
        this.value = value;
    }

    /**
     * The purpose of this method is to pars the injected value for its type.
     * Type will be set using setParameterType.
     * @param value
     */
    private void parseValueForType(Object value) {
        if (value instanceof String) {
            setParameterType(TEXT_PARAMETER);
        } else if (value instanceof OMElement) {
            setParameterType(OM_PARAMETER);
        } else {
            setParameterType(ANY_PARAMETER);
        }
    }

    public String toString() {
        return "Parameter : " + name + "=" + value;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Parameter) {
            return ((Parameter) obj).name.equals(name);
        }
        return false;
    }

    public int hashCode() {
        return name.hashCode();
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
        
        // Don't write out transient parameters
        if (this.isTransient()) {
            return;  
        }
        
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
        // simple fields
        //---------------------------------------------------------

        out.writeInt(type);
        out.writeBoolean(locked);
        out.writeObject(name);

        //---------------------------------------------------------
        // object fields
        //---------------------------------------------------------

        // TODO: investigate serializing the OMElement more efficiently
        // This currently will basically serialize the given OMElement
        // to a String but will build the OMTree in the memory

        String tmp = null;

        if (parameterElement != null) {
            tmp = parameterElement.toString();
        }
        out.writeObject(tmp); // parameterElement
        out.writeObject(value);
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
        // simple fields
        //---------------------------------------------------------

        type = in.readInt();
        locked = in.readBoolean();
        name = (String) in.readObject();

        //---------------------------------------------------------
        // object fields
        //---------------------------------------------------------

        // TODO: investigate serializing the OMElement more efficiently
        // This currently will basically serialize the given OMElement
        // to a String but will build the OMTree in the memory

        // treat as an object, don't do UTF
        String tmp = (String) in.readObject();

        // convert to an OMElement
        if (tmp != null) {
            try {
                OMElement docElement = AXIOMUtil.stringToOM(tmp);

                if (docElement != null) {
                    parameterElement = docElement;
                } else {
                    // TODO: error handling if can't create an OMElement
                    parameterElement = null;
                }
            }
            catch (Exception exc) {
                // TODO: error handling if can't create an OMElement
                parameterElement = null;
            }
        } else {
            parameterElement = null;
        }

        // TODO: error handling if this can't be serialized
        value = in.readObject();

        //---------------------------------------------------------
        // done
        //---------------------------------------------------------

    }


    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public boolean isTransient() {
        return _transient;
    }

    public void setTransient(boolean _transient) {
        this._transient = _transient;
    }
}
