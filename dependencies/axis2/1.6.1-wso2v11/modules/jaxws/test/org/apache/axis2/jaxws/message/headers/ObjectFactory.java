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

package org.apache.axis2.jaxws.message.headers;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


@XmlRegistry
public class ObjectFactory {

    private final static QName _ConfigHeader1_QNAME = new QName("http://headers.message.jaxws.axis2.apache.org/types4", "ConfigHeader1");
    private final static QName _ConfigHeader3_QNAME = new QName("http://headers.message.jaxws.axis2.apache.org/types4", "ConfigHeader3");
    private final static QName _ConfigHeader2_QNAME = new QName("http://headers.message.jaxws.axis2.apache.org/types4", "ConfigHeader2");
    private final static QName _ConfigBody_QNAME = new QName("http://headers.message.jaxws.axis2.apache.org/types4", "ConfigBody");

    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ConfigHeader }
     * 
     */
    public ConfigHeader createConfigHeader() {
        return new ConfigHeader();
    }
    
    /**
     * Create an instance of {@link ConfigHeader }
     * 
     */
    public ConfigBody createConfigBody() {
        return new ConfigBody();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ConfigBody }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://headers.message.jaxws.axis2.apache.org/types4", name = "ConfigBody")
    public JAXBElement<ConfigBody> createConfigBody(ConfigBody value) {
        return new JAXBElement<ConfigBody>(_ConfigBody_QNAME, ConfigBody.class, null, value);
    }
    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ConfigHeader }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://headers.message.jaxws.axis2.apache.org/types4", name = "ConfigHeader1")
    public JAXBElement<ConfigHeader> createConfigHeader1(ConfigHeader value) {
        return new JAXBElement<ConfigHeader>(_ConfigHeader1_QNAME, ConfigHeader.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ConfigHeader }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://headers.message.jaxws.axis2.apache.org/types4", name = "ConfigHeader3")
    public JAXBElement<ConfigHeader> createConfigHeader3(ConfigHeader value) {
        return new JAXBElement<ConfigHeader>(_ConfigHeader3_QNAME, ConfigHeader.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ConfigHeader }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://headers.message.jaxws.axis2.apache.org/types4", name = "ConfigHeader2")
    public JAXBElement<ConfigHeader> createConfigHeader2(ConfigHeader value) {
        return new JAXBElement<ConfigHeader>(_ConfigHeader2_QNAME, ConfigHeader.class, null, value);
    }

}
