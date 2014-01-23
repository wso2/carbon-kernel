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

package org.apache.axis2.description;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SpeakingHandler extends AbstractHandler implements Handler {
    private static final Log log = LogFactory.getLog(SpeakingHandler.class);
    private String message;
    private String name;

    public SpeakingHandler() {
        this.message = "Hi I amtesting ";
    }

    public String getName() {
        return name;
    }

    public InvocationResponse invoke(MessageContext msgContext) throws
            AxisFault {
        log.info("I am " + message + " Handler Running :)");
        return InvocationResponse.CONTINUE;
    }

    public void revoke(MessageContext msgContext) {
        log.info("I am " + message + " Handler Running :)");
    }

    public void setName(String name) {
        this.name = name;
    }

}
