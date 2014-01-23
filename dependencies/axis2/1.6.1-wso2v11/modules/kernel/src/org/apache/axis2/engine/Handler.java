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

/**
 * A Handler represents a piece of message processing functionality in Axis2.
 *
 * Handlers are combined into chains and phases in order to provide customizable functionality
 * such as security, reliability, etc.  Handlers must be multi-thread safe and should keep all
 * their state in Context objects (see the org.apache.axis2.context package).
 */
public interface Handler {

    /**
     * @deprecated This method will be going away after the 1.3 release, it was never used.
     */
    public void cleanup();

    /**
     * Initialize a Handler.
     *
     * @param handlerDesc the HandlerDescription for this Handler
     */
    public void init(HandlerDescription handlerDesc);

    /**
     * This method will be called on each registered handler when a message
     * needs to be processed.  If the message processing is paused by the
     * handler, then this method will be called again for the handler that
     * paused the processing once it is resumed.
     * <p/>
     * This method may be called concurrently from multiple threads.
     * <p/>
     * Handlers that want to determine the type of message that is to be
     * processed (e.g. response vs request, inbound vs. outbound, etc.) can
     * retrieve that information from the MessageContext via
     * MessageContext.getFLOW() and
     * MessageContext.getAxisOperation().getMessageExchangePattern() APIs.
     *
     * @param msgContext the <code>MessageContext</code> to process with this
     *                   <code>Handler</code>.
     * @return An InvocationResponse that indicates what
     *         the next step in the message processing should be.
     * @throws AxisFault if the handler encounters an error
     */
    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault;

    /**
     * This method will be called on each registered handler that had its
     * invoke(...) method called during the processing of the message, once
     * the message processing has completed.  During execution of the
     * flowComplete's, handlers are invoked in the opposite order that they
     * were invoked originally.  Note that implementations SHOULD check
     * msgContext.getFailureReason() to see if this is an error or a normal
     * completion.
     *
     * @param msgContext the <code>MessageContext</code> to process with this
     *                   <code>Handler</code>.
     */
    public void flowComplete(MessageContext msgContext);

    /**
     * Gets the HandlerDescription of a handler.
     *
     * @return Returns HandlerDescription.
     */
    public HandlerDescription getHandlerDesc();

    /**
     * Return the name of this Handler
     *
     * @return the handler's name as a String
     */
    public String getName();

    /**
     * Get a Parameter from this Handler
     *
     * @param name the name of the desired value
     * @return the Parameter, or null.
     */
    public Parameter getParameter(String name);

    /**
     * This type encapsulates an enumeration of possible message processing
     * instruction values that may be returned by a handler/phase within the
     * runtime.  The returned instruction will determine the next step in
     * the processing.
     */
    public final class InvocationResponse {
        public static final InvocationResponse CONTINUE =
                new InvocationResponse(0, "InvocationResponse.CONTINUE");
        public static final InvocationResponse SUSPEND =
                new InvocationResponse(1, "InvocationResponse.SUSPEND");
        public static final InvocationResponse ABORT =
                new InvocationResponse(2, "InvocationResponse.ABORT");

        private final int instructionID;
        private final String description;

        private InvocationResponse(int instructionID, String description) {
            this.instructionID = instructionID;
            this.description = description;
        }

        public int hashCode() {
            return instructionID;
        }

        public boolean equals(Object obj) {
        	if(this==obj) {
        		return true;
        	}
            if (!(obj instanceof InvocationResponse)) {
                return false;
            }
            final InvocationResponse instance = (InvocationResponse) obj;
            return (instructionID == instance.instructionID);
        }

        public String toString() {
            return description;
        }
    }
}
