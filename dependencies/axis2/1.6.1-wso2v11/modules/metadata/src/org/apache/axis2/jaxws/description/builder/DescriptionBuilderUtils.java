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

package org.apache.axis2.jaxws.description.builder;

import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/**
 * 
 */
class DescriptionBuilderUtils {

    private static final Log log = LogFactory.getLog(DescriptionBuilderUtils.class);

    static String JAXWS_HOLDER_CLASS = "javax.xml.ws.Holder";

    private static final String INT_PRIMITIVE = "int";
    private static final String INT_PRIMITIVE_ENCODING = "I";
    private static final String BYTE_PRIMITIVE = "byte";
    private static final String BYTE_PRIMITIVE_ENCODING = "B";
    private static final String CHAR_PRIMITIVE = "char";
    private static final String CHAR_PRIMITIVE_ENCODING = "C";
    private static final String SHORT_PRIMITIVE = "short";
    private static final String SHORT_PRIMITIVE_ENCODING = "S";
    private static final String BOOLEAN_PRIMITIVE = "boolean";
    private static final String BOOLEAN_PRIMITIVE_ENCODING = "Z";
    private static final String LONG_PRIMITIVE = "long";
    private static final String LONG_PRIMITIVE_ENCODING = "J";
    private static final String FLOAT_PRIMITIVE = "float";
    private static final String FLOAT_PRIMITIVE_ENCODING = "F";
    private static final String DOUBLE_PRIMITIVE = "double";
    private static final String DOUBLE_PRIMITIVE_ENCODING = "D";
    private static final String VOID_PRIMITIVE = "void";
    // REVIEW: This may not be the correct encoding for Void
    private static final String VOID_PRIMITIVE_ENCODING = "V";

    /**
     * Returns a string representing the outermost generic raw type class, or null if the argument
     * is not a generic.  For example if the string "javax.xml.ws.Holder<my.package.MyObject>" is
     * passed in, the string "javax.xml.ws.Holder" will be returned.
     * <p/>
     * Note that generic arrays are supported.  For example, for "Holder<List<String>[][]", the
     * returned value will be "List[][]".
     *
     * @param inputType
     * @return A string representing the generic raw type or null if there is no generic.
     */
    static String getRawType(String inputType) {
        String returnRawType = null;
        int leftBracket = inputType.indexOf("<");
        int rightBracket = inputType.lastIndexOf(">");
        if (leftBracket > 0 && rightBracket > 0 && rightBracket > leftBracket) {
            String part1 = inputType.substring(0, leftBracket);
            if ((rightBracket + 1) == inputType.length()) {
                // There is nothing after the closing ">" we need to append to the raw type
                returnRawType = part1;
            } else {
                // Skip over the closing ">" then append the rest of the string to the raw type
                // This would be an array declaration for example.
                String part2 = inputType.substring(rightBracket + 1).trim();
                returnRawType = part1 + part2;
            }
        }
        return returnRawType;
    }

    /**
     * Return the actual type in a JAX-WS holder declaration.  For example, for the argument
     * "javax.xml.ws.Holder<my.package.MyObject>", return "my.package.MyObject". If the actual type
     * itself is a generic, then that raw type will be returned.  For example,
     * "javax.xml.ws.Holder<java.util.List<my.package.MyObject>>" will return "java.util.List".
     * <p/>
     * Note that Holders of Arrays and of Generic Arrays are also supported.  For example, for
     * "javax.xml.ws.Holder<String[]>", return "String[]".  For an array of a generic, the array of
     * the raw type is returned.  For example, for "javax.xml.ws.Holder<List<String>[][]>", return
     * "List[][]".
     * <p/>
     * Important note!  The JAX-WS Holder generic only supports a single actual type, i.e. the
     * generic is javax.xml.ws.Holder<T>.  This method is not general purpose; it does not support
     * generics with multiple types such as Generic<K,V> at the outermost level.
     *
     * @param holderInputString
     * @return return the actual argument class name for a JAX-WS Holder; returns null if the
     *         argument is not a JAX-WS Holder
     */
    static String getHolderActualType(String holderInputString) {
        String returnString = null;
        if (DescriptionBuilderUtils.isHolderType(holderInputString)) {
            int leftBracket = holderInputString.indexOf("<");
            int rightBracket = holderInputString.lastIndexOf(">");
            if (leftBracket > 0 && rightBracket > leftBracket + 1) {
                // Get everything between the outermost "<" and ">"
                String actualType =
                        holderInputString.substring(leftBracket + 1, rightBracket).trim();
                // If the holder contained a generic, then get the generic raw type (e.g. "List" for
                // Holder<List<String>>).
                String rawType = getRawType(actualType);
                if (rawType != null) {
                    returnString = rawType;
                } else {
                    return returnString = actualType;
                }
            }
        }
        return returnString;
    }


    /**
     * Check if the input String is a JAX-WS Holder.  For example "javax.xml.ws.Holder<my.package.MyObject>".
     *
     * @param checkType
     * @return true if it is a JAX-WS Holder type; false otherwise.
     */
    static boolean isHolderType(String checkType) {
        boolean isHolder = false;
        if (checkType != null) {
            if (checkType.startsWith(JAXWS_HOLDER_CLASS)) {
                isHolder = checkType.length() == JAXWS_HOLDER_CLASS.length() ||
                           checkType.charAt(JAXWS_HOLDER_CLASS.length()) == '<';
            }
        }
        return isHolder;
    }

    /**
     * Answers if the String representing the class contains an array declaration. For example
     * "Foo[][]" would return true, as would "int[]".
     *
     * @param className
     * @return
     */
    static boolean isClassAnArray(String className) {
        if (className != null && className.indexOf("[") > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * For an class name that is an array, return the non-array declaration portion. For example
     * "my.package.Foo[][]" would return "my.package.Foo". Returns null if the argument does not
     * contain an array declaration.
     *
     * @param fullClassName
     * @return
     */
    static String getBaseArrayClassName(String fullClassName) {
        String baseArrayClassName = null;
        if (fullClassName != null) {
            int firstArrayDimension = fullClassName.indexOf("[");
            if (firstArrayDimension > 0) {
                baseArrayClassName = fullClassName.substring(0, firstArrayDimension);
            }
        }
        return baseArrayClassName;
    }

    /**
     * Return a prefix suitable for passing to Class.forName(String) for an array.  Each array
     * dimension represented by "[]" will be represented by a single "[".
     *
     * @param arrayClassName
     * @return
     */
    static String getArrayDimensionPrefix(String arrayClassName) {
        StringBuffer arrayDimPrefix = new StringBuffer();

        if (arrayClassName != null) {
            int arrayDimIndex = arrayClassName.indexOf("[]");
            while (arrayDimIndex > 0) {
                arrayDimPrefix.append("[");
                // Skip over this "[]" and see if there are any more.
                int startNext = arrayDimIndex + 2;
                arrayDimIndex = arrayClassName.indexOf("[]", startNext);
            }
        }

        if (arrayDimPrefix.length() > 0)
            return arrayDimPrefix.toString();
        else
            return null;
    }

    /**
     * For primitives, return the appropriate primitive class.  Note that arrays of primitives are
     * handled differently, like arrays of objects.  Only non-array primitives are processed by this
     * method.  This method understands both the typical primitive declaration (e.g. "int") and the
     * encoding used as for arrays (e.g. "I").
     *
     * @param classType
     * @return
     */
    static Class getPrimitiveClass(String classType) {

        Class paramClass = null;

        if (INT_PRIMITIVE.equals(classType) || INT_PRIMITIVE_ENCODING.equals(classType)) {
            paramClass = int.class;
        } else if (BYTE_PRIMITIVE.equals(classType) || BYTE_PRIMITIVE_ENCODING.equals(classType)) {
            paramClass = byte.class;
        } else if (CHAR_PRIMITIVE.equals(classType) || CHAR_PRIMITIVE_ENCODING.equals(classType)) {
            paramClass = char.class;
        } else
        if (SHORT_PRIMITIVE.equals(classType) || SHORT_PRIMITIVE_ENCODING.equals(classType)) {
            paramClass = short.class;
        } else
        if (BOOLEAN_PRIMITIVE.equals(classType) || BOOLEAN_PRIMITIVE_ENCODING.equals(classType)) {
            paramClass = boolean.class;
        } else if (LONG_PRIMITIVE.equals(classType) || LONG_PRIMITIVE_ENCODING.equals(classType)) {
            paramClass = long.class;
        } else
        if (FLOAT_PRIMITIVE.equals(classType) || FLOAT_PRIMITIVE_ENCODING.equals(classType)) {
            paramClass = float.class;
        } else
        if (DOUBLE_PRIMITIVE.equals(classType) || DOUBLE_PRIMITIVE_ENCODING.equals(classType)) {
            paramClass = double.class;
        } else if (VOID_PRIMITIVE.equals(classType) || VOID_PRIMITIVE_ENCODING.equals(classType)) {
            paramClass = void.class;
        }
        return paramClass;
    }

    /**
     * Returns the encoding used to represent a Class for an array of a primitive type.  For
     * example, an array of boolean is represented by "Z". This is as described in the javadoc for
     * Class.getName().  If the argument is not a primitive type, a null will be returned.
     * <p/>
     * Note that arrays of voids are not allowed; a null will be returned.
     *
     * @param primitiveType
     * @return
     */
    static String getPrimitiveTypeArrayEncoding(String primitiveType) {
        String encoding = null;

        if (BOOLEAN_PRIMITIVE.equals(primitiveType)) {
            encoding = BOOLEAN_PRIMITIVE_ENCODING;
        } else if (BYTE_PRIMITIVE.equals(primitiveType)) {
            encoding = BYTE_PRIMITIVE_ENCODING;
        } else if (CHAR_PRIMITIVE.equals(primitiveType)) {
            encoding = CHAR_PRIMITIVE_ENCODING;
        } else if (DOUBLE_PRIMITIVE.equals(primitiveType)) {
            encoding = DOUBLE_PRIMITIVE_ENCODING;
        } else if (FLOAT_PRIMITIVE.equals(primitiveType)) {
            encoding = FLOAT_PRIMITIVE_ENCODING;
        } else if (INT_PRIMITIVE.equals(primitiveType)) {
            encoding = INT_PRIMITIVE_ENCODING;
        } else if (LONG_PRIMITIVE.equals(primitiveType)) {
            encoding = LONG_PRIMITIVE_ENCODING;
        } else if (SHORT_PRIMITIVE.equals(primitiveType)) {
            encoding = SHORT_PRIMITIVE_ENCODING;
        }
        return encoding;
    }

    /**
     * If the parameter represents and array, then the returned string is in a format that a
     * Class.forName(String) can be done on it.  This format is described by Class.getName(). If the
     * parameter does not represent an array, the parememter is returned unmodified.
     * <p/>
     * Note that arrays of primitives are processed as well as arrays of objects.
     *
     * @param classToLoad
     * @return
     */
    static String reparseIfArray(String classToLoad) {
        if (log.isDebugEnabled())
        {
            log.debug("entry with String parameter classToLoad: " + classToLoad);
        }
        if (classToLoad.startsWith("[")) {
            // It appears that the string is already in binary form.
            // Detect if the form is valid and fix it if it is not.
            // For example, sometimes a [my.Foo is input instead of the required [Lmy.Foo;
            String binaryForm = classToLoad;
            
            int indexAfterBracket = classToLoad.lastIndexOf("[") + 1;
            
            String base = classToLoad.substring(indexAfterBracket);
            String dims = classToLoad.substring(0, indexAfterBracket);
            
            if (getPrimitiveClass(base) == null) {
                // Make sure the base starts with an L and ends with a ;
                if (!base.startsWith("L") && !base.endsWith(";")) {
                    binaryForm = dims + "L" + base + ";";
                }
            }
            if (log.isDebugEnabled())
            {
                log.debug("exit method with String return value binaryForm: " + binaryForm);
            }            
            return binaryForm;
        }
                    
        String reparsedClassName = classToLoad;
        if (isClassAnArray(classToLoad)) {
            String baseType = getBaseArrayClassName(classToLoad);
            String dimensionPrefix = getArrayDimensionPrefix(classToLoad);
            if (getPrimitiveTypeArrayEncoding(baseType) != null) {
                reparsedClassName = dimensionPrefix + getPrimitiveTypeArrayEncoding(baseType);
            } else {
                reparsedClassName = dimensionPrefix + "L" + baseType + ";";
            }
        }
        if (log.isDebugEnabled())
        {
            log.debug("exit method with String return value reparsedClassName: " + reparsedClassName);
        }        
        return reparsedClassName;
    }

    /**
     * Load a class represented in a Description Builder Composite.  If a classloader is specified,
     * it will be used; otherwise the default classloader is used.
     *
     * @param classToLoad
     * @param classLoader
     * @return
     */
    static Class loadClassFromComposite(String classToLoad, ClassLoader classLoader) {
        Class returnClass = null;

        // If this is an array, then create a string version as described by Class.getName.
        // For example, "Foo[][]" becomes "[[LFoo".  Note that arrays of primitives must also be parsed.
        classToLoad = DescriptionBuilderUtils.reparseIfArray(classToLoad);

        if (classLoader != null) {
            // Use the specified classloader to load the class.
            try {
                returnClass = forName(classToLoad, false, classLoader);
            }
            //Catch Throwable as ClassLoader can throw an NoClassDefFoundError that
            //does not extend Exception, so lets catch everything that extends Throwable
            //rather than just Exception.
            catch (Throwable ex) {
                throw ExceptionFactory.makeWebServiceException(
                        Messages.getMessage("DBUClassNotFound", classToLoad, classLoader.toString()));
            }
        } else {
            //Use the thread context class loader to load the class.
            try {
                returnClass = forName(classToLoad, false,
                                                       getContextClassLoader(null));
            }
            catch (Throwable ex) {
                //Use the default classloader to load the class.
                try {
                    returnClass = forName(classToLoad);
                }
                //Catch Throwable as ClassLoader can throw an NoClassDefFoundError that
                //does not extend Exception
                catch (Throwable ex2) {
                    throw ExceptionFactory.makeWebServiceException(
                            Messages.getMessage("DBUClassNotFound2", classToLoad));
                }
            }
        }
        return returnClass;
    }

    static boolean isEmpty(String string) {
        return (string == null || "".equals(string));
    }

    static boolean isEmpty(QName qname) {
        return qname == null || isEmpty(qname.getLocalPart());
    }

    /**
     * Return the class for this name
     *
     * @return Class
     */
    private static Class forName(final String className, final boolean initialize,
                                final ClassLoader classloader) throws ClassNotFoundException {
        Class cl = null;
        try {
            cl = (Class) AccessController.doPrivileged(
                    new PrivilegedExceptionAction() {
                        public Object run() throws ClassNotFoundException {
                            return Class.forName(className, initialize, classloader);
                        }
                    }
            );
        } catch (PrivilegedActionException e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception thrown from AccessController: " + e.getMessage(), e);
            }
            throw (ClassNotFoundException) e.getException();
        }

        return cl;
    }

    /**
     * Return the class for this name
     *
     * @return Class
     */
    private static Class forName(final String className) throws ClassNotFoundException {
        Class cl = null;
        try {
            cl = (Class) AccessController.doPrivileged(
                    new PrivilegedExceptionAction() {
                        public Object run() throws ClassNotFoundException {
                            return Class.forName(className);
                        }
                    }
            );
        } catch (PrivilegedActionException e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception thrown from AccessController: " + e.getMessage(), e);
            }
            throw (ClassNotFoundException) e.getException();
        }

        return cl;
    }

    /**
     * @return ClassLoader
     */
    private static ClassLoader getContextClassLoader(final ClassLoader classLoader) {
        ClassLoader cl;
        try {
            cl = (ClassLoader) AccessController.doPrivileged(
                    new PrivilegedExceptionAction() {
                        public Object run() throws ClassNotFoundException {
                            return classLoader != null ? classLoader : Thread.currentThread().getContextClassLoader();
                        }
                    }
            );
        } catch (PrivilegedActionException e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception thrown from AccessController: " + e.getMessage(), e);
            }
            throw ExceptionFactory.makeWebServiceException(e.getException());
        }

        return cl;
    }
}
