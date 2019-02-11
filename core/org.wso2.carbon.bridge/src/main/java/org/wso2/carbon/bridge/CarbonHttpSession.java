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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * The HttpSession request that is used withing Carbon. This is set in the
 * {@link org.wso2.carbon.bridge.BridgeServlet} so that Carbon can take control over how attributes
 * are set in the servlet API. This has been adopted so that ClassNotFoundExceptions &
 * ClassCastExceptions can be avoided when Carbon is deployed within App server such as WebLogic
 * which serializes HttpSession attributes & HttpRequest attributes.
 */
public class CarbonHttpSession implements HttpSession {

    private HttpSession delegate;
    private Map<String, Boolean> restrictedItems = new HashMap<String, Boolean>();

    public CarbonHttpSession(HttpSession delegate) {
        this.delegate = delegate;
        // The CarbonContextHolder parameter is restricted. We have hardcoded the string here, since
        // we don't want to make this publicly available. See
        // CarbonContextHolder.CARBON_CONTEXT_HOLDER for more information.
        restrictedItems.put("carbonContextHolder", false);
        restrictedItems.put("is.super.tenant", false); // MultitenantConstants.IS_SUPER_TENANT
        restrictedItems.put("javax.security.auth.subject", false);
    }

    /**
     * Overriden
     *
     * @param s Key
     * @return attribute
     */
    public Object getAttribute(String s) {
        Object attribute = delegate.getAttribute(s);
        if (attribute instanceof CarbonAttributeWrapper) {
            return ((CarbonAttributeWrapper) attribute).getObject();
        }
        return attribute;
    }

    /**
     * Overriden
     *
     * @param s key
     * @return value
     */
    public Object getValue(String s) {
        Object attribute = delegate.getAttribute(s);
        if (attribute instanceof CarbonAttributeWrapper) {
            return ((CarbonAttributeWrapper) attribute).getObject();
        }
        return attribute;
    }

    /**
     * Overriden
     *
     * @param name key
     * @param value attribute
     */
    public void setAttribute(String name, Object value) {
        checkRestrictedItem(name);
        if (name.equals("javax.security.auth.subject")) {
            delegate.setAttribute(name, value);
        } else {
            delegate.setAttribute(name, new CarbonAttributeWrapper(value));
        }
    }

    /**
     * Overriden
     *
     * @param s key
     * @param o value
     */
    public void putValue(String s, Object o) {
        delegate.putValue(s, new CarbonAttributeWrapper(o));
    }

    public void invalidate() {
        delegate.invalidate();
    }

    // ---------------------------------------------------------------------------------------------

    public void removeAttribute(String s) {
        delegate.removeAttribute(s);
    }

    public void removeValue(String s) {
        delegate.removeValue(s);
    }

    public long getCreationTime() {
        return delegate.getCreationTime();
    }

    public String getId() {
        return delegate.getId();
    }

    public long getLastAccessedTime() {
        return delegate.getLastAccessedTime();
    }

    public ServletContext getServletContext() {
        return delegate.getServletContext();
    }

    public void setMaxInactiveInterval(int i) {
        delegate.setMaxInactiveInterval(i);
    }

    public int getMaxInactiveInterval() {
        return delegate.getMaxInactiveInterval();
    }

    public HttpSessionContext getSessionContext() {
        return delegate.getSessionContext();
    }

    public Enumeration getAttributeNames() {
        return delegate.getAttributeNames();
    }

    public String[] getValueNames() {
        return delegate.getValueNames();
    }

    public boolean isNew() {
        return delegate.isNew();
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
