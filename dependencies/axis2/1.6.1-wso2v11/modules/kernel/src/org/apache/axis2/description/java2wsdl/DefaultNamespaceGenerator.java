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

/**
 * This class provides the default implementatoin for mapping java classes to namespaces
 *
 */
public class DefaultNamespaceGenerator implements NamespaceGenerator {
    public static final String HTTP = "http://";
    public static final char PACKAGE_CLASS_DELIMITER = '.';
    public static final String SCHEMA_NAMESPACE_EXTN = "/xsd";

    /* (non-Javadoc)
     * @see org.apache.axis2.description.java2wsdl.NamespaceGenerator#namespaceFromPackageName(java.lang.String)
     */
    public StringBuffer namespaceFromPackageName(String packageName) {

        StringBuffer strBuf = new StringBuffer();
        int prevIndex = packageName.length();
        int currentIndex = packageName.lastIndexOf(PACKAGE_CLASS_DELIMITER);
        if (currentIndex > 0) {
            strBuf.append(HTTP);
        } else if (prevIndex > 0) {
            strBuf.append(HTTP);
            strBuf.append(packageName);
            return strBuf;
        } else if (currentIndex == -1) {
//            strBuf.append(HTTP);
//            strBuf.append(packageName);
            return strBuf;
        }
        while (currentIndex != -1) {
            strBuf.append(packageName.substring(currentIndex + 1, prevIndex));
            prevIndex = currentIndex;
            currentIndex = packageName.lastIndexOf(PACKAGE_CLASS_DELIMITER, prevIndex - 1);
            strBuf.append(PACKAGE_CLASS_DELIMITER);

            if (currentIndex == -1) {
                strBuf.append(packageName.substring(0, prevIndex));
            }
        }
        return strBuf;
    }

    /* (non-Javadoc)
     * @see org.apache.axis2.description.java2wsdl.NamespaceGenerator#schemaNamespaceFromPackageName(java.lang.String)
     */
    public StringBuffer schemaNamespaceFromPackageName(String packageName) {
        if (packageName.length() > 0) {
            return namespaceFromPackageName(packageName).append(SCHEMA_NAMESPACE_EXTN);
        } else {
            StringBuffer buffer = new StringBuffer();
            buffer.append(Java2WSDLConstants.DEFAULT_TARGET_NAMESPACE);
            buffer.append(SCHEMA_NAMESPACE_EXTN);
            return buffer;
        }
    }
    
    

}
