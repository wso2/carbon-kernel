/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.core.services.authentication;

import org.apache.axis2.context.MessageContext;
import org.wso2.carbon.core.common.AuthenticationException;

import java.rmi.RemoteException;

/**
 */

public interface CarbonServerAuthenticator extends BackendAuthenticator {

    /**
     * Does this authenticator handle this type of requests
     * 
     * @param request
     * @return
     * @throws RemoteException
     */
    boolean isHandle(MessageContext msgContext);

    /**
     * Authenticates user based on the credentials contained in the request.
     * 
     * @param request
     * @return
     * @throws AuthenticationException
     */
    boolean isAuthenticated(MessageContext messageContext);

    /**
     * If this authenticator implements remember me then implement this method.
     * BE remembers FE via this method by calling it at AuthenticationHandler.
     * 
     * @param cookie The cookie
     * @return True if valid, otherwise false
     * @throws AuthenticationException
     */
    boolean authenticateWithRememberMe(MessageContext msgContext);
    
   /**
     * The name of the Authenticator
     * 
     * @return
     */
    String getAuthenticatorName();
}
