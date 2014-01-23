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

package org.apache.axis2.jaxws.description.feature;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.description.DescriptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.soap.MTOM;

public class MTOMFeatureTests extends TestCase {
    
    private static final String ns = "http://jaxws.axis2.apache.org/metadata/feature/mtom";
    
    private static final String defaultServicePortName = "DefaultServicePort";
    private static final String plainServicePortName = "PlainServicePort";
    private static final String disabledServicePortName = "DisabledServicePort";
    private static final String thresholdServicePortName = "ThresholdServicePort";
    private static final String badThresholdServicePortName = "BadThresholdServicePort";
    
    public void testNoAnnotation() {
        ServiceDescription sd = DescriptionFactory.createServiceDescription(DefaultService.class);
        
        EndpointDescription ed = sd.getEndpointDescription(new QName(ns, defaultServicePortName));
        assertTrue("The EndpointDescription should not be null.", ed != null);
        
        boolean mtomEnabled = ed.isMTOMEnabled();
        assertTrue("MTOM should not be enabled by default", mtomEnabled == false);
    }
    
    public void testPlainAnnotation() {
        ServiceDescription sd = DescriptionFactory.createServiceDescription(PlainService.class);
        
        EndpointDescription ed = sd.getEndpointDescription(new QName(ns, plainServicePortName));
        assertTrue("The EndpointDescription should not be null.", ed != null);
        
        boolean mtomEnabled = ed.isMTOMEnabled();
        assertTrue("@MTOM included, but was not enabled.", mtomEnabled == true);
        
        int threshold = ed.getMTOMThreshold();
        assertTrue("MTOM threshold should be 0 or less.", threshold <= 0);
    }
    
    public void testDisabled() {
        ServiceDescription sd = DescriptionFactory.createServiceDescription(DisabledService.class);
        
        EndpointDescription ed = sd.getEndpointDescription(new QName(ns, disabledServicePortName));
        assertTrue("The EndpointDescription should not be null.", ed != null);
        
        boolean mtomEnabled = ed.isMTOMEnabled();
        assertTrue("@MTOM included, and should be disabled.", mtomEnabled == false);
    }
    
    public void testThreshold() {
        ServiceDescription sd = DescriptionFactory.createServiceDescription(ThresholdService.class);
        
        EndpointDescription ed = sd.getEndpointDescription(new QName(ns, thresholdServicePortName));
        assertTrue("The EndpointDescription should not be null.", ed != null);
        
        int threshold = ed.getMTOMThreshold();
        assertTrue("MTOM threshold should be 2000.", threshold == 20000);
    }
    
    public void testBadThreshold() {
        ServiceDescription sd = DescriptionFactory.createServiceDescription(BadThresholdService.class);
        
        EndpointDescription ed = sd.getEndpointDescription(new QName(ns, badThresholdServicePortName));
        assertTrue("The EndpointDescription should not be null.", ed != null);
        
        int threshold = ed.getMTOMThreshold();
        assertTrue("MTOM threshold should be [0], but was [" + threshold + "].", threshold == 0);
    }
    
    @WebService(targetNamespace=ns, portName=defaultServicePortName)
    class DefaultService {
        public double getQuote(String symbol) {
            return 101.01;
        }
    }
    
    @WebService(targetNamespace=ns, portName=plainServicePortName)
    @MTOM
    class PlainService {
        public double getQuote(String symbol) {
            return 101.01;
        }
    }
    
    @WebService(targetNamespace=ns, portName=disabledServicePortName)
    @MTOM(enabled=false)
    class DisabledService {
        public double getQuote(String symbol) {
            return 101.01;
        }
    }
    
    @WebService(targetNamespace=ns, portName=thresholdServicePortName)
    @MTOM(threshold=20000)
    class ThresholdService {
        public double getQuote(String symbol) {
            return 101.01;
        }
    }
    
    @WebService(targetNamespace=ns, portName=badThresholdServicePortName)
    @MTOM(threshold=-1000)
    class BadThresholdService {
        public double getQuote(String symbol) {
            return 101.01;
        }
    }
}
