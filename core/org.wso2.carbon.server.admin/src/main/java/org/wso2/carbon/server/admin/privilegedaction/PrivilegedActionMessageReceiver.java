/*
 * Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.carbon.server.admin.privilegedaction;

import java.util.ArrayList;
import java.util.List;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.rpc.receivers.RPCMessageReceiver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.server.admin.internal.PrivilegedActionServiceComponent;

public class PrivilegedActionMessageReceiver extends RPCMessageReceiver {

	private static final Log log = LogFactory.getLog(PrivilegedActionMessageReceiver.class);
	private static boolean SKIP_SERVICE_INVOCATION = false;
	private static boolean SKIP_LOWER_PRIORITY_EXTENSIONS = false;

    /**
         * Process the <code>privilegedaction.xml</code> configuration file and build a
         * <code>PrivilegedActionConfiguration</code> object
         *
         * @throws PrivilegedActionException
         */
    public PrivilegedActionMessageReceiver() throws PrivilegedActionException {
        PrivilegedActionConfiguration privilegedActionConfiguration =
                    PrivilegedActionConfigurationXMLProcessor.buildPrivilegedActionConfigurationFromFile();
        SKIP_SERVICE_INVOCATION = privilegedActionConfiguration.isSkipServiceInvocation();
        SKIP_LOWER_PRIORITY_EXTENSIONS = privilegedActionConfiguration.isSkipLowerPriorityExtensions();
    }

	@Override
	public void invokeBusinessLogic(MessageContext inMsgCtx, MessageContext outMsgCtx)
	                                                                                  throws AxisFault {

		boolean skipLowerPriorityExtensions = false;
		boolean skipServiceInvocation = false;
		int skipPriority = -1;

		List<PrivilegedAction> extensions = getPrivilegedActionExtensions(inMsgCtx);

		for (PrivilegedAction ext : extensions) {
			if (!skipLowerPriorityExtensions || ext.getPriority() == skipPriority) {
				try {
					ext.execute(inMsgCtx, outMsgCtx);
					if (SKIP_LOWER_PRIORITY_EXTENSIONS) {
						if (ext.skipLowerPriorityExtensions()) {
							skipLowerPriorityExtensions = true;
							skipPriority = ext.getPriority();
						}
					}
					if (SKIP_SERVICE_INVOCATION) {
						if (ext.skipServiceInvocation()) {
							skipServiceInvocation = true;
						}
					}
				} catch (PrivilegedActionException e) {
                    log.error("Error while executing the privileged action extension " +
						                            ext.getExtensionName(), e);
					throw new AxisFault("Error while executing the privileged action extension " +
						                            ext.getExtensionName(), e);
				}
			}
		}

		if (!skipServiceInvocation) {
			super.invokeBusinessLogic(inMsgCtx, outMsgCtx);
		}

	}

    /**
         * Returns the list of PrivilegedActionExtensions that handle this Axis2 operation specified in the
         * <code>MessageContext</code>
         *
         * @param messageContext Axis2 MessageContext
         * @return List of PrivilegedActionExtensions
         */
    private List<PrivilegedAction> getPrivilegedActionExtensions(MessageContext messageContext){
        ArrayList<PrivilegedAction> returnList = new ArrayList<PrivilegedAction>();
        for (PrivilegedAction privilegedAction : PrivilegedActionServiceComponent.privilegedActions) {
            if(!privilegedAction.isDisabled() && privilegedAction.doesHandle(messageContext)){
                returnList.add(privilegedAction);
            }
		}
		return returnList;
    }

}
