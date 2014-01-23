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

package org.apache.axis2.corba.receivers;

import org.apache.axis2.corba.exceptions.CorbaInvocationException;
import org.apache.axis2.corba.idl.types.IDL;
import org.apache.axis2.corba.idl.types.Interface;
import org.apache.axis2.corba.idl.types.Operation;

import java.util.Map;

public class CorbaInvokerFactory implements InvokerFactory {
    //private Map compositeDataTypes;
    private Map interfaces;

    public CorbaInvokerFactory(IDL idl){
        //this.compositeDataTypes = idl.getCompositeDataTypes();
        this.interfaces = idl.getInterfaces();
    }

	public Invoker newInvoker(String interfaceName, String operationName, org.omg.CORBA.Object object) throws CorbaInvocationException {
        Interface intf = (Interface) interfaces.get(interfaceName);

        if (intf==null)
            throw new CorbaInvocationException("Interface " + interfaceName + " not found");

        Map operations = intf.getOperationsMap();

        if (operations==null || operations.isEmpty())
            throw new CorbaInvocationException("Interface " + interfaceName + " does not have operations");

        Operation operation = (Operation) operations.get(operationName);

        if (operation==null)
            throw new CorbaInvocationException("Operation " + operationName + " not found in interface " + interfaceName);

        return new CorbaInvoker(operation, intf, /*compositeDataTypes,*/ object);
	}
}
