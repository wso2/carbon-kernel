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

package org.apache.axis2.description.java2wsdl.bytecode;


import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * This is the class file reader for obtaining the parameter names
 * for declared methods in a class.  The class must have debugging
 * attributes for us to obtain this information. <p>
 * <p/>
 * This does not work for inherited methods.  To obtain parameter
 * names for inherited methods, you must use a paramReader for the
 * class that originally declared the method. <p>
 * <p/>
 * don't get tricky, it's the bare minimum.  Instances of this class
 * are not threadsafe -- don't share them. <p>
 */
public class ParamReader
        extends ClassReader {
    private String methodName;
    private Map methods = new HashMap();
    private Class[] paramTypes;

    /**
     * Processes a class file, given it's class.  We'll use the defining
     * classloader to locate the bytecode.
     *
     * @param c
     * @throws java.io.IOException
     */
    public ParamReader(Class c) throws IOException {
        this(getBytes(c));
    }

    /**
     * Processes the given class bytes directly.
     *
     * @param b
     * @throws IOException
     */
    public ParamReader(byte[] b) throws IOException {
        super(b, findAttributeReaders(ParamReader.class));

        // check the magic number
        if (readInt() != 0xCAFEBABE) {
            // not a class file!
            throw new IOException("Error looking for paramter names in bytecode: input does not appear to be a valid class file");
        }

        readShort(); // minor version
        readShort(); // major version

        readCpool(); // slurp in the constant pool

        readShort(); // access flags
        readShort(); // this class name
        readShort(); // super class name

        int count = readShort(); // ifaces count
        for (int i = 0; i < count; i++) {
            readShort(); // interface index
        }

        count = readShort(); // fields count
        for (int i = 0; i < count; i++) {
            readShort(); // access flags
            readShort(); // name index
            readShort(); // descriptor index
            skipAttributes(); // field attributes
        }

        count = readShort(); // methods count
        for (int i = 0; i < count; i++) {
            readShort(); // access flags
            int m = readShort(); // name index
            String name = resolveUtf8(m);
            int d = readShort(); // descriptor index
            this.methodName = name + resolveUtf8(d);
            readAttributes(); // method attributes
        }

    }

    public void readCode() throws IOException {
        readShort(); // max stack
        int maxLocals = readShort(); // max locals

        MethodInfo info = new MethodInfo(maxLocals);
        if (methods != null && methodName != null) {
            methods.put(methodName, info);
        }

        skipFully(readInt()); // code
        skipFully(8 * readShort()); // exception table
        // read the code attributes (recursive).  This is where
        // we will find the LocalVariableTable attribute.
        readAttributes();
    }

    /**
     * Returns the names of the declared parameters for the given constructor.
     * If we cannot determine the names, return null.  The returned array will
     * have one name per parameter.  The length of the array will be the same
     * as the length of the Class[] array returned by Constructor.getParameterTypes().
     *
     * @param ctor
     * @return Returns String[] array of names, one per parameter, or null
     */
    public String[] getParameterNames(Constructor ctor) {
        paramTypes = ctor.getParameterTypes();
        return getParameterNames(ctor, paramTypes);
    }

    /**
     * Returns the names of the declared parameters for the given method.
     * If we cannot determine the names, return null.  The returned array will
     * have one name per parameter.  The length of the array will be the same
     * as the length of the Class[] array returned by Method.getParameterTypes().
     *
     * @param method
     * @return Returns String[] array of names, one per parameter, or null
     */
    public String[] getParameterNames(Method method) {
        paramTypes = method.getParameterTypes();
        return getParameterNames(method, paramTypes);
    }

    protected String[] getParameterNames(Member member, Class [] paramTypes) {
        // look up the names for this method
        MethodInfo info = (MethodInfo) methods.get(getSignature(member, paramTypes));

        // we know all the local variable names, but we only need to return
        // the names of the parameters.

        if (info != null) {
            String[] paramNames = new String[paramTypes.length];
            int j = Modifier.isStatic(member.getModifiers()) ? 0 : 1;

            boolean found = false;  // did we find any non-null names
            for (int i = 0; i < paramNames.length; i++) {
                if (info.names[j] != null) {
                    found = true;
                    paramNames[i] = info.names[j];
                }
                j++;
                if (paramTypes[i] == double.class || paramTypes[i] == long.class) {
                    // skip a slot for 64bit params
                    j++;
                }
            }

            if (found) {
                return paramNames;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private static class MethodInfo {
        String[] names;
        int maxLocals;

        public MethodInfo(int maxLocals) {
            this.maxLocals = maxLocals;
            names = new String[maxLocals];
        }
    }

    private MethodInfo getMethodInfo() {
        MethodInfo info = null;
        if (methods != null && methodName != null) {
            info = (MethodInfo) methods.get(methodName);
        }
        return info;
    }

    /**
     * This is invoked when a LocalVariableTable attribute is encountered.
     *
     * @throws IOException
     */
    public void readLocalVariableTable() throws IOException {
        int len = readShort(); // table length
        MethodInfo info = getMethodInfo();
        for (int j = 0; j < len; j++) {
            readShort(); // start pc
            readShort(); // length
            int nameIndex = readShort(); // name_index
            readShort(); // descriptor_index
            int index = readShort(); // local index
            if (info != null) {
                info.names[index] = resolveUtf8(nameIndex);
            }
        }
    }
}
