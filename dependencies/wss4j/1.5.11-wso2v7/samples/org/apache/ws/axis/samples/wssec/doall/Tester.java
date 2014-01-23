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

package org.apache.ws.axis.samples.wssec.doall;

import org.apache.axis.utils.Options;
import org.apache.ws.axis.samples.wssec.doall.axisSec.SecPort;
import org.apache.ws.axis.samples.wssec.doall.axisSec.SecServiceLocator;


public class Tester
{

    private static final java.lang.String address =
    "http://localhost:8081/axis/services/SecHttp";


    public static void main(String [] args) throws Exception {
        Options opts = new Options(args);

        /*
          * Start to prepare service call. Once this is done, several
          * calls can be made on the port (see below)
         *
          * Fist: get the service locator. This implements the functionality
          * to get a client stub (aka port).
          */
        SecServiceLocator service = new SecServiceLocator();
        /* 
          * this is a JAX-RPC compliant call. It uses a preconfigured
          * endpoint address (usually contained in the WSDL). Note the
          * cast.
          *
        SecPort port = (SwaPort)service.getPort(SwaPortType.class);
         */

        /*
          * Here we use an Axis specific call that allows to override the
          * port address (service endpoint address) with an own URL. Comes
          * in handy for testing.
          */
        java.net.URL endpoint;
            try {
                endpoint = new java.net.URL(address);
            }
            catch (java.net.MalformedURLException e) {
                throw new javax.xml.rpc.ServiceException(e);
            }

        SecPort port = (SecPort)service.getSecHttp(endpoint);
        /*
          * At this point all preparations are done. Using the port we can
          * now perform as many calls as necessary.
          */
    
         // perform call
        String result = port.secSend("AppName");
        System.out.println(result);
        
        if (opts.isFlagSet('t') > 0) {
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < 20; i++)
                port.secSend("AppName");
            long endTime = System.currentTimeMillis();
            System.out.println("Time used: " + (endTime - startTime) + "ms");
        }
    }
}