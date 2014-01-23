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
import org.apache.axis2.corba.idl.types.DataType;
import org.apache.axis2.corba.idl.types.ExceptionType;
import org.apache.axis2.corba.idl.types.Interface;
import org.apache.axis2.corba.idl.types.Member;
import org.apache.axis2.corba.idl.types.Operation;
import org.apache.axis2.corba.idl.values.ExceptionValue;
import org.omg.CORBA.Any;
import org.omg.CORBA.ExceptionList;
import org.omg.CORBA.Request;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.UnknownUserException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class CorbaInvoker implements Invoker {
    private Operation operation;
    private Interface intf;
    //private Map compositeDataTypes;
    private org.omg.CORBA.Object object;
    private Object[] parameters;
    private List parameterTypeList = new ArrayList();
    private List returnedParams;

    protected CorbaInvoker(Operation operation, Interface intf, org.omg.CORBA.Object object) {
        this.operation = operation;
        this.intf = intf;
        this.object = object;

        List params = operation.getParams();
        if (params!=null) {
            for (int i = 0; i < params.size(); i++) {
                Member member = (Member) params.get(i);
                parameterTypeList.add(member);
            }
        }
    }

    public Object invoke() throws CorbaInvocationException {
        // Create request
        Request request = object._request(operation.getName());

        // Set parameters
        Any arg = null;
        List memArgs = new ArrayList();
        if (parameters!=null) {
            List patamList = new LinkedList(Arrays.asList(parameters));
            Iterator paramsIter = patamList.iterator();
            for (int i = 0; i < parameterTypeList.size(); i++) {
                Member member = (Member) parameterTypeList.get(i);
                DataType type = member.getDataType();
                Object value = null;
                String mode = member.getMode();
                if (mode.equals(Member.MODE_IN)) {
                    arg = request.add_in_arg();
                    value = paramsIter.next();
                }else if (mode.equals(Member.MODE_INOUT)) {
                    arg = request.add_inout_arg();
                    value = paramsIter.next();
                } else if (mode.equals(Member.MODE_OUT)) {
                    arg = request.add_out_arg();
                    value = CorbaUtil.getEmptyValue(type);
                }

                memArgs.add(arg);
                CorbaUtil.insertValue(arg, type, value);
            }
        }

        // Set return type
        DataType returnType = operation.getReturnType();
        if (returnType!=null) {
            TypeCode typeCode = returnType.getTypeCode();
            request.set_return_type(typeCode);
        }

        // Set exceptions
        List exceptions = operation.getRaises();
        if (exceptions!=null && !exceptions.isEmpty()) {
            ExceptionList exceptionList = request.exceptions();
            for (int i = 0; i < exceptions.size(); i++) {
                ExceptionType exType = (ExceptionType) exceptions.get(i);
                exceptionList.add(exType.getTypeCode());
            }
        }

        // Invoke
        request.invoke();

        // Get exception
        Object returnValue = null;
        Exception exception = request.env().exception();
        if (exception == null) {
            // Extract the return value
            if (returnType != null) {
                Any returned = request.return_value();
                returnValue = CorbaUtil.extractValue(returnType, returned);
            }

            // Extract the values of inout and out parameters
            returnedParams = new ArrayList();
            for (int i = 0; i < parameterTypeList.size(); i++) {
                Member member = (Member) parameterTypeList.get(i);
                String mode = member.getMode();
                if (mode.equals(Member.MODE_INOUT) || mode.equals(Member.MODE_OUT)) {
                    returnedParams.add(CorbaUtil.extractValue(member.getDataType(), (Any) memArgs.get(i)));
                }
            }
        } else {
            if(exception instanceof UnknownUserException) {
                UnknownUserException userException = (UnknownUserException) exception;
                TypeCode exTypeCode = userException.except.type();
                ExceptionType exceptionType = null;
                if (exceptions!=null && !exceptions.isEmpty()) {
                    for (int i = 0; i < exceptions.size(); i++) {
                        ExceptionType exType = (ExceptionType) exceptions.get(i);
                        if (exTypeCode.equal(exType.getTypeCode())) {
                            exceptionType = exType;
                            break;
                        }
                    }
                }
                if (exceptionType==null) {
                    throw new CorbaInvocationException(exception);
                } else {
                    ExceptionValue exceptionValue = (ExceptionValue) CorbaUtil.extractValue(exceptionType, userException.except);
                    if (exceptionValue!=null)
                        throw exceptionValue.getException();
                }
            } else {
                throw new CorbaInvocationException(exception);
            }
        }

        return returnValue;
    }

    public void setParameters(Object[] parameters){
        this.parameters = parameters;
    }

    public String getInterfaceName(){
        return intf.getName();
    }

    public String getOperationName(){
        return operation.getName();
    }

    public DataType getReturnType() {
        return operation.getReturnType();
    }

    public Object[] getOutParameterValuess() {
        if (returnedParams == null)
            return null;
        else
            return returnedParams.toArray();
    }

    public Member[] getParameterMembers() {
        Member[] membersArray = new Member[parameterTypeList.size()];
        for (int i = 0; i < parameterTypeList.size(); i++) {
            membersArray[i] = (Member) parameterTypeList.get(i);
        }
        return membersArray;
    }
}
