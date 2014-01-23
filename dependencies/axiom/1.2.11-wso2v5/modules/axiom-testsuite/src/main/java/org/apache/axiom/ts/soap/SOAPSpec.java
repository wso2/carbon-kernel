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
package org.apache.axiom.ts.soap;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMMetaFactory;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP11Version;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAP12Version;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPVersion;

/**
 * Describes the characteristics of a given SOAP version. This is similar to {@link SOAPVersion},
 * but is designed specifically for the test suite.
 */
public abstract class SOAPSpec {
    public static final SOAPSpec SOAP11 = new SOAPSpec(SOAP11Version.getSingleton()) {
        public String getName() {
            return "soap11";
        }
        
        public SOAPFactory getFactory(OMMetaFactory metaFactory) {
            return metaFactory.getSOAP11Factory();
        }

        public SOAPFactory getAltFactory(OMMetaFactory metaFactory) {
            return metaFactory.getSOAP12Factory();
        }

        public String getEnvelopeNamespaceURI() {
            return SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;
        }
    };

    public static final SOAPSpec SOAP12 = new SOAPSpec(SOAP12Version.getSingleton()) {
        public String getName() {
            return "soap12";
        }
        
        public SOAPFactory getFactory(OMMetaFactory metaFactory) {
            return metaFactory.getSOAP12Factory();
        }

        public SOAPFactory getAltFactory(OMMetaFactory metaFactory) {
            return metaFactory.getSOAP11Factory();
        }

        public String getEnvelopeNamespaceURI() {
            return SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI;
        }
    };
    
    private final SOAPVersion version;
    
    public SOAPSpec(SOAPVersion version) {
        this.version = version;
    }
    
    public abstract String getName();
    public abstract SOAPFactory getFactory(OMMetaFactory metaFactory);
    public abstract SOAPFactory getAltFactory(OMMetaFactory metaFactory);
    public abstract String getEnvelopeNamespaceURI();
    
    public QName getFaultCodeQName() {
        return version.getFaultCodeQName();
    }
    
    public QName getFaultReasonQName() {
        return version.getFaultReasonQName();
    }

    public QName getFaultRoleQName() {
        return version.getFaultRoleQName();
    }

    public QName getFaultDetailQName() {
        return version.getFaultDetailQName();
    }

    public String getNextRoleURI() {
        return version.getNextRoleURI();
    }
}
