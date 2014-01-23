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
import org.apache.axis2.dataretrieval.AxisDataLocator;
import org.apache.axis2.dataretrieval.DRConstants;
import org.apache.axis2.dataretrieval.Data;
import org.apache.axis2.dataretrieval.DataRetrievalException;
import org.apache.axis2.dataretrieval.DataRetrievalRequest;
import org.apache.axis2.dataretrieval.OutputForm;

/*
 * Sample user-defined Global Level Locator, GlobalDataLocator
 * 
 * The sample Data Locator implemented supports data retrieval for the Policy and Schema
 * dialects. For dialects that it does not understand, it delegates the request
 * to the available Data Locators in the hierachy by returning Result with
 * useDataLocatorHierachy indicator set.
 * 
 * See  {@link DemoServiceLevelDataLocator} for steps to invoke getData API
 * of this Data Locator.   
 * 
 */
public class GlobalDataLocator implements AxisDataLocator {

	public Data[] getData(DataRetrievalRequest request,
			MessageContext msgContext) throws DataRetrievalException {
		Data[] output = null;
        String dialect = request.getDialect();
        OutputForm form = request.getOutputForm();
        if (form == OutputForm.REFERENCE_FORM){
        	
        }
        if (dialect.equals(DRConstants.SPEC.DIALECT_TYPE_POLICY) || dialect.equals(DRConstants.SPEC.DIALECT_TYPE_SCHEMA)){
       	 System.out.print("ServiceLevelDataLocator has not implemented data retrieval for dialect " + dialect);
        	 System.out.println("");
       	 System.out.println("!!!! get Axis2 default Data Locator to retrieve data for " + dialect);

       	// result = new Result();
        	// result.setUseDataLocatorHierachy(true);
  
        }
        else {
       	 System.out.println("!!!! ServiceLevelDataLocator does not support dialect " + dialect);
       	 System.out.println("");
       	 System.out.println("!!!! get Axis2 default Data Locator to retrieve data for " + dialect);
        	// result = new Result();
        	// result.setUseDataLocatorHierachy(true);
          }
        return output;
		
	}

}
