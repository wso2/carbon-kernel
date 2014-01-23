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

package org.apache.axis2.context.externalize;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.ObjectOutputStream.PutField;
import java.io.ObjectStreamConstants;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A SafeObjectOutputStream provides extra mechanisms to ensure that 
 * objects can be safely serialized to the ObjectOutput.
 * 
 * If an Object is written to a normal ObjectOutput, the ObjectOutput is left in 
 * an unknown state if a NotSerializableException occurs.
 * 
 * The SafeObjectOutputStream does some additonal checking to ensure that the Object can
 * be safely written.  If the Object is suspicious, it is first written to a buffer to ensure
 * that the underlying ObjectOutput is not corrupted.
 * 
 * In addition, SafeObjectOutputStream provides extra methods to write containers of Objects.
 * For example the writeMap object will write the key and value pairs that are can be serialized.
 * 
 * @see SafeObjectInputStream
 *
 */
public class SafeObjectOutputStream implements ObjectOutput,
        ObjectStreamConstants, ExternalizeConstants {
    
    private static final Log log = LogFactory.getLog(SafeObjectOutputStream.class);
    private static final boolean isDebug = log.isDebugEnabled();
    
    // Actual Stream 
    private ObjectOutput out = null;
    
    // There are two ways to write out an object, a series of bytes or an Object.  
    // These flags are embedded in the stream so that the reader knows which form was used.
    private static final boolean FORM_BYTE = false;
    private static final boolean FORM_OBJECT = true;
    
    // Temporary ObjectOutputStream for
    MyOOS tempOOS = null;
    
    // As a way to improve performance and reduce trace logging with
    // extra exceptions, keep a table of classes that are not serializable
    // and only log the first time it that the class is encountered in
    // an NotSerializableException
    // note that the Hashtable is synchronized by Java so we shouldn't need to 
    // do extra control over access to the table
    public static final Hashtable notSerializableList = new Hashtable();
    
    /**
     * Add the SafeOutputStream if necessary.
     * @param out Current ObjectOutput
     * @return
     * @throws IOException
     */
    public static SafeObjectOutputStream install(ObjectOutput out) throws IOException {
        if (out instanceof SafeObjectOutputStream) {
            return (SafeObjectOutputStream) out;
        }
        return new SafeObjectOutputStream(out);
    }
    
    /**
     * Intentionally private.  
     * Callers should use the install method to add the SafeObjectOutputStream
     * into the stream.
     * @param oo
     * @throws IOException
     */
    private SafeObjectOutputStream(ObjectOutput oo) throws IOException {
        if (log.isDebugEnabled()) {
            this.out = new DebugObjectOutputStream(oo);
        } else {
            this.out = oo;
        }
    }

    // START DELEGATE METHODS
    public void close() throws IOException {
        if (tempOOS != null) {
            tempOOS.close();
            tempOOS = null;
        }
        out.close();
    }

    public void defaultWriteObject() throws IOException {
        if (out instanceof ObjectOutputStream) {
            ((ObjectOutputStream)out).defaultWriteObject();
        }
    }

    public boolean equals(Object o) {
        return out.equals(o);
    }

    public void flush() throws IOException {
        out.flush();
    }

    public int hashCode() {
        return out.hashCode();
    }

    public PutField putFields() throws IOException {
        if (out instanceof ObjectOutputStream) {
            return ((ObjectOutputStream)out).putFields();
        } else {
            throw new IOException("This method is not supported.");
        }
            
    }

    public void reset() throws IOException {
        if (out instanceof ObjectOutputStream) {
            ((ObjectOutputStream)out).reset();
        }
    }

    public String toString() {
        return out.toString();
    }

    public void useProtocolVersion(int version) throws IOException {
        if (out instanceof ObjectOutputStream) {
            ((ObjectOutputStream)out).useProtocolVersion(version);
        }
    }

    public void write(byte[] buf, int off, int len) throws IOException {
        out.write(buf, off, len);
    }

    public void write(byte[] buf) throws IOException {
        out.write(buf);
    }

    public void write(int val) throws IOException {
        out.write(val);
    }

    public void writeBoolean(boolean val) throws IOException {
        out.writeBoolean(val);
    }

    public void writeByte(int val) throws IOException {
        out.writeByte(val);
    }

    public void writeBytes(String str) throws IOException {
        out.writeBytes(str);
    }

    public void writeChar(int val) throws IOException {
        out.writeChar(val);
    }

    public void writeChars(String str) throws IOException {
        out.writeChars(str);
    }

    public void writeDouble(double val) throws IOException {
        out.writeDouble(val);
    }

    public void writeFields() throws IOException {
        if (out instanceof ObjectOutputStream) {
            ((ObjectOutputStream)out).writeFields();
        }
    }

    public void writeFloat(float val) throws IOException {
        out.writeFloat(val);
    }

    public void writeInt(int val) throws IOException {
        out.writeInt(val);
    }

    public void writeLong(long val) throws IOException {
        out.writeLong(val);
    }

    public void writeObject(Object obj) throws IOException {
        writeObject(obj, false);  // Assume object is not safe
    }

    public void writeShort(int val) throws IOException {
        out.writeShort(val);
    }

    public void writeUTF(String str) throws IOException {
        out.writeUTF(str);
    }

    // END DELEGATE METHODS
    
    /**
     * Write a map
     * 
     * FORMAT for null map
     *     EMPTY_OBJECT
     *     
     * FORMAT for non-empty map
     *     ACTIVE_OBJECT
     *     for each contained key value pair
     *        writePair
     *     EMPTY_OBJECT (indicates end of the list
     * 
     * @param ll
     * @return
     * @throws IOException
     */
    public boolean writeMap(Map map) throws IOException {
        
        if (map == null) {
            out.writeBoolean(EMPTY_OBJECT);
            return false;
        } else {
            out.writeBoolean(ACTIVE_OBJECT);
            Iterator it = map.entrySet().iterator();
            while (it.hasNext()) {
            	final Map.Entry entry = (Entry) it.next();
            	writePair(entry.getKey(), false, entry.getValue(), false);
            }            // Empty object indicates end of list
            out.writeBoolean(EMPTY_OBJECT);
        }
        return true;
    }
    
    /**
     * Write a list.
     * 
     * FORMAT for null list
     *     EMPTY_OBJECT
     *     
     * FORMAT for non-empty list
     *     ACTIVE_OBJECT
     *     for each contained object
     *        ACTOVE_OBJECT
     *        writeObject
     *     EMPTY_OBJECT (indicates end of the list
     * 
     * @param ll
     * @return
     * @throws IOException
     */
    public boolean writeList(List al) throws IOException {
        if (al == null) {
            out.writeBoolean(EMPTY_OBJECT);
            return false;
        } else {
            out.writeBoolean(ACTIVE_OBJECT);
            Iterator it = al.iterator();

            while (it.hasNext()) {
                Object value = it.next();
                writeItem(value, false);
            }
            // Empty object indicates end of list
            out.writeBoolean(EMPTY_OBJECT);
        }
        return true;
    }
    
    /**
     * Writes an object to the stream.
     * If the object is known (apriori) to be completely serializable it
     * is "safe".  Safe objects are written directly to the stream.
     * Objects that are not known are to be safe are tested for safety and 
     * only written if they are deemed safe.  Unsafe objects are not written.
     * Note: The java.io.ObjectOutputStream is left in an unrecoverable state
     * if any object written to it causes a serialization error.  So please
     * use the isSafe parameter wisely
     * 
     * FORMAT for NULL Object
     *    EMPTY_OBJECT
     *   
     * FORMAT for non-serializable Object
     *    EMPTY_OBJECT
     *    
     * FORMAT for safe serializable Object
     *    ACTIVE_OBJECT
     *    FORM_OBJECT
     *    Object
     *    
     * FORMAT for other serializable Object
     *    ACTIVE_OBJECT
     *    FORM_BYTE
     *    length of bytes
     *    bytes representing the object
     *   
     * @param obj
     * @param isSafe true if you know that object can be safely serialized. false if the 
     * object needs to be tested for serialization.
     * @returns true if written
     * @throws IOException
     */
    private boolean writeObject(Object obj, 
                       boolean isSafe) throws IOException {
        
        if (isDebug) {
            log.debug("Writing object:" + valueName(obj));
        }
        
        // Shortcut for null objects
        if (obj == null) {
            out.writeBoolean(EMPTY_OBJECT);
            return false;
        }
        // Shortcut for non-serializable objects
        if (!isSafe) {
            if (!isSerializable(obj)) {
                out.writeBoolean(EMPTY_OBJECT);
                return false;
            }
        }
        
        // If not safe, see if there are characteristics of the Object
        // that guarantee that it can be safely serialized 
        // (for example Strings are always serializable)
        if (!isSafe) {
            isSafe = isSafeSerializable(obj);
        }
        if (isSafe) {
            // Use object form
            if (isDebug) {
                log.debug("  write using object form");
            }
            out.writeBoolean(ACTIVE_OBJECT);  
            out.writeBoolean(FORM_OBJECT);
            out.writeObject(obj);
        } else {

            // Fall-back to byte form
            if (isDebug) {
                log.debug("  write using byte form");
            }
            MyOOS tempOOS;
            try {
                tempOOS = writeTempOOS(obj);
            } catch (IOException e) {
                // Put a EMPTY object in the file
                out.writeBoolean(EMPTY_OBJECT);
                throw e;
            }
            if (tempOOS == null) {
                out.writeBoolean(EMPTY_OBJECT);
                return false;
            } 

            out.writeBoolean(ACTIVE_OBJECT);   
            out.writeBoolean(FORM_BYTE);
            tempOOS.write(out);
            resetOnSuccess();
        }
        return true;
    }
    
    
    /**
     * Writes pair of objects to the stream.
     * 
     * If the objects are known (apriori) to be completely serializable they 
     * are "safe".  Safe objects are written directly to the stream.
     * Objects that are not known are to be safe are tested for safety and 
     * only written if they are deemed safe.  Unsafe objects are not written.
     * Note: The java.io.ObjectOutputStream is left in an unrecoverable state
     * if any object written to it causes a serialization error.  So please
     * use the isSafe parameter wisely
     * 
     *   
     * FORMAT for non-serializable key/value pair
     *    nothing is written
     *    
     * FORMAT for safe serializable key/value pair
     *    ACTIVE_OBJECT
     *    FORM_OBJECT
     *    Object
     *    
     * FORMAT for other serializable key/value pair
     *    ACTIVE_OBJECT
     *    FORM_BYTE
     *    length of bytes
     *    bytes representing the object
     *    
     * @param obj1
     * @param isSafe1 true if you know that object can be safely serialized. false if the 
     * object needs to be tested for serialization.
     * @param obj2
     * @param isSafe2 true if you know that object can be safely serialized. false if the 
     * object needs to be tested for serialization.
     * @returns true if both are written to the stream
     * @throws IOException
     */
    public boolean writePair(Object obj1, 
                       boolean isSafe1, 
                       Object obj2,
                       boolean isSafe2) throws IOException {
        
        if (isDebug) {
            log.debug("Writing key=" + valueName(obj1) + 
                      " value="+valueName(obj2));
        }
        // Shortcut for non-serializable objects
        if ((!isSafe1 && !isSerializable(obj1)) ||
             (!isSafe2 && !isSerializable(obj2))) {
            return false;
        }

        boolean isSafe = (isSafe1 || isSafeSerializable(obj1)) && 
            (isSafe2 || isSafeSerializable(obj2));

        if (isSafe) {
            if (isDebug) {
                log.debug("  write using object form");
            }
            out.writeBoolean(ACTIVE_OBJECT);
            out.writeBoolean(FORM_OBJECT);
            out.writeObject(obj1);
            out.writeObject(obj2);
        } else {
            if (isDebug) {
                log.debug("  write using byte form");
            }
            MyOOS tempOOS = writeTempOOS(obj1, obj2);
            if (tempOOS == null) {
                return false;
            } 
            out.writeBoolean(ACTIVE_OBJECT);
            out.writeBoolean(FORM_BYTE);
            tempOOS.write(out);
            resetOnSuccess();
        }
        return true;
    }
    
    /**
     * Writes pair of objects to the stream.
     * 
     * If the objects are known (apriori) to be completely serializable they 
     * are "safe".  Safe objects are written directly to the stream.
     * Objects that are not known are to be safe are tested for safety and 
     * only written if they are deemed safe.  Unsafe objects are not written.
     * Note: The java.io.ObjectOutputStream is left in an unrecoverable state
     * if any object written to it causes a serialization error.  So please
     * use the isSafe parameter wisely
     * 
     *   
     * FORMAT for non-serializable key/value pair
     *    nothing is written
     *    
     * FORMAT for safe serializable key/value pair
     *    ACTIVE_OBJECT
     *    FORM_OBJECT
     *    Object
     *    
     * FORMAT for other serializable key/value pair
     *    ACTIVE_OBJECT
     *    FORM_BYTE
     *    length of bytes
     *    bytes representing the object
     *    
     * @param obj1
     * @param isSafe1 true if you know that object can be safely serialized. false if the 
     * object needs to be tested for serialization.
     * @param obj2
     * @param isSafe2 true if you know that object can be safely serialized. false if the 
     * object needs to be tested for serialization.
     * @returns true if both are written to the stream
     * @throws IOException
     */
    public boolean writeItem(Object obj, 
                       boolean isSafe) throws IOException {
        
        if (isDebug) {
            log.debug("Writing obj=" + valueName(obj));
        }
        // Shortcut for non-serializable objects
        if (!isSafe && !isSerializable(obj)) {
            return false;
        }

        isSafe = (isSafe || isSafeSerializable(obj));

        if (isSafe) {
            if (isDebug) {
                log.debug("  write using object form");
            }
            out.writeBoolean(ACTIVE_OBJECT);
            out.writeBoolean(FORM_OBJECT);
            out.writeObject(obj);
        } else {
            if (isDebug) {
                log.debug("  write using byte form");
            }
            MyOOS tempOOS;
            try {
                tempOOS = writeTempOOS(obj);
            } catch (RuntimeException e) {
                return false;
            }
            if (tempOOS == null) {
                return false;
            } 
            out.writeBoolean(ACTIVE_OBJECT);
            out.writeBoolean(FORM_BYTE);
            tempOOS.write(out);
            resetOnSuccess();
        }
        return true;
    }
    
    /**
     * Does a quick check of the implemented interfaces to ensure that this 
     * object is serializable
     * @return true if the object is marked as Serializable 
     */
    private static boolean isSerializable(Object obj) {
        boolean isSerializable = (obj == null) || obj instanceof Serializable;
        if (!isSerializable) {
            markNotSerializable(obj);
        }
        return isSerializable;
    }
    
    /**
     * Does a quick check of the implemented class is safe to serialize without 
     * buffering.
     * @return true if the object is marked as safe.
     */
    private static boolean isSafeSerializable(Object obj) {
        
        boolean isSafeSerializable = (obj == null) || 
            obj instanceof SafeSerializable ||
            obj instanceof String ||
            obj instanceof Integer ||
            obj instanceof Boolean ||
            obj instanceof Long;
        return isSafeSerializable;
    }
    
    
    /**
     * Write the object to a temporary ObjectOutput
     * @param obj
     * @return ObjectOutput if successful
     */
    private MyOOS writeTempOOS(Object obj) throws IOException {
        MyOOS oos = null;
        
        try {
            oos = getTempOOS();
            oos.writeObject(obj);
            oos.flush();
        } catch (NotSerializableException nse2) {
            markNotSerializable(obj);
            if (oos != null) {
                resetOnFailure();
                oos = null;
            }
            throw nse2;
        } catch (IOException e) {
            if (oos != null) {
                resetOnFailure();
                oos = null;
            }
            throw e;
        } catch (RuntimeException e) {
            if (oos != null) {
                resetOnFailure();
                oos = null;
            }
            throw e;
        }
        return oos;
    }
    
    /**
     * Write the objects to a temporary ObjectOutput
     * @param obj1
     * @param obj2
     * @return ObjectOutput if successful
     */
    private MyOOS writeTempOOS(Object obj1, Object obj2) throws IOException {
        MyOOS oos = null;
        boolean first = true;
        try {
            oos = getTempOOS();
            oos.writeObject(obj1);
            first = false;
            oos.writeObject(obj2);
            oos.flush();
        } catch (NotSerializableException nse2) {
            // This is okay and expected in some cases.
            // Log the error and continue
            markNotSerializable((first) ? obj1 :obj2);
            if (oos != null) {
                resetOnFailure();
                oos = null;
            }
        } catch (IOException e) {
            if (oos != null) {
                resetOnFailure();
                oos = null;
            }
            throw e;
        } catch (RuntimeException e) {
            if (oos != null) {
                resetOnFailure();
                oos = null;
            }
            throw e;
        }
        return oos;
    }
    
    /**
     * Get or create a temporary ObjectOutputStream
     * @return MyOOS
     * @throws IOException
     */
    private MyOOS getTempOOS() throws IOException {
        if (tempOOS == null) {
            tempOOS = new MyOOS(new MyBAOS());
        }
        return tempOOS;
    }
    
    /**
     * If a failure occurs, reset the temporary ObjectOutputStream
     */
    private void resetOnFailure() throws IOException {
        if (tempOOS != null) {
            tempOOS.close();
            tempOOS = null;  // The ObjectOutput is in an unknown state and thus discarded
        }
    }
    
    /**
     * Reset the temporary ObjectOutputStream
     * @throws IOException
     */
    private void resetOnSuccess() throws IOException {
        tempOOS.reset();
    }
    
    private static void markNotSerializable(Object obj) {
        if (!isDebug) {
            return;
        }
        if (obj != null) {
            String name = obj.getClass().getName();
            Object value = notSerializableList.get(name);
            if (value == null) {
                notSerializableList.put(name, name);
                if (log.isTraceEnabled()) {
                    log.trace("***NotSerializableException*** [" + name + "]");
                }
            }
        }
    }
    
    private String valueName(Object obj) {
        if (obj == null) {
            return "null";
        } else if (obj instanceof String) {
            return (String) obj;
        } else {
            return "Object of class = " + obj.getClass().getName();
        }
    }
    /**
     * MyBAOS is a ByteArrayOutputStream with a few additions.
     *
     */
    static class MyBAOS extends ByteArrayOutputStream {
        /**
         * Return direct access to the buffer without creating a copy of the byte[]
         * @return buf
         */
        public byte[] getBytes() {
            return buf;
        }
        /**
         * Reset to a specific index in the buffer
         * @param count
         */
        public void reset(int count) {
            this.count = count;
        }
    }
    
    
    /**
     * MyOOS is an ObjectOutputStream with a few performant additions.
     *
     */
    static class MyOOS extends ObjectOutputStream {
        MyBAOS baos;
        int dataOffset;
        MyOOS(MyBAOS baos) throws IOException {
            super(baos);
            super.flush();
            this.baos = baos;
            
            // Capture the data offset 
            // (the location where data starts..which is after header information)
            dataOffset = baos.size();
        }
        
        /**
         * Override the reset so that we can reset to the data offset.
         */
        public void reset() throws IOException {
            super.reset();
            // Reset the byte stream to the position past the headers
            baos.reset(dataOffset);
        }
        
        /**
         * Write the contents of MyOOS to the indicated ObjectOutput.
         * Note that this direct write avoids any byte[] buffer creation
         * @param out
         * @throws IOException
         */
        public void write(ObjectOutput out) throws IOException {
            out.flush();
            // out.writeObject(out.toByteArray());
            out.writeInt(baos.size());
            out.write(baos.getBytes(),0,baos.size());
        }
    }

}
