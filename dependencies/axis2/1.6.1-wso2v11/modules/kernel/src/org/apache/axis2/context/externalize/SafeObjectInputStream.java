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

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectStreamConstants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A SafeObjectInputStream reads data that was written by SafeObjectOutputStream
 * 
 * @see SafeObjectInput
 */
public class SafeObjectInputStream implements ObjectInput, ObjectStreamConstants {

    private static final Log log = LogFactory.getLog(SafeObjectInputStream.class);
    private static final boolean isDebug = log.isDebugEnabled();
    
    // All data is read from the delegated ObjectInput
    ObjectInput in = null;
    final ObjectInput original;
    
    // A buffer is used when Object is persisted as bytes
    private byte[] buffer = null;
    private static final int BUFFER_MIN_SIZE = 4096;
    
    /**
     * Add the SafeObjectInputStream if necessary
     * @param in
     * @return
     */
    public static SafeObjectInputStream install(ObjectInput in) {
        if (in instanceof SafeObjectInputStream) {
            return (SafeObjectInputStream) in;
        }
        return new SafeObjectInputStream(in);
    }
    
    
    /**
     * Intentionally private.  Callers should use the install method to add the SafeObjectInputStream
     * into the stream.
     * @param in
     */
    private SafeObjectInputStream(ObjectInput in) {
        original = in;
        if (log.isDebugEnabled()) {
            this.in = new DebugObjectInput(original);
        } else {
            this.in = original;
        }
    }
    
    //  Delegated Methods
    public int available() throws IOException {
        return in.available();
    }
    public void close() throws IOException {
        in.close();
    }
    public int read() throws IOException {
        return in.read();
    }
    public int read(byte[] b, int off, int len) throws IOException {
        return in.read(b, off, len);
    }
    public int read(byte[] b) throws IOException {
        return in.read(b);
    }
    public boolean readBoolean() throws IOException {
        return in.readBoolean();
    }
    public byte readByte() throws IOException {
        return in.readByte();
    }
    public char readChar() throws IOException {
        return in.readChar();
    }
    public double readDouble() throws IOException {
        return in.readDouble();
    }
    public float readFloat() throws IOException {
        return in.readFloat();
    }
    public void readFully(byte[] b, int off, int len) throws IOException {
        in.readFully(b, off, len);
    }
    public void readFully(byte[] b) throws IOException {
        in.readFully(b);
    }
    public int readInt() throws IOException {
        return in.readInt();
    }
    public String readLine() throws IOException {
        return in.readLine();
    }
    public long readLong() throws IOException {
        return in.readLong();
    }
    public Object readObject() throws ClassNotFoundException, IOException {
        return readObjectOverride();
    }
    public short readShort() throws IOException {
        return in.readShort();
    }
    public int readUnsignedByte() throws IOException {
        return in.readUnsignedByte();
    }
    public int readUnsignedShort() throws IOException {
        return in.readUnsignedShort();
    }
    public String readUTF() throws IOException {
        return in.readUTF();
    }
    public long skip(long n) throws IOException {
        return in.skip(n);
    }
    public int skipBytes(int n) throws IOException {
        return in.skipBytes(n);
    }
    
    // Value Add Methods
    
    /**
     * Read the input stream and place the key/value pairs in a hashmap
     * @return HashMap or null
     * @throws IOException
     * @see SafeObjectOutputStream.writeMap()
     */
    public HashMap readHashMap() throws IOException  {
        HashMap hashMap = new HashMap();
        return (HashMap) readMap(hashMap);
    }
    
    /**
     * Read the input stream and place the key/value pairs in the
     * indicated Map
     * @param map input map
     * @return map or null
     * @throws IOException
     * @see SafeObjectOutputStream.writeMap()
     */
    public Map readMap(Map map) throws IOException {
        boolean isActive = in.readBoolean();
        if (!isActive) {
            return null;
        }
        
        while (in.readBoolean()) {
            Object key = null;
            Object value = null;

            boolean isObjectForm = in.readBoolean();
            try {
                if (isObjectForm) {
                    if (isDebug) {
                        log.debug(" reading using object form");
                    }
                    // Read the key and value directly
                    key = in.readObject();
                    value = in.readObject();
                } else {
                    if (isDebug) {
                        log.debug(" reading using byte form");
                    }
                    // Get the byte stream
                    ByteArrayInputStream bais = getByteStream(in);

                    // Now get the real key and value
                    ObjectInputStream tempOIS = createObjectInputStream(bais);
                    key = tempOIS.readObject();
                    value = tempOIS.readObject();
                    tempOIS.close();
                    bais.close();
                }
                // Put the key and value in the map
                if (isDebug) {
                    log.debug("Read key=" + valueName(key) + " value="+valueName(value));
                }
                map.put(key, value);
            } catch (ClassNotFoundException e) {
                // Swallow the error and try to continue
                log.error(e);
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                throw AxisFault.makeFault(e);
            }
        }
        return map;
    }
    
    /**
     * Read the input stream and place objects in an ArrayList
     * @return ArrayList or null
     * @throws IOException
     * @see SafeObjectInputStream.writeList()
     */
    public ArrayList readArrayList() throws IOException {
        List ll = new ArrayList();
        return (ArrayList) readList(ll);
    }
    
    /**
     * Read the input stream and place objects in a LinkedList
     * @return LinkedList or null
     * @throws IOException
     * @see SafeObjectInputStream.writeList()
     */
    public LinkedList readLinkedList() throws IOException {
       List ll = new LinkedList();
       return (LinkedList) readList(ll);
    }
    
    /**
     * Read hte input stream and place objects in the specified List
     * @param list List
     * @return List or null
     * @throws IOException
     * @see SafeObjectInputStream.writeList()
     */
    public List readList(List list) throws IOException {
        boolean isActive = in.readBoolean();
        if (!isActive) {
            return null;
        }
        
        while (in.readBoolean()) {
            Object value;
            boolean isObjectForm = in.readBoolean();
            try {
                if (isObjectForm) {
                    if (isDebug) {
                        log.debug(" reading using object form");
                    }
                    // Read the value directly
                    value = in.readObject();
                } else {
                    if (isDebug) {
                        log.debug(" reading using byte form");
                    }
                    // Get the byte stream
                    ByteArrayInputStream bais = getByteStream(in);

                    // Now get the real key and value
                    ObjectInputStream tempOIS = createObjectInputStream(bais);
                    value = tempOIS.readObject();
                    tempOIS.close();
                    bais.close();
                }
                // Put the key and value in the list
                if (isDebug) {
                    log.debug("Read value="+valueName(value));
                }
                list.add(value);
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                throw AxisFault.makeFault(e);
            }
        }
        return list;
    }
    
    /**
     * Reads the object using the same format that was written.
     * 
     * EXPECTED FORMATS
     *   boolean=false
     *   return null Object
     *   
     *   
     *   boolean=true
     *   boolean=true
     *   return Object read from inputStream
     *   
     *   
     *   boolean=true
     *   boolean=false
     *   int=nuber of bytes
     *   bytes
     *   return Object read from bytes
     *   
     * @return Object or null
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private Object readObjectOverride() throws IOException, ClassNotFoundException {
        boolean isActive = in.readBoolean();
        if (!isActive) {
            if (isDebug) {
                log.debug("Read object=null");
            }
            return null;
        }
        Object obj = null;
        boolean isObjectForm = in.readBoolean();
        if (isObjectForm) {
            // Read the object directly
            if (isDebug) {
                log.debug(" reading using object form");
            }
            obj = in.readObject();
        } else {
            if (isDebug) {
                log.debug(" reading using byte form");
            }
            // Get the byte stream
            ByteArrayInputStream bais = getByteStream(in);

            // Now get the real object
            ObjectInputStream tempOIS = createObjectInputStream(bais);
            obj = tempOIS.readObject();
            tempOIS.close();
            bais.close();
        }
        
        if (isDebug) {
            log.debug("Read object=" + valueName(obj));
        }
        return obj;   
        
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
     * Get the byte stream for an object that is written using the 
     * byte format.  
     * EXPECTED format
     *     int (number of bytes)
     *     bytes 
     *     
     * @param in
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private ByteArrayInputStream getByteStream(ObjectInput in) throws IOException, ClassNotFoundException {
        
        // Read the length
        int size = in.readInt();
        
        // Fill our temporary buffer
        if (buffer == null || buffer.length < size) {
            int allocSize = (size > BUFFER_MIN_SIZE) ? size : BUFFER_MIN_SIZE;
            buffer = new byte[allocSize];
        }
        in.readFully(buffer, 0, size);
        
        // Return a stream backed by the buffer
        return new ByteArrayInputStream(buffer, 0, size);
    }
    
    private ObjectInputStream createObjectInputStream(InputStream is) throws IOException {
        // The created ObjectInputStream must use the same class/object resolution 
        // code that is used by the original ObjectInput
        return new ObjectInputStreamWithCL(is);
    }
}
