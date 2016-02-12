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

import org.wso2.carbon.security.util.UserStoreManager;

import java.security.AccessController;
import java.security.BasicPermission;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Principal;
import javax.security.auth.Subject;

/**
 * The {@code CarbonPermission} class is an extension of {@code BasicPermission}.
 * This is the permission representation used for principal based authorization in carbon environment.
 */
public class CarbonPermission extends BasicPermission {

    private static final long serialVersionUID = 6056209529374720070L;

    private String actions;

    public CarbonPermission(String name, String actions) {
        super(name);

        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("Permission name cannot be empty");
        }

        if (actions != null && !actions.isEmpty()) {
            this.actions = actions;
        }

    }

    //Never get called
    @Override
    public boolean implies(Permission permission) {

        // we do not need to worry about - if it is not a CarbonPermission
        if (!(permission instanceof CarbonPermission)) {
            return super.implies(permission);
        }

        // get the current subject.
        Subject subject = Subject.getSubject(AccessController.getContext());

        // find the CarbonPrincipal
        for (Principal principal : subject.getPrincipals()) {
            if (principal instanceof CarbonPrincipal) {
                if (UserStoreManager.getInstance().authorizePrincipal(principal.getName(),
                                                                          (CarbonPermission) permission)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String getActions() {
        return this.actions;
    }

    @Override
    public PermissionCollection newPermissionCollection() {
        return new CarbonPermissionCollection(this.getClass());
    }

}
