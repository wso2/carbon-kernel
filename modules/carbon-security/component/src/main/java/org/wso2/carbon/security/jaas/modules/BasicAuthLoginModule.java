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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.context.api.PrivilegedCarbonContext;
import org.wso2.carbon.security.jaas.CarbonPrincipal;
import org.wso2.carbon.security.util.InMemoryUserStoreManager;

import java.io.IOException;
import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

/**
 * <p>
 * This LoginModule authenticates users against the underline UserStoreManager.
 * Upon successful authentication, <code>CarbonPrincipal</code> with user information is added to the subject.
 * This LoginModule does not recognize any options defined in the login configuration.
 * </p>
 */
public class BasicAuthLoginModule implements LoginModule {

    private static final Logger log = LoggerFactory.getLogger(BasicAuthLoginModule.class);
    private Subject subject;
    private String username;
    private char[] password;
    private CallbackHandler callbackHandler;
    private Map sharedState;
    private Map options;
    private boolean success = false;
    private boolean commitSuccess = false;
    private CarbonPrincipal carbonPrincipal;

    /**
     * <p>
     * This method determines whether the login module can handle the content of the CarbonCallback.
     * The method will extract the HttpRequest from the callback and check for basic authorization header.
     * @param carbonCallback CarbonCallback
     * @return true if the callback content can be handled, false if not.
     * @throws LoginException
     */

    /**
     * This method initializes the login module.
     *
     * @param subject
     * @param callbackHandler
     * @param sharedState
     * @param options
     */
    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
                           Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.sharedState = sharedState;
        this.options = options;
    }

    /**
     * This method authenticates a Subject (phase 1 )with the underlying <code>UserStoreManager</code>.
     * The result of the authentication attempt as private state within the LoginModule.
     *
     * @return true if the authentication is success.
     * @throws LoginException if the authentication fails.
     */
    @Override
    public boolean login() throws LoginException {

        NameCallback usernameCallback = new NameCallback("username");
        PasswordCallback passwordCallback = new PasswordCallback("password", false);
        Callback[] callbacks = {usernameCallback, passwordCallback};

        try {
            callbackHandler.handle(callbacks);
        } catch (IOException | UnsupportedCallbackException e) {
            log.error("Error while handling callbacks.", e);
            throw new LoginException("Error while handling callbacks.");
        }

        username = usernameCallback.getName();
        password = passwordCallback.getPassword();

        success = InMemoryUserStoreManager.getInstance().authenticate(username, password);
        return success;
    }

    /**
     * This method is called if the LoginContext's  overall authentication success.
     * <p/>
     * <p> If this LoginModule's own authentication attempt
     * success (checked by retrieving the private state saved by the <code>login</code> method), then this method
     * associates a <code>SamplePrincipal</code> with the <code>Subject</code> located in the
     * <code>LoginModule</code>.  If this LoginModule's own authentication attempted failed, then this method removes
     * any state that was originally saved.
     *
     * @return true if this LoginModule's own login and commit attempts success, or false otherwise.
     * @throws LoginException if the commit fails.
     */
    @Override
    public boolean commit() throws LoginException {

        if (!success) {
            commitSuccess = false;
        } else {
            // TODO username is set as role name temporally
            carbonPrincipal = new CarbonPrincipal(username);
            if (!subject.getPrincipals().contains(carbonPrincipal)) {
                subject.getPrincipals().add(carbonPrincipal);
            }

            PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            privilegedCarbonContext.setSubject(subject);

            username = null;
            for (int i = 0; i < password.length; i++) {
                password[i] = ' ';
            }
            password = null;

            commitSuccess = true;
        }
        return commitSuccess;
    }

    /**
     * This method is called if the LoginContext's overall authentication failed.
     * <p/>
     * <p> If this LoginModule's own authentication attempt success (checked by retrieving the private state saved
     * by the <code>login</code> and <code>commit</code> methods), then this method cleans up any state that was
     * originally saved.
     *
     * @return if this LoginModule's own login and/or commit attempts failed, and true otherwise.
     * @throws LoginException if the abort fails.
     */
    @Override
    public boolean abort() throws LoginException {

        if (!success) {
            return false;
        } else if (!commitSuccess) {
            // login success but overall authentication failed
            success = false;
            username = null;
            if (password != null) {
                for (int i = 0; i < password.length; i++) {
                    password[i] = ' ';
                }
                password = null;
            }
            carbonPrincipal = null;
        } else {
            // overall authentication success and commit success,
            // but someone else's commit failed
            logout();
        }
        return true;
    }

    /**
     * This method performs the user logout.
     * The principals set to the Subject and any state that was originally saved is cleared.
     *
     * @return true when the logout flow is success.
     * @throws LoginException if logout fails.
     */
    @Override
    public boolean logout() throws LoginException {

        subject.getPrincipals().remove(carbonPrincipal);
        success = false;
        commitSuccess = false;
        username = null;
        if (password != null) {
            for (int i = 0; i < password.length; i++) {
                password[i] = ' ';
            }
            password = null;
        }
        carbonPrincipal = null;
        return true;
    }

}
