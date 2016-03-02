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

import sun.security.provider.PolicyFile;

import javax.security.auth.Subject;
import java.net.URL;
import java.security.AccessController;
import java.security.Permission;
import java.security.Principal;
import java.security.ProtectionDomain;

/**
 * <p>
 * The class {@code CarbonPolicy} is the carbon specific extension of {@code PolicyFile}.
 * This class's {@code implies} method is overridden to specially handle {@code CarbonPermission} and uses the carbon
 * authorization implementation to check authorization.
 *
 */
public class CarbonPolicy extends PolicyFile {

    public CarbonPolicy()
    {
        super();
    }

    public CarbonPolicy(URL paramURL)
    {
        super(paramURL);
    }

    @Override
    public boolean implies(ProtectionDomain domain, Permission permission) {

        if (permission instanceof CarbonPermission) {

            // get the current subject.
            Subject subject = Subject.getSubject(AccessController.getContext());

            for (Principal principal : subject.getPrincipals()) {
                if (principal instanceof CarbonPrincipal) {
                    if (((CarbonPrincipal) principal).isAuthorized((CarbonPermission) permission)) {
                        return true;
                    }
                }
            }
        } else {
            return super.implies(domain, permission);
        }

        return false;
    }

}
