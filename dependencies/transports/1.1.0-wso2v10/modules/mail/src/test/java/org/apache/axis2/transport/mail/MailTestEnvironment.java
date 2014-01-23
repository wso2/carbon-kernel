/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.axis2.transport.mail;

import java.util.Map;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.ParameterInclude;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.transport.testkit.axis2.TransportDescriptionFactory;
import org.apache.axis2.transport.testkit.name.Key;

@Key("server")
public abstract class MailTestEnvironment implements TransportDescriptionFactory {
    public static class Account {
        private final String address;
        private final String login;
        private final String password;
        
        public Account(String address, String login, String password) {
            this.address = address;
            this.login = login;
            this.password = password;
        }

        public String getAddress() {
            return address;
        }

        public String getLogin() {
            return login;
        }

        public String getPassword() {
            return password;
        }
    };
    
    public abstract String getProtocol();
    
    public abstract Account allocateAccount() throws Exception;
    
    public abstract void freeAccount(Account account);
    
    public abstract Map<String,String> getInProperties(Account account);
    
    public abstract Map<String,String> getOutProperties();

    public TransportInDescription createTransportInDescription() throws Exception {
        TransportInDescription trpInDesc = new TransportInDescription(MailConstants.TRANSPORT_NAME);
        trpInDesc.setReceiver(new MailTransportListener());
        return trpInDesc;
    }

    public TransportOutDescription createTransportOutDescription() throws Exception {
        TransportOutDescription trpOutDesc = new TransportOutDescription(MailConstants.TRANSPORT_NAME);
        trpOutDesc.setSender(new MailTransportSender());
        trpOutDesc.addParameter(new Parameter(MailConstants.TRANSPORT_MAIL_DEBUG, "true"));
        for (Map.Entry<String,String> prop : getOutProperties().entrySet()) {
            trpOutDesc.addParameter(new Parameter(prop.getKey(), prop.getValue()));
        }
        return trpOutDesc;
    }
    
    public void setupPoll(ParameterInclude params, Account account) throws AxisFault {
        params.addParameter(new Parameter(MailConstants.TRANSPORT_MAIL_DEBUG, "true"));
        params.addParameter(new Parameter("transport.mail.Protocol", getProtocol()));
        params.addParameter(new Parameter("transport.mail.Address", account.getAddress()));
        params.addParameter(new Parameter("transport.PollInterval", "50ms"));
        for (Map.Entry<String,String> prop : getInProperties(account).entrySet()) {
            params.addParameter(new Parameter(prop.getKey(), prop.getValue()));
        }
    }
}
