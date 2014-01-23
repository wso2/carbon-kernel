/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.ws.security.processor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSDocInfo;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSConfig;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.WSUsernameTokenPrincipal;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.message.token.UsernameToken;
import org.apache.ws.security.util.Base64;
import org.w3c.dom.Element;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;
import java.security.Principal;
import java.util.Vector;

public class UsernameTokenProcessor implements Processor {
    private static Log log = LogFactory.getLog(UsernameTokenProcessor.class.getName());

    private String utId;
    private UsernameToken ut;
    private boolean handleCustomPasswordTypes;
    private boolean allowNamespaceQualifiedPasswordTypes;
    private boolean passwordsAreEncoded;
    
    public void handleToken(Element elem, Crypto crypto, Crypto decCrypto, CallbackHandler cb, 
        WSDocInfo wsDocInfo, Vector returnResults, WSSConfig wsc) throws WSSecurityException {
        if (log.isDebugEnabled()) {
            log.debug("Found UsernameToken list element");
        }
        handleCustomPasswordTypes = wsc.getHandleCustomPasswordTypes();
        allowNamespaceQualifiedPasswordTypes = wsc.getAllowNamespaceQualifiedPasswordTypes();
        passwordsAreEncoded = wsc.getPasswordsAreEncoded();
        
        Principal lastPrincipalFound = handleUsernameToken((Element) elem, cb);
        returnResults.add(
            0, 
            new WSSecurityEngineResult(WSConstants.UT, lastPrincipalFound, null, null, null)
        );
        utId = ut.getID();
    }

    /**
     * Check the UsernameToken element. Depending on the password type
     * contained in the element the processing differs. If the password type
     * is digested, then retrieve a password from the callback handler and
     * authenticate the UsernameToken here.
     * <p/>
     * If the password is in plaintext or any other yet unknown password type
     * then delegate the password validation to the callback class. Note that for unknown
     * password types an exception is thrown if WSSConfig.getHandleCustomPasswordTypes()
     * is set to false (as it is by default). The security engine hands over all necessary
     * data to the callback class via the WSPasswordCallback object. The usage parameter of
     * WSPasswordCallback is set to <code>USERNAME_TOKEN_UNKNOWN</code>.
     *
     * @param token the DOM element that contains the UsernameToken
     * @param cb    the reference to the callback object
     * @return WSUsernameTokenPrincipal that contain data that an application
     *         may use to further validate the password/user combination.
     * @throws WSSecurityException
     */
    public WSUsernameTokenPrincipal handleUsernameToken(Element token, CallbackHandler cb) 
        throws WSSecurityException {
        if (cb == null) {
            throw new WSSecurityException(WSSecurityException.FAILURE, "noCallback");
        }
        //
        // Parse the UsernameToken element
        //
        ut = new UsernameToken(token, allowNamespaceQualifiedPasswordTypes);
        ut.setPasswordsAreEncoded(passwordsAreEncoded);
        String user = ut.getName();
        String password = ut.getPassword();
        String nonce = ut.getNonce();
        String createdTime = ut.getCreated();
        String pwType = ut.getPasswordType();
        if (log.isDebugEnabled()) {
            log.debug("UsernameToken user " + user);
            log.debug("UsernameToken password type " + pwType);
        }

        Callback[] callbacks = new Callback[1];
        String origPassword = null;
        
        //
        // If the UsernameToken is hashed, then retrieve the password from the callback handler 
        // and compare directly. If the UsernameToken is in plaintext or of some unknown type,
        // then delegate authentication to the callback handler
        //
        if (ut.isHashed()) {
            WSPasswordCallback pwCb = 
                new WSPasswordCallback(user, null, pwType, WSPasswordCallback.USERNAME_TOKEN);
            callbacks[0] = pwCb;
            try {
                cb.handle(callbacks);
            } catch (IOException e) {
                if (log.isDebugEnabled()) {
                    log.debug(e);
                }
                throw new WSSecurityException(
                    WSSecurityException.FAILED_AUTHENTICATION, null, null, e
                );
            } catch (UnsupportedCallbackException e) {
                if (log.isDebugEnabled()) {
                    log.debug(e);
                }
                throw new WSSecurityException(
                    WSSecurityException.FAILED_AUTHENTICATION, null, null, e
                );
            }
            origPassword = pwCb.getPassword();
            if (origPassword == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Callback supplied no password for: " + user);
                }
                throw new WSSecurityException(WSSecurityException.FAILED_AUTHENTICATION);
            }
            String passDigest;
            if (passwordsAreEncoded) {
                passDigest = UsernameToken.doPasswordDigest(nonce, createdTime, Base64.decode(origPassword));
            } else {
                passDigest = UsernameToken.doPasswordDigest(nonce, createdTime, origPassword);
            }
            if (!passDigest.equals(password)) {
                throw new WSSecurityException(WSSecurityException.FAILED_AUTHENTICATION);
            }
            ut.setRawPassword(origPassword);
        } else {
            if (!WSConstants.PASSWORD_TEXT.equals(pwType) 
                && pwType != null
                && !handleCustomPasswordTypes
            ) {
                if (log.isDebugEnabled()) {
                    log.debug("Authentication failed as handleCustomUsernameTokenTypes is false");
                }
                throw new WSSecurityException(WSSecurityException.FAILED_AUTHENTICATION);
            }
            WSPasswordCallback pwCb = new WSPasswordCallback(user, password,
                    pwType, WSPasswordCallback.USERNAME_TOKEN_UNKNOWN);
            callbacks[0] = pwCb;
            try {
                cb.handle(callbacks);
            } catch (IOException e) {
                if (log.isDebugEnabled()) {
                    log.debug(e);
                }
                throw new WSSecurityException(
                    WSSecurityException.FAILED_AUTHENTICATION, null, null, e
                );
            } catch (UnsupportedCallbackException e) {
                if (log.isDebugEnabled()) {
                    log.debug(e);
                }
                throw new WSSecurityException(
                    WSSecurityException.FAILED_AUTHENTICATION, null, null, e
                );
            }
            origPassword = pwCb.getPassword();
            ut.setRawPassword(origPassword);
        }
        WSUsernameTokenPrincipal principal = new WSUsernameTokenPrincipal(user, ut.isHashed());
        principal.setNonce(nonce);
        principal.setPassword(password);
        principal.setCreatedTime(createdTime);
        principal.setPasswordType(pwType);

        return principal;
    }

    /* (non-Javadoc)
     * @see org.apache.ws.security.processor.Processor#getId()
     */
    public String getId() {
        return utId;
    }

    /**
     * Get the processed UsernameToken.
     * 
     * @return the ut
     */
    public UsernameToken getUt() {
        return ut;
    }    
    
    public byte[] getDerivedKey(CallbackHandler cb) throws WSSecurityException {
        String password = ut.getRawPassword();
        if (password == null) {
            password = "";
        }
        byte[] saltValue = ut.getSalt();
        int iteration = ut.getIteration();
        if (passwordsAreEncoded) {
            return UsernameToken.generateDerivedKey(Base64.decode(password), saltValue, iteration);
        } else {
            return UsernameToken.generateDerivedKey(password, saltValue, iteration);
        }
    }
}
