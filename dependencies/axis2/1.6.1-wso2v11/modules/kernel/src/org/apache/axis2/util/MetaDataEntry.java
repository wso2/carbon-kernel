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

package org.apache.axis2.util;

import org.apache.axis2.context.externalize.SafeObjectInputStream;
import org.apache.axis2.context.externalize.SafeObjectOutputStream;
import org.apache.axis2.context.externalize.SafeSerializable;

import javax.xml.namespace.QName;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;


/**
 * An internal class for holding a set of information
 * about an object.
 */
public class MetaDataEntry implements Externalizable, SafeSerializable {
    // serialization identifier
    private static final long serialVersionUID = 8978361069526299875L;

    // supported revision levels, add a new level to manage compatible changes
    private static final int REVISION_2 = 2;
    // current revision level of this object
    private static final int revisionID = REVISION_2;

    // data to keep on an object

    private String className = null;
    private String qnameAsString = null;
    private String extraName = null;

    // list of MetaDataEntry objects that are owned by the
    // original object referred to by this MetaDataEntry
    private ArrayList children = null;

    // marker to indicate end-of-list
    public static String END_OF_LIST = "LAST_ENTRY";

    /**
     * Simple constructor
     */
    public MetaDataEntry() {
    }

    /**
     * Constructor
     * @param className name of the object class
     * @param qnameAsString an expanded version of the QName of this object
     */
    public MetaDataEntry(String className, String qnameAsString) {
        this.className = className;
        this.qnameAsString = qnameAsString;
    }

    /**
     * Constructor
     * @param className name of the object class
     * @param qnameAsString an expanded version of the QName of this object
     * @param extraName an additional name associated withe the object
     */
    public MetaDataEntry(String className, String qnameAsString, String extraName) {
        this.className = className;
        this.qnameAsString = qnameAsString;
        this.extraName = extraName;
    }

    /**
     * Constructor
     * @param className name of the object class
     * @param qnameAsString an expanded version of the QName of this object
     * @param children an ArrayList containing MetaDataEntries for owned objects
     */
    public MetaDataEntry(String className, String qnameAsString, ArrayList children) {
        this.className = className;
        this.qnameAsString = qnameAsString;
        this.children = children;
    }


    /**
     * Get the class name
     *
     * @return the class name string
     */
    public String getClassName() {
        return className;
    }


    /**
     * Set the class name
     *
     * @param c the class name string
     */
    public void setClassName(String c) {
        className = c;
    }


    /**
     * Get the QName
     *
     * @return the QName based on the qnameAsString value
     */
    public QName getQName() {
        if (qnameAsString != null) {
            return QName.valueOf(qnameAsString);
        } else {
            return null;
        }
    }


    /**
     * Set the QName
     *
     * @param q the QName
     */
    public void setQName(QName q) {
        if (q != null) {
            qnameAsString = q.toString();
        } else {
            qnameAsString = null;
        }
    }

    /**
     * Set the QName
     *
     * @param n the QName as a string
     */
    public void setQName(String n) {
        qnameAsString = n;
    }


    /**
     * Get the QName as a string
     *
     * @return the QName as a string
     */
    public String getQNameAsString() {
        return qnameAsString;
    }


    /**
     * This is a convenience method.
     * Returns the string that is used as a name.
     *
     * @return the name
     */
    public String getName() {
        return qnameAsString;
    }


    /**
     * Get the additional name associated with the object
     *
     * @return the additional name string
     */
    public String getExtraName() {
        return extraName;
    }


    /**
     * Set the additional name associated with the object
     *
     * @param e the extra name string
     */
    public void setExtraName(String e) {
        extraName = e;
    }


    /**
     * Indicates whether the list is empty or not
     *
     * @return false for a non-empty list, true for an empty list
     */
    public boolean isListEmpty() {
        return children == null || children.isEmpty();
    }


    /**
     * Get the list
     *
     * @return the array list
     */
    public ArrayList getChildren() {
        return children;
    }


    /**
     * Set the list
     *
     * @param L the ArrayList of MetaDataEntry objects
     */
    public void setChildren(ArrayList L) {
        children = L;
    }

    /**
     * Add to the list
     *
     * @param e the MetaDataEntry object to add to the list
     */
    public void addToList(MetaDataEntry e) {
        if (children == null) {
            children = new ArrayList();
        }
        children.add(e);
    }

    /**
     * Remove the list
     */
    public void removeList() {
        children = null;
    }


    // message strings
    private static final String UNSUPPORTED_SUID = "Serialization version ID is not supported.";
    private static final String UNSUPPORTED_REVID = "Revision ID is not supported.";


    /**
     * Save the contents of this object
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
        // various simple fields
        //---------------------------------------------------------
        out.writeObject(className);
        out.writeObject(qnameAsString);
        out.writeObject(extraName);
        out.writeList(children);

    }


    /**
     * Restore the contents of the object that was
     * previously saved.
     * <p/>
     * NOTE: The field data must read back in the same order and type
     * as it was written.
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
            throw new ClassNotFoundException(UNSUPPORTED_SUID);
        }

        // make sure the object data is in a revision level we can handle
        if (revID != REVISION_2) {
            throw new ClassNotFoundException(UNSUPPORTED_REVID);
        }

        //---------------------------------------------------------
        // various simple fields
        //---------------------------------------------------------

        className = (String) in.readObject();
        qnameAsString = (String) in.readObject();
        extraName = (String) in.readObject();
        children = in.readArrayList();

    }

}
