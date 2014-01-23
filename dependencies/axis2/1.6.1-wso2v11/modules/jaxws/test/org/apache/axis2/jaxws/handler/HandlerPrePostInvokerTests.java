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

package org.apache.axis2.jaxws.handler;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.handler.factory.HandlerPostInvokerFactory;
import org.apache.axis2.jaxws.handler.factory.HandlerPreInvokerFactory;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.message.factory.XMLStringBlockFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;

import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.ArrayList;
import java.util.Set;

/**
 * HandlerPrePostInvokerTests verifies that the mechanisms for finding the implementation classes to 
 * call before and after handler.handleMessage work, and that the calls are actually made.  Simple as that.
 */
public class HandlerPrePostInvokerTests extends TestCase {

	private MessageContext mc = null;
	private boolean preInvokerCalled = false;
	private boolean postInvokerCalled = false;
    private boolean messageAccessed = false;
	
	private static final String soap11env = "http://schemas.xmlsoap.org/soap/envelope/";
	
    public static final String SOAP11_ENVELOPE = 
        "<?xml version='1.0' encoding='utf-8'?>" + 
        "<soapenv:Envelope xmlns:soapenv=\"" + soap11env + "\">" +
        "<soapenv:Header />" + 
        "<soapenv:Body>" +
        "</soapenv:Body>" + 
        "</soapenv:Envelope>";

	@Override
	protected void setUp() throws Exception {

        // Create a SOAP 1.1 Message and MessageContext
		// I just grabbed this code from the JAXWS MessageTests
        MessageFactory mf = (MessageFactory) FactoryRegistry.getFactory(MessageFactory.class);
        Message m = mf.create(Protocol.soap11);
        XMLStringBlockFactory f =
                (XMLStringBlockFactory) FactoryRegistry.getFactory(XMLStringBlockFactory.class);
        Block block = f.createFrom(SOAP11_ENVELOPE, null, null);
        m.setBodyBlock(block);

        mc = new MessageContext();
        mc.setMessage(m);
        mc.setMEPContext(new MEPContext(mc));
	}

	/**
	 * make sure the defaults are as expected
	 *
	 */
	public void testFactoryRegistry() {
		HandlerPreInvokerFactory preFact = (HandlerPreInvokerFactory)FactoryRegistry.getFactory(HandlerPreInvokerFactory.class);
		HandlerPostInvokerFactory postFact = (HandlerPostInvokerFactory)FactoryRegistry.getFactory(HandlerPostInvokerFactory.class);
		HandlerPreInvoker preInvoker = preFact.createHandlerPreInvoker();
		HandlerPostInvoker postInvoker = postFact.createHandlerPostInvoker();
		assertTrue("preInvoker should be instanceof " + org.apache.axis2.jaxws.handler.impl.HandlerPreInvokerImpl.class.getCanonicalName(), preInvoker instanceof org.apache.axis2.jaxws.handler.impl.HandlerPreInvokerImpl);
		assertTrue("postInvoker should be instanceof " + org.apache.axis2.jaxws.handler.impl.HandlerPostInvokerImpl.class.getCanonicalName(), postInvoker instanceof org.apache.axis2.jaxws.handler.impl.HandlerPostInvokerImpl);
	}
	
	/**
	 * make sure the registered factories are used, and the calls are made in the places we expect
	 *
	 */
	public void testFactoryPrePost() {
		
		FactoryRegistry.setFactory(HandlerPreInvokerFactory.class, new HandlerPreInvokerFactoryImpl());
		FactoryRegistry.setFactory(HandlerPostInvokerFactory.class, new HandlerPostInvokerFactoryImpl());
		
		ArrayList<Handler> handlers = new ArrayList<Handler>();
		handlers.add(new SOAPHandler1());
        HandlerChainProcessor processor =
                new HandlerChainProcessor(handlers, Protocol.soap11);
        boolean success = true;
        try {
            // server-side incoming request
            success = processor.processChain(mc.getMEPContext(),
                                    HandlerChainProcessor.Direction.IN,
                                    HandlerChainProcessor.MEP.REQUEST,
                                    true);
        } catch (Exception e) {
            assertNull(e);  // should not get exception
        }
        
        assertTrue("processChain should have succeeded", success);
        assertTrue("preInvoker should have been called", preInvokerCalled);
        assertTrue("postInvoker should have been called", postInvokerCalled);
        assertTrue("Handler did not access message but messageAccessed property is true.", !messageAccessed);

	}

    public void testPostInvokerMessageAccessed() {
        
        FactoryRegistry.setFactory(HandlerPostInvokerFactory.class, new HandlerPostInvokerFactoryImpl());
        
        ArrayList<Handler> handlers = new ArrayList<Handler>();
        handlers.add(new SOAPHandlerGetsMessage());
        HandlerChainProcessor processor =
                new HandlerChainProcessor(handlers, Protocol.soap11);
        boolean success = true;
        try {
            // server-side incoming request
            success = processor.processChain(mc.getMEPContext(),
                                    HandlerChainProcessor.Direction.IN,
                                    HandlerChainProcessor.MEP.REQUEST,
                                    true);
        } catch (Exception e) {
            assertNull(e);  // should not get exception
        }
        
        assertTrue("processChain should have succeeded", success);
        assertTrue("postInvoker should have been called", postInvokerCalled);
        assertTrue("Handler did access message but messageAccessed property is false.", messageAccessed);


    }

    /*****************************************
     * Classes needed for junit testcase     *
     *****************************************/
    
    private class SOAPHandler1 implements SOAPHandler<SOAPMessageContext> {

        public Set getHeaders() {
            return null;
        }

        public void close(javax.xml.ws.handler.MessageContext messagecontext) {
        }

        public boolean handleFault(SOAPMessageContext messagecontext) {
            return true;
        }

        public boolean handleMessage(SOAPMessageContext messagecontext) {
            return true;
        }

    }
    /*****************************************
     * Classes needed for junit testcase     *
     *****************************************/
    
    private class SOAPHandlerGetsMessage implements SOAPHandler<SOAPMessageContext> {

        public Set getHeaders() {
            return null;
        }

        public void close(javax.xml.ws.handler.MessageContext messagecontext) {
        }

        public boolean handleFault(SOAPMessageContext messagecontext) {
            return true;
        }

        public boolean handleMessage(SOAPMessageContext messagecontext) {
            messagecontext.getMessage();
            return true;
        }

    }
    
    private class HandlerPreInvokerFactoryImpl implements HandlerPreInvokerFactory {
		public HandlerPreInvoker createHandlerPreInvoker() {
			return new HandlerPreInvokerImpl();
		}
    }
    
    private class HandlerPostInvokerFactoryImpl implements HandlerPostInvokerFactory {
    	public HandlerPostInvoker createHandlerPostInvoker() {
    		return new HandlerPostInvokerImpl();
    	}
    }
    
    private class HandlerPreInvokerImpl implements HandlerPreInvoker {
		public void preInvoke(javax.xml.ws.handler.MessageContext mc) {
			preInvokerCalled = true;
		}
    }
    
    private class HandlerPostInvokerImpl implements HandlerPostInvoker {
		public void postInvoke(javax.xml.ws.handler.MessageContext mc) {
			postInvokerCalled = true;
            if (mc instanceof SoapMessageContext) {
                SoapMessageContext smc = (SoapMessageContext) mc;
                // PK96521 - before getting the message (which is expensive) check first to 
                // see if it was actually accessed by the handlers
                messageAccessed = false;
                if (smc.containsKey("jaxws.isMessageAccessed")) {
                    messageAccessed = (Boolean)(smc.get("jaxws.isMessageAccessed"));
                }
            }
		}
    }
}
