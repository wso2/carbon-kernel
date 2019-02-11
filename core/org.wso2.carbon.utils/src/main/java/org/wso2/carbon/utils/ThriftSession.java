/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.utils;

import org.wso2.carbon.base.CarbonBaseUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;

public class ThriftSession implements Serializable {

    private static Log log = LogFactory.getLog(ThriftSession.class);

    private String sessionId;
    private long createdAt;
    private long lastAccessedAt;
    private String userName;
    private String password;
    private Map<String, Object> attributeMap = new HashMap<String, Object>();

    /*To store the allowed methods for security checks used in this class.*/
    private static final Map<String, String> ALLOWED_METHODS = new HashMap<String, String>();
    private final Map<String, Boolean> restrictedItems = new HashMap<String, Boolean>();
    // The CarbonContextHolder parameter is restricted. We have hardcoded the string here, since
    // we don't want to make this publicly available. See
    // CarbonContextHolder.CARBON_CONTEXT_HOLDER for more information.
    private static final String CARBON_CONTEXT_HOLDER = "carbonContextHolder";

    static {
        //currently only allow this method to get CarbonContextHolderBase from thrift session.
        ALLOWED_METHODS.put("org.wso2.carbon.identity.entitlement.thrift.ThriftEntitlementServiceImpl",
                           "populateCurrentCarbonContextFromAuthSession");
    }

    public ThriftSession(){
        restrictedItems.put(CARBON_CONTEXT_HOLDER, false);
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getLastAccess() {
        return lastAccessedAt;
    }

    public void setLastAccess(long lastAccess) {
        this.lastAccessedAt = lastAccess;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {

        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ThriftSession)) {
            return false;
        }

        ThriftSession that = (ThriftSession) o;

        if (sessionId != null ? !sessionId.equals(that.sessionId) : that.sessionId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return sessionId != null ? sessionId.hashCode() : 0;
    }

    public void setAttribute(String key, Object val) {
        checkRestrictedItem(key);
        attributeMap.put(key, val);
    }

    public Object getAttribute(String key) throws Exception {
        if (key.equals(CARBON_CONTEXT_HOLDER)) {
            String errorMsg = "Trying to retrieve a restricted item. Access Denied.";
            log.error(errorMsg);
            throw new Exception(errorMsg);
        } else {
            return attributeMap.get(key);
        }
    }

    /**
     * Separate method to retrieve carbon context holder since the attribute value is restricted.
     * @return
     */
    public CarbonContext getSessionCarbonContextHolder() {
        CarbonBaseUtils.checkSecurity(ALLOWED_METHODS);
        return (PrivilegedCarbonContext) attributeMap.get(CARBON_CONTEXT_HOLDER);
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
                                            + itemName);
            } else {
                restrictedItems.put(itemName, true);
            }
        }
    }
}
