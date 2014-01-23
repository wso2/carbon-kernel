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

package org.apache.axis2.jaxws.provider;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;

import javax.xml.namespace.QName;
import java.util.Iterator;

/**
 * Plugin to remove "understood" headers for the SoapMessageMUProviderTests.  This class must
 * be configured in the axis2.xml file on both the client and the server. 
 */
public class SoapMessageMUProviderChecker extends org.apache.axis2.handlers.AbstractHandler {

    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
        // Get the list of headers for the roles we're acting in, then mark any we understand
        // as processed.
        SOAPEnvelope envelope = msgContext.getEnvelope();
        if (envelope.getHeader() == null) {
            return InvocationResponse.CONTINUE;
        }
        // These are the headers the test expects to be understood
        String ns1 = "http://ws.apache.org/axis2";
        String clientLocalName = "muclientunderstood";
        String serverLocalName = "muserverunderstood";

        QName clientQN = new QName(ns1, clientLocalName);
        QName serverQN = new QName(ns1, serverLocalName);

        Iterator headerBlocks = envelope.getHeader().getHeadersToProcess(null);
        while (headerBlocks.hasNext()) {
            SOAPHeaderBlock headerBlock = (SOAPHeaderBlock) headerBlocks.next();
            QName headerQN = headerBlock.getQName();
            if (clientQN.equals(headerQN)) {
//                System.out.println("Marking as processed CLIENT QN: " + clientQN);
                headerBlock.setProcessed();
            } else if (serverQN.equals(headerQN)) {
//                System.out.println("Marking as processed SERVER QN: " + serverQN);
                headerBlock.setProcessed();
            }
        }

        return InvocationResponse.CONTINUE;
    }
}
