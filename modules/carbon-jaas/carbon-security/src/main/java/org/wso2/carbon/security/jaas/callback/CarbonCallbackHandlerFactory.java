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

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import org.wso2.carbon.security.exception.CarbonSecurityException;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.security.auth.callback.CallbackHandler;

/**
 * <p> CarbonCallbackHandlerFactory class can be used to obtain an application specific CallbackHandler.
 */
public class CarbonCallbackHandlerFactory {

    private static final String AUTH_TYPE_BASIC = "Basic";
    public static final String BASIC_AUTH_CREDENTIALS_SEPARATOR = ":";
    private static final int AUTH_TYPE_BASIC_LENGTH = AUTH_TYPE_BASIC.length();

    /**
     * Returns a CallbackHandler that can handle the request type.
     *
     * @param request Object
     * @return callbackHandler
     * @throws CarbonSecurityException
     */
    public static CallbackHandler getCallbackHandler(Object request) throws CarbonSecurityException {

        if (request == null) {
            return null;

        } else if (request instanceof HttpRequest) {
            return buildBasicAuthCallbackHandler((HttpRequest) request);

        }

        return null;
    }

    /**
     * Returns a BasicAuthCallbackHandler including Basic credentials from the HttpRequest header.
     *
     * @param request HttpRequest with credentials.
     * @return buildBasicAuthCallbackHandler
     * @throws CarbonSecurityException
     */
    private static BasicAuthCallbackHandler buildBasicAuthCallbackHandler(HttpRequest request)
            throws CarbonSecurityException {

        HttpHeaders headers = request.headers();

        if (headers != null) {
            String authHeader = headers.get(HttpHeaders.Names.AUTHORIZATION);

            if (authHeader != null) {
                String authType = authHeader.substring(0, AUTH_TYPE_BASIC_LENGTH);
                String authEncoded = authHeader.substring(AUTH_TYPE_BASIC_LENGTH).trim();

                if (AUTH_TYPE_BASIC.equals(authType) && !authEncoded.isEmpty()) {
                    byte[] decodedByte = authEncoded.getBytes(Charset.forName(StandardCharsets.UTF_8.name()));
                    String authDecoded = new String(Base64.getDecoder().decode(decodedByte),
                                                    Charset.forName(StandardCharsets.UTF_8.name()));
                    String[] authParts = authDecoded.split(BASIC_AUTH_CREDENTIALS_SEPARATOR);

                    String username = authParts[0];
                    char[] password;

                    if (authParts[1] != null && !authParts[1].isEmpty()) {
                        password = authParts[1].toCharArray();
                    } else {
                        password = new char[0];
                    }

                    return new BasicAuthCallbackHandler(username, password);
                }
            }
        }

        throw new CarbonSecurityException("Unable to extract user credentials");
    }


}
