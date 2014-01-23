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

package org.apache.axis2.dataretrieval;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisService2WSDL11;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Axis 2 Data Locator responsibles for retrieving WSDL metadata.
 */
public class WSDLDataLocator extends BaseAxisDataLocator implements AxisDataLocator {
    private static final Log log = LogFactory.getLog(WSDLDataLocator.class);
    String serviceURL = null;
    AxisService theService = null;
    String request_Identifier = null;

    protected WSDLDataLocator() {
    }

    /**
     * Constructor
     *
     * @param data an array of ServiceData instance defined in the
     *             ServiceData.xml for the WSDL dialect.
     */
    protected WSDLDataLocator(ServiceData[] data) {
        dataList = data;
    }

    /**
     * getData API
     * Implement data retrieval logic for WSDL dialect
     */
    public Data[] getData(DataRetrievalRequest request,
                          MessageContext msgContext) throws DataRetrievalException {
        log.trace("Default WSDL DataLocator getData starts");

        request_Identifier = request.getIdentifier();
        serviceURL = msgContext.getTo().getAddress();

        OutputForm outputform = request.getOutputForm();

        if (outputform == null) { // not defined, defualt to inline
            outputform = OutputForm.INLINE_FORM;
        }

        Data[] output;

        String outputFormString = outputform.getType();

        if (outputform == OutputForm.INLINE_FORM) {
            output = outputInlineForm(msgContext, dataList);
        } else if (outputform == OutputForm.LOCATION_FORM) {
            output = outputLocationForm(dataList);

        } else if (outputform == OutputForm.REFERENCE_FORM) {
            output = outputReferenceForm(msgContext, dataList);

        } else {
            output = outputInlineForm(msgContext, dataList);

        }

        if (output == null) {
            if (log.isTraceEnabled()) {
                log.trace(
                        "Null data return! Data Locator does not know how to handle request for dialect= " +
                                request.getDialect() + " in the form of " + outputFormString);
            }
        }


        log.trace("Default WSDL DataLocator getData ends");


        return output;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.axis2.dataretrieval.BaseAxisDataLocator#outputInlineForm(org.apache.axis2.context.MessageContext, org.apache.axis2.dataretrieval.ServiceData[])
     */
    protected Data[] outputInlineForm(MessageContext msgContext, ServiceData[] dataList)
            throws DataRetrievalException {
        Data[]  result = super.outputInlineForm(msgContext, dataList);

        // Do not generate WSDL if Identifier was specified in the request as
        // (1) this is to support ?wsdl request; 
        // (2) Data for specified Identifier must be available to satisfy the GetMetadata request.

        if (result.length == 0) {
            
            log.trace("Default WSDL DataLocator attempt to generates WSDL.");

            if (msgContext != null) {
                theService = msgContext.getAxisService();
                serviceURL = msgContext.getTo().getAddress();
                theService.setEndpointURL(serviceURL);
            } else {
                throw new DataRetrievalException("MessageContext was not set!");
            }

            if (request_Identifier == null || request_Identifier.equals(theService.getTargetNamespace())) {

                AxisService2WSDL11 axisService2WOM;
                OMElement wsdlElement;

                try {
                    axisService2WOM = new AxisService2WSDL11(theService);
                    wsdlElement = axisService2WOM.generateOM();
                    
                } catch (Exception e) {
                    log.debug(e);
                    throw new DataRetrievalException(e);
                }

                if (wsdlElement != null) {
                    log.trace("Default WSDL DataLocator successfully generated WSDL.");
                    result = new Data[1];
                    result[0] = new Data(wsdlElement, null);
                }
            }
        }
        
        return result;
    }

    /*
     * 
     */
    protected Data[] outputLocationForm(ServiceData[] serviceData) throws DataRetrievalException {
        Data[] result = super.outputLocationForm(serviceData);

        // Do not generate URL if Identifier was specified in the request as
        // (1) Axis2 ?wsdl URL request is not supporting Identifier; 
        // (2) URL data for specified Identifier must be available to satisfy
        //     the GetMetadata request.

        if (result.length == 0 && request_Identifier == null) {
            result = new Data[1];
            result[0] = new Data(serviceURL + "?wsdl", null);
        }
        return result;
    }


}
