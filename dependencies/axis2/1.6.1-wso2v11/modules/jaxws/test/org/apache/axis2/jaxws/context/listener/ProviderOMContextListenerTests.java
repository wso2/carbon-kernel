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
package org.apache.axis2.jaxws.context.listener;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;

import org.apache.axis2.engine.AxisConfiguration;

import junit.framework.TestCase;

public class ProviderOMContextListenerTests extends TestCase {
	private ServiceContext sc = null;
	private MessageContext mc = null;
	
	public void testCreate(){
		try{
			ProviderOMContextListener.create(createAxisServiceContext());
			assertTrue(sc.getAxisService().hasMessageContextListener(ProviderOMContextListener.class));
		}catch(Exception e){
			fail(e.getMessage());
		}
	}
	
	private ServiceContext createAxisServiceContext() throws Exception {
		if(this.sc != null){
			return sc;
		}
        
        AxisConfiguration ac = new AxisConfiguration();
        ConfigurationContext cc = new ConfigurationContext(ac);
        
        // Create a dummy AxisService
        AxisService service = new AxisService();
        service.setName("dummy");
        
        AxisServiceGroup asg = new AxisServiceGroup();
        asg.addService(service);
        
        
        // Create a Dummy ServiceContext
        ServiceGroupContext sgc = new ServiceGroupContext(cc, asg);
        ServiceContext sc = sgc.getServiceContext(service);
        this.sc = sc;
        
        return sc;

    }
	
	private MessageContext createMessageContext(){
		if(this.mc !=null){
			return mc;
		}
        // Create a MessageContext
        MessageContext mc = new MessageContext();
        
        // Attach the ServiceContext and MessageContext.
        // This will trigger the MyServiceContextListener.attachEvent
        mc.setServiceContext(sc);
        this.mc = mc;
        return mc;
	}
}
