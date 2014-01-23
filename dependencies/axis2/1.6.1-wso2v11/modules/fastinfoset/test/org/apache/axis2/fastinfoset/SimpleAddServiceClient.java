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

package org.apache.axis2.fastinfoset;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.fastinfoset.SimpleAddServiceStub;
import org.apache.axis2.fastinfoset.SimpleAddServiceStub.AddFloats;
import org.apache.axis2.fastinfoset.SimpleAddServiceStub.AddFloatsResponse;
import org.apache.axis2.fastinfoset.SimpleAddServiceStub.AddInts;
import org.apache.axis2.fastinfoset.SimpleAddServiceStub.AddIntsResponse;
import org.apache.axis2.fastinfoset.SimpleAddServiceStub.AddStrings;
import org.apache.axis2.fastinfoset.SimpleAddServiceStub.AddStringsResponse;

import java.rmi.RemoteException;

public class SimpleAddServiceClient {
	
	private SimpleAddServiceStub serviceStub;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			SimpleAddServiceClient client = new SimpleAddServiceClient();
			System.out.println("Response 1 is " + client.addStrings("Hello ", "World!"));
			System.out.println("Response 2 is " + client.addInts(23, 27));
			System.out.println("Response 3 is " + client.addFloats(22.84f, 27.16f));
		} catch (AxisFault af) {
			af.printStackTrace();
		} catch (RemoteException re) {
			re.printStackTrace();
		}
	}
	
	/**
	 * Contructor which uses "http://localhost:8081/axis2/services/SimpleAddService" as the target.
	 * When the serrver runs at port 8080, this can be use with tcpmon for debugging.
	 * 
	 * @throws AxisFault
	 */
	public SimpleAddServiceClient() throws AxisFault {
		this(new EndpointReference("http://localhost:8081/axis2/services/SimpleAddService"));
	}
	
	/**
	 * Constructor used by the default JUnit test case.
	 * 
	 * @param target
	 * @throws AxisFault
	 */
	public SimpleAddServiceClient(EndpointReference target) throws AxisFault {
		ConfigurationContext context = 
			ConfigurationContextFactory.createConfigurationContextFromFileSystem("target/test-classes", "test-resources/axis2.xml");
		serviceStub = new SimpleAddServiceStub(context, target.getAddress());
		//serviceStub = new SimpleAddServiceStub();
		ServiceClient client=  serviceStub._getServiceClient();
		Options options = client.getOptions();
		options.setProperty(Constants.Configuration.MESSAGE_TYPE, "application/soap+fastinfoset");
	}

	/**
	 * Constructor used by the default JUnit test case.
	 * 
	 * @param target
	 * @throws AxisFault
	 */
	public SimpleAddServiceClient(EndpointReference target, boolean pox) throws AxisFault {
		ConfigurationContext context = 
			ConfigurationContextFactory.createConfigurationContextFromFileSystem("target/test-classes", "test-resources/axis2.xml");
		serviceStub = new SimpleAddServiceStub(context, target.getAddress());
		//serviceStub = new SimpleAddServiceStub();
		ServiceClient client=  serviceStub._getServiceClient();
		Options options = client.getOptions();
		if (pox) {
			options.setProperty(Constants.Configuration.MESSAGE_TYPE, "application/fastinfoset");
		} else {
			options.setProperty(Constants.Configuration.MESSAGE_TYPE, "application/soap+fastinfoset");
		}
	}
	
	public String addStrings(String param0, String param1) throws RemoteException {
		AddStrings addStrings = new SimpleAddServiceStub.AddStrings();
		addStrings.setVal1(param0);
		addStrings.setVal2(param1);
		AddStringsResponse response1 = serviceStub.addStrings(addStrings);
		return response1.get_return();
	}

	public int addInts(int param0, int param1) throws RemoteException {
		AddInts addInts = new SimpleAddServiceStub.AddInts();
		addInts.setVal1(param0);
		addInts.setVal2(param1);
		AddIntsResponse response2 = serviceStub.addInts(addInts);
		return response2.get_return();
	}
	
	public float addFloats(float param0, float param1) throws RemoteException {
		AddFloats addFloats = new SimpleAddServiceStub.AddFloats();
		addFloats.setVal1(param0);
		addFloats.setVal2(param1);
		AddFloatsResponse response3 = serviceStub.addFloats(addFloats);
		return response3.get_return();
	}
}
