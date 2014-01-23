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
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.Parameter;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import java.util.ArrayList;

/**
 * 
 */
public class MustUnderstandTests extends TestCase {
    
    public void testHeaderParameters() {
        // Test IN and INOUT header paramaters in SEI
        ServiceDescription svcDesc = DescriptionFactory.createServiceDescription(HeaderParameters.class);
        assertNotNull(svcDesc);
        EndpointDescription epDescs[] = svcDesc.getEndpointDescriptions();
        assertNotNull(epDescs);
        assertEquals(1, epDescs.length);
        EndpointInterfaceDescription epiDesc = epDescs[0].getEndpointInterfaceDescription();
        assertNotNull(epiDesc);
        
        OperationDescription opDescs[] = epiDesc.getOperations();
        assertNotNull(opDescs);
        assertEquals(1, opDescs.length);
        OperationDescription opDesc = opDescs[0];
        assertEquals("echoString", opDesc.getOperationName());
        
        AxisOperation axisOperation = opDesc.getAxisOperation();
        assertNotNull(axisOperation);
        Parameter understoodQNamesParameter = axisOperation.getParameter(OperationDescription.HEADER_PARAMETER_QNAMES);
        assertNotNull(understoodQNamesParameter);
        ArrayList understoodQNames = (ArrayList) understoodQNamesParameter.getValue();
        assertEquals(6, understoodQNames.size());
        
        assertTrue(understoodQNames.contains(new QName("webservice.namespace", "renamedParam1")));
        assertTrue(understoodQNames.contains(new QName("webservice.namespace", "arg1")));
        assertTrue(understoodQNames.contains(new QName("webparam.namespace", "arg2")));
        assertFalse(understoodQNames.contains(new QName("webservice.namespace", "outOnly")));
        assertTrue(understoodQNames.contains(new QName("webservice.namespace", "arg3")));
        assertTrue(understoodQNames.contains(new QName("webservice.namespace", "inOut")));
        assertFalse(understoodQNames.contains(new QName("webservice.namespace", "arg4")));
        assertFalse(understoodQNames.contains(new QName("webservice.namespace", "notInHeader")));
        assertFalse(understoodQNames.contains(new QName("webservice.namespace", "arg5")));
        assertTrue(understoodQNames.contains(new QName("webservice.namespace", "headerReturn")));
        
    }
}

@WebService(targetNamespace="webservice.namespace")
class HeaderParameters {
    @WebMethod
    @WebResult(name = "headerReturn", header=true)
    public String echoString(
            @WebParam(name="renamedParam1", header=true) String param1,
            @WebParam(header=true) String param2,
            @WebParam(targetNamespace="webparam.namespace", header=true) String param3,
            @WebParam(mode=WebParam.Mode.OUT, header=true) Holder<String> outOnly,
            @WebParam(name="inOut", mode=WebParam.Mode.INOUT, header=true) Holder<String> inOut,
            String notInHeader) {
                String headerReturn = param2; 
                return headerReturn;
            }
}

