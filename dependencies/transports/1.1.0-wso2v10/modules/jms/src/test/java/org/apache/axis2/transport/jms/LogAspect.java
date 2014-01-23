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

package org.apache.axis2.transport.jms;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.apache.axis2.transport.jms.iowrappers.BytesMessageInputStream;
import org.apache.axis2.transport.testkit.util.LogManager;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Aspect
public class LogAspect {
    private static final Log log = LogFactory.getLog(LogAspect.class);
    
    @Before("(call(void javax.jms.MessageProducer.send(javax.jms.Message)) ||" +
    		" call(void javax.jms.TopicPublisher.publish(javax.jms.Message))) && args(message)")
    public void beforeSend(Message message) {
        try {
            OutputStream out = LogManager.INSTANCE.createLog("jms");
            try {
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(out), false);
                pw.println("Type: " + message.getClass().getName());
                pw.println("JMS message ID: " + message.getJMSMessageID());
                pw.println("JMS correlation ID: " + message.getJMSCorrelationID());
                pw.println("JMS reply to: " + message.getJMSReplyTo());
                for (Enumeration<?> e = message.getPropertyNames(); e.hasMoreElements(); ) {
                    String name = (String)e.nextElement();
                    pw.print(name);
                    pw.print(": ");
                    pw.println(message.getStringProperty(name));
                }
                pw.println();
                pw.flush();
                if (message instanceof BytesMessage) {
                    BytesMessage bytesMessage = (BytesMessage)message;
                    bytesMessage.reset();
                    IOUtils.copy(new BytesMessageInputStream(bytesMessage), out);
                } else if (message instanceof TextMessage) {
                    pw.print(((TextMessage)message).getText());
                    pw.flush();
                }
            } finally {
                out.close();
            }
        } catch (Throwable ex) {
            log.error("Failed to dump JMS message", ex);
        }
    }
}
