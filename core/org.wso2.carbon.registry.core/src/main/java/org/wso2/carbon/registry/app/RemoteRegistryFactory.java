/*
 * Copyright (c) 2007, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.registry.app;

import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryFactory;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Create a RemoteRegistry from some properties
 */
@Deprecated
public class RemoteRegistryFactory extends RegistryFactory {

    public static final String REMOTE_URL = "RemoteRegistry.url";

    String remoteURL;

    public RemoteRegistryFactory(java.util.Properties props) {
        // We MUST have a remote URL property for this factory to work
        remoteURL = props.getProperty(REMOTE_URL);
    }

    public Registry getRegistry() throws RegistryException {
        return getRegistry(null, null);
    }

    public Registry getRegistry(String username, String password) throws RegistryException {
        if (remoteURL == null) {
            // Here's where we could do things like check system properties, environment
            // vars, etc.
            throw new RegistryException("No remote URL!");
        }
        try {
            if (username == null) {
                return new RemoteRegistry(new URL(remoteURL));
            } else {
                return new RemoteRegistry(new URL(remoteURL), username, password);
            }
        } catch (MalformedURLException e) {
            throw new RegistryException("Bad remote URL '" + remoteURL + "'");
        }
    }
}
