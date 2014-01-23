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
package org.apache.axis2.jaxws.context.listener;

import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.MessageContextListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is a MessageContextListener that installs ParserInputStreamCustomBuilder
 * during an Envelope or ServiceContext event on AxisService. The installed
 * ParserInputStreamCustomBuilder is then used by StAXSoapModelBuilder to create
 * OMSourcedElement for payload data from Soap body.
 *
 */
public class ProviderOMContextListener implements MessageContextListener {
    private static final Log log = 
        LogFactory.getLog(ProviderOMContextListener.class);
    /**
     * Create and add a listener
     * @param sc ServiceContext
     */
    public static void create(ServiceContext sc) {
        if(log.isDebugEnabled()){
            log.debug("Start ProviderOMContextListener.create(ServiceContext)");
        }
        if (sc == null || 
            sc.getAxisService() == null ||
            sc.getAxisService().
            hasMessageContextListener(ProviderOMContextListener.class)){
            if(log.isDebugEnabled()){
                log.debug("ProviderOMContextListener already installed on AxisService");
            }
            return;
        }
        ProviderOMContextListener listener = new ProviderOMContextListener();
        sc.getAxisService().addMessageContextListener(listener);
        if(log.isDebugEnabled()){
            log.debug("End ProviderOMContextListener.create(ServiceContext)");
        }
    }
    /**
     * Attach the ParserInputStreamCustomBuilder on the Axiom builder.
     * This will create ParserInputStreamCustomBuilder and register it with StAXOMBuilder which will 
     * use ByteArrayCustomBuilder to create byteArray backed OM.
     * @param sc
     * @param mc
     */
    private void installParserInputStreamCustomBuilder(ServiceContext sc, MessageContext mc) {
        if (log.isDebugEnabled()) {
            log.debug("attachEvent for sc= " + sc.getName() + "and  mc=" + mc.getLogCorrelationID());
        }

        // Make sure the MessageContext has a SOAPEnvelope and Builder
        SOAPEnvelope envelope = mc.getEnvelope();

        if (envelope == null) {
            return;
        }
        if (!(envelope.getBuilder() instanceof StAXOMBuilder)) {
            return;
        }

        AxisOperation axisOp = mc.getAxisOperation();
        if (axisOp == null) {
            return;
        }

        ParserInputStreamCustomBuilder pacb = new ParserInputStreamCustomBuilder(null);
        ((StAXOMBuilder) envelope.getBuilder()).registerCustomBuilderForPayload(pacb);
    }

    /* (non-Javadoc)
     * @see org.apache.axis2.description.MessageContextListener#attachEnvelopeEvent(org.apache.axis2.context.MessageContext)
     */
    public void attachEnvelopeEvent(MessageContext mc) {
        if(log.isDebugEnabled()){
            log.debug("Start attachEnvelopeEvent");
        }
        if(mc.getServiceContext() !=null){
            installParserInputStreamCustomBuilder(mc.getServiceContext(), mc);
            if(log.isDebugEnabled()){
                log.debug("Installed ParserInputStreamCustomBuilder");
            }
        }
        if(log.isDebugEnabled()){
            log.debug("Stop attachEnvelopeEvent");
        }
    }

    /* (non-Javadoc)
     * @see org.apache.axis2.description.MessageContextListener#attachServiceContextEvent(org.apache.axis2.context.ServiceContext, org.apache.axis2.context.MessageContext)
     */
    public void attachServiceContextEvent(ServiceContext sc, MessageContext mc) {
        if(log.isDebugEnabled()){
            log.debug("Start attachServiceContextEvent");
        }
        if (mc.getEnvelope() != null) {

            installParserInputStreamCustomBuilder(sc, mc);
            if(log.isDebugEnabled()){
                log.debug("Installed ParserInputStreamCustomBuilder");
            }
        }
        if(log.isDebugEnabled()){
            log.debug("Stop attachServiceContextEvent");
        }
    }

}
