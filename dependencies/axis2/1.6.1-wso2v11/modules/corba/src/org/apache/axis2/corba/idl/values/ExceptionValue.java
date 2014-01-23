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

import org.apache.axis2.corba.exceptions.CorbaInvocationException;
import org.apache.axis2.corba.idl.types.ExceptionType;
import org.apache.axis2.corba.idl.types.Member;
import org.omg.CORBA_2_3.portable.InputStream;

public class ExceptionValue extends AbstractValue {

    public ExceptionValue(ExceptionType exceptionType) {
        super(exceptionType);
    }

    public void read(InputStream inputStream) {
        Member[] members = getMembers();
        Object[] memberValues = new Object[members.length];
        if (!dataType.getId().equals(inputStream.read_string()))
            throw new RuntimeException("Mismaching IDs");
        for (int i = 0; i < members.length; i++) {
            memberValues[i] = read(members[i].getDataType(), inputStream);
        }
        setMemberValues(memberValues);
    }

    public CorbaInvocationException getException() {
        return new CorbaInvocationException(toString());
    }

    public String toString() {
        Member[] members = getMembers();
        String ret = "Exception name: " + dataType.getModule() + dataType.getName() + '\n';
        for (int i = 0; i < members.length; i++) {
            Object value = memberValues[i];
            ret += '\t' + members[i].getName() + ": " + value + '\n';
        }
        return ret;
    }
}
