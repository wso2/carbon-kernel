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

/**
 * PingBindingImpl.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2dev Oct 27, 2003 (02:34:09 EST) WSDL2Java emitter.
 */

package org.apache.ws.axis.oasis.ping;

import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.handler.WSHandlerResult;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.handler.WSHandlerResult;

import javax.xml.rpc.holders.StringHolder;
import java.security.Principal;
import java.util.Vector;

public class PingBindingImpl
    implements org.apache.ws.axis.oasis.ping.PingPort {
    
    public void ping(
        org.apache.ws.axis.oasis.ping.TicketType pingTicket, 
        StringHolder text
    ) throws java.rmi.RemoteException {
        MessageContext msgContext = MessageContext.getCurrentContext();
        Message reqMsg = msgContext.getRequestMessage();

        Vector results = 
            (Vector) msgContext.getProperty(WSHandlerConstants.RECV_RESULTS);
        if (results == null) {
            System.out.println("No security results!!");
        }
        // System.out.println("Number of results: " + results.size());
        for (int i = 0; i < results.size(); i++) {
            WSHandlerResult rResult =
                (WSHandlerResult) results.get(i);
            Vector wsSecEngineResults = rResult.getResults();

            for (int j = 0; j < wsSecEngineResults.size(); j++) {
                WSSecurityEngineResult wser =
                    (WSSecurityEngineResult) wsSecEngineResults.get(j);
                int action = 
                    ((java.lang.Integer)wser.get(WSSecurityEngineResult.TAG_ACTION)).intValue();
                Principal principal = 
                    (Principal)wser.get(WSSecurityEngineResult.TAG_PRINCIPAL);
                if (action != WSConstants.ENCR && principal != null) {
                    // System.out.println(principal.getName());
                }
            }
        }
    }

}
