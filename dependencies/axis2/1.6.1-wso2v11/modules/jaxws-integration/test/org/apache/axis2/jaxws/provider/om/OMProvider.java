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
package org.apache.axis2.jaxws.provider.om;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;

import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.soap.SOAPFaultException;

import java.io.StringReader;

@WebServiceProvider(serviceName="OMProviderService")
@ServiceMode(value=Service.Mode.MESSAGE)
public class OMProvider implements Provider<OMElement> {
    private static final String SOAP11_NS_URI = "http://schemas.xmlsoap.org/soap/envelope/";

    /**
     * SOAP 1.1 header
     */
    private static final String SOAP11_ENVELOPE_HEAD = "<?xml version='1.0' encoding='utf-8'?>"
                    + "<soapenv:Envelope xmlns:soapenv=\""
                    + SOAP11_NS_URI
                    + "\">"
                    + "<soapenv:Header />" + "<soapenv:Body>";

    /**
     * SOAP 1.1 footer
     */
    private static final String SOAP11_ENVELOPE_TAIL = "</soapenv:Body>"
                    + "</soapenv:Envelope>";


    private static String response = "<invokeOp>Hello Dispatch OM</invokeOp>";
    private static XMLInputFactory inputFactory = XMLInputFactory.newInstance();
    public OMElement invoke(OMElement om) throws SOAPFaultException{
        try{
            StringReader sr = null;
           if(om!=null){
             String requestText = om.toStringWithConsume();
             if(requestText.contains("SOAPFault")){
            	 throwSOAPFaultException();
             }
             if((requestText.contains("Hello Provider OM"))){
                 sr = new StringReader(SOAP11_ENVELOPE_HEAD+response+SOAP11_ENVELOPE_TAIL);
             }
             if((!requestText.contains("Hello Provider OM"))){
                 sr = new StringReader(SOAP11_ENVELOPE_HEAD+"ack:received OM"+SOAP11_ENVELOPE_TAIL);
             }
           }else{
               sr = new StringReader(SOAP11_ENVELOPE_HEAD+"null request"+SOAP11_ENVELOPE_TAIL);
           }
            XMLStreamReader inputReader = inputFactory.createXMLStreamReader(sr);
            StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(inputReader, null); 
            SOAPEnvelope responseOM = (SOAPEnvelope) builder.getDocumentElement();

            return responseOM;
        }catch(Exception e){
            throw new WebServiceException(e);
        }
    }
    
    private void throwSOAPFaultException() throws SOAPFaultException {
        try {
            MessageFactory mf = MessageFactory.newInstance();
            SOAPFactory sf = SOAPFactory.newInstance();
            
            SOAPMessage m = mf.createMessage();
            SOAPBody body = m.getSOAPBody();
            SOAPFault fault = body.addFault();
            QName faultCode = new QName(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE, "Client"); 
            fault.setFaultCode(faultCode);
            fault.setFaultString("sample fault");
            Detail detail = fault.addDetail();
            Name deName = sf.createName("detailEntry");
            SOAPElement detailEntry = detail.addDetailEntry(deName);
            detailEntry.addTextNode("sample detail");
            fault.setFaultActor("sample actor");
            
            SOAPFaultException sfe = new SOAPFaultException(fault);
            throw sfe;
        } catch (SOAPFaultException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
