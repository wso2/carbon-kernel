/*
 *  Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.server.admin.privilegedaction;

import org.apache.axis2.context.MessageContext;

/**
 * In order to write a PrivilegedActionExtension this class needs to be implemented
 *
 */
public interface PrivilegedAction {
	
	/**
	 * The extension logic must be implemented here.
	 * 
	 * @param inMessageContext   Axis2 MessageContext
	 * @param outMessageContext Axis2 MessageContext
        * @throws PrivilegedActionException
	 */
	public void execute(MessageContext inMessageContext, MessageContext outMessageContext) throws
                                                                          PrivilegedActionException;

	/**
	 * Returns the priority of the extension in the framework
	 * 
	 * @return priority
	 */
	public int getPriority();

	/**
	 * Checks if this extension should be executed for the Axis2 operation in the MessageContext
	 * 
	 * @param msgContext
	 * @return true - This extension should be invoked for this operation
        *               false - This extension should not be invoked for this operation
	 */
	public boolean doesHandle(MessageContext msgContext);

    /**
         * Checks if extension is disabled
         *
         * @return true - extension is disabled
         *               false - extension is enabled
         */
    public boolean isDisabled();
	
	/**
	 * Returns the extension name
        *
	 * @return    extension name
	 */
	public String getExtensionName();

    /**
	 * Extensions can skip service invocation if supported by MessageReceiver
        *
	 * @return    true - skip service invocation
        *                  false - continue with service invocation
	 */
    public boolean skipServiceInvocation();

    /**
	 * Extensions can skip lower priority extensions if supported    by MessageReceiver
        *
	 * @return true - skip lower priority extensions
        *               false - continue with lower priority extensions
	 */
    public boolean skipLowerPriorityExtensions();

}
