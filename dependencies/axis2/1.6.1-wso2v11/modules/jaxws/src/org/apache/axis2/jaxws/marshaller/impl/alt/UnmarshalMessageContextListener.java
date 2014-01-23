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

package org.apache.axis2.jaxws.marshaller.impl.alt;

import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.datasource.jaxb.JAXBCustomBuilder;
import org.apache.axis2.datasource.jaxb.JAXBDSContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.MessageContextListener;
import org.apache.axis2.description.Parameter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This MessageContextListener is triggered when 
 * a ServiceContext is attached to the MessageContext and
 * when a SOAPEnvelope is attached to the MessageContext.
 * 
 * In such cases, it attempts to get a previously cached (from a prior web service call)
 * UnmarshalInfo object from the AxisService.  The UnmarshalInfo
 * data is used to create a JAXBCustomBuilder on the SOAPEnvelope's builder.
 * 
 * The net effect is that the StAXOMBuilder will use the JAXBCustomBuilder during
 * unmarshalling.  This saves time and space.
 */
public class UnmarshalMessageContextListener implements MessageContextListener {

    private static final Log log = 
            LogFactory.getLog(UnmarshalMessageContextListener.class);
    /**
     * Create and add a listener
     * @param sc ServiceContext
     */
    public static void create(ServiceContext sc) {
        
        // Only create and add one listener 
        if (sc == null || 
            sc.getAxisService() == null ||
            sc.getAxisService().
               hasMessageContextListener(UnmarshalMessageContextListener.class)) {
            return;
        }
        UnmarshalMessageContextListener listener = new UnmarshalMessageContextListener();
        sc.getAxisService().addMessageContextListener(listener);
    }
    
    /**
     * User create factory method to create and register listener
     */
    private UnmarshalMessageContextListener() {  
    }
    
    /**
     * Attach the JAXBCustomBuilder on the Axiom builder.
     * This will speedup the JAXB unmarshalling code
     * @param sc
     * @param mc
     */
    private void installJAXBCustomBuilder(ServiceContext sc, MessageContext mc) {
        
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

        // Get the UnmarshalInfo object.
        // This contains information from prior unmarshalling
        AxisOperation axisOp = mc.getAxisOperation();
        if (axisOp == null) {
            return;
        }
        
        Parameter parameterInfo = axisOp.getParameter(UnmarshalInfo.KEY);
        if (parameterInfo == null) {
            return;
        }
        UnmarshalInfo info =  (UnmarshalInfo) parameterInfo.getValue();
        
        if (info == null) {
            return;
        }
        
        // Crate a JAXBCustomBuilder and register it on the Axiom StAXOMBuilder
        JAXBDSContext jaxbDSC = new JAXBDSContext(info.getPackages(), info.getPackagesKey());
        jaxbDSC.setMessageContext(mc);
        JAXBCustomBuilder jcb = new JAXBCustomBuilder(jaxbDSC);
        ((StAXOMBuilder) envelope.getBuilder()).registerCustomBuilderForPayload(jcb);
        if (log.isDebugEnabled()) {
            log.debug("Registering JAXBCustomBuilder: " + jcb + " for AxisOperation: " + axisOp.getName());
        }
    }

    public void attachEnvelopeEvent(MessageContext mc) {
        if (mc.getServiceContext() != null) {
            installJAXBCustomBuilder(mc.getServiceContext(), mc);
        } 
    }

    public void attachServiceContextEvent(ServiceContext sc, MessageContext mc) {
        if (mc.getEnvelope() != null) {
            installJAXBCustomBuilder(sc, mc);
        }
    }

}
