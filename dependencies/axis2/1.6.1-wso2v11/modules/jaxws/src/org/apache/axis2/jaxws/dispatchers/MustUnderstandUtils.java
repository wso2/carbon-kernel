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
import org.apache.axis2.description.AxisDescription;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Static utility methods used in processing mustUnderstand headers relative to JAXWS.
 */
public class MustUnderstandUtils {
    private static final Log log = LogFactory.getLog(MustUnderstandUtils.class);
    
    /**
     * Mark all headers for JAXWS SEI method paramaters as understood.  Note that per the JAXWS
     * 2.0 specification, a header is considered understood if it used by as a parameter for 
     * any method on the SEI, not just the method corresponding to the incoming operation.  
     * See Section 10.2.1 item 3.a which specifically says "mapped to method parameters
     * in the service endpoint interface".
     * 
     * @param msgContext
     */
    public static void markUnderstoodHeaderParameters(MessageContext msgContext) {
        if (msgContext == null) {
            return;
        }
        
        SOAPEnvelope envelope = msgContext.getEnvelope();
        if (envelope.getHeader() == null) {
            return;
        }
        
        ArrayList understoodHeaderQNames = MustUnderstandUtils.getHeaderParamaterList(msgContext);
        if (understoodHeaderQNames == null || understoodHeaderQNames.isEmpty()) {
            return;
        }
        
        // Passing in null will get headers targeted for NEXT and ULTIMATE RECEIVER
        Iterator headerBlocks = envelope.getHeader().getHeadersToProcess(null);
        while (headerBlocks.hasNext()) {
            SOAPHeaderBlock headerBlock = (SOAPHeaderBlock) headerBlocks.next();
            QName headerQN = headerBlock.getQName();
            if (understoodHeaderQNames.contains(headerQN)) {
                headerBlock.setProcessed();
                if (log.isDebugEnabled()) {
                    log.debug("Header marked as processed by JAXWS MustUnderstandChecker: " 
                              + headerQN);
                }
            }
        }
    }

    /**
     * Return an ArrayList of QNames corresponding to SOAP headers which map to method parameters
     * for any methods on the corresponding SEI and the SOAP handlers.
     * 
     * @param msgContext
     * @return ArrayList of QNames for all header parameters for an SEI and SOAP handlers.  
     *         The list may be empty but will not be null.
     */
    static ArrayList EMPTY_LIST = new ArrayList();
    public static ArrayList getHeaderParamaterList(MessageContext msgContext) {
        ArrayList headers = null;
        // Build a list of understood headers for all the operations under the service
        AxisService axisService = msgContext.getAxisService();
        if (log.isDebugEnabled()) {
            log.debug("Building list of understood headers for all operations under " + axisService);
        }
        if (axisService == null) {
            headers = EMPTY_LIST;
        } else  {
            
            // Get the understood headers from the sei methods
            ArrayList seiMethodHeaders = (ArrayList) 
                axisService.getParameterValue("seiMethodHeaderParameter");

            if (seiMethodHeaders == null) {
                // examine SEI methods
                seiMethodHeaders = new ArrayList();
                Iterator operationIterator = axisService.getOperations();
                if (operationIterator != null) {
                    while (operationIterator.hasNext()) {
                        AxisOperation operation = (AxisOperation) operationIterator.next();
                        ArrayList list = getSEIMethodHeaderParameterList(operation);
                        if (log.isDebugEnabled()) {
                            log.debug("Adding headers from operation " + operation + "; headers = "
                                      + list);
                        }
                        if (list != null && !list.isEmpty()) {
                            seiMethodHeaders.addAll(list);
                        }
                    }
                }

                try {
                    // Save calculated value since this won't change
                    axisService.addParameter("seiMethodHeaderParameter", seiMethodHeaders);
                } catch (AxisFault e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Problem caching seiMethodHeaderParameter.  " +
                                        "Processing continues without cached value");
                    }
                }
                
            }
            
            // Get the understood headers from the handlers
            ArrayList handlerHeaders = getHandlersHeaderParameterList(axisService);
            if (log.isDebugEnabled()) {
                log.debug("Adding headers from SOAP handlers; headers = " + handlerHeaders);
            }
            
            // Make the combined headers list.
            // The following code avoids making temporary array lists for performance/gc reasons
            if (seiMethodHeaders == null || seiMethodHeaders.isEmpty()) {
                headers = (handlerHeaders == null) ? EMPTY_LIST : handlerHeaders;  // Return handler headers
            } else if (handlerHeaders == null || handlerHeaders.isEmpty()) {
                headers = (seiMethodHeaders == null) ? EMPTY_LIST : seiMethodHeaders;  // Return sei method headers
            } else {
                headers = new ArrayList();
                headers.addAll(seiMethodHeaders);
                headers.addAll(handlerHeaders);
            }
        }
        return headers;
    }
    
    /**
     * Return an ArrayList of QNames corresponding to SOAP headers which map to method parameters
     * for a specific operation.
     * 
     * @param axisOperation
     * @return ArrayList of header QNames for all header paramters on an operation, or null if none.
     */
    public static ArrayList getSEIMethodHeaderParameterList(AxisOperation axisOperation) {
        return getHeaderParameterList(axisOperation, OperationDescription.HEADER_PARAMETER_QNAMES);
    }
    
    /**
     * Return an ArrayList of QNames that is a collection of SOAP handlers 
     * 
     * @param axisService
     * @return ArrayList of header QNames.     
     */
    public static ArrayList getHandlersHeaderParameterList(AxisService axisService) {
        return getHeaderParameterList(axisService, EndpointDescription.HANDLER_PARAMETER_QNAMES);
    }
    
    private static ArrayList getHeaderParameterList(AxisDescription axisDescription, String paramName) {        
        Parameter headerQNamesParameter = axisDescription.getParameter(paramName);
        if (headerQNamesParameter == null) {
            if (log.isDebugEnabled()) {
                log.debug("Parameter not on " + axisDescription + "; " 
                          + paramName);
            }
            return null;
        }

        ArrayList understoodHeaderQNames = (ArrayList) headerQNamesParameter.getValue();
        if (understoodHeaderQNames == null || understoodHeaderQNames.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("Parameter value is empty: "  + axisDescription + "; " 
                          + paramName);
            }
            return null;
        }

        return understoodHeaderQNames;
    }
    
}
