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

package org.apache.axis2.jaxws.lifecycle;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.injection.ResourceInjectionTestImpl1;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.server.endpoint.lifecycle.EndpointLifecycleManager;
import org.apache.axis2.jaxws.server.endpoint.lifecycle.factory.EndpointLifecycleManagerFactory;
import org.apache.axis2.jaxws.unitTest.TestLogger;

/**
 * Unit Tests for lifecycle creatoin
 *
 */
public class EndpointLifecycleTests extends TestCase {
    Object endpointInstance = new ResourceInjectionTestImpl1();
    Object badObject = new Object();
    public EndpointLifecycleTests() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param arg0
     */
    public EndpointLifecycleTests(String arg0) {
        super(arg0);
        // TODO Auto-generated constructor stub
    }

    public void testPostConstruct(){
        TestLogger.logger.debug("------------------------------");
        TestLogger.logger.debug("Test : " + getName());
        try{
            EndpointLifecycleManagerFactory elmf = (EndpointLifecycleManagerFactory)FactoryRegistry.getFactory(EndpointLifecycleManagerFactory.class);
            assertNotNull(elmf);
            EndpointLifecycleManager elm = elmf.createEndpointLifecycleManager(endpointInstance);
            assertNotNull(elmf);
            elm.invokePostConstruct();
            TestLogger.logger.debug("------------------------------");
        }catch(Exception e){
            fail(e.getMessage());
        }
    }

    public void testPreDestroy(){
        TestLogger.logger.debug("------------------------------");
        TestLogger.logger.debug("Test : " + getName());
        try{
            EndpointLifecycleManagerFactory elmf = (EndpointLifecycleManagerFactory)FactoryRegistry.getFactory(EndpointLifecycleManagerFactory.class);
            assertNotNull(elmf);
            EndpointLifecycleManager elm = elmf.createEndpointLifecycleManager(endpointInstance);
            assertNotNull(elm);
            elm.invokePreDestroy();
            TestLogger.logger.debug("------------------------------");
        }catch(Exception e){
            fail(e.getMessage());
        }
    }

    public void testBadPostConstruct(){
        TestLogger.logger.debug("------------------------------");
        TestLogger.logger.debug("Test : " + getName());
        try{
            EndpointLifecycleManagerFactory elmf = (EndpointLifecycleManagerFactory)FactoryRegistry.getFactory(EndpointLifecycleManagerFactory.class);
            assertNotNull(elmf);
            EndpointLifecycleManager elm = elmf.createEndpointLifecycleManager(badObject);
            assertNotNull(elmf);
            elm.invokePostConstruct();
            TestLogger.logger.debug("------------------------------");
        }catch(Exception e){
            fail(e.getMessage());
        }
    }

    public void testBadPreDestroy(){
        TestLogger.logger.debug("------------------------------");
        TestLogger.logger.debug("Test : " + getName());
        try{
            EndpointLifecycleManagerFactory elmf = (EndpointLifecycleManagerFactory)FactoryRegistry.getFactory(EndpointLifecycleManagerFactory.class);
            assertNotNull(elmf);
            EndpointLifecycleManager elm = elmf.createEndpointLifecycleManager(badObject);
            assertNotNull(elm);
            elm.invokePreDestroy();
            TestLogger.logger.debug("------------------------------");
        }catch(Exception e){
            fail(e.getMessage());
        }
    }
}
