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

package sample.eventing;

import java.net.URI;
import java.util.Random;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ServiceContext;
import org.apache.savan.publication.client.PublicationClient;
import org.apache.savan.storage.SubscriberStore;
import org.apache.savan.util.CommonUtil;

public class PublisherService {
  
	ServiceContext serviceContext = null;
	
	public void init(ServiceContext serviceContext) throws AxisFault {
		System.out.println("Eventing Service INIT called");
		this.serviceContext = serviceContext;
		
		PublisherThread thread = new PublisherThread ();
		thread.start();
	}
  
	public void dummyMethod(OMElement param) throws Exception  {
		System.out.println("Eventing Service dummy method called");
	}
	
	private class PublisherThread extends Thread {
		
		String Publication = "Publication";
		String publicationNamespaceValue = "http://tempuri/publication/";
		Random r = new Random ();
		
		public void run () {
			try {
				while (true) {
					
					Thread.sleep(5000);
					
					//publishing
					System.out.println("Publishing next publication...");
					
					SubscriberStore store = CommonUtil.getSubscriberStore(serviceContext.getAxisService());
					if (store==null)
						throw new Exception ("Cant find the Savan subscriber store");
					
					OMElement data = getNextPublicationData ();
					
					PublicationClient publicationClient = new PublicationClient (serviceContext.getConfigurationContext());
					publicationClient.sendPublication(data,serviceContext.getAxisService(),null);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public OMElement getNextPublicationData () {
			OMFactory factory = OMAbstractFactory.getOMFactory();
			OMNamespace namespace = factory.createOMNamespace(publicationNamespaceValue,"ns1");
			OMElement publicationElement = factory.createOMElement(Publication,namespace);
			
			int value = r.nextInt();
			publicationElement.setText(Integer.toString(value));
			
			OMElement data = factory.createOMElement("publish",namespace);
			data.addChild(publicationElement);
			
			
			return data;
		}
	}
}
