/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.base;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * ICF wrapper class for Carbon.
 *
 * This class itself can not create any initial context. It makes use of an already registered provider ICF instance
 * to create an initial context. The desired provider ICF name should be specified using the environment variable
 * provider.naming.factory.initial, which is the fully qualified class name of the provider.
 */
public class CarbonInitialContextFactory implements InitialContextFactory {

    private static Map providers = new HashMap();
    private final String PROVIDER_NAME_PARAMETER = "provider.naming.factory.initial";
    private final String DEFAULT_PROVIDER_NAME = "org.wso2.carbon.qpid.client.jndi.CarbonRegistryInitialContextFactory";

    /**
        *  Create initial context using the given provider ICF name. If the provider ICF name is not given
        *  the default provider is used.
        *
        * @param environment
        *                                   Initial context properties
        * @return Created IC
        * @throws NamingException
        */
    public Context getInitialContext(Hashtable environment) throws NamingException {
        // Get provider name
        Object providerName = environment.get(PROVIDER_NAME_PARAMETER);
        InitialContextFactory providerICF =
                (InitialContextFactory)((providerName != null) ? providers.get(providerName) :
                        providers.get(DEFAULT_PROVIDER_NAME));

        if (null == providerICF) {
            throw new NamingException("Provider Initial Context Factories not found");
        }

        // Create IC
        return providerICF.getInitialContext(environment);
    }

    /**
        * Register a provider ICF instance
        *  
        * @param providerName
        *                                   Fully qualified class name of the provider
        * @param providerICF
        *                                   Provider instance
        */
    public static void registerProviderICF(String providerName, InitialContextFactory providerICF) {
        providers.put(providerName, providerICF);
    }
}
