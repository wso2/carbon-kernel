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

package org.apache.axiom.om.impl.dom.factory;

import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.AbstractOMMetaFactory;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.impl.dom.soap11.SOAP11Factory;
import org.apache.axiom.soap.impl.dom.soap12.SOAP12Factory;

/**
 * Meta factory for the DOOM implementation.
 * <p>
 * As explained in {@link OMDOMFactory}, OM factories for DOOM are not stateless.
 * Therefore {@link #getOMFactory()}, {@link #getSOAP11Factory()} and
 * {@link #getSOAP12Factory()} will return a new instance on every invocation.
 * 
 * @scr.component name="metafactory.dom.component" immediate="true"
 * @scr.service interface="org.apache.axiom.om.OMMetaFactory"
 * @scr.property name="implementationName" type="String" value="doom"
 */
public class OMDOMMetaFactory extends AbstractOMMetaFactory {
    public OMFactory getOMFactory() {
        return new OMDOMFactory(this);
    }

    public SOAPFactory getSOAP11Factory() {
        return new SOAP11Factory(this);
    }

    public SOAPFactory getSOAP12Factory() {
        return new SOAP12Factory(this);
    }
}
