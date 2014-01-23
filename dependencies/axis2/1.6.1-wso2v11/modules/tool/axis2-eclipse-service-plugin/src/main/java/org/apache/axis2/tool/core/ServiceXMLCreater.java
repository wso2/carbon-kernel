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

package org.apache.axis2.tool.core;

import java.util.ArrayList;

public class ServiceXMLCreater {
    private String serviceName;
    private String serviceClass;
    private ArrayList operations;

    public ServiceXMLCreater(String serviceName, String serviceClass, ArrayList operations) {
        this.serviceName = serviceName;
        this.serviceClass = serviceClass;
        this.operations = operations;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getServiceClass() {
        return serviceClass;
    }

    public ArrayList getOperations() {
        return operations;
    }

    public String toString() {
        String serviceXML = "<service name=\"" + serviceName + "\" >\n" +
        "\t<description>\n" +
        "\t\tPlease Type your service description here\n" +
        "\t</description>\n" +
        "\t<messageReceivers>\n" +
        "\t\t<messageReceiver mep=\"http://www.w3.org/2004/08/wsdl/in-only\" class=\"org.apache.axis2.rpc.receivers.RPCInOnlyMessageReceiver\" />\n" +
        "\t\t<messageReceiver  mep=\"http://www.w3.org/2004/08/wsdl/in-out\"  class=\"org.apache.axis2.rpc.receivers.RPCMessageReceiver\"/>\n" +
        "\t</messageReceivers>\n" + 
        "\t<parameter name=\"ServiceClass\">" + serviceClass + "</parameter>\n" ; 
        serviceXML = serviceXML + "</service>\n";
        return serviceXML;
    }

}

