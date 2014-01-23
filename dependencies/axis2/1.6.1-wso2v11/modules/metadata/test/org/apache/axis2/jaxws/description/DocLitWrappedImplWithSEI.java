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


package org.apache.axis2.jaxws.description;

import org.test.proxy.doclitwrapped.FinOpResponse;
import org.test.proxy.doclitwrapped.FinancialOperation;
import org.test.proxy.doclitwrapped.ReturnType;
import org.test.proxy.doclitwrapped.TwoWayHolder;

import javax.jws.WebService;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Holder;
import javax.xml.ws.Response;
import java.util.concurrent.Future;

/**
 * 
 */
@WebService(serviceName = "DocLitWrappedService",
            endpointInterface = "org.apache.axis2.jaxws.description.DocLitWrappedProxy")
public class DocLitWrappedImplWithSEI {

    public void oneWayVoid() {
        return;
    }

    public void oneWay(String onewayStr) {
        return;
    }

    public Response<TwoWayHolder> twoWayHolderAsync(String twoWayHolderStr, int twoWayHolderInt) {
        return null;
    }

    public Future<?> twoWayHolderAsync(String twoWayHolderStr, int twoWayHolderInt,
                                       AsyncHandler<TwoWayHolder> asyncHandler) {
        return null;
    }

    public void twoWayHolder(Holder<String> twoWayHolderStr, Holder<Integer> twoWayHolderInt) {
        return;
    }

    public Response<ReturnType> twoWayAsync(String twowayStr) {
        return null;
    }

    public Future<?> twoWayAsync(String twowayStr, AsyncHandler<ReturnType> asyncHandler) {
        return null;
    }

    public String twoWay(String twowayStr) {
        return null;
    }

    public Response<ReturnType> invokeAsync(String invokeStr) {
        return null;
    }

    public Future<?> invokeAsync(String invokeStr, AsyncHandler<ReturnType> asyncHandler) {
        return null;
    }

    public String invoke(String invokeStr) {
        return null;
    }

    public Response<FinOpResponse> finOpAsync(FinancialOperation op) {
        return null;
    }

    public Future<?> finOpAsync(FinancialOperation op, AsyncHandler<FinOpResponse> asyncHandler) {
        return null;
    }

    public FinancialOperation finOp(FinancialOperation op) {
        return null;
    }

}
