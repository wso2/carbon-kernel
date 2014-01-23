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

import org.apache.axis2.jaxws.server.InvocationListenerFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;


/**
 * This class will provide a means for JAX-WS users to register
 * InvocationListenerFactory implementations. This will manage
 * the factory implementations such that they list of factories
 * is only built when necessary so as to reduce the overhead of
 * doing this on a per-request basis.
 *
 */
public class InvocationListenerRegistry {
    
    private static final Log log = LogFactory.getLog(InvocationListenerRegistry.class);
    
    // This is a collection of all the InvocationListenerFactory instances
    private static Collection<InvocationListenerFactory> factoryList = new ArrayList<InvocationListenerFactory>();
    
    /**
     * This method accepts an object instance that is an implementation of
     * the InvocationListenerFactory. The instance will be stored in an 
     * internal map by its class name.
     */
    public synchronized static void addFactory(InvocationListenerFactory facInstance) {
        if(log.isDebugEnabled()) {
            log.debug("Adding InvocationListenerFactory instance: " + facInstance.getClass().getName());
        }
        // Ensure only one instance of a specific factory class is registered.
        boolean found = false;
        for (Iterator<InvocationListenerFactory> iterator = factoryList.iterator(); iterator.hasNext();) {
            InvocationListenerFactory factory = iterator.next();
            if (facInstance.getClass() == factory.getClass()) {
                found = true;
            }
        }
        if(!found){
            factoryList.add(facInstance);
        }
    }
    
    /**
     * This method will return all the InvocationListenerFactory instances 
     * that have been registered.
     */
    public static Collection<InvocationListenerFactory> getFactories() {
        return factoryList;
    }

}
