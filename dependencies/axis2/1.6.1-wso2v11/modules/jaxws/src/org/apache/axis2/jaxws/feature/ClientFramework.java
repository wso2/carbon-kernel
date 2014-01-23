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

package org.apache.axis2.jaxws.feature;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.spi.BindingProvider;

import javax.xml.ws.WebServiceFeature;
import java.util.HashMap;
import java.util.Map;

public class ClientFramework {
    private static final WebServiceFeature[] ZERO_LENGTH_ARRAY = new WebServiceFeature[0];
    
    private Map<String, ClientConfigurator> configuratorMap;
    private Map<String, WebServiceFeature> featureMap;
    
    public ClientFramework() {
    	super();
        configuratorMap = new HashMap<String, ClientConfigurator>();
        featureMap = new HashMap<String, WebServiceFeature>();
    }
    
    public void addConfigurator(String id, ClientConfigurator configurator) {
        configuratorMap.put(id, configurator);
    }
    
    public boolean isValid(WebServiceFeature feature) {
        if (feature == null)
            return false;
        
        return configuratorMap.containsKey(feature.getID());
    }
    
    public void addFeature(WebServiceFeature feature) {
        if (!isValid(feature)) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("invalidWSFeature",
                        feature.getID()));
        }
        featureMap.put(feature.getID(), feature);
    }
    
    public WebServiceFeature getFeature(String id) {
        return featureMap.get(id);
    }
    
    public WebServiceFeature[] getAllFeatures() {
        return featureMap.values().toArray(ZERO_LENGTH_ARRAY);
    }
    
    public void configure(MessageContext messageContext, BindingProvider provider) {
        for (WebServiceFeature feature : getAllFeatures()) {
            ClientConfigurator configurator = configuratorMap.get(feature.getID());
            configurator.configure(messageContext, provider);
        }
    }
}
