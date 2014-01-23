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

package org.apache.axis2.jaxws.client;

import javax.xml.ws.Dispatch;
import javax.xml.ws.Service.Mode;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.jaxws.BindingProvider;
import org.apache.axis2.jaxws.client.dispatch.BaseDispatch;
import org.apache.axis2.jaxws.client.dispatch.XMLDispatch;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ClientUtils {

    private static Log log = LogFactory.getLog(ClientUtils.class);

    /**
     * Determines what the SOAPAction value should be for a given MessageContext.
     *
     * @param ctx - The MessageContext for the request
     * @return A string with the calculated SOAPAction
     */
    public static String findSOAPAction(MessageContext ctx) {
        OperationDescription op = ctx.getOperationDescription();
        Boolean useSoapAction =
                (Boolean)ctx.getProperty(BindingProvider.SOAPACTION_USE_PROPERTY);
        if (useSoapAction != null && useSoapAction.booleanValue()) {
            // If SOAPAction use hasn't been disabled by the client, then first
            // look in the context properties.
            String action =
                    (String)ctx.getProperty(BindingProvider.SOAPACTION_URI_PROPERTY);
            if (action != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Setting soap action from JAX-WS request context.  Action [" +
                            action + "]");
                }
                return action;
            }

            // If we didn't find anything in the context props, then we need to 
            // check the OperationDescrition to see if one was configured in the WSDL.
            if (op != null) {
                action = op.getAction();
                if (action != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Setting soap action from operation description.  Action [" +
                                action + "]");
                    }
                    return action;
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Cannot set the soap action.  No operation description was found.");
                }
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Soap action usage was disabled");
            }
        }

        return null;
    }
    /**
     * The operation is used to make sure Option is property configured for ThrowingExceptionOnFault
     * @param msgContext
     */
    public static boolean getExceptionToBeThrownOnSOAPFault(org.apache.axis2.context.MessageContext msgContext){
    	if(log.isDebugEnabled()){
    		log.debug("start getExceptionToBeThrownOnSOAPFault(MsgContext)");
    	}
    	BindingProvider bp = (BindingProvider)msgContext.getProperty(BindingProvider.BINDING_PROVIDER);
    	if(bp == null){
    		return true;
    	}
    	//ThrowExceptionIfSOAPFault should be checked only for message Mode.
    	if(bp instanceof BaseDispatch){
    		if(((BaseDispatch)bp).getMode()!= Mode.MESSAGE){
    			if(log.isDebugEnabled()){
    	    		log.debug("throwExceptionIfSOAPFault will not be checked as Dispatch is not set to Mode.MESSAGE");
    	    		log.debug("End getExceptionToBeThrownOnSOAPFault(MsgContext)");
    	    	}
    			return true;
    		}
    	}
        Object value = msgContext.getProperty(org.apache.axis2.jaxws.Constants.THROW_EXCEPTION_IF_SOAP_FAULT);
        boolean throwExceptionIfSOAPFault = (value!=null && value instanceof Boolean)?(Boolean)value:Boolean.TRUE;
    	if(log.isDebugEnabled()){
    		log.debug("throwExceptionIfSOAPFault ="+throwExceptionIfSOAPFault);
    		log.debug("End getExceptionToBeThrownOnSOAPFault(MsgContext)");
    	}
        return throwExceptionIfSOAPFault;
    }
}
