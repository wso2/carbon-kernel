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

package org.wso2.carbon.security.jaas;

import io.netty.handler.codec.http.HttpRequest;

import java.io.Serializable;
import javax.security.auth.callback.Callback;

/**
 * <p>
 * The {@code Callback} isn an implementation of the {@code Callback} class.
 * Underlying security services instantiate and pass a
 * {@code NameCallback} to the {@code handle}
 * method of a {@code CallbackHandler} to retrieve HttpRequest information.
 * </p>
 */
public class CarbonCallback implements Callback, Serializable {

    private static final long serialVersionUID = 6056209529374750070L;

    private transient HttpRequest httpRequest;

    public HttpRequest getHttpRequest() {

        return httpRequest;
    }

    public void setHttpRequest(HttpRequest httpRequest) {

        this.httpRequest = httpRequest;
    }
}
