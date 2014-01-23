/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.axis2.transport.mail;

import org.apache.axis2.transport.RequestResponseTransport;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.AxisFault;

/**
 * this class is not generally used with the SMTP transport. this is added to work
 * this smtp transport with Sandesah2. 
 */

public class MailRequestResponseTransport implements RequestResponseTransport {

    RequestResponseTransportStatus status = RequestResponseTransportStatus.WAITING;

    public void acknowledgeMessage(MessageContext messageContext) throws AxisFault {
    }

    public void awaitResponse() throws InterruptedException, AxisFault {
    }

    public void signalResponseReady() {
    }

    public void signalFaultReady(AxisFault axisFault) {
    }

    public RequestResponseTransportStatus getStatus() {
        return status;
    }

    public boolean isResponseWritten() {
        return false;
    }

    public void setResponseWritten(boolean b) {
    }
}
