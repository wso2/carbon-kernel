/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.axis2.transport.testkit.axis2.endpoint;

import javax.mail.internet.ContentType;

import org.apache.axis2.description.AxisService;
import org.apache.axis2.transport.testkit.axis2.AxisServiceConfigurator;
import org.apache.axis2.transport.testkit.client.ClientOptions;
import org.apache.axis2.transport.testkit.tests.Setup;
import org.apache.axis2.transport.testkit.tests.Transient;

public class ContentTypeServiceConfigurator implements AxisServiceConfigurator {
    private final String parameterName;
    private @Transient ContentType contentType;
    
    public ContentTypeServiceConfigurator(String parameterName) {
        this.parameterName = parameterName;
    }

    @Setup @SuppressWarnings("unused")
    private void setUp(ClientOptions options) throws Exception {
        contentType = options.getTransportContentType();
    }

    public void setupService(AxisService service, boolean isClientSide) throws Exception {
        service.addParameter(parameterName, contentType.toString());
    }
}
