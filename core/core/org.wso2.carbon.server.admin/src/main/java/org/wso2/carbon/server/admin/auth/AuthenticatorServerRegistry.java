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
package org.wso2.carbon.server.admin.auth;

import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.wso2.carbon.core.services.authentication.BackendAuthenticator;
import org.wso2.carbon.core.services.authentication.CarbonServerAuthenticator;
import org.wso2.carbon.core.services.authentication.ServerAuthenticator;

import java.util.Arrays;

public class AuthenticatorServerRegistry {

    private static Log log = LogFactory.getLog(AuthenticatorServerRegistry.class);

    /**
     * @deprecated As of Carbon 4.0.0. Use {@code #defaultAuthTracker}
     */
    @Deprecated
    private static ServiceTracker authTracker;

    private static ServiceTracker defaultAuthTracker;

    public static final String AUTHENTICATOR_TYPE = "authenticator.type";
    
    public static void init(BundleContext bc) throws Exception {
        try {
            authTracker = new ServiceTracker(bc, CarbonServerAuthenticator.class.getName(), null);
            authTracker.open();

            defaultAuthTracker = new ServiceTracker(bc, ServerAuthenticator.class.getName(), null);
            defaultAuthTracker.open();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public static BackendAuthenticator getCarbonAuthenticator(MessageContext msgContext) {
    	BackendAuthenticator authenticator = null;
        Object[] legacyAuthenticatorObjects = authTracker.getServices();

        Object[] authenticatorObjects = defaultAuthTracker.getServices();

        int allAuthenticators = 0;

        if (authenticatorObjects != null) {
            allAuthenticators = authenticatorObjects.length;
        }

        if (legacyAuthenticatorObjects != null) {
            allAuthenticators += legacyAuthenticatorObjects.length;
        }

        if (allAuthenticators == 0) {
            log.debug("No authenticators registers. This is a programming error");
            return null;
        }

        // cast each object - cannot cast object array
        BackendAuthenticator[] authenticators = new BackendAuthenticator[allAuthenticators];
        int i = 0;

        if (legacyAuthenticatorObjects != null) {
            for (Object obj : legacyAuthenticatorObjects) {
                authenticators[i] = (BackendAuthenticator) obj;
                i++;
            }
        }

        if (authenticatorObjects != null) {
            for (Object obj : authenticatorObjects) {
                authenticators[i] = (BackendAuthenticator) obj;
                i++;
            }
        }

        Arrays.sort(authenticators, new AuthenticatorComparator());

        for (BackendAuthenticator auth : authenticators) {
            if (!auth.isDisabled() && canHandle(auth, msgContext)) {
                authenticator = auth;
                break;
            }
        }
        return authenticator;
    }

    private static boolean canHandle(BackendAuthenticator authenticator, MessageContext msgContext) {

        // TODO we need to get rid of following logic in a future release. This is to preserver the
        // backward compatibility
        if (authenticator instanceof CarbonServerAuthenticator) {
            CarbonServerAuthenticator carbonServerAuthenticator = (CarbonServerAuthenticator)authenticator;
            return carbonServerAuthenticator.isHandle(msgContext);
        } else {
            ServerAuthenticator serverAuthenticator = (ServerAuthenticator)authenticator;
            return serverAuthenticator.canHandle(msgContext);
        }

    }
}
