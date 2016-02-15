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

package org.wso2.carbon.security.jaas.modules;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.ReadOnlyJWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.context.api.PrivilegedCarbonContext;
import org.wso2.carbon.security.exception.CarbonSecurityException;
import org.wso2.carbon.security.jaas.CarbonCallback;
import org.wso2.carbon.security.jaas.CarbonPrincipal;
import org.wso2.carbon.security.util.CarbonSecurityConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;


/**
 * <p>
 * This LoginModule authenticates users with JWT tokens.
 * Upon successful authentication, <code>CarbonPrincipal</code> with user information is added to the subject.
 * This LoginModule does not recognize any options defined in the login configuration.
 * </p>
 */
public class JWTLoginModule implements LoginModule {

    private static final Logger log = LoggerFactory.getLogger(JWTLoginModule.class);
    private static final String ALIAS = "wso2carbon";
    private static final String KEYSTORE_PASSWORD = "wso2carbon";

    private Subject subject;
    private CallbackHandler callbackHandler;
    private Map<String, ?> sharedState;
    private Map<String, ?> options;
    private boolean succeeded;
    private boolean commitSucceeded;
    private SignedJWT signedJWT;
    private CarbonPrincipal carbonPrincipal;

    /**
     * <p>
     * This method determines whether the login module can handle the content of the CarbonCallback.
     * The method will extract the HttpRequest from the callback and check for a JWT.
     *
     * @param carbonCallback CarbonCallback
     * @return true if the callback content can be handled, false if not.
     */
    private boolean canHandle(CarbonCallback carbonCallback) {

        HttpRequest httpRequest = carbonCallback.getHttpRequest();
        try {
            requestPreProcessor(httpRequest);
        } catch (CarbonSecurityException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error while handling callbacks.", e);
            }
            return false;
        }

        return true;
    }

    /**
     * <p>
     * This method process the HTTP request and extracts signed JWT from the the HTTP header.
     * The JWT should be included with the 'Bearer' prefix in the authorization header.
     *
     * @param httpRequest HTTP request with authorization header.
     * @throws CarbonSecurityException
     */
    private void requestPreProcessor(HttpRequest httpRequest) throws CarbonSecurityException {

        if (httpRequest != null) {

            HttpHeaders headers = httpRequest.headers();
            if (headers != null) {
                String authorizationHeader = headers.get(HttpHeaders.Names.AUTHORIZATION);

                if (authorizationHeader != null && !authorizationHeader.isEmpty()) {

                    if (authorizationHeader.trim().startsWith(CarbonSecurityConstants
                                                                      .HTTP_AUTHORIZATION_PREFIX_BEARER)) {

                        String jwt = authorizationHeader.trim().split(" ")[1];

                        if (jwt != null && !jwt.trim().isEmpty()) {
                            try {
                                signedJWT = SignedJWT.parse(jwt);
                            } catch (ParseException e) {
                                signedJWT = null;
                                throw new CarbonSecurityException("Error while parsing the JWT token.", e);
                            }
                        } else {
                            throw new CarbonSecurityException("JWT token cannot be found in the authorization header.");
                        }
                    } else {
                        throw new CarbonSecurityException("Unsupported authorization header.");
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

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
                           Map<String, ?> options) {

        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.sharedState = sharedState;
        this.options = options;

    }

    @Override
    public boolean login() throws LoginException {

        Callback[] callbacks = new Callback[1];

        callbacks[0] = new CarbonCallback();

        try {
            callbackHandler.handle(callbacks);
        } catch (IOException | UnsupportedCallbackException e) {
            log.error("Error while handling callbacks.", e);
            throw new LoginException("Error while handling callbacks.");
        }

        if (!this.canHandle((CarbonCallback) callbacks[0])) {
            if (log.isDebugEnabled()) {
                log.debug("Unsupported callback.");
            }
            return false;
        }

        if (verifySignature(signedJWT)) {
            succeeded = true;
        } else {
            succeeded = false;
        }
        return succeeded;
    }

    @Override
    public boolean commit() throws LoginException {
        if (!succeeded) {
            commitSucceeded = false;
        } else {

            try {
                ReadOnlyJWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
                String username = claimsSet.getSubject();
                carbonPrincipal = new CarbonPrincipal(username);

                //TODO Populate the CarbonPrincipal with claims once the CarbonPrincipal class is finalized.

                if (!subject.getPrincipals().contains(carbonPrincipal)) {
                    subject.getPrincipals().add(carbonPrincipal);
                }

                PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                privilegedCarbonContext.setSubject(subject);

                commitSucceeded = true;
            } catch (ParseException e) {
                log.error("Error while retrieving claims from JWT Token", e);
                commitSucceeded = false;
            }
        }

        return commitSucceeded;
    }

    @Override
    public boolean abort() throws LoginException {
        if (!succeeded) {
            return false;
        } else if (!commitSucceeded) {
            // login success but overall authentication failed
            succeeded = false;
            signedJWT = null;
            carbonPrincipal = null;
        } else {
            // overall authentication success and commit success,
            // but someone else's commit failed
            logout();
        }
        return true;
    }

    @Override
    public boolean logout() throws LoginException {
        subject.getPrincipals().remove(carbonPrincipal);
        succeeded = false;
        commitSucceeded = false;
        signedJWT = null;
        carbonPrincipal = null;
        return true;
    }

    /**
     * <p>Verifies the signature of a signed JWT.
     *
     * @param signedJWT Signed JWT which needed to be verified.
     * @return true if the signature of the given JWT can is verified else false.
     */
    private boolean verifySignature(SignedJWT signedJWT) {
        try {

            if (signedJWT != null) {
                if (new Date().before(signedJWT.getJWTClaimsSet().getExpirationTime())) {
                    JWSVerifier verifier =
                            new RSASSAVerifier((RSAPublicKey) getPublicKey(getTrustStorePath(), KEYSTORE_PASSWORD,
                                                                           ALIAS));
                    return signedJWT.verify(verifier);
                } else {
                    log.warn("Token has expired.");
                }
            }
        } catch (ParseException | IOException | KeyStoreException | CertificateException |
                NoSuchAlgorithmException | UnrecoverableKeyException | JOSEException e) {
            log.error("Error occurred while JWT signature verification", e);
        }
        return false;
    }

    /**
     *
     * Returns public key from a certificate when provided key store path, key store password and certificate alias.
     *
     * @param keyStorePath Absolute path to the key store.
     * @param keyStorePassword Password of the key store.
     * @param alias Alias of the public key certificate that needed be extracted.
     * @return PublicKey extracted public key.
     * @throws IOException
     * @throws KeyStoreException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws UnrecoverableKeyException
     */

    private PublicKey getPublicKey(String keyStorePath, String keyStorePassword, String alias)
            throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException,
                   UnrecoverableKeyException {

        try (InputStream inputStream = new FileInputStream(keyStorePath)) {

            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(inputStream, keyStorePassword.toCharArray());
            Certificate cert = keystore.getCertificate(alias);

            return cert.getPublicKey();
        }
    }


    /**
     *
     * Retrieves the file path of the client trust store.
     *
     * @return String representing the trust store path.
     */
    private String getTrustStorePath() {
        //TODO Get the key store from a System Property or a util.
        String truststore = System.getProperty("carbon.home") + File.separator + "conf" + File.separator +
                            "data-bridge" + File.separator + "client-truststore.jks";
        return truststore;
    }
}
