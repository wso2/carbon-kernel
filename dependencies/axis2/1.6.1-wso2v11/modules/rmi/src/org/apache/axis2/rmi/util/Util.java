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

package org.apache.axis2.rmi.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class Util {
    private static int i = 0;

    public synchronized static String getNextNamespacePrefix() {
        if (i > 1000) {
            i = 0;
        }
        return "ns" + ++i;
    }

    private static Set primitives;

    static {
        primitives = new HashSet();
        primitives.add(int.class);
        primitives.add(long.class);
        primitives.add(float.class);
        primitives.add(double.class);
        primitives.add(short.class);
        primitives.add(byte.class);
        primitives.add(boolean.class);
        primitives.add(char.class);
    }

    public boolean isPrimitive(Class type) {
        return primitives.contains(type);
    }

    public static int getClassType(Class className)
            throws IllegalAccessException,
            InstantiationException {
        int type = Constants.OTHER_TYPE;
        // if it is an array then it can not be a collection type
        if (List.class.isAssignableFrom(className)) {
            type = Constants.LIST_TYPE | Constants.COLLECTION_TYPE;
        } else if (Set.class.isAssignableFrom(className)) {
            type = Constants.SET_TYPE | Constants.COLLECTION_TYPE;
        } else if (Map.class.isAssignableFrom(className)) {
            type = Constants.MAP_TYPE;
        } else if (Collection.class.isAssignableFrom(className)) {
            type = Constants.COLLECTION_TYPE;
        }
        return type;
    }
}
