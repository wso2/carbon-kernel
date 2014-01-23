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

package org.apache.axis2.corba.idl.values;

import org.apache.axis2.corba.idl.types.ValueType;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA_2_3.ORB;
import org.omg.CORBA_2_3.portable.InputStream;

import java.io.Serializable;

public class StreamableValueFactory implements ValueFactory {
    private ValueType valueType;

    private StreamableValueFactory(ValueType valueType) {
        this.valueType = valueType;
    }

    public Serializable read_value(InputStream inputStream) {
        return inputStream.read_value(new ObjectByValue(valueType));
    }

    public static void register(ORB orb, ValueType valueType) {
        orb.register_value_factory(valueType.getId(), new StreamableValueFactory(valueType));
    }
}
