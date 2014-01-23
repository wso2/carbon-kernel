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

package org.apache.axis2.tool.codegen.eclipse.util;

import org.apache.axis2.util.URLProcessor;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;



/**
 * This class presents a convenient way of reading the 
 * WSDL file(url) and producing a useful set of information
 * It does NOT use any of the standard WSDL classes from 
 * Axis2, rather it uses wsdl4j to read the wsdl and extract 
 * the properties (This is meant as a convenience for the UI
 * only. We may not need the whole conversion the WSDLpump 
 * goes through)
 * One would need to change this to suit a proper WSDL 
 */
public class WSDLPropertyReader {
    private Definition wsdlDefinition = null;
    
	public void readWSDL(String filepath) throws WSDLException {
		WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
		wsdlDefinition = reader.readWSDL(filepath); 
	}
	
	/**
	 * Returns the namespace map from definition
	 * @return
	 */
	public Map getDefinitionNamespaceMap(){
		return wsdlDefinition.getNamespaces();
	}
	
	/**
	 * get the default package derived by the targetNamespace
	 */
	public String packageFromTargetNamespace(){
		return  URLProcessor.makePackageName(wsdlDefinition.getTargetNamespace());
		
	}
	
	/**
	 * Returns a list of service names
	 * the names are QNames
	 * @return
	 */
	public List getServiceList(){
		List returnList = new ArrayList();
		Service service = null;
		Map serviceMap = wsdlDefinition.getServices();
		if(serviceMap!=null && !serviceMap.isEmpty()){
		   Iterator serviceIterator = serviceMap.values().iterator();
		   while(serviceIterator.hasNext()){
			   service = (Service)serviceIterator.next();
			   returnList.add(service.getQName());
		   }
		}
		
		return returnList;
	}

	/**
	 * Returns a list of ports for a particular service
	 * the names are QNames
	 * @return
	 */
	public List getPortNameList(QName serviceName){
		List returnList = new ArrayList();
		Service service = wsdlDefinition.getService(serviceName);
		Port port = null; 
		if(service!=null){
		   Map portMap = service.getPorts();
		   if (portMap!=null && !portMap.isEmpty()){
			   Iterator portIterator = portMap.values().iterator();
			   while(portIterator.hasNext()){
				 port = (Port)portIterator.next();
				 returnList.add(port.getName());
			   }
		   }
		  
		}
		
		return returnList;
	}

	/**
	 * public method to get loaded wsdl Definition
	 * @return
	 */
	public Definition getWsdlDefinition() {
		return wsdlDefinition;
	}
}
