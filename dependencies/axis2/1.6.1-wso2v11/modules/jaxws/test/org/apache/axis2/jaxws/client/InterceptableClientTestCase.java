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

package org.apache.axis2.jaxws.client;

import org.apache.axis2.jaxws.core.controller.InvocationControllerFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.custommonkey.xmlunit.XMLTestCase;

/**
 * This TestCase can be extended to write JAX-WS client side unit tests without 
 * having to have an end-to-end test.  The <source>TestClientInvocationController</source>
 * will be used to capture the request.  
 */
public class InterceptableClientTestCase extends XMLTestCase {

    private InvocationControllerFactory oldFactory;
    private TestClientInvocationControllerFactory newFactory;
    private TestClientInvocationController testController;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        InvocationControllerFactory icf = (InvocationControllerFactory) FactoryRegistry.getFactory(InvocationControllerFactory.class);
        oldFactory = icf;
        
        testController = new TestClientInvocationController();
        
        newFactory = new TestClientInvocationControllerFactory();
        newFactory.setInvocationController(testController);
        
        FactoryRegistry.setFactory(InvocationControllerFactory.class, newFactory);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        FactoryRegistry.setFactory(InvocationControllerFactory.class, oldFactory);
    }
    
    protected TestClientInvocationController getInvocationController() {
        return testController;
    }
    
}
