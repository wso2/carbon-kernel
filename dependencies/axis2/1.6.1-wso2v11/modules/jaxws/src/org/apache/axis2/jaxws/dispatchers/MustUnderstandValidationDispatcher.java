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

package org.apache.axis2.jaxws.dispatchers;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AbstractDispatcher;
import org.apache.axis2.i18n.Messages;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.util.Iterator;

/**
 * Do JAXWS MustUnderstand header processing per the JAXWS 2.0 specification.  This checks for
 * a specific compliance situation where a non-existant operation with mustUnderstood headers 
 * that are not understood must throw a mustUnderstandFault rather than an invalid EPR exception.
 * 
 *  Note that this handler should be inserted in the inbound dispather chains so that the
 *  Dispatcher checkPostConditions does not throw the invalid EPR fault if the operation is null.
 */
public class MustUnderstandValidationDispatcher extends AbstractDispatcher {
    private static final Log log = LogFactory.getLog(MustUnderstandValidationDispatcher.class);

    /* (non-Javadoc)
     * @see org.apache.axis2.engine.AbstractDispatcher#findOperation(org.apache.axis2.description.AxisService, org.apache.axis2.context.MessageContext)
     */
    @Override
    public AxisOperation findOperation(AxisService service, MessageContext messageContext)
            throws AxisFault {
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.axis2.engine.AbstractDispatcher#findService(org.apache.axis2.context.MessageContext)
     */
    @Override
    public AxisService findService(MessageContext messageContext) throws AxisFault {
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.axis2.engine.AbstractDispatcher#initDispatcher()
     */
    @Override
    public void initDispatcher() {
    }

    public InvocationResponse invoke(MessageContext msgctx) throws AxisFault {
        AxisService axisService = msgctx.getAxisService();
        AxisOperation axisOperation = msgctx.getAxisOperation();
        
        if (log.isDebugEnabled()) {
            log.debug("JAXWS MustUnderstandValidationDispatcher.invoke on AxisService " 
                      + axisService
                      + "; AxisOperation " + axisOperation);
        }
        // REVIEW: This will only check do the mustUnderstand checking if the operation is null
        // That is because JAXWS compliance requires we throw a mustUnderstand fault even if the operation is
        // invalid.  If the operation is valid, then further mustUnderstand processing will be done
        // by the JAXWS MustUnderstandChecker handler. 
        if (axisService != null && axisOperation == null) {
            checkMustUnderstand(msgctx);
        }
        return InvocationResponse.CONTINUE;
    }

    private boolean checkMustUnderstand(MessageContext msgContext) throws AxisFault {
        boolean checksPass = true;

        SOAPEnvelope envelope = msgContext.getEnvelope();
        if (envelope.getHeader() == null) {
            return checksPass;
        }
        
        // First mark all the headers JAXWS would understand to make sure we don't throw
        // a mustUnderstand fault inappropriately for those.
        MustUnderstandUtils.markUnderstoodHeaderParameters(msgContext);

        // Now check all the headers and throw the mustUnderstandFault if any not understood.
        // REVIEW: Note that QoSes that would run after the dispatch phase will not have marked
        // their headers yet.

        Iterator headerBlocks = envelope.getHeader().getHeadersToProcess(null);
        while (headerBlocks.hasNext()) {
            SOAPHeaderBlock headerBlock = (SOAPHeaderBlock) headerBlocks.next();
            if (headerBlock.isProcessed() || !headerBlock.getMustUnderstand()) {
                continue;
            }

            QName faultQName = headerBlock.getVersion().getMustUnderstandFaultCode();
            throw new AxisFault(Messages.getMessage("mustunderstandfailed",
                                                    headerBlock.getNamespace().getNamespaceURI(),
                                                    headerBlock.getLocalName()), faultQName);
        }
        return checksPass;
    }

}
