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


package org.apache.axis2.engine;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.PhaseRule;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.phaseresolver.PhaseException;
import org.apache.axis2.util.LoggingControl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A Phase is an ordered collection of Handlers.
 */
public class Phase implements Handler {

    public static final String ALL_PHASES = "*";

    /**
     * Field log
     */
    private static final Log log = LogFactory.getLog(Phase.class);
    private static boolean isDebugEnabled = LoggingControl.debugLoggingAllowed && log.isDebugEnabled();

    /**
     * Field handlers
     */
    private List<Handler> handlers;

    /**
     * A handler has been marked as present in both the first phase and the last phase
     */
    private boolean isOneHandler;

    /**
     * Field phaseName
     */
    private String phaseName;

    /**
     * Field phaseFirstSet
     */
    private boolean phaseFirstSet;

    /**
     * Field phaseLastSet
     */
    private boolean phaseLastSet;

    /**
     * Default constructor
     */
    public Phase() {
        this(null);
    }

    /**
     * Create a named Phase
     *
     * @param phaseName the name for this Phase
     */
    public Phase(String phaseName) {
        handlers = new CopyOnWriteArrayList<Handler>();
        this.phaseName = phaseName;
    }

    /**
     * Add a handler to the Phase.
     *
     * @param handler the Handler to add
     */
    public void addHandler(Handler handler) {
        log.debug("Handler " + handler.getName() + " added to Phase " + phaseName);

        if (phaseLastSet) {
            // handlers.size() can not be 0 , since when setting phase last it is always > 0
            if (handlers.size() == 1) {
                handlers.add(0, handler);
            } else {
                handlers.add(handlers.size() - 2, handler);
            }
        } else {
            handlers.add(handler);
        }
    }

    /**
     * Add a HandlerDescription to the Phase
     *
     * @param handlerDesc the HandlerDescription to add
     * @throws PhaseException if there is a problem
     */
    public void addHandler(HandlerDescription handlerDesc) throws PhaseException {
        Iterator<Handler> handlers_itr = getHandlers().iterator();

        while (handlers_itr.hasNext()) {
            Handler hand = (Handler) handlers_itr.next();
            HandlerDescription thisDesc = hand.getHandlerDesc();
            if (handlerDesc.getName().equals(thisDesc.getName())) {
                return;
            }
        }

        if (isOneHandler) {
            throw new PhaseException("Phase '" + this.getPhaseName()
                    + "' can only have one handler, since there is a "
                    + "handler with both phaseFirst and phaseLast true ");
        }

        if (handlerDesc.getRules().isPhaseFirst() && handlerDesc.getRules().isPhaseLast()) {
            if (!handlers.isEmpty()) {
                throw new PhaseException(this.getPhaseName()
                        + " already contains Handlers, and "
                        + handlerDesc.getName()
                        + " cannot therefore be both phaseFirst and phaseLast.");
            } else {
                handlers.add(handlerDesc.getHandler());
                isOneHandler = true;
            }
        } else if (handlerDesc.getRules().isPhaseFirst()) {
            setPhaseFirst(handlerDesc.getHandler());
        } else if (handlerDesc.getRules().isPhaseLast()) {
            setPhaseLast(handlerDesc.getHandler());
        } else {
            insertHandler(handlerDesc);
        }
    }

    /**
     * Add a Handler at a particular index within the Phase.
     *
     * If we have a Phase with (H1, H2), calling addHandler(H3, 1) will result in (H1, H3, H2)
     *
     * @param handler the Handler to add
     * @param index the position in the Phase at which to place the Handler
     */
    public void addHandler(Handler handler, int index) {
        if (log.isDebugEnabled()) {
            log.debug("Handler " + handler.getName() + " inserted at position " + index +
                    " of Phase " + phaseName);
        }
        handlers.add(index, handler);
    }

    /**
     * Confirm that all post-conditions of this Phase are met.  After all Handlers in a
     * Phase are invoke()d, this method will be called.  Subclasses should override it in order
     * to confirm that the purpose of the given Phase has been acheived.
     *
     * @param msgContext the active MessageContext
     * @throws AxisFault if a post-condition has not been met, or other problems occur
     */
    public void checkPostConditions(MessageContext msgContext) throws AxisFault {
        // Default version does nothing
    }

    /**
     * Check the preconditions for a Phase.  This method will be called when the Phase is
     * invoked, BEFORE any Handlers are invoked.  Subclasses should override it in order
     * to confirm that necessary preconditions are met before the Phase does its work.  They
     * should throw an appropriate AxisFault if not.
     *
     * @param msgContext the active MessageContext
     * @throws AxisFault if a precondition is not met, or in case of other problem
     */
    public void checkPreconditions(MessageContext msgContext) throws AxisFault {
        // Default version does nothing
    }

    public void cleanup() {
        // Default version does nothing
    }

    public void init(HandlerDescription handlerdesc) {
        // Default version does nothing
    }

    private void insertHandler(HandlerDescription handlerDesc) throws PhaseException {
        Handler handler = handlerDesc.getHandler();
        PhaseRule rules = handler.getHandlerDesc().getRules();
        String beforeName = rules.getBefore();
        String afterName = rules.getAfter();

        // If we don't care where it goes, tack it on at the end
        if (beforeName == null && afterName == null) {
            addHandler(handler);
            return;
        }

        // Otherwise walk the list and find the right place to put it
        int beforeIndex = -1, afterIndex = -1;

        for (int i = 0; i < handlers.size(); i++) {
            Handler tempHandler = (Handler) handlers.get(i);

            if ((beforeName != null) && (beforeIndex == -1)) {
                if (tempHandler.getName().equals(beforeName)) {
                    // Found the "before" handler
                    beforeIndex = i;
                }
            }

            if ((afterName != null) && (afterIndex == -1)) {
                if (tempHandler.getName().equals(afterName)) {
                    // Found the "after" handler
                    afterIndex = i;
                }
            }
        }

        if ((beforeIndex > -1) && (afterIndex >= beforeIndex)) {
            throw new PhaseException("Can't insert handler because " + beforeName + " is before " +
                    afterName + " in Phase '" + phaseName + "'");
        }

        if (phaseFirstSet && beforeIndex == 0) {
            throw new PhaseException("Can't insert handler before handler '"
                    + beforeName
                    + "', which is marked phaseFirst");
        }

        if (phaseLastSet && afterIndex == (handlers.size() - 1)) {
            throw new PhaseException("Can't insert handler after handler '"
                    + afterName
                    + "', which is marked phaseLast");
        }

        if (beforeIndex > -1) {
            handlers.add(beforeIndex, handler);
        } else if (afterIndex > -1){
            if (phaseLastSet){
                if (handlers.size() ==1){
                    handlers.add(0,handler);
                }  else {
                    handlers.add(handlers.size() -2,handler);
                }
            }  else {
                if (afterIndex == (handlers.size() -1)) {
                    handlers.add(handler);
                } else {
                    handlers.add(afterIndex +1,handler);
                }
            }
        }  else {
            if (phaseLastSet) {
                if (handlers.size() ==1){
                    handlers.add(0,handler);
                }  else {
                    handlers.add(handlers.size() -2,handler);
                }
            }    else {
                handlers.add(handler);
            }
        }
    }

    /**
     * Invoke all the handlers in this Phase
     *
     * @param msgctx the current MessageContext
     * @return An InvocationResponse that indicates what
     *         the next step in the message processing should be.
     * @throws org.apache.axis2.AxisFault
     */
    public final InvocationResponse invoke(MessageContext msgctx) throws AxisFault {
        
        if (isDebugEnabled) {
            log.debug(msgctx.getLogIDString() + " Checking pre-condition for Phase \"" + phaseName +
                    "\"");
        }

        int currentIndex = msgctx.getCurrentPhaseIndex();

        if (currentIndex == 0) {
            checkPreconditions(msgctx);
        }

        if (isDebugEnabled) {
            log.debug(msgctx.getLogIDString() + " Invoking phase \"" + phaseName + "\"");
        }

        int handlersSize = handlers.size();
        
        for (int i= currentIndex; i < handlersSize; i++) {
            Handler handler = (Handler) handlers.get(i);

            InvocationResponse pi = invokeHandler(handler, msgctx);
           
            if (!pi.equals(InvocationResponse.CONTINUE)) {
                return pi;
            }
            
            // Set phase index to the next handler
            msgctx.setCurrentPhaseIndex(i+1);
        }

        if (isDebugEnabled) {
            log.debug(msgctx.getLogIDString() + " Checking post-conditions for phase \"" +
                    phaseName + "\"");
        }

        msgctx.setCurrentPhaseIndex(0);
        checkPostConditions(msgctx);
        return InvocationResponse.CONTINUE;
    }
    
    private InvocationResponse invokeHandler(Handler handler, MessageContext msgctx)
            throws AxisFault  {
        if (isDebugEnabled) {
            log.debug(msgctx.getLogIDString() + " Invoking Handler '" + handler.getName() +
                    "' in Phase '" + phaseName + "'");
        }
        
        return handler.invoke(msgctx);
    }

    public void flowComplete(MessageContext msgContext) {
        if (isDebugEnabled) {
            log.debug(msgContext.getLogIDString() + " Invoking flowComplete() in Phase \"" +
                    phaseName + "\"");
        }

        // This will be non-zero if we failed during execution of one of the
        // handlers in this phase
        int currentHandlerIndex = msgContext.getCurrentPhaseIndex();
        if (currentHandlerIndex == 0) {
            currentHandlerIndex = handlers.size();
        } else {
            /*We need to set it to 0 so that any previous phases will execute all
         * of their handlers.*/
            msgContext.setCurrentPhaseIndex(0);
        }

        for (; currentHandlerIndex > 0; currentHandlerIndex--) {
            Handler handler = (Handler) handlers.get(currentHandlerIndex - 1);

            if (isDebugEnabled) {
                log.debug(msgContext.getLogIDString() + " Invoking flowComplete() for Handler '" +
                        handler.getName() + "' in Phase '" + phaseName + "'");
            }

            handler.flowComplete(msgContext);
        }
    }

    public String toString() {
        return this.getPhaseName();
    }

    public int getHandlerCount() {
        return handlers.size();
    }

    public HandlerDescription getHandlerDesc() {
        return null;
    }

    /**
     * Gets all the handlers in the phase.
     *
     * @return Returns an ArrayList of Handlers
     */
    public List<Handler> getHandlers() {
        return handlers;
    }

    public String getName() {
        return phaseName;
    }

    public Parameter getParameter(String name) {
        return null;
    }

    /**
     * @return Returns the name.
     */
    public String getPhaseName() {
        return phaseName;
    }

    public void setName(String phaseName) {
        this.phaseName = phaseName;
    }

    /**
     * Add a Handler to the Phase in the very first position, and ensure no other Handler
     * will come before it.
     *
     * @param handler the Handler to add
     * @throws PhaseException if another Handler is already set as phaseFirst
     */
    public void setPhaseFirst(Handler handler) throws PhaseException {
        if (phaseFirstSet) {
            throw new PhaseException("PhaseFirst has been set already, cannot have two"
                    + " phaseFirst Handlers for Phase '" + this.getPhaseName() + "'");
        }
        handlers.add(0, handler);
        phaseFirstSet = true;
    }

    /**
     * Add a Handler to the Phase in the very last position, and ensure no other Handler
     * will come after it.
     *
     * @param handler the Handler to add
     * @throws PhaseException if another Handler is already set as phaseLast
     */
    public void setPhaseLast(Handler handler) throws PhaseException {
        if (phaseLastSet) {
            throw new PhaseException("PhaseLast already has been set,"
                    + " cannot have two PhaseLast Handler for same phase "
                    + this.getPhaseName());
        }

        handlers.add(handler);
        phaseLastSet = true;
    }

    /**
     * Remove a given Handler from a phase using a HandlerDescription
     *
     * @param handlerDesc the HandlerDescription to remove
     */
    public void removeHandler(HandlerDescription handlerDesc) {
        if (handlers.remove(handlerDesc.getHandler())) {
            PhaseRule rule = handlerDesc.getRules();
            if (rule.isPhaseFirst()) {
                phaseFirstSet = false;
            }
            if (rule.isPhaseLast()) {
                phaseLastSet = false;
            }
            if (rule.isPhaseFirst() && rule.isPhaseLast()) {
                isOneHandler = false;
            }
            log.debug("removed handler " + handlerDesc.getName()
                    + " from the phase " + phaseName);
        } else {
            log.debug("unable to remove handler " + handlerDesc.getName()
                    + " from the phase " + phaseName);
        }
    }

}
