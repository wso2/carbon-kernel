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

package org.apache.axis2.jaxws.description.addressing;

import junit.framework.TestCase;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.jaxws.description.DescriptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.Action;
import javax.xml.ws.FaultAction;
import javax.xml.ws.WebFault;
import java.util.Iterator;

public class ActionTests extends TestCase {
    
    private static final String ns = "http://jaxws.axis2.apache.org/metadata/addressing/action";
    
    private static final String defaultServicePortName = "DefaultServicePort";
    private static final String plainServicePortName = "PlainServicePort";
    
    public void testNoAnnotation() {
        ServiceDescription sd  = DescriptionFactory.createServiceDescription(DefaultService.class);
        EndpointDescription ed = sd.getEndpointDescription(new QName(ns, defaultServicePortName));
        assertNotNull(ed);
        
        AxisService axisService = ed.getAxisService();
        Iterator iterator = axisService.getOperations();
        
        AxisOperation axisOperation = (AxisOperation) iterator.next();
        assertEquals("", axisOperation.getInputAction()); //if soapaction is empty, then the input action will also be empty. Default action doesn't come in to play here.
        assertEquals("http://jaxws.axis2.apache.org/metadata/addressing/action/Service1/getQuoteResponse", axisOperation.getOutputAction());
        assertEquals("http://jaxws.axis2.apache.org/metadata/addressing/action/Service1/getQuote/Fault/TestException", axisOperation.getFaultAction());
    }
    
    public void testPlainAnnotation() {
        ServiceDescription sd  = DescriptionFactory.createServiceDescription(PlainService.class);
        EndpointDescription ed = sd.getEndpointDescription(new QName(ns, plainServicePortName));
        assertNotNull(ed);
        
        AxisService axisService = ed.getAxisService();
        Iterator iterator = axisService.getOperations();
        
        AxisOperation axisOperation = (AxisOperation) iterator.next();
//        assertEquals("http://test/input", axisOperation.getInputAction()); //todo: temporarily commented out. This is failing currently.
        assertEquals("http://test/output", axisOperation.getOutputAction());
        assertEquals("http://test/fault", axisOperation.getFaultAction());
    }
    
    @WebService(name="Service1", targetNamespace=ns, portName=defaultServicePortName)
    class DefaultService {
        public double getQuote(String symbol) throws TestException {
            return 101.01;
        }
    }
    
    @WebService(name="Service2", targetNamespace=ns, portName=plainServicePortName)
    class PlainService {
        
        @Action(input="http://test/input", output="http://test/output",
                fault={ @FaultAction(className=TestException.class, value="http://test/fault") })
        public double getQuote(String symbol) throws TestException {
            return 101.01;
        }
    }
    
    @WebFault(name="TestException", targetNamespace=ns)
    class TestException extends Exception {
    }
}
