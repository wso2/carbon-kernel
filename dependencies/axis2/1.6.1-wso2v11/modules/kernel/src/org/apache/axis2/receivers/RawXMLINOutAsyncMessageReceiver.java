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

package org.apache.axis2.receivers;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;

/**
 * The RawXMLINOutAsyncMessageReceiver MessageReceiver hands over the raw request received to
 * the service implementation class as an OMElement. The implementation class is expected
 * to return back the OMElement to be returned to the caller. This is an asynchronous
 * MessageReceiver, and finds the service implementation class to invoke by referring to
 * the "ServiceClass" parameter value specified in the service.xml and looking at the
 * methods of the form OMElement <<methodName>>(OMElement request)
 *
 * @see RawXMLINOnlyMessageReceiver
 * @see RawXMLINOutMessageReceiver
 * @deprecated use RawXMLINOutMessageReceiver and the DO_ASYNC property instead....
 */
public class RawXMLINOutAsyncMessageReceiver extends RawXMLINOutMessageReceiver {
    public void receive(final MessageContext messageCtx) throws AxisFault {
        messageCtx.setProperty(DO_ASYNC, Boolean.TRUE);
        super.receive(messageCtx);
    }
}
