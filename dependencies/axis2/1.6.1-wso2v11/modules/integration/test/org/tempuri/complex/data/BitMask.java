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

package org.tempuri.complex.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BitMask {

    public static final BitMask BIT_ONE = new BitMask("BitOne");
    public static final BitMask BIT_TWO = new BitMask("BitTwo");
    public static final BitMask BIT_THREE = new BitMask("BitThree");
    public static final BitMask BIT_FOUR = new BitMask("BitFour");
    public static final BitMask BIT_FIVE = new BitMask("BitFive");
    private final String value;
    private static List values = new ArrayList();

    BitMask(String v) {
        value = v;
        values = new ArrayList();
        values.add(this);
    }

    public String value() {
        return value;
    }

    public static BitMask fromValue(String v) {
        Iterator iterator = values.iterator();
        while (iterator.hasNext()) {
            BitMask c = (BitMask) iterator.next();
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
