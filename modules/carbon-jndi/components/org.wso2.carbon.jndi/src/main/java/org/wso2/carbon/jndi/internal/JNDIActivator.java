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
package org.wso2.carbon.jndi.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.jndi.JNDIContextManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.jndi.internal.osgi.JNDIContextManagerServiceFactory;
import org.wso2.carbon.jndi.internal.osgi.builders.DefaultContextFactoryBuilder;
import org.wso2.carbon.jndi.internal.osgi.builders.DefaultObjectFactoryBuilder;
import org.wso2.carbon.jndi.java.javaURLContextFactory;

import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.NamingManager;
import javax.naming.spi.ObjectFactory;
import java.util.Dictionary;
import java.util.Hashtable;

public class JNDIActivator implements BundleActivator {

    private static final Logger logger = LoggerFactory.getLogger(JNDIActivator.class);

    @Override
    public void start(BundleContext bundleContext) throws Exception {

        try {
            NamingManager.setInitialContextFactoryBuilder(new DefaultContextFactoryBuilder());
            NamingManager.setObjectFactoryBuilder(new DefaultObjectFactoryBuilder());

            Dictionary<String, String> propertyMap = new Hashtable<>();
            propertyMap.put("osgi.jndi.url.scheme", "java");

            bundleContext.registerService(ObjectFactory.class, new javaURLContextFactory(), propertyMap);

            // InitialContextFactory Provider should be registered with its implementation class as well as the
            // InitialContextFactory class.
            bundleContext.registerService(new String[]{javaURLContextFactory.class.getName(),
                    InitialContextFactory.class.getName()}, new javaURLContextFactory(), null);

            logger.info("Setting up INITIAL_CONTEXT_FACTORY");
            bundleContext.registerService(JNDIContextManager.class, new JNDIContextManagerServiceFactory(), null);

        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {

    }
}
