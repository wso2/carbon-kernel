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

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;

import org.apache.axis2.jaxws.Constants;
import org.apache.axis2.jaxws.api.MessageAccessor;

public class TestHeaders {
	
	/*
	 * These are just some standard small SOAP headers to be used by the the HeadersHandler sample app and handlers
	 * 
	 * If more strenuous tests are required, such as a very large header, or some non-XML-safe strings, they can be
	 * added here.  The purpose of having these constants is so multiple handlers can use the constant to add/remove/manipulate
	 * headers without hard-coding the data.
	 * 
	 */
	
	private static String LARGE_STRING = "";
	static {
		// 100k string
		for (int i = 0; i < 10000; i++) {
			LARGE_STRING += "LongString";  // 10 chars * Integer.MAX_VALUE is pretty big.
		}
	}
	
	
	// app client outbound header 1 (ACOH1)
	private static final String identifierACOH1 = "acoh1";
	private static final String identifierACOH1namespaceURI = "http://"+identifierACOH1+"ns";
	private static final String identifierACOH1prefix = identifierACOH1+"pre";
    public static final QName ACOH1_HEADER_QNAME = new QName(identifierACOH1namespaceURI, identifierACOH1, identifierACOH1prefix);
    
	// app client outbound header 2 (ACOH2)
	private static final String identifierACOH2 = "acoh2";
	private static final String identifierACOH2namespaceURI = "http://"+identifierACOH2+"ns";
	private static final String identifierACOH2prefix = identifierACOH2+"pre";
    public static final QName ACOH2_HEADER_QNAME = new QName(identifierACOH2namespaceURI, identifierACOH2, identifierACOH2prefix);
    
	// app client outbound header 3 (ACOH3)
	private static final String identifierACOH3 = "acoh3";
	private static final String identifierACOH3namespaceURI = "http://"+identifierACOH3+"ns";
	private static final String identifierACOH3prefix = identifierACOH3+"pre";
    public static final QName ACOH3_HEADER_QNAME = new QName(identifierACOH3namespaceURI, identifierACOH3, identifierACOH3prefix);
    
	// app client outbound header 4 (ACOH4)
	private static final String identifierACOH4 = "acoh4";
	private static final String identifierACOH4namespaceURI = "http://"+identifierACOH4+"ns";
	private static final String identifierACOH4prefix = identifierACOH4+"pre";
    public static final QName ACOH4_HEADER_QNAME = new QName(identifierACOH4namespaceURI, identifierACOH4, identifierACOH4prefix);
	
	// client outbound soap handler 1 (COSH1)
	private static final String identifierCOSH1 = "cosh1";
	private static final String identifierCOSH1namespaceURI = "http://"+identifierCOSH1+"ns";
	private static final String identifierCOSH1prefix = identifierCOSH1+"pre";
    public static final QName COSH1_HEADER_QNAME = new QName(identifierCOSH1namespaceURI, identifierCOSH1, identifierCOSH1prefix);

    // some content
    public static final String CONTENT_SMALL1 = "small content 1";
    public static final String CONTENT_SMALL2 = "small content 2";
    public static final String CONTENT_SMALL3 = "small content 3";
    public static final String CONTENT_SMALL4 = "small content 4";
    public static final String CONTENT_SMALL5 = "small content 5";
    public static final String CONTENT_SMALL6 = "small content 6";
    public static final String CONTENT_LARGE = LARGE_STRING;
    public static final String CONTENT_OTHER = "other content";

    private String className = "";
    
    /**
     * 
     * @param clazz the currently executing handler class
     */
    public TestHeaders(Class clazz) {
    	className = clazz.getSimpleName();
    }
    
    /**
     * Utility method so handlers can easily confirm the map of headers under the message context is as expected.
     * 
     * @param className - for logging purposes, pass the name of the handler class currently executing
     * @param key - for logging purposes, pass the key of the list - Constants.JAXWS_INBOUND_SOAP_HEADERS or Constants.JAXWS_OUTBOUND_SOAP_HEADERS
     * @param list - the list itself
     * @param expectedSize - the expected size of the map being passed
     * @throws Exception
     */
    public void confirmHeadersAdapterList(String key, Map<QName, List<String>> map, int expectedSize) throws WebServiceException {
		if (map == null) {
			throw new WebServiceException(className + ": Expected to find JAXWS SOAP Headers by way of SOAPMessageContext.get(" +
					key+"), but none was found.  This probably means something is" +
					" wrong with the way SOAPHeadersAdapter is \"installed\" or used.");
		} else if (map.size() != expectedSize) {
			throw new WebServiceException(className + ": List of requestHeaders does not match expected size of "+expectedSize+".  " +
			"Actual size was " + map.size() + ".  This probably means something is wrong with the way SOAPHeadersAdapter " +
			"is \"installed\" or used.");
		}
    }
    
    /**
     * Utility method so handlers can easily confirm the list of headers under a given QName is as expected.
     * 
     * @param className - for logging purposes, pass the name of the handler class currently executing
     * @param qnameKeyForList - for logging purposes, pass the QName key of the list
     * @param list - the list itself
     * @param expectedSize - the expected size of the list being passed
     * @throws Exception
     */
    public void confirmList(QName qnameKeyForList, List<String> list, int expectedSize) throws WebServiceException {
		if (list == null) {
			throw new WebServiceException(className + ": List of headers under QName " + qnameKeyForList + " is missing.");
		} else if (list.size() != expectedSize) {
			throw new WebServiceException(className + ": List of headers under QName " + qnameKeyForList + " expected size " +
					"was " + expectedSize + ", but actual size was " + list.size() + ".");
		}
    }
    
    public void compareHeaderStrings(String xmlHeader1, String xmlHeader2) throws WebServiceException {
    	if ((xmlHeader1 == null) || (xmlHeader2 == null) || (!xmlHeader1.equals(xmlHeader2))) {
			throw new WebServiceException(className + ": Expected outbound header element was not found.");
		}
    }
    
    // callers should use the QName objects and CONTENT_* Strings defined in this class as params
    public static String createHeaderXMLString(QName qname, String textContent) throws WebServiceException {
    	try {
    		SOAPFactory sf = SOAPFactory.newInstance();
    		SOAPElement e = sf.createElement(qname);
    		e.addTextNode(textContent);
    		return e.toString();
    	} catch (SOAPException e) {
    		throw new WebServiceException(e);
    	}
    }
    
    // callers should use the QName objects and CONTENT_* Strings defined in this class as params
    public static SOAPElement createHeaderSOAPElement(QName qname, String textContent) throws WebServiceException {
    	try {
    		SOAPFactory sf = SOAPFactory.newInstance();
    		SOAPElement e = sf.createElement(qname);
    		e.addTextNode(textContent);
    		return e;
    	} catch (SOAPException e) {
    		throw new WebServiceException(e);
    	}
    }
    
    /**
     * Throw an exception if the jaxws.message.as.string property fails.
     * @param mc
     */
    public void confirmMessageAsString(MessageContext mc) {
        String text = null;
        if (mc != null) {
            Object accessor =  mc.get(Constants.JAXWS_MESSAGE_ACCESSOR);
            if (accessor != null) {
                Boolean preMessageAccessed = (Boolean) mc.get("jaxws.isMessageAccessed");
                text = accessor.toString();
                Boolean postMessageAccessed = (Boolean) mc.get("jaxws.isMessageAccessed");
                if (preMessageAccessed != postMessageAccessed) {
                    throw new WebServiceException("The message was accessed when toString was called.");
                }
                if (!text.contains("Envelope") || !text.contains("Body")) {
                    throw new WebServiceException("The message appears to be invalid: " + text);
                }
                return;
            }
        }
        throw new WebServiceException("Could not access the MessageAccessor: " + mc);   
    }
}
