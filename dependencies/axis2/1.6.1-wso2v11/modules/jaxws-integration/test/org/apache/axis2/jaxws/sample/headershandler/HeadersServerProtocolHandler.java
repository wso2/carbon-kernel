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
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.axis2.Constants;
import org.w3c.dom.Node;

public class HeadersServerProtocolHandler implements
        javax.xml.ws.handler.soap.SOAPHandler<SOAPMessageContext> {

	private HandlerTracker tracker = new HandlerTracker(HeadersServerProtocolHandler.class.getSimpleName());
	private TestHeaders headerUtil = new TestHeaders(this.getClass());
	
    public void close(MessageContext messagecontext) {
    	tracker.close();
    }

    public boolean handleFault(SOAPMessageContext messagecontext) {
    	Boolean outbound = (Boolean) messagecontext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
    	tracker.handleFault(outbound);
    	headerUtil.confirmMessageAsString(messagecontext);
    	if (outbound) {
    	    
    	    Map<QName, List<String>> requestHeaders = (Map<QName, List<String>>)messagecontext.get(Constants.JAXWS_OUTBOUND_SOAP_HEADERS);
    	    headerUtil.confirmHeadersAdapterList(Constants.JAXWS_OUTBOUND_SOAP_HEADERS, requestHeaders, 0);

    	    // add header
    	    String acoh3 = TestHeaders.createHeaderXMLString(TestHeaders.ACOH3_HEADER_QNAME, TestHeaders.CONTENT_LARGE);
    	    List<String> acoh3list = new ArrayList<String>();
    	    acoh3list.add(acoh3);
    	    requestHeaders.put(TestHeaders.ACOH3_HEADER_QNAME, acoh3list);
    	    messagecontext.put(Constants.JAXWS_OUTBOUND_SOAP_HEADERS, requestHeaders);
    	    tracker.addHeader(acoh3.toString());
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
        if (outbound) {  // server outbound response
        	
        	// this is the second server outbound handler hit
            Map<QName, List<String>> requestHeaders = (Map<QName, List<String>>)messagecontext.get(Constants.JAXWS_OUTBOUND_SOAP_HEADERS);
            
            // if the message object contains "33", it means we reversed directions in the "next inbound" server handler
            // For testing purposes, we add a header here that would have been added by the previous handler in the flow.
            try {
                if (messagecontext.getMessage().getSOAPBody().getChildElements().next().toString().contains("33")) {
                    String acoh1 = TestHeaders.createHeaderXMLString(TestHeaders.ACOH1_HEADER_QNAME, TestHeaders.CONTENT_SMALL1);
                    List<String> acoh1list = new ArrayList<String>();
                    acoh1list.add(acoh1);
                    requestHeaders.put(TestHeaders.ACOH1_HEADER_QNAME, acoh1list);
                    messagecontext.put(Constants.JAXWS_OUTBOUND_SOAP_HEADERS, requestHeaders);
                    tracker.addHeader(acoh1.toString());
                }
            } catch (SOAPException e) {
                throw new WebServiceException(e);
            }

        	
        	// expecting one header element
        	
        	headerUtil.confirmHeadersAdapterList(Constants.JAXWS_OUTBOUND_SOAP_HEADERS, requestHeaders, 1);
        	
        	List<String> list1 = requestHeaders.get(TestHeaders.ACOH1_HEADER_QNAME);
        	headerUtil.confirmList(TestHeaders.ACOH1_HEADER_QNAME, list1, 1);
        	String acoh1 = TestHeaders.createHeaderXMLString(TestHeaders.ACOH1_HEADER_QNAME, TestHeaders.CONTENT_SMALL1);
        	headerUtil.compareHeaderStrings(acoh1, list1.get(0));
        	tracker.checkHeader(list1.get(0));
        	
        	// remove header
        	tracker.removedHeader(acoh1);
        	requestHeaders.remove(TestHeaders.ACOH1_HEADER_QNAME);
        	
        	// add header
        	String acoh5 = TestHeaders.createHeaderXMLString(TestHeaders.ACOH3_HEADER_QNAME, TestHeaders.CONTENT_LARGE);
        	List<String> acoh4list = new ArrayList<String>();
        	acoh4list.add(acoh5);
        	requestHeaders.put(TestHeaders.ACOH3_HEADER_QNAME, acoh4list);
        	messagecontext.put(Constants.JAXWS_OUTBOUND_SOAP_HEADERS, requestHeaders);
        	tracker.addHeader(acoh5.toString());
        	
        }
        else {  // server inbound request

        	// this is the first server inbound handler hit
        	
        	Map<QName, List<String>> requestHeaders = (Map<QName, List<String>>)messagecontext.get(Constants.JAXWS_INBOUND_SOAP_HEADERS);
        	headerUtil.confirmHeadersAdapterList(Constants.JAXWS_INBOUND_SOAP_HEADERS, requestHeaders, 2);

        	// expecting two header elements under two different QNames
        	
        	List<String> list1 = requestHeaders.get(TestHeaders.ACOH1_HEADER_QNAME);
        	headerUtil.confirmList(TestHeaders.ACOH1_HEADER_QNAME, list1, 1);
        	String acoh1 = TestHeaders.createHeaderXMLString(TestHeaders.ACOH1_HEADER_QNAME, TestHeaders.CONTENT_SMALL2);
        	headerUtil.compareHeaderStrings(acoh1, list1.get(0));
        	tracker.checkHeader(list1.get(0));

        	List<String> list2 = requestHeaders.get(TestHeaders.ACOH3_HEADER_QNAME);
        	headerUtil.confirmList(TestHeaders.ACOH3_HEADER_QNAME, list2, 1);
        	String acoh2 = TestHeaders.createHeaderXMLString(TestHeaders.ACOH3_HEADER_QNAME, TestHeaders.CONTENT_LARGE);
        	headerUtil.compareHeaderStrings(acoh2, list2.get(0));
        	tracker.checkHeader(list2.get(0));

        	// TODO: Would very much like to remove a header using regular SOAP api, but when I attempt this, I get
        	// an exception later in the runtime (not here):
        	// "Can't find bundle for base name org.apache.axiom.om.impl.dom.msg.DOMMessages, locale en_US"
        	
//        	try {
//        		SOAPHeader soapHeader = messagecontext.getMessage().getSOAPHeader();
//        		Node firstChild = messagecontext.getMessage().getSOAPHeader().getFirstChild();
//        		soapHeader.removeChild(firstChild);
//        	} catch (SOAPException e) {
//        		throw new WebServiceException(this.getClass().getSimpleName() + ": " + e.getMessage());
//        	}
        	
        	// instead, we'll do it the new way and forego the above attempt
        	tracker.removedHeader(acoh1);
        	requestHeaders.remove(TestHeaders.ACOH1_HEADER_QNAME);
        	
        	// add a header
        	String acoh4 = TestHeaders.createHeaderXMLString(TestHeaders.ACOH4_HEADER_QNAME, TestHeaders.CONTENT_SMALL4);
        	List<String> acoh4list = new ArrayList<String>();
        	acoh4list.add(acoh4);
        	requestHeaders.put(TestHeaders.ACOH4_HEADER_QNAME, acoh4list);
        	messagecontext.put(Constants.JAXWS_INBOUND_SOAP_HEADERS, requestHeaders);
        	tracker.addHeader(acoh4.toString());
        	
        }
        return true;
    }

}
