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

package org.apache.axis2.jaxws.wsdl;

import javax.wsdl.WSDLException;
import javax.wsdl.xml.WSDLReader;

/**
 * An instance of this class will be registered with the MetadataFactoryRegistry
 * and will be retrieved when a WSDLReader instance is created. This implementation
 * will be able to configure the instance as needed.
 *
 */
public class WSDLReaderConfiguratorImpl implements WSDLReaderConfigurator {

	/**
	 * This will be called to configure the WSDLReader instance.
	 */
	public void configureReaderInstance(WSDLReader reader) throws WSDLException 
	{
		// prevent the WSDLReader instance from using the System.out
		// stream for messages and logging
		reader.setFeature(com.ibm.wsdl.Constants.FEATURE_VERBOSE, false);
	}

}
