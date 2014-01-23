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

package org.apache.axis2.saaj.util;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.handlers.AbstractHandler;

import java.util.Iterator;

/**
 * Marks all SOAP headers as processed.
 */
public class UnderstandAllHeadersHandler extends AbstractHandler {

    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
        if (msgContext == null) {
            return InvocationResponse.CONTINUE;
        } 
                
        SOAPEnvelope envelope = msgContext.getEnvelope();
        if (envelope.getHeader() == null) {
            return InvocationResponse.CONTINUE;          
        }
        
        // Passing in null will get headers targeted for NEXT and ULTIMATE RECEIVER
        Iterator headerBlocks = envelope.getHeader().getHeadersToProcess(null);
        while (headerBlocks.hasNext()) {
            SOAPHeaderBlock headerBlock = (SOAPHeaderBlock) headerBlocks.next();
            headerBlock.setProcessed();            
        }
        return InvocationResponse.CONTINUE;        
    }
  
}
