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

package org.wso2.carbon.security;

import org.wso2.carbon.security.jaas.CarbonPermission;

import java.security.AccessControlException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import javax.security.auth.Subject;

/**
 * Carbon Security Manager
 */
public class CarbonSecurityManager {

    /**
     *
     * @param subject
     * @param carbonPermission
     * @return
     */
    public static boolean checkPermission(final Subject subject, final CarbonPermission carbonPermission) {

        final SecurityManager securityManager;

        if (System.getSecurityManager() == null) {
            securityManager = new SecurityManager();
        } else {
            securityManager = System.getSecurityManager();
        }

        try {
            Subject.doAsPrivileged(subject, (PrivilegedExceptionAction) () -> {
                securityManager.checkPermission(carbonPermission);
                return null;
            }, null);
            return true;
        } catch (AccessControlException ace) {
            return false;
        } catch (PrivilegedActionException pae) {
            return false;
        }
    }

}
