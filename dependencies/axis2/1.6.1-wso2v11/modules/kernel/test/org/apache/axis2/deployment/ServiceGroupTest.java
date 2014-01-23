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

import junit.framework.TestCase;
import org.apache.axis2.AbstractTestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.engine.AxisConfiguration;

public class ServiceGroupTest extends TestCase {
    AxisConfiguration ar;
    String repo = AbstractTestCase.basedir + "/test-resources/deployment/serviceGroupRepo";


    protected void setUp() throws Exception {
        ar = ConfigurationContextFactory.createConfigurationContextFromFileSystem(repo, repo + "/axis2.xml")
                .getAxisConfiguration();
    }

    public void testServiceGroup() throws AxisFault {
        AxisServiceGroup sgd = ar.getServiceGroup("serviceGroup");
        assertNotNull(sgd);
        AxisService service1 = ar.getService("service1");
        assertNotNull(service1);
        AxisService service2 = ar.getService("service2");
        assertNotNull(service2);
    }

}
