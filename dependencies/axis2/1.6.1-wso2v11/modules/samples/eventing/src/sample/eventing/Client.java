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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.savan.eventing.client.EventingClient;
import org.apache.savan.eventing.client.EventingClientBean;
import org.apache.savan.eventing.client.SubscriptionStatus;

public class Client {

    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    
    private final int MIN_OPTION = 1;
    private final int MAX_OPTION = 9;
    
    private final String SUBSCRIBER_1_ID = "subscriber1";
    private final String SUBSCRIBER_2_ID = "subscriber2";
    
    private ServiceClient serviceClient = null;
    private Options options = null;
    private EventingClient eventingClient = null;
    
    private String toAddressPart = "/axis2/services/PublisherService";
    private String listner1AddressPart = "/axis2/services/ListnerService1";
    private String listner2AddressPart = "/axis2/services/ListnerService2";
    
	private final String applicationNamespaceName = "http://tempuri.org/"; 
	private final String dummyMethod = "dummyMethod";
    
	private static String repo = null;
	private static int port = 8080;
	private static String serverIP = "127.0.0.1";
	
	private static final String portParam = "-p";
	private static final String repoParam = "-r";
	private static final String helpParam = "-h";
	
	public static void main (String[] args) throws Exception {
		
		for (int i=0;i<args.length;i++) {
			if (helpParam.equalsIgnoreCase(args[i])) {
				displayHelp ();
				System.exit(0);
			}
		}
		
		String portStr = getParam(portParam,args);
		if (portStr!=null) {
			port = Integer.parseInt(portStr);
			System.out.println("Server Port was set to:" + port);
		}
		
		String repoStr = getParam(repoParam,args);
		if (repoStr!=null) {
			repo = repoStr;
			System.out.println("Client Repository was set to:" + repo);
		}
		
		Client c = new Client ();
		c.run ();
	}
	
	private static void displayHelp () {
		System.out.println("Help page for the Eventing Client");
		System.out.println("---------------------------------");
		System.out.println("Set the client reposiory using the parameter -r");
		System.out.println("Set the server port using the parameter -p");
	}
	
    /**
     * This will check the given parameter in the array and will return, if available
     *
     * @param param
     * @param args
     * @return
     */
    private static String getParam(String param, String[] args) {
        if (param == null || "".equals(param)) {
            return null;
        }

        for (int i = 0; i < args.length; i = i + 2) {
            String arg = args[i];
            if (param.equalsIgnoreCase(arg) && (args.length >= (i + 1))) {
                return args[i + 1];
            }
        }
        return null;
    }
	
	public void run () throws Exception {
		
		System.out.println("\n");
		System.out.println("Welcome to Axis2 Eventing Sample");
		System.out.println("================================\n");
		
		boolean validOptionSelected = false;
		int selectedOption = -1;
		while (!validOptionSelected) {
			displayMenu();
			selectedOption = getIntInput();
			if (selectedOption>=MIN_OPTION && selectedOption<=MAX_OPTION)
				validOptionSelected = true;
			else 
				System.out.println("\nInvalid Option \n\n");
		}
			
		initClient ();
		performAction (selectedOption);
		
		//TODO publish
		
		System.out.println("Press enter to initialize the publisher service.");
		reader.readLine();
		
		options.setAction("uuid:DummyMethodAction");
		serviceClient.fireAndForget(getDummyMethodRequestElement ());
		
		while (true) {
			
			validOptionSelected = false;
			selectedOption = -1;
			while (!validOptionSelected) {
				displayMenu();
				selectedOption = getIntInput();
				if (selectedOption>=MIN_OPTION && selectedOption<=MAX_OPTION)
					validOptionSelected = true;
				else 
					System.out.println("\nInvalid Option \n\n");
			}
				
			performAction (selectedOption);
			
		}
	}
	
	private void displayMenu () {
		System.out.println("Press 1 to subscribe Listner Service 1");
		System.out.println("Press 2 to subscribe Listner Service 2");
		System.out.println("Press 3 to subscribe both listner services");
		System.out.println("Press 4 to unsubscribe Listner Service 1");
		System.out.println("Press 5 to unsubscribe Listner Service 2");
		System.out.println("Press 6 to unsubscribe both listner services");
		System.out.println("Press 7 to to get the status of the subscription to Service 1");
		System.out.println("Press 8 to to get the status of the subscription to Service 2");
		System.out.println("Press 9 to Exit");
	}
	
	private int getIntInput () throws IOException {
        String option = reader.readLine();
        try {
            return Integer.parseInt(option);
        } catch (NumberFormatException e) {
        	//invalid option
        	return -1;
        }
	}
	
	private void initClient () throws AxisFault {

		String CLIENT_REPO = null;
		String AXIS2_XML = null;
		
		if (repo!=null) {
			CLIENT_REPO = repo;
			AXIS2_XML = repo + File.separator + "axis2.xml";
		} else {
//			throw new AxisFault ("Please specify the client repository as a program argument.Use '-h' for help.");
		}
		
		ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(CLIENT_REPO,null);
		serviceClient = new ServiceClient (configContext,null); //TODO give a repo
		
		options = new Options ();
		serviceClient.setOptions(options);
		serviceClient.engageModule(new QName ("addressing"));
		
		eventingClient = new EventingClient (serviceClient);
		
		String toAddress = "http://" + serverIP + ":" + port + toAddressPart;
		options.setTo(new EndpointReference (toAddress));
	}
	
	private void performAction (int action) throws Exception {
		
		switch (action) {
		case 1:
			doSubscribe(SUBSCRIBER_1_ID);
			break;
		case 2:
			doSubscribe(SUBSCRIBER_2_ID);
			break;
		case 3:
			doSubscribe(SUBSCRIBER_1_ID);
			doSubscribe(SUBSCRIBER_2_ID);
			break;
		case 4:
			doUnsubscribe(SUBSCRIBER_1_ID);
			break;
		case 5:
			doUnsubscribe(SUBSCRIBER_2_ID);
			break;
		case 6:
			doUnsubscribe(SUBSCRIBER_1_ID);
			doUnsubscribe(SUBSCRIBER_2_ID);
			break;
		case 7:
			doGetStatus(SUBSCRIBER_1_ID);
			break;
		case 8:
			doGetStatus(SUBSCRIBER_2_ID);
			break;
		case 9:
			System.exit(0);
			break;
		default:
			break;
		}
	}
	
	private void doSubscribe (String ID) throws Exception {
		EventingClientBean bean = new EventingClientBean ();
		
		String subscribingAddress = null;
		if (SUBSCRIBER_1_ID.equals(ID)) {
            subscribingAddress = "http://" + serverIP + ":" + port + listner1AddressPart;
		} else if (SUBSCRIBER_2_ID.equals(ID)) {
            subscribingAddress = "http://" + serverIP + ":" + port + listner2AddressPart;
		}
	
		bean.setDeliveryEPR(new EndpointReference (subscribingAddress));
	
		//uncomment following to set an expiration time of 10 minutes.
//		Date date = new Date ();
//		date.setMinutes(date.getMinutes()+10);
//		bean.setExpirationTime(date);
		
		eventingClient.subscribe(bean,ID);
		Thread.sleep(1000);   //TODO remove if not sequired
	}
	
	private void doUnsubscribe (String ID) throws Exception {
		eventingClient.unsubscribe(ID);
		Thread.sleep(1000);   //TODO remove if not sequired
	}
	
	private void doGetStatus (String ID) throws Exception {
		SubscriptionStatus status  = eventingClient.getSubscriptionStatus(ID);
		Thread.sleep(1000);   //TODO remove if not sequired
		
		String statusValue = status.getExpirationValue();
		System.out.println("Status of the subscriber '" + ID +"' is" + statusValue);
	}
	
	private OMElement getDummyMethodRequestElement() {
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMNamespace namespace = fac.createOMNamespace(applicationNamespaceName,"ns1");
		return fac.createOMElement(dummyMethod, namespace);
	}
	
}
