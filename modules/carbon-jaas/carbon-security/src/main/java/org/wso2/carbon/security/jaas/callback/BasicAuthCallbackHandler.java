/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.security.jaas.callback;

import java.io.IOException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * <p>Callback handler for Basic Auth login.
 */
public class BasicAuthCallbackHandler implements CallbackHandler {

    private String username;
    private char[] password;

    public BasicAuthCallbackHandler() {

    }

    public BasicAuthCallbackHandler(String username, char[] password) {
        this.username = username;
        this.password = password.clone();
    }

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {

        if (callbacks != null) {
            for (Callback callback : callbacks) {
                if (callback instanceof NameCallback) {
                    ((NameCallback) callback).setName(username);

                } else if (callback instanceof PasswordCallback) {
                    ((PasswordCallback) callback).setPassword(password.clone());

                } else {
                    throw new UnsupportedCallbackException(callback, "Unsupported Callback");

                }
            }
        }
    }
}
