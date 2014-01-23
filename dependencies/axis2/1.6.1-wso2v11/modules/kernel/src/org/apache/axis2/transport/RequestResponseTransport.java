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

package org.apache.axis2.transport;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;

/**
 * This interface represents a control object for a Request/Response transport.
 * The normal flow of Axis2 is rooted at the transport -- this does not
 * allow for an acknowledgement to be transmitted before processing has
 * completed, nor does it allow for processing to be paused and resumed
 * on a separate thread without having a response be sent back.  This interface
 * enables both of those scenarios by allowing the transport to expose
 * controls to the rest of the engine via a callback.
 */
public interface RequestResponseTransport {

    /*This is the name of the property that is to be stored on the
    MessageContext*/
    public static final String TRANSPORT_CONTROL
            = "RequestResponseTransportControl";

    /**
     * If this property is set to true in a message transport will call the awaitResponse method
     * of the RequestResponseTransport instead of returning. The value should be a Boolean object.
     */
    public static final String HOLD_RESPONSE = "HoldResponse";

    /**
     * Notify the transport that a message should be acknowledged at this time.
     *
     * @param msgContext
     * @throws AxisFault
     */
    public void acknowledgeMessage(MessageContext msgContext) throws AxisFault;

    /**
     * Pause execution and wait for a response message to be ready.  This will
     * typically be called by the transport after a message has been paused and
     * will cause the transport to block until a response message is ready to be
     * returned.  This is required to enable RM for in-out MEPs over a
     * request/response transport; without it the message would be paused and the
     * transport would simply ack the request.
     *
     * @throws InterruptedException
     */
    public void awaitResponse() throws InterruptedException, AxisFault;

    /**
     * Signal that a response has be created and is ready for transmission.  This
     * should release anyone who is blocked on a awaitResponse().
     */
    public void signalResponseReady();

    /**
     * This will tell the transport to end a current wait by raising the given fault.
     * @param fault The fault to be raised.
     */
    public void signalFaultReady(AxisFault fault);
    
    /**
     * This gives the current status of an RequestResponseTransport object.
     *
     * @return
     */
    public RequestResponseTransportStatus getStatus();
    
    /**
     * This will indicate whether or not the response has already been written
     */
    public boolean isResponseWritten();
    
    /**
     * This is used to set the response written flag on the RequestResponseTransport 
     * instance
     */
    public void setResponseWritten(boolean responseWritten);
    

    /**
     * Used to give the current status of the RequestResponseTransport object.
     */
    public class RequestResponseTransportStatus {
        /**
         * Transport is in its initial stage.
         */
        public static RequestResponseTransportStatus INITIAL =
                new RequestResponseTransportStatus(1);

        /**
         * awaitResponse has been called.
         */
        public static RequestResponseTransportStatus WAITING =
                new RequestResponseTransportStatus(2);

        /**
         * acknowledgeMessage has been called.
         */
        public static RequestResponseTransportStatus ACKED = 
                new RequestResponseTransportStatus (3);
        /**
         * 'signalResponseReady' has been called.
         */
        public static RequestResponseTransportStatus SIGNALLED =
                new RequestResponseTransportStatus(4);

        private int value;

        private RequestResponseTransportStatus(int value) {
            this.value = value;
        }

        public int hashCode() {
            return value;
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof RequestResponseTransportStatus)) {
                return false;
            }
            final RequestResponseTransportStatus instance = (RequestResponseTransportStatus) obj;
            return (value == instance.value);
        }

        public String toString() {
            return Integer.toString(value);
        }

    }

}
