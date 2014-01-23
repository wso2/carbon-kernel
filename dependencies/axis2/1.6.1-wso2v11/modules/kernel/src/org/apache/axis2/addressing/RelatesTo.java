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


package org.apache.axis2.addressing;

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
import java.util.ArrayList;

/**
 * Class RelatesTo
 */
public class RelatesTo implements Externalizable, SafeSerializable {

    /*
     * setup for logging
     */
    private static final Log log = LogFactory.getLog(RelatesTo.class);

    private static final String myClassName = "RelatesTo";

    /**
     * @serial The serialization version ID tracks the version of the class.
     * If a class definition changes, then the serialization/externalization
     * of the class is affected. If a change to the class is made which is
     * not compatible with the serialization/externalization of the class,
     * then the serialization version ID should be updated.
     * Refer to the "serialVer" utility to compute a serialization
     * version ID.
     */
    private static final long serialVersionUID = -1120384315333414960L;

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
     * Field relationshipType
     */
    private String relationshipType;

    /**
     * Field value
     */
    private String value;

    private ArrayList extensibilityAttributes = null;

    /**
     * Constructor RelatesTo
     */
    public RelatesTo() {
    }

    /**
     * Constructor RelatesTo
     *
     * @param value
     */
    public RelatesTo(String value) {
        this.value = value;
    }

    /**
     * Constructor RelatesTo
     *
     * @param value
     * @param relationshipType
     */
    public RelatesTo(String value, String relationshipType) {
        this.value = value;
        this.relationshipType = relationshipType;
    }

    /**
     * Method getRelationshipType. If the relationship type has not been set it returns
     * the default value {@link AddressingConstants.Final.WSA_DEFAULT_RELATIONSHIP_TYPE}
     */
    public String getRelationshipType() {
        return (relationshipType != null && !"".equals(relationshipType) ?
                relationshipType :
                AddressingConstants.Final.WSA_DEFAULT_RELATIONSHIP_TYPE);
    }

    /**
     * Method getValue
     */
    public String getValue() {
        return value;
    }

    /**
     * Method setRelationshipType
     *
     * @param relationshipType
     */
    public void setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
    }

    /**
     * Method setValue
     *
     * @param value
     */
    public void setValue(String value) {
        this.value = value;
    }

    public ArrayList getExtensibilityAttributes() {
        return extensibilityAttributes;
    }

    public void setExtensibilityAttributes(ArrayList extensibilityAttributes) {
        this.extensibilityAttributes = extensibilityAttributes;
    }

    /*
    *  (non-Javadoc)
    * @see java.lang.Object#toString()
    */
    public String toString() {
        return "Identifier: " + value
                + ", Relationship type: " + relationshipType;
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
        // in order to handle future changes to the object 
        // definition, be sure to maintain the 
        // object level identifiers
        //---------------------------------------------------------
        // serialization version ID
        out.writeLong(serialVersionUID);

        // revision ID
        out.writeInt(revisionID);

        //---------------------------------------------------------
        // various strings
        //---------------------------------------------------------

        // String relationshipType
        out.writeObject(relationshipType);

        // String value
        out.writeObject(value);

        //---------------------------------------------------------
        // collections and lists
        //---------------------------------------------------------
        out.writeList(extensibilityAttributes);

    }


    /**
     * Restore the contents of the object that was
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
        // various strings
        //---------------------------------------------------------

        // String relationshipType
        relationshipType = (String)in.readObject();

        // String value
        value = (String) in.readObject();

        //---------------------------------------------------------
        // collections and lists
        //---------------------------------------------------------

        // ArrayList extensibilityAttributes
        extensibilityAttributes = in.readArrayList();
    }

}
