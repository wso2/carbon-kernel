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

package org.apache.axis2.java.security;

import java.security.AccessControlContext;
import java.security.AccessControlException;
import java.security.Permission;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/**
 * This utility wrapper class is created to support AXIS2 runs
 * inside of Java 2 Security environment. Due to the access control
 * checking algorithm, for Java 2 Security to function properly,
 * <code>doPrivileged()</code>
 * is required in cases where there is application code on the stack frame
 * accessing the system resources (ie, read/write files, opening ports, and etc).
 * This class also improve performance no matther Security Manager is being enabled
 * or not.
 * <p/>
 * Note: This utility should be used properly, otherwise might introduce
 * security holes.
 * <p/>
 * Usage Example:
 * <code>
 * public void changePassword() {
 * ...
 * AccessController.doPrivileged(new PrivilegedAction() {
 * public Object run() {
 * f = Util.openPasswordFile();
 * ...
 * <p/>
 * }
 * });
 * ...
 * }
 * </code>
 */


public class AccessController {

    /**
     * Performs the specified <code>PrivilegedAction</code> with privileges
     * enabled if a security manager is present.
     * <p/>
     * If the action's <code>run</code> method throws an (unchecked) exception,
     * it will propagate through this method.
     *
     * @param action the action to be performed.
     * @return the value returned by the action's <code>run</code> method.
     * @see #doPrivileged(PrivilegedAction,AccessControlContext)
     * @see #doPrivileged(PrivilegedExceptionAction)
     */
    public static <T> T doPrivileged(PrivilegedAction<T> action) {
        SecurityManager sm = System.getSecurityManager();
        if (sm == null) {
            return (action.run());
        } else {
            return java.security.AccessController.doPrivileged(action);
        }
    }


    /**
     * Performs the specified <code>PrivilegedAction</code> with privileges
     * enabled and restricted by the specified <code>AccessControlContext</code>.
     * The action is performed with the intersection of the permissions
     * possessed by the caller's protection domain, and those possessed
     * by the domains represented by the specified
     * <code>AccessControlContext</code> if a security manager is present.
     * <p/>
     * <p/>
     * If the action's <code>run</code> method throws an (unchecked) exception,
     * it will propagate through this method.
     *
     * @param action  the action to be performed.
     * @param context an <i>access control context</i> representing the
     *                restriction to be applied to the caller's domain's
     *                privileges before performing the specified action.
     * @return the value returned by the action's <code>run</code> method.
     * @see #doPrivileged(PrivilegedAction)
     * @see #doPrivileged(PrivilegedExceptionAction,AccessControlContext)
     */
    public static <T> T doPrivileged(PrivilegedAction<T> action, AccessControlContext context) {
        SecurityManager sm = System.getSecurityManager();
        if (sm == null) {
            return action.run();
        } else {
            return java.security.AccessController.doPrivileged(action, context);
        }
    }

    /**
     * Performs the specified <code>PrivilegedExceptionAction</code> with
     * privileges enabled.  The action is performed with <i>all</i> of the
     * permissions possessed by the caller's protection domain.
     * <p/>
     * If the action's <code>run</code> method throws an <i>unchecked</i>
     * exception, it will propagate through this method.
     *
     * @param action the action to be performed.
     * @return the value returned by the action's <code>run</code> method.
     * @throws PrivilgedActionException the specified action's
     *                                  <code>run</code> method threw a <i>checked</i> exception.
     * @see #doPrivileged(PrivilegedExceptionAction,AccessControlContext)
     * @see #doPrivileged(PrivilegedAction)
     */
    public static <T> T doPrivileged(PrivilegedExceptionAction<T> action)
            throws PrivilegedActionException {
        SecurityManager sm = System.getSecurityManager();
        if (sm == null) {
            try {
                return action.run();
            } catch (java.lang.RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new PrivilegedActionException(e);
            }
        } else {
            return java.security.AccessController.doPrivileged(action);
        }
    }


    /**
     * Performs the specified <code>PrivilegedExceptionAction</code> with
     * privileges enabled and restricted by the specified
     * <code>AccessControlContext</code>.  The action is performed with the
     * intersection of the the permissions possessed by the caller's
     * protection domain, and those possessed by the domains represented by the
     * specified <code>AccessControlContext</code>.
     * <p/>
     * If the action's <code>run</code> method throws an <i>unchecked</i>
     * exception, it will propagate through this method.
     *
     * @param action  the action to be performed.
     * @param context an <i>access control context</i> representing the
     *                restriction to be applied to the caller's domain's
     *                privileges before performing the specified action.
     * @return the value returned by the action's <code>run</code> method.
     * @throws PrivilegedActionException the specified action's
     *                                   <code>run</code> method
     *                                   threw a <i>checked</i> exception.
     * @see #doPrivileged(PrivilegedAction)
     * @see #doPrivileged(PrivilegedExceptionAction,AccessControlContext)
     */
    public static <T> T doPrivileged(PrivilegedExceptionAction<T> action,
                                      AccessControlContext context)
            throws PrivilegedActionException {

        SecurityManager sm = System.getSecurityManager();
        if (sm == null) {
            try {
                return action.run();
            } catch (java.lang.RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new PrivilegedActionException(e);
            }
        } else {
            return java.security.AccessController.doPrivileged(action, context);
        }
    }

    /**
     * This method takes a "snapshot" of the current calling context, which
     * includes the current Thread's inherited AccessControlContext,
     * and places it in an AccessControlContext object. This context may then
     * be checked at a later point, possibly in another thread.
     *
     * @return the AccessControlContext based on the current context.
     * @see AccessControlContext
     */
    public static AccessControlContext getContext() {
        return java.security.AccessController.getContext();
    }

    /**
     * Determines whether the access request indicated by the
     * specified permission should be allowed or denied, based on
     * the security policy currently in effect.
     * This method quietly returns if the access request
     * is permitted, or throws a suitable AccessControlException otherwise.
     *
     * @param perm the requested permission.
     * @throws AccessControlException if the specified permission
     *                                is not permitted, based on the current security policy.
     */
    public static void checkPermission(Permission perm) throws AccessControlException {
        java.security.AccessController.checkPermission(perm);
    }

    /**
     * No instantiation allowed
     */
    private AccessController() {
    }
}
