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

package org.apache.axis2.jaxws.spi.handler;

import org.apache.axis2.jaxws.unitTest.TestLogger;

import javax.annotation.PostConstruct;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;

public class DummyLogicalHandler implements LogicalHandler<LogicalMessageContext> {
    
    @PostConstruct
    public void setup() {
        TestLogger.logger.debug("@PostConstruct method invoked.");
    }

	public boolean handleMessage(LogicalMessageContext context) {
		return true;
	}

	public boolean handleFault(LogicalMessageContext messagecontext) {
		return false;
	}

	public void close(MessageContext messagecontext){
	}
}
