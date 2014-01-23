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

package userguide.mex.clients;

import java.io.StringWriter;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.dataretrieval.DRConstants;
import org.apache.axis2.dataretrieval.client.MexClient;

/**
 * Sample to demostrate  using Defautl Axis 2 Data Locator to perform data retrieval 
 * for the WS-MEX GetMetadata request.
 * 
 * In the example, no Data Locator is configured in the Axis2.xml nor the services.xml.
 * 
 * Before running the sample, you must first deploy the Axis 2 sample DefaultAxisDataLocatorDemoService service i.e.
 * DefaultAxisDataLocatorDemoService.aar file.  
 * 
 */

public class DemoDefaultDataLocator {

   private static EndpointReference targetEPR = new EndpointReference("http://127.0.0.1:8080/axis2/services/DefaultAxis2DataLocatorDemoService");

    public static void main(String[] args) {
        try {
            System.out.println("Test getMetadata for " + targetEPR.getAddress());
         
            MexClient serviceClient =  new MexClient();
            Options options = new Options();
            serviceClient.setOptions(options);
            
            options.setTo(targetEPR);
            options.setAction(DRConstants.SPEC.Actions.GET_METADATA_REQUEST);
            
            System.out.println ("No DataLocator configured! Used AxisDataLocator");
            
            OMElement method = serviceClient.setupGetMetadataRequest(null, null);
              
            OMElement result = serviceClient.sendReceive(method);
            System.out.println(result);
            StringWriter writer = new StringWriter();
            try {
				result.serialize(XMLOutputFactory.newInstance()
				      .createXMLStreamWriter(writer));
			} catch (XMLStreamException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FactoryConfigurationError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            writer.flush();
            System.out.println(writer.toString());

                     
       
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
          }
        }

}
