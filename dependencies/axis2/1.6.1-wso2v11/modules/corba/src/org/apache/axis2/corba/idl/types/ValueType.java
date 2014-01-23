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

package org.apache.axis2.corba.idl.types;

import org.omg.CORBA.ORB;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.TypeCodePackage.BadKind;
import org.omg.CORBA.ValueMember;

import java.util.HashMap;

public class ValueType extends CompositeDataType {
	private java.util.Map operations;

	protected org.omg.CORBA.TypeCode generateTypeCode(){
        ORB orb = ORB.init();
        ValueMember[] valueMembers = new ValueMember[members.size()];
        String id = getId();
        for (int i = 0; i < members.size(); i++) {
            Member member = (Member) members.get(i);
            TypeCode typeCode = member.getDataType().getTypeCode();
            TCKind kind = typeCode.kind();
            switch(kind.value()) {
                case TCKind._tk_value :
                    String memberTypeId = "";
                    try {
                        memberTypeId = typeCode.id();
                    } catch (BadKind badKind) {
                        badKind.printStackTrace();
                    }
                    valueMembers[i] = new org.omg.CORBA.ValueMember(member.getName(), memberTypeId, id, "", typeCode,
                            null,org.omg.CORBA.PRIVATE_MEMBER.value);
                    break;
                default:
                    valueMembers[i] = new org.omg.CORBA.ValueMember(member.getName(), "", id, "", typeCode,
                            null,org.omg.CORBA.PRIVATE_MEMBER.value);
                    break;
            }
        }
        return orb.create_value_tc (id, getName(), (short)0, null, valueMembers);
	}

//    public Class getJavaType() {
//        return ObjectByValue.class;
//    }

    public java.util.Map getOperations() {
        return operations;
    }

    public void setOperations(java.util.Map operations) {
        this.operations = operations;
    }

    public void addOperation(Operation operation) {
        if (operations == null)
            operations = new HashMap();
        operations.put(operation.getName(), operation);
    }
}
