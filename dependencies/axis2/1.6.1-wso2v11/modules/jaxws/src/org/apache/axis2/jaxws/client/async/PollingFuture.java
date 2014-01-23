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

package org.apache.axis2.jaxws.client.async;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.async.AxisCallback;
import org.apache.axis2.jaxws.core.InvocationContext;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.ws.WebServiceException;

public class PollingFuture implements AxisCallback {

    private static final Log log = LogFactory.getLog(PollingFuture.class);

    private AsyncResponse response;
    private InvocationContext invocationCtx;

    public PollingFuture(InvocationContext ic) {
        response = ic.getAsyncResponseListener();

        /*
        * TODO review.  We need to save the invocation context so we can set it on the
        * response (or fault) context so the FutureCallback has access to the handler list.
        */
        invocationCtx = ic;
    }

    public void onComplete(org.apache.axis2.context.MessageContext msgContext) {
        boolean debug = log.isDebugEnabled();
        if (debug) {
            log.debug("JAX-WS async response listener received the response");
        }

        MessageContext responseMsgCtx = null;
        try {
            responseMsgCtx = AsyncUtils.createJAXWSMessageContext(msgContext);
            responseMsgCtx.setInvocationContext(invocationCtx);
            // make sure request and response contexts share a single parent
            responseMsgCtx.setMEPContext(invocationCtx.getRequestMessageContext().getMEPContext());
        } catch (WebServiceException e) {
            response.onError(e, null);
            if (debug) {
                log.debug(
                        "An error occured while processing the async response.  " + e.getMessage());
            }
        }

        if (response == null) {
            // TODO: throw an exception
        }

        response.onComplete(responseMsgCtx);
    }


    public void onError(Exception e) {
        // If a SOAPFault was returned by the AxisEngine, the AxisFault
        // that is returned should have a MessageContext with it.  Use
        // this to unmarshall the fault included there.
        if (e.getClass().isAssignableFrom(AxisFault.class)) {
            AxisFault fault = (AxisFault)e;
            MessageContext faultMessageContext = null;
            try {
                faultMessageContext =
                        AsyncUtils.createJAXWSMessageContext(fault.getFaultMessageContext());
                faultMessageContext.setInvocationContext(invocationCtx);
                // make sure request and response contexts share a single parent
                faultMessageContext.setMEPContext(invocationCtx.getRequestMessageContext().getMEPContext());
            } catch (WebServiceException wse) {
                response.onError(wse, null);
            }

            response.onError(e, faultMessageContext);
        } else {
            response.onError(e, null);
        }
    }

    public void onMessage(org.apache.axis2.context.MessageContext msgContext) {
        onComplete(msgContext);

    }

    public void onFault(org.apache.axis2.context.MessageContext msgContext) {
        onComplete(msgContext);
    }

    public void onComplete() {

    }

}

