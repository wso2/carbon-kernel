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

import org.apache.axis2.Constants;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.binding.SOAPBinding;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.feature.ClientConfigurator;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.spi.Binding;
import org.apache.axis2.jaxws.spi.BindingProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.xml.ws.soap.MTOMFeature;
import java.io.InputStream;
import java.util.List;

/**
 *
 */
public class MTOMConfigurator implements ClientConfigurator {

    private static Log log = LogFactory.getLog(MTOMConfigurator.class);
    
    /*
     * (non-Javadoc)
     * @see org.apache.axis2.jaxws.feature.util.WebServiceFeatureConfigurator#performConfiguration(org.apache.axis2.jaxws.core.MessageContext, org.apache.axis2.jaxws.spi.BindingProvider)
     */
    public void configure(MessageContext messageContext, BindingProvider provider) {
        Binding bnd = (Binding) provider.getBinding();
        MTOMFeature mtomFeature = (MTOMFeature) bnd.getFeature(MTOMFeature.ID);
        Message requestMsg = messageContext.getMessage();
        
        //Disable MTOM.
        requestMsg.setMTOMEnabled(false);
                
        if (mtomFeature == null) {
            throw ExceptionFactory.
              makeWebServiceException(Messages.getMessage("mtomFeatureErr"));
        }

        //Enable MTOM if specified.
        if (mtomFeature.isEnabled()) {
            int threshold = mtomFeature.getThreshold();
            List<String> attachmentIDs = requestMsg.getAttachmentIDs();
            
            // Enable MTOM
            requestMsg.setMTOMEnabled(true);
            
            if (threshold <= 0) {
                if (log.isDebugEnabled()) {
                    log.debug("Enabling MTOM with no threshold.");
                }             
            }else{
                if(log.isDebugEnabled()){
                	log.debug("MTOM Threshold Value ="+threshold);
                }
                
                //set MTOM threshold value on message context.
                //Once MTOM threshold is set on message context it will be
                //read by SOAPMessageFormatter.writeTo() while writing the attachment
                //SOAPMessageFormatter will further propogate the threshold value to
                //Axiom.OMOutputFormat. JAXBAttachmentUnmarshaller will then make 
                //decision if the attachment should be inlined or optimized.  
                messageContext.setProperty(Constants.Configuration.MTOM_THRESHOLD, new Integer(threshold));
            }
        }
        else {
            if (log.isDebugEnabled()) {
                log.debug("The MTOMFeature was found, but not enabled.");
            }
        }
    }

    /*
     *  (non-Javadoc)
     * @see org.apache.axis2.jaxws.feature.ClientConfigurator#supports(org.apache.axis2.jaxws.spi.Binding)
     */
    public boolean supports(Binding binding) {
        return binding instanceof SOAPBinding;
    }
}
