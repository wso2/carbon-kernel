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

package org.apache.axis2.jaxws.registry;

import org.apache.axis2.jaxws.addressing.SubmissionAddressingFeature;
import org.apache.axis2.jaxws.description.builder.MDQConstants;
import org.apache.axis2.jaxws.feature.ServerConfigurator;
import org.apache.axis2.jaxws.server.config.AddressingConfigurator;
import org.apache.axis2.jaxws.server.config.MTOMConfigurator;
import org.apache.axis2.jaxws.server.config.RespectBindingConfigurator;

import javax.xml.ws.RespectBindingFeature;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.soap.MTOMFeature;
import javax.xml.ws.soap.SOAPBinding;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ServerConfiguratorRegistry {
    private static Map<String, ServerConfigurator> map =
        new ConcurrentHashMap<String, ServerConfigurator>();
    
    static {
        map.put(AddressingFeature.ID, new AddressingConfigurator());
        map.put(SubmissionAddressingFeature.ID, new AddressingConfigurator());
        map.put(MTOMFeature.ID, new MTOMConfigurator());
        map.put(RespectBindingFeature.ID, new RespectBindingConfigurator());
    }
    
    public static void setConfigurator(String id, ServerConfigurator configurator) {
        map.put(id, configurator);
    }
    
    public static ServerConfigurator getConfigurator(String id) {
        return map.get(id);
    }
    
    public static Set<String> getIds() {
        return map.keySet();
    }
    
    public static boolean isSOAPBinding(String url) {
        if (url != null && (url.equals(SOAPBinding.SOAP11HTTP_BINDING) ||
                url.equals(SOAPBinding.SOAP11HTTP_MTOM_BINDING) ||
                url.equals(SOAPBinding.SOAP12HTTP_BINDING)|| 
                url.equals(SOAPBinding.SOAP12HTTP_MTOM_BINDING) ||
                url.equals(MDQConstants.SOAP11JMS_BINDING) ||
                url.equals(MDQConstants.SOAP11JMS_MTOM_BINDING) ||
                url.equals(MDQConstants.SOAP12JMS_BINDING) ||
                url.equals(MDQConstants.SOAP12JMS_MTOM_BINDING) )) {
            return true;
        }
        return false;
    }
}
