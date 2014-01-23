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

import java.util.Comparator;
import java.util.Arrays;
import java.lang.reflect.Field;

public class FieldComparator implements Comparator {
    public int compare(Object o1, Object o2) {
        Field field1 = (Field) o1;
        Field field2 = (Field) o2;
        String[] values = new String[2];
        values[0] = field1.getName();
        values[1] = field2.getName();
        Arrays.sort(values);
        if (values[0].equals(field1.getName())) {
            return 0;
        } else {
            return 1;
        }
    }
}
