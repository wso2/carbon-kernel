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
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;

import javax.xml.namespace.QName;

public class AxisMessageTest extends TestCase {

    public void testAxisMessage() throws Exception {
        String filename =
                AbstractTestCase.basedir + "/test-resources/deployment/AxisMessageTestRepo";
        AxisConfiguration er = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(filename, filename + "/axis2.xml")
                .getAxisConfiguration();

        assertNotNull(er);
        AxisService service = er.getService("MessagetestService");
        assertNotNull(service);
        AxisOperation op = service.getOperation(new QName("echoString"));
        assertNotNull(op);
        AxisMessage message = op.getMessage("In");
        assertNotNull(message);
        Parameter para = message.getParameter("messageIN");
        assertNotNull(para);
        assertEquals(para.getValue(), "messageIN");


        op = service.getOperation(new QName("echoStringArray"));
        assertNotNull(op);
        message = op.getMessage("In");
        assertNotNull(message);
        para = message.getParameter("messageIN");
        assertNotNull(para);
        assertEquals(para.getValue(), "messageIN");

        message = op.getMessage("Out");
        assertNotNull(message);
        para = message.getParameter("messageOut");
        assertNotNull(para);
        assertEquals(para.getValue(), "messageOut");

    }
}
