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

package org.apache.axis2.jaxws.sample.mtom;


import org.apache.axis2.datasource.jaxb.JAXBAttachmentUnmarshallerMonitor;
import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.jaxws.provider.DataSourceImpl;
import org.test.mtom.ImageDepot;
import org.test.mtom.ObjectFactory;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.imageio.ImageIO;
import javax.jws.WebService;
import javax.xml.ws.BindingType;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.MTOM;
import javax.xml.ws.soap.SOAPBinding;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

@WebService(serviceName="MtomSampleService",
	    endpointInterface="org.apache.axis2.jaxws.sample.mtom.MtomSample")
@BindingType(SOAPBinding.SOAP11HTTP_MTOM_BINDING)
public class MtomSampleService implements MtomSample {

    public ImageDepot sendImage(ImageDepot input) {
        TestLogger.logger.debug("MtomSampleService [new sendImage request received]");
        DataHandler data = input.getImageData();

        TestLogger.logger.debug("[contentType] " + data.getContentType());
        ImageDepot output = (new ObjectFactory()).createImageDepot();
        Image image = null;
        
        resetAttachmentUnmarshallingMonitor();
        try {
            InputStream stream = (InputStream) data.getContent();
            image = ImageIO.read(stream);
            
            DataSource imageDS = new DataSourceImpl("image/jpeg", "test.jpg", image);
            DataHandler handler = new DataHandler(imageDS);
            output.setImageData(handler);
        }
        catch (Exception e) {
            throw new WebServiceException(e);
        }
        return output;
    }

    public ImageDepot sendText(byte[] input) {
        TestLogger.logger.debug("[new sendText request received]");
        return null;
    }

    /**
     * Reset the monitor so that we can determine if an
     * attachment is unmarshalled on the response.
     */
    private void resetAttachmentUnmarshallingMonitor() {
        if (JAXBAttachmentUnmarshallerMonitor.isMonitoring()) {
            JAXBAttachmentUnmarshallerMonitor.clear();
        }
    }
}
