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

import org.apache.axis2.corba.idl.types.DataType;
import org.apache.axis2.corba.idl.types.EnumType;
import org.apache.axis2.corba.idl.types.Member;
import org.apache.axis2.corba.idl.types.UnionMember;
import org.apache.axis2.corba.idl.types.UnionType;
import org.apache.axis2.corba.receivers.CorbaUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.omg.CORBA.TCKind;
import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;

public class UnionValue extends AbstractValue {
    private static final Log log = LogFactory.getLog(UnionValue.class);
    private Object discriminator;
    private String memberName;
    private Object memberValue;
    private DataType memberType;


    public UnionValue(UnionType unionType) {
        super(unionType);    
    }

    public void read(InputStream inputStream) {
        UnionType unionType = (UnionType) dataType;
        discriminator = read(unionType.getDiscriminatorType(), inputStream);
        populateValue();
        memberValue = read(getMemberType(), inputStream);
    }

    private void populateValue() {
        Member[] members = getMembers();
        UnionMember unionMember = null;

        String discriminatorStr;
        if (discriminator instanceof EnumValue) {
            discriminatorStr = ((EnumValue) discriminator).getValueAsString();
        } else {
            discriminatorStr = discriminator.toString();
        }

        for (int i = 0; i < members.length; i++) {
            unionMember = (UnionMember) members[i];
            if (discriminatorStr.equals(unionMember.getDiscriminatorValue()))
                break;
        }
        if (unionMember != null) {
            memberName = unionMember.getName();
            setMemberType(unionMember.getDataType());
        } else {
            log.error("Union must have atleast one members");
        }
    }

    private void populateDiscriminator() {
        Member[] members = getMembers();
        UnionMember unionMember = null;
        for (int i = 0; i < members.length; i++) {
            unionMember = (UnionMember) members[i];
            if (unionMember.getName().equals(memberName))
                break;
        }
        if (unionMember != null) {
            setMemberType(unionMember.getDataType());
            if (!unionMember.isDefault()) {
                discriminator = CorbaUtil.parseValue(((UnionType)dataType).getDiscriminatorType(), unionMember.getDiscriminatorValue());
            } else if (unionMember.isDefault()) {
                DataType discriminatorType = ((UnionType)dataType).getDiscriminatorType();
                int kindVal = discriminatorType.getTypeCode().kind().value();
                switch (kindVal) {
                    case TCKind._tk_long:
                        discriminator = Integer.valueOf(-2147483648);
                        break;
                    case TCKind._tk_char:
                    case TCKind._tk_wchar:
                        discriminator = Character.valueOf('\u0000');
                        break;
                    case TCKind._tk_enum:
                        EnumType enumType = (EnumType) discriminatorType;
                        EnumValue enumValue = new EnumValue(enumType);
                        enumValue.setValue(0);
                        discriminator = enumValue;
                        break;
                    default:
                        log.error("Unsupported union member type");                        
                }
            } else {
                discriminator = null;
            }
        }
    }

    public void write(OutputStream outputStream) {
        populateDiscriminator();
        write(discriminator, ((UnionType) dataType).getDiscriminatorType(), outputStream);
        write(memberValue, getMemberType(), outputStream);
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public Object getMemberValue() {
        return memberValue;
    }

    public void setMemberValue(Object memberValue) {
        this.memberValue = memberValue;
    }

    public DataType getMemberType() {
        return memberType;
    }

    public void setMemberType(DataType memberType) {
        this.memberType = memberType;
    }
}
