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

package org.apache.axis2.jaxws.server.endpoint.injection.factory;

import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.injection.ResourceInjectionException;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.server.endpoint.injection.ResourceInjector;

/**
 * This class is is responsible for creating instances that can 
 * handle resource injection.
 *
 */
public class ResourceInjectionFactory {

    /**
     *
     */
    public ResourceInjectionFactory() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * This method retrieves the appropriate ResourceInjector instance
     * based on the type that is supplied.
     * 
     */
    public static ResourceInjector createResourceInjector(Class resourceType)
        throws ResourceInjectionException {
        Object obj = FactoryRegistry.getFactory(resourceType);
        ResourceInjector injector = null;

        // make sure we have a ResourceInjector instance
        if (obj instanceof ResourceInjector) {
            injector = (ResourceInjector) obj;
        }

        if (injector == null) {
            throw new ResourceInjectionException(Messages.getMessage("ResourceInjectionFactoryErr1"));
        }

        return injector;
    }
}
