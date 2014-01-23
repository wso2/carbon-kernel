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

/**
 * 
 */
package org.apache.axis2.jaxws.misc;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * Sample Enum
 */
@XmlEnum(value=java.lang.Integer.class)
@XmlType(name="SampleEnum", namespace="urn://misc.jaxws.axis2.apache.org")
public enum EnumSample3 {
    DATA_A3(10), DATA_B3(20), DATA_C3(30);
    final private Integer value;
    EnumSample3(int data) {
        value = data;
    }
    public int value() { return value; }
    public static EnumSample3 fromValue(int value) {
        if (value == 10) {
            return DATA_A3;
        } else if (value == 20) {
            return DATA_B3;
        } else if (value == 30) {
            return DATA_C3;
        }
        return null;
    }
                
}