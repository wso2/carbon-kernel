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

package org.apache.axis2.transport.mail;

import java.io.OutputStream;

import javax.mail.Message;

import org.apache.axis2.transport.testkit.util.LogManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Aspect
public class LogAspect {
    private static final Log log = LogFactory.getLog(LogAspect.class);
    
    @Before("call(void javax.mail.Transport.send(javax.mail.Message)) && args(message)")
    public void beforeSend(Message message) {
        try {
            OutputStream out = LogManager.INSTANCE.createLog("javamail");
            try {
                message.writeTo(out);
            } finally {
                out.close();
            }
        } catch (Throwable ex) {
            log.error("Failed to dump mail message", ex);
        }
    }
}
