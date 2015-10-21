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
package org.wso2.carbon.identity.carbon;

import org.wso2.carbon.identity.carbon.commons.EntityTree;
import org.wso2.carbon.identity.carbon.commons.IdentityException;
import org.wso2.carbon.identity.carbon.identifiers.EntryIdentifier;
import org.wso2.carbon.identity.carbon.identifiers.RoleIdentifier;
import org.wso2.carbon.identity.carbon.identifiers.StoreIdentifier;

import java.util.List;

public interface CarbonRole<U extends CarbonUser, G extends CarbonGroup>
        extends Role {

    /**
     * @return
     */
    public EntryIdentifier getRoleEntryId();

    /**
     * @return
     * @throws IdentityException
     */
    public List<CarbonPermission> getPermissions() throws IdentityException;

    /**
     * @return
     * @throws IdentityException
     */
    public List<G> getGroups() throws IdentityException;

    /**
     * @return
     * @throws IdentityException
     */
    public List<U> getUsers() throws IdentityException;

    /**
     * @return
     * @throws IdentityException
     */
    public List<EntityTree> getChildren() throws IdentityException;

    /**
     * @param childRoleIdentifier
     * @return
     * @throws IdentityException
     */
    public boolean hasChild(RoleIdentifier childRoleIdentifier)
            throws IdentityException;

    /**
     * @param parentRoleIdentifier
     * @return
     * @throws IdentityException
     */
    public boolean hasParent(RoleIdentifier parentRoleIdentifier)
            throws IdentityException;

    /**
     * @return
     */
    public StoreIdentifier getStoreIdentifier();

}
