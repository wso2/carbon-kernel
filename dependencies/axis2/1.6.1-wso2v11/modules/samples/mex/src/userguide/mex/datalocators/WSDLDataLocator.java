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

package userguide.mex.datalocators;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.dataretrieval.*;
import org.apache.axis2.description.AxisService;

/*
 * Sample user-defined WSDL Data Locator, WSDLDataLocator is configured as a Service level 
 * WSDL specific Data Locator for the Sample WSDLDataServiceMyService service.
 * 
 * The sample Data Locator is only for the purpose of demostrating an implementation of
 * user-defined Service Level Dialect Data Locator, the actual data retrieval code is not implemented.
 * In the example, a empty Data array is returned for WSDL data retrieval
 * request.
 * 
 * Note: Empty Data array means Data Locator understood the request.
 * 
 * Run  {@link DemoServiceWSDLServiceLocator} client code to invoke this sample data locator. 
 * 
 */
public class WSDLDataLocator implements AxisDataLocator {
	AxisService theService = null;

	String serviceName = "WSDLDataLocatorDemoService";

	public Data[] getData(DataRetrievalRequest request,
			MessageContext msgContext) throws DataRetrievalException {
		Data[] result = new Data[0];
		System.out.print(serviceName
				+ " User-defined WSDL Data Locator has been invoked!!!");
		// You may implement logic to retrieve WSDL metadata here!!
		
		System.out.print("WSDLDataLocator has not implemented data retrieval.");
 
		return result;
	}

}

