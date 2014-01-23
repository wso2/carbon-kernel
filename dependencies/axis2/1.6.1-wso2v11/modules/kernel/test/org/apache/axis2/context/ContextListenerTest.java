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

package org.apache.axis2.context;

import junit.framework.TestCase;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.axis2.engine.AxisConfiguration;

import javax.xml.namespace.QName;

/**
 * 
 */
public class ContextListenerTest extends TestCase {
    static class MyListener implements ContextListener {
        private AbstractContext lastContext;

        public void contextCreated(AbstractContext context) {
            lastContext = context;
        }

        public void contextRemoved(AbstractContext context) {
            //TODO: Method implementation

        }

        public AbstractContext getLastContext() {
            return lastContext;
        }
    }

    /**
     * Confirm that creating contexts at various levels correctly causes notifications to
     * ContextListeners that are registered on a given ConfigurationContext.
     *
     * @throws Exception if an error occurs
     */
    public void testContextListener() throws Exception {
        // Set up metadata
        AxisConfiguration axisConfig = new AxisConfiguration();
        ConfigurationContext configCtx = new ConfigurationContext(axisConfig);
        AxisServiceGroup serviceGroup = new AxisServiceGroup(axisConfig);
        AxisService service = new AxisService("TestService");
        AxisOperation operation = new InOutAxisOperation(new QName("ns", "op1"));
        service.addOperation(operation);
        serviceGroup.addService(service);

        // Register a listener and make sure it starts out clean
        MyListener listener = new MyListener();
        configCtx.addContextListener(listener);
        assertNull(listener.getLastContext());

        MessageContext mc = configCtx.createMessageContext();
        assertNotNull(mc);
        assertEquals("MessageContext not stored", mc, listener.getLastContext());

        ServiceGroupContext sgc = configCtx.createServiceGroupContext(serviceGroup);
        assertNotNull(sgc);
        assertEquals("ServiceGroupContext not stored", sgc, listener.getLastContext());

        ServiceContext sc = sgc.getServiceContext(service);
        assertNotNull(sc);
        assertEquals("ServiceContext not stored", sc, listener.getLastContext());

        OperationContext oc = sc.createOperationContext(operation);
        assertNotNull(oc);
        assertEquals("OperationContext not stored", oc, listener.getLastContext());

        // Try a second listener and make sure they both get notified
        MyListener listener2 = new MyListener();
        configCtx.addContextListener(listener2);

        mc = configCtx.createMessageContext();
        assertNotNull(mc);
        assertEquals("MessageContext not stored", mc, listener.getLastContext());
        assertEquals("MessageContext not stored in listener 2", mc, listener2.getLastContext());
    }
}
