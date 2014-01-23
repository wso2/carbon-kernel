/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.java.security.less;

import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.java.security.interf.Actor;

import java.security.AccessControlContext;
import java.security.PrivilegedAction;


/**
 * LessPermission has no read permission to the private.txt file
 */

public class LessPermissionAccessControlContext implements Actor {

    private Actor _actor;
    private boolean _usingDoPrivilege;

    // Construtor
    public LessPermissionAccessControlContext(Actor a, boolean usingDoPrivilege) {
        _actor = a;
        _usingDoPrivilege = usingDoPrivilege;
    }

    // Implement Actor's takeAction method
    public void takeAction() {
        try {
            if (_usingDoPrivilege) {
                final AccessControlContext acc = AccessController.getContext();
                // Demostrate the usage of AccessController.doPrivileged(PrivilegeAction action, AccessContext ctx)
                AccessController.doPrivileged(
                        new PrivilegedAction() {
                            public Object run() {
                                _actor.takeAction();
                                return null;
                            }
                        }, acc);
            } else {
                // Use no doPrivileged
                _actor.takeAction();
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
}

