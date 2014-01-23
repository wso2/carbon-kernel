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

import org.apache.axis2.corba.idl.types.Typedef;
import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;

public class AliasValue extends AbstractValue {
    private Object value;
    public AliasValue(Typedef dataType) {
        super(dataType);
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void read(InputStream inputStream) {
        value = read(((Typedef) dataType).getDataType(), inputStream);
    }

    public void write(OutputStream outputStream) {
        write(value, ((Typedef) dataType).getDataType(), outputStream);
    }

    public String toString() {
        return (value == null)? " NULL value" : "Alias of: " + value.toString();
    }
}
