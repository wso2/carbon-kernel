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

package org.apache.axis2.databinding.utils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

/** Converter */
public class Converter {
    public static Object convert(Object arg, Class destClass) {
        if (destClass == null) {
            return arg;
        }

        Class argHeldType = null;
        if (arg != null) {
            argHeldType = getHolderValueType(arg.getClass());
        }

        if (arg != null && argHeldType == null && destClass.isAssignableFrom(arg.getClass())) {
            return arg;
        }

        // See if a previously converted value is stored in the argument.
        Object destValue = null;

        // Get the destination held type or the argument held type if they exist
        Class destHeldType = getHolderValueType(destClass);

        // Convert between Calendar and Date
        if (arg instanceof Calendar && destClass == Date.class) {
            return ((Calendar)arg).getTime();
        }
        if (arg instanceof Date && destClass == Calendar.class) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime((Date)arg);
            return calendar;
        }

        // Convert between Calendar and java.sql.Date
        if (arg instanceof Calendar && destClass == java.sql.Date.class) {
            return new java.sql.Date(((Calendar)arg).getTime().getTime());
        }

        // Convert between HashMap and Hashtable
        if (arg instanceof HashMap && destClass == Hashtable.class) {
            return new Hashtable((HashMap)arg);
        }

        // If the destination is an array and the source
        // is a suitable component, return an array with
        // the single item.
        if (arg != null &&
                destClass.isArray() &&
                !destClass.getComponentType().equals(Object.class) &&
                destClass.getComponentType().isAssignableFrom(arg.getClass())) {
            Object array =
                    Array.newInstance(destClass.getComponentType(), 1);
            Array.set(array, 0, arg);
            return array;
        }

        // Return if no conversion is available
        if (!(arg instanceof Collection ||
                (arg != null && arg.getClass().isArray())) &&
                ((destHeldType == null && argHeldType == null) ||
                        (destHeldType != null && argHeldType != null))) {
            return arg;
        }

        // Flow to here indicates that neither arg or destClass is a Holder

        if (arg == null) {
            return arg;
        }

        // The arg may be an array or List
        int length = 0;
        if (arg.getClass().isArray()) {
            length = Array.getLength(arg);
        } else {
            length = ((Collection)arg).size();
        }
        if (destClass.isArray()) {
            if (destClass.getComponentType().isPrimitive()) {

                Object array = Array.newInstance(destClass.getComponentType(),
                                                 length);
                // Assign array elements
                if (arg.getClass().isArray()) {
                    for (int i = 0; i < length; i++) {
                        Array.set(array, i, Array.get(arg, i));
                    }
                } else {
                    int idx = 0;
                    for (Iterator i = ((Collection)arg).iterator();
                         i.hasNext();) {
                        Array.set(array, idx++, i.next());
                    }
                }
                destValue = array;

            } else {
                Object [] array;
                try {
                    array = (Object [])Array.newInstance(destClass.getComponentType(),
                                                         length);
                } catch (Exception e) {
                    return arg;
                }

                // Use convert to assign array elements.
                if (arg.getClass().isArray()) {
                    for (int i = 0; i < length; i++) {
                        array[i] = convert(Array.get(arg, i),
                                           destClass.getComponentType());
                    }
                } else {
                    int idx = 0;
                    for (Iterator i = ((Collection)arg).iterator();
                         i.hasNext();) {
                        array[idx++] = convert(i.next(),
                                               destClass.getComponentType());
                    }
                }
                destValue = array;
            }
        } else if (Collection.class.isAssignableFrom(destClass)) {
            Collection newList = null;
            try {
                // if we are trying to create an interface, build something
                // that implements the interface
                if (destClass == Collection.class || destClass == java.util.List.class) {
                    newList = new ArrayList();
                } else if (destClass == Set.class) {
                    newList = new HashSet();
                } else {
                    newList = (Collection)destClass.newInstance();
                }
            } catch (Exception e) {
                // Couldn't build one for some reason... so forget it.
                return arg;
            }

            if (arg.getClass().isArray()) {
                for (int j = 0; j < length; j++) {
                    newList.add(Array.get(arg, j));
                }
            } else {
                for (Iterator j = ((Collection)arg).iterator();
                     j.hasNext();) {
                    newList.add(j.next());
                }
            }
            destValue = newList;
        } else {
            destValue = arg;
        }

        return destValue;
    }

    private static Class getHolderValueType(Class aClass) {
        return null;
    }
}
