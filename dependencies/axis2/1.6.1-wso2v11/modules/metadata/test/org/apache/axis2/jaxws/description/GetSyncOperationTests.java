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

import junit.framework.TestCase;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;
import java.util.concurrent.Future;

/**
 * 
 */
public class GetSyncOperationTests extends TestCase {
    
    public void testNoSyncOperation() {
        ServiceDescription sDesc = 
            DescriptionFactory.createServiceDescription(null, 
                                                        new QName("org.apache.axis2.jaxws.description", "syncOperationTestService2"), 
                                                        javax.xml.ws.Service.class);
        EndpointDescription eDesc = 
            DescriptionFactory.updateEndpoint(sDesc, 
                                              AsyncOnlySEI.class, 
                                              new QName("org.apache.axis2.jaxws.description", "syncOperationTestPort2"), 
                                              DescriptionFactory.UpdateType.GET_PORT);
        EndpointInterfaceDescription eiDesc = eDesc.getEndpointInterfaceDescription();
        OperationDescription opDescs[] = eiDesc.getOperations();
        assertNotNull(opDescs);
        assertEquals(2, opDescs.length);
        // Make sure each of the async operations reference the sync opDesc
        int asyncOperations = 0;
        OperationDescription syncOpDescs[] = eiDesc.getOperationForJavaMethod("echo");
        assertNull(syncOpDescs);
        for (OperationDescription opDesc : opDescs) {
            if (opDesc.isJAXWSAsyncClientMethod()) {
                asyncOperations++;
            }
            // Since there isn't a sync method in the (invalid) SEI, then this should return null
            assertNull(opDesc.getSyncOperation());
        }
        assertEquals(2, asyncOperations);
    }

    public void testSyncOperation() {
        ServiceDescription sDesc = 
            DescriptionFactory.createServiceDescription(null, 
                                                        new QName("org.apache.axis2.jaxws.description", "syncOperationTestService1"), 
                                                        javax.xml.ws.Service.class);
        EndpointDescription eDesc = 
            DescriptionFactory.updateEndpoint(sDesc, 
                                              SyncAndAsyncSEI.class, 
                                              new QName("org.apache.axis2.jaxws.description", "syncOperationTestPort1"), 
                                              DescriptionFactory.UpdateType.GET_PORT);
        EndpointInterfaceDescription eiDesc = eDesc.getEndpointInterfaceDescription();
        OperationDescription opDescs[] = eiDesc.getOperations();
        assertNotNull(opDescs);
        assertEquals(3, opDescs.length);
        // Make sure each of the async operations reference the sync opDesc
        int asyncOperations = 0;
        OperationDescription syncOpDescs[] = eiDesc.getOperationForJavaMethod("echo");
        assertNotNull(syncOpDescs);
        assertEquals(1, syncOpDescs.length);
        OperationDescription syncOpDesc = syncOpDescs[0];

        for (OperationDescription opDesc : opDescs) {
            if (opDesc.isJAXWSAsyncClientMethod()) {
                asyncOperations++;
            }
            // Make sure all the operations point to the sync operation
            assertEquals(syncOpDesc, opDesc.getSyncOperation());
        }
        
        assertEquals(2, asyncOperations);
    }


    public void testSyncMismatchedCaseOperation() {
        ServiceDescription sDesc = 
            DescriptionFactory.createServiceDescription(null, 
                                                        new QName("org.apache.axis2.jaxws.description", "syncOperationTestService3"), 
                                                        javax.xml.ws.Service.class);
        EndpointDescription eDesc = 
            DescriptionFactory.updateEndpoint(sDesc, 
                                              SyncAndAsyncSEIMismatchedCase.class, 
                                              new QName("org.apache.axis2.jaxws.description", "syncOperationTestPort3"), 
                                              DescriptionFactory.UpdateType.GET_PORT);
        EndpointInterfaceDescription eiDesc = eDesc.getEndpointInterfaceDescription();
        OperationDescription opDescs[] = eiDesc.getOperations();
        assertNotNull(opDescs);
        assertEquals(3, opDescs.length);
        // Make sure each of the async operations reference the sync opDesc
        int asyncOperations = 0;
        
        //essentially getting the java-cased sync method name "echo"
        OperationDescription syncOpDescs[] = eiDesc.getOperationForJavaMethod("echo");
        assertNotNull(syncOpDescs);
        assertEquals(1, syncOpDescs.length);
        // In this test case, only 1 sync method exists for the interface
        // SyncAndAsyncSEIMismatchedCase, namely "echo"
        OperationDescription syncOpDesc = syncOpDescs[0];

        for (OperationDescription opDesc : opDescs) {
            if (opDesc.isJAXWSAsyncClientMethod()) {
                asyncOperations++;
                // Make sure the sync operation can be found from the 
                // async operation's getSyncOperation() newly corrected
                // fail-safe algorithm
                assertEquals(syncOpDesc, opDesc.getSyncOperation());
            }
        }
        
        assertEquals(2, asyncOperations);
    }

}

@WebService
interface AsyncOnlySEI {
    // Note this is an INVALID SEI since it only contains the
    // JAXWS client async methods, and not the corresponding sync 
    // method.
    @WebMethod(operationName = "echo")
    public Response<String> echoAsync(String toEcho);
    @WebMethod(operationName = "echo")
    public Future<?> echoAsync(String toEcho, AsyncHandler<String> asyncHandler);
}

@WebService
interface SyncAndAsyncSEI {
    @WebMethod(operationName = "echo")
    public Response<String> echoAsync(String toEcho);
    @WebMethod(operationName = "echo")
    public Future<?> echoAsync(String toEcho, AsyncHandler<String> asyncHandler);
    @WebMethod(operationName = "echo")
    public String echo(String toEcho);
}

@WebService
interface SyncAndAsyncSEIMismatchedCase {
    @WebMethod(operationName = "Echo")
    public Response<String> echoAsync(String toEcho);
    @WebMethod(operationName = "Echo")
    public Future<?> echoAsync(String toEcho, AsyncHandler<String> asyncHandler);
    @WebMethod(operationName = "Echo")
    public String echo(String toEcho);
}

