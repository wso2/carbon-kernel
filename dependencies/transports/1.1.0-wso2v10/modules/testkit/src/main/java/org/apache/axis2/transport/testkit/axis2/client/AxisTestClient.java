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

package org.apache.axis2.transport.testkit.axis2.client;

import javax.mail.internet.ContentType;
import javax.xml.namespace.QName;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

import org.apache.axiom.attachments.Attachments;
import org.apache.axis2.Constants;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.TransportSender;
import org.apache.axis2.transport.base.BaseConstants;
import org.apache.axis2.transport.base.ManagementSupport;
import org.apache.axis2.transport.testkit.MessageExchangeValidator;
import org.apache.axis2.transport.testkit.axis2.util.MessageLevelMetricsCollectorImpl;
import org.apache.axis2.transport.testkit.channel.Channel;
import org.apache.axis2.transport.testkit.client.ClientOptions;
import org.apache.axis2.transport.testkit.client.TestClient;
import org.apache.axis2.transport.testkit.message.AxisMessage;
import org.apache.axis2.transport.testkit.name.Name;
import org.apache.axis2.transport.testkit.tests.Setup;
import org.apache.axis2.transport.testkit.tests.TearDown;
import org.apache.axis2.transport.testkit.tests.Transient;
import org.apache.axis2.transport.testkit.util.ContentTypeUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Name("axis")
public class AxisTestClient implements TestClient, MessageExchangeValidator {
    private static final Log log = LogFactory.getLog(AxisTestClient.class);
    
    private @Transient AxisTestClientConfigurator[] configurators;
    private @Transient TransportSender sender;
    protected @Transient ServiceClient serviceClient;
    protected @Transient Options axisOptions;
    private long messagesSent;
    private long bytesSent;
    private MessageLevelMetricsCollectorImpl metrics;
    
    @Setup @SuppressWarnings("unused")
    private void setUp(AxisTestClientContext context, Channel channel, AxisTestClientConfigurator[] configurators) throws Exception {
        this.configurators = configurators;
        sender = context.getSender();
        serviceClient = new ServiceClient(context.getConfigurationContext(), null);
        axisOptions = new Options();
        axisOptions.setTo(channel.getEndpointReference());
        serviceClient.setOptions(axisOptions);
    }
    
    @TearDown @SuppressWarnings("unused")
    private void tearDown() throws Exception {
        serviceClient.cleanup();
    }

    public ContentType getContentType(ClientOptions options, ContentType contentType) {
        // TODO: this may be incorrect in some cases
        String charset = options.getCharset();
        if (charset == null) {
            return contentType;
        } else {
            return ContentTypeUtil.addCharset(contentType, options.getCharset());
        }
    }

    public void beforeSend() throws Exception {
        if (sender instanceof ManagementSupport) {
            ManagementSupport sender = (ManagementSupport)this.sender;
            messagesSent = sender.getMessagesSent();
            bytesSent = sender.getBytesSent();
            metrics = new MessageLevelMetricsCollectorImpl();
        } else {
            metrics = null;
        }
    }

    protected MessageContext send(ClientOptions options, AxisMessage message, QName operationQName,
            boolean block, String resultMessageLabel) throws Exception {
        
        OperationClient mepClient = serviceClient.createClient(operationQName);
        MessageContext mc = new MessageContext();
        mc.setProperty(Constants.Configuration.MESSAGE_TYPE, message.getMessageType());
        mc.setEnvelope(message.getEnvelope());
        Attachments attachments = message.getAttachments();
        if (attachments != null) {
            mc.setAttachmentMap(attachments);
            mc.setDoingSwA(true);
            mc.setProperty(Constants.Configuration.ENABLE_SWA, true);
        }
        for (AxisTestClientConfigurator configurator : configurators) {
            configurator.setupRequestMessageContext(mc);
        }
        mc.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING, options.getCharset());
        mc.setServiceContext(serviceClient.getServiceContext());
        if (metrics != null) {
            mc.setProperty(BaseConstants.METRICS_COLLECTOR, metrics);
        }
        mepClient.addMessageContext(mc);
        mepClient.execute(block);
//        mepClient.complete(mc);
        return resultMessageLabel == null ? null : mepClient.getMessageContext(resultMessageLabel);
    }

    public void afterReceive() throws Exception {
        if (sender instanceof ManagementSupport) {
            ManagementSupport sender = (ManagementSupport)this.sender;
            synchronized (metrics) {
                long start = System.currentTimeMillis();
                while (true) {
                    try {
                        Assert.assertEquals(1, metrics.getMessagesSent());
                        Assert.assertEquals(messagesSent+1, sender.getMessagesSent());
                        long thisBytesSent = metrics.getBytesSent();
                        Assert.assertTrue("No increase in bytes sent in message level metrics", thisBytesSent != 0);
                        long newBytesSent = sender.getBytesSent();
                        Assert.assertTrue("No increase in bytes sent in transport level metrics", newBytesSent > bytesSent);
                        Assert.assertEquals("Mismatch between message and transport level metrics", thisBytesSent, newBytesSent - bytesSent);
                        break;
                    } catch (AssertionFailedError ex) {
                        // SYNAPSE-491: Maybe the transport sender didn't finish updating the
                        // metrics yet. We give it a couple of seconds to do so.
                        long remaining = start + 5000 - System.currentTimeMillis();
                        if (remaining <= 0) {
                            throw ex;
                        } else {
                            log.debug("The transport sender didn't update the metrics yet ("
                                    + ex.getMessage() + "). Waiting for " + remaining + " ms.");
                            metrics.wait(remaining);
                        }
                    }
                }
            }
            log.debug("Message level metrics check OK");
        }
    }
}
