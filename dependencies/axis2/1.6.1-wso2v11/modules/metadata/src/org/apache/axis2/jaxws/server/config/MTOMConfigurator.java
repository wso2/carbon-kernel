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

package org.apache.axis2.jaxws.server.config;

import org.apache.axis2.Constants;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.EndpointDescriptionJava;
import org.apache.axis2.jaxws.feature.ServerConfigurator;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.registry.ServerConfiguratorRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.ws.soap.MTOM;
import javax.xml.ws.soap.MTOMFeature;

/**
 *
 */
public class MTOMConfigurator implements ServerConfigurator {

    private static Log log = LogFactory.getLog(MTOMConfigurator.class);
    
    /*
     * (non-Javadoc)
     * @see org.apache.axis2.jaxws.feature.WebServiceFeatureConfigurator#configure(org.apache.axis2.jaxws.description.EndpointDescription)
     */
    public void configure(EndpointDescription endpointDescription) {
    	MTOM mtomAnnoation =
    		(MTOM) ((EndpointDescriptionJava) endpointDescription).getAnnoFeature(MTOMFeature.ID);
    	AxisService service = endpointDescription.getAxisService();
    	
    	//Disable MTOM
    	Parameter enableMTOM = new Parameter(Constants.Configuration.ENABLE_MTOM, Boolean.FALSE);
    	Parameter threshold = new Parameter(Constants.Configuration.MTOM_THRESHOLD, 0);
      
        if (mtomAnnoation == null) {
            throw ExceptionFactory.
              makeWebServiceException(Messages.getMessage("mtomAnnotationErr"));
        }
        
        //Enable MTOM.
    	if (mtomAnnoation.enabled()) {
            if (log.isDebugEnabled()) {
                log.debug("Enabling MTOM via annotation.");
            }
    	    enableMTOM.setValue(Boolean.TRUE);
    	}
        
        //Set the threshold value.
        if (mtomAnnoation.threshold() > 0) {
            if (log.isDebugEnabled()) {
                log.debug("Setting MTOM threshold to [" + mtomAnnoation.threshold() + "].");
            }
            threshold.setValue(mtomAnnoation.threshold());
        }
    	
    	try {
    	    service.addParameter(enableMTOM);
            service.addParameter(threshold);
    	}
    	catch (Exception e) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("mtomEnableErr"), 
                                                           e);    		
    	}
    }    

    /*
     *  (non-Javadoc)
     * @see org.apache.axis2.jaxws.feature.ServerConfigurator#supports(java.lang.String)
     */
    public boolean supports(String bindingId) {
        return ServerConfiguratorRegistry.isSOAPBinding(bindingId);
    }
}
