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

package samples.ping.client;


import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;

public class PingClient {
    private static EndpointReference targetEPR =
            new EndpointReference(
                    "http://localhost:8080/axis2/services/SamplePingService");

    private static final String PING_REQUEST = "pingRequest";
    private static final String PING_OPERATION = "operation";
    private static final String PING_SOAPACTION = "http://ws.apache.org/axis2/modules/ping";
    private static final String PING_NAMESPACE = "http://ws.apache.org/axis2/modules/ping/xsd";

    public PingClient() {

    }

    public static OMElement getPingRequest1(String [] operationList) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace(PING_NAMESPACE, "tns");

        OMElement pingRequest = fac.createOMElement(PING_REQUEST, omNs);

        for (int i = 0; i < operationList.length; i++) {
            OMElement operation = fac.createOMElement(PING_OPERATION, omNs);
            operation.addChild(fac.createOMText(operationList[i]));
            pingRequest.addChild(operation);
        }
        return pingRequest;
    }

    public static OMElement getPingRequest4() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace(PING_NAMESPACE, "tns");

        return fac.createOMElement(PING_REQUEST, omNs);
    }

    public static void sendRequest(ServiceClient sender, OMElement request, String description) throws AxisFault {
        System.out.println("Ping request - " + description);
        System.out.println(request.toString());
        OMElement result = sender.sendReceive(request);
        System.out.println("Ping response ");
        System.out.println(result.toString());
        System.out.println();
    }

    public static void main(String[] args) {
        try {
            Options options = new Options();
            options.setTo(targetEPR);
            options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
            options.setAction(PING_SOAPACTION);
         
	    ServiceClient sender = new ServiceClient();
            sender.setOptions(options);

            OMElement pingRequestPayload;

            pingRequestPayload = getPingRequest1(new String[]{"getPrice", "update"});
            sendRequest(sender, pingRequestPayload, "two operations are specified");

            pingRequestPayload = getPingRequest1(new String[]{"getPrice"});
            sendRequest(sender, pingRequestPayload, "one operation is specified");

            pingRequestPayload = getPingRequest1(new String[0]);
            sendRequest(sender, pingRequestPayload, "Service level");

            pingRequestPayload = getPingRequest4();
            sendRequest(sender, pingRequestPayload,"Service level");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
