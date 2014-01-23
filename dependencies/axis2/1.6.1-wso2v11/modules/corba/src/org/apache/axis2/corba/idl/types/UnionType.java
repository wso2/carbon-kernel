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

import org.apache.axis2.corba.receivers.CorbaUtil;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TypeCode;

public class UnionType extends CompositeDataType {
    private DataType discriminatorType;

    protected TypeCode generateTypeCode() {
        ORB orb = ORB.init();
        TypeCode disTypeCode = discriminatorType.getTypeCode();
        org.omg.CORBA.UnionMember[] unmembers = new org.omg.CORBA.UnionMember [members.size()];

        Any memberAny;
        UnionMember unionMember;
        for (int i = 0; i < members.size(); i++) {
            unionMember = (UnionMember) members.get(i);
            memberAny = orb.create_any();
            if (unionMember.isDefault()) {
                memberAny.insert_octet((byte) 0); // default member label
            } else {
                CorbaUtil.insertValue(memberAny, discriminatorType, CorbaUtil.parseValue(discriminatorType, unionMember.getDiscriminatorValue()));
            }
            unmembers[i] = new org.omg.CORBA.UnionMember (
                    unionMember.getName(),
                    memberAny,
                    unionMember.getDataType().getTypeCode(),
                    null);
        }
        return orb.create_union_tc (getId(), name, disTypeCode, unmembers);
    }

    public DataType getDiscriminatorType() {
        return discriminatorType;
    }

    public void setDiscriminatorType(DataType discriminatorType) {
        this.discriminatorType = discriminatorType;
    }
}
