/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.carbon.jndi.osgi.utils;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleReference;

/**
 *  This DummyBundleClassLoader is used to test the code which retrieves the caller's BundleContext from the
 *  Thread Context ClassLoader.
 */
public class DummyBundleClassLoader extends ClassLoader implements BundleReference {

    private Bundle bundle;
    private boolean getBundleMethodInvoked;

    public DummyBundleClassLoader(ClassLoader parent, Bundle bundle) {
        super(parent);
        this.bundle = bundle;
    }

    @Override
    public Bundle getBundle() {
        getBundleMethodInvoked = true;
        return bundle;
    }

    public boolean isGetBundleMethodInvoked() {
        return getBundleMethodInvoked;
    }
}