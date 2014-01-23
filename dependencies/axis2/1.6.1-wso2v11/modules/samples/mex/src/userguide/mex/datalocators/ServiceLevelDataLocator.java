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

import java.util.Calendar;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.dataretrieval.*;

/*
 * Sample Service Level Locator
 * ServiceLevelDataLocator is configured as a Service level Data Locator 
 * for the Sample ServiceLevelDataLocatorDemoService service.
 * 
 * The sample Data Locator is only for the purpose of demostrating an implementation of
 * user-defined Service Level Data Locator, the actual data retrieval code is not implemented.
 * In the example, a empty Data array is returned for Policy and Schema data retrieval
 * request, and a null Data array is returned for WSDL data retrieval. 
 * 
 * Note: Empty Data array means Data Locator understood the request, however data
 * is not available for the Dialect; a null data array means Data Locator does not understand
 * the request.
 * 
 * Run  {@link DemoServiceLevelDataLocator} client code to invoke this sample data locator. 
 *    
 */

public class ServiceLevelDataLocator implements AxisDataLocator {

	public Data[] getData(DataRetrievalRequest request,
			MessageContext msgContext) throws DataRetrievalException {
		System.out
				.println(Calendar.getInstance().getTime()
						+ " !!!! userguide.mex.datalocators.ServiceLevelDataLocator performing getData !!!!");

		System.out
				.print(" !!! SERVICE LEVEL Data Locator supports Policy and Schema dialects only !!!");
		Data[] result = null;

		String dialect = request.getDialect();
		if (dialect.equals(DRConstants.SPEC.DIALECT_TYPE_POLICY)
				|| dialect.equals(DRConstants.SPEC.DIALECT_TYPE_SCHEMA)) {
			// You may implement logic to retrieve Policy and Schema metadata here!!
			//
			System.out
					.print("ServiceLevelDataLocator has not implemented data retrieval for dialect "
							+ dialect);
			System.out.println("");
			System.out
					.println("!!!! Return empty Data means the Data Locator understood the request for Dialect, but no data is available for the dialect");
			result = new Data[0];

		} else {
			System.out
					.println("!!!! ServiceLevelDataLocator does not support dialect "
							+ dialect);
			System.out.println("");
			System.out
					.println("!!!! Return Null Data means escalate to the Axis2 default Data Locator to retrieve data for "
							+ dialect);
		}
		return result;
	}
		
		
	
}
