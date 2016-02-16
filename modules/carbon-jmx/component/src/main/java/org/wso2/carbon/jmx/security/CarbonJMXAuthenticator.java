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
package org.wso2.carbon.jmx.security;

import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXPrincipal;
import javax.security.auth.Subject;
import java.util.Collections;

public class CarbonJMXAuthenticator implements JMXAuthenticator {

    @Override
    public Subject authenticate(Object credentials) {
        if (!(credentials instanceof String[])) {
            // Special case for null so we get a more informative message
            if (credentials == null) {
                throw new SecurityException("Credentials required");
            }
            throw new SecurityException("Credentials should be String[]");
        }

        // TODO : Implement proper authentication
        final String[] aCredentials = (String[]) credentials;
        if (aCredentials.length < 2) {
            throw new SecurityException("Credentials should have at least username & password");
        }

        String userName = aCredentials[0];
        String password = aCredentials[1];

        if ("admin".equals(userName) && "password".equals(password)) {
            return new Subject(true,
                    Collections.singleton(new JMXPrincipal(userName)),
                    Collections.EMPTY_SET,
                    Collections.EMPTY_SET);
        } else {
            throw new SecurityException("Invalid credentials");
        }
    }
}
