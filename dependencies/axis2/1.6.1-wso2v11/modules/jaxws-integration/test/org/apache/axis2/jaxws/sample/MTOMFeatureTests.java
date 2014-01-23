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

package org.apache.axis2.jaxws.sample;

import java.io.BufferedInputStream;
import java.io.File;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.soap.MTOMFeature;
import javax.xml.ws.soap.SOAPBinding;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.jaxws.framework.AbstractTestCase;
import org.apache.axis2.jaxws.sample.mtomfeature.ObjectFactory;
import org.apache.axis2.jaxws.sample.mtomfeature.ProcessDocumentDelegate;
import org.apache.axis2.jaxws.sample.mtomfeature.ProcessDocumentService;
import org.apache.axis2.jaxws.sample.mtomfeature.SendPDFFile;
import org.apache.axis2.jaxws.sample.mtomfeature.SendPDFFileResponse;
import org.apache.axis2.jaxws.spi.Binding;


public class MTOMFeatureTests extends AbstractTestCase {
    private static final String axisEndpoint = "http://localhost:6060/axis2/services/ProcessDocumentService.ProcessDocumentPortBindingImplPort";
    private static final QName serviceName = new QName("http://mtomfeature.sample.jaxws.axis2.apache.org/", "ProcessDocumentService");
    private static final QName portName = new QName("http://mtomfeature.sample.jaxws.axis2.apache.org/", "ProcessDocumentPort");
    String resourceDir = System.getProperty("basedir",".")+ File.separator+"test-resources"+File.separator+"pdf";
    public static Test suite() {
        return getTestSetup(new TestSuite(MTOMFeatureTests.class));
    }
    public void testMTOMFeatureProxy(){
        try{
            
            TestLogger.logger.debug("----------------------------------");
            TestLogger.logger.debug("test: " + getName());
            MTOMFeature mtomFeature = new MTOMFeature(true, 1);
            ProcessDocumentService service = new ProcessDocumentService();
            ProcessDocumentDelegate proxy = service.getProcessDocumentPort(mtomFeature);
            
            BindingProvider bp = (BindingProvider)proxy;
            bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, axisEndpoint);
            Binding b =(Binding) bp.getBinding();

            WebServiceFeature wsFeature = b.getFeature(MTOMFeature.ID);
            assertNotNull(wsFeature);
            assertTrue("Expecting WSFeature to be enabled, but found disabled.", wsFeature.isEnabled());
            assertTrue("Expecting WSFeature to be instance of MTOMFeature, but found WSFeature is not a MTOMFeature",wsFeature instanceof MTOMFeature);
            assertTrue("Expecting Threshold value to be 1, but found"+ ((MTOMFeature)wsFeature).getThreshold(),((MTOMFeature)wsFeature).getThreshold()==1);
           //so webservices feature is correctly set at this point.
            //lets make sure attachments payloads are flowing correctly.
          
            //Fetch attachment file
            File pdfFile = new File(resourceDir+File.separator+"JAX-WS.pdf");
            FileDataSource fds = new FileDataSource(pdfFile);
            DataHandler pdfHandler = new DataHandler(fds);
            
            //Invoke Operation
            DataHandler response = proxy.sendPDFFile(pdfHandler);
            // The cids are collected in the monitor.  We will check
            // this to make sure the response mtom is not inlined
            assertNotNull(response);
            //Lets ensure the file content came back as expected.
            //Validate that the file data is available
            BufferedInputStream fileIn = new BufferedInputStream(response.getInputStream());
            assertNotNull(fileIn);
            assertTrue(fileIn.available()>0);
            
        }catch(Exception e){
            fail(e.getMessage());
        }
    }
    
    public void testMTOMFeatureDispatch(){
        try{      
            TestLogger.logger.debug("----------------------------------");
            TestLogger.logger.debug("test: " + getName());
            MTOMFeature mtomFeature = new MTOMFeature(true, 1);
            //Create the necessary JAXBContext
            JAXBContext jbc = JAXBContext.newInstance("org.apache.axis2.jaxws.sample.mtomfeature");
            // Create the JAX-WS client needed to send the request
            Service service = Service.create(serviceName);
            service.addPort(portName, SOAPBinding.SOAP11HTTP_BINDING, axisEndpoint);
            Dispatch<Object> dispatch = service.createDispatch(portName, jbc, Mode.PAYLOAD, mtomFeature);
            ObjectFactory of = new ObjectFactory();
            SendPDFFile pdf = of.createSendPDFFile();
            //Fetch attachment file
            File pdfFile = new File(resourceDir+File.separator+"JAX-WS.pdf");
            FileDataSource fds = new FileDataSource(pdfFile);
            DataHandler pdfHandler = new DataHandler(fds);
            pdf.setArg0(pdfHandler);
            BindingProvider bp = (BindingProvider)dispatch;
            Binding b =(Binding) bp.getBinding();
            
            WebServiceFeature wsFeature = b.getFeature(MTOMFeature.ID);
            assertNotNull(wsFeature);
            assertTrue("Expecting WSFeature to be enabled, but found disabled.", wsFeature.isEnabled());
            assertTrue("Expecting WSFeature to be instance of MTOMFeature, but found WSFeature is not a MTOMFeature",wsFeature instanceof MTOMFeature);
            assertTrue("Expecting Threshold value to be 1, but found"+ ((MTOMFeature)wsFeature).getThreshold(),((MTOMFeature)wsFeature).getThreshold()==1);
            
            JAXBElement<SendPDFFileResponse> response = (JAXBElement<SendPDFFileResponse>)dispatch.invoke(of.createSendPDFFile(pdf));
            assertNotNull(response);
            SendPDFFileResponse responsePdf = response.getValue();
            assertNotNull(responsePdf);
            DataHandler dh = responsePdf.getReturn();
            assertNotNull(dh);
            //Lets ensure the file content came back as expected.
            //Validate that the file data is available
            BufferedInputStream fileIn = new BufferedInputStream(dh.getInputStream());
            assertNotNull(fileIn);
            assertTrue(fileIn.available()>0);
        }catch(Exception e){
            fail(e.getMessage());
        }
    }
}
