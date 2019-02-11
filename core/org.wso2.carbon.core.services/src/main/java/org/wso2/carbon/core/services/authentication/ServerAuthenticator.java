package org.wso2.carbon.core.services.authentication;

import org.apache.axis2.context.MessageContext;


/**
 * This defines a framework for authenticators. Developers can implement this interface and write their own
 * method of authentication. The carbon framework will pick authenticators as a chain and will sort according to their
 * priority. Then it will check whether each authenticator is capable handling the request by calling
 * {@link #canHandle(org.apache.axis2.context.MessageContext)}. If an authenticator implementation is capable of
 * handling a request, framework will call {@link #isAuthenticated(org.apache.axis2.context.MessageContext)} method.
 * If user is already is authenticated the authenticator should return <code>true</code>. Additionally, an authenticator
 * should implement {@link #authenticate(org.apache.axis2.context.MessageContext)} method. The implementing method
 * should have logic to actually check whether user is legitimate.
 */
public interface ServerAuthenticator extends BackendAuthenticator {

    static String CONTINUE_PROCESSING = "org.wso2.carbon.core.services.authentication.continue";

    /**
     * Does this authenticator can handle the given request.
     * @param msgContext The request as a MessageContext.
     * @return <code>true</code> if this Authenticator is capable of handling the request, else <code>false</code>.
     */
    boolean canHandle(MessageContext msgContext);

    /**
     * Checks whether user is already authenticated.
     * @param msgContext The request as a MessageContext.
     * @return <code>true</code> if user is already authenticated else <code>false</code>.
     */
    boolean isAuthenticated(MessageContext msgContext);

    /**
     * This method should implement logic to authenticate a user. i.e. checking whether user name and password
     * are correct or whether given token is correct etc ...
     * @param msgContext The incoming request as a message context.
     * @throws AuthenticationFailureException If authentication failed.
     */
    void authenticate(MessageContext msgContext) throws AuthenticationFailureException;

    /**
     * Get the name of the Authenticator.
     * @return The name of the Authenticator.
     */
    String getAuthenticatorName();


}
