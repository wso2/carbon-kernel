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

package org.apache.axis2.jaxws.spi;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.description.EndpointDescription;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;

/**
 * The client APIs should each implement the org.apache.axis2.jaxws.spi.BindingProvider
 * interface.  This suite should be used for any client APIs to verify that they 
 * maintain that behavior. 
 */
public class BindingProviderTests extends TestCase {
    
    public QName serviceQName;
    public QName portQName;
    
    public BindingProviderTests(String name) {
        super(name);
        
        serviceQName = new QName("http://test", "TestService");
        portQName = new QName("http://test", "TestPort");
    }
    
    /**
     * A test to verify that the Dispatch objects implement the proper interface
     */
    public void testDisptachBindingProviderSPI() {
        Service svc = Service.create(serviceQName);
        svc.addPort(portQName, SOAPBinding.SOAP11HTTP_BINDING, "");
        
        Dispatch dsp = svc.createDispatch(portQName, Source.class, Service.Mode.MESSAGE);
        
        // Make sure we can cast the object to the right interfaces
        assertTrue("The Dispatch object should also be a javax.xml.ws.BindingProvider", 
                (dsp instanceof javax.xml.ws.BindingProvider));
        assertTrue("The Dispatch object should also be a org.apache.axis2.jaxws.spi.BindingProvider", 
                dsp instanceof org.apache.axis2.jaxws.spi.BindingProvider);
        
        org.apache.axis2.jaxws.spi.BindingProvider bp = (org.apache.axis2.jaxws.spi.BindingProvider) dsp;
        
        ServiceDelegate sd = bp.getServiceDelegate();
        assertTrue("The ServiceDescription was null", sd != null);
        
        EndpointDescription ed = bp.getEndpointDescription();
        assertTrue("The EndpointDescription was null", ed != null);
    }

    /**
     * A test to verify that the proxy objects implement the proper interface.
     */
    public void testProxyBindingProviderSPI() {
        Service svc = Service.create(serviceQName);
        Sample s = svc.getPort(Sample.class);
        
        // Make sure we can cast the object to the right interfaces
        assertTrue("The Proxy object should also be a javax.xml.ws.BindingProvider",
                s instanceof javax.xml.ws.BindingProvider);
        assertTrue("The Proxy object should also be a org.apache.axis2.jaxws.spi.BindingProvider",
                s instanceof org.apache.axis2.jaxws.spi.BindingProvider);
        
        org.apache.axis2.jaxws.spi.BindingProvider bp = (org.apache.axis2.jaxws.spi.BindingProvider) s;
        
        ServiceDelegate sd = bp.getServiceDelegate();
        assertTrue("The ServiceDescription was null", sd != null);
        
        EndpointDescription ed = bp.getEndpointDescription();
        assertTrue("The EndpointDescription was null", ed != null);
    }
}
