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
package org.apache.axis2.jaxws.sample;

import org.apache.axis2.jaxws.TestLogger;
import org.test.sample.nonwrap.ReturnType;
import org.test.sample.nonwrap.TwoWayHolder;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;
import java.util.concurrent.ExecutionException;


public class AsyncCallback implements AsyncHandler {

	/**
	 * 
	 */
	public AsyncCallback() {
		super();
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see javax.xml.ws.AsyncHandler#handleResponse(javax.xml.ws.Response)
	 */
	public void handleResponse(Response response) {
		try{
			Object obj = response.get();
			if(obj instanceof ReturnType){
				ReturnType type = (ReturnType)obj;
                TestLogger.logger.debug(">>Return String = " + type.getReturnStr());
				return;
			}
			if(obj instanceof TwoWayHolder){
				TwoWayHolder twh = (TwoWayHolder)obj;
                TestLogger.logger.debug("AsyncCallback Holder string =" + twh.getTwoWayHolderStr());
                TestLogger.logger.debug("AsyncCallback Holder int =" + twh.getTwoWayHolderInt());
			}
			
		}catch(ExecutionException e){
			e.printStackTrace();
		}catch(InterruptedException e){
			e.printStackTrace();
		}

	}

}
