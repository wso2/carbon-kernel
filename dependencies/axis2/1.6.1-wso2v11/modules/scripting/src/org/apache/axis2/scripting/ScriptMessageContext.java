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

package org.apache.axis2.scripting;

import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.scripting.convertors.OMElementConvertor;

import javax.xml.stream.XMLStreamException;

/**
 * ScriptMessageContext decorates the Axis2 MessageContext adding methods to use
 * the message payload XML in a way natural to the scripting language.
 */
public class ScriptMessageContext extends MessageContext {

	private static final long serialVersionUID = 1L;

	private MessageContext mc;

    private OMElementConvertor convertor;

    public ScriptMessageContext(MessageContext mc, OMElementConvertor convertor) {
        this.mc = mc;
        this.convertor = convertor;
    }

    /**
     * Get the XML representation of SOAP Body payload. The payload is the first
     * element inside the SOAP <Body> tags
     * 
     * @return the XML SOAP Body
     */
    public Object getPayloadXML() {
        return convertor.toScript(mc.getEnvelope().getBody().getFirstElement());
    }

    /**
     * Set the SOAP body payload from XML
     * 
     * @param payload
     * @throws XMLStreamException
     */
    public void setPayloadXML(Object payload) {
        mc.getEnvelope().getBody().setFirstChild(convertor.fromScript(payload));
    }

    /**
     * Get the XML representation of the complete SOAP envelope
     */
    public Object getEnvelopeXML() {
        return convertor.toScript(mc.getEnvelope());
    }

    // helpers to set EPRs from a script string

    public void setTo(String reference) {
        mc.setTo(new EndpointReference(reference));
    }

    public void setFaultTo(String reference) {
        mc.setFaultTo(new EndpointReference(reference));
    }

    public void setFrom(String reference) {
        mc.setFrom(new EndpointReference(reference));
    }

    public void setReplyTo(String reference) {
        mc.setReplyTo(new EndpointReference(reference));
    }

}
