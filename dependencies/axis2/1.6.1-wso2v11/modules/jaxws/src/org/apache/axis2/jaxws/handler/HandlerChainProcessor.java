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

import org.apache.axis2.AxisFault;
import org.apache.axis2.jaxws.Constants;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.context.factory.MessageContextFactory;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.handler.factory.HandlerPostInvokerFactory;
import org.apache.axis2.jaxws.handler.factory.HandlerPreInvokerFactory;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.marshaller.impl.alt.MethodMarshallerUtils;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.XMLFault;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.message.util.MessageUtils;
import org.apache.axis2.jaxws.message.util.XMLFaultUtils;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.utility.SAAJFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.soap.SOAPHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class HandlerChainProcessor {

    private static final Log log = LogFactory.getLog(HandlerChainProcessor.class);
    private HandlerPreInvoker handlerPreInvoker = null;
    private HandlerPostInvoker handlerPostInvoker = null;
    
    public enum Direction {
        IN, OUT
    };

    // the type of message, not indicative of one-way vs. request-response
    public enum MEP {
        REQUEST, RESPONSE
    };

    private javax.xml.ws.handler.MessageContext currentMC;  // just a pointer
    private LogicalMessageContext logicalMC = null;
    private SoapMessageContext soapMC = null;

    private MEPContext mepCtx;

    // Copy of the handler chain used by HandlerChainProcessor
    private List<Handler> handlers = null;
    private static final List<Handler> EMPTY_CHAIN = new ArrayList<Handler>(0); 
    
    // for tracking purposes -- see trackInternalCall
    private static Handler currentHandler = null;
    private static String currentMethod = null;

    // track start/end of logical and protocol handlers in the list
    // The two scenarios are:  1) run logical handlers only, 2) run all handlers
    // logical start is always 0
    // protocol start is always logicalLength + 1
    // list end is always handlers.size()-1
    private int logicalLength = 0;

    private final static int SUCCESSFUL = 0;
    private final static int FAILED = 1;
    private final static int PROTOCOL_EXCEPTION = 2;
    private final static int OTHER_EXCEPTION = 3;
    // save it if Handler.handleMessage throws one in
    // HandlerChainProcessor.handleMessage
    private RuntimeException savedException;
    private Protocol proto; // need to save it incase we have to make a fault message

    // we track whether the SOAPHeadersAdapter and SAAJ are both used in a given handler
    // method.  If the tracker property is set, and both are called in a handler method,
    // we throw an exception.  This behavior can easily be turned off by a handler method
    // by removing or setting to true the Constants.JAXWS_HANDLER_TRACKER property
    public enum TRACKER {
        SOAP_HEADERS_ADAPTER_CALLED, SAAJ_CALLED
    };
    private static boolean soap_headers_adapter_called = false;
    private static boolean saaj_called = false;

    /*
     * The HandlerChainProcessor is constructed with the handler chain.
     * The handler chain may be null, empty or already sorted.
     * It also may be shared.  For this reason a copy of chain is made
     * so that the sort and other manipulation does not affect the original
     * chain.
     * @param chain Handler chain
     * @param proto Protocol
      */
	public HandlerChainProcessor(List<Handler> chain, Protocol proto) {
	    if (chain != null) {
            synchronized (chain) {
                if (chain.size() == 0) {
                    // Use empty chain to avoid excessive garbage collection
                    this.handlers = EMPTY_CHAIN;
                } else {
                    this.handlers = new CopyOnWriteArrayList<Handler>(chain); 
                }
            }
        } else {
            handlers = EMPTY_CHAIN;
        }
        this.proto = proto;
    }

	/*
	 * sortChain sorts the local copy of the handlers chain.
	 * The logical handlers are first followed by the protocol handlers.
	 * sortChain also keeps track of the start/end of each type of handler.
	 */
	private void sortChain() throws WebServiceException {
        
        if (handlers.size() == 0) {
            logicalLength = 0;
            return;
        }
        
        ArrayList<Handler> logicalHandlers = new ArrayList<Handler>();
        ArrayList<Handler> protocolHandlers = new ArrayList<Handler>();
        
        Iterator handlerIterator = handlers.iterator();
        
        while (handlerIterator.hasNext()) {
            // this is a safe cast since the handlerResolver and binding.setHandlerChain
            // and InvocationContext.setHandlerChain verifies it before we get here
            Handler handler = (Handler)handlerIterator.next();
            // JAXWS 9.2.1.2 sort them by Logical, then SOAP
            if (LogicalHandler.class.isAssignableFrom(handler.getClass()))
                logicalHandlers.add((LogicalHandler) handler);
            else if (SOAPHandler.class.isAssignableFrom(handler.getClass()))
                // instanceof ProtocolHandler
                protocolHandlers.add((SOAPHandler) handler);
            else if (Handler.class.isAssignableFrom(handler.getClass())) {
                throw ExceptionFactory.makeWebServiceException(Messages
                    .getMessage("handlerChainErr1", handler.getClass().getName()));
            } else {
                throw ExceptionFactory.makeWebServiceException(Messages
                    .getMessage("handlerChainErr2", handler.getClass().getName()));
            }
        }
        
        logicalLength = logicalHandlers.size();
        
        // JAXWS 9.2.1.2 sort them by Logical, then SOAP
        handlers.clear();
        handlers.addAll(logicalHandlers);
        handlers.addAll(protocolHandlers);
	}
	

	
	/**
	 * @param mc
	 * By the time processChain method is called, we already have the sorted chain,
	 * and now we have the direction, MEP, MessageContext, and if a response is expected.  We should
	 * be able to handle everything from here, no pun intended.
	 * 
	 * Two things a user of processChain should check when the method completes:
	 * 1.  Has the MessageContext.MESSAGE_OUTBOUND_PROPERTY changed, indicating reversal of message direction
	 * 2.  Has the message been converted to a fault message? (indicated by a flag in the message)
	 */
    public boolean processChain(MEPContext mepCtx, Direction direction, MEP mep,
                                boolean expectResponse) {

        if (handlers.size() == 0)
            return true;
        
        this.mepCtx = mepCtx;
        sortChain();
        initContext(direction);
        boolean result = true;

        if (direction == Direction.OUT) { // 9.3.2 outbound
            currentMC.put(javax.xml.ws.handler.MessageContext.MESSAGE_OUTBOUND_PROPERTY,
                            (direction == Direction.OUT));
            result = callGenericHandlers(mep, expectResponse, 0, handlers.size() - 1, direction);
        } else { // IN case - 9.3.2 inbound
            currentMC.put(javax.xml.ws.handler.MessageContext.MESSAGE_OUTBOUND_PROPERTY,
                            (direction == Direction.OUT));
            result = callGenericHandlers(mep, expectResponse, handlers.size() - 1, 0, direction);
        }

        // message context may have been changed to be response, and message
        // converted
        // according to the JAXWS spec 9.3.2.1 footnote 2
        if ((Boolean) (currentMC.get(javax.xml.ws.handler.MessageContext.MESSAGE_OUTBOUND_PROPERTY)) != (direction == Direction.OUT))
            return false;
        return result;

	}

    public boolean processChainForClose(MEPContext mepCtx, Direction direction) {

        boolean result = true;
        if (handlers.size() == 0)
            return true;
        
        this.mepCtx = mepCtx;
        sortChain();
        initContext(direction);
        callCloseHandlers(handlers.size() - 1, 0, direction);
        return result;
    }
    
     /*
      * This is the implementation of JAX-WS 2.0 section 9.3.2.1
      */
    private boolean callGenericHandlers(MEP mep, boolean expectResponse, int start, int end,
                                     Direction direction) throws RuntimeException {

        // if this is a response message, expectResponse should always be false
        if (mep == MEP.RESPONSE)
            expectResponse = false;

        int i = start;
        int result = SUCCESSFUL;

        // declared and initialized just in case we need them
        // in a reverse flow situation
        int newStart = 0, newStart_inclusive = 0, newEnd = 0;
        Direction newDirection = direction;

        if (direction == Direction.OUT) {
            while ((i <= end) && (result == SUCCESSFUL)) {
                result = handleMessage(((Handler)handlers.get(i)), direction, expectResponse);
                newStart = i - 1;
                newStart_inclusive = i;
                newEnd = 0;
                newDirection = Direction.IN;
                i++;
                if (result == SUCCESSFUL)  // don't switch if failed, since we'll be reversing directions
                    switchContext(direction, i);
            }
        } else { // IN case
            while ((i >= end) && (result == SUCCESSFUL)) {
                result = handleMessage(((Handler)handlers.get(i)), direction, expectResponse);
                newStart = i + 1;
                newStart_inclusive = i;
                newEnd = handlers.size() - 1;
                newDirection = Direction.OUT;
                i--;
                if (result == SUCCESSFUL)  // don't switch if failed, since we'll be reversing directions
                    switchContext(direction, i);
            }
        }

        if (newDirection == direction) // we didn't actually process anything, probably due to empty list
            return true;  // no need to continue

        // 9.3.2.3 in all situations, we want to close as many handlers as
        // were invoked prior to completion or exception throwing
        if (expectResponse) {
            if (result == FAILED) {
                if (log.isDebugEnabled()) {
                    log.debug("Handler returned false...Start running the handlers in reverse");
                }
                // One of that handlers returned false, therefore the handler processing
                // is stoped and the transport outbound will be avoided.
                // This may be due to an exception or it may be due the customer intentionally
                // preventing a message from flowing outbound.
                
                // we should only use callGenericHandlers_avoidRecursion in this case
                // the message context is now an outbound message context,
                // and should be marked as such so the SOAPHeadersAdapter will
                // "install" with the correct property key.
                
                // Get the MessageContext and switch its direction
                MessageContext jaxwsMC = mepCtx.getMessageContext();
                jaxwsMC.setOutbound(newDirection == Direction.OUT);
                SOAPHeadersAdapter.install(jaxwsMC);                
                callGenericHandlers_avoidRecursion(newStart, newEnd, newDirection);
                
                // Now we need to place the Message (which is the one edited by the handler)
                // onto the Axis2 MC.  This is necessary because the Axis2 response MC
                // will be created from this MC and must have the correct message.
                this.placeMessageOnAxis2MessageContext(jaxwsMC);
                
                // Now close the handlers
                callCloseHandlers(newStart_inclusive, newEnd, newDirection);
                if (log.isDebugEnabled()) {
                    log.debug("Handler returned false...End running the handlers in reverse");
                }
            } else if (result == PROTOCOL_EXCEPTION) {
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("Handler threw ProtocolException...Start running the handlers in reverse");
                    }
                    // the message context is now an outbound message context,
                    // and should be marked as such so the SOAPHeadersAdapter will
                    // "install" with the correct property key.
                    
                    // Get the MessageContext and switch its direction
                    MessageContext jaxwsMC = mepCtx.getMessageContext();
                    jaxwsMC.setOutbound(newDirection == Direction.OUT);
                    SOAPHeadersAdapter.install(jaxwsMC);
                    
                    // Call the handlerFault methods on the handlers and also close the handlers
                    callGenericHandleFault(newStart, newEnd, newDirection);
                    
                    // Now we need to place the Message (which is the one edited by the handler)
                    // onto the Axis2 MC.  This is necessary because the Axis2 response MC
                    // will be created from this MC and must have the correct message.
                    this.placeMessageOnAxis2MessageContext(jaxwsMC);
                    
                    // Now close the handlers
                    callCloseHandlers(newStart_inclusive, newEnd, newDirection);
                    if (log.isDebugEnabled()) {
                        log.debug("Handler threw ProtocolException...End running the handlers in reverse");
                    }
                } catch (RuntimeException re) {
                    callCloseHandlers(newStart_inclusive, newEnd, newDirection);
                    throw re;
                }
            } else if (result == OTHER_EXCEPTION) {
                if (log.isDebugEnabled()) {
                    log.debug("Handler threw unanticipated Exception...Start handler closure");
                }
                callCloseHandlers(newStart_inclusive, newEnd, newDirection);
                // savedException initialized in HandlerChainProcessor.handleMessage
                if (log.isDebugEnabled()) {
                    log.debug("Handler threw unanticipated Exception...End handler closure");
                }
                throw savedException;
            }
        } else { // everything was successful OR finished processing handlers
            /*
             * This is a little confusing. There are several cases we should be
             * aware of. An incoming request with false expectResponse is
             * equivalent to server inbound one-way, for example.
             * 
             * An outgoing response is server outbound, and is always marked
             * with a false expectResponse. The problem, however, is that the
             * direction for the call to closehandlers will be incorrect. In
             * this case, the handlers should be closed in the opposite order of
             * the ORIGINAL invocation.
             */
            if (mep.equals(MEP.REQUEST)) {
                // a request that requires no response is a one-way message
                // and we should only close whomever got invoked
                callCloseHandlers(newStart_inclusive, newEnd, newDirection);
                
                // As according to the Sun "experts", exceptions raised by
                // handlers in one way invocation are discarded. They
                // are NOT propagated to the user code.
                if (savedException != null) {
                    log.warn("Exception thrown by a handler in one way invocation",
                             savedException);
                    //But do return failure so that we know not to send to server
                    return false;
                }
            }
            else {
                // it's a response, so we can safely assume that 
                // ALL the handlers were invoked on the request,
                // so we need to close ALL of them
                if (direction.equals(Direction.IN)) {
                    callCloseHandlers(handlers.size() - 1, 0, direction);
                } else {
                    callCloseHandlers(0, handlers.size() - 1, direction);
                }
 
                if (savedException != null) {
                    // we have a saved exception, throw it (JAX-WS 9.3.2.1 "Throw 
                    // ProtocolException or any other runtime exception --> No 
                    // response" case.
                    throw savedException;
                }
            }
        }
        // If we've failed before this, we would have already thrown exception
        // or returned false, so just return true here ... don't need to check result again.
        return true;
    }

    /**
     * Called during reversal and exception processing to place the 
     * current Message (edited by the Handlers) onto the MessageContext
     * @param jaxwsMC
     */
    private void placeMessageOnAxis2MessageContext(MessageContext jaxwsMC) {
        if (log.isDebugEnabled()) {
            log.debug("start placeMessageOnAxis2MessageContext");
        }
        try {
            org.apache.axis2.context.MessageContext mc = jaxwsMC.getAxisMessageContext();
            if (mc != null) {
                Message message = jaxwsMC.getMessage();
                if (message != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Place the (perhaps new or edited) Message on the MessageContext");
                    }
                    MessageUtils.putMessageOnMessageContext(message, mc);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("There is no Message.  Message is not copied to the Axis2 MessageContext.");
                    }
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Axis2 MessageContext is not available.  Message is not copied");
                }
            }
        } catch (AxisFault e) {
            throw ExceptionFactory.makeWebServiceException(e);
        } 
        if (log.isDebugEnabled()) {
            log.debug("end placeMessageOnAxis2MessageContext");
        }
    }
    
    /*
      * callGenericHandlers_avoidRecursion should ONLY be called from one place.
      * TODO:  We cannot necessarily assume no false returns and no exceptions will be
      * thrown from here even though the handlers we will be calling have all already
      * succeeded in callGenericHandlers.
      */
    private void callGenericHandlers_avoidRecursion(int start,
                                                    int end, Direction direction) {
        int i = start;

        if (direction == Direction.OUT) {
            for (; i <= end; i++) {
                switchContext(direction, i);
                Handler handler = (Handler) handlers.get(i);
                
                if (log.isDebugEnabled()) {
                    log.debug("Invoking handleMessage on: " + handler.getClass().getName());
                }
                callHandleMessageWithTracker(handler);
            }
        } else { // IN case
            for (; i >= end; i--) {
                switchContext(direction, i);
                Handler handler = (Handler) handlers.get(i);
               
                if (log.isDebugEnabled()) {
                    log.debug("Invoking handleMessage on: " + handler.getClass().getName());
                }
                callHandleMessageWithTracker(handler);
            }
        }
    }


    /**
     * Calls handleMessage on the Handler. If an exception is thrown and a response is expected, the
     * MessageContext is updated with the handler information
     *
     * @returns SUCCESSFUL if successfully, UNSUCCESSFUL if false, EXCEPTION if exception thrown
     */
    private int handleMessage(Handler handler, Direction direction,
                              boolean expectResponse) throws RuntimeException {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Invoking handleMessage on: " + handler.getClass().getName()); 
            }
            
            // The pre and post invokers will likely need more than just the handler message context.
            // They may need access to the axis service object or description objects.
            currentMC.put(Constants.MEP_CONTEXT, mepCtx);

            getPreInvoker().preInvoke(currentMC);
            boolean success = callHandleMessageWithTracker(handler);
            getPostInvoker().postInvoke(currentMC);
            if (success) {
                if (log.isDebugEnabled()) {
                    log.debug("handleMessage() returned true");
                }
                return SUCCESSFUL;
            }
            else {
                if (log.isDebugEnabled()) {
                    log.debug("handleMessage() returned false");
                }
                if (expectResponse)
                    currentMC.put(javax.xml.ws.handler.MessageContext.MESSAGE_OUTBOUND_PROPERTY,
                                    (direction != Direction.OUT));
                return FAILED;
            }
        } 
        catch (RuntimeException re) { 
            // RuntimeException and ProtocolException
            if(log.isDebugEnabled()) {
               log.debug("An exception was thrown during the handleMessage() invocation");
               log.debug("Exception: ", re);
            }
            
            savedException = re;
            if (expectResponse)
                // mark it as reverse direction
                currentMC.put(javax.xml.ws.handler.MessageContext.MESSAGE_OUTBOUND_PROPERTY,
                                (direction != Direction.OUT));
            if (ProtocolException.class.isAssignableFrom(re.getClass())) {
                convertToFaultMessage(mepCtx, re, proto, true);
                // just re-initialize the current handler message context since
                // that will pick up the now-changed message
                return PROTOCOL_EXCEPTION;
            }
            return OTHER_EXCEPTION;
        }

    }


    /*
      * start and end should be INclusive of the handlers that have already been
      * invoked on Handler.handleMessage or Handler.handleFault
      */
    private void callCloseHandlers(int start, int end,
                                   Direction direction) {
        int i = start;

        if (direction == Direction.OUT) {
            for (; i <= end; i++) {
                try {
                    switchContext(direction, i);
                    Handler handler = (Handler) handlers.get(i);
                    if (log.isDebugEnabled()) {
                        log.debug("Invoking close on: " + handler.getClass().getName());
                    }
                    callCloseWithTracker(handler);
                    
                    // TODO when we close, are we done with the handler instance, and thus
                    // may call the PreDestroy annotated method?  I don't think so, especially
                    // if we've cached the handler list somewhere.
                } catch (Exception e) {
                    if (log.isDebugEnabled()) {
                        log.debug("An Exception occurred while calling handler.close()");
                        log.debug("Exception: " + e.getClass().getName() + ":" + e.getMessage());
                    }
                }
            }
        } else { // IN case
            for (; i >= end; i--) {
                try {
                    switchContext(direction, i);
                    Handler handler = (Handler) handlers.get(i);
                    if (log.isDebugEnabled()) {
                        log.debug("Invoking close on: " + handler.getClass().getName());
                    }
                    callCloseWithTracker(handler);
                    
                    // TODO when we close, are we done with the handler instance, and thus
                    // may call the PreDestroy annotated method?  I don't think so, especially
                    // if we've cached the handler list somewhere.
                } catch (Exception e) {
                    if (log.isDebugEnabled()) {
                        log.debug("An Exception occurred while calling handler.close()");
                        log.debug("Exception: " + e.getClass().getName() + ":" + e.getMessage());
                    }
                }
            }
        }
    }

    /*
     * processFault is available for a server to use when the endpoint
      * throws an exception or a client when it gets a fault response message
      *
      * In both cases, all of the handlers have run successfully in the
      * opposite direction as this call to callHandleFault, and thus
      * should be closed.
      */
    public void processFault(MEPContext mepCtx, Direction direction) {

        // direction.IN = client
        // direction.OUT = server
        if (handlers.size() == 0)
            return;

        this.mepCtx = mepCtx;
		sortChain();
        initContext(direction);
		currentMC.put(javax.xml.ws.handler.MessageContext.MESSAGE_OUTBOUND_PROPERTY, (direction == Direction.OUT));

        try {
            if (direction == Direction.OUT) {
                callGenericHandleFault(0, handlers.size() - 1, direction);
            } else { // IN case
                callGenericHandleFault(handlers.size() - 1, 0, direction);
            }
        } catch (RuntimeException re) {
            if (log.isDebugEnabled()) {
                log.debug("An exception occurred during handleFault chain processing", re);
            }
            throw re;
        } finally {
            // we can close all the Handlers in reverse order
            if (direction == Direction.OUT) {
                initContext(Direction.IN);
                callCloseHandlers(0, handlers.size() - 1, Direction.OUT);
            } else { // IN case
                initContext(Direction.IN);
                callCloseHandlers(handlers.size() - 1, 0, Direction.IN);
            }
        }
    }


    /*
      * The callGenericHandleFault caller is responsible for closing any invoked
      * Handlers.  We don't know how far the Handler.handleMessage calls got
      * before a failure may have occurred.
      *
      * Regardless of the Handler.handleFault result, the flow is the same (9.3.2.2)
      */
    private void callGenericHandleFault(int start, int end,
                                        Direction direction) throws RuntimeException {

        int i = start;
        
        // we may be starting in the middle of the list, and therefore may need to switch contexts
        switchContext(direction, i);

        if (direction == Direction.OUT) {
            for (; i <= end; i++) {
                Handler handler = (Handler) handlers.get(i);
                if (log.isDebugEnabled()) {
                    log.debug("Invoking handleFault on: " + handler.getClass().getName());
                }
                boolean success = callHandleFaultWithTracker(handler);

                if (!success)
                    break;
                switchContext(direction, i + 1);
            }
        } else { // IN case
            for (; i >= end; i--) {
                Handler handler = (Handler) handlers.get(i);
                if (log.isDebugEnabled()) {
                    log.debug("Invoking handleFault on: " + handler.getClass().getName());
                }
                boolean success = callHandleFaultWithTracker(handler);

                if (!success)
                    break;
                switchContext(direction, i - 1);
            }
        }
    }

    public static void convertToFaultMessage(MEPContext mepCtx, Exception e, Protocol protocol) {
        convertToFaultMessage(mepCtx, e, protocol, false);
    }
    
    /**
     * Converts the Exception into an XML Fault Message that is stored on the MEPContext.
     * Note that if the forceConversion flag is true, this conversion will always occur.
     * If the checkMsg flag is true, this conversion only occurs if the Message is not already
     * a Fault (per 9,3,2.1 of the JAX-WS specification)
     * 
     * @param mepCtx  MEPContext
     * @param e Exception
     * @param protocol Protocol
     * @param forceConversion  If true, the Exception is always converted to a Message
     */
    public static void convertToFaultMessage(MEPContext mepCtx, 
            Exception e, 
            Protocol protocol, 
            boolean checkMsg) {

        // need to check if message is already a fault message or not,
        // probably by way of a flag (isFault) in the MessageContext or Message
        if (log.isDebugEnabled()) {
            log.debug("start convertToFaultMessge with exception: " + e.getClass().getName());
            log.debug(" checkMsg is : " + checkMsg);
        }
           
        try {
            // According to the 9.3.2.1, The message is converted into a fault only if it is not already a Fault
            Message messageFromHandler = null; 
            if (checkMsg) {
                messageFromHandler = mepCtx.getMessageContext().getMessage();
            } 
            if (messageFromHandler != null && messageFromHandler.isFault()) {
                if (log.isDebugEnabled()) {
                    log.debug("The Message is already a SOAPFault.  The exception is not converted into a Message");
                }
            } else if (protocol == Protocol.soap11 || protocol == Protocol.soap12) {
                if (log.isDebugEnabled()) {
                    log.debug("Converting Exception into a Message");
                }
                String protocolNS = (protocol == Protocol.soap11) ?
                        SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE :
                        SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE;

                // The following set of instructions is used to avoid 
                // some unimplemented methods in the Axis2 SAAJ implementation
                XMLFault xmlFault = MethodMarshallerUtils.createXMLFaultFromSystemException(e);
                javax.xml.soap.MessageFactory mf = SAAJFactory.createMessageFactory(protocolNS);
                SOAPMessage message = mf.createMessage();
                SOAPBody body = message.getSOAPBody();
                SOAPFault soapFault = XMLFaultUtils.createSAAJFault(xmlFault, body);

                MessageFactory msgFactory = (MessageFactory) FactoryRegistry.getFactory(MessageFactory.class);
                Message msg = msgFactory.createFrom(message);
                mepCtx.setMessage(msg);

            } else {
                WebServiceException wse = ExceptionFactory.makeWebServiceException(Messages.getMessage("cFaultMsgErr"));
                if (log.isDebugEnabled()) {
                    log.debug("end convertToFaultMessge due to error ", wse);
                }
                throw wse;
            }

        } catch (Exception ex) {
            WebServiceException wse =ExceptionFactory.makeWebServiceException(ex);
            if (log.isDebugEnabled()) {
                log.debug("end convertToFaultMessge due to error ", wse);
            }
            throw wse;
        }

    }


    private void initContext(Direction direction) {
        soapMC = MessageContextFactory.createSoapMessageContext(mepCtx.getMessageContext());
        logicalMC = MessageContextFactory.createLogicalMessageContext(mepCtx.getMessageContext());
        if (direction == Direction.OUT) {
            // logical context, then SOAP
            if ((logicalLength == 0) && (handlers.size() > 0)) // we only have soap handlers
                currentMC = soapMC; //MessageContextFactory.createSoapMessageContext(mepCtx.getMessageContext());
            else
                currentMC = logicalMC; //MessageContextFactory.createLogicalMessageContext(mepCtx.getMessageContext());
        } else {
            // SOAP context, then logical
            if ((logicalLength == handlers.size()) && (handlers.size() > 0)) // we only have logical handlers
                currentMC = logicalMC; //MessageContextFactory.createLogicalMessageContext(mepCtx.getMessageContext());
            else
                currentMC = soapMC; //MessageContextFactory.createSoapMessageContext(mepCtx.getMessageContext());
        }
    }

    private void switchContext(Direction direction, int index) {

        if ((logicalLength == handlers.size()) || (logicalLength == 0))
            return; // all handlers must be the same type, so no context switch

        if (((direction == Direction.OUT) && (index == logicalLength))
                || ((direction == Direction.IN) && (index == (logicalLength - 1)))) {
            //if (currentMC.getClass().isAssignableFrom(LogicalMessageContext.class))
            if (currentMC == logicalMC)  // object check, not .equals()
                currentMC = soapMC; //MessageContextFactory.createSoapMessageContext(mepCtx.getMessageContext());
            else
                currentMC = logicalMC; //MessageContextFactory.createLogicalMessageContext(mepCtx.getMessageContext());
        }
    }
    
    private HandlerPreInvoker getPreInvoker() {
    	if (handlerPreInvoker == null) {
    		HandlerPreInvokerFactory preInvokerFactory = (HandlerPreInvokerFactory)FactoryRegistry.getFactory(HandlerPreInvokerFactory.class);
    		handlerPreInvoker = (HandlerPreInvoker)preInvokerFactory.createHandlerPreInvoker();
    	}
    	return handlerPreInvoker;
    }
    
    private HandlerPostInvoker getPostInvoker() {
    	if (handlerPostInvoker == null) {
    		HandlerPostInvokerFactory postInvokerFactory = (HandlerPostInvokerFactory)FactoryRegistry.getFactory(HandlerPostInvokerFactory.class);
    		handlerPostInvoker = (HandlerPostInvoker)postInvokerFactory.createHandlerPostInvoker();
    	}
    	return handlerPostInvoker;
    }
    
    public static void trackInternalCall(org.apache.axis2.jaxws.core.MessageContext mc, TRACKER tracker) {
        switch (tracker) {
        case SAAJ_CALLED:
            saaj_called = true;
            break;
        case SOAP_HEADERS_ADAPTER_CALLED:
            soap_headers_adapter_called = true;
            break;
        }
        Object trackerProp = (mc == null ? null : mc.getProperty(org.apache.axis2.jaxws.handler.Constants.JAXWS_HANDLER_TRACKER));
        if ((trackerProp != null) && ((Boolean)trackerProp).booleanValue()) {
            if (saaj_called && soap_headers_adapter_called) {
                // this means both the SAAJ model and SOAPHeadersAdapter code has been called in such a
                // way as to cause data transformation.  We want customers to avoid doing this (calling both)
                // in a given handler method, so we throw an exception:
                if (log.isDebugEnabled()) {
                    String logString = "JAX-WS Handler implementations should not use the " + org.apache.axis2.jaxws.handler.Constants.JAXWS_HANDLER_TRACKER 
                    + " property to retrieve SOAP headers and manipulate the SOAP message using the SAAJ API in the same method "
                    + "implementation.  The Handler implementation and method doing this is: "
                    + currentHandler.getClass().getName() + currentMethod;
                    log.debug(logString);
                }
            }
        }
    }
    
    private boolean callHandleMessageWithTracker(Handler handler) throws RuntimeException {
        currentHandler = handler;
        currentMethod = "handleMessage";
        // turn on the tracker property
        currentMC.put(org.apache.axis2.jaxws.handler.Constants.JAXWS_HANDLER_TRACKER, true);
        boolean success = false;
        RuntimeException savedEx = null;
        try {
            success = handler.handleMessage(currentMC);
        } catch (RuntimeException t) {
            savedEx = t;
        }
        // turn off the tracker property and reset the static tracker booleans
        currentMC.put(org.apache.axis2.jaxws.handler.Constants.JAXWS_HANDLER_TRACKER, false);
        saaj_called = false;
        soap_headers_adapter_called = false;
        
        // If the handler changed the SOAPPart or Attachments, then we need
        // that the Message gets updated
        if (currentMC instanceof SoapMessageContext){
            ((SoapMessageContext)currentMC).checkAndUpdate();
        }
        
        if (savedEx != null) {
            throw savedEx;
        }
        return success;
    }
    
    private boolean callHandleFaultWithTracker(Handler handler) {
        currentHandler = handler;
        currentMethod = "handleFault";
        // turn on the tracker property
        currentMC.put(org.apache.axis2.jaxws.handler.Constants.JAXWS_HANDLER_TRACKER, true);
        boolean success = false;
        RuntimeException savedEx = null;
        try {
            success = handler.handleFault(currentMC);
        } catch (RuntimeException t) {
            savedEx = t;
        }
        // turn off the tracker property and reset the static tracker booleans
        currentMC.put(org.apache.axis2.jaxws.handler.Constants.JAXWS_HANDLER_TRACKER, false);
        saaj_called = false;
        soap_headers_adapter_called = false;
        
        // If the handler changed the SOAPPart or Attachments, then we need
        // that the Message gets updated
        if (currentMC instanceof SoapMessageContext){
            ((SoapMessageContext)currentMC).checkAndUpdate();
        }
        
        if (savedEx != null) {
            throw savedEx;
        }
        return success;
    }
    
    private void callCloseWithTracker(Handler handler) {
        currentHandler = handler;
        currentMethod = "close";
        // turn on the tracker property
        currentMC.put(org.apache.axis2.jaxws.handler.Constants.JAXWS_HANDLER_TRACKER, true);
        RuntimeException savedEx = null;
        try {
            handler.close(currentMC);
        } catch (RuntimeException t) {
            savedEx = t;
        }
        // turn off the tracker property and reset the static tracker booleans
        currentMC.put(org.apache.axis2.jaxws.handler.Constants.JAXWS_HANDLER_TRACKER, false);
        saaj_called = false;
        soap_headers_adapter_called = false;
        if (savedEx != null) {
            throw savedEx;
        }
    }

}
