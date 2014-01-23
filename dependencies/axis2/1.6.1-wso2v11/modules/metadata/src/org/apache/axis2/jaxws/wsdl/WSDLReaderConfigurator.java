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
 * This interface will be implemented by classes that wish to
 * configure an instance of a WSDLReader object. Implementations
 * may modify the WSDLReader instance as needed in order to
 * create a WSDL Definition object that reflects the desired
 * metadata.
 *
 */
public interface WSDLReaderConfigurator {
	
	/**
	 * This method accepts a WSDLReader instance so that implementors
	 * may modify the instance as necessary. 
	 */
	public void configureReaderInstance(WSDLReader reader) throws WSDLException;

}
