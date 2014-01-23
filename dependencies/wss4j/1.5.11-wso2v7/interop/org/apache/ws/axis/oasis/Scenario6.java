/*
 * Copyright  2003-2004 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.ws.axis.oasis;

import org.apache.axis.utils.Options;
import org.apache.ws.axis.oasis.ping.PingPort;
import org.apache.ws.axis.oasis.ping.PingServiceLocator;

import javax.xml.rpc.holders.StringHolder;

/**
 * Class Scenario6
 */
public class Scenario6 {

    /** Field address */
    private static final java.lang.String address =
            "http://localhost:9080/axis/services/Ping6";

    /**
     * Method main
     * 
     * @param args 
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {

        Options opts = new Options(args);
        opts.setDefaultURL(address);

        /*
         *     Start to prepare service call. Once this is done, several
         *     calls can be made on the port (see below)
         *
         *     Fist: get the service locator. This implements the functionality
         *     to get a client stub (aka port).
         */
        PingServiceLocator service = new PingServiceLocator();

        /*
         *     this is a JAX-RPC compliant call. It uses a preconfigured
         *     endpoint address (usually contained in the WSDL). Note the
         *     cast.
         *    
         * SecPort port = (SwaPort)service.getPort(SwaPortType.class);
         */

        /*
         *     Here we use an Axis specific call that allows to override the
         *     port address (service endpoint address) with an own URL. Comes
         *     in handy for testing.
         */
        java.net.URL endpoint;

        try {
            endpoint = new java.net.URL(opts.getURL());
        } catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }

        PingPort port = (PingPort) service.getPing6(endpoint);

        /*
         *     At this point all preparations are done. Using the port we can
         *     now perform as many calls as necessary.
         */

        // perform call
        StringHolder text =
                new StringHolder("WSS4J - Scenario 6 text");
        port.ping(new org.apache.ws.axis.oasis.ping.TicketType("WSS4J"), text);
        System.out.println(text.value);

        if (opts.isFlagSet('t') > 0) {
            long startTime = System.currentTimeMillis();

            for (int i = 0; i < 20; i++) {
                port.ping(new org.apache.ws.axis.oasis.ping.TicketType("WSS4J"), text);
            }

            long endTime = System.currentTimeMillis();

            System.out.println("Time used: " + (endTime - startTime) + "ms");
        }
    }
}
