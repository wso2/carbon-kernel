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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.axis2.Constants;

public class HeadersClientProtocolHandler implements
        javax.xml.ws.handler.soap.SOAPHandler<SOAPMessageContext> {

	private HandlerTracker tracker = new HandlerTracker(HeadersClientProtocolHandler.class.getSimpleName());
	private TestHeaders headerUtil = new TestHeaders(this.getClass());
	
    public void close(MessageContext messagecontext) {
    	tracker.close();
    }

    public boolean handleFault(SOAPMessageContext messagecontext) {
    	Boolean outbound = (Boolean) messagecontext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
    	tracker.handleFault(outbound);
    	if (!outbound) {
    	    // this is the second client inbound handler hit
        
    	    // expecting 0 headers
    	    
    	    Map<QName, List<String>> requestHeaders = (Map<QName, List<String>>)messagecontext.get(Constants.JAXWS_INBOUND_SOAP_HEADERS);
    	    headerUtil.confirmHeadersAdapterList(Constants.JAXWS_INBOUND_SOAP_HEADERS, requestHeaders, 0);
    	}
        return true;
    }

    public Set getHeaders() {
    	tracker.getHeaders();
        return null;
    }

    public boolean handleMessage(SOAPMessageContext messagecontext) {
        Boolean outbound = (Boolean) messagecontext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        tracker.handleMessage(outbound);
        headerUtil.confirmMessageAsString(messagecontext);
        if (outbound) {

        	// this is the second client outbound handler hit
        	
        	Map<QName, List<String>> requestHeaders = (Map<QName, List<String>>)messagecontext.get(Constants.JAXWS_OUTBOUND_SOAP_HEADERS);
        	headerUtil.confirmHeadersAdapterList(Constants.JAXWS_OUTBOUND_SOAP_HEADERS, requestHeaders, 1);

        	// expecting one header element
        	
        	List<String> list1 = requestHeaders.get(TestHeaders.ACOH1_HEADER_QNAME);
        	headerUtil.confirmList(TestHeaders.ACOH1_HEADER_QNAME, list1, 1);
        	String acoh1 = TestHeaders.createHeaderXMLString(TestHeaders.ACOH1_HEADER_QNAME, TestHeaders.CONTENT_SMALL2);
        	headerUtil.compareHeaderStrings(acoh1, list1.get(0));
        	tracker.checkHeader(list1.get(0));

        	/*
        	 * TODO:  would rather do the below type of testing so a header is added by some means other than
        	 * SOAPHeadersAdapter, but Axis2 SAAJ implementation appears be be lacking some basic support here.
        	 * Under Axis2 SAAJ, a header is inserted that has no text node underneath:  <ns:blarg someheader />
        	 */
        	//  SOAPElement acoh2 = TestHeaders.createHeaderSOAPElement(TestHeaders.ACOH3_HEADER_QNAME, TestHeaders.CONTENT_LARGE);
        	//  messagecontext.getMessage().getSOAPHeader().addChildElement(acoh2);

        	/*
        	 * TODO: instead, we'll do this:
        	 */
        	String acoh2 = TestHeaders.createHeaderXMLString(TestHeaders.ACOH3_HEADER_QNAME, TestHeaders.CONTENT_LARGE);
        	List<String> acoh2list = new ArrayList<String>();
        	acoh2list.add(acoh2);
        	requestHeaders.put(TestHeaders.ACOH3_HEADER_QNAME, acoh2list);
        	messagecontext.put(Constants.JAXWS_OUTBOUND_SOAP_HEADERS, requestHeaders);
        	tracker.addHeader(acoh2.toString());

        }
        else {  // client inbound response
        	
        	// this is the second client inbound handler hit

        	Map<QName, List<String>> requestHeaders = (Map<QName, List<String>>)messagecontext.get(Constants.JAXWS_INBOUND_SOAP_HEADERS);
        	headerUtil.confirmHeadersAdapterList(Constants.JAXWS_INBOUND_SOAP_HEADERS, requestHeaders, 2);

        	// expecting two header elements under two QNames
        	
        	List<String> list1 = requestHeaders.get(TestHeaders.ACOH3_HEADER_QNAME);
        	headerUtil.confirmList(TestHeaders.ACOH3_HEADER_QNAME, list1, 1);
        	String acoh5 = TestHeaders.createHeaderXMLString(TestHeaders.ACOH3_HEADER_QNAME, TestHeaders.CONTENT_LARGE);
        	headerUtil.compareHeaderStrings(acoh5, list1.get(0));
        	tracker.checkHeader(list1.get(0));
        	
        	List<String> list2 = requestHeaders.get(TestHeaders.ACOH2_HEADER_QNAME);
        	headerUtil.confirmList(TestHeaders.ACOH2_HEADER_QNAME, list2, 1);
        	String acoh3 = TestHeaders.createHeaderXMLString(TestHeaders.ACOH2_HEADER_QNAME, TestHeaders.CONTENT_SMALL3);
        	headerUtil.compareHeaderStrings(acoh3, list2.get(0));
        	tracker.checkHeader(list2.get(0));
        	
        	// remove list1
        	requestHeaders.remove(TestHeaders.ACOH3_HEADER_QNAME);
        	tracker.removedHeader(acoh5);
        	
        	// add header to list2
        	String acoh4 = TestHeaders.createHeaderXMLString(TestHeaders.ACOH2_HEADER_QNAME, TestHeaders.CONTENT_SMALL4);
        	list2.add(acoh4);
        	tracker.addHeader(acoh4);
        	
        	// list2 is not a "live" list, so...
        	requestHeaders.put(TestHeaders.ACOH2_HEADER_QNAME, list2);

        }
        return true;
    }

}
