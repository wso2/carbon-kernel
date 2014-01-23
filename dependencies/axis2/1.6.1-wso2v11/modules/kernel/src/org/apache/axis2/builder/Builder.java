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

package org.apache.axis2.builder;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;

import java.io.InputStream;

/**
 * Message builder able to convert a byte stream into a SOAP infoset.
 * Message builders are used by {@link org.apache.axis2.transport.TransportListener}
 * implementations to process the raw payload of the message and turn it into SOAP.
 * Transports should use
 * {@link org.apache.axis2.builder.BuilderUtil#getBuilderFromSelector(String, MessageContext)}
 * to select the message builder appropriate for the content type of the message.
 */
public interface Builder {

    /**
     * Process a message.
     * <p>
     * The raw content of the message is provided as an input stream. It is the responsibility
     * of the caller (typically a transport implementation) to close the stream after the
     * message has been processed (more precisely after the SOAP infoset returned by this method
     * is no longer used). This implies that implementations are not required to consume the
     * input stream during the execution of this method. This enables deferred parsing of the
     * message.
     * 
     * @param inputStream the byte stream with the raw payload
     * @param contentType
     * @param messageContext
     * @return The SOAP infoset for the given message.
     */
    public OMElement processDocument(InputStream inputStream, String contentType,
                                     MessageContext messageContext) throws AxisFault;
}
