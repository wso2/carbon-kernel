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

import org.omg.CORBA.TypeCode;

import java.util.Iterator;
import java.util.TreeMap;

public class Interface extends CompositeDataType {
	private java.util.Map operations;

    protected TypeCode generateTypeCode(){
        return org.omg.CORBA.ORB.init ().create_interface_tc (getId(), getName());
	}

    public java.util.Map getOperationsMap() {
        return operations;
    }

    public Operation[] getOperations() {
        Operation[] operationsArr = new Operation[operations.size()];
        Iterator iter = operations.values().iterator();
        int i = 0;
        while (iter.hasNext()) {
            operationsArr[i] = (Operation) iter.next();
            i++;
        }
        return operationsArr;
    }

    public void addOperation(Operation operation) {
        if (operations == null)
            operations = new TreeMap();
        operations.put(operation.getName(), operation);
    }
}
