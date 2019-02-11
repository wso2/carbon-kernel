/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
package org.wso2.carbon.bridge;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * The HttpServlet request that is used withing Carbon. This is set in the
 * {@link org.wso2.carbon.bridge.BridgeServlet} so that Carbon can take control over how attributes
 * are set in the servlet API. This has been adopted so that ClassNotFoundExceptions &
 * ClassCastExceptions can be avoided when Carbon is deployed within App server such as WebLogic
 * which serializes HttpSession attributes & HttpRequest attributes.
 */
public class CarbonHttpServletRequest extends HttpServletRequestWrapper {

    private HttpServletRequest request;
    private Map<String, Boolean> restrictedItems = new HashMap<String, Boolean>();
    
    /**
     * Constructs a request object wrapping the given request.
     *
     * @param request The original HttpRequest
     * @throws IllegalArgumentException if the request is null
     */
    public CarbonHttpServletRequest(HttpServletRequest request) {
        super(request);
        this.request = request;
        restrictedItems.put("tenantDomain", false); // MultitenantConstants.TENANT_DOMAIN
    }

    @Override
    public void setAttribute(String name, Object value) {
        checkRestrictedItem(name);
        if (value == null) {
            super.setAttribute(name, null);
            return;
        }
        if (value.getClass().getName().equals("java.util.Stack") ||
            (!value.getClass().getName().startsWith("javax.") &&
             !value.getClass().getName().startsWith("java.") &&
             !value.getClass().getName().startsWith("[Ljava.")) &&
            !(value instanceof CarbonAttributeWrapper)) {
            value = new CarbonAttributeWrapper(value);
        }
        super.setAttribute(name, value);
    }

    @Override
    public Object getAttribute(String s) {
        Object attribute = super.getAttribute(s);
        if (attribute instanceof CarbonAttributeWrapper) {
            attribute = ((CarbonAttributeWrapper) attribute).getObject();
        }
        return attribute;
    }

    /**
     * Returns the current HttpSession  associated with this request or, if there is no
     * current session and create is true, returns a new session.
     * @param create True if a new session needs to be created
     * @return The current HttpSession  associated with this request or, if there is no
     * current session and create is true, returns a new session.
     */
    public HttpSession getSession(boolean create) {
        CarbonHttpSession wrappedSession = null;

        // The line below may result in creation of an HttpSession at which point the
        // HttpSessionManager will get called byt the servlet container
        HttpSession originalSession = request.getSession(create);
        if (originalSession != null) {
            wrappedSession = HttpSessionManager.getSession(originalSession.getId());
        }
        return wrappedSession;
    }

    /**
     * Returns the current session associated with this request, or if the request does not have
     * a session, creates one.
     *
     * @return The current session associated with this request, or if the request does not have
     * a session, creates one.
     */
    public HttpSession getSession() {
        CarbonHttpSession wrappedSession = null;

        // The line below may result in creation of an HttpSession at which point the
        // HttpSessionManager will get called byt the servlet container
        HttpSession originalSession = request.getSession();
        if (originalSession != null) {
            wrappedSession = HttpSessionManager.getSession(originalSession.getId());
        }
        return wrappedSession;
    }

    /**
     * Converts non UTF-8 parameters, into UTF-8, if the Character Encoding has not been set.
     *
     * @return The proper parameter value.
     */
    public String getParameter(String name) {
        if (request.getCharacterEncoding() == null ||
                request.getCharacterEncoding().toUpperCase().equals("ISO-8859-1")) {
            // The default content type ISO-8859-1 is used in this case.
            String result = request.getParameter(name);
            if (result == null) {
                // Even though the result is null, the wrapper might return a valid value.
                // This violates [1], but we have assumed this behaviour in our UI.
                //
                // [1] http://java.sun.com/webservices/docs/1.6/api/javax/servlet/ServletRequestWrapper.html#getParameter(java.lang.String)
                return super.getParameter(name);
            } else if (request instanceof CarbonHttpServletRequest) {
                // Handle recursive calls and null valued parameters.
                return result;
            } else {
                try {
                    return new String(result.getBytes("ISO-8859-1"), "UTF-8");
                } catch (UnsupportedEncodingException ignore) {
                    // If an exception occurred, don't do the charset conversion.
                    return result;
                }
            }
        }
        return super.getParameter(name);
    }


    /**
     * Restrict setting of critical items so that they can be set only once.
     *
     * @param itemName The item that needs to be checked
     */
    private void checkRestrictedItem(String itemName) {
        if (restrictedItems.containsKey(itemName)) {
            Boolean isSet = restrictedItems.get(itemName);
            if (isSet) {
                throw new SecurityException("Malicious code detected! Trying to override restricted item: "
                                            + itemName + ". An incident has been logged for tenant " +
                                            getAttribute("tenantDomain"));
            } else {
                restrictedItems.put(itemName, true);
            }
        }
    }
}
