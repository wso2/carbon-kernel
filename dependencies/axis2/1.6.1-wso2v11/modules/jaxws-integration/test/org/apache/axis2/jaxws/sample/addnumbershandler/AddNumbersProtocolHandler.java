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

package org.apache.axis2.jaxws.sample.addnumbershandler;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.TestLogger;

import javax.annotation.PreDestroy;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.Set;

public class AddNumbersProtocolHandler implements javax.xml.ws.handler.soap.SOAPHandler<SOAPMessageContext> {

    HandlerTracker tracker = new HandlerTracker(AddNumbersProtocolHandler.class.getSimpleName());
    
    public void close(MessageContext messagecontext) {
        tracker.close();
    }

    public Set<QName> getHeaders() {
        tracker.getHeaders();
        return null;
    }
    
    public boolean handleFault(SOAPMessageContext messagecontext) {
        tracker.handleFault((Boolean) messagecontext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY));
        
        try {
            SOAPFault fault = messagecontext.getMessage().getSOAPBody().getFault();
            String faultString = fault.getFaultString();
            Throwable webmethodException = (Throwable)
                messagecontext.get("jaxws.outbound.response.webmethod.exception");
            
            // Update the fault string with the stack trace
            if (webmethodException != null) {
                TestLogger.logger.debug("The webmethod exception is available...setting the fault string");
                faultString += "stack = " +  stackToString(webmethodException);
                fault.setFaultString(faultString);
            } else {
                TestLogger.logger.debug("The webmethod exception was not available");
            }
        } catch (Exception e) {
            tracker.log("Exception occurred:" + e.getMessage(),
                        (Boolean) messagecontext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY));
        }
        
        return true;
    }

    public boolean handleMessage(SOAPMessageContext messagecontext) {
        tracker.handleMessage((Boolean) messagecontext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY));
        // Ensure that the expected headers are present
        JAXBContext context;
        try {
            context = JAXBContext.newInstance("org.test.addnumbershandler");
        } catch (Exception e) {
            throw new WebServiceException(e);
        }
        QName qName= new QName("http://org/test/addnumbershandler","myHeader");

        try {
            Object[] values = messagecontext.getHeaders(qName, context, true);

            if (values.length > 0) {
                if (values.length !=3 ||
                        !((JAXBElement)values[0]).getValue().equals("Good1") ||
                        !((JAXBElement)values[1]).getValue().equals("Good2") ||
                        !((JAXBElement)values[2]).getValue().equals("Bad")) {
                    throw new WebServiceException("Failed getting all of the headers:" + values);
                }

                values = messagecontext.getHeaders(qName, context, false);
                if (values.length !=2 ||
                        !((JAXBElement)values[0]).getValue().equals("Good1") ||
                        !((JAXBElement)values[1]).getValue().equals("Good2")) {
                    throw new WebServiceException("Failed getting the expected headers:" + values);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
            throw new WebServiceException(e);
        }
        
        return true;
    }
    
    @PreDestroy
    public void preDestroy() {
        tracker.preDestroy();
    	try {
    		/*
    		 * since @PreDestroy methods are called just before the managed (server) side
    		 * handler instance goes out of scope, there's not a good way to test if it is
    		 * called.  So, we are creating a file that one of the AddNumbersHandlerTests tests
    		 * checks the existance of.
    		 */
    		File file = new File("AddNumbersProtocolHandler.preDestroy.txt");
    		file.createNewFile();
    		FileOutputStream fos = new FileOutputStream(file);
    		fos.write(new byte[]{'h','i'});
    		fos.close();
    		file.deleteOnExit();
    	} catch (Exception e) {
    		throw ExceptionFactory.makeWebServiceException(e);
    	}
    }

    private static String stackToString(Throwable e) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.BufferedWriter bw = new java.io.BufferedWriter(sw);
        java.io.PrintWriter pw = new java.io.PrintWriter(bw);
        e.printStackTrace(pw);
        pw.close();
        return sw.getBuffer().toString();
    }
}
