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

package org.apache.axis2.transport.testkit.message;

import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.transport.http.HTTPConstants;

public class XMLMessage {
    public enum Type {
        SOAP11(SOAP11Constants.SOAP_11_CONTENT_TYPE),
        SOAP12(SOAP12Constants.SOAP_12_CONTENT_TYPE),
        POX("application/xml"),
        SWA(HTTPConstants.MEDIA_TYPE_MULTIPART_RELATED);
        
        private final String contentType;
        
        private Type(String contentType) {
            this.contentType = contentType;
        }
        
        public ContentType getContentType() {
            try {
                return new ContentType(contentType);
            } catch (ParseException ex) {
                throw new Error(ex);
            }
        }
    }
    
    private final Type type;
    private final OMElement payload;
    private final Attachments attachments;
    
    public XMLMessage(OMElement payload, Type type, Attachments attachments) {
        this.payload = payload;
        this.type = type;
        this.attachments = attachments;
    }

    public XMLMessage(OMElement payload, Type type) {
        this(payload, type, null);
    }
    
    public Type getType() {
        return type;
    }

    public OMElement getPayload() {
        return payload;
    }
    
    public OMElement getMessageElement() {
        if (type == Type.POX) {
            return payload;
        } else {
            SOAPFactory factory;
            if (type == Type.SOAP11) {
                factory = OMAbstractFactory.getSOAP11Factory();
            } else {
                factory = OMAbstractFactory.getSOAP12Factory();
            }
            SOAPEnvelope envelope = factory.getDefaultEnvelope();
            envelope.getBody().addChild(payload);
            return envelope;
        }
    }

    public Attachments getAttachments() {
        return attachments;
    }
    
    public static Type getTypeFromContentType(ContentType contentType) {
        String baseType = contentType.getBaseType();
        Type type = null;
        for (Type candidate : Type.values()) {
            if (candidate.getContentType().getBaseType().equalsIgnoreCase(baseType)) {
                type = candidate;
                break;
            }
        }
        return type;
    }
}
