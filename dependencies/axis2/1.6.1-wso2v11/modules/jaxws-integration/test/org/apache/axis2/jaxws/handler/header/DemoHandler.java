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

package org.apache.axis2.jaxws.handler.header;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.HashSet;
import java.util.Set;

public class DemoHandler implements SOAPHandler<SOAPMessageContext> {

    public Set<QName> getHeaders() {
        System.out.println("DemoHandler.getHeaders Invoked by JAXWS");
        Set<QName> result = new HashSet<QName>();
        result.add(new QName("http://demo/", "myheader"));
        return result;
    }

    public void close(MessageContext arg0) {

    }

    public boolean handleFault(SOAPMessageContext arg0) {
        return true;
    }

    public boolean handleMessage(SOAPMessageContext arg0) {
        System.out.println("DemoHandler.handleMessage");
        return true;
    }



}
