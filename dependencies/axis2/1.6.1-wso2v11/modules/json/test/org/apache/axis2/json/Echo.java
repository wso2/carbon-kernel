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

package org.apache.axis2.json;

import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMSourcedElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.wsdl.WSDLConstants;

public class Echo {

    public Echo() {
    }

    public OMElement echoOM(OMElement omEle) throws AxisFault {
        MessageContext outMsgCtx = MessageContext.getCurrentMessageContext().getOperationContext().getMessageContext(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
        Object object = outMsgCtx.getProperty(Constants.Configuration.MESSAGE_TYPE);
        String messageType = (String) object;

        //if the request is through GET, the message type is application/xml. otherwise don't allow
        //any non json specific message types
        if (messageType.equalsIgnoreCase(HTTPConstants.MEDIA_TYPE_APPLICATION_XML)) {
            outMsgCtx.setProperty(Constants.Configuration.MESSAGE_TYPE, "application/json");
        } else if (messageType.indexOf("json") < 0) {
            throw new AxisFault("Type of the Received Message is not JSON");
        }
        OMDataSource omdataOSuce = ((OMSourcedElement) omEle).getDataSource();
        OMElement newOmEle = (OMElement) omEle.detach();
        ((OMSourcedElement) newOmEle).setDataSource(omdataOSuce);
        return omEle;
    }
}
