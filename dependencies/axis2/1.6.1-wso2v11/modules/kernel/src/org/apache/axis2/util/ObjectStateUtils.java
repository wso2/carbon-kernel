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

import org.apache.axis2.context.externalize.ActivateUtils;
import org.apache.axis2.context.externalize.ExternalizeConstants;
import org.apache.axis2.context.externalize.SafeObjectInputStream;
import org.apache.axis2.context.externalize.SafeObjectOutputStream;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.transport.TransportListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Utility to write, read and activate externalized Objects
 */
public class ObjectStateUtils implements ExternalizeConstants {
    /*
     * setup for logging
     */
    private static final Log log = LogFactory.getLog(ObjectStateUtils.class);

    // used to indicate an valid "null" object,
    // typically used in key-value pairs where a non-null key refers to a null
    // value
    public static String NULL_OBJECT = "NULL_OBJ";

    // message/trace/logging strings
    public static final String UNSUPPORTED_SUID = "Serialization version ID is not supported.";

    public static final String UNSUPPORTED_REVID = "Revision ID is not supported.";

    // --------------------------------------------------------------------
    // Save/Restore methods
    // --------------------------------------------------------------------

    /**
     * Write a string to the specified output stream.
     * 
     * @param o The output stream
     * @param str The string to write
     * @param desc A text description to use for logging
     * @throws IOException Exception
     */
    public static void writeString(ObjectOutput o, String str, String desc) throws IOException {
        SafeObjectOutputStream out = SafeObjectOutputStream.install(o);
        out.writeUTF(desc);
        out.writeObject(str);
    }

    /**
     * Read a string from the specified input stream. Returns null if no string is available.
     * 
     * @param i The input stream
     * @param desc A text description to use for logging
     * @return The string or null, if not available
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static String readString(ObjectInput i, String desc) throws IOException,
                                                               ClassNotFoundException {
        SafeObjectInputStream in = SafeObjectInputStream.install(i);

        // Get the marker
        in.readUTF();

        // Get the object
        return (String) in.readObject();
    }

    /**
     * Write an object to the specified output stream.
     * 
     * @param o The output stream
     * @param obj The object to write
     * @param desc A text description to use for logging
     * @throws IOException Exception
     */
    public static void writeObject(ObjectOutput o, Object obj, String desc) throws IOException {
        SafeObjectOutputStream out = SafeObjectOutputStream.install(o);
        out.writeUTF(desc);
        out.writeObject(obj);
    }

    /**
     * Read an object from the specified input stream. Returns null if no object is available.
     * 
     * @param i The input stream
     * @param desc A text description to use for logging
     * @return The object or null, if not available
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Object readObject(ObjectInput i, String desc) throws IOException,
                                                               ClassNotFoundException {
        SafeObjectInputStream in = SafeObjectInputStream.install(i);
        in.readUTF(); // Read Marker
        return in.readObject();
    }

    /**
     * Write an array of objects to the specified output stream. NOTE: each object in the array
     * should implement either java.io.Serializable or java.io.Externalizable in order to be saved
     * 
     * @param o The output stream
     * @param al The ArrayList to write
     * @param desc A text description to use for logging
     * @throws IOException Exception
     */
    public static void writeArrayList(ObjectOutput o, ArrayList al, String desc) 
      throws IOException {
        SafeObjectOutputStream out = SafeObjectOutputStream.install(o);
        out.writeUTF(desc);
        out.writeList(al);
    }

    /**
     * Reads an array of objects from the specified input stream. Returns null if no array is
     * available. NOTE: each object in the array should implement either java.io.Serializable or
     * java.io.Externalizable in order to be saved
     * 
     * @param i The input stream
     * @param desc A text description to use for logging
     * @return The ArrayList or null, if not available
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static ArrayList readArrayList(ObjectInput i, String desc) throws IOException {
        SafeObjectInputStream in = SafeObjectInputStream.install(i);
        in.readUTF();
        return in.readArrayList();
    }

    /**
     * Write a hashmap of objects to the specified output stream. NOTE: each object in the map
     * should implement either java.io.Serializable or java.io.Externalizable in order to be saved
     * 
     * @param o The output stream
     * @param map The HashMap to write
     * @param desc A text description to use for logging
     * @throws IOException Exception
     */
    public static void writeHashMap(ObjectOutput o, HashMap map, String desc) throws IOException {
        SafeObjectOutputStream out = SafeObjectOutputStream.install(o);
        out.writeUTF(desc);
        out.writeMap(map);
    }

    /**
     * Read a hashmap of objects from the specified input stream. Returns null if no hashmap is
     * available.
     * 
     * @param in The input stream
     * @param desc A text description to use for logging
     * @return The HashMap or null, if not available
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static HashMap readHashMap(ObjectInput i, String desc) throws IOException {
        SafeObjectInputStream in = SafeObjectInputStream.install(i);
        in.readUTF();
        return in.readHashMap();
    }

    /**
     * Write a linked list of objects to the specified output stream. <NOTE: each object in the
     * array should implement either java.io.Serializable or java.io.Externalizable in order to be
     * saved
     * 
     * @param o The output stream
     * @param list The LinkedList to write
     * @param desc A text description to use for logging
     * @throws IOException Exception
     */
    public static void writeLinkedList(ObjectOutput o, LinkedList objlist, String desc)
      throws IOException {
        SafeObjectOutputStream out = SafeObjectOutputStream.install(o);
        out.writeUTF(desc);
        out.writeList(objlist);

    }

    /**
     * Reads a linked list of objects from the specified input stream. Returns null if no array is
     * available.
     * 
     * @param in The input stream
     * @param desc A text description to use for logging
     * @return The linked list or null, if not available
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static LinkedList readLinkedList(ObjectInput i, String desc) throws IOException {
        SafeObjectInputStream in = SafeObjectInputStream.install(i);
        in.readUTF();
        return in.readLinkedList();
    }

    // --------------------------------------------------------------------
    // Finder methods
    // --------------------------------------------------------------------

    /**
     * Find the AxisOperation object that matches the criteria
     * 
     * @param axisConfig The AxisConfiguration object
     * @param opClassName the class name string for the target object (could be a derived class)
     * @param opQName the name associated with the operation
     * @return the AxisOperation object that matches the given criteria
     */
    public static AxisOperation findOperation(AxisConfiguration axisConfig, String opClassName,
                                              QName opQName) {
        return ActivateUtils.findOperation(axisConfig, opClassName, opQName);
    }

    /**
     * Find the AxisOperation object that matches the criteria
     * 
     * @param service The AxisService object
     * @param opClassName The class name string for the target object (could be a derived class)
     * @param opQName the name associated with the operation
     * @return the AxisOperation object that matches the given criteria
     */
    public static AxisOperation findOperation(AxisService service, 
                                              String opClassName, 
                                              QName opQName) {
        return ActivateUtils.findOperation(service, opClassName, opQName);
    }

    /**
     * Find the AxisService object that matches the criteria
     * 
     * @param axisConfig The AxisConfiguration object
     * @param serviceClassName the class name string for the target object (could be a derived
     * class)
     * @param serviceName the name associated with the service
     * @return the AxisService object that matches the criteria
     */
    public static AxisService findService(AxisConfiguration axisConfig, String serviceClassName,
                                          String serviceName) {
        return ActivateUtils.findService(axisConfig, serviceClassName, serviceName);
    }

    /**
     * Find the AxisServiceGroup object that matches the criteria <p/> <B>Note<B> the saved 
     * service group meta information may not match up with any of the serviceGroups that 
     * are in the current AxisConfiguration object.
     * 
     * @param axisConfig The AxisConfiguration object
     * @param serviceGrpClassName the class name string for the target object (could be a derived
     * class)
     * @param serviceGrpName the name associated with the service group
     * @return the AxisServiceGroup object that matches the criteria
     */
    public static AxisServiceGroup findServiceGroup(AxisConfiguration axisConfig,
                                                    String serviceGrpClassName,
                                                    String serviceGrpName) {
        return ActivateUtils.findServiceGroup(axisConfig, serviceGrpClassName, serviceGrpName);
    }

    /**
     * Find the AxisMessage object that matches the criteria
     * 
     * @param op The AxisOperation object
     * @param msgName The name associated with the message
     * @param msgElementName The name associated with the message element
     * @return the AxisMessage object that matches the given criteria
     */
    public static AxisMessage findMessage(AxisOperation op, 
                                          String msgName, 
                                          String msgElementName) {
        return ActivateUtils.findMessage(op, msgName, msgElementName);
    }

    /**
     * Find the Handler object that matches the criteria
     * 
     * @param existingHandlers The list of existing handlers and phases
     * @param handlerClassName the class name string for the target object (could be a derived
     * class)
     * @return the Handler object that matches the criteria
     */
    public static Object findHandler(ArrayList existingHandlers, 
                                     MetaDataEntry metaDataEntry) 
    {
        return ActivateUtils.findHandler(existingHandlers, metaDataEntry);
    }

    /**
     * Find the TransportListener object that matches the criteria <p/> <B>Note<B> the saved meta
     * information may not match up with any of the objects that are in the current
     * AxisConfiguration object.
     * 
     * @param axisConfig The AxisConfiguration object
     * @param listenerClassName the class name string for the target object (could be a derived
     * class)
     * @return the TransportListener object that matches the criteria
     */
    public static TransportListener findTransportListener(AxisConfiguration axisConfig,
                                                          String listenerClassName) {
        return ActivateUtils.findTransportListener(axisConfig, listenerClassName);
    }

    /**
     * Compares the two collections to see if they are equivalent.
     * 
     * @param a1 The first collection
     * @param a2 The second collection
     * @param strict Indicates whether strict checking is required. Strict checking means that the
     * two collections must have the same elements in the same order. 
     * Non-strict checking means that the two collections must have the same elements, 
     * but the order is not significant.
     * @return TRUE if the two collections are equivalent FALSE, otherwise
     */
    public static boolean isEquivalent(ArrayList a1, ArrayList a2, boolean strict) {
        return ActivateUtils.isEquivalent(a1, a2, strict);
    }

    /**
     * Compares the two collections to see if they are equivalent.
     * 
     * @param m1 The first collection
     * @param m2 The second collection
     * @param strict Indicates whether strict checking is required. Strict checking means that the
     * two collections must have the same mappings. Non-strict checking means that the two
     * collections must have the same keys. In both cases, the order is not significant.
     * @return TRUE if the two collections are equivalent FALSE, otherwise
     */
    public static boolean isEquivalent(Map m1, Map m2, boolean strict) {
        return ActivateUtils.isEquivalent(m1, m2, strict);
    }

    /**
     * Compares the two collections to see if they are equivalent.
     * 
     * @param l1
     *            The first collection
     * @param l2
     *            The second collection
     * @return TRUE if the two collections are equivalent FALSE, otherwise
     */
    public static boolean isEquivalent(LinkedList l1, LinkedList l2) {
        return ActivateUtils.isEquivalent(l1, l2);
    }
}
