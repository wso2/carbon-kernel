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

package org.apache.axis2.soap12testing.handlers;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;

import java.util.Iterator;

public class SOAP12OutFlowHandlerDefault extends AbstractHandler {


    public void revoke(MessageContext msgContext) {

    }

    public InvocationResponse invoke(MessageContext msgContext) {
        Integer headerBlockPresent = (Integer) msgContext.getOperationContext().getProperty("HEADER_BLOCK_PRESENT");
        if (headerBlockPresent.equals(new Integer(1))) {
            SOAPHeader headerAdd = (SOAPHeader) msgContext.getOperationContext().getProperty("HEADER_BLOCK");
           Iterator headerBlocks = headerAdd.examineAllHeaderBlocks();
            while(headerBlocks.hasNext()){
                SOAPHeaderBlock headerBlock=(SOAPHeaderBlock) headerBlocks.next();
                msgContext.getEnvelope().getHeader().addChild(headerBlock);
            }
        } else {
            msgContext.getEnvelope().getHeader().discard();
        }
        return InvocationResponse.CONTINUE;
    }
}
