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

/**
 * 
 */
package org.apache.axis2.jaxws.sample.faults;

import org.test.faults.FaultyWebServiceFault;
import org.test.faults.FaultyWebServiceResponse;

import javax.jws.WebService;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;
import java.util.concurrent.Future;

@WebService(serviceName="FaultyWebServiceService",
			endpointInterface="org.apache.axis2.jaxws.sample.faults.FaultyWebServicePortType")
			public class FaultyWebServicePortTypeImpl {

    /* (non-Javadoc)
     * @see org.apache.axis2.jaxws.sample.faults.FaultyWebServicePortType#faultyWebService(int)
     */
    public int faultyWebService(int arg0) throws FaultyWebServiceFault_Exception {

        FaultyWebServiceFault bean = new FaultyWebServiceFault();
        bean.setFaultInfo("bean custom fault info");
        bean.setMessage("bean custom message");

        throw new FaultyWebServiceFault_Exception("custom exception", bean);
    }

    public Future<?> faultyWebServiceAsync(int arg0,
                                           AsyncHandler<FaultyWebServiceResponse> asyncHandler) {
        return null;
    }


    public Response<FaultyWebServiceResponse> faultyWebServiceAsync(int arg0) {
        return null;
    }



}
