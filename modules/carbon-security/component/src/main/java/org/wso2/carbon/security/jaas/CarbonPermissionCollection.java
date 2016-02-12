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

import java.security.BasicPermission;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class CarbonPermissionCollection extends PermissionCollection implements java.io.Serializable {

    private static final long serialVersionUID = 739301742472979398L;

    private transient Map<String, Permission> perms;

    private boolean all_allowed;

    private Class<?> permClass;

    public CarbonPermissionCollection(Class<?> clazz) {
        perms = new HashMap<String, Permission>(11);
        all_allowed = false;
        permClass = clazz;
    }

    @Override
    public void add(Permission permission) {
        if (!(permission instanceof BasicPermission)) {
            throw new IllegalArgumentException("invalid permission: " +
                                               permission);
        }
        if (isReadOnly()) {
            throw new SecurityException("attempt to add a Permission to a readonly PermissionCollection");
        }

        BasicPermission bp = (BasicPermission) permission;

        // make sure we only add new BasicPermissions of the same class
        // Also check null for compatibility with deserialized form from
        // previous versions.
        if (permClass == null) {
            // adding first permission
            permClass = bp.getClass();
        } else {
            if (bp.getClass() != permClass) {
                throw new IllegalArgumentException("invalid permission: " +
                                                   permission);
            }
        }

        synchronized (this) {
            perms.put(bp.getName(), permission);
        }

        // No sync on all_allowed; staleness OK
        if (!all_allowed) {
            if (bp.getName().equals("*")) {
                all_allowed = true;
            }
        }
    }

    @Override
    public boolean implies(Permission permission) {
        if (!(permission instanceof BasicPermission)) {
            return false;
        }

        BasicPermission bp = (BasicPermission) permission;

        // random subclasses of BasicPermission do not imply each other
        if (bp.getClass() != permClass) {
            return false;
        }

        // short circuit if the "*" Permission was added
        if (all_allowed) {
            return true;
        }

        // strategy:
        // Check for full match first. Then work our way up the
        // path looking for matches on a.b..*

        String path = bp.getName();
        //System.out.println("check "+path);

        Permission x;

        synchronized (this) {
            x = perms.get(path);
        }

        if (x != null) {
            // we have a direct hit!
            return x.implies(permission);
        }

        // work our way up the tree...
        int last, offset;

        offset = path.length() - 1;

        while ((last = path.lastIndexOf(".", offset)) != -1) {

            path = path.substring(0, last + 1) + "*";
            //System.out.println("check "+path);

            synchronized (this) {
                x = perms.get(path);
            }

            if (x != null) {
                return x.implies(permission);
            }
            offset = last - 1;
        }

        // we don't have to check for "*" as it was already checked
        // at the top (all_allowed), so we just return false
        return false;
    }

    @Override
    public Enumeration<Permission> elements() {
        // Convert Iterator of Map values into an Enumeration
        synchronized (this) {
            return Collections.enumeration(perms.values());
        }
    }
}
