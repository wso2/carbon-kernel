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

package org.apache.axis2.jaxws.core.util;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.MessageContextBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** A utility class for handling some of the common issues related to the JAX-WS MessageContext. */
public class MessageContextUtils {
    private static Log log = LogFactory.getLog(MessageContextUtils.class);

    /**
     * Given a request MessageContext, create a new MessageContext from there with the necessary
     * information to make sure the new MessageContext is related to the existing one.
     *
     * @param mc - the MessageContext to use as the source
     * @return
     */
    public static MessageContext createResponseMessageContext(MessageContext mc) {
        try {
            org.apache.axis2.context.MessageContext sourceAxisMC = mc.getAxisMessageContext();

            // There are a number of things that need to be copied over at the
            // Axis2 level.
            org.apache.axis2.context.MessageContext newAxisMC =
                    MessageContextBuilder.createOutMessageContext(sourceAxisMC);

            MessageContext newMC = new MessageContext(newAxisMC);
            newMC.setOutbound(true);
            newMC.setServer(true);
            newMC.setMEPContext(mc.getMEPContext());
            newMC.setEndpointDescription(mc.getEndpointDescription());
            newMC.setOperationDescription(mc.getOperationDescription());
            return newMC;
        } catch (AxisFault e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }
    
    
    /*
     * special messagecontext that has no AxisContext associated with it.  Typically, this
     * would be used in a "client outbound handler throws exception" case since that would
     * mean we never hit the InvocationController and thus never hit the Axis layer.
     */
    public static MessageContext createMinimalResponseMessageContext(MessageContext mc) {
        org.apache.axis2.context.MessageContext sourceAxisMC = mc.getAxisMessageContext();
        MessageContext newMC = new MessageContext(sourceAxisMC);
        newMC.setMEPContext(mc.getMEPContext());
        newMC.setEndpointDescription(mc.getEndpointDescription());
        newMC.setOperationDescription(mc.getOperationDescription());
        return newMC;
    }
    
    /**
     * Given a request MessageContext, create a new MessageContext for a fault response.
     *
     * @param mc
     * @return
     */
    public static MessageContext createFaultMessageContext(MessageContext mc) {
        try {
            org.apache.axis2.context.MessageContext faultMC =
                    MessageContextBuilder.createFaultMessageContext(mc.getAxisMessageContext(),
                                                                    null);
            MessageContext jaxwsFaultMC = new MessageContext(faultMC);
            jaxwsFaultMC.setOutbound(true);
            jaxwsFaultMC.setServer(true);
            jaxwsFaultMC.setMEPContext(mc.getMEPContext());
            jaxwsFaultMC.setEndpointDescription(mc.getEndpointDescription());
            jaxwsFaultMC.setOperationDescription(mc.getOperationDescription());
            return jaxwsFaultMC;
        }
        catch (AxisFault e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }
    
    public static boolean getJaxwsProviderInterpretNullOneway(MessageContext mc){
        boolean retval = true;  // default is true
        org.apache.axis2.context.MessageContext ac = mc.getAxisMessageContext();
        if (ac == null) {
            if (log.isDebugEnabled()) {
                log.debug("getJaxwsProviderInterpretNullOneway returns true due to missing MessageContext");
            }
            return retval;
        }
        // First examine the JaxwsProviderInterpretNullOneway flag on the context hierarchy
        Boolean providerNullOneway = (Boolean) ac.getProperty(
                org.apache.axis2.jaxws.Constants.JAXWS_PROVIDER_NULL_ONEWAY);
        if (providerNullOneway != null) {
            retval = providerNullOneway.booleanValue();
            if (log.isDebugEnabled()) {
                log.debug("getJaxwsProviderInterpretNullOneway returns " + retval + " per Context property " + 
                        org.apache.axis2.jaxws.Constants.JAXWS_PROVIDER_NULL_ONEWAY);
            }
            return retval;
        }
        
        // Now look at the JaxwsProviderInterpretNullOneway parameter
        Parameter p = ac.getParameter(org.apache.axis2.jaxws.Constants.JAXWS_PROVIDER_NULL_ONEWAY);
        if (p != null) {
            retval = JavaUtils.isTrue(p.getValue());
            if (log.isDebugEnabled()) {
                log.debug("getJaxwsProviderInterpretNullOneway returns " + retval + " per inspection of Configuration property " + 
                        org.apache.axis2.jaxws.Constants.JAXWS_PROVIDER_NULL_ONEWAY);
            }
            return retval;
        }
      
        if (log.isDebugEnabled()) {
            log.debug("getJaxwsProviderInterpretNullOneway returns the default value: " + retval);
        }
        return retval;
    }

}
