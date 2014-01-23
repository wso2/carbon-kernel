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

package org.apache.axis2.deployment;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.integration.LocalTestCase;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.service.Lifecycle;

public class LifecycleTest extends LocalTestCase {
    static public class Service implements Lifecycle {
        static boolean initCalled, destroyCalled;

        public void init(ServiceContext context) throws AxisFault {
            initCalled = true;
        }

        public void destroy(ServiceContext context) {
            destroyCalled = true;
        }
    }

    public void testServiceObjectLifecycle() throws Exception {
        deployClassAsService("lifecycle", Service.class, Constants.SCOPE_APPLICATION);
        assertTrue("init() not called!", Service.initCalled);
    }
}
