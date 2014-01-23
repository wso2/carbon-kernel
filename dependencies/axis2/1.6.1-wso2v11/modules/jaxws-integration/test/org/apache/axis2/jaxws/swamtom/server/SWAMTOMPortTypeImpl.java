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
package org.apache.axis2.jaxws.swamtom.server;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.ws.BindingType;
import javax.xml.ws.Holder;
import javax.jws.WebParam.Mode;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;

/**
 * Endpont that is a SOAP 1.1 MTOM Binding 
 * but has an operation (swaAttachment) that passes
 * SWA attachments
 *
 */
@WebService(name="SWAMTOMPortType",
            serviceName="SWAMTOMService", 
            wsdlLocation="META-INF/swamtomservice.wsdl", 
            targetNamespace="http://swamtomservice.test.org")
@BindingType(javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_MTOM_BINDING)
public class SWAMTOMPortTypeImpl {

    /**
     * This method passes an SWA attachment as a request
     * and expects an SWA attachment as a response.
     * Note that the body content in both cases is empty.
     * (See the wsdl)
     * @param attachment (swa)
     * @return attachment (swa)
     */
    @WebMethod(operationName="swaAttachment", action="swaAttachment")
    @XmlJavaTypeAdapter(HexBinaryAdapter.class)
    @WebResult(name = "jpegImageResponse", targetNamespace = "", partName = "jpegImageResponse")
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public byte[] swaAttachment(
          @XmlJavaTypeAdapter(HexBinaryAdapter.class)
          @WebParam(name = "jpegImageRequest", targetNamespace = "", partName = "jpegImageRequest")
          byte[] attachment) {
        if (attachment == null || attachment.length == 0){
            throw new RuntimeException("Received empty attachment");
        } else {
            // Change the first three characters and return the attachment
            attachment[0] = 'S';
            attachment[1] = 'W';
            attachment[2] = 'A';
        }
        return attachment;
    }
    
    /**
     * This method passes two SWA attachments as a request
     * and expects two SWA attachments as a response.
     * Note that the body content in both cases is empty.
     * (See the wsdl)
     * @param attachment (swa)
     * @return attachment (swa)
     */
    @WebMethod(operationName="swaAttachment2", action="swaAttachment2")
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public void swaAttachment2(
          @XmlJavaTypeAdapter(HexBinaryAdapter.class)
          @WebParam(name = "jpegImage1Request", targetNamespace = "", partName = "jpegImage1Request")
          byte[] attachment,
          
          @XmlJavaTypeAdapter(HexBinaryAdapter.class)
          @WebParam(name = "jpegImage2Request", targetNamespace = "", partName = "jpegImage2Request")
          byte[] attachment2,
          
          @XmlJavaTypeAdapter(HexBinaryAdapter.class)
          @WebParam(name = "jpegImage1Response", 
                    targetNamespace = "", 
                    partName = "jpegImage1Response",
                    mode= Mode.OUT)
          Holder<byte[]> attachmentOut1,
          
          @XmlJavaTypeAdapter(HexBinaryAdapter.class)
          @WebParam(name = "jpegImage2Response", 
                    targetNamespace = "", 
                    partName = "jpegImage2Response",
                    mode= Mode.OUT)
          Holder<byte[]> attachmentOut2) {
          
        if (attachment == null || attachment.length == 0){
            throw new RuntimeException("Received empty first attachment");
        } else if (attachment2 == null || attachment2.length == 0){
            throw new RuntimeException("Received empty second attachment");
        } else if (attachment[0] != '1') {
            throw new RuntimeException("First attachment is not '1'");
        } else if (attachment2[0] != '2') {
            throw new RuntimeException("Second attachment is not '2'");
        } else {
            attachmentOut1.value = new byte[1];
            attachmentOut1.value[0] = '3';
            attachmentOut2.value = new byte[1];
            attachmentOut2.value[0] = '4';
        }
        
    }
    
    @WebMethod(operationName="mtomAttachment", action="mtomAttachment")
    @SOAPBinding(parameterStyle=ParameterStyle.BARE)
    public void mtomAttachment(Holder<byte[]> message) {

        byte[] attachment = message.value;

        if (attachment == null || attachment.length == 0){
            throw new RuntimeException("Received empty mtom attachment");
        } else {
            // Change the first characters
            attachment[0] = 'X';
            attachment[1] = 'O';
            attachment[2] = 'P';
        }

        message.value = attachment;
    }
}
