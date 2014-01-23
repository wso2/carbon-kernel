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

package org.apache.axis2.jaxws.dispatch;

import javax.xml.namespace.QName;

public class DispatchTestConstants {

    public static final String URL = "http://localhost:6060/axis2/services/EchoService";
    public static final String BADURL = "http://this.is.not.a.valid.hostname.at.all.no.way:9999/wacky";
    public static final QName QNAME_SERVICE = new QName("http://ws.apache.org/axis2", "EchoService");
    public static final QName QNAME_PORT = new QName("http://ws.apache.org/axis2", "EchoServiceSOAP11port0");

    private static final String sampleSoapEnvelopeHeader = 
        "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" + 
        "<soap:Body>";
    
    private static final String sampleSoapEnvelopeFooter =
        "</soap:Body>" + 
        "</soap:Envelope>";
    
    public static final String sampleBodyContent = 
        "<ns1:echoString xmlns:ns1=\"http://test\">" + 
        "<ns1:input xmlns=\"http://test\">HELLO THERE!!!</ns1:input>" + 
        "</ns1:echoString>";
    
    public static final String sampleBodyContent_bad = 
        "<ns1:echoString xmlns:ns1=\"http://test\">" + 
        "<ns1:input xmlns=\"http://test\">THROW EXCEPTION</ns1:input>" + 
        "</ns1:echoString>";
    
    public static final String sampleSoapMessage = 
        sampleSoapEnvelopeHeader +
        sampleBodyContent + 
        sampleSoapEnvelopeFooter;
}
