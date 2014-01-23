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

package org.apache.axis2.jaxws.injection;

import org.apache.axis2.jaxws.unitTest.TestLogger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;


public class ResourceInjectionTestImpl2{

	
	public WebServiceContext ctx = null;
	
	public ResourceInjectionTestImpl2() {
		super();
		// TODO Auto-generated constructor stub
	}

	@PostConstruct
	public void initialize(){
		//Called after resource injection and before a method is called.
        TestLogger.logger.debug("Calling PostConstruct to Initialize");
	}
	
	@PreDestroy
	public void distructor(){
		//Called before the scope of request or session or application ends.

        TestLogger.logger.debug("Calling PreDestroy ");
		
	}
	@Resource
	public void setCtx(WebServiceContext ctx) {
		this.ctx = ctx;
	}

}
