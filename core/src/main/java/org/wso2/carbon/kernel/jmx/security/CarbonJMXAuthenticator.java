/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.kernel.jmx.security;

import org.wso2.carbon.kernel.Constants;

import java.util.Collections;
import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXPrincipal;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

/**
 * Implementation class for JMXAuthenticator
 *
 * @since 5.1.0
 */
public class CarbonJMXAuthenticator implements JMXAuthenticator {

    @Override
    public Subject authenticate(Object credentials) {
        if (credentials == null) {
            throw new SecurityException("Credentials required");
        }

        if (!(credentials instanceof String[])) {
            throw new SecurityException("Credentials should be String[]");
        }

        CallbackHandler callbackHandler = new CarbonJMXCallbackHandler(credentials);
        try {
            LoginContext loginContext = new LoginContext(Constants.LOGIN_MODULE_ENTRY, callbackHandler);
            loginContext.login();
            return new Subject(true, Collections.singleton(new JMXPrincipal(((String[]) credentials)[0])),
                    Collections.EMPTY_SET, Collections.EMPTY_SET);
        } catch (LoginException e) {
            throw new SecurityException("Invalid credentials", e);
        }
    }
}
