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
package org.apache.axiom.ts.om.factory;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;

public abstract class OMElementCreator {
    public static final OMElementCreator[] INSTANCES = new OMElementCreator[] {
        new OMElementCreator("QName") {
            public OMElement createOMElement(OMFactory factory, QName qname) {
                return factory.createOMElement(qname);
            }
        },
        new OMElementCreator("QName,OMContainer") {
            public OMElement createOMElement(OMFactory factory, QName qname) {
                return factory.createOMElement(qname, null);
            }
        },
        new OMElementCreator("String,OMNamespace") {
            public OMElement createOMElement(OMFactory factory, QName qname) {
                return factory.createOMElement(qname.getLocalPart(),
                        factory.createOMNamespace(qname.getNamespaceURI(), qname.getPrefix()));
            }
        },
        new OMElementCreator("String,OMNamespace,OMContainer") {
            public OMElement createOMElement(OMFactory factory, QName qname) {
                return factory.createOMElement(qname.getLocalPart(),
                        factory.createOMNamespace(qname.getNamespaceURI(), qname.getPrefix()), null);
            }
        },
        new OMElementCreator("String,String,String") {
            public OMElement createOMElement(OMFactory factory, QName qname) {
                return factory.createOMElement(qname.getLocalPart(), qname.getNamespaceURI(), qname.getPrefix());
            }
        },
    };
    
    private final String name;
    
    public OMElementCreator(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    protected abstract OMElement createOMElement(OMFactory factory, QName qname);
}
