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

package org.apache.axis2.jaxws.sample.headershandler;

import java.io.ByteArrayOutputStream;
import java.io.StringBufferInputStream;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.LogicalMessage;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;

import org.apache.axis2.Constants;
import org.apache.axis2.jaxws.handler.LogicalMessageContext;

public class HeadersClientLogicalHandler implements
        javax.xml.ws.handler.LogicalHandler<LogicalMessageContext> {

	private HandlerTracker tracker = new HandlerTracker(HeadersClientLogicalHandler.class.getSimpleName());
	private TestHeaders headerUtil = new TestHeaders(this.getClass());
	
    public void close(MessageContext messagecontext) {
    	tracker.close();
    }

    public boolean handleFault(LogicalMessageContext messagecontext) {
    	Boolean outbound = (Boolean) messagecontext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
    	tracker.handleFault(outbound);
    	if (!outbound) {
    	    // this is the third client inbound hit
    	    
    	    // calling getPayload just to exercise the code:
    	    messagecontext.getMessage().getPayload();
    	}
        return true;
    }

    public boolean handleMessage(LogicalMessageContext messagecontext) {
        Boolean outbound = (Boolean) messagecontext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        tracker.handleMessage(outbound);
        if (outbound) { // outbound request on the client

        	// this is the first client outbound handler hit
        	
            // turn off special property that logs ability to use
            // both SOAPHeadersAdapter and SAAJ in a single handler method:
            messagecontext.put(org.apache.axis2.jaxws.handler.Constants.JAXWS_HANDLER_TRACKER, false);
            
        	// let's check for all the headers we expect, remove a few, and manipulate the message payload
        	
        	Map<QName, List<String>> requestHeaders = (Map<QName, List<String>>)messagecontext.get(Constants.JAXWS_OUTBOUND_SOAP_HEADERS);
        	headerUtil.confirmHeadersAdapterList(Constants.JAXWS_OUTBOUND_SOAP_HEADERS, requestHeaders, 2);

        	// expecting four header elements under two different QNames

        	List<String> list1 = requestHeaders.get(TestHeaders.ACOH1_HEADER_QNAME);
        	headerUtil.confirmList(TestHeaders.ACOH1_HEADER_QNAME, list1, 2);
        	String acoh1 = TestHeaders.createHeaderXMLString(TestHeaders.ACOH1_HEADER_QNAME, TestHeaders.CONTENT_SMALL1);
        	headerUtil.compareHeaderStrings(acoh1, list1.get(0));
        	tracker.checkHeader(list1.get(0));
        	// also:
        	String acoh2 = TestHeaders.createHeaderXMLString(TestHeaders.ACOH1_HEADER_QNAME, TestHeaders.CONTENT_SMALL2);
        	headerUtil.compareHeaderStrings(acoh2, list1.get(1));
        	tracker.checkHeader(list1.get(1));
        	
        	List<String> list2 = requestHeaders.get(TestHeaders.ACOH2_HEADER_QNAME);
        	headerUtil.confirmList(TestHeaders.ACOH2_HEADER_QNAME, list2, 2);
        	String acoh3 = TestHeaders.createHeaderXMLString(TestHeaders.ACOH2_HEADER_QNAME, TestHeaders.CONTENT_SMALL3);
        	headerUtil.compareHeaderStrings(acoh3, list2.get(0));
        	tracker.checkHeader(list2.get(0));
        	// also:
        	String acoh4 = TestHeaders.createHeaderXMLString(TestHeaders.ACOH2_HEADER_QNAME, TestHeaders.CONTENT_SMALL4);
        	headerUtil.compareHeaderStrings(acoh4, list2.get(1));
        	tracker.checkHeader(list2.get(1));

        	// remove header from list 1
        	list1.remove(0);
        	tracker.removedHeader(acoh1);
        	// list1 is not a "live" list, so we need to put it back in
        	requestHeaders.put(TestHeaders.ACOH1_HEADER_QNAME, list1);
        	// remove all of list 2
        	tracker.removedHeader(acoh3);
        	tracker.removedHeader(acoh4);
        	requestHeaders.remove(TestHeaders.ACOH2_HEADER_QNAME);
        	
        	// the requestHeaders object is a "live" list, so no need to do this:
        	//messagecontext.put(Constants.JAXWS_OUTBOUND_SOAP_HEADERS, requestHeaders);

        	// The 'get' on the requestHeaders map triggers a toString() on the underlying OM structure for the header element.
        	// We want to make sure the parser was not closed prematurely, so...we do a getPayload() below on the message.
        	// This causes a parse and build up of SAAJ, confirming the parser was not prematurely closed.

        	// manipulate the payload just to be thorough
        	LogicalMessage msg = messagecontext.getMessage();
        	String st = getStringFromSourcePayload(msg.getPayload());
        	String txt = String.valueOf(Integer.valueOf(getFirstArg(st)) - 1);
        	st = replaceFirstArg(st, txt);
        	msg.setPayload(new StreamSource(new StringBufferInputStream(st)));

        } else {
        	// TODO implement some inbound stuff
        	
        	// this is the third client inbound hit

            LogicalMessage msg = messagecontext.getMessage();
            String st = getStringFromSourcePayload(msg.getPayload());

            if (st.contains("33")) {
            
                Map<QName, List<String>> requestHeaders = (Map<QName, List<String>>)messagecontext.get(Constants.JAXWS_INBOUND_SOAP_HEADERS);
                headerUtil.confirmHeadersAdapterList(Constants.JAXWS_INBOUND_SOAP_HEADERS, requestHeaders, 1);

                // expecting two header elements under two QNames

                /*
                 * these headers need to be removed because this is a return path taken by a 
                 * server handler that returned 'false' from a handleMessage call.  This means the original
                 * outbound message was returned as-is, and JAXB will get confused if we leave
                 * header elements on the Envelope.
                 */

                List<String> list2 = requestHeaders.get(TestHeaders.ACOH2_HEADER_QNAME);
                headerUtil.confirmList(TestHeaders.ACOH2_HEADER_QNAME, list2, 2);
                String acoh3 = TestHeaders.createHeaderXMLString(TestHeaders.ACOH2_HEADER_QNAME, TestHeaders.CONTENT_SMALL3);
                headerUtil.compareHeaderStrings(acoh3, list2.get(0));
                tracker.checkHeader(list2.get(0));
                String acoh4 = TestHeaders.createHeaderXMLString(TestHeaders.ACOH2_HEADER_QNAME, TestHeaders.CONTENT_SMALL4);
                headerUtil.compareHeaderStrings(acoh4, list2.get(1));
                tracker.checkHeader(list2.get(1));

                // remove list1
                requestHeaders.remove(TestHeaders.ACOH2_HEADER_QNAME);
                tracker.removedHeader(acoh3);
                tracker.removedHeader(acoh4);
                
                // currently, the server runtime is returning the original client outbound message (see JAXWS 9.3.2.1), but
                // once we exit this handler, the message will be passed to JAXB, and it won't like it because it's not
                // the WSDL-specified response message.  In the interest of testing, however, let's do this, and make sure
                // the junit test handles it and checks the calls into the handler flow anyway:
                throw new WebServiceException("I don't like 33");

            }
        	
        }
        return true;
    }

    private static String getFirstArg(String payloadString) {
        StringTokenizer st = new StringTokenizer(payloadString, ">");
        st.nextToken(); // skip first token.
        st.nextToken(); // skip second
        String tempString = st.nextToken();
        String returnString = new StringTokenizer(tempString, "<").nextToken();
        return returnString;
    }

    private static String replaceFirstArg(String payloadString, String newArg) {
        String firstArg = getFirstArg(payloadString);
        payloadString = payloadString.replaceFirst(firstArg, newArg);
        return payloadString;
    }

    private static String getStringFromSourcePayload(Source payload) {
        try {

            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer trans = factory.newTransformer();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            StreamResult result = new StreamResult(baos);

            trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            trans.transform(payload, result);

            return new String(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
