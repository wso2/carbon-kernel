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

package org.apache.axis2.jaxws.handler.soapheadersadapter;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.WebServiceException;

import junit.framework.TestCase;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.llom.OMSourcedElementImpl;
import org.apache.axiom.soap.impl.llom.soap11.SOAP11HeaderImpl;
import org.apache.axis2.Constants;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.handler.LogicalMessageImpl;
import org.apache.axis2.jaxws.handler.SOAPHeadersAdapter;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.message.factory.SourceBlockFactory;
import org.apache.axis2.jaxws.message.factory.XMLStringBlockFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;

public class SOAPHeadersAdapterTests extends TestCase {

	private static final String sampleText = "<pre:a xmlns:pre=\"urn://sample\">"
			+ "<b>Hello</b>" + "<c>World</c>" + "</pre:a>";
	
	private static String LARGE_STRING;
	static {
		// 100k string
		for (int i = 0; i < 10000; i++) {
			LARGE_STRING += "LongString";  // 10 chars * Integer.MAX_VALUE is pretty big.
		}
	}
	
	// header 1 (ACOH1)
	private static final String identifierACOH1 = "acoh1";
	private static final String identifierACOH1namespaceURI = "http://"+identifierACOH1+"ns";
	private static final String identifierACOH1prefix = identifierACOH1+"pre";
    public static final QName ACOH1_HEADER_QNAME = new QName(identifierACOH1namespaceURI, identifierACOH1, identifierACOH1prefix);
    
	// header 2 (ACOH2)
	private static final String identifierACOH2 = "acoh2";
	private static final String identifierACOH2namespaceURI = "http://"+identifierACOH2+"ns";
	private static final String identifierACOH2prefix = identifierACOH2+"pre";
    public static final QName ACOH2_HEADER_QNAME = new QName(identifierACOH2namespaceURI, identifierACOH2, identifierACOH2prefix);

    public static final String CONTENT_STRING1 = "content string 1";
    public static final String CONTENT_STRING2 = "content string 2";
    
    
	public void testAddRemove() throws Exception {

		MessageContext messageContext = getMessageContext();
		
		SOAPHeadersAdapter.install(messageContext);
		
    	SOAPFactory sf = SOAPFactory.newInstance();
    	
    	// QName used here should match the key for the list set on the requestCtx
    	SOAPElement e1 = sf.createElement(ACOH1_HEADER_QNAME);
    	e1.addTextNode(CONTENT_STRING1);
    	String acoh1 = e1.toString();
    	
    	// QName used here should match the key for the list set on the requestCtx
    	SOAPElement e2 = sf.createElement(ACOH1_HEADER_QNAME);
    	e2.addTextNode(CONTENT_STRING2);
    	String acoh2 = e2.toString();

        List<String> acoh1ContentList = new ArrayList<String>();
        acoh1ContentList.add(acoh1);
        acoh1ContentList.add(acoh2);
        
        SOAPHeadersAdapter adapter = (SOAPHeadersAdapter)messageContext.getProperty(Constants.JAXWS_OUTBOUND_SOAP_HEADERS);
        
        adapter.put(ACOH1_HEADER_QNAME, acoh1ContentList);
        adapter.put(ACOH2_HEADER_QNAME, acoh1ContentList);
        adapter.remove(ACOH1_HEADER_QNAME);
        
        assertTrue("Adapter should have one item, but has " + adapter.size(), adapter.size() == 1);

	}
	

	public void testAddRemoveEmpty() throws Exception {

		MessageContext messageContext = getMessageContext();
		
		SOAPHeadersAdapter.install(messageContext);
		
    	SOAPFactory sf = SOAPFactory.newInstance();
    	
    	// QName used here should match the key for the list set on the requestCtx
    	SOAPElement e1 = sf.createElement(ACOH1_HEADER_QNAME);
    	e1.addTextNode(CONTENT_STRING1);
    	String acoh1 = e1.toString();
    	
    	// QName used here should match the key for the list set on the requestCtx
    	SOAPElement e2 = sf.createElement(ACOH2_HEADER_QNAME);
    	e2.addTextNode(CONTENT_STRING2);
    	String acoh2 = e2.toString();

        List<String> acoh1ContentList = new ArrayList<String>();
        acoh1ContentList.add(acoh1);
        
        List<String> acoh2ContentList = new ArrayList<String>();
        acoh2ContentList.add(acoh2);
        
        SOAPHeadersAdapter adapter = (SOAPHeadersAdapter)messageContext.getProperty(Constants.JAXWS_OUTBOUND_SOAP_HEADERS);
        
        adapter.put(ACOH1_HEADER_QNAME, acoh1ContentList);
        adapter.put(ACOH2_HEADER_QNAME, acoh2ContentList);
        adapter.remove(ACOH1_HEADER_QNAME);
        adapter.remove(ACOH2_HEADER_QNAME);
        
        // testing isEmpty() method
        assertTrue("Adapter should have no items, but has " + adapter.size(), adapter.isEmpty());
        
        // double-check
        assertTrue("isEmpty() reported 'true' but we found an item", adapter.get(ACOH1_HEADER_QNAME) == null);
        assertTrue("isEmpty() reported 'true' but we found an item", adapter.get(ACOH2_HEADER_QNAME) == null);

	}
	
	public void testEmptyList() throws Exception {

		MessageContext messageContext = getMessageContext();
		
		SOAPHeadersAdapter.install(messageContext);
		
    	SOAPFactory sf = SOAPFactory.newInstance();
    	
    	// QName used here should match the key for the list set on the requestCtx
    	SOAPElement e1 = sf.createElement(ACOH1_HEADER_QNAME);
    	e1.addTextNode(CONTENT_STRING1);
    	String acoh1 = e1.toString();

        List<String> acoh1ContentList = new ArrayList<String>();
        acoh1ContentList.add(acoh1);
        
        // leaving this list empty
        List<String> acoh2ContentList = new ArrayList<String>();
        
        SOAPHeadersAdapter adapter = (SOAPHeadersAdapter)messageContext.getProperty(Constants.JAXWS_OUTBOUND_SOAP_HEADERS);
        
        adapter.put(ACOH1_HEADER_QNAME, acoh1ContentList);
        adapter.put(ACOH2_HEADER_QNAME, acoh2ContentList);
        
        // TODO is this reasonable for a map (the adapter) to ignore an empty list?  I think so.
        assertTrue("Adapter should have one item, but has " + adapter.size(), adapter.size() == 1);
        
        // double-check
        assertTrue(adapter.get(ACOH1_HEADER_QNAME).get(0).equals(acoh1));
        // TODO is this reasonable for a map (the adapter) to ignore an empty list?  I think so.
        assertTrue("We found an item where we shouldn't have.", adapter.get(ACOH2_HEADER_QNAME) == null);

	}
	
	public void testKeyEquivalence() throws Exception {

		MessageContext messageContext = getMessageContext();
		
		SOAPHeadersAdapter.install(messageContext);
		
    	SOAPFactory sf = SOAPFactory.newInstance();
    	
    	// QName used here should match the key for the list set on the requestCtx
    	SOAPElement e1 = sf.createElement(ACOH1_HEADER_QNAME);
    	e1.addTextNode(CONTENT_STRING1);
    	String acoh1 = e1.toString();
    	
    	// QName used here should match the key for the list set on the requestCtx
    	SOAPElement e2 = sf.createElement(ACOH1_HEADER_QNAME);
    	e2.addTextNode(CONTENT_STRING2);
    	String acoh2 = e2.toString();

        List<String> acoh1ContentList = new ArrayList<String>();
        acoh1ContentList.add(acoh1);
        
        // leaving this list empty
        List<String> acoh2ContentList = new ArrayList<String>();
        acoh2ContentList.add(acoh2);
        
        SOAPHeadersAdapter adapter = (SOAPHeadersAdapter)messageContext.getProperty(Constants.JAXWS_OUTBOUND_SOAP_HEADERS);
        
        QName equivalentKey = new QName(ACOH1_HEADER_QNAME.getNamespaceURI(), ACOH1_HEADER_QNAME.getLocalPart(), ACOH1_HEADER_QNAME.getPrefix());
        
        adapter.put(ACOH1_HEADER_QNAME, acoh1ContentList);
        adapter.put(equivalentKey, acoh2ContentList);

        // testing that two object keys that pass the QName.equals() will result
        // in previously added headers being wiped
        
        assertTrue("Adapter should have one item, but has " + adapter.size(), adapter.size() == 1);
        
        // double-check
        assertTrue(adapter.get(ACOH1_HEADER_QNAME).get(0).equals(acoh2));
        assertTrue(((List<String>)(adapter.get(ACOH1_HEADER_QNAME))).size() == 1);

	}
	
	public void testListItemRemoval() throws Exception {

		MessageContext messageContext = getMessageContext();
		
		SOAPHeadersAdapter.install(messageContext);
		
    	SOAPFactory sf = SOAPFactory.newInstance();
    	
    	// QName used here should match the key for the list set on the requestCtx
    	SOAPElement e1 = sf.createElement(ACOH1_HEADER_QNAME);
    	e1.addTextNode(CONTENT_STRING1);
    	String acoh1 = e1.toString();
    	
    	// QName used here should match the key for the list set on the requestCtx
    	SOAPElement e2 = sf.createElement(ACOH2_HEADER_QNAME);
    	e2.addTextNode(CONTENT_STRING2);
    	String acoh2 = e2.toString();
    	
    	// QName used here should match the key for the list set on the requestCtx
    	SOAPElement e3 = sf.createElement(ACOH2_HEADER_QNAME);
    	e3.addTextNode(LARGE_STRING);
    	String acoh3 = e3.toString();

        List<String> acoh1ContentList = new ArrayList<String>();
        acoh1ContentList.add(acoh1);
        
        // leaving this list empty
        List<String> acoh2ContentList = new ArrayList<String>();
        acoh2ContentList.add(acoh2);
        acoh2ContentList.add(acoh3);
        
        SOAPHeadersAdapter adapter = (SOAPHeadersAdapter)messageContext.getProperty(Constants.JAXWS_OUTBOUND_SOAP_HEADERS);
        
        adapter.put(ACOH1_HEADER_QNAME, acoh1ContentList);
        adapter.put(ACOH2_HEADER_QNAME, acoh2ContentList);
        
        // remove everything, by different means
        adapter.remove(ACOH1_HEADER_QNAME);
        
        // SOAPHeadersAdapter does NOT give back a live list, so these don't mean anything
        adapter.get(ACOH2_HEADER_QNAME).remove(0);
        adapter.get(ACOH2_HEADER_QNAME).remove(acoh3);

        assertTrue("Adapter should have one item, but has " + adapter.size(), adapter.size() == 1);
        
        // double-check
        assertTrue(adapter.get(ACOH2_HEADER_QNAME).get(0).equals(acoh2));
        assertTrue(adapter.get(ACOH2_HEADER_QNAME).get(1).equals(acoh3));
        assertTrue(((List<String>)(adapter.get(ACOH2_HEADER_QNAME))).size() == 2);

	}
	
	// TODO review to verify validity of this test's expectations
	public void testAddRemoveException() throws Exception {

		MessageContext messageContext = getMessageContext();
		
		SOAPHeadersAdapter.install(messageContext);
		
    	SOAPFactory sf = SOAPFactory.newInstance();
    	
    	// QName used here should match the key for the list set on the requestCtx
    	// NOTE that for this test it intentionally does not match
    	SOAPElement e1 = sf.createElement(ACOH2_HEADER_QNAME);
    	e1.addTextNode(CONTENT_STRING1);
    	String acoh1 = e1.toString();
    	
        List<String> acoh1ContentList = new ArrayList<String>();
        acoh1ContentList.add(acoh1);
        
        SOAPHeadersAdapter adapter = (SOAPHeadersAdapter)messageContext.getProperty(Constants.JAXWS_OUTBOUND_SOAP_HEADERS);
        
        adapter.put(ACOH1_HEADER_QNAME, acoh1ContentList);
        
        try {
        	adapter.remove(ACOH1_HEADER_QNAME);
        	fail("should have got an exception");
        } catch (WebServiceException e) {
        	// it's not ideal to compare exception output, but...
        	assertTrue(e.getCause().getMessage().contains("Element name from data source is acoh2, not the expected acoh1"));
        	return;
        }
        fail("Should have returned in the 'catch' block.");
	}
	
	public void testBigContent() throws Exception {
		MessageContext messageContext = getMessageContext();
		
		SOAPHeadersAdapter.install(messageContext);
		
    	SOAPFactory sf = SOAPFactory.newInstance();
    	
    	// QName used here should match the key for the list set on the requestCtx
    	SOAPElement e1 = sf.createElement(ACOH1_HEADER_QNAME);
    	e1.addTextNode(LARGE_STRING);
    	String acoh1 = e1.toString();

        List<String> acoh1ContentList = new ArrayList<String>();
        acoh1ContentList.add(acoh1);
        
        SOAPHeadersAdapter adapter = (SOAPHeadersAdapter)messageContext.getProperty(Constants.JAXWS_OUTBOUND_SOAP_HEADERS);
        
        adapter.put(ACOH1_HEADER_QNAME, acoh1ContentList);
        
        assertTrue("Adapter should have one item, but has " + adapter.size(), adapter.size() == 1);
	}
	
	public void testHeaderStringReuse() throws Exception {
		MessageContext messageContext = getMessageContext();

		SOAPHeadersAdapter.install(messageContext);
		
    	SOAPFactory sf = SOAPFactory.newInstance();
    	
    	// QName used here should match the key for the list set on the requestCtx
    	// NOTE we are re-using this xml string in three adds, two of which have mismatched QNames
    	SOAPElement e1 = sf.createElement(ACOH1_HEADER_QNAME);
    	e1.addTextNode(LARGE_STRING);
    	String acoh1 = e1.toString();

        List<String> acoh1ContentList = new ArrayList<String>();
        acoh1ContentList.add(acoh1);
        
        SOAPHeadersAdapter adapter = (SOAPHeadersAdapter)messageContext.getProperty(Constants.JAXWS_OUTBOUND_SOAP_HEADERS);
        
        // Mismatched QNames!
        adapter.put(ACOH1_HEADER_QNAME, acoh1ContentList);
        adapter.put(ACOH2_HEADER_QNAME, acoh1ContentList);
        
        assertTrue("Adapter should have two items, but has " + adapter.size(), adapter.size() == 2);
	}
	
	public void testContainsKey() throws Exception {
		MessageContext messageContext = getMessageContext();
		
		SOAPHeadersAdapter.install(messageContext);
		
    	SOAPFactory sf = SOAPFactory.newInstance();
    	
    	// QName used here should match the key for the list set on the requestCtx
    	SOAPElement e1 = sf.createElement(ACOH1_HEADER_QNAME);
    	e1.addTextNode(LARGE_STRING);
    	String acoh1 = e1.toString();

        List<String> acoh1ContentList = new ArrayList<String>();
        acoh1ContentList.add(acoh1);
        
        SOAPHeadersAdapter adapter = (SOAPHeadersAdapter)messageContext.getProperty(Constants.JAXWS_OUTBOUND_SOAP_HEADERS);
        
        adapter.put(ACOH1_HEADER_QNAME, acoh1ContentList);
        
        assertTrue("Adapter should contain the key " + ACOH1_HEADER_QNAME, adapter.containsKey(ACOH1_HEADER_QNAME));
	}
	
	public void testContainsValue() throws Exception {
		MessageContext messageContext = getMessageContext();
		
		SOAPHeadersAdapter.install(messageContext);
		
    	SOAPFactory sf = SOAPFactory.newInstance();
    	
    	// QName used here should match the key for the list set on the requestCtx
    	SOAPElement e1 = sf.createElement(ACOH1_HEADER_QNAME);
    	e1.addTextNode(LARGE_STRING);
    	String acoh1 = e1.toString();

        List<String> acoh1ContentList = new ArrayList<String>();
        acoh1ContentList.add(acoh1);
        
        SOAPHeadersAdapter adapter = (SOAPHeadersAdapter)messageContext.getProperty(Constants.JAXWS_OUTBOUND_SOAP_HEADERS);
        
        adapter.put(ACOH1_HEADER_QNAME, acoh1ContentList);
        
        assertTrue("Adapter should contain the value " + acoh1ContentList, adapter.containsValue(acoh1ContentList));
	}
	
	public void testEmpty() throws Exception {
		MessageContext messageContext = getMessageContext();
		
		SOAPHeadersAdapter.install(messageContext);
		
    	SOAPFactory sf = SOAPFactory.newInstance();
    	
    	// QName used here should match the key for the list set on the requestCtx
    	SOAPElement e1 = sf.createElement(ACOH1_HEADER_QNAME);
    	e1.addTextNode(LARGE_STRING);
    	String acoh1 = e1.toString();

        List<String> acoh1ContentList = new ArrayList<String>();
        acoh1ContentList.add(acoh1);
        
        SOAPHeadersAdapter adapter = (SOAPHeadersAdapter)messageContext.getProperty(Constants.JAXWS_OUTBOUND_SOAP_HEADERS);
        
        adapter.put(ACOH1_HEADER_QNAME, acoh1ContentList);
        adapter.remove(ACOH1_HEADER_QNAME);
        
        assertTrue("Adapter should be empty.", adapter.isEmpty());
	}

	
	public void testValues() throws Exception {
		MessageContext messageContext = getMessageContext();
		
		SOAPHeadersAdapter.install(messageContext);
		
    	SOAPFactory sf = SOAPFactory.newInstance();
    	
    	// QName used here should match the key for the list set on the requestCtx
    	SOAPElement e1 = sf.createElement(ACOH1_HEADER_QNAME);
    	e1.addTextNode(CONTENT_STRING1);
    	String acoh1 = e1.toString();
    	
    	// QName used here should match the key for the list set on the requestCtx
    	SOAPElement e2 = sf.createElement(ACOH2_HEADER_QNAME);
    	e2.addTextNode(CONTENT_STRING2);
    	String acoh2 = e2.toString();

        List<String> acoh1ContentList = new ArrayList<String>();
        acoh1ContentList.add(acoh1);
        
        List<String> acoh2ContentList = new ArrayList<String>();
        acoh2ContentList.add(acoh2);
        
        SOAPHeadersAdapter adapter = (SOAPHeadersAdapter)messageContext.getProperty(Constants.JAXWS_OUTBOUND_SOAP_HEADERS);
        
        adapter.put(ACOH1_HEADER_QNAME, acoh1ContentList);
        adapter.put(ACOH2_HEADER_QNAME, acoh2ContentList);

        // testing "values()" method 
        assertTrue(adapter.values().contains(acoh1ContentList));
        assertTrue(adapter.values().contains(acoh2ContentList));

        // re-check to make sure nothing got corrupted
        assertTrue(adapter.get(ACOH1_HEADER_QNAME).get(0).equals(acoh1));
        assertTrue(adapter.get(ACOH2_HEADER_QNAME).get(0).equals(acoh2));
	}
	

	public void testKeySet() throws Exception {
		MessageContext messageContext = getMessageContext();
		
		SOAPHeadersAdapter.install(messageContext);
		
    	SOAPFactory sf = SOAPFactory.newInstance();
    	
    	// QName used here should match the key for the list set on the requestCtx
    	SOAPElement e1 = sf.createElement(ACOH1_HEADER_QNAME);
    	e1.addTextNode(CONTENT_STRING1);
    	String acoh1 = e1.toString();
    	
    	// QName used here should match the key for the list set on the requestCtx
    	SOAPElement e2 = sf.createElement(ACOH2_HEADER_QNAME);
    	e2.addTextNode(CONTENT_STRING2);
    	String acoh2 = e2.toString();

        List<String> acoh1ContentList = new ArrayList<String>();
        acoh1ContentList.add(acoh1);
        
        List<String> acoh2ContentList = new ArrayList<String>();
        acoh2ContentList.add(acoh2);
        
        SOAPHeadersAdapter adapter = (SOAPHeadersAdapter)messageContext.getProperty(Constants.JAXWS_OUTBOUND_SOAP_HEADERS);
        
        adapter.put(ACOH1_HEADER_QNAME, acoh1ContentList);
        adapter.put(ACOH2_HEADER_QNAME, acoh2ContentList);
        adapter.remove(ACOH1_HEADER_QNAME);
        
        // testing "keySet()" method 
        Set<QName> keyset = adapter.keySet();
        assertTrue(!keyset.contains(ACOH1_HEADER_QNAME));
        assertTrue(keyset.contains(ACOH2_HEADER_QNAME));

        // re-check to make sure nothing got corrupted
        assertTrue(adapter.get(ACOH2_HEADER_QNAME).get(0).equals(acoh2));
	}
	
	public void testEntrySet() throws Exception {
	    MessageContext messageContext = getMessageContext();

	    SOAPHeadersAdapter.install(messageContext);

	    SOAPFactory sf = SOAPFactory.newInstance();

	    // QName used here should match the key for the list set on the requestCtx
	    SOAPElement e1 = sf.createElement(ACOH1_HEADER_QNAME);
	    e1.addTextNode(CONTENT_STRING1);
	    String acoh1 = e1.toString();

	    // QName used here should match the key for the list set on the requestCtx
	    SOAPElement e2 = sf.createElement(ACOH2_HEADER_QNAME);
	    e2.addTextNode(CONTENT_STRING2);
	    String acoh2 = e2.toString();

	    List<String> acoh1ContentList = new ArrayList<String>();
	    acoh1ContentList.add(acoh1);

	    List<String> acoh2ContentList = new ArrayList<String>();
	    acoh2ContentList.add(acoh2);

	    SOAPHeadersAdapter adapter = (SOAPHeadersAdapter)messageContext.getProperty(Constants.JAXWS_OUTBOUND_SOAP_HEADERS);

	    Map<QName, List<String>> hm1 = new HashMap<QName, List<String>>();
        hm1.put(ACOH1_HEADER_QNAME, acoh1ContentList);
        Map<QName, List<String>> hm2 = new HashMap<QName, List<String>>();
        hm2.put(ACOH2_HEADER_QNAME, acoh2ContentList);
	    
	    adapter.putAll(hm1);
	    adapter.putAll(hm2);

	    // testing "entrySet()" method
	    Set<Map.Entry<QName, List<String>>> entryset = adapter.entrySet();
	    
        int checkCounter = 0;
	    for (Iterator it = entryset.iterator();it.hasNext();) {
	        Map.Entry<QName, List<String>> entry = (Map.Entry<QName, List<String>>)it.next();
	        // we cannot assume that the order the maps went into
	        // the adapter will be the order they come out:
	        if (entry.getKey().equals(ACOH1_HEADER_QNAME)) {
	            assertTrue(entry.getKey().equals(ACOH1_HEADER_QNAME));
	            assertTrue(entry.getValue().equals(acoh1ContentList));
	            checkCounter++;
	        } else if (entry.getKey().equals(ACOH2_HEADER_QNAME)) {
	            assertTrue(entry.getKey().equals(ACOH2_HEADER_QNAME));
	            assertTrue(entry.getValue().equals(acoh2ContentList));
	            checkCounter += 2;
	        }
	    }
        assertTrue("Expected entrySet was not returned from SOAPHeadersAdapter.entrySet().", checkCounter == 3);

	    // re-check to make sure nothing got corrupted
	    assertTrue(adapter.get(ACOH1_HEADER_QNAME).get(0).equals(acoh1));
	    assertTrue(adapter.get(ACOH2_HEADER_QNAME).get(0).equals(acoh2));
	}
	
	public void testPutAll() throws Exception {
		MessageContext messageContext = getMessageContext();
		
		SOAPHeadersAdapter.install(messageContext);
		
    	SOAPFactory sf = SOAPFactory.newInstance();
    	
    	// QName used here should match the key for the list set on the requestCtx
    	SOAPElement e1 = sf.createElement(ACOH1_HEADER_QNAME);
    	e1.addTextNode(CONTENT_STRING1);
    	String acoh1 = e1.toString();
    	
    	// QName used here should match the key for the list set on the requestCtx
    	SOAPElement e2 = sf.createElement(ACOH2_HEADER_QNAME);
    	e2.addTextNode(CONTENT_STRING2);
    	String acoh2 = e2.toString();

        List<String> acoh1ContentList = new ArrayList<String>();
        acoh1ContentList.add(acoh1);
        
        List<String> acoh2ContentList = new ArrayList<String>();
        acoh2ContentList.add(acoh2);
        
        Map<QName, List<String>> requestHeaders = new HashMap<QName, List<String>>();
        requestHeaders.put(ACOH1_HEADER_QNAME, acoh1ContentList);
        requestHeaders.put(ACOH2_HEADER_QNAME, acoh2ContentList);
        
        SOAPHeadersAdapter adapter = (SOAPHeadersAdapter)messageContext.getProperty(Constants.JAXWS_OUTBOUND_SOAP_HEADERS);

        adapter.putAll(requestHeaders);
        
        // testing "keySet()" method 
        Set<QName> keyset = adapter.keySet();
        assertTrue(keyset.contains(ACOH1_HEADER_QNAME));
        assertTrue(keyset.contains(ACOH2_HEADER_QNAME));

        // check the data too
        assertTrue(adapter.get(ACOH1_HEADER_QNAME).get(0).equals(acoh1));
        assertTrue(adapter.get(ACOH2_HEADER_QNAME).get(0).equals(acoh2));
	}

	
	// The next few tests exercise the underlying data structures that define a "Message" object

	public void testAddRemoveAsSOAPMessage() throws Exception {

		MessageContext messageContext = getMessageContext();
		
		SOAPHeadersAdapter.install(messageContext);
		
    	SOAPFactory sf = SOAPFactory.newInstance();
    	
    	// QName used here should match the key for the list set on the requestCtx
    	SOAPElement e1 = sf.createElement(ACOH1_HEADER_QNAME);
    	e1.addTextNode(CONTENT_STRING1);
    	String acoh1 = e1.toString();
    	
    	// QName used here should match the key for the list set on the requestCtx
    	SOAPElement e2 = sf.createElement(ACOH1_HEADER_QNAME);
    	e2.addTextNode(CONTENT_STRING2);
    	String acoh2 = e2.toString();

        List<String> acoh1ContentList = new ArrayList<String>();
        acoh1ContentList.add(acoh1);
        acoh1ContentList.add(acoh2);
        
        SOAPHeadersAdapter adapter = (SOAPHeadersAdapter)messageContext.getProperty(Constants.JAXWS_OUTBOUND_SOAP_HEADERS);
        
        adapter.put(ACOH1_HEADER_QNAME, acoh1ContentList);
        
        // get message object and convert to SOAPMessage
        SOAPMessage soapMessage = messageContext.getMessage().getAsSOAPMessage();
        
        // confirm headers are there
        SOAPHeader soapHeader = soapMessage.getSOAPHeader();
        Iterator<SOAPHeaderElement> it = soapHeader.getChildElements();
        // TODO: not sure if the order of the header additions is or should be preserved.
        // in other words, this test may be a little too strict.
        SOAPHeaderElement headerElem1 = it.next();
        SOAPHeaderElement headerElem2 = it.next();
        // should only be two header elements, so...
        assertFalse(it.hasNext());
        
        assertTrue(headerElem1.toString().equals(acoh1));
        assertTrue(headerElem2.toString().equals(acoh2));
        
        // now that we've done a toString() on the header elements, they've been parsed and
        // processed by the underlying OM implementation...  let's remove one by way of SOAP
        // API, then let's make sure we can still get and manipulate the headers via the
        // SOAPHeadersAdapter
        
        // TODO:  removeChild gives an exception
        //soapHeader.removeChild(headerElem1);
        headerElem1.detachNode();
        
        // one is removed, make sure the SOAPHeadersAdapter reflects the change
        
        List<String> contentListAfterSOAPRemoval = adapter.get(ACOH1_HEADER_QNAME);
        assertTrue(contentListAfterSOAPRemoval.size() == 1);
        // remember we removed headerElem1, so we expect acoh2 to still exist
        assertTrue(contentListAfterSOAPRemoval.get(0).equals(acoh2));
        
	}
	
	public void testAddRemoveAsSOAPEnvelope() throws Exception {

		MessageContext messageContext = getMessageContext();
		
		SOAPHeadersAdapter.install(messageContext);
		
    	SOAPFactory sf = SOAPFactory.newInstance();
    	
    	// QName used here should match the key for the list set on the requestCtx
    	SOAPElement e1 = sf.createElement(ACOH1_HEADER_QNAME);
    	e1.addTextNode(CONTENT_STRING1);
    	String acoh1 = e1.toString();
    	
    	// QName used here should match the key for the list set on the requestCtx
    	SOAPElement e2 = sf.createElement(ACOH1_HEADER_QNAME);
    	e2.addTextNode(CONTENT_STRING2);
    	String acoh2 = e2.toString();

        List<String> acoh1ContentList = new ArrayList<String>();
        acoh1ContentList.add(acoh1);
        acoh1ContentList.add(acoh2);
        
        SOAPHeadersAdapter adapter = (SOAPHeadersAdapter)messageContext.getProperty(Constants.JAXWS_OUTBOUND_SOAP_HEADERS);
        
        adapter.put(ACOH1_HEADER_QNAME, acoh1ContentList);
        
        // get message object and convert to SOAPEnvelope
        SOAPEnvelope soapEnvelope = messageContext.getMessage().getAsSOAPEnvelope();
        
        // confirm headers are there
        SOAPHeader soapHeader = soapEnvelope.getHeader();
        Iterator<SOAPHeaderElement> it = soapHeader.getChildElements();
        // TODO: not sure if the order of the header additions is or should be preserved.
        // in other words, this test may be a little too strict.
        SOAPHeaderElement headerElem1 = it.next();
        SOAPHeaderElement headerElem2 = it.next();
        // should only be two header elements, so...
        assertFalse(it.hasNext());
        
        assertTrue(headerElem1.toString().equals(acoh1));
        assertTrue(headerElem2.toString().equals(acoh2));
        
        // now that we've done a toString() on the header elements, they've been parsed and
        // processed by the underlying OM implementation...  let's remove one by way of SOAP
        // API, then let's make sure we can still get and manipulate the headers via the
        // SOAPHeadersAdapter
        
        // TODO:  removeChild gives an exception
        //soapHeader.removeChild(headerElem1);
        headerElem1.detachNode();
        
        // one is removed, make sure the SOAPHeadersAdapter reflects the change
        
        List<String> contentListAfterSOAPRemoval = adapter.get(ACOH1_HEADER_QNAME);
        assertTrue(contentListAfterSOAPRemoval.size() == 1);
        // remember we removed headerElem1, so we expect acoh2 to still exist
        assertTrue(contentListAfterSOAPRemoval.get(0).equals(acoh2));
        
	}
	
	public void testAddRemoveAsOMElement() throws Exception {

		MessageContext messageContext = getMessageContext();
		
		SOAPHeadersAdapter.install(messageContext);
		
    	SOAPFactory sf = SOAPFactory.newInstance();
    	
    	// QName used here should match the key for the list set on the requestCtx
    	SOAPElement e1 = sf.createElement(ACOH1_HEADER_QNAME);
    	e1.addTextNode(CONTENT_STRING1);
    	String acoh1 = e1.toString();
    	
    	// QName used here should match the key for the list set on the requestCtx
    	SOAPElement e2 = sf.createElement(ACOH1_HEADER_QNAME);
    	e2.addTextNode(CONTENT_STRING2);
    	String acoh2 = e2.toString();

        List<String> acoh1ContentList = new ArrayList<String>();
        acoh1ContentList.add(acoh1);
        acoh1ContentList.add(acoh2);
        
        SOAPHeadersAdapter adapter = (SOAPHeadersAdapter)messageContext.getProperty(Constants.JAXWS_OUTBOUND_SOAP_HEADERS);
        
        adapter.put(ACOH1_HEADER_QNAME, acoh1ContentList);
        
        // get message object and convert to SOAPEnvelope
        OMElement omEnvelope = messageContext.getMessage().getAsOMElement();
        
        // confirm headers are there.  I can cast here only because I know the implementation.  :)
        SOAP11HeaderImpl omHeader = (SOAP11HeaderImpl)omEnvelope.getChildElements().next();

        Iterator<OMSourcedElementImpl> it = omHeader.getChildElements();
        // TODO: not sure if the order of the header additions is or should be preserved.
        // in other words, this test may be a little too strict.
        OMSourcedElementImpl headerElem1 = it.next();
        OMSourcedElementImpl headerElem2 = it.next();
        // should only be two header elements, so...
        assertFalse(it.hasNext());
        
        assertTrue(headerElem1.toString().equals(acoh1));
        assertTrue(headerElem2.toString().equals(acoh2));
        
        // now that we've done a toString() on the header elements, they've been parsed and
        // processed by the underlying OM implementation...  let's remove one by way of SOAP
        // API, then let's make sure we can still get and manipulate the headers via the
        // SOAPHeadersAdapter
        
        // TODO:  removeChild gives an exception
        //soapHeader.removeChild(headerElem1);
        headerElem1.detach();
        
        // one is removed, make sure the SOAPHeadersAdapter reflects the change
        
        List<String> contentListAfterSOAPRemoval = adapter.get(ACOH1_HEADER_QNAME);
        assertTrue(contentListAfterSOAPRemoval.size() == 1);
        // remember we removed headerElem1, so we expect acoh2 to still exist
        assertTrue(contentListAfterSOAPRemoval.get(0).equals(acoh2));

	}
	
	public void testAddRemoveAsOMElementUsingSourceFactory() throws Exception {

		MessageContext messageContext = getMessageContextUsingSourceFactory();
		
		SOAPHeadersAdapter.install(messageContext);
		
    	SOAPFactory sf = SOAPFactory.newInstance();
    	
    	// QName used here should match the key for the list set on the requestCtx
    	SOAPElement e1 = sf.createElement(ACOH1_HEADER_QNAME);
    	e1.addTextNode(CONTENT_STRING1);
    	String acoh1 = e1.toString();
    	
    	// QName used here should match the key for the list set on the requestCtx
    	SOAPElement e2 = sf.createElement(ACOH1_HEADER_QNAME);
    	e2.addTextNode(CONTENT_STRING2);
    	String acoh2 = e2.toString();

        List<String> acoh1ContentList = new ArrayList<String>();
        acoh1ContentList.add(acoh1);
        acoh1ContentList.add(acoh2);
        
        SOAPHeadersAdapter adapter = (SOAPHeadersAdapter)messageContext.getProperty(Constants.JAXWS_OUTBOUND_SOAP_HEADERS);
        
        adapter.put(ACOH1_HEADER_QNAME, acoh1ContentList);
        
        // get message object and convert to SOAPEnvelope
        OMElement omEnvelope = messageContext.getMessage().getAsOMElement();
        
        // confirm headers are there.  I can cast here only because I know the implementation.  :)
        SOAP11HeaderImpl omHeader = (SOAP11HeaderImpl)omEnvelope.getChildElements().next();

        Iterator<OMSourcedElementImpl> it = omHeader.getChildElements();
        // TODO: not sure if the order of the header additions is or should be preserved.
        // in other words, this test may be a little too strict.
        OMSourcedElementImpl headerElem1 = it.next();
        OMSourcedElementImpl headerElem2 = it.next();
        // should only be two header elements, so...
        assertFalse(it.hasNext());
        
        assertTrue(headerElem1.toString().equals(acoh1));
        assertTrue(headerElem2.toString().equals(acoh2));
        
        // now that we've done a toString() on the header elements, they've been parsed and
        // processed by the underlying OM implementation...  let's remove one by way of SOAP
        // API, then let's make sure we can still get and manipulate the headers via the
        // SOAPHeadersAdapter
        
        // TODO:  removeChild gives an exception
        //soapHeader.removeChild(headerElem1);
        headerElem1.detach();
        
        // one is removed, make sure the SOAPHeadersAdapter reflects the change
        
        List<String> contentListAfterSOAPRemoval = adapter.get(ACOH1_HEADER_QNAME);
        assertTrue(contentListAfterSOAPRemoval.size() == 1);
        // remember we removed headerElem1, so we expect acoh2 to still exist
        assertTrue(contentListAfterSOAPRemoval.get(0).equals(acoh2));

	}
	
	public void testAddRemoveAsOMElementUsingSourceFactoryLogicalMessageImpl() throws Exception {

		MessageContext messageContext = getMessageContextUsingSourceFactory();
		
		SOAPHeadersAdapter.install(messageContext);
		
    	SOAPFactory sf = SOAPFactory.newInstance();
    	
    	// QName used here should match the key for the list set on the requestCtx
    	SOAPElement e1 = sf.createElement(ACOH1_HEADER_QNAME);
    	e1.addTextNode(CONTENT_STRING1);
    	String acoh1 = e1.toString();
    	
    	// QName used here should match the key for the list set on the requestCtx
    	SOAPElement e2 = sf.createElement(ACOH1_HEADER_QNAME);
    	e2.addTextNode(CONTENT_STRING2);
    	String acoh2 = e2.toString();

        List<String> acoh1ContentList = new ArrayList<String>();
        acoh1ContentList.add(acoh1);
        acoh1ContentList.add(acoh2);
        
        SOAPHeadersAdapter adapter = (SOAPHeadersAdapter)messageContext.getProperty(Constants.JAXWS_OUTBOUND_SOAP_HEADERS);
        
        adapter.put(ACOH1_HEADER_QNAME, acoh1ContentList);
        
        List<String> headersList = adapter.get(ACOH1_HEADER_QNAME);
        headersList.get(0).toString();  // trigger some underlying OM implementation parsing
        
        // use the LogicalMessageImpl to get the message payload
        MyLogicalMessageImpl logicalMessageImpl = new MyLogicalMessageImpl(messageContext);
        Source payload = logicalMessageImpl.getPayload();
        payload.toString();
        
        // get message object and convert to SOAPEnvelope
        OMElement omEnvelope = messageContext.getMessage().getAsOMElement();
        
        // confirm headers are there.  I can cast here only because I know the implementation.  :)
        SOAP11HeaderImpl omHeader = (SOAP11HeaderImpl)omEnvelope.getChildElements().next();

        Iterator<OMSourcedElementImpl> it = omHeader.getChildElements();
        // TODO: not sure if the order of the header additions is or should be preserved.
        // in other words, this test may be a little too strict.
        OMSourcedElementImpl headerElem1 = it.next();
        OMSourcedElementImpl headerElem2 = it.next();
        // should only be two header elements, so...
        assertFalse(it.hasNext());
        
        assertTrue(headerElem1.toString().equals(acoh1));
        assertTrue(headerElem2.toString().equals(acoh2));
        
        // now that we've done a toString() on the header elements, they've been parsed and
        // processed by the underlying OM implementation...  let's remove one by way of SOAP
        // API, then let's make sure we can still get and manipulate the headers via the
        // SOAPHeadersAdapter
        
        // TODO:  removeChild gives an exception
        //soapHeader.removeChild(headerElem1);
        headerElem1.detach();
        
        // one is removed, make sure the SOAPHeadersAdapter reflects the change
        
        List<String> contentListAfterSOAPRemoval = adapter.get(ACOH1_HEADER_QNAME);
        assertTrue(contentListAfterSOAPRemoval.size() == 1);
        // remember we removed headerElem1, so we expect acoh2 to still exist
        assertTrue(contentListAfterSOAPRemoval.get(0).equals(acoh2));

	}

	
	/*
	 *  provide a MessageContext with a valid Message object
	 */
	private MessageContext getMessageContext() throws XMLStreamException {
		// Create a SOAP 1.1 Message
		MessageFactory mf = (MessageFactory) FactoryRegistry
				.getFactory(MessageFactory.class);
		Message m = mf.create(Protocol.soap11);

		// Get the BlockFactory
		XMLStringBlockFactory f = (XMLStringBlockFactory) FactoryRegistry
				.getFactory(XMLStringBlockFactory.class);

		Block block = f.createFrom(sampleText, null, null);
		
		// Add the block to the message as normal body content.
		m.setBodyBlock(block);

		MessageContext messageContext = new MessageContext();
		messageContext.setMessage(m);
		
		return messageContext;
	}
	
	/*
	 * provide a MessageContext with a valid Message object
	 */
	private MessageContext getMessageContextUsingSourceFactory() throws XMLStreamException {
		// Create a SOAP 1.1 Message
		MessageFactory mf = (MessageFactory) FactoryRegistry
				.getFactory(MessageFactory.class);
		Message m = mf.create(Protocol.soap11);

		// Get the BlockFactory
		SourceBlockFactory f = (SourceBlockFactory) FactoryRegistry
				.getFactory(SourceBlockFactory.class);

		Block block = f.createFrom(new StreamSource(new StringReader(sampleText)), null, null);
		
		// Add the block to the message as normal body content.
		m.setBodyBlock(block);

		MessageContext messageContext = new MessageContext();
		messageContext.setMessage(m);
		
		return messageContext;
	}
	
	private class MyLogicalMessageImpl extends LogicalMessageImpl {
		public MyLogicalMessageImpl(MessageContext mc) {
			super(mc.getMEPContext());
		}
	}
	
}
