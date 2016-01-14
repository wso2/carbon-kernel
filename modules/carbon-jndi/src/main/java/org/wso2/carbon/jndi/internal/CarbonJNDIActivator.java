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

import org.apache.naming.java.javaURLContextFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;
import javax.naming.spi.NamingManager;
import java.util.Hashtable;

public class CarbonJNDIActivator implements BundleActivator {

    private static final Logger logger = LoggerFactory.getLogger(CarbonJNDIActivator.class);

//    static {
//        try {
//            NamingManager.setInitialContextFactoryBuilder(new CarbonInitialJNDIContextFactoryBuilder());
//        } catch (NamingException e) {
//            logger.error("Error occurred", e);
//        }
//    }

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        System.setProperty
                (javax.naming.Context.INITIAL_CONTEXT_FACTORY,
                        "org.apache.naming.java.javaURLContextFactory");

        logger.info("Setting up INITIAL_CONTEXT_FACTORY");

        Context ctx = new InitialContext();
        ctx.bind("user-name", "Sameera Jayasoma1234");
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {

    }


    private static class CarbonInitialJNDIContextFactoryBuilder implements
            InitialContextFactoryBuilder {

        @Override
        public InitialContextFactory createInitialContextFactory(Hashtable<?, ?> environment) throws NamingException {
            return new javaURLContextFactory();
        }
    }
}
