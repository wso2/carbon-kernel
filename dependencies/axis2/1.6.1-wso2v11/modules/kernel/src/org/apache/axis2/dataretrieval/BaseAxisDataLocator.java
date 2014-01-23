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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;

/**
 * BaseAxisDataLocator implements common code and serves as a base class
 * for the supported default Axis2 dialect data locators.
 */

public abstract class BaseAxisDataLocator {
    private static final Log log = LogFactory.getLog(BaseAxisDataLocator.class);

    protected ServiceData[] dataList = null;
    private OutputForm outputform = OutputForm.INLINE_FORM;

    /**
     * The default Axis2 Data locator getData API
     * Checks data information configured in ServiceData.xml for the supported
     * output forms: inline, url, EndpointReference.
     * <p/>
     * Note: Subclass that has its implementation of outInlineForm, outputLocationForm,
     * and outputReferenceForm logic must implement the getData API.
     */

    public Data[] getData(DataRetrievalRequest request,
                          MessageContext msgContext) throws DataRetrievalException {
        log.trace("Default Base DataLocator getData starts");

        OutputForm outputform = (OutputForm) request.getOutputForm();

        if (outputform == null) { // not defined, defualt to inline
            outputform = OutputForm.INLINE_FORM;
        }

        Data[] output = null;

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
            log.info(
                    "Null data return! Data Locator does not know how to handle request for dialect= " +
                            (String) request.getDialect()
                            + " in the form of " + outputFormString);
        }


        log.trace("Default Base DataLocator getData ends");

        return output;
    }

    /*
     * WSDL or a policy document
     */
    protected Data[] outputInlineForm(MessageContext msgContext,
                                      ServiceData[] serviceData) throws DataRetrievalException {
    	assert(msgContext != null);
    	if (serviceData == null || serviceData.length == 0) {
    		return new Data[0];
    	}
    	
        final ArrayList<Data> result = new ArrayList<Data>();
        for(final ServiceData sd: serviceData) {
        	final OMElement metaElement 
        		= sd.getFileContent(msgContext.getAxisService().getClassLoader());
            if (metaElement != null) {
            	result.add(new Data(metaElement, sd.getIdentifier()));
            }
        }
        return (Data[]) result.toArray(new Data[result.size()]);
    }


    protected Data[] outputLocationForm(ServiceData[] serviceData)
            throws DataRetrievalException {
    	if(serviceData == null || serviceData.length == 0) {
    		return new Data[0];
    	}
    	
        final ArrayList<Data> result = new ArrayList<Data>();
        for (final ServiceData sd: serviceData) {
        	final String urlValue = sd.getURL();
        	if (urlValue != null) {
        		result.add(new Data(urlValue, sd.getIdentifier()));
            }
        }
        return result.toArray(new Data[result.size()]);
    }
    

    protected Data[] outputReferenceForm(MessageContext msgContext,
                                         ServiceData[] serviceData) throws DataRetrievalException {
        if(serviceData == null || serviceData.length == 0) {
        	return new Data[0];
        }
        
        final ArrayList<Data> result = new ArrayList<Data>();
        for (final ServiceData sd: serviceData) {
        	final OMElement epr = sd.getEndpointReference();
        	if (epr != null) {
        		result.add(new Data(epr, sd.getIdentifier()));
            }
        }
        return result.toArray(new Data[result.size()]);
    }


    protected void setServiceData(ServiceData[] inServiceData) {
        this.dataList = inServiceData;
    }


    protected OutputForm getOutputForm() {
        return outputform;
    }

}
