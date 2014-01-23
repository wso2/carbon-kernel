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

package org.apache.axis2.jaxws.endpoint;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.jws.WebService;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Binding;
import javax.xml.ws.Endpoint;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.soap.SOAPBinding;

import junit.framework.TestCase;

import org.w3c.dom.Element;

public class BasicEndpointTests extends TestCase {

    public void testCreateSimpleEndpoint() {
        SampleEndpoint sample = new SampleEndpoint();
        
        Endpoint ep = Endpoint.create(sample);
        assertTrue("The returned Endpoint instance was null", ep != null);
        
        ep.publish("test");
        assertTrue("The endpoint was not published successfully", ep.isPublished());
        ep.stop();
    }
    
    public void testCreateAndPublishEndpoint() {
        SampleEndpoint sample = new SampleEndpoint();

        Endpoint ep = Endpoint.publish("test" , sample);
        assertTrue("The returned Endpoint instance was null", ep != null);
        assertTrue("The endpoint was not published successfully", ep.isPublished());
        ep.stop();
    }
    
    public void testGetBinding() throws Exception {
        SampleEndpoint sample = new SampleEndpoint();

        Endpoint ep = Endpoint.create(sample);
        assertTrue("The returned Endpoint instance was null", ep != null);

        Binding bnd = ep.getBinding();
        assertTrue("The returned Binding instance was null", bnd != null);
        assertTrue("The returned Binding instance was of the wrong type (" + bnd.getClass().getName() + "), expected SOAPBinding", 
                SOAPBinding.class.isAssignableFrom(bnd.getClass()));
        ep.stop();
    }
    
    public void testGetEndpointReference() throws Exception {
        SampleEndpoint sample = new SampleEndpoint();

        Endpoint ep = Endpoint.publish("test" , sample);
        assertNotNull("The returned Endpoint instance was null", ep);
        assertTrue("The endpoint was not published successfully", ep.isPublished());
        
        Element [] refParams = new Element[0];
        EndpointReference epr = ep.getEndpointReference(refParams);
        
        assertNotNull("The returned EndpointReference instance was null", epr);
        
        ep.stop();
    }
    
    public void testMetadata() throws Exception {
        SampleEndpoint sample = new SampleEndpoint();
        
        Endpoint ep = Endpoint.create(sample);
        assertTrue("The returned Endpoint instance was null", ep != null);
        
        ep.publish("test");
        assertTrue("The endpoint was not published successfully", ep.isPublished());
        
        String wsdlLocation = "http://test.wsdl.com/Test.wsdl"; // Dummy URL
        List<Source> metadata = new ArrayList<Source>();
        Source source = new StreamSource(new ByteArrayInputStream(new byte[0])); // Dummy content  
        source.setSystemId(wsdlLocation);  
        metadata.add(source);
        ep.setMetadata(metadata);
        
        metadata = ep.getMetadata();
        assertNotNull(metadata);
        source = metadata.get(0);
        assertNotNull(source);
        assertEquals(source.getSystemId(), wsdlLocation);
        
        ep.stop();
    }

    public void testCreateAndPublishOnAlternatePort() throws Exception {
        Endpoint ep = Endpoint.create(new SampleEndpoint());
        ep.publish("http://localhost:16060/SampleEndpoint");
        assertTrue("The returned Endpoint instance was null", ep != null);
        assertTrue("The endpoint was not published successfully", ep.isPublished());
        ep.stop();
    }

    @WebService
    class SampleEndpoint {
        
        public int foo(String bar) {
            return bar.length();
        }
    }
}