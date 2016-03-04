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

import com.nimbusds.jwt.SignedJWT;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.security.exception.CarbonSecurityException;
import org.wso2.carbon.security.util.CarbonSecurityConstants;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Base64;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * The class {@code CarbonCallbackHandler} is an implementation {@code CarbonCallbackHandler}.
 * This callback handler is used for handling {@code CarbonCallback} type callbacks.
 */
public class CarbonCallbackHandler implements CallbackHandler {

    private static final Logger log = LoggerFactory.getLogger(CarbonCallbackHandler.class);

    private HttpRequest httpRequest;

    private boolean preProcessed;

    private String username;

    private char[] password;

    private SignedJWT singedJWT;


    public CarbonCallbackHandler(HttpRequest httpRequest) {

        this.httpRequest = httpRequest;
    }

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {

        if (callbacks != null) {
            preProcessed = false;
            for (Callback callback : callbacks) {
                if (callback instanceof NameCallback) {
                    if (!preProcessed) {
                        try {
                            preProcessRequest(CarbonCallback.Type.BASIC_AUTH);
                            preProcessed = true;
                        } catch (CarbonSecurityException e) {
                            if (log.isDebugEnabled()) {
                                log.debug(e.getMessage(), e);
                            }
                            throw new UnsupportedCallbackException(callback);
                        }
                    }
                    ((NameCallback) callback).setName(username);

                } else if (callback instanceof PasswordCallback) {
                    if (!preProcessed) {
                        try {
                            preProcessRequest(CarbonCallback.Type.BASIC_AUTH);
                            preProcessed = true;
                        } catch (CarbonSecurityException e) {
                            if (log.isDebugEnabled()) {
                                log.debug(e.getMessage(), e);
                            }
                            throw new UnsupportedCallbackException(callback);
                        }
                    }
                    ((PasswordCallback) callback).setPassword(password);

                } else if (callback instanceof CarbonCallback) {
                    CarbonCallback carbonCallback = ((CarbonCallback) callback);
                    try {
                        preProcessRequest(carbonCallback.getType());
                    } catch (CarbonSecurityException e) {
                        if (log.isDebugEnabled()) {
                            log.debug(e.getMessage(), e);
                        }
                        throw new UnsupportedCallbackException(callback);
                    }

                    if (CarbonCallback.Type.JWT.equals(carbonCallback.getType())) {
                        ((CarbonCallback) callback).setContent(singedJWT);
                    }

                } else {
                    throw new UnsupportedCallbackException(callback);
                }
            }
            clearCredentials();
        }
    }

    private void preProcessRequest(CarbonCallback.Type type) throws CarbonSecurityException {

        if (httpRequest != null) {

            HttpHeaders headers = httpRequest.headers();
            if (headers != null) {

                String authorizationHeader = headers.get(HttpHeaders.Names.AUTHORIZATION);
                if (authorizationHeader != null && !authorizationHeader.isEmpty()) {

                    if (CarbonCallback.Type.BASIC_AUTH.equals(type)) {
                        if (authorizationHeader.trim().startsWith(CarbonSecurityConstants.HTTP_AUTHORIZATION_PREFIX_BASIC)) {

                            String credentials = authorizationHeader.trim().split(" ")[1];
                            byte[] decodedByte = credentials.getBytes(Charset.forName(StandardCharsets.UTF_8.name()));
                            String authDecoded = new String(Base64.getDecoder().decode(decodedByte),
                                                            Charset.forName(StandardCharsets.UTF_8.name()));
                            String[] authParts = authDecoded.split(":");
                            if (authParts.length == 2) {
                                username = authParts[0];
                                password = authParts[1].toCharArray();
                            } else {
                                throw new CarbonSecurityException("Invalid authorization header.");
                            }
                        } else {
                            throw new CarbonSecurityException("Basic authorization header cannot be found.");
                        }

                    } else if (CarbonCallback.Type.JWT.equals(type)) {
                        if (authorizationHeader.trim().startsWith(CarbonSecurityConstants
                                                                          .HTTP_AUTHORIZATION_PREFIX_BEARER)) {

                            String jwt = authorizationHeader.trim().split(" ")[1];

                            if (jwt != null && !jwt.trim().isEmpty()) {
                                try {
                                    singedJWT = SignedJWT.parse(jwt);
                                } catch (ParseException e) {
                                    throw new CarbonSecurityException("Error while parsing the JWT token.", e);
                                }
                            } else {
                                throw new CarbonSecurityException("JWT token cannot be found in the authorization header.");
                            }
                        } else {
                            throw new CarbonSecurityException("Bearer authorization header cannot be found.");
                        }
                    }
                } else {
                    throw new CarbonSecurityException("Authorization header cannot be found in the request.");
                }
            } else {
                throw new CarbonSecurityException("HTTP headers cannot be found in the request.");
            }
        } else {
            throw new CarbonSecurityException("HTTP request cannot be found.");
        }
    }

    private void clearCredentials() {
        username = null;
        if (password != null) {
            for (int i = 0; i < password.length; i++) {
                password[i] = ' ';
            }
            password = null;
        }
    }

}
