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

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AbstractDispatcher;
import org.apache.axis2.jaxws.description.EndpointInterfaceDescription;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;

/**
 * This dispatcher will look for a specific operation on the AxisService and return it
 * if found.  This dispatcher is used for Provider-based endpoints which do not have WSDL
 * associated with them.  Those types of endpoints will not have WSDL operations created for them
 * since (a) there is no WSDL and (b) there is no SEI from which to build operations using
 * annotations.  For these types of endpoints, a generic operation will have been added to the
 * service which will accept any incoming WSDL operation and pass the incoming message to the
 * Provider endpoint. 
 */
public class GenericProviderDispatcher extends AbstractDispatcher {
    private static final Log log = LogFactory.getLog(GenericProviderDispatcher.class);

    /* (non-Javadoc)
     * @see org.apache.axis2.engine.AbstractDispatcher#findOperation(org.apache.axis2.description.AxisService, org.apache.axis2.context.MessageContext)
     */
    @Override
    public AxisOperation findOperation(AxisService service, MessageContext messageContext)
            throws AxisFault {
        AxisOperation theOperation = null;
        if (log.isDebugEnabled()) {
            log.debug("findOperation service = " + service + "; messagectx: " + messageContext);
        }
        
        // If there's an AxisService, then look for the specially named operation and return it
        if (service != null) {
            theOperation = service.getOperation(new QName(EndpointInterfaceDescription.JAXWS_NOWSDL_PROVIDER_OPERATION_NAME));
            if (log.isDebugEnabled()) {
                log.debug("operation " + EndpointInterfaceDescription.JAXWS_NOWSDL_PROVIDER_OPERATION_NAME + " is " + theOperation );
            }
        }
        
        return theOperation;
    }

    /* (non-Javadoc)
     * @see org.apache.axis2.engine.AbstractDispatcher#findService(org.apache.axis2.context.MessageContext)
     */
    @Override
    public AxisService findService(MessageContext messageContext) throws AxisFault {
        // This dispatcher will not try to resolve and AxisService if one hasn't been
        // resolved already
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.axis2.engine.AbstractDispatcher#initDispatcher()
     */
    @Override
    public void initDispatcher() {
        // No initialization necessary
    }

}
