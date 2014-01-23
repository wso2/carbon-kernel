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

package org.apache.axis2.description;

import org.apache.axiom.util.UIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.i18n.Messages;

import javax.xml.namespace.QName;
import java.util.HashMap;

public class InOutAxisOperation extends TwoChannelAxisOperation {

    public InOutAxisOperation() {
        super();
        //setup a temporary name
        QName tmpName = new QName(this.getClass().getName() + "_" + UIDGenerator.generateUID());
        this.setName(tmpName);
    }

    public InOutAxisOperation(QName name) {
        super(name);
    }

    public OperationClient createClient(ServiceContext sc, Options options) {
        throw new UnsupportedOperationException(
                Messages.getMessage("mepnotyetimplemented", mepURI));
    }

    public void addMessageContext(MessageContext msgContext, OperationContext opContext)
            throws AxisFault {
        HashMap mep = opContext.getMessageContexts();
        MessageContext inMsgContext = (MessageContext) mep.get(MESSAGE_LABEL_IN_VALUE);
        MessageContext outmsgContext = (MessageContext) mep.get(MESSAGE_LABEL_OUT_VALUE);

        if ((inMsgContext != null) && (outmsgContext != null)) {
            throw new AxisFault(Messages.getMessage("mepcompleted"));
        }

        if (inMsgContext == null) {
            mep.put(MESSAGE_LABEL_IN_VALUE, msgContext);
        } else {
            mep.put(MESSAGE_LABEL_OUT_VALUE, msgContext);
            opContext.setComplete(true);
            opContext.cleanup();
        }
    }
}
