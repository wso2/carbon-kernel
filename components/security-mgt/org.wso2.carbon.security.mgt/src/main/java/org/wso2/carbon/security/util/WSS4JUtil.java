/*
 * Copyright (c) 2007, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.security.util;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSUsernameTokenPrincipal;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.handler.WSHandlerResult;

import java.util.Vector;

/**
 * This is a utility class to be used to extract security information from the
 * message context IF the WSS4J security module was used to validate the secured
 * messages.
 */
public class WSS4JUtil {

    private WSS4JUtil(){}

    /**
     * Returns the UsernameTokenPrincipal from the security results.
     *
     * @param mc The message context of the message
     * @return the UsernameTokenPrincipal from the security results as an
     * <code>org.apache.ws.security.WSUsernameTokenPrincipal</code>.
     * If a wsse:UsernameToken was not present in the wsse:Security header then
     * <code>null</code> will be returned.
     * @throws Exception If there are no security results.
     * @see org.apache.ws.security.WSUsernameTokenPrincipal
     */
    public static WSUsernameTokenPrincipal getUsernameTokenPrincipal(
            MessageContext mc) throws Exception {

        Vector results;
        if ((results = (Vector) mc.getProperty(WSHandlerConstants.RECV_RESULTS)) == null) {
            throw new Exception("No security results available in the message context");
        } else {
            for (int i = 0; i < results.size(); i++) {
                WSHandlerResult rResult = (WSHandlerResult) results.get(i);
                Vector wsSecEngineResults = rResult.getResults();
                for (int j = 0; j < wsSecEngineResults.size(); j++) {
                    WSSecurityEngineResult wser =
                            (WSSecurityEngineResult) wsSecEngineResults.get(j);

                    Integer actInt = (Integer) wser
                            .get(WSSecurityEngineResult.TAG_ACTION);
                    if (actInt.intValue() == WSConstants.UT) {
                        return (WSUsernameTokenPrincipal) wser
                                .get(WSSecurityEngineResult.TAG_PRINCIPAL);
                    }
                }
            }
        }
        return null;
    }


    public static Parameter getClientUsernameTokenHandler(String password) {
        Parameter param = new Parameter();
        param.setName(WSHandlerConstants.PW_CALLBACK_REF);
        param.setValue(new ClientUserPasswordCallbackHandler(password));
        return param;
    }
}
