/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.carbon.jndi.osgi.factories;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

/**
 * This ServiceFactory implementation is used to test the code which retrieve the caller's BundleContext code in
 * {@code DefaultContextFactory}
 */
public class BundleContextICFServiceFactory implements ServiceFactory<FooInitialContextFactory> {

    private BundleContext bundleContext;

    public BundleContext getFirstConsumersBundleContext() {
        return bundleContext;
    }

    @Override
    public FooInitialContextFactory getService(
            Bundle bundle,
            ServiceRegistration<FooInitialContextFactory> serviceRegistration) {

        if (bundleContext == null) {
            bundleContext = bundle.getBundleContext();
        }
        return new FooInitialContextFactory();
    }

    @Override
    public void ungetService(
            Bundle bundle,
            ServiceRegistration<FooInitialContextFactory> serviceRegistration,
            FooInitialContextFactory fooInitialContextFactory) {

    }
}
