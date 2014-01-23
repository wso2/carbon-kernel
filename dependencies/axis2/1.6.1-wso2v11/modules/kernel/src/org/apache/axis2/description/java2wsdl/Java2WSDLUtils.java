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

package org.apache.axis2.description.java2wsdl;

public class Java2WSDLUtils {

    public static final String HTTP = "http://";

    public static final char PACKAGE_CLASS_DELIMITER = '.';

    public static final String SCHEMA_NAMESPACE_EXTN = "/xsd";

    private static NamespaceGenerator defaultNsGenerator = new DefaultNamespaceGenerator();

    /**
     * check the entry for a URL. This is a simple check and need to be improved
     *
     * @param entry
     */

    public static boolean isURL(String entry) {
        return entry.startsWith("http://");
    }

    /**
     * A method to strip the fully qualified className to a simple classname
     *
     * @param qualifiedName
     */
    public static String getSimpleClassName(String qualifiedName) {
        int index = qualifiedName.lastIndexOf(".");
        if (index > 0) {
            return qualifiedName.substring(index + 1,
                    qualifiedName.length());
        }
        return qualifiedName;
    }

    public static StringBuffer namespaceFromClassName(String className,
                                                      ClassLoader classLoader) throws Exception {
        return namespaceFromClassName(className, classLoader, defaultNsGenerator);
    }


    public static StringBuffer namespaceFromClassName(String className,
                                                      ClassLoader classLoader,
                                                      NamespaceGenerator nsGen) throws Exception {
        Class clazz = Class.forName(className,
                true,
                classLoader);
        Package pkg = clazz.getPackage();
        String name;

        if (pkg != null)
            name = pkg.getName();
        else
            name = packageNameFromClass(className);

        return nsGen.namespaceFromPackageName(name);
    }

    public static StringBuffer schemaNamespaceFromClassName(String packageName, ClassLoader loader) throws Exception {
        return schemaNamespaceFromClassName(packageName, loader, defaultNsGenerator);
    }

    public static StringBuffer schemaNamespaceFromClassName(String packageName, ClassLoader loader, NamespaceGenerator nsGen) throws Exception {
        StringBuffer stringBuffer = namespaceFromClassName(packageName,
                loader,
                nsGen);
        if (stringBuffer.length() == 0) {
            stringBuffer.append(Java2WSDLConstants.DEFAULT_TARGET_NAMESPACE);
        }
//        stringBuffer.append(SCHEMA_NAMESPACE_EXTN);
        return stringBuffer;
    }

    public static StringBuffer targetNamespaceFromClassName(String packageName, ClassLoader loader,
                                                            NamespaceGenerator nsGen) throws Exception {
        StringBuffer stringBuffer = namespaceFromClassName(packageName,
                loader,
                nsGen);
        if (stringBuffer.length() == 0) {
            stringBuffer.append(Java2WSDLConstants.DEFAULT_TARGET_NAMESPACE);
        }
        return stringBuffer;
    }


    public static String getPackageName(String className, ClassLoader classLoader) throws Exception {
        Class clazz = Class.forName(className,
                true,
                classLoader);
        Package pkg = clazz.getPackage();
        String name;

        if (pkg != null)
            name = pkg.getName();
        else
            name = packageNameFromClass(className);
        return name;
    }

    protected static String packageNameFromClass(String name) {
        String ret = "";
        int lastDot = name.lastIndexOf('.');

        if (lastDot != -1)
            ret = name.substring(0, lastDot);
        return ret;
    }
}
