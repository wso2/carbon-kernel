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

/**
 * 
 */
package org.apache.axis2.jaxws.sample.doclitbare;

import org.apache.axis2.jaxws.sample.doclitbare.sei.DocLitBarePortType;
import org.apache.axis2.jaxws.sample.doclitbare.sei.FaultBeanWithWrapper;
import org.apache.axis2.jaxws.sample.doclitbare.sei.SimpleFault;
import org.test.sample.doclitbare.Composite;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebParam.Mode;
import javax.jws.WebService;
import javax.xml.ws.Holder;

@WebService(serviceName="BareDocLitService",
			endpointInterface="org.apache.axis2.jaxws.sample.doclitbare.sei.DocLitBarePortType")
public class DocLitBarePortTypeImpl implements DocLitBarePortType {

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.sample.doclitbare.sei.DocLitBarePortType#oneWayEmpty()
	 */
	public void oneWayEmpty() {
		String retValue = "Running One way call";

	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.sample.doclitbare.sei.DocLitBarePortType#oneWay(java.lang.String)
	 */
	public void oneWay(String allByMyself) {
		// TODO Auto-generated method stub
		String retValue = "Running One way call with String input" + allByMyself;
	}
        
         
	public String echoString(String echoStringIn) {
	    return echoStringIn;
	}


	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.sample.doclitbare.sei.DocLitBarePortType#twoWaySimple(int)
	 */
	public String twoWaySimple(int allByMyself) {
		// TODO Auto-generated method stub
		String retValue = "Acknowledgement: received input value as integer:"+ allByMyself;
		return retValue;
	}
	
	public void twoWayHolder(
	        @WebParam(name = "Composite", targetNamespace = "http://org.test.sample.doclitbare", mode = Mode.INOUT, partName = "allByMyself")
	        Holder<Composite> allByMyself)
	        throws FaultBeanWithWrapper, SimpleFault{
		
	}
	
	 public String headerTest(
             int allByMyself,
             String headerParam) {
         if (headerParam == null) {
             return "Acknowledgement: No Header";
         } else {
             return "Acknowledgement: Header is " + headerParam;
         }
         
     }
	    
}
