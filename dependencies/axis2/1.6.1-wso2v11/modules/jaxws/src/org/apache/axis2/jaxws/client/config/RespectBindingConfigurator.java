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

package org.apache.axis2.jaxws.client.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


import javax.xml.namespace.QName;
import javax.xml.ws.RespectBindingFeature;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.binding.SOAPBinding;
import org.apache.axis2.jaxws.common.config.WSDLValidatorElement;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.validator.EndpointDescriptionValidator;
import org.apache.axis2.jaxws.feature.ClientConfigurator;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.spi.Binding;
import org.apache.axis2.jaxws.spi.BindingProvider;
import org.apache.axis2.jaxws.util.WSDLExtensionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 */
public class RespectBindingConfigurator implements ClientConfigurator {
    private static final Log log = LogFactory.getLog(RespectBindingConfigurator.class);
    /*
     *  (non-Javadoc)
     * @see org.apache.axis2.jaxws.feature.util.WebServiceFeatureConfigurator#performConfiguration(org.apache.axis2.jaxws.core.MessageContext, org.apache.axis2.jaxws.spi.BindingProvider)
     */
    public void configure(MessageContext messageContext, BindingProvider provider) {
        if(log.isDebugEnabled()){
            log.debug("Invoking RespectBindingConfiguration.configure() on client");
        }
        Binding bnd = (Binding) provider.getBinding();
        RespectBindingFeature respectBindingFeature =
            (RespectBindingFeature) bnd.getFeature(RespectBindingFeature.ID);
        
        if (respectBindingFeature == null) {
            throw ExceptionFactory.makeWebServiceException(
                 Messages.getMessage("respectBindingNotSpecified"));
        }
        boolean isEnabled = respectBindingFeature.isEnabled();
        if(isEnabled){
            if(bnd instanceof SOAPBinding){
                ((SOAPBinding)bnd).setRespectBindingEnabled(isEnabled);
            }
            //Get the wsdl document location, if wsdl is not found throw a WebservicesException.
            //If wsdl is found, look for wsdl extensions.
            EndpointDescription endpointDescription = provider.getEndpointDescription();
            endpointDescription.setRespectBinding(isEnabled);
            WSDLExtensionUtils.processExtensions(endpointDescription);
            
            //We have build up set of extensions from wsdl
            //let go ahead and validate these extensions now.
            EndpointDescriptionValidator endpointValidator = new EndpointDescriptionValidator(endpointDescription);
             
            boolean isEndpointValid = endpointValidator.validate(true);
            //throw Exception if extensions are not understood by Engine.
            if (!isEndpointValid) {
                String msg = Messages.getMessage("endpointDescriptionValidationErrors",
                                                 endpointValidator.toString());
                throw ExceptionFactory.makeWebServiceException(msg);
            }
        }
        if(log.isDebugEnabled()){
            log.debug("Exit from RespectBindingConfiguration.configure() on client.");
        }
    }

    /*
     *  (non-Javadoc)
     * @see org.apache.axis2.jaxws.feature.ClientConfigurator#supports(org.apache.axis2.jaxws.spi.Binding)
     */
    public boolean supports(Binding binding) {
        return true;
    }
}
