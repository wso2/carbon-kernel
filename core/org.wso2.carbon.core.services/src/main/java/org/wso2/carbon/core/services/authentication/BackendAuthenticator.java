package org.wso2.carbon.core.services.authentication;

/**
 * This is a parent authenticator interface class. This is to make sure that new authenticator framework is
 * backward compatible.
 * TODO If possible in a future release get rid of this interface as well as CarbonServerAuthenticator.
 */
public interface BackendAuthenticator {

    /**
     * Gets the priority of this authenticator in the framework. Authenticators will be sorted against the priority.
     * Lower the priority, higher the precedence.
     * @return An integer expressing the priority.
     */
    int getPriority();

    /**
     * By default all the authenticators found in the system are enabled. Can use this property to
     * control default behavior.
     * @return <code>true</code> if authenticator is disabled else <code>false</code>.
     */
    boolean isDisabled();
}
