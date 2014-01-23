
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

package org.apache.axis2.jaxws.sample.mtom1;

import org.apache.axis2.jaxws.TestLogger;

import javax.jws.WebService;
import javax.xml.ws.BindingType;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPBinding;


/**
 * A JAXB implementation
 *
 */

@WebService(
		targetNamespace = "urn://mtom1.sample.jaxws.axis2.apache.org",
		serviceName = "SendImageService",
		portName = "sendImagePort",		
		endpointInterface = "org.apache.axis2.jaxws.sample.mtom1.SendImageInterface")
@BindingType (SOAPBinding.SOAP11HTTP_MTOM_BINDING)	
public class SendImageService implements SendImageInterface {
 
	
    /**
     * Required impl method from JAXB interface
     * 
     * - No MTOM setting via @BindingType
     * - Using PAYLOAD mode
     * - Sending back the same obj it received
     * - The JAXB object is for image/jpeg MIME type
     *
     * @param ImageDepot obj
     * @return ImageDepot obj
     */
    public ImageDepot invoke(ImageDepot request) throws WebServiceException
    {
        TestLogger.logger.debug("--------------------------------------");
        TestLogger.logger.debug("SendImageService");

       if (request == null) {
           throw new WebServiceException("Null input received.");
       } else if (request.getImageData() == null) {
           throw new WebServiceException("Image is null");
       }

        TestLogger.logger.debug("SendImageService: Request received.");
       return request;
    }
}
