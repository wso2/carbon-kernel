/**
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

/**
 * SecServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2dev Oct 27, 2003 (02:34:09 EST) WSDL2Java emitter.
 */

package org.apache.ws.axis.samples.wssec.doall.axisSec;

public class SecServiceLocator extends org.apache.axis.client.Service implements org.apache.ws.axis.samples.wssec.doall.axisSec.SecService {

    // Use to get a proxy class for SecHttp
    private java.lang.String SecHttp_address = "http://localhost:8081/axis/services/secHttp";

    public java.lang.String getSecHttpAddress() {
        return SecHttp_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String SecHttpWSDDServiceName = "SecHttp";

    public java.lang.String getSecHttpWSDDServiceName() {
        return SecHttpWSDDServiceName;
    }

    public void setSecHttpWSDDServiceName(java.lang.String name) {
        SecHttpWSDDServiceName = name;
    }

    public org.apache.ws.axis.samples.wssec.doall.axisSec.SecPort getSecHttp() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(SecHttp_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getSecHttp(endpoint);
    }

    public org.apache.ws.axis.samples.wssec.doall.axisSec.SecPort getSecHttp(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            org.apache.ws.axis.samples.wssec.doall.axisSec.SecBindingStub _stub = new org.apache.ws.axis.samples.wssec.doall.axisSec.SecBindingStub(portAddress, this);
            _stub.setPortName(getSecHttpWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setSecHttpEndpointAddress(java.lang.String address) {
        SecHttp_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (org.apache.ws.axis.samples.wssec.doall.axisSec.SecPort.class.isAssignableFrom(serviceEndpointInterface)) {
                org.apache.ws.axis.samples.wssec.doall.axisSec.SecBindingStub _stub = new org.apache.ws.axis.samples.wssec.doall.axisSec.SecBindingStub(new java.net.URL(SecHttp_address), this);
                _stub.setPortName(getSecHttpWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        String inputPortName = portName.getLocalPart();
        if ("SecHttp".equals(inputPortName)) {
            return getSecHttp();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("uri:axis_sec", "secService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("SecHttp"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        if ("SecHttp".equals(portName)) {
            setSecHttpEndpointAddress(address);
        }
        else { // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
