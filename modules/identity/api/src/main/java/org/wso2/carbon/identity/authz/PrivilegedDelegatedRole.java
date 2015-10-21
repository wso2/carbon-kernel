/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.identity.authz;

import org.wso2.carbon.identity.authn.PrivilegedGroup;
import org.wso2.carbon.identity.authn.PrivilegedUser;
import org.wso2.carbon.identity.authz.spi.ReadOnlyAuthorizationStore;

import java.util.Date;

public class PrivilegedDelegatedRole<U extends PrivilegedUser, G extends PrivilegedGroup>
        extends PrivilegedRole<U, G> {

    private Date effectiveFrom;
    private Date effectiveUpTo;

    /**
     * @param authzStore
     * @param roleIdentifier
     * @param effectiveFrom
     * @param effectiveUpTo
     * @throws AuthorizationStoreException
     */
    public PrivilegedDelegatedRole(RoleIdentifier roleIdentifier,
                                   ReadOnlyAuthorizationStore authzStore, Date effectiveFrom,
                                   Date effectiveUpTo) throws AuthorizationStoreException {
        super(roleIdentifier, authzStore);
        this.effectiveFrom = effectiveFrom;
        this.effectiveUpTo = effectiveUpTo;
    }

    /**
     * @return
     */
    public Date getEffectiveUpTo() {
        return effectiveUpTo;
    }

    /**
     * @return
     */
    public Date getEffectiveFrom() {
        return effectiveFrom;
    }

}
