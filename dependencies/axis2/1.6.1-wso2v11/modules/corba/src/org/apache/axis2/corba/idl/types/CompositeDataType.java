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

import java.util.ArrayList;
import java.util.List;

public abstract class CompositeDataType extends DataType {
    public static final String IDL_REPO_STRING = "IDL:";
    public static final String IDL_VERSION = ":1.0";
    public static final String MODULE_SEPERATOR = "::";

	protected String id;
	protected String name;
	protected String module;
	protected List members = new ArrayList();

    public String getId() {
        if (id==null)
            id = IDL_REPO_STRING + module.replaceAll(MODULE_SEPERATOR, "/") + name + IDL_VERSION;
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public void addMember(Member member) {
        members.add(member);
    }

    public Member[] getMembers() {
        Member[] membersArray = new Member[members.size()];
        for (int i = 0; i < members.size(); i++) {
            membersArray[i] = (Member) members.get(i);
        }
        return membersArray;
    }
}
