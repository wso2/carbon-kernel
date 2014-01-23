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
package org.apache.axis2.jaxws.sample.mtomfeature;

import java.io.BufferedInputStream;

import javax.activation.DataHandler;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.MTOM;

import org.apache.axis2.jaxws.TestLogger;
@javax.jws.WebService (serviceName="ProcessDocumentService", endpointInterface="org.apache.axis2.jaxws.sample.mtomfeature.ProcessDocumentDelegate")
@MTOM(threshold=0)
public class ProcessDocumentPortBindingImpl implements ProcessDocumentDelegate {

    public DataHandler sendPDFFile(DataHandler dh) {
        try {
            TestLogger.logger.debug("--------------------------------------");
            TestLogger.logger.debug("sendPDFFile");
           //Check if we got the attachment.
           if (dh == null) {
               TestLogger.logger.debug("Null DataHandler received.");
               throw new WebServiceException("Null input received.");
           } 
           //Validate that the file data is available
           BufferedInputStream fileIn = new BufferedInputStream(dh.getInputStream());
           if(fileIn.available() <= 0) {
               TestLogger.logger.debug("No File Content in the MTOM attachment");
               throw new WebServiceException("PDF File is empty");
           }
           //All went well and we got the pdf file content.
            TestLogger.logger.debug("sendPDFFile: Request received.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        //return the file data handler.
        return dh;
    }
}
