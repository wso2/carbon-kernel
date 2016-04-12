/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.kernel.context;


import org.wso2.carbon.kernel.internal.context.CarbonContextHolder;
import org.wso2.carbon.kernel.utils.Utils;

import java.security.Principal;

/**
 * This CarbonContext provides users the ability to carry out privileged actions such as setting user principal,
 * properties which are needed at thread local level. It also exposes a privileged action to destroy the current
 * context which is to remove the current carbon context instance stored at thread local space.
 *
 * @since 5.1.0
 */

public final class PrivilegedCarbonContext extends CarbonContext {

    private PrivilegedCarbonContext(CarbonContextHolder carbonContextHolder) {
        super(carbonContextHolder);
    }

    /**
     * Returns the carbon context instance which is stored at current thread local space.
     *
     * @return the carbon context instance.
     */
    public static PrivilegedCarbonContext getCurrentContext() {
        Utils.checkSecurity();
        return new PrivilegedCarbonContext(CarbonContextHolder.getCurrentContextHolder());
    }

    /**
     * Destroys the current carbon context instance by removing it from thread local space.
     */
    public static void destroyCurrentContext() {
        Utils.checkSecurity();
        getCurrentContext().getCarbonContextHolder().destroyCurrentCarbonContextHolder();
    }

    /**
     * Method to set the given JAAS principal object to current carbon context instance. This will throw a
     * IllegalStateException if a thread is trying to override currently set principal instance with the different
     * instance.
     *
     * @param userPrincipal the jaas principal object to be set.
     */
    public void setUserPrincipal(Principal userPrincipal) {
        Utils.checkSecurity();
        getCarbonContextHolder().setUserPrincipal(userPrincipal);
    }

    /**
     * Method to set key, value pair as properties with carbon context instance. The stored properties can be
     * replaced with new values by using the same property name.
     *
     * @param name the name of property to be set.
     * @param value the value of the property to be set.
     */
    public void setProperty(String name, Object value) {
        Utils.checkSecurity();
        getCarbonContextHolder().setProperty(name, value);
    }
}
