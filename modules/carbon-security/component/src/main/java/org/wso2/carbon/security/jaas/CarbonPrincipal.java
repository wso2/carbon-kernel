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

import org.wso2.carbon.security.util.InMemoryUserStoreManager;

import java.io.Serializable;
import java.security.Principal;

/**
 * This class {@code CarbonPrincipal} is the principal representation of the carbon platform.
 * This is an implementation of {@code Principal}.
 */
public class CarbonPrincipal implements Principal, Serializable {

    private static final long serialVersionUID = 6056209529374720080L;

    private String name;

    public CarbonPrincipal() {

    }

    public CarbonPrincipal(String name) {

        this.name = name;
    }

    @Override
    public boolean equals(Object another) {

        if (another instanceof CarbonPrincipal) {
            //TODO add logic to compare
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {

        return 0;
    }

    @Override
    public String getName() {

        return this.name;
    }

    /**
     * Checks whether the current principal has a given {@code CarbonPermission}.
     * @param carbonPermission CarbonPermission which needs to be checked with principal instance.
     * @return true if authorized.
     */
    public boolean isAuthorized(CarbonPermission carbonPermission) {

        if (carbonPermission == null) {
            throw new IllegalArgumentException("Permission object cannot be null");
        }

        return (InMemoryUserStoreManager.getInstance().authorizePrincipal(this.getName(), carbonPermission));

    }


}
