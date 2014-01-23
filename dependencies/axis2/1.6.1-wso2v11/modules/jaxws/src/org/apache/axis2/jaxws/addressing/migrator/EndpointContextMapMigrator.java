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

package org.apache.axis2.jaxws.addressing.migrator;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.jaxws.Constants;
import org.apache.axis2.jaxws.addressing.util.EndpointContextMap;
import org.apache.axis2.jaxws.addressing.util.EndpointContextMapManager;
import org.apache.axis2.util.ThreadContextMigrator;
import org.apache.axis2.util.Utils;
import org.apache.axis2.wsdl.WSDLConstants;

/**
 * This class will enable the JAX-WS 2.1 API methods to create instances of
 * {@link javax.xml.ws.EndpointReference} that target a particular web service
 * endpoint, identified by specifying the WSDL service name and port name of the
 * endpoint, to work correctly. This is achieved by enabling the implementation of 
 * {@link org.apache.axis2.jaxws.addressing.factory.Axis2EndpointReferenceFactory}
 * to retrieve the context it needs from the invoking thread. The instances of
 * {@link org.apache.axis2.addressing.EndpointReference} that it produces can
 * then converted to instances of {@link javax.xml.ws.EndpointReference}, as
 * needed.
 * 
 */
public class EndpointContextMapMigrator implements ThreadContextMigrator {

    /* (non-Javadoc)
     * @see org.apache.axis2.util.ThreadContextMigrator#migrateContextToThread(org.apache.axis2.context.MessageContext)
     */
    public void migrateContextToThread(MessageContext messageContext)
            throws AxisFault {
        //Only make the context map available if we have an inbound request
        //message, in the server.
        AxisOperation axisOperation = messageContext.getAxisOperation();
        String mep = axisOperation.getMessageExchangePattern();
        int mepConstant = Utils.getAxisSpecifMEPConstant(mep);
        
        if (mepConstant == WSDLConstants.MEP_CONSTANT_IN_ONLY ||
            mepConstant == WSDLConstants.MEP_CONSTANT_IN_OUT ||
            mepConstant == WSDLConstants.MEP_CONSTANT_IN_OPTIONAL_OUT ||
            mepConstant == WSDLConstants.MEP_CONSTANT_ROBUST_IN_ONLY)
        {
            EndpointContextMap map = (EndpointContextMap) 
                messageContext.getConfigurationContext().getProperty(Constants.ENDPOINT_CONTEXT_MAP);
            EndpointContextMapManager.setEndpointContextMap(map);
        }        
    }

    /* (non-Javadoc)
     * @see org.apache.axis2.util.ThreadContextMigrator#cleanupThread(org.apache.axis2.context.MessageContext)
     */
    public void cleanupThread(MessageContext messageContext) {
        //Only clean up if we are inbound to the server.
        AxisOperation axisOperation = messageContext.getAxisOperation();
        String mep = axisOperation.getMessageExchangePattern();
        int mepConstant = Utils.getAxisSpecifMEPConstant(mep);
        
        if (mepConstant == WSDLConstants.MEP_CONSTANT_IN_ONLY ||
            mepConstant == WSDLConstants.MEP_CONSTANT_IN_OUT ||
            mepConstant == WSDLConstants.MEP_CONSTANT_IN_OPTIONAL_OUT ||
            mepConstant == WSDLConstants.MEP_CONSTANT_ROBUST_IN_ONLY)
        {
            EndpointContextMapManager.setEndpointContextMap(null);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.axis2.util.ThreadContextMigrator#migrateThreadToContext(org.apache.axis2.context.MessageContext)
     */
    public void migrateThreadToContext(MessageContext messageContext)
            throws AxisFault {
        //Nothing to do.
    }

    /* (non-Javadoc)
     * @see org.apache.axis2.util.ThreadContextMigrator#cleanupContext(org.apache.axis2.context.MessageContext)
     */
    public void cleanupContext(MessageContext messageContext) {
        //Nothing to do.
    }

}
