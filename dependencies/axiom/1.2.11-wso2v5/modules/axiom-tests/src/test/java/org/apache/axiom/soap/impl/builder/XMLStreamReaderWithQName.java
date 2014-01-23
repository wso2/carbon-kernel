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

package org.apache.axiom.soap.impl.builder;

import org.apache.axiom.soap.SOAPConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;

/**
 * Used by StAXSOAPModelBuilderTest to simulate 
 *
 */
public class XMLStreamReaderWithQName extends StreamReaderDelegate {

    private QName soapBodyFirstChildElementQName;
    private boolean readBody = false;
    
    
    public XMLStreamReaderWithQName(XMLStreamReader reader, 
                                 QName soapBodyFirstChildElementQName) {
        super(reader);
        this.soapBodyFirstChildElementQName = soapBodyFirstChildElementQName;
    }

    public String getLocalName() {
        String localName = super.getLocalName();
        if (localName.equals("Body")) {
            this.readBody = true;
        }
        return localName;
    }


    public boolean isReadBody() {
        return readBody;
    }

    public Object getProperty(String arg0) throws IllegalArgumentException {
        // Return the qname
        if (arg0.equals(SOAPConstants.SOAPBODY_FIRST_CHILD_ELEMENT_QNAME)) {
            return this.soapBodyFirstChildElementQName;
        }
        return super.getProperty(arg0);
    }
}
