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

package org.apache.axis2.jaxws.dispatch;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.jaxws.framework.AbstractTestCase;
import org.test.dispatch.jaxbsource.Invoke;
import org.test.dispatch.jaxbsource.ObjectFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.util.JAXBSource;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import java.io.StringWriter;

/*
* This is a test case for Invoking Dispatch with a JAXBSource.
* test uses JAXB Objects from org.test.dispatch.jaxbsource package, create a request of JAXBSource type
* and invokes the service endpoint and reads the response of type Source. Assert failure if response not received.
*/


public class JAXBSourceDispatchTests extends AbstractTestCase {
	/**
     * Invoke a sync Dispatch<JAXBSource> in PAYLOAD mode
     */
	
	private String url = "http://localhost:6060/axis2/services/SourceProviderService";
	private QName serviceName = new QName("http://ws.apache.org/axis2", "SourceProviderService");
	private QName portName =new QName("http://ws.apache.org/axis2", "SimpleProviderServiceSOAP11port0");
    
	public static Test suite() {
        return getTestSetup(new TestSuite(JAXBSourceDispatchTests.class));
    }

    public void testJAXBSourceSyncPayloadMode() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        try{
            // Initialize the JAX-WS client artifacts
            Service svc = Service.create(serviceName);
            svc.addPort(portName, null, url);
            Dispatch<Source> dispatch = svc.createDispatch(portName, Source.class, Service.Mode.PAYLOAD);

            //Create JAXBContext and JAXBSource here.
            ObjectFactory factory = new ObjectFactory();
            Invoke invokeObj = factory.createInvoke();
            invokeObj.setInvokeStr("Some Request");
            JAXBContext ctx = JAXBContext.newInstance("org.test.dispatch.jaxbsource");

            JAXBSource jbSrc = new JAXBSource(ctx.createMarshaller(), invokeObj);
            // Invoke the Dispatch
            TestLogger.logger.debug(">> Invoking sync Dispatch");
            //Invoke Server endpoint and read response
            Source response = dispatch.invoke(jbSrc);

            assertNotNull("dispatch invoke returned null", response);
            //Print the response as string.
            StringWriter writer = new StringWriter();
            Transformer t = TransformerFactory.newInstance().newTransformer();
            Result result = new StreamResult(writer);
            t.transform(response, result);

            TestLogger.logger.debug("Response On Client: \n" + writer.getBuffer().toString());
            
            // Invoke a second time
            jbSrc = new JAXBSource(ctx.createMarshaller(), invokeObj);
            // Invoke the Dispatch
            TestLogger.logger.debug(">> Invoking sync Dispatch");
            //Invoke Server endpoint and read response
            response = dispatch.invoke(jbSrc);

            assertNotNull("dispatch invoke returned null", response);
            //Print the response as string.
            writer = new StringWriter();
            t = TransformerFactory.newInstance().newTransformer();
            result = new StreamResult(writer);
            t.transform(response, result);

            TestLogger.logger.debug("Response On Client: \n" + writer.getBuffer().toString());
            TestLogger.logger.debug("---------------------------------------");
        }catch(Exception e){
            e.printStackTrace();
        }
        
    }
    
}
