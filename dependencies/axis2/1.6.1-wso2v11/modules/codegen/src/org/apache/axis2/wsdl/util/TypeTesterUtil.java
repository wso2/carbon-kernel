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

package org.apache.axis2.wsdl.util;

public class TypeTesterUtil {

    /**
     * Check whether a given classname is primitives
     *
     * @param className
     */
    public static boolean isPrimitive(String className) {

        return "int".equals(className) ||
                "float".equals(className) ||
                "boolean".equals(className) ||
                "double".equals(className) ||
                "byte".equals(className) ||
                "short".equals(className) ||
                "long".equals(className);


    }

    /**
     * Tests whether the given classname has a package the current logic is simple where we look
     * for a '.' inside the classname string
     *
     * @param className
     */
    public static boolean hasPackage(String className) {
        return (className.indexOf('.') != -1);
    }
}
