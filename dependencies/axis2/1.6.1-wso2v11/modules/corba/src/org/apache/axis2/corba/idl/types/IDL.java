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

import java.util.Map;
import java.util.TreeMap;

public class IDL {
	private Map compositeDataTypes;
	private Map interfaces;

    public Map getCompositeDataTypes() {
        return compositeDataTypes;
    }

    public void setCompositeDataTypes(java.util.Map compositeDataTypes) {
        this.compositeDataTypes = compositeDataTypes;
    }

    public void addType(CompositeDataType dataType) {
        if (compositeDataTypes == null)
            compositeDataTypes = new TreeMap();
        compositeDataTypes.put(dataType.getModule() + dataType.getName(), dataType);
    }

    public java.util.Map getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(java.util.Map interfaces) {
        this.interfaces = interfaces;
        if (compositeDataTypes == null)
            compositeDataTypes = new TreeMap();
        this.compositeDataTypes.putAll(interfaces);
    }

    public void addInterface(Interface intf) {
        if (interfaces == null)
            interfaces = new TreeMap();
        interfaces.put(intf.getModule() + intf.getName(), intf);
        if (compositeDataTypes == null)
            compositeDataTypes = new TreeMap();
        compositeDataTypes.put(intf.getModule() + intf.getName(), intf);
    }

    public void addIDL (IDL childIdl) {
        Map temp = childIdl.getInterfaces();
        if (temp!=null) {
            if (interfaces==null)
                interfaces = new TreeMap();
            interfaces.putAll(temp);
        }
        temp = childIdl.getCompositeDataTypes();
        if (temp!=null) {
            if (compositeDataTypes==null)
                compositeDataTypes = new TreeMap();
            compositeDataTypes.putAll(temp);
        }
    }
}
