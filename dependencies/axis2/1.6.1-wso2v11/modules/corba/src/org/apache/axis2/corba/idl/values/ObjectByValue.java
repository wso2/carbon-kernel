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

import org.apache.axis2.corba.idl.types.Member;
import org.apache.axis2.corba.idl.types.ValueType;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;

public class ObjectByValue extends AbstractValue implements org.omg.CORBA.portable.StreamableValue {

    public ObjectByValue(ValueType valueType) {
        super(valueType);
    }

    public void _read(InputStream is) {
        read(is);
    }

    public void _write(OutputStream os) {
        write(os);
    }

    public TypeCode _type() {
        return dataType.getTypeCode();
    }

    public String[] _truncatable_ids() {
        return new String[] {dataType.getId()};
    }

    private void read(InputStream is) {
        org.omg.CORBA_2_3.portable.InputStream inputStream
                = (org.omg.CORBA_2_3.portable.InputStream) is;
        Member[] members = dataType.getMembers();
        memberValues = new Object[members.length];
        for (int i = 0; i < members.length; i++) {
            memberValues[i] = read(members[i].getDataType(), inputStream);
        }
    }

    private void write(OutputStream os) {
        org.omg.CORBA_2_3.portable.OutputStream outputStream
                = (org.omg.CORBA_2_3.portable.OutputStream) os;
        Member[] members = dataType.getMembers();
        for (int i = 0; i < members.length; i++) {
            write(memberValues[i], members[i].getDataType(), outputStream);
        }
    }
}
