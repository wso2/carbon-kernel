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
import org.apache.axis2.corba.idl.types.Struct;
import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;

public class StructValue extends AbstractValue {

    public StructValue (Struct struct) {
        super(struct);
    }

    public void write(OutputStream outputStream) {
        Member[] members = getMembers();
        for (int i = 0; i < members.length; i++) {
            write(memberValues[i], members[i].getDataType(), outputStream);
        }
    }

    public void read(InputStream inputStream) {
        Member[] members = getMembers();
        memberValues = new Object[members.length];
        for (int i = 0; i < members.length; i++) {
            memberValues[i] = read(members[i].getDataType(), inputStream);
        }
    }
}
