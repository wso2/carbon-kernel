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
import org.apache.axis2.jaxws.client.config.AddressingConfigurator;
import org.apache.axis2.jaxws.client.config.MTOMConfigurator;
import org.apache.axis2.jaxws.client.config.RespectBindingConfigurator;
import org.apache.axis2.jaxws.feature.ClientConfigurator;

import javax.xml.ws.RespectBindingFeature;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.soap.MTOMFeature;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ClientConfiguratorRegistry {
    private static Map<String, ClientConfigurator> map =
        new ConcurrentHashMap<String, ClientConfigurator>();
    
    static {
        map.put(AddressingFeature.ID, new AddressingConfigurator());
        map.put(SubmissionAddressingFeature.ID, new AddressingConfigurator());
        map.put(MTOMFeature.ID, new MTOMConfigurator());
        map.put(RespectBindingFeature.ID, new RespectBindingConfigurator());
    }
    
    public static void setConfigurator(String id, ClientConfigurator configurator) {
        map.put(id, configurator);
    }
    
    public static ClientConfigurator getConfigurator(String id) {
        return map.get(id);
    }
    
    public static Set<String> getIds() {
        return map.keySet();
    }
}
