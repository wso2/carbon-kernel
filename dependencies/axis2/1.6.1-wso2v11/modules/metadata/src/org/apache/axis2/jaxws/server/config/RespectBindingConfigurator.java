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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.wsdl.Binding;
import javax.wsdl.BindingFault;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.WSDLElement;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap12.SOAP12Binding;
import javax.xml.namespace.QName;
import javax.xml.ws.RespectBinding;
import javax.xml.ws.RespectBindingFeature;

import org.apache.axis2.jaxws.common.config.WSDLValidatorElement;
import org.apache.axis2.jaxws.common.config.WSDLValidatorElement.State;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.EndpointDescriptionJava;
import org.apache.axis2.jaxws.description.EndpointDescriptionWSDL;
import org.apache.axis2.jaxws.feature.ServerConfigurator;
import org.apache.axis2.jaxws.util.WSDLExtensionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An implementation of the <code>ServerConfigurator</code> interface that will
 * configure the endpoint based on the presence of a <code>RespectBinding</code>
 * attribute.
 */
public class RespectBindingConfigurator implements ServerConfigurator {

    private static final Log log = LogFactory.getLog(RespectBindingConfigurator.class); 
    
    /*
     * (non-Javadoc)
     * @see org.apache.axis2.jaxws.feature.WebServiceFeatureConfigurator#configure(org.apache.axis2.jaxws.description.EndpointDescription)
     */
    public void configure(EndpointDescription endpointDescription) {
        if(log.isDebugEnabled()){
            log.debug("Invoking RespectBindingConfiguration.configure() on Server");
        }
    	RespectBinding annotation =
    		(RespectBinding) ((EndpointDescriptionJava) endpointDescription).getAnnoFeature(RespectBindingFeature.ID);
    	
        if (annotation != null) {
            if (log.isDebugEnabled()) {
                log.debug("Setting respectBinding to " + annotation.enabled());
            }
            endpointDescription.setRespectBinding(annotation.enabled());
            
            // Once we know that @RespectBinding is enabled, we have to find
            // any binding extensibility elements available and see which ones
            // have the "required" flag set to true.
            EndpointDescriptionWSDL edw = (EndpointDescriptionWSDL) endpointDescription;
            Binding bnd = edw.getWSDLBinding();
            Set<WSDLValidatorElement> requiredExtension = endpointDescription.getRequiredBindings();
            List<QName> unusedExtensions = new ArrayList<QName>();
            //invoke the search algorithm.
            WSDLExtensionUtils.search(bnd, requiredExtension, unusedExtensions);
                   
            if (log.isDebugEnabled()) {
                log.debug("The following extensibility elements were found, but were not required.");
                for (int n = 0; n < unusedExtensions.size(); ++n)
                    log.debug("[" + (n + 1) + "] - " + unusedExtensions.get(n));
            }
        }
        else {
            if (log.isDebugEnabled()) {
                log.debug("No @RespectBinding annotation was found.");
            }
        }
        if(log.isDebugEnabled()){
            log.debug("Exit from RespectBindingConfiguration.configure() on Server.");
        }
    }
    /*
     * (non-Javadoc)
     * @see org.apache.axis2.jaxws.feature.ServerConfigurator#supports(java.lang.String)
     */
    public boolean supports(String bindingId) {
        return true;
    }
}
