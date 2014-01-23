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

package org.apache.axis2.jaxws.description;

import org.apache.axis2.jaxws.util.WSDLWrapper;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;

public interface ServiceDescriptionWSDL {
    public abstract WSDLWrapper getWSDLWrapper();

    public abstract WSDLWrapper getGeneratedWsdlWrapper();

    public Service getWSDLService();

    public Map getWSDLPorts();

    /**
     * Return a collection of WSDL ports under this service which use the portType QName.
     *
     * @param portTypeQN
     * @return
     */
    public List<Port> getWSDLPortsUsingPortType(QName portTypeQN);

    /**
     * Return a subset of the collection of WSDL ports which specify a SOAP 1.1 or 1.2 address.
     *
     * @param wsdlPorts
     * @return
     */
    public List<Port> getWSDLPortsUsingSOAPAddress(List<Port> wsdlPorts);

    public abstract String getWSDLLocation();

    public Definition getWSDLDefinition();

    public boolean isWSDLSpecified();

}