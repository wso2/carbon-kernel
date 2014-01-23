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
import org.omg.CORBA.TypeCode;

import java.util.ArrayList;
import java.util.List;

public class EnumType extends CompositeDataType {
    private List enumMembers = new ArrayList();

    protected TypeCode generateTypeCode() {
        String[] arrMembers = new String[enumMembers.size()];
        for (int i = 0; i < enumMembers.size(); i++)
            arrMembers[i] = (String) enumMembers.get(i);

        return ORB.init ().create_enum_tc (getId(), getName(), arrMembers);        
    }

    public List getEnumMembers() {
        return enumMembers;
    }

    public void setEnumMembers(List enumMembers) {
        this.enumMembers = enumMembers;
    }

    public void addEnumMember(String s) {
        enumMembers.add(s);
    }
}
